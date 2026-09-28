"use client";
import { useRef } from "react";
import { motion, useReducedMotion, useScroll, useTransform } from "framer-motion";
import { Check } from "lucide-react";
import PlayBadge from "./PlayBadge";
import Phone from "./Phone";

const TRUST = ["Free", "No account", "Works offline", "No data collected"];

export default function DownloadHero() {
  const ref = useRef<HTMLElement>(null);
  const reduce = useReducedMotion();
  const { scrollYProgress } = useScroll({ target: ref, offset: ["start start", "end start"] });
  const leftX = useTransform(scrollYProgress, [0, 1], [0, -60]);
  const rightX = useTransform(scrollYProgress, [0, 1], [0, 60]);
  const lift = useTransform(scrollYProgress, [0, 1], [0, -80]);
  const spring = (delay: number) => ({ type: "spring" as const, stiffness: 70, damping: 16, delay });

  return (
    <section ref={ref} className="dl-hero pk-bleed" data-nav="dark" aria-labelledby="dl-title">
      <div className="dl-hero__sky" aria-hidden />
      <div className="dl-hero__glow" aria-hidden />
      <div className="dl-wrap dl-hero__grid">
        <div className="dl-hero__copy">
          <motion.span className="pk-chip pk-chip--glass" initial={reduce ? false : { opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.1 }}>
            Free on Google Play
          </motion.span>
          <motion.h1 id="dl-title" className="pk-display dl-hero__title" initial={reduce ? false : { opacity: 0, y: 24, filter: "blur(10px)" }} animate={{ opacity: 1, y: 0, filter: "blur(0px)" }} transition={{ duration: 1, delay: 0.2, ease: [0.22, 1, 0.36, 1] }}>
            Download PrayerKey. <span className="pk-gold-text">Prayer &amp; Bible</span> in your pocket.
          </motion.h1>
          <motion.p className="dl-hero__lead" initial={reduce ? false : { opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8, delay: 0.45 }}>
            PrayerKey helps you pray, read Scripture, and make time with God part of your everyday life.
          </motion.p>
          <motion.div className="dl-hero__get" initial={reduce ? false : { opacity: 0, y: 16, scale: 0.97 }} animate={{ opacity: 1, y: 0, scale: 1 }} transition={spring(0.6)}>
            <PlayBadge size="lg" source="download" />
            <span className="dl-qr" aria-hidden>
              <img src="/app/qr-play.svg" alt="" width={84} height={84} />
              <span>Scan with your phone</span>
            </span>
          </motion.div>
          <motion.ul className="dl-trust" initial={reduce ? false : { opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.8, delay: 0.85 }}>
            {TRUST.map((t) => <li key={t}><Check size={15} /> {t}</li>)}
          </motion.ul>
        </div>

        <div className="dl-hero__phones" aria-hidden>
          <motion.div className="dl-hero__phone dl-hero__phone--left" style={reduce ? undefined : { x: leftX }} initial={reduce ? false : { opacity: 0, x: 60, rotate: 0, y: 40 }} animate={{ opacity: 1, x: 0, rotate: -8, y: 0 }} transition={spring(0.5)}>
            <Phone src="/app/prayer-points.webp" alt="" />
          </motion.div>
          <motion.div className="dl-hero__phone dl-hero__phone--right" style={reduce ? undefined : { x: rightX }} initial={reduce ? false : { opacity: 0, x: -60, rotate: 0, y: 40 }} animate={{ opacity: 1, x: 0, rotate: 8, y: 0 }} transition={spring(0.6)}>
            <Phone src="/app/bible.webp" alt="" />
          </motion.div>
          <motion.div className="dl-hero__phone dl-hero__phone--front" style={reduce ? undefined : { y: lift }} initial={reduce ? false : { opacity: 0, y: 80, scale: 0.94 }} animate={{ opacity: 1, y: 0, scale: 1 }} transition={spring(0.35)}>
            <Phone src="/app/home.webp" alt="" />
          </motion.div>
        </div>
      </div>
    </section>
  );
}
