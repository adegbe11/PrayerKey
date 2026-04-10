import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import Link from "next/link";
import AmenButton from "@/components/community/AmenButton";
import PrayButton from "@/components/community/PrayButton";

// ── Tag chip ─────────────────────────────────────────────────────────────────

function TagChip({ label }: { label: string }) {
  return (
    <span
      style={{
        fontSize:     "10px",
        fontWeight:   600,
        padding:      "2px 8px",
        borderRadius: "100px",
        background:   "rgba(176,124,31,0.08)",
        color:        "var(--pk-gold)",
      }}
    >
      {label}
    </span>
  );
}

// ── Relative time ─────────────────────────────────────────────────────────────

function timeAgo(d: Date) {
  const diff = Math.floor((Date.now() - new Date(d).getTime()) / 1000);
  if (diff < 60)     return "just now";
  if (diff < 3600)   return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400)  return `${Math.floor(diff / 3600)}h ago`;
  return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default async function CommunityPage() {
  const session = await auth();
  if (!session?.user) redirect("/login");

  const user = await prisma.user.findUnique({
    where:  { id: session.user.id as string },
    select: { churchId: true, role: true },
  });

  if (!user?.churchId) redirect("/");

  const isAdmin = ["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(user.role);

  const [testimonies, prayerRequests, announcements] = await Promise.all([
    prisma.testimony.findMany({
      where:   { user: { churchId: user.churchId }, approved: true },
      include: { user: { select: { name: true, avatarUrl: true } } },
      orderBy: { createdAt: "desc" },
      take:    6,
    }),

    prisma.prayerRequest.findMany({
      where:   { churchId: user.churchId, answered: false },
      include: { user: { select: { name: true } } },
      orderBy: { createdAt: "desc" },
      take:    6,
    }),

    prisma.announcement.findMany({
      where:   { churchId: user.churchId },
      include: { author: { select: { name: true, role: true } } },
      orderBy: [{ pinned: "desc" }, { createdAt: "desc" }],
      take:    3,
    }),
  ]);

  // Pending count for admins
  const pendingTestimonies = isAdmin
    ? await prisma.testimony.count({ where: { user: { churchId: user.churchId }, approved: false } })
    : 0;

  return (
    <div style={{ padding: "32px 24px", maxWidth: "960px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ marginBottom: "28px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Community
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 4px" }}>
          Your Church Family
        </h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: 0 }}>
          Testimonies, prayer requests, and announcements from your community.
        </p>
      </div>

      {/* Admin badge */}
      {isAdmin && pendingTestimonies > 0 && (
        <div
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "10px",
            background:   "rgba(255,159,10,0.08)",
            border:       "0.5px solid rgba(255,159,10,0.3)",
            borderRadius: "12px",
            padding:      "12px 16px",
            marginBottom: "24px",
          }}
        >
          <span style={{ fontSize: "16px" }}>⚠️</span>
          <p style={{ fontSize: "14px", color: "#FF9F0A", fontWeight: 600, margin: 0 }}>
            {pendingTestimonies} testimon{pendingTestimonies === 1 ? "y" : "ies"} awaiting approval
          </p>
        </div>
      )}

      {/* 3-column layout */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 300px", gap: "24px", alignItems: "start" }}>

        {/* ── Testimonies ──────────────────────────────────────────── */}
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "14px" }}>
            <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", margin: 0 }}>
              Testimonies
            </p>
            <Link href="/community/testimonies" style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 500, textDecoration: "none" }}>
              All →
            </Link>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {testimonies.map((t) => (
              <div
                key={t.id}
                className="pk-hover-card"
                style={{ background: "#FFFFFF", borderRadius: "14px", padding: "18px", boxShadow: "var(--pk-shadow-sm)" }}
              >
                {/* Author */}
                <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "10px" }}>
                  <div
                    style={{
                      width:          "28px",
                      height:         "28px",
                      borderRadius:   "50%",
                      background:     "linear-gradient(135deg, var(--pk-gold), #AF52DE)",
                      display:        "flex",
                      alignItems:     "center",
                      justifyContent: "center",
                      fontSize:       "11px",
                      fontWeight:     700,
                      color:          "#FFFFFF",
                      flexShrink:     0,
                    }}
                  >
                    {t.anonymous ? "?" : (t.user.name ?? "?").slice(0, 1).toUpperCase()}
                  </div>
                  <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", margin: 0 }}>
                    {t.anonymous ? "Anonymous" : (t.user.name ?? "Member")}
                  </p>
                  <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: "0 0 0 auto" }}>
                    {timeAgo(t.createdAt)}
                  </p>
                </div>

                <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 6px" }}>{t.title}</p>
                <p style={{ fontSize: "13px", color: "var(--pk-t2)", lineHeight: 1.5, margin: "0 0 12px", overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 3, WebkitBoxOrient: "vertical" }}>
                  {t.story}
                </p>

                {t.tags.length > 0 && (
                  <div style={{ display: "flex", gap: "5px", flexWrap: "wrap", marginBottom: "12px" }}>
                    {t.tags.map((tag: string) => <TagChip key={tag} label={tag} />)}
                  </div>
                )}

                <AmenButton testimonyId={t.id} initialCount={t.amenCount} />
              </div>
            ))}

            {testimonies.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "14px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)", margin: "0 0 8px" }}>No testimonies yet</p>
                <Link href="/community/testimonies" style={{ fontSize: "13px", color: "var(--pk-gold)", fontWeight: 500, textDecoration: "none" }}>Share yours →</Link>
              </div>
            )}
          </div>
        </div>

        {/* ── Prayer Wall ──────────────────────────────────────────── */}
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "14px" }}>
            <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", margin: 0 }}>
              Prayer Wall
            </p>
            <Link href="/community/prayer-wall" style={{ fontSize: "12px", color: "#0071E3", fontWeight: 500, textDecoration: "none" }}>
              All →
            </Link>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {prayerRequests.map((r) => (
              <div
                key={r.id}
                className="pk-hover-card"
                style={{ background: "#FFFFFF", borderRadius: "14px", padding: "18px", boxShadow: "var(--pk-shadow-sm)" }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "8px" }}>
                  <div style={{ width: "6px", height: "6px", borderRadius: "50%", background: "#0071E3", flexShrink: 0 }} />
                  <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", margin: 0 }}>
                    {r.anonymous ? "Anonymous" : (r.user.name ?? "Member")}
                  </p>
                  <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: "0 0 0 auto" }}>{timeAgo(r.createdAt)}</p>
                </div>
                <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 6px" }}>{r.title}</p>
                <p style={{ fontSize: "13px", color: "var(--pk-t2)", lineHeight: 1.5, margin: "0 0 12px", overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                  {r.body}
                </p>
                <PrayButton requestId={r.id} initialCount={r.prayCount} />
              </div>
            ))}

            {prayerRequests.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "14px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)", margin: "0 0 8px" }}>No prayer requests</p>
                <Link href="/community/prayer-wall" style={{ fontSize: "13px", color: "#0071E3", fontWeight: 500, textDecoration: "none" }}>Add one →</Link>
              </div>
            )}
          </div>
        </div>

        {/* ── Announcements sidebar ─────────────────────────────────── */}
        <div>
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            Announcements
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {announcements.map((a) => (
              <div
                key={a.id}
                style={{
                  background:   "#FFFFFF",
                  borderRadius: "12px",
                  padding:      "16px",
                  boxShadow:    "var(--pk-shadow-sm)",
                  borderLeft:   a.pinned ? "3px solid var(--pk-gold)" : "none",
                }}
              >
                {a.pinned && (
                  <p style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.04em", margin: "0 0 5px" }}>
                    📌 PINNED
                  </p>
                )}
                <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 5px" }}>{a.title}</p>
                <p style={{ fontSize: "12px", color: "var(--pk-t2)", lineHeight: 1.5, margin: "0 0 8px" }}>{a.body}</p>
                <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>
                  {a.author.name} · {timeAgo(a.createdAt)}
                </p>
              </div>
            ))}
            {announcements.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "12px", padding: "20px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)", margin: 0 }}>No announcements</p>
              </div>
            )}
          </div>

          {/* Quick nav */}
          <div style={{ marginTop: "20px", display: "flex", flexDirection: "column", gap: "8px" }}>
            <Link
              href="/community/testimonies"
              style={{ display: "block", background: "#FFFFFF", borderRadius: "10px", padding: "12px 16px", textDecoration: "none", boxShadow: "var(--pk-shadow-sm)" }}
            >
              <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px" }}>Share a Testimony</p>
              <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>Tell your church what God has done</p>
            </Link>
            <Link
              href="/community/prayer-wall"
              style={{ display: "block", background: "#FFFFFF", borderRadius: "10px", padding: "12px 16px", textDecoration: "none", boxShadow: "var(--pk-shadow-sm)" }}
            >
              <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px" }}>Prayer Wall</p>
              <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>Request and receive prayer support</p>
            </Link>
          </div>
        </div>

      </div>
    </div>
  );
}
