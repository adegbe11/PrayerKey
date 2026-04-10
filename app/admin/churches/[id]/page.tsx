import { auth } from "@/lib/auth";
import { redirect, notFound } from "next/navigation";
import { prisma } from "@/lib/prisma";
import Link from "next/link";

export default async function AdminChurchDetailPage({ params }: { params: { id: string } }) {
  const session = await auth();
  const user = session?.user as { role?: string } | undefined;
  if (user?.role !== "SUPER_ADMIN") redirect("/church");

  const church = await prisma.church.findUnique({
    where: { id: params.id },
    include: {
      members:    { orderBy: { createdAt: "desc" }, take: 10, select: { id: true, name: true, email: true, role: true, createdAt: true } },
      donations:  { orderBy: { createdAt: "desc" }, take: 5,  select: { id: true, amount: true, currency: true, status: true, createdAt: true } },
      services:   { orderBy: { createdAt: "desc" }, take: 5,  select: { id: true, title: true, status: true, versesDetected: true, createdAt: true } },
      apiKeys:    { orderBy: { createdAt: "desc" },           select: { id: true, name: true, keyPrefix: true, scopes: true, lastUsedAt: true, revokedAt: true, createdAt: true } },
      _count: { select: { members: true, donations: true, services: true, sermons: true } },
    },
  });
  if (!church) notFound();

  const totalRaised = await prisma.donation.aggregate({
    where: { churchId: church.id, status: "COMPLETED" },
    _sum: { amount: true },
  });

  function fmt(cents: number) {
    return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(cents / 100);
  }

  const roleColor: Record<string, string> = {
    SUPER_ADMIN: "#EF4444", CHURCH_ADMIN: "#8B5CF6", PASTOR: "#B07C1F",
    VOLUNTEER: "#3B82F6",   MEMBER: "#6E6E73",
  };

  return (
    <main style={{ minHeight: "100vh", backgroundColor: "#F5F5F7", padding: "40px 24px" }}>
      <div style={{ maxWidth: 1100, margin: "0 auto" }}>

        <div style={{ marginBottom: 28 }}>
          <Link href="/admin/churches" style={{ fontSize: 12, color: "#B07C1F", fontWeight: 600, textDecoration: "none" }}>← All Churches</Link>
          <h1 style={{ fontSize: 28, fontWeight: 700, color: "#1D1D1F", margin: "8px 0 4px" }}>{church.name}</h1>
          <p style={{ color: "#6E6E73", fontSize: 14, fontFamily: "monospace" }}>{church.slug} · {church.plan}</p>
        </div>

        {/* KPI row */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16, marginBottom: 28 }}>
          {[
            { label: "Members",   value: church._count.members,                  color: "#3B82F6" },
            { label: "Sermons",   value: church._count.sermons,                  color: "#B07C1F" },
            { label: "Services",  value: church._count.services,                 color: "#EF4444" },
            { label: "Raised",    value: fmt(totalRaised._sum.amount ?? 0),      color: "#22C55E" },
          ].map((s) => (
            <div key={s.label} style={{ backgroundColor: "#fff", borderRadius: 14, padding: 20, borderTop: `3px solid ${s.color}`, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
              <p style={{ fontSize: 24, fontWeight: 800, color: s.color, margin: 0 }}>{s.value}</p>
              <p style={{ fontSize: 12, color: "#6E6E73", marginTop: 4 }}>{s.label}</p>
            </div>
          ))}
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
          {/* Recent members */}
          <section style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
            <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>Recent Members</h2>
            {church.members.map((m) => (
              <div key={m.id} style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 10 }}>
                <div style={{ width: 30, height: 30, borderRadius: 15, backgroundColor: "#B07C1F", display: "flex", alignItems: "center", justifyContent: "center", color: "#fff", fontWeight: 700, fontSize: 12, flexShrink: 0 }}>
                  {(m.name ?? m.email ?? "?").slice(0, 1).toUpperCase()}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: "#1D1D1F", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{m.name ?? m.email}</p>
                  <p style={{ margin: 0, fontSize: 11, color: "#6E6E73" }}>{m.email}</p>
                </div>
                <span style={{ fontSize: 10, fontWeight: 700, padding: "2px 6px", borderRadius: 10, backgroundColor: `${roleColor[m.role] ?? "#6E6E73"}18`, color: roleColor[m.role] ?? "#6E6E73", flexShrink: 0 }}>
                  {m.role}
                </span>
              </div>
            ))}
          </section>

          {/* API Keys */}
          <section style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
            <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>API Keys</h2>
            {church.apiKeys.length === 0 && (
              <p style={{ fontSize: 13, color: "#6E6E73" }}>No API keys created yet.</p>
            )}
            {church.apiKeys.map((k) => (
              <div key={k.id} style={{ marginBottom: 12, padding: "10px 12px", borderRadius: 10, backgroundColor: k.revokedAt ? "#F2F2F7" : "#F5F5F7", border: "1px solid #E5E5EA" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: k.revokedAt ? "#6E6E73" : "#1D1D1F" }}>{k.name}</p>
                  <span style={{ fontSize: 10, fontWeight: 700, padding: "2px 6px", borderRadius: 10, backgroundColor: k.revokedAt ? "#6E6E7318" : "#22C55E18", color: k.revokedAt ? "#6E6E73" : "#22C55E" }}>
                    {k.revokedAt ? "REVOKED" : "ACTIVE"}
                  </span>
                </div>
                <p style={{ margin: "4px 0 0", fontSize: 11, color: "#6E6E73", fontFamily: "monospace" }}>{k.keyPrefix}••••••••</p>
                <p style={{ margin: "2px 0 0", fontSize: 11, color: "#6E6E73" }}>
                  Scopes: {k.scopes.join(", ")} · {k.lastUsedAt ? `Last used ${new Date(k.lastUsedAt).toLocaleDateString()}` : "Never used"}
                </p>
              </div>
            ))}
          </section>

          {/* Recent services */}
          <section style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
            <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>Recent Services</h2>
            {church.services.map((s) => (
              <div key={s.id} style={{ marginBottom: 10, display: "flex", alignItems: "center", gap: 10 }}>
                <div style={{ width: 8, height: 8, borderRadius: 4, backgroundColor: s.status === "LIVE" ? "#EF4444" : "#22C55E", flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: "#1D1D1F" }}>{s.title}</p>
                  <p style={{ margin: 0, fontSize: 11, color: "#6E6E73" }}>{s.versesDetected} verses · {new Date(s.createdAt).toLocaleDateString()}</p>
                </div>
                <span style={{ fontSize: 11, fontWeight: 700, color: s.status === "LIVE" ? "#EF4444" : "#22C55E" }}>{s.status}</span>
              </div>
            ))}
          </section>

          {/* Recent donations */}
          <section style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
            <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>Recent Donations</h2>
            {church.donations.map((d) => (
              <div key={d.id} style={{ marginBottom: 10, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <p style={{ margin: 0, fontSize: 13, color: "#6E6E73" }}>{new Date(d.createdAt).toLocaleDateString()}</p>
                <p style={{ margin: 0, fontSize: 14, fontWeight: 700, color: "#22C55E" }}>
                  {new Intl.NumberFormat("en-US", { style: "currency", currency: d.currency, maximumFractionDigits: 0 }).format(d.amount / 100)}
                </p>
              </div>
            ))}
          </section>
        </div>

      </div>
    </main>
  );
}
