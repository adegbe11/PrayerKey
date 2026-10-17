package com.prayerkey.manna.ui.share

import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.data.nowMillis
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.PlatformContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Prayer of the Day in each social shape. */
enum class CardFormat(val label: String, val w: Int, val h: Int) {
    SQUARE("Instagram", 1080, 1080),
    STORY("Story / TikTok", 1080, 1920),
    FACEBOOK("Facebook", 1200, 630),
    TWITTER("Twitter / X", 1200, 675),
    PINTEREST("Pinterest", 1000, 1500),
}

/**
 * Everything the app shares as a picture, a document or a page of text.
 *
 * Android keeps the renderers it has always had, drawn on its own canvas, so
 * its cards do not change by a pixel; the app installs them at launch. iPhone
 * draws the same layouts with Compose ([ComposeCards]).
 */
interface ShareCards {
    fun shareStatus(context: PlatformContext, card: VerseCard)
    fun saveStatus(context: PlatformContext, card: VerseCard)
    fun sharePrayerStatus(context: PlatformContext, prayer: String)
    fun savePrayerStatus(context: PlatformContext, prayer: String)
    fun shareClosingVerse(context: PlatformContext, verse: String, reference: String)
    fun saveClosingVerse(context: PlatformContext, verse: String, reference: String)
    fun shareVerse(context: PlatformContext, card: VerseCard)
    fun sharePrayer(context: PlatformContext, topic: PrayerTopic)
    fun shareAnswered(context: PlatformContext, request: String, testimony: String, answeredAt: Long)
    fun shareBadge(context: PlatformContext, title: String, detail: String)
    fun sharePrayerCard(context: PlatformContext, prayer: DailyPrayer, format: CardFormat)
    fun shareSundayNotes(context: PlatformContext, note: SermonNote)
    fun shareRemembranceBook(
        context: PlatformContext, name: String, entries: List<JournalEntry>, sermons: List<SermonNote>,
        prayers: List<JournalPrayer>, saved: List<SavedWord>,
    )
    fun exportArchive(
        context: PlatformContext, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>,
        saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState,
    )
}

object Share {
    var cards: ShareCards = defaultShareCards()
}

expect fun defaultShareCards(): ShareCards

/** A sermon's notes as a page a small group can use. Nothing private goes with it. */
fun sundayNotesText(note: SermonNote): String {
    val questions = note.points.take(3).mapIndexed { index, point ->
        "${index + 1}. ${point.trim().take(100)} - where does this meet ordinary life this week?"
    }.ifEmpty { listOf("1. What stayed with you from this message?", "2. What might faithful action look like this week?") }
    val scripture = note.scriptures.joinToString(" · ").ifBlank { "Review the sermon passage" }
    return buildString {
        appendLine("SUNDAY NOTES · PRAYERKEY")
        appendLine(note.title)
        appendLine()
        appendLine("READ")
        appendLine(scripture)
        appendLine()
        appendLine("CARRY")
        appendLine(note.takeaway.ifBlank { note.points.firstOrNull() ?: "Carry one truth from this message into the week." })
        appendLine()
        appendLine("TALK IT OVER")
        questions.forEach(::appendLine)
        appendLine()
        appendLine("PRAY")
        appendLine("God, help us receive what is true, practice it with love, and remember it when the week becomes difficult. Amen.")
        appendLine()
        appendLine("ONE ACTION")
        appendLine("Choose one small, specific response and tell someone you trust.")
        appendLine()
        appendLine("Shared from PrayerKey. Nothing private from a journal or prayer list is included.")
    }
}

/** The private backup, in the format Android writes and both stores restore (manna-journey-v3). */
fun archiveJson(
    entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>,
    saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState,
): String {
    fun strings(values: List<String>) = JsonArray(values.map { JsonPrimitive(it) })
    return buildJsonObject {
        put("format", "manna-journey-v3")
        put("createdAt", nowMillis())
        putJsonArray("entries") {
            entries.forEach {
                add(buildJsonObject {
                    put("mood", it.mood); put("title", it.title); put("body", it.body); put("gratitude", it.gratitude)
                    put("verseRef", it.verseRef); put("verseText", it.verseText); put("createdAt", it.createdAt); put("updatedAt", it.updatedAt)
                    put("source", it.source); put("isPrayer", it.isPrayer); put("prayerStage", it.prayerStage.key); put("nextAction", it.nextAction)
                    put("answeredAt", it.answeredAt); put("testimony", it.testimony); put("tags", strings(it.tags)); put("journal", it.journal)
                    put("favorite", it.favorite); put("location", it.location); put("weather", it.weather); put("media", strings(it.media))
                })
            }
        }
        putJsonArray("sermons") {
            sermons.forEach {
                add(buildJsonObject {
                    put("title", it.title); put("scriptures", strings(it.scriptures)); put("points", strings(it.points))
                    put("quotes", strings(it.quotes)); put("takeaway", it.takeaway); put("transcript", it.transcript)
                    put("minutes", it.minutes); put("createdAt", it.createdAt)
                })
            }
        }
        putJsonArray("prayers") {
            prayers.forEach {
                add(buildJsonObject {
                    put("title", it.title); put("request", it.request); put("prayer", it.prayer)
                    put("scriptureRef", it.scriptureRef); put("createdAt", it.createdAt)
                })
            }
        }
        putJsonArray("savedWords") {
            saved.forEach {
                add(buildJsonObject {
                    put("reference", it.reference); put("translation", it.translation); put("verse", it.verse)
                    put("savedAt", it.savedAt); put("answeredAt", it.answeredAt); put("testimony", it.testimony)
                })
            }
        }
        putJsonArray("memory") {
            memory.forEach {
                add(buildJsonObject {
                    put("reference", it.reference); put("verse", it.verse); put("stage", it.stage)
                    put("correctCount", it.correctCount); put("nextReviewAt", it.nextReviewAt)
                })
            }
        }
        putJsonObject("formation") {
            put("morningWord", formation.morningWord); put("middayPrayer", formation.middayPrayer)
            put("eveningExamen", formation.eveningExamen); put("sabbathDay", formation.sabbathDay)
            put("pilgrimageId", formation.pilgrimageId); put("pilgrimageDay", formation.pilgrimageDay)
            put("familyNames", formation.familyNames); put("trustedPhone", formation.trustedPhone)
        }
    }.toString()
}
