import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import { BIBLE_BOOKS, BOOK_MAP } from "@/lib/seo/bible-books";
import { getBookContent } from "@/lib/seo/bible-book-content";
import { TOPIC_MAP } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";
const AUTHOR   = "Collins Asein";

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

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />

      <div style={{ maxWidth: "900px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav aria-label="Breadcrumb" style={{
          marginBottom: "28px", fontSize: "13px", color: "var(--pk-text-3)",
          display: "flex", gap: "6px", alignItems: "center", flexWrap: "wrap",
        }}>
          <Link href="/" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/bible" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Bible</Link>
          <span>/</span>
          <span style={{ color: "var(--pk-accent)" }}>{book.name}</span>
        </nav>

        {/* ── Header ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
            fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            {testament} · Book {bookIndex + 1} of 66
          </div>

          <h1 style={{
            fontSize: "clamp(32px, 5.5vw, 52px)", fontWeight: 800,
            color: "var(--pk-text)", margin: "0 0 14px",
            letterSpacing: "-0.03em", lineHeight: 1.08,
          }}>
            The Book of {book.name}
          </h1>

          {/* Stats */}
          <div style={{ display: "flex", gap: "24px", flexWrap: "wrap", marginBottom: "20px" }}>
            {[
              { n: book.chapters, label: book.chapters > 1 ? "Chapters" : "Chapter" },
              { n: totalVerses.toLocaleString(), label: "Verses" },
              { n: content.themes.length, label: "Key Themes" },
              { n: content.keyVerses.length, label: "Famous Verses" },
            ].map((s) => (
              <div key={s.label}>
                <span style={{ fontSize: "20px", fontWeight: 800, color: "var(--pk-accent)" }}>{s.n}</span>{" "}
                <span style={{ fontSize: "12px", color: "var(--pk-text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.06em" }}>{s.label}</span>
              </div>
            ))}
          </div>

          {/* Overview — unique per book */}
          <p style={{ fontSize: "17px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: 0 }}>
            {content.overview}
          </p>
        </div>

        {/* ── Themes ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 14px", letterSpacing: "-0.02em" }}>
            Key Themes in {book.name}
          </h2>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            {content.themes.map((theme) => (
              <span key={theme} style={{
                padding: "7px 16px", borderRadius: "999px",
                border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
                fontSize: "13px", fontWeight: 600, color: "var(--pk-accent)",
              }}>
                {theme}
              </span>
            ))}
          </div>
        </section>

        {/* ── Key verses ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 16px", letterSpacing: "-0.02em" }}>
            Most Famous Verses in {book.name}
          </h2>
          <div style={{ display: "grid", gap: "12px" }}>
            {content.keyVerses.map((kv) => (
              <Link key={kv.ref} href={`/bible/verse/${kv.ref}`} style={{
                display: "block", padding: "18px 22px",
                background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
                borderRadius: "14px", textDecoration: "none",
                transition: "border-color 150ms ease",
              }}>
                <div style={{ fontSize: "15px", fontWeight: 800, color: "var(--pk-accent)", marginBottom: "4px" }}>
                  {kv.display} →
                </div>
                <div style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.6 }}>
                  {kv.note}
                </div>
              </Link>
            ))}
          </div>
        </section>

        {/* ── Chapter grid ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px", letterSpacing: "-0.02em" }}>
            All {book.chapters} Chapter{book.chapters > 1 ? "s" : ""} of {book.name}
          </h2>
          <p style={{ fontSize: "13px", color: "var(--pk-text-3)", margin: "0 0 16px" }}>
            Select a chapter to start reading verse by verse.
          </p>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(52px, 1fr))", gap: "8px" }}>
            {Array.from({ length: book.chapters }, (_, i) => i + 1).map((ch) => (
              <Link key={ch} href={`/bible/verse/${book.slug}-${ch}-1`} style={{
                display: "flex", alignItems: "center", justifyContent: "center",
                aspectRatio: "1", borderRadius: "10px",
                border: "1.5px solid var(--pk-border)", background: "var(--pk-surface)",
                fontSize: "14px", fontWeight: 700, color: "var(--pk-text-2)",
                textDecoration: "none", transition: "all 130ms ease",
              }}>
                {ch}
              </Link>
            ))}
          </div>
        </section>

        {/* ── Related prayers (verse → prayer internal links) ── */}
        {relatedPrayers.length > 0 && (
          <section style={{ marginBottom: "40px" }}>
            <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 16px", letterSpacing: "-0.02em" }}>
              Prayers Inspired by {book.name}
            </h2>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: "12px" }}>
              {relatedPrayers.map((p) => p && (
                <Link key={p.slug} href={`/prayer/${p.slug}`} style={{
                  display: "block", padding: "16px 18px",
                  background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
                  borderRadius: "12px", textDecoration: "none",
                }}>
                  <div style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)", marginBottom: "4px" }}>
                    ✦ {p.title}
                  </div>
                  <div style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>{p.category}</div>
                </Link>
              ))}
            </div>
          </section>
        )}

        {/* ── Prev / next book nav ── */}
        <nav style={{ display: "flex", justifyContent: "space-between", gap: "12px", marginBottom: "40px" }}>
          {prevBook ? (
            <Link href={`/bible/${prevBook.slug}`} style={{
              padding: "12px 20px", borderRadius: "10px",
              border: "1.5px solid var(--pk-border)", background: "var(--pk-surface)",
              fontSize: "13px", fontWeight: 700, color: "var(--pk-text-2)", textDecoration: "none",
            }}>
              ← {prevBook.name}
            </Link>
          ) : <span />}
          {nextBook ? (
            <Link href={`/bible/${nextBook.slug}`} style={{
              padding: "12px 20px", borderRadius: "10px",
              border: "1.5px solid var(--pk-border)", background: "var(--pk-surface)",
              fontSize: "13px", fontWeight: 700, color: "var(--pk-text-2)", textDecoration: "none",
            }}>
              {nextBook.name} →
            </Link>
          ) : <span />}
        </nav>

        {/* ── All books (hub linking — every book reachable from every book) ── */}
        <section>
          <h2 style={{ fontSize: "16px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 14px" }}>
            Explore All 66 Books of the Bible
          </h2>
          <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
            {BIBLE_BOOKS.map((b) => (
              <Link key={b.slug} href={`/bible/${b.slug}`} style={{
                padding: "5px 12px", borderRadius: "999px",
                border: b.slug === book.slug ? "1.5px solid var(--pk-accent)" : "1px solid var(--pk-border)",
                background: b.slug === book.slug ? "var(--pk-accent-dim)" : "transparent",
                fontSize: "11px", fontWeight: 600,
                color: b.slug === book.slug ? "var(--pk-accent)" : "var(--pk-text-3)",
                textDecoration: "none",
              }}>
                {b.name}
              </Link>
            ))}
          </div>
        </section>

        {/* ── Author / E-E-A-T ── */}
        <div style={{
          marginTop: "48px", padding: "20px 24px",
          background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
          borderRadius: "14px", display: "flex", gap: "14px", alignItems: "center", flexWrap: "wrap",
        }}>
          <div style={{
            width: "44px", height: "44px", borderRadius: "50%",
            background: "linear-gradient(135deg, var(--pk-accent), var(--pk-purple))",
            display: "flex", alignItems: "center", justifyContent: "center",
            fontSize: "15px", fontWeight: 800, color: "#fff", flexShrink: 0,
          }}>CA</div>
          <div>
            <div style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)" }}>
              <Link href="/author/collins-asein" style={{ color: "var(--pk-text)", textDecoration: "none" }}>{AUTHOR}</Link>
              <span style={{ color: "var(--pk-text-3)", fontWeight: 400 }}> · Christian Author & Faith Technologist</span>
            </div>
            <div style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>
              Book overviews written and scripture-verified by PrayerKey Editorial.
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
