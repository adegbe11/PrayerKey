"use client";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { X } from "lucide-react";
import { useReducedMotion } from "@/lib/use-reduced-motion";
import { playUrl } from "@/lib/app-store";

// The app banner, the way the best sites do it: a slim bar across the top on Android phones -
// the icon, the name, where it comes from, and Install. Always there, never in the way.
// Honest: no invented stars or download counts. Closing it is respected for a week, and it
// steps aside on the download page, the projector, inside the installed app, and on iPhone,
// where there is no app to install yet.

const KEY = "pk-app-banner";
const SNOOZE_DAYS = 7;
const AFTER_INSTALL_DAYS = 30;
const HIDDEN_ON = ["/download", "/live/projector"];
export const BANNER_HEIGHT = 64;

function snoozed() {
  try { return Number(localStorage.getItem(KEY) || 0) > Date.now(); } catch { return false; }
}
function snooze(days: number) {
  try { localStorage.setItem(KEY, String(Date.now() + days * 86_400_000)); } catch { /* private mode */ }
}

export default function AppBanner() {
  const path = usePathname() || "/";
  const reduce = useReducedMotion();
  const [android, setAndroid] = useState(false);
  const [closed, setClosed] = useState(true);

  useEffect(() => {
    const ua = navigator.userAgent;
    const standalone = window.matchMedia?.("(display-mode: standalone)").matches;
    if (!/Android/i.test(ua) || standalone) return;
    setAndroid(true);
    setClosed(snoozed());
  }, []);

  const show = android && !closed && !HIDDEN_ON.some((p) => path.startsWith(p));

  // make room: the page and the fixed nav move down by the banner's height while it shows
  useEffect(() => {
    const root = document.documentElement;
    if (show) root.style.setProperty("--pk-banner", `${BANNER_HEIGHT}px`);
    else root.style.removeProperty("--pk-banner");
    return () => { root.style.removeProperty("--pk-banner"); };
  }, [show]);

  const dismiss = () => { snooze(SNOOZE_DAYS); setClosed(true); };
  const installed = () => { snooze(AFTER_INSTALL_DAYS); window.setTimeout(() => setClosed(true), 600); };

  return (
    <AnimatePresence>
      {show && (
        <motion.div
          key="banner"
          className="pk-banner"
          role="region"
          aria-label="Get the PrayerKey app"
          initial={reduce ? { opacity: 0 } : { y: -BANNER_HEIGHT, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          exit={reduce ? { opacity: 0 } : { y: -BANNER_HEIGHT, opacity: 0 }}
          transition={{ type: "spring", stiffness: 320, damping: 30 }}
        >
          <button type="button" className="pk-banner__close" onClick={dismiss} aria-label="Close"><X size={16} /></button>
          <img className="pk-banner__icon" src="/icon-192.png" alt="" width={40} height={40} />
          <span className="pk-banner__meta">
            <strong>PrayerKey</strong>
            <span>Free on Google Play</span>
          </span>
          <a className="pk-banner__get" href={playUrl("banner")} target="_blank" rel="noopener" onClick={installed}>
            Install
          </a>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
