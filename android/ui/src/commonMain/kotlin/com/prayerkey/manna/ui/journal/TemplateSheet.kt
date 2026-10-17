package com.prayerkey.manna.ui.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.JournalTemplate
import com.prayerkey.manna.data.TemplatePrompt
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A template, as one printed page.
 *
 * WHAT A PRINTED PRAYER JOURNAL PAGE IS
 *
 * One sheet, repeated: a title, the date, and every section of the day laid out
 * together - a list to tick, a few numbered lines, a fill-in line, some ruled
 * space to write - with the same two typefaces, the same hairline rules and the
 * same margins on every page. The reader sees the whole of it at a glance and
 * learns where everything goes. Nothing is behind a button.
 *
 * HOW A HUNDRED TEMPLATES EACH GET THEIR OWN PAGE
 *
 * A small set of blocks - fill-in line, ruled space, checkbox list, numbered
 * list, scripture line - and each template is its own arrangement of them,
 * chosen from what its questions ask for. "Who are you praying for?" becomes a
 * list to tick. "Church and speaker" becomes one line. "Three things" becomes
 * three numbered lines. Two short lists sitting next to each other share a row,
 * the way "Thankful for / Praying for" sit side by side on paper.
 *
 * The page is sized to fit a phone screen when empty: the more sections a
 * template has, the fewer ruled lines each one gets, and the last place to
 * write keeps the most room. It grows as it is written in, as paper fills.
 */

private enum class Block { Field, Ruled, Scripture, Checklist, Numbered }

private data class Section(
    val key: Int,
    val label: String,
    val sub: String,
    val block: Block,
    val rows: Int,
    val hint: String = "",
    val private: Boolean = false,
)

private val ORDINALS = setOf("one", "two", "three", "four", "five")

/** Short fill-in answers: a name, a reference, a place. One line is the page's way of saying "briefly". */
private val FIELDS = setOf(
    "church and speaker", "passage", "chapter", "who", "why", "what i read", "the habit",
    "who said it", "how often", "when and where", "the verse", "the passage", "who, and what happened",
    "what i studied",
)

private fun blockFor(p: TemplatePrompt): Block {
    val q = p.question.trim().lowercase(Locale.UK)
    return when {
        q in FIELDS -> Block.Field
        p.heading == "TODAY'S WORD" || q.startsWith("scripture") || q == "the verse" -> Block.Scripture
        p.people || p.heading in setOf("PEOPLE ON MY HEART", "I AM ASKING") ||
            q.startsWith("who am i praying") || q.startsWith("who needs my prayers") ||
            q.startsWith("who is praying") || q.startsWith("who or what am i") ||
            q.startsWith("what am i asking god for") -> Block.Checklist
        p.heading == "GRATITUDE" || q.contains("three things") || p.guide.lowercase(Locale.UK).contains("three things") -> Block.Numbered
        else -> Block.Ruled
    }
}

private fun sectionsOf(t: JournalTemplate): List<Section> {
    // consecutive ordinal prompts ("One", "Two", "Three") are one numbered list
    val merged = mutableListOf<Pair<Int, TemplatePrompt>>()
    var ordinals = 0
    t.prompts.forEachIndexed { i, p ->
        if (p.question.trim().lowercase(Locale.UK) in ORDINALS && p.heading.isBlank()) ordinals++
        else merged += i to p
    }
    val raw = mutableListOf<Section>()
    if (ordinals > 0) raw += Section(1000, if (t.collection == "Gratitude") "TODAY I AM THANKFUL FOR" else "", "", Block.Numbered, ordinals)
    merged.forEach { (i, p) ->
        val block = blockFor(p)
        raw += Section(
            key = i,
            label = (p.heading.ifBlank { p.question }).uppercase(Locale.UK),
            sub = if (p.heading.isNotBlank()) p.question else p.guide,
            block = block,
            rows = when (block) { Block.Field -> 1; Block.Checklist, Block.Numbered -> 3; else -> 0 },
            hint = p.starter,
            private = p.private,
        )
    }

    // a page ends in writing space: its lines run on to the foot of the sheet
    if (raw.lastOrNull()?.block != Block.Ruled) raw += Section(2000, "NOTES", "", Block.Ruled, 0)

    // ruled space, fitted to one screen: fewer lines each as sections multiply,
    // and the last ruled section - usually the prayer itself - keeps the most
    val n = raw.size
    val each = when { n <= 2 -> 7; n <= 3 -> 5; n <= 5 -> 3; n <= 7 -> 2; else -> 1 }
    val lastRuled = raw.indexOfLast { it.block == Block.Ruled }
    return raw.mapIndexed { idx, s ->
        when (s.block) {
            Block.Ruled -> s.copy(rows = if (idx == lastRuled) each + 2 else each)
            Block.Scripture -> s.copy(rows = if (n > 5) 1 else 2)
            else -> s
        }
    }
}

/** Two short lists next to each other share a row. */
private fun rowsOf(sections: List<Section>): List<List<Section>> {
    val out = mutableListOf<List<Section>>()
    var i = 0
    while (i < sections.size) {
        val a = sections[i]
        val b = sections.getOrNull(i + 1)
        val short = setOf(Block.Checklist, Block.Numbered)
        if (b != null && a.block in short && b.block in short && a.label.length < 26 && b.label.length < 26) {
            out += listOf(a, b); i += 2
        } else {
            out += listOf(a); i += 1
        }
    }
    return out
}

@Composable
fun TemplateSheet(
    template: JournalTemplate,
    onClose: () -> Unit,
    onFinish: (title: String, body: String, journal: String, prayer: Boolean) -> Unit,
) {
    BackHandler(onBack = onClose)
    val sections = remember(template.id) { sectionsOf(template) }
    val answers = rememberSaveable(template.id, saver = mapSaver(
        save = { m: SnapshotStateMap<Int, String> -> m.mapKeys { it.key.toString() } },
        restore = { saved -> mutableStateMapOf<Int, String>().apply { saved.forEach { (k, v) -> put(k.toInt(), v as String) } } },
    )) { mutableStateMapOf<Int, String>() }
    val today = remember { LocalDate.now() }
    val written = answers.values.any { it.isNotBlank() }

    fun done() {
        val body = buildString {
            sections.forEach { s ->
                val a = answers[s.key].orEmpty().trim()
                if (a.isEmpty()) return@forEach
                if (s.label.isNotBlank()) append("## ").append(s.label.lowercase(Locale.UK).replaceFirstChar { it.titlecase(Locale.UK) }).append("\n\n")
                append(a).append("\n\n")
            }
        }.trim()
        if (body.isBlank()) onClose() else onFinish(template.title, body, template.journal, template.prayer)
    }

    val lastKey = sections.last().key
    val lastField = remember { FocusRequester() }

    JournalPaper {
      BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        val page = maxHeight
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 34.dp, end = 32.dp, top = 52.dp, bottom = 34.dp)
                .heightIn(min = page - 86.dp),
        ) {
            // ── the head of the page ──────────────────────────────────────
            Text(
                "✦   " + template.title + "   ✦",
                Modifier.fillMaxWidth(),
                color = Pk.JournalInk, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic,
                fontSize = 27.sp, lineHeight = 32.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Date  " + today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)),
                    Modifier.weight(1f),
                    color = Pk.JournalInk.copy(alpha = .75f), fontFamily = BookSerif, fontSize = 13.sp,
                )
                listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, d ->
                    val on = today.dayOfWeek.value - 1 == i
                    Box(
                        Modifier.size(19.dp).clip(CircleShape)
                            .then(if (on) Modifier.border(1.dp, Pk.Oxblood.copy(alpha = .7f), CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(d, color = if (on) Pk.Oxblood else Pk.JournalInk.copy(alpha = .45f), fontFamily = UtilitySans, fontSize = 9.sp)
                    }
                }
            }
            Rule(Modifier.padding(top = 10.dp))

            if (template.verse.isNotBlank()) {
                Text(
                    template.verse + "  — " + template.verseRef,
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    color = Pk.JournalInk.copy(alpha = .72f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 13.5.sp, lineHeight = 20.sp, textAlign = TextAlign.Center,
                )
            }

            // ── the sections ─────────────────────────────────────────────
            rowsOf(sections).forEach { row ->
                Spacer(Modifier.height(20.dp))
                if (row.size == 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        row.forEach { s -> Column(Modifier.weight(1f)) { SectionBlock(s, answers) } }
                    }
                } else {
                    SectionBlock(row.first(), answers, if (row.first().key == lastKey) lastField else null)
                }
            }

            // the rest of the sheet, ruled on from the last lines; writing there
            // continues the last section
            val lineH = with(LocalDensity.current) { LINE.toPx() }
            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .clickable(interactionSource = null, indication = null) { runCatching { lastField.requestFocus() } }
                    .drawBehind {
                        var y = lineH - 1.dp.toPx()
                        while (y <= size.height) {
                            drawLine(Pk.JournalRule.copy(alpha = .75f), Offset(0f, y), Offset(size.width, y), .6.dp.toPx())
                            y += lineH
                        }
                    },
            )
        }
      }

        // ── the way out and the way to keep it ─────────────────────────────
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 12.dp, end = 30.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, "Close", tint = Pk.JournalInk.copy(alpha = .65f), modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Done",
                color = if (written) Pk.Oxblood else Pk.JournalInk.copy(alpha = .35f),
                fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = written) { done() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionBlock(s: Section, answers: SnapshotStateMap<Int, String>, focus: FocusRequester? = null) {
    if (s.label.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                s.label,
                color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 10.sp,
                letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp,
            )
            if (s.private) {
                Icon(Icons.Outlined.Lock, "Private", tint = Pk.Muted, modifier = Modifier.padding(start = 6.dp).size(11.dp))
            }
        }
    }
    if (s.sub.isNotBlank()) {
        Text(
            s.sub,
            Modifier.padding(top = 2.dp),
            color = Pk.JournalInk.copy(alpha = .55f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
            fontSize = 12.5.sp, lineHeight = 17.sp,
        )
    }
    val value = answers[s.key].orEmpty()
    val set: (String) -> Unit = { answers[s.key] = it }
    when (s.block) {
        Block.Field, Block.Ruled -> RuledSpace(value, set, s.rows, s.hint, italic = false, focus = focus)
        Block.Scripture -> RuledSpace(value, set, s.rows, s.hint.ifBlank { "Reference or verse" }, italic = true)
        Block.Checklist -> ListRows(value, set, s.rows, numbered = false, hint = s.hint)
        Block.Numbered -> ListRows(value, set, s.rows, numbered = true, hint = s.hint)
    }
}

private val LINE: Dp = 30.dp

@Composable
private fun Rule(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Pk.JournalRule.copy(alpha = .75f)))
}

/** Writing space ruled to its height; it grows past that as it is written in. */
@Composable
private fun RuledSpace(value: String, onChange: (String) -> Unit, rows: Int, hint: String, italic: Boolean, focus: FocusRequester? = null) {
    val lineH = with(LocalDensity.current) { LINE.toPx() }
    Box(
        Modifier.fillMaxWidth().heightIn(min = LINE * rows).drawBehind {
            var y = lineH - 1.dp.toPx()
            while (y <= size.height + 1f) {
                drawLine(Pk.JournalRule.copy(alpha = .75f), Offset(0f, y), Offset(size.width, y), .6.dp.toPx())
                y += lineH
            }
        },
    ) {
        val style = TextStyle(
            color = Pk.JournalInk, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 30.sp,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        )
        BasicTextField(
            value, onChange,
            textStyle = style,
            singleLine = rows == 1,
            modifier = Modifier.fillMaxWidth().heightIn(min = LINE * rows).then(if (focus != null) Modifier.focusRequester(focus) else Modifier),
            decorationBox = { field ->
                Box {
                    if (value.isEmpty() && hint.isNotBlank()) Text(hint, style = style.copy(color = Pk.Muted.copy(alpha = .6f), fontStyle = FontStyle.Italic))
                    field()
                }
            },
        )
    }
}

/**
 * A box (or a number) and a line per item.
 *
 * Kept as markdown - "- [ ] Mum", "1. The sunrise" - so the saved page reads
 * the same way, and a request can be ticked answered from the journal later.
 */
@Composable
private fun ListRows(value: String, onChange: (String) -> Unit, rows: Int, numbered: Boolean, hint: String) {
    val items = remember(value, rows) {
        val parsed = value.lines().filter { it.isNotBlank() }.map { line ->
            val done = line.startsWith("- [x] ")
            done to line.removePrefix("- [x] ").removePrefix("- [ ] ").replace(Regex("^\\d+\\. "), "")
        }
        // always one empty line past the last written one, so a list can grow
        parsed + List((rows - parsed.size).coerceAtLeast(1)) { false to "" }
    }
    fun save(next: List<Pair<Boolean, String>>) {
        onChange(
            next.filter { it.second.isNotBlank() }.mapIndexed { i, (done, text) ->
                if (numbered) "${i + 1}. $text" else (if (done) "- [x] " else "- [ ] ") + text
            }.joinToString("\n"),
        )
    }
    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { i, (done, text) ->
            Row(
                Modifier.fillMaxWidth().height(LINE).drawBehind {
                    drawLine(Pk.JournalRule.copy(alpha = .75f), Offset(0f, size.height - 1.dp.toPx()), Offset(size.width, size.height - 1.dp.toPx()), .6.dp.toPx())
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (numbered) {
                    Text("${i + 1}.", Modifier.width(20.dp), color = Pk.JournalInk.copy(alpha = .55f), fontFamily = BookSerif, fontSize = 14.sp)
                } else {
                    Box(
                        Modifier.size(13.dp).clip(RoundedCornerShape(2.dp))
                            .border(1.dp, Pk.Oxblood.copy(alpha = .55f), RoundedCornerShape(2.dp))
                            .background(if (done) Pk.Oxblood else Color.Transparent)
                            .clickable(enabled = text.isNotBlank()) {
                                save(items.mapIndexed { j, it -> if (j == i) (!it.first to it.second) else it })
                            },
                        contentAlignment = Alignment.Center,
                    ) { if (done) Text("✓", color = Color.White, fontSize = 9.sp) }
                    Spacer(Modifier.width(9.dp))
                }
                val style = TextStyle(
                    color = Pk.JournalInk, fontFamily = BookSerif, fontSize = 15.5.sp,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                BasicTextField(
                    text,
                    { typed -> save(items.mapIndexed { j, it -> if (j == i) (it.first to typed.replace("\n", " ")) else it }) },
                    singleLine = true,
                    textStyle = style,
                    modifier = Modifier.weight(1f),
                    decorationBox = { field ->
                        Box {
                            if (text.isEmpty() && i == 0 && hint.isNotBlank()) Text(hint, style = style.copy(color = Pk.Muted.copy(alpha = .6f), fontStyle = FontStyle.Italic))
                            field()
                        }
                    },
                )
            }
        }
    }
}
