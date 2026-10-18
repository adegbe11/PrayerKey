import Link from "next/link";
import InView from "@/components/home/InView";
import PrayerCard from "@/components/prayer/PrayerCard";
import PrayerReader from "@/components/prayer/PrayerReader";
import { leatherOf } from "@/lib/bible/leather";
import { neighbours, type Devotional } from "@/lib/devotionals";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";

// One day's verse and its reflection: the verse alone on its book's leather,
// then the reflection to read, a question to carry, and a prayer to pray.
export default function DevotionalView({ d, heading, dateLine, url, children }: {
  d: Devotional; heading: string; dateLine: string; url: string; children?: React.ReactNode;
}) {
  const words = d.verse.text.split(/\s+/);
  const step = Math.min(90, 2400 / Math.max(words.length, 1));
  const { prev, next } = neighbours(d);
  const prayers = PRAYER_TOPICS.filter((t) => t.category === d.prayerCategory).slice(0, 4);

  return (
    <article className="vd" style={{ "--leather": leatherOf(d.verse.bookSlug) } as React.CSSProperties}>
      <InView />

      <header className="vd-hero pk-bleed" data-nav="dark">
        <div className="vd-hero__in">
          <p className="vd-date">{dateLine}</p>
          <h1 className="vd-h1">{heading}</h1>
          <blockquote className="vd-verse">
            {words.map((w, i) => <span key={i} style={{ animationDelay: `${0.4 + (i * step) / 1000}s` }}>{w} </span>)}
          </blockquote>
          <Link href={`/bible/verse/${d.verse.refSlug}`} className="vd-ref">{d.verse.ref} <span>KJV</span></Link>
          <p className="vd-series">{d.series} · Day {d.day} of {d.seriesOf}</p>
        </div>
      </header>

      {/* the reflection */}
      <section className="vd-read" data-in="" aria-labelledby="vd-title">
        <p className="vd-kicker">Reflection · {d.minutes} min read</p>
        <h2 id="vd-title" className="vd-title">{d.title}</h2>
        <div className="vd-body">
          {d.blocks.map((b, i) => b.kind === "p"
            ? <p key={i}>{b.text}</p>
            : (
              <figure key={i} className="vd-quote">
                <p>{b.text}</p>
                <figcaption><Link href={`/bible/verse/${b.refSlug}`}>{b.ref}</Link></figcaption>
              </figure>
            ))}
        </div>
        <p className="vd-line"><span>{d.line.a}</span><span>{d.line.b}</span></p>
        <div className="vd-body">
          {d.after.map((p, i) => <p key={i}>{p}</p>)}
        </div>
      </section>

      {/* something to carry */}
      <section className="vd-carry" data-in="">
        <p className="vd-kicker">Carry this today</p>
        <p className="vd-question">{d.question}</p>
      </section>

      <section className="vd-pray" data-in="" aria-labelledby="vd-pray">
        <h2 id="vd-pray" className="vd-h2">Pray</h2>
        <PrayerReader text={d.prayer} title={`${d.dateLabel}: ${d.title}`} url={url} />
      </section>

      {/* the passage it comes from */}
      <section className="vd-passage" data-in="" aria-labelledby="vd-passage">
        <h2 id="vd-passage" className="vd-h2">Read {d.reading.label}</h2>
        <p className="vd-passage__text">
          {d.reading.verses.map((v) => (
            <span key={v.n} className={v.n === d.read.verse ? "is-key" : ""}>
              <Link href={`/bible/verse/${d.verse.bookSlug}-${d.read.chapter}-${v.n}`} className="vd-passage__n">{v.n}</Link>{v.text}{" "}
            </span>
          ))}
        </p>
      </section>

      <nav className="vd-turn" aria-label="Previous and next day">
        <Link href={`/verse-of-the-day/${prev.slug}`}>
          <small>{prev.dateLabel}</small>
          <span>{prev.title}</span>
        </Link>
        <Link href={`/verse-of-the-day/${next.slug}`} className="vd-turn__next">
          <small>{next.dateLabel}</small>
          <span>{next.title}</span>
        </Link>
      </nav>

      {prayers.length > 0 && (
        <section className="vd-prayers" data-in="" aria-labelledby="vd-prayers">
          <h2 id="vd-prayers" className="vd-h2">Keep praying</h2>
          <ul>
            {prayers.map((p, i) => (
              <li key={p.slug} style={{ "--i": i } as React.CSSProperties}><PrayerCard slug={p.slug} title={p.title} href={`/prayer/${p.slug}`} /></li>
            ))}
          </ul>
        </section>
      )}

      {children}

      <p className="vd-note">
        Reflection by <Link href="/author/collins-asein">Collins Asein</Link>, from the daily devotionals in the <Link href="/download">PrayerKey app</Link>. Bible text: King James Version.
      </p>
    </article>
  );
}
