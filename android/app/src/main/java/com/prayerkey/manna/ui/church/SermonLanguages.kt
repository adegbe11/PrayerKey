package com.prayerkey.manna.ui.church

/**
 * The Englishes the service can be captured in.
 *
 * This list held 31 languages — Yorùbá, Hausa, Español, 中文 and the rest —
 * and none of them worked. Captions come from the bundled Vosk model, which
 * is an English model; `SermonService.setLanguage` writes to a flow the
 * capture path never reads, so choosing Igbo produced English guesses at Igbo
 * speech. A picker that silently does nothing is worse than no picker, and a
 * church whose service is not in English would have found that out in the
 * middle of a sermon.
 *
 * What is left is true. One model decodes all of these — the accents differ,
 * the language does not — and the tag still tells [SermonArranger] which
 * signposts to look for when it builds the note.
 *
 * The other languages come back when a model for them ships, and not before.
 * sherpa-onnx has them; that is the migration this is waiting on.
 */
data class SermonLanguage(val tag: String, val label: String)

val SERMON_LANGUAGES = listOf(
    SermonLanguage("en-US", "English (US)"),
    SermonLanguage("en-GB", "English (UK)"),
    SermonLanguage("en-NG", "English (Nigeria)"),
    SermonLanguage("en-ZA", "English (South Africa)"),
    SermonLanguage("en-IN", "English (India)"),
    SermonLanguage("en-GH", "English (Ghana)"),
    SermonLanguage("en-KE", "English (Kenya)"),
    SermonLanguage("en-CA", "English (Canada)"),
    SermonLanguage("en-AU", "English (Australia)"),
    SermonLanguage("en-PH", "English (Philippines)"),
    SermonLanguage("en-CB", "English (Caribbean)"),
)

fun sermonLanguageLabel(tag: String): String =
    SERMON_LANGUAGES.firstOrNull { it.tag == tag }?.label ?: tag
