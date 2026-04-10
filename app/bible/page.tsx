"use client";
import { useState } from "react";

type Result   = { ref: string; text: string; match: string };
type CrossRef = { ref: string; text: string; reason: string };

export default function BiblePage() {
  const [query,     setQuery]     = useState("");
  const [results,   setResults]   = useState<Result[]>([]);
  const [loading,   setLoading]   = useState(false);
  const [selected,  setSelected]  = useState<Result | null>(null);
  const [crossRefs, setCrossRefs] = useState<CrossRef[]>([]);
  const [crLoading, setCrLoading] = useState(false);

  async function search() {
    if (!query.trim()) return;
    setLoading(true);
    setResults([]);
    setSelected(null);
    setCrossRefs([]);
    try {
      const res  = await fetch(`/api/bible/search?q=${encodeURIComponent(query)}&translation=NIV`);
      const data = await res.json();
      setResults(data.results ?? []);
    } finally {
      setLoading(false);
    }
  }

  async function loadCrossRefs(v: Result) {
    setSelected(v);
    setCrossRefs([]);
    setCrLoading(true);
    try {
      const res  = await fetch(`/api/bible/cross-refs?ref=${encodeURIComponent(v.ref)}&translation=NIV`);
      const data = await res.json();
      setCrossRefs(data.refs ?? []);
    } finally {
      setCrLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: "720px", margin: "0 auto", padding: "0 0 80px" }}>

      {/* ── Page header ── */}
      <div className="animate-fadeUp" style={{ textAlign: "center", marginBottom: "48px" }}>
        <div style={{
          display:      "inline-flex",
          alignItems:   "center",
          gap:          "8px",
          padding:      "4px 12px",
          border:       "1.5px solid rgba(176,124,31,0.35)",
          borderRadius: "4px",
          marginBottom: "20px",
          background:   "rgba(176,124,31,0.06)",
          boxShadow:    "3px 3px 0 0 rgba(176,124,31,0.15)",
        }}>
          <span style={{ fontSize: "10px", fontWeight: 700, color: "#B07C1F", letterSpacing: "0.12em", textTransform: "uppercase" }}>
            Bible Search
          </span>
        </div>

        <h1 style={{
          fontSize:      "clamp(32px, 6vw, 56px)",
          fontWeight:    800,
          color:         "#fff",
          margin:        "0 0 14px",
          letterSpacing: "-0.03em",
          lineHeight:    1.05,
        }}>
          Find any verse,<br />any topic.
        </h1>
        <p style={{
          fontSize:    "clamp(15px, 1.4vw, 17px)",
          color:       "rgba(255,255,255,0.42)",
          margin:      0,
          lineHeight:  1.65,
          maxWidth:    "420px",
          marginInline:"auto",
        }}>
          Search all 66 books of the Bible by reference, keyword, or topic — then see related scriptures instantly.
        </p>
      </div>

      {/* ── Search bar ── */}
      <div className="animate-fadeUp delay-100" style={{
        display:      "flex",
        gap:          "8px",
        marginBottom: "28px",
        background:   "rgba(255,255,255,0.025)",
        border:       "1.5px solid rgba(255,255,255,0.09)",
        borderRadius: "8px",
        padding:      "8px",
        boxShadow:    "4px 4px 0 0 rgba(255,255,255,0.03)",
      }}>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && search()}
          placeholder='e.g. "John 3:16" or "do not fear" or "peace"'
          style={{
            flex:       1,
            padding:    "12px 16px",
            minHeight:  "48px",
            background: "transparent",
            border:     "none",
            color:      "#fff",
            fontSize:   "clamp(14px, 1.4vw, 16px)",
            outline:    "none",
            fontFamily: "inherit",
          }}
        />
        <button
          onClick={search}
          disabled={loading || !query.trim()}
          style={{
            padding:      "12px 24px",
            minHeight:    "48px",
            borderRadius: "4px",
            border:       "1.5px solid",
            borderColor:  loading || !query.trim() ? "rgba(176,124,31,0.25)" : "#B07C1F",
            background:   loading || !query.trim() ? "rgba(176,124,31,0.1)" : "#B07C1F",
            color:        loading || !query.trim() ? "rgba(255,255,255,0.3)" : "#fff",
            fontSize:     "14px",
            fontWeight:   800,
            cursor:       loading || !query.trim() ? "not-allowed" : "pointer",
            letterSpacing:"-0.01em",
            boxShadow:    loading || !query.trim() ? "none" : "3px 3px 0 0 rgba(176,124,31,0.3)",
            transition:   "all 150ms ease",
            flexShrink:   0,
          }}
          onMouseEnter={e => {
            if (loading || !query.trim()) return;
            (e.currentTarget as HTMLButtonElement).style.transform = "translate(-1px,-1px)";
            (e.currentTarget as HTMLButtonElement).style.boxShadow = "5px 5px 0 0 rgba(176,124,31,0.3)";
          }}
          onMouseLeave={e => {
            (e.currentTarget as HTMLButtonElement).style.transform = "translate(0,0)";
            (e.currentTarget as HTMLButtonElement).style.boxShadow = loading || !query.trim() ? "none" : "3px 3px 0 0 rgba(176,124,31,0.3)";
          }}
        >
          {loading ? (
            <span style={{ width: "14px", height: "14px", border: "2px solid rgba(255,255,255,0.3)", borderTopColor: "#fff", borderRadius: "50%", display: "inline-block", animation: "spin 700ms linear infinite" }} />
          ) : "Search →"}
        </button>
      </div>

      {/* ── Results ── */}
      {results.length > 0 && (
        <div className="animate-fadeUp" style={{ display: "flex", flexDirection: "column", gap: "8px", marginBottom: "24px" }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: "rgba(255,255,255,0.25)", letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 8px" }}>
            {results.length} Result{results.length !== 1 ? "s" : ""}
          </p>
          {results.map((r) => {
            const isSelected = selected?.ref === r.ref;
            return (
              <button
                key={r.ref}
                onClick={() => loadCrossRefs(r)}
                style={{
                  textAlign:    "left",
                  padding:      "18px 20px",
                  minHeight:    "44px",
                  borderRadius: "8px",
                  border:       `1.5px solid ${isSelected ? "rgba(176,124,31,0.4)" : "rgba(255,255,255,0.07)"}`,
                  background:   isSelected ? "rgba(176,124,31,0.1)" : "rgba(255,255,255,0.025)",
                  cursor:       "pointer",
                  transition:   "all 150ms ease",
                  boxShadow:    isSelected ? "3px 3px 0 0 rgba(176,124,31,0.15)" : "none",
                }}
                onMouseEnter={e => {
                  if (isSelected) return;
                  (e.currentTarget as HTMLButtonElement).style.background = "rgba(255,255,255,0.04)";
                  (e.currentTarget as HTMLButtonElement).style.borderColor = "rgba(255,255,255,0.12)";
                }}
                onMouseLeave={e => {
                  if (isSelected) return;
                  (e.currentTarget as HTMLButtonElement).style.background = "rgba(255,255,255,0.025)";
                  (e.currentTarget as HTMLButtonElement).style.borderColor = "rgba(255,255,255,0.07)";
                }}
              >
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "8px" }}>
                  <span style={{ fontSize: "14px", fontWeight: 700, color: "#B07C1F", letterSpacing: "-0.01em" }}>{r.ref}</span>
                  <span style={{ fontSize: "10px", fontWeight: 700, color: isSelected ? "#B07C1F" : "rgba(255,255,255,0.22)", letterSpacing: "0.08em", textTransform: "uppercase", background: isSelected ? "rgba(176,124,31,0.12)" : "rgba(255,255,255,0.04)", padding: "2px 8px", borderRadius: "3px" }}>
                    {r.match === "direct" ? "Exact" : "Related"}
                  </span>
                </div>
                <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.75)", lineHeight: 1.65, margin: 0 }}>{r.text}</p>
              </button>
            );
          })}
        </div>
      )}

      {/* Empty state */}
      {results.length === 0 && !loading && query && (
        <div style={{ textAlign: "center", padding: "48px 20px", color: "rgba(255,255,255,0.25)" }}>
          <div style={{ fontSize: "32px", marginBottom: "12px" }}>🔍</div>
          <p style={{ fontSize: "15px", margin: 0 }}>No results. Try different keywords or a direct reference like &ldquo;Romans 8:28&rdquo;.</p>
        </div>
      )}

      {/* Default hint (no search yet) */}
      {!query && results.length === 0 && (
        <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", justifyContent: "center" }}>
          {["John 3:16", "faith", "do not fear", "Psalm 23", "grace"].map((hint) => (
            <button
              key={hint}
              onClick={() => { setQuery(hint); }}
              style={{
                padding:      "8px 16px",
                minHeight:    "36px",
                borderRadius: "4px",
                border:       "1px solid rgba(176,124,31,0.2)",
                background:   "rgba(176,124,31,0.05)",
                color:        "#B07C1F",
                fontSize:     "13px",
                fontWeight:   500,
                cursor:       "pointer",
                transition:   "all 150ms ease",
              }}
            >
              {hint}
            </button>
          ))}
        </div>
      )}

      {/* ── Cross references ── */}
      {selected && (
        <div className="animate-fadeUp" style={{
          marginTop:    "8px",
          background:   "rgba(176,124,31,0.05)",
          border:       "1.5px solid rgba(176,124,31,0.18)",
          borderRadius: "12px",
          overflow:     "hidden",
          boxShadow:    "4px 4px 0 0 rgba(176,124,31,0.1)",
        }}>
          <div style={{ padding: "16px 20px", borderBottom: "1px solid rgba(176,124,31,0.12)", background: "rgba(176,124,31,0.07)", display: "flex", alignItems: "center", gap: "10px" }}>
            <span style={{ fontSize: "10px", fontWeight: 700, color: "#B07C1F", letterSpacing: "0.1em", textTransform: "uppercase" }}>
              Related Verses
            </span>
            <span style={{ fontSize: "13px", fontWeight: 600, color: "rgba(255,255,255,0.5)" }}>
              for {selected.ref}
            </span>
          </div>

          <div style={{ padding: "16px 20px" }}>
            {crLoading && (
              <div style={{ display: "flex", alignItems: "center", gap: "10px", padding: "8px 0" }}>
                <span style={{ width: "14px", height: "14px", border: "2px solid rgba(176,124,31,0.3)", borderTopColor: "#B07C1F", borderRadius: "50%", display: "inline-block", animation: "spin 700ms linear infinite" }} />
                <span style={{ fontSize: "13px", color: "rgba(255,255,255,0.3)" }}>Loading related scriptures...</span>
              </div>
            )}
            {crossRefs.length > 0 && (
              <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                {crossRefs.map((c) => (
                  <div key={c.ref} style={{
                    padding:      "14px 16px",
                    background:   "rgba(255,255,255,0.03)",
                    border:       "1px solid rgba(255,255,255,0.07)",
                    borderRadius: "8px",
                  }}>
                    <div style={{ fontSize: "13px", fontWeight: 700, color: "#B07C1F", marginBottom: "6px" }}>{c.ref}</div>
                    <div style={{ fontSize: "14px", color: "rgba(255,255,255,0.75)", lineHeight: 1.65, marginBottom: "8px" }}>{c.text}</div>
                    <div style={{ fontSize: "12px", color: "rgba(255,255,255,0.28)", fontStyle: "italic", borderTop: "1px solid rgba(255,255,255,0.05)", paddingTop: "6px" }}>{c.reason}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      <style>{`
        input:focus { outline: none; }
        input::placeholder { color: rgba(255,255,255,0.2); }
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
