package com.prayerkey.manna.ui.church

/**
 * Languages the service can be captured in.
 *
 * The recogniser is the device's own, so the real limit is which packs the
 * phone has installed — Android ships a lot and downloads more on demand.
 * [SermonArranger.hasCuesFor] says which of these also get the sharper,
 * signpost-aware arranging; the rest still get a full transcript, the
 * scriptures and a note built from the language-agnostic core.
 */
data class SermonLanguage(val tag: String, val label: String)

val SERMON_LANGUAGES = listOf(
    SermonLanguage("en-US", "English (US)"),
    SermonLanguage("en-GB", "English (UK)"),
    SermonLanguage("en-NG", "English (Nigeria)"),
    SermonLanguage("en-ZA", "English (South Africa)"),
    SermonLanguage("en-IN", "English (India)"),
    SermonLanguage("es-ES", "Español"),
    SermonLanguage("es-MX", "Español (México)"),
    SermonLanguage("pt-BR", "Português (Brasil)"),
    SermonLanguage("fr-FR", "Français"),
    SermonLanguage("de-DE", "Deutsch"),
    SermonLanguage("it-IT", "Italiano"),
    SermonLanguage("nl-NL", "Nederlands"),
    SermonLanguage("pl-PL", "Polski"),
    SermonLanguage("ro-RO", "Română"),
    SermonLanguage("ru-RU", "Русский"),
    SermonLanguage("uk-UA", "Українська"),
    SermonLanguage("sw-KE", "Kiswahili"),
    SermonLanguage("am-ET", "አማርኛ"),
    SermonLanguage("yo-NG", "Yorùbá"),
    SermonLanguage("ig-NG", "Igbo"),
    SermonLanguage("ha-NG", "Hausa"),
    SermonLanguage("zu-ZA", "isiZulu"),
    SermonLanguage("af-ZA", "Afrikaans"),
    SermonLanguage("hi-IN", "हिन्दी"),
    SermonLanguage("ta-IN", "தமிழ்"),
    SermonLanguage("ml-IN", "മലയാളം"),
    SermonLanguage("tl-PH", "Filipino"),
    SermonLanguage("id-ID", "Bahasa Indonesia"),
    SermonLanguage("ko-KR", "한국어"),
    SermonLanguage("zh-CN", "中文"),
    SermonLanguage("ar-EG", "العربية"),
)

fun sermonLanguageLabel(tag: String): String =
    SERMON_LANGUAGES.firstOrNull { it.tag == tag }?.label ?: tag
