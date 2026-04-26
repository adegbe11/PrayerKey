import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS, TOPIC_MAP, ALL_SLUGS } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

export function generateStaticParams() {
  return ALL_SLUGS.map((slug) => ({ slug }));
}

export async function generateMetadata(
  { params }: { params: { slug: string } }
): Promise<Metadata> {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) return {};
  return {
    title:       `${topic.title} — Powerful Scripture-Based Prayer | PrayerKey`,
    description: topic.metaDesc,
    keywords:    topic.keywords.join(", "),
    authors:     [{ name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` }],
    alternates:  { canonical: `${BASE_URL}/prayer/${topic.slug}` },
    openGraph: {
      title:       `${topic.title} | PrayerKey`,
      description: topic.metaDesc,
      url:         `${BASE_URL}/prayer/${topic.slug}`,
      images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
      type:        "article",
    },
    twitter: {
      card:        "summary_large_image",
      title:       `${topic.title} | PrayerKey`,
      description: topic.metaDesc,
    },
  };
}

export default function PrayerSlugPage({ params }: { params: { slug: string } }) {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) notFound();

  const related = topic.related
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean) as typeof PRAYER_TOPICS;

  const gold   = "#B07C1F";
  const purple = "#AF52DE";

  const jsonLd = {
    "@context":    "https://schema.org",
    "@type":       "Article",
    "headline":    topic.title,
    "description": topic.metaDesc,
    "url":         `${BASE_URL}/prayer/${topic.slug}`,
    "dateModified": new Date().toISOString(),
    "author": {
      "@type":    "Person",
      "name":     "Collins Asein",
      "url":      `${BASE_URL}/author/collins-asein`,
      "jobTitle": "Christian Author & Faith Technologist",
    },
    "publisher": {
      "@type": "Organization",
      "name":  "PrayerKey",
      "url":   BASE_URL,
      "logo":  { "@type": "ImageObject", "url": `${BASE_URL}/og-image.png` },
    },
    "mainEntityOfPage": { "@type": "WebPage", "@id": `${BASE_URL}/prayer/${topic.slug}` },
    "breadcrumb": {
      "@type":           "BreadcrumbList",
      "itemListElement": [
        { "@type": "ListItem", "position": 1, "name": "Home",   "item": BASE_URL },
        { "@type": "ListItem", "position": 2, "name": "Prayer", "item": `${BASE_URL}/prayer` },
        { "@type": "ListItem", "position": 3, "name": topic.title, "item": `${BASE_URL}/prayer/${topic.slug}` },
      ],
    },
    "keywords": topic.keywords.join(", "),
    "about":    { "@type": "Thing", "name": topic.title },
    "mainEntity": {
      "@type": "FAQPage",
      "mainEntity": [
        {
          "@type": "Question",
          "name":  `What is a ${topic.title.toLowerCase()}?`,
          "acceptedAnswer": { "@type": "Answer", "text": topic.metaDesc },
        },
        {
          "@type": "Question",
          "name":  `What scripture supports ${topic.title.toLowerCase()}?`,
          "acceptedAnswer": {
            "@type": "Answer",
            "text":  topic.scripture.map(s => `${s.ref}: "${s.text}"`).join(" — "),
          },
        },
      ],
    },
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <div style={{ maxWidth: "780px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav style={{ marginBottom: "32px", fontSize: "13px", color: "rgba(255,255,255,0.35)", display: "flex", gap: "6px", alignItems: "center", flexWrap: "wrap" }}>
          <Link href="/"        style={{ color: "rgba(255,255,255,0.35)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/prayer"  style={{ color: "rgba(255,255,255,0.35)", textDecoration: "none" }}>Prayer</Link>
          <span>/</span>
          <span style={{ color: gold }}>{topic.title}</span>
        </nav>

        {/* ── TL;DR — AI Overview bait ── */}
        <div style={{
          background:   "rgba(176,124,31,0.07)",
          border:       `1.5px solid rgba(176,124,31,0.2)`,
          borderLeft:   `4px solid ${gold}`,
          borderRadius: "0 12px 12px 0",
          padding:      "14px 20px",
          marginBottom: "32px",
        }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: gold, letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 5px" }}>
            📌 Quick Answer
          </p>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.7)", margin: 0, lineHeight: 1.7 }}>
            {topic.metaDesc}
          </p>
        </div>

        {/* ── Header ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: `1.5px solid rgba(176,124,31,0.35)`, background: "rgba(176,124,31,0.06)",
            fontSize: "10px", fontWeight: 700, color: gold,
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            {topic.category}
          </div>
          <h1 style={{
            fontSize: "clamp(30px, 5vw, 50px)", fontWeight: 800,
            color: "#fff", margin: "0 0 14px", letterSpacing: "-0.03em", lineHeight: 1.1,
          }}>
            {topic.title}
          </h1>
          <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.5)", lineHeight: 1.7, margin: 0 }}>
            {topic.metaDesc} — by{" "}
            <Link href="/author/collins-asein" style={{ color: gold, textDecoration: "none", fontWeight: 600 }}>
              Collins Asein
            </Link>
          </p>
        </div>

        {/* ── Sample Prayer ── */}
        <div style={{
          background:   "rgba(255,255,255,0.03)",
          border:       `1.5px solid rgba(176,124,31,0.2)`,
          borderRadius: "20px",
          padding:      "clamp(24px,4vw,40px)",
          marginBottom: "28px",
          boxShadow:    `4px 4px 0 0 rgba(176,124,31,0.07)`,
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "22px" }}>
            <span style={{ fontSize: "22px" }}>🙏</span>
            <h2 style={{ fontSize: "13px", fontWeight: 700, color: gold, margin: 0, letterSpacing: "0.08em", textTransform: "uppercase" }}>
              {topic.title}
            </h2>
          </div>

          <p style={{
            fontSize:   "clamp(15px, 1.5vw, 17px)",
            color:      "rgba(255,255,255,0.87)",
            lineHeight: 1.95,
            fontStyle:  "italic",
            margin:     "0 0 24px",
            whiteSpace: "pre-wrap",
          }}>
            {topic.samplePrayer}
          </p>

          {topic.scripture.map((v) => (
            <div key={v.ref} style={{
              display:      "flex",
              gap:          "12px",
              padding:      "12px 16px",
              marginBottom: "8px",
              background:   `rgba(176,124,31,0.06)`,
              borderLeft:   `3px solid ${gold}`,
              borderRadius: "0 8px 8px 0",
            }}>
              <Link
                href={`/bible/verse/${v.ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "")}`}
                style={{ fontSize: "12px", fontWeight: 700, color: gold, minWidth: "110px", flexShrink: 0, textDecoration: "none" }}
              >
                {v.ref}
              </Link>
              <span style={{ fontSize: "13px", color: "rgba(255,255,255,0.6)", lineHeight: 1.7, fontStyle: "italic" }}>
                {v.text}
              </span>
            </div>
          ))}
        </div>

        {/* ── AI Generator CTA ── */}
        <div style={{
          background:   `linear-gradient(135deg, rgba(175,82,222,0.12) 0%, rgba(176,124,31,0.08) 100%)`,
          border:       `1.5px solid rgba(175,82,222,0.22)`,
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "48px",
          textAlign:    "center",
        }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 10px" }}>
            Want a personalised {topic.title.toLowerCase()}?
          </h2>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.4)", margin: "0 0 20px", lineHeight: 1.7 }}>
            Tell us your specific situation — our AI writes a prayer just for you in seconds. Free.
          </p>
          <Link
            href={`/pray?topic=${encodeURIComponent(topic.title)}`}
            style={{
              display:       "inline-block",
              padding:       "14px 32px",
              borderRadius:  "6px",
              background:    purple,
              color:         "#fff",
              textDecoration:"none",
              fontSize:      "15px",
              fontWeight:    800,
              letterSpacing: "-0.01em",
              boxShadow:     `4px 4px 0 0 rgba(175,82,222,0.3)`,
            }}
          >
            ✦ Generate My Personal Prayer
          </Link>
        </div>

        {/* ── Related Prayers ── */}
        {related.length > 0 && (
          <div style={{ marginBottom: "48px" }}>
            <h2 style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: "0 0 16px" }}>
              Related Prayers
            </h2>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
              {related.map((r) => (
                <Link
                  key={r.slug}
                  href={`/prayer/${r.slug}`}
                  style={{
                    display:       "block",
                    padding:       "14px 18px",
                    borderRadius:  "10px",
                    border:        "1.5px solid rgba(255,255,255,0.07)",
                    background:    "rgba(255,255,255,0.025)",
                    color:         "rgba(255,255,255,0.7)",
                    textDecoration:"none",
                    fontSize:      "13px",
                    fontWeight:    600,
                    transition:    "all 150ms",
                  }}
                >
                  🙏 {r.title}
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* ── Back to all prayers ── */}
        <div style={{ textAlign: "center", paddingTop: "8px" }}>
          <Link
            href="/prayer"
            style={{ color: gold, fontSize: "13px", fontWeight: 600, textDecoration: "none", letterSpacing: "0.04em" }}
          >
            ← Browse all {ALL_SLUGS.length} prayers
          </Link>
        </div>

      </div>
    </>
  );
}
