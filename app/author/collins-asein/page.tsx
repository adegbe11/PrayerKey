import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "Collins Asein — Christian Author & Founder of PrayerKey",
  description:
    "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey — the AI-powered prayer and Bible tool serving believers worldwide.",
  alternates: { canonical: `${BASE_URL}/author/collins-asein` },
  openGraph: {
    title:       "Collins Asein — Christian Author & Founder of PrayerKey",
    description: "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey — the AI-powered prayer and Bible tool serving believers worldwide.",
    url:         `${BASE_URL}/author/collins-asein`,
    siteName:    "PrayerKey",
    images:      [{ url: "/og-image.png", width: 1200, height: 630, alt: "Collins Asein" }],
    type:        "profile",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Collins Asein — Christian Author & Founder of PrayerKey",
    description: "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey.",
  },
};

const FEATURED_PRAYERS = PRAYER_TOPICS.slice(0, 8);

const ARTICLES = [
  { title: "How AI Is Transforming the Way Christians Pray",    href: "/pray",         date: "March 2026" },
  { title: "Why Every Church Needs a Live Bible Verse Tracker", href: "/live",         date: "February 2026" },
  { title: "120 Powerful Prayers for Every Situation",          href: "/pray/topics",  date: "January 2026" },
  { title: "The Science and Spirit of Prayer",                  href: "/pray/morning", date: "December 2025" },
];

export default function CollinsAseinPage() {
  const jsonLd = {
    "@context":   "https://schema.org",
    "@graph": [
      {
        "@type":       "Person",
        "@id":         `${BASE_URL}/author/collins-asein`,
        "name":        "Collins Asein",
        "url":         `${BASE_URL}/author/collins-asein`,
        "jobTitle":    "Christian Author & Faith Technologist",
        "description": "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey — an AI-powered prayer generation and Bible study platform serving believers worldwide.",
        "knowsAbout":  ["Christian Prayer", "Bible Study", "Faith Technology", "AI Tools for Christians", "Worship", "Sermon Preparation"],
        "worksFor": {
          "@type": "Organization",
          "name":  "PrayerKey",
          "url":   BASE_URL,
        },
        "sameAs": [
          BASE_URL,
        ],
      },
      {
        "@type":        "ProfilePage",
        "@id":          `${BASE_URL}/author/collins-asein#profilepage`,
        "url":          `${BASE_URL}/author/collins-asein`,
        "name":         "Collins Asein — Author Profile",
        "mainEntity":   { "@id": `${BASE_URL}/author/collins-asein` },
        "breadcrumb": {
          "@type":           "BreadcrumbList",
          "itemListElement": [
            { "@type": "ListItem", "position": 1, "name": "Home",   "item": BASE_URL },
            { "@type": "ListItem", "position": 2, "name": "Author", "item": `${BASE_URL}/author` },
            { "@type": "ListItem", "position": 3, "name": "Collins Asein", "item": `${BASE_URL}/author/collins-asein` },
          ],
        },
      },
    ],
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <div style={{ maxWidth: "860px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav style={{ marginBottom: "40px", fontSize: "13px", color: "rgba(255,255,255,0.35)", display: "flex", gap: "6px", alignItems: "center" }}>
          <Link href="/" style={{ color: "rgba(255,255,255,0.35)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <span style={{ color: "rgba(255,255,255,0.35)" }}>Author</span>
          <span>/</span>
          <span style={{ color: "var(--pk-gold, #B07C1F)" }}>Collins Asein</span>
        </nav>

        {/* ── Hero ── */}
        <div style={{
          display:       "flex",
          gap:           "clamp(24px, 4vw, 48px)",
          alignItems:    "flex-start",
          marginBottom:  "60px",
          flexWrap:      "wrap",
        }}>
          {/* Avatar */}
          <div style={{
            width:        "clamp(80px, 12vw, 120px)",
            height:       "clamp(80px, 12vw, 120px)",
            borderRadius: "50%",
            background:   "linear-gradient(135deg, #AF52DE 0%, #B07C1F 100%)",
            display:      "flex",
            alignItems:   "center",
            justifyContent: "center",
            fontSize:     "clamp(32px, 5vw, 52px)",
            flexShrink:   0,
            border:       "3px solid rgba(176,124,31,0.3)",
            boxShadow:    "0 0 40px rgba(175,82,222,0.2)",
          }}>
            ✝️
          </div>

          {/* Bio */}
          <div style={{ flex: 1, minWidth: "240px" }}>
            {/* Badge */}
            <div style={{
              display:      "inline-block",
              padding:      "4px 12px",
              borderRadius: "4px",
              border:       "1.5px solid rgba(176,124,31,0.35)",
              background:   "rgba(176,124,31,0.06)",
              fontSize:     "10px",
              fontWeight:   700,
              color:        "var(--pk-gold, #B07C1F)",
              letterSpacing:"0.1em",
              textTransform:"uppercase",
              marginBottom: "14px",
            }}>
              Christian Author · Faith Technologist
            </div>

            <h1 style={{
              fontSize:      "clamp(28px, 5vw, 48px)",
              fontWeight:    800,
              color:         "#fff",
              margin:        "0 0 12px",
              letterSpacing: "-0.03em",
              lineHeight:    1.1,
            }}>
              Collins Asein
            </h1>

            <p style={{
              fontSize:   "clamp(15px, 1.5vw, 17px)",
              color:      "rgba(255,255,255,0.55)",
              lineHeight: 1.75,
              margin:     "0 0 20px",
            }}>
              Collins Asein is a Christian author, faith technologist, and the founder of{" "}
              <Link href="/" style={{ color: "var(--pk-gold, #B07C1F)", textDecoration: "none", fontWeight: 700 }}>
                PrayerKey
              </Link>{" "}
              — an AI-powered prayer and Bible platform serving believers worldwide. His work sits at the
              intersection of deep Christian faith and modern technology, believing that the tools of today
              should amplify the Gospel, not replace it.
            </p>

            {/* Stats */}
            <div style={{ display: "flex", gap: "24px", flexWrap: "wrap" }}>
              {[
                { label: "Prayer Topics",    value: "120+" },
                { label: "Bible Verses",     value: "31,102" },
                { label: "Prayers Generated","value": "Thousands" },
                { label: "Faith",            value: "Christian" },
              ].map((stat) => (
                <div key={stat.label}>
                  <div style={{ fontSize: "22px", fontWeight: 800, color: "#fff", letterSpacing: "-0.02em" }}>
                    {stat.value}
                  </div>
                  <div style={{ fontSize: "11px", color: "rgba(255,255,255,0.35)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase" }}>
                    {stat.label}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* ── About ── */}
        <section style={{ marginBottom: "60px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 20px", letterSpacing: "-0.02em" }}>
            About Collins
          </h2>
          <div style={{
            background:   "rgba(255,255,255,0.025)",
            border:       "1.5px solid rgba(255,255,255,0.07)",
            borderRadius: "16px",
            padding:      "clamp(24px,4vw,40px)",
          }}>
            <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.7)", lineHeight: 1.85, margin: "0 0 16px" }}>
              Collins Asein built PrayerKey from a deeply personal conviction: that prayer is the most
              powerful force available to a believer, and that technology should make it more accessible —
              not less meaningful. As a Christian who has seen prayer transform lives, families, and
              communities, Collins wanted to create a tool that meets people exactly where they are —
              whether they&apos;re a new believer who doesn&apos;t know how to pray, or a pastor preparing a
              sermon at midnight.
            </p>
            <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.7)", lineHeight: 1.85, margin: "0 0 16px" }}>
              PrayerKey was born from that vision: a platform where AI serves the Spirit, where every
              generated prayer is grounded in Scripture, and where technology bows to theology. Collins
              writes extensively on faith, prayer, and the intersection of Christianity with the modern world.
            </p>
            <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.7)", lineHeight: 1.85, margin: 0 }}>
              His belief is simple: <em style={{ color: "#fff", fontStyle: "italic" }}>"The Gospel doesn&apos;t need technology to be powerful. But technology,
              when surrendered to God, can carry the Gospel further than we ever imagined."</em>
            </p>
          </div>
        </section>

        {/* ── Expertise ── */}
        <section style={{ marginBottom: "60px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 20px", letterSpacing: "-0.02em" }}>
            Areas of Expertise
          </h2>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: "12px" }}>
            {[
              { icon: "🙏", label: "Christian Prayer & Intercession" },
              { icon: "📖", label: "Bible Study & Scripture" },
              { icon: "⛪", label: "Church Technology" },
              { icon: "🤖", label: "AI Tools for Faith" },
              { icon: "🎙️", label: "Sermon Preparation" },
              { icon: "💡", label: "Devotional Writing" },
              { icon: "🌍", label: "Global Christian Outreach" },
              { icon: "✝️", label: "Apologetics & Theology" },
            ].map((item) => (
              <div key={item.label} style={{
                display:      "flex",
                alignItems:   "center",
                gap:          "12px",
                padding:      "14px 18px",
                borderRadius: "10px",
                border:       "1.5px solid rgba(176,124,31,0.2)",
                background:   "rgba(176,124,31,0.04)",
              }}>
                <span style={{ fontSize: "20px" }}>{item.icon}</span>
                <span style={{ fontSize: "13px", fontWeight: 600, color: "rgba(255,255,255,0.75)" }}>{item.label}</span>
              </div>
            ))}
          </div>
        </section>

        {/* ── Prayers by Collins ── */}
        <section style={{ marginBottom: "60px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 6px", letterSpacing: "-0.02em" }}>
            Prayers Written by Collins Asein
          </h2>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.4)", margin: "0 0 20px" }}>
            Every prayer on PrayerKey is rooted in Scripture and written with pastoral care.
          </p>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "10px" }}>
            {FEATURED_PRAYERS.map((topic) => (
              <Link
                key={topic.slug}
                href={`/pray/${topic.slug}`}
                style={{
                  display:      "block",
                  padding:      "16px 18px",
                  borderRadius: "10px",
                  border:       "1.5px solid rgba(255,255,255,0.07)",
                  background:   "rgba(255,255,255,0.025)",
                  textDecoration:"none",
                  transition:   "all 150ms",
                }}
              >
                <div style={{ fontSize: "11px", fontWeight: 700, color: "rgba(176,124,31,0.7)", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: "6px" }}>
                  {topic.category}
                </div>
                <div style={{ fontSize: "14px", fontWeight: 600, color: "rgba(255,255,255,0.8)" }}>
                  🙏 {topic.title}
                </div>
              </Link>
            ))}
          </div>
          <div style={{ marginTop: "16px", textAlign: "center" }}>
            <Link href="/pray/topics" style={{ color: "var(--pk-gold, #B07C1F)", fontSize: "13px", fontWeight: 600, textDecoration: "none" }}>
              View all 120+ prayers →
            </Link>
          </div>
        </section>

        {/* ── Articles ── */}
        <section style={{ marginBottom: "60px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 20px", letterSpacing: "-0.02em" }}>
            Featured Writing
          </h2>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {ARTICLES.map((article) => (
              <Link
                key={article.href}
                href={article.href}
                style={{
                  display:        "flex",
                  justifyContent: "space-between",
                  alignItems:     "center",
                  padding:        "18px 24px",
                  borderRadius:   "12px",
                  border:         "1.5px solid rgba(255,255,255,0.07)",
                  background:     "rgba(255,255,255,0.02)",
                  textDecoration: "none",
                  gap:            "16px",
                  transition:     "all 150ms",
                }}
              >
                <span style={{ fontSize: "15px", fontWeight: 600, color: "rgba(255,255,255,0.8)" }}>
                  📝 {article.title}
                </span>
                <span style={{ fontSize: "12px", color: "rgba(255,255,255,0.3)", flexShrink: 0 }}>
                  {article.date}
                </span>
              </Link>
            ))}
          </div>
        </section>

        {/* ── CTA ── */}
        <div style={{
          background:   "linear-gradient(135deg, rgba(175,82,222,0.1) 0%, rgba(176,124,31,0.08) 100%)",
          border:       "1.5px solid rgba(175,82,222,0.2)",
          borderRadius: "20px",
          padding:      "clamp(28px,5vw,48px)",
          textAlign:    "center",
        }}>
          <h2 style={{ fontSize: "24px", fontWeight: 800, color: "#fff", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Experience PrayerKey
          </h2>
          <p style={{ fontSize: "15px", color: "rgba(255,255,255,0.45)", margin: "0 0 24px", lineHeight: 1.7 }}>
            Generate a scripture-based prayer for any situation — free, instant, no account needed.
          </p>
          <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
            <Link
              href="/pray"
              style={{
                display:      "inline-block",
                padding:      "14px 32px",
                borderRadius: "6px",
                background:   "#AF52DE",
                color:        "#fff",
                textDecoration:"none",
                fontSize:     "15px",
                fontWeight:   800,
                boxShadow:    "4px 4px 0 0 rgba(175,82,222,0.3)",
              }}
            >
              ✦ Generate a Prayer
            </Link>
            <Link
              href="/bible"
              style={{
                display:      "inline-block",
                padding:      "14px 32px",
                borderRadius: "6px",
                border:       "1.5px solid rgba(176,124,31,0.4)",
                background:   "rgba(176,124,31,0.06)",
                color:        "var(--pk-gold, #B07C1F)",
                textDecoration:"none",
                fontSize:     "15px",
                fontWeight:   700,
              }}
            >
              📖 Search the Bible
            </Link>
          </div>
        </div>

      </div>
    </>
  );
}
