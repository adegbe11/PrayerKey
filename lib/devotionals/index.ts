import data from "./devotionals.json";
import { getVerseText } from "@/lib/bible/kjv";
import { BIBLE_BOOKS } from "@/lib/seo/bible-books";

// The 366 written devotionals from the PrayerKey app: one verse and one
// reflection for every day of the year. The website and the app read the same
// day's devotional. Scripture is not stored here; every reference is read from
// the King James text, so a devotional can never quote a verse that isn't there.

type Ref = { book: string; chapter: number; verse: number };
type Block = { p: string } | { q: Ref };
type Raw = {
  day: number; title: string; minutes: number;
  read: Ref & { from: number; to: number };
  body: Block[];
  line: { a: string; b: string };
  after: string[];
  prayer: string;
  question: string;
  prayerCategory: string;
};
type Month = { month: number; name: string; theme: string; days: Raw[] };

const MONTHS = data as Month[];
export const MONTH_NAMES = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

export type Devotional = Raw & {
  month: number;
  monthName: string;
  series: string;
  theme: string;
  seriesOf: number;
  slug: string;              // "october-17"
  dateLabel: string;         // "October 17"
  verse: { ref: string; refSlug: string; text: string; bookSlug: string };
  reading: { label: string; refSlug: string; verses: { n: number; text: string }[] };
  blocks: ({ kind: "p"; text: string } | { kind: "q"; ref: string; refSlug: string; text: string })[];
};

const bookSlug = (name: string) =>
  BIBLE_BOOKS.find((b) => b.name.toLowerCase() === name.toLowerCase())?.slug
  ?? name.toLowerCase().replace(/^psalm$/, "psalms").replace(/\s+/g, "-");

const refLabel = (r: Ref) => `${r.book === "Psalms" ? "Psalm" : r.book} ${r.chapter}:${r.verse}`;
const refSlug = (r: Ref) => `${bookSlug(r.book)}-${r.chapter}-${r.verse}`;
const text = (r: Ref) => getVerseText(bookSlug(r.book), r.chapter, r.verse) ?? "";

export const slugOf = (month: number, day: number) => `${MONTH_NAMES[month - 1].toLowerCase()}-${day}`;

function build(m: Month, raw: Raw): Devotional {
  const { read } = raw;
  // a verse the text does not have (the source lacks Mark 4:41) is left out rather than shown empty
  const verses = Array.from({ length: read.to - read.from + 1 }, (_, i) => ({ n: read.from + i, text: text({ ...read, verse: read.from + i }) })).filter((v) => v.text);
  return {
    ...raw,
    month: m.month,
    monthName: MONTH_NAMES[m.month - 1],
    series: m.name,
    theme: m.theme,
    seriesOf: m.days.length,
    slug: slugOf(m.month, raw.day),
    dateLabel: `${MONTH_NAMES[m.month - 1]} ${raw.day}`,
    verse: { ref: refLabel(read), refSlug: refSlug(read), text: text(read), bookSlug: bookSlug(read.book) },
    reading: {
      label: `${read.book === "Psalms" ? "Psalm" : read.book} ${read.chapter}:${read.from}${read.to > read.from ? `–${read.to}` : ""}`,
      refSlug: `${bookSlug(read.book)}-${read.chapter}-${read.from}`,
      verses,
    },
    blocks: raw.body.map((b) => ("p" in b
      ? { kind: "p" as const, text: b.p }
      : { kind: "q" as const, ref: refLabel(b.q), refSlug: refSlug(b.q), text: text(b.q) })),
  };
}

export const DEVOTIONALS: Devotional[] = MONTHS.flatMap((m) => m.days.map((d) => build(m, d)));
const BY_SLUG = new Map(DEVOTIONALS.map((d) => [d.slug, d]));

export const devotionalBySlug = (slug: string) => BY_SLUG.get(slug);

// Today's devotional. Outside a leap year, February 29 is simply skipped.
export function devotionalFor(date: Date) {
  return BY_SLUG.get(slugOf(date.getUTCMonth() + 1, date.getUTCDate())) ?? DEVOTIONALS[0];
}

// The day before and after, around the calendar.
export function neighbours(d: Devotional) {
  const i = DEVOTIONALS.indexOf(d);
  return {
    prev: DEVOTIONALS[(i - 1 + DEVOTIONALS.length) % DEVOTIONALS.length],
    next: DEVOTIONALS[(i + 1) % DEVOTIONALS.length],
  };
}

export const MONTHS_INDEX = MONTHS.map((m) => ({ month: m.month, name: MONTH_NAMES[m.month - 1], series: m.name, days: m.days.length }));
