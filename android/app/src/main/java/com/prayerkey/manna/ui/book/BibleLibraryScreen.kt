package com.prayerkey.manna.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleBook
import com.prayerkey.manna.data.BibleCanon
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.UtilitySans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleLibraryScreen(
    translation: String,
    onChooseTranslation: () -> Unit,
    onPull: () -> Unit,
    onFlip: () -> Unit,
    onOpenChapter: (String, Int) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var oldTestament by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<BibleBook?>(null) }
    var viewMenuOpen by remember { mutableStateOf(false) }
    val books = remember(oldTestament, query) {
        BibleCanon.books.filter { it.oldTestament == oldTestament }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF2EBE0)).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(cs.primary.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                Text("P", color = cs.primary, fontFamily = DisplaySerif, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Bible", color = cs.onBackground, fontFamily = DisplaySerif, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            Row(
                Modifier.padding(start = 10.dp).height(42.dp).clip(RoundedCornerShape(13.dp)).background(cs.surface).clickable(onClick = onChooseTranslation).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(translation, color = cs.onSurface, fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.KeyboardArrowDown, "Choose translation", tint = cs.onSurface, modifier = Modifier.padding(start = 5.dp).size(18.dp))
            }
            Spacer(Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("CHOOSE A BOOK", color = cs.onBackground.copy(alpha = .55f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.weight(1f))
            Row(
                Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(cs.surface).clickable { viewMenuOpen = true }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.AutoStories, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                Text("  LIBRARY", color = cs.onSurface, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
                Icon(Icons.Outlined.KeyboardArrowDown, "Change Bible view", tint = cs.onSurface, modifier = Modifier.padding(start = 4.dp).size(16.dp))
            }
        }

        Row(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(18.dp)).background(cs.surface.copy(alpha = .52f))) {
            TestamentTab("OLD TESTAMENT", oldTestament, Modifier.weight(1f)) { oldTestament = true }
            TestamentTab("NEW TESTAMENT", !oldTestament, Modifier.weight(1f)) { oldTestament = false }
        }

        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(16.dp)).background(cs.surface).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, null, tint = cs.onSurface.copy(alpha = .46f), modifier = Modifier.size(20.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = cs.onSurface, fontFamily = UtilitySans, fontSize = 15.sp),
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    decorationBox = { inner -> if (query.isBlank()) Text("Search Bible books", color = cs.onSurface.copy(alpha = .40f), fontFamily = UtilitySans, fontSize = 15.sp); inner() },
                )
            }
        }

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(13.dp), contentPadding = PaddingValues(bottom = 110.dp)) {
            items(books, key = { it.name }) { book ->
                Row(
                    Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(22.dp)).background(cs.surface)
                        .clickable { selectedBook = book }.padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(book.name, color = cs.onSurface, fontFamily = UtilitySans, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("${book.chapters} CH", color = cs.onSurface.copy(alpha = .42f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = .7.sp)
                }
            }
        }
    }

    if (viewMenuOpen) {
        ModalBottomSheet(onDismissRequest = { viewMenuOpen = false }, containerColor = cs.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp)) {
                Text("Choose how you read", color = cs.onSurface, fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                ViewChoice(Icons.Outlined.Style, "Pull", false) { viewMenuOpen = false; onPull() }
                ViewChoice(Icons.Outlined.AutoStories, "Library", true) { viewMenuOpen = false }
                ViewChoice(Icons.Outlined.MenuBook, "Flip Bible", false) { viewMenuOpen = false; onFlip() }
            }
        }
    }

    selectedBook?.let { book ->
        ModalBottomSheet(onDismissRequest = { selectedBook = null }, containerColor = cs.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 30.dp)) {
                Text(book.name, color = cs.onSurface, fontFamily = DisplaySerif, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("CHOOSE A CHAPTER", color = cs.onSurface.copy(alpha = .50f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 5.dp, bottom = 16.dp))
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(5),
                    modifier = Modifier.heightIn(max = 470.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(book.chapters, key = { it }) { chapter ->
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(cs.background)
                                .clickable { selectedBook = null; onOpenChapter(book.name, chapter + 1) },
                            contentAlignment = Alignment.Center,
                        ) { Text("${chapter + 1}", color = cs.onSurface, fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ExperienceTab(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(if (active) Ink else Color.Transparent).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = if (active) cs.surface else cs.onSurface.copy(alpha = .55f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
    }
}

@Composable
private fun ViewChoice(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, active: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(18.dp))
            .background(if (active) Ink else cs.background).clickable(onClick = onClick).padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (active) cs.surface else cs.primary, modifier = Modifier.size(23.dp))
        Text(title, color = if (active) cs.surface else cs.onSurface, fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 14.dp))
        if (active) Text("CURRENT", color = cs.surface.copy(alpha = .55f), fontFamily = UtilitySans, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
    }
}

@Composable
private fun RowScope.TestamentTab(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(if (active) Ink else Color.Transparent).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = if (active) cs.surface else cs.onSurface, fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
    }
}
