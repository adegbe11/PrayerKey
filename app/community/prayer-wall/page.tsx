
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import PrayButton from "@/components/community/PrayButton";
import PrayerRequestForm from "@/components/community/PrayerRequestForm";

export default async function PrayerWallPage() {

  const user = await prisma.user.findUnique({
    where:  { id: "anonymous" },
    select: { churchId: true },
  });

  if (!user?.churchId) redirect("/");

  const [active, answered] = await Promise.all([
    prisma.prayerRequest.findMany({
      where:   { churchId: user.churchId, answered: false },
      include: { user: { select: { name: true } } },
      orderBy: [{ prayCount: "desc" }, { createdAt: "desc" }],
    }),

    prisma.prayerRequest.findMany({
      where:   { churchId: user.churchId, answered: true },
      include: { user: { select: { name: true } } },
      orderBy: { createdAt: "desc" },
      take:    5,
    }),
  ]);

  const timeAgo = (d: Date) => {
    const diff = Math.floor((Date.now() - new Date(d).getTime()) / 1000);
    if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" });
  };

  return (
    <div style={{ padding: "32px 24px", maxWidth: "960px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ marginBottom: "28px" }}>
        <p style={{ fontSize: "12px", color: "#0071E3", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Community
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>Prayer Wall</h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: "4px 0 0" }}>
          Lift each other up in prayer.
        </p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: "28px", alignItems: "start" }}>

        {/* Requests */}
        <div>
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            Active Requests · {active.length}
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "12px", marginBottom: "32px" }}>
            {active.map((r) => (
              <div
                key={r.id}
                className="pk-hover-card"
                style={{ background: "#FFFFFF", borderRadius: "14px", padding: "20px", boxShadow: "var(--pk-shadow-sm)" }}
              >
                <div style={{ display: "flex", alignItems: "flex-start", gap: "10px", marginBottom: "10px" }}>
                  <div
                    style={{
                      width:          "32px",
                      height:         "32px",
                      borderRadius:   "50%",
                      background:     "rgba(0,113,227,0.10)",
                      display:        "flex",
                      alignItems:     "center",
                      justifyContent: "center",
                      fontSize:       "16px",
                      flexShrink:     0,
                    }}
                  >
                    🙏
                  </div>
                  <div style={{ flex: 1 }}>
                    <p style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 3px" }}>{r.title}</p>
                    <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>
                      {r.anonymous ? "Anonymous" : (r.user.name ?? "Member")} · {timeAgo(r.createdAt)}
                    </p>
                  </div>
                  {r.prayCount > 0 && (
                    <span
                      style={{
                        fontSize:     "11px",
                        fontWeight:   700,
                        color:        "#0071E3",
                        background:   "rgba(0,113,227,0.08)",
                        borderRadius: "100px",
                        padding:      "3px 8px",
                        flexShrink:   0,
                      }}
                    >
                      {r.prayCount} praying
                    </span>
                  )}
                </div>

                <p style={{ fontSize: "13px", color: "var(--pk-t2)", lineHeight: 1.6, margin: "0 0 14px" }}>{r.body}</p>
                <PrayButton requestId={r.id} initialCount={r.prayCount} />
              </div>
            ))}

            {active.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "14px", padding: "36px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "24px", marginBottom: "10px" }}>🙏</p>
                <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", marginBottom: "6px" }}>The wall is clear</p>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)" }}>Be the first to share a prayer need.</p>
              </div>
            )}
          </div>

          {/* Answered prayers */}
          {answered.length > 0 && (
            <>
              <p style={{ fontSize: "12px", fontWeight: 600, color: "#34C759", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
                ✓ Answered Prayers · {answered.length}
              </p>
              <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                {answered.map((r) => (
                  <div
                    key={r.id}
                    style={{
                      background:   "#FFFFFF",
                      borderRadius: "12px",
                      padding:      "14px 16px",
                      boxShadow:    "var(--pk-shadow-sm)",
                      display:      "flex",
                      alignItems:   "center",
                      gap:          "12px",
                      opacity:      0.75,
                    }}
                  >
                    <span style={{ fontSize: "16px" }}>✅</span>
                    <div>
                      <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px" }}>{r.title}</p>
                      <p style={{ fontSize: "11px", color: "#34C759", fontWeight: 600, margin: 0 }}>Answered · {r.prayCount} prayed</p>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Sidebar */}
        <div style={{ position: "sticky", top: "80px" }}>
          <PrayerRequestForm />
        </div>
      </div>
    </div>
  );
}
