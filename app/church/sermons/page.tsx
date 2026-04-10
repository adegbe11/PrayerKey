import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import Link from "next/link";

export default async function SermonsPage() {
  const session = await auth();
  if (!session?.user) redirect("/login");

  const viewer = await prisma.user.findUnique({
    where:  { id: session.user.id as string },
    select: { churchId: true, role: true },
  });

  if (!viewer?.churchId) redirect("/");

  const sermons = await prisma.sermon.findMany({
    where:   { churchId: viewer.churchId },
    orderBy: { date: "desc" },
  });

  const fmtDate = (d: Date) =>
    new Date(d).toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric", year: "numeric" });

  const fmtDuration = (mins?: number | null) => {
    if (!mins) return null;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return h > 0 ? `${h}h ${m}m` : `${m}m`;
  };

  // Group by year
  const byYear = sermons.reduce<Record<number, typeof sermons>>((acc, s) => {
    const yr = new Date(s.date).getFullYear();
    if (!acc[yr]) acc[yr] = [];
    acc[yr].push(s);
    return acc;
  }, {});

  const years = Object.keys(byYear).map(Number).sort((a, b) => b - a);

  return (
    <div style={{ padding: "32px 24px", maxWidth: "900px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "28px" }}>
        <div>
          <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
            Church Management
          </p>
          <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>Sermons</h1>
          <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: "4px 0 0" }}>
            {sermons.length} recorded sermon{sermons.length !== 1 ? "s" : ""}
          </p>
        </div>
        <Link
          href="/live"
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "6px",
            padding:      "10px 18px",
            borderRadius: "980px",
            background:   "#FF3B30",
            color:        "#FFFFFF",
            fontSize:     "14px",
            fontWeight:   600,
            textDecoration: "none",
          }}
        >
          <span style={{ fontSize: "10px" }}>●</span>
          Start Live Service
        </Link>
      </div>

      {sermons.length === 0 && (
        <div style={{ background: "#FFFFFF", borderRadius: "16px", padding: "48px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
          <p style={{ fontSize: "32px", marginBottom: "12px" }}>📖</p>
          <p style={{ fontSize: "16px", fontWeight: 600, color: "var(--pk-t1)", marginBottom: "6px" }}>No sermons yet</p>
          <p style={{ fontSize: "14px", color: "var(--pk-t3)", marginBottom: "20px" }}>
            Start a live service to automatically capture and archive sermons.
          </p>
          <Link href="/live" style={{ fontSize: "14px", color: "var(--pk-gold)", fontWeight: 600, textDecoration: "none" }}>
            Go to Live →
          </Link>
        </div>
      )}

      {/* Year groups */}
      {years.map((year) => (
        <div key={year} style={{ marginBottom: "36px" }}>
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            {year} · {byYear[year].length} sermons
          </p>
          <div style={{ background: "#FFFFFF", borderRadius: "16px", overflow: "hidden", boxShadow: "var(--pk-shadow-sm)" }}>
            {byYear[year].map((s, i) => (
              <div
                key={s.id}
                className="pk-hover-row"
                style={{
                  display:      "flex",
                  gap:          "16px",
                  alignItems:   "center",
                  padding:      "16px 20px",
                  borderBottom: i < byYear[year].length - 1 ? "1px solid var(--pk-b1)" : "none",
                }}
              >
                {/* Icon */}
                <div
                  style={{
                    width:          "40px",
                    height:         "40px",
                    background:     "rgba(0,113,227,0.08)",
                    borderRadius:   "10px",
                    display:        "flex",
                    alignItems:     "center",
                    justifyContent: "center",
                    flexShrink:     0,
                    fontSize:       "18px",
                  }}
                >
                  📖
                </div>

                {/* Details */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 3px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {s.title}
                  </p>
                  <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>
                    {fmtDate(s.date)}
                    {fmtDuration(s.duration) ? ` · ${fmtDuration(s.duration)}` : ""}
                    {s.verseCount > 0 ? ` · ${s.verseCount} verses detected` : ""}
                  </p>
                </div>

                {/* Tags */}
                <div style={{ display: "flex", gap: "6px", flexShrink: 0 }}>
                  {s.summaryAI && (
                    <span style={{ fontSize: "10px", fontWeight: 600, padding: "3px 8px", borderRadius: "100px", background: "rgba(0,113,227,0.08)", color: "#0071E3" }}>
                      AI Summary
                    </span>
                  )}
                  {s.audioUrl && (
                    <span style={{ fontSize: "10px", fontWeight: 600, padding: "3px 8px", borderRadius: "100px", background: "rgba(52,199,89,0.1)", color: "#34C759" }}>
                      Audio
                    </span>
                  )}
                  {s.blogPostAI && (
                    <span style={{ fontSize: "10px", fontWeight: 600, padding: "3px 8px", borderRadius: "100px", background: "rgba(175,82,222,0.1)", color: "#AF52DE" }}>
                      Blog
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
