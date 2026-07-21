package com.prayerkey.manna.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.UserPrefs
import com.prayerkey.manna.data.SermonSession
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.GeneratedPrayer
import com.prayerkey.manna.model.VerseCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = MannaStore(application)
    private val _saved = MutableStateFlow(store.all())
    val saved = _saved.asStateFlow()
    private val _streak = MutableStateFlow(store.streak())
    val streak = _streak.asStateFlow()
    private val _memory = MutableStateFlow(store.memoryVerses())
    val memory = _memory.asStateFlow()
    private val _preferences = MutableStateFlow(store.preferences())
    val preferences = _preferences.asStateFlow()
    private val _sermons = MutableStateFlow(store.sermons())
    val sermons = _sermons.asStateFlow()
    private val _journal = MutableStateFlow(store.prayerJournal())
    val journal = _journal.asStateFlow()

    fun save(card: VerseCard) { store.save(card); _saved.value = store.all() }
    fun markAnswered(id: Long, testimony: String) { store.markAnswered(id, testimony); _saved.value = store.all() }
    fun recordDailyPull() { _streak.value = store.recordDailyPull() }
    fun memorize(card: VerseCard) { store.memorize(card); _memory.value = store.memoryVerses() }
    fun advanceMemory(reference: String) { store.advanceMemory(reference); _memory.value = store.memoryVerses() }
    fun updatePreferences(prefs: UserPrefs) { store.savePreferences(prefs); _preferences.value = prefs }
    fun startSermon(): Long = store.startSermon()
    fun addSermonVerse(sessionId: Long, reference: String, text: String) { store.addSermonVerse(sessionId, reference, text); _sermons.value = store.sermons() }
    fun endSermon(sessionId: Long) { store.endSermon(sessionId); _sermons.value = store.sermons() }
    fun savePrayer(request: String, prayer: GeneratedPrayer) {
        store.savePrayer(prayer.title, request, prayer.prayer, prayer.verses.firstOrNull()?.first)
        store.save(VerseCard(prayer.verses.firstOrNull()?.first ?: prayer.title, "PRAYER", prayer.prayer, ""))
        _journal.value = store.prayerJournal()
        _saved.value = store.all()
    }
}
