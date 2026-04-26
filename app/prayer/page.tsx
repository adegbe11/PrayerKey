import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS, PrayerTopic } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

/* ─────────────────────────────────────────────────────────── */
/*  METADATA                                                   */
/* ─────────────────────────────────────────────────────────── */
export const metadata: Metadata = {
  title: "Prayer for Every Situation | 544+ Prayers — PrayerKey",
  description:
    "Find the perfect prayer for any situation in life. 544+ scripture-based prayers across 16 categories — healing, anxiety, marriage, finances, protection, grief, and more.",
  keywords:
    "prayers for every situation, prayer for healing, prayer for anxiety, prayer for strength, prayer for marriage, prayer for finances, prayer for protection, daily prayers, christian prayers",
  authors: [{ name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` }],
  alternates: { canonical: `${BASE_URL}/prayer` },
  openGraph: {
    title:       "Prayer for Every Situation | 544+ Prayers — PrayerKey",
    description: "Find the perfect prayer for any situation in life. 544+ scripture-based prayers across 16 categories.",
    url:         `${BASE_URL}/prayer`,
    siteName:    "PrayerKey",
    images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
    type:        "website",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Prayer for Every Situation | 544+ Prayers — PrayerKey",
    description: "544+ scripture-based prayers across 16 categories — healing, anxiety, marriage, finances, and more.",
  },
};

/* ─────────────────────────────────────────────────────────── */
/*  DATA                                                       */
/* ─────────────────────────────────────────────────────────── */

/** Category metadata — description only, no emoji */
const CATEGORY_META: Record<string, { desc: string }> = {
  "Health":        { desc: "Healing, recovery, sickness & body"          },
  "Mental Health": { desc: "Anxiety, depression, peace of mind"           },
  "Daily Prayer":  { desc: "Morning, night, short & powerful prayers"     },
  "Family":        { desc: "Children, parents, home & unity"              },
  "Finance":       { desc: "Breakthrough, debt, provision & wealth"       },
  "Protection":    { desc: "Safety, travel, spiritual warfare & home"     },
  "Faith":         { desc: "Salvation, trust, scripture & belief"         },
  "Grief":         { desc: "Loss, death, funeral & bereavement"           },
  "Celebration":   { desc: "Birthday, graduation, new job & milestones"   },
  "Education":     { desc: "Exams, students, wisdom & learning"           },
  "Salvation":     { desc: "Repentance, forgiveness & new life in Christ" },
  "Purpose":       { desc: "Direction, calling, vision & destiny"         },
  "Relationships": { desc: "Marriage, friendship, love & reconciliation"  },
  "Thanksgiving":  { desc: "Gratitude, praise & worship"                  },
  "Ministry":      { desc: "Church, pastors, evangelism & mission"        },
  "Nation":        { desc: "Government, leaders, peace & revival"         },
};

/** Time-of-day quick access */
const BY_TIME = [
  { label: "Morning Prayer",  slug: "morning",        desc: "Start your day with God"   },
  { label: "Night Prayer",    slug: "night",          desc: "Rest in His peace"          },
  { label: "Daily Prayer",    slug: "daily",          desc: "Stay connected all day"     },
  { label: "Short Prayer",    slug: "short-prayer",   desc: "One-minute prayers"         },
  { label: "Powerful Prayer", slug: "powerful-prayer",desc: "Bold, faith-filled prayer"  },
  { label: "Fasting Prayer",  slug: "fasting",        desc: "Pray with fasting"          },
];

/** Emotion / mood quick access — label only, no emoji */
const BY_MOOD = [
  { label: "Anxious",   slug: "anxiety"    },
  { label: "Depressed", slug: "depression" },
  { label: "Grieving",  slug: "grief"      },
  { label: "Angry",     slug: "anger"      },
  { label: "Lonely",    slug: "loneliness" },
  { label: "Afraid",    slug: "fear"       },
  { label: "Hopeful",   slug: "hope"       },
  { label: "Grateful",  slug: "gratitude"  },
  { label: "Stressed",  slug: "stress"     },
  { label: "Confused",  slug: "guidance"   },
  { label: "Joyful",    slug: "joy"        },
  { label: "Sick",      slug: "healing"    },
];

/** Popular prayers — top searches */
const POPULAR_SLUGS = [
  "healing", "anxiety", "strength", "protection", "morning",
  "marriage", "financial-breakthrough", "depression", "forgiveness",
  "salvation", "guidance", "gratitude", "sleep", "fear", "hope",
];

/* Group topics by category */
function groupByCategory(topics: PrayerTopic[]): Record<string, PrayerTopic[]> {
  return topics.reduce<Record<string, PrayerTopic[]>>((acc, t) => {
    (acc[t.category] ??= []).push(t);
    return acc;
  }, {});
}

/* ─────────────────────────────────────────────────────────── */
/*  PAGE                                                       */
/* ─────────────────────────────────────────────────────────── */
export default function PrayerHubPage() {
  const grouped  = groupByCategory(PRAYER_TOPICS);
  const categories = Object.keys(CATEGORY_META);
  const popular  = POPULAR_SLUGS
    .map(s => PRAYER_TOPICS.find(t => t.slug === s))
    .filter(Boolean) as PrayerTopic[];

  /* JSON-LD */
  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type":       "CollectionPage",
        "@id":         `${BASE_URL}/prayer`,
        "url":         `${BASE_URL}/prayer`,
        "name":        "Prayer for Every Situation — PrayerKey",
        "description": `${PRAYER_TOPICS.length} scripture-based prayers across ${categories.length} categories for every situation in life.`,
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
        "hasPart": PRAYER_TOPICS.map(t => ({
          "@type": "Article",
          "name":  t.title,
          "url":   `${BASE_URL}/prayer/${t.slug}`,
        })),
      },
      {
        "@type":           "BreadcrumbList",
        "itemListElement": [
          { "@type": "ListItem", "position": 1, "name": "Home",   "item": BASE_URL },
          { "@type": "ListItem", "position": 2, "name": "Prayer", "item": `${BASE_URL}/prayer` },
        ],
      },
    ],
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <div style={{ maxWidth: "1100px", margin: "0 auto", padding: "0 24px 120px" }}>

        {/* ═══════════════════════════════════════════════════ */}
        {/* HERO                                               */}
        {/* ═══════════════════════════════════════════════════ */}
        <div style={{ textAlign: "center", marginBottom: "64px", paddingTop: "8px" }}>
          <div style={{
            display:      "inline-block",
            padding:      "4px 14px",
            borderRadius: "4px",
            border:       "1.5px solid var(--pk-accent-border)",
            background:   "var(--pk-accent-dim)",
            fontSize:     "10px",
            fontWeight:   700,
            color:        "var(--pk-accent)",
            letterSpacing:"0.12em",
            textTransform:"uppercase",
            marginBottom: "20px",
          }}>
            ✦ PrayerKey Prayers
          </div>

          <h1 style={{
            fontSize:      "clamp(36px, 6vw, 64px)",
            fontWeight:    800,
            color:         "var(--pk-text)",
            margin:        "0 0 16px",
            letterSpacing: "-0.035em",
            lineHeight:    1.05,
          }}>
            Find a prayer for<br />every situation in life
          </h1>

          <p style={{
            fontSize:     "clamp(15px, 1.5vw, 18px)",
            color:        "var(--pk-text-2)",
            margin:       "0 0 28px",
            lineHeight:   1.7,
            maxWidth:     "560px",
            marginInline: "auto",
          }}>
            {PRAYER_TOPICS.length} scripture-based prayers across {categories.length} categories —
            for healing, anxiety, marriage, finances, grief, and every moment in between.
          </p>

          {/* Stats row */}
          <div style={{ display: "flex", gap: "32px", justifyContent: "center", flexWrap: "wrap", marginBottom: "32px" }}>
            {[
              { n: `${PRAYER_TOPICS.length}+`, label: "Prayers" },
              { n: `${categories.length}`,     label: "Categories" },
              { n: "31,102",                   label: "Bible Verses" },
              { n: "Free",                     label: "Always" },
            ].map(s => (
              <div key={s.label} style={{ textAlign: "center" }}>
                <div style={{ fontSize: "clamp(22px,3vw,32px)", fontWeight: 800, color: "var(--pk-text)", letterSpacing: "-0.03em" }}>{s.n}</div>
                <div style={{ fontSize: "11px", color: "var(--pk-text-3)", fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase" }}>{s.label}</div>
              </div>
            ))}
          </div>

          {/* CTA */}
          <Link
            href="/pray"
            style={{
              display:      "inline-flex",
              alignItems:   "center",
              gap:          "8px",
              padding:      "14px 32px",
              borderRadius: "6px",
              background:   "var(--pk-accent)",
              color:        "#fff",
              textDecoration:"none",
              fontSize:     "15px",
              fontWeight:   800,
              boxShadow:    "4px 4px 0 0 var(--pk-accent-border)",
              letterSpacing:"-0.01em",
            }}
          >
            ✦ Generate My Personal Prayer
          </Link>
        </div>

        {/* ═══════════════════════════════════════════════════ */}
        {/* BROWSE BY CATEGORY                                 */}
        {/* ═══════════════════════════════════════════════════ */}
        <section style={{ marginBottom: "64px" }}>
          <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", marginBottom: "20px", flexWrap: "wrap", gap: "8px" }}>
            <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: 0, letterSpacing: "-0.02em" }}>
              Browse by Category
            </h2>
            <span style={{ fontSize: "13px", color: "var(--pk-text-3)" }}>{categories.length} categories</span>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "14px" }}>
            {categories.map(cat => {
              const meta    = CATEGORY_META[cat];
              const prayers = grouped[cat] ?? [];
              return (
                <div key={cat} style={{
                  padding:      "20px 22px",
                  borderRadius: "14px",
                  border:       "1.5px solid var(--pk-border)",
                  background:   "var(--pk-card)",
                  transition:   "all 150ms",
                }}>
                  <div style={{ marginBottom: "12px" }}>
                    <div style={{ fontSize: "13px", fontWeight: 800, color: "var(--pk-text)", letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "3px" }}>{cat}</div>
                    {meta && <div style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>{meta.desc}</div>}
                  </div>

                  <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginBottom: "14px" }}>
                    {prayers.slice(0, 5).map(p => (
                      <Link
                        key={p.slug}
                        href={`/prayer/${p.slug}`}
                        style={{
                          fontSize:     "12px",
                          color:        "var(--pk-text-2)",
                          textDecoration:"none",
                          padding:      "4px 10px",
                          borderRadius: "4px",
                          background:   "var(--pk-surface-2)",
                          border:       "1px solid var(--pk-border)",
                          transition:   "all 120ms",
                        }}
                      >
                        {p.title.replace("Prayer for ", "").replace("Prayer ", "")}
                      </Link>
                    ))}
                    {prayers.length > 5 && (
                      <span style={{ fontSize: "12px", color: "var(--pk-text-3)", padding: "4px 6px" }}>
                        +{prayers.length - 5} more
                      </span>
                    )}
                  </div>

                  <div style={{ borderTop: "1px solid var(--pk-border)", paddingTop: "12px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ fontSize: "12px", color: "var(--pk-accent)", fontWeight: 600 }}>
                      {prayers.length} prayers
                    </span>
                    <Link
                      href={`/prayer/${prayers[0]?.slug ?? ""}`}
                      style={{ fontSize: "12px", color: "var(--pk-text-3)", textDecoration: "none", fontWeight: 600 }}
                    >
                      Browse prayers →
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* ═══════════════════════════════════════════════════ */}
        {/* BROWSE BY TIME OF DAY                              */}
        {/* ═══════════════════════════════════════════════════ */}
        <section style={{ marginBottom: "64px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 20px", letterSpacing: "-0.02em" }}>
            Browse by Time of Day
          </h2>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(170px, 1fr))", gap: "12px" }}>
            {BY_TIME.map(t => (
              <Link
                key={t.slug}
                href={`/prayer/${t.slug}`}
                style={{
                  display:      "block",
                  padding:      "18px 20px",
                  borderRadius: "12px",
                  border:       "1.5px solid var(--pk-border)",
                  background:   "var(--pk-card)",
                  textDecoration:"none",
                  transition:   "all 150ms",
                }}
              >
                <div style={{ fontSize: "10px", fontWeight: 800, color: "var(--pk-accent)", letterSpacing: "0.14em", textTransform: "uppercase", marginBottom: "8px" }}>✦</div>
                <div style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)", marginBottom: "4px", letterSpacing: "-0.01em" }}>{t.label}</div>
                <div style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>{t.desc}</div>
                <div style={{ fontSize: "12px", color: "var(--pk-accent)", fontWeight: 600, marginTop: "10px" }}>
                  Pray now →
                </div>
              </Link>
            ))}
          </div>
        </section>

        {/* ═══════════════════════════════════════════════════ */}
        {/* BROWSE BY HOW YOU FEEL                             */}
        {/* ═══════════════════════════════════════════════════ */}
        <section style={{ marginBottom: "64px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 8px", letterSpacing: "-0.02em" }}>
            Browse by How You Feel
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 20px" }}>
            Find a prayer that meets you exactly where you are.
          </p>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
            {BY_MOOD.map(m => (
              <Link
                key={m.slug}
                href={`/prayer/${m.slug}`}
                style={{
                  display:       "inline-flex",
                  alignItems:    "center",
                  gap:           "8px",
                  padding:       "10px 18px",
                  borderRadius:  "50px",
                  border:        "1.5px solid var(--pk-border)",
                  background:    "var(--pk-card)",
                  color:         "var(--pk-text-2)",
                  textDecoration:"none",
                  fontSize:      "14px",
                  fontWeight:    600,
                  transition:    "all 150ms",
                }}
              >
                {m.label}
              </Link>
            ))}
          </div>
        </section>

        {/* ═══════════════════════════════════════════════════ */}
        {/* POPULAR PRAYERS RIGHT NOW                          */}
        {/* ═══════════════════════════════════════════════════ */}
        <section style={{ marginBottom: "64px" }}>
          <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", marginBottom: "20px", flexWrap: "wrap", gap: "8px" }}>
            <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: 0, letterSpacing: "-0.02em" }}>
              Popular Prayers Right Now
            </h2>
            <Link href="/pray/topics" style={{ fontSize: "13px", color: "var(--pk-accent)", textDecoration: "none", fontWeight: 600 }}>
              Browse all {PRAYER_TOPICS.length} →
            </Link>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "12px" }}>
            {popular.map((p, i) => (
              <Link
                key={p.slug}
                href={`/prayer/${p.slug}`}
                style={{
                  display:       "flex",
                  alignItems:    "center",
                  gap:           "14px",
                  padding:       "16px 18px",
                  borderRadius:  "12px",
                  border:        "1.5px solid var(--pk-border)",
                  background:    "var(--pk-card)",
                  textDecoration:"none",
                  transition:    "all 150ms",
                }}
              >
                <span style={{
                  fontSize:     "11px",
                  fontWeight:   800,
                  color:        "var(--pk-text-3)",
                  width:        "20px",
                  flexShrink:   0,
                  fontVariantNumeric: "tabular-nums",
                }}>
                  {String(i + 1).padStart(2, "0")}
                </span>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)", marginBottom: "2px", letterSpacing: "-0.01em" }}>
                    {p.title}
                  </div>
                  <div style={{ fontSize: "11px", color: "var(--pk-text-3)", textTransform: "uppercase", letterSpacing: "0.06em" }}>
                    {p.category}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </section>

        {/* ═══════════════════════════════════════════════════ */}
        {/* ALL PRAYERS — FULL DIRECTORY BY CATEGORY           */}
        {/* ═══════════════════════════════════════════════════ */}
        <section style={{ marginBottom: "64px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 8px", letterSpacing: "-0.02em" }}>
            All {PRAYER_TOPICS.length} Prayers
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 32px" }}>
            Every prayer on PrayerKey, organized by life category.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "40px" }}>
            {categories.map(cat => {
              const meta    = CATEGORY_META[cat];
              const prayers = grouped[cat] ?? [];
              return (
                <div key={cat}>
                  {/* Category header — pure typography */}
                  <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "14px", paddingBottom: "10px", borderBottom: "1px solid var(--pk-border)" }}>
                    <h3 style={{ fontSize: "13px", fontWeight: 800, color: "var(--pk-text)", margin: 0, letterSpacing: "0.08em", textTransform: "uppercase" }}>{cat}</h3>
                    {meta && <span style={{ fontSize: "12px", color: "var(--pk-text-3)", fontWeight: 500 }}>{meta.desc}</span>}
                    <span style={{ fontSize: "11px", color: "var(--pk-accent)", fontWeight: 700, marginLeft: "auto", letterSpacing: "0.04em" }}>
                      {prayers.length}
                    </span>
                  </div>

                  {/* Prayer grid */}
                  <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: "8px" }}>
                    {prayers.map(p => (
                      <Link
                        key={p.slug}
                        href={`/prayer/${p.slug}`}
                        style={{
                          display:       "flex",
                          alignItems:    "center",
                          gap:           "10px",
                          padding:       "12px 14px",
                          borderRadius:  "8px",
                          border:        "1px solid var(--pk-border)",
                          background:    "var(--pk-card)",
                          textDecoration:"none",
                          transition:    "all 120ms",
                        }}
                      >
                        <span style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-text-2)", letterSpacing: "-0.01em" }}>
                          {p.title}
                        </span>
                      </Link>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* ═══════════════════════════════════════════════════ */}
        {/* GENERATE CTA                                       */}
        {/* ═══════════════════════════════════════════════════ */}
        <div style={{
          background:   "var(--pk-accent-dim)",
          border:       "1.5px solid var(--pk-accent-border)",
          borderRadius: "20px",
          padding:      "clamp(32px,5vw,56px)",
          textAlign:    "center",
        }}>
          <div style={{ fontSize: "13px", fontWeight: 800, color: "var(--pk-accent)", letterSpacing: "0.16em", textTransform: "uppercase", marginBottom: "16px" }}>✦ AI Prayer Generator</div>
          <h2 style={{ fontSize: "clamp(22px,3vw,32px)", fontWeight: 800, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.025em" }}>
            Don&apos;t see your situation?
          </h2>
          <p style={{ fontSize: "16px", color: "var(--pk-text-2)", margin: "0 0 28px", lineHeight: 1.7, maxWidth: "480px", marginInline: "auto" }}>
            Our AI prayer generator writes a personalised prayer for <em>any</em> situation you&apos;re facing — in seconds, free, no account needed.
          </p>
          <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
            <Link
              href="/pray"
              style={{
                display:      "inline-block",
                padding:      "16px 36px",
                borderRadius: "6px",
                background:   "var(--pk-accent)",
                color:        "#fff",
                textDecoration:"none",
                fontSize:     "16px",
                fontWeight:   800,
                boxShadow:    "4px 4px 0 0 var(--pk-accent-border)",
                letterSpacing:"-0.01em",
              }}
            >
              ✦ Generate My Prayer
            </Link>
            <Link
              href="/bible"
              style={{
                display:      "inline-block",
                padding:      "16px 36px",
                borderRadius: "6px",
                border:       "1.5px solid var(--pk-gold-border)",
                background:   "var(--pk-gold-dim)",
                color:        "var(--pk-gold)",
                textDecoration:"none",
                fontSize:     "16px",
                fontWeight:   700,
              }}
            >
              Search the Bible →
            </Link>
          </div>
        </div>

      </div>

      <style>{`
        a:hover { opacity: 0.85; }
      `}</style>
    </>
  );
}
