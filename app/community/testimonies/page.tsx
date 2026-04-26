
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import AmenButton from "@/components/community/AmenButton";
import TestimonyForm from "@/components/community/TestimonyForm";

export default async function TestimoniesPage() {

  const user = await prisma.user.findUnique({
    where:  { id: "anonymous" },
    select: { churchId: true, role: true },
  });

  if (!user?.churchId) redirect("/");

  const isAdmin = ["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(user.role);

  const [approved, pending] = await Promise.all([
    prisma.testimony.findMany({
      where:   { user: { churchId: user.churchId }, approved: true },
      include: { user: { select: { name: true, avatarUrl: true } } },
      orderBy: { createdAt: "desc" },
    }),

    isAdmin
      ? prisma.testimony.findMany({
          where:   { user: { churchId: user.churchId }, approved: false },
          include: { user: { select: { name: true } } },
          orderBy: { createdAt: "desc" },
        })
      : Promise.resolve([]),
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
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Community
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>Testimonies</h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: "4px 0 0" }}>
          What God has done in your church family
        </p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: "28px", alignItems: "start" }}>

        {/* Feed */}
        <div>
          {/* Pending (admin) */}
          {isAdmin && pending.length > 0 && (
            <div style={{ marginBottom: "28px" }}>
              <p style={{ fontSize: "12px", fontWeight: 600, color: "#FF9F0A", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
                Awaiting Approval · {pending.length}
              </p>
              <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                {pending.map((t) => (
                  <div
                    key={t.id}
                    style={{
                      background:   "#FFFFFF",
                      borderRadius: "14px",
                      padding:      "18px",
                      boxShadow:    "var(--pk-shadow-sm)",
                      borderLeft:   "3px solid #FF9F0A",
                      opacity:      0.8,
                    }}
                  >
                    <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 4px" }}>{t.title}</p>
                    <p style={{ fontSize: "13px", color: "var(--pk-t2)", lineHeight: 1.5, margin: "0 0 10px" }}>{t.story.slice(0, 160)}…</p>
                    <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>
                      by {t.anonymous ? "Anonymous" : (t.user.name ?? "Member")} · {timeAgo(t.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Approved */}
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
            Testimonies · {approved.length}
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
            {approved.map((t) => (
              <div
                key={t.id}
                className="pk-hover-card"
                style={{ background: "#FFFFFF", borderRadius: "16px", padding: "22px", boxShadow: "var(--pk-shadow-sm)" }}
              >
                {/* Author + time */}
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "14px" }}>
                  <div
                    style={{
                      width:          "36px",
                      height:         "36px",
                      borderRadius:   "50%",
                      background:     "linear-gradient(135deg, var(--pk-gold), #AF52DE)",
                      display:        "flex",
                      alignItems:     "center",
                      justifyContent: "center",
                      fontSize:       "14px",
                      fontWeight:     700,
                      color:          "#FFFFFF",
                      flexShrink:     0,
                    }}
                  >
                    {t.anonymous ? "?" : (t.user.name ?? "?").slice(0, 1).toUpperCase()}
                  </div>
                  <div style={{ flex: 1 }}>
                    <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>
                      {t.anonymous ? "Anonymous" : (t.user.name ?? "Member")}
                    </p>
                    <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>{timeAgo(t.createdAt)}</p>
                  </div>
                  {/* Amen count */}
                  {t.amenCount > 0 && (
                    <span style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 700 }}>
                      {t.amenCount} Amen
                    </span>
                  )}
                </div>

                <p style={{ fontSize: "16px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 10px" }}>{t.title}</p>
                <p style={{ fontSize: "14px", color: "var(--pk-t2)", lineHeight: 1.7, margin: "0 0 14px" }}>{t.story}</p>

                {/* Tags */}
                {t.tags.length > 0 && (
                  <div style={{ display: "flex", gap: "5px", flexWrap: "wrap", marginBottom: "14px" }}>
                    {t.tags.map((tag: string) => (
                      <span
                        key={tag}
                        style={{
                          fontSize:     "10px",
                          fontWeight:   600,
                          padding:      "2px 8px",
                          borderRadius: "100px",
                          background:   "rgba(176,124,31,0.08)",
                          color:        "var(--pk-gold)",
                        }}
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                )}

                <AmenButton testimonyId={t.id} initialCount={t.amenCount} />
              </div>
            ))}

            {approved.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "16px", padding: "40px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "28px", marginBottom: "12px" }}>✦</p>
                <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", marginBottom: "6px" }}>No testimonies yet</p>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)" }}>Be the first to share what God has done.</p>
              </div>
            )}
          </div>
        </div>

        {/* Sidebar — submit form */}
        <div style={{ position: "sticky", top: "80px" }}>
          <TestimonyForm />
        </div>
      </div>
    </div>
  );
}
