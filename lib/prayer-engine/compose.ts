/**
 * PrayerKey prayer composition — pure functions, runs entirely in the browser.
 * 1. Tokenise input  2. Score categories by keyword hits
 * 3. Stitch opening + personal line + topic fragments + closing
 * 4. Attach matching scriptures
 */

import { CATEGORIES, GENERAL, OPENINGS, PERSONAL, TRANSITIONS, CLOSINGS, type PrayerVerse } from "./data";

export interface ComposedPrayer {
  prayer: string;          // full prayer text, paragraphs joined with \n\n
  topics: string[];        // detected category labels
  verses: PrayerVerse[];   // attached scriptures (deduped)
}

const pick = <T,>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];

/** Score categories against the input; return up to 3 best-matching keys. */
export function detectCategories(text: string): string[] {
  const t = " " + text.toLowerCase().replace(/[^a-z0-9'\s]/g, " ") + " ";
  const scores: { key: string; s: number }[] = [];
  for (const key in CATEGORIES) {
    let s = 0;
    for (const kw of CATEGORIES[key].keywords) {
      const pattern = new RegExp("\\b" + kw.replace(/'/g, "'?") + "\\b", "g");
      const hits = (t.match(pattern) || []).length;
      if (hits) s += hits * (kw.includes(" ") ? 2 : 1);
    }
    if (s > 0) scores.push({ key, s });
  }
  scores.sort((a, b) => b.s - a.s);
  return scores.slice(0, 3).map((o) => o.key);
}

function cleanInput(raw: string): string {
  let s = raw.trim().replace(/\s+/g, " ");
  if (s.length > 180) s = s.slice(0, 180).replace(/\s\S*$/, "") + "…";
  s = s.charAt(0).toLowerCase() + s.slice(1);
  return s.replace(/[.!?,;]+$/, "");
}

/** Compose a complete prayer from free-text prayer points. */
export function composePrayer(raw: string): ComposedPrayer {
  const keys = detectCategories(raw);
  const cats = keys.length ? keys.map((k) => CATEGORIES[k]) : [GENERAL];

  const parts: string[] = [];
  parts.push(pick(OPENINGS));
  parts.push(pick(PERSONAL).replace("{x}", cleanInput(raw)));

  cats.forEach((c, i) => {
    let frag = pick(c.fragments);
    if (i > 0) frag = pick(TRANSITIONS) + " " + frag.charAt(0).toLowerCase() + frag.slice(1);
    parts.push(frag);
  });

  parts.push(pick(CLOSINGS));

  const used = new Set<string>();
  const verses: PrayerVerse[] = [];
  cats.forEach((c) => {
    const v = pick(c.verses);
    if (used.has(v.ref)) return;
    used.add(v.ref);
    verses.push(v);
  });

  return {
    prayer: parts.join("\n\n"),
    topics: cats.map((c) => c.label),
    verses,
  };
}
