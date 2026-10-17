package com.prayerkey.manna.data

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn


/**
 * The day's prayer, cut into ten movements you pull through one at a time.
 *
 * The distinction that matters: this is **one prayer in ten parts**, not ten
 * prayers. A deck of ten unrelated prayer cards is a browsing surface — you
 * skim it, you put it down, nothing was said. Ten movements that open at
 * "Dear Lord" and close at "Amen" are a session: pull all ten and you have
 * actually prayed, aloud, for about four minutes.
 *
 * Everything in the middle is written content, not generated. Movements four
 * through seven are the day's real prayer from [LocalPrayerTopics], cut at
 * sentence boundaries, and the declaration is one of that prayer's own points.
 * Only the address, the thanksgiving, the intercession and the sealing come
 * from banks here, because those are the parts every prayer shares and the
 * only honest way to vary them is to write more of them. Each bank is sized
 * past a month, so a daily user never meets the same opening twice inside one.
 *
 * A pure function of the date, like [planFor] — everyone praying on the same
 * day prays the same words, and a whole year can be checked without a device.
 */
data class PrayerPulls(
    val date: LocalDate,
    val title: String,
    val movements: List<PrayerPull>,
) {
    val size: Int get() = movements.size
}

data class PrayerPull(
    /** 1-based, because it is shown to the reader. */
    val index: Int,
    val kind: PullKind,
    val text: String,
    /** Set only on the scripture movement. */
    val reference: String? = null,
)

enum class PullKind(val label: String) {
    OPEN("BEGIN"),
    THANKS("THANKSGIVING"),
    SCRIPTURE("THE WORD"),
    PETITION("ASK"),
    DECLARATION("DECLARE"),
    INTERCESSION("FOR OTHERS"),
    SEAL("AMEN"),
}

/** How many pulls a session is. Ten is short enough to finish before the day starts. */
const val PRAYER_PULLS = 10

/**
 * Builds the day's session.
 *
 * [prayer] is the day's matched topic from [TodayPlan]; [reading] is the day's
 * chapter, used when the topic carries no scripture of its own. Both may be
 * absent on a cold start, and the session still runs — a person who opens a
 * prayer notification must never arrive at an empty screen.
 */
fun pullsFor(date: LocalDate, prayer: PrayerTopic?, reading: String? = null): PrayerPulls {
    val scripture = prayer?.scripture?.firstOrNull()
    val word = when {
        scripture != null -> PrayerPull(3, PullKind.SCRIPTURE, scripture.second, scripture.first)
        else -> PrayerPull(3, PullKind.SCRIPTURE, WORD_INVITATION, reading)
    }
    val declaration = prayer?.prayerPoints?.firstOrNull()?.let(::asDeclaration)
        ?: pick(DECLARATIONS, date, 13)

    val movements = buildList {
        add(PrayerPull(1, PullKind.OPEN, pick(OPENINGS, date, 5)))
        add(PrayerPull(2, PullKind.THANKS, pick(THANKSGIVINGS, date, 7)))
        add(word)
        petitionsFrom(prayer, date).forEachIndexed { i, text ->
            add(PrayerPull(4 + i, PullKind.PETITION, text))
        }
        add(PrayerPull(8, PullKind.DECLARATION, declaration))
        add(PrayerPull(9, PullKind.INTERCESSION, pick(INTERCESSIONS, date, 3)))
        add(PrayerPull(10, PullKind.SEAL, pick(SEALINGS, date, 11)))
    }
    return PrayerPulls(date, prayer?.title ?: "Today's prayer", movements)
}

/**
 * The four asking movements.
 *
 * The written prayer is cut at sentence boundaries and grouped into four
 * contiguous parts — contiguous because a prayer read out of order stops being
 * a prayer. Short prayers are padded from their own points before the standing
 * bank is touched, so the padding still belongs to the day's subject.
 */
private fun petitionsFrom(prayer: PrayerTopic?, date: LocalDate): List<String> {
    val sentences = prayer?.prayer.orEmpty()
        .split(Regex("""(?<=[.!?])\s+"""))
        .map { it.trim() }
        .filter { it.length > 12 }
    val padded = sentences.toMutableList()
    var extra = 0
    while (padded.size < 4) {
        val point = prayer?.prayerPoints?.getOrNull(extra)
        padded += point?.let(::asDeclaration) ?: pick(PETITIONS, date, 17L + extra)
        extra++
    }
    return group(padded, 4).map { it.joinToString(" ") }
}

/** Contiguous, near-equal parts; the remainder goes to the earliest groups. */
private fun group(items: List<String>, parts: Int): List<List<String>> {
    val base = items.size / parts
    val over = items.size % parts
    var at = 0
    return (0 until parts).map { i ->
        val take = base + if (i < over) 1 else 0
        items.subList(at, at + take).also { at += take }
    }
}

/**
 * A prayer point, said in the first person.
 *
 * Points are written as headings — "Protection over the children" — and a
 * heading read aloud in a prayer sounds like a slide title. This turns one
 * into something a mouth can actually say.
 */
private fun asDeclaration(point: String): String {
    val body = point.trim().trimEnd('.', ':')
    if (body.isEmpty()) return "Lord, I stand on Your word today."
    val first = body.substringBefore(' ').lowercase()
    return when {
        first in setOf("i", "father", "lord", "god", "you", "my") -> "$body."
        first.endsWith("ing") || first in setOf("pray", "ask", "declare", "stand") -> "$body."
        else -> "Lord, I ask You for " + body.replaceFirstChar { it.lowercase() } + "."
    }
}

/**
 * Stable choice from a bank.
 *
 * The stride is coprime with every bank size here, so consecutive days step
 * across the bank rather than walking it in order — otherwise a week of
 * mornings opens with seven addresses that were written next to each other and
 * therefore sound alike.
 */
private fun pick(bank: List<String>, date: LocalDate, stride: Long): String =
    bank[(date.toEpochDays().toLong() * stride).mod(bank.size.toLong()).toInt()]

private const val WORD_INVITATION =
    "Speak to me before I speak to anyone else today. Let Your word settle the things in me that argument never settles."

/** 32 addresses — past a month, so no opening repeats inside one. */
private val OPENINGS = listOf(
    "Dear Lord, before I say anything else — thank You. You kept me through a night I did not keep myself through.",
    "Dear Lord, I come to You first. Before the phone, before the noise, before the list. Meet me here.",
    "Dear Lord, here I am. Not polished, not ready, but here — and I believe that is enough to begin with.",
    "Dear Lord, I am not coming for what I can get. I am coming because You are my Father and I want to be near You.",
    "Dear Lord, quiet me. My mind is already running ahead into the day, and I want to be still with You first.",
    "Dear Lord, I woke up. That is not a small thing. Thank You for breath I did not have to arrange.",
    "Dear Lord, You were awake all night while I slept. Nothing about today has surprised You.",
    "Dear Lord, I bring You the whole of me this morning — the strong parts, and the parts I hide.",
    "Dear Lord, teach me to pray. Not to perform, not to fill the silence, but to actually talk with You.",
    "Dear Lord, I have nothing to offer You today except my attention. Take it.",
    "Dear Lord, before I ask You to change anything around me, change something in me.",
    "Dear Lord, I have been carrying this alone. I am putting it down in front of You now.",
    "Dear Lord, You are not far off and You are not tired of me. I need to say that out loud before I go on.",
    "Dear Lord, I am starting the day the right way round — with You, and not with my worry.",
    "Dear Lord, thank You for another page. Whatever is written on it, write it with me.",
    "Dear Lord, I am here again, and You are not surprised to see me. Thank You for that.",
    "Dear Lord, my heart is heavy this morning, and I am bringing it heavy rather than pretending.",
    "Dear Lord, You know what today holds. I do not. Let that be a comfort and not a fear.",
    "Dear Lord, I want to know You more than I want an easy day. Help me mean that.",
    "Dear Lord, still my hands. Slow my thoughts. Let me hear You before the day gets loud.",
    "Dear Lord, You called me Your child. I am coming as one, not as a stranger asking a favour.",
    "Dear Lord, forgive me for the mornings I ran out of the door without a word to You.",
    "Dear Lord, I have run out of my own strength, and that is exactly where prayer begins.",
    "Dear Lord, I do not want to say prayers today. I want to pray.",
    "Dear Lord, thank You that I can come like this — no appointment, no queue, nothing to pay.",
    "Dear Lord, whatever I was anxious about last night, I am handing over before it grows.",
    "Dear Lord, You are already at work in this day. Show me where, and let me join You.",
    "Dear Lord, I am small this morning and You are not. Let that settle me.",
    "Dear Lord, You have never once forgotten me. Help me stop living as though You might.",
    "Dear Lord, I am not asking for a comfortable life. I am asking for Your presence in this one.",
    "Dear Lord, I lay this morning open in front of You. Nothing hidden, nothing held back.",
    "Dear Lord, You have been faithful for as long as I have been alive. Meet me again today.",
)

/** 32 thanksgivings. */
private val THANKSGIVINGS = listOf(
    "Thank You for the ordinary mercies — a bed, a roof, a body that woke up. I stop counting them too quickly.",
    "Thank You for the prayers You answered that I never noticed, and for the ones You lovingly refused.",
    "Thank You that You have never once dealt with me the way my failures deserve.",
    "Thank You for the people still in my life. Not one of them was owed to me.",
    "Thank You for keeping me last year, when I did not know how close the edge was.",
    "Thank You for food, for clean water, for a door that locks. Half the world is praying for these.",
    "Thank You for the work in front of me — including the part of it I complain about.",
    "Thank You that You did not give up on me in the season I gave up on myself.",
    "Thank You for Your patience. I have tested it, and I have never found the end of it.",
    "Thank You for the doors You closed. I wanted them open, and You knew what was behind them.",
    "Thank You for a mind that can still think and a heart that can still feel.",
    "Thank You for every person who prayed for me and never told me.",
    "Thank You for the cross, which I did not earn and could never repay.",
    "Thank You that Your mercy is new this morning, and not rationed from yesterday.",
    "Thank You for the sleep I was given, and for carrying what I could not carry while I slept.",
    "Thank You for the quiet answers — the accident that did not happen, the illness that passed by.",
    "Thank You for correcting me. It did not feel like love at the time, and it was.",
    "Thank You for Your word — kept, carried and copied, so that it could reach me.",
    "Thank You that I am not what I was five years ago, and that You are not finished.",
    "Thank You for strength for today. Not for the year — for today, and that is enough.",
    "Thank You for laughter. It is proof that sorrow does not get the last word.",
    "Thank You for the roof over my family, and for the peace underneath it.",
    "Thank You that You hear me the first time, and I never have to shout.",
    "Thank You for the friend who told me the truth when it cost them something.",
    "Thank You for the seasons of waiting. They taught me what comfort could not.",
    "Thank You for every provision that arrived without my name on the arrangement.",
    "Thank You that my name is written down, and cannot be erased.",
    "Thank You for the small joys I nearly walked straight past yesterday.",
    "Thank You for standing between me and things I will only learn about in eternity.",
    "Thank You for a church, a family, a place to belong.",
    "Thank You that You are good — not only generous, but good, all the way through.",
    "Thank You for beginning something in me that You have promised to finish.",
)

/** 24 intercessions — the movement where prayer stops being about the one praying. */
private val INTERCESSIONS = listOf(
    "Now for the people I carry who do not know I carry them. Meet each one where I cannot reach.",
    "Bless my family today. Guard the ones who left before I woke, and the ones still asleep.",
    "For the friend who is pretending to be fine — send them someone today who will not be fooled.",
    "For the people who have hurt me: I release them to You, and I ask You to do them good.",
    "For every home on my street where this morning began badly. Send peace before evening.",
    "For children growing up with nobody praying over them. Be their Father in the gap.",
    "For those who are ill today and have run out of words. Pray through their silence.",
    "For the ones burying somebody this week. Sit with them in the part no one else can enter.",
    "For my leaders — in my home, my work, my country. Give them wisdom heavier than their pride.",
    "For those who will lose their job today and do not know it yet. Go before them.",
    "For the person I find hardest to love. Change my heart faster than I change theirs.",
    "For anyone who prayed last night and heard nothing. Let today be the day they hear.",
    "For the church — not the building, the people. Make us worth belonging to.",
    "For those praying in places where praying is dangerous. Cover them.",
    "For my neighbours, whose names I should know and mostly do not. Bless their houses anyway.",
    "For the marriages near me that are quietly failing. Interrupt them with mercy.",
    "For the young people I know. Let them meet You early, and not the hard way.",
    "For everyone who will be told bad news today. Have someone standing beside them when it comes.",
    "For those far from home tonight. Be the familiar thing in an unfamiliar room.",
    "For the doctors and the nurses — the ones who walk towards what everybody else runs from.",
    "For the people I envy. Bless them fully, and take the envy out of me while You do it.",
    "For those with nobody at all to pray for them. Count this prayer for them.",
    "For my enemies, if I have them. Do them no harm through me.",
    "For the person who will need me today and has not asked yet. Make me available.",
)

/** 16 sealings. */
private val SEALINGS = listOf(
    "I have said what I came to say, and I trust You with all of it. In Jesus' name, Amen.",
    "Not my will in any of this, but Yours. In Jesus' name, Amen.",
    "I am leaving this here rather than picking it back up. In Jesus' name, Amen.",
    "Whatever the answer is, I will still be Yours when it comes. In Jesus' name, Amen.",
    "I have asked. Now I will go and live like Someone heard. In Jesus' name, Amen.",
    "Do what only You can do, and let me recognise it when You do. In Jesus' name, Amen.",
    "I stop here — not because I ran out of words, but because You have already heard. In Jesus' name, Amen.",
    "Keep me until I pray again. In Jesus' name, Amen.",
    "Take these words further than my faith can carry them. In Jesus' name, Amen.",
    "I put my name and my day into Your hands, and close them. In Jesus' name, Amen.",
    "Let the rest of today be the continuation of this prayer. In Jesus' name, Amen.",
    "You are able. That is the last thing I want to say. In Jesus' name, Amen.",
    "Have Your way in every part of this. In Jesus' name, Amen.",
    "I thank You in advance for what I have not yet seen. In Jesus' name, Amen.",
    "Hold me to what I have just prayed. In Jesus' name, Amen.",
    "To You be all of it — the asking, the answer, and the glory. In Jesus' name, Amen.",
)

/** Standing asks, used only when a day's prayer is too short to fill four movements. */
private val PETITIONS = listOf(
    "Order my steps today. Where I would choose wrongly, close the way.",
    "Give me wisdom for the decisions waiting for me, and the humility to take advice.",
    "Guard my mouth. Let nothing I say today need an apology tomorrow.",
    "Provide for my house — not extravagantly, but faithfully, the way You always have.",
    "Give me courage for the conversation I have been avoiding.",
    "Keep me from the thing that keeps catching me. You know what it is.",
    "Let my work today be honest, and let it be enough.",
    "Make me patient with difficult people, since You are patient with me.",
)

/** Used only when a day's prayer carries no points of its own. */
private val DECLARATIONS = listOf(
    "I am not abandoned. I have never been abandoned, and I will not be today.",
    "What God has begun in me, He will finish. I am not a project He walked away from.",
    "Fear does not run my house. It does not get a vote here.",
    "I am covered. Nothing reaches me that has not first passed through His hands.",
    "My provision comes from God, and not from my own arrangements.",
    "I will not repay evil for evil. Someone greater is keeping that account.",
    "This season is not my ending. It is a chapter, and He is still writing.",
    "I belong to God, and nothing that happens today changes that.",
)
