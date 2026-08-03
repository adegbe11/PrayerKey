package com.prayerkey.manna.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.UserPrefs
import com.prayerkey.manna.data.SermonSession
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.GeneratedPrayer
import com.prayerkey.manna.model.VerseCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PRAYING_MOOD = "\uD83D\uDE4F"

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = MannaStore(application)

    /** The app must NEVER die on a cold start because of a storage
     *  problem — every read falls back to an empty default. */
    private fun <T> safe(default: T, read: () -> T): T = runCatching(read).getOrDefault(default)

    private val _saved = MutableStateFlow(emptyList<SavedWord>())
    val saved = _saved.asStateFlow()
    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()
    private val _memory = MutableStateFlow(emptyList<MemoryVerse>())
    val memory = _memory.asStateFlow()
    private val _preferences = MutableStateFlow(UserPrefs())
    val preferences = _preferences.asStateFlow()
    private val _sermons = MutableStateFlow(emptyList<SermonSession>())
    val sermons = _sermons.asStateFlow()
    private val _journal = MutableStateFlow(emptyList<JournalPrayer>())
    val journal = _journal.asStateFlow()
    private val _entries = MutableStateFlow(emptyList<JournalEntry>())
    val entries = _entries.asStateFlow()
    private val _journalStreak = MutableStateFlow(0)
    val journalStreak = _journalStreak.asStateFlow()
    private val _sermonNotes = MutableStateFlow(emptyList<com.prayerkey.manna.data.SermonNote>())
    val sermonNotes = _sermonNotes.asStateFlow()
    private val _formation = MutableStateFlow(com.prayerkey.manna.data.FormationState())
    val formation = _formation.asStateFlow()

    /** False until the first disk read lands. The UI shows a branded hold
     *  instead of guessing — otherwise a returning user would flash the
     *  onboarding screen before their saved name arrives. */
    private val _hydrated = MutableStateFlow(false)
    val hydrated = _hydrated.asStateFlow()

    init {
        // PERF: the constructor touches no disk. Opening the database and
        // running eight queries on the main thread was costing >15s on a
        // cold start and tripping "failed to complete startup" ANRs.
        viewModelScope.launch(Dispatchers.IO) {
            val saved = safe(emptyList<SavedWord>()) { store.all() }
            val streak = safe(0) { store.streak() }
            val memory = safe(emptyList<MemoryVerse>()) { store.memoryVerses() }
            val prefs = safe(UserPrefs()) { store.preferences() }
            val sermons = safe(emptyList<SermonSession>()) { store.sermons() }
            val journal = safe(emptyList<JournalPrayer>()) { store.prayerJournal() }
            val entries = safe(emptyList<JournalEntry>()) { store.journalEntries() }
            val jStreak = safe(0) { store.journalStreak() }
            val notes = safe(emptyList<com.prayerkey.manna.data.SermonNote>()) { store.sermonNotes() }
            val formation = safe(com.prayerkey.manna.data.FormationState()) { store.formation() }
            withContext(Dispatchers.Main) {
                _sermonNotes.value = notes
                _formation.value = formation
                _saved.value = saved
                _streak.value = streak
                _memory.value = memory
                _preferences.value = prefs
                _sermons.value = sermons
                _journal.value = journal
                _entries.value = entries
                _journalStreak.value = jStreak
                _hydrated.value = true
            }
        }
    }
    // 544 prayer decks — fetched once, cached for the whole session so
    // the Prayers tab opens instantly every time
    private val _topics = MutableStateFlow<List<com.prayerkey.manna.data.PrayerTopic>>(emptyList())
    val topics = _topics.asStateFlow()
    private var topicsRequested = false
    fun loadTopics() {
        if (topicsRequested) return
        topicsRequested = true
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { com.prayerkey.manna.data.PrayerKeyApi.prayerTopics() }
                .onSuccess { _topics.value = it }
                .onFailure { topicsRequested = false }
        }
    }

    /** Every mutation writes on the IO dispatcher and publishes on Main,
     *  so a card swipe is never blocked by a disk write. */
    private fun io(block: () -> Unit) = viewModelScope.launch(Dispatchers.IO) { runCatching(block) }

    fun save(card: VerseCard) = io { store.save(card); val r = store.all(); _saved.value = r }
    fun markAnswered(id: Long, testimony: String) = io { store.markAnswered(id, testimony); _saved.value = store.all() }
    fun recordDailyPull() { io { val s = store.recordDailyPull(); _streak.value = s } }
    fun memorize(card: VerseCard) = io { store.memorize(card); _memory.value = store.memoryVerses() }
    fun reviewMemory(reference: String, correct: Boolean) = io { store.reviewMemory(reference, correct); _memory.value = store.memoryVerses() }
    fun updatePreferences(prefs: UserPrefs) {
        // publish immediately so the UI reacts on this frame; persist behind it
        _preferences.value = prefs
        io { store.savePreferences(prefs) }
    }
    fun startSermon(): Long = safe(0L) { store.startSermon() }

    /** Church mode: the arranged Sunday note, saved for good. */
    fun saveSermonNote(note: com.prayerkey.manna.ui.church.SermonArranger.Note, minutes: Int) = io {
        store.saveSermonNote(
            title = note.title, scriptures = note.scriptures, points = note.points,
            quotes = note.quotes, takeaway = note.takeaway, transcript = note.transcript,
            minutes = minutes,
        )
        _sermonNotes.value = store.sermonNotes()

        /* Sunday also becomes a journal page, so the whole walk lives in one
           place instead of the sermon sitting in its own silo. */
        store.addJournalEntry(
            mood = PRAYING_MOOD,
            body = buildString {
                append(note.title)
                if (note.takeaway.isNotBlank()) { append("\n\n"); append(note.takeaway) }
            },
            gratitude = "",
            verseRef = note.scriptures.firstOrNull(),
            verseText = null,
            source = "sermon",
        )
        refreshEntries()
    }

    fun deleteSermonNote(id: Long) = io {
        store.deleteSermonNote(id); _sermonNotes.value = store.sermonNotes()
    }
    fun addSermonVerse(sessionId: Long, reference: String, text: String) = io { store.addSermonVerse(sessionId, reference, text); _sermons.value = store.sermons() }
    fun endSermon(sessionId: Long) = io { store.endSermon(sessionId); _sermons.value = store.sermons() }
    private fun refreshEntries() {
        _entries.value = store.journalEntries()
        _journalStreak.value = store.journalStreak()
    }
    fun addEntry(mood: String, body: String, gratitude: String, verseRef: String?, verseText: String?) = io {
        store.addJournalEntry(mood, body, gratitude, verseRef, verseText); refreshEntries()
    }

    /** The richer write flow: carries the entry's origin and prayer flag. */
    fun addWrite(r: com.prayerkey.manna.ui.journal.WriteResult) = io {
        store.addJournalEntry(r.mood, r.body, r.gratitude, r.verseRef, r.verseText, r.source, r.isPrayer, r.title, r.tags, r.journal, r.favorite, r.location, r.weather, r.media, r.entryAt)
        refreshEntries()
    }

    fun updateWrite(id: Long, r: com.prayerkey.manna.ui.journal.WriteResult) = io {
        store.updateJournalEntry(id, r.mood, r.body, r.gratitude, r.isPrayer, r.title, r.tags, r.journal, r.favorite, r.location, r.weather, r.media, r.entryAt); refreshEntries()
    }

    fun answerEntry(id: Long, testimony: String) = io {
        store.answerJournalEntry(id, testimony); refreshEntries()
    }
    fun updateFormation(value: com.prayerkey.manna.data.FormationState) {
        _formation.value = value
        io { store.saveFormation(value) }
    }
    fun restoreArchive(raw: String) = io {
        store.restoreArchive(raw)
        _saved.value = store.all(); _memory.value = store.memoryVerses(); _journal.value = store.prayerJournal()
        _entries.value = store.journalEntries(); _journalStreak.value = store.journalStreak(); _sermonNotes.value = store.sermonNotes(); _formation.value = store.formation()
    }
    fun updatePrayerJourney(id: Long, stage: com.prayerkey.manna.data.PrayerStage, nextAction: String, testimony: String) = io {
        store.updatePrayerJourney(id, stage, nextAction, testimony); refreshEntries()
    }
    fun updateEntry(id: Long, mood: String, body: String, gratitude: String) = io {
        store.updateJournalEntry(id, mood, body, gratitude); refreshEntries()
    }
    fun deleteEntry(id: Long) = io { store.deleteJournalEntry(id); refreshEntries() }

    fun savePrayer(request: String, prayer: GeneratedPrayer) = io {
        store.savePrayer(prayer.title, request, prayer.prayer, prayer.verses.firstOrNull()?.first)
        store.save(VerseCard(prayer.verses.firstOrNull()?.first ?: prayer.title, "PRAYER", prayer.prayer, ""))
        _journal.value = store.prayerJournal()
        _saved.value = store.all()
    }
}
