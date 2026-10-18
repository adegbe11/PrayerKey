import Link from "next/link";
import PlayBadge from "@/components/app/PlayBadge";

// The opening, the way the best app sites do it: one bold promise, the real app
// in a hand, and one way to get it - on the brand's own violet.
export default function HandHero() {
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

          {/* one action on this screen: the app. The web prayer stays a quiet link. */}
          <Link href="/pray" className="hh__web hh-in" {...rise(0.35)}>Or pray now on the web</Link>
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
