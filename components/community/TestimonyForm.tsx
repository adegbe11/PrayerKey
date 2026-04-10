"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const TAGS = ["Healing", "Provision", "Salvation", "Restoration", "Answered Prayer", "Breakthrough", "Peace", "Other"];

export default function TestimonyForm() {
  const router  = useRouter();
  const [form, setForm] = useState({ title: "", story: "", tags: [] as string[], anonymous: false });
  const [saving, setSaving] = useState(false);
  const [done, setDone]     = useState(false);
  const [error, setError]   = useState("");

  function toggleTag(t: string) {
    setForm((p) => ({
      ...p,
      tags: p.tags.includes(t) ? p.tags.filter((x) => x !== t) : [...p.tags, t],
    }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const res = await fetch("/api/community/testimony", {
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
      <div
        style={{
          background:   "#FFFFFF",
          borderRadius: "16px",
          padding:      "32px",
          textAlign:    "center",
          boxShadow:    "var(--pk-shadow-sm)",
        }}
      >
        <p style={{ fontSize: "32px", marginBottom: "12px" }}>✦</p>
        <p style={{ fontSize: "16px", fontWeight: 700, color: "var(--pk-t1)", marginBottom: "6px" }}>
          Testimony submitted!
        </p>
        <p style={{ fontSize: "14px", color: "var(--pk-t3)", marginBottom: "20px" }}>
          Your testimony is pending approval and will appear in the feed shortly.
        </p>
        <button
          onClick={() => { setDone(false); setForm({ title: "", story: "", tags: [], anonymous: false }); }}
          style={{ fontSize: "13px", color: "var(--pk-gold)", fontWeight: 600, background: "none", border: "none", cursor: "pointer" }}
        >
          Share another →
        </button>
      </div>
    );
  }

  return (
    <div style={{ background: "#FFFFFF", borderRadius: "16px", padding: "24px", boxShadow: "var(--pk-shadow-sm)" }}>
      <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "16px" }}>
        Share Your Testimony
      </p>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>Title *</label>
          <input
            type="text"
            placeholder="What did God do?"
            value={form.title}
            onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
            required
            style={inputStyle}
          />
        </div>

        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>Your story *</label>
          <textarea
            placeholder="Share what happened…"
            value={form.story}
            onChange={(e) => setForm((p) => ({ ...p, story: e.target.value }))}
            required
            rows={5}
            style={{ ...inputStyle, resize: "vertical" }}
          />
        </div>

        {/* Tags */}
        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "8px" }}>Tags</label>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
            {TAGS.map((t) => {
              const active = form.tags.includes(t);
              return (
                <button
                  key={t}
                  type="button"
                  onClick={() => toggleTag(t)}
                  style={{
                    padding:      "5px 12px",
                    borderRadius: "100px",
                    border:       active ? "1.5px solid var(--pk-gold)" : "1px solid var(--pk-b1)",
                    background:   active ? "rgba(176,124,31,0.08)" : "#FAFAFA",
                    color:        active ? "var(--pk-gold)" : "var(--pk-t2)",
                    fontSize:     "12px",
                    fontWeight:   active ? 600 : 400,
                    cursor:       "pointer",
                  }}
                >
                  {t}
                </button>
              );
            })}
          </div>
        </div>

        {/* Anonymous toggle */}
        <label style={{ display: "flex", alignItems: "center", gap: "10px", cursor: "pointer" }}>
          <input
            type="checkbox"
            checked={form.anonymous}
            onChange={(e) => setForm((p) => ({ ...p, anonymous: e.target.checked }))}
            style={{ width: "16px", height: "16px", accentColor: "var(--pk-gold)" }}
          />
          <span style={{ fontSize: "13px", color: "var(--pk-t2)" }}>Post anonymously</span>
        </label>

        {error && <p style={{ fontSize: "12px", color: "#FF3B30" }}>{error}</p>}

        <button
          type="submit"
          disabled={saving || !form.title || !form.story}
          style={{
            padding:      "11px 20px",
            borderRadius: "980px",
            border:       "none",
            background:   saving || !form.title || !form.story ? "var(--pk-b1)" : "var(--pk-t1)",
            color:        saving || !form.title || !form.story ? "var(--pk-t3)" : "#FFFFFF",
            fontSize:     "14px",
            fontWeight:   600,
            cursor:       saving || !form.title || !form.story ? "not-allowed" : "pointer",
          }}
        >
          {saving ? "Submitting…" : "Submit Testimony"}
        </button>
      </form>
    </div>
  );
}
