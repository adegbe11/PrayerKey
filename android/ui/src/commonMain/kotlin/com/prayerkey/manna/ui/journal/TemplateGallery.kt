package com.prayerkey.manna.ui.journal

import com.prayerkey.manna.ui.platform.StatusBarIcons
import org.jetbrains.compose.resources.DrawableResource
import com.prayerkey.manna.resources.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
 * The template gallery, arranged the way Apple Music arranges Search.
 *
 * A large title, one rounded search field, one featured card, and then the
 * library as a two-column wall of picture tiles - each collection its own
 * colour over its own artwork, named in the corner and nothing else. A tile
 * opens its collection as the same wall, one tile per template. Search
 * answers with tiles too, so the page never changes shape under the reader.
 */
private data class Look(val colour: Color, val art: DrawableResource)

private val LOOKS = mapOf(
    "Prayer" to Look(Color(0xFF6200ED), Res.drawable.onboarding_pray_v3),
    "Daily walk" to Look(Color(0xFFE0782F), Res.drawable.welcome_prayer_dawn),
    "Bible & Scripture" to Look(Color(0xFF2F4FA8), Res.drawable.discovery_cover_2),
    "Answered prayer" to Look(Color(0xFFD69A12), Res.drawable.discovery_cover_4),
    "Gratitude" to Look(Color(0xFFD9406E), Res.drawable.discovery_cover_3),
    "Reflection" to Look(Color(0xFF3C6FB0), Res.drawable.rhythm_reflection),
    "Spiritual warfare" to Look(Color(0xFF7A1FA2), Res.drawable.discovery_cover_1),
    "Spiritual growth" to Look(Color(0xFF2E9459), Res.drawable.discovery_cover_5),
    "Difficult seasons" to Look(Color(0xFF475C84), Res.drawable.onboarding_prayer),
    "Family" to Look(Color(0xFFE25A45), Res.drawable.discovery_cover_6),
    "Purpose & decisions" to Look(Color(0xFF178A86), Res.drawable.closing_verse_violet),
    "Dreams & notes" to Look(Color(0xFF2E2A7A), Res.drawable.rhythm_evening),
    "Sermon & church" to Look(Color(0xFF9C4A1C), Res.drawable.onboarding_church_v3),
)

/** Every template tile gets its own picture, in its collection's colour. */
private val ART = listOf(
    Res.drawable.onboarding_pray_v3, Res.drawable.welcome_prayer_dawn, Res.drawable.discovery_cover_2,
    Res.drawable.discovery_cover_4, Res.drawable.discovery_cover_3, Res.drawable.rhythm_reflection,
    Res.drawable.discovery_cover_1, Res.drawable.discovery_cover_5, Res.drawable.onboarding_prayer,
    Res.drawable.discovery_cover_6, Res.drawable.closing_verse_violet, Res.drawable.rhythm_evening,
    Res.drawable.onboarding_church_v3, Res.drawable.rhythm_morning, Res.drawable.prayerkey_hero_dawn,
    Res.drawable.onboarding_read_v3, Res.drawable.onboarding_journal_v3, Res.drawable.onboarding_audio_v3,
    Res.drawable.today_prayer_violet, Res.drawable.prayerkey_discovery_world,
)

private fun lookOf(collection: String) = LOOKS[collection] ?: Look(Pk.Oxblood, Res.drawable.onboarding_pray_v3)

private fun artOf(t: JournalTemplate): DrawableResource {
    val inCollection = JOURNAL_TEMPLATES.filter { it.collection == t.collection }
    val base = ART.indexOf(lookOf(t.collection).art).coerceAtLeast(0)
    return ART[(base + inCollection.indexOf(t).coerceAtLeast(0)) % ART.size]
}

@Composable
fun TemplateGallery(
    recent: List<String>,
    onPick: (JournalTemplate) -> Unit,
    onClose: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var collection by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler { if (collection != null) collection = null else onClose() }

    val q = query.trim().lowercase()
    val results = remember(q, collection) {
        JOURNAL_TEMPLATES.filter { t ->
            (collection == null || t.collection == collection) &&
                (
                    q.isEmpty() ||
                        t.title.lowercase().contains(q) ||
                        t.summary.lowercase().contains(q) ||
                        t.prompts.any { it.question.lowercase().contains(q) || it.heading.lowercase().contains(q) }
                    )
        }
    }
    val featured = remember(recent) {
        recent.firstNotNullOfOrNull { id -> JOURNAL_TEMPLATES.find { it.id == id } }
            ?: JOURNAL_TEMPLATES.first { it.id == "prayer-morning" }
    }
    val full = GridItemSpan(2)
    val grid = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    androidx.compose.runtime.LaunchedEffect(collection) { grid.scrollToItem(0) }

    // dark status-bar icons on the white page
    StatusBarIcons(dark = true, inDialog = true)

    Box(Modifier.fillMaxSize().background(Color.White)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = grid,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { full }, key = "title") {
                Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (collection != null) {
                        Box(
                            Modifier.padding(end = 10.dp).size(36.dp).clip(CircleShape)
                                .background(Color(0xFFF1EFF4)).clickable { collection = null },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
                    }
                    Text(
                        collection ?: "Templates",
                        Modifier.weight(1f),
                        color = Color(0xFF111014), fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
                        fontSize = if (collection == null) 34.sp else 28.sp, lineHeight = 38.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1EFF4)).clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
                }
            }
            item(span = { full }, key = "search") {
                SearchField(query) { query = it }
            }

            if (q.isEmpty() && collection == null) {
                item(span = { full }, key = "featured") { Featured(featured, onPick) }
                // an odd tile out at the foot runs the full width, so the wall ends square
                items(
                    TEMPLATE_COLLECTIONS, key = { "c-" + it.name },
                    span = { c -> GridItemSpan(if (TEMPLATE_COLLECTIONS.size % 2 == 1 && c == TEMPLATE_COLLECTIONS.last()) 2 else 1) },
                ) { c ->
                    val look = lookOf(c.name)
                    val held = JOURNAL_TEMPLATES.count { it.collection == c.name }
                    Tile(c.name, look.colour, look.art, meta = "$held templates") { collection = c.name }
                }
            } else if (results.isEmpty()) {
                item(span = { full }, key = "none") {
                    Text(
                        "No results",
                        Modifier.fillMaxWidth().padding(top = 60.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp, textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(
                    results, key = { it.id },
                    span = { t -> GridItemSpan(if (results.size % 2 == 1 && t == results.last()) 2 else 1) },
                ) { t ->
                    Tile(t.title, lookOf(t.collection).colour, artOf(t)) { onPick(t) }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(99.dp)).background(Color(0xFFF1EFF4))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, null, tint = Color(0xFF3A3840), modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f).padding(start = 10.dp)) {
            if (query.isEmpty()) {
                Text("Prayer, gratitude, fear, family…", color = Color(0xFF8E8B94), fontFamily = UtilitySans, fontSize = 16.sp, maxLines = 1)
            }
            BasicTextField(
                query, onChange,
                textStyle = TextStyle(color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 16.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The one large card: the template you reach for, else Morning prayer. */
@Composable
private fun Featured(t: JournalTemplate, onPick: (JournalTemplate) -> Unit) {
    val look = lookOf(t.collection)
    Column(
        Modifier.fillMaxWidth().padding(bottom = 8.dp)
            .clip(RoundedCornerShape(22.dp)).background(Color.White)
            .clickable { onPick(t) },
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.55f).clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))) {
            Image(
                painterResource(Res.drawable.prayerkey_discovery_world), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, look.colour.copy(alpha = .55f)))))
            Text(
                t.title,
                Modifier.align(Alignment.Center).padding(horizontal = 20.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 38.sp, lineHeight = 42.sp, textAlign = TextAlign.Center,
            )
        }
        Column(
            Modifier.fillMaxWidth()
                .background(Color(0xFFF7F6F9), RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(t.summary, color = Color(0xFF111014), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 19.sp, textAlign = TextAlign.Center)
            Box(
                Modifier.padding(top = 14.dp).clip(RoundedCornerShape(99.dp)).background(Pk.Oxblood)
                    .clickable { onPick(t) }.padding(horizontal = 30.dp, vertical = 11.dp),
            ) { Text("Begin", color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
        }
    }
}

/** A picture in a colour, named in the corner. */
@Composable
private fun Tile(
    label: String,
    colour: Color,
    art: DrawableResource,
    /** What is behind the tile - "15 templates". Blank on a template itself. */
    meta: String = "",
    onClick: () -> Unit,
) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .clip(RoundedCornerShape(14.dp)).background(colour)
            .clickable(onClick = onClick),
    ) {
        Image(
            painterResource(art), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = .88f,
        )
        // the collection's colour, held to a trace so the artwork stays itself
        Box(Modifier.fillMaxSize().background(colour.copy(alpha = .22f)))
        /* The band the words sit on. A wash over the whole tile dulled the
           picture and still left a pale photograph beating the label; a short
           dark band at the foot does the legibility work where the words
           actually are, and the top two thirds of the art stay bright. */
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    .42f to Color.Transparent,
                    .72f to Color(0xFF14061F).copy(alpha = .42f),
                    1f to Color(0xFF14061F).copy(alpha = .88f),
                ),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Text(
                label,
                color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
                fontSize = if (label.length > 22) 14.sp else 16.sp, lineHeight = 18.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    Modifier.padding(top = 2.dp),
                    color = Color.White.copy(alpha = .74f), fontFamily = UtilitySans,
                    fontSize = 11.5.sp,
                )
            }
        }
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

    JournalPaper {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
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
                    Column {
                        if (prompt.heading.isNotBlank()) {
                            Text(
                                prompt.heading,
                                color = Pk.Oxblood, fontFamily = UtilitySans,
                                fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 5.dp),
                            )
                        }
                        Text(
                            prompt.question,
                            color = Pk.Charcoal, fontFamily = BookSerif,
                            fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 26.sp,
                        )
                    }
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
                "Use now",
                color = Color.White, fontFamily = UtilitySans,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            )
        }
    }
    }
}
