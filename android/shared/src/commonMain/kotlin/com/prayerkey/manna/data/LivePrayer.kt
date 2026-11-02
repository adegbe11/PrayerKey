package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * The morning live prayer: a session that leads you through prayer the way a
 * prayer leader does in a morning meeting. Stand up, lift your hands, say this
 * after me, now pray for your family for two minutes.
 *
 * It is you praying for your own life. Nothing here pretends other people are
 * on a call with you. Each day of the week has its own subject, and the
 * session ends by praying the day's Prayer of the Day.
 */
data class LiveVerse(val book: String, val chapter: Int, val verse: Int) {
    val label: String get() = "${if (book == "Psalms") "Psalm" else book} $chapter:$verse"
}

data class LivePoint(
    /** What to pray for, as a heading. */
    val title: String,
    /** What to do with your body and your voice, said by the leader. */
    val lead: String,
    val verse: LiveVerse,
    val seconds: Int = 90,
)

data class LiveDay(
    val theme: String,
    /** Read aloud together near the start: one whole sentence, not half of one. */
    val word: LiveVerse,
    val declarations: List<String>,
    val points: List<LivePoint>,
)

sealed class LiveStep {
    /** Something the leader says before anything else happens. */
    data class Lead(val title: String, val words: String, val seconds: Int = 0) : LiveStep()
    /** A verse read aloud together. */
    data class Read(val title: String, val verse: LiveVerse) : LiveStep()
    /** One line at a time, said aloud after the leader. */
    data class SayAfterMe(val lines: List<String>) : LiveStep()
    /** Praying out loud for a set time. */
    data class Pray(val title: String, val words: String, val seconds: Int, val verse: LiveVerse? = null) : LiveStep()
    /** The day's Prayer of the Day, prayed together to close. */
    data object TodaysPrayer : LiveStep()
    data class Blessing(val words: String) : LiveStep()
}

object LivePrayer {

    fun dayFor(date: LocalDate): LiveDay = WEEK.getValue(date.dayOfWeek)

    fun steps(date: LocalDate): List<LiveStep> {
        val day = dayFor(date)
        return buildList {
            add(LiveStep.Lead("Get ready", "Find a quiet place. If you can, stand up. Put your phone where you can see it, and let us pray.", 8))
            add(LiveStep.Pray("Thanksgiving", "Lift your hands and thank God out loud. Thank Him for waking you up. Thank Him for three things by name.", 60))
            add(LiveStep.Read("Today's word", day.word))
            add(LiveStep.SayAfterMe(day.declarations))
            day.points.forEach { add(LiveStep.Pray(it.title, it.lead, it.seconds, it.verse)) }
            add(LiveStep.Pray("Your own need", "Now bring the thing that is on your heart this morning. Say it plainly. Tell God exactly what you need.", 120))
            add(LiveStep.Pray("In your own words", "Keep praying. Pray in your own words, or in the Spirit if that is how you pray. Do not rush.", 60))
            add(LiveStep.TodaysPrayer)
            add(LiveStep.Blessing("The LORD bless thee, and keep thee: the LORD make his face shine upon thee, and be gracious unto thee: the LORD lift up his countenance upon thee, and give thee peace."))
        }
    }

    /** Minutes the session takes, so the row can say it before you start. */
    fun minutes(date: LocalDate): Int =
        (steps(date).sumOf { step ->
            when (step) {
                is LiveStep.Lead -> step.seconds + 10
                is LiveStep.Pray -> step.seconds + 10
                is LiveStep.Read -> 30
                is LiveStep.SayAfterMe -> step.lines.size * 8
                LiveStep.TodaysPrayer -> 60
                is LiveStep.Blessing -> 20
            }
        } + 59) / 60

    private const val STORE = "live_prayer"

    fun joined(context: PlatformContext, date: LocalDate): Boolean =
        context.keyValues(STORE).getBoolean(date.toString(), false)

    fun markJoined(context: PlatformContext, date: LocalDate) {
        context.keyValues(STORE).edit().putBoolean(date.toString(), true).apply()
    }

    private val WEEK = mapOf(
        DayOfWeek.MONDAY to LiveDay(
            theme = "Direction for the week",
            word = LiveVerse("Proverbs", 16, 9),
            declarations = listOf(
                "This week belongs to God.",
                "My steps are ordered by the Lord.",
                "I will not walk into this week afraid.",
                "Where I do not know the way, He will show me.",
                "I will finish this week stronger than I started it.",
            ),
            points = listOf(
                LivePoint("Your plans for this week", "Put your hand on your chest. Hand this week to God, day by day: Monday, Tuesday, all of it. Ask Him to order your steps.", LiveVerse("Psalms", 37, 23)),
                LivePoint("Wisdom for decisions", "Name the decision you are carrying. Ask God for wisdom. He gives it freely and does not scold you for asking.", LiveVerse("James", 1, 5)),
                LivePoint("Open and closed doors", "Lift your hands. Ask God to open the right doors this week, and to shut the wrong ones, even the ones you want.", LiveVerse("Revelation", 3, 8)),
            ),
        ),
        DayOfWeek.TUESDAY to LiveDay(
            theme = "Your family",
            word = LiveVerse("Proverbs", 14, 26),
            declarations = listOf(
                "As for me and my house, we will serve the Lord.",
                "My home is covered by the blood of Jesus.",
                "Peace will live in my house.",
                "My children will be taught of the Lord.",
                "What God has joined, nothing will break.",
            ),
            points = listOf(
                LivePoint("Your home", "Point towards your home, wherever you are. Pray peace over every room and every person in it, by name.", LiveVerse("Joshua", 24, 15)),
                LivePoint("Your children", "Pray for your children, or the children in your family. Name them. Pray for their minds, their friends and their future.", LiveVerse("Isaiah", 54, 13)),
                LivePoint("The one you are worried about", "There is one person in your family you worry about. Pray for them now, out loud, for the whole time.", LiveVerse("Acts", 16, 31), 120),
            ),
        ),
        DayOfWeek.WEDNESDAY to LiveDay(
            theme = "Work and money",
            word = LiveVerse("Deuteronomy", 8, 18),
            declarations = listOf(
                "God gives me power to get wealth.",
                "The work of my hands is blessed.",
                "I will not borrow to eat.",
                "I will be faithful with little, and trusted with much.",
                "My God shall supply all my need.",
            ),
            points = listOf(
                LivePoint("The work of your hands", "Open your hands in front of you. Ask God to bless your work, your shop, your studies, whatever you do with these hands.", LiveVerse("Psalms", 90, 17)),
                LivePoint("Your money", "Name your debts and your needs to God, one by one. Ask Him for provision, and for wisdom with what you already have.", LiveVerse("Philippians", 4, 19)),
                LivePoint("Favour", "Ask for favour with the people who decide things for you this week: your boss, your customers, the office you are waiting on.", LiveVerse("Proverbs", 3, 4)),
            ),
        ),
        DayOfWeek.THURSDAY to LiveDay(
            theme = "Health and strength",
            word = LiveVerse("3 John", 1, 2),
            declarations = listOf(
                "By His stripes I am healed.",
                "My body is the temple of the Holy Ghost.",
                "I will live, and not die, and declare the works of the Lord.",
                "The Lord renews my strength.",
                "Sickness will not have the last word in my house.",
            ),
            points = listOf(
                LivePoint("Your body", "Put your hand where it hurts, if something hurts. Pray for healing. Thank God for every part of your body that works.", LiveVerse("Isaiah", 53, 5)),
                LivePoint("Someone who is sick", "Think of someone who is sick. Pray for them by name. Pray for their doctors and the people caring for them.", LiveVerse("James", 5, 15)),
                LivePoint("Your mind", "Pray for your mind: for peace, for sleep, for rest from worry. Ask God to guard your thoughts today.", LiveVerse("Isaiah", 26, 3)),
            ),
        ),
        DayOfWeek.FRIDAY to LiveDay(
            theme = "Thanksgiving",
            word = LiveVerse("Psalms", 136, 1),
            declarations = listOf(
                "The Lord has been good to me.",
                "I will bless the Lord at all times.",
                "What He did before, He will do again.",
                "I am not forgotten.",
                "My mouth will be full of praise.",
            ),
            points = listOf(
                LivePoint("This week", "Lift your hands. Go back over this week and thank God for every day, the easy ones and the hard ones.", LiveVerse("Psalms", 103, 2)),
                LivePoint("Answered prayers", "Remember a prayer God answered. Say it out loud, and thank Him for it as if it happened today.", LiveVerse("Psalms", 126, 3)),
                LivePoint("What has not changed yet", "Thank God now for the things you are still waiting for. Thank Him before you see them.", LiveVerse("John", 11, 41)),
            ),
        ),
        DayOfWeek.SATURDAY to LiveDay(
            theme = "Protection and rest",
            word = LiveVerse("Psalms", 4, 8),
            declarations = listOf(
                "I dwell in the secret place of the Most High.",
                "No weapon formed against me shall prosper.",
                "I will lie down in peace, and sleep.",
                "The Lord is my keeper.",
                "I am safe in His hands.",
            ),
            points = listOf(
                LivePoint("Protection", "Pray Psalm 91 over yourself and your family. Pray over your going out and your coming in, your roads and your home.", LiveVerse("Psalms", 91, 1)),
                LivePoint("Fear", "Name the thing you are afraid of. Give it to God out loud. Say: I will not be afraid.", LiveVerse("Isaiah", 41, 10)),
                LivePoint("Rest", "Ask God for real rest this weekend. For your body, and for the part of you that is always running.", LiveVerse("Matthew", 11, 28)),
            ),
        ),
        DayOfWeek.SUNDAY to LiveDay(
            theme = "Your church",
            word = LiveVerse("Matthew", 16, 18),
            declarations = listOf(
                "I was glad when they said, Let us go into the house of the Lord.",
                "The gates of hell shall not prevail against the church.",
                "My pastor will speak the word with power today.",
                "I will not leave church the same way I came in.",
                "The Lord is in His holy temple.",
            ),
            points = listOf(
                LivePoint("Your pastor", "Pray for your pastor by name. Pray for strength, for clear words today, and for their family.", LiveVerse("Ephesians", 6, 19)),
                LivePoint("The service today", "Pray for today's service: for the worship, the word, and everyone who walks in carrying something heavy.", LiveVerse("Psalms", 122, 1)),
                LivePoint("Your own heart", "Ask God to prepare your heart before you get there. Ask Him to speak to you today, not just to everyone else.", LiveVerse("Psalms", 139, 23)),
            ),
        ),
    )
}
