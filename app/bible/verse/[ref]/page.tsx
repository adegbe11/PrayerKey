export const dynamic    = "force-dynamic";
export const revalidate = 86400; // Re-generate once per day (ISR)

import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { parseRef, refToDisplay, getRelatedPrayers, BOOK_MAP } from "@/lib/seo/bible-books";
import { getBookContent } from "@/lib/seo/bible-book-content";
import { TOPIC_MAP } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

/* ── Static KJV engine — no API, no fetch, instant ─────────────── */
import { getVerseText, getCrossRefs } from "@/lib/bible/kjv";
import { leatherOf } from "@/lib/bible/leather";
import InView from "@/components/home/InView";
import ChapterWindow from "@/components/bible/ChapterWindow";
import VerseTools from "@/components/bible/VerseTools";
import PrayerCard from "@/components/prayer/PrayerCard";
import "@/components/prayer/cards.css";
import "./verse.css";

// "ecclesiastes-3-2" -> the verse's text, cut short for a preview
function verseAt(ref: string) {
  const p = parseRef(ref);
  const t = p ? getVerseText(p.book.slug, p.chapter, p.verse) : null;
  if (!t) return null;
  return t.length > 110 ? t.slice(0, 110).replace(/\s+\S*$/, "") + "…" : t;
}

interface VerseResult { ref: string; text: string; match: string }

/* ── Metadata ───────────────────────────────────────────────────── */
export async function generateMetadata(
  { params }: { params: { ref: string } }
): Promise<Metadata> {
  const parsed = parseRef(params.ref);
  if (!parsed) return {};

  const display = `${parsed.book.name} ${parsed.chapter}:${parsed.verse}`;
  const desc    = `${display} KJV — meaning, commentary, cross-references, and a prayer based on this Bible verse. Explore Scripture with PrayerKey.`;

  return {
    title:       `${display} — Meaning, Commentary & Prayer | PrayerKey`,
    description: desc,
    keywords:    [
      `${display}`, `${display} meaning`, `${display} NIV`, `${display} KJV`,
      `${display} commentary`, `${display} explanation`, `Bible verse ${display}`,
      `what does ${display} mean`, `${parsed.book.name} chapter ${parsed.chapter}`,
    ].join(", "),
    authors:     [{ name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` }],
    alternates:  { canonical: `${BASE_URL}/bible/verse/${params.ref}` },
    openGraph: {
      title:       `${display} — Meaning, Commentary & Prayer | PrayerKey`,
      description: desc,
      url:         `${BASE_URL}/bible/verse/${params.ref}`,
      siteName:    "PrayerKey",
      images:      [{ url: "/og-image.png", width: 1200, height: 630, alt: display }],
      type:        "article",
    },
    twitter: {
      card:        "summary_large_image",
      title:       `${display} | PrayerKey`,
      description: desc,
    },
  };
}

/* ── Page ───────────────────────────────────────────────────────── */
export default async function BibleVersePage({ params }: { params: { ref: string } }) {
  const parsed = parseRef(params.ref);
  if (!parsed) notFound();

  const { book, chapter, verse } = parsed;
  const display     = `${book.name} ${chapter}:${verse}`;
  const bookContent = getBookContent(book.slug);

  // Static KJV lookups — synchronous, no network
  const verseText = getVerseText(book.slug, chapter, verse);
  const verseData: VerseResult | null = verseText
    ? { ref: display, text: verseText, match: "direct" }
    : null;
  const crossRefs = getCrossRefs(book.slug, chapter, verse, 5);

  // Related prayers
  const relatedSlugs  = getRelatedPrayers(book.name);
  const relatedPrayers = relatedSlugs
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean);

  // Prev / next verse navigation
  const vCount    = book.verses[chapter - 1] ?? 0;
  const prevVerse = verse > 1
    ? `${book.slug}-${chapter}-${verse - 1}`
    : chapter > 1
      ? `${book.slug}-${chapter - 1}-${book.verses[chapter - 2]}`
      : null;
  const nextVerse = verse < vCount
    ? `${book.slug}-${chapter}-${verse + 1}`
    : chapter < book.chapters
      ? `${book.slug}-${chapter + 1}-1`
      : null;

  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type":         "Article",
        "@id":           `${BASE_URL}/bible/verse/${params.ref}`,
        "headline":      `${display} — Meaning, Commentary & Prayer`,
        "description":   `Explore the meaning of ${display}, cross-references, and a prayer inspired by this verse.`,
        "url":           `${BASE_URL}/bible/verse/${params.ref}`,
        "dateModified":  new Date().toISOString(),
        "author": {
          "@type": "Person",
          "name":  "Collins Asein",
          "url":   `${BASE_URL}/author/collins-asein`,
        },
        "publisher": {
          "@type": "Organization",
          "name":  "PrayerKey",
          "url":   BASE_URL,
          "logo":  { "@type": "ImageObject", "url": `${BASE_URL}/og-image.png` },
        },
        "mainEntityOfPage": { "@type": "WebPage", "@id": `${BASE_URL}/bible/verse/${params.ref}` },
        "about": {
          "@type": "Book",
          "name":  `The Bible — ${book.name}`,
        },
      },
      {
        "@type":           "BreadcrumbList",
        "itemListElement": [
          { "@type": "ListItem", "position": 1, "name": "Home",         "item": BASE_URL },
          { "@type": "ListItem", "position": 2, "name": "Bible",        "item": `${BASE_URL}/bible` },
          { "@type": "ListItem", "position": 3, "name": book.name,      "item": `${BASE_URL}/bible/${book.slug}` },
          { "@type": "ListItem", "position": 4, "name": display,        "item": `${BASE_URL}/bible/verse/${params.ref}` },
        ],
      },
      ...(verseData ? [{
        "@type":        "FAQPage",
        "mainEntity": [
          {
            "@type": "Question",
            "name":  `What does ${display} mean?`,
            "acceptedAnswer": {
              "@type": "Answer",
              "text":  verseData.text,
            },
          },
          {
            "@type": "Question",
            "name":  `What is the context of ${display}?`,
            "acceptedAnswer": {
              "@type": "Answer",
              "text":  `${display} is found in the book of ${book.name}, chapter ${chapter}, verse ${verse}. It is part of the ${book.testament === "old" ? "Old" : "New"} Testament.`,
            },
          },
        ],
      }] : []),
    ],
  };

  // the whole chapter, with this verse in its place
  const chapterText = Array.from({ length: vCount }, (_, i) => ({ n: i + 1, text: getVerseText(book.slug, chapter, i + 1) ?? "" }));
  const prevText = prevVerse ? verseAt(prevVerse) : null;
  const nextText = nextVerse ? verseAt(nextVerse) : null;
  const url = `${BASE_URL}/bible/verse/${params.ref}`;
  const words = verseData ? verseData.text.split(/\s+/) : [];
  const step = words.length ? Math.min(90, 2400 / words.length) : 0;

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <InView />

      <article className="vs" style={{ "--leather": leatherOf(book.slug) } as React.CSSProperties}>
        {/* the verse, alone, lit */}
        <header className="vs-hero pk-bleed" data-nav="dark">
          <nav aria-label="Breadcrumb" className="vs-crumbs">
            <Link href="/">Home</Link><span>/</span><Link href="/bible">Bible</Link><span>/</span>
            <Link href={`/bible/${book.slug}`}>{book.name}</Link><span>/</span><span>Chapter {chapter}</span>
          </nav>
          <div className="vs-hero__in">
            <h1 className="vs-ref">{display}</h1>
            {verseData ? (
              <blockquote className="vs-verse">
                {words.map((w, i) => <span key={i} style={{ animationDelay: `${0.35 + (i * step) / 1000}s` }}>{w} </span>)}
              </blockquote>
            ) : (
              <p className="vs-verse vs-verse--missing">This verse could not be found. Try the <Link href="/bible">Bible search</Link>.</p>
            )}
            <p className="vs-kjv">King James Version</p>
            {verseData && <VerseTools text={verseData.text} display={display} url={url} />}
          </div>
        </header>

        {/* read it in its place */}
        {chapterText.length > 0 && (
          <section className="vs-context" data-in="" aria-labelledby="vs-context">
            <h2 id="vs-context" className="vs-h2">{book.name} {chapter}</h2>
            <ChapterWindow total={chapterText.length} label={`${book.name} ${chapter}`}>
              {chapterText.map((v) => (
                <span key={v.n} className={`vs-v ${v.n === verse ? "is-here" : ""} ${Math.abs(v.n - verse) > 4 ? "vs-far" : ""}`}>
                  <Link href={`/bible/verse/${book.slug}-${chapter}-${v.n}`} className="vs-v__n" aria-label={`${book.name} ${chapter}:${v.n}`}>{v.n}</Link>
                  {v.text}{" "}
                </span>
              ))}
            </ChapterWindow>
          </section>
        )}

        {/* where else the Bible says it */}
        {crossRefs.length > 0 && (
          <section className="vs-cross" data-in="" aria-labelledby="vs-cross">
            <h2 id="vs-cross" className="vs-h2">The Bible says it again</h2>
            <ul>
              {crossRefs.map((cr, i) => (
                <li key={cr.ref} style={{ "--i": i } as React.CSSProperties}>
                  <Link href={`/bible/verse/${cr.ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "")}`}>
                    {cr.text && <span className="vs-cross__text">{cr.text}</span>}
                    <span className="vs-cross__ref">{cr.ref}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        )}

        {/* the book it comes from */}
        {bookContent && (
          <section className="vs-book" data-in="">
            <Link href={`/bible/${book.slug}`} className="vs-book__cover" aria-label={`The Book of ${book.name}`}>
              <small>The Book of</small>
              <span>{book.name}</span>
            </Link>
            <div className="vs-book__about">
              <p>{bookContent.overview}</p>
              <Link href={`/bible/${book.slug}`} className="vs-book__open">Open {book.name}</Link>
            </div>
          </section>
        )}

        {relatedPrayers.length > 0 && (
          <section className="vs-prayers" data-in="" aria-labelledby="vs-prayers">
            <h2 id="vs-prayers" className="vs-h2">Pray it</h2>
            <ul>
              {relatedPrayers.slice(0, 4).map((p, i) => p && (
                <li key={p.slug} style={{ "--i": i } as React.CSSProperties}><PrayerCard slug={p.slug} title={p.title} href={`/pray/${p.slug}`} /></li>
              ))}
            </ul>
          </section>
        )}

        {/* keep reading */}
        <nav className="vs-turn" aria-label="Previous and next verse">
          {prevVerse ? (
            <Link href={`/bible/verse/${prevVerse}`}>
              <small>Before · {refToDisplay(prevVerse)}</small>
              {prevText && <span>{prevText}</span>}
            </Link>
          ) : <span />}
          {nextVerse ? (
            <Link href={`/bible/verse/${nextVerse}`} className="vs-turn__next">
              <small>Next · {refToDisplay(nextVerse)}</small>
              {nextText && <span>{nextText}</span>}
            </Link>
          ) : <span />}
        </nav>

        <p className="vs-note">Curated by <Link href="/author/collins-asein">Collins Asein</Link>. Bible text: King James Version.</p>
      </article>
    </>
  );
}
