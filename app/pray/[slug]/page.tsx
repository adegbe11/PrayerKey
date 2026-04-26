import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS, TOPIC_MAP, ALL_SLUGS } from "@/lib/seo/prayer-topics";

/* ── Static generation ─────────────────────────────────────────────── */
export function generateStaticParams() {
  return ALL_SLUGS.map((slug) => ({ slug }));
}

export async function generateMetadata(
  { params }: { params: { slug: string } }
): Promise<Metadata> {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) return {};
  return {
    title:       `${topic.title} | PrayerKey`,
    description: topic.metaDesc,
    keywords:    topic.keywords.join(", "),
    openGraph: {
      title:       `${topic.title} | PrayerKey`,
      description: topic.metaDesc,
      url:         `https://www.prayerkey.com/pray/${topic.slug}`,
      images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
    },
    alternates: {
      canonical: `https://www.prayerkey.com/pray/${topic.slug}`,
    },
  };
}

/* ── Page ──────────────────────────────────────────────────────────── */
export default function PrayerTopicPage({ params }: { params: { slug: string } }) {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) notFound();

  const related = topic.related
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean) as typeof PRAYER_TOPICS;

  // JSON-LD structured data
  const jsonLd = {
    "@context":    "https://schema.org",
    "@type":       "Article",
    "headline":    topic.title,
    "description": topic.metaDesc,
    "url":         `https://www.prayerkey.com/pray/${topic.slug}`,
    "dateModified": new Date().toISOString(),
    "author": {
      "@type": "Person",
      "name":  "Collins Asein",
      "url":   "https://www.prayerkey.com/author/collins-asein",
      "jobTitle": "Christian Author & Faith Technologist",
    },
    "publisher": {
      "@type": "Organization",
      "name":  "PrayerKey",
      "url":   "https://www.prayerkey.com",
      "logo":  { "@type": "ImageObject", "url": "https://www.prayerkey.com/og-image.png" },
    },
    "mainEntityOfPage": {
      "@type": "WebPage",
      "@id":   `https://www.prayerkey.com/pray/${topic.slug}`,
    },
    "breadcrumb": {
      "@type":           "BreadcrumbList",
      "itemListElement": [
        { "@type": "ListItem", "position": 1, "name": "Home",   "item": "https://www.prayerkey.com" },
        { "@type": "ListItem", "position": 2, "name": "Pray",   "item": "https://www.prayerkey.com/pray" },
        { "@type": "ListItem", "position": 3, "name": topic.title, "item": `https://www.prayerkey.com/pray/${topic.slug}` },
      ],
    },
    "keywords": topic.keywords.join(", "),
    "about": { "@type": "Thing", "name": topic.title },
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <div style={{ maxWidth: "780px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav style={{ marginBottom: "32px", fontSize: "13px", color: "var(--pk-text-3)", display: "flex", gap: "6px", alignItems: "center" }}>
          <Link href="/" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/pray" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Pray</Link>
          <span>/</span>
          <span style={{ color: "var(--pk-gold)" }}>{topic.title}</span>
        </nav>

        {/* ── Header ── */}
        <div style={{ marginBottom: "40px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: "1.5px solid var(--pk-gold-border)", background: "var(--pk-gold-dim)",
            fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)",
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            {topic.category}
          </div>
          <h1 style={{
            fontSize: "clamp(32px, 5vw, 52px)", fontWeight: 800,
            color: "var(--pk-text)", margin: "0 0 16px", letterSpacing: "-0.03em", lineHeight: 1.1,
          }}>
            {topic.title}
          </h1>
          <p style={{ fontSize: "17px", color: "var(--pk-text-2)", lineHeight: 1.7, margin: 0 }}>
            {topic.metaDesc}
          </p>
        </div>

        {/* ── Sample Prayer Card ── */}
        <div style={{
          background: "var(--pk-card)", border: "1.5px solid var(--pk-gold-border)",
          borderRadius: "20px", padding: "clamp(24px,4vw,40px)", marginBottom: "32px",
          boxShadow: "4px 4px 0 0 var(--pk-gold-border)",
        }}>
          <div style={{ marginBottom: "24px" }}>
            <span style={{
              display:       "inline-block",
              fontSize:      "10px",
              fontWeight:    800,
              color:         "var(--pk-gold)",
              letterSpacing: "0.16em",
              textTransform: "uppercase",
              borderBottom:  "1.5px solid var(--pk-gold-border)",
              paddingBottom: "8px",
            }}>
              {topic.title}
            </span>
          </div>
          <p style={{
            fontSize: "clamp(15px, 1.5vw, 17px)", color: "var(--pk-text)",
            lineHeight: 1.95, fontStyle: "italic", margin: "0 0 24px", whiteSpace: "pre-wrap",
          }}>
            {topic.samplePrayer}
          </p>

          {/* Scripture */}
          {topic.scripture.map((v) => (
            <div key={v.ref} style={{
              display: "flex", gap: "12px", padding: "12px 16px", marginBottom: "8px",
              background: "var(--pk-gold-dim)", borderLeft: "3px solid var(--pk-gold)",
              borderRadius: "0 8px 8px 0",
            }}>
              <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)", minWidth: "110px", flexShrink: 0 }}>
                {v.ref}
              </span>
              <span style={{ fontSize: "13px", color: "var(--pk-text-2)", lineHeight: 1.7, fontStyle: "italic" }}>
                {v.text}
              </span>
            </div>
          ))}
        </div>

        {/* ── AI Generate CTA ── */}
        <div style={{
          background: "var(--pk-purple-dim)",
          border: "1.5px solid var(--pk-purple-border)", borderRadius: "16px",
          padding: "clamp(20px,3vw,32px)", marginBottom: "48px", textAlign: "center",
        }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 10px" }}>
            Want a personalised {topic.title.toLowerCase()}?
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-2)", margin: "0 0 20px" }}>
            Our AI will write a prayer tailored to your exact situation in seconds — free.
          </p>
          <Link
            href={`/pray?topic=${encodeURIComponent(topic.title)}`}
            style={{
              display: "inline-block", padding: "14px 32px", borderRadius: "6px",
              background: "var(--pk-purple)", color: "#fff", textDecoration: "none",
              fontSize: "15px", fontWeight: 800, letterSpacing: "-0.01em",
              boxShadow: "4px 4px 0 0 var(--pk-purple-border)",
              transition: "all 200ms ease",
            }}
          >
            ✦ Generate My Personal Prayer
          </Link>
        </div>

        {/* ── Related Prayers ── */}
        {related.length > 0 && (
          <div>
            <h2 style={{ fontSize: "18px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 16px" }}>
              Related Prayers
            </h2>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
              {related.map((r) => (
                <Link
                  key={r.slug}
                  href={`/pray/${r.slug}`}
                  style={{
                    display: "block", padding: "14px 18px", borderRadius: "10px",
                    border: "1.5px solid var(--pk-border)",
                    background: "var(--pk-card)",
                    color: "var(--pk-text-2)", textDecoration: "none",
                    fontSize: "13px", fontWeight: 600, transition: "all 150ms",
                  }}
                >
                  {r.title} →
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* ── Browse all ── */}
        <div style={{ marginTop: "48px", textAlign: "center" }}>
          <Link
            href="/pray/topics"
            style={{ color: "var(--pk-gold)", fontSize: "13px", fontWeight: 600, textDecoration: "none", letterSpacing: "0.04em" }}
          >
            Browse all {ALL_SLUGS.length} prayer topics →
          </Link>
        </div>
      </div>
    </>
  );
}
