import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title:       "Page Not Found — PrayerKey",
  description: "We couldn't find that page. Try the AI prayer generator, Bible search, live sermon dashboard, or the full documentation.",
  robots:      { index: false, follow: true },
};

export default function NotFound() {
  return (
    <div style={{
      minHeight:      "60vh",
      display:        "flex",
      flexDirection:  "column",
      alignItems:     "center",
      justifyContent: "center",
      textAlign:      "center",
      padding:        "60px 20px",
    }}>
      <div style={{
        display:      "inline-flex",
        alignItems:   "center",
        padding:      "4px 12px",
        border:       "1.5px solid var(--pk-accent-border)",
        borderRadius: "4px",
        marginBottom: "24px",
        background:   "var(--pk-accent-dim)",
        boxShadow:    "3px 3px 0 0 var(--pk-accent-border)",
      }}>
        <span style={{
          fontSize:       "10px",
          fontWeight:     700,
          color:          "var(--pk-accent)",
          letterSpacing:  "0.12em",
          textTransform:  "uppercase",
        }}>
          404 · Not Found
        </span>
      </div>

      <h1 style={{
        fontSize:      "clamp(36px, 5vw, 56px)",
        fontWeight:    800,
        color:         "var(--pk-text)",
        margin:        "0 0 16px",
        letterSpacing: "-0.03em",
        lineHeight:    1.1,
      }}>
        We couldn't find that page.
      </h1>

      <p style={{
        fontSize:   "17px",
        color:      "var(--pk-text-2)",
        lineHeight: 1.7,
        margin:     "0 0 32px",
        maxWidth:   "520px",
      }}>
        The link may be broken, or the page may have moved. Here are the things
        most people are looking for:
      </p>

      <div style={{
        display:        "flex",
        flexWrap:       "wrap",
        gap:            "12px",
        justifyContent: "center",
      }}>
        {[
          { href: "/",      label: "Home" },
          { href: "/pray",  label: "AI Prayer Generator" },
          { href: "/live",  label: "Live Sermon" },
          { href: "/bible", label: "Bible Search" },
          { href: "/prayer",label: "Prayer Library" },
          { href: "/docs",  label: "Documentation" },
        ].map((l) => (
          <Link key={l.href} href={l.href} style={{
            display:        "inline-flex",
            alignItems:     "center",
            padding:        "10px 18px",
            fontSize:       "14px",
            fontWeight:     600,
            color:          "var(--pk-text)",
            border:         "1.5px solid var(--pk-border)",
            borderRadius:   "8px",
            background:     "var(--pk-surface)",
            boxShadow:      "3px 3px 0 0 var(--pk-border)",
            textDecoration: "none",
            letterSpacing:  "-0.005em",
          }}>
            {l.label}
          </Link>
        ))}
      </div>
    </div>
  );
}
