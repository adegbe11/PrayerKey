package com.prayerkey.manna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.prayerkey.manna.data.DEFAULT_THEME_ID
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.data.resolveTheme
import com.prayerkey.manna.ui.MannaApp
import com.prayerkey.manna.ui.theme.MannaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            /* Read once, synchronously, so the very first frame is already in
               the user's theme rather than flashing the default. */
            val stored = remember {
                runCatching { MannaStore(this).preferences() }.getOrNull()
            }
            var themeId by remember { mutableStateOf(stored?.themeId ?: DEFAULT_THEME_ID) }
            var followSystem by remember { mutableStateOf(stored?.followSystemTheme ?: true) }

            MannaTheme(resolveTheme(themeId, followSystem, isSystemInDarkTheme())) {
                MannaApp(
                    onThemeChange = { id, follow -> themeId = id; followSystem = follow },
                )
            }
        }
    }
}
