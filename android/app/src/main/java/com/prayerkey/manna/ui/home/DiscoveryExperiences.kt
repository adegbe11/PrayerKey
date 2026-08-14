package com.prayerkey.manna.ui.home

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
    "My prayer list" -> Experience(ExperienceKind.LIST, "YOUR PRIVATE ALTAR", "Keep the people and situations you are carrying before God in one quiet place.", listOf("My family", "Wisdom for the next step", "Healing and strength"), "Write a name or prayer need", "ADD PRAYER")
    "Prayer points" -> Experience(ExperienceKind.GUIDE, "PRAY BY SITUATION", "Choose the need before you. Each path opens Scripture-led prayer points you can pray one at a time.", listOf("Health & body", "Family & children", "Work & money", "Travel & protection", "Justice & legal matters", "Spiritual warfare"), "What are you standing in prayer for?", "BEGIN PRAYING")
    "Prayer times" -> Experience(ExperienceKind.SCHEDULE, "A RHYTHM OF PRAYER", "Return your attention to God throughout the day with complete prayers and timely reminders.", listOf("Morning · Surrender the day", "Midday · Renew your focus", "Evening · Review with gratitude", "Midnight · Take your watch"), "Choose a prayer time", "SET REMINDER")
    "Voice prayers" -> Experience(ExperienceKind.JOURNAL, "PRAY ALOUD", "Speak naturally. PrayerKey keeps your words as a private prayer transcript you can return to.", listOf("Record a new prayer", "Recent voice prayers", "Extract prayer points", "Move an answer to Testimonies"), "What is on your heart?", "START RECORDING")
    "Prayer timer" -> Experience(ExperienceKind.SCHEDULE, "STAY IN HIS PRESENCE", "Create protected time for worship, confession, requests and thanksgiving.", listOf("5 minutes · Be still", "10 minutes · Daily prayer", "15 minutes · Four movements", "30 minutes · Deep prayer", "60 minutes · Prayer watch"), "Select a session", "START TIMER")
    "Prayer challenge" -> Experience(ExperienceKind.COURSE, "BUILD CONSISTENCY", "Complete today’s prayer before the next day unlocks. Your progress is kept on this device.", listOf("21 Days of Midnight Prayer", "30 Prayers for My Family", "40 Days Before the Door Opens", "7 Days Back to the Altar", "14 Days of Thanksgiving"), "Choose your challenge", "START DAY 1")
    "Fasting" -> Experience(ExperienceKind.COURSE, "SET APART THE TIME", "Prepare wisely, pray deliberately and record what God teaches you during the fast.", listOf("Single-day fast", "Daniel fast", "Esther fast", "Three-day fast", "Seven-day fast", "Custom or church-wide fast"), "What is the purpose of this fast?", "PREPARE MY FAST")
    "Forgiveness" -> Experience(ExperienceKind.GUIDE, "RELEASE THE WEIGHT", "A private guided journey toward release. Forgiveness never requires returning to danger or denying what happened.", listOf("Name what happened", "Acknowledge what it cost", "Read the promise", "Pray the release", "Choose a healthy next step"), "Who or what are you ready to place before God?", "BEGIN PRIVATELY")
    "Confession" -> Experience(ExperienceKind.GUIDE, "RETURN WITH HONESTY", "Come without performance. Examine your heart, receive Scripture and choose a practical next step.", listOf("My relationship with God", "Words and attitudes", "Relationships", "Habits and integrity", "Things left undone"), "Lord, search me and know my heart…", "BEGIN EXAMINATION")
    "Bible challenge" -> Experience(ExperienceKind.COURSE, "READ WITH PURPOSE", "Follow a clear reading path. Finish today’s chapters before tomorrow’s portion unlocks.", listOf("Bible in One Year", "New Testament in 90 Days", "Psalms in 30 Days", "The Gospels in 40 Days", "Proverbs Each Month"), "Choose a reading journey", "READ DAY 1")
    "Memory verse" -> Experience(ExperienceKind.COURSE, "HIDE THE WORD", "Learn Scripture through reading, listening, progressive word hiding and spaced review.", listOf("Read it aloud", "Learn one phrase", "Hide key words", "Recite from memory", "Review tomorrow"), "Today · Philippians 4:6–7", "START PRACTICE")
    "Devotional" -> Experience(ExperienceKind.LIBRARY, "SCRIPTURE TO ACTION", "Daily readings that move from God’s Word to reflection, prayer and a practical response.", listOf("Today’s devotional", "Listen", "Saved devotionals", "Previous days", "Reflection notes"), "A quiet word for today", "READ TODAY")
    "Names of God" -> Experience(ExperienceKind.LIBRARY, "KNOW WHO HE IS", "Study the names of God, their biblical setting and how each truth shapes prayer.", listOf("YHWH · The LORD", "El Shaddai · God Almighty", "Jehovah Jireh · The LORD Provides", "Jehovah Rapha · The LORD Heals", "Prince of Peace"), "Search a name or need", "EXPLORE")
    "Hymn of the day" -> Experience(ExperienceKind.LIBRARY, "SING THE FAITH", "Meet one historic hymn through its story, Scripture and a licensed listening experience.", listOf("Today · Great Is Thy Faithfulness", "The story behind the hymn", "Related Scripture", "Instrumental reflection", "Saved hymns"), "Today’s hymn", "LISTEN")
    "Anchors" -> Experience(ExperienceKind.LIBRARY, "WORDS CHRISTIANS CARRY", "Read, hear and memorize the foundational prayers and declarations of the faith.", listOf("The Lord’s Prayer", "The Apostles’ Creed", "The Nicene Creed", "The Ten Commandments", "The Beatitudes", "Psalm 23"), "Choose an anchor", "READ")
    "Quote of the day" -> Experience(ExperienceKind.LIBRARY, "WISDOM FOR TODAY", "One carefully sourced Christian reflection each day, ready to save and share.", listOf("Today’s quote", "Author context", "Saved quotes", "365-day archive"), "A thought worth carrying", "OPEN TODAY’S QUOTE")
    "Answered" -> Experience(ExperienceKind.LIST, "REMEMBER HIS FAITHFULNESS", "Move answered prayers here and preserve the story of what changed.", listOf("Healing for Mum", "A new work opportunity", "Peace in our home"), "What prayer has been answered?", "RECORD AN ANSWER")
    "Thanksgiving" -> Experience(ExperienceKind.JOURNAL, "NAME THE GOODNESS", "Notice grace in ordinary days and build a record of gratitude you can revisit.", listOf("Today’s gratitude", "Seven-day thanksgiving", "Voice thanksgiving", "Past memories"), "Father, thank You for…", "WRITE THANKS")
    "Dreams" -> Experience(ExperienceKind.JOURNAL, "DREAM JOURNAL", "Record details before interpreting them. PrayerKey does not claim every dream is prophetic.", listOf("People", "Places", "Words", "Emotions", "What happened before waking"), "Record everything you remember", "NEW DREAM")
    "Midnight prayer" -> Experience(ExperienceKind.COURSE, "THE MIDNIGHT WATCH", "A guided seven-night prayer watch. Complete tonight's Scripture, prayer points and closing declaration before the next watch unlocks.", listOf("Night 1 · Consecration", "Night 2 · Family", "Night 3 · Deliverance", "Night 4 · Open doors", "Night 5 · Healing", "Night 6 · Nation and church", "Night 7 · Thanksgiving"), "What are you bringing before God tonight?", "BEGIN NIGHT 1")
    "Warfare prayer" -> Experience(ExperienceKind.GUIDE, "STAND IN SCRIPTURE", "Choose the battle before you and pray a complete Scripture-led set. PrayerKey never treats another person as the enemy.", listOf("Freedom from fear", "Breaking destructive habits", "Protection of my home", "Peace over confusion", "Strength against temptation", "Justice and vindication"), "What situation are you praying through?", "OPEN PRAYER SET")
    "Testimonies" -> Experience(ExperienceKind.JOURNAL, "TELL WHAT GOD DID", "Turn an answered prayer into a clear testimony and choose whether it stays private or is shared.", listOf("The situation before", "What changed", "The prayer connected to it", "Scripture", "Private · Anonymous · Public"), "What did God do?", "CREATE TESTIMONY")
    "Resolutions" -> Experience(ExperienceKind.LIST, "LIVE INTENTIONALLY", "Turn faith-centred intentions into small, measurable steps and monthly reflections.", listOf("Spiritual life", "Family", "Health", "Work", "Service", "Stewardship"), "What do you want to practice faithfully?", "ADD RESOLUTION")
    "Word for the year" -> Experience(ExperienceKind.JOURNAL, "YOUR YEARLY ANCHOR", "Choose one word, connect it to Scripture and revisit it through the year.", listOf("Choose my word", "Anchor Scripture", "Prayer and declaration", "Monthly check-in", "Create wallpaper"), "My word for this year is…", "SET MY WORD")
    "Milestones" -> Experience(ExperienceKind.LIST, "YOUR FAITH JOURNEY", "See the moments and practices that have shaped your walk with God.", listOf("First completed Bible plan", "Seven-day prayer streak", "First recorded testimony", "Fast completed", "Create my own milestone"), "What do you want to remember?", "ADD MILESTONE")
    "Nation & church" -> Experience(ExperienceKind.GUIDE, "STAND IN THE GAP", "Pray with clear, factual context for leaders, churches, communities and persecuted believers.", listOf("My nation", "Leaders", "Local community", "The persecuted church", "Missionaries", "Pastors and churches"), "Choose an intercession focus", "PRAY NOW")
    "Watchman rota" -> Experience(ExperienceKind.SCHEDULE, "TAKE YOUR WATCH", "Join a church prayer watch, receive a reminder and leave a handover for the next person.", listOf("6:00 AM · Morning watch", "12:00 PM · Midday watch", "6:00 PM · Evening watch", "12:00 AM · Midnight watch"), "Select an available watch", "JOIN THIS WATCH")
    "Prayer wall" -> Experience(ExperienceKind.COMMUNITY, "PRAY TOGETHER", "Stand with real requests. Mark ‘I prayed’ only after you have genuinely prayed.", listOf("Peace after bereavement · 18 prayed", "Healing before surgery · 41 prayed", "Wisdom for a family · 27 prayed", "Mission team protection · 33 prayed"), "Share a request privately or anonymously", "ADD REQUEST")
    "Prayer partner" -> Experience(ExperienceKind.COMMUNITY, "NEVER PRAY ALONE", "Invite someone you know, share selected prayers and keep a safe rhythm of encouragement.", listOf("Invite a trusted person", "Shared prayer list", "Weekly check-in", "Prayer schedule", "Safety and privacy"), "Who would you trust to pray with?", "INVITE PARTNER")
    else -> Experience(ExperienceKind.GUIDE, "PRAYERKEY", "A focused space for your next faithful step.", listOf("Begin", "Reflect", "Save your progress"), "What is on your heart?", "CONTINUE")
}

@Composable internal fun DiscoveryExperienceScreen(title: String, detail: String, icon: ImageVector, onClose: () -> Unit, openHub: () -> Unit) {
    if (title == "Prayer challenge") {
        PrayerChallengeMachine(onClose)
        return
    }
    val spec = remember(title) { experience(title) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("discovery_experiences", Context.MODE_PRIVATE) }
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
                Surface(shape = CircleShape, color = Pk.Oxblood.copy(alpha = .10f)) { Icon(icon, null, Modifier.padding(14.dp).size(27.dp), tint = Pk.Oxblood) }
                Text(spec.eyebrow, Modifier.padding(top = 20.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.4.sp, color = Pk.GoldDeep)
                Text(title, Modifier.padding(top = 7.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 35.sp, lineHeight = 40.sp, color = Pk.Charcoal)
                Text(spec.intro, Modifier.padding(top = 12.dp, bottom = 22.dp), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp, color = Pk.Muted)
                when (spec.kind) {
                    ExperienceKind.COURSE -> CourseProgress(completed.size, spec.items.size)
                    ExperienceKind.SCHEDULE -> TimeBanner()
                    ExperienceKind.COMMUNITY -> CommunityBanner()
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
                Text("Saved privately on this device", Modifier.padding(top = 9.dp, bottom = 22.dp), fontFamily = UtilitySans, fontSize = 10.sp, color = Pk.Muted)
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
                                confirmation = if (completed.size == spec.items.size) "Programme completed. Your progress is saved." else "Today's step completed. The next step is now unlocked."
                            } else confirmation = "Programme completed. Your progress is saved."
                        }
                        else -> {
                            prefs.edit().putString("note_$title", note).apply()
                            confirmation = when (title) {
                                "Dreams" -> "Dream saved privately. Review it with Scripture, prayer and wise counsel."
                                "My prayer list" -> "Prayer added to your private list."
                                "Testimonies" -> "Testimony saved privately."
                                else -> "Saved here in $title."
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("YOUR PROGRESS", style = MaterialTheme.typography.labelSmall, color = Pk.Muted); Text("$done of $total", style = MaterialTheme.typography.labelSmall, color = Pk.Oxblood) }
        LinearProgressIndicator(progress = { if (total == 0) 0f else done.toFloat() / total }, Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp), color = Pk.Oxblood, trackColor = Pk.Hair)
    }
}

@Composable private fun TimeBanner() = Surface(Modifier.fillMaxWidth().padding(bottom = 18.dp), shape = RoundedCornerShape(18.dp), color = Pk.DeepDark) {
    Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Schedule, null, tint = Pk.GoldLight); Text("Your next faithful moment can begin now.", Modifier.padding(start = 12.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White) }
}

@Composable private fun CommunityBanner() = Surface(Modifier.fillMaxWidth().padding(bottom = 18.dp), shape = RoundedCornerShape(18.dp), color = Pk.Oxblood.copy(alpha = .08f)) {
    Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.VerifiedUser, null, tint = Pk.Oxblood); Text("Privacy, consent and reporting controls are always available.", Modifier.padding(start = 12.dp), fontFamily = UtilitySans, fontSize = 12.sp, lineHeight = 18.sp, color = Pk.Charcoal) }
}
