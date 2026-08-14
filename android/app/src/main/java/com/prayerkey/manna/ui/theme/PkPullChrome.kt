package com.prayerkey.manna.ui.theme

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/** How the Bible is being read. All three already existed; nothing chose between them. */
enum class BibleMode { Cards, Book, Library, Audio }

/**
 * Chrome that hides until it is asked for.
 *
 * The Bible screen carried ten floating controls over the artwork at all
 * times — five chips on top, five on the bottom — on a screen whose entire
 * job is to show one verse. Hiding them behind a gesture is not minimalism
 * for its own sake: it is the only way a full-bleed card stays full bleed.
 *
 * Pull the card down and it slides, shrinks slightly and rounds at the
 * corners, revealing the bar behind it. Let go past the threshold and it
 * stays open; let go short of it and it springs shut. The one permanent hint
 * is a grip at the top — 40dp of rounded bar and a chevron, which is the
 * smallest affordance that still reads as "there is something above this".
 *
 * The gesture is vertical on purpose. A left-edge pull would collide with
 * Android's back gesture, which during testing threw the reader clean out of
 * the book mid-page-turn.
 */
@Composable
fun PullToReveal(
    barHeight: Float,
    bar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val pull = remember { Animatable(0f) }
    var open by remember { mutableStateOf(false) }
    val threshold = barHeight * .38f

    Box(Modifier.fillMaxSize().background(Pk.Charcoal)) {

        // the bar sits behind the stack and is only seen once it moves
        Box(Modifier.fillMaxWidth()) { bar() }

        Box(
            Modifier.fillMaxSize()
                .graphicsLayer {
                    val t = (pull.value / barHeight).coerceIn(0f, 1f)
                    translationY = pull.value
                    // a slight shrink sells the card as a physical thing being
                    // slid out of the way rather than a panel being pushed
                    scaleX = 1f - t * .05f
                    scaleY = 1f - t * .05f
                    shape = RoundedCornerShape((t * 26).dp)
                    clip = true
                }
                ,
        ) {
            content()

            /* The pull lives in a strip at the top, not across the whole card.
               The verse deck already owns vertical drags — pull for the next
               verse, push up to keep it — so a full-surface gesture here would
               have eaten an interaction that already works. Restricting it to
               the grip's own strip lets both exist, and the grip is where a
               user reaches for it anyway. */
            Box(
                Modifier.fillMaxWidth().height(110.dp).align(Alignment.TopCenter)
                    .pointerInput(barHeight) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            val settled = if (pull.value > threshold) barHeight else 0f
                            if ((settled > 0f) != open) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            }
                            open = settled > 0f
                            scope.launch { pull.animateTo(settled, tween(260)) }
                        },
                        onDragCancel = {
                            scope.launch { pull.animateTo(if (open) barHeight else 0f, tween(200)) }
                        },
                    ) { change, delta ->
                        /* Consume it. Without this the deck underneath saw the
                           same drag and armed its own pull, so one gesture both
                           opened the bar and stamped RECEIVE. */
                        change.consume()
                        val base = pull.value
                        // past the bar it gets stiff, so the pull has an end
                        val next = (base + delta).let {
                            if (it > barHeight) barHeight + (it - barHeight) * .35f else it
                        }
                        scope.launch { pull.snapTo(next.coerceAtLeast(0f)) }
                    }
                    },
            )

            /* The only permanent chrome on the screen. Tappable as well as
               draggable: a hidden gesture that has exactly one visible handle
               should also answer a tap. */
            Column(
                Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 10.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        open = !open
                        scope.launch { pull.animateTo(if (open) barHeight else 0f, tween(260)) }
                    }
                    .padding(horizontal = 28.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.width(40.dp).height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Pk.Cream.copy(alpha = .42f)),
                )
                Icon(
                    Icons.Outlined.KeyboardArrowDown, null,
                    tint = Pk.Cream.copy(alpha = .42f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * The bar behind the card: where you are, which translation, and how you want
 * to read it.
 *
 * Three modes that all already existed in the app with no way to choose
 * between them — the deck, the bound book, and the plain chapter.
 */
@Composable
fun BibleModeBar(
    reference: String,
    chapter: String,
    version: String,
    mode: BibleMode,
    onReference: () -> Unit,
    onVersion: () -> Unit,
    onMode: (BibleMode) -> Unit,
    onMemorize: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth()
            .background(Pk.Charcoal)
            .statusBarsPadding()
            .padding(start = Pk.Gutter, end = Pk.Gutter, top = 12.dp, bottom = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.clickable(onClick = onReference).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(reference, color = Pk.Cream, fontFamily = Spectral, fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                // the chapter in rubric red, the way a printed Bible marks it
                Text(chapter, color = Pk.Gold, fontFamily = Spectral, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Icon(
                    Icons.Outlined.ExpandMore, "Choose a book and chapter",
                    tint = Pk.Cream.copy(alpha = .6f),
                    modifier = Modifier.padding(start = 4.dp).size(18.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    version.uppercase(),
                    color = Pk.Cream.copy(alpha = .8f),
                    style = PkText.SectionLabel,
                    modifier = Modifier.clickable(onClick = onVersion).padding(8.dp),
                )
                Spacer(Modifier.width(2.dp))
                // Memorize and Share had no gesture on the card, so they live
                // here rather than as permanent floating chips
                androidx.compose.material3.IconButton(onClick = onMemorize) {
                    Icon(
                        Icons.Outlined.School, "Memorize this verse",
                        tint = Pk.Cream.copy(alpha = .7f), modifier = Modifier.size(19.dp),
                    )
                }
                androidx.compose.material3.IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share, "Share this verse",
                        tint = Pk.Cream.copy(alpha = .7f), modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(Pk.S3))

        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Pk.Cream.copy(alpha = .16f), RoundedCornerShape(10.dp)),
        ) {
            BibleMode.entries.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(Modifier.width(1.dp).height(38.dp).background(Pk.Cream.copy(alpha = .16f)))
                }
                val on = item == mode
                Box(
                    Modifier.weight(1f)
                        .background(if (on) Pk.Oxblood else Color.Transparent)
                        .clickable { onMode(item) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item.name.uppercase(),
                        color = if (on) Pk.Cream else Pk.Cream.copy(alpha = .55f),
                        style = PkText.Meta.copy(fontSize = 10.5.sp, letterSpacing = 1.6.sp),
                    )
                }
            }
        }
    }
}

/**
 * The one-time coaching line, low and quiet, gone after five seconds.
 *
 * A hidden gesture needs telling once. Telling twice is nagging, so it never
 * comes back.
 */
@Composable
fun PullCoach(text: String, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        visible = false
    }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(visible) { if (!visible) alpha.animateTo(0f, tween(600)) }

    Text(
        text.uppercase(),
        color = Pk.Cream.copy(alpha = .5f),
        style = PkText.Meta.copy(fontSize = 10.sp, letterSpacing = 2.sp),
        modifier = modifier.graphicsLayer { this.alpha = alpha.value },
    )
}
