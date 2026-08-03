package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import com.prayerkey.manna.data.SermonNote

/** A church bridge with no account and no access to anyone's private Journey. */
object MannaPack {
    fun share(context: Context, note: SermonNote) {
        val questions = note.points.take(3).mapIndexed { index, point ->
            "${index + 1}. Where does “${point.trim().take(100)}” meet ordinary life this week?"
        }.ifEmpty { listOf("1. What stayed with you from this message?", "2. What might faithful action look like this week?") }
        val scripture = note.scriptures.joinToString(" · ").ifBlank { "Review the sermon passage" }
        val text = buildString {
            appendLine("MANNA PACK")
            appendLine(note.title)
            appendLine()
            appendLine("READ")
            appendLine(scripture)
            appendLine()
            appendLine("CARRY")
            appendLine(note.takeaway.ifBlank { note.points.firstOrNull() ?: "Carry one truth from this message into the week." })
            appendLine()
            appendLine("TALK IT OVER")
            questions.forEach(::appendLine)
            appendLine()
            appendLine("PRAY")
            appendLine("God, help us receive what is true, practice it with love, and remember it when the week becomes difficult. Amen.")
            appendLine()
            appendLine("ONE ACTION")
            appendLine("Choose one small, specific response and tell someone you trust.")
            appendLine()
            appendLine("Shared from MANNA. This pack contains no private journal or prayer data.")
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MANNA Pack · ${note.title}")
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share this MANNA Pack"))
    }
}
