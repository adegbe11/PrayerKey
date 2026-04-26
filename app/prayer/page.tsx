import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS, PrayerTopic } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";

/* ─────────────────────────────────────────────────────────── */
/*  METADATA                                                   */
/* ─────────────────────────────────────────────────────────── */
export const metadata: Metadata = {
  title: "Prayer for Every Situation | 116 Prayers — PrayerKey",
  description:
    "Find the perfect prayer for any situation in life. 116 scripture-based prayers across 16 categories — healing, anxiety, marriage, finances, protection, grief, and more.",
  keywords:
    "prayers for every situation, prayer for healing, prayer for anxiety, prayer for strength, prayer for marriage, prayer for finances, prayer for protection, daily prayers, christian prayers",
  authors: [{ name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` }],
  alternates: { canonical: `${BASE_URL}/prayer` },
  openGraph: {
    title:       "Prayer for Every Situation | 116 Prayers — PrayerKey",
    description: "Find the perfect prayer for any situation in life. 116 scripture-based prayers across 16 categories.",
    url:         `${BASE_URL}/prayer`,
    siteName:    "PrayerKey",
    images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
    type:        "website",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Prayer for Every Situation | 116 Prayers — PrayerKey",
    description: "116 scripture-based prayers across 16 categories — healing, anxiety, marriage, finances, and more.",
  },
};

/* ─────────────────────────────────────────────────────────── */
/*  DATA                                                       */
/* ─────────────────────────────────────────────────────────── */

/** Category metadata — icon, description, color accent */
const CATEGORY_META: Record<string, { icon: string; desc: string; color: string }> = {
  "Health":        { icon: "🩺", desc: "Healing, recovery, sickness & body",         color: "#34C759" },
  "Mental Health": { icon: "🧠", desc: "Anxiety, depression, peace of mind",          color: "#5E5CE6" },
  "Daily Prayer":  { icon: "🌅", desc: "Morning, night, short & powerful prayers",    color: "#FF9F0A" },
  "Family":        { icon: "👨‍👩‍👧‍👦", desc: "Children, parents, home & unity",           color: "#FF6B35" },
  "Finance":       { icon: "💰", desc: "Breakthrough, debt, provision & wealth",      color: "#30D158" },
  "Protection":    { icon: "🛡️", desc: "Safety, travel, spiritual warfare & home",    color: "#0A84FF" },
  "Faith":         { icon: "✝️", desc: "Salvation, trust, scripture & belief",        color: "#BF5AF2" },
  "Grief":         { icon: "🕊️", desc: "Loss, death, funeral & bereavement",         color: "#98989D" },
  "Celebration":   { icon: "🎉", desc: "Birthday, graduation, new job & milestones",  color: "#FF375F" },
  "Education":     { icon: "📚", desc: "Exams, students, wisdom & learning",          color: "#64D2FF" },
  "Salvation":     { icon: "🙏", desc: "Repentance, forgiveness & new life in Christ",color: "#B07C1F" },
  "Purpose":       { icon: "🎯", desc: "Direction, calling, vision & destiny",        color: "#FF9F0A" },
  "Relationships": { icon: "❤️", desc: "Marriage, friendship, love & reconciliation", color: "#FF375F" },
  "Thanksgiving":  { icon: "🌻", desc: "Gratitude, praise & worship",                 color: "#FFD60A" },
  "Ministry":      { icon: "⛪", desc: "Church, pastors, evangelism & mission",       color: "#AF52DE" },
  "Nation":        { icon: "🌍", desc: "Government, leaders, peace & revival",        color: "#32ADE6" },
};

/** Time-of-day quick access */
const BY_TIME = [
  { label: "Morning Prayer",   slug: "morning",       icon: "🌅", desc: "Start your day with God" },
  { label: "Night Prayer",     slug: "night",         icon: "🌙", desc: "Rest in His peace" },
  { label: "Daily Prayer",     slug: "daily",         icon: "📅", desc: "Stay connected all day" },
  { label: "Short Prayer",     slug: "short-prayer",  icon: "⚡", desc: "One-minute prayers" },
  { label: "Powerful Prayer",  slug: "powerful-prayer",icon: "🔥", desc: "Bold, faith-filled prayer" },
  { label: "Fasting Prayer",   slug: "fasting",       icon: "✋", desc: "Pray with fasting" },
];

/** Emotion / mood quick access */
const BY_MOOD = [
  { label: "Anxious",   slug: "anxiety",    emoji: "😰" },
  { label: "Depressed", slug: "depression", emoji: "😔" },
  { label: "Grieving",  slug: "grief",      emoji: "😢" },
  { label: "Angry",     slug: "anger",      emoji: "😤" },
  { label: "Lonely",    slug: "loneliness", emoji: "🥺" },
  { label: "Afraid",    slug: "fear",       emoji: "😨" },
  { label: "Hopeful",   slug: "hope",       emoji: "🌟" },
  { label: "Grateful",  slug: "gratitude",  emoji: "🙌" },
  { label: "Stressed",  slug: "stress",     emoji: "😩" },
  { label: "Confused",  slug: "guidance",   emoji: "🤔" },
  { label: "Joyful",    slug: "joy",        emoji: "😄" },
  { label: "Sick",      slug: "healing",    emoji: "🤒" },
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

  const gold   = "#B07C1F";
  const purple = "#AF52DE";

  /* JSON-LD */
  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type":       "CollectionPage",
        "@id":         `${BASE_URL}/prayer`,
        "url":         `${BASE_URL}/prayer`,
        "name":        "Prayer for Every Situation — PrayerKey",
        "description": "116 scripture-based prayers across 16 categories for every situation in life.",
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
            border:       `1.5px solid rgba(176,124,31,0.35)`,
            background:   "rgba(176,124,31,0.06)",
            fontSize:     "10px",
            fontWeight:   700,
            color:        gold,
            letterSpacing:"0.12em",
            textTransform:"uppercase",
            marginBottom: "20px",
          }}>
            ✦ PrayerKey Prayers
          </div>

          <h1 style={{
            fontSize:      "clamp(36px, 6vw, 64px)",
            fontWeight:    800,
            color:         "#fff",
            margin:        "0 0 16px",
            letterSpacing: "-0.035em",
            lineHeight:    1.05,
          }}>
            Find a prayer for<br />every situation in life
          </h1>

          <p style={{
            fontSize:     "clamp(15px, 1.5vw, 18px)",
            color:        "rgba(255,255,255,0.45)",
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
                <div style={{ fontSize: "clamp(22px,3vw,32px)", fontWeight: 800, color: "#fff", letterSpacing: "-0.03em" }}>{s.n}</div>
                <div style={{ fontSize: "11px", color: "rgba(255,255,255,0.3)", fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase" }}>{s.label}</div>
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
              background:   purple,
              color:        "#fff",
              textDecoration:"none",
              fontSize:     "15px",
              fontWeight:   800,
              boxShadow:    `4px 4px 0 0 rgba(175,82,222,0.3)`,
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
            <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: 0, letterSpacing: "-0.02em" }}>
              Browse by Category
            </h2>
            <span style={{ fontSize: "13px", color: "rgba(255,255,255,0.3)" }}>{categories.length} categories</span>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "14px" }}>
            {categories.map(cat => {
              const meta    = CATEGORY_META[cat];
              const prayers = grouped[cat] ?? [];
              return (
                <div key={cat} style={{
                  padding:      "20px 22px",
                  borderRadius: "14px",
                  border:       `1.5px solid rgba(255,255,255,0.07)`,
                  background:   "rgba(255,255,255,0.025)",
                  transition:   "all 150ms",
                }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "10px" }}>
                    <span style={{ fontSize: "24px" }}>{meta.icon}</span>
                    <div>
                      <div style={{ fontSize: "15px", fontWeight: 700, color: "#fff" }}>{cat}</div>
                      <div style={{ fontSize: "11px", color: "rgba(255,255,255,0.35)" }}>{meta.desc}</div>
                    </div>
                  </div>

                  <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginBottom: "14px" }}>
                    {prayers.slice(0, 5).map(p => (
                      <Link
                        key={p.slug}
                        href={`/prayer/${p.slug}`}
                        style={{
                          fontSize:     "12px",
                          color:        "rgba(255,255,255,0.55)",
                          textDecoration:"none",
                          padding:      "4px 10px",
                          borderRadius: "4px",
                          background:   "rgba(255,255,255,0.04)",
                          border:       "1px solid rgba(255,255,255,0.07)",
                          transition:   "all 120ms",
                        }}
                      >
                        {p.title.replace("Prayer for ", "").replace("Prayer ", "")}
                      </Link>
                    ))}
                    {prayers.length > 5 && (
                      <span style={{ fontSize: "12px", color: "rgba(255,255,255,0.25)", padding: "4px 6px" }}>
                        +{prayers.length - 5} more
                      </span>
                    )}
                  </div>

                  <div style={{ borderTop: "1px solid rgba(255,255,255,0.05)", paddingTop: "12px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ fontSize: "12px", color: `${meta.color}`, fontWeight: 600 }}>
                      {prayers.length} prayers
                    </span>
                    <Link
                      href={`/prayer/${prayers[0]?.slug ?? ""}`}
                      style={{ fontSize: "12px", color: "rgba(255,255,255,0.4)", textDecoration: "none", fontWeight: 600 }}
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
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 20px", letterSpacing: "-0.02em" }}>
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
                  border:       "1.5px solid rgba(255,255,255,0.08)",
                  background:   "rgba(255,255,255,0.025)",
                  textDecoration:"none",
                  transition:   "all 150ms",
                }}
              >
                <div style={{ fontSize: "28px", marginBottom: "8px" }}>{t.icon}</div>
                <div style={{ fontSize: "14px", fontWeight: 700, color: "#fff", marginBottom: "4px" }}>{t.label}</div>
                <div style={{ fontSize: "12px", color: "rgba(255,255,255,0.35)" }}>{t.desc}</div>
                <div style={{ fontSize: "12px", color: gold, fontWeight: 600, marginTop: "10px" }}>
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
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 8px", letterSpacing: "-0.02em" }}>
            Browse by How You Feel
          </h2>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.35)", margin: "0 0 20px" }}>
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
                  border:        "1.5px solid rgba(255,255,255,0.09)",
                  background:    "rgba(255,255,255,0.03)",
                  color:         "rgba(255,255,255,0.7)",
                  textDecoration:"none",
                  fontSize:      "14px",
                  fontWeight:    600,
                  transition:    "all 150ms",
                }}
              >
                <span style={{ fontSize: "18px" }}>{m.emoji}</span>
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
            <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: 0, letterSpacing: "-0.02em" }}>
              Popular Prayers Right Now
            </h2>
            <Link href="/pray/topics" style={{ fontSize: "13px", color: gold, textDecoration: "none", fontWeight: 600 }}>
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
                  border:        "1.5px solid rgba(255,255,255,0.07)",
                  background:    "rgba(255,255,255,0.025)",
                  textDecoration:"none",
                  transition:    "all 150ms",
                }}
              >
                <span style={{
                  fontSize:     "11px",
                  fontWeight:   800,
                  color:        "rgba(255,255,255,0.2)",
                  width:        "20px",
                  flexShrink:   0,
                  fontVariantNumeric: "tabular-nums",
                }}>
                  {String(i + 1).padStart(2, "0")}
                </span>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: "14px", fontWeight: 700, color: "#fff", marginBottom: "2px" }}>
                    🙏 {p.title}
                  </div>
                  <div style={{ fontSize: "11px", color: "rgba(255,255,255,0.3)", textTransform: "uppercase", letterSpacing: "0.06em" }}>
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
          <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 8px", letterSpacing: "-0.02em" }}>
            All {PRAYER_TOPICS.length} Prayers
          </h2>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.35)", margin: "0 0 32px" }}>
            Every prayer on PrayerKey, organized by life category.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "40px" }}>
            {categories.map(cat => {
              const meta    = CATEGORY_META[cat];
              const prayers = grouped[cat] ?? [];
              return (
                <div key={cat}>
                  {/* Category header */}
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "14px", paddingBottom: "10px", borderBottom: "1px solid rgba(255,255,255,0.06)" }}>
                    <span style={{ fontSize: "20px" }}>{meta.icon}</span>
                    <h3 style={{ fontSize: "16px", fontWeight: 700, color: "#fff", margin: 0 }}>{cat}</h3>
                    <span style={{ fontSize: "12px", color: `${meta.color}`, fontWeight: 600, marginLeft: "auto" }}>
                      {prayers.length} prayers
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
                          border:        "1px solid rgba(255,255,255,0.06)",
                          background:    "rgba(255,255,255,0.018)",
                          textDecoration:"none",
                          transition:    "all 120ms",
                        }}
                      >
                        <span style={{ fontSize: "16px", flexShrink: 0 }}>🙏</span>
                        <span style={{ fontSize: "13px", fontWeight: 600, color: "rgba(255,255,255,0.75)" }}>
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
          background:   `linear-gradient(135deg, rgba(175,82,222,0.12) 0%, rgba(176,124,31,0.08) 100%)`,
          border:       `1.5px solid rgba(175,82,222,0.2)`,
          borderRadius: "20px",
          padding:      "clamp(32px,5vw,56px)",
          textAlign:    "center",
        }}>
          <div style={{ fontSize: "40px", marginBottom: "16px" }}>🙏</div>
          <h2 style={{ fontSize: "clamp(22px,3vw,32px)", fontWeight: 800, color: "#fff", margin: "0 0 12px", letterSpacing: "-0.025em" }}>
            Don&apos;t see your situation?
          </h2>
          <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.45)", margin: "0 0 28px", lineHeight: 1.7, maxWidth: "480px", marginInline: "auto" }}>
            Our AI prayer generator writes a personalised prayer for <em>any</em> situation you&apos;re facing — in seconds, free, no account needed.
          </p>
          <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
            <Link
              href="/pray"
              style={{
                display:      "inline-block",
                padding:      "16px 36px",
                borderRadius: "6px",
                background:   purple,
                color:        "#fff",
                textDecoration:"none",
                fontSize:     "16px",
                fontWeight:   800,
                boxShadow:    `4px 4px 0 0 rgba(175,82,222,0.3)`,
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
                border:       `1.5px solid rgba(176,124,31,0.4)`,
                background:   "rgba(176,124,31,0.06)",
                color:        gold,
                textDecoration:"none",
                fontSize:     "16px",
                fontWeight:   700,
              }}
            >
              📖 Search the Bible
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
