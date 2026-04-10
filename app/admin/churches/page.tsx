import { auth } from "@/lib/auth";
import { redirect } from "next/navigation";
import { prisma } from "@/lib/prisma";
import Link from "next/link";

export default async function AdminChurchesPage() {
  const session = await auth();
  const user = session?.user as { role?: string } | undefined;
  if (user?.role !== "SUPER_ADMIN") redirect("/church");

  const churches = await prisma.church.findMany({
    orderBy: { createdAt: "desc" },
    include: {
      _count: { select: { members: true, donations: true, services: true, sermons: true, apiKeys: true } },
    },
  });

  const planColor: Record<string, string> = {
    STARTER: "#6E6E73", PLUS: "#3B82F6", CORE: "#B07C1F", ENTERPRISE: "#8B5CF6",
  };

  return (
    <main style={{ minHeight: "100vh", backgroundColor: "#F5F5F7", padding: "40px 24px" }}>
      <div style={{ maxWidth: 1100, margin: "0 auto" }}>

        <div style={{ marginBottom: 28 }}>
          <Link href="/admin" style={{ fontSize: 12, color: "#B07C1F", fontWeight: 600, textDecoration: "none" }}>← Admin</Link>
          <h1 style={{ fontSize: 28, fontWeight: 700, color: "#1D1D1F", margin: "8px 0 4px" }}>All Churches</h1>
          <p style={{ color: "#6E6E73", fontSize: 14 }}>{churches.length} registered</p>
        </div>

        <div style={{ backgroundColor: "#fff", borderRadius: 20, padding: 28, boxShadow: "0 1px 4px rgba(0,0,0,0.06)" }}>
          <div style={{ overflowX: "auto" }}>
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr style={{ borderBottom: "1px solid #F2F2F7" }}>
                  {["Church", "Slug", "Location", "Plan", "Members", "Sermons", "Services", "API Keys", "Joined", ""].map((h) => (
                    <th key={h} style={{ textAlign: "left", padding: "8px 12px", color: "#6E6E73", fontWeight: 600, fontSize: 11, letterSpacing: "0.5px", textTransform: "uppercase", whiteSpace: "nowrap" }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {churches.map((c) => (
                  <tr key={c.id} style={{ borderBottom: "1px solid #F2F2F7" }}>
                    <td style={{ padding: "12px 12px", fontWeight: 700, color: "#1D1D1F", whiteSpace: "nowrap" }}>{c.name}</td>
                    <td style={{ padding: "12px 12px", color: "#6E6E73", fontFamily: "monospace", fontSize: 12 }}>{c.slug}</td>
                    <td style={{ padding: "12px 12px", color: "#6E6E73", whiteSpace: "nowrap" }}>
                      {[c.city, c.country].filter(Boolean).join(", ") || "—"}
                    </td>
                    <td style={{ padding: "12px 12px" }}>
                      <span style={{ fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 20, backgroundColor: `${planColor[c.plan] ?? "#6E6E73"}18`, color: planColor[c.plan] ?? "#6E6E73" }}>
                        {c.plan}
                      </span>
                    </td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F", textAlign: "center" }}>{c._count.members}</td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F", textAlign: "center" }}>{c._count.sermons}</td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F", textAlign: "center" }}>{c._count.services}</td>
                    <td style={{ padding: "12px 12px", color: "#1D1D1F", textAlign: "center" }}>{c._count.apiKeys}</td>
                    <td style={{ padding: "12px 12px", color: "#6E6E73", whiteSpace: "nowrap" }}>
                      {new Date(c.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                    </td>
                    <td style={{ padding: "12px 12px" }}>
                      <Link href={`/admin/churches/${c.id}`} style={{ fontSize: 12, color: "#B07C1F", fontWeight: 600, textDecoration: "none", whiteSpace: "nowrap" }}>
                        Details →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  );
}
