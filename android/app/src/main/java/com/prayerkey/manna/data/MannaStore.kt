package com.prayerkey.manna.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.prayerkey.manna.model.VerseCard

data class SavedWord(
    val id: Long,
    val reference: String,
    val translation: String,
    val verse: String,
    val savedAt: Long,
    val answeredAt: Long? = null,
    val testimony: String? = null,
)

data class MemoryVerse(val reference: String, val verse: String, val stage: Int)
data class UserPrefs(val name: String = "Collins", val reminderHour: Int = 7, val reminderMinute: Int = 0, val reminderEnabled: Boolean = false, val reduceMotion: Boolean = false)
data class SermonVerse(val reference: String, val text: String, val detectedAt: Long)
data class SermonSession(val id: Long, val title: String, val startedAt: Long, val endedAt: Long?, val verses: List<SermonVerse>)
data class JournalPrayer(val id: Long, val title: String, val request: String, val prayer: String, val scriptureRef: String?, val createdAt: Long)

class MannaStore(context: Context) : SQLiteOpenHelper(context, "manna.db", null, 5) {
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createStateTable(db)
        if (oldVersion < 3) createMemoryTable(db)
        if (oldVersion < 4) createSermonTables(db)
        if (oldVersion < 5) createPrayerJournal(db)
    }

    private fun createStateTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS app_state (state_key TEXT PRIMARY KEY, state_value TEXT NOT NULL)")
    }

    private fun createMemoryTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS memory_verses (reference TEXT PRIMARY KEY, verse TEXT NOT NULL, stage INTEGER NOT NULL DEFAULT 1)")
    }

    private fun createSermonTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS sermon_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, started_at INTEGER NOT NULL, ended_at INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS sermon_verses (id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER NOT NULL, reference TEXT NOT NULL, verse TEXT NOT NULL, detected_at INTEGER NOT NULL, UNIQUE(session_id, reference))")
    }

    private fun createPrayerJournal(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS prayer_journal (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, request TEXT NOT NULL, prayer TEXT NOT NULL, scripture_ref TEXT, created_at INTEGER NOT NULL)")
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

    fun advanceMemory(reference: String) {
        writableDatabase.execSQL("UPDATE memory_verses SET stage = MIN(stage + 1, 5) WHERE reference = ?", arrayOf(reference))
    }

    fun memoryVerses(): List<MemoryVerse> = readableDatabase.query("memory_verses", null, null, null, null, null, "stage ASC").use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(MemoryVerse(
                cursor.getString(cursor.getColumnIndexOrThrow("reference")),
                cursor.getString(cursor.getColumnIndexOrThrow("verse")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stage")),
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
        name = state("name") ?: "Collins",
        reminderHour = state("reminder_hour")?.toIntOrNull() ?: 7,
        reminderMinute = state("reminder_minute")?.toIntOrNull() ?: 0,
        reminderEnabled = state("reminder_enabled") == "true",
        reduceMotion = state("reduce_motion") == "true",
    )

    fun savePreferences(prefs: UserPrefs) {
        putState("name", prefs.name); putState("reminder_hour", prefs.reminderHour.toString())
        putState("reminder_minute", prefs.reminderMinute.toString()); putState("reminder_enabled", prefs.reminderEnabled.toString())
        putState("reduce_motion", prefs.reduceMotion.toString())
    }

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
