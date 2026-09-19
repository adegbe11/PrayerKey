package com.prayerkey.manna.ui.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.JOURNAL_TEMPLATES
import com.prayerkey.manna.data.TEMPLATE_COLLECTIONS
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans

@Composable
fun PromptLibrary(onClose: () -> Unit, onPrompt: (String, String) -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    BackHandler(onBack = { if (selected == null) onClose() else selected = null })
    val questions = remember(selected) {
        JOURNAL_TEMPLATES.filter { selected == null || it.collection == selected }
            .flatMap { it.prompts.map { prompt -> prompt.question to it.collection } }
            .distinctBy { it.first }
    }
    Column(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
            IconButton(onClick = { if (selected == null) onClose() else selected = null }) {
                Icon(Icons.Outlined.ArrowBack, "Back", tint = Pk.Charcoal)
            }
            Text(if (selected == null) "Prompts" else selected!!, Modifier.weight(1f).padding(top = 9.dp), fontFamily = DisplaySerif, fontSize = 25.sp, color = Pk.Charcoal)
        }
        if (selected == null) {
            Text("Recommended", Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
            val recommended = listOf(
                "What do you need to place in God’s hands today?" to "Prayer",
                "Where did you notice God’s goodness today?" to "Gratitude",
                "What prayer are you still waiting on?" to "Waiting",
            )
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recommended) { (question, pack) -> PromptCard(question, pack) { onPrompt(question, pack) } }
                item { Text("Prompt packs", Modifier.padding(top = 16.dp, bottom = 4.dp), fontFamily = DisplaySerif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal) }
                items(TEMPLATE_COLLECTIONS) { collection ->
                    Row(Modifier.fillMaxWidth().clickable { selected = collection.name }.padding(vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(collection.emoji, fontSize = 22.sp); Column(Modifier.weight(1f)) { Text(collection.name, fontFamily = UtilitySans, fontSize = 16.sp, color = Pk.Charcoal); Text(collection.blurb, fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted) }; Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(questions) { (question, pack) -> PromptCard(question, pack) { onPrompt(question, pack) } }
            }
        }
    }
}

@Composable private fun PromptCard(question: String, pack: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) { Text(question, fontFamily = DisplaySerif, fontSize = 17.sp, lineHeight = 23.sp, color = Pk.Charcoal); Text(pack, Modifier.padding(top = 8.dp), fontFamily = UtilitySans, fontSize = 11.sp, color = Pk.Oxblood, fontWeight = FontWeight.Bold) }
        Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
    }
}
