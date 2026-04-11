import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";

export const metadata: Metadata = {
  title: "All Prayer Topics | PrayerKey — Prayers for Every Situation",
  description: `Browse ${PRAYER_TOPICS.length}+ prayer topics — healing, anxiety, marriage, finances, grief, and more. Find the perfect prayer for every situation, free.`,
  alternates: { canonical: "https://www.prayerkey.com/pray/topics" },
};

// Group by category
const grouped = PRAYER_TOPICS.reduce<Record<string, typeof PRAYER_TOPICS>>((acc, t) => {
  if (!acc[t.category]) acc[t.category] = [];
  acc[t.category].push(t);
  return acc;
}, {});

const CATEGORY_ICONS: Record<string, string> = {
  "Health":       "🏥",
  "Mental Health":"🧠",
  "Daily Prayer": "☀️",
  "Family":       "👨‍👩‍👧",
  "Finance":      "💼",
  "Protection":   "🛡️",
  "Faith":        "✝️",
  "Grief":        "🕊️",
  "Celebration":  "🎉",
  "Education":    "📚",
  "Salvation":    "🙌",
  "Purpose":      "🎯",
  "Relationships":"❤️",
  "Thanksgiving": "🙏",
};

export default function TopicsPage() {
  return (
    <div style={{ maxWidth: "900px", margin: "0 auto", padding: "0 24px 100px" }}>

      {/* Header */}
      <div style={{ textAlign: "center", marginBottom: "56px" }}>
        <div style={{
          display: "inline-block", padding: "4px 12px", borderRadius: "4px",
          border: "1.5px solid rgba(176,124,31,0.35)", background: "rgba(176,124,31,0.06)",
          fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)",
          letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
        }}>
          Prayer Library
        </div>
        <h1 style={{
          fontSize: "clamp(32px, 5vw, 52px)", fontWeight: 800, color: "#fff",
          margin: "0 0 14px", letterSpacing: "-0.03em",
        }}>
          Prayers for Every Situation
        </h1>
        <p style={{ fontSize: "17px", color: "rgba(255,255,255,0.45)", maxWidth: "500px", margin: "0 auto", lineHeight: 1.7 }}>
          Browse {PRAYER_TOPICS.length}+ scripture-based prayers across every area of life — or let our AI write one just for you.
        </p>
      </div>

      {/* Categories */}
      {Object.entries(grouped).map(([category, topics]) => (
        <div key={category} style={{ marginBottom: "48px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "16px" }}>
            <span style={{ fontSize: "18px" }}>{CATEGORY_ICONS[category] ?? "🙏"}</span>
            <h2 style={{ fontSize: "16px", fontWeight: 700, color: "var(--pk-gold)", margin: 0, letterSpacing: "0.04em", textTransform: "uppercase" }}>
              {category}
            </h2>
            <div style={{ flex: 1, height: "1px", background: "rgba(176,124,31,0.15)" }} />
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: "10px" }}>
            {topics.map((t) => (
              <Link
                key={t.slug}
                href={`/pray/${t.slug}`}
                style={{
                  display: "block", padding: "16px 20px", borderRadius: "12px",
                  border: "1.5px solid rgba(255,255,255,0.07)",
                  background: "rgba(255,255,255,0.025)", textDecoration: "none",
                  transition: "all 150ms",
                }}
              >
                <p style={{ fontSize: "14px", fontWeight: 700, color: "#fff", margin: "0 0 4px" }}>
                  {t.title}
                </p>
                <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.35)", margin: 0, lineHeight: 1.5,
                  overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" as const }}>
                  {t.metaDesc}
                </p>
              </Link>
            ))}
          </div>
        </div>
      ))}

      {/* AI CTA */}
      <div style={{
        textAlign: "center", padding: "40px", borderRadius: "20px",
        background: "rgba(175,82,222,0.07)", border: "1.5px solid rgba(175,82,222,0.2)",
      }}>
        <h2 style={{ fontSize: "22px", fontWeight: 700, color: "#fff", margin: "0 0 10px" }}>
          Don&apos;t see your situation?
        </h2>
        <p style={{ fontSize: "15px", color: "rgba(255,255,255,0.45)", margin: "0 0 20px" }}>
          Our AI can write a personalised prayer for any situation in seconds.
        </p>
        <Link
          href="/pray"
          style={{
            display: "inline-block", padding: "14px 32px", borderRadius: "6px",
            background: "#AF52DE", color: "#fff", textDecoration: "none",
            fontSize: "15px", fontWeight: 800, boxShadow: "4px 4px 0 0 rgba(175,82,222,0.3)",
          }}
        >
          ✦ Generate Any Prayer with AI
        </Link>
      </div>
    </div>
  );
}
