import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import { BIBLE_BOOKS, BOOK_MAP } from "@/lib/seo/bible-books";
import { getBookContent } from "@/lib/seo/bible-book-content";
import { TOPIC_MAP } from "@/lib/seo/prayer-topics";
import { getVerseText } from "@/lib/bible/kjv";
import { leatherOf, spineHeight } from "@/lib/bible/leather";
import InView from "@/components/home/InView";
import ShelfHere from "@/components/bible/ShelfHere";
import PrayerCard from "@/components/prayer/PrayerCard";
import "@/components/prayer/cards.css";
import "./book.css";

const BASE_URL = "https://www.prayerkey.com";
const AUTHOR   = "Collins Asein";

// "The words of the Preacher, the son of David, king in Jerusalem." -> the first few words
function firstWords(text: string, max = 64) {
  if (text.length <= max) return text;
  const cut = text.slice(0, max);
  return cut.slice(0, cut.lastIndexOf(" ")).replace(/[,;:]$/, "") + "…";
}

/* ── Static generation: all 66 books at build time ─────────────────── */
export function generateStaticParams() {
  return BIBLE_BOOKS.map((b) => ({ book: b.slug }));
}

export async function generateMetadata(
  { params }: { params: { book: string } }
): Promise<Metadata> {
  const book = BOOK_MAP.get(params.book);
  const content = getBookContent(params.book);
  if (!book || !content) return {};
  const totalVerses = book.verses.reduce((s, v) => s + v, 0);
  const title = `Book of ${book.name} — Summary, Key Verses & Chapters | PrayerKey`;
  const description = `${book.name}: ${content.overview.slice(0, 120)}… Read all ${book.chapters} chapters, ${totalVerses.toLocaleString()} verses, key themes, and related prayers.`;
  return {
    title,
    description,
    keywords: [
      `book of ${book.name.toLowerCase()}`,
      `${book.name.toLowerCase()} summary`,
      `${book.name.toLowerCase()} bible`,
      `${book.name.toLowerCase()} key verses`,
      `${book.name.toLowerCase()} chapters`,
      ...content.themes.map((t) => t.toLowerCase()),
    ].join(", "),
    authors: [{ name: AUTHOR, url: `${BASE_URL}/author/collins-asein` }],
    alternates: { canonical: `${BASE_URL}/bible/${book.slug}` },
    openGraph: {
      title, description,
      url: `${BASE_URL}/bible/${book.slug}`,
      images: [{ url: "/og-image.png", width: 1200, height: 630 }],
      type: "article",
    },
    twitter: { card: "summary_large_image", title, description },
  };
}

/* ── Page ──────────────────────────────────────────────────────────── */
export default function BibleBookPage({ params }: { params: { book: string } }) {
  const book    = BOOK_MAP.get(params.book);
  const content = getBookContent(params.book);
  if (!book || !content) notFound();

  const totalVerses = book.verses.reduce((s, v) => s + v, 0);
  const bookIndex   = BIBLE_BOOKS.findIndex((b) => b.slug === book.slug);
  const prevBook    = bookIndex > 0 ? BIBLE_BOOKS[bookIndex - 1] : null;
  const nextBook    = bookIndex < BIBLE_BOOKS.length - 1 ? BIBLE_BOOKS[bookIndex + 1] : null;
  const testament   = book.testament === "old" ? "Old Testament" : "New Testament";
  const relatedPrayers = content.relatedPrayers
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean);

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Article",
    headline: `Book of ${book.name} — Summary, Key Verses & Chapters`,
    description: content.overview,
    url: `${BASE_URL}/bible/${book.slug}`,
    dateModified: new Date().toISOString(),
    author: {
      "@type": "Person", name: AUTHOR,
      url: `${BASE_URL}/author/collins-asein`,
      jobTitle: "Christian Author & Faith Technologist",
    },
    publisher: {
      "@type": "Organization", name: "PrayerKey", url: BASE_URL,
      logo: { "@type": "ImageObject", url: `${BASE_URL}/og-image.png` },
    },
    mainEntityOfPage: { "@type": "WebPage", "@id": `${BASE_URL}/bible/${book.slug}` },
    breadcrumb: {
      "@type": "BreadcrumbList",
      itemListElement: [
        { "@type": "ListItem", position: 1, name: "Home",     item: BASE_URL },
        { "@type": "ListItem", position: 2, name: "Bible",    item: `${BASE_URL}/bible` },
        { "@type": "ListItem", position: 3, name: book.name,  item: `${BASE_URL}/bible/${book.slug}` },
      ],
    },
    about: { "@type": "Book", name: `Book of ${book.name}`, isPartOf: { "@type": "Book", name: "The Bible" } },
    keywords: content.themes.join(", "),
  };

  const faqLd = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: [
      {
        "@type": "Question",
        name: `What is the book of ${book.name} about?`,
        acceptedAnswer: { "@type": "Answer", text: content.overview },
      },
      {
        "@type": "Question",
        name: `How many chapters are in ${book.name}?`,
        acceptedAnswer: { "@type": "Answer", text: `The book of ${book.name} has ${book.chapters} chapter${book.chapters > 1 ? "s" : ""} and ${totalVerses.toLocaleString()} verses. It is part of the ${testament}.` },
      },
      {
        "@type": "Question",
        name: `What are the main themes of ${book.name}?`,
        acceptedAnswer: { "@type": "Answer", text: `The main themes of ${book.name} are: ${content.themes.join(", ")}.` },
      },
    ],
  };

  // the opening words of each chapter, for books short enough to show them
  const openings = book.chapters <= 50
    ? Array.from({ length: book.chapters }, (_, i) => firstWords(getVerseText(book.slug, i + 1, 1) ?? ""))
    : [];
  const loved = content.keyVerses.map((kv) => {
    const m = kv.ref.match(/-(\d+)-(\d+)$/);
    return { ...kv, text: m ? getVerseText(book.slug, Number(m[1]), Number(m[2])) : null };
  });

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />
      <InView />

      <article className="bk" style={{ "--leather": leatherOf(book.slug) } as React.CSSProperties}>
        {/* the book, opened: its own leather, its name in gold */}
        <header className="bk-cover pk-bleed" data-nav="dark">
          <nav aria-label="Breadcrumb" className="bk-crumbs">
            <Link href="/">Home</Link><span>/</span><Link href="/bible">Bible</Link><span>/</span><span>{book.name}</span>
          </nav>
          <div className="bk-cover__in">
            <span className="bk-rule" aria-hidden />
            <h1 className="bk-title">
              <span className="bk-title__the">The Book of</span>
              <span className="bk-title__name">{book.name}</span>
            </h1>
            <span className="bk-rule" aria-hidden />
            <p className="bk-facts">
              {testament} · Book {bookIndex + 1} of 66 · {book.chapters} chapter{book.chapters > 1 ? "s" : ""} · {totalVerses.toLocaleString()} verses
            </p>
          </div>
        </header>

        <section className="bk-open" data-in="">
          <p className="bk-overview">{content.overview}</p>
          <p className="bk-themes">
            {content.themes.map((t, i) => <span key={t} style={{ "--i": i } as React.CSSProperties}>{t}</span>)}
          </p>
        </section>

        {loved.length > 0 && (
          <section className="bk-loved" data-in="" aria-labelledby="bk-loved">
            <h2 id="bk-loved" className="bk-h2">Its most loved verses</h2>
            <ul>
              {loved.map((kv, i) => (
                <li key={kv.ref} style={{ "--i": i } as React.CSSProperties}>
                  <Link href={`/bible/verse/${kv.ref}`}>
                    {kv.text && <span className="bk-loved__text">{kv.text}</span>}
                    <span className="bk-loved__ref">{kv.display}</span>
                    <span className="bk-loved__note">{kv.note}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        )}

        <section className="bk-chapters" data-in="" aria-labelledby="bk-chapters">
          <h2 id="bk-chapters" className="bk-h2">{book.chapters === 1 ? "The chapter" : `${book.chapters} chapters`}</h2>
          <ol className={openings.length ? "bk-ch" : "bk-ch bk-ch--compact"}>
            {Array.from({ length: book.chapters }, (_, i) => i + 1).map((ch) => (
              <li key={ch} style={{ "--i": Math.min(ch - 1, 24) } as React.CSSProperties}>
                <Link href={`/bible/verse/${book.slug}-${ch}-1`} aria-label={`${book.name} chapter ${ch}`}>
                  <span className="bk-ch__n">{ch}</span>
                  {openings.length > 0 && <span className="bk-ch__first">{openings[ch - 1]}</span>}
                  <span className="bk-ch__count">{book.verses[ch - 1]} verses</span>
                </Link>
              </li>
            ))}
          </ol>
        </section>

        {relatedPrayers.length > 0 && (
          <section className="bk-prayers" data-in="" aria-labelledby="bk-prayers">
            <h2 id="bk-prayers" className="bk-h2">Pray with {book.name}</h2>
            <ul>
              {relatedPrayers.map((p, i) => p && (
                <li key={p.slug} style={{ "--i": i } as React.CSSProperties}><PrayerCard slug={p.slug} title={p.title} href={`/prayer/${p.slug}`} /></li>
              ))}
            </ul>
          </section>
        )}

        {/* back on the shelf, with this one lifted out */}
        <section className="bk-shelf" aria-label="All 66 books">
          <nav className="bk-turn" aria-label="Previous and next book">
            {prevBook ? <Link href={`/bible/${prevBook.slug}`} style={{ "--c": leatherOf(prevBook.slug) } as React.CSSProperties}><small>Before</small>{prevBook.name}</Link> : <span />}
            {nextBook ? <Link href={`/bible/${nextBook.slug}`} style={{ "--c": leatherOf(nextBook.slug) } as React.CSSProperties}><small>Next</small>{nextBook.name}</Link> : <span />}
          </nav>
          <div className="bk-spines">
            {BIBLE_BOOKS.map((b) => (
              <Link
                key={b.slug}
                href={`/bible/${b.slug}`}
                className={`bk-spine ${b.slug === book.slug ? "is-here" : ""}`}
                aria-current={b.slug === book.slug ? "page" : undefined}
                title={b.name}
                style={{ "--c": leatherOf(b.slug), "--h": `${spineHeight(b.chapters, 0.55)}px` } as React.CSSProperties}
              >
                <span>{b.name}</span>
              </Link>
            ))}
          </div>
          <ShelfHere />
        </section>

        <p className="bk-note">
          Book overviews written by <Link href="/author/collins-asein">{AUTHOR}</Link> and checked against Scripture by the PrayerKey editorial team. Bible text: King James Version.
        </p>
      </article>
    </>
  );
}
