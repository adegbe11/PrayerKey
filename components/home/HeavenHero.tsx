"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion, useScroll, useTransform } from "framer-motion";
import { useReducedMotion } from "@/lib/use-reduced-motion";
import { ArrowRight, BookOpen, Radio } from "lucide-react";

// Deterministic "random" so the server and the browser draw the same dust.
function seeded(n: number) { const x = Math.sin(n * 12.9898) * 43758.5453; return x - Math.floor(x); }
const DUST = Array.from({ length: 38 }, (_, i) => ({
  left: `${(seeded(i + 1) * 100).toFixed(2)}%`,
  size: +(1.5 + seeded(i + 40) * 3.5).toFixed(2),
  duration: +(12 + seeded(i + 80) * 16).toFixed(2),
  delay: +(-seeded(i + 120) * 28).toFixed(2),
  drift: Math.round((seeded(i + 160) - 0.5) * 120),
  opacity: +(0.35 + seeded(i + 200) * 0.6).toFixed(2),
}));

// The opening plays like a film: five of our own scenes dissolve into each other, each with a slow camera move.
const REEL = [
  { src: "/art/heaven-hands.webp", pos: "50% 74%", mpos: "50% 28%", dx: "-1.6%", dy: "-1.4%" },
  { src: "/art/dawn-mountain.webp", pos: "50% 60%", mpos: "50% 50%", dx: "1.6%", dy: "-1%" },
  { src: "/art/window-prayer.webp", pos: "50% 38%", mpos: "50% 40%", dx: "-1.2%", dy: "1.2%" },
  { src: "/art/open-door.webp", pos: "50% 45%", mpos: "50% 45%", dx: "1.4%", dy: "-1.4%" },
  { src: "/art/morning-prayer.webp", pos: "50% 40%", mpos: "50% 40%", dx: "-1.4%", dy: "-1%" },
];

export default function HeavenHero({ words }: { words: string[] }) {
  const router = useRouter();
  const ref = useRef<HTMLElement>(null);
  const reduce = useReducedMotion();
  const { scrollYProgress } = useScroll({ target: ref, offset: ["start start", "end start"] });
  const artY = useTransform(scrollYProgress, [0, 1], ["0%", "16%"]);
  const textY = useTransform(scrollYProgress, [0, 1], ["0%", "-22%"]);
  const textFade = useTransform(scrollYProgress, [0, 0.75], [1, 0]);

  // Starts on "blessings", the line people know; then the others take their turn.
  const start = Math.max(0, words.indexOf("blessings"));
  const [index, setIndex] = useState(start);
  useEffect(() => {
    if (reduce) return;
    const id = setInterval(() => setIndex((i) => (i + 1) % words.length), 2800);
    return () => clearInterval(id);
  }, [reduce, words.length]);

  const [scene, setScene] = useState(0);
  useEffect(() => {
    const id = setInterval(() => setScene((n) => (n + 1) % REEL.length), 7000);
    return () => clearInterval(id);
  }, []);

  const [heart, setHeart] = useState("");
  const pray = (event: React.FormEvent) => {
    event.preventDefault();
    const topic = heart.trim();
    router.push(topic ? `/pray?topic=${encodeURIComponent(topic)}` : "/pray");
  };

  return (
    <section ref={ref} className="hm-hero pk-bleed" data-nav="dark" aria-labelledby="hm-hero-title">
      <motion.div className="hm-hero__art" style={reduce ? undefined : { y: artY }} aria-hidden>
        {REEL.map((r, i) => (
          <img
            key={r.src}
            src={r.src}
            alt=""
            className={`hm-reel ${i === scene ? "is-on" : ""}`}
            style={{ ["--pos" as string]: r.pos, ["--mpos" as string]: r.mpos, ["--dx" as string]: r.dx, ["--dy" as string]: r.dy }}
            fetchPriority={i === 0 ? "high" : "low"}
          />
        ))}
      </motion.div>
      <div className="hm-hero__veil" aria-hidden />
      <div className="hm-hero__rays" aria-hidden />
      <div className="hm-hero__glow" aria-hidden />
      <div className="hm-hero__dust" aria-hidden>
        {DUST.map((d, i) => (
          <span key={i} style={{ left: d.left, width: d.size, height: d.size, animationDuration: `${d.duration}s`, animationDelay: `${d.delay}s`, ["--dx" as string]: `${d.drift}px`, ["--o" as string]: d.opacity }} />
        ))}
      </div>

      <motion.div className="hm-hero__content" style={reduce ? undefined : { y: textY, opacity: textFade }}>
        <motion.span className="pk-chip pk-chip--glass" initial={reduce ? false : { opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8, delay: 0.2 }}>
          <img src="/pk-mark.png" alt="" width={8} height={14} style={{ height: 14, width: "auto" }} /> Prayer · Scripture · Church
        </motion.span>

        <h1 id="hm-hero-title" className="hm-hero__title pk-display">
          <motion.span initial={reduce ? false : { opacity: 0, y: 24, filter: "blur(10px)" }} animate={{ opacity: 1, y: 0, filter: "blur(0px)" }} transition={{ duration: 1.1, delay: 0.35, ease: [0.22, 1, 0.36, 1] }} style={{ display: "block" }}>
            Where beautiful
          </motion.span>
          <span className="hm-hero__word">
            {/* Old and new words cross-fade in the same cell, so the line is never empty. */}
            <AnimatePresence initial={false}>
              <motion.span key={words[index]} className="pk-gold-text" initial={{ opacity: 0, y: "0.3em", filter: "blur(10px)" }} animate={{ opacity: 1, y: 0, filter: "blur(0px)" }} exit={{ opacity: 0, y: "-0.3em", filter: "blur(10px)" }} transition={{ duration: 0.55, ease: [0.22, 1, 0.36, 1] }}>
                {words[index]}
              </motion.span>
            </AnimatePresence>
          </span>
          <motion.span initial={reduce ? false : { opacity: 0, y: 24, filter: "blur(10px)" }} animate={{ opacity: 1, y: 0, filter: "blur(0px)" }} transition={{ duration: 1.1, delay: 0.55, ease: [0.22, 1, 0.36, 1] }} style={{ display: "block" }}>
            are born.
          </motion.span>
        </h1>

        <motion.p className="hm-hero__lead" initial={reduce ? false : { opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.9, delay: 0.8 }}>
          Type a prayer request and get a full prayer back. Preach a sermon and see Bible verses on the screen. Search any scripture in seconds. Free, forever.
        </motion.p>

        <motion.form className="hm-hero__ask" onSubmit={pray} initial={reduce ? false : { opacity: 0, y: 18, scale: 0.98 }} animate={{ opacity: 1, y: 0, scale: 1 }} transition={{ type: "spring", stiffness: 90, damping: 18, delay: 1.0 }}>
          <label htmlFor="hm-heart" className="sr-only">What&apos;s on your heart?</label>
          <input id="hm-heart" value={heart} onChange={(e) => setHeart(e.target.value)} maxLength={500} placeholder="What’s on your heart?" autoComplete="off" />
          <button type="submit" className="pk-capsule pk-capsule--light">Pray <ArrowRight size={17} /></button>
        </motion.form>

        <motion.div className="hm-hero__more" initial={reduce ? false : { opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.9, delay: 1.25 }}>
          <Link href="/live" className="pk-capsule pk-capsule--glass"><Radio size={16} /> Start Live Sermon</Link>
          <Link href="/bible" className="pk-capsule pk-capsule--glass"><BookOpen size={16} /> Read the Bible</Link>
        </motion.div>
      </motion.div>

      <div className="hm-hero__cue" aria-hidden><span /></div>
    </section>
  );
}
