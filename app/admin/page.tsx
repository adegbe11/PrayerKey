import { auth } from "@/lib/auth";
import { redirect } from "next/navigation";
import { prisma } from "@/lib/prisma";
import Link from "next/link";

async function getSystemStats() {
  const [
    churchCount, memberCount, prayerCount,
    donationAgg, testimonyCount, serviceCount,
    churches,
  ] = await Promise.all([
    prisma.church.count(),
    prisma.user.count(),
    prisma.prayer.count(),
    prisma.donation.aggregate({ _sum: { amount: true }, where: { status: "COMPLETED" } }),
    prisma.testimony.count(),
    prisma.service.count(),
    prisma.church.findMany({
      orderBy: { createdAt: "desc" },
      take: 10,
      include: {
        _count: { select: { members: true, donations: true, services: true } },
      },
    }),
  ]);
  return { churchCount, memberCount, prayerCount, donationAgg, testimonyCount, serviceCount, churches };
}

function fmt(cents: number) {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(cents / 100);
}

export default async function AdminPage() {
  const session = await auth();
  const user = session?.user as { role?: string } | undefined;
  if (user?.role !== "SUPER_ADMIN") redirect("/church");

  const { churchCount, memberCount, prayerCount, donationAgg, testimonyCount, serviceCount, churches } = await getSystemStats();
  const totalRaised = donationAgg._sum.amount ?? 0;

  const stats = [
    { label: "Churches",    value: churchCount.toLocaleString(),   color: "#B07C1F" },
    { label: "Members",     value: memberCount.toLocaleString(),   color: "#3B82F6" },
    { label: "Prayers",     value: prayerCount.toLocaleString(),   color: "#8B5CF6" },
    { label: "Total Given", value: fmt(totalRaised),               color: "#22C55E" },
    { label: "Testimonies", value: testimonyCount.toLocaleString(), color: "#F97316" },
    { label: "Services",    value: serviceCount.toLocaleString(),  color: "#EF4444" },
  ];

  return (
    <main style={{ minHeight: "100vh", backgroundColor: "#F5F5F7", padding: "40px 24px" }}>
      <div style={{ maxWidth: 1100, margin: "0 auto" }}>

        {/* Header */}
        <div style={{ marginBottom: 32 }}>
          <p style={{ fontSize: 11, fontWeight: 700, color: "#B07C1F", letterSpacing: "1.2px", textTransform: "uppercase", marginBottom: 4 }}>
            Super Admin
          </p>
          <h1 style={{ fontSize: 34, fontWeight: 700, color: "#1D1D1F", margin: 0 }}>System Dashboard</h1>
          <p style={{ color: "#6E6E73", marginTop: 6 }}>All churches · Global platform view</p>
        </div>

        {/* KPI grid */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))", gap: 16, marginBottom: 40 }}>
          {stats.map((s) => (
            <div key={s.label} style={{ backgroundColor: "#fff", borderRadius: 16, padding: "20px 20px", borderTop: `3px solid ${s.color}`, boxShadow: "0 1px 4px rgba(0,0,0,0.06)" }}>
              <p style={{ fontSize: 28, fontWeight: 800, color: s.color, margin: 0 }}>{s.value}</p>
              <p style={{ fontSize: 12, color: "#6E6E73", marginTop: 4 }}>{s.label}</p>
            </div>
          ))}
        </div>

        {/* Churches table */}
        <div style={{ backgroundColor: "#fff", borderRadius: 20, padding: 28, boxShadow: "0 1px 4px rgba(0,0,0,0.06)" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
            <h2 style={{ fontSize: 18, fontWeight: 700, color: "#1D1D1F", margin: 0 }}>All Churches</h2>
            <Link href="/admin/churches" style={{ fontSize: 13, color: "#B07C1F", fontWeight: 600, textDecoration: "none" }}>
              View all →
            </Link>
          </div>

          <div style={{ overflowX: "auto" }}>
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr style={{ borderBottom: "1px solid #F2F2F7" }}>
                  {["Church", "Slug", "Plan", "Members", "Donations", "Services", ""].map((h) => (
                    <th key={h} style={{ textAlign: "left", padding: "8px 12px", color: "#6E6E73", fontWeight: 600, fontSize: 11, letterSpacing: "0.5px", textTransform: "uppercase" }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {churches.map((c) => (
                  <tr key={c.id} style={{ borderBottom: "1px solid #F2F2F7" }}>
                    <td style={{ padding: "12px 12px", fontWeight: 600, color: "#1D1D1F" }}>{c.name}</td>
                    <td style={{ padding: "12px 12px", color: "#6E6E73", fontFamily: "monospace" }}>{c.slug}</td>
                    <td style={{ padding: "12px 12px" }}>
                      <span style={{ fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 20, backgroundColor: "#B07C1F18", color: "#B07C1F" }}>
                        {c.plan}
                      </span>
                    </td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F" }}>{c._count.members}</td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F" }}>{c._count.donations}</td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F" }}>{c._count.services}</td>
                    <td style={{ padding: "12px 12px" }}>
                      <Link href={`/admin/churches/${c.id}`} style={{ fontSize: 12, color: "#B07C1F", fontWeight: 600, textDecoration: "none" }}>
                        Manage →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Nav links */}
        <div style={{ display: "flex", gap: 12, marginTop: 24, flexWrap: "wrap" }}>
          {[
            { href: "/admin/churches", label: "🏛 All Churches" },
            { href: "/church",         label: "⛪ My Church" },
            { href: "/church/api-keys", label: "🔑 API Keys" },
          ].map((l) => (
            <Link key={l.href} href={l.href} style={{ fontSize: 13, fontWeight: 600, color: "#1D1D1F", backgroundColor: "#fff", padding: "10px 18px", borderRadius: 12, textDecoration: "none", border: "1px solid #E5E5EA", boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
              {l.label}
            </Link>
          ))}
        </div>

      </div>
    </main>
  );
}
