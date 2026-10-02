import Link from "next/link";
import { ArrowRight, BookOpen, Check, Sparkles } from "lucide-react";
import HeavenHero from "@/components/home/HeavenHero";
import PrayerOfDay from "@/components/home/PrayerOfDay";
import Story from "@/components/home/Story";
import Reveal, { RevealWords } from "@/components/home/Reveal";
import { DAILY_VERSES, ROTATING_WORDS, getDailyPrayer, getDailyVerse } from "@/lib/home/daily";
import { FAQS } from "@/lib/home/faqs";
import "@/components/home/home.css";
import "@/components/home/stage.css";
import "@/components/home/story.css";
import "@/components/home/journal-app.css";

// The verse and prayer change daily, so the page is rebuilt every hour rather than frozen at build time.
export const revalidate = 3600;

const WORLD = [
  { href: "/pray", art: "/art/glass-pray.webp", chip: "Pray", title: "Tell God what’s on your heart.", cta: "Start praying", big: true },
  { href: "/bible", art: "/art/glass-read.webp", chip: "Bible", title: "Every word of Scripture." },
  { href: "/live", art: "/art/glass-church.webp", chip: "Live Sermon", title: "Verses on screen as you preach." },
  { href: "/prayer", art: "/art/morning-prayer.webp", chip: "Prayers", title: "A prayer for every moment." },
  { href: "/pray/topics", art: "/art/evening-prayer.webp", chip: "Topics", title: "Prayers for what you face." },
  { href: "/guides", art: "/art/cover-candle.webp", chip: "Guides", title: "Learn to pray, step by step." },
];

// The three long-standing feature write-ups, kept word for word.
const DEEP = [
  {
    href: "/pray", art: "/art/window-prayer.webp", chip: "Feature 01", title: <>#1 Prayer<br />Generator for Churches</>,
    lead: "The most personal way to write scripture-grounded prayers in seconds — for any situation.",
    heading: "Your Personal Prayer Writer",
    body: "Tell PrayerKey what you’re going through and it writes a full, heartfelt prayer grounded in scripture — personalised to your exact words, mood, and situation. Every prayer includes relevant Bible verses and an encouragement note.",
    points: ["Works for healing, grief, anxiety, finances, marriage and more", "Includes scripture-backed Bible verses automatically", "Choose your mood to personalise the tone", "No account, no limit, completely free"],
    cta: "Write my prayer",
  },
  {
    href: "/live", art: "/art/glass-church.webp", chip: "Feature 02", title: <>#1 Live Sermon<br />Verse Detection Tool</>,
    lead: "The fastest way to display Bible verses on your projector — automatically, as you preach.",
    heading: "Just preach.",
    body: "PrayerKey listens through your microphone, detects every Bible verse you quote or reference in real time, and displays it on the projector screen for your whole congregation — no operator, no typing, no delay.",
    points: ["Detects verses automatically as you speak", "Displays on projector with zero manual input", "Works with 11 Bible translations", "Shows confidence score for every verse", "Pastor controls everything from one screen"],
    cta: "Start a live sermon",
  },
  {
    href: "/bible", art: "/art/bible-key.webp", chip: "Feature 03", title: <>#1 Bible Search<br />&amp; Cross-Reference Tool</>,
    lead: "The smartest way to find any verse, topic, or keyword — with related scriptures included.",
    heading: "Find Any Verse Instantly",
    body: "Type a reference, keyword, or topic and PrayerKey returns the most relevant verses from across the entire Bible. Click any result to instantly load 4–6 cross-referenced scriptures that share the same theme — perfect for sermon prep, Bible study, or personal devotion.",
    points: ["Search by reference, keyword, topic, or paraphrase", "Covers all 66 books of the Bible", "Cross-references show deeply related scriptures", "Supports 11 major translations including KJV and NIV", "Instant results — no waiting, no account needed"],
    cta: "Search the Bible",
  },
];

export default function HomePage() {
  const verse = getDailyVerse();
  const prayer = getDailyPrayer();
  // Ten verses a day, starting somewhere new each day.
  const first = DAILY_VERSES.indexOf(verse);
  const pulls = Array.from({ length: 10 }, (_, k) => DAILY_VERSES[(first + k) % DAILY_VERSES.length]);
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

      <PrayerOfDay prayer={prayer} />

      {/* Feature stories */}
      <div className="hm-deep pk-bleed">
        {DEEP.map((d, i) => (
          <section key={d.href} className={`hm-band ${i % 2 ? "hm-band--night" : ""}`} data-nav={i % 2 ? "dark" : undefined} aria-label={d.heading}>
            <div className={`hm-wrap hm-band__grid ${i % 2 ? "hm-band__grid--flip" : ""}`}>
              <Reveal className="hm-band__art"><img src={d.art} alt="" loading="lazy" /></Reveal>
              <Reveal className="hm-band__copy" delay={0.08}>
                <span className={`pk-chip ${i % 2 ? "pk-chip--glass" : "pk-chip--violet"}`}>{d.chip}</span>
                <h2 className="pk-display hm-h2">{d.title}</h2>
                <p className="hm-band__lead">{d.lead}</p>
                <h3 className="hm-band__heading">{d.heading}</h3>
                <p className="hm-band__body">{d.body}</p>
                <ul className="hm-band__points">
                  {d.points.map((p) => <li key={p}><Check size={16} /> {p}</li>)}
                </ul>
                <Link href={d.href} className={`pk-capsule ${i % 2 ? "pk-capsule--light" : "pk-capsule--violet"}`}>{d.cta} <ArrowRight size={16} /></Link>
              </Reveal>
            </div>
          </section>
        ))}
      </div>

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
        <Reveal><h2 id="hm-faq-title" className="pk-display hm-h2 hm-h2--center">Frequently Asked Questions</h2></Reveal>
        <div className="hm-faq__list">
          {FAQS.slice(0, 8).map((f) => (
            <details key={f.q}>
              <summary><span>{f.q}</span><span aria-hidden className="hm-faq__plus" /></summary>
              <p>{f.a}</p>
            </details>
          ))}
          <details className="hm-faq__more">
            <summary><span>More questions</span><span aria-hidden className="hm-faq__plus" /></summary>
            {FAQS.slice(8).map((f) => (
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
