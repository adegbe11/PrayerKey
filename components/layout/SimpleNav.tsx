"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import Image from "next/image";

const LINKS = [
  { href: "/pray",  label: "Pray"        },
  { href: "/live",  label: "Live Sermon" },
  { href: "/bible", label: "Bible"       },
  { href: "/about", label: "About"       },
];

export default function SimpleNav() {
  const path = usePathname();
  const [open, setOpen] = useState(false);

  // Close menu on route change
  useEffect(() => { setOpen(false); }, [path]);
  // Prevent body scroll when menu open
  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  return (
    <>
      <header style={{
        position:       "sticky",
        top:            0,
        zIndex:         1000,
        borderBottom:   "1px solid rgba(255,255,255,0.07)",
        background:     "rgba(6,6,8,0.85)",
        backdropFilter: "blur(24px) saturate(200%)",
        WebkitBackdropFilter: "blur(24px) saturate(200%)",
      }}>
        <div style={{
          maxWidth:   "1440px",
          margin:     "0 auto",
          padding:    "0 clamp(20px, 4vw, 48px)",
          height:     "64px",
          display:    "flex",
          alignItems: "center",
          gap:        "8px",
        }}>

          {/* Logo */}
          <Link href="/" style={{ textDecoration: "none", display: "flex", alignItems: "center", gap: "10px", marginRight: "28px", flexShrink: 0 }}>
            <Image
              src="/og-image.png"
              alt="PrayerKey"
              width={36}
              height={36}
              style={{ borderRadius: "6px", display: "block" }}
              priority
            />
            <span style={{
              fontSize:      "15px",
              fontWeight:    700,
              color:         "#fff",
              letterSpacing: "-0.03em",
            }}>
              PrayerKey
            </span>
          </Link>

          {/* Desktop nav links */}
          <nav className="desktop-only" style={{ display: "flex", alignItems: "center", gap: "2px", flex: 1 }}>
            {LINKS.map((l) => {
              const active = path.startsWith(l.href);
              return (
                <Link key={l.href} href={l.href} style={{
                  textDecoration: "none",
                  padding:        "8px 16px",
                  minHeight:      "44px",
                  display:        "flex",
                  alignItems:     "center",
                  fontSize:       "13px",
                  fontWeight:     active ? 600 : 400,
                  color:          active ? "#C49A2A" : "rgba(255,255,255,0.42)",
                  borderRadius:   "6px",
                  background:     active ? "rgba(196,154,42,0.08)" : "transparent",
                  border:         active ? "1px solid rgba(196,154,42,0.2)" : "1px solid transparent",
                  transition:     "all 180ms ease",
                  letterSpacing:  "-0.01em",
                }}>
                  {l.label}
                </Link>
              );
            })}
          </nav>

          {/* Desktop Go Live CTA */}
          <Link href="/live" className="desktop-only" style={{ textDecoration: "none", flexShrink: 0 }}>
            <div style={{
              display:      "flex",
              alignItems:   "center",
              gap:          "7px",
              padding:      "10px 20px",
              minHeight:    "44px",
              background:   "#FF3B30",
              border:       "2px solid #FF3B30",
              boxShadow:    "3px 3px 0 0 rgba(255,59,48,0.4)",
              borderRadius: "6px",
              transition:   "transform 150ms ease, box-shadow 150ms ease",
              cursor:       "none",
            }}
            onMouseEnter={e => {
              (e.currentTarget as HTMLDivElement).style.transform = "translate(-2px,-2px)";
              (e.currentTarget as HTMLDivElement).style.boxShadow = "5px 5px 0 0 rgba(255,59,48,0.4)";
            }}
            onMouseLeave={e => {
              (e.currentTarget as HTMLDivElement).style.transform = "translate(0,0)";
              (e.currentTarget as HTMLDivElement).style.boxShadow = "3px 3px 0 0 rgba(255,59,48,0.4)";
            }}
            >
              <span style={{ width: "6px", height: "6px", borderRadius: "50%", background: "#fff", display: "inline-block", animation: "liveDot 1.5s ease infinite" }} />
              <span style={{ fontSize: "12px", fontWeight: 700, color: "#fff", letterSpacing: "0.04em", textTransform: "uppercase" }}>
                Go Live
              </span>
            </div>
          </Link>

          {/* Spacer on mobile */}
          <div style={{ flex: 1 }} className="mobile-only" />

          {/* Hamburger button (mobile only) */}
          <button
            className="mobile-only touch-target"
            aria-label={open ? "Close menu" : "Open menu"}
            onClick={() => setOpen(o => !o)}
            style={{
              background:  "transparent",
              border:      "1px solid rgba(255,255,255,0.12)",
              borderRadius: "6px",
              width:       "44px",
              height:      "44px",
              display:     "flex",
              flexDirection: "column",
              alignItems:  "center",
              justifyContent: "center",
              gap:         "5px",
              cursor:      "pointer",
              padding:     "0",
              flexShrink:  0,
            }}
          >
            <span style={{
              display:    "block",
              width:      "18px",
              height:     "2px",
              background: "#fff",
              borderRadius: "2px",
              transition: "transform 250ms ease, opacity 250ms ease",
              transform:  open ? "translateY(7px) rotate(45deg)" : "none",
            }} />
            <span style={{
              display:    "block",
              width:      "18px",
              height:     "2px",
              background: "#fff",
              borderRadius: "2px",
              transition: "opacity 250ms ease",
              opacity:    open ? 0 : 1,
            }} />
            <span style={{
              display:    "block",
              width:      "18px",
              height:     "2px",
              background: "#fff",
              borderRadius: "2px",
              transition: "transform 250ms ease, opacity 250ms ease",
              transform:  open ? "translateY(-7px) rotate(-45deg)" : "none",
            }} />
          </button>

        </div>
      </header>

      {/* Mobile full-screen overlay menu */}
      <div style={{
        position:   "fixed",
        inset:      0,
        zIndex:     999,
        background: "rgba(6,6,8,0.97)",
        backdropFilter: "blur(24px)",
        display:    "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        gap:        "8px",
        opacity:    open ? 1 : 0,
        pointerEvents: open ? "auto" : "none",
        transition: "opacity 280ms ease",
        paddingTop: "64px",
      }}>
        {LINKS.map((l, i) => {
          const active = path.startsWith(l.href);
          return (
            <Link
              key={l.href}
              href={l.href}
              onClick={() => setOpen(false)}
              style={{
                textDecoration: "none",
                padding:        "16px 40px",
                minHeight:      "60px",
                width:          "100%",
                maxWidth:       "320px",
                display:        "flex",
                alignItems:     "center",
                justifyContent: "center",
                fontSize:       "clamp(22px, 5vw, 28px)",
                fontWeight:     700,
                letterSpacing:  "-0.02em",
                color:          active ? "#C49A2A" : "rgba(255,255,255,0.72)",
                borderBottom:   "1px solid rgba(255,255,255,0.06)",
                transform:      open ? "translateY(0)" : "translateY(20px)",
                opacity:        open ? 1 : 0,
                transition:     `transform 300ms cubic-bezier(0.22,1,0.36,1) ${i * 60}ms, opacity 300ms ease ${i * 60}ms`,
              }}
            >
              {l.label}
            </Link>
          );
        })}

        <Link
          href="/live"
          onClick={() => setOpen(false)}
          style={{
            textDecoration: "none",
            marginTop:      "24px",
            display:        "flex",
            alignItems:     "center",
            gap:            "10px",
            padding:        "16px 40px",
            background:     "#FF3B30",
            border:         "2px solid #FF3B30",
            boxShadow:      "4px 4px 0 0 rgba(255,59,48,0.4)",
            borderRadius:   "8px",
            transform:      open ? "translateY(0)" : "translateY(20px)",
            opacity:        open ? 1 : 0,
            transition:     `transform 300ms cubic-bezier(0.22,1,0.36,1) ${LINKS.length * 60}ms, opacity 300ms ease ${LINKS.length * 60}ms`,
          }}
        >
          <span style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#fff", display: "inline-block", animation: "liveDot 1.5s ease infinite" }} />
          <span style={{ fontSize: "16px", fontWeight: 700, color: "#fff", letterSpacing: "0.06em", textTransform: "uppercase" }}>
            Go Live Now
          </span>
        </Link>
      </div>

      <style>{`
        @keyframes liveDot {
          0%,100% { opacity:1; transform:scale(1); }
          50%     { opacity:0.4; transform:scale(0.7); }
        }
        @media (max-width: 768px) {
          .desktop-only { display: none !important; }
          .mobile-only  { display: flex !important; }
        }
        @media (min-width: 769px) {
          .mobile-only  { display: none !important; }
          .desktop-only { display: flex !important; }
        }
      `}</style>
    </>
  );
}
