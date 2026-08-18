package com.prayerkey.manna.data

import java.time.LocalDate
import com.prayerkey.manna.model.DailyPrayer

data class DailyDevotional(
    val title: String,
    val reference: String,
    val opening: String,
    val reading: List<String>,
    val reflectionQuestion: String,
    val practice: String,
    val prayer: String,
    val declaration: String,
)

private data class DevotionalTheme(
    val title: String,
    val truth: String,
    val invitation: String,
    val question: String,
    val practice: String,
    val prayer: String,
)

private val themes = listOf(
    DevotionalTheme("Come Back to God", "God does not ask you to arrive polished. He asks you to come near honestly.", "Bring the real condition of your heart into His presence today.", "What have you been keeping from God?", "Take two quiet minutes and name it plainly before God.", "God, meet me as I am and lead me closer to You today."),
    DevotionalTheme("Grace for Today", "Grace meets you before achievement and stays when your strength runs out.", "Receive today as a gift instead of a test you must pass.", "Where are you trying to earn what God wants you to receive?", "Replace one harsh thought about yourself with a true word of grace.", "Father, teach me to receive Your grace and extend it to others."),
    DevotionalTheme("Trust the Next Step", "Faith rarely reveals the whole road; it gives enough light for the next faithful step.", "Obey what is clear while trusting God with what is not.", "What next step have you delayed because you cannot see the outcome?", "Write down one faithful action and do it before the day ends.", "Lord, guide my next step and quiet my need to control the whole journey."),
    DevotionalTheme("Peace in the Middle", "God's peace is not denial of trouble; it is His presence within it.", "Slow your breathing and let truth become louder than urgency.", "What fear is setting the pace of your life today?", "Pause three times today and pray, ‘You are here, and I am held.’", "Prince of Peace, steady my heart and govern my response."),
    DevotionalTheme("Love in Practice", "Biblical love becomes visible through patience, truth, mercy, and action.", "Let love move from intention into one concrete choice.", "Who needs patient, practical love from you today?", "Send the message, make the call, forgive, or offer help.", "Jesus, make Your love visible through the way I treat people today."),
    DevotionalTheme("Hope While Waiting", "Waiting is not empty when God is forming endurance, wisdom, and deeper trust.", "Let this unfinished season become a place of companionship with God.", "What has waiting revealed about what you depend on?", "Thank God for one sign of grace inside the waiting.", "God of hope, keep my heart soft and faithful while I wait."),
    DevotionalTheme("Courage to Obey", "Courage is not the absence of fear; it is choosing faithfulness while fear is present.", "Ask for strength to do the right thing without needing to feel ready.", "Where is God asking for honest courage?", "Take one small action toward the difficult right thing.", "Lord, give me courage that is humble, loving, and obedient."),
    DevotionalTheme("A Listening Heart", "A hurried heart can miss the quiet ways God brings conviction and comfort.", "Read slowly enough for one phrase to remain with you.", "Which word in today's passage keeps drawing your attention?", "Carry that word with you and return to it at midday.", "Speak, Lord. Give me a listening heart and willingness to respond."),
    DevotionalTheme("Strength for the Weary", "God does not despise your limits. He meets you within them.", "Receive rest as trust, not failure.", "What burden are you carrying as though everything depends on you?", "Release one unnecessary task and make room to breathe.", "God, carry what is too heavy and renew what has grown tired."),
    DevotionalTheme("Faithful in Small Things", "A life of faith is usually built through ordinary choices repeated with love.", "Do not overlook the holy opportunity inside today's routine.", "What small responsibility deserves your full faithfulness?", "Complete one ordinary task with gratitude and care.", "Father, make me faithful in the small things You place before me."),
    DevotionalTheme("Forgiven and Free", "God's forgiveness does not excuse the past; it breaks the past's claim over your future.", "Confess honestly, receive mercy, and choose a new direction.", "What shame are you continuing to carry after bringing it to God?", "Write what grace says about you, then read it aloud.", "Merciful God, help me receive forgiveness and walk in freedom."),
    DevotionalTheme("Give Thanks Again", "Gratitude trains the heart to notice grace without pretending life is easy.", "Look for what is still good, still held, and still growing.", "What gift have you stopped noticing?", "Name three specific gifts and thank God for each one.", "Giver of every good thing, open my eyes and make me grateful."),
)

fun devotionalFor(date: LocalDate): DailyDevotional {
    val passage = passageFor(date)
    val theme = themes[Math.floorMod(date.toEpochDay().toInt() * 7, themes.size)]
    return DailyDevotional(
        title = theme.title,
        reference = passage.reference,
        opening = "Before you rush into the day, pause with ${passage.reference}. Read it with an open heart, not simply to finish a task but to notice how God may be meeting you through His Word.",
        reading = listOf(
            theme.truth,
            "As you consider ${passage.reference}, notice what it reveals about God and the kind of life faith produces. You may not resolve every question today. Let one clear truth become enough to live with faithfully.",
            "The pressure to understand everything at once can make the soul restless. Scripture invites you into a different rhythm: receive truth, trust God's character, and walk out the obedience available today. Growth often arrives quietly through one surrendered decision repeated with faith.",
            theme.invitation,
        ),
        reflectionQuestion = theme.question,
        practice = theme.practice,
        prayer = theme.prayer,
        declaration = "Today I receive God's grace for ${theme.title.lowercase()}. His Word guides me, His presence steadies me, and I will walk faithfully in the light He gives me.",
    )
}

/**
 * The day's prayer, drawn from the 543 written prayers the app already ships.
 *
 * What this replaces was a template. Measured across a year, it produced 365
 * "different" prayers — but blank out the one substituted verse reference and
 * **only twelve remained**, on the same twelve-day cycle as the old devotional
 * themes. "Faithful in Small Things" landed on the 14th of August and again on
 * the 26th. A daily user would have noticed inside a fortnight.
 *
 * There was no need to write anything: [LocalPrayerTopics] already holds 543
 * full prayers with their own scripture, bundled and offline. Rotating those by
 * date gives 543 genuinely distinct prayers instead of twelve wearing costumes,
 * and it uses content that was already there and already good.
 *
 * [topics] is passed in rather than loaded here because it comes from an asset
 * and this stays a pure function of the date — the property that makes the
 * whole plan the same for everybody on the same calendar day.
 *
 * Falls back to the old template only when the topics have not loaded yet, so
 * a cold start shows a prayer rather than a gap.
 */
fun dailyPrayerFor(date: LocalDate, topics: List<PrayerTopic>): DailyPrayer {
    if (topics.isEmpty()) return templatedPrayerFor(date)
    /* A stride coprime with almost any list length, so consecutive days land in
       different parts of the library rather than walking it in order — two
       prayers about grief on Monday and Tuesday read as a rut. */
    val topic = topics[Math.floorMod(date.toEpochDay() * 7, topics.size.toLong()).toInt()]
    return DailyPrayer(
        ref = topic.scripture.firstOrNull()?.first ?: passageFor(date).reference,
        title = topic.title,
        verse = topic.scripture.firstOrNull()?.second ?: "",
        prayer = buildString {
            append("Father, thank You for the gift of this morning and for the breath in my body. I place this new day in Your hands. ")
            append("Go before me, order my steps, guard my thoughts, and let my words and choices honour You.\n\n")
            append(topic.prayer)
            append("\n\nAs I leave this prayer, keep me aware of Your presence. Give me wisdom for every decision, strength for every responsibility, and kindness for every person I meet. ")
            append("Protect my home and everyone connected to me. Let me recognise Your help, obey Your direction, and return tonight with gratitude. In Jesus' name, Amen.")
        },
    )
}

/**
 * The old generated prayer, kept only as the cold-start fallback.
 *
 * Twelve distinct bodies across a year. Not for display when anything better
 * is available — see [dailyPrayerFor].
 */
private fun templatedPrayerFor(date: LocalDate): DailyPrayer {
    val passage = passageFor(date)
    val devotional = devotionalFor(date)
    return DailyPrayer(
        ref = passage.reference,
        title = "Prayer: ${devotional.title}",
        verse = "Today’s reading: ${passage.reference}",
        prayer = buildString {
            append("Heavenly Father,\n\n")
            append("According to Your Word in ${passage.reference}, I come before You today with faith. ")
            append("Let Your Word speak over my life, my family, my work, my health, my plans, and everything that concerns me. Let today be a day of blessing, peace, progress, and divine direction for me.\n\n")
            append("Father, go before me and make every crooked path straight. Order my steps and keep me from wrong decisions. ")
            append("Give me wisdom when I do not know what to do, courage when I feel afraid, strength when I feel weak, and peace when my mind is troubled. Let confusion leave me, and let clarity fill my heart.\n\n")
            append("Protect me and everyone connected to me. Keep us from danger, sickness, accidents, evil, and every plan meant to bring harm. ")
            append("Surround my home with Your presence. Guard my going out and my coming in. Let no weapon formed against me prosper, and let every voice speaking fear or defeat over my life be silenced by Your truth.\n\n")
            append("Bless the work of my hands today. Open the right doors for me and close every door that would lead me away from Your will. ")
            append("Give me favour with the people I meet. Provide what I need, help me use what I have wisely, and do not let anxiety about tomorrow steal the grace You have given me for today.\n\n")
            append("Renew my mind and cleanse my heart. Forgive my sins and help me turn away from every habit, thought, and relationship that weakens my walk with You. ")
            append("Fill me with the Holy Spirit. Let my words carry kindness, my choices show wisdom, and my life bring honour to Jesus.\n\n")
            append("Lord, let ${devotional.title.lowercase()} become real in my life today. ")
            append("Help me to ${devotional.practice.replaceFirstChar { it.lowercase() }.removeSuffix(".")}. ")
            append("Turn every delay into preparation, every difficulty into growth, and every disappointment into a reason to trust You more.\n\n")
            append("I receive Your mercy for this day. I receive strength for every responsibility, provision for every need, healing for every wounded place, and hope for every situation that feels unfinished. ")
            append("Let me return home with a testimony of Your goodness.\n\n")
            append("Thank You because You hear me. I place this day in Your hands, and I trust You to lead me, keep me, and bless me according to Your perfect will.\n\n")
            append("In Jesus’ name, Amen.")
        },
    )
}
