package com.prayerkey.manna

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prayerkey.manna.data.MannaStore
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryPreservationTest {
    @Test fun downgradeNeverDeletesPrayerHistory() {
        SQLiteDatabase.create(null).use { db ->
            db.execSQL("CREATE TABLE journal_entries (body TEXT NOT NULL)")
            db.execSQL("INSERT INTO journal_entries VALUES ('My daughter')")
            MannaStore(ApplicationProvider.getApplicationContext()).use { store ->
                try { store.onDowngrade(db, 15, 14); fail("Downgrade must be refused") }
                catch (_: SQLiteException) { }
            }
            db.rawQuery("SELECT body FROM journal_entries", null).use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("My daughter", cursor.getString(0))
            }
        }
    }
}
