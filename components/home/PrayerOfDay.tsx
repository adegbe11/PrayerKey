"use client";
import Link from "next/link";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Download, Loader2, Sparkles } from "lucide-react";
import { FORMATS, type DailyPrayer, type FormatId } from "@/lib/home/daily";
import Reveal from "./Reveal";

// Today's prayer, shown as the share image it becomes, in whichever shape the person needs.
export default function PrayerOfDay({ prayer }: { prayer: DailyPrayer }) {
  const [format, setFormat] = useState<FormatId>("instagram");
  const [busy, setBusy] = useState(false);
  const f = FORMATS.find((x) => x.id === format)!;
  const match = prayer.ref.match(/^(.*?)\s+(\d+[:\d–-]*)$/);
  const book = match ? match[1] : prayer.ref;
  const cv = match ? match[2] : "";
  const firstLine = prayer.prayer.replace(/\n/g, " ").split(/(?<=[.!?])\s+/)[0];

  const download = async () => {
    if (busy) return;
    setBusy(true);
    try { const { downloadPrayerCard } = await import("@/lib/home/prayer-card"); await downloadPrayerCard(prayer, format); }
    finally { setBusy(false); }
  };

  return (
    <section className="hm-pod pk-bleed" data-nav="dark" aria-labelledby="hm-pod-title">
      <div className="hm-wrap hm-pod__grid">
        <Reveal className="hm-pod__stage">
          <AnimatePresence mode="wait">
            <motion.div key={format} className="hm-pod__card" style={{ aspectRatio: `${f.width} / ${f.height}` }} initial={{ opacity: 0, scale: 0.94, rotateX: 8 }} animate={{ opacity: 1, scale: 1, rotateX: 0 }} exit={{ opacity: 0, scale: 0.96 }} transition={{ type: "spring", stiffness: 120, damping: 18 }}>
              <span className="hm-pod__label">Prayer of the day</span>
              <span className="hm-pod__book pk-display">{book}</span>
              <span className="hm-pod__cv pk-display">{cv}</span>
              <span className="hm-pod__rule" />
              <span className="hm-pod__verse pk-book">“{prayer.verse}”</span>
              <span className="hm-pod__line pk-book">{firstLine}</span>
              <span className="hm-pod__mark">PRAYERKEY.COM</span>
            </motion.div>
          </AnimatePresence>
        </Reveal>

        <Reveal className="hm-pod__copy" delay={0.1}>
          <span className="pk-chip pk-chip--glass">Prayer of the day</span>
          <h2 id="hm-pod-title" className="pk-display hm-h2 hm-h2--light">{prayer.title}</h2>
          <p className="hm-pod__prayer pk-book">{prayer.prayer.split("\n\n").slice(1, 2).join(" ") || firstLine}</p>
          <div className="hm-pod__formats" role="radiogroup" aria-label="Image size">
            {FORMATS.map((x) => (
              <button key={x.id} type="button" role="radio" aria-checked={format === x.id} onClick={() => setFormat(x.id)} className={format === x.id ? "is-on" : ""}>
                <strong>{x.label}</strong><span>{x.sub}</span>
              </button>
            ))}
          </div>
          <div className="hm-actions">
            <button type="button" onClick={() => void download()} disabled={busy} className="pk-capsule pk-capsule--gold">
              {busy ? <Loader2 size={17} className="hm-spin" /> : <Download size={17} />} Download for {f.label}
            </button>
            <Link href={`/pray?topic=${encodeURIComponent("prayer based on " + prayer.ref)}`} className="pk-capsule pk-capsule--glass"><Sparkles size={16} /> Pray it</Link>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
