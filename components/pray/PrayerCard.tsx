import type { Prayer } from "@prisma/client";
import type { PrayerVerse } from "@/lib/ai/prayer-generation";

interface PrayerCardProps {
  prayer: Prayer;
}

export default function PrayerCard({ prayer }: PrayerCardProps) {
  const verses = (prayer.verses as PrayerVerse[] | null) ?? [];
  const moods  = prayer.mood ?? [];

  // Format relative date
  const date = new Date(prayer.createdAt);
  const now  = new Date();
  const diff = Math.floor((now.getTime() - date.getTime()) / 1000);
  const relativeTime =
    diff < 60
      ? "just now"
      : diff < 3600
      ? `${Math.floor(diff / 60)}m ago`
      : diff < 86400
      ? `${Math.floor(diff / 3600)}h ago`
      : date.toLocaleDateString("en-US", { month: "short", day: "numeric" });

  return (
    <div
      className="pk-hover-card"
      style={{
        background:   "#FFFFFF",
        borderRadius: "14px",
        padding:      "20px",
        boxShadow:    "var(--pk-shadow-sm)",
        display:      "flex",
        flexDirection: "column",
        gap:          "12px",
      }}
    >
      {/* Header row */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          {/* Mood chips */}
          {moods.length > 0 && (
            <div style={{ display: "flex", gap: "5px", flexWrap: "wrap", marginBottom: "6px" }}>
              {moods.slice(0, 3).map((m: string) => (
                <span
                  key={m}
                  style={{
                    fontSize:     "10px",
                    padding:      "2px 8px",
                    borderRadius: "100px",
                    background:   "rgba(176,124,31,0.08)",
                    color:        "var(--pk-gold)",
                    fontWeight:   600,
                    letterSpacing: "0.02em",
                  }}
                >
                  {m}
                </span>
              ))}
            </div>
          )}

          {/* Prayer snippet */}
          <p
            style={{
              fontSize:     "14px",
              color:        "var(--pk-t1)",
              lineHeight:   1.6,
              fontStyle:    "italic",
              overflow:     "hidden",
              display:      "-webkit-box",
              WebkitLineClamp: 3,
              WebkitBoxOrient: "vertical",
              margin:       0,
            }}
          >
            &ldquo;{prayer.generatedPrayer}&rdquo;
          </p>
        </div>

        {/* Bookmark indicator */}
        {prayer.isBookmarked && (
          <span
            style={{
              fontSize:   "16px",
              color:      "var(--pk-gold)",
              marginLeft: "12px",
              flexShrink: 0,
            }}
          >
            ★
          </span>
        )}
      </div>

      {/* First verse (if any) */}
      {verses[0] && (
        <div
          style={{
            background:   "#FAFAFA",
            borderRadius: "8px",
            padding:      "10px 12px",
            borderLeft:   "2px solid var(--pk-gold)",
          }}
        >
          <p style={{ fontSize: "12px", color: "var(--pk-t2)", fontStyle: "italic", margin: "0 0 3px", lineHeight: 1.5 }}>
            &ldquo;{verses[0].text.slice(0, 100)}{verses[0].text.length > 100 ? "…" : ""}&rdquo;
          </p>
          <p style={{ fontSize: "11px", color: "var(--pk-gold)", fontWeight: 600, margin: 0 }}>
            {verses[0].ref}
          </p>
        </div>
      )}

      {/* Footer */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingTop: "4px", borderTop: "1px solid var(--pk-b1)" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>
          {relativeTime}
        </p>
        <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>
          {verses.length} verse{verses.length !== 1 ? "s" : ""}
        </p>
      </div>
    </div>
  );
}
