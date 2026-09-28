"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion, useReducedMotion, useScroll, useTransform } from "framer-motion";
import { ArrowRight, BookOpen, Radio } from "lucide-react";

// Deterministic "random" so the server and the browser draw the same dust.
function seeded(n: number) { const x = Math.sin(n * 12.9898) * 43758.5453; return x - Math.floor(x); }
const DUST = Array.from({ length: 38 }, (_, i) => ({
  left: `${(seeded(i + 1) * 100).toFixed(2)}%`,
  size: 1.5 + seeded(i + 40) * 3.5,
  duration: 12 + seeded(i + 80) * 16,
  delay: -seeded(i + 120) * 28,
  drift: Math.round((seeded(i + 160) - 0.5) * 120),
  opacity: 0.35 + seeded(i + 200) * 0.6,
}));

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

  const [heart, setHeart] = useState("");
  const pray = (event: React.FormEvent) => {
    event.preventDefault();
    const topic = heart.trim();
    router.push(topic ? `/pray?topic=${encodeURIComponent(topic)}` : "/pray");
  };

  return (
    <section ref={ref} className="hm-hero pk-bleed" data-nav="dark" aria-labelledby="hm-hero-title">
      <motion.div className="hm-hero__art" style={reduce ? undefined : { y: artY }} aria-hidden>
        <img src="/art/heaven-hands.webp" alt="" fetchPriority="high" />
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
            <AnimatePresence mode="wait" initial={false}>
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
