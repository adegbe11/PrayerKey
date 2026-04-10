"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function PrayerRequestForm() {
  const router  = useRouter();
  const [form, setForm] = useState({ title: "", body: "", anonymous: false });
  const [saving, setSaving] = useState(false);
  const [done, setDone]     = useState(false);
  const [error, setError]   = useState("");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const res = await fetch("/api/community/prayer-request", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify(form),
      });
      if (!res.ok) {
        const d = await res.json().catch(() => ({})) as { error?: string };
        throw new Error(d.error ?? "Failed");
      }
      setDone(true);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setSaving(false);
    }
  }

  const inputStyle: React.CSSProperties = {
    width:        "100%",
    padding:      "10px 13px",
    borderRadius: "10px",
    border:       "1px solid var(--pk-b1)",
    background:   "#FAFAFA",
    color:        "var(--pk-t1)",
    fontSize:     "14px",
    outline:      "none",
    fontFamily:   "inherit",
    boxSizing:    "border-box",
  };

  if (done) {
    return (
      <div style={{ background: "#FFFFFF", borderRadius: "16px", padding: "28px", textAlign: "center", boxShadow: "var(--pk-shadow-sm)" }}>
        <p style={{ fontSize: "28px", marginBottom: "10px" }}>🙏</p>
        <p style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-t1)", marginBottom: "5px" }}>Request submitted!</p>
        <p style={{ fontSize: "13px", color: "var(--pk-t3)", marginBottom: "18px" }}>Your church family will be praying for you.</p>
        <button
          onClick={() => { setDone(false); setForm({ title: "", body: "", anonymous: false }); }}
          style={{ fontSize: "13px", color: "#0071E3", fontWeight: 600, background: "none", border: "none", cursor: "pointer" }}
        >
          Add another →
        </button>
      </div>
    );
  }

  return (
    <div style={{ background: "#FFFFFF", borderRadius: "16px", padding: "24px", boxShadow: "var(--pk-shadow-sm)" }}>
      <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "16px" }}>
        Share a Prayer Request
      </p>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>Title *</label>
          <input
            type="text"
            placeholder="What do you need prayer for?"
            value={form.title}
            onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
            required
            style={inputStyle}
          />
        </div>

        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>Details *</label>
          <textarea
            placeholder="Share as much as you'd like…"
            value={form.body}
            onChange={(e) => setForm((p) => ({ ...p, body: e.target.value }))}
            required
            rows={4}
            style={{ ...inputStyle, resize: "vertical" }}
          />
        </div>

        <label style={{ display: "flex", alignItems: "center", gap: "10px", cursor: "pointer" }}>
          <input
            type="checkbox"
            checked={form.anonymous}
            onChange={(e) => setForm((p) => ({ ...p, anonymous: e.target.checked }))}
            style={{ width: "16px", height: "16px", accentColor: "#0071E3" }}
          />
          <span style={{ fontSize: "13px", color: "var(--pk-t2)" }}>Post anonymously</span>
        </label>

        {error && <p style={{ fontSize: "12px", color: "#FF3B30" }}>{error}</p>}

        <button
          type="submit"
          disabled={saving || !form.title || !form.body}
          style={{
            padding:      "11px 20px",
            borderRadius: "980px",
            border:       "none",
            background:   saving || !form.title || !form.body ? "var(--pk-b1)" : "#0071E3",
            color:        saving || !form.title || !form.body ? "var(--pk-t3)" : "#FFFFFF",
            fontSize:     "14px",
            fontWeight:   600,
            cursor:       saving || !form.title || !form.body ? "not-allowed" : "pointer",
          }}
        >
          {saving ? "Submitting…" : "Submit Request"}
        </button>
      </form>
    </div>
  );
}
