import Link from "next/link";
import { ArrowRight, BookOpen, Sparkles } from "lucide-react";
import HeavenHero from "@/components/home/HeavenHero";
import Story from "@/components/home/Story";
import Reveal, { RevealWords } from "@/components/home/Reveal";
import { DAILY_VERSES, ROTATING_WORDS, getDailyVerse } from "@/lib/home/daily";
import { FAQS } from "@/lib/home/faqs";
import "@/components/home/home.css";
import "@/components/home/stage.css";
import "@/components/home/story.css";
import "@/components/home/journal-app.css";

// The verse and prayer change daily, so the page is rebuilt every hour rather than frozen at build time.
export const revalidate = 3600;

// Three things the site does, one line each. They were three long write-ups -
// a "#1" claim, a lead, a heading, a paragraph and five ticks apiece - which made
// the home page fourteen screens on a phone.
const DOORS = [
  { href: "/pray", art: "/art/window-prayer.webp", title: "Pray", line: "Tell God what is on your heart and pray it through.", cta: "Start praying" },
  { href: "/live", art: "/art/glass-church.webp", title: "Live sermon", line: "Bible verses on the screen as you preach.", cta: "Start a sermon" },
  { href: "/bible", art: "/art/bible-key.webp", title: "Bible", line: "Find any verse, and the verses that go with it.", cta: "Search the Bible" },
];

export default function HomePage() {
  const verse = getDailyVerse();
  // Ten verses a day, starting somewhere new each day.
  const first = DAILY_VERSES.indexOf(verse);
  // the deck starts on tomorrow's verse: today's is already the first thing on the page
  const pulls = Array.from({ length: 10 }, (_, k) => DAILY_VERSES[(first + 1 + k) % DAILY_VERSES.length]);
  const faqLd = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: FAQS.map((f) => ({ "@type": "Question", name: f.q, acceptedAnswer: { "@type": "Answer", text: f.a } })),
  };

  return (
    <div className="hm">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />
      <HeavenHero words={ROTATING_WORDS} />

      {/* Verse of the day */}
      <section className="hm-verse pk-bleed" aria-labelledby="hm-verse-title">
        <div className="hm-wrap hm-verse__inner">
          <Reveal><span className="pk-chip pk-chip--gold">Verse of the day</span></Reveal>
          <h2 id="hm-verse-title" className="sr-only">Verse of the day: {verse.ref}</h2>
          <blockquote className="hm-verse__text pk-book">
            <RevealWords text={`“${verse.text}”`} />
          </blockquote>
          <Reveal delay={0.2}><p className="hm-verse__ref pk-display">{verse.ref}</p></Reveal>
          <Reveal delay={0.3} className="hm-actions hm-actions--center">
            <Link href={`/pray?topic=${encodeURIComponent("prayer based on " + verse.ref)}`} className="pk-capsule pk-capsule--violet"><Sparkles size={16} /> Pray this verse</Link>
            <Link href={`/bible?q=${encodeURIComponent(verse.ref)}`} className="pk-capsule pk-capsule--tint"><BookOpen size={16} /> Read {verse.book} {verse.chapter}</Link>
          </Reveal>
        </div>
      </section>

      <Story verses={pulls} />

      {/* What else is here */}
      <section className="hm-doors" aria-labelledby="hm-doors-title">
        <div className="hm-wrap">
          <Reveal><h2 id="hm-doors-title" className="pk-display hm-h2 hm-h2--center">Pray. Preach. Read.</h2></Reveal>
          <div className="hm-doors__row">
            {DOORS.map((d, i) => (
              <Reveal key={d.href} delay={0.08 * i}>
                <Link href={d.href} className="hm-door-card">
                  <img src={d.art} alt="" loading="lazy" />
                  <span className="hm-door-card__text">
                    <strong className="pk-display">{d.title}</strong>
                    <span>{d.line}</span>
                    <em>{d.cta} <ArrowRight size={14} /></em>
                  </span>
                </Link>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* The door is open */}
      <section className="hm-door pk-bleed" data-nav="dark" aria-labelledby="hm-door-title">
        <img src="/art/open-door.webp" alt="" loading="lazy" className="hm-door__art" />
        <span className="hm-door__shade" />
        <div className="hm-door__text">
          <Reveal><h2 id="hm-door-title" className="pk-display hm-door__title">The door is open.</h2></Reveal>
          <Reveal delay={0.15}><Link href="/pray" className="pk-capsule pk-capsule--light">Start praying <ArrowRight size={16} /></Link></Reveal>
        </div>
      </section>

      {/* Questions */}
      <section className="hm-faq" aria-labelledby="hm-faq-title">
        <Reveal><h2 id="hm-faq-title" className="pk-display hm-h2 hm-h2--center">Questions</h2></Reveal>
        <div className="hm-faq__list">
          {FAQS.slice(0, 4).map((f) => (
            <details key={f.q}>
              <summary><span>{f.q}</span><span aria-hidden className="hm-faq__plus" /></summary>
              <p>{f.a}</p>
            </details>
          ))}
          <details className="hm-faq__more">
            <summary><span>More questions</span><span aria-hidden className="hm-faq__plus" /></summary>
            {FAQS.slice(4).map((f) => (
              <details key={f.q}>
                <summary><span>{f.q}</span><span aria-hidden className="hm-faq__plus" /></summary>
                <p>{f.a}</p>
              </details>
            ))}
          </details>
        </div>
      </section>
    </div>
  );
}
