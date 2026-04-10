import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import Link from "next/link";

// ── Stat card ─────────────────────────────────────────────────────────────────

function StatCard({
  label, value, sub, color = "var(--pk-t1)", href,
}: {
  label: string; value: number | string; sub?: string; color?: string; href?: string;
}) {
  const inner = (
    <div
      className="pk-hover-card"
      style={{
        background:   "#FFFFFF",
        borderRadius: "16px",
        padding:      "22px 24px",
        boxShadow:    "var(--pk-shadow-sm)",
        display:      "flex",
        flexDirection: "column",
        gap:          "4px",
        height:       "100%",
      }}
    >
      <p style={{ fontSize: "30px", fontWeight: 700, color, margin: 0, lineHeight: 1 }}>{value}</p>
      <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>{label}</p>
      {sub && <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>{sub}</p>}
    </div>
  );
  return href ? <Link href={href} style={{ textDecoration: "none", display: "block" }}>{inner}</Link> : inner;
}

// ── Section header ────────────────────────────────────────────────────────────

function SectionHeader({ title, href, linkLabel }: { title: string; href: string; linkLabel: string }) {
  return (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "14px" }}>
      <h2 style={{ fontSize: "16px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>{title}</h2>
      <Link
        href={href}
        style={{ fontSize: "13px", color: "var(--pk-gold)", fontWeight: 500, textDecoration: "none" }}
      >
        {linkLabel} →
      </Link>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default async function ChurchDashboard() {
  const session = await auth();
  if (!session?.user) redirect("/login");

  const user = await prisma.user.findUnique({
    where:  { id: session.user.id as string },
    select: { churchId: true, role: true, name: true },
  });

  if (!user?.churchId) redirect("/");

  const churchId = user.churchId;

  const [
    church,
    memberCount,
    recentMembers,
    upcomingEvents,
    recentSermons,
    givingFunds,
    totalDonated,
    liveServiceCount,
  ] = await Promise.all([
    prisma.church.findUnique({ where: { id: churchId } }),

    prisma.user.count({ where: { churchId } }),

    prisma.user.findMany({
      where:   { churchId },
      orderBy: { id: "desc" },
      take:    5,
      select:  { id: true, name: true, email: true, role: true, avatarUrl: true },
    }),

    prisma.event.findMany({
      where:   { churchId, date: { gte: new Date() } },
      orderBy: { date: "asc" },
      take:    4,
    }),

    prisma.sermon.findMany({
      where:   { churchId },
      orderBy: { date: "desc" },
      take:    4,
    }),

    prisma.givingFund.findMany({
      where:   { churchId },
      include: {
        donations: {
          where:  { status: "COMPLETED" },
          select: { amount: true },
        },
      },
    }),

    prisma.donation.aggregate({
      where: { churchId, status: "COMPLETED" },
      _sum:  { amount: true },
    }),

    prisma.service.count({ where: { churchId, status: "LIVE" } }),
  ]);

  const totalRaised = totalDonated._sum.amount ?? 0;

  const formatCurrency = (cents: number) =>
    new Intl.NumberFormat("en-US", { style: "currency", currency: church?.plan === "STARTER" ? "USD" : "USD", maximumFractionDigits: 0 }).format(cents / 100);

  const formatDate = (d: Date) =>
    new Date(d).toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric" });

  const ROLE_COLORS: Record<string, string> = {
    PASTOR:       "#B07C1F",
    CHURCH_ADMIN: "#0071E3",
    MEMBER:       "#6E6E73",
    SUPER_ADMIN:  "#AF52DE",
  };

  return (
    <div style={{ padding: "32px 24px", maxWidth: "960px", margin: "0 auto" }}>

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div style={{ marginBottom: "32px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Church Management
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 4px" }}>
          {church?.name}
        </h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: 0 }}>
          {church?.city}{church?.country ? `, ${church.country}` : ""} &middot; {church?.plan} plan
        </p>
      </div>

      {/* ── KPI stats ───────────────────────────────────────────────── */}
      <div
        style={{
          display:             "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap:                 "16px",
          marginBottom:        "40px",
        }}
      >
        <StatCard label="Members"     value={memberCount}         color="var(--pk-t1)"   href="/church/members" />
        <StatCard label="Total Giving" value={formatCurrency(totalRaised)} color="var(--pk-gold)"  href="/church/giving" />
        <StatCard label="Sermons"     value={recentSermons.length} color="#0071E3"        href="/church/sermons" sub="this year" />
        <StatCard label="Live Now"    value={liveServiceCount}    color="#FF3B30"        href="/live" />
      </div>

      {/* ── Two columns ─────────────────────────────────────────────── */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "28px" }}>

        {/* ── Recent Members ──────────────────────────────────────── */}
        <div>
          <SectionHeader title="Recent Members" href="/church/members" linkLabel="All members" />
          <div
            style={{
              background:   "#FFFFFF",
              borderRadius: "16px",
              overflow:     "hidden",
              boxShadow:    "var(--pk-shadow-sm)",
            }}
          >
            {recentMembers.map((m, i) => (
              <div
                key={m.id}
                className="pk-hover-row"
                style={{
                  display:     "flex",
                  alignItems:  "center",
                  gap:         "12px",
                  padding:     "14px 18px",
                  borderBottom: i < recentMembers.length - 1 ? "1px solid var(--pk-b1)" : "none",
                }}
              >
                {/* Avatar */}
                <div
                  style={{
                    width:          "36px",
                    height:         "36px",
                    borderRadius:   "50%",
                    background:     "linear-gradient(135deg, var(--pk-gold), #AF52DE)",
                    display:        "flex",
                    alignItems:     "center",
                    justifyContent: "center",
                    flexShrink:     0,
                    fontSize:       "13px",
                    fontWeight:     700,
                    color:          "#FFFFFF",
                  }}
                >
                  {(m.name ?? m.email).slice(0, 1).toUpperCase()}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {m.name ?? m.email.split("@")[0]}
                  </p>
                  <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {m.email}
                  </p>
                </div>
                <span
                  style={{
                    fontSize:     "10px",
                    fontWeight:   600,
                    padding:      "3px 8px",
                    borderRadius: "100px",
                    background:   `${ROLE_COLORS[m.role] ?? "#6E6E73"}14`,
                    color:        ROLE_COLORS[m.role] ?? "#6E6E73",
                    flexShrink:   0,
                  }}
                >
                  {m.role}
                </span>
              </div>
            ))}
            {recentMembers.length === 0 && (
              <p style={{ padding: "24px", textAlign: "center", color: "var(--pk-t3)", fontSize: "13px" }}>No members yet</p>
            )}
          </div>
        </div>

        {/* ── Upcoming Events ─────────────────────────────────────── */}
        <div>
          <SectionHeader title="Upcoming Events" href="/church/events" linkLabel="All events" />
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {upcomingEvents.map((ev) => (
              <div
                key={ev.id}
                className="pk-hover-card"
                style={{
                  background:   "#FFFFFF",
                  borderRadius: "12px",
                  padding:      "16px 18px",
                  boxShadow:    "var(--pk-shadow-sm)",
                  display:      "flex",
                  gap:          "14px",
                  alignItems:   "center",
                }}
              >
                {/* Date badge */}
                <div
                  style={{
                    width:          "44px",
                    height:         "44px",
                    background:     "rgba(176,124,31,0.08)",
                    borderRadius:   "10px",
                    display:        "flex",
                    flexDirection:  "column",
                    alignItems:     "center",
                    justifyContent: "center",
                    flexShrink:     0,
                  }}
                >
                  <p style={{ fontSize: "16px", fontWeight: 800, color: "var(--pk-gold)", margin: 0, lineHeight: 1 }}>
                    {new Date(ev.date).getDate()}
                  </p>
                  <p style={{ fontSize: "9px", fontWeight: 600, color: "var(--pk-gold)", margin: 0, textTransform: "uppercase" }}>
                    {new Date(ev.date).toLocaleString("default", { month: "short" })}
                  </p>
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {ev.title}
                  </p>
                  <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>
                    {formatDate(ev.date)}{ev.location ? ` · ${ev.location}` : ""}
                  </p>
                </div>
              </div>
            ))}
            {upcomingEvents.length === 0 && (
              <div
                style={{
                  background: "#FFFFFF", borderRadius: "12px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)",
                }}
              >
                <p style={{ color: "var(--pk-t3)", fontSize: "13px", margin: 0 }}>No upcoming events</p>
                <Link href="/church/events" style={{ fontSize: "13px", color: "var(--pk-gold)", fontWeight: 500, textDecoration: "none" }}>
                  Create one →
                </Link>
              </div>
            )}
          </div>
        </div>

        {/* ── Recent Sermons ──────────────────────────────────────── */}
        <div>
          <SectionHeader title="Recent Sermons" href="/church/sermons" linkLabel="All sermons" />
          <div
            style={{
              background:   "#FFFFFF",
              borderRadius: "16px",
              overflow:     "hidden",
              boxShadow:    "var(--pk-shadow-sm)",
            }}
          >
            {recentSermons.map((s, i) => (
              <div
                key={s.id}
                className="pk-hover-row"
                style={{
                  display:      "flex",
                  alignItems:   "center",
                  gap:          "14px",
                  padding:      "14px 18px",
                  borderBottom: i < recentSermons.length - 1 ? "1px solid var(--pk-b1)" : "none",
                }}
              >
                <div
                  style={{
                    width:          "8px",
                    height:         "8px",
                    borderRadius:   "50%",
                    background:     "#0071E3",
                    flexShrink:     0,
                  }}
                />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {s.title}
                  </p>
                  <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>
                    {formatDate(s.date)}{s.verseCount > 0 ? ` · ${s.verseCount} verses` : ""}
                  </p>
                </div>
                {s.summaryAI && (
                  <span
                    style={{
                      fontSize:     "10px",
                      fontWeight:   600,
                      padding:      "3px 8px",
                      borderRadius: "100px",
                      background:   "rgba(0,113,227,0.08)",
                      color:        "#0071E3",
                      flexShrink:   0,
                    }}
                  >
                    AI Summary
                  </span>
                )}
              </div>
            ))}
            {recentSermons.length === 0 && (
              <p style={{ padding: "24px", textAlign: "center", color: "var(--pk-t3)", fontSize: "13px" }}>No sermons recorded yet</p>
            )}
          </div>
        </div>

        {/* ── Giving Funds ────────────────────────────────────────── */}
        <div>
          <SectionHeader title="Giving Funds" href="/church/giving" linkLabel="Full report" />
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {givingFunds.map((fund) => {
              const raised = fund.donations.reduce((s, d) => s + d.amount, 0);
              const pct    = fund.goal ? Math.min(100, Math.round((raised / fund.goal) * 100)) : null;
              return (
                <div
                  key={fund.id}
                  className="pk-hover-card"
                  style={{
                    background:   "#FFFFFF",
                    borderRadius: "12px",
                    padding:      "16px 18px",
                    boxShadow:    "var(--pk-shadow-sm)",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                    <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>{fund.name}</p>
                    <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-gold)", margin: 0 }}>
                      {formatCurrency(raised)}
                    </p>
                  </div>
                  {pct !== null && (
                    <>
                      <div style={{ height: "5px", background: "var(--pk-b1)", borderRadius: "100px", overflow: "hidden" }}>
                        <div
                          style={{
                            height:     "100%",
                            width:      `${pct}%`,
                            background: pct >= 100 ? "#34C759" : "var(--pk-gold)",
                            borderRadius: "100px",
                            transition: "width 0.6s var(--pk-ease)",
                          }}
                        />
                      </div>
                      <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: "5px 0 0", textAlign: "right" }}>
                        {pct}% of {formatCurrency(fund.goal!)} goal
                      </p>
                    </>
                  )}
                </div>
              );
            })}
            {givingFunds.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "12px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ color: "var(--pk-t3)", fontSize: "13px", margin: 0 }}>No giving funds yet</p>
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
