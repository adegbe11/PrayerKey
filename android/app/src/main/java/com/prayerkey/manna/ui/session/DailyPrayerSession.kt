package com.prayerkey.manna.ui.session

import com.prayerkey.manna.resources.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleReading
import com.prayerkey.manna.data.PrayerPulls
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * The prayer session: one screen, ten pulls, nothing else on it.
 *
 * There is no dock, no tab bar and no back arrow, because everything of that
 * kind is an invitation to be somewhere else. The competitor we looked at puts
 * a music toggle, a track picker and two volume sliders between a person and
 * the first word of their prayer. That is the whole difference — this screen
 * has one job and offers exactly one thing to do.
 *
 * The gesture is written here rather than borrowed from
 * [com.prayerkey.manna.ui.components.PullDeck], which was the plan until I read
 * it: that deck wraps forever (`index % size`), treats an upward swipe as
 * "keep", and starts at zero. A session must end, must not be saveable
 * halfway, and must open where you stopped — four changes to a component three
 * other screens depend on. The tuned constants are copied across unchanged, so
 * the pull feels identical to the rest of the app without putting the working
 * deck at risk.
 */
/** One pull: a small label, the words, and a reference when there is one. */
data class PullItem(val label: String, val text: String, val reference: String? = null)

/** The day's ten pulls of prayer. */
@Composable
fun DailyPrayerSession(
    session: PrayerPulls,
    startAt: Int = 0,
    onProgress: (Int) -> Unit = {},
    onClose: () -> Unit,
) = PullSession(
    title = session.title,
    items = session.movements.map { PullItem(it.kind.label, it.text, it.reference) },
    headline = "Amen.",
    closing = "You prayed all ten.",
    verb = "prayed",
    startAt = startAt,
    onProgress = onProgress,
    onClose = onClose,
)

/** The day's chapter, in pulls. */
@Composable
fun DailyBibleSession(
    reading: BibleReading,
    startAt: Int = 0,
    onProgress: (Int) -> Unit = {},
    onClose: () -> Unit,
) = PullSession(
    title = reading.title,
    items = reading.pulls.map { PullItem("READ", it.text, it.reference) },
    headline = "Read.",
    closing = "You read the whole chapter.",
    verb = "read",
    startAt = startAt,
    onProgress = onProgress,
    onClose = onClose,
)

/**
 * The ground, while the chapter is still being read off disk.
 *
 * A Dialog whose content is null occupies no space, and every tap then counts
 * as a tap outside it, so the reader's first tap dismissed the screen instead
 * of turning the page. It must always fill.
 */
@Composable
fun PullSessionLoading(title: String) {
    val reveal = remember { Animatable(1f) }
    val light = remember { Animatable(.35f) }
    LaunchedEffect(Unit) {
        launch { reveal.animateTo(1.06f, tween(4800)) }
        launch { light.animateTo(.78f, tween(1800)) }
    }

    Box(Modifier.fillMaxSize().background(Pk.DeepDark)) {
        Image(
            painterResource(Res.drawable.verse_card_dawn),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                scaleX = reveal.value
                scaleY = reveal.value
            },
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Pk.DeepDark.copy(alpha = .42f),
                        Pk.VioletDeep.copy(alpha = .12f),
                        Pk.DeepDark.copy(alpha = .74f),
                    ),
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Pk.GoldLight.copy(alpha = light.value * .22f), Color.Transparent),
                    center = Offset(180f, 310f),
                    radius = 720f,
                ),
            ),
        )

        Column(
            Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "The word is opening",
                fontFamily = UtilitySans,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.2.sp,
                color = Pk.GoldLight,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                title,
                fontFamily = DisplaySerif,
                fontSize = 43.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.width(52.dp).height(2.dp)
                    .background(Pk.GoldLight.copy(alpha = .72f), RoundedCornerShape(99.dp)),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Preparing today’s chapter",
                fontFamily = UtilitySans,
                fontSize = 12.sp,
                letterSpacing = .5.sp,
                color = Color.White.copy(alpha = .72f),
            )
        }

        LinearProgressIndicator(
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding().padding(horizontal = 74.dp, vertical = 42.dp)
                .fillMaxWidth().height(2.dp).clip(RoundedCornerShape(99.dp)),
            color = Pk.GoldLight,
            trackColor = Color.White.copy(alpha = .18f),
        )
    }
}

/**
 * The deck both sessions are.
 *
 * Prayer and scripture differ in what they say and not in how they are pulled,
 * so they share this rather than each owning a near-copy that would drift
 * apart the first time one of them was tuned.
 */
@Composable
fun PullSession(
    title: String,
    items: List<PullItem>,
    /** The word the finished deck ends on. */
    headline: String,
    /** The line under it. */
    closing: String,
    /** What the reader just did, for the hint at the bottom. */
    verb: String,
    /** Where to open — the resume position from PullsProgress. */
    startAt: Int = 0,
    /** Fires with the number of pulls done, every time it advances. */
    onProgress: (Int) -> Unit = {},
    onClose: () -> Unit,
) {
    val movements = items
    if (movements.isEmpty()) return
    val isBible = verb == "read"

    val view = LocalView.current
    val density = LocalDensity.current
    val screenH = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    var index by remember(title) { mutableIntStateOf(startAt.coerceIn(0, movements.lastIndex)) }
    var done by remember(title) { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var lastMoveAt by remember { mutableLongStateOf(0L) }
    val anim = remember { Animatable(0f) }
    var animating by remember { mutableStateOf(false) }
    val offset = if (animating) anim.value else dragY

    LaunchedEffect(index) { onProgress(index) }

    fun advance() {
        if (animating) return
        scope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            animating = true
            anim.snapTo(dragY)
            anim.animateTo(screenH * 1.15f, tween(230))
            if (index >= movements.lastIndex) {
                onProgress(movements.size)
                done = true
            } else {
                index++
            }
            dragY = 0f
            anim.snapTo(0f)
            animating = false
        }
    }

    fun back() {
        if (animating || index == 0) return
        index--
    }

    Box(Modifier.fillMaxSize().background(Pk.DeepDark)) {
        SessionGround(isBible)
        if (done) {
            SessionComplete(
                title, headline, closing,
                isBible = isBible,
                onClose = onClose,
                again = { index = 0; done = false },
            )
            return@Box
        }

        val threshold = screenH * .18f
        val pull = (offset / threshold).coerceIn(0f, 1f)

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(title) {
                    detectTapGestures { at ->
                        if (at.x > size.width * .45f) advance() else back()
                    }
                }
                .pointerInput(title) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragY > threshold * .55f || velocity > 2.4f) advance()
                            else scope.launch {
                                animating = true
                                anim.snapTo(dragY)
                                anim.animateTo(0f, spring(dampingRatio = .62f, stiffness = Spring.StiffnessMedium))
                                dragY = 0f
                                animating = false
                            }
                        },
                    ) { change, amount ->
                        change.consume()
                        val now = System.currentTimeMillis()
                        val dt = (now - lastMoveAt).coerceAtLeast(1)
                        velocity = amount.y / dt * 16f
                        lastMoveAt = now
                        // downward only: a prayer has one direction
                        dragY = (dragY + amount.y).coerceAtLeast(0f)
                    }
                },
        ) {
            /* The movement underneath, revealed by the pull.
               It starts fully transparent, not at 35% as PullDeck's does: that
               deck's cards paint an opaque background, so the waiting one is
               hidden behind the front one. These are words on a shared ground,
               so any alpha at rest prints the next movement straight through
               the current one and both become unreadable. */
            movements.getOrNull(index + 1)?.let { next ->
                PullBody(
                    next,
                    isBible = isBible,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        val grow = .94f + (.06f * pull)
                        scaleX = grow; scaleY = grow; alpha = pull * pull
                    },
                )
            }

            PullBody(
                movements[index],
                isBible = isBible,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    translationY = offset
                    alpha = 1f - (pull * .35f)
                },
            )
        }

        SessionChrome(
            title = title,
            at = index,
            total = movements.size,
            pull = pull,
            isBible = isBible,
            onClose = onClose,
        )

        PullHint(index + 1, movements.size, verb, pull, isBible, Modifier.align(Alignment.BottomCenter))
    }
}

/** The one movement being prayed. Nothing on this layer but words. */
@Composable
private fun PullBody(movement: PullItem, isBible: Boolean, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            // the full card, so the words below know how much room they have
            Modifier.fillMaxSize().padding(
                start = if (isBible) 54.dp else 30.dp,
                end = if (isBible) 54.dp else 30.dp,
                top = if (isBible) 185.dp else 190.dp,
                bottom = if (isBible) 245.dp else 230.dp,
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isBible) {
                Box(
                    Modifier.width(42.dp).height(3.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Pk.GoldDeep.copy(alpha = .62f)),
                )
                Spacer(Modifier.height(20.dp))
            }
            Text(
                movement.label,
                fontFamily = UtilitySans,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.2.sp,
                color = if (isBible) Pk.Oxblood else Pk.GoldLight,
            )
            Spacer(Modifier.height(20.dp))
            FittedWords(
                movement.text,
                color = if (isBible) Pk.Charcoal else Color.White,
                // what sits under the words is subtracted from their room
                reserve = if (movement.reference != null) 40.dp else 0.dp,
                modifier = Modifier.weight(1f, fill = false),
            )
            movement.reference?.let { ref ->
                Spacer(Modifier.height(22.dp))
                Text(
                    ref.uppercase(),
                    fontFamily = UtilitySans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.4.sp,
                    color = if (isBible) Pk.Oxblood.copy(alpha = .72f) else Color.White.copy(alpha = .66f),
                )
            }
            if (isBible) {
                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(Pk.GoldDeep.copy(alpha = .18f)),
                )
            }
        }
    }
}

/**
 * Words at the largest size that fits the space they are given.
 *
 * Tried from 29sp down to 14sp, measured, and the first that fits is used.
 */
@Composable
private fun FittedWords(text: String, color: Color, reserve: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    androidx.compose.foundation.layout.BoxWithConstraints(modifier) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val room = constraints.maxHeight - with(density) { reserve.roundToPx() }
        val width = constraints.maxWidth
        val size = remember(text, room, width) {
            (29 downTo 14).firstOrNull { sp ->
                val style = androidx.compose.ui.text.TextStyle(
                    fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold,
                    fontSize = sp.sp, lineHeight = (sp * 1.38f).sp,
                )
                measurer.measure(
                    text, style,
                    constraints = androidx.compose.ui.unit.Constraints(maxWidth = width.coerceAtLeast(1)),
                ).size.height <= room
            } ?: 14
        }
        /* No scroll here: the card is pulled down to advance, and a scroll
           container under the thumb takes that drag for itself. The size
           search above is what keeps the words inside the card. */
        Text(
            text,
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = size.sp,
            lineHeight = (size * 1.38f).sp,
            color = color,
        )
    }
}

@Composable
private fun SessionGround(isBible: Boolean) {
    Image(
        painterResource(if (isBible) Res.drawable.verse_card_botanical else Res.drawable.today_prayer_violet),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
    if (isBible) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Pk.DeepDark.copy(alpha = .68f),
                        Color.Transparent,
                        Color.Transparent,
                        Pk.DeepDark.copy(alpha = .52f),
                    ),
                ),
            ),
        )
    } else {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Pk.DeepDark.copy(alpha = .56f),
                        Pk.DeepDark.copy(alpha = .18f),
                        Pk.DeepDark.copy(alpha = .70f),
                    ),
                ),
            ),
        )
    }
}

/** Ten segments, a close, and the day's title. That is the entire interface. */
@Composable
private fun SessionChrome(
    title: String,
    at: Int,
    total: Int,
    pull: Float,
    isBible: Boolean,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(total) { i ->
                val fill = when {
                    i < at -> 1f
                    i == at -> pull.coerceAtLeast(.001f)
                    else -> 0f
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = .22f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(if (i == at) .35f + (.65f * fill) else fill)
                            .height(3.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White),
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isBible) Pk.VioletDeep.copy(alpha = .92f) else Color.White.copy(alpha = .12f))
                    .then(
                        if (isBible) Modifier.border(1.dp, Pk.GoldLight.copy(alpha = .54f), CircleShape)
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    ),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(17.dp)) }
            Spacer(Modifier.width(14.dp))
            Text(
                title.uppercase(),
                fontFamily = UtilitySans,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.6.sp,
                color = Color.White.copy(alpha = .62f),
                maxLines = 1,
            )
        }
    }
}

/**
 * The only instruction on the screen, and it retires itself.
 *
 * It shows on the first two movements and then goes, because by the third pull
 * the gesture has been learned and a permanent hint is just furniture.
 */
@Composable
private fun PullHint(at: Int, total: Int, verb: String, pull: Float, isBible: Boolean, modifier: Modifier) {
    Column(
        modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isBible) {
            Column(
                Modifier
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = .28f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Pk.VioletDeep.copy(alpha = .94f))
                    .border(1.dp, Pk.GoldLight.copy(alpha = .32f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 22.dp, vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (at <= 2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown, null,
                            tint = Pk.GoldLight.copy(alpha = 1f - pull),
                            modifier = Modifier.size(18.dp).graphicsLayer { translationY = pull * 10f },
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Pull down when you have $verb it",
                            fontFamily = UtilitySans,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = .88f - (pull * .5f)),
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                }
                Text(
                    "$at  /  $total",
                    fontFamily = UtilitySans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = Pk.GoldLight,
                )
            }
        } else {
            if (at <= 2) {
                Icon(
                    Icons.Outlined.KeyboardArrowDown, null,
                    tint = Color.White.copy(alpha = .5f - (pull * .5f)),
                    modifier = Modifier.size(22.dp).graphicsLayer { translationY = pull * 14f },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Pull down when you have $verb it",
                    fontFamily = UtilitySans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = .5f - (pull * .5f)),
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                "$at / $total",
                fontFamily = UtilitySans,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color = Color.White.copy(alpha = .56f),
            )
        }
    }
}

/** After the tenth pull. One statement, then out of the way. */
@Composable
private fun SessionComplete(
    title: String,
    headline: String,
    closing: String,
    isBible: Boolean,
    onClose: () -> Unit,
    again: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .then(
                    if (isBible) {
                        Modifier
                            .shadow(28.dp, RoundedCornerShape(36.dp), spotColor = Pk.Oxblood.copy(alpha = .44f))
                            .clip(RoundedCornerShape(36.dp))
                            .background(Pk.VioletDeep)
                            .border(1.dp, Pk.GoldLight.copy(alpha = .42f), RoundedCornerShape(36.dp))
                            .padding(horizontal = 30.dp, vertical = 48.dp)
                    } else Modifier.padding(horizontal = 6.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isBible) {
                Text(
                    "Chapter complete",
                    fontFamily = UtilitySans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp,
                    color = Pk.GoldLight,
                )
                Spacer(Modifier.height(18.dp))
            }
            Text(
                headline,
                fontFamily = DisplaySerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 56.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                closing,
                fontFamily = UtilitySans,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = .72f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                fontFamily = UtilitySans,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = Pk.GoldLight,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(44.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (isBible) Pk.Cream else Color.White)
                    .clickable(onClick = onClose)
                    .padding(horizontal = 44.dp, vertical = 15.dp),
            ) {
                Text(
                    "Done",
                    fontFamily = UtilitySans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Pk.Charcoal,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Do it again",
                fontFamily = UtilitySans,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = .55f),
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = again)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }
    }
}

/**
 * The ground the words sit on.
 *
 * Brand oxblood, unchanged — deepened at the corners rather than tinted, so it
 * reads as the same purple the rest of the app uses, lit from behind.
 */
private fun nightBrush(): Brush = Brush.linearGradient(
    0f to Pk.DeepDark,
    .42f to Pk.VioletDeep,
    .72f to Pk.Oxblood,
    1f to Pk.DeepDark,
    start = Offset(0f, 0f),
    end = Offset(900f, 2400f),
)
