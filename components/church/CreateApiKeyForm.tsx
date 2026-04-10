"use client";

import { useState } from "react";

const SCOPE_OPTIONS = ["read", "write", "members", "donations", "prayers", "events"];

interface CreatedKey {
  key: string;
  name: string;
  prefix: string;
  scopes: string[];
}

export default function CreateApiKeyForm({ onCreated }: { onCreated: () => void }) {
  const [name,         setName]         = useState("");
  const [scopes,       setScopes]       = useState<string[]>(["read"]);
  const [expiresInDays, setExpiresInDays] = useState("");
  const [loading,      setLoading]      = useState(false);
  const [created,      setCreated]      = useState<CreatedKey | null>(null);
  const [copied,       setCopied]       = useState(false);

  function toggleScope(s: string) {
    setScopes((p) => p.includes(s) ? p.filter((x) => x !== s) : [...p, s]);
  }

  async function handleCreate() {
    if (!name.trim() || scopes.length === 0) return;
    setLoading(true);
    try {
      const res = await fetch("/api/church/api-keys", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          scopes,
          expiresInDays: expiresInDays ? parseInt(expiresInDays) : undefined,
        }),
      });
      const data = await res.json() as CreatedKey & { error?: string };
      if (!res.ok) throw new Error(data.error ?? "Failed");
      setCreated(data);
      setName("");
      setScopes(["read"]);
      setExpiresInDays("");
    } catch (err) {
      alert(err instanceof Error ? err.message : "Error creating key");
    } finally {
      setLoading(false);
    }
  }

  function handleCopy() {
    if (!created) return;
    navigator.clipboard.writeText(created.key);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  function handleDone() {
    setCreated(null);
    onCreated();
  }

  // ── Show created key (one-time reveal) ──
  if (created) {
    return (
      <div style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, border: "2px solid #22C55E" }}>
        <p style={{ fontSize: 11, fontWeight: 700, color: "#22C55E", letterSpacing: "1px", textTransform: "uppercase", margin: "0 0 8px" }}>
          ✓ API Key Created
        </p>
        <p style={{ fontSize: 13, color: "#1D1D1F", fontWeight: 700, margin: "0 0 4px" }}>{created.name}</p>
        <p style={{ fontSize: 12, color: "#6E6E73", margin: "0 0 16px" }}>
          Scopes: {created.scopes.join(", ")}
        </p>

        <p style={{ fontSize: 12, color: "#EF4444", fontWeight: 600, margin: "0 0 8px" }}>
          ⚠ Copy this key now — it won&apos;t be shown again.
        </p>
        <div style={{ backgroundColor: "#F5F5F7", borderRadius: 10, padding: "12px 14px", fontFamily: "monospace", fontSize: 12, color: "#1D1D1F", wordBreak: "break-all", marginBottom: 12 }}>
          {created.key}
        </div>

        <div style={{ display: "flex", gap: 10 }}>
          <button
            onClick={handleCopy}
            style={{ flex: 1, padding: "10px 0", borderRadius: 10, backgroundColor: copied ? "#22C55E" : "#1D1D1F", color: "#fff", border: "none", fontWeight: 700, fontSize: 13, cursor: "pointer" }}
          >
            {copied ? "✓ Copied!" : "Copy Key"}
          </button>
          <button
            onClick={handleDone}
            style={{ flex: 1, padding: "10px 0", borderRadius: 10, backgroundColor: "#F5F5F7", color: "#1D1D1F", border: "1px solid #E5E5EA", fontWeight: 600, fontSize: 13, cursor: "pointer" }}
          >
            Done
          </button>
        </div>
      </div>
    );
  }

  // ── Create form ──
  return (
    <div style={{ backgroundColor: "#fff", borderRadius: 16, padding: 24, border: "1px solid #E5E5EA" }}>
      <h3 style={{ fontSize: 15, fontWeight: 700, color: "#1D1D1F", margin: "0 0 20px" }}>New API Key</h3>

      <label style={{ display: "block", fontSize: 11, fontWeight: 700, color: "#6E6E73", letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 6 }}>
        Key Name
      </label>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="e.g. Mobile App Integration"
        style={{ width: "100%", boxSizing: "border-box", padding: "10px 12px", borderRadius: 10, border: "1px solid #E5E5EA", fontSize: 14, color: "#1D1D1F", marginBottom: 16, outline: "none" }}
      />

      <label style={{ display: "block", fontSize: 11, fontWeight: 700, color: "#6E6E73", letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 8 }}>
        Scopes
      </label>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginBottom: 16 }}>
        {SCOPE_OPTIONS.map((s) => {
          const active = scopes.includes(s);
          return (
            <button
              key={s}
              onClick={() => toggleScope(s)}
              style={{ padding: "5px 12px", borderRadius: 20, border: `1px solid ${active ? "#B07C1F" : "#E5E5EA"}`, backgroundColor: active ? "#B07C1F18" : "#F5F5F7", color: active ? "#B07C1F" : "#6E6E73", fontWeight: active ? 700 : 400, fontSize: 12, cursor: "pointer" }}
            >
              {s}
            </button>
          );
        })}
      </div>

      <label style={{ display: "block", fontSize: 11, fontWeight: 700, color: "#6E6E73", letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 6 }}>
        Expires In (days, optional)
      </label>
      <input
        value={expiresInDays}
        onChange={(e) => setExpiresInDays(e.target.value)}
        placeholder="Leave blank for no expiry"
        type="number"
        min="1"
        style={{ width: "100%", boxSizing: "border-box", padding: "10px 12px", borderRadius: 10, border: "1px solid #E5E5EA", fontSize: 14, color: "#1D1D1F", marginBottom: 20, outline: "none" }}
      />

      <button
        onClick={handleCreate}
        disabled={!name.trim() || scopes.length === 0 || loading}
        style={{ width: "100%", padding: "12px 0", borderRadius: 10, backgroundColor: !name.trim() || scopes.length === 0 || loading ? "#E5E5EA" : "#B07C1F", color: "#fff", border: "none", fontWeight: 700, fontSize: 14, cursor: !name.trim() || scopes.length === 0 || loading ? "not-allowed" : "pointer" }}
      >
        {loading ? "Creating…" : "Generate API Key"}
      </button>
    </div>
  );
}
