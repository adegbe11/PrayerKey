package com.prayerkey.manna.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prayerkey.manna.model.VerseCard
import org.json.JSONObject

data class SavedWord(
    val id: Long,
    val reference: String,
    val translation: String,
    val verse: String,
    val savedAt: Long,
    val answeredAt: Long? = null,
    val testimony: String? = null,
)

data class MemoryVerse(val reference: String, val verse: String, val stage: Int, val correctCount: Int = 0, val nextReviewAt: Long = 0L)
data class UserPrefs(
    val name: String = "",
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = false,
    val reduceMotion: Boolean = false,
    val translation: String = "KJV",
    val onboarded: Boolean = false,
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
data class SermonVerse(val reference: String, val text: String, val detectedAt: Long)
data class SermonSession(val id: Long, val title: String, val startedAt: Long, val endedAt: Long?, val verses: List<SermonVerse>)
data class JournalPrayer(val id: Long, val title: String, val request: String, val prayer: String, val scriptureRef: String?, val createdAt: Long)

enum class PrayerStage(val key: String, val label: String) {
    Praying("praying", "Praying"),
    Waiting("waiting", "Waiting"),
    Acting("acting", "Taking action"),
    Released("released", "Released"),
    AnsweredDifferently("answered_differently", "Answered differently"),
    Answered("answered", "Answered"),
    ;

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.key == raw } ?: Praying
    }
}

data class JournalEntry(
    val id: Long,
    val entryDay: Long,          // epoch day — one calendar day per group
    val mood: String,            // emoji key, e.g. "🙏"
    val body: String,            // what's on your heart
    val gratitude: String,       // one thing you're grateful for
    val verseRef: String?,       // optionally attached word
    val verseText: String?,
    val createdAt: Long,
    val updatedAt: Long,
    /** How this entry began: "write", "sermon", "prayer" or "verse". Drives
     *  the origin icon on the card so a page traces back to its moment. */
    val source: String = "write",
    val isPrayer: Boolean = false,
    val answeredAt: Long? = null,
    val testimony: String? = null,
    val prayerStage: PrayerStage = PrayerStage.Praying,
    val nextAction: String = "",
    val title: String = "",
    val tags: List<String> = emptyList(),
    val journal: String = "My Journey",
    val favorite: Boolean = false,
    val location: String = "",
    val weather: String = "",
    val media: List<String> = emptyList(),
)
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

data class SermonNote(
    val id: Long,
    val title: String,
    val scriptures: List<String>,
    val points: List<String>,
    val quotes: List<String>,
    val takeaway: String,
    val transcript: String,
    val minutes: Int,
    val createdAt: Long,
)

class MannaStore(context: Context) : SQLiteOpenHelper(context, "manna.db", null, 12) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE saved_words (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            reference TEXT NOT NULL,
            translation TEXT NOT NULL,
            verse TEXT NOT NULL,
            saved_at INTEGER NOT NULL,
            answered_at INTEGER,
            testimony TEXT
        )""")
        db.execSQL("CREATE UNIQUE INDEX saved_reference ON saved_words(reference, translation)")
        createStateTable(db)
        createMemoryTable(db)
        createSermonTables(db)
        createPrayerJournal(db)
        // onCreate must build EVERY table. Leaving this out meant a fresh
        // install landed at the current schema version with no journal
        // table, and onUpgrade never ran to add it.
        createJournalEntries(db)
        createSermonNotes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createStateTable(db)
        if (oldVersion < 3) createMemoryTable(db)
        if (oldVersion < 4) createSermonTables(db)
        if (oldVersion < 5) createPrayerJournal(db)
        // v7 repairs installs created at v6, whose onCreate skipped this table
        if (oldVersion < 7) createJournalEntries(db)
        if (oldVersion < 8) createSermonNotes(db)
        if (oldVersion < 9) {
            // added per column, and tolerated individually: a table created
            // fresh at v9 already has them, and ALTER on an existing column
            // throws rather than no-opping
            listOf(
                "ALTER TABLE journal_entries ADD COLUMN source TEXT NOT NULL DEFAULT 'write'",
                "ALTER TABLE journal_entries ADD COLUMN is_prayer INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE journal_entries ADD COLUMN answered_at INTEGER",
                "ALTER TABLE journal_entries ADD COLUMN testimony TEXT",
            ).forEach { sql -> runCatching { db.execSQL(sql) } }
        }
        if (oldVersion < 10) {
            listOf(
                "ALTER TABLE journal_entries ADD COLUMN prayer_status TEXT NOT NULL DEFAULT 'praying'",
                "ALTER TABLE journal_entries ADD COLUMN next_action TEXT NOT NULL DEFAULT ''",
            ).forEach { sql -> runCatching { db.execSQL(sql) } }
        }
        if (oldVersion < 11) {
            listOf(
                "ALTER TABLE memory_verses ADD COLUMN correct_count INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE memory_verses ADD COLUMN next_review_at INTEGER NOT NULL DEFAULT 0",
            ).forEach { sql -> runCatching { db.execSQL(sql) } }
        }
        if (oldVersion < 12) {
            listOf(
                "ALTER TABLE journal_entries ADD COLUMN title TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE journal_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE journal_entries ADD COLUMN journal_name TEXT NOT NULL DEFAULT 'My Journey'",
                "ALTER TABLE journal_entries ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE journal_entries ADD COLUMN location TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE journal_entries ADD COLUMN weather TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE journal_entries ADD COLUMN media TEXT NOT NULL DEFAULT ''",
            ).forEach { sql -> runCatching { db.execSQL(sql) } }
        }
    }

    /** A phone with a newer/foreign schema must never crash the app —
     *  rebuild the database instead of throwing (default behaviour). */
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        listOf("saved_words", "app_state", "memory_verses", "sermon_sessions", "sermon_verses", "prayer_journal", "journal_entries", "sermon_notes")
            .forEach { table -> runCatching { db.execSQL("DROP TABLE IF EXISTS $table") } }
        onCreate(db)
    }

    private fun createStateTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS app_state (state_key TEXT PRIMARY KEY, state_value TEXT NOT NULL)")
    }

    private fun createMemoryTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS memory_verses (reference TEXT PRIMARY KEY, verse TEXT NOT NULL, stage INTEGER NOT NULL DEFAULT 1, correct_count INTEGER NOT NULL DEFAULT 0, next_review_at INTEGER NOT NULL DEFAULT 0)")
    }

    private fun createSermonTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS sermon_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, started_at INTEGER NOT NULL, ended_at INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS sermon_verses (id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER NOT NULL, reference TEXT NOT NULL, verse TEXT NOT NULL, detected_at INTEGER NOT NULL, UNIQUE(session_id, reference))")
    }

    private fun createPrayerJournal(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS prayer_journal (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, request TEXT NOT NULL, prayer TEXT NOT NULL, scripture_ref TEXT, created_at INTEGER NOT NULL)")
    }

    private fun createJournalEntries(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS journal_entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            entry_day INTEGER NOT NULL,
            mood TEXT NOT NULL,
            body TEXT NOT NULL,
            gratitude TEXT NOT NULL DEFAULT '',
            verse_ref TEXT,
            verse_text TEXT,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            source TEXT NOT NULL DEFAULT 'write',
            is_prayer INTEGER NOT NULL DEFAULT 0,
            answered_at INTEGER,
            testimony TEXT
            ,prayer_status TEXT NOT NULL DEFAULT 'praying'
            ,next_action TEXT NOT NULL DEFAULT ''
            ,title TEXT NOT NULL DEFAULT ''
            ,tags TEXT NOT NULL DEFAULT ''
            ,journal_name TEXT NOT NULL DEFAULT 'My Journey'
            ,favorite INTEGER NOT NULL DEFAULT 0
            ,location TEXT NOT NULL DEFAULT ''
            ,weather TEXT NOT NULL DEFAULT ''
            ,media TEXT NOT NULL DEFAULT ''
        )""")
        db.execSQL("CREATE INDEX IF NOT EXISTS journal_day ON journal_entries(entry_day)")
    }

    private fun createSermonNotes(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS sermon_notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            scriptures TEXT NOT NULL,
            points TEXT NOT NULL,
            quotes TEXT NOT NULL,
            takeaway TEXT NOT NULL,
            transcript TEXT NOT NULL,
            minutes INTEGER NOT NULL,
            created_at INTEGER NOT NULL
        )""")
    }

    /** Lists are stored newline-joined — no JSON dependency needed. */
    private fun join(items: List<String>) = items.joinToString("\n")
    private fun split(raw: String) = raw.split("\n").filter { it.isNotBlank() }

    fun saveSermonNote(
        title: String, scriptures: List<String>, points: List<String>,
        quotes: List<String>, takeaway: String, transcript: String, minutes: Int,
    ): Long = writableDatabase.insert("sermon_notes", null, ContentValues().apply {
        put("title", title)
        put("scriptures", join(scriptures))
        put("points", join(points))
        put("quotes", join(quotes))
        put("takeaway", takeaway)
        put("transcript", transcript)
        put("minutes", minutes)
        put("created_at", System.currentTimeMillis())
    })

    fun sermonNotes(): List<SermonNote> = readableDatabase.query(
        "sermon_notes", null, null, null, null, null, "created_at DESC"
    ).use { c ->
        buildList {
            while (c.moveToNext()) add(SermonNote(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("title")),
                split(c.getString(c.getColumnIndexOrThrow("scriptures"))),
                split(c.getString(c.getColumnIndexOrThrow("points"))),
                split(c.getString(c.getColumnIndexOrThrow("quotes"))),
                c.getString(c.getColumnIndexOrThrow("takeaway")),
                c.getString(c.getColumnIndexOrThrow("transcript")),
                c.getInt(c.getColumnIndexOrThrow("minutes")),
                c.getLong(c.getColumnIndexOrThrow("created_at")),
            ))
        }
    }

    fun deleteSermonNote(id: Long) {
        writableDatabase.delete("sermon_notes", "id = ?", arrayOf(id.toString()))
    }

    fun addJournalEntry(
        mood: String, body: String, gratitude: String, verseRef: String?, verseText: String?,
        source: String = "write", isPrayer: Boolean = false,
        title: String = "", tags: List<String> = emptyList(), journal: String = "My Journey",
        favorite: Boolean = false, location: String = "", weather: String = "", media: List<String> = emptyList(),
        entryAt: Long = System.currentTimeMillis(),
    ): Long {
        val now = System.currentTimeMillis()
        return writableDatabase.insert("journal_entries", null, ContentValues().apply {
            put("entry_day", java.time.Instant.ofEpochMilli(entryAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay())
            put("mood", mood); put("body", body.trim()); put("gratitude", gratitude.trim())
            put("verse_ref", verseRef); put("verse_text", verseText)
            put("created_at", entryAt); put("updated_at", now)
            put("source", source); put("is_prayer", if (isPrayer) 1 else 0)
            put("title", title.trim()); put("tags", tags.joinToString("\n")); put("journal_name", journal.ifBlank { "My Journey" })
            put("favorite", if (favorite) 1 else 0); put("location", location.trim()); put("weather", weather.trim()); put("media", media.joinToString("\n"))
        })
    }

    /** A prayer entry that God answered — the proof pile. */
    fun answerJournalEntry(id: Long, testimony: String) {
        writableDatabase.update("journal_entries", ContentValues().apply {
            put("answered_at", System.currentTimeMillis())
            put("testimony", testimony.trim())
            put("prayer_status", PrayerStage.Answered.key)
            put("updated_at", System.currentTimeMillis())
        }, "id = ?", arrayOf(id.toString()))
    }

    fun updatePrayerJourney(id: Long, stage: PrayerStage, nextAction: String, testimony: String) {
        val answered = stage == PrayerStage.Answered || stage == PrayerStage.AnsweredDifferently
        writableDatabase.update("journal_entries", ContentValues().apply {
            put("prayer_status", stage.key)
            put("next_action", nextAction.trim())
            put("updated_at", System.currentTimeMillis())
            if (answered) {
                put("answered_at", System.currentTimeMillis())
                put("testimony", testimony.trim())
            } else {
                putNull("answered_at")
                putNull("testimony")
            }
        }, "id = ?", arrayOf(id.toString()))
    }

    fun updateJournalEntry(
        id: Long, mood: String, body: String, gratitude: String, isPrayer: Boolean? = null,
        title: String? = null, tags: List<String>? = null, journal: String? = null,
        favorite: Boolean? = null, location: String? = null, weather: String? = null, media: List<String>? = null,
        entryAt: Long? = null,
    ) {
        writableDatabase.update("journal_entries", ContentValues().apply {
            put("mood", mood); put("body", body.trim()); put("gratitude", gratitude.trim())
            put("updated_at", System.currentTimeMillis())
            isPrayer?.let { put("is_prayer", if (it) 1 else 0) }
            title?.let { put("title", it.trim()) }; tags?.let { put("tags", it.joinToString("\n")) }; journal?.let { put("journal_name", it.ifBlank { "My Journey" }) }
            favorite?.let { put("favorite", if (it) 1 else 0) }; location?.let { put("location", it.trim()) }; weather?.let { put("weather", it.trim()) }; media?.let { put("media", it.joinToString("\n")) }
            entryAt?.let { value -> put("created_at", value); put("entry_day", java.time.Instant.ofEpochMilli(value).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()) }
        }, "id = ?", arrayOf(id.toString()))
    }

    fun deleteJournalEntry(id: Long) {
        writableDatabase.delete("journal_entries", "id = ?", arrayOf(id.toString()))
    }

    fun journalEntries(): List<JournalEntry> = readableDatabase.query(
        "journal_entries", null, null, null, null, null, "created_at DESC"
    ).use { cursor ->
        buildList { while (cursor.moveToNext()) add(JournalEntry(
            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            cursor.getLong(cursor.getColumnIndexOrThrow("entry_day")),
            cursor.getString(cursor.getColumnIndexOrThrow("mood")),
            cursor.getString(cursor.getColumnIndexOrThrow("body")),
            cursor.getString(cursor.getColumnIndexOrThrow("gratitude")),
            cursor.getColumnIndexOrThrow("verse_ref").let { if (cursor.isNull(it)) null else cursor.getString(it) },
            cursor.getColumnIndexOrThrow("verse_text").let { if (cursor.isNull(it)) null else cursor.getString(it) },
            cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")),
            cursor.getColumnIndex("source").let { if (it < 0 || cursor.isNull(it)) "write" else cursor.getString(it) },
            cursor.getColumnIndex("is_prayer").let { it >= 0 && cursor.getInt(it) == 1 },
            cursor.getColumnIndex("answered_at").let { if (it < 0 || cursor.isNull(it)) null else cursor.getLong(it) },
            cursor.getColumnIndex("testimony").let { if (it < 0 || cursor.isNull(it)) null else cursor.getString(it) },
            PrayerStage.from(cursor.getColumnIndex("prayer_status").let { if (it < 0 || cursor.isNull(it)) null else cursor.getString(it) }),
            cursor.getColumnIndex("next_action").let { if (it < 0 || cursor.isNull(it)) "" else cursor.getString(it) },
            cursor.getColumnIndex("title").let { if (it < 0 || cursor.isNull(it)) "" else cursor.getString(it) },
            cursor.getColumnIndex("tags").let { if (it < 0 || cursor.isNull(it)) emptyList() else cursor.getString(it).lines().filter(String::isNotBlank) },
            cursor.getColumnIndex("journal_name").let { if (it < 0 || cursor.isNull(it)) "My Journey" else cursor.getString(it) },
            cursor.getColumnIndex("favorite").let { it >= 0 && cursor.getInt(it) == 1 },
            cursor.getColumnIndex("location").let { if (it < 0 || cursor.isNull(it)) "" else cursor.getString(it) },
            cursor.getColumnIndex("weather").let { if (it < 0 || cursor.isNull(it)) "" else cursor.getString(it) },
            cursor.getColumnIndex("media").let { if (it < 0 || cursor.isNull(it)) emptyList() else cursor.getString(it).lines().filter(String::isNotBlank) },
        )) }
    }

    /** Consecutive days (ending today or yesterday) with at least one entry. */
    fun journalStreak(): Int {
        val days = journalEntries().map { it.entryDay }.toSortedSet().reversed()
        if (days.isEmpty()) return 0
        val today = java.time.LocalDate.now().toEpochDay()
        var cursor = when (days.first()) { today, today - 1 -> days.first(); else -> return 0 }
        var count = 0
        for (day in days) {
            if (day == cursor) { count++; cursor-- } else if (day < cursor) break
        }
        return count
    }

    fun savePrayer(title: String, request: String, prayer: String, scriptureRef: String?) {
        writableDatabase.insert("prayer_journal", null, ContentValues().apply {
            put("title", title); put("request", request); put("prayer", prayer); put("scripture_ref", scriptureRef); put("created_at", System.currentTimeMillis())
        })
    }

    fun prayerJournal(): List<JournalPrayer> = readableDatabase.query("prayer_journal", null, null, null, null, null, "created_at DESC").use { cursor ->
        buildList { while (cursor.moveToNext()) add(JournalPrayer(
            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            cursor.getString(cursor.getColumnIndexOrThrow("title")),
            cursor.getString(cursor.getColumnIndexOrThrow("request")),
            cursor.getString(cursor.getColumnIndexOrThrow("prayer")),
            cursor.getColumnIndexOrThrow("scripture_ref").let { if (cursor.isNull(it)) null else cursor.getString(it) },
            cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
        )) }
    }

    fun startSermon(title: String = "Sunday service"): Long = writableDatabase.insert("sermon_sessions", null, ContentValues().apply {
        put("title", title); put("started_at", System.currentTimeMillis())
    })

    fun addSermonVerse(sessionId: Long, reference: String, text: String) {
        writableDatabase.insertWithOnConflict("sermon_verses", null, ContentValues().apply {
            put("session_id", sessionId); put("reference", reference); put("verse", text); put("detected_at", System.currentTimeMillis())
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun endSermon(sessionId: Long) {
        writableDatabase.update("sermon_sessions", ContentValues().apply { put("ended_at", System.currentTimeMillis()) }, "id = ?", arrayOf(sessionId.toString()))
    }

    fun sermons(): List<SermonSession> = readableDatabase.query("sermon_sessions", null, null, null, null, null, "started_at DESC").use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val verses = readableDatabase.query("sermon_verses", null, "session_id = ?", arrayOf(id.toString()), null, null, "detected_at ASC").use { verseCursor ->
                    buildList { while (verseCursor.moveToNext()) add(SermonVerse(
                        verseCursor.getString(verseCursor.getColumnIndexOrThrow("reference")),
                        verseCursor.getString(verseCursor.getColumnIndexOrThrow("verse")),
                        verseCursor.getLong(verseCursor.getColumnIndexOrThrow("detected_at")),
                    )) }
                }
                add(SermonSession(id, cursor.getString(cursor.getColumnIndexOrThrow("title")), cursor.getLong(cursor.getColumnIndexOrThrow("started_at")), cursor.getColumnIndexOrThrow("ended_at").let { if (cursor.isNull(it)) null else cursor.getLong(it) }, verses))
            }
        }
    }

    fun memorize(card: VerseCard) {
        writableDatabase.insertWithOnConflict("memory_verses", null, ContentValues().apply {
            put("reference", card.reference); put("verse", card.verse); put("stage", 1)
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun reviewMemory(reference: String, correct: Boolean) {
        val current = memoryVerses().firstOrNull { it.reference == reference } ?: return
        val review = MemorySchedule.after(current, correct, System.currentTimeMillis())
        writableDatabase.update("memory_verses", ContentValues().apply {
            put("stage", review.stage)
            put("correct_count", review.correctCount)
            put("next_review_at", review.nextReviewAt)
        }, "reference = ?", arrayOf(reference))
    }

    fun memoryVerses(): List<MemoryVerse> = readableDatabase.query("memory_verses", null, null, null, null, null, "next_review_at ASC, stage ASC").use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(MemoryVerse(
                cursor.getString(cursor.getColumnIndexOrThrow("reference")),
                cursor.getString(cursor.getColumnIndexOrThrow("verse")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stage")),
                cursor.getColumnIndex("correct_count").let { if (it < 0) 0 else cursor.getInt(it) },
                cursor.getColumnIndex("next_review_at").let { if (it < 0) 0L else cursor.getLong(it) },
            ))
        }
    }

    private fun state(key: String): String? = readableDatabase.query(
        "app_state", arrayOf("state_value"), "state_key = ?", arrayOf(key), null, null, null
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    private fun putState(key: String, value: String) {
        writableDatabase.insertWithOnConflict("app_state", null, ContentValues().apply {
            put("state_key", key); put("state_value", value)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun preferences(): UserPrefs = UserPrefs(
        name = state("name") ?: "",
        reminderHour = state("reminder_hour")?.toIntOrNull() ?: 7,
        reminderMinute = state("reminder_minute")?.toIntOrNull() ?: 0,
        reminderEnabled = state("reminder_enabled") == "true",
        reduceMotion = state("reduce_motion") == "true",
        translation = state("translation") ?: "KJV",
        onboarded = state("onboarded") == "true",
        sermonLanguage = state("sermon_language") ?: "en-US",
        journalLock = state("journal_lock") == "true",
        concealJournalPreviews = state("journal_conceal") == "true",
        readerTextSize = state("reader_size")?.toIntOrNull() ?: 17,
        readerRibbon = state("reader_ribbon")?.takeIf { it.isNotBlank() },
        /* The theme was in this model but in neither the read nor the write,
           so choosing one lasted exactly as long as the process did. */
        themeId = state("theme") ?: DEFAULT_THEME_ID,
        followSystemTheme = state("theme_follow_system") != "false",
    )

    fun savePreferences(prefs: UserPrefs) {
        putState("name", prefs.name); putState("reminder_hour", prefs.reminderHour.toString())
        putState("reminder_minute", prefs.reminderMinute.toString()); putState("reminder_enabled", prefs.reminderEnabled.toString())
        putState("reduce_motion", prefs.reduceMotion.toString()); putState("translation", prefs.translation)
        putState("onboarded", prefs.onboarded.toString())
        putState("sermon_language", prefs.sermonLanguage)
        putState("journal_lock", prefs.journalLock.toString()); putState("journal_conceal", prefs.concealJournalPreviews.toString())
        putState("reader_size", prefs.readerTextSize.toString()); putState("reader_ribbon", prefs.readerRibbon.orEmpty())
        putState("theme", prefs.themeId); putState("theme_follow_system", prefs.followSystemTheme.toString())
    }

    fun formation(): FormationState = FormationState(
        morningWord = state("formation_morning")?.toBooleanStrictOrNull() ?: true,
        middayPrayer = state("formation_midday")?.toBooleanStrictOrNull() ?: false,
        eveningExamen = state("formation_evening")?.toBooleanStrictOrNull() ?: true,
        sabbathDay = state("formation_sabbath") ?: "Sunday",
        pilgrimageId = state("formation_pilgrimage") ?: "",
        pilgrimageDay = state("formation_pilgrimage_day")?.toIntOrNull() ?: 0,
        familyNames = state("formation_family") ?: "",
        trustedPhone = state("formation_trusted_phone") ?: "",
    )

    fun saveFormation(value: FormationState) {
        putState("formation_morning", value.morningWord.toString())
        putState("formation_midday", value.middayPrayer.toString())
        putState("formation_evening", value.eveningExamen.toString())
        putState("formation_sabbath", value.sabbathDay)
        putState("formation_pilgrimage", value.pilgrimageId)
        putState("formation_pilgrimage_day", value.pilgrimageDay.toString())
        putState("formation_family", value.familyNames)
        putState("formation_trusted_phone", value.trustedPhone)
    }

    /** Replaces journey content from a MANNA-owned archive. The caller must
     * obtain explicit confirmation because restore intentionally replaces the
     * current private journey rather than creating silent duplicates. */
    fun restoreArchive(raw: String) {
        val root = JSONObject(raw)
        require(root.optString("format").startsWith("manna-journey-v")) { "Not a MANNA journey archive" }
        writableDatabase.beginTransaction()
        try {
            listOf("journal_entries", "sermon_notes", "prayer_journal", "saved_words", "memory_verses").forEach {
                writableDatabase.delete(it, null, null)
            }
            root.optJSONArray("entries")?.let { rows -> repeat(rows.length()) { index ->
                val item = rows.getJSONObject(index)
                writableDatabase.insertOrThrow("journal_entries", null, ContentValues().apply {
                    val created = item.optLong("createdAt", System.currentTimeMillis())
                    put("entry_day", java.time.Instant.ofEpochMilli(created).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay())
                    put("mood", item.optString("mood", "🙏")); put("body", item.optString("body")); put("gratitude", item.optString("gratitude"))
                    putNullable("verse_ref", item, "verseRef"); putNullable("verse_text", item, "verseText")
                    put("created_at", created); put("updated_at", created); put("source", item.optString("source", "write")); put("is_prayer", if (item.optBoolean("isPrayer")) 1 else 0)
                    put("prayer_status", item.optString("prayerStage", "praying")); put("next_action", item.optString("nextAction"))
                    putNullableLong("answered_at", item, "answeredAt"); putNullable("testimony", item)
                    put("title", item.optString("title")); put("tags", jsonLines(item, "tags")); put("journal_name", item.optString("journal", "My Journey")); put("favorite", if (item.optBoolean("favorite")) 1 else 0)
                    put("location", item.optString("location")); put("weather", item.optString("weather")); put("media", jsonLines(item, "media"))
                })
            } }
            root.optJSONArray("sermons")?.let { rows -> repeat(rows.length()) { index ->
                val item = rows.getJSONObject(index)
                writableDatabase.insertOrThrow("sermon_notes", null, ContentValues().apply {
                    put("title", item.optString("title")); put("scriptures", jsonLines(item, "scriptures")); put("points", jsonLines(item, "points")); put("quotes", jsonLines(item, "quotes")); put("takeaway", item.optString("takeaway")); put("transcript", item.optString("transcript")); put("minutes", item.optInt("minutes")); put("created_at", item.optLong("createdAt", System.currentTimeMillis()))
                })
            } }
            root.optJSONArray("prayers")?.let { rows -> repeat(rows.length()) { index ->
                val item = rows.getJSONObject(index)
                writableDatabase.insertOrThrow("prayer_journal", null, ContentValues().apply { put("title", item.optString("title")); put("request", item.optString("request")); put("prayer", item.optString("prayer")); putNullable("scripture_ref", item, "scriptureRef"); put("created_at", item.optLong("createdAt", System.currentTimeMillis())) })
            } }
            root.optJSONArray("savedWords")?.let { rows -> repeat(rows.length()) { index ->
                val item = rows.getJSONObject(index)
                writableDatabase.insertWithOnConflict("saved_words", null, ContentValues().apply { put("reference", item.optString("reference")); put("translation", item.optString("translation", "KJV")); put("verse", item.optString("verse")); put("saved_at", item.optLong("savedAt", System.currentTimeMillis())); putNullableLong("answered_at", item, "answeredAt"); putNullable("testimony", item) }, SQLiteDatabase.CONFLICT_IGNORE)
            } }
            root.optJSONArray("memory")?.let { rows -> repeat(rows.length()) { index ->
                val item = rows.getJSONObject(index)
                writableDatabase.insertWithOnConflict("memory_verses", null, ContentValues().apply { put("reference", item.optString("reference")); put("verse", item.optString("verse")); put("stage", item.optInt("stage", 1)); put("correct_count", item.optInt("correctCount")); put("next_review_at", item.optLong("nextReviewAt")) }, SQLiteDatabase.CONFLICT_REPLACE)
            } }
            root.optJSONObject("formation")?.let { item -> saveFormation(FormationState(
                item.optBoolean("morningWord", true), item.optBoolean("middayPrayer"), item.optBoolean("eveningExamen", true), item.optString("sabbathDay", "Sunday"), item.optString("pilgrimageId"), item.optInt("pilgrimageDay"), item.optString("familyNames"), item.optString("trustedPhone"),
            )) }
            writableDatabase.setTransactionSuccessful()
        } finally { writableDatabase.endTransaction() }
    }

    private fun ContentValues.putNullable(column: String, json: JSONObject, key: String = column) {
        if (json.has(key) && !json.isNull(key)) put(column, json.optString(key)) else putNull(column)
    }
    private fun ContentValues.putNullableLong(column: String, json: JSONObject, key: String) {
        if (json.has(key) && !json.isNull(key)) put(column, json.optLong(key)) else putNull(column)
    }
    private fun jsonLines(json: JSONObject, key: String): String = json.optJSONArray(key)?.let { rows -> buildList { repeat(rows.length()) { add(rows.optString(it)) } }.joinToString("\n") }.orEmpty()

    fun streak(): Int = state("streak")?.toIntOrNull() ?: 0

    fun recordDailyPull(): Int {
        val today = java.time.LocalDate.now().toEpochDay()
        val last = state("last_pull_day")?.toLongOrNull()
        val next = when (last) {
            today -> streak()
            today - 1 -> streak() + 1
            else -> 1
        }
        putState("last_pull_day", today.toString()); putState("streak", next.toString())
        return next
    }

    fun save(card: VerseCard) {
        val values = ContentValues().apply {
            put("reference", card.reference); put("translation", card.translation)
            put("verse", card.verse); put("saved_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("saved_words", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun markAnswered(id: Long, testimony: String) {
        val values = ContentValues().apply {
            put("answered_at", System.currentTimeMillis()); put("testimony", testimony.trim())
        }
        writableDatabase.update("saved_words", values, "id = ?", arrayOf(id.toString()))
    }

    fun all(): List<SavedWord> = readableDatabase.query(
        "saved_words", null, null, null, null, null, "saved_at DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(
                SavedWord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    reference = cursor.getString(cursor.getColumnIndexOrThrow("reference")),
                    translation = cursor.getString(cursor.getColumnIndexOrThrow("translation")),
                    verse = cursor.getString(cursor.getColumnIndexOrThrow("verse")),
                    savedAt = cursor.getLong(cursor.getColumnIndexOrThrow("saved_at")),
                    answeredAt = cursor.getColumnIndexOrThrow("answered_at").let { if (cursor.isNull(it)) null else cursor.getLong(it) },
                    testimony = cursor.getColumnIndexOrThrow("testimony").let { if (cursor.isNull(it)) null else cursor.getString(it) },
                )
            )
        }
    }
}
