"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import Image from "next/image";
import DarkModeToggle from "@/components/ui/DarkModeToggle";

const LINKS = [
  { href: "/pray",   label: "Pray"        },
  { href: "/prayer", label: "All Prayers" },
  { href: "/bible",  label: "Bible"       },
  { href: "/live",   label: "Live Sermon" },
  { href: "/docs",   label: "Docs"        },
  { href: "/about",  label: "About"       },
];

export default function SimpleNav() {
  const path = usePathname();
  const [open, setOpen] = useState(false);

  useEffect(() => { setOpen(false); }, [path]);
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
        borderBottom:   "1px solid var(--pk-nav-border)",
        background:     "var(--pk-nav-bg)",
        backdropFilter: "blur(24px) saturate(200%)",
        WebkitBackdropFilter: "blur(24px) saturate(200%)",
        transition:     "background 250ms ease, border-color 250ms ease",
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
          <Link href="/" style={{
            textDecoration: "none",
            display:        "flex",
            alignItems:     "center",
            gap:            "10px",
            marginRight:    "20px",
            flexShrink:     0,
          }}>
            <Image
              src="/prayerkey-icon.png"
              alt="PrayerKey"
              width={36}
              height={36}
              style={{ borderRadius: "8px", display: "block" }}
              priority
            />
            <span style={{
              fontSize:      "15px",
              fontWeight:    700,
              color:         "var(--pk-nav-text)",
              letterSpacing: "-0.03em",
              transition:    "color 250ms ease",
            }}>
              PrayerKey
            </span>
          </Link>

          {/* Desktop nav links */}
          <nav className="desktop-only" style={{ display: "flex", alignItems: "center", gap: "2px", flex: 1 }}>
            {LINKS.map((l) => {
              const active = path === l.href || (l.href !== "/" && path.startsWith(l.href));
              return (
                <Link key={l.href} href={l.href} style={{
                  textDecoration: "none",
                  padding:        "8px 14px",
                  minHeight:      "44px",
                  display:        "flex",
                  alignItems:     "center",
                  fontSize:       "13px",
                  fontWeight:     active ? 600 : 400,
                  color:          active ? "var(--pk-nav-active-clr)" : "var(--pk-nav-text-muted)",
                  borderRadius:   "8px",
                  background:     active ? "var(--pk-nav-active-bg)" : "transparent",
                  border:         active ? "1px solid var(--pk-accent-border)" : "1px solid transparent",
                  transition:     "all 180ms ease",
                  letterSpacing:  "-0.01em",
                }}>
                  {l.label}
                </Link>
              );
            })}
          </nav>

          {/* Desktop right side */}
          <div className="desktop-only" style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            {/* Dark mode toggle */}
            <DarkModeToggle />

            {/* Go Live CTA */}
            <Link href="/live" style={{ textDecoration: "none", flexShrink: 0 }}>
              <div style={{
                display:      "flex",
                alignItems:   "center",
                gap:          "7px",
                padding:      "10px 18px",
                minHeight:    "44px",
                background:   "#B22222",
                border:       "2px solid #B22222",
                boxShadow:    "3px 3px 0 0 rgba(178,34,34,0.3)",
                borderRadius: "8px",
                transition:   "transform 150ms ease, box-shadow 150ms ease",
                cursor:       "none",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLDivElement).style.transform  = "translate(-2px,-2px)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "5px 5px 0 0 rgba(178,34,34,0.4)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLDivElement).style.transform  = "translate(0,0)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "3px 3px 0 0 rgba(178,34,34,0.3)";
              }}
              >
                <span style={{
                  width: "6px", height: "6px", borderRadius: "50%",
                  background: "#fff", display: "inline-block",
                  animation: "liveDot 1.5s ease infinite",
                }} />
                <span style={{
                  fontSize: "12px", fontWeight: 700, color: "#fff",
                  letterSpacing: "0.04em", textTransform: "uppercase",
                }}>
                  Go Live
                </span>
              </div>
            </Link>
          </div>

          {/* Mobile: spacer + toggle + hamburger */}
          <div style={{ flex: 1 }} className="mobile-only" />
          <DarkModeToggle />
          <button
            className="mobile-only touch-target"
            aria-label={open ? "Close menu" : "Open menu"}
            onClick={() => setOpen(o => !o)}
            style={{
              background:     "transparent",
              border:         "1px solid var(--pk-border)",
              borderRadius:   "8px",
              width:          "44px",
              height:         "44px",
              display:        "flex",
              flexDirection:  "column",
              alignItems:     "center",
              justifyContent: "center",
              gap:            "5px",
              cursor:         "pointer",
              padding:        "0",
              flexShrink:     0,
              transition:     "border-color 200ms ease",
            }}
          >
            {[
              { t: open ? "translateY(7px) rotate(45deg)"  : "none",  o: 1 },
              { t: "none",                                              o: open ? 0 : 1 },
              { t: open ? "translateY(-7px) rotate(-45deg)" : "none", o: 1 },
            ].map((s, i) => (
              <span key={i} style={{
                display:      "block",
                width:        "18px",
                height:       "2px",
                background:   "var(--pk-text)",
                borderRadius: "2px",
                transition:   "transform 250ms ease, opacity 250ms ease, background 250ms ease",
                transform:    s.t,
                opacity:      s.o,
              }} />
            ))}
          </button>

        </div>
      </header>

      {/* Mobile full-screen overlay menu */}
      <div style={{
        position:       "fixed",
        inset:          0,
        zIndex:         999,
        background:     "var(--pk-bg)",
        backdropFilter: "blur(24px)",
        display:        "flex",
        flexDirection:  "column",
        justifyContent: "center",
        alignItems:     "center",
        gap:            "4px",
        opacity:        open ? 1 : 0,
        pointerEvents:  open ? "auto" : "none",
        transition:     "opacity 280ms ease",
        paddingTop:     "64px",
      }}>
        {LINKS.map((l, i) => {
          const active = path === l.href || (l.href !== "/" && path.startsWith(l.href));
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
                color:          active ? "var(--pk-accent)" : "var(--pk-text-2)",
                borderBottom:   "1px solid var(--pk-border)",
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
            background:     "#B22222",
            border:         "2px solid #B22222",
            boxShadow:      "4px 4px 0 0 rgba(178,34,34,0.35)",
            borderRadius:   "10px",
            transform:      open ? "translateY(0)" : "translateY(20px)",
            opacity:        open ? 1 : 0,
            transition:     `transform 300ms cubic-bezier(0.22,1,0.36,1) ${LINKS.length * 60}ms, opacity 300ms ease ${LINKS.length * 60}ms`,
          }}
        >
          <span style={{
            width: "8px", height: "8px", borderRadius: "50%",
            background: "#fff", display: "inline-block",
            animation: "liveDot 1.5s ease infinite",
          }} />
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
      `}</style>
    </>
  );
}
