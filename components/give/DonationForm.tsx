"use client";

import { useState } from "react";

interface Fund {
  id:          string;
  name:        string;
  description: string | null;
  goal:        number | null;
  raised:      number;
}

interface Props {
  funds: Fund[];
}

const CURRENCIES = [
  { code: "USD", symbol: "$",  label: "USD — US Dollar"       },
  { code: "NGN", symbol: "₦",  label: "NGN — Nigerian Naira"  },
  { code: "GBP", symbol: "£",  label: "GBP — British Pound"   },
  { code: "EUR", symbol: "€",  label: "EUR — Euro"            },
  { code: "CAD", symbol: "CA$", label: "CAD — Canadian Dollar" },
  { code: "GHS", symbol: "₵",  label: "GHS — Ghana Cedi"      },
  { code: "KES", symbol: "KSh", label: "KES — Kenyan Shilling" },
  { code: "ZAR", symbol: "R",  label: "ZAR — South African Rand" },
  { code: "AUD", symbol: "A$", label: "AUD — Australian Dollar" },
];

const QUICK_AMOUNTS = [10, 25, 50, 100, 250, 500];

type Status = "idle" | "loading" | "success" | "error";

export default function DonationForm({ funds }: Props) {
  const [fundId,   setFundId]   = useState(funds[0]?.id ?? "");
  const [amount,   setAmount]   = useState("");
  const [currency, setCurrency] = useState("USD");
  const [status,   setStatus]   = useState<Status>("idle");
  const [errorMsg, setErrorMsg] = useState("");
  const [receipt,  setReceipt]  = useState<{ fundName: string; amount: number; currency: string } | null>(null);

  const currencyObj = CURRENCIES.find((c) => c.code === currency) ?? CURRENCIES[0];
  const selectedFund = funds.find((f) => f.id === fundId);

  async function handleGive(e: React.FormEvent) {
    e.preventDefault();
    const parsed = parseFloat(amount);
    if (!fundId || isNaN(parsed) || parsed <= 0) return;

    setStatus("loading");
    setErrorMsg("");

    try {
      const res = await fetch("/api/give", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ fundId, amount: parsed, currency }),
      });

      if (!res.ok) {
        const d = await res.json().catch(() => ({})) as { error?: string };
        throw new Error(d.error ?? "Failed to record donation");
      }

      const data = await res.json() as { fundName: string; amount: number; currency: string };
      setReceipt(data);
      setStatus("success");
      setAmount("");
    } catch (err) {
      setErrorMsg(err instanceof Error ? err.message : "Something went wrong");
      setStatus("error");
    }
  }

  // ── Success state ────────────────────────────────────────────────────────
  if (status === "success" && receipt) {
    const fmtAmount = new Intl.NumberFormat("en-US", {
      style:                 "currency",
      currency:              receipt.currency,
      maximumFractionDigits: 0,
    }).format(receipt.amount / 100);

    return (
      <div
        style={{
          background:   "#FFFFFF",
          borderRadius: "20px",
          padding:      "40px 32px",
          textAlign:    "center",
          boxShadow:    "var(--pk-shadow-md)",
        }}
      >
        <div
          style={{
            width:          "64px",
            height:         "64px",
            borderRadius:   "50%",
            background:     "rgba(52,199,89,0.10)",
            display:        "flex",
            alignItems:     "center",
            justifyContent: "center",
            margin:         "0 auto 16px",
            fontSize:       "28px",
          }}
        >
          ✓
        </div>
        <h2 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-t1)", marginBottom: "8px" }}>
          Thank you for giving!
        </h2>
        <p style={{ fontSize: "16px", color: "var(--pk-t2)", marginBottom: "4px" }}>
          {fmtAmount} to <strong style={{ color: "var(--pk-t1)" }}>{receipt.fundName}</strong>
        </p>
        <p style={{ fontSize: "13px", color: "var(--pk-t3)", marginBottom: "28px" }}>
          Your generosity makes a difference.
        </p>
        <button
          onClick={() => { setStatus("idle"); setReceipt(null); }}
          style={{
            padding:      "12px 28px",
            borderRadius: "980px",
            border:       "none",
            background:   "var(--pk-t1)",
            color:        "#FFFFFF",
            fontSize:     "15px",
            fontWeight:   600,
            cursor:       "pointer",
          }}
        >
          Give Again
        </button>
      </div>
    );
  }

  // ── Form ─────────────────────────────────────────────────────────────────
  return (
    <form
      onSubmit={handleGive}
      style={{
        background:   "#FFFFFF",
        borderRadius: "20px",
        padding:      "32px",
        boxShadow:    "var(--pk-shadow-md)",
      }}
    >
      <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-gold)", letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "20px" }}>
        Give
      </p>

      {/* Fund selector */}
      <div style={{ marginBottom: "20px" }}>
        <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "8px" }}>
          Giving Fund
        </label>
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
          {funds.map((f) => {
            const pct = f.goal ? Math.min(100, Math.round((f.raised / f.goal) * 100)) : null;
            const selected = fundId === f.id;
            return (
              <button
                key={f.id}
                type="button"
                onClick={() => setFundId(f.id)}
                style={{
                  textAlign:    "left",
                  padding:      "14px 16px",
                  borderRadius: "12px",
                  border:       selected ? "1.5px solid var(--pk-gold)" : "1px solid var(--pk-b1)",
                  background:   selected ? "rgba(176,124,31,0.04)" : "#FAFAFA",
                  cursor:       "pointer",
                  transition:   "all 0.2s ease",
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                  <div>
                    <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-t1)", margin: "0 0 2px" }}>{f.name}</p>
                    {f.description && (
                      <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: 0 }}>{f.description}</p>
                    )}
                  </div>
                  {selected && (
                    <span style={{ fontSize: "16px", color: "var(--pk-gold)", marginLeft: "8px", flexShrink: 0 }}>✓</span>
                  )}
                </div>
                {pct !== null && (
                  <div style={{ marginTop: "8px" }}>
                    <div style={{ height: "3px", background: "var(--pk-b1)", borderRadius: "100px", overflow: "hidden" }}>
                      <div style={{ height: "100%", width: `${pct}%`, background: pct >= 100 ? "#34C759" : "var(--pk-gold)", borderRadius: "100px" }} />
                    </div>
                    <p style={{ fontSize: "10px", color: "var(--pk-t3)", margin: "3px 0 0", textAlign: "right" }}>{pct}% funded</p>
                  </div>
                )}
              </button>
            );
          })}
        </div>
        {funds.length === 0 && (
          <p style={{ fontSize: "13px", color: "var(--pk-t3)", padding: "16px", textAlign: "center", background: "#FAFAFA", borderRadius: "10px", border: "1px solid var(--pk-b1)" }}>
            No giving funds set up yet. Contact your church admin.
          </p>
        )}
      </div>

      {/* Currency selector */}
      <div style={{ marginBottom: "20px" }}>
        <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "6px" }}>
          Currency
        </label>
        <div style={{ position: "relative" }}>
          <select
            value={currency}
            onChange={(e) => setCurrency(e.target.value)}
            style={{
              width:        "100%",
              padding:      "10px 36px 10px 13px",
              borderRadius: "10px",
              border:       "1px solid var(--pk-b1)",
              background:   "#FAFAFA",
              color:        "var(--pk-t1)",
              fontSize:     "14px",
              outline:      "none",
              appearance:   "none",
              fontFamily:   "inherit",
              cursor:       "pointer",
            }}
          >
            {CURRENCIES.map((c) => (
              <option key={c.code} value={c.code}>{c.label}</option>
            ))}
          </select>
          <span style={{ position: "absolute", right: "12px", top: "50%", transform: "translateY(-50%)", fontSize: "10px", color: "var(--pk-t3)", pointerEvents: "none" }}>▾</span>
        </div>
      </div>

      {/* Quick-amount buttons */}
      <div style={{ marginBottom: "14px" }}>
        <label style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", display: "block", marginBottom: "8px" }}>
          Amount ({currencyObj.symbol})
        </label>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginBottom: "10px" }}>
          {QUICK_AMOUNTS.map((q) => (
            <button
              key={q}
              type="button"
              onClick={() => setAmount(String(q))}
              style={{
                padding:      "7px 14px",
                borderRadius: "100px",
                border:       amount === String(q) ? "1.5px solid var(--pk-gold)" : "1px solid var(--pk-b1)",
                background:   amount === String(q) ? "rgba(176,124,31,0.08)" : "#FAFAFA",
                color:        amount === String(q) ? "var(--pk-gold)" : "var(--pk-t2)",
                fontSize:     "13px",
                fontWeight:   amount === String(q) ? 700 : 400,
                cursor:       "pointer",
                transition:   "all 0.15s ease",
              }}
            >
              {currencyObj.symbol}{q}
            </button>
          ))}
        </div>

        {/* Custom amount input */}
        <div style={{ position: "relative" }}>
          <span
            style={{
              position:  "absolute",
              left:      "13px",
              top:       "50%",
              transform: "translateY(-50%)",
              fontSize:  "15px",
              color:     "var(--pk-t2)",
              fontWeight: 600,
            }}
          >
            {currencyObj.symbol}
          </span>
          <input
            type="number"
            min="1"
            step="0.01"
            placeholder="Custom amount"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            style={{
              width:        "100%",
              padding:      "11px 13px 11px 32px",
              borderRadius: "10px",
              border:       "1px solid var(--pk-b1)",
              background:   "#FAFAFA",
              color:        "var(--pk-t1)",
              fontSize:     "15px",
              outline:      "none",
              fontFamily:   "inherit",
              boxSizing:    "border-box",
              fontWeight:   600,
            }}
            onFocus={(e) => (e.target.style.borderColor = "var(--pk-gold)")}
            onBlur={(e) => (e.target.style.borderColor = "var(--pk-b1)")}
          />
        </div>
      </div>

      {status === "error" && (
        <p style={{ fontSize: "13px", color: "#FF3B30", marginBottom: "12px" }}>{errorMsg}</p>
      )}

      {/* Give button */}
      <button
        type="submit"
        disabled={status === "loading" || !fundId || !amount || parseFloat(amount) <= 0}
        style={{
          width:        "100%",
          padding:      "14px 24px",
          borderRadius: "980px",
          border:       "none",
          background:   status === "loading" || !fundId || !amount || parseFloat(amount) <= 0
            ? "var(--pk-b1)"
            : "var(--pk-gold)",
          color:        status === "loading" || !fundId || !amount || parseFloat(amount) <= 0
            ? "var(--pk-t3)"
            : "#FFFFFF",
          fontSize:     "16px",
          fontWeight:   700,
          cursor:       status === "loading" || !fundId || !amount || parseFloat(amount) <= 0
            ? "not-allowed"
            : "pointer",
          transition:   "all 0.2s ease",
          letterSpacing: "0.01em",
        }}
      >
        {status === "loading"
          ? "Recording…"
          : `Give ${amount && parseFloat(amount) > 0 ? `${currencyObj.symbol}${parseFloat(amount).toLocaleString()}` : ""}`
        }
      </button>

      <p style={{ fontSize: "11px", color: "var(--pk-t3)", textAlign: "center", marginTop: "12px" }}>
        Donations are recorded and tracked by your church. Free to give — always.
      </p>
    </form>
  );
}
