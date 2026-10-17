package com.prayerkey.manna.data

import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.deleteFile
import com.prayerkey.manna.platform.readFile
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.platform.writeFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * The iPhone's store: the same behaviour as Android's SQLite MannaStore - the
 * same orderings, the same "save it once" rules, the same streaks and the same
 * backup format - kept in one file in the app's Documents.
 *
 * A journal is text and a few thousand rows at most, so the whole store lives
 * in memory and every change rewrites the file, atomically. Callers must not
 * run two calls at once; the app runs them all on one IO lane.
 */
class JsonMannaStore(private val context: PlatformContext, private val fileName: String = "manna.json") : MannaRepository {

    @Serializable
    private data class Db(
        var nextId: Long = 1,
        val saved: MutableList<SavedWord> = mutableListOf(),
        val memory: MutableList<MemoryVerse> = mutableListOf(),
        val prayers: MutableList<JournalPrayer> = mutableListOf(),
        val entries: MutableList<JournalEntry> = mutableListOf(),
        val notes: MutableList<SermonNote> = mutableListOf(),
        val sermons: MutableList<SermonSession> = mutableListOf(),
        val sessions: MutableList<PrayerSession> = mutableListOf(),
        val state: MutableMap<String, String> = mutableMapOf(),
    )

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var db: Db? = null

    private fun data(): Db = db ?: run {
        val raw = context.readFile(fileName)
        // a file that exists but will not parse is kept aside, never overwritten
        val loaded = if (raw == null) Db() else runCatching { json.decodeFromString(Db.serializer(), raw) }.getOrElse {
            context.writeFile("$fileName.unreadable-${nowMillis()}", raw)
            Db()
        }
        loaded.also { db = it }
    }

    private fun commit() {
        context.writeFile(fileName, json.encodeToString(Db.serializer(), data()))
    }

    private fun nextId(): Long = data().nextId.also { data().nextId = it + 1 }

    override fun open() {
        data()
    }

    /* ── prayer sessions ───────────────────────────────────────────── */

    override fun logPrayerSession(challenge: String, focus: String, day: Int, minutes: Int): Long {
        val id = nextId()
        data().sessions += PrayerSession(id, challenge, focus.trim(), day, minutes.coerceAtLeast(1), nowMillis())
        commit()
        return id
    }

    override fun prayerSessions(limit: Int): List<PrayerSession> =
        data().sessions.sortedByDescending { it.prayedAt }.take(limit)

    /* ── sermon notes ──────────────────────────────────────────────── */

    override fun saveSermonNote(
        title: String, scriptures: List<String>, points: List<String>,
        quotes: List<String>, takeaway: String, transcript: String, minutes: Int, audioPath: String,
    ): Long {
        val id = nextId()
        data().notes += SermonNote(
            id, title, scriptures.clean(), points.clean(), quotes.clean(), takeaway, transcript,
            minutes, nowMillis(), audioPath,
        )
        commit()
        return id
    }

    override fun refineSermonNote(
        id: Long, title: String, scriptures: List<String>, points: List<String>,
        quotes: List<String>, takeaway: String, transcript: String,
    ) {
        data().notes.update {
            if (it.id != id) it else it.copy(
                title = title, scriptures = scriptures.clean(), points = points.clean(),
                quotes = quotes.clean(), takeaway = takeaway, transcript = transcript, refined = true,
            )
        }
        commit()
    }

    override fun sermonNotes(): List<SermonNote> = data().notes.sortedByDescending { it.createdAt }

    override fun deleteSermonNote(id: Long) {
        val audio = data().notes.firstOrNull { it.id == id }?.audioPath.orEmpty()
        data().notes.removeAll { it.id == id }
        commit()
        if (audio.isBlank()) return
        deleteFile(audio)
        // the transcript written beside it, same stem
        deleteFile(audio.substringBeforeLast('.') + ".json")
    }

    /* ── journal ───────────────────────────────────────────────────── */

    override fun addJournalEntry(
        mood: String, body: String, gratitude: String, verseRef: String?, verseText: String?,
        source: String, isPrayer: Boolean, title: String, tags: List<String>, journal: String,
        favorite: Boolean, location: String, weather: String, media: List<String>, entryAt: Long,
    ): Long {
        val id = nextId()
        data().entries += JournalEntry(
            id = id, entryDay = epochDayOf(entryAt), mood = mood, body = body.trim(), gratitude = gratitude.trim(),
            verseRef = verseRef, verseText = verseText, createdAt = entryAt, updatedAt = nowMillis(),
            source = source, isPrayer = isPrayer, title = title.trim(), tags = tags.clean(),
            journal = journal.ifBlank { "My Journey" }, favorite = favorite, location = location.trim(),
            weather = weather.trim(), media = media.clean(),
        )
        commit()
        return id
    }

    /** A prayer entry that God answered — the proof pile. */
    override fun answerJournalEntry(id: Long, testimony: String) {
        val now = nowMillis()
        entry(id) { it.copy(answeredAt = now, testimony = testimony.trim(), prayerStage = PrayerStage.Answered, updatedAt = now) }
    }

    override fun updatePrayerJourney(id: Long, stage: PrayerStage, nextAction: String, testimony: String) {
        val answered = stage == PrayerStage.Answered || stage == PrayerStage.AnsweredDifferently
        val now = nowMillis()
        entry(id) {
            it.copy(
                prayerStage = stage, nextAction = nextAction.trim(), updatedAt = now,
                answeredAt = if (answered) now else null,
                testimony = if (answered) testimony.trim() else null,
            )
        }
    }

    override fun updateJournalEntry(
        id: Long, mood: String, body: String, gratitude: String, isPrayer: Boolean?,
        title: String?, tags: List<String>?, journal: String?, favorite: Boolean?,
        location: String?, weather: String?, media: List<String>?, entryAt: Long?,
    ) {
        entry(id) { e ->
            e.copy(
                mood = mood, body = body.trim(), gratitude = gratitude.trim(), updatedAt = nowMillis(),
                isPrayer = isPrayer ?: e.isPrayer,
                title = title?.trim() ?: e.title,
                tags = tags?.clean() ?: e.tags,
                journal = journal?.ifBlank { "My Journey" } ?: e.journal,
                favorite = favorite ?: e.favorite,
                location = location?.trim() ?: e.location,
                weather = weather?.trim() ?: e.weather,
                media = media?.clean() ?: e.media,
                createdAt = entryAt ?: e.createdAt,
                entryDay = entryAt?.let(::epochDayOf) ?: e.entryDay,
            )
        }
    }

    private fun entry(id: Long, change: (JournalEntry) -> JournalEntry) {
        data().entries.update { if (it.id == id) change(it) else it }
        commit()
    }

    override fun deleteJournalEntry(id: Long) {
        data().entries.removeAll { it.id == id }
        commit()
    }

    override fun journalEntries(): List<JournalEntry> = data().entries.sortedByDescending { it.createdAt }

    /** Consecutive days (ending today or yesterday) with at least one entry. */
    override fun journalStreak(): Int {
        val days = data().entries.map { it.entryDay }.distinct().sortedDescending()
        if (days.isEmpty()) return 0
        val today = today().toEpochDays().toLong()
        var cursor = when (days.first()) { today, today - 1 -> days.first(); else -> return 0 }
        var count = 0
        for (day in days) {
            if (day == cursor) { count++; cursor-- } else if (day < cursor) break
        }
        return count
    }

    /* ── prayer journal ────────────────────────────────────────────── */

    override fun savePrayer(title: String, request: String, prayer: String, scriptureRef: String?) {
        data().prayers += JournalPrayer(nextId(), title, request, prayer, scriptureRef, nowMillis())
        commit()
    }

    override fun prayerJournal(): List<JournalPrayer> = data().prayers.sortedByDescending { it.createdAt }

    /* ── live sermons ──────────────────────────────────────────────── */

    override fun startSermon(title: String): Long {
        val id = nextId()
        data().sermons += SermonSession(id, title, nowMillis(), null, emptyList())
        commit()
        return id
    }

    override fun addSermonVerse(sessionId: Long, reference: String, text: String) {
        data().sermons.update { s ->
            if (s.id != sessionId || s.verses.any { it.reference == reference }) s
            else s.copy(verses = s.verses + SermonVerse(reference, text, nowMillis()))
        }
        commit()
    }

    override fun endSermon(sessionId: Long) {
        data().sermons.update { if (it.id == sessionId) it.copy(endedAt = nowMillis()) else it }
        commit()
    }

    override fun sermons(): List<SermonSession> =
        data().sermons.sortedByDescending { it.startedAt }.map { s -> s.copy(verses = s.verses.sortedBy { it.detectedAt }) }

    /* ── memory verses ─────────────────────────────────────────────── */

    override fun memorize(card: VerseCard) {
        if (data().memory.any { it.reference == card.reference }) return
        data().memory += MemoryVerse(card.reference, card.verse, 1)
        commit()
    }

    override fun reviewMemory(reference: String, correct: Boolean) {
        val current = data().memory.firstOrNull { it.reference == reference } ?: return
        val review = MemorySchedule.after(current, correct, nowMillis())
        data().memory.update {
            if (it.reference == reference) it.copy(stage = review.stage, correctCount = review.correctCount, nextReviewAt = review.nextReviewAt) else it
        }
        commit()
    }

    override fun memoryVerses(): List<MemoryVerse> =
        data().memory.sortedWith(compareBy<MemoryVerse> { it.nextReviewAt }.thenBy { it.stage })

    /* ── state: challenges, settings, streak ───────────────────────── */

    private fun state(key: String): String? = data().state[key]
    private fun putState(key: String, value: String) { data().state[key] = value }

    override fun challengeDays(id: String): Set<Int> =
        state("challenge_$id").orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()

    override fun setChallengeDay(id: String, day: Int, done: Boolean) {
        val next = challengeDays(id).toMutableSet().apply { if (done) add(day) else remove(day) }
        putState("challenge_$id", next.sorted().joinToString(","))
        commit()
    }

    override fun preferences(): UserPrefs = UserPrefs(
        name = state("name") ?: "",
        reminderHour = state("reminder_hour")?.toIntOrNull() ?: 7,
        reminderMinute = state("reminder_minute")?.toIntOrNull() ?: 0,
        reminderEnabled = state("reminder_enabled") == "true",
        reduceMotion = state("reduce_motion") == "true",
        translation = state("translation") ?: "KJV",
        onboarded = state("onboarded") == "true",
        accountReady = state("account_ready") == "true",
        accountEmail = state("account_email") ?: "",
        accountKind = state("account_kind") ?: "private",
        sermonLanguage = state("sermon_language") ?: "en-US",
        journalLock = state("journal_lock") == "true",
        concealJournalPreviews = state("journal_conceal") == "true",
        readerTextSize = state("reader_size")?.toIntOrNull() ?: 17,
        readerRibbon = state("reader_ribbon")?.takeIf { it.isNotBlank() },
        themeId = state("theme") ?: DEFAULT_THEME_ID,
        followSystemTheme = state("theme_follow_system") != "false",
    )

    override fun savePreferences(prefs: UserPrefs) {
        putState("name", prefs.name); putState("reminder_hour", prefs.reminderHour.toString())
        putState("reminder_minute", prefs.reminderMinute.toString()); putState("reminder_enabled", prefs.reminderEnabled.toString())
        putState("reduce_motion", prefs.reduceMotion.toString()); putState("translation", prefs.translation)
        putState("onboarded", prefs.onboarded.toString())
        putState("account_ready", prefs.accountReady.toString())
        putState("account_email", prefs.accountEmail)
        putState("account_kind", prefs.accountKind)
        putState("sermon_language", prefs.sermonLanguage)
        putState("journal_lock", prefs.journalLock.toString()); putState("journal_conceal", prefs.concealJournalPreviews.toString())
        putState("reader_size", prefs.readerTextSize.toString()); putState("reader_ribbon", prefs.readerRibbon.orEmpty())
        putState("theme", prefs.themeId); putState("theme_follow_system", prefs.followSystemTheme.toString())
        commit()
    }

    override fun formation(): FormationState = FormationState(
        morningWord = state("formation_morning")?.toBooleanStrictOrNull() ?: true,
        middayPrayer = state("formation_midday")?.toBooleanStrictOrNull() ?: false,
        eveningExamen = state("formation_evening")?.toBooleanStrictOrNull() ?: true,
        sabbathDay = state("formation_sabbath") ?: "Sunday",
        pilgrimageId = state("formation_pilgrimage") ?: "",
        pilgrimageDay = state("formation_pilgrimage_day")?.toIntOrNull() ?: 0,
        familyNames = state("formation_family") ?: "",
        trustedPhone = state("formation_trusted_phone") ?: "",
    )

    override fun saveFormation(value: FormationState) {
        putState("formation_morning", value.morningWord.toString())
        putState("formation_midday", value.middayPrayer.toString())
        putState("formation_evening", value.eveningExamen.toString())
        putState("formation_sabbath", value.sabbathDay)
        putState("formation_pilgrimage", value.pilgrimageId)
        putState("formation_pilgrimage_day", value.pilgrimageDay.toString())
        putState("formation_family", value.familyNames)
        putState("formation_trusted_phone", value.trustedPhone)
        commit()
    }

    override fun streak(): Int = state("streak")?.toIntOrNull() ?: 0

    override fun recordDailyPull(): Int {
        val today = today().toEpochDays().toLong()
        val last = state("last_pull_day")?.toLongOrNull()
        val next = when (last) {
            today -> streak()
            today - 1 -> streak() + 1
            else -> 1
        }
        putState("last_pull_day", today.toString()); putState("streak", next.toString())
        commit()
        return next
    }

    /* ── saved words ───────────────────────────────────────────────── */

    override fun save(card: VerseCard) {
        if (data().saved.any { it.reference == card.reference && it.translation == card.translation }) return
        data().saved += SavedWord(nextId(), card.reference, card.translation, card.verse, nowMillis())
        commit()
    }

    override fun markAnswered(id: Long, testimony: String) {
        val now = nowMillis()
        data().saved.update { if (it.id == id) it.copy(answeredAt = now, testimony = testimony.trim()) else it }
        commit()
    }

    override fun all(): List<SavedWord> = data().saved.sortedByDescending { it.savedAt }

    /* ── restore ───────────────────────────────────────────────────── */

    /** Replaces journey content from a PrayerKey backup - the format Android writes. */
    override fun restoreArchive(raw: String) {
        val root = Json.parseToJsonElement(raw).jsonObject
        require(root.str("format").startsWith("manna-journey-v")) { "This is not a PrayerKey backup file" }
        val d = data()
        // built aside and swapped in, so a backup that fails halfway changes nothing
        val entries = root.rows("entries").map { item ->
            val created = item.long("createdAt") ?: nowMillis()
            JournalEntry(
                id = 0, entryDay = epochDayOf(created), mood = item.str("mood", "🙏"), body = item.str("body"),
                gratitude = item.str("gratitude"), verseRef = item.strOrNull("verseRef"), verseText = item.strOrNull("verseText"),
                createdAt = created, updatedAt = created, source = item.str("source", "write"), isPrayer = item.bool("isPrayer"),
                answeredAt = item.long("answeredAt"), testimony = item.strOrNull("testimony"),
                prayerStage = PrayerStage.from(item.str("prayerStage", "praying")), nextAction = item.str("nextAction"),
                title = item.str("title"), tags = item.lines("tags"), journal = item.str("journal", "My Journey"),
                favorite = item.bool("favorite"), location = item.str("location"), weather = item.str("weather"), media = item.lines("media"),
            )
        }
        val notes = root.rows("sermons").map { item ->
            SermonNote(
                0, item.str("title"), item.lines("scriptures"), item.lines("points"), item.lines("quotes"),
                item.str("takeaway"), item.str("transcript"), (item.long("minutes") ?: 0L).toInt(), item.long("createdAt") ?: nowMillis(),
            )
        }
        val prayers = root.rows("prayers").map { item ->
            JournalPrayer(0, item.str("title"), item.str("request"), item.str("prayer"), item.strOrNull("scriptureRef"), item.long("createdAt") ?: nowMillis())
        }
        val saved = root.rows("savedWords").map { item ->
            SavedWord(0, item.str("reference"), item.str("translation", "KJV"), item.str("verse"), item.long("savedAt") ?: nowMillis(), item.long("answeredAt"), item.strOrNull("testimony"))
        }.distinctBy { it.reference to it.translation }
        val memory = root.rows("memory").map { item ->
            MemoryVerse(item.str("reference"), item.str("verse"), (item.long("stage") ?: 1L).toInt(), (item.long("correctCount") ?: 0L).toInt(), item.long("nextReviewAt") ?: 0L)
        }.associateBy { it.reference }.values

        d.entries.clear(); entries.forEach { d.entries += it.copy(id = nextId()) }
        d.notes.clear(); notes.forEach { d.notes += it.copy(id = nextId()) }
        d.prayers.clear(); prayers.forEach { d.prayers += it.copy(id = nextId()) }
        d.saved.clear(); saved.forEach { d.saved += it.copy(id = nextId()) }
        d.memory.clear(); d.memory += memory
        (root["formation"] as? JsonObject)?.let { item ->
            saveFormation(
                FormationState(
                    item.boolOr("morningWord", true), item.bool("middayPrayer"), item.boolOr("eveningExamen", true),
                    item.str("sabbathDay", "Sunday"), item.str("pilgrimageId"), (item.long("pilgrimageDay") ?: 0L).toInt(),
                    item.str("familyNames"), item.str("trustedPhone"),
                ),
            )
        }
        commit()
    }

    private fun List<String>.clean() = filter { it.isNotBlank() }
    private inline fun <T> MutableList<T>.update(change: (T) -> T) { for (i in indices) this[i] = change(this[i]) }
    private fun JsonObject.rows(key: String): List<JsonObject> = (this[key] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()
    private fun JsonObject.strOrNull(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
    private fun JsonObject.str(key: String, fallback: String = ""): String = strOrNull(key) ?: fallback
    private fun JsonObject.long(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull
    private fun JsonObject.bool(key: String): Boolean = boolOr(key, false)
    private fun JsonObject.boolOr(key: String, fallback: Boolean): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull ?: fallback
    private fun JsonObject.lines(key: String): List<String> =
        (this[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }?.filter { it.isNotBlank() }.orEmpty()
}
