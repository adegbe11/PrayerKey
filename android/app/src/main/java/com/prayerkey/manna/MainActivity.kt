package com.prayerkey.manna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.prayerkey.manna.ui.MannaApp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.data.themeById
import com.prayerkey.manna.ui.theme.MannaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // read once, synchronously, so the very first frame is already
            // in the user's theme rather than flashing the default
            val stored = remember {
                runCatching { MannaStore(this).preferences().themeId }.getOrDefault("imperial_key")
            }
            var themeId by remember { mutableStateOf(stored) }
            MannaTheme(themeById(themeId)) {
                MannaApp(onThemeChange = { themeId = it })
            }
        }
    }
}
