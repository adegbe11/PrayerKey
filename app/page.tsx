import Link from "next/link";
import HandHero from "@/components/home/HandHero";
import ChapterMoments from "@/components/home/ChapterMoments";
import InView from "@/components/home/InView";
import { FAQS } from "@/lib/home/faqs";
import "@/components/home/hand-hero.css";
import "@/components/home/home-story.css";

// The home page tells one story about one thing - the app - the way Apple
// tells a product: a promise, then each part of the day shown on the real
// screen, then one way to get it. It used to be seven unrelated sections with
// the same button three times; now every section earns the next scroll.
export const revalidate = 3600;

const CHAPTERS = [
  {
    id: "pray",
    tone: "light",
    kicker: "Pray",
    title: ["Ten pulls.", "Four minutes."],
    line: "Each day is one prayer in ten short pulls. Read one, pray it, and pull down for the next.",
    shots: [{ src: "/app/v2/pray.webp", alt: "A prayer in PrayerKey: the first of ten pulls, Dear Lord, I am not coming for what I can get." }],
  },
  {
    id: "read",
    tone: "night",
    kicker: "Read",
    title: ["The whole Bible.", "No signal needed."],
    line: "The King James Version lives on your phone. Every day brings a passage, a written devotional and a question to carry.",
    shots: [
      { src: "/app/v2/read.webp", alt: "A verse from Lamentations on a paper card in PrayerKey." },
      { src: "/app/v2/devotional.webp", alt: "Today's devotional in PrayerKey: Endurance Through an Open Door." },
    ],
  },
  {
    id: "remember",
    tone: "light",
    kicker: "Remember",
    title: ["Write it down.", "Watch Him answer."],
    line: "A private journal that stays on your phone. Mark a page as a prayer, and mark it answered when God does.",
    shots: [{ src: "/app/v2/journal.webp", alt: "A journal page in PrayerKey: Mum came home today." }],
  },
  {
    id: "church",
    tone: "violet",
    kicker: "Church",
    title: ["Listen to the sermon.", "Keep every word."],
    line: "Tap Start and PrayerKey writes your notes as the preacher speaks, with the scriptures caught along the way.",
    shots: [{ src: "/app/v2/church.webp", alt: "Church in PrayerKey: tap Start to take sermon notes." }],
  },
] as const;

// what the preacher says, and what goes up on the screen
const LIVE = [
  { said: "and Paul could say, I can do all things through Christ", ref: "Philippians 4:13", text: "I can do all things through Christ which strengtheneth me." },
  { said: "turn with me to Psalm twenty-three, verse one", ref: "Psalm 23:1", text: "The LORD is my shepherd; I shall not want." },
  { said: "He promised He would never walk away from you", ref: "Hebrews 13:5", text: "I will never leave thee, nor forsake thee." },
];

export default function HomePage() {
  const faqLd = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: FAQS.slice(0, 5).map((f) => ({ "@type": "Question", name: f.q, acceptedAnswer: { "@type": "Answer", text: f.a } })),
  };

  return (
    <div className="hs">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />
      <HandHero />
      <InView />

      {CHAPTERS.map((c, index) => (
        <section
          key={c.id}
          id={c.id}
          className={`hs-chapter hs-chapter--${c.tone} hs-chapter--${c.id} ${index % 2 ? "hs-chapter--flip" : ""} pk-bleed`}
          data-nav={c.tone === "light" ? undefined : "dark"}
          aria-labelledby={`hs-${c.id}`}
        >
          <div className="hs-chapter__grid">
            <div className="hs-chapter__copy" data-in="">
              <p className="hs-kicker">{c.kicker}</p>
              <h2 id={`hs-${c.id}`} className="hs-title">
                {c.title.map((t) => <span key={t}>{t}</span>)}
              </h2>
              <p className="hs-line">{c.line}</p>
            </div>
            <div className={`hs-phones hs-phones--${c.shots.length}`} data-in="">
              {c.shots.map((s) => (
                <figure key={s.src} className="hs-phone">
                  <img src={s.src} alt={s.alt} loading="lazy" decoding="async" width={720} height={1616} />
                </figure>
              ))}
              <ChapterMoments id={c.id} />
            </div>
          </div>
        </section>
      ))}

      <section className="hs-live pk-bleed" data-nav="dark" aria-labelledby="hs-live-title">
        <div className="hs-live__grid">
          <div className="hs-live__copy hs-copy-in" data-in="">
            <p className="hs-kicker">PrayerKey Live</p>
            <h2 id="hs-live-title" className="hs-title">
              <span>Preach.</span><span>The verse is</span><span>already up.</span>
            </h2>
            <p className="hs-line">
              Open PrayerKey Live on the church laptop. As the preacher speaks, every Bible verse appears on the
              projector by itself, even when it is only paraphrased.
            </p>
            <Link href="/live" className="hs-live__cta">Open PrayerKey Live</Link>
          </div>

          {/* a projector, showing what happens on a Sunday: words spoken, then the verse */}
          <div className="hs-live__stage" data-in="" aria-hidden>
            <div className="hs-live__screen">
              {LIVE.map((v, i) => (
                <div key={v.ref} className="hs-live__slide" style={{ "--d": `${i * 6}s` } as React.CSSProperties}>
                  <p className="hs-live__verse">{v.text}</p>
                  <p className="hs-live__ref">{v.ref} <span>KJV</span></p>
                </div>
              ))}
            </div>
            <div className="hs-live__mic">
              <span className="hs-live__dot" />
              <span className="hs-live__wave">{Array.from({ length: 5 }, (_, i) => <i key={i} style={{ "--b": i } as React.CSSProperties} />)}</span>
              <span className="hs-live__said">
                {LIVE.map((v, i) => (
                  <span key={v.ref} style={{ "--d": `${i * 6}s` } as React.CSSProperties}>{v.said}</span>
                ))}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section className="hs-faq" aria-labelledby="hs-faq-title">
        <h2 id="hs-faq-title" className="hs-faq__title">Questions</h2>
        <div className="hs-faq__list" data-in="">
          {FAQS.slice(0, 5).map((f) => (
            <details key={f.q}>
              <summary><span>{f.q}</span><span aria-hidden className="hs-faq__plus" /></summary>
              <p>{f.a}</p>
            </details>
          ))}
        </div>
      </section>
    </div>
  );
}
