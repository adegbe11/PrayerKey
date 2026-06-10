/**
 * Static KJV Bible engine — zero APIs, zero cost, works offline.
 * Public-domain King James text (31,100 verses) bundled at build time.
 * Powers /api/bible/search, /api/bible/cross-refs, verse pages, and Live Sermon.
 */

import kjvData from "@/lib/bible-data/kjv.json";
import { BIBLE_BOOKS, type BibleBook } from "@/lib/seo/bible-books";

type KjvBook = { abbrev: string; chapters: string[][] };
const KJV = kjvData as KjvBook[];

/* Books are in standard order in both datasets — zip by index.
   Source marks translator-added words with {braces} — strip them. */
const clean = (s: string) => s.replace(/[{}]/g, "");
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
  // 1. exact reference?
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

/* ── Live sermon: detect a quoted verse inside a transcript chunk ─── */
export function detectVerseInTranscript(transcript: string): VerseHit | null {
  // spoken references: "john chapter 3 verse 16", "john 3 16", "john 3:16"
  const spoken = transcript.toLowerCase()
    .replace(/\bchapter\b/g, " ")
    .replace(/\bverse\b/g, ":");
  const refMatch = spoken.match(/((?:[123]|first|second|third)?\s*[a-z]+)\s+(\d{1,3})\s*[:\s]\s*(\d{1,3})/);
  if (refMatch) {
    const book = findBook(refMatch[1]);
    if (book) {
      const text = getVerseText(book.slug, parseInt(refMatch[2], 10), parseInt(refMatch[3], 10));
      if (text) return { ref: `${book.name} ${refMatch[2]}:${refMatch[3]}`, text, match: "direct" };
    }
  }
  // quoted text: fall back to phrase search over the last words
  const hits = searchBible(transcript.split(/\s+/).slice(-25).join(" "), 1);
  return hits[0] ?? null;
}
