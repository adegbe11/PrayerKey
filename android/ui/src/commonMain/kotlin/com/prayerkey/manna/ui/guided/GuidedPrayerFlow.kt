package com.prayerkey.manna.ui.guided

import com.prayerkey.manna.ui.platform.StatusBarIcons
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerMachine
import com.prayerkey.manna.data.PrayerMachine.Kind
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

val Night = Color(0xFF0B0620)
val Deep = Color(0xFF1C0B4A)
private val Violet = Color(0xFF6200ED)
val Halo = Color(0xFFB79CFF)
val Gold = Color(0xFFE9C46A)
val GoldPale = Color(0xFFFFF1C9)

/** The pattern a guided prayer teaches, in the order it is prayed. */
private val Pattern = listOf(Kind.Worship, Kind.Confess, Kind.Ask, Kind.Listen)

/**
 * A guided prayer: what someone brought, prayed through one step at a time.
 *
 * Everything moves at the pace of a breath - four seconds in, four out. The
 * light behind the words swells and settles with it, so the body slows before
 * the prayer begins. Each step rises in on its own; nothing moves except what
 * is new. At the Amen the light turns gold and the prayer is kept, and the
 * four movements of the prayer light in turn so the shape of it is learned.
 */
@Composable
fun GuidedPrayerFlow(
    guided: PrayerMachine.Guided,
    onKeep: () -> Unit,
    onClose: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val steps = guided.steps
    var index by rememberSaveable { mutableIntStateOf(0) }
    var kept by rememberSaveable { mutableStateOf(false) }
    val step = steps[index]
    val amen = step.kind == Kind.Amen

    LightIcons()
    BackHandler { if (index > 0 && !kept) index-- else onClose() }

    // the breath: four seconds in, four out, for the whole prayer
    var inhale by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { while (true) { delay(4000); inhale = !inhale } }
    val breath by animateFloatAsState(if (inhale) 1f else 0f, tween(4000, easing = FastOutSlowInEasing), label = "breath")
    val glow by animateColorAsState(if (kept) Gold else Halo, tween(1600), label = "glow")
    val seal by animateFloatAsState(if (kept) 1f else 0f, tween(1800, easing = FastOutSlowInEasing), label = "seal")

    fun next() {
        if (index < steps.lastIndex) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            index++
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Deep, Night)))) {
        Heaven(breath = breath, glow = glow, seal = seal)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // the way out, and where you are in the prayer
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White.copy(alpha = .78f)) }
                Spacer(Modifier.weight(1f))
                Progress(steps.size, index, glow)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(44.dp))
            }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 30.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        (fadeIn(tween(1100, delayMillis = 250)) + slideInVertically(tween(1100, delayMillis = 250)) { it / 14 })
                            .togetherWith(fadeOut(tween(450)))
                    },
                    label = "step",
                ) { i ->
                    StepFace(steps[i], breath = breath, inhale = inhale, kept = kept, glow = glow)
                }
            }

            // one way forward
            Box(Modifier.fillMaxWidth().padding(horizontal = 30.dp).padding(bottom = 18.dp), contentAlignment = Alignment.Center) {
                when {
                    amen && kept -> Pill("Done", filled = false, onClick = onClose)
                    amen -> Pill("Amen", filled = true, gold = true) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        kept = true
                        onKeep()
                    }
                    else -> Pill("Continue", filled = false, onClick = ::next)
                }
            }
        }
    }
}

/** The words of one step, its name above them. */
@Composable
private fun StepFace(step: PrayerMachine.Step, breath: Float, inhale: Boolean, kept: Boolean, glow: Color) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (step.kind != Kind.Amen) {
            Text(
                step.label.uppercase(),
                color = Gold.copy(alpha = .9f), fontFamily = UtilitySans,
                fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(22.dp))
        }
        when (step.kind) {
            Kind.Still -> {
                Text(
                    step.text,
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 30.sp, lineHeight = 38.sp, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(44.dp))
                BreathRing(breath, glow)
                Spacer(Modifier.height(18.dp))
                AnimatedContent(inhale, transitionSpec = { fadeIn(tween(900)) togetherWith fadeOut(tween(600)) }, label = "breathWord") { inn ->
                    Text(
                        if (inn) "Breathe in" else "Breathe out",
                        color = Color.White.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 15.sp,
                    )
                }
            }
            Kind.Amen -> AmenFace(kept)
            else -> {
                Text(
                    step.text,
                    color = Color.White, fontFamily = BookSerif,
                    fontSize = if (step.kind == Kind.Word) 26.sp else 24.sp,
                    lineHeight = if (step.kind == Kind.Word) 38.sp else 36.sp,
                    textAlign = TextAlign.Center,
                )
                if (step.reference.isNotBlank()) Text(
                    step.reference,
                    Modifier.padding(top = 18.dp),
                    color = Gold, fontFamily = UtilitySans, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                )
                if (step.kind == Kind.Listen) {
                    Spacer(Modifier.height(40.dp))
                    StillnessRing(glow)
                }
            }
        }
    }
}

/** The Amen, and once it is said, the shape of what was prayed. */
@Composable
private fun AmenFace(kept: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val lift by animateFloatAsState(if (kept) 1f else 0f, tween(1400, easing = FastOutSlowInEasing), label = "lift")
        Text(
            "Amen",
            Modifier.graphicsLayer { scaleX = 1f + .08f * lift; scaleY = 1f + .08f * lift },
            color = if (kept) Gold else Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
            fontSize = 58.sp,
        )
        if (kept) {
            Spacer(Modifier.height(26.dp))
            // the four movements of the prayer, lit one after another
            var lit by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) { repeat(Pattern.size) { delay(520); lit++ } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pattern.forEachIndexed { i, kind ->
                    val on by animateFloatAsState(if (i < lit) 1f else .18f, tween(700), label = "lit$i")
                    Text(
                        when (kind) { Kind.Worship -> "Worship"; Kind.Confess -> "Confess"; Kind.Ask -> "Ask"; else -> "Listen" },
                        color = GoldPale.copy(alpha = on), fontFamily = UtilitySans, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (i < Pattern.lastIndex) Box(Modifier.size(3.dp).clip(CircleShape).background(Gold.copy(alpha = on * .8f)))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Kept in your prayers",
                color = Color.White.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 13.sp,
            )
        }
    }
}

/** A ring that swells as you breathe in and settles as you breathe out. */
@Composable
private fun BreathRing(breath: Float, glow: Color) {
    Canvas(Modifier.size(150.dp)) {
        val r = size.minDimension / 2 * (.58f + .42f * breath)
        drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .30f), Color.Transparent), center, r * 1.5f), r * 1.5f)
        drawCircle(glow.copy(alpha = .55f), r, style = Stroke(1.6.dp.toPx()))
        drawCircle(glow.copy(alpha = .14f + .10f * breath), r * .82f)
    }
}

/** Fifteen seconds of quiet, filling slowly; the Continue button is always there. */
@Composable
private fun StillnessRing(glow: Color) {
    val fill = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) { fill.animateTo(1f, tween(15_000, easing = LinearEasing)) }
    val f = fill.value
    Canvas(Modifier.size(64.dp)) {
        drawCircle(Color.White.copy(alpha = .14f), style = Stroke(2.dp.toPx()))
        drawArc(glow, -90f, 360f * f, false, style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** One light per step, the current one brightest. */
@Composable
private fun Progress(count: Int, at: Int, glow: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val w by animateFloatAsState(if (i == at) 18f else 6f, tween(500), label = "dot$i")
            Box(
                Modifier.height(6.dp).width(w.dp).clip(RoundedCornerShape(99.dp))
                    .background(if (i <= at) glow.copy(alpha = if (i == at) 1f else .55f) else Color.White.copy(alpha = .18f)),
            )
        }
    }
}

@Composable
fun Pill(label: String, filled: Boolean, gold: Boolean = false, onClick: () -> Unit) {
    val shine = rememberInfiniteTransition(label = "shine")
    val sweep by shine.animateFloat(-0.3f, 1.3f, infiniteRepeatable(tween(2800, delayMillis = 1200)), label = "sweep")
    Box(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(99.dp))
            .background(
                when {
                    gold -> Brush.horizontalGradient(listOf(Color(0xFFC9A24A), Gold, Color(0xFFC9A24A)))
                    filled -> Brush.horizontalGradient(listOf(Violet, Violet))
                    else -> Brush.horizontalGradient(listOf(Color.White.copy(alpha = .14f), Color.White.copy(alpha = .10f)))
                },
            )
            .border(1.dp, Color.White.copy(alpha = if (gold) .0f else .24f), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (gold) Canvas(Modifier.fillMaxSize()) {
            val x = size.width * sweep
            drawRect(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = .45f), Color.Transparent), Offset(x - 90f, 0f), Offset(x + 90f, size.height)))
        }
        Text(
            label,
            color = if (gold) Color(0xFF2A1700) else Color.White,
            fontFamily = UtilitySans, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * The sky the prayer is prayed under: a light that breathes, a few slow rays,
 * and motes of light drifting up. At the Amen the light turns gold and opens.
 */
@Composable
fun Heaven(breath: Float, glow: Color, seal: Float) {
    val time = rememberInfiniteTransition(label = "heaven")
    val turn by time.animateFloat(0f, 360f, infiniteRepeatable(tween(90_000, easing = LinearEasing)), label = "turn")
    val drift by time.animateFloat(0f, 1f, infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "drift")
    val motes = remember {
        val r = Random(8)
        List(28) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) }
    }
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height * .38f)
        val radius = size.minDimension * (.62f + .10f * breath + .55f * seal)
        // the light
        drawCircle(
            Brush.radialGradient(
                listOf(glow.copy(alpha = .30f + .10f * breath + .15f * seal), glow.copy(alpha = .06f), Color.Transparent),
                center, radius,
            ),
            radius, center,
        )
        // the rays, turning so slowly you notice them only later
        rotate(turn, center) {
            repeat(9) { i ->
                rotate(i * 40f, center) {
                    drawRect(
                        Brush.verticalGradient(listOf(glow.copy(alpha = .07f + .05f * seal), Color.Transparent), center.y - size.height, center.y),
                        topLeft = Offset(center.x - 14.dp.toPx(), center.y - size.height),
                        size = androidx.compose.ui.geometry.Size(28.dp.toPx(), size.height),
                    )
                }
            }
        }
        // motes of light, rising
        motes.forEach { (x, y, s) ->
            val yy = ((y - drift * (.4f + .6f * s)) % 1f + 1f) % 1f
            val twinkle = .35f + .65f * ((sin((drift * 6 + s * 10) * PI) + 1) / 2).toFloat()
            drawCircle(
                (if (seal > .5f) GoldPale else Color.White).copy(alpha = .45f * twinkle),
                radius = (1.2f + 2.2f * s).dp.toPx(),
                center = Offset(x * size.width, yy * size.height),
            )
        }
    }
}

/** Light clock and battery icons over the night sky, restored on leaving. */
@Composable
fun LightIcons() {
    StatusBarIcons(dark = false)
}
