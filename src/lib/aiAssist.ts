'use client';

import type { BookData } from '@/types';
import { groqChat, DEFAULT_GROQ_MODEL } from './groq';
import { generateBlurb as heuristicBlurb } from './blurbs';

interface BaseOpts {
  apiKey?: string;
  model?: string;
}

function firstWordsOfBook(bookData: BookData, maxChars = 6000): string {
  let out = '';
  for (const ch of bookData.chapters) {
    const plain = ch.content.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
    out += `## ${ch.title}\n${plain}\n\n`;
    if (out.length > maxChars) break;
  }
  return out.slice(0, maxChars);
}

function safeJson<T>(raw: string): T | null {
  const cleaned = raw
    .replace(/^```(?:json)?/i, '')
    .replace(/```\s*$/i, '')
    .trim();
  try {
    return JSON.parse(cleaned) as T;
  } catch {
    // find first { ... } or [ ... ] segment
    const m = cleaned.match(/[\[{][\s\S]*[\]}]/);
    if (m) {
      try { return JSON.parse(m[0]) as T; } catch { return null; }
    }
    return null;
  }
}

export interface BlurbResult {
  tagline: string;
  blurb: string;
  subtitleSuggestion?: string;
}

export async function generateBlurbSmart(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<BlurbResult> {
  if (!opts.apiKey) {
    return heuristicBlurb(bookData);
  }
  const excerpt = firstWordsOfBook(bookData, 5000);
  const system = `You are a senior book publicist writing back-cover copy. Output must be JSON with keys "tagline" (≤10 words), "blurb" (3 short paragraphs, evocative, no spoilers, 90–140 words total), and "subtitleSuggestion" (≤8 words, optional). Never mention the word "tagline" or "blurb" in the text. Do not quote the book; write original copy inspired by it.`;
  const user = `Title: ${bookData.title}\nAuthor: ${bookData.author}\nGenre: ${bookData.metadata.genre ?? bookData.genre ?? 'fiction'}\n\nManuscript excerpt:\n${excerpt}`;

  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.7,
    maxTokens: 600,
    responseFormat: 'json_object',
  });

  const parsed = safeJson<BlurbResult>(raw);
  if (!parsed?.tagline || !parsed?.blurb) {
    return heuristicBlurb(bookData);
  }
  return parsed;
}

export interface TitleSuggestions {
  titles: string[];
  subtitles: string[];
}

export async function suggestTitles(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<TitleSuggestions> {
  if (!opts.apiKey) {
    // Weak heuristic fallback
    return {
      titles: [bookData.title, `${bookData.title}: A Story`].slice(0, 2),
      subtitles: bookData.subtitle ? [bookData.subtitle] : ['A Novel', 'A Memoir'],
    };
  }

  const excerpt = firstWordsOfBook(bookData, 4000);
  const system = `You are a bestselling title doctor. Output JSON with keys "titles" (array of 6 strong title candidates, each ≤8 words) and "subtitles" (array of 4 candidates, each ≤10 words). Titles should be specific, memorable, commercially viable. No duplicates. No numbers.`;
  const user = `Current title: ${bookData.title}\nAuthor: ${bookData.author}\nGenre: ${bookData.metadata.genre ?? 'fiction'}\n\nExcerpt:\n${excerpt}`;

  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.85,
    maxTokens: 500,
    responseFormat: 'json_object',
  });

  const parsed = safeJson<TitleSuggestions>(raw);
  return {
    titles: Array.isArray(parsed?.titles) ? parsed!.titles.slice(0, 6) : [],
    subtitles: Array.isArray(parsed?.subtitles) ? parsed!.subtitles.slice(0, 4) : [],
  };
}

export interface ChapterTitleMap {
  [chapterId: string]: string;
}

/**
 * Generate nicer chapter titles for chapters that are generic (e.g. "Chapter 1").
 * Returns a map of chapterId → new title. Chapters with non-generic titles are skipped.
 */
export async function polishChapterTitles(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<ChapterTitleMap> {
  if (!opts.apiKey) return {};

  // Build list of chapters needing polish (generic name like "Chapter 1")
  const generic = bookData.chapters.filter((c) =>
    /^(chapter\s+\d+|chapter\s+[ivxlcdm]+|chapter\s+(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty))$/i.test(
      c.title.trim()
    )
  );
  if (!generic.length) return {};

  const payload = generic
    .map((c) => {
      const plain = c.content.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
      return `ID: ${c.id}\nCurrent: ${c.title}\nExcerpt: ${plain.slice(0, 800)}`;
    })
    .join('\n---\n')
    .slice(0, 9000);

  const system = `You are a book editor. For each chapter, invent a concise chapter title (3–8 words) that evokes the content without spoilers. Output JSON object: { "titles": [{ "id": "<chapter-id>", "title": "<new-title>" }, ...] }. Do not include chapters that should keep their current title.`;
  const user = `Book title: ${bookData.title}\nGenre: ${bookData.metadata.genre ?? 'fiction'}\n\nChapters:\n${payload}`;

  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.7,
    maxTokens: 900,
    responseFormat: 'json_object',
  });

  const parsed = safeJson<{ titles: { id: string; title: string }[] }>(raw);
  const out: ChapterTitleMap = {};
  if (parsed?.titles) {
    for (const entry of parsed.titles) {
      if (entry?.id && entry?.title && typeof entry.title === 'string') {
        out[entry.id] = entry.title.trim().replace(/^["'\u201C\u2018]+|["'\u201D\u2019]+$/g, '');
      }
    }
  }
  return out;
}

export async function generateDedication(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<string> {
  if (!opts.apiKey) return '';
  const system = `Write a single heartfelt book dedication, 8–20 words, tasteful and warm. No quotes. No "Dedicated to". Output only the dedication text.`;
  const user = `Title: ${bookData.title}\nGenre: ${bookData.metadata.genre ?? 'fiction'}`;
  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.85,
    maxTokens: 120,
  });
  return raw.replace(/^["'\u201C\u2018]+|["'\u201D\u2019]+$/g, '').trim();
}

export async function improveText(
  original: string,
  opts: BaseOpts & { instruction?: string } = {}
): Promise<string> {
  if (!opts.apiKey) throw new Error('Add your Groq API key first');
  const trimmed = original.trim();
  if (!trimmed) return original;

  const instruction = opts.instruction?.trim() || 'Rewrite to be more vivid and publishable while preserving meaning, voice, names, quoted dialogue, and POV. Keep length similar.';
  const system = `You are a seasoned line editor. ${instruction} Output ONLY the rewritten text — no preamble, no explanation, no quotes around it.`;
  const user = `Original:\n${trimmed}\n\nRewritten version:`;

  const out = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.5,
    maxTokens: Math.max(400, Math.ceil(trimmed.length / 2)),
  });

  return out
    .replace(/^```[a-z]*\n?|\n?```$/gi, '')
    .replace(/^["\u201C]|["\u201D]$/g, '')
    .trim();
}

export interface ExtractedCharacter {
  name: string;
  role: string;
  description: string;
  firstAppearsIn?: string;
}

export async function extractCharacters(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<ExtractedCharacter[]> {
  if (!opts.apiKey) throw new Error('Add your Groq API key first');
  const excerpt = firstWordsOfBook(bookData, 9000);
  const system = `You are a literary editor. Extract the cast of characters from this manuscript. Output strict JSON: { "characters": [{ "name": "<name>", "role": "protagonist|antagonist|supporting|minor", "description": "<15-word profile>", "firstAppearsIn": "<chapter title>" }, ...] }. Only include actual named or clearly identified characters. Merge duplicates. Max 25 entries.`;
  const user = `Title: ${bookData.title}\n\nManuscript:\n${excerpt}`;

  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.3,
    maxTokens: 1400,
    responseFormat: 'json_object',
  });

  const parsed = safeJson<{ characters: ExtractedCharacter[] }>(raw);
  return Array.isArray(parsed?.characters) ? parsed!.characters.slice(0, 25) : [];
}

export type GrammarSeverity = 'style' | 'grammar' | 'spelling' | 'clarity';

export interface GrammarIssue {
  excerpt: string;         // the offending snippet as it appears in the text
  suggestion: string;      // the proposed rewrite
  reason: string;          // 1-sentence explanation
  severity: GrammarSeverity;
  chapterTitle?: string;
}

export async function grammarCheck(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<GrammarIssue[]> {
  if (!opts.apiKey) throw new Error('Add your Groq API key first');
  const excerpt = firstWordsOfBook(bookData, 8000);
  const system = `You are a professional copy editor. Find grammar, spelling, clarity, and style issues. Output JSON: { "issues": [{ "excerpt": "<exact short snippet from text>", "suggestion": "<rewrite>", "reason": "<why>", "severity": "grammar|spelling|clarity|style", "chapterTitle": "<title>" }, ...] }. Prioritize real errors over stylistic preferences. Max 30 issues. Keep excerpt under 20 words.`;
  const user = `Manuscript:\n${excerpt}`;

  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.2,
    maxTokens: 2000,
    responseFormat: 'json_object',
  });

  const parsed = safeJson<{ issues: GrammarIssue[] }>(raw);
  return Array.isArray(parsed?.issues) ? parsed!.issues.slice(0, 30) : [];
}

export async function generateEpigraph(
  bookData: BookData,
  opts: BaseOpts = {}
): Promise<{ text: string; attribution: string }> {
  if (!opts.apiKey) return { text: '', attribution: '' };
  const system = `Suggest a single existing public-domain quote that would make a great epigraph for this book. Output JSON: { "text": "<quote>", "attribution": "<author name, year-or-work>" }. Keep quote under 40 words. No made-up citations.`;
  const user = `Title: ${bookData.title}\nGenre: ${bookData.metadata.genre ?? 'fiction'}\nAuthor: ${bookData.author}`;
  const raw = await groqChat({
    apiKey: opts.apiKey,
    model: opts.model || DEFAULT_GROQ_MODEL,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    temperature: 0.7,
    maxTokens: 200,
    responseFormat: 'json_object',
  });
  const parsed = safeJson<{ text: string; attribution: string }>(raw);
  return {
    text: parsed?.text?.trim() || '',
    attribution: parsed?.attribution?.trim() || '',
  };
}
