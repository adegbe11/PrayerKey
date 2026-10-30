/**
 * Bible references as people say them, turned into references we can look up.
 *
 * Speech recognition writes what the preacher says, not what is printed:
 * "John three sixteen", "John 3 16", "first Corinthians thirteen four",
 * "Psalm ninety-one verse one", "Romans chapter 8 verses 28 through 30".
 * This reads all of those as "John 3:16", "1 Corinthians 13:4", and so on,
 * checked against the real chapter and verse counts so a stray number never
 * becomes a verse that does not exist. Pure and small: safe in the browser.
 */

import { BIBLE_BOOKS, type BibleBook } from "@/lib/seo/bible-books";

export interface SpokenRef {
  book: BibleBook;
  chapter: number;
  verse: number | null;     // null when only a chapter was said
  verseEnd: number | null;
  /** "John 3:16", "Romans 8:28-30", "Psalms 23" */
  label: string;
  /** where it ended in the normalized text, to pick the most recent one */
  end: number;
}

/* ── Number words → digits ─────────────────────────────────────────── */
const UNITS: Record<string, number> = {
  zero: 0, one: 1, two: 2, three: 3, four: 4, five: 5, six: 6, seven: 7, eight: 8, nine: 9,
  ten: 10, eleven: 11, twelve: 12, thirteen: 13, fourteen: 14, fifteen: 15, sixteen: 16,
  seventeen: 17, eighteen: 18, nineteen: 19,
};
const TENS: Record<string, number> = {
  twenty: 20, thirty: 30, forty: 40, fourty: 40, fifty: 50, sixty: 60, seventy: 70, eighty: 80, ninety: 90,
};

/** "one hundred and nineteen" → "119", "twenty-eight" → "28"; other words untouched. */
export function numberWordsToDigits(text: string): string {
  const words = text.replace(/(\w)-(\w)/g, "$1 $2").split(/\s+/).filter(Boolean);
  const out: string[] = [];
  let current: number | null = null;
  let afterHundred = false;
  const flush = () => { if (current !== null) out.push(String(current)); current = null; afterHundred = false; };
  for (const raw of words) {
    const w = raw.toLowerCase().replace(/[^a-z]/g, "");
    const trail = raw.match(/[^a-zA-Z]+$/)?.[0] ?? "";
    const pureWord = /^[a-zA-Z]+[^a-zA-Z]*$/.test(raw);
    let used = false;
    if (pureWord && w in UNITS) {
      // a unit joins "twenty" or "one hundred" / "one hundred twenty"; otherwise it is a new number
      if (current !== null && ((current % 10 === 0 && current % 100 >= 20) || (current >= 100 && current % 100 === 0)) && UNITS[w] < 10 && UNITS[w] > 0) current += UNITS[w];
      else if (current !== null && current >= 100 && current % 100 === 0) current += UNITS[w];
      else { flush(); current = UNITS[w]; }
      used = true;
    } else if (pureWord && w in TENS) {
      if (current !== null && current >= 100 && current % 100 === 0) current += TENS[w];
      else { flush(); current = TENS[w]; }
      used = true;
    } else if (pureWord && w === "hundred") {
      if (current !== null && current >= 1 && current <= 9) current *= 100;
      else if (current === null && out[out.length - 1]?.toLowerCase() === "a") { out.pop(); current = 100; }
      else { flush(); out.push(raw); continue; }
      afterHundred = true; used = true;
    } else if (w === "and" && afterHundred && current !== null && current % 100 === 0 && !trail) {
      continue; // "a hundred and ten"
    }
    if (used) {
      if (trail) { const n = current; flush(); out[out.length - 1] = `${n}${trail}`; }
      continue;
    }
    flush();
    out.push(raw);
  }
  flush();
  return out.join(" ");
}

/* ── Book names as spoken ──────────────────────────────────────────── */
const ORDINAL = "(?:1|2|3|i{1,3}|first|second|third|1st|2nd|3rd|one|two|three)";
const ORD_VALUE: Record<string, number> = { "1": 1, i: 1, first: 1, "1st": 1, one: 1, "2": 2, ii: 2, second: 2, "2nd": 2, two: 2, "3": 3, iii: 3, third: 3, "3rd": 3, three: 3 };

/** spoken base name → slugs by number ("corinthians" → 1/2-corinthians) */
const BASES = new Map<string, { plain?: BibleBook; numbered: Record<number, BibleBook> }>();
for (const b of BIBLE_BOOKS) {
  const m = b.name.match(/^([123]) (.+)$/);
  const base = (m ? m[2] : b.name).toLowerCase();
  const entry = BASES.get(base) ?? { numbered: {} };
  if (m) entry.numbered[Number(m[1])] = b; else entry.plain = b;
  BASES.set(base, entry);
}
const EXTRA: Record<string, string> = {
  psalm: "psalms", "song of songs": "song of solomon", songs: "song of solomon",
  revelations: "revelation", "book of revelation": "revelation",
};
for (const [alias, target] of Object.entries(EXTRA)) BASES.set(alias, BASES.get(target)!);

/* Book names that are also everyday words ("my job", "mark my words", "acts of
   kindness", "in numbers"): only trusted with a verse, or with "chapter". */
const COMMON_WORDS = new Set(["job", "mark", "acts", "numbers", "james", "jude", "ruth", "titus", "amos", "joel", "kings", "song", "songs", "lamentations", "daniel", "peter", "john", "esther"]);

const BASE_ALT = [...BASES.keys()].sort((a, b) => b.length - a.length).map((s) => s.replace(/ /g, "\\s+")).join("|");
const SEP_VERSE = "(?:\\s*[:.]\\s*|\\s+verses?\\s+|\\s+vs\\.?\\s+|\\s+colon\\s+|\\s+)";
const SEP_RANGE = "(?:\\s*[-–]\\s*|\\s+(?:through|thru|to)\\s+)";
const REF_RE = new RegExp(
  `\\b(?:(${ORDINAL})\\s+)?(${BASE_ALT})\\s+(chapter\\s+)?(\\d{1,3})(?:${SEP_VERSE}(\\d{1,3})(?:${SEP_RANGE}(\\d{1,3}))?)?(?!\\d)`,
  "gi",
);

function resolveBook(ordinal: string | undefined, base: string): BibleBook | null {
  const entry = BASES.get(base.toLowerCase().replace(/\s+/g, " "));
  if (!entry) return null;
  if (ordinal) {
    const n = ORD_VALUE[ordinal.toLowerCase()];
    if (entry.numbered[n]) return entry.numbered[n];
    // "one John" said as a count, not a book number: fall back to the plain book
    return entry.plain ?? null;
  }
  return entry.plain ?? entry.numbered[1] ?? null;
}

/** Every reference spoken in `text`, in the order said. */
export function findSpokenRefs(text: string): SpokenRef[] {
  const norm = numberWordsToDigits(text);
  const refs: SpokenRef[] = [];
  for (const m of norm.matchAll(REF_RE)) {
    const [, ordinal, base, chapterWord, chStr, vStr, v2Str] = m;
    const book = resolveBook(ordinal, base);
    if (!book) continue;
    const chapter = Number(chStr);
    if (chapter < 1 || chapter > book.chapters) continue;
    const count = book.verses[chapter - 1];
    let verse = vStr ? Number(vStr) : null;
    let verseEnd = v2Str ? Number(v2Str) : null;
    // "John 3 and" — a second number that is not a real verse is just talk
    if (verse !== null && (verse < 1 || verse > count)) { verse = null; verseEnd = null; }
    if (verseEnd !== null && (verse === null || verseEnd <= verse || verseEnd > count)) verseEnd = null;
    const baseWord = base.toLowerCase().replace(/\s+/g, " ");
    if (verse === null && !chapterWord && !ordinal && COMMON_WORDS.has(baseWord)) continue;
    const label = `${book.name} ${chapter}${verse !== null ? `:${verse}${verseEnd !== null ? `-${verseEnd}` : ""}` : ""}`;
    refs.push({ book, chapter, verse, verseEnd, label, end: (m.index ?? 0) + m[0].length });
  }
  return refs;
}

/** A typed or spoken query rewritten as a plain reference when it is one. */
export function normalizeRefQuery(q: string): string {
  const refs = findSpokenRefs(q);
  if (refs.length !== 1) return q;
  // only when the reference is (nearly) the whole query, so "love is kind" stays a phrase search
  const rest = numberWordsToDigits(q).replace(REF_RE, "").replace(/[^a-z0-9]/gi, "");
  return rest.length <= 3 ? refs[0].label : q;
}
