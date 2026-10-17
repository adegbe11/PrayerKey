package com.prayerkey.manna.data

/**
 * The fast as a machine: it knows where you are in it and speaks at the
 * moments that matter - the first hard hours, the middle, the start of each
 * new day, the last hour, and the end - with the verse and the focus for that
 * day, the same ones the fast shows inside the app.
 *
 * It replaces milestone notifications that fired twice a minute apart, spoke
 * of a feature that did not exist, and knew nothing about the fast itself.
 */
object FastMachine {

    enum class Moment { EARLY, HALF, NEW_DAY, LAST_HOUR, DONE }

    /** A moment of the fast, [at] in epoch seconds, on [day] of it (from 1). */
    data class Planned(val at: Long, val moment: Moment, val day: Int)

    fun plan(fast: FastingChallenge): List<Planned> {
        if (!fast.running || fast.hours <= 0) return emptyList()
        val start = fast.startedAt
        val end = fast.endsAt()
        val hour = 3_600L
        return buildList {
            if (fast.hours >= 12) add(Planned(start + 3 * hour, Moment.EARLY, 1))
            if (fast.hours in 6..48) add(Planned(start + fast.hours * hour / 2, Moment.HALF, 1 + (fast.hours / 2) / 24))
            for (d in 1 until fast.days) add(Planned(start + d * 24 * hour, Moment.NEW_DAY, d + 1))
            if (fast.hours >= 6) add(Planned(end - hour, Moment.LAST_HOUR, fast.days))
            add(Planned(end, Moment.DONE, fast.days))
        }.distinctBy { it.at }.sortedBy { it.at }
    }

    /** What the machine says at a moment: a title and the words under it. */
    fun message(fast: FastingChallenge, moment: Moment, day: Int): Pair<String, String> {
        val m = fastMoment(fast, day)
        val word = "${m.verse} ${m.ref}"
        val purpose = fast.purpose.ifBlank { "" }.lowercase()
        val forWhat = if (purpose.isNotBlank()) " for $purpose" else ""
        return when (moment) {
            Moment.EARLY -> "Three hours into your fast" to "${m.focus}. $word"
            Moment.HALF -> "Halfway" to "You are halfway through your fast$forWhat. $word"
            Moment.NEW_DAY -> "Day $day of ${fast.days}" to "${m.focus}. $word"
            Moment.LAST_HOUR -> "One hour left" to (
                if (fast.type == FastType.SOCIAL) "Finish well. When it ends, go back slowly, and keep the quiet you found."
                else "Finish well. When it ends, break it gently: water first, then something light."
                )
            Moment.DONE -> "Your fast is complete" to "You finished what you started. Thank Him, and write down what you saw. Then break your fast gently."
        }
    }
}
