package com.prayerkey.manna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.prayerkey.manna.ui.MannaApp
import com.prayerkey.manna.ui.theme.MannaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MannaTheme { MannaApp() } }
    }
}
