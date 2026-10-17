package com.prayerkey.manna.share

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
import com.prayerkey.manna.ui.share.CardFormat
import com.prayerkey.manna.ui.share.ShareCards

/** The shared screens' cards, drawn by Android's own renderers, unchanged. */
object AndroidShareCards : ShareCards {
    override fun shareStatus(context: PlatformContext, card: VerseCard) = CardShareRenderer.shareStatus(context, card)
    override fun saveStatus(context: PlatformContext, card: VerseCard) = CardShareRenderer.saveStatus(context, card)
    override fun sharePrayerStatus(context: PlatformContext, prayer: String) = CardShareRenderer.sharePrayerStatus(context, prayer)
    override fun savePrayerStatus(context: PlatformContext, prayer: String) = CardShareRenderer.savePrayerStatus(context, prayer)
    override fun shareClosingVerse(context: PlatformContext, verse: String, reference: String) =
        CardShareRenderer.shareClosingVerse(context, verse, reference)
    override fun saveClosingVerse(context: PlatformContext, verse: String, reference: String) =
        CardShareRenderer.saveClosingVerse(context, verse, reference)
    override fun shareVerse(context: PlatformContext, card: VerseCard) = CardShareRenderer.share(context, card)
    override fun sharePrayer(context: PlatformContext, topic: PrayerTopic) = CardShareRenderer.sharePrayer(context, topic)
    override fun shareAnswered(context: PlatformContext, request: String, testimony: String, answeredAt: Long) =
        AnsweredPrayerCardRenderer.share(context, request, testimony, answeredAt)
    override fun shareBadge(context: PlatformContext, title: String, detail: String) = ChallengeBadgeRenderer.share(context, title, detail)
    override fun sharePrayerCard(context: PlatformContext, prayer: DailyPrayer, format: CardFormat) =
        PrayerCardRenderer.share(context, prayer, format)
    override fun shareSundayNotes(context: PlatformContext, note: SermonNote) = MannaPack.share(context, note)
    override fun shareRemembranceBook(
        context: PlatformContext, name: String, entries: List<JournalEntry>, sermons: List<SermonNote>,
        prayers: List<JournalPrayer>, saved: List<SavedWord>,
    ) = RemembranceBook.share(context, name, entries, sermons, prayers, saved)
    override fun exportArchive(
        context: PlatformContext, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>,
        saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState,
    ) = JourneyArchive.share(context, entries, sermons, prayers, saved, memory, formation)
}
