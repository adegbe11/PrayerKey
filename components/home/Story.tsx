"use client";
import Link from "next/link";
import { useRef, useState } from "react";
import { AnimatePresence, motion, useMotionValueEvent, useScroll } from "framer-motion";
import { ArrowRight } from "lucide-react";
import PlayBadge from "@/components/app/PlayBadge";
import Phone from "@/components/app/Phone";
import { BlackBible, PullDeck, type PullVerse } from "./BibleStage";
import JournalApp from "./JournalApp";

// One pinned stage. As you scroll, each idea takes over the whole screen, then hands over to the next.
const SCENES = [
  { id: "bible", chip: "The Bible", title: "Open Monochrome Bible Old and New Testament" },
  { id: "pull", chip: "Pull", title: "Pull your manna." },
  { id: "journal", chip: "Journal", title: "Write it down." },
  { id: "app", chip: "The app", title: "Take it with you." },
] as const;

export default function Story({ verses }: { verses: PullVerse[] }) {
  const ref = useRef<HTMLElement>(null);
  const [at, setAt] = useState(0);
  const { scrollYProgress } = useScroll({ target: ref, offset: ["start start", "end end"] });
  useMotionValueEvent(scrollYProgress, "change", (v) => setAt(Math.min(SCENES.length - 1, Math.max(0, Math.floor(v * SCENES.length * 0.999)))));
  const scene = SCENES[at];

  const jump = (n: number) => {
    const el = ref.current;
    if (!el) return;
    const top = el.getBoundingClientRect().top + window.scrollY;
    window.scrollTo({ top: top + (el.offsetHeight - window.innerHeight) * ((n + 0.5) / SCENES.length), behavior: "smooth" });
  };

  return (
    <section ref={ref} className="st pk-bleed" data-nav="dark" aria-label="PrayerKey, the app" style={{ height: `${SCENES.length * 100}svh` }}>
      <div className="st-pin">
        <div className="st-glow" aria-hidden />
        <ol className="st-dots" aria-label="Sections">
          {SCENES.map((s, n) => (
            <li key={s.id}><button type="button" aria-label={s.chip} aria-current={n === at} className={n === at ? "is-on" : n < at ? "is-done" : ""} onClick={() => jump(n)} /></li>
          ))}
        </ol>

        <div className="hm-wrap st-stage">
          <AnimatePresence mode="wait">
            <motion.div key={scene.id} className="st-scene" initial={{ opacity: 0, y: 46 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -34 }} transition={{ duration: 0.55, ease: [0.22, 1, 0.36, 1] }}>
              <div className="st-copy">
                <span className="pk-chip pk-chip--glass">{scene.chip}</span>
                {at === 0 ? <h2 className="pk-display st-title st-title--long">{scene.title}</h2> : <h2 className="pk-display st-title">{scene.title}</h2>}
                {scene.id === "bible" && <Link href="/bible" className="pk-capsule pk-capsule--light">Open the Bible <ArrowRight size={16} /></Link>}
                {scene.id === "app" && (
                  <div className="hm-actions">
                    <PlayBadge source="home" size="lg" />
                    <Link href="/download" className="pk-capsule pk-capsule--glass">See the app <ArrowRight size={16} /></Link>
                  </div>
                )}
              </div>
              <div className="st-visual">
                {scene.id === "bible" && <BlackBible />}
                {scene.id === "pull" && <PullDeck verses={verses} />}
                {scene.id === "journal" && <JournalApp />}
                {scene.id === "app" && (
                  <div className="st-phones">
                    <Phone src="/app/bible.webp" alt="The Bible in the PrayerKey app" className="st-phone st-phone--l" />
                    <Phone src="/app/home.webp" alt="The PrayerKey app home screen" className="st-phone st-phone--c" />
                    <Phone src="/app/journal.webp" alt="The journal in the PrayerKey app" className="st-phone st-phone--r" />
                  </div>
                )}
              </div>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </section>
  );
}
