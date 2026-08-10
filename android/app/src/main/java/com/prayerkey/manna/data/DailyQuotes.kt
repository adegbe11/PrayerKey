package com.prayerkey.manna.data

import java.time.LocalDate

data class DailyQuote(val text: String, val author: String = "PrayerKey")

/*
 * A full year of original daily reflections. Twenty openings and twenty
 * responses form 400 coherent combinations; the coprime step selects 365 of
 * them without repetition. This keeps the shipped library compact while
 * guaranteeing a different, offline-safe quote every day of the year.
 */
private val quoteOpenings = listOf(
    "When the road feels uncertain", "When prayer feels difficult",
    "When you are tired", "When fear speaks loudly", "When answers take time",
    "When your heart feels heavy", "When the day begins", "When the night feels long",
    "When you cannot see the whole way", "When yesterday still hurts",
    "When plans fall apart", "When hope feels small", "When you feel alone",
    "When peace seems far away", "When you need a new beginning",
    "When the work feels unnoticed", "When faith asks you to wait",
    "When joy returns", "When you have more questions than answers",
    "When God feels quiet",
)

private val quoteResponses = listOf(
    "take the next faithful step.", "let grace carry what strength cannot.",
    "begin with one honest prayer.", "remember that God is nearer than the noise.",
    "choose trust before you have proof.", "rest before you decide to give up.",
    "make room for God to surprise you.", "hold on to the truth you already know.",
    "give God the part you cannot control.", "look for mercy in the smallest things.",
    "return to the Word and start again.", "pray for someone else and let love widen your heart.",
    "be still long enough to hear hope.", "do the loving thing that is in front of you.",
    "remember that waiting is not the same as being forgotten.",
    "thank God for what has carried you this far.", "let today be enough for today.",
    "speak to God plainly; you do not need perfect words.",
    "keep walking—faithfulness grows one day at a time.",
    "leave space for an answer different from the one you imagined.",
)

val DAILY_QUOTES: List<DailyQuote> = List(365) { index ->
    val pair = Math.floorMod(index * 73, 400)
    DailyQuote("${quoteOpenings[pair / 20]}, ${quoteResponses[pair % 20]}")
}

fun quoteFor(date: LocalDate): DailyQuote =
    DAILY_QUOTES[Math.floorMod(date.toEpochDay().toInt(), DAILY_QUOTES.size)]
