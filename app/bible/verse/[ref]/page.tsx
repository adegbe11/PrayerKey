export const dynamic    = "force-dynamic";
export const revalidate = 86400; // Re-generate once per day (ISR)

import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { parseRef, refToDisplay, getRelatedPrayers, BOOK_MAP } from "@/lib/seo/bible-books";
import { TOPIC_MAP } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

/* ── Types ─────────────────────────────────────────────────────── */
interface VerseResult { ref: string; text: string; match: string }
interface CrossRef    { ref: string; text: string; reason: string }

/* ── Internal fetch helpers ─────────────────────────────────────── */
async function fetchVerse(display: string): Promise<VerseResult | null> {
  try {
    const url = `${BASE_URL}/api/bible/search?q=${encodeURIComponent(display)}&translation=NIV`;
    const res  = await fetch(url, { next: { revalidate: 86400 } });
    const data = await res.json() as { results: VerseResult[] };
    return data.results?.[0] ?? null;
  } catch { return null; }
}

async function fetchCrossRefs(display: string): Promise<CrossRef[]> {
  try {
    const url = `${BASE_URL}/api/bible/cross-refs?ref=${encodeURIComponent(display)}&translation=NIV`;
    const res  = await fetch(url, { next: { revalidate: 86400 } });
    const data = await res.json() as { refs: CrossRef[] };
    return data.refs ?? [];
  } catch { return []; }
}

/* ── Metadata ───────────────────────────────────────────────────── */
export async function generateMetadata(
  { params }: { params: { ref: string } }
): Promise<Metadata> {
  const parsed = parseRef(params.ref);
  if (!parsed) return {};

  const display = `${parsed.book.name} ${parsed.chapter}:${parsed.verse}`;
  const desc    = `${display} NIV — meaning, commentary, cross-references, and a prayer based on this Bible verse. Explore Scripture with PrayerKey.`;

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
  const display  = `${book.name} ${chapter}:${verse}`;

  // Fetch verse + cross-refs in parallel
  const [verseData, crossRefs] = await Promise.all([
    fetchVerse(display),
    fetchCrossRefs(display),
  ]);

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
          { "@type": "ListItem", "position": 3, "name": book.name,      "item": `${BASE_URL}/bible` },
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

  /* ── S T Y L E  T O K E N S ── */
  const gold   = "#B07C1F";
  const purple = "#AF52DE";
  const bg     = "#04040A";

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <div style={{ maxWidth: "840px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav style={{ marginBottom: "32px", fontSize: "13px", color: "rgba(255,255,255,0.35)", display: "flex", gap: "6px", alignItems: "center", flexWrap: "wrap" }}>
          <Link href="/"       style={{ color: "rgba(255,255,255,0.35)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/bible"  style={{ color: "rgba(255,255,255,0.35)", textDecoration: "none" }}>Bible</Link>
          <span>/</span>
          <span style={{ color: gold }}>{display}</span>
        </nav>

        {/* ── Header ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-flex", gap: "8px", marginBottom: "16px",
          }}>
            <span style={{
              padding: "4px 12px", borderRadius: "4px",
              border: `1.5px solid rgba(176,124,31,0.35)`, background: "rgba(176,124,31,0.06)",
              fontSize: "10px", fontWeight: 700, color: gold,
              letterSpacing: "0.1em", textTransform: "uppercase",
            }}>
              {book.testament === "old" ? "Old Testament" : "New Testament"}
            </span>
            <span style={{
              padding: "4px 12px", borderRadius: "4px",
              border: "1.5px solid rgba(255,255,255,0.1)", background: "rgba(255,255,255,0.03)",
              fontSize: "10px", fontWeight: 700, color: "rgba(255,255,255,0.4)",
              letterSpacing: "0.08em", textTransform: "uppercase",
            }}>
              {book.name} · Chapter {chapter}
            </span>
          </div>

          <h1 style={{
            fontSize: "clamp(32px, 5vw, 52px)", fontWeight: 800,
            color: "#fff", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.1,
          }}>
            {display}
          </h1>
          <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.45)", margin: 0, lineHeight: 1.7 }}>
            Meaning, cross-references, and a prayer for {display} — by{" "}
            <Link href="/author/collins-asein" style={{ color: gold, textDecoration: "none", fontWeight: 600 }}>
              Collins Asein
            </Link>
          </p>
        </div>

        {/* ── TL;DR (AI Overview bait) ── */}
        <div style={{
          background: "rgba(176,124,31,0.07)", border: `1.5px solid rgba(176,124,31,0.25)`,
          borderLeft: `4px solid ${gold}`, borderRadius: "0 12px 12px 0",
          padding: "16px 20px", marginBottom: "32px",
        }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: gold, letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 6px" }}>
            📌 Quick Summary
          </p>
          <p style={{ fontSize: "15px", color: "rgba(255,255,255,0.75)", margin: 0, lineHeight: 1.7 }}>
            {display} is a verse from the book of {book.name} in the {book.testament === "old" ? "Old" : "New"} Testament.
            {verseData ? ` The NIV reads: "${verseData.text.slice(0, 120)}${verseData.text.length > 120 ? "..." : ""}"` : ""}
            {" "}Below you will find the full verse text, cross-references, meaning, and a prayer based on this scripture.
          </p>
        </div>

        {/* ── Verse Card ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
            {display} — NIV
          </h2>
          <div style={{
            background: "rgba(255,255,255,0.03)", border: `1.5px solid rgba(176,124,31,0.2)`,
            borderRadius: "16px", padding: "clamp(24px,4vw,40px)",
            boxShadow: "4px 4px 0 0 rgba(176,124,31,0.06)",
          }}>
            {verseData ? (
              <>
                <p style={{
                  fontSize: "clamp(18px, 2.5vw, 26px)", color: "rgba(255,255,255,0.92)",
                  lineHeight: 1.75, fontStyle: "italic", margin: "0 0 20px",
                  letterSpacing: "-0.01em",
                }}>
                  &ldquo;{verseData.text}&rdquo;
                </p>
                <p style={{ fontSize: "13px", color: gold, fontWeight: 700, margin: 0 }}>
                  — {display} (NIV)
                </p>
              </>
            ) : (
              <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.4)", fontStyle: "italic", margin: 0 }}>
                Search for {display} using our Bible search above to see the full verse text.
              </p>
            )}
          </div>
        </section>

        {/* ── Cross References ── */}
        {crossRefs.length > 0 && (
          <section style={{ marginBottom: "40px" }}>
            <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
              Cross-References for {display}
            </h2>
            <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
              {crossRefs.map((cr) => {
                const crRef = cr.ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "");
                return (
                  <div key={cr.ref} style={{
                    padding: "16px 20px", borderRadius: "10px",
                    border: "1.5px solid rgba(255,255,255,0.07)",
                    background: "rgba(255,255,255,0.02)",
                  }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "12px", marginBottom: "8px" }}>
                      <Link
                        href={`/bible/verse/${crRef}`}
                        style={{ fontSize: "13px", fontWeight: 700, color: gold, textDecoration: "none" }}
                      >
                        📖 {cr.ref}
                      </Link>
                      <span style={{ fontSize: "11px", color: "rgba(255,255,255,0.25)", flexShrink: 0 }}>
                        related
                      </span>
                    </div>
                    {cr.text && (
                      <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.6)", margin: "0 0 6px", fontStyle: "italic", lineHeight: 1.65 }}>
                        &ldquo;{cr.text}&rdquo;
                      </p>
                    )}
                    {cr.reason && (
                      <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.3)", margin: 0 }}>
                        {cr.reason}
                      </p>
                    )}
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {/* ── Pray This Verse ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
            Pray {display}
          </h2>
          <div style={{
            background: `linear-gradient(135deg, rgba(175,82,222,0.1) 0%, rgba(176,124,31,0.07) 100%)`,
            border: `1.5px solid rgba(175,82,222,0.2)`,
            borderRadius: "16px", padding: "clamp(24px,4vw,36px)", textAlign: "center",
          }}>
            <div style={{ fontSize: "32px", marginBottom: "12px" }}>🙏</div>
            <h3 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 10px" }}>
              Generate a Prayer from {display}
            </h3>
            <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.45)", margin: "0 0 20px", lineHeight: 1.7 }}>
              Our AI will write a personal prayer inspired by this scripture — tailored to your situation. Free, instant, no account needed.
            </p>
            <Link
              href={`/pray?q=${encodeURIComponent(`Based on ${display}: ${verseData?.text ?? display}`)}`}
              style={{
                display: "inline-block", padding: "14px 32px", borderRadius: "6px",
                background: purple, color: "#fff", textDecoration: "none",
                fontSize: "15px", fontWeight: 800, letterSpacing: "-0.01em",
                boxShadow: `4px 4px 0 0 rgba(175,82,222,0.3)`,
              }}
            >
              ✦ Pray This Verse
            </Link>
          </div>
        </section>

        {/* ── Related Prayers ── */}
        {relatedPrayers.length > 0 && (
          <section style={{ marginBottom: "40px" }}>
            <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
              Prayers Related to {book.name}
            </h2>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
              {relatedPrayers.map((p) => p && (
                <Link
                  key={p.slug}
                  href={`/pray/${p.slug}`}
                  style={{
                    display: "block", padding: "14px 18px", borderRadius: "10px",
                    border: "1.5px solid rgba(255,255,255,0.07)",
                    background: "rgba(255,255,255,0.025)",
                    color: "rgba(255,255,255,0.7)", textDecoration: "none",
                    fontSize: "13px", fontWeight: 600,
                  }}
                >
                  🙏 {p.title}
                </Link>
              ))}
            </div>
          </section>
        )}

        {/* ── Chapter Navigation ── */}
        <section style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
            More from {book.name} {chapter}
          </h2>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            {Array.from({ length: Math.min(book.verses[chapter - 1] ?? 0, 12) }, (_, i) => i + 1).map((v) => (
              <Link
                key={v}
                href={`/bible/verse/${book.slug}-${chapter}-${v}`}
                style={{
                  padding: "8px 14px", borderRadius: "8px",
                  border: v === verse
                    ? `1.5px solid ${gold}`
                    : "1.5px solid rgba(255,255,255,0.08)",
                  background: v === verse ? `rgba(176,124,31,0.1)` : "rgba(255,255,255,0.02)",
                  color: v === verse ? gold : "rgba(255,255,255,0.5)",
                  textDecoration: "none", fontSize: "13px", fontWeight: v === verse ? 700 : 500,
                }}
              >
                v{v}
              </Link>
            ))}
            {(book.verses[chapter - 1] ?? 0) > 12 && (
              <span style={{ padding: "8px 14px", fontSize: "13px", color: "rgba(255,255,255,0.25)" }}>
                +{(book.verses[chapter - 1] ?? 0) - 12} more
              </span>
            )}
          </div>
        </section>

        {/* ── Prev / Next Navigation ── */}
        <div style={{ display: "flex", justifyContent: "space-between", gap: "12px", marginBottom: "48px" }}>
          {prevVerse ? (
            <Link href={`/bible/verse/${prevVerse}`} style={{
              padding: "12px 20px", borderRadius: "8px",
              border: "1.5px solid rgba(255,255,255,0.08)",
              background: "rgba(255,255,255,0.02)",
              color: "rgba(255,255,255,0.6)", textDecoration: "none",
              fontSize: "13px", fontWeight: 600, display: "flex", alignItems: "center", gap: "6px",
            }}>
              ← {refToDisplay(prevVerse)}
            </Link>
          ) : <div />}
          {nextVerse ? (
            <Link href={`/bible/verse/${nextVerse}`} style={{
              padding: "12px 20px", borderRadius: "8px",
              border: "1.5px solid rgba(255,255,255,0.08)",
              background: "rgba(255,255,255,0.02)",
              color: "rgba(255,255,255,0.6)", textDecoration: "none",
              fontSize: "13px", fontWeight: 600, display: "flex", alignItems: "center", gap: "6px",
            }}>
              {refToDisplay(nextVerse)} →
            </Link>
          ) : <div />}
        </div>

        {/* ── Author Attribution ── */}
        <div style={{
          padding: "20px 24px", borderRadius: "12px",
          border: "1.5px solid rgba(255,255,255,0.06)",
          background: "rgba(255,255,255,0.015)",
          display: "flex", gap: "16px", alignItems: "center",
        }}>
          <div style={{
            width: "44px", height: "44px", borderRadius: "50%", flexShrink: 0,
            background: `linear-gradient(135deg, ${purple} 0%, ${gold} 100%)`,
            display: "flex", alignItems: "center", justifyContent: "center", fontSize: "20px",
          }}>✝️</div>
          <div>
            <p style={{ fontSize: "13px", color: "rgba(255,255,255,0.45)", margin: "0 0 2px" }}>
              Curated by
            </p>
            <Link href="/author/collins-asein" style={{ fontSize: "14px", fontWeight: 700, color: "#fff", textDecoration: "none" }}>
              Collins Asein
            </Link>
            <span style={{ fontSize: "13px", color: "rgba(255,255,255,0.35)" }}> — Christian Author & Founder of PrayerKey</span>
          </div>
        </div>

      </div>
    </>
  );
}
