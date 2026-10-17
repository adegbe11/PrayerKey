package com.prayerkey.manna.data

import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.AppContext
import com.prayerkey.manna.platform.today
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JsonMannaStoreTest {

    private fun store() = JsonMannaStore(AppContext, "test-${kotlin.random.Random.nextLong()}.json")

    @Test
    fun aVerseIsSavedOncePerTranslation() {
        val s = store()
        s.save(VerseCard("John 3:16", "KJV", "For God so loved", ""))
        s.save(VerseCard("John 3:16", "KJV", "For God so loved", ""))
        s.save(VerseCard("John 3:16", "WEB", "For God so loved", ""))
        assertEquals(2, s.all().size)
        val id = s.all().first().id
        s.markAnswered(id, "  He did  ")
        assertEquals("He did", s.all().first { it.id == id }.testimony)
    }

    @Test
    fun theJournalIsNewestFirstAndCountsAStreak() {
        val s = store()
        val day = 86_400_000L
        val now = nowMillis()
        s.addJournalEntry("🙏", " first ", "", null, null, entryAt = now - 2 * day)
        s.addJournalEntry("🙏", "second", "", null, null, entryAt = now - day)
        val third = s.addJournalEntry("🙏", "third", "", null, null, entryAt = now)
        assertEquals(listOf("third", "second", "first"), s.journalEntries().map { it.body })
        assertEquals(3, s.journalStreak())
        s.answerJournalEntry(third, "answered")
        assertEquals(PrayerStage.Answered, s.journalEntries().first().prayerStage)
        s.updatePrayerJourney(third, PrayerStage.Waiting, "wait", "x")
        assertNull(s.journalEntries().first().answeredAt)
        s.deleteJournalEntry(third)
        assertEquals(2, s.journalEntries().size)
    }

    @Test
    fun settingsAndTheDailyStreakSurvive() {
        val s = store()
        s.savePreferences(UserPrefs(name = "Grace", reminderHour = 6, readerRibbon = "Psalms 23", followSystemTheme = false))
        val p = s.preferences()
        assertEquals("Grace", p.name); assertEquals(6, p.reminderHour); assertEquals("Psalms 23", p.readerRibbon)
        assertEquals(false, p.followSystemTheme)
        assertEquals(1, s.recordDailyPull())
        assertEquals(1, s.recordDailyPull())
        s.setChallengeDay("c", 0, true); s.setChallengeDay("c", 1, true); s.setChallengeDay("c", 0, false)
        assertEquals(setOf(1), s.challengeDays("c"))
    }

    @Test
    fun aBackupRestoresAndAForeignFileIsRefused() {
        val s = store()
        s.addJournalEntry("🙏", "will be replaced", "", null, null)
        s.restoreArchive(
            """{"format":"manna-journey-v2",
               "entries":[{"createdAt":1700000000000,"body":"restored","isPrayer":true,"tags":["a","","b"],"prayerStage":"waiting"}],
               "prayers":[{"title":"t","request":"r","prayer":"p","createdAt":1}],
               "savedWords":[{"reference":"Ps 23:1","verse":"The LORD","savedAt":5},{"reference":"Ps 23:1","verse":"dup","savedAt":6}],
               "memory":[{"reference":"Ps 23:1","verse":"The LORD","stage":2}],
               "formation":{"sabbathDay":"Saturday","middayPrayer":true}}""",
        )
        val e = s.journalEntries().single()
        assertEquals("restored", e.body); assertTrue(e.isPrayer); assertEquals(listOf("a", "b"), e.tags)
        assertEquals(PrayerStage.Waiting, e.prayerStage)
        assertEquals(1, s.all().size)
        assertEquals(2, s.memoryVerses().single().stage)
        assertEquals("Saturday", s.formation().sabbathDay)
        assertTrue(s.formation().middayPrayer)
        assertFailsWith<IllegalArgumentException> { s.restoreArchive("""{"format":"something-else"}""") }
        assertEquals("restored", s.journalEntries().single().body)
    }

    @Test
    fun sermonsKeepEachVerseOnce() {
        val s = store()
        val id = s.startSermon()
        s.addSermonVerse(id, "John 1:1", "In the beginning")
        s.addSermonVerse(id, "John 1:1", "In the beginning")
        s.endSermon(id)
        assertEquals(1, s.sermons().single().verses.size)
        val note = s.saveSermonNote("Grace", listOf("John 1:1", ""), listOf("p"), emptyList(), "t", "x", 40)
        s.refineSermonNote(note, "Better", listOf("John 1:1"), emptyList(), emptyList(), "t", "y")
        assertTrue(s.sermonNotes().single().refined)
        assertEquals(today().toEpochDays().toLong(), epochDayOf(nowMillis()))
    }
}
