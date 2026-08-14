package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.view.HapticFeedbackConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Challenge
import com.prayerkey.manna.data.Devotion
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.R
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.GiltLine
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.Ivory
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import com.prayerkey.manna.ui.worlds.VerseWorld
import com.prayerkey.manna.ui.worlds.WorldScene
import java.time.LocalDate
import java.time.LocalTime

/**
 * The editorial Home: one greeting, one daily focus and the practices that
 * are actually in progress. Navigation is deliberately left to the dock.
 */
@Composable
fun AwardHomeDashboard(
    name: String,
    streak: Int,
    activeDays: Set<LocalDate>,
    devotion: Devotion?,
    bible: Challenge?,
    prayer: Challenge?,
    savedCount: Int,
    journalCount: Int,
    sermonCount: Int,
    onOpenWord: () -> Unit,
    onWriteDevotion: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    onOpenChallenge: (Challenge) -> Unit,
    onToggleChallenge: (Challenge) -> Unit,
    onSettings: () -> Unit,
) {
    PrayerKeyOperatingHome(
        streak = streak,
        savedCount = savedCount,
        journalCount = journalCount,
        sermonCount = sermonCount,
        devotion = devotion,
        bible = bible,
        prayer = prayer,
        onOpenWord = onOpenWord,
        onWriteDevotion = onWriteDevotion,
        onOpenBible = onOpenBible,
        onOpenPrayer = onOpenPrayer,
        onOpenJournal = onOpenJournal,
        onOpenChurch = onOpenChurch,
        onSettings = onSettings,
    )
}

@Composable
private fun ExactDailyHome(
    devotion: Devotion?,
    bible: Challenge?,
    prayer: Challenge?,
    activeDays: Set<LocalDate>,
    onOpenWord: () -> Unit,
    onWriteDevotion: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    onSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var dayOffset by rememberSaveable { mutableIntStateOf(0) }
    val today = LocalDate.now().plusDays(dayOffset.toLong())
    val context = LocalContext.current
    val journeyPrefs = remember { context.getSharedPreferences("daily_journey", android.content.Context.MODE_PRIVATE) }
    val journeyKey = remember(today) { "step_$today" }
    var quoteOpen by remember { mutableStateOf(false) }
    var passageOpen by remember { mutableStateOf(false) }
    var devotionalOpen by remember { mutableStateOf(false) }
    var prayerCardOpen by remember { mutableStateOf(false) }
    var faithTool by remember { mutableStateOf<FaithTool?>(null) }
    var prayerEntrance by remember { mutableIntStateOf(0) }
    var journeyStep by rememberSaveable(today.toString()) { mutableIntStateOf(journeyPrefs.getInt(journeyKey, 0)) }
    fun advanceJourney(step: Int) {
        journeyStep = maxOf(journeyStep, step)
        journeyPrefs.edit().putInt(journeyKey, journeyStep).apply()
    }
    var backdropIndex by remember { mutableIntStateOf(Math.floorMod(today.toEpochDay().toInt(), 120)) }
    val dailyPassage = remember(today) { com.prayerkey.manna.data.passageFor(today) }
    val dailyDevotional = remember(today) { com.prayerkey.manna.data.devotionalFor(today) }
    val dailyPrayer = remember(today) { com.prayerkey.manna.data.dailyPrayerFor(today) }
    val dailyQuote = remember(today) { com.prayerkey.manna.data.quoteFor(today) }
    var dailyVerseText by remember(today) { mutableStateOf("") }
    LaunchedEffect(today, dailyPassage) {
        dailyVerseText = OfflineBible(context).chapter(dailyPassage.book, dailyPassage.chapter)
            .firstOrNull { it.verse == dailyPassage.firstVerse }?.text.orEmpty()
    }
    val dailyVerseArtwork = remember(today) {
        val cards = intArrayOf(
            R.drawable.verse_card_botanical,
            R.drawable.verse_card_dawn,
            R.drawable.verse_card_oxblood,
        )
        cards[Math.floorMod(today.toEpochDay().toInt(), cards.size)]
    }
    if (quoteOpen) {
        QuoteCinema(
            quote = dailyQuote.text,
            author = dailyQuote.author,
            backdropIndex = backdropIndex,
            onComplete = { quoteOpen = false; advanceJourney(1) },
        )
        return
    }
    if (passageOpen) {
        DailyPassageReader(dailyPassage, onComplete = { passageOpen = false; advanceJourney(2) })
        return
    }
    if (devotionalOpen) {
        DailyDevotionalReader(dailyDevotional, dailyPassage, onComplete = { devotionalOpen = false; advanceJourney(3) })
        return
    }
    if (prayerCardOpen) {
        DailyPrayerCard(dailyPrayer, prayerEntrance, onComplete = { prayerCardOpen = false; advanceJourney(4) })
        return
    }
    when (faithTool) {
        FaithTool.Midnight -> {
            DailyPrayerCard(midnightPrayer(dailyPrayer), prayerEntrance, onComplete = { faithTool = null })
            return
        }
        FaithTool.Points -> {
            PrayerPointsScreen(reference = dailyPassage.reference, onClose = { faithTool = null })
            return
        }
        FaithTool.Someone -> {
            PrayForSomeoneScreen(onClose = { faithTool = null })
            return
        }
        null -> Unit
    }
    Column(
        Modifier.fillMaxSize().background(cs.background).verticalScroll(rememberScrollState())
            .padding(bottom = 112.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().background(com.prayerkey.manna.ui.theme.Pk.Cream)
                .statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "PRAYERKEY",
                color = com.prayerkey.manna.ui.theme.Pk.Charcoal,
                fontFamily = DisplaySerif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(com.prayerkey.manna.ui.theme.Pk.Oxblood)
                    .clickable {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://www.prayerkey.com/donate"),
                                ),
                            )
                        }
                    }.padding(horizontal = 13.dp, vertical = 10.dp),
            ) {
                Text("DONATE", color = Ivory, fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
            }
            Icon(
                Icons.Outlined.Search,
                "Search Bible",
                tint = com.prayerkey.manna.ui.theme.Pk.Charcoal,
                modifier = Modifier.padding(start = 15.dp).size(25.dp).clickable(onClick = onOpenBible),
            )
            Icon(
                Icons.Outlined.Menu,
                "Open menu",
                tint = com.prayerkey.manna.ui.theme.Pk.Charcoal,
                modifier = Modifier.padding(start = 16.dp).size(27.dp).clickable(onClick = onSettings),
            )
        }

        Box(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)
                .height(176.dp).clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.today_manuscript_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(com.prayerkey.manna.ui.theme.Pk.Charcoal.copy(alpha = .24f)))
            Text(
                "Today",
                color = Ivory,
                fontFamily = UtilitySans,
                fontSize = 68.sp,
                lineHeight = 70.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(com.prayerkey.manna.ui.theme.Pk.Sunken)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "←",
                color = com.prayerkey.manna.ui.theme.Pk.Muted,
                fontFamily = UtilitySans,
                fontSize = 24.sp,
                modifier = Modifier.clickable { dayOffset-- }.padding(8.dp),
            )
            Text(
                today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu")),
                color = com.prayerkey.manna.ui.theme.Pk.Charcoal,
                fontFamily = UtilitySans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Text(
                "→",
                color = com.prayerkey.manna.ui.theme.Pk.Muted,
                fontFamily = UtilitySans,
                fontSize = 24.sp,
                modifier = Modifier.clickable { dayOffset++ }.padding(8.dp),
            )
        }

        Text(
            "BIBLE VERSE OF THE DAY",
            color = com.prayerkey.manna.ui.theme.Pk.Muted,
            fontFamily = UtilitySans,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp),
        )
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                .height(390.dp).clip(RoundedCornerShape(18.dp))
                .clickable { passageOpen = true },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(dailyVerseArtwork),
                contentDescription = "${dailyPassage.reference}, Bible verse of the day",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                Modifier.fillMaxSize().padding(horizontal = 52.dp, vertical = 82.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (dailyVerseText.isBlank()) "Loading today’s word…" else "“$dailyVerseText”",
                    color = com.prayerkey.manna.ui.theme.Pk.Charcoal,
                    fontFamily = BookSerif,
                    fontSize = when {
                        dailyVerseText.length > 190 -> 18.sp
                        dailyVerseText.length > 120 -> 20.sp
                        else -> 23.sp
                    },
                    lineHeight = when {
                        dailyVerseText.length > 190 -> 25.sp
                        dailyVerseText.length > 120 -> 28.sp
                        else -> 32.sp
                    },
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Box(
                    Modifier.padding(top = 18.dp, bottom = 13.dp).width(52.dp).height(1.dp)
                        .background(com.prayerkey.manna.ui.theme.Pk.Gold),
                )
                Text(
                    "${dailyPassage.book} ${dailyPassage.chapter}:${dailyPassage.firstVerse}",
                    color = com.prayerkey.manna.ui.theme.Pk.Oxblood,
                    fontFamily = UtilitySans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
            }
        }

        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(com.prayerkey.manna.ui.theme.Pk.Sunken)
                    .border(1.dp, com.prayerkey.manna.ui.theme.Pk.Hair, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "TODAY’S DEVOTION",
                            color = com.prayerkey.manna.ui.theme.Pk.Oxblood,
                            fontFamily = UtilitySans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                        )
                        Text(
                            dailyPassage.title,
                            color = cs.onSurface,
                            fontFamily = DisplaySerif,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            dailyPassage.reference.uppercase(),
                            color = com.prayerkey.manna.ui.theme.Pk.Muted,
                            fontFamily = UtilitySans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = .8.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                    Box(
                        Modifier.size(48.dp).clip(CircleShape)
                            .background(com.prayerkey.manna.ui.theme.Pk.Blush.copy(alpha = .45f))
                            .clickable(onClick = onWriteDevotion),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, "Save today", tint = cs.primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Text(
                "DAILY JOURNEY",
                color = com.prayerkey.manna.ui.theme.Pk.Muted,
                fontFamily = UtilitySans,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(top = 26.dp, bottom = 12.dp),
            )

            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(com.prayerkey.manna.ui.theme.Pk.Sunken)
                    .border(1.dp, com.prayerkey.manna.ui.theme.Pk.Hair, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                    Text("  QUOTE", color = cs.primary, fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.3.sp, modifier = Modifier.weight(1f))
                    if (journeyStep > 0) Box(Modifier.size(22.dp).clip(CircleShape).background(cs.onSurface.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Check, null, tint = cs.onSurface.copy(alpha = .62f), modifier = Modifier.size(15.dp))
                    }
                }
                Text("TODAY’S QUOTE FROM", color = cs.onSurface.copy(alpha = .55f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.1.sp, modifier = Modifier.padding(top = 18.dp))
                Text(dailyQuote.author, color = cs.onSurface, fontFamily = UtilitySans, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 3.dp))
                Text("“${dailyQuote.text}”", color = cs.onSurface.copy(alpha = .86f), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp))
                Box(
                    Modifier.fillMaxWidth().padding(top = 18.dp).height(50.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (journeyStep == 0) cs.primary else cs.onSurface.copy(alpha = .08f))
                        .clickable {
                            backdropIndex = (backdropIndex + 1) % 120
                            quoteOpen = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (journeyStep > 0) "✓  COMPLETED" else "READ", color = if (journeyStep == 0) cs.onPrimary else cs.onSurface.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .24.sp)
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(top = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(com.prayerkey.manna.ui.theme.Pk.Sunken)
                    .border(1.dp, com.prayerkey.manna.ui.theme.Pk.Hair, RoundedCornerShape(16.dp)),
            ) {
                DailyJourneyRow(Icons.Outlined.MenuBook, "Passage", "3 MIN", active = journeyStep == 1, done = journeyStep > 1, showRule = true) { passageOpen = true }
                DailyJourneyRow(Icons.Outlined.EditNote, "Devotional", "4 MIN", active = journeyStep == 2, done = journeyStep > 2, showRule = true) { devotionalOpen = true }
                DailyJourneyRow(Icons.Outlined.SelfImprovement, "Prayer", "5 MIN", active = journeyStep == 3, done = journeyStep > 3, showRule = false) {
                    prayerEntrance++
                    prayerCardOpen = true
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(top = 14.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Ink).clickable(onClick = onWriteDevotion)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("REFLECT", color = GiltLine, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text(dailyDevotional.reflectionQuestion, color = Ivory, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 24.sp, modifier = Modifier.padding(top = 7.dp))
                Text("WRITE A THOUGHT  →", color = Ivory.copy(alpha = .60f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = .7.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Text("MORE FOR YOUR FAITH", color = cs.onSurface, fontFamily = DisplaySerif, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .42.sp, modifier = Modifier.padding(top = 30.dp, bottom = 12.dp))
            DevotionalRow(Icons.Outlined.Nightlight, "Midnight prayer", "PRAY", {
                prayerEntrance++
                faithTool = FaithTool.Midnight
            })
            DevotionalRow(Icons.Outlined.FormatListBulleted, "Prayer points", "OPEN", { faithTool = FaithTool.Points })
            DevotionalRow(Icons.Outlined.Groups, "Pray for someone", "OPEN", { faithTool = FaithTool.Someone })
            DevotionalRow(Icons.Outlined.BookmarkBorder, "Write in your journal", "WRITE", onOpenJournal)
            DevotionalRow(Icons.Outlined.MicNone, "Record the sermon", "START", onOpenChurch)
            Box(
                Modifier.fillMaxWidth().padding(top = 28.dp).clip(RoundedCornerShape(14.dp)).background(Ink),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "They gathered it morning by morning, each as much as he needed.",
                    color = Ivory.copy(alpha = .60f), fontFamily = BookSerif, fontSize = 11.sp,
                    lineHeight = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun QuoteCinema(
    quote: String,
    author: String,
    backdropIndex: Int,
    onComplete: () -> Unit,
) {
    androidx.activity.compose.BackHandler(onBack = onComplete)
    val context = LocalContext.current
    val worlds = VerseWorld.entries
    val overlays = listOf(
        Color(0xFF07111F), Color(0xFF15243A), Color(0xFF251B2C),
        Color(0xFF162820), Color(0xFF2B2118), Color(0xFF111111),
    )
    // Four photographs × five crop positions × six cinematic color grades
    // gives 120 distinct treatments without downloading anything at runtime.
    val world = worlds[backdropIndex % worlds.size]
    val grade = overlays[(backdropIndex / worlds.size) % overlays.size]

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        WorldScene(world, animate = true, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(grade.copy(alpha = .64f), Color.Black.copy(alpha = .34f), Color.Black.copy(alpha = .76f)))))

        Text(
            "PRAYERKEY", color = Color.White, fontFamily = DisplaySerif,
            fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 54.dp),
        )
        Column(
            Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("“$quote”", color = Color.White, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 24.sp, lineHeight = 34.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(author.uppercase(), color = Color.White, fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, modifier = Modifier.padding(top = 24.dp))
            Icon(Icons.Outlined.FavoriteBorder, "Save quote", tint = Color.White, modifier = Modifier.padding(top = 22.dp).size(30.dp))
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 22.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp))
                    .background(Color(0xE61A1A1A)).clickable {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "“$quote” — $author\n\nShared from PrayerKey")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share quote"))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("  SHARE QUOTE", color = Color.White, fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "TAP HERE TO COMPLETE", color = Color.White, fontFamily = UtilitySans, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, letterSpacing = .7.sp,
                modifier = Modifier.padding(top = 22.dp).clickable(onClick = onComplete),
            )
        }
    }
}

@Composable
private fun DailyJourneyRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    duration: String,
    active: Boolean,
    done: Boolean,
    showRule: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().height(64.dp)
            .then(if (showRule) Modifier.drawBehind {
                drawLine(Color(0x1712161F), Offset(18.dp.toPx(), size.height), Offset(size.width - 18.dp.toPx(), size.height), 1.dp.toPx())
            } else Modifier)
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(if (active) cs.primary else cs.onSurface.copy(alpha = .06f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (done) Icons.Outlined.Check else icon,
                contentDescription = null,
                tint = if (active) cs.onPrimary else cs.onSurface.copy(alpha = if (done) .48f else .78f),
                modifier = Modifier.size(18.dp),
            )
        }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Text(title, color = cs.onSurface, fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            if (active) Text("NEXT STEP", color = cs.primary, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text(
            if (done) "DONE" else duration,
            color = if (active) cs.primary else cs.onSurface.copy(alpha = .55f),
            fontFamily = UtilitySans, fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DevotionalRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: String,
    onClick: () -> Unit,
    done: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(com.prayerkey.manna.ui.theme.Pk.Sunken)
            .border(1.dp, com.prayerkey.manna.ui.theme.Pk.Hair, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val timed = trailing.contains("MIN")
        Box(
            Modifier.size(42.dp).clip(CircleShape)
                .background(com.prayerkey.manna.ui.theme.Pk.Blush.copy(alpha = .38f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = com.prayerkey.manna.ui.theme.Pk.Oxblood, modifier = Modifier.size(20.dp))
        }
        Text(title, color = cs.onSurface, fontFamily = UtilitySans, fontSize = if (timed) 13.sp else 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).padding(start = 13.dp))
        Box(
            Modifier.clip(RoundedCornerShape(99.dp))
                .background(if (done) com.prayerkey.manna.ui.theme.Pk.Sage else com.prayerkey.manna.ui.theme.Pk.Blush.copy(alpha = .55f))
                .padding(horizontal = 11.dp, vertical = 7.dp),
        ) {
            Text(
                if (done) "DONE" else trailing,
                color = if (done) com.prayerkey.manna.ui.theme.Pk.Cream else com.prayerkey.manna.ui.theme.Pk.Oxblood,
                fontFamily = UtilitySans,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .6.sp,
            )
        }
    }
}

@Composable
private fun LegacyAwardHomeDashboard(
    name: String,
    streak: Int,
    activeDays: Set<LocalDate>,
    devotion: Devotion?,
    bible: Challenge?,
    prayer: Challenge?,
    savedCount: Int,
    journalCount: Int,
    sermonCount: Int,
    onOpenWord: () -> Unit,
    onWriteDevotion: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    onOpenChallenge: (Challenge) -> Unit,
    onToggleChallenge: (Challenge) -> Unit,
    onSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val today = LocalDate.now()

    Column(
        Modifier.fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 22.dp, bottom = 112.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(greetingLine(), color = cs.onBackground.copy(alpha = .52f), fontSize = 13.sp)
                Text(
                    if (name.isBlank()) "Your time with God today" else "$name, your time with God",
                    color = cs.onBackground,
                    fontFamily = BookSerif,
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    letterSpacing = (-.55).sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(cs.onBackground.copy(alpha = .045f))
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Settings, "Settings", tint = cs.onBackground.copy(alpha = .58f), modifier = Modifier.size(20.dp))
            }
        }

        WeekRhythm(today, activeDays, streak)

        FaithSnapshot(streak, savedCount, journalCount)

        Text(
            "TODAY",
            color = cs.onBackground.copy(alpha = .44f),
            fontSize = 9.5.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
        )

        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cs.surface)
                .border(.7.dp, cs.outlineVariant, RoundedCornerShape(18.dp))
                .padding(20.dp),
        ) {
            if (devotion != null) {
                Text(
                    "“${devotion.verse}”",
                    color = cs.onSurface,
                    fontFamily = BookSerif,
                    fontSize = 22.sp,
                    lineHeight = 31.sp,
                )
                Text(
                    devotion.reference.uppercase(),
                    color = cs.primary,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    devotion.reflection,
                    color = cs.onSurface.copy(alpha = .64f),
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 14.dp),
                )
            } else {
                Text("Receive a word for this moment.", color = cs.onSurface, fontFamily = BookSerif, fontSize = 22.sp)
            }

            Box(
                Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp)
                    .clip(RoundedCornerShape(15.dp)).background(cs.onSurface)
                    .clickable(onClick = onOpenWord),
                contentAlignment = Alignment.Center,
            ) { Text("Start today’s devotion", color = cs.surface, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }

        Text(
            "TODAY’S STEPS",
            color = cs.onBackground.copy(alpha = .44f), fontSize = 9.5.sp,
            letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
        )
        DailyPathRow(Icons.Outlined.MenuBook, "Read the Bible", "Today’s passage · 3 min", onOpenBible)
        DailyPathRow(Icons.Outlined.EditNote, "Think about it", "A short reflection · 2 min", onWriteDevotion)
        DailyPathRow(Icons.Outlined.SelfImprovement, "Pray", devotion?.prayerTitle ?: "A prayer for today · 5 min", onOpenPrayer)
        DailyPathRow(Icons.Outlined.BookmarkBorder, "Write in your journal", "Save your thoughts · 3 min", onOpenJournal)

        Text(
            "WAYS TO PRAY",
            color = cs.onBackground.copy(alpha = .44f), fontSize = 9.5.sp,
            letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            PrayerFocusCard(Icons.Outlined.WbSunny, "Morning prayer", "Start your day", Modifier.weight(1f), onOpenPrayer)
            PrayerFocusCard(Icons.Outlined.Nightlight, "Midnight prayer", "Pray at night", Modifier.weight(1f), onOpenPrayer)
        }
        Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            PrayerFocusCard(Icons.Outlined.FormatListBulleted, "Prayer points", "Choose a topic", Modifier.weight(1f), onOpenPrayer)
            PrayerFocusCard(Icons.Outlined.Groups, "Pray for someone", "Family and friends", Modifier.weight(1f), onOpenPrayer)
        }

        if (bible != null || prayer != null) {
            Text(
                "READING & PRAYER PLANS",
                color = cs.onBackground.copy(alpha = .44f),
                fontSize = 9.5.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
            )
            bible?.let { PracticeRow(it, { onOpenChallenge(it) }, { onToggleChallenge(it) }) }
            if (bible != null && prayer != null) Spacer(Modifier.height(9.dp))
            prayer?.let { PracticeRow(it, { onOpenChallenge(it) }, { onToggleChallenge(it) }) }
        }

        Text(
            "AT CHURCH",
            color = cs.onBackground.copy(alpha = .44f), fontSize = 9.5.sp,
            letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
        )
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(cs.onSurface).clickable(onClick = onOpenChurch).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(cs.primary.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.MicNone, null, tint = cs.primary, modifier = Modifier.size(21.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("Record the sermon", color = cs.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (sermonCount > 0) Text("$sermonCount ${if (sermonCount == 1) "sermon" else "sermons"}", color = cs.surface.copy(alpha = .62f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Icon(Icons.Outlined.ChevronRight, "Open Church", tint = cs.primary)
        }
    }
}

@Composable
private fun PrayerFocusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    note: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(cs.surface)
            .border(.7.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(15.dp),
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(21.dp))
        Text(title, color = cs.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
        Text(note, color = cs.onSurface.copy(alpha = .52f), fontSize = 10.5.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun FaithSnapshot(streak: Int, saved: Int, journal: Int) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(streak to "day streak", saved to "verses saved", journal to "journal entries").forEach { (value, label) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(cs.surface.copy(alpha = .72f)).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(value.toString(), color = cs.onSurface, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(label, color = cs.onSurface.copy(alpha = .5f), fontSize = 9.5.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun DailyPathRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(15.dp))
            .background(cs.surface).border(.7.dp, cs.outlineVariant, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = cs.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = cs.onSurface.copy(alpha = .55f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Outlined.ChevronRight, "Open $title", tint = cs.onSurface.copy(alpha = .32f), modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun WeekRhythm(today: LocalDate, activeDays: Set<LocalDate>, streak: Int) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(top = 20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (6 downTo 0).forEach { back ->
                val day = today.minusDays(back.toLong())
                val active = day in activeDays
                val current = day == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.dayOfWeek.name.take(1), color = cs.onBackground.copy(alpha = .38f), fontSize = 9.5.sp)
                    Box(
                        Modifier.padding(top = 6.dp).size(30.dp).clip(CircleShape)
                            .background(if (active) cs.primary else androidx.compose.ui.graphics.Color.Transparent)
                            .then(if (current && !active) Modifier.border(1.dp, cs.primary, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.dayOfMonth.toString(),
                            color = if (active) cs.onPrimary else if (current) cs.primary else cs.onBackground.copy(alpha = .5f),
                            fontSize = 11.5.sp,
                            fontWeight = if (active || current) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
        if (streak > 0) {
            Text(
                "$streak ${if (streak == 1) "day" else "days"} of returning",
                color = cs.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun QuietAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier.height(48.dp).clip(RoundedCornerShape(14.dp))
            .background(if (primary) cs.primary.copy(alpha = .10f) else cs.onSurface.copy(alpha = .035f))
            .then(if (!primary) Modifier.border(.7.dp, cs.outlineVariant, RoundedCornerShape(14.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val actionColor = if (primary) cs.primary else cs.onSurface.copy(alpha = .66f)
        Icon(icon, null, tint = actionColor, modifier = Modifier.size(17.dp))
        Text(label, color = actionColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 7.dp))
    }
}

@Composable
private fun PracticeRow(challenge: Challenge, onOpen: () -> Unit, onToggle: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val done = challenge.today.done
    val view = LocalView.current
    val checkScale = remember { Animatable(1f) }
    LaunchedEffect(done) {
        if (done) {
            checkScale.snapTo(.72f)
            checkScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(cs.surface).border(.7.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(challenge.title, color = cs.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                challenge.today.label,
                color = cs.onSurface.copy(alpha = .58f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(if (done) cs.primary else androidx.compose.ui.graphics.Color.Transparent)
                .border(1.dp, if (done) cs.primary else cs.outlineVariant, CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onToggle()
                }
                .scale(checkScale.value),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                if (done) "Mark today incomplete" else "Mark today complete",
                tint = if (done) cs.onPrimary else cs.onSurface.copy(alpha = .38f),
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

private fun greetingLine(): String = when (LocalTime.now().hour) {
    in 0..4 -> "Still awake"
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
