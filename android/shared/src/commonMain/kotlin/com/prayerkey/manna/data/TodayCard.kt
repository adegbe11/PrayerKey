package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues

/**
 * What today holds, kept small so a home-screen widget can show it without
 * opening the Bible: the day's verse in the King James Version and the title
 * of the day's devotional. Home writes it whenever it has worked these out.
 *
 * A widget that finds an older day falls back to a short list of verses kept
 * here, so it is never blank and never shows yesterday.
 */
data class TodayCard(val date: String, val ref: String, val verse: String, val devotional: String)

object Today {
    /** Called after anything a widget shows has changed. The app sets it. */
    var onChange: ((PlatformContext) -> Unit)? = null

    fun changed(context: PlatformContext) { runCatching { onChange?.invoke(context) } }

    fun save(context: PlatformContext, card: TodayCard) {
        val kv = context.keyValues("today_card")
        if (kv.getString("date", "") == card.date && kv.getString("verse", "") == card.verse &&
            kv.getString("devotional", "") == card.devotional) return
        kv.edit().putString("date", card.date).putString("ref", card.ref)
            .putString("verse", card.verse).putString("devotional", card.devotional).apply()
        changed(context)
    }

    fun read(context: PlatformContext, date: String, epochDay: Long): TodayCard {
        val kv = context.keyValues("today_card")
        if (kv.getString("date", "") == date && !kv.getString("verse", "").isNullOrBlank()) {
            return TodayCard(date, kv.getString("ref", "").orEmpty(), kv.getString("verse", "").orEmpty(), kv.getString("devotional", "").orEmpty())
        }
        val (ref, verse) = Fallback[epochDay.mod(Fallback.size.toLong()).toInt()]
        return TodayCard(date, ref, verse, "")
    }

    /** King James Version, word for word. */
    val Fallback = listOf(
        "Psalm 121:8" to "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore.",
        "Psalm 23:1" to "The LORD is my shepherd; I shall not want.",
        "Psalm 46:1" to "God is our refuge and strength, a very present help in trouble.",
        "Proverbs 3:5" to "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
        "1 Peter 5:7" to "Casting all your care upon him; for he careth for you.",
        "Psalm 119:105" to "Thy word is a lamp unto my feet, and a light unto my path.",
        "Psalm 56:3" to "What time I am afraid, I will trust in thee.",
        "Isaiah 40:31" to "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint.",
        "Lamentations 3:22" to "It is of the LORD'S mercies that we are not consumed, because his compassions fail not.",
        "Philippians 4:6" to "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
        "Matthew 11:28" to "Come unto me, all ye that labour and are heavy laden, and I will give you rest.",
        "Joshua 1:9" to "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.",
        "Romans 8:28" to "And we know that all things work together for good to them that love God, to them who are the called according to his purpose.",
        "Psalm 37:5" to "Commit thy way unto the LORD; trust also in him; and he shall bring it to pass.",
    )
}
