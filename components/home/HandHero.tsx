"use client";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { ArrowRight } from "lucide-react";
import PlayBadge from "@/components/app/PlayBadge";

// The opening, the way the best app sites do it: one bold promise, the real app
// in a hand, and one way to get it - on the brand's own violet.
export default function HandHero() {
  const router = useRouter();
  const [heart, setHeart] = useState("");
  const pray = (event: React.FormEvent) => {
    event.preventDefault();
    const topic = heart.trim();
    router.push(topic ? `/pray?topic=${encodeURIComponent(topic)}` : "/pray");
  };
  // entrances are CSS (hand-hero.css): visible without JavaScript
  const rise = (delay: number) => ({ style: { animationDelay: `${delay}s` } as React.CSSProperties });

  return (
    <section className="hh pk-bleed" data-nav="dark" aria-labelledby="hh-title">
      <div className="hh__grid">
        <div className="hh__copy">
          <h1 id="hh-title" className="hh__title hh-in" {...rise(0.05)}>
            Pray every day.<br />Hear God in<br className="hh__br" /> His Word.
          </h1>
          <p className="hh__lead hh-in" {...rise(0.15)}>
            Prayers for what you are facing, the whole Bible, devotionals and a prayer journal, all in one place. Free.
          </p>

          <div className="hh__get hh-in" {...rise(0.25)}>
            <PlayBadge source="home" size="lg" />
          </div>

          <form className="hh__ask hh-in" onSubmit={pray} {...rise(0.35)}>
            <label htmlFor="hh-heart" className="sr-only">What do you want to pray about?</label>
            <input id="hh-heart" value={heart} onChange={(e) => setHeart(e.target.value)} maxLength={500} placeholder="Or pray now: what is on your heart?" autoComplete="off" />
            <button type="submit" aria-label="Pray"><ArrowRight size={18} /></button>
          </form>
        </div>

        {/* A CSS entrance, not a script one: the image must show even before the
            page's JavaScript runs (it was server-rendered at opacity 0, so a slow
            phone showed no image at all). */}
        <div className="hh__hand" aria-hidden>
          <picture>
            <source media="(max-width: 700px)" srcSet="/art/hero-phone-sm.webp" />
            <img src="/art/hero-phone.webp" alt="" fetchPriority="high" />
          </picture>
        </div>
      </div>
    </section>
  );
}
