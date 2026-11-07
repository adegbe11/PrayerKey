package com.prayerkey.manna.ui.home

import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

private enum class ExperienceKind { LIST, GUIDE, COURSE, JOURNAL, LIBRARY, SCHEDULE, COMMUNITY }
private data class Experience(
    val kind: ExperienceKind,
    val eyebrow: String,
    val intro: String,
    val items: List<String>,
    val prompt: String,
    val action: String,
)

private fun experience(title: String): Experience = when (title) {
    "Prayer ledger" -> Experience(ExperienceKind.JOURNAL, "PRAYERS AND ANSWERS", "Write the prayer and the date. Come back and write what happened. It stays on the list until you mark it answered.", listOf("Write a new prayer", "Open prayers", "Answered prayers", "How God answered", "Year in review", "Share an answer card"), "What are you asking God for?", "ADD TO MY LEDGER")
    "I need prayer now" -> Experience(ExperienceKind.COMMUNITY, "PRAYER RIGHT NOW", "Write one line about what is wrong and start praying now. It stays on your phone. Nobody else sees it.", listOf("Write my need", "Read the verse", "Pray the guided prayer", "Save to my prayer ledger", "Choose a reminder"), "I need prayer for…", "SAVE PRIVATE REQUEST")
    "Sunday into Monday" -> Experience(ExperienceKind.COURSE, "SUNDAY CONTINUES", "Keep Sunday's sermon going for five days, a few minutes each morning.", listOf("Monday · Remember the message", "Tuesday · Read the main passage", "Wednesday · Examine my life", "Thursday · Pray the message", "Friday · Practice one truth"), "What was Sunday's sermon about?", "CREATE MY WEEK")
    /* The three items here used to be "My family", "Wisdom for the next step" and
       "Healing and strength" — which read as the user's own list on a screen
       headed YOUR PRIVATE ALTAR, on a first launch, before they had written
       anything. Prompts now, phrased as prompts. */
    "My prayer list" -> Experience(ExperienceKind.LIST, "YOUR PRIVATE ALTAR", "The people and problems you are praying about, all in one list.", listOf("Someone you are carrying", "A decision ahead of you", "A body that needs healing"), "Write a name or prayer need", "ADD PRAYER")
    "Prayer points" -> Experience(ExperienceKind.GUIDE, "PRAY BY SITUATION", "Pick what you are facing. Each one has prayer points from the Bible, one at a time.", listOf("Health & body", "Family & children", "Work & money", "Travel & protection", "Justice & legal matters", "Spiritual warfare"), "What are you standing in prayer for?", "BEGIN PRAYING")
    "Prayer times" -> Experience(ExperienceKind.SCHEDULE, "PRAYER TIMES", "A written prayer for morning, midday, evening and midnight, and a reminder for each.", listOf("Morning · Give God the day", "Midday · Stop for a minute", "Evening · Say thank you", "Midnight · Keep watch"), "Choose a prayer time", "SET REMINDER")
    "Voice prayers" -> Experience(ExperienceKind.JOURNAL, "PRAY ALOUD", "Pray out loud. PrayerKey writes down what you said so you can read it later.", listOf("Record a new prayer", "Recent voice prayers", "Extract prayer points", "Move an answer to Testimonies"), "What is on your heart?", "START RECORDING")
    "Prayer timer" -> Experience(ExperienceKind.SCHEDULE, "PRAYER TIMER", "Set a time and pray until it ends. Nothing will interrupt you.", listOf("5 minutes · Be still", "10 minutes · Daily prayer", "15 minutes · Four movements", "30 minutes · Deep prayer", "60 minutes · Prayer watch"), "Select a session", "START TIMER")
    "Prayer challenge" -> Experience(ExperienceKind.COURSE, "PRAYER CHALLENGE", "One prayer a day. Pray today's and tomorrow's opens.", listOf("21 Days of Midnight Prayer", "30 Prayers for My Family", "40 Days Before the Door Opens", "7 Days Back to the Altar", "14 Days of Thanksgiving"), "Choose your challenge", "START DAY 1")
    "Fasting" -> Experience(ExperienceKind.COURSE, "SET APART THE TIME", "Get ready, pray each day, and write down what God shows you.", listOf("Single-day fast", "Daniel fast", "Esther fast", "Three-day fast", "Seven-day fast", "Custom or church-wide fast"), "What is the purpose of this fast?", "PREPARE MY FAST")
    "Forgiveness" -> Experience(ExperienceKind.GUIDE, "FORGIVENESS", "Private, and at your own pace. Forgiving someone never means going back to danger or pretending it did not happen.", listOf("Name what happened", "Say what it cost you", "Read the promise", "Pray the release", "Decide what to do next"), "Who or what are you ready to place before God?", "BEGIN PRIVATELY")
    "Confession" -> Experience(ExperienceKind.GUIDE, "CONFESSION", "No need to dress it up. Look honestly at your heart, read the promise, and decide what to change.", listOf("My relationship with God", "Words and attitudes", "Relationships", "Habits and integrity", "Things left undone"), "Lord, search me and know my heart…", "BEGIN EXAMINATION")
    "Bible challenge" -> Experience(ExperienceKind.COURSE, "BIBLE CHALLENGE", "A reading plan with set chapters each day. Read today's and tomorrow's open.", listOf("Bible in One Year", "New Testament in 90 Days", "Psalms in 30 Days", "The Gospels in 40 Days", "Proverbs Each Month"), "Pick a plan", "READ DAY 1")
    "Memory verse" -> Experience(ExperienceKind.COURSE, "HIDE THE WORD", "Read it, hear it, then hide a few words at a time until you can say it without looking.", listOf("Read it aloud", "Learn one phrase", "Hide key words", "Recite from memory", "Review tomorrow"), "Today · Philippians 4:6-7", "START PRACTICE")
    "Devotional" -> Experience(ExperienceKind.LIBRARY, "DEVOTIONAL", "A short reading every day: a passage, a few paragraphs about it, and a prayer.", listOf("Today’s devotional", "Listen", "Saved devotionals", "Previous days", "Reflection notes"), "A quiet word for today", "READ TODAY")
    "Names of God" -> Experience(ExperienceKind.LIBRARY, "KNOW WHO HE IS", "The names God is called in the Bible, where they come from, and how to pray with them.", listOf("YHWH · The LORD", "El Shaddai · God Almighty", "Jehovah Jireh · The LORD Provides", "Jehovah Rapha · The LORD Heals", "Prince of Peace"), "Search a name or need", "EXPLORE")
    "Hymn of the day" -> Experience(ExperienceKind.LIBRARY, "SING THE FAITH", "One old hymn a day, the story of who wrote it, and the verse behind it.", listOf("Today · Great Is Thy Faithfulness", "The story behind the hymn", "Related Scripture", "Listen", "Saved hymns"), "Today’s hymn", "LISTEN")
    "Anchors" -> Experience(ExperienceKind.LIBRARY, "WORDS CHRISTIANS CARRY", "The prayers and creeds Christians have learned by heart for centuries.", listOf("The Lord’s Prayer", "The Apostles’ Creed", "The Nicene Creed", "The Ten Commandments", "The Beatitudes", "Psalm 23"), "Pick one", "READ")
    "Quote of the day" -> Experience(ExperienceKind.LIBRARY, "WISDOM FOR TODAY", "One quote a day from a Christian writer, with who said it.", listOf("Today’s quote", "Author context", "Saved quotes", "365-day archive"), "Today's quote", "OPEN TODAY’S QUOTE")
    // "Healing for Mum" / "A new work opportunity" / "Peace in our home" read as
    // the user's own answered prayers. They were nobody's.
    "Answered" -> Experience(ExperienceKind.LIST, "REMEMBER HIS FAITHFULNESS", "When God answers, write down what you asked and what happened.", listOf("What you asked for", "What actually changed", "The date it happened"), "What prayer has been answered?", "RECORD AN ANSWER")
    "Thanksgiving" -> Experience(ExperienceKind.JOURNAL, "THANKSGIVING", "Write down what you are thankful for today. Read it back on a hard day.", listOf("Today’s gratitude", "Seven-day thanksgiving", "Voice thanksgiving", "Past memories"), "Father, thank You for…", "WRITE THANKS")
    "Dreams" -> Experience(ExperienceKind.JOURNAL, "DREAM JOURNAL", "Record details before interpreting them. PrayerKey does not claim every dream is prophetic.", listOf("People", "Places", "Words", "Emotions", "What happened before waking"), "Record everything you remember", "NEW DREAM")
    "Midnight prayer" -> Experience(ExperienceKind.COURSE, "THE MIDNIGHT WATCH", "Seven nights of prayer at midnight. Pray tonight's and tomorrow night's opens.", listOf("Night 1 · Consecration", "Night 2 · Family", "Night 3 · Deliverance", "Night 4 · Open doors", "Night 5 · Healing", "Night 6 · Nation and church", "Night 7 · Thanksgiving"), "What are you bringing before God tonight?", "BEGIN NIGHT 1")
    "Warfare prayer" -> Experience(ExperienceKind.GUIDE, "STAND IN SCRIPTURE", "Pick what you are fighting and pray it through with the Bible. The enemy is never a person.", listOf("Freedom from fear", "Breaking destructive habits", "Protection of my home", "Peace over confusion", "Strength against temptation", "Justice and vindication"), "What situation are you praying through?", "OPEN PRAYER SET")
    "Testimonies" -> Experience(ExperienceKind.JOURNAL, "TELL WHAT GOD DID", "Write down what God did. Keep it to yourself or share it.", listOf("The situation before", "What changed", "The prayer connected to it", "Scripture", "Private · Anonymous · Public"), "What did God do?", "CREATE TESTIMONY")
    "Resolutions" -> Experience(ExperienceKind.LIST, "THIS YEAR", "Write what you want to change this year, in small steps you can check each month.", listOf("Spiritual life", "Family", "Health", "Work", "Service", "Stewardship"), "What do you want to change?", "ADD RESOLUTION")
    "Word for the year" -> Experience(ExperienceKind.JOURNAL, "ONE WORD", "Pick one word for the year and a verse to go with it.", listOf("Choose my word", "My verse", "Prayer and declaration", "Monthly check-in", "Create wallpaper"), "My word for this year is…", "SET MY WORD")
    "Milestones" -> Experience(ExperienceKind.LIST, "MILESTONES", "The days you want to remember: the first plan you finished, the first answered prayer.", listOf("First completed Bible plan", "Seven-day prayer streak", "First recorded testimony", "Fast completed", "Create my own milestone"), "What do you want to remember?", "ADD MILESTONE")
    "Nation & church" -> Experience(ExperienceKind.GUIDE, "STAND IN THE GAP", "Pray for your country, its leaders, your town, and Christians in danger.", listOf("My nation", "Leaders", "Local community", "The persecuted church", "Missionaries", "Pastors and churches"), "Choose an intercession focus", "PRAY NOW")
    "Watchman rota" -> Experience(ExperienceKind.SCHEDULE, "TAKE YOUR WATCH", "Take an hour on your church's prayer rota and leave a note for the next person.", listOf("6:00 AM · Morning watch", "12:00 PM · Midday watch", "6:00 PM · Evening watch", "12:00 AM · Midnight watch"), "Select an available watch", "JOIN THIS WATCH")
            else -> Experience(ExperienceKind.GUIDE, "PRAYERKEY", "Start here.", listOf("Begin", "Reflect", "Save your progress"), "What is on your heart?", "CONTINUE")
}

/**
 * A programme.
 *
 * [onKeep] is why this exists in its current form. What the user wrote here
 * used to go to `prefs.putString("note_$title", ...)` — **one string per
 * programme, overwritten every time** — under a button that said "Prayer added
 * to your private list". There was no list. Nothing written here ever reached
 * the prayer list, the journal or anything else in the app; it was a parallel
 * store that only ever held the most recent sentence.
 *
 * It now goes to the same place the rest of the app keeps things, so a prayer
 * added here is in the prayer list and a dream is in the journal.
 */
@Composable internal fun DiscoveryExperienceScreen(
    title: String,
    detail: String,
    icon: ImageVector,
    onKeep: (String, String) -> Unit,
    onClose: () -> Unit,
    openHub: () -> Unit,
) {
    /*
     * The named challenges replace four configurators.
     *
     * Every one of these titles used to open a screen that asked questions
     * before day one - "CHOOSE YOUR FOCUS", a length, a reminder hour - and
     * produced something with no name. A plan nobody can name is a plan nobody
     * can recommend, and the writing inside it can never be specific, because
     * it has to serve every combination. The prayer challenge alone had over
     * 2,700 of them.
     *
     * These now open a written plan directly, or the shelf when the title is a
     * category rather than a plan.
     */
    /* Midnight prayer is the nightly office, not the seven-night challenge.
       The Midnight Watch is still on the shelf for anybody who wants to keep
       seven of them in a row. */
    if (title in setOf("Midnight prayer", "Three watches", "Night vigil")) {
        com.prayerkey.manna.ui.morning.MidnightPrayerScreen(onClose)
        return
    }
    if (title == "Night prayer") {
        com.prayerkey.manna.ui.morning.NightPrayerScreen(onClose)
        return
    }
    val namedChallenge = when (title) {
        "Warfare prayer", "Imprecatory Psalms" -> "standing-in-scripture"
        "Start a fast", "Daniel fast", "Full fast", "Partial fast", "Esther fast",
        "Timed fast", "First fruits", "Church-wide fast", "Fasting record" -> "fasting-and-prayer"
        else -> null
    }
    if (namedChallenge != null) {
        com.prayerkey.manna.ui.challenge.ChallengeById(namedChallenge, onClose)
        return
    }
    if (title in setOf(
            "Prayer challenge", "30 prayers for my family", "40 days before the door opens",
            "Back to the altar", "Thanksgiving challenge", "Church challenge",
        )
    ) {
        com.prayerkey.manna.ui.challenge.ChallengeShelfRoute(onClose)
        return
    }
    if (title == "Bible challenge") {
        BibleChallengeMachine(onClose)
        return
    }
    val faithMachine = when (title) {
        "Prayer points" -> "Prayer points"
        "Memory verse" -> "Memory verse"
        "Declarations" -> "Declarations"
        else -> null
    }
    if (faithMachine != null) {
        FaithProgrammeMachine(faithMachine, onClose)
        return
    }
    val spec = remember(title) { experience(title) }
    val context = platformContext()
    val prefs = remember { context.keyValues("discovery_experiences") }
    var note by remember(title) { mutableStateOf(prefs.getString("note_$title", "").orEmpty()) }
    var completed by remember(title) { mutableStateOf(prefs.getStringSet("done_$title", emptySet()).orEmpty()) }
    var confirmation by remember(title) { mutableStateOf<String?>(null) }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal) }
                Text("PRAYERKEY", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.6.sp, color = Pk.Oxblood)
                Text("${completed.size}/${spec.items.size}", fontFamily = UtilitySans, fontSize = 11.sp, color = Pk.Muted)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                /* Title, then the thing. The icon medallion, the tracked
                   eyebrow and the paragraph of explanation above it were three
                   pieces of furniture between the user and what they came for.
                   No screen in this app introduces itself any more. */
                Text(title, Modifier.padding(top = 4.dp, bottom = 20.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 35.sp, lineHeight = 40.sp, color = Pk.Charcoal)
                when (spec.kind) {
                    ExperienceKind.COURSE -> CourseProgress(completed.size, spec.items.size)
                    else -> Unit
                }
                spec.items.forEachIndexed { index, item ->
                    val isDone = item in completed
                    val locked = spec.kind == ExperienceKind.COURSE && index > completed.size
                    Surface(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable(enabled = !locked) {
                            completed = if (isDone) completed - item else completed + item
                            prefs.edit().putStringSet("done_$title", completed).apply()
                        },
                        shape = RoundedCornerShape(18.dp), color = if (isDone) Pk.Oxblood.copy(alpha = .07f) else Color.White,
                        border = BorderStroke(1.dp, if (isDone) Pk.Oxblood.copy(alpha = .22f) else Pk.Hair),
                    ) {
                        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (locked) Icons.Outlined.Lock else if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (locked) Pk.Muted else Pk.Oxblood, modifier = Modifier.size(21.dp))
                            Text(item, Modifier.weight(1f).padding(start = 13.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (locked) Pk.Muted else Pk.Charcoal)
                            if (!locked) Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = note, onValueChange = { note = it; prefs.edit().putString("note_$title", it).apply() },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp), minLines = if (spec.kind == ExperienceKind.JOURNAL) 5 else 2,
                    label = { Text(spec.prompt) }, shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Pk.Oxblood, focusedLabelColor = Pk.Oxblood),
                )
                Spacer(Modifier.height(22.dp))
            }
            confirmation?.let {
                Text(it, Modifier.fillMaxWidth().padding(horizontal = 22.dp), color = Pk.Sage,
                    fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    when (spec.kind) {
                        ExperienceKind.COURSE -> {
                            val next = spec.items.getOrNull(completed.size)
                            if (next != null) {
                                completed = completed + next
                                prefs.edit().putStringSet("done_$title", completed).apply()
                                confirmation = if (completed.size == spec.items.size) "Finished." else "Done for today."
                            } else confirmation = "Finished."
                        }
                        else -> {
                            // kept in the app's own store, not a private preference
                            onKeep(title, note)
                            prefs.edit().putString("note_$title", note).apply()
                            confirmation = when (title) {
                                "Dreams" -> "Dream saved."
                                "My prayer list" -> "Added."
                                "Testimonies" -> "Testimony saved."
                                else -> "Saved."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood),
            ) {
                Text(
                    if (spec.kind == ExperienceKind.COURSE && completed.isNotEmpty() && completed.size < spec.items.size)
                        "COMPLETE ${completed.size + 1} OF ${spec.items.size}" else spec.action,
                    fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .8.sp,
                )
            }
        }
    }
}

@Composable private fun CourseProgress(done: Int, total: Int) {
    Column(Modifier.padding(bottom = 18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Your progress", style = MaterialTheme.typography.labelSmall, color = Pk.Muted); Text("$done of $total", style = MaterialTheme.typography.labelSmall, color = Pk.Oxblood) }
        LinearProgressIndicator(progress = { if (total == 0) 0f else done.toFloat() / total }, Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp), color = Pk.Oxblood, trackColor = Pk.Hair)
    }
}
