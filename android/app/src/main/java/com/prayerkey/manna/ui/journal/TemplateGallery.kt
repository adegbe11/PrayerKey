package com.prayerkey.manna.ui.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.JOURNAL_TEMPLATES
import com.prayerkey.manna.data.JournalTemplate
import com.prayerkey.manna.data.TEMPLATE_COLLECTIONS
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

/**
 * The template gallery.
 *
 * Ninety-seven templates is a library, and a library shown as a list of
 * ninety-seven things is a wall. So: a search field, the collections as
 * chips, and the templates of whichever collection is open.
 *
 * Search is over title, summary and the questions themselves, because people
 * look for a template by what they want to write about - "fear", "my
 * daughter", "waiting" - far more often than by its name.
 */
@Composable
fun TemplateGallery(
    recent: List<String>,
    onPick: (JournalTemplate) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    var query by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf<String?>(null) }

    val results = remember(query, collection) {
        val q = query.trim().lowercase()
        JOURNAL_TEMPLATES.filter { t ->
            (collection == null || t.collection == collection) &&
                (
                    q.isEmpty() ||
                        t.title.lowercase().contains(q) ||
                        t.summary.lowercase().contains(q) ||
                        t.prompts.any { it.lowercase().contains(q) }
                    )
        }
    }
    val recents = remember(recent) { recent.mapNotNull { id -> JOURNAL_TEMPLATES.find { it.id == id } } }

    Column(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBack, "Close", tint = Pk.Charcoal) }
            Text(
                "Templates", Modifier.weight(1f),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp, color = Pk.Charcoal,
            )
            Text(
                "${JOURNAL_TEMPLATES.size}",
                Modifier.padding(end = 16.dp),
                fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp)).background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, null, tint = Pk.Muted, modifier = Modifier.size(17.dp))
            Box(Modifier.weight(1f).padding(start = 10.dp)) {
                if (query.isEmpty()) {
                    Text(
                        "Search templates",
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
                    )
                }
                BasicTextField(
                    query, { query = it },
                    textStyle = TextStyle(color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 14.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Chip("All", collection == null) { collection = null }
            TEMPLATE_COLLECTIONS.forEach { c ->
                Chip(c.emoji + "  " + c.name, collection == c.name) { collection = c.name }
            }
        }

        LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            if (query.isBlank() && collection == null && recents.isNotEmpty()) {
                item {
                    Text(
                        "RECENTLY USED",
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 9.sp,
                        letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                items(recents, key = { "recent-" + it.id }) { TemplateRow(it, onPick) }
                item { Spacer(Modifier.height(22.dp)) }
            }
            if (results.isEmpty()) {
                item {
                    Text(
                        "Nothing matches “" + query.trim() + "”",
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 30.dp),
                    )
                }
            }
            items(results, key = { it.id }) { TemplateRow(it, onPick) }
        }
    }
}

@Composable
private fun Chip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(99.dp))
            .background(if (on) Pk.Oxblood else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (on) Color.White else Pk.Charcoal,
            fontFamily = UtilitySans, fontSize = 12.sp,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun TemplateRow(t: JournalTemplate, onPick: (JournalTemplate) -> Unit) {
    val emoji = TEMPLATE_COLLECTIONS.find { it.name == t.collection }?.emoji.orEmpty()
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp)).background(Color.White)
            .clickable { onPick(t) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                .background(Pk.Oxblood.copy(alpha = .08f)),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 19.sp) }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Text(
                t.title, color = Pk.Charcoal, fontFamily = UtilitySans,
                fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
            )
            Text(
                t.summary, Modifier.padding(top = 2.dp), color = Pk.Muted,
                fontFamily = UtilitySans, fontSize = 11.5.sp, lineHeight = 16.sp,
            )
        }
        Text(
            "${t.prompts.size}",
            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.sp,
        )
    }
}

/**
 * A template, before you commit to it.
 *
 * The questions are the template, so the preview shows all of them rather than
 * a description of them. Somebody deciding between two templates is deciding
 * between two sets of questions, and no summary substitutes for reading them.
 */
@Composable
fun TemplatePreview(
    template: JournalTemplate,
    onUse: (JournalTemplate) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val emoji = TEMPLATE_COLLECTIONS.find { it.name == template.collection }?.emoji.orEmpty()

    Column(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Pk.Charcoal) }
            Text(
                template.collection, Modifier.weight(1f),
                fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted,
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp)) {
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .08f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 26.sp) }

            Text(
                template.title,
                Modifier.padding(top = 18.dp),
                color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Light,
                fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp,
            )
            Text(
                template.summary,
                Modifier.padding(top = 10.dp),
                color = Pk.Charcoal.copy(alpha = .70f), fontFamily = BookSerif,
                fontWeight = FontWeight.Light, fontSize = 16.sp, lineHeight = 24.sp,
            )

            Spacer(Modifier.height(26.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Pk.Charcoal.copy(alpha = .08f)))
            Spacer(Modifier.height(22.dp))

            template.prompts.forEachIndexed { index, prompt ->
                Row(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text(
                        "${index + 1}",
                        color = Pk.Oxblood, fontFamily = UtilitySans,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp, top = 3.dp),
                    )
                    Text(
                        prompt,
                        color = Pk.Charcoal, fontFamily = BookSerif,
                        fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 26.sp,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(99.dp)).background(Pk.Oxblood)
                .clickable { onUse(template) }
                .padding(vertical = 17.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Use this template",
                color = Color.White, fontFamily = UtilitySans,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
