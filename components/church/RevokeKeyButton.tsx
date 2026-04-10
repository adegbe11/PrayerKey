"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function RevokeKeyButton({ keyId }: { keyId: string }) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  async function handleRevoke() {
    if (!confirm("Revoke this API key? Any integrations using it will stop working immediately.")) return;
    setLoading(true);
    try {
      await fetch(`/api/church/api-keys/${keyId}`, { method: "DELETE" });
      router.refresh();
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={handleRevoke}
      disabled={loading}
      style={{ fontSize: 12, fontWeight: 600, color: "#EF4444", background: "none", border: "1px solid #EF444440", borderRadius: 8, padding: "4px 10px", cursor: "pointer" }}
    >
      {loading ? "…" : "Revoke"}
    </button>
  );
}
