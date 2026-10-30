/**
 * Static KJV Bible engine — zero APIs, zero cost, works offline.
 * Public-domain King James text (31,100 verses) bundled at build time.
 * Powers /api/bible/search, /api/bible/cross-refs, verse pages, and Live Sermon.
 */

import kjvData from "@/lib/bible-data/kjv.json";
import { BIBLE_BOOKS, type BibleBook } from "@/lib/seo/bible-books";
import { findSpokenRefs, normalizeRefQuery } from "@/lib/bible/spoken-refs";
import { MODERN_QUOTES } from "@/lib/bible/modern-quotes";

type KjvBook = { abbrev: string; chapters: string[][] };
const KJV = kjvData as KjvBook[];

/* Books are in standard order in both datasets — zip by index.
   Source uses {braces} for two things: words the translators supplied
   ({is}), which stay in the text, and margin notes ({firmament: Heb.
   expansion}), which always contain a colon and are dropped. */
const clean = (s: string) =>
  s.replace(/\s*\{[^{}]*:[^{}]*\}/g, "")
    .replace(/[{}]/g, "")
    .replace(/\s+([,.;:!?])/g, "$1")
    .replace(/\s{2,}/g, " ")
    .trim();
const TEXT_BY_SLUG = new Map<string, string[][]>(
  BIBLE_BOOKS.map((b, i) => [
    b.slug,
    (KJV[i]?.chapters ?? []).map((ch) => ch.map(clean)),
  ])
);

/* ── Book name resolution ──────────────────────────────────────────
   Accepts: "john", "jn", "1 corinthians", "first corinthians",
   "psalm"/"psalms", "song of songs", common abbreviations. */
const ALIASES: Record<string, string> = {
  "psalm": "psalms", "song of songs": "song-of-solomon", "songs": "song-of-solomon",
  "canticles": "song-of-solomon", "revelations": "revelation", "apocalypse": "revelation",
};

function normBookName(s: string): string {
  return s.toLowerCase().trim()
    .replace(/^(first|1st)\s+/, "1 ")
    .replace(/^(second|2nd)\s+/, "2 ")
    .replace(/^(third|3rd)\s+/, "3 ")
    .replace(/\s+/g, " ");
}

function findBook(raw: string): BibleBook | null {
  let q = normBookName(raw);
  if (ALIASES[q]) q = ALIASES[q];
  const slugQ = q.replace(/\s+/g, "-");
  for (const b of BIBLE_BOOKS) {
    if (b.slug === slugQ) return b;
    if (b.name.toLowerCase() === q) return b;
    if (b.abbr.toLowerCase().replace(/\s+/g, " ") === q) return b;
  }
  // prefix match ("gen" → genesis, "phil" → philippians before philemon by order)
  for (const b of BIBLE_BOOKS) {
    if (b.name.toLowerCase().startsWith(q) && q.length >= 3) return b;
  }
  return null;
}

/* ── Verse access ─────────────────────────────────────────────────── */
export function getVerseText(slug: string, chapter: number, verse: number): string | null {
  const chapters = TEXT_BY_SLUG.get(slug);
  return chapters?.[chapter - 1]?.[verse - 1] ?? null;
}

export interface VerseHit { ref: string; text: string; match: "direct" | "semantic" }

/* ── Reference parsing: "John 3:16", "john 3:16-18", "Psalm 23" ───── */
const REF_RE = /^((?:[123]|first|second|third|1st|2nd|3rd)?\s*[a-z][a-z\s]+?)\s*(\d{1,3})(?:\s*[:.]\s*(\d{1,3})(?:\s*[-–]\s*(\d{1,3}))?)?$/i;

export function parseQueryRef(query: string): VerseHit[] | null {
  const m = query.trim().match(REF_RE);
  if (!m) return null;
  const book = findBook(m[1]);
  if (!book) return null;

  const chapter = parseInt(m[2], 10);
  if (chapter < 1 || chapter > book.chapters) return null;
  const chapterVerses = TEXT_BY_SLUG.get(book.slug)?.[chapter - 1];
  if (!chapterVerses) return null;

  const out: VerseHit[] = [];
  if (m[3]) {
    const from = parseInt(m[3], 10);
    const to   = m[4] ? Math.min(parseInt(m[4], 10), from + 4) : from;
    for (let v = from; v <= to && v <= chapterVerses.length; v++) {
      out.push({ ref: `${book.name} ${chapter}:${v}`, text: chapterVerses[v - 1], match: "direct" });
    }
  } else {
    // chapter only → first 5 verses
    for (let v = 1; v <= Math.min(5, chapterVerses.length); v++) {
      out.push({ ref: `${book.name} ${chapter}:${v}`, text: chapterVerses[v - 1], match: "direct" });
    }
  }
  return out.length ? out : null;
}

/* ── Keyword / phrase search across all 31,100 verses ─────────────── */
const STOP = new Set(["the","and","of","to","a","in","that","he","shall","unto","for","i","his","they","be","is","him","not","them","it","with","all","thou","thy","was","my","which","me","said","but","ye","their","have","will","thee","from","as","are","when","this","out","were","upon","man","you","we","she","her","who","what","there","been","on","at","by","an","or","so","do","did","has","had","your","our","us"]);

function significantWords(text: string): string[] {
  return text.toLowerCase().replace(/[^a-z\s]/g, " ").split(/\s+/)
    .filter((w) => w.length > 2 && !STOP.has(w));
}

export function searchBible(query: string, limit = 5): VerseHit[] {
  // 1. exact reference? ("John 3 16" and "John three sixteen" read as John 3:16)
  query = normalizeRefQuery(query);
  const refHits = parseQueryRef(query);
  if (refHits) return refHits.slice(0, limit);

  // 2. keyword scoring
  const terms = [...new Set(significantWords(query))].slice(0, 12);
  if (!terms.length) return [];
  const phrase = query.toLowerCase().replace(/[^a-z\s]/g, " ").replace(/\s+/g, " ").trim();
  // word-pair sequences (stopwords included) — catches quoted scripture
  // even when the wording differs slightly ("fear not" vs "fear thou not")
  const qWords  = phrase.split(" ");
  const bigrams = qWords.slice(0, -1).map((w, i) => `${w} ${qWords[i + 1]}`).filter((b) => b.length > 5);

  const scored: { score: number; hit: VerseHit }[] = [];
  for (const book of BIBLE_BOOKS) {
    const chapters = TEXT_BY_SLUG.get(book.slug)!;
    for (let c = 0; c < chapters.length; c++) {
      const verses = chapters[c];
      for (let v = 0; v < verses.length; v++) {
        const lower = verses[v].toLowerCase();
        let score = 0;
        for (const t of terms) if (lower.includes(t)) score += 1;
        if (score === 0) continue;
        if (score === terms.length) score += 2;               // all terms present
        if (phrase.length > 12 && lower.includes(phrase)) score += 6; // exact phrase
        for (const bg of bigrams) if (lower.includes(bg)) score += 1.5; // quoted-sequence bonus
        score += Math.min(1, (score * 40) / lower.length);    // prefer concise, focused verses
        scored.push({
          score,
          hit: { ref: `${book.name} ${c + 1}:${v + 1}`, text: verses[v], match: "semantic" },
        });
      }
    }
  }
  scored.sort((a, b) => b.score - a.score);
  return scored.slice(0, limit).map((s) => s.hit);
}

/* ── Cross-references: verses sharing the most significant words ──── */
export interface CrossRefHit { ref: string; text: string; reason: string }

export function getCrossRefs(slug: string, chapter: number, verse: number, limit = 5): CrossRefHit[] {
  const source = getVerseText(slug, chapter, verse);
  if (!source) return [];
  const words = [...new Set(significantWords(source))];
  if (words.length < 2) return [];

  const srcBook = BIBLE_BOOKS.find((b) => b.slug === slug);
  const scored: { score: number; hit: CrossRefHit }[] = [];

  for (const book of BIBLE_BOOKS) {
    const chapters = TEXT_BY_SLUG.get(book.slug)!;
    for (let c = 0; c < chapters.length; c++) {
      const verses = chapters[c];
      for (let v = 0; v < verses.length; v++) {
        // skip the verse itself and immediate neighbours in the same chapter
        if (book.slug === slug && c === chapter - 1 && Math.abs(v - (verse - 1)) <= 1) continue;
        const lower = verses[v].toLowerCase();
        const shared = words.filter((w) => lower.includes(w));
        if (shared.length < 2) continue;
        scored.push({
          score: shared.length + (book.slug !== slug ? 0.5 : 0), // prefer cross-book links
          hit: {
            ref: `${book.name} ${c + 1}:${v + 1}`,
            text: verses[v],
            reason: `Shares key themes with ${srcBook?.name ?? slug} ${chapter}:${verse}: ${shared.slice(0, 3).join(", ")}`,
          },
        });
      }
    }
  }
  scored.sort((a, b) => b.score - a.score);
  return scored.slice(0, limit).map((s) => s.hit);
}

/* ── Live sermon: detect a verse inside a preaching transcript ──────
   Two detection paths, both confidence-scored so ordinary preaching
   never flashes random verses on the projector:
   1. Spoken reference  → "John chapter 3 verse 16" (confidence 1.0)
   2. Quoted scripture  → consecutive word-sequences shared with a
      verse; rejected below a strict bigram threshold.            */

export interface DetectionResult {
  detected: boolean;
  ref: string;
  text: string;
  confidence: number; // 0–1
}

const NO_DETECTION: DetectionResult = { detected: false, ref: "", text: "", confidence: 0 };

/** `quotesOnly`: the caller reads spoken references itself (the live page), so
 *  a reference said a few seconds ago must not answer for a quote said now. */
export function detectVerseInTranscript(transcript: string, { quotesOnly = false } = {}): DetectionResult {
  // 1. spoken references: "John chapter 3 verse 16", "John three sixteen",
  //    "first Corinthians 13 4". The most recent one wins.
  const spokenRefs = quotesOnly ? [] : findSpokenRefs(transcript);
  const said = spokenRefs[spokenRefs.length - 1];
  if (said) {
    const verse = said.verse ?? 1;
    const text = getVerseText(said.book.slug, said.chapter, verse);
    if (text) {
      return {
        detected: true,
        ref: `${said.book.name} ${said.chapter}:${verse}`,
        text,
        // a chapter on its own ("turn to Psalm 23") is a good guess, not a certainty
        confidence: said.verse === null ? 0.85 : 1.0,
      };
    }
  }

  // 2. quoted scripture, in King James words or the modern words most preachers use
  const kjv = detectKjvQuote(transcript);
  const modern = detectModernQuote(transcript);
  if (kjv.detected && (!modern.detected || kjv.confidence >= modern.confidence)) return kjv;
  return modern.detected ? modern : kjv;
}

/* Every verse as bare lowercase words, built once: "fear; but" in the text
   has to match "fear but" as it is spoken. */
let BARE: { ref: string; text: string; bare: string }[] | null = null;
function bareVerses() {
  if (!BARE) {
    BARE = [];
    for (const book of BIBLE_BOOKS) {
      const chapters = TEXT_BY_SLUG.get(book.slug)!;
      chapters.forEach((verses, c) => verses.forEach((text, v) => {
        BARE!.push({ ref: `${book.name} ${c + 1}:${v + 1}`, text, bare: ` ${words(text).join(" ")} ` });
      }));
    }
  }
  return BARE;
}

const meaningful = (w: string) => w.length > 2 && !STOP.has(w);

/** How strongly the last words said match one King James verse: the number of
 *  word-pairs said in order that also sit side by side in the verse. Pairs of
 *  small words ("to the", "and say") are everywhere, so a pair only counts
 *  when it holds a word that means something. */
export function kjvQuoteScore(transcript: string): { score: number; ref: string; text: string } | null {
  const said = words(transcript).slice(-30);
  if (said.length < 5) return null;
  const seen = new Set<string>();
  const pairs: { pair: string; recent: boolean }[] = [];
  said.slice(0, -1).forEach((w, i) => {
    const pair = ` ${w} ${said[i + 1]} `;
    if (seen.has(pair) || !(meaningful(w) || meaningful(said[i + 1]))) return;
    seen.add(pair);
    // a quote just said sits at the end; matches scattered across older talk do not
    pairs.push({ pair, recent: i >= said.length - 14 });
  });
  if (pairs.length < 3) return null;
  let best: { score: number; ref: string; text: string } | null = null;
  for (const v of bareVerses()) {
    let hits = 0, recent = 0;
    for (const p of pairs) if (v.bare.includes(p.pair)) { hits++; if (p.recent) recent++; }
    if (hits >= 3 && recent >= 2 && (!best || hits > best.score)) best = { score: hits, ref: v.ref, text: v.text };
  }
  return best;
}

/* Quoted scripture in King James words. */
function detectKjvQuote(transcript: string): DetectionResult {
  const best = kjvQuoteScore(transcript);
  if (!best || best.score < KJV_MIN_PAIRS) return NO_DETECTION;
  // 5 waits in preview for the operator; 6 or more is clear enough to put on air
  const confidence = best.score >= KJV_MIN_PAIRS + 2 ? 0.95 : best.score >= KJV_MIN_PAIRS + 1 ? 0.85 : 0.7;
  return { detected: true, ref: best.ref, text: best.text, confidence };
}
/* Measured on real quotes (5 to 11 meaningful pairs) and on ordinary preaching
   (never above 4); scripts/check-live-detection.ts keeps both honest. */
const KJV_MIN_PAIRS = 5;

/* Quoted scripture in modern words (NIV, ESV, NLT, NKJV): recognised from a
   short phrase list, then shown in King James words. */
const words = (s: string) =>
  s.toLowerCase().replace(/[’']/g, "").replace(/[^a-z\s]/g, " ").replace(/\s+/g, " ").trim().split(" ").filter(Boolean);

const MODERN = MODERN_QUOTES.flatMap(([ref, phrases]) => {
  const r = findSpokenRefs(ref)[0];
  const text = r && r.verse !== null ? getVerseText(r.book.slug, r.chapter, r.verse) : null;
  if (!r || !text) return [];
  return phrases.map((p) => {
    const w = words(p);
    return { ref: `${r.book.name} ${r.chapter}:${r.verse}`, text, phrase: w.join(" "), pairs: w.slice(0, -1).map((x, i) => `${x} ${w[i + 1]}`) };
  });
});

function detectModernQuote(transcript: string): DetectionResult {
  const said = words(transcript).slice(-40);
  if (said.length < 2) return NO_DETECTION; // "Jesus wept" is a whole verse
  const saidText = ` ${said.join(" ")} `;
  const saidPairs = new Set(said.slice(0, -1).map((x, i) => `${x} ${said[i + 1]}`));
  let best: { score: number; length: number; ref: string; text: string } | null = null;
  for (const m of MODERN) {
    let score: number;
    if (m.pairs.length < 5) {
      // short phrases must be said exactly, or ordinary talk would match them
      score = saidText.includes(` ${m.phrase} `) ? 1 : 0;
    } else {
      const hits = m.pairs.filter((p) => saidPairs.has(p)).length;
      score = hits >= 4 ? hits / m.pairs.length : 0;
    }
    if (score < 0.7) continue;
    if (!best || score > best.score || (score === best.score && m.pairs.length > best.length)) {
      best = { score, length: m.pairs.length, ref: m.ref, text: m.text };
    }
  }
  if (!best) return NO_DETECTION;
  const confidence = best.score >= 0.99 && best.length >= 5 ? 0.95 : best.score >= 0.85 ? 0.9 : 0.8;
  return { detected: true, ref: best.ref, text: best.text, confidence };
}
