/**
 * Bakes the prayer deck into the Android app as an offline asset.
 *
 * The deck was the only part of Manna that needed a network: with no signal
 * the fetch threw and the screen span forever. Everything else — the KJV
 * text, the journal, the sermon listener — already works on the device, so
 * the deck ships with it too.
 *
 * The shape here is exactly what /api/prayer/topics returns, and it reads
 * the same PRAYER_TOPICS source, so the two cannot drift. Re-run whenever
 * the topic list changes:
 *
 *   node scripts/build-prayer-topics-asset.mjs
 *
 * jiti is the TypeScript loader Next already depends on, so this needs no
 * extra dev dependency.
 */
import { writeFileSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
// jiti ships as CommonJS, so it arrives on the default export
import jitiPkg from "jiti";

const createJiti = jitiPkg.createJiti ?? jitiPkg;
const jiti = createJiti(import.meta.url, { interopDefault: true });
const { PRAYER_TOPICS } = await jiti.import("../lib/seo/prayer-topics.ts");

const OUT = resolve("android/app/src/main/assets/prayer-topics.json");

const topics = PRAYER_TOPICS.map(
  ({ slug, title, category, samplePrayer, scripture, prayerPoints, howToPray }) => ({
    slug,
    title,
    category,
    prayer: samplePrayer,
    scripture,
    prayerPoints: prayerPoints ?? [],
    guide: howToPray ?? [],
  }),
);

mkdirSync(dirname(OUT), { recursive: true });
writeFileSync(
  OUT,
  JSON.stringify({
    topics,
    categories: [...new Set(PRAYER_TOPICS.map((t) => t.category))].sort(),
    total: topics.length,
  }),
);

console.log(`wrote ${topics.length} topics -> ${OUT}`);
