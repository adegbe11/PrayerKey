"use client";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { motion } from "framer-motion";
import { ArrowRight } from "lucide-react";
import { useReducedMotion } from "@/lib/use-reduced-motion";
import PlayBadge from "@/components/app/PlayBadge";

// The opening, the way the best app sites do it: one bold promise, the real app
// in a hand, and one way to get it - on the brand's own violet.
export default function HandHero() {
  const router = useRouter();
  const reduce = useReducedMotion();
  const [heart, setHeart] = useState("");
  const pray = (event: React.FormEvent) => {
    event.preventDefault();
    const topic = heart.trim();
    router.push(topic ? `/pray?topic=${encodeURIComponent(topic)}` : "/pray");
  };
  const rise = (delay: number) =>
    reduce ? {} : { initial: { opacity: 0, y: 24 }, animate: { opacity: 1, y: 0 }, transition: { type: "spring" as const, stiffness: 110, damping: 20, delay } };

  return (
    <section className="hh pk-bleed" data-nav="dark" aria-labelledby="hh-title">
      <div className="hh__grid">
        <div className="hh__copy">
          <motion.h1 id="hh-title" className="hh__title" {...rise(0.05)}>
            Pray every day.<br />Hear God in<br className="hh__br" /> His Word.
          </motion.h1>
          <motion.p className="hh__lead" {...rise(0.15)}>
            Prayers for what you are facing, the whole Bible, devotionals and a prayer journal, all in one place. Free.
          </motion.p>

          <motion.div className="hh__get" {...rise(0.25)}>
            <PlayBadge source="home" size="lg" />
          </motion.div>

          <motion.form className="hh__ask" onSubmit={pray} {...rise(0.35)}>
            <label htmlFor="hh-heart" className="sr-only">What do you want to pray about?</label>
            <input id="hh-heart" value={heart} onChange={(e) => setHeart(e.target.value)} maxLength={500} placeholder="Or pray now: what is on your heart?" autoComplete="off" />
            <button type="submit" aria-label="Pray"><ArrowRight size={18} /></button>
          </motion.form>

          <motion.p className="hh__trust" {...rise(0.45)}>Free · Works offline · Your prayers stay on your phone</motion.p>
        </div>

        <motion.div
          className="hh__hand"
          aria-hidden
          initial={reduce ? false : { opacity: 0, y: 90, rotate: 4 }}
          animate={{ opacity: 1, y: 0, rotate: 0 }}
          transition={{ type: "spring", stiffness: 60, damping: 16, delay: 0.2 }}
        >
          <picture>
            <source media="(max-width: 700px)" srcSet="/art/hero-phone-sm.webp" />
            <img src="/art/hero-phone.webp" alt="" fetchPriority="high" />
          </picture>
        </motion.div>
      </div>
    </section>
  );
}
