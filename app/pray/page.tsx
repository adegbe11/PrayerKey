import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import PrayerComposer from "@/components/pray/PrayerComposer";
import PrayerCard from "@/components/pray/PrayerCard";
import type { Prayer } from "@prisma/client";

// ── Stats card ────────────────────────────────────────────────────────────────

function StatPill({ label, value, color }: { label: string; value: number | string; color: string }) {
  return (
    <div
      style={{
        background:   "#FFFFFF",
        borderRadius: "14px",
        padding:      "18px 22px",
        boxShadow:    "var(--pk-shadow-sm)",
        display:      "flex",
        flexDirection: "column",
        gap:          "4px",
        minWidth:     "120px",
      }}
    >
      <p style={{ fontSize: "26px", fontWeight: 700, color, margin: 0, lineHeight: 1 }}>
        {value}
      </p>
      <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0, fontWeight: 500 }}>
        {label}
      </p>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────

function EmptyPrayers() {
  return (
    <div
      style={{
        background:     "#FFFFFF",
        borderRadius:   "14px",
        padding:        "40px 24px",
        textAlign:      "center",
        boxShadow:      "var(--pk-shadow-sm)",
      }}
    >
      <div
        style={{
          width:         "56px",
          height:        "56px",
          borderRadius:  "50%",
          background:    "rgba(176,124,31,0.08)",
          display:       "flex",
          alignItems:    "center",
          justifyContent: "center",
          margin:        "0 auto 16px",
          fontSize:      "24px",
        }}
      >
        🙏
      </div>
      <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", marginBottom: "6px" }}>
        No prayers yet
      </p>
      <p style={{ fontSize: "13px", color: "var(--pk-t3)", maxWidth: "240px", margin: "0 auto" }}>
        Compose your first prayer above and let AI craft something personal for you.
      </p>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default async function PrayPage() {
  const session = await auth();
  if (!session?.user) redirect("/login");

  // Fetch user's recent prayers + stats
  const [prayers, totalCount, bookmarkCount] = await Promise.all([
    prisma.prayer.findMany({
      where:   { userId: session.user.id as string },
      orderBy: { createdAt: "desc" },
      take:    12,
    }),
    prisma.prayer.count({ where: { userId: session.user.id as string } }),
    prisma.prayer.count({ where: { userId: session.user.id as string, isBookmarked: true } }),
  ]);

  // Most used mood (from flat array)
  const allMoods = prayers.flatMap((p: Prayer) => p.mood);
  const moodFreq = allMoods.reduce<Record<string, number>>((acc, m) => {
    acc[m] = (acc[m] ?? 0) + 1;
    return acc;
  }, {});
  const topMood = Object.entries(moodFreq).sort((a, b) => b[1] - a[1])[0]?.[0] ?? "—";

  return (
    <div style={{ padding: "32px 24px", maxWidth: "800px", margin: "0 auto" }}>

      {/* ── Page header ─────────────────────────────────────────────── */}
      <div style={{ marginBottom: "32px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Prayer AI Engine
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 8px" }}>
          Pray
        </h1>
        <p style={{ fontSize: "15px", color: "var(--pk-t2)", margin: 0 }}>
          Tell God what&apos;s on your heart. AI will craft a personal, scripture-grounded prayer for you.
        </p>
      </div>

      {/* ── Stats row ───────────────────────────────────────────────── */}
      <div style={{ display: "flex", gap: "12px", flexWrap: "wrap", marginBottom: "32px" }}>
        <StatPill label="Prayers"    value={totalCount}    color="var(--pk-t1)" />
        <StatPill label="Bookmarked" value={bookmarkCount} color="var(--pk-gold)" />
        <StatPill label="Top mood"   value={topMood}       color="#AF52DE" />
      </div>

      {/* ── Two-column layout ────────────────────────────────────────── */}
      <div
        style={{
          display:             "grid",
          gridTemplateColumns: prayers.length > 0 ? "1fr 320px" : "1fr",
          gap:                 "28px",
          alignItems:          "start",
        }}
      >
        {/* Left: Composer */}
        <div>
          <PrayerComposer />
        </div>

        {/* Right: History (only when prayers exist) */}
        {prayers.length > 0 && (
          <aside>
            <p
              style={{
                fontSize:      "12px",
                fontWeight:    600,
                color:         "var(--pk-t3)",
                letterSpacing: "0.05em",
                textTransform: "uppercase",
                marginBottom:  "14px",
              }}
            >
              Recent Prayers
            </p>
            <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
              {prayers.map((p: Prayer) => (
                <PrayerCard key={p.id} prayer={p} />
              ))}
            </div>

            {totalCount > 12 && (
              <p
                style={{
                  fontSize:   "13px",
                  color:      "var(--pk-gold)",
                  textAlign:  "center",
                  marginTop:  "16px",
                  cursor:     "pointer",
                  fontWeight: 500,
                }}
              >
                View all {totalCount} prayers →
              </p>
            )}
          </aside>
        )}

        {/* Empty state when no prayers */}
        {prayers.length === 0 && (
          <div style={{ gridColumn: "1 / -1", marginTop: "8px" }}>
            <EmptyPrayers />
          </div>
        )}
      </div>
    </div>
  );
}
