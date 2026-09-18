package com.prayerkey.manna.data

/**
 * The template library.
 *
 * WHY THESE AND NOT A GENERAL-PURPOSE SET
 *
 * The obvious move is to copy a general journalling library - gratitude,
 * productivity, travel, health - and add a spiritual category to it. That
 * produces an app with three prayer templates in it, which is a journal that
 * happens to allow prayer rather than a place to pray.
 *
 * This is the other way round. Ninety-seven templates, and prayer is the
 * largest collection rather than a shelf near the bottom. There is no
 * productivity collection and no travel collection, because this is not that
 * app and pretending otherwise would dilute the one thing it is for.
 *
 * WHAT A TEMPLATE IS HERE
 *
 * A set of questions, not a blank page with a title. The questions are the
 * work: "What am I asking God for?" is a different entry from "How was your
 * day?", and somebody who cannot start writing can almost always answer a
 * question. Each becomes a heading in the entry, so the finished page is
 * readable months later rather than a wall of remembered context.
 *
 * ON THE WRITING
 *
 * Plain words, second person, no religious throat-clearing. A template that
 * opens "Come now, beloved, into the sacred space of communion" is a template
 * nobody uses twice. Several of these are for bad days - fear, grief, silence -
 * and those in particular do not tell anyone how to feel or promise that
 * writing will fix it.
 */

data class JournalTemplate(
    val id: String,
    val collection: String,
    val title: String,
    /** One line on the gallery card: what this is for. */
    val summary: String,
    /** The questions, in order. Each becomes a heading in the entry. */
    val prompts: List<String>,
    /** Suggested when the template is used, unless the writer changes it. */
    val journal: String = "My Journey",
    /** Whether an entry from this template joins the prayer list by default. */
    val prayer: Boolean = false,
) {
    val minutes: Int get() = maxOf(2, prompts.size)
}

data class TemplateCollection(val name: String, val emoji: String, val blurb: String)

val TEMPLATE_COLLECTIONS = listOf(
    TemplateCollection("Prayer", "🙏", "Bring it to Him in words"),
    TemplateCollection("Daily walk", "🌅", "The ordinary day, with God in it"),
    TemplateCollection("Bible & Scripture", "📖", "Read it, then take it somewhere"),
    TemplateCollection("Answered prayer", "✨", "What you asked, and what He did"),
    TemplateCollection("Gratitude", "❤️", "Name it before you forget it"),
    TemplateCollection("Reflection", "🕊", "Look back honestly"),
    TemplateCollection("Spiritual warfare", "⚔️", "Stand, and know what you stand on"),
    TemplateCollection("Spiritual growth", "🌱", "Where you are changing"),
    TemplateCollection("Difficult seasons", "🌧", "For the days that are hard"),
    TemplateCollection("Family", "👨‍👩‍👧", "The people you are carrying"),
    TemplateCollection("Purpose & decisions", "🎯", "When you do not know what to do"),
    TemplateCollection("Dreams & notes", "🌙", "Write it down before it goes"),
    TemplateCollection("Sermon & church", "⛪", "Sunday, kept"),
)

val JOURNAL_TEMPLATES: List<JournalTemplate> = listOf(

    /* ── Prayer · 15 ────────────────────────────────────────────────────
       The largest collection, deliberately. */

    JournalTemplate(
        "prayer-today", "Prayer", "Prayer for today",
        "Bring the day in front of you to God",
        listOf(
            "What is on my heart today?",
            "What am I asking God for?",
            "Who needs my prayers today?",
            "What Scripture am I standing on?",
            "What am I surrendering to Him?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-morning", "Prayer", "Morning prayer",
        "Hand the day over before it starts",
        listOf(
            "Father, before this day takes me…",
            "What I am asking for today",
            "Who I am carrying into this day",
            "What I am trusting You with",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-evening", "Prayer", "Evening prayer",
        "Put the day down",
        listOf(
            "What happened today that I want to tell You",
            "What I am sorry for",
            "What I am thankful for",
            "What I am leaving with You tonight",
        ),
    ),
    JournalTemplate(
        "prayer-bed", "Prayer", "Prayer before bed",
        "Short, for when you are already tired",
        listOf(
            "What I am still turning over",
            "What I am handing over before I sleep",
        ),
    ),
    JournalTemplate(
        "prayer-self", "Prayer", "Prayer for myself",
        "The one people find hardest to write",
        listOf(
            "What do I actually need right now?",
            "What am I afraid to ask for?",
            "What would change if He answered?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-someone", "Prayer", "Prayer for someone else",
        "Hold one person up by name",
        listOf(
            "Who am I praying for?",
            "What are they facing?",
            "What am I asking God to do for them?",
            "What Scripture am I praying over them?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-intercession", "Prayer", "Intercession",
        "Standing in the gap for others",
        listOf(
            "Who or what am I standing in the gap for?",
            "What is the need, plainly?",
            "What am I asking God to do?",
            "How long have I been praying this?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-family", "Prayer", "Prayer for my family",
        "Each one, by name",
        listOf(
            "Who in my family am I praying for today?",
            "What do they need?",
            "What am I asking God for our home?",
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-children", "Prayer", "Prayer for my children",
        "For the ones you are raising",
        listOf(
            "What I see in them that I thank God for",
            "What I am worried about",
            "What I am asking God to do in them",
            "What I want them to know about Him",
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-marriage", "Prayer", "Prayer for my marriage",
        "For the two of you",
        listOf(
            "What I am thankful for in my marriage",
            "Where we are struggling",
            "What I am asking God to change in me first",
            "What I am asking Him to do between us",
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-healing", "Prayer", "Prayer for healing",
        "For a body, a mind, or a wound",
        listOf(
            "Who needs healing, and what kind?",
            "What am I asking God for specifically?",
            "What Scripture am I holding on to?",
            "What is hard about waiting for this?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-work", "Prayer", "Prayer for work",
        "The hours you spend most of your life in",
        listOf(
            "What is in front of me at work?",
            "Where do I need wisdom?",
            "Who do I work with that I should pray for?",
            "How can this work be done for Him today?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-finances", "Prayer", "Prayer for finances",
        "Honest about money",
        listOf(
            "What is the real situation?",
            "What am I anxious about?",
            "What am I asking God for?",
            "What am I trusting Him with instead of solving?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-guidance", "Prayer", "Prayer for guidance",
        "When the next step is not obvious",
        listOf(
            "What am I trying to decide?",
            "What do I already sense He is saying?",
            "What am I afraid of choosing wrongly?",
            "What am I asking Him to make clear?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-open", "Prayer", "Open prayer",
        "No structure. Just write it.",
        listOf("Father…"),
        prayer = true,
    ),

    /* ── Daily walk · 10 ───────────────────────────────────────────── */

    JournalTemplate(
        "walk-morning", "Daily walk", "Morning with God",
        "Five quiet minutes before anything else",
        listOf(
            "What is the first thing on my mind?",
            "What am I reading or thinking about today?",
            "What am I asking for?",
            "What do I want to carry into today?",
        ),
    ),
    JournalTemplate(
        "walk-evening", "Daily walk", "Evening with God",
        "Look back over the day with Him",
        listOf(
            "Where was God in today?",
            "What did I get right?",
            "What would I do differently?",
            "What am I thankful for tonight?",
        ),
    ),
    JournalTemplate(
        "walk-start", "Daily walk", "Start my day",
        "Three lines, then go",
        listOf("Today I am…", "Today I need…", "Today I will…"),
    ),
    JournalTemplate(
        "walk-end", "Daily walk", "End my day",
        "Three lines, then sleep",
        listOf("Today was…", "I am thankful for…", "I am leaving behind…"),
    ),
    JournalTemplate(
        "walk-five", "Daily walk", "Five minutes with God",
        "When that is honestly all there is",
        listOf("One thing I want to say to Him", "One thing I want to ask Him"),
    ),
    JournalTemplate(
        "walk-today", "Daily walk", "Today with God",
        "The whole day, in one page",
        listOf(
            "What happened today?",
            "Where did I notice Him?",
            "What did today teach me?",
        ),
    ),
    JournalTemplate(
        "walk-moment", "Daily walk", "A God moment",
        "The thing you will forget by Friday",
        listOf("What happened?", "Why did it stay with me?", "What do I think He was saying?"),
    ),
    JournalTemplate(
        "walk-saw", "Daily walk", "Where I saw God today",
        "Look for Him in the ordinary parts",
        listOf(
            "In something small",
            "In another person",
            "In something that did not go my way",
        ),
    ),
    JournalTemplate(
        "walk-sleep", "Daily walk", "Before I sleep",
        "The last thought of the day",
        listOf("What I am putting down", "What I am thankful for", "What I am trusting Him with tonight"),
    ),
    JournalTemplate(
        "walk-checkin", "Daily walk", "Daily spiritual check-in",
        "An honest state of things",
        listOf(
            "How is my heart today, really?",
            "How has my prayer life been this week?",
            "What am I avoiding?",
            "What is one step back towards Him?",
        ),
    ),

    /* ── Bible & Scripture · 10 ────────────────────────────────────── */

    JournalTemplate(
        "bible-soap", "Bible & Scripture", "SOAP study",
        "Scripture, Observation, Application, Prayer",
        listOf(
            "SCRIPTURE — write the verse out in full",
            "OBSERVATION — what does it actually say?",
            "APPLICATION — what does it ask of me?",
            "PRAYER — pray it back to Him",
        ),
    ),
    JournalTemplate(
        "bible-study", "Bible & Scripture", "Bible study",
        "A passage, worked through",
        listOf(
            "Passage",
            "What is happening here?",
            "What does this say about God?",
            "What does this say about people?",
            "What will I do about it?",
        ),
    ),
    JournalTemplate(
        "bible-verse", "Bible & Scripture", "Verse reflection",
        "One verse, sat with",
        listOf("The verse", "What stands out", "What it means for today"),
    ),
    JournalTemplate(
        "bible-chapter", "Bible & Scripture", "Chapter study",
        "A whole chapter at once",
        listOf(
            "Chapter",
            "What happens in it",
            "The verse that stopped me",
            "What I take from the whole chapter",
        ),
    ),
    JournalTemplate(
        "bible-character", "Bible & Scripture", "Character study",
        "A person in Scripture, closely",
        listOf(
            "Who am I studying?",
            "What did they do?",
            "Where did they fail?",
            "How did God deal with them?",
            "What is here for me?",
        ),
    ),
    JournalTemplate(
        "bible-meditation", "Bible & Scripture", "Scripture meditation",
        "Slowly, one line at a time",
        listOf(
            "The passage",
            "Read it once — what do I notice?",
            "Read it again — what word stays?",
            "Read it a third time — what is He saying?",
        ),
    ),
    JournalTemplate(
        "bible-mean", "Bible & Scripture", "What does this verse mean to me?",
        "Between the text and your life",
        listOf("The verse", "What it meant then", "What it means for where I am now"),
    ),
    JournalTemplate(
        "bible-remember", "Bible & Scripture", "Scripture to remember",
        "Worth carrying",
        listOf("The verse", "Why I want to keep it", "When I will need it"),
    ),
    JournalTemplate(
        "bible-notes", "Bible & Scripture", "Bible reading notes",
        "Plain notes as you read",
        listOf("What I read", "Notes", "Questions I still have"),
    ),
    JournalTemplate(
        "bible-apply", "Bible & Scripture", "Scripture application",
        "From reading to doing",
        listOf(
            "The passage",
            "What it asks of me",
            "One thing I will change this week",
            "How I will know I did it",
        ),
    ),

    /* ── Answered prayer · 8 ───────────────────────────────────────── */

    JournalTemplate(
        "answered-new", "Answered prayer", "New prayer request",
        "Start the record. Come back to it later.",
        listOf(
            "What am I praying for?",
            "Why this matters to me",
            "Scripture I am standing on",
            "Who is praying with me",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "answered-mark", "Answered prayer", "He answered",
        "Write it down while you still remember the detail",
        listOf(
            "What did I ask for?",
            "What happened?",
            "How did He answer — was it what I expected?",
            "What did I learn about Him?",
        ),
    ),
    JournalTemplate(
        "answered-update", "Answered prayer", "Update on a prayer",
        "Nothing finished yet, but something moved",
        listOf("What am I still praying for?", "What has changed?", "What am I asking now?"),
        prayer = true,
    ),
    JournalTemplate(
        "answered-testimony", "Answered prayer", "Testimony",
        "The version you could tell someone",
        listOf(
            "What was the situation?",
            "What did I pray?",
            "What did God do?",
            "What do I want people to know from this?",
        ),
    ),
    JournalTemplate(
        "answered-waiting", "Answered prayer", "Still waiting",
        "Honest about the ones that are not answered yet",
        listOf(
            "What am I still waiting on?",
            "How long has it been?",
            "What is hard about the waiting?",
            "What keeps me praying it anyway?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "answered-no", "Answered prayer", "When the answer was no",
        "This belongs in the record too",
        listOf(
            "What did I ask for?",
            "What happened instead?",
            "What do I not understand?",
            "What am I choosing to trust?",
        ),
    ),
    JournalTemplate(
        "answered-review", "Answered prayer", "Looking back over my prayers",
        "Read the old ones and see",
        listOf(
            "What have I been praying this year?",
            "What has He already answered?",
            "What pattern do I see?",
        ),
    ),
    JournalTemplate(
        "answered-thanks", "Answered prayer", "Thank You for answering",
        "Close the loop",
        listOf("What He did", "What I want to say to Him about it"),
    ),

    /* ── Gratitude · 8 ─────────────────────────────────────────────── */

    JournalTemplate(
        "grat-three", "Gratitude", "Three things",
        "The shortest useful entry there is",
        listOf("One", "Two", "Three"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-thank", "Gratitude", "Thank You, God",
        "Said directly to Him",
        listOf("Thank You for…", "Thank You especially for…"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-provision", "Gratitude", "His provision today",
        "What arrived that you did not arrange",
        listOf("What I needed", "What came", "What that tells me about Him"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-people", "Gratitude", "People I am thankful for",
        "Name them",
        listOf("Who", "Why", "Will I tell them?"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-small", "Gratitude", "Small blessings",
        "The ones too small to mention",
        listOf("Something small today I am glad of"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-unexpected", "Gratitude", "An unexpected blessing",
        "The thing you were not praying for",
        listOf("What happened?", "Why it surprised me", "What I want to say about it"),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-week", "Gratitude", "Weekly gratitude",
        "Once a week, look back over seven days",
        listOf(
            "The best thing this week",
            "The hardest thing, and what came of it",
            "Three things I am thankful for",
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-hard", "Gratitude", "Gratitude in a hard season",
        "Not pretending. Still looking.",
        listOf(
            "What is hard right now",
            "What is still true and good",
            "One thing I can thank Him for today",
        ),
        journal = "Gratitude",
    ),

    /* ── Reflection · 8 ────────────────────────────────────────────── */

    JournalTemplate(
        "ref-week", "Reflection", "This week",
        "Seven days, looked at properly",
        listOf("What happened", "What I learned", "What I want to carry forward"),
    ),
    JournalTemplate(
        "ref-month", "Reflection", "This month",
        "A wider view",
        listOf(
            "What changed this month",
            "Where did I grow?",
            "Where did I go backwards?",
            "What am I asking for next month?",
        ),
    ),
    JournalTemplate(
        "ref-year", "Reflection", "This year",
        "The long look",
        listOf(
            "What defined this year",
            "What God did",
            "What I got wrong",
            "What I am taking into next year",
        ),
    ),
    JournalTemplate(
        "ref-examen", "Reflection", "Evening examen",
        "An old practice: walk back through the day",
        listOf(
            "What am I grateful for today?",
            "Where did I feel most alive?",
            "Where did I turn away from God or people?",
            "What do I want tomorrow to look like?",
        ),
    ),
    JournalTemplate(
        "ref-honest", "Reflection", "An honest page",
        "For things you would not say out loud",
        listOf("What I am actually thinking", "What I have not admitted", "What I want to do about it"),
    ),
    JournalTemplate(
        "ref-lesson", "Reflection", "What I learned",
        "One lesson, written down so it sticks",
        listOf("What happened", "What I learned", "What it will change"),
    ),
    JournalTemplate(
        "ref-letter", "Reflection", "A letter to God",
        "Write to Him, not about Him",
        listOf("Dear Father,"),
    ),
    JournalTemplate(
        "ref-future", "Reflection", "A letter to myself, later",
        "For the version of you who reads this in a year",
        listOf("Where I am now", "What I hope has happened by then", "What I want you to remember"),
    ),

    /* ── Spiritual warfare · 8 ─────────────────────────────────────── */

    JournalTemplate(
        "war-fighting", "Spiritual warfare", "What I am fighting in prayer",
        "Name it plainly",
        listOf(
            "What am I up against?",
            "What am I asking God to do?",
            "What Scripture am I standing on?",
            "What am I refusing to agree with?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-fear", "Spiritual warfare", "Prayer against fear",
        "For the thing that wakes you at three",
        listOf("What am I afraid of?", "What is true instead?", "What am I asking Him for?"),
        prayer = true,
    ),
    JournalTemplate(
        "war-thoughts", "Spiritual warfare", "Breaking a thought pattern",
        "The sentence you keep saying to yourself",
        listOf(
            "What do I keep telling myself?",
            "Where did that come from?",
            "What does God say instead?",
            "What will I say next time it comes?",
        ),
    ),
    JournalTemplate(
        "war-standing", "Spiritual warfare", "Scripture I am standing on",
        "Write the verse. Then write why.",
        listOf("The verse", "What I am facing", "What this verse settles"),
    ),
    JournalTemplate(
        "war-armour", "Spiritual warfare", "The armour of God",
        "Ephesians 6, one piece at a time",
        listOf(
            "Truth — what lie am I putting down?",
            "Righteousness — what am I making right?",
            "Peace — where do I need to stand still?",
            "Faith — what am I trusting Him for?",
            "Salvation — what do I know is settled?",
            "The Word — what am I praying back to Him?",
        ),
    ),
    JournalTemplate(
        "war-temptation", "Spiritual warfare", "Prayer against temptation",
        "Honest, and without shame",
        listOf(
            "What keeps pulling at me?",
            "When is it strongest?",
            "What will I do differently?",
            "What am I asking God for?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-protection", "Spiritual warfare", "Prayer for protection",
        "Over people, a home, a journey",
        listOf("Who or what am I covering?", "What am I asking God to guard?", "Scripture I am praying"),
        prayer = true,
    ),
    JournalTemplate(
        "war-victory", "Spiritual warfare", "Victory journal",
        "Write down what He has already won",
        listOf("What was I fighting?", "What changed?", "What do I now know?"),
    ),

    /* ── Spiritual growth · 7 ──────────────────────────────────────── */

    JournalTemplate(
        "grow-where", "Spiritual growth", "Where I am growing",
        "Notice it, or you will miss it",
        listOf("What is different in me than a year ago?", "What made the difference?", "What is next?"),
    ),
    JournalTemplate(
        "grow-stuck", "Spiritual growth", "Where I am stuck",
        "The same thing, again",
        listOf("What keeps repeating?", "What have I tried?", "What have I not tried?", "What am I asking for?"),
    ),
    JournalTemplate(
        "grow-conviction", "Spiritual growth", "Something convicted me",
        "The part that was uncomfortable",
        listOf("What was it?", "Why did it land?", "What will I do?"),
    ),
    JournalTemplate(
        "grow-habit", "Spiritual growth", "A habit I am building",
        "Small, and repeated",
        listOf("The habit", "Why it matters", "How it has gone this week", "What will help next week"),
    ),
    JournalTemplate(
        "grow-fruit", "Spiritual growth", "Fruit of the Spirit check",
        "Galatians 5, honestly",
        listOf(
            "Which of these is growing in me?",
            "Which is missing right now?",
            "Where was that tested this week?",
        ),
    ),
    JournalTemplate(
        "grow-obedience", "Spiritual growth", "Something He asked me to do",
        "And what you did about it",
        listOf("What did I sense He was asking?", "Did I do it?", "What happened?"),
    ),
    JournalTemplate(
        "grow-mentor", "Spiritual growth", "Someone I am learning from",
        "Who is ahead of you, and what you see",
        listOf("Who", "What I see in them", "What I want to learn"),
    ),

    /* ── Difficult seasons · 6 ─────────────────────────────────────── */

    JournalTemplate(
        "hard-afraid", "Difficult seasons", "When I am afraid",
        "Put the fear into words first",
        listOf(
            "What am I afraid of?",
            "What is the worst I am imagining?",
            "What is actually true?",
            "What do I need from God right now?",
        ),
    ),
    JournalTemplate(
        "hard-anxious", "Difficult seasons", "When I am anxious",
        "For when it will not settle",
        listOf(
            "What is my mind circling?",
            "What is in my control?",
            "What is not?",
            "What am I handing over?",
        ),
    ),
    JournalTemplate(
        "hard-lonely", "Difficult seasons", "When I am lonely",
        "Written for a real evening",
        listOf(
            "What does the loneliness feel like?",
            "When did it start?",
            "Who could I reach for?",
            "What do I want to say to God about it?",
        ),
    ),
    JournalTemplate(
        "hard-grief", "Difficult seasons", "When I am grieving",
        "No stages, no timeline",
        listOf(
            "Who or what have I lost?",
            "What do I miss today?",
            "What do I want to remember?",
            "What do I want to say to God?",
        ),
    ),
    JournalTemplate(
        "hard-silence", "Difficult seasons", "When God feels silent",
        "This is allowed to be written down",
        listOf(
            "How long has it felt like this?",
            "What did I expect Him to say?",
            "What do I still believe when I do not feel it?",
            "What am I going to keep doing anyway?",
        ),
    ),
    JournalTemplate(
        "hard-nopray", "Difficult seasons", "When I do not know what to pray",
        "Start anywhere",
        listOf("What I would say if I could", "What I want Him to know", "Help me with…"),
        prayer = true,
    ),

    /* ── Family · 5 ────────────────────────────────────────────────── */

    JournalTemplate(
        "fam-home", "Family", "Our home",
        "What is happening under this roof",
        listOf("How is our home right now?", "What needs prayer?", "What am I thankful for?"),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-child", "Family", "About one of my children",
        "One child, one page",
        listOf("Who", "What I am noticing", "What I am praying for them", "What I want to tell them"),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-spouse", "Family", "About my husband or wife",
        "Written for you, not for them",
        listOf("What I am grateful for", "What is hard", "What I will do this week"),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-parents", "Family", "My parents",
        "Whatever that relationship is",
        listOf("Where things stand", "What I carry from them", "What I am praying"),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-hard", "Family", "A hard relationship",
        "Honest, and not for sending",
        listOf(
            "Who, and what happened",
            "My part in it",
            "What forgiveness would look like",
            "What I am asking God for",
        ),
        journal = "Family",
    ),

    /* ── Purpose & decisions · 5 ───────────────────────────────────── */

    JournalTemplate(
        "purp-decision", "Purpose & decisions", "A decision before God",
        "Lay it out before you choose",
        listOf(
            "What am I deciding?",
            "The case for",
            "The case against",
            "What is my gut, and why?",
            "What am I asking God to show me?",
        ),
        prayer = true,
    ),
    JournalTemplate(
        "purp-calling", "Purpose & decisions", "What is God calling me to?",
        "The long question",
        listOf(
            "What keeps coming back to me?",
            "What am I good at that serves others?",
            "What would I do if I were not afraid?",
            "What is one step I could take?",
        ),
    ),
    JournalTemplate(
        "purp-discern", "Purpose & decisions", "Discernment",
        "Testing what you think you are hearing",
        listOf(
            "What do I think God is saying?",
            "Does it agree with Scripture?",
            "What do wise people say?",
            "What is my motive, honestly?",
            "What happens if I wait?",
        ),
    ),
    JournalTemplate(
        "purp-doors", "Purpose & decisions", "Open door, closed door",
        "What has opened and what has shut",
        listOf("What has opened", "What has closed", "What I make of that"),
    ),
    JournalTemplate(
        "purp-purpose", "Purpose & decisions", "Why am I here?",
        "Occasionally worth asking",
        listOf("What am I for?", "Who is better off because of me?", "What would I regret not doing?"),
    ),

    /* ── Dreams & notes · 4 ────────────────────────────────────────── */

    JournalTemplate(
        "dream-record", "Dreams & notes", "A dream",
        "Record it before you interpret it",
        listOf("What happened in it", "How it felt", "What I noticed on waking"),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-recurring", "Dreams & notes", "A recurring dream",
        "When it keeps coming back",
        listOf("The dream", "How often", "What is always the same", "What I am asking God about it"),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-impression", "Dreams & notes", "Something I sensed",
        "An impression, written down plainly",
        listOf("What I sensed", "When and where", "What I will do with it — if anything yet"),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-word", "Dreams & notes", "A word someone gave me",
        "Keep the record, test it later",
        listOf("Who said it", "What they said", "What I think about it", "What happened afterwards"),
        journal = "Dreams",
    ),

    /* ── Sermon & church · 3 ───────────────────────────────────────── */

    JournalTemplate(
        "church-sermon", "Sermon & church", "Sermon notes",
        "Sunday, in a form you can use on Wednesday",
        listOf(
            "Church and speaker",
            "Scripture",
            "The main message",
            "What stood out",
            "What convicted me",
            "What I will do differently",
        ),
        journal = "Church",
    ),
    JournalTemplate(
        "church-service", "Sermon & church", "Church today",
        "The whole morning, not only the sermon",
        listOf("What we sang that stayed with me", "What was said", "Who I spoke to", "What I am taking home"),
        journal = "Church",
    ),
    JournalTemplate(
        "church-group", "Sermon & church", "Bible study group",
        "What the room said, and what you thought",
        listOf("What we studied", "What others saw that I did not", "My own question", "What I will follow up"),
        journal = "Church",
    ),
)

/** Grouped for the gallery, in the order the collections are declared. */
fun templatesByCollection(): Map<String, List<JournalTemplate>> =
    TEMPLATE_COLLECTIONS.associate { c -> c.name to JOURNAL_TEMPLATES.filter { it.collection == c.name } }

/**
 * The body a template starts with: each question as a heading, with room to
 * write under it. Markdown, because the editor already speaks it.
 */
fun starterFor(template: JournalTemplate): String = template.starter()

fun JournalTemplate.starter(): String =
    prompts.joinToString("\n\n") { "## " + it + "\n" } + "\n"
