"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

interface Props {
  churchId: string;
}

export default function CreateEventForm({ churchId }: Props) {
  const router = useRouter();
  const [form, setForm] = useState({
    title:       "",
    date:        "",
    time:        "10:00",
    location:    "",
    description: "",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState("");

  function update(field: string, value: string) {
    setForm((p) => ({ ...p, [field]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.title || !form.date) return;
    setSaving(true);
    setError("");

    try {
      const dateTime = new Date(`${form.date}T${form.time}:00`);
      const res = await fetch("/api/church/events", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({
          title:       form.title,
          date:        dateTime.toISOString(),
          location:    form.location || undefined,
          description: form.description || undefined,
          churchId,
        }),
      });

      if (!res.ok) {
        const d = await res.json().catch(() => ({})) as { error?: string };
        throw new Error(d.error ?? "Failed to create event");
      }

      setForm({ title: "", date: "", time: "10:00", location: "", description: "" });
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

  return (
    <div
      style={{
        background:   "#FFFFFF",
        borderRadius: "16px",
        padding:      "24px",
        boxShadow:    "var(--pk-shadow-sm)",
        position:     "sticky",
        top:          "80px",
      }}
    >
      <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "16px" }}>
        New Event
      </p>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "12px" }}>

        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
            Title *
          </label>
          <input
            type="text"
            placeholder="Sunday Worship Service"
            value={form.title}
            onChange={(e) => update("title", e.target.value)}
            required
            style={inputStyle}
          />
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
          <div>
            <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
              Date *
            </label>
            <input
              type="date"
              value={form.date}
              onChange={(e) => update("date", e.target.value)}
              required
              style={inputStyle}
            />
          </div>
          <div>
            <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
              Time
            </label>
            <input
              type="time"
              value={form.time}
              onChange={(e) => update("time", e.target.value)}
              style={inputStyle}
            />
          </div>
        </div>

        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
            Location
          </label>
          <input
            type="text"
            placeholder="Main Sanctuary"
            value={form.location}
            onChange={(e) => update("location", e.target.value)}
            style={inputStyle}
          />
        </div>

        <div>
          <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
            Description
          </label>
          <textarea
            placeholder="Optional details…"
            value={form.description}
            onChange={(e) => update("description", e.target.value)}
            rows={3}
            style={{ ...inputStyle, resize: "vertical" }}
          />
        </div>

        {error && (
          <p style={{ fontSize: "12px", color: "#FF3B30" }}>{error}</p>
        )}

        <button
          type="submit"
          disabled={saving || !form.title || !form.date}
          style={{
            padding:      "11px 20px",
            borderRadius: "980px",
            border:       "none",
            background:   saving || !form.title || !form.date ? "var(--pk-b1)" : "var(--pk-t1)",
            color:        saving || !form.title || !form.date ? "var(--pk-t3)" : "#FFFFFF",
            fontSize:     "14px",
            fontWeight:   600,
            cursor:       saving || !form.title || !form.date ? "not-allowed" : "pointer",
            marginTop:    "4px",
          }}
        >
          {saving ? "Creating…" : "Create Event"}
        </button>
      </form>
    </div>
  );
}
