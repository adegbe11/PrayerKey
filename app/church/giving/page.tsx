
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";

export default async function GivingPage() {

  const viewer = await prisma.user.findUnique({
    where:  { id: "anonymous" },
    select: { churchId: true, role: true },
  });

  if (!viewer?.churchId) redirect("/");

  const isAdmin = ["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(viewer.role);

  const [funds, recentDonations, totals] = await Promise.all([
    prisma.givingFund.findMany({
      where:   { churchId: viewer.churchId },
      include: {
        donations: {
          where:  { status: "COMPLETED" },
          select: { amount: true },
        },
      },
    }),

    isAdmin
      ? prisma.donation.findMany({
          where:   { churchId: viewer.churchId },
          include: {
            user: { select: { name: true, email: true } },
            fund: { select: { name: true } },
          },
          orderBy: { createdAt: "desc" },
          take:    20,
        })
      : Promise.resolve([]),

    prisma.donation.groupBy({
      by:     ["status"],
      where:  { churchId: viewer.churchId },
      _sum:   { amount: true },
      _count: { id: true },
    }),
  ]);

  const completedTotal = totals.find((t) => t.status === "COMPLETED")?._sum.amount ?? 0;
  const donorCount     = await prisma.donation.groupBy({
    by:    ["userId"],
    where: { churchId: viewer.churchId, status: "COMPLETED", userId: { not: null } },
  });

  const fmt = (cents: number) =>
    new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(cents / 100);

  const fmtDate = (d: Date) =>
    new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });

  return (
    <div style={{ padding: "32px 24px", maxWidth: "960px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ marginBottom: "28px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Church Management
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>Giving</h1>
      </div>

      {/* KPI row */}
      {isAdmin && (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "16px", marginBottom: "36px" }}>
          {[
            { label: "Total Raised",   value: fmt(completedTotal), color: "var(--pk-gold)" },
            { label: "Total Donors",   value: donorCount.length,   color: "var(--pk-t1)"  },
            { label: "Active Funds",   value: funds.length,        color: "#0071E3"        },
          ].map((k) => (
            <div
              key={k.label}
              style={{ background: "#FFFFFF", borderRadius: "16px", padding: "22px 24px", boxShadow: "var(--pk-shadow-sm)" }}
            >
              <p style={{ fontSize: "30px", fontWeight: 700, color: k.color, margin: "0 0 4px", lineHeight: 1 }}>{k.value}</p>
              <p style={{ fontSize: "13px", color: "var(--pk-t2)", margin: 0 }}>{k.label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Funds grid */}
      <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
        Giving Funds
      </p>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: "16px", marginBottom: "40px" }}>
        {funds.map((fund) => {
          const raised = fund.donations.reduce((s, d) => s + d.amount, 0);
          const pct    = fund.goal ? Math.min(100, Math.round((raised / fund.goal) * 100)) : null;
          return (
            <div
              key={fund.id}
              className="pk-hover-card"
              style={{ background: "#FFFFFF", borderRadius: "16px", padding: "22px", boxShadow: "var(--pk-shadow-sm)" }}
            >
              <p style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 4px" }}>{fund.name}</p>
              {fund.description && (
                <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: "0 0 14px" }}>{fund.description}</p>
              )}
              <p style={{ fontSize: "26px", fontWeight: 800, color: "var(--pk-gold)", margin: "0 0 12px", lineHeight: 1 }}>
                {fmt(raised)}
              </p>
              {pct !== null && (
                <>
                  <div style={{ height: "5px", background: "var(--pk-b1)", borderRadius: "100px", overflow: "hidden", marginBottom: "6px" }}>
                    <div
                      style={{
                        height:     "100%",
                        width:      `${pct}%`,
                        background: pct >= 100 ? "#34C759" : "var(--pk-gold)",
                        borderRadius: "100px",
                      }}
                    />
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between" }}>
                    <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>{pct}% of goal</p>
                    <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>Goal: {fmt(fund.goal!)}</p>
                  </div>
                </>
              )}
            </div>
          );
        })}
      </div>

      {/* Recent donations (admin) */}
      {isAdmin && recentDonations.length > 0 && (
        <>
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            Recent Donations
          </p>
          <div style={{ background: "#FFFFFF", borderRadius: "16px", overflow: "hidden", boxShadow: "var(--pk-shadow-sm)" }}>
            {/* Table header */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 160px 120px 100px 100px", padding: "11px 20px", borderBottom: "1px solid var(--pk-b1)", background: "#FAFAFA" }}>
              {["Donor", "Fund", "Amount", "Status", "Date"].map((h) => (
                <p key={h} style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", margin: 0 }}>{h}</p>
              ))}
            </div>
            {recentDonations.map((d, i) => (
              <div
                key={d.id}
                className="pk-hover-row"
                style={{
                  display:      "grid",
                  gridTemplateColumns: "1fr 160px 120px 100px 100px",
                  padding:      "13px 20px",
                  alignItems:   "center",
                  borderBottom: i < recentDonations.length - 1 ? "1px solid var(--pk-b1)" : "none",
                }}
              >
                <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>
                  {d.user?.name ?? d.user?.email?.split("@")[0] ?? "Anonymous"}
                </p>
                <p style={{ fontSize: "13px", color: "var(--pk-t2)", margin: 0 }}>{d.fund.name}</p>
                <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)", margin: 0 }}>{fmt(d.amount)}</p>
                <span
                  style={{
                    fontSize:     "11px",
                    fontWeight:   600,
                    padding:      "3px 8px",
                    borderRadius: "100px",
                    background:   d.status === "COMPLETED" ? "rgba(52,199,89,0.1)" : "rgba(255,159,10,0.1)",
                    color:        d.status === "COMPLETED" ? "#34C759" : "#FF9F0A",
                    display:      "inline-block",
                  }}
                >
                  {d.status}
                </span>
                <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>{fmtDate(d.createdAt)}</p>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
