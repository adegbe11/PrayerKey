package com.prayerkey.manna.ui.book

import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleCanon
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.ui.theme.BookSerif
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/* A bound Bible, drawn. Not the app's theme — a book is leather and paper
   whichever sky you chose for the rest of Manna. */
/**
 * The colours of one edition of the book.
 *
 * Classic is leather and cream paper with gilt and a red rubric. Monochrome
 * is the black edition: black board, near-black leaves, the text in soft white
 * and the verse numbers in silver - no colour anywhere, the way a black
 * leather Bible with dark pages is bound.
 */
class BookPalette(
    val desk: Color, val deskLit: Color,
    val spine: List<Color>,
    val pageLit: Color, val page: Color, val pageEdge: Color,
    val foreEdge: List<Color>, val foreSheen: Color, val foreShade: Color,
    val gild: Color, val ink: Color, val rubric: Color, val ribbon: Color,
    /** hairlines and borders in the page chrome */
    val rule: Color,
    /** run-head, folio and chrome labels */
    val caption: Color,
    val icon: Color,
    val popup: Color,
    val gutter: Color,
    val chromeFill: Color,
)

enum class BookEdition { Classic, Monochrome }

private val ClassicPalette = BookPalette(
    desk = Color(0xFF17130D), deskLit = Color(0xFF2A2016),
    spine = listOf(Color(0xFF1B0E07), Color(0xFF442718), Color(0xFF2B1710)),
    pageLit = Color(0xFFFDFAF0), page = Color(0xFFF6EFDC), pageEdge = Color(0xFFE6D9B6),
    foreEdge = listOf(Color(0xFFEFE5C8), Color(0xFFDED0A8), Color(0xFFF4ECD6), Color(0xFFDED0A8)),
    foreSheen = Color(0xFFFFF0C8), foreShade = Color(0xFFB49B5F),
    gild = Color(0xFFC9A26D), ink = Color(0xFF221C12), rubric = Color(0xFF9A1B2B), ribbon = Color(0xFF8C1F2B),
    rule = Color(0xFF8C6E32), caption = Color(0xFF5A4828), icon = Color(0xFF3A2A12),
    popup = Color(0xFF1E1207), gutter = Color(0xFF6B5A34), chromeFill = Color(0xFFF6EFDC),
)

private val MonochromePalette = BookPalette(
    desk = Color(0xFF050505), deskLit = Color(0xFF141414),
    spine = listOf(Color(0xFF000000), Color(0xFF1C1C1E), Color(0xFF0A0A0A)),
    pageLit = Color(0xFF232325), page = Color(0xFF1B1B1D), pageEdge = Color(0xFF111113),
    foreEdge = listOf(Color(0xFF3A3A3C), Color(0xFF232325), Color(0xFF48484A), Color(0xFF232325)),
    foreSheen = Color(0xFFE5E5EA), foreShade = Color(0xFF000000),
    gild = Color(0xFFBDBDC2), ink = Color(0xFFEDEDED), rubric = Color(0xFFB9B9BE), ribbon = Color(0xFFD1D1D6),
    rule = Color(0xFF8E8E93), caption = Color(0xFFAEAEB2), icon = Color(0xFFEDEDED),
    popup = Color(0xFF000000), gutter = Color(0xFF000000), chromeFill = Color(0xFF2C2C2E),
)

/* The edition being drawn. One book is on screen at a time, and the screen
   sets this as it composes, so every leaf, rule and label below follows it. */
private var P: BookPalette = ClassicPalette

private val Desk get() = P.desk
private val Page get() = P.page
private val PageEdge get() = P.pageEdge
/* Gilt, and it has to actually be gold. This was #5D91F2 — a blue — left
   behind by the app-wide violet pass, which painted the sheen on the fore
   edge of a leather Bible cornflower blue on every page. The book is the one
   surface in the app that does not follow the theme: gilding is a metal. */
private val Gild get() = P.gild
private val BookInk get() = P.ink
// the red of the bookmark; the old near-black maroon disappeared into the ink
private val Rubric get() = P.rubric
private val Ribbon get() = P.ribbon

/**
 * The Bible as a book.
 *
 * The verse deck is for being handed something; this is for sitting down and
 * reading. Two different acts, so two different surfaces.
 *
 * What makes it a book rather than a page with a leather picture:
 *
 *  - **The edge.** Drag the gilded block down the right side and you riffle
 *    through all 1,189 chapters, a haptic tick per chapter, with the book and
 *    chapter you are passing shown on the leaf. This is how you find
 *    something in a physical Bible, and no Bible app does it — they all give
 *    you a modal list of books.
 *  - **The ribbon.** Drop it where you are; tap it again from anywhere to
 *    come back. It survives the app closing.
 *  - **Real pages.** The chapter is laid out once and shown a window of lines
 *    at a time, so verses flow inside paragraphs the way a printed Bible sets
 *    them, and a page turn is a page turn rather than a scroll.
 *
 * One column, not the two of a printed Bible: this book's text area is about
 * 68mm wide, so two columns would leave 30mm each — five words a line, and
 * justified text at that measure is all rivers and broken words. A printed
 * Bible can do it because its page is twice as wide. The run-head, the folio
 * and the gilt edge carry the book feeling; the column count was never doing
 * that work.
 *
 * No red-letter. Marking the words of Christ needs a tagged text and the KJV
 * we ship is plain; inferring it from quotation marks would put words in his
 * mouth that are not his, which is worse than not colouring them.
 */
@Composable
fun BibleBookScreen(
    bible: OfflineBible,
    textSize: Int,
    onTextSize: (Int) -> Unit,
    ribbon: String?,
    onRibbon: (String?) -> Unit,
    onReadPlain: ((String, Int) -> Unit)?,
    onClose: () -> Unit = {},
    edition: BookEdition = BookEdition.Classic,
    /** where to keep the chapter you were in, so the book opens there again */
    placeKey: String? = null,
) {
    P = if (edition == BookEdition.Monochrome) MonochromePalette else ClassicPalette
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer()

    val chapters = remember { BibleCanon.chapters }
    val placeStore = remember(placeKey) {
        placeKey?.let { view.context.getSharedPreferences("book_place", android.content.Context.MODE_PRIVATE) }
    }
    var chapterIndex by remember {
        mutableIntStateOf(placeStore?.getInt(placeKey, 0)?.coerceIn(0, BibleCanon.chapters.lastIndex) ?: 0)
    }
    var pageIndex by remember { mutableIntStateOf(0) }
    // set when arriving backwards, so we land on the last leaf of the chapter
    var landOnLast by remember { mutableStateOf(false) }
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var sizeOpen by remember { mutableStateOf(false) }
    var bookHeight by remember { mutableFloatStateOf(0f) }

    /* You meet a Bible by its cover, not by page one. The board swings open
       on the spine when tapped; at 1f it is out of the way and stops being
       drawn at all. */
    val cover = remember { Animatable(0f) }
    var closed by remember { mutableStateOf(true) }

    val here = chapters[chapterIndex]

    /* Which chapter [verses] actually holds. Loading is a suspend hop, so for
       a frame or two after [chapterIndex] moves the layout below is still the
       old chapter's. Anything that reasons about page counts has to know that. */
    var loadedChapter by remember { mutableIntStateOf(-1) }

    /* Where you are, in characters rather than pages.
       Changing the type size relaid the chapter and sent you back to page one,
       which is the one thing a reader will not forgive: you changed the size
       *because* you were reading, and it threw away the place you were reading
       from. A page number means nothing across a relayout; a character offset
       survives it. [restoreTo] is set when the size changes and consumed once
       the new layout exists. */
    var readingAt by remember { mutableIntStateOf(0) }
    var restoreTo by remember { mutableIntStateOf(-1) }

    LaunchedEffect(chapterIndex) {
        placeStore?.edit()?.putInt(placeKey, chapterIndex)?.apply()
        verses = bible.chapter(here.book.name, here.chapter)
        loadedChapter = chapterIndex
    }

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(P.deskLit, Desk), radius = 1400f))) {

        // ── the book ──────────────────────────────────────────────────────
        Box(
            /* Full bleed. Inset by 12dp with rounded corners, the app's own
               background showed down both sides and along the bottom, and the
               book read as a picture of a book floating on a screen. */
            Modifier.fillMaxSize()
                .onSizeChanged { bookHeight = it.height.toFloat() },
        ) {
            /* The same tooled board the cover is made of, so what shows above
               and below the page block is the open book's own leather rather
               than a flat brown band. */
            TooledBoard(centrepiece = false)

            // spine
            Box(
                Modifier.fillMaxHeight().width(13.dp)
                    .background(Brush.horizontalGradient(P.spine)),
            )

            BoxWithConstraints(
                // the leaf owns the screen; only the gilt fore edge is kept
                Modifier.fillMaxSize().padding(end = 22.dp),
            ) {
                val pageW = with(density) { maxWidth.toPx() } - with(density) { 48.dp.toPx() }
                val pageH = with(density) { maxHeight.toPx() }

                val body = remember(textSize) {
                    TextStyle(
                        fontFamily = BookSerif,
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.52f).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BookInk,
                        textAlign = TextAlign.Justify,
                        /* Justified type on a phone-width measure opens rivers
                           between words unless long words can break, so the
                           setting hyphenates and balances the paragraph the
                           way a printed page does. */
                        hyphens = androidx.compose.ui.text.style.Hyphens.Auto,
                        lineBreak = androidx.compose.ui.text.style.LineBreak.Paragraph,
                    )
                }

                /* One layout for the whole chapter, then windows onto it.
                   Laying out per verse would start every verse on a new line;
                   a Bible flows them. */
                val flow = remember(verses, textSize) { chapterText(verses, textSize) }
                /* The initial rises from the first line's baseline, about two
                   and a half times the reading size, the way a printed Bible
                   opens a chapter. Its place in the line is only as tall as the
                   line itself: anything taller in the first line makes Android
                   draw the whole page a line lower than it measured it, which
                   cut the last line off and repeated it on the next page. The
                   letter rises into the empty left of the chapter heading. */
                val initial: TextLayoutResult? = remember(flow, textSize) {
                    if (flow.initialAt < 0) null
                    else measurer.measure(
                        flow.initial,
                        TextStyle(
                            fontFamily = BookSerif, fontSize = (textSize * 2.5f).sp,
                            fontWeight = FontWeight.Bold, color = BookInk,
                        ),
                    )
                }
                val layout: TextLayoutResult? = remember(flow, pageW, textSize, initial) {
                    if (verses.isEmpty() || pageW <= 0f) null
                    else measurer.measure(
                        flow.text, body,
                        placeholders = initial?.let { cap ->
                            listOf(
                                AnnotatedString.Range(
                                    androidx.compose.ui.text.Placeholder(
                                        width = with(density) { (cap.size.width + 1.dp.toPx()).toSp() },
                                        height = (textSize * .7f).sp,
                                        placeholderVerticalAlign =
                                            androidx.compose.ui.text.PlaceholderVerticalAlign.AboveBaseline,
                                    ),
                                    flow.initialAt, flow.initialAt + 1,
                                ),
                            )
                        } ?: emptyList(),
                        constraints = Constraints(maxWidth = pageW.roundToInt().coerceAtLeast(1)),
                    )
                }

                /* Measured, not estimated. Guessing the run-head and folio
                   heights left the window a fraction taller than the box, so
                   the last line of every page was sliced through the middle.
                   The body box reports its own height instead. */
                var bodyPx by remember { mutableFloatStateOf(0f) }

                /* Pages by real height: the drop-cap line is far taller than
                   the rest, so a single line height (the tallest) made every
                   page think it held half what it drew - see
                   [pageWindowsByHeight]. */
                val windows = remember(layout, bodyPx) {
                    val l = layout
                    if (l == null || bodyPx <= 0f) pageWindows(l?.lineCount ?: 0, 1)
                    else pageWindowsByHeight(
                        List(l.lineCount) { l.getLineTop(it) },
                        List(l.lineCount) { l.getLineBottom(it) },
                        bodyPx,
                    )
                }

                /* Wait for the chapter you are actually in.
                   Turning back past the start of a chapter asked to land on the
                   last leaf of the previous one — but this fired straight away,
                   while [windows] still described the chapter being left. It
                   spent the flag on the old page count and cleared it, so by the
                   time the real chapter arrived nothing was asking for its last
                   leaf any more. Going back into a longer chapter dropped you
                   somewhere in its middle. */
                LaunchedEffect(windows, landOnLast, loadedChapter, restoreTo, layout) {
                    if (loadedChapter != chapterIndex || windows.isEmpty()) return@LaunchedEffect
                    val l = layout
                    if (restoreTo >= 0 && l != null) {
                        // the line that offset now falls on, and the leaf holding it
                        val line = l.getLineForOffset(restoreTo.coerceIn(0, l.layoutInput.text.length))
                        pageIndex = windows.indexOfFirst { line < it.firstLine + it.lines }
                            .coerceIn(0, windows.lastIndex)
                        restoreTo = -1
                    } else if (landOnLast) {
                        pageIndex = windows.lastIndex; landOnLast = false
                    } else if (pageIndex > windows.lastIndex) {
                        pageIndex = windows.lastIndex
                    }
                }

                val window = windows.getOrElse(pageIndex) { windows.first() }

                // the character the current leaf opens on, kept for the above
                LaunchedEffect(window, layout) {
                    layout?.let { readingAt = it.getLineStart(window.firstLine) }
                }
                val shown = versesInWindow(flow.verseLines(layout), window)

                val canGoForward = pageIndex < windows.lastIndex || chapterIndex < chapters.lastIndex
                val canGoBack = pageIndex > 0 || chapterIndex > 0


                /* ── the turn ────────────────────────────────────────────
                   A real page turn is a reflection, not a rotation: the part
                   of the sheet past the crease is that same sheet mirrored
                   about the crease. See FoldedPage. The crease position is
                   the only thing that animates, and which face is folding
                   depends on which way you are going:

                     forward — the current page folds, the next is revealed,
                               the crease travels from the fore edge to the
                               spine
                     back    — the previous page unfolds back over the
                               current one, crease spine to fore edge

                   Held in an Animatable and read inside the draw phase, so
                   dragging a page never recomposes the text. */
                // 0 flat, 1 fully over
                val crease = remember { Animatable(0f) }
                var direction by remember { mutableIntStateOf(0) }
                var travelled by remember { mutableFloatStateOf(0f) }
                var startedAt by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

                fun settle(commit: Boolean, forward: Boolean) {
                    scope.launch {
                        val to = if (commit) 1f else 0f
                        crease.animateTo(to, tween(330))
                        if (commit) {
                            sizeOpen = false
                            if (forward) {
                                if (pageIndex < windows.lastIndex) pageIndex++
                                else if (chapterIndex < chapters.lastIndex) { chapterIndex++; pageIndex = 0 }
                            } else {
                                if (pageIndex > 0) pageIndex--
                                else if (chapterIndex > 0) { chapterIndex--; landOnLast = true }
                            }
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        }
                        direction = 0
                        crease.snapTo(0f)
                    }
                }

                fun animateTurn(forward: Boolean) {
                    if (forward && !canGoForward) return
                    if (!forward && !canGoBack) return
                    direction = if (forward) 1 else -1
                    scope.launch { crease.snapTo(0f) }
                    settle(commit = true, forward = forward)
                }

                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(chapterIndex, pageIndex, windows, pageW) {
                            detectDragGestures(
                                onDragStart = {
                                    travelled = 0f
                                    startedAt = System.currentTimeMillis()
                                },
                                onDragEnd = {
                                    val went = travelled
                                    val quick = System.currentTimeMillis() - startedAt < 320
                                    val far = pageW * .28f
                                    val flick = 20f
                                    val forward = direction >= 0
                                    val enough = if (forward) {
                                        went < -far || (quick && went < -flick)
                                    } else {
                                        went > far || (quick && went > flick)
                                    }
                                    settle(commit = enough && direction != 0, forward = forward)
                                },
                                onDragCancel = { settle(commit = false, forward = direction >= 0) },
                            ) { change, amount ->
                                change.consume()
                                travelled += amount.x
                                if (direction == 0) {
                                    // the first real movement decides the way
                                    direction = when {
                                        travelled < -6f && canGoForward -> 1
                                        travelled > 6f && canGoBack -> -1
                                        else -> 0
                                    }
                                    if (direction != 0) scope.launch { crease.snapTo(0f) }
                                }
                                if (direction != 0) {
                                    // the crease tracks the thumb one to one
                                    val span = size.width.toFloat().coerceAtLeast(1f)
                                    val gone = (if (direction > 0) -travelled else travelled) / span
                                    scope.launch { crease.snapTo(gone.coerceIn(0f, 1f)) }
                                }
                            }
                        }
                        /* Tap zones as well as the drag. A horizontal drag
                           that starts near either screen edge competes with
                           Android's back gesture and loses — testing threw
                           the reader clean out of the book mid-turn. Every
                           real e-reader gives you tap-to-turn for the same
                           reason, and it is the quicker gesture anyway. */
                        .pointerInput(chapterIndex, pageIndex, windows, pageW) {
                            detectTapGestures { at ->
                                when {
                                    at.x > size.width * .62f -> animateTurn(forward = true)
                                    at.x < size.width * .38f -> animateTurn(forward = false)
                                }
                            }
                        },
                ) {
                    val leaf: @Composable (PageWindow?) -> Unit = { win ->
                        Leaf(
                            book = here.book.name,
                            chapter = here.chapter,
                            folio = chapterIndex + 1,
                            window = win,
                            verses = win?.let { versesInWindow(flow.verseLines(layout), it) },
                            layout = layout,
                            initial = initial,
                            textSize = textSize,
                            onBodyHeight = { if (win === window) bodyPx = it },
                        )
                    }

                    /* Which sheet is in the air. Going back it is the page
                       before this one that comes over the top, so the faces
                       swap; forward, the page under is the one arriving. */
                    val ahead = windows.getOrNull(pageIndex + 1)
                    val behind = windows.getOrNull(pageIndex - 1)

                    FoldedPage(
                        progress = { if (direction == 0) 0f else crease.value },
                        under = { leaf(if (direction < 0) window else ahead) },
                        folding = { leaf(if (direction < 0) behind else window) },
                    )
                }
            }

            /* ── the gilded edge: thumb it to riffle ─────────────────────── */
            var scrubbing by remember { mutableStateOf(false) }
            var scrubTarget by remember { mutableIntStateOf(0) }
            var thumbFraction by remember { mutableFloatStateOf(0f) }
            var lastTick by remember { mutableIntStateOf(-1) }
            // the thumb marker needs the edge's height outside the gesture
            // scope, where `size` is not in scope
            var edgeHeight by remember { mutableFloatStateOf(0f) }

            Box(
                Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(22.dp)
                    .onSizeChanged { edgeHeight = it.height.toFloat() }
                    .clip(RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp))
                    .background(
                        Brush.horizontalGradient(
                            0f to P.foreEdge[0], .34f to P.foreEdge[1],
                            .67f to P.foreEdge[2], 1f to P.foreEdge[3],
                        ),
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                scrubbing = true
                                thumbFraction = (offset.y / size.height).coerceIn(0f, 1f)
                                scrubTarget = (thumbFraction * (chapters.size - 1)).roundToInt()
                            },
                            onDragEnd = {
                                scrubbing = false
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                chapterIndex = scrubTarget
                                pageIndex = 0
                            },
                            onDragCancel = { scrubbing = false },
                        ) { change, amount ->
                            change.consume()
                            thumbFraction = (thumbFraction + amount / size.height).coerceIn(0f, 1f)
                            val next = (thumbFraction * (chapters.size - 1)).roundToInt()
                            if (next != scrubTarget) {
                                scrubTarget = next
                                // a tick per chapter, the way pages click past a thumb
                                if (next != lastTick) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    lastTick = next
                                }
                            }
                        }
                    },
            ) {
                // gilt sheen
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Gild.copy(alpha = .45f), P.foreSheen.copy(alpha = .18f),
                                Gild.copy(alpha = .35f), P.foreSheen.copy(alpha = .2f),
                            ),
                        ),
                    ),
                )
                if (scrubbing) {
                    Box(
                        Modifier.fillMaxWidth().height(44.dp)
                            .offset { IntOffset(0, ((edgeHeight - 44.dp.toPx()) * thumbFraction).roundToInt()) }
                            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .55f), Color.White.copy(alpha = .1f)))),
                    )
                }
            }

            if (!scrubbing) {
                Text(
                    "Thumb the edge",
                    color = Page.copy(alpha = .32f), fontSize = 8.5.sp, letterSpacing = 2.4.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .padding(end = 26.dp)
                        .graphicsLayer { rotationZ = -90f },
                )
            }

            // the leaf you are riffling to
            if (scrubbing) {
                val target = chapters[scrubTarget]
                Box(
                    Modifier.fillMaxSize().padding(end = 22.dp)
                        .background(Brush.horizontalGradient(listOf(Page.copy(alpha = .94f), PageEdge.copy(alpha = .97f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0f to P.foreShade.copy(alpha = .14f),
                                .3f to Color.Transparent,
                                .6f to P.foreShade.copy(alpha = .12f),
                                1f to Color.Transparent,
                            ),
                        ),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (target.book.oldTestament) "OLD TESTAMENT" else "NEW TESTAMENT",
                            color = P.rule.copy(alpha = .85f),
                            fontSize = 8.sp, letterSpacing = 3.sp, fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            target.book.name, color = BookInk, fontFamily = BookSerif,
                            fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "CHAPTER ${target.chapter}",
                            color = P.caption.copy(alpha = .85f),
                            fontSize = 10.sp, letterSpacing = 3.sp,
                        )
                    }
                }
            }

        }

        if (closed) {
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer {
                        // hinged on the spine, and lit less as it opens
                        rotationY = -168f * cover.value
                        cameraDistance = 22f * this.density
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, .5f)
                        alpha = if (cover.value > .92f) 0f else 1f
                    }
                    .clip(RoundedCornerShape(0.dp)),
            ) {
                val open = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    scope.launch {
                        cover.animateTo(1f, tween(760))
                        closed = false
                    }
                    Unit
                }
                if (edition == BookEdition.Monochrome) MonochromeCover(onOpen = open)
                else BibleCover(
                    onOpen = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        scope.launch {
                            cover.animateTo(1f, tween(760))
                            closed = false
                        }
                    },
                )
                Box(
                    Modifier.fillMaxSize()
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            scope.launch {
                                cover.animateTo(1f, tween(760))
                                closed = false
                            }
                        },
                )
            }
        }

        /* Drawn after the page, which would otherwise cover it. On paper now
           rather than on leather, so it takes ink and needs no scrim — the
           dark band existed only to lift gold chrome off gold filigree. */
        Row(
            // statusBarsPadding, or the row sits under the clock and battery —
            // it was drawn at a fixed 14dp from the top of the window
            Modifier.fillMaxWidth().statusBarsPadding()
                .padding(start = 66.dp, end = 22.dp).padding(top = 10.dp)
                .graphicsLayer { alpha = cover.value },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "PrayerKey", color = BookInk.copy(alpha = .55f), fontFamily = BookSerif,
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                modifier = Modifier.weight(1f),
            )
            /* The bookmark used to be a silk ribbon hanging down the gutter.
               Whatever margin it was given it lay across the first character
               of the opening lines, so it is a mark in the chrome now: filled
               where you have left it, hollow where you have not, and it still
               takes you back from anywhere. */
            val ribbonHere = ribbon == here.label
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(99.dp))
                    .background(if (ribbonHere) Ribbon.copy(alpha = .14f) else BookInk.copy(alpha = .05f))
                    .border(
                        1.dp,
                        if (ribbonHere) Ribbon.copy(alpha = .5f) else P.rule.copy(alpha = .4f),
                        RoundedCornerShape(99.dp),
                    )
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (ribbon == null || ribbonHere) {
                            onRibbon(if (ribbonHere) null else here.label)
                        } else {
                            val at = ribbon.substringBeforeLast(' ')
                            val ch = ribbon.substringAfterLast(' ').toIntOrNull() ?: 1
                            val index = BibleCanon.indexOf(at, ch)
                            if (index >= 0) { chapterIndex = index; pageIndex = 0 }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (ribbon != null) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    when {
                        ribbonHere -> "Remove the bookmark"
                        ribbon != null -> "Go to $ribbon"
                        else -> "Leave a bookmark here"
                    },
                    tint = if (ribbon != null) Ribbon else P.caption,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            EdgeButton("Aa") { sizeOpen = !sizeOpen }
            Spacer(Modifier.width(8.dp))
            onReadPlain?.let { plain -> EdgeButton("Read plain") { plain(here.book.name, here.chapter) } }
        }

        /* The way out of the book, on the cover and on every page. The row
           beside it fades in only once the book is open, so this is drawn on
           its own. */
        Box(
            Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 18.dp, top = 8.dp)
                .size(38.dp).clip(RoundedCornerShape(99.dp))
                .background(Page.copy(alpha = .82f))
                .border(1.dp, P.rule.copy(alpha = .4f), RoundedCornerShape(99.dp))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack, "Back",
                tint = P.icon, modifier = Modifier.size(19.dp),
            )
        }

        // drawn last, or the book paints over it
        if (sizeOpen) {
            Row(
                Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 52.dp, end = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(P.popup)
                    .border(1.dp, Gild.copy(alpha = .35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(14, 17, 20, 24).forEach { size ->
                    Box(
                        Modifier.clip(RoundedCornerShape(10.dp))
                            .background(if (size == textSize) Gild.copy(alpha = .22f) else Color.Transparent)
                            .clickable { restoreTo = readingAt; onTextSize(size); sizeOpen = false }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "A", color = if (size == textSize) Gild else Page.copy(alpha = .6f),
                            fontFamily = BookSerif, fontSize = (size - 3).sp,
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun EdgeButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(99.dp))
            .background(BookInk.copy(alpha = .05f))
            .border(1.dp, P.rule.copy(alpha = .4f), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text(
            label, color = P.caption, fontFamily = BookSerif,
            fontSize = 10.sp, letterSpacing = 1.4.sp,
        )
    }
}

/**
 * The chapter as one flowing string, plus where each verse starts.
 *
 * Verse numbers are superscript rubric, the way a printed Bible sets them,
 * and the offsets let the run-head say which verses are on the leaf.
 */
private class ChapterText(
    val text: AnnotatedString,
    private val starts: List<Pair<Int, Int>>,
    /** where the chapter's first letter sits in [text], or -1 */
    val initialAt: Int = -1,
) {
    /** the chapter's first letter, drawn large at [initialAt] */
    val initial: String get() = if (initialAt >= 0) text.text.substring(initialAt, initialAt + 1) else ""

    /** verse number to the line it begins on, for the current layout */
    fun verseLines(layout: TextLayoutResult?): List<Pair<Int, Int>> {
        if (layout == null) return emptyList()
        return starts.map { (verse, offset) ->
            verse to layout.getLineForOffset(offset.coerceIn(0, layout.layoutInput.text.length))
        }
    }
}

private fun chapterText(verses: List<BibleVerse>, textSize: Int): ChapterText {
    val starts = mutableListOf<Pair<Int, Int>>()
    var initialAt = -1
    val text = buildAnnotatedString {
        verses.forEachIndexed { index, verse ->
            starts += verse.verse to length
            withStyle(
                SpanStyle(
                    // in step with the reading size, not a fixed 10sp speck
                    // raised a third, not a half: a full superscript rose above
                    // its own line and showed beneath the last line of a page
                    color = Rubric, fontSize = (textSize * .66f).coerceAtLeast(10f).sp,
                    baselineShift = BaselineShift(.33f), fontWeight = FontWeight.Bold,
                ),
            ) { append("${verse.verse}") }
            // a no-break space ties the number to its verse, so a number is
            // never left alone at the end of a line
            append("\u00A0")
            /* Words the translators supplied are set in italic, which is what
               printed KJVs have done since 1611. They arrive as character
               ranges from the parser rather than braces in the text. */
            val body = verse.text
            var cursor = 0
            /* The great initial. The letter stays in the text, but the
               layout gives its place to a placeholder the size of the initial
               and the leaf paints the letter into it. Set as a 40sp glyph in
               the flowing text, Android drew the page lower than it measured
               it: the last line of every opening leaf was cut in half and the
               run-head named a verse you could not see. */
            if (index == 0 && body.isNotEmpty()) {
                initialAt = length
                append(body.substring(0, 1))
                cursor = 1
            }
            verse.supplied.sortedBy { it.first }.forEach { range ->
                val from = range.first.coerceIn(cursor, body.length)
                val to = (range.last + 1).coerceIn(from, body.length)
                if (from > cursor) append(body.substring(cursor, from))
                if (to > cursor) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(body.substring(from, to))
                    }
                }
                cursor = to
            }
            if (cursor < body.length) append(body.substring(cursor))
            if (index != verses.lastIndex) append(" ")
        }
    }
    return ChapterText(text, starts, initialAt)
}

/**
 * One leaf of the book: run-head, chapter heading, a window of the chapter's
 * layout, and the folio.
 *
 * Pulled out of the screen because a page turn needs to draw two of these —
 * the sheet in the air and the one being revealed — and they have to be the
 * same thing or the fold gives itself away.
 *
 * A null [window] is the blank leaf you see across a chapter boundary, where
 * the page arriving belongs to a chapter whose layout has not been measured
 * yet. Cream paper is the honest thing to show there.
 */
@Composable
private fun Leaf(
    book: String,
    chapter: Int,
    folio: Int,
    window: PageWindow?,
    verses: IntRange?,
    layout: TextLayoutResult?,
    initial: TextLayoutResult?,
    textSize: Int,
    onBodyHeight: (Float) -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(
            /* Cream, edge to edge. The leather bands above and below the page
               cost about a ninth of the height to ornament and put the header
               on top of gold filigree; the board belongs to the cover. */
            Brush.linearGradient(
                0f to P.pageLit,
                .45f to Page,
                1f to PageEdge,
            ),
        ),
    ) {
        // the gutter, where the leaf turns into the spine
        Box(
            Modifier.fillMaxHeight().width(16.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(P.gutter.copy(alpha = .22f), Color.Transparent),
                    ),
                ),
        )

        Column(Modifier.fillMaxSize().statusBarsPadding().padding(start = 26.dp, end = 22.dp)) {
            Row(
                /* 58dp assumed the header row sat flush against the top of the
                   window. It now insets for the status bar itself, so GENESIS
                   was landing under PrayerKey. Its own statusBarsPadding above
                   accounts for the bar; this only has to clear the floating
                   header row (bookmark circle at 34dp, plus its own 10dp top
                   padding). */
                Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    book,
                    color = P.caption.copy(alpha = .8f), fontFamily = BookSerif,
                    fontSize = 10.sp, letterSpacing = 2.2.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    verses?.let { "$chapter:${it.first}–${it.last}" } ?: "KJV",
                    color = P.caption.copy(alpha = .8f), fontFamily = BookSerif,
                    fontSize = 10.sp, letterSpacing = 1.6.sp,
                )
            }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(P.rule.copy(alpha = .3f)))

            /* The heading's space is reserved on every leaf, even where it
               draws nothing: a body box that changed height between page one
               and page two would change the page capacity, which changes how
               many pages there are, which is a loop. */
            Box(Modifier.fillMaxWidth().height(38.dp), contentAlignment = Alignment.Center) {
                if (window?.index == 0) {
                    Text(
                        "Chapter $chapter",
                        color = BookInk, fontFamily = BookSerif,
                        fontSize = (textSize - 1).sp, fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp, textAlign = TextAlign.Center,
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .onSizeChanged { onBodyHeight(it.height.toFloat()) },
            ) {
                if (layout != null && window != null && window.lines > 0) {
                    val top = layout.getLineTop(window.firstLine)
                    val bottom = layout.getLineBottom(window.lastLine - 1)
                    Canvas(Modifier.fillMaxSize()) {
                        // only this leaf's lines: the next leaf's first line
                        // used to show beneath them, sliced in half
                        // the opening leaf lets the initial rise above the first line
                        val rise = if (window.firstLine == 0 && initial != null) initial.firstBaseline else 0f
                        clipRect(top = -rise, bottom = bottom - top) {
                            translate(top = -top) {
                                drawText(layout)
                                // the initial, standing on the first line's baseline
                                val place = layout.placeholderRects.firstOrNull()
                                if (initial != null && place != null) {
                                    drawText(
                                        initial,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            place.left, place.bottom - initial.firstBaseline,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "$folio",
                color = P.caption.copy(alpha = .7f), fontFamily = BookSerif,
                fontSize = 10.sp, textAlign = TextAlign.Center,
                // clears the floating nav — the folio was landing behind it
                modifier = Modifier.fillMaxWidth().padding(bottom = 96.dp, top = 6.dp),
            )
        }
    }
}
