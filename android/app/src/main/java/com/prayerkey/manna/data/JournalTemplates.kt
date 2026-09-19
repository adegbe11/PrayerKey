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

/**
 * One page of a template.
 *
 * A printed prayer journal does not put ten questions on a spread. It gives a
 * page a name, one question, and a line of direction underneath - and then the
 * rest of the page is empty, which is the whole point. This is that page.
 *
 * @param heading  the page name, set in small capitals: ADORATION, CONFESSION
 * @param question what the page actually asks
 * @param guide    one line under the question, telling you how to approach it
 * @param starter  an opening clause, offered and never inserted on its own
 * @param people   the page offers the people you have prayed for before
 * @param private  the page says plainly that nobody else sees it
 * @param skippable confession and the like can be passed over without penalty
 */
data class TemplatePrompt(
    val heading: String,
    val question: String,
    val guide: String = "",
    val starter: String = "",
    val people: Boolean = false,
    val private: Boolean = false,
    val skippable: Boolean = false,
)

/** A question with no page furniture, for the shorter templates. */
fun p(question: String, heading: String = "", guide: String = "") =
    TemplatePrompt(heading.ifBlank { "" }, question, guide)

data class JournalTemplate(
    val id: String,
    val collection: String,
    val title: String,
    /** One line on the gallery card: what this is for. */
    val summary: String,
    /** The pages, in order. */
    val prompts: List<TemplatePrompt>,
    /** Suggested when the template is used, unless the writer changes it. */
    val journal: String = "My Journey",
    /** Whether an entry from this template joins the prayer list by default. */
    val prayer: Boolean = false,
    /** The verse on the cover. */
    val verse: String = "",
    val verseRef: String = "",
    /** A held silence before the first question, in seconds. 0 for none. */
    val stillness: Int = 0,
    /** A held silence near the end, for listening. 0 for none. */
    val listen: Int = 0,
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
            p("What is on my heart today?"),
            p("What am I asking God for?"),
            p("Who needs my prayers today?"),
            p("What Scripture am I standing on?"),
            p("What am I surrendering to Him?"),
        ),
        prayer = true,
    ),
    /*
     * Morning Prayer, written out in full as a book.
     *
     * The shape is the one the good printed prayer journals use, and the
     * reason they use it is that it removes decisions. Somebody at seven in the
     * morning should not have to work out what to pray about; the page should
     * already know, and should ask one thing at a time.
     *
     * Adoration comes before asking, which is the whole argument of an ACTS
     * journal - begin with who God is and the page cannot become a wishlist.
     * Confession is marked private and skippable, because a page that shames
     * somebody for passing over it will be passed over for good. Listening sits
     * near the end and records what came to mind without telling the writer
     * what it was.
     */
    JournalTemplate(
        "prayer-morning", "Prayer", "Morning prayer",
        "Begin the day with God.",
        listOf(
            TemplatePrompt(
                "ADORATION", "God, You are…",
                "Begin with who God is, not with what you need.",
                starter = "Father, You are faithful…",
            ),
            TemplatePrompt(
                "SURRENDER", "What are you carrying this morning?",
                "Leave it here before you carry it all day.",
            ),
            TemplatePrompt(
                "CONFESSION", "Search my heart, O God.",
                "Anything you need to bring to Him honestly.",
                private = true, skippable = true,
            ),
            TemplatePrompt(
                "GRATITUDE", "Before I ask, I remember.",
                "Three things you are thankful for this morning.",
                starter = "Today, thank You for…",
            ),
            TemplatePrompt(
                "TODAY'S WORD", "What stands out to you?",
                "Read it slowly, then write what caught you.",
            ),
            TemplatePrompt(
                "PEOPLE ON MY HEART", "Who are you praying for?",
                "Name them, and say what you are asking for them.",
                people = true,
            ),
            TemplatePrompt(
                "I AM ASKING", "What do you need from God today?",
                "Plainly. He is not put off by a direct request.",
            ),
            TemplatePrompt(
                "I SURRENDER", "What will you stop trying to control today?",
                "Different from confession. This is letting go.",
                starter = "I place this in Your hands…",
            ),
            TemplatePrompt(
                "MY PRAYER", "Father, this morning…",
                "Everything above, in your own words.",
            ),
        ),
        prayer = true,
        verse = "My voice shalt thou hear in the morning, O LORD; in the morning will I direct my prayer unto thee, and will look up.",
        verseRef = "Psalm 5:3",
        stillness = 30,
        listen = 60,
    ),
    JournalTemplate(
        "prayer-evening", "Prayer", "Evening prayer",
        "Put the day down",
        listOf(
            p("What happened today that I want to tell You"),
            p("What I am sorry for"),
            p("What I am thankful for"),
            p("What I am leaving with You tonight"),
        ),
    ),
    JournalTemplate(
        "prayer-bed", "Prayer", "Prayer before bed",
        "Short, for when you are already tired",
        listOf(
            p("What I am still turning over"),
            p("What I am handing over before I sleep"),
        ),
    ),
    JournalTemplate(
        "prayer-self", "Prayer", "Prayer for myself",
        "The one people find hardest to write",
        listOf(
            p("What do I actually need right now?"),
            p("What am I afraid to ask for?"),
            p("What would change if He answered?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-someone", "Prayer", "Prayer for someone else",
        "Hold one person up by name",
        listOf(
            p("Who am I praying for?"),
            p("What are they facing?"),
            p("What am I asking God to do for them?"),
            p("What Scripture am I praying over them?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-intercession", "Prayer", "Intercession",
        "Standing in the gap for others",
        listOf(
            p("Who or what am I standing in the gap for?"),
            p("What is the need, plainly?"),
            p("What am I asking God to do?"),
            p("How long have I been praying this?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-family", "Prayer", "Prayer for my family",
        "Each one, by name",
        listOf(
            p("Who in my family am I praying for today?"),
            p("What do they need?"),
            p("What am I asking God for our home?"),
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-children", "Prayer", "Prayer for my children",
        "For the ones you are raising",
        listOf(
            p("What I see in them that I thank God for"),
            p("What I am worried about"),
            p("What I am asking God to do in them"),
            p("What I want them to know about Him"),
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-marriage", "Prayer", "Prayer for my marriage",
        "For the two of you",
        listOf(
            p("What I am thankful for in my marriage"),
            p("Where we are struggling"),
            p("What I am asking God to change in me first"),
            p("What I am asking Him to do between us"),
        ),
        journal = "Family", prayer = true,
    ),
    JournalTemplate(
        "prayer-healing", "Prayer", "Prayer for healing",
        "For a body, a mind, or a wound",
        listOf(
            p("Who needs healing, and what kind?"),
            p("What am I asking God for specifically?"),
            p("What Scripture am I holding on to?"),
            p("What is hard about waiting for this?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-work", "Prayer", "Prayer for work",
        "The hours you spend most of your life in",
        listOf(
            p("What is in front of me at work?"),
            p("Where do I need wisdom?"),
            p("Who do I work with that I should pray for?"),
            p("How can this work be done for Him today?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-finances", "Prayer", "Prayer for finances",
        "Honest about money",
        listOf(
            p("What is the real situation?"),
            p("What am I anxious about?"),
            p("What am I asking God for?"),
            p("What am I trusting Him with instead of solving?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-guidance", "Prayer", "Prayer for guidance",
        "When the next step is not obvious",
        listOf(
            p("What am I trying to decide?"),
            p("What do I already sense He is saying?"),
            p("What am I afraid of choosing wrongly?"),
            p("What am I asking Him to make clear?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "prayer-open", "Prayer", "Open prayer",
        "No structure. Just write it.",
        listOf(
            p("Father…"),
        ),
        prayer = true,
    ),

    /* ── Daily walk · 10 ───────────────────────────────────────────── */

    JournalTemplate(
        "walk-morning", "Daily walk", "Morning with God",
        "Five quiet minutes before anything else",
        listOf(
            p("What is the first thing on my mind?"),
            p("What am I reading or thinking about today?"),
            p("What am I asking for?"),
            p("What do I want to carry into today?"),
        ),
    ),
    JournalTemplate(
        "walk-evening", "Daily walk", "Evening with God",
        "Look back over the day with Him",
        listOf(
            p("Where was God in today?"),
            p("What did I get right?"),
            p("What would I do differently?"),
            p("What am I thankful for tonight?"),
        ),
    ),
    JournalTemplate(
        "walk-start", "Daily walk", "Start my day",
        "Three lines, then go",
        listOf(
            p("Today I am…"),
            p("Today I need…"),
            p("Today I will…"),
        ),
    ),
    JournalTemplate(
        "walk-end", "Daily walk", "End my day",
        "Three lines, then sleep",
        listOf(
            p("Today was…"),
            p("I am thankful for…"),
            p("I am leaving behind…"),
        ),
    ),
    JournalTemplate(
        "walk-five", "Daily walk", "Five minutes with God",
        "When that is honestly all there is",
        listOf(
            p("One thing I want to say to Him"),
            p("One thing I want to ask Him"),
        ),
    ),
    JournalTemplate(
        "walk-today", "Daily walk", "Today with God",
        "The whole day, in one page",
        listOf(
            p("What happened today?"),
            p("Where did I notice Him?"),
            p("What did today teach me?"),
        ),
    ),
    JournalTemplate(
        "walk-moment", "Daily walk", "A God moment",
        "The thing you will forget by Friday",
        listOf(
            p("What happened?"),
            p("Why did it stay with me?"),
            p("What do I think He was saying?"),
        ),
    ),
    JournalTemplate(
        "walk-saw", "Daily walk", "Where I saw God today",
        "Look for Him in the ordinary parts",
        listOf(
            p("In something small"),
            p("In another person"),
            p("In something that did not go my way"),
        ),
    ),
    JournalTemplate(
        "walk-sleep", "Daily walk", "Before I sleep",
        "The last thought of the day",
        listOf(
            p("What I am putting down"),
            p("What I am thankful for"),
            p("What I am trusting Him with tonight"),
        ),
    ),
    JournalTemplate(
        "walk-checkin", "Daily walk", "Daily spiritual check-in",
        "An honest state of things",
        listOf(
            p("How is my heart today, really?"),
            p("How has my prayer life been this week?"),
            p("What am I avoiding?"),
            p("What is one step back towards Him?"),
        ),
    ),

    /* ── Bible & Scripture · 10 ────────────────────────────────────── */

    JournalTemplate(
        "bible-soap", "Bible & Scripture", "SOAP study",
        "Scripture, Observation, Application, Prayer",
        listOf(
            p("SCRIPTURE — write the verse out in full"),
            p("OBSERVATION — what does it actually say?"),
            p("APPLICATION — what does it ask of me?"),
            p("PRAYER — pray it back to Him"),
        ),
    ),
    JournalTemplate(
        "bible-study", "Bible & Scripture", "Bible study",
        "A passage, worked through",
        listOf(
            p("Passage"),
            p("What is happening here?"),
            p("What does this say about God?"),
            p("What does this say about people?"),
            p("What will I do about it?"),
        ),
    ),
    JournalTemplate(
        "bible-verse", "Bible & Scripture", "Verse reflection",
        "One verse, sat with",
        listOf(
            p("The verse"),
            p("What stands out"),
            p("What it means for today"),
        ),
    ),
    JournalTemplate(
        "bible-chapter", "Bible & Scripture", "Chapter study",
        "A whole chapter at once",
        listOf(
            p("Chapter"),
            p("What happens in it"),
            p("The verse that stopped me"),
            p("What I take from the whole chapter"),
        ),
    ),
    JournalTemplate(
        "bible-character", "Bible & Scripture", "Character study",
        "A person in Scripture, closely",
        listOf(
            p("Who am I studying?"),
            p("What did they do?"),
            p("Where did they fail?"),
            p("How did God deal with them?"),
            p("What is here for me?"),
        ),
    ),
    JournalTemplate(
        "bible-meditation", "Bible & Scripture", "Scripture meditation",
        "Slowly, one line at a time",
        listOf(
            p("The passage"),
            p("Read it once — what do I notice?"),
            p("Read it again — what word stays?"),
            p("Read it a third time — what is He saying?"),
        ),
    ),
    JournalTemplate(
        "bible-mean", "Bible & Scripture", "What does this verse mean to me?",
        "Between the text and your life",
        listOf(
            p("The verse"),
            p("What it meant then"),
            p("What it means for where I am now"),
        ),
    ),
    JournalTemplate(
        "bible-remember", "Bible & Scripture", "Scripture to remember",
        "Worth carrying",
        listOf(
            p("The verse"),
            p("Why I want to keep it"),
            p("When I will need it"),
        ),
    ),
    JournalTemplate(
        "bible-notes", "Bible & Scripture", "Bible reading notes",
        "Plain notes as you read",
        listOf(
            p("What I read"),
            p("Notes"),
            p("Questions I still have"),
        ),
    ),
    JournalTemplate(
        "bible-apply", "Bible & Scripture", "Scripture application",
        "From reading to doing",
        listOf(
            p("The passage"),
            p("What it asks of me"),
            p("One thing I will change this week"),
            p("How I will know I did it"),
        ),
    ),

    /* ── Answered prayer · 8 ───────────────────────────────────────── */

    JournalTemplate(
        "answered-new", "Answered prayer", "New prayer request",
        "Start the record. Come back to it later.",
        listOf(
            p("What am I praying for?"),
            p("Why this matters to me"),
            p("Scripture I am standing on"),
            p("Who is praying with me"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "answered-mark", "Answered prayer", "He answered",
        "Write it down while you still remember the detail",
        listOf(
            p("What did I ask for?"),
            p("What happened?"),
            p("How did He answer — was it what I expected?"),
            p("What did I learn about Him?"),
        ),
    ),
    JournalTemplate(
        "answered-update", "Answered prayer", "Update on a prayer",
        "Nothing finished yet, but something moved",
        listOf(
            p("What am I still praying for?"),
            p("What has changed?"),
            p("What am I asking now?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "answered-testimony", "Answered prayer", "Testimony",
        "The version you could tell someone",
        listOf(
            p("What was the situation?"),
            p("What did I pray?"),
            p("What did God do?"),
            p("What do I want people to know from this?"),
        ),
    ),
    JournalTemplate(
        "answered-waiting", "Answered prayer", "Still waiting",
        "Honest about the ones that are not answered yet",
        listOf(
            p("What am I still waiting on?"),
            p("How long has it been?"),
            p("What is hard about the waiting?"),
            p("What keeps me praying it anyway?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "answered-no", "Answered prayer", "When the answer was no",
        "This belongs in the record too",
        listOf(
            p("What did I ask for?"),
            p("What happened instead?"),
            p("What do I not understand?"),
            p("What am I choosing to trust?"),
        ),
    ),
    JournalTemplate(
        "answered-review", "Answered prayer", "Looking back over my prayers",
        "Read the old ones and see",
        listOf(
            p("What have I been praying this year?"),
            p("What has He already answered?"),
            p("What pattern do I see?"),
        ),
    ),
    JournalTemplate(
        "answered-thanks", "Answered prayer", "Thank You for answering",
        "Close the loop",
        listOf(
            p("What He did"),
            p("What I want to say to Him about it"),
        ),
    ),

    /* ── Gratitude · 8 ─────────────────────────────────────────────── */

    JournalTemplate(
        "grat-three", "Gratitude", "Three things",
        "The shortest useful entry there is",
        listOf(
            p("One"),
            p("Two"),
            p("Three"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-thank", "Gratitude", "Thank You, God",
        "Said directly to Him",
        listOf(
            p("Thank You for…"),
            p("Thank You especially for…"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-provision", "Gratitude", "His provision today",
        "What arrived that you did not arrange",
        listOf(
            p("What I needed"),
            p("What came"),
            p("What that tells me about Him"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-people", "Gratitude", "People I am thankful for",
        "Name them",
        listOf(
            p("Who"),
            p("Why"),
            p("Will I tell them?"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-small", "Gratitude", "Small blessings",
        "The ones too small to mention",
        listOf(
            p("Something small today I am glad of"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-unexpected", "Gratitude", "An unexpected blessing",
        "The thing you were not praying for",
        listOf(
            p("What happened?"),
            p("Why it surprised me"),
            p("What I want to say about it"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-week", "Gratitude", "Weekly gratitude",
        "Once a week, look back over seven days",
        listOf(
            p("The best thing this week"),
            p("The hardest thing, and what came of it"),
            p("Three things I am thankful for"),
        ),
        journal = "Gratitude",
    ),
    JournalTemplate(
        "grat-hard", "Gratitude", "Gratitude in a hard season",
        "Not pretending. Still looking.",
        listOf(
            p("What is hard right now"),
            p("What is still true and good"),
            p("One thing I can thank Him for today"),
        ),
        journal = "Gratitude",
    ),

    /* ── Reflection · 8 ────────────────────────────────────────────── */

    JournalTemplate(
        "ref-week", "Reflection", "This week",
        "Seven days, looked at properly",
        listOf(
            p("What happened"),
            p("What I learned"),
            p("What I want to carry forward"),
        ),
    ),
    JournalTemplate(
        "ref-month", "Reflection", "This month",
        "A wider view",
        listOf(
            p("What changed this month"),
            p("Where did I grow?"),
            p("Where did I go backwards?"),
            p("What am I asking for next month?"),
        ),
    ),
    JournalTemplate(
        "ref-year", "Reflection", "This year",
        "The long look",
        listOf(
            p("What defined this year"),
            p("What God did"),
            p("What I got wrong"),
            p("What I am taking into next year"),
        ),
    ),
    JournalTemplate(
        "ref-examen", "Reflection", "Evening examen",
        "An old practice: walk back through the day",
        listOf(
            p("What am I grateful for today?"),
            p("Where did I feel most alive?"),
            p("Where did I turn away from God or people?"),
            p("What do I want tomorrow to look like?"),
        ),
    ),
    JournalTemplate(
        "ref-honest", "Reflection", "An honest page",
        "For things you would not say out loud",
        listOf(
            p("What I am actually thinking"),
            p("What I have not admitted"),
            p("What I want to do about it"),
        ),
    ),
    JournalTemplate(
        "ref-lesson", "Reflection", "What I learned",
        "One lesson, written down so it sticks",
        listOf(
            p("What happened"),
            p("What I learned"),
            p("What it will change"),
        ),
    ),
    JournalTemplate(
        "ref-letter", "Reflection", "A letter to God",
        "Write to Him, not about Him",
        listOf(
            p("Dear Father,"),
        ),
    ),
    JournalTemplate(
        "ref-future", "Reflection", "A letter to myself, later",
        "For the version of you who reads this in a year",
        listOf(
            p("Where I am now"),
            p("What I hope has happened by then"),
            p("What I want you to remember"),
        ),
    ),

    /* ── Spiritual warfare · 8 ─────────────────────────────────────── */

    JournalTemplate(
        "war-fighting", "Spiritual warfare", "What I am fighting in prayer",
        "Name it plainly",
        listOf(
            p("What am I up against?"),
            p("What am I asking God to do?"),
            p("What Scripture am I standing on?"),
            p("What am I refusing to agree with?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-fear", "Spiritual warfare", "Prayer against fear",
        "For the thing that wakes you at three",
        listOf(
            p("What am I afraid of?"),
            p("What is true instead?"),
            p("What am I asking Him for?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-thoughts", "Spiritual warfare", "Breaking a thought pattern",
        "The sentence you keep saying to yourself",
        listOf(
            p("What do I keep telling myself?"),
            p("Where did that come from?"),
            p("What does God say instead?"),
            p("What will I say next time it comes?"),
        ),
    ),
    JournalTemplate(
        "war-standing", "Spiritual warfare", "Scripture I am standing on",
        "Write the verse. Then write why.",
        listOf(
            p("The verse"),
            p("What I am facing"),
            p("What this verse settles"),
        ),
    ),
    JournalTemplate(
        "war-armour", "Spiritual warfare", "The armour of God",
        "Ephesians 6, one piece at a time",
        listOf(
            p("Truth — what lie am I putting down?"),
            p("Righteousness — what am I making right?"),
            p("Peace — where do I need to stand still?"),
            p("Faith — what am I trusting Him for?"),
            p("Salvation — what do I know is settled?"),
            p("The Word — what am I praying back to Him?"),
        ),
    ),
    JournalTemplate(
        "war-temptation", "Spiritual warfare", "Prayer against temptation",
        "Honest, and without shame",
        listOf(
            p("What keeps pulling at me?"),
            p("When is it strongest?"),
            p("What will I do differently?"),
            p("What am I asking God for?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-protection", "Spiritual warfare", "Prayer for protection",
        "Over people, a home, a journey",
        listOf(
            p("Who or what am I covering?"),
            p("What am I asking God to guard?"),
            p("Scripture I am praying"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "war-victory", "Spiritual warfare", "Victory journal",
        "Write down what He has already won",
        listOf(
            p("What was I fighting?"),
            p("What changed?"),
            p("What do I now know?"),
        ),
    ),

    /* ── Spiritual growth · 7 ──────────────────────────────────────── */

    JournalTemplate(
        "grow-where", "Spiritual growth", "Where I am growing",
        "Notice it, or you will miss it",
        listOf(
            p("What is different in me than a year ago?"),
            p("What made the difference?"),
            p("What is next?"),
        ),
    ),
    JournalTemplate(
        "grow-stuck", "Spiritual growth", "Where I am stuck",
        "The same thing, again",
        listOf(
            p("What keeps repeating?"),
            p("What have I tried?"),
            p("What have I not tried?"),
            p("What am I asking for?"),
        ),
    ),
    JournalTemplate(
        "grow-conviction", "Spiritual growth", "Something convicted me",
        "The part that was uncomfortable",
        listOf(
            p("What was it?"),
            p("Why did it land?"),
            p("What will I do?"),
        ),
    ),
    JournalTemplate(
        "grow-habit", "Spiritual growth", "A habit I am building",
        "Small, and repeated",
        listOf(
            p("The habit"),
            p("Why it matters"),
            p("How it has gone this week"),
            p("What will help next week"),
        ),
    ),
    JournalTemplate(
        "grow-fruit", "Spiritual growth", "Fruit of the Spirit check",
        "Galatians 5, honestly",
        listOf(
            p("Which of these is growing in me?"),
            p("Which is missing right now?"),
            p("Where was that tested this week?"),
        ),
    ),
    JournalTemplate(
        "grow-obedience", "Spiritual growth", "Something He asked me to do",
        "And what you did about it",
        listOf(
            p("What did I sense He was asking?"),
            p("Did I do it?"),
            p("What happened?"),
        ),
    ),
    JournalTemplate(
        "grow-mentor", "Spiritual growth", "Someone I am learning from",
        "Who is ahead of you, and what you see",
        listOf(
            p("Who"),
            p("What I see in them"),
            p("What I want to learn"),
        ),
    ),

    /* ── Difficult seasons · 6 ─────────────────────────────────────── */

    JournalTemplate(
        "hard-afraid", "Difficult seasons", "When I am afraid",
        "Put the fear into words first",
        listOf(
            p("What am I afraid of?"),
            p("What is the worst I am imagining?"),
            p("What is actually true?"),
            p("What do I need from God right now?"),
        ),
    ),
    JournalTemplate(
        "hard-anxious", "Difficult seasons", "When I am anxious",
        "For when it will not settle",
        listOf(
            p("What is my mind circling?"),
            p("What is in my control?"),
            p("What is not?"),
            p("What am I handing over?"),
        ),
    ),
    JournalTemplate(
        "hard-lonely", "Difficult seasons", "When I am lonely",
        "Written for a real evening",
        listOf(
            p("What does the loneliness feel like?"),
            p("When did it start?"),
            p("Who could I reach for?"),
            p("What do I want to say to God about it?"),
        ),
    ),
    JournalTemplate(
        "hard-grief", "Difficult seasons", "When I am grieving",
        "No stages, no timeline",
        listOf(
            p("Who or what have I lost?"),
            p("What do I miss today?"),
            p("What do I want to remember?"),
            p("What do I want to say to God?"),
        ),
    ),
    JournalTemplate(
        "hard-silence", "Difficult seasons", "When God feels silent",
        "This is allowed to be written down",
        listOf(
            p("How long has it felt like this?"),
            p("What did I expect Him to say?"),
            p("What do I still believe when I do not feel it?"),
            p("What am I going to keep doing anyway?"),
        ),
    ),
    JournalTemplate(
        "hard-nopray", "Difficult seasons", "When I do not know what to pray",
        "Start anywhere",
        listOf(
            p("What I would say if I could"),
            p("What I want Him to know"),
            p("Help me with…"),
        ),
        prayer = true,
    ),

    /* ── Family · 5 ────────────────────────────────────────────────── */

    JournalTemplate(
        "fam-home", "Family", "Our home",
        "What is happening under this roof",
        listOf(
            p("How is our home right now?"),
            p("What needs prayer?"),
            p("What am I thankful for?"),
        ),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-child", "Family", "About one of my children",
        "One child, one page",
        listOf(
            p("Who"),
            p("What I am noticing"),
            p("What I am praying for them"),
            p("What I want to tell them"),
        ),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-spouse", "Family", "About my husband or wife",
        "Written for you, not for them",
        listOf(
            p("What I am grateful for"),
            p("What is hard"),
            p("What I will do this week"),
        ),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-parents", "Family", "My parents",
        "Whatever that relationship is",
        listOf(
            p("Where things stand"),
            p("What I carry from them"),
            p("What I am praying"),
        ),
        journal = "Family",
    ),
    JournalTemplate(
        "fam-hard", "Family", "A hard relationship",
        "Honest, and not for sending",
        listOf(
            p("Who, and what happened"),
            p("My part in it"),
            p("What forgiveness would look like"),
            p("What I am asking God for"),
        ),
        journal = "Family",
    ),

    /* ── Purpose & decisions · 5 ───────────────────────────────────── */

    JournalTemplate(
        "purp-decision", "Purpose & decisions", "A decision before God",
        "Lay it out before you choose",
        listOf(
            p("What am I deciding?"),
            p("The case for"),
            p("The case against"),
            p("What is my gut, and why?"),
            p("What am I asking God to show me?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "purp-calling", "Purpose & decisions", "What is God calling me to?",
        "The long question",
        listOf(
            p("What keeps coming back to me?"),
            p("What am I good at that serves others?"),
            p("What would I do if I were not afraid?"),
            p("What is one step I could take?"),
        ),
    ),
    JournalTemplate(
        "purp-discern", "Purpose & decisions", "Discernment",
        "Testing what you think you are hearing",
        listOf(
            p("What do I think God is saying?"),
            p("Does it agree with Scripture?"),
            p("What do wise people say?"),
            p("What is my motive, honestly?"),
            p("What happens if I wait?"),
        ),
    ),
    JournalTemplate(
        "purp-doors", "Purpose & decisions", "Open door, closed door",
        "What has opened and what has shut",
        listOf(
            p("What has opened"),
            p("What has closed"),
            p("What I make of that"),
        ),
    ),
    JournalTemplate(
        "purp-purpose", "Purpose & decisions", "Why am I here?",
        "Occasionally worth asking",
        listOf(
            p("What am I for?"),
            p("Who is better off because of me?"),
            p("What would I regret not doing?"),
        ),
    ),

    /* ── Dreams & notes · 4 ────────────────────────────────────────── */

    JournalTemplate(
        "dream-record", "Dreams & notes", "A dream",
        "Record it before you interpret it",
        listOf(
            p("What happened in it"),
            p("How it felt"),
            p("What I noticed on waking"),
        ),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-recurring", "Dreams & notes", "A recurring dream",
        "When it keeps coming back",
        listOf(
            p("The dream"),
            p("How often"),
            p("What is always the same"),
            p("What I am asking God about it"),
        ),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-impression", "Dreams & notes", "Something I sensed",
        "An impression, written down plainly",
        listOf(
            p("What I sensed"),
            p("When and where"),
            p("What I will do with it — if anything yet"),
        ),
        journal = "Dreams",
    ),
    JournalTemplate(
        "dream-word", "Dreams & notes", "A word someone gave me",
        "Keep the record, test it later",
        listOf(
            p("Who said it"),
            p("What they said"),
            p("What I think about it"),
            p("What happened afterwards"),
        ),
        journal = "Dreams",
    ),

    /* ── Sermon & church · 3 ───────────────────────────────────────── */

    JournalTemplate(
        "church-sermon", "Sermon & church", "Sermon notes",
        "Sunday, in a form you can use on Wednesday",
        listOf(
            p("Church and speaker"),
            p("Scripture"),
            p("The main message"),
            p("What stood out"),
            p("What convicted me"),
            p("What I will do differently"),
        ),
        journal = "Church",
    ),
    JournalTemplate(
        "church-service", "Sermon & church", "Church today",
        "The whole morning, not only the sermon",
        listOf(
            p("What we sang that stayed with me"),
            p("What was said"),
            p("Who I spoke to"),
            p("What I am taking home"),
        ),
        journal = "Church",
    ),
    JournalTemplate(
        "church-group", "Sermon & church", "Bible study group",
        "What the room said, and what you thought",
        listOf(
            p("What we studied"),
            p("What others saw that I did not"),
            p("My own question"),
            p("What I will follow up"),
        ),
        journal = "Church",
    ),
    JournalTemplate(
        "prayer-update", "Prayer", "Prayer update",
        "Return to a prayer matter and notice what has changed",
        listOf(
            p("What am I still bringing to God?"),
            p("What has changed since I last wrote?"),
            p("What Scripture is helping me keep going?"),
            p("What is my next faithful step?"),
        ),
        prayer = true,
    ),
    JournalTemplate(
        "scripture-prayer", "Bible & Scripture", "Pray this Scripture",
        "Let one passage become your prayer today",
        listOf(
            p("The Scripture I am carrying is…"),
            p("What do I notice about God here?"),
            p("My prayer from this passage is…"),
            p("How will I carry this into today?"),
        ),
        journal = "Scripture",
        prayer = true,
    ),
    JournalTemplate(
        "remember-faithfulness", "Answered prayer", "Remember His faithfulness",
        "Keep a record of what God has carried you through",
        listOf(
            p("What was I asking God for?"),
            p("What happened, in its own time?"),
            p("What do I want to remember when I am afraid again?"),
            p("My thank-you prayer is…"),
        ),
        journal = "Answered prayer",
        prayer = true,
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
    prompts.joinToString("\n\n") { "## " + it.heading.ifBlank { it.question } + "\n" } + "\n"

/**
 * The finished page, built from what was actually written.
 *
 * Pages left empty leave nothing behind - a book you stopped halfway through
 * should read as far as you got, not as a run of blank headings.
 */
fun JournalTemplate.compose(answers: Map<Int, String>): String = buildString {
    prompts.forEachIndexed { index, prompt ->
        val written = answers[index].orEmpty().trim()
        if (written.isEmpty()) return@forEachIndexed
        append("## ").append(prompt.heading.ifBlank { prompt.question }).append("\n\n")
        append(written).append("\n\n")
    }
}

fun composeOf(template: JournalTemplate, answers: Map<Int, String>): String =
    template.compose(answers)
