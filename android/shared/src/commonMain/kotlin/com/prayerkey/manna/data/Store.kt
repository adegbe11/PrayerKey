package com.prayerkey.manna.data

import com.prayerkey.manna.model.VerseCard
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

/** Grace, unless the phone is in dark mode and the user follows the system. */
const val DEFAULT_THEME_ID = "grace"
const val DARK_THEME_ID = "vigil"

@Serializable
data class UserPrefs(
    val name: String = "",
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = false,
    val reduceMotion: Boolean = false,
    val translation: String = "KJV",
    val onboarded: Boolean = false,
    val accountReady: Boolean = false,
    val accountEmail: String = "",
    val accountKind: String = "private",
    val sermonLanguage: String = "en-US",
    val themeId: String = DEFAULT_THEME_ID,
    /** Let the phone decide light or dark; the theme supplies the accent. */
    val followSystemTheme: Boolean = true,
    val journalLock: Boolean = false,
    val concealJournalPreviews: Boolean = false,
    /* The book reader. Bible readers skew older and a fixed small serif
       would shut a lot of them out, so the size is theirs to set and the
       reader re-paginates around it. */
    val readerTextSize: Int = 17,
    /** Where the ribbon is resting, as "Psalms 23". Null = not dropped. */
    val readerRibbon: String? = null,
)

@Serializable
data class SermonVerse(val reference: String, val text: String, val detectedAt: Long)

@Serializable
data class SermonSession(val id: Long, val title: String, val startedAt: Long, val endedAt: Long?, val verses: List<SermonVerse>)

@Serializable
data class PrayerSession(
    val id: Long,
    val challenge: String,
    val focus: String,
    val day: Int,
    val minutes: Int,
    val prayedAt: Long,
)

@Serializable
data class FormationState(
    val morningWord: Boolean = true,
    val middayPrayer: Boolean = false,
    val eveningExamen: Boolean = true,
    val sabbathDay: String = "Sunday",
    val pilgrimageId: String = "",
    val pilgrimageDay: Int = 0,
    val familyNames: String = "",
    val trustedPhone: String = "",
)

/**
 * Everything PrayerKey keeps about a person's walk: saved words, the journal,
 * prayers, sermon notes, memory verses, streaks and settings.
 *
 * Two homes for one shape. On Android it is the SQLite database the app has
 * always had (MannaStore), untouched, so nobody's journal moves. On iPhone,
 * which starts fresh, it is [JsonMannaStore]. The screens only see this.
 */
interface MannaRepository {
    /** Opens storage. Throws when it cannot be opened; nothing is erased. */
    fun open()

    fun logPrayerSession(challenge: String, focus: String, day: Int, minutes: Int): Long
    fun prayerSessions(limit: Int = 100): List<PrayerSession>

    fun saveSermonNote(
        title: String, scriptures: List<String>, points: List<String>,
        quotes: List<String>, takeaway: String, transcript: String, minutes: Int,
        audioPath: String = "",
    ): Long
    fun refineSermonNote(
        id: Long, title: String, scriptures: List<String>, points: List<String>,
        quotes: List<String>, takeaway: String, transcript: String,
    )
    fun sermonNotes(): List<SermonNote>
    fun deleteSermonNote(id: Long)

    fun addJournalEntry(
        mood: String, body: String, gratitude: String, verseRef: String?, verseText: String?,
        source: String = "write", isPrayer: Boolean = false,
        title: String = "", tags: List<String> = emptyList(), journal: String = "My Journey",
        favorite: Boolean = false, location: String = "", weather: String = "", media: List<String> = emptyList(),
        entryAt: Long = nowMillis(),
    ): Long
    fun answerJournalEntry(id: Long, testimony: String)
    fun updatePrayerJourney(id: Long, stage: PrayerStage, nextAction: String, testimony: String)
    fun updateJournalEntry(
        id: Long, mood: String, body: String, gratitude: String, isPrayer: Boolean? = null,
        title: String? = null, tags: List<String>? = null, journal: String? = null,
        favorite: Boolean? = null, location: String? = null, weather: String? = null, media: List<String>? = null,
        entryAt: Long? = null,
    )
    fun deleteJournalEntry(id: Long)
    fun journalEntries(): List<JournalEntry>
    fun journalStreak(): Int

    fun savePrayer(title: String, request: String, prayer: String, scriptureRef: String?)
    fun prayerJournal(): List<JournalPrayer>

    fun startSermon(title: String = "Sunday service"): Long
    fun addSermonVerse(sessionId: Long, reference: String, text: String)
    fun endSermon(sessionId: Long)
    fun sermons(): List<SermonSession>

    fun memorize(card: VerseCard)
    fun reviewMemory(reference: String, correct: Boolean)
    fun memoryVerses(): List<MemoryVerse>

    fun challengeDays(id: String): Set<Int>
    fun setChallengeDay(id: String, day: Int, done: Boolean)

    fun preferences(): UserPrefs
    fun savePreferences(prefs: UserPrefs)
    fun formation(): FormationState
    fun saveFormation(value: FormationState)

    /** Replaces journey content from a PrayerKey backup (format manna-journey-v*). */
    fun restoreArchive(raw: String)

    fun streak(): Int
    fun recordDailyPull(): Int

    fun save(card: VerseCard)
    fun markAnswered(id: Long, testimony: String)
    fun all(): List<SavedWord>
}

fun nowMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

/** The calendar day a moment fell on, on this phone, as days since 1970. */
fun epochDayOf(millis: Long): Long =
    kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toEpochDays().toLong()
