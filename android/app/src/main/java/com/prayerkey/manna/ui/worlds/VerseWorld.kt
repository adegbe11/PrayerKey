package com.prayerkey.manna.ui.worlds

import androidx.compose.ui.graphics.Color

/**
 * Every verse gets a world.
 *
 * A deck where every card is the same cream rectangle stops rewarding you
 * after about three pulls — there is nothing left to predict, so there is
 * nothing to anticipate. Giving each verse a scene drawn from its own
 * imagery restores the variance, and it doubles as a memory aid: people
 * remember "the storm one" long after they have forgotten Mark 4:39.
 *
 * Assignment is keyword scoring over the verse text, done on device. No
 * asset files, no network, no per-verse authoring for 31,000 verses.
 */
enum class VerseWorld(
    val label: String,
    /** Sky colours, top to bottom. */
    val sky: List<Color>,
    /** Ink for the verse text. Scenes are dark enough for white almost always. */
    val onScene: Color = Color.White,
) {
    STARFIELD(
        "Under the stars",
        listOf(Color(0xFF0A1030), Color(0xFF141B45), Color(0xFF241F4A), Color(0xFF171226)),
    ),
    PASTURE(
        "Green pastures",
        listOf(Color(0xFF7FB2D9), Color(0xFFBCD8E8), Color(0xFFDFEAD2), Color(0xFF3F6B38)),
    ),
    SEA(
        "The storm",
        listOf(Color(0xFF1A2733), Color(0xFF233846), Color(0xFF16303C), Color(0xFF0C1E26)),
    ),
    HEIGHTS(
        "Eagle heights",
        listOf(Color(0xFFF2B26A), Color(0xFFE0805A), Color(0xFF8A4B57), Color(0xFF3A2742)),
    ),
    DAWN(
        "New every morning",
        listOf(Color(0xFF2A2140), Color(0xFF7A4A5C), Color(0xFFD98D5F), Color(0xFFC9A06A)),
    ),
    WATCH(
        "The night watch",
        listOf(Color(0xFF05060F), Color(0xFF101226), Color(0xFF1A1430), Color(0xFF120E22)),
    ),
    FIRE(
        "Through the fire",
        listOf(Color(0xFF1A0D08), Color(0xFF4A1D0C), Color(0xFF8A3A12), Color(0xFF2A1206)),
    ),
    HARVEST(
        "The harvest",
        listOf(Color(0xFF8FB6D9), Color(0xFFE4D9A6), Color(0xFFCFA648), Color(0xFF6B4E1C)),
    ),
    RIVER(
        "Living water",
        listOf(Color(0xFF184A52), Color(0xFF2C7C82), Color(0xFF5FB3A8), Color(0xFF123A42)),
    ),
    WILDERNESS(
        "The wilderness",
        listOf(Color(0xFFE8C79A), Color(0xFFD9A46E), Color(0xFFA9743F), Color(0xFF5C3A1E)),
    ),
    CITY(
        "The city",
        listOf(Color(0xFF2B2350), Color(0xFF4A3A6B), Color(0xFF7A5A7C), Color(0xFF1B1430)),
    ),
    THRONE(
        "Before the throne",
        listOf(Color(0xFF2A1E52), Color(0xFF4B2F72), Color(0xFF7A4A86), Color(0xFF1A1030)),
    ),
    GARDEN(
        "The garden",
        listOf(Color(0xFF9BC7A8), Color(0xFF6FA87C), Color(0xFF3F7A52), Color(0xFF1E4430)),
    ),
    PROMISE(
        "A promise, sealed",
        listOf(Color(0xFFFFE9B0), Color(0xFFE8BD62), Color(0xFFA87A28), Color(0xFF6B4A12)),
        onScene = Color(0xFF3A2A08),
    ),
    ;

    val isPromise: Boolean get() = this == PROMISE
}

object WorldPicker {

    /** Scored keywords per world. Longer, rarer words score higher. */
    private val SIGNS: List<Pair<VerseWorld, List<String>>> = listOf(
        VerseWorld.STARFIELD to listOf("stars", "star", "heavens", "heaven", "moon", "constellation", "seed as the stars"),
        VerseWorld.PASTURE to listOf("pasture", "shepherd", "sheep", "flock", "lamb", "still waters", "green"),
        VerseWorld.SEA to listOf("sea", "waves", "storm", "tempest", "ship", "deep", "waters prevailed", "floods", "drown"),
        VerseWorld.HEIGHTS to listOf("eagle", "eagles", "wings", "mountain", "mountains", "hills", "rock", "high place", "soar"),
        VerseWorld.DAWN to listOf("morning", "dawn", "daybreak", "dayspring", "mercies", "new every", "sunrise", "manna"),
        VerseWorld.WATCH to listOf("night", "darkness", "sleep", "terror by night", "watch", "midnight", "shadow of death"),
        VerseWorld.FIRE to listOf("fire", "flame", "burn", "burning", "furnace", "refine", "coals", "kindle"),
        VerseWorld.HARVEST to listOf("harvest", "reap", "sow", "field", "fields", "wheat", "vineyard", "fruit of the", "labourers"),
        VerseWorld.RIVER to listOf("river", "rivers", "fountain", "springs", "well", "thirst", "living water", "streams", "brook"),
        VerseWorld.WILDERNESS to listOf("wilderness", "desert", "dry", "drought", "valley", "barren", "parched", "wander"),
        VerseWorld.CITY to listOf("city", "cities", "zion", "jerusalem", "gates", "walls", "tabernacle", "temple", "streets"),
        VerseWorld.THRONE to listOf("throne", "king", "kingdom", "glory", "holy, holy", "majesty", "worship", "reign", "crown"),
        VerseWorld.GARDEN to listOf("garden", "vine", "branches", "tree", "trees", "planted", "root", "olive", "fig"),
    )

    /**
     * References that are outright promises. These get the gold card.
     *
     * Rarity here is deliberate and meaningful rather than random: a slot
     * machine pays out on a dice roll, this pays out when God actually made
     * a promise. It stays rare, and you can say what it is out loud.
     */
    private val PROMISES = setOf(
        "jeremiah 29:11", "isaiah 41:10", "romans 8:28", "philippians 4:13", "philippians 4:19",
        "joshua 1:9", "psalm 23:1", "isaiah 40:31", "matthew 11:28", "john 3:16",
        "proverbs 3:5", "proverbs 3:6", "2 corinthians 12:9", "isaiah 43:2", "isaiah 43:19",
        "deuteronomy 31:6", "hebrews 13:5", "1 peter 5:7", "psalm 46:1", "psalm 91:1",
        "lamentations 3:22", "lamentations 3:23", "numbers 6:24", "john 14:27", "matthew 28:20",
        "ephesians 3:20", "romans 8:38", "psalm 121:1", "psalm 121:2", "isaiah 26:3",
    )

    private fun isPromise(reference: String): Boolean {
        val key = reference.lowercase().trim()
        if (key in PROMISES) return true
        // "Lamentations 3:22-23" should still match its first verse
        val head = key.substringBefore('-').trim()
        return head in PROMISES
    }

    /**
     * Picks a world for a verse. Falls back deterministically on the
     * reference so a verse with no strong imagery still gets a stable scene
     * rather than a different one every time it appears.
     */
    fun forVerse(reference: String, text: String): VerseWorld {
        if (isPromise(reference)) return VerseWorld.PROMISE

        val low = text.lowercase()
        // whole words only: raw contains() matched "sea" inside "season" and
        // "star" inside "started", which sent verses to the wrong world
        val tokens = low.split(Regex("[^a-z']+")).filter { it.isNotEmpty() }.toHashSet()

        var best: VerseWorld? = null
        var bestScore = 0
        for ((world, words) in SIGNS) {
            var score = 0
            for (w in words) {
                if (w.contains(' ')) {
                    // a phrase is a much stronger signal than a lone noun
                    if (low.contains(w)) score += 6
                } else if (w in tokens || (w + "s") in tokens) {
                    score += 4
                }
            }
            if (score > bestScore) { bestScore = score; best = world }
        }
        // one solid keyword is enough; below that the verse has no imagery
        if (best != null && bestScore >= 4) return best

        // stable fallback: hash the reference, never Random
        val pool = VerseWorld.entries.filter { !it.isPromise }
        val h = reference.fold(7) { acc, c -> acc * 31 + c.code } and 0x7fffffff
        return pool[h % pool.size]
    }
}
