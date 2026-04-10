import { auth } from "@/lib/auth";
import { redirect } from "next/navigation";
import { prisma } from "@/lib/prisma";
import Link from "next/link";
import CreateApiKeyForm from "@/components/church/CreateApiKeyForm";
import RevokeKeyButton from "@/components/church/RevokeKeyButton";

export default async function ApiKeysPage() {
  const session = await auth();
  const user = session?.user as { id?: string; role?: string; churchId?: string | null } | undefined;
  if (!user?.id || !user.churchId) redirect("/login");
  if (!["SUPER_ADMIN", "CHURCH_ADMIN", "PASTOR"].includes(user.role ?? "")) redirect("/church");

  const keys = await prisma.apiKey.findMany({
    where:   { churchId: user.churchId },
    orderBy: { createdAt: "desc" },
  });

  const activeKeys  = keys.filter((k) => !k.revokedAt);
  const revokedKeys = keys.filter((k) => k.revokedAt);

  function timeAgo(d: Date) {
    const diff = Math.floor((Date.now() - new Date(d).getTime()) / 1000);
    if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
  }

  return (
    <main style={{ minHeight: "100vh", backgroundColor: "#F5F5F7", padding: "40px 24px" }}>
      <div style={{ maxWidth: 1000, margin: "0 auto" }}>

        {/* Header */}
        <div style={{ marginBottom: 32 }}>
          <Link href="/church" style={{ fontSize: 12, color: "#B07C1F", fontWeight: 600, textDecoration: "none" }}>← Church Dashboard</Link>
          <h1 style={{ fontSize: 28, fontWeight: 700, color: "#1D1D1F", margin: "8px 0 4px" }}>API Keys</h1>
          <p style={{ color: "#6E6E73", fontSize: 14 }}>
            Integrate PrayerKey data with your website, app, or external tools.
          </p>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 340px", gap: 24, alignItems: "start" }}>

          {/* Keys list */}
          <div>
            {/* Base URL info card */}
            <div style={{ backgroundColor: "#1D1D1F", borderRadius: 14, padding: 20, marginBottom: 24 }}>
              <p style={{ margin: "0 0 6px", fontSize: 11, fontWeight: 700, color: "#B07C1F", letterSpacing: "1px", textTransform: "uppercase" }}>REST API v1 Base URL</p>
              <code style={{ fontSize: 13, color: "#fff" }}>
                {process.env.NEXT_PUBLIC_APP_URL ?? "https://your-domain.com"}/api/v1
              </code>
              <p style={{ margin: "10px 0 0", fontSize: 12, color: "#6E6E73" }}>
                Authenticate with: <code style={{ color: "#B07C1F" }}>Authorization: Bearer pk_live_…</code>
              </p>
            </div>

            {/* Active keys */}
            <div style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, marginBottom: 20, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
              <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>
                Active Keys <span style={{ fontSize: 13, color: "#22C55E", fontWeight: 800 }}>({activeKeys.length})</span>
              </h2>

              {activeKeys.length === 0 && (
                <p style={{ fontSize: 13, color: "#6E6E73" }}>No active keys yet. Create one to get started.</p>
              )}

              {activeKeys.map((k) => (
                <div key={k.id} style={{ marginBottom: 14, padding: "14px 16px", borderRadius: 12, backgroundColor: "#F5F5F7", border: "1px solid #E5E5EA" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 6 }}>
                    <div>
                      <p style={{ margin: 0, fontSize: 14, fontWeight: 700, color: "#1D1D1F" }}>{k.name}</p>
                      <code style={{ fontSize: 12, color: "#6E6E73" }}>{k.keyPrefix}••••••••••••••••</code>
                    </div>
                    <RevokeKeyButton keyId={k.id} />
                  </div>

                  <div style={{ display: "flex", flexWrap: "wrap", gap: 6, marginTop: 8 }}>
                    {k.scopes.map((s) => (
                      <span key={s} style={{ fontSize: 10, fontWeight: 700, padding: "2px 8px", borderRadius: 10, backgroundColor: "#B07C1F18", color: "#B07C1F", textTransform: "uppercase" }}>
                        {s}
                      </span>
                    ))}
                  </div>

                  <p style={{ margin: "8px 0 0", fontSize: 11, color: "#6E6E73" }}>
                    Created {timeAgo(k.createdAt)}
                    {k.lastUsedAt ? ` · Last used ${timeAgo(k.lastUsedAt)}` : " · Never used"}
                    {k.expiresAt ? ` · Expires ${new Date(k.expiresAt).toLocaleDateString()}` : ""}
                  </p>
                </div>
              ))}
            </div>

            {/* Revoked keys (collapsed) */}
            {revokedKeys.length > 0 && (
              <div style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)", opacity: 0.7 }}>
                <h2 style={{ fontSize: 14, fontWeight: 700, color: "#6E6E73", margin: "0 0 12px" }}>
                  Revoked ({revokedKeys.length})
                </h2>
                {revokedKeys.map((k) => (
                  <div key={k.id} style={{ marginBottom: 8, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div>
                      <p style={{ margin: 0, fontSize: 13, color: "#6E6E73", textDecoration: "line-through" }}>{k.name}</p>
                      <code style={{ fontSize: 11, color: "#A1A1A6" }}>{k.keyPrefix}••••</code>
                    </div>
                    <span style={{ fontSize: 11, color: "#EF4444", fontWeight: 600 }}>
                      Revoked {timeAgo(k.revokedAt!)}
                    </span>
                  </div>
                ))}
              </div>
            )}

            {/* Quick reference */}
            <div style={{ marginTop: 24, backgroundColor: "#fff", borderRadius: 16, padding: 24, boxShadow: "0 1px 3px rgba(0,0,0,0.05)" }}>
              <h2 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 16px" }}>Quick Reference</h2>
              {[
                { method: "GET",  path: "/api/v1/members",     desc: "List church members" },
                { method: "GET",  path: "/api/v1/prayers",     desc: "List generated prayers" },
                { method: "GET",  path: "/api/v1/testimonies", desc: "List approved testimonies" },
                { method: "GET",  path: "/api/v1/donations",   desc: "List donations" },
                { method: "GET",  path: "/api/v1/events",      desc: "List events" },
                { method: "POST", path: "/api/v1/events",      desc: "Create an event" },
                { method: "GET",  path: "/api/v1/services",    desc: "List sermon services" },
              ].map((e) => (
                <div key={e.path + e.method} style={{ display: "flex", gap: 10, alignItems: "center", marginBottom: 10 }}>
                  <span style={{ fontSize: 10, fontWeight: 700, width: 40, textAlign: "center", padding: "2px 0", borderRadius: 5, backgroundColor: e.method === "GET" ? "#3B82F618" : "#22C55E18", color: e.method === "GET" ? "#3B82F6" : "#22C55E" }}>
                    {e.method}
                  </span>
                  <code style={{ fontSize: 12, color: "#1D1D1F", flex: 1 }}>{e.path}</code>
                  <span style={{ fontSize: 12, color: "#6E6E73" }}>{e.desc}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Create form — sticky sidebar */}
          <div style={{ position: "sticky", top: 24 }}>
            <CreateApiKeyForm onCreated={() => {}} />
          </div>
        </div>

      </div>
    </main>
  );
}
