import type { Metadata } from "next";
import Link from "next/link";
import { Check, CloudOff, Lock, ShieldCheck, Wallet } from "lucide-react";
import DownloadHero from "@/components/app/DownloadHero";
import PlayBadge from "@/components/app/PlayBadge";
import Phone from "@/components/app/Phone";
import Reveal from "@/components/home/Reveal";
import { APP_INCLUDES, PLAY_ID } from "@/lib/app-store";
import "@/components/app/download.css";

const URL = "https://www.prayerkey.com/download";

export const metadata: Metadata = {
  title: "Download PrayerKey — Free Prayer & Bible App for Android",
  description: "Download PrayerKey free on Google Play: personal prayers, 500+ prayer situations, 366 devotionals and the whole King James Bible offline. No account needed.",
  alternates: { canonical: URL },
  openGraph: {
    title: "Download PrayerKey — Free Prayer & Bible App",
    description: "Pray, read the whole Bible offline and keep a daily devotional. Free on Google Play, no account needed.",
    url: URL, type: "website", images: [{ url: "/og-image.png", width: 1200, height: 630 }],
  },
};

// Chapters follow the Play listing, in its own words.
const CHAPTERS = [
  { shot: "/app/today.webp", chip: "Pray", title: "Pray about what you’re facing.", body: "Write what you want to pray about in your own words — your marriage, your children, your health, your work, your finances, a decision, or someone you love. PrayerKey turns what you carry into a prayer you can actually pray. Or browse over 500 prayer situations and find the one that fits." },
  { shot: "/app/prayer-points.webp", chip: "Prayer points", title: "Know what to pray.", body: "Prayer points for salvation, repentance, thanksgiving, faith, protection, healing, marriage, children, family and finances. Choose your situation and pray through them one at a time." },
  { shot: "/app/salvation.webp", chip: "Guided prayer", title: "Five minutes or an hour.", body: "Choose 5, 10, 15, 30 or 60 minutes. Guided prayer with Scripture and prompts, lighter guidance when you need it, or a plain timer. PrayerKey gives you the structure. You do the praying." },
  { shot: "/app/challenge.webp", chip: "Morning & evening", title: "A rhythm you can keep.", body: "Start the morning with Scripture and prayer, and come back in the evening to give thanks and pray before you sleep. Midnight prayer, warfare prayer and prayer challenges when you want to go further." },
  { shot: "/app/bible.webp", chip: "Bible", title: "The whole Bible, offline.", body: "The complete King James Version is inside the app: all 66 books and 31,102 verses, with no connection needed. Or listen to the audio Bible when you would rather hear the Word." },
  { shot: "/app/church.webp", chip: "Church notes", title: "Stay with the sermon.", body: "PrayerKey listens during the sermon and writes down what is said. Scripture references are caught as they are spoken, and the recording never leaves your phone." },
  { shot: "/app/journal.webp", chip: "Journal", title: "See how God answered.", body: "Write what you’re praying for and what you’re learning. Mark a prayer when it is answered, and look back in a year to see how much of it was." },
];

const PRIVATE = [
  { Icon: Wallet, title: "Nothing to pay.", body: "PrayerKey is free." },
  { Icon: Lock, title: "No account to create.", body: "Open it and start praying." },
  { Icon: CloudOff, title: "Works offline.", body: "Everything essential, and the whole Bible." },
  { Icon: ShieldCheck, title: "No data collected.", body: "As declared on Google Play." },
];

const FAQS = [
  { q: "Is the PrayerKey app free?", a: "Yes. PrayerKey is free to download on Google Play, and there is nothing to pay inside the app." },
  { q: "Do I need an account?", a: "No. There is no account to create. Install the app and start praying straight away." },
  { q: "Does PrayerKey work offline?", a: "Yes. Everything essential works offline, including the complete King James Bible — all 66 books and 31,102 verses — with nothing extra to download." },
  { q: "Is there a PrayerKey app for iPhone?", a: "PrayerKey is available for Android on Google Play. On iPhone and iPad you can use prayerkey.com in Safari — tap Share, then Add to Home Screen to keep it one tap away." },
  { q: "What data does the app collect?", a: "The developer declares on Google Play that the app collects no data and shares no data with third parties. Sermon recordings for church notes stay on your phone." },
  { q: "Which Bible translation is in the app?", a: "The King James Version, complete and offline, with an audio Bible for listening." },
  { q: "What can I pray about?", a: "Anything you are carrying. Write it in your own words and PrayerKey gives you a Bible-grounded prayer, or browse over 500 prayer situations, from marriage and children to health, work, finances and decisions." },
  { q: "Does it have a daily devotional?", a: "Yes. There are 366 devotionals, arranged as twelve monthly keys, each written for its day, plus a Verse of the Day to read, save and share." },
  { q: "Can it take sermon notes?", a: "Yes. PrayerKey listens during the sermon, writes down what is said and catches Scripture references as they are spoken. The recording never leaves your phone." },
  { q: "Can I set prayer reminders or a fast?", a: "Yes. Set a fast with a rhythm you can keep, and get reminded at the hour you actually pray." },
];

export default function DownloadPage() {
  const ld = [
    {
      "@context": "https://schema.org", "@type": "MobileApplication", name: "PrayerKey: Prayer & Bible",
      operatingSystem: "ANDROID", applicationCategory: "LifestyleApplication", url: URL,
      installUrl: `https://play.google.com/store/apps/details?id=${PLAY_ID}`,
      description: "Personal prayers, prayer points, guided prayer, 366 devotionals and the whole King James Bible offline.",
      offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
      featureList: APP_INCLUDES,
    },
    { "@context": "https://schema.org", "@type": "FAQPage", mainEntity: FAQS.map((f) => ({ "@type": "Question", name: f.q, acceptedAnswer: { "@type": "Answer", text: f.a } })) },
  ];

  return (
    <div className="dl">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(ld) }} />
      <DownloadHero />

      <section className="dl-start pk-bleed">
        <div className="dl-wrap">
          <Reveal><p className="pk-display dl-start__line">Some days you know exactly what to pray. <span className="dl-muted">Other days, you don’t know where to begin.</span></p></Reveal>
          <Reveal delay={0.15}><p className="pk-display dl-start__answer">PrayerKey gives you a place to start.</p></Reveal>
        </div>
      </section>

      <div className="dl-chapters pk-bleed">
        {CHAPTERS.map((c, i) => (
          <section key={c.title} className="dl-chapter" aria-label={c.title}>
            <div className={`dl-wrap dl-chapter__grid ${i % 2 ? "dl-chapter__grid--flip" : ""}`}>
              <Reveal className="dl-chapter__shot" y={48}><Phone src={c.shot} alt={`PrayerKey app: ${c.chip}`} /></Reveal>
              <Reveal className="dl-chapter__copy" delay={0.1}>
                <span className="pk-chip pk-chip--violet">{c.chip}</span>
                <h2 className="pk-display dl-h2">{c.title}</h2>
                <p>{c.body}</p>
              </Reveal>
            </div>
          </section>
        ))}
      </div>

      <section className="dl-includes pk-bleed" aria-labelledby="dl-includes-title">
        <div className="dl-wrap">
          <Reveal><h2 id="dl-includes-title" className="pk-display dl-h2 dl-center">Everything inside.</h2></Reveal>
          <div className="dl-includes__list" role="list">
            {APP_INCLUDES.map((item, i) => (
              <Reveal key={item} delay={Math.min(i, 10) * 0.03} className="dl-includes__item" role="listitem"><Check size={16} /> {item}</Reveal>
            ))}
          </div>
        </div>
      </section>

      <section className="dl-private pk-bleed" data-nav="dark" aria-labelledby="dl-private-title">
        <div className="dl-wrap">
          <Reveal><h2 id="dl-private-title" className="pk-display dl-h2 dl-h2--light dl-center">Your prayers stay yours.</h2></Reveal>
          <div className="dl-private__grid">
            {PRIVATE.map(({ Icon, title, body }, i) => (
              <Reveal key={title} delay={i * 0.08} className="dl-private__card">
                <Icon size={26} />
                <strong className="pk-display">{title}</strong>
                <span>{body}</span>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      <section className="dl-steps pk-bleed" aria-labelledby="dl-steps-title">
        <div className="dl-wrap">
          <Reveal><h2 id="dl-steps-title" className="pk-display dl-h2 dl-center">Start in a minute.</h2></Reveal>
          <div className="dl-steps__list">
            {[["Install", "Get PrayerKey free from Google Play."], ["Tell it what’s on your heart", "Write it in your own words, or choose a situation."], ["Keep praying", "Morning, evening, and every day after."]].map(([t, b], i) => (
              <Reveal key={t} delay={i * 0.1} className="dl-steps__item"><span className="pk-display">{i + 1}</span><strong>{t}</strong><p>{b}</p></Reveal>
            ))}
          </div>
          <Reveal delay={0.2} className="dl-center dl-steps__cta"><PlayBadge size="lg" source="download" /></Reveal>
        </div>
      </section>

      <section className="dl-faq" aria-labelledby="dl-faq-title">
        <Reveal><h2 id="dl-faq-title" className="pk-display dl-h2 dl-center">Questions.</h2></Reveal>
        <div className="dl-faq__list">
          {FAQS.map((f) => (
            <details key={f.q}><summary><span>{f.q}</span><span aria-hidden className="dl-faq__plus" /></summary><p>{f.a}</p></details>
          ))}
        </div>
        <p className="dl-faq__web">Prefer the browser? <Link href="/pray">Pray on prayerkey.com</Link>.</p>
      </section>

      <section className="dl-end pk-bleed" data-nav="dark" aria-labelledby="dl-end-title">
        <img src="/art/open-door.webp" alt="" loading="lazy" className="dl-end__art" />
        <span className="dl-end__shade" />
        <div className="dl-end__text">
          <Reveal><h2 id="dl-end-title" className="pk-display dl-end__title">Pray. Read the Bible. Keep going.</h2></Reveal>
          <Reveal delay={0.15}><PlayBadge size="lg" source="download" /></Reveal>
        </div>
      </section>
    </div>
  );
}
