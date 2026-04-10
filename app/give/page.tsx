
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import DonationForm from "@/components/give/DonationForm";

// ── Helpers ───────────────────────────────────────────────────────────────────

function fmt(cents: number, currency = "USD") {
  return new Intl.NumberFormat("en-US", {
    style:                 "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(cents / 100);
}

function timeAgo(d: Date) {
  const diff = Math.floor((Date.now() - new Date(d).getTime()) / 1000);
  if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default async function GivePage() {

  const user = await prisma.user.findUnique({
    where:  { id: "anonymous" },
    select: { churchId: true },
  });

  if (!user?.churchId) redirect("/");

  const [church, funds, myDonations] = await Promise.all([
    prisma.church.findUnique({
      where:  { id: user.churchId },
      select: { name: true, logoUrl: true },
    }),

    prisma.givingFund.findMany({
      where:   { churchId: user.churchId },
      include: {
        donations: {
          where:  { status: "COMPLETED" },
          select: { amount: true },
        },
      },
      orderBy: { createdAt: "asc" },
    }),

    prisma.donation.findMany({
      where:   { userId: "anonymous", status: "COMPLETED" },
      include: { fund: { select: { name: true } } },
      orderBy: { createdAt: "desc" },
      take:    8,
    }),
  ]);

  const myTotal = myDonations.reduce((s, d) => s + d.amount, 0);

  // Build fund data for DonationForm
  const fundData = funds.map((f) => ({
    id:          f.id,
    name:        f.name,
    description: f.description,
    goal:        f.goal,
    raised:      f.donations.reduce((s, d) => s + d.amount, 0),
  }));

  return (
    <div style={{ padding: "32px 24px", maxWidth: "960px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ marginBottom: "32px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Free Giving
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 6px" }}>
          Give to {church?.name}
        </h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: 0 }}>
          100% free. No fees, no subscriptions. Every gift goes straight to the fund.
        </p>
      </div>

      {/* My giving stat */}
      {myTotal > 0 && (
        <div
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "14px",
            background:   "rgba(176,124,31,0.06)",
            border:       "0.5px solid rgba(176,124,31,0.2)",
            borderRadius: "14px",
            padding:      "16px 20px",
            marginBottom: "28px",
          }}
        >
          <span style={{ fontSize: "22px" }}>✦</span>
          <div>
            <p style={{ fontSize: "13px", color: "var(--pk-t2)", margin: "0 0 2px" }}>Your total giving</p>
            <p style={{ fontSize: "22px", fontWeight: 800, color: "var(--pk-gold)", margin: 0, lineHeight: 1 }}>
              {fmt(myTotal)}
            </p>
          </div>
        </div>
      )}

      <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: "32px", alignItems: "start" }}>

        {/* Left — fund overviews + my history */}
        <div>
          {/* Fund progress bars */}
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            Active Funds
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px", marginBottom: "36px" }}>
            {fundData.map((f) => {
              const pct = f.goal ? Math.min(100, Math.round((f.raised / f.goal) * 100)) : null;
              return (
                <div
                  key={f.id}
                  style={{
                    background:   "#FFFFFF",
                    borderRadius: "14px",
                    padding:      "20px",
                    boxShadow:    "var(--pk-shadow-sm)",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: pct !== null ? "12px" : "0" }}>
                    <div>
                      <p style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 3px" }}>{f.name}</p>
                      {f.description && (
                        <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>{f.description}</p>
                      )}
                    </div>
                    <p style={{ fontSize: "18px", fontWeight: 800, color: "var(--pk-gold)", margin: 0, flexShrink: 0, marginLeft: "12px" }}>
                      {fmt(f.raised)}
                    </p>
                  </div>

                  {pct !== null && (
                    <>
                      <div style={{ height: "5px", background: "var(--pk-b1)", borderRadius: "100px", overflow: "hidden" }}>
                        <div
                          style={{
                            height:       "100%",
                            width:        `${pct}%`,
                            background:   pct >= 100 ? "#34C759" : "var(--pk-gold)",
                            borderRadius: "100px",
                            transition:   "width 0.6s ease",
                          }}
                        />
                      </div>
                      <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: "5px 0 0", textAlign: "right" }}>
                        {pct}% of {fmt(f.goal!)} goal
                      </p>
                    </>
                  )}
                </div>
              );
            })}

            {fundData.length === 0 && (
              <div style={{ background: "#FFFFFF", borderRadius: "14px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
                <p style={{ fontSize: "13px", color: "var(--pk-t3)", margin: 0 }}>No giving funds set up. Ask your church admin.</p>
              </div>
            )}
          </div>

          {/* My giving history */}
          {myDonations.length > 0 && (
            <>
              <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
                My Giving History
              </p>
              <div
                style={{
                  background:   "#FFFFFF",
                  borderRadius: "14px",
                  overflow:     "hidden",
                  boxShadow:    "var(--pk-shadow-sm)",
                }}
              >
                {myDonations.map((d, i) => (
                  <div
                    key={d.id}
                    style={{
                      display:      "flex",
                      justifyContent: "space-between",
                      alignItems:   "center",
                      padding:      "13px 18px",
                      borderBottom: i < myDonations.length - 1 ? "1px solid var(--pk-b1)" : "none",
                    }}
                  >
                    <div>
                      <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 2px" }}>
                        {d.fund.name}
                      </p>
                      <p style={{ fontSize: "11px", color: "var(--pk-t3)", margin: 0 }}>{timeAgo(d.createdAt)}</p>
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-gold)", margin: 0 }}>
                        {fmt(d.amount, d.currency)}
                      </p>
                      <span
                        style={{
                          fontSize:     "10px",
                          fontWeight:   600,
                          color:        "#34C759",
                          background:   "rgba(52,199,89,0.08)",
                          borderRadius: "100px",
                          padding:      "1px 7px",
                        }}
                      >
                        Recorded
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Right — donation form */}
        <div style={{ position: "sticky", top: "80px" }}>
          <DonationForm funds={fundData} />
        </div>
      </div>
    </div>
  );
}
