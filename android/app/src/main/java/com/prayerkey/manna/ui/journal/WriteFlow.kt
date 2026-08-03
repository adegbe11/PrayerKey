package com.prayerkey.manna.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.church.ReferenceDetector
import com.prayerkey.manna.ui.theme.premiumCard
import com.prayerkey.manna.ui.theme.PaperFill
import com.prayerkey.manna.ui.theme.topHighlight
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.theme.InkSoft
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.Muted

/** Mood vocabulary, shared by the sheet and the feed cards. */
val MOOD_LIST = listOf(
    "🙏" to "Praying", "😊" to "Grateful", "✨" to "Hopeful",
    "🔥" to "Faith", "😔" to "Heavy", "😭" to "Broken",
)

/** Moods that mean "this is hard" get a calmer canvas and a body cue. */
private val HEAVY = setOf("😔", "😭", "🙏")

data class WriteResult(
    val mood: String,
    val body: String,
    val gratitude: String,
    val verseRef: String?,
    val verseText: String?,
    val source: String,
    val isPrayer: Boolean,
)

/**
 * The intercept sheet. Tapping Write never lands on a blank page — it lands
 * on things this person already did, turned into questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSheet(
    prompts: List<JournalSuggestions.Prompt>,
    onPick: (JournalSuggestions.Prompt?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp)) {
            Text("What do you want to write about?", fontFamily = FontFamily.Serif, fontSize = 23.sp)
            Text(
                "Pulled from your own week. Nothing here left your phone.",
                color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp, bottom = 18.dp),
            )

            prompts.forEach { prompt ->
                Box(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        .premiumCard(fill = PaperFill, lift = false)
                        .clickable { onPick(prompt) },
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(prompt.kicker, color = Gold, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                            Text(prompt.text, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
                    }
                }
            }

            // the escape hatch, for people who already know what to say
            Surface(
                onClick = { onPick(null) },
                shape = R.card, color = Color.White,
                border = BorderStroke(1.dp, Hairline),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    "Start from a blank page",
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Electric,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/**
 * The editor. Three things earn their place here beyond a text box:
 * the canvas calms when the mood is heavy, references the person types are
 * recognised on device and offered as attachments, and an entry can be
 * marked a prayer so it can later be marked answered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteSheet(
    prompt: JournalSuggestions.Prompt?,
    todayCard: VerseCard,
    initialMood: String = MOOD_LIST.first().first,
    initialBody: String = "",
    initialGratitude: String = "",
    initialIsPrayer: Boolean = false,
    editing: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (WriteResult) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var mood by remember { mutableStateOf(initialMood) }
    var body by remember { mutableStateOf(initialBody) }
    var gratitude by remember { mutableStateOf(initialGratitude) }
    var isPrayer by remember { mutableStateOf(initialIsPrayer || prompt?.suggestPrayer == true) }
    var attachToday by remember { mutableStateOf(false) }
    var attachedRef by remember { mutableStateOf(prompt?.verseRef) }
    var attachedText by remember { mutableStateOf(prompt?.verseText) }

    val heavy = mood in HEAVY
    val canvas by animateColorAsState(
        if (heavy) Color(0xFFF4F6FB) else Color(0xFFFFFCF4),
        label = "write-canvas",
    )

    // scripture the person typed, recognised on device — the same parser the
    // sermon listener uses, so it costs nothing and works offline
    val detected = remember(body) {
        ReferenceDetector.scan(body).map { it.reference }.distinct().take(3)
            .filter { it != attachedRef }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = canvas,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(bottom = 30.dp),
        ) {
            Text(
                if (editing) "Edit entry" else "Today's entry",
                fontFamily = FontFamily.Serif, fontSize = 24.sp,
            )
            prompt?.let {
                Text(
                    it.text, color = Muted, fontSize = 13.sp, lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Text("HOW IS YOUR HEART?", color = Muted, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MOOD_LIST.forEach { (emoji, _) ->
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(if (mood == emoji) Color(0xFFE7EDFF) else Color.Transparent)
                            .clickable { mood = emoji },
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 20.sp) }
                }
            }

            // somatic cue — journaling a hard thing tightens the body
            AnimatedVisibility(heavy) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Color(0xFFE8EDF7),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(
                        "Drop your shoulders. Unclench your jaw. Keep typing.",
                        color = Color(0xFF3A4664), fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }

            OutlinedTextField(
                body, { body = it },
                placeholder = { Text("What's on your heart today?", fontFamily = FontFamily.Serif, fontSize = 15.sp) },
                minLines = 6,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                shape = R.card,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 23.sp, color = InkSoft),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold.copy(alpha = .5f),
                    unfocusedBorderColor = Hairline,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            AnimatedVisibility(detected.isNotEmpty()) {
                Column(Modifier.padding(top = 10.dp)) {
                    Text("SCRIPTURE YOU MENTIONED", color = Muted, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        detected.forEach { ref ->
                            Surface(
                                onClick = { attachedRef = ref; attachedText = null },
                                shape = RoundedCornerShape(99.dp),
                                color = Gold.copy(alpha = .13f),
                                border = BorderStroke(1.dp, Gold.copy(alpha = .3f)),
                            ) {
                                Text(
                                    "+ $ref", color = Gold, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }

            attachedRef?.let { ref ->
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Color(0xFFF4EDDC),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📜 $ref", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("Remove", color = Muted, fontSize = 11.sp, modifier = Modifier.clickable { attachedRef = null; attachedText = null })
                    }
                }
            }

            OutlinedTextField(
                gratitude, { gratitude = it },
                placeholder = { Text("One thing you're grateful for…", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = R.control,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold.copy(alpha = .5f),
                    unfocusedBorderColor = Hairline,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            /* The lifecycle toggle. Marking this a prayer is what makes an
               "answered" badge possible months from now. */
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("This is a prayer request", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("So you can mark it answered later", color = Muted, fontSize = 11.sp)
                }
                Switch(isPrayer, { isPrayer = it })
            }

            if (!editing && attachedRef == null) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp).clickable { attachToday = !attachToday },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(attachToday, { attachToday = it })
                    Text("Attach today's word (${todayCard.reference})", fontSize = 12.5.sp)
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFB3402A), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Delete", color = Color(0xFFB3402A))
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) }
                Spacer(Modifier.width(4.dp))
                val ready = body.isNotBlank()
                Box(
                    Modifier.height(48.dp).clip(R.control)
                        .background(
                            if (ready) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF4E70FF), Electric))
                            else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFDCDCE2), Color(0xFFD1D1D8))),
                        )
                        .topHighlight(R.control, strength = if (ready) .4f else .2f)
                        .clickable(enabled = ready) {
                            onSubmit(
                                WriteResult(
                                    mood = mood,
                                    body = body,
                                    gratitude = gratitude,
                                    verseRef = attachedRef ?: todayCard.reference.takeIf { attachToday },
                                    verseText = attachedText ?: todayCard.verse.takeIf { attachToday },
                                    source = prompt?.source ?: "write",
                                    isPrayer = isPrayer,
                                ),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Save entry", color = Color.White, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, modifier = Modifier.padding(horizontal = 22.dp),
                    )
                }
            }
        }
    }
}
