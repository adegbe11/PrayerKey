import type { Metadata } from "next";
import Link from "next/link";
import { GUIDES } from "@/lib/seo/guides";

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "Prayer Guides — How to Pray, Morning Prayers, Healing Verses & More | PrayerKey",
  description: "Free in-depth prayer guides: how to pray for beginners, morning and night prayers, Bible verses about healing, prayers for anxiety, marriage, and financial breakthrough.",
  keywords: "prayer guides, how to pray, morning prayer guide, healing scriptures, prayer for anxiety guide, bible reading plan, prayer tutorials",
  alternates: { canonical: `${BASE_URL}/guides` },
  openGraph: {
    title: "Prayer Guides | PrayerKey",
    description: "Free in-depth guides on prayer and the Bible — written for real life.",
    url: `${BASE_URL}/guides`,
    images: [{ url: "/og-image.png", width: 1200, height: 630 }],
    type: "website",
  },
};

const jsonLd = {
  "@context": "https://schema.org",
  "@type": "CollectionPage",
  name: "PrayerKey Prayer Guides",
  url: `${BASE_URL}/guides`,
  description: "In-depth guides on prayer and the Bible.",
  hasPart: GUIDES.map((g) => ({
    "@type": "Article",
    headline: g.title,
    url: `${BASE_URL}/guides/${g.slug}`,
  })),
  breadcrumb: {
    "@type": "BreadcrumbList",
    itemListElement: [
      { "@type": "ListItem", position: 1, name: "Home",   item: BASE_URL },
      { "@type": "ListItem", position: 2, name: "Guides", item: `${BASE_URL}/guides` },
    ],
  },
};

export default function GuidesHubPage() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <div style={{ maxWidth: "900px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Hero ── */}
        <div style={{ textAlign: "center", marginBottom: "48px", paddingTop: "8px" }}>
          <div style={{
            display: "inline-block", padding: "4px 14px", borderRadius: "4px",
            border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
            fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
            letterSpacing: "0.12em", textTransform: "uppercase", marginBottom: "20px",
          }}>
            ✦ Prayer Guides
          </div>
          <h1 style={{
            fontSize: "clamp(32px, 6vw, 56px)", fontWeight: 800,
            color: "var(--pk-text)", margin: "0 0 16px",
            letterSpacing: "-0.035em", lineHeight: 1.05,
          }}>
            Learn to pray —<br />for real life
          </h1>
          <p style={{
            fontSize: "clamp(15px, 1.5vw, 17px)", color: "var(--pk-text-2)",
            margin: "0 auto", lineHeight: 1.7, maxWidth: "540px",
          }}>
            In-depth, scripture-based guides on prayer and the Bible — written for
            beginners and lifelong believers alike. Free, practical, no fluff.
          </p>
        </div>

        {/* ── Guide cards ── */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "16px" }}>
          {GUIDES.map((g) => (
            <Link key={g.slug} href={`/guides/${g.slug}`} style={{
              display: "block", padding: "24px",
              background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
              borderRadius: "16px", textDecoration: "none",
              transition: "border-color 150ms ease, transform 150ms ease",
            }}>
              <div style={{
                fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
                letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "10px",
              }}>
                {g.readMinutes} min read
              </div>
              <div style={{
                fontSize: "18px", fontWeight: 700, color: "var(--pk-text)",
                marginBottom: "8px", lineHeight: 1.35, letterSpacing: "-0.01em",
              }}>
                {g.title}
              </div>
              <p style={{ fontSize: "13px", color: "var(--pk-text-3)", lineHeight: 1.65, margin: 0 }}>
                {g.metaDesc}
              </p>
            </Link>
          ))}
        </div>
      </div>
    </>
  );
}
