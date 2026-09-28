"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import { X } from "lucide-react";
import { playUrl } from "@/lib/app-store";

// A gentle invitation to install the app, shown once someone has spent a moment on the site.
// Honest by design: no countdowns or fake scarcity. "Not now" is respected for two weeks,
// iPhone visitors never see it (the app is Android only), and it stays off the download page.

const KEY = "pk-app-invite";
const SNOOZE_DAYS = 14;
const AFTER_INSTALL_DAYS = 30;
const HIDDEN_ON = ["/download", "/live/projector"];

type Kind = "android" | "desktop";

function moment() {
  const h = new Date().getHours();
  if (h >= 5 && h < 12) return { title: "Start your morning with God.", line: "Morning prayer, today’s verse and a devotional, waiting when you wake." };
  if (h >= 12 && h < 17) return { title: "Take a moment with God today.", line: "Tell PrayerKey what’s on your heart and pray about it in minutes." };
  if (h >= 17 && h < 22) return { title: "End your day in prayer.", line: "Evening prayer to give thanks and lay down what you’re carrying." };
  return { title: "Can’t sleep? Pray before you rest.", line: "Night and midnight prayer, and the whole Bible offline." };
}

function snoozed() {
  try {
    const until = Number(localStorage.getItem(KEY) || 0);
    return until > Date.now();
  } catch { return false; }
}
function snooze(days: number) {
  try { localStorage.setItem(KEY, String(Date.now() + days * 86_400_000)); } catch { /* private mode */ }
}

export default function AppInvite() {
  const path = usePathname() || "/";
  const reduce = useReducedMotion();
  const [kind, setKind] = useState<Kind | null>(null);
  const [open, setOpen] = useState(false);
  const [copy, setCopy] = useState(moment);

  useEffect(() => {
    const ua = navigator.userAgent;
    const ios = /iPhone|iPad|iPod/.test(ua) || (/Macintosh/.test(ua) && navigator.maxTouchPoints > 1);
    const standalone = window.matchMedia?.("(display-mode: standalone)").matches;
    if (ios || standalone) return;
    setKind(/Android/i.test(ua) ? "android" : window.matchMedia("(pointer: fine)").matches ? "desktop" : null);
  }, []);

  // Wait for interest: a few seconds on the site, or a third of the way down a page.
  useEffect(() => {
    if (!kind || open || HIDDEN_ON.some((p) => path.startsWith(p)) || snoozed()) return;
    let done = false;
    const show = () => { if (done || snoozed()) return; done = true; setCopy(moment()); setOpen(true); };
    const timer = window.setTimeout(show, 8000);
    const onScroll = () => {
      const max = document.documentElement.scrollHeight - window.innerHeight;
      if (max > 0 && window.scrollY / max > 0.33) show();
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => { window.clearTimeout(timer); window.removeEventListener("scroll", onScroll); };
  }, [kind, path, open]);

  useEffect(() => { if (HIDDEN_ON.some((p) => path.startsWith(p))) setOpen(false); }, [path]);
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") { snooze(SNOOZE_DAYS); setOpen(false); } };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open]);

  const dismiss = () => { snooze(SNOOZE_DAYS); setOpen(false); };
  const installed = () => { snooze(AFTER_INSTALL_DAYS); window.setTimeout(() => setOpen(false), 400); };

  return (
    <AnimatePresence>
      {open && kind && (
        <motion.aside
          key="invite"
          className={`pk-invite pk-invite--${kind}`}
          role="dialog"
          aria-label="Get the PrayerKey app"
          initial={reduce ? { opacity: 0 } : { opacity: 0, y: 60, scale: 0.94 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={reduce ? { opacity: 0 } : { opacity: 0, y: 40, scale: 0.96 }}
          transition={{ type: "spring", stiffness: 260, damping: 24 }}
        >
          <button type="button" className="pk-invite__close" onClick={dismiss} aria-label="Not now"><X size={16} /></button>

          <div className="pk-invite__head">
            <span className="pk-invite__icon" aria-hidden>
              <img src="/icon-192.png" alt="" width={56} height={56} />
              {!reduce && Array.from({ length: 8 }, (_, i) => (
                <motion.i key={i} initial={{ opacity: 0, scale: 0.2, x: 0, y: 0 }} animate={{ opacity: [0, 1, 0], scale: [0.2, 1, 0.4], x: Math.cos((i / 8) * Math.PI * 2) * 38, y: Math.sin((i / 8) * Math.PI * 2) * 38 }} transition={{ duration: 1.1, delay: 0.35 + i * 0.02, ease: "easeOut" }} />
              ))}
            </span>
            <span className="pk-invite__meta">
              <strong>PrayerKey</strong>
              <span>Prayer &amp; Bible · Free on Google Play</span>
            </span>
          </div>

          <p className="pk-invite__title pk-display">{copy.title}</p>
          <p className="pk-invite__line">{copy.line}</p>
          <div className="pk-invite__chips" aria-hidden>
            <span>Free</span><span>Works offline</span><span>No account</span>
          </div>

          {kind === "android" ? (
            <div className="pk-invite__actions">
              <a href={playUrl("banner")} target="_blank" rel="noopener" onClick={installed} className="pk-capsule pk-capsule--violet pk-invite__get">Get the app</a>
              <button type="button" onClick={dismiss} className="pk-invite__later">Not now</button>
            </div>
          ) : (
            <div className="pk-invite__desk">
              <img src="/app/qr-play.svg" alt="QR code for PrayerKey on Google Play" width={92} height={92} />
              <span>
                Scan with your Android phone to install.
                <Link href="/download" onClick={() => setOpen(false)}>See the app</Link>
              </span>
            </div>
          )}
        </motion.aside>
      )}
    </AnimatePresence>
  );
}
