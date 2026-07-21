package com.prayerkey.manna.data

/**
 * The full translation catalog. Three sources, all free to the user:
 *  - OFFLINE : bundled in the APK, works on a plane (KJV)
 *  - FREE    : public-domain text fetched from bible-api.com — always works
 *  - PREMIUM : modern licensed translations served through prayerkey.com
 */
enum class VersionSource { OFFLINE, FREE, PREMIUM }

data class BibleVersion(
    val id: String,        // "KJV", "WEB", …
    val name: String,      // "King James Version"
    val tagline: String,   // short human line for the picker
    val source: VersionSource,
    val apiId: String = "",// bible-api.com translation id for FREE versions
)

val BIBLE_VERSIONS = listOf(
    BibleVersion("KJV",   "King James Version",     "The classic. Works offline.",      VersionSource.OFFLINE),
    BibleVersion("WEB",   "World English Bible",    "Modern English, public domain.",   VersionSource.FREE, "web"),
    BibleVersion("ASV",   "American Standard",      "Trusted 1901 revision.",           VersionSource.FREE, "asv"),
    BibleVersion("BBE",   "Bible in Basic English", "Simple words, easy reading.",      VersionSource.FREE, "bbe"),
    BibleVersion("DARBY", "Darby Translation",      "Precise, literal rendering.",      VersionSource.FREE, "darby"),
    BibleVersion("YLT",   "Young's Literal",        "Word-for-word from the original.", VersionSource.FREE, "ylt"),
    BibleVersion("NIV",   "New International",      "Most-read modern English.",        VersionSource.PREMIUM),
    BibleVersion("ESV",   "English Standard",       "Word-for-word, modern.",           VersionSource.PREMIUM),
    BibleVersion("NLT",   "New Living Translation", "Warm, thought-for-thought.",       VersionSource.PREMIUM),
    BibleVersion("NKJV",  "New King James",         "KJV beauty, modern words.",        VersionSource.PREMIUM),
    BibleVersion("NASB",  "New American Standard",  "Scholars' precision.",             VersionSource.PREMIUM),
    BibleVersion("AMP",   "Amplified Bible",        "Every shade of meaning.",          VersionSource.PREMIUM),
    BibleVersion("CSB",   "Christian Standard",     "Faithful and readable.",           VersionSource.PREMIUM),
    BibleVersion("MSG",   "The Message",            "Scripture in street clothes.",     VersionSource.PREMIUM),
)

val VERSION_MAP = BIBLE_VERSIONS.associateBy { it.id }
fun versionOf(id: String): BibleVersion = VERSION_MAP[id] ?: BIBLE_VERSIONS.first()
