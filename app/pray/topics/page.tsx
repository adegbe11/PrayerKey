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

export default function TopicsPage() {
  return (
    <div style={{ maxWidth: "900px", margin: "0 auto", padding: "0 24px 100px" }}>

      {/* Header */}
      <div style={{ textAlign: "center", marginBottom: "64px" }}>
        <div style={{
          display: "inline-block", padding: "4px 14px", borderRadius: "3px",
          border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
          fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
          letterSpacing: "0.16em", textTransform: "uppercase", marginBottom: "20px",
        }}>
          Prayer Library
        </div>
        <h1 style={{
          fontSize: "clamp(32px, 5vw, 56px)", fontWeight: 800, color: "var(--pk-text)",
          margin: "0 0 16px", letterSpacing: "-0.03em", lineHeight: 1.05,
        }}>
          Prayers for Every Situation
        </h1>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", maxWidth: "480px", margin: "0 auto", lineHeight: 1.75 }}>
          {PRAYER_TOPICS.length}+ scripture-based prayers across every area of life.
        </p>
      </div>

      {/* Categories */}
      {Object.entries(grouped).map(([category, topics], catIdx) => (
        <div key={category} style={{ marginBottom: "52px" }}>

          {/* Category header — pure typography, no emoji */}
          <div style={{ display: "flex", alignItems: "center", gap: "14px", marginBottom: "18px" }}>
            <span style={{
              fontSize:      "10px",
              fontWeight:    800,
              color:         "var(--pk-accent)",
              letterSpacing: "0.18em",
              textTransform: "uppercase",
              flexShrink:    0,
              fontVariantNumeric: "tabular-nums",
            }}>
              {String(catIdx + 1).padStart(2, "0")}
            </span>
            <h2 style={{
              fontSize:      "13px",
              fontWeight:    700,
              color:         "var(--pk-text)",
              margin:        0,
              letterSpacing: "0.08em",
              textTransform: "uppercase",
              flexShrink:    0,
            }}>
              {category}
            </h2>
            <div style={{ flex: 1, height: "1px", background: "var(--pk-border)" }} />
            <span style={{ fontSize: "10px", color: "var(--pk-text-3)", fontWeight: 600, flexShrink: 0 }}>
              {topics.length}
            </span>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: "8px" }}>
            {topics.map((t) => (
              <Link
                key={t.slug}
                href={`/pray/${t.slug}`}
                style={{
                  display:        "block",
                  padding:        "16px 20px",
                  borderRadius:   "10px",
                  border:         "1px solid var(--pk-border)",
                  background:     "var(--pk-card)",
                  textDecoration: "none",
                  transition:     "border-color 150ms ease, background 150ms ease",
                }}
              >
                <p style={{
                  fontSize:      "14px",
                  fontWeight:    700,
                  color:         "var(--pk-text)",
                  margin:        "0 0 4px",
                  letterSpacing: "-0.01em",
                }}>
                  {t.title}
                </p>
                <p style={{
                  fontSize:  "12px",
                  color:     "var(--pk-text-3)",
                  margin:    0,
                  lineHeight: 1.5,
                  overflow:   "hidden",
                  display:    "-webkit-box",
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: "vertical" as const,
                }}>
                  {t.metaDesc}
                </p>
              </Link>
            ))}
          </div>
        </div>
      ))}

      {/* AI CTA */}
      <div style={{
        marginTop:    "20px",
        textAlign:    "center",
        padding:      "clamp(32px,5vw,52px) clamp(24px,4vw,48px)",
        borderRadius: "20px",
        background:   "var(--pk-accent-dim)",
        border:       "1.5px solid var(--pk-accent-border)",
        boxShadow:    "4px 4px 0 0 var(--pk-accent-border)",
      }}>
        <p style={{
          fontSize:      "11px",
          fontWeight:    700,
          color:         "var(--pk-accent)",
          letterSpacing: "0.14em",
          textTransform: "uppercase",
          margin:        "0 0 12px",
        }}>
          AI Prayer Generator
        </p>
        <h2 style={{ fontSize: "clamp(22px,3vw,30px)", fontWeight: 800, color: "var(--pk-text)", margin: "0 0 10px", letterSpacing: "-0.02em" }}>
          Don&apos;t see your situation?
        </h2>
        <p style={{ fontSize: "15px", color: "var(--pk-text-2)", margin: "0 0 28px", lineHeight: 1.65 }}>
          Our AI writes a personalised, scripture-grounded prayer for any situation in seconds.
        </p>
        <Link
          href="/pray"
          style={{
            display:       "inline-block",
            padding:       "15px 36px",
            borderRadius:  "6px",
            background:    "var(--pk-accent)",
            color:         "#fff",
            textDecoration:"none",
            fontSize:      "14px",
            fontWeight:    800,
            letterSpacing: "0.02em",
            textTransform: "uppercase",
            boxShadow:     "4px 4px 0 0 var(--pk-accent-border)",
            transition:    "transform 140ms ease, box-shadow 140ms ease",
          }}
        >
          ✦ Generate Any Prayer
        </Link>
      </div>
    </div>
  );
}
