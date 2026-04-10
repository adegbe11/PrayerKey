"use client";

import { useState } from "react";

interface Props {
  requestId:    string;
  initialCount: number;
}

export default function PrayButton({ requestId, initialCount }: Props) {
  const [count, setCount]     = useState(initialCount);
  const [pressed, setPressed] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handlePray() {
    if (pressed || loading) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/community/prayer-request/${requestId}/pray`, {
        method: "POST",
      });
      if (res.ok) {
        setCount((c) => c + 1);
        setPressed(true);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={handlePray}
      disabled={pressed || loading}
      title={pressed ? "Praying!" : "I'll pray for this"}
      style={{
        display:      "flex",
        alignItems:   "center",
        gap:          "6px",
        padding:      "6px 12px",
        borderRadius: "100px",
        border:       pressed
          ? "1.5px solid rgba(0,113,227,0.35)"
          : "1px solid var(--pk-b1)",
        background:   pressed ? "rgba(0,113,227,0.07)" : "transparent",
        color:        pressed ? "#0071E3" : "var(--pk-t3)",
        fontSize:     "13px",
        fontWeight:   pressed ? 700 : 500,
        cursor:       pressed ? "default" : "pointer",
        transition:   "all 0.2s ease",
        opacity:      loading ? 0.6 : 1,
      }}
    >
      <span style={{ fontSize: "15px" }}>🙏</span>
      <span>{pressed ? "Praying!" : "Pray"}</span>
      {count > 0 && (
        <span
          style={{
            background:   pressed ? "rgba(0,113,227,0.12)" : "var(--pk-b1)",
            borderRadius: "100px",
            padding:      "1px 7px",
            fontSize:     "11px",
            fontWeight:   700,
          }}
        >
          {count}
        </span>
      )}
    </button>
  );
}
