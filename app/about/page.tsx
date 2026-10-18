import type { Metadata } from "next";
import Link from "next/link";
import InView from "@/components/home/InView";
import AboutMission from "./AboutMission";
import "./about.css";

const BASE_URL  = "https://www.prayerkey.com";
const ABOUT_URL = `${BASE_URL}/about`;

export const metadata: Metadata = {
  title:       "About PrayerKey — Free Prayer, Bible and Church Companion | Built by Collins Asein",
  description: "PrayerKey is a free church companion built for every pastor and believer: live sermon verse detection, prayers built on Scripture, and Bible search. No account, free forever. Built by Collins Omoikhudu Asein.",
  alternates:  { canonical: ABOUT_URL },
  openGraph: {
    title:       "About PrayerKey — For Every Church, Not Just the Big Ones",
    description: "Free prayer, Bible and church tools for every pastor and believer. Built by Collins Omoikhudu Asein.",
    url:         ABOUT_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "About PrayerKey" }],
  },
};

const organizationLd = {
  "@context": "https://schema.org",
  "@type":    "Organization",
  name:       "PrayerKey",
  url:        BASE_URL,
  logo:       `${BASE_URL}/og-image.png`,
  description: "Free church companion: live sermon Bible-verse detection, a prayer generator built on Scripture, and Bible search.",
  founder: {
    "@type": "Person",
    name:    "Collins Omoikhudu Asein",
  },
  sameAs: [
    "https://github.com/adegbe11/PrayerKey",
  ],
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",  item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "About", item: ABOUT_URL },
  ],
};

// why PrayerKey exists, a line at a time
const MISSION = [
  "For too long, the best ministry tools belonged to the biggest churches.",
  "Small congregations, home churches and independent pastors made do with whatever they could piece together.",
  "Every pastor behind a pulpit deserves the same tools,",
  "whatever the budget, whatever the size of the room.",
  "PrayerKey exists to make them free.",
];

// what it is: one idea each
const PARTS = [
  {
    kicker: "Pray",
    title: "A prayer for today.",
    line: "Each day is one prayer in ten short pulls. Or tell PrayerKey what you are carrying and the free prayer generator writes a prayer for it from Scripture, with the verses it stands on and a word of encouragement.",
    href: "/pray", cta: "Write a prayer",
  },
  {
    kicker: "Read",
    title: "The whole Bible.",
    line: "Search by reference, keyword, topic, or a verse you only half remember. Open any verse and its cross-references are a tap away, across all 66 books.",
    href: "/bible", cta: "Open the Bible",
  },
  {
    kicker: "Remember",
    title: "A private journal.",
    line: "Write down what you are praying for. Mark it answered when God does. It stays on your phone.",
    href: "/download", cta: "Get the app",
  },
  {
    kicker: "Church",
    title: "Sermon notes that write themselves.",
    line: "Tap Start and PrayerKey writes your notes as the preacher speaks, with the scriptures caught along the way.",
    href: "/download", cta: "Get the app",
  },
  {
    kicker: "PrayerKey Live",
    title: "Preach. The verse is already up.",
    line: "PrayerKey listens through the microphone and matches what is said against the whole Bible, even a paraphrase. Each verse appears on the projector with no operator and no typing, with a confidence score and eleven translations including KJV, NIV, ESV, NKJV and NLT.",
    href: "/live", cta: "Open PrayerKey Live",
  },
];

const PEOPLE = [
  "Pastors who want every listener to follow the scripture.",
  "Church leaders without the budget for expensive software.",
  "Ministers preparing a funeral, a wedding or a dedication.",
  "Believers who pray every day and want to go deeper in the Word.",
  "Bible study leaders who need to find and connect scriptures faster.",
];

const TRADITIONS = ["Catholic", "Protestant", "Pentecostal", "Baptist", "Anglican", "Methodist", "Non-denominational"];

export default function AboutPage() {
  return (
    <div className="ab">
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      <InView />

      {/* an open door: that is the whole idea */}
      <section className="ab-hero pk-bleed" data-nav="dark">
        <img className="ab-hero__art" src="/art/open-door.webp" alt="" width={1122} height={1402} decoding="async" fetchPriority="high" />
        <div className="ab-hero__in">
          <p className="ab-hero__kicker">About PrayerKey</p>
          <h1 className="ab-hero__title"><span>For every church,</span><span>not just the big ones.</span></h1>
          <p className="ab-hero__line">
            PrayerKey is a free companion for prayer, the Bible and Sunday service. It helps pastors preach,
            believers pray deeper, and congregations stay close to the Word of God.
          </p>
        </div>
      </section>

      {/* the signature moment: the mission, lit line by line */}
      <section className="ab-mission" aria-labelledby="ab-mission-title">
        <h2 id="ab-mission-title" className="ab-kicker">Why it exists</h2>
        <AboutMission lines={MISSION} />
      </section>

      {/* what it is */}
      <section className="ab-parts" aria-labelledby="ab-parts-title">
        <h2 id="ab-parts-title" className="ab-h2" data-in="">One companion,<br />from Monday to Sunday.</h2>
        <ol className="ab-parts__list">
          {PARTS.map((p) => (
            <li key={p.kicker} className="ab-part" data-in="">
              <p className="ab-part__kicker">{p.kicker}</p>
              <h3 className="ab-part__title">{p.title}</h3>
              <div className="ab-part__body">
                <p>{p.line}</p>
                <Link href={p.href}>{p.cta}</Link>
              </div>
            </li>
          ))}
        </ol>
      </section>

      {/* who it is for */}
      <section className="ab-who" aria-labelledby="ab-who-title" data-in="">
        <h2 id="ab-who-title" className="ab-h2">For pastors,<br />and anyone who prays.</h2>
        <ul className="ab-who__people">
          {PEOPLE.map((p, i) => <li key={p} style={{ "--i": i } as React.CSSProperties}>{p}</li>)}
        </ul>
        <p className="ab-who__traditions">
          {TRADITIONS.map((t) => <span key={t}>{t}</span>)}
          <span>and every Christian tradition.</span>
        </p>
      </section>

      {/* free */}
      <section className="ab-free pk-bleed" data-nav="dark" data-in="" aria-labelledby="ab-free-title">
        <h2 id="ab-free-title" className="ab-free__title"><span>No account.</span><span>No payment.</span><span>No barrier.</span></h2>
        <p className="ab-free__line">
          You open PrayerKey and you begin. No sign-up, no login, no payment of any kind. A pastor in a rural
          village deserves the same access as a pastor in a city megachurch, and a registration wall would keep
          out the people who need it most. PrayerKey is, and will remain, free to use.
        </p>
      </section>

      {/* privacy */}
      <section className="ab-quiet" data-in="" aria-labelledby="ab-private-title">
        <h2 id="ab-private-title" className="ab-h2">What you pray<br />is yours.</h2>
        <div className="ab-quiet__body">
          <p>With no account there is nothing to sign in to and no profile to build. Your journal stays on your phone, where you wrote it.</p>
          <Link href="/privacy">Read the privacy policy</Link>
        </div>
      </section>

      {/* who built it */}
      <section className="ab-maker" data-in="" aria-labelledby="ab-maker-title">
        <img className="ab-maker__mark" src="/pk-mark.png" alt="" width={44} height={80} />
        <h2 id="ab-maker-title" className="ab-maker__title">Built by Collins Omoikhudu Asein.</h2>
        <p className="ab-maker__line">
          It began with a simple observation: the church around the world is full of devoted people serving God
          with little. Technology should be a blessing to the church, not a business built on top of it.
          PrayerKey is independent, with no investor pressure and no agenda but to serve pastors and believers well.
        </p>
        <nav className="ab-maker__links" aria-label="Next">
          <Link href="/download" className="ab-maker__cta">Get PrayerKey</Link>
          <Link href="/author/collins-asein" className="ab-maker__more">About Collins</Link>
        </nav>
      </section>
    </div>
  );
}
