package com.prayerkey.manna.ui.share

import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.PlatformContext

/** Shares nothing: the stand-in before a platform installs its own. */
object NoShareCards : ShareCards {
    override fun shareStatus(context: PlatformContext, card: VerseCard) = Unit
    override fun saveStatus(context: PlatformContext, card: VerseCard) = Unit
    override fun sharePrayerStatus(context: PlatformContext, prayer: String) = Unit
    override fun savePrayerStatus(context: PlatformContext, prayer: String) = Unit
    override fun shareClosingVerse(context: PlatformContext, verse: String, reference: String) = Unit
    override fun saveClosingVerse(context: PlatformContext, verse: String, reference: String) = Unit
    override fun shareVerse(context: PlatformContext, card: VerseCard) = Unit
    override fun sharePrayer(context: PlatformContext, topic: PrayerTopic) = Unit
    override fun shareAnswered(context: PlatformContext, request: String, testimony: String, answeredAt: Long) = Unit
    override fun shareBadge(context: PlatformContext, title: String, detail: String) = Unit
    override fun sharePrayerCard(context: PlatformContext, prayer: DailyPrayer, format: CardFormat) = Unit
    override fun shareSundayNotes(context: PlatformContext, note: SermonNote) = Unit
    override fun shareRemembranceBook(
        context: PlatformContext, name: String, entries: List<JournalEntry>, sermons: List<SermonNote>,
        prayers: List<JournalPrayer>, saved: List<SavedWord>,
    ) = Unit
    override fun exportArchive(
        context: PlatformContext, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>,
        saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState,
    ) = Unit
}
