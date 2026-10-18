import Link from "next/link";
import InView from "@/components/home/InView";
import PrayerComposer from "@/components/PrayerComposer";
import PrayerCard, { colourOf, splitTitle } from "@/components/prayer/PrayerCard";
import PrayerReader from "@/components/prayer/PrayerReader";
import { ALL_SLUGS, TOPIC_MAP, type PrayerTopic } from "@/lib/seo/prayer-topics";

export const AUTHOR = "Collins Asein";
export const REVIEW_DATE = "April 2026";

// The prayer's title as it reads inside a sentence: "prayer for visa and
// immigration approval", "morning prayer", "prayer based on Psalm 23".
// Capitals stay only where the prayer's own description keeps them mid-sentence,
// so names (God, Kenya, Psalm 23) and acronyms (IVF) survive.
function phraseOf(topic: PrayerTopic) {
  return topic.title.split(" ").map((w, i) => {
    if (!/^[A-Z][a-z]/.test(w)) return w;
    const bare = w.replace(/[^A-Za-z]/g, "");
    const keep = i > 0 && new RegExp(`[a-z,;]\\s${bare}\\b`).test(topic.metaDesc);
    return keep ? w : w[0].toLowerCase() + w.slice(1);
  }).join(" ");
}

function refToSlug(ref: string) {
  return ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "");
}

export function prayerPoints(topic: PrayerTopic): string[] {
  if (topic.prayerPoints?.length) return topic.prayerPoints;
  const phrase = phraseOf(topic);
  const s0 = topic.scripture[0];
  const s1 = topic.scripture[1] ?? topic.scripture[0];
  return [
    `Thank God that He hears every ${phrase} and that His Word promises He will answer.`,
    `Declare ${s0.ref} over this situation: ${s0.text}`,
    `Ask God to step into every detail of what you are bringing to Him today.`,
    `Pray for perseverance, and for faith to keep believing before you see the answer.`,
    `Pray for others who are bringing the same need to God right now. You are not alone.`,
    `Declare ${s1.ref}: ${s1.text} Receive it as a promise for today.`,
    `Close by giving thanks in advance, trusting that God has already begun to move.`,
  ];
}

export function howToPray(topic: PrayerTopic): string[] {
  if (topic.howToPray?.length) return topic.howToPray;
  const phrase = phraseOf(topic);
  return [
    `Find a quiet place. Even two minutes of stillness before God is enough to begin your ${phrase}.`,
    `Open with gratitude. Acknowledge who God is before you bring your request.`,
    `Pray the prayer above aloud or from your heart, with the names and details of your own situation.`,
    `Speak the scriptures out. Faith comes by hearing, and hearing by the word of God (Romans 10:17).`,
    `Close with an act of faith. Give the outcome to God, thank Him in advance, and choose trust over worry.`,
  ];
}

export function faqsOf(topic: PrayerTopic): { q: string; a: string }[] {
  if (topic.faqs?.length) return topic.faqs;
  const phrase = phraseOf(topic);
  const s0 = topic.scripture[0];
  return [
    { q: `What is the best ${phrase}?`, a: `${topic.metaDesc} The prayer above is built on Scripture and written to be prayed aloud. Make it yours with the details of your situation.` },
    { q: `Which Bible verse goes with a ${phrase}?`, a: `${s0.ref} is one of the strongest: ${s0.text} Praying Scripture back to God anchors your prayer in His promises rather than your feelings.` },
    { q: `How do I pray a ${phrase}?`, a: `Begin in stillness, acknowledge God's power and faithfulness, then pray specifically. Use the prayer above as your guide, speak the verses with it, and close by giving the outcome to God with thanksgiving.` },
    { q: `How often should I pray a ${phrase}?`, a: `Scripture says to pray without ceasing (1 Thessalonians 5:17). Pray this daily, or whenever the need comes back. Many people choose a set time, morning or night, and keep praying until they see the answer.` },
    { q: `Can God really answer a ${phrase}?`, a: `Yes. Jesus said, ask, and ye shall receive (John 16:24). He may answer differently than you expect, but He always hears. No ${phrase} is too small or too big for Him.` },
  ];
}

// One prayer, as a page. The hero is the card you opened, grown to full width;
// then the prayer itself, read slowly; then what helps you pray it.
export default function PrayerPost({ topic, base, full }: { topic: PrayerTopic; base: "/prayer" | "/pray"; full: boolean }) {
  // the heading says exactly what the page title says: "Prayer for" small, the subject large
  const parts = topic.title.match(/^(Prayer (?:for|before|after|against|of|when|during|on|in|to))\s+(.*)$/i);
  const lead = parts?.[1] ?? null;
  const subject = parts?.[2] ?? topic.title;
  const colour = colourOf(topic.slug);
  const related = topic.related.map((s) => TOPIC_MAP.get(s)).filter(Boolean) as PrayerTopic[];
  const points = full ? prayerPoints(topic) : [];
  const steps = full ? howToPray(topic) : [];
  const faqs = full ? faqsOf(topic) : [];
  const url = `https://www.prayerkey.com${base}/${topic.slug}`;
  const long = subject.length > 22;

  return (
    <article className="pp" style={{ "--bg": colour } as React.CSSProperties}>
      <InView />

      <header className="pp-hero pk-bleed" data-nav="dark">
        <div className="pp-hero__in">
          <nav aria-label="Breadcrumb" className="pp-crumbs">
            <Link href="/">Home</Link><span>/</span>
            <Link href={base === "/prayer" ? "/prayer" : "/pray/topics"}>Prayers</Link><span>/</span>
            <span>{topic.category}</span>
          </nav>
          <h1 className={`pp-title ${long ? "pp-title--long" : ""}`}>
            {lead && <span className="pp-title__lead"><img src="/pk-mark.png" alt="" width={12} height={21} />{lead}</span>}{lead && " "}
            <span className="pp-title__subject">{subject}</span>
          </h1>
          <p className="pp-lede">{topic.metaDesc}</p>
          <p className="pp-meta">
            <Link href="/author/collins-asein">{AUTHOR}</Link>
            <span>Reviewed {REVIEW_DATE}</span>
            <span>2 min prayer</span>
          </p>
        </div>
      </header>

      <section className="pp-body" aria-label={topic.title}>
        <PrayerReader text={topic.samplePrayer} title={topic.title} url={url} />

        <ul className="pp-verses" data-in="">
          {topic.scripture.map((v, i) => (
            <li key={v.ref} style={{ "--i": i } as React.CSSProperties}>
              <p>{v.text}</p>
              <Link href={`/bible/verse/${refToSlug(v.ref)}`}>{v.ref}</Link>
            </li>
          ))}
        </ul>
      </section>

      {full && (
        <>
          <section className="pp-section" data-in="" aria-labelledby="points">
            <h2 id="points" className="pp-h2">Prayer points</h2>
            <ol className="pp-points">
              {points.map((p, i) => <li key={i} style={{ "--i": i } as React.CSSProperties}><span className="pp-points__n">{i + 1}</span><span>{p}</span></li>)}
            </ol>
          </section>

          <section className="pp-section" data-in="" aria-labelledby="how">
            <h2 id="how" className="pp-h2">How to pray it</h2>
            <ol className="pp-steps">
              {steps.map((s, i) => <li key={i} style={{ "--i": i } as React.CSSProperties}>{s}</li>)}
            </ol>
          </section>

          <section className="pp-section" data-in="" aria-labelledby="why">
            <h2 id="why" className="pp-h2">Why people pray this</h2>
            {topic.context ? (
              <p className="pp-p">{topic.context}</p>
            ) : (
              <>
                <p className="pp-p">People have prayed a {phraseOf(topic)} in every culture and every season of life. They bring it to God not because they have nowhere else to go, but because they have learned that He hears.</p>
                <p className="pp-p">The Bible is full of people who prayed this kind of prayer and found God faithful. From the Psalms of David to the letters of Paul, Scripture says it again and again: God hears, God cares, and God acts.</p>
              </>
            )}
          </section>
        </>
      )}

      <section className="pp-own" data-in="" aria-labelledby="own">
        <h2 id="own" className="pp-h2">Pray about your own situation</h2>
        <PrayerComposer seedTopic={topic.title} compact />
      </section>

      {faqs.length > 0 && (
        <section className="pp-section pp-faq" data-in="" aria-labelledby="faq">
          <h2 id="faq" className="pp-h2">Questions</h2>
          {faqs.map((f, i) => (
            <details key={i}>
              <summary><span>{f.q}</span><span aria-hidden className="pp-faq__plus" /></summary>
              <p>{f.a}</p>
            </details>
          ))}
        </section>
      )}

      {related.length > 0 && (
        <section className="pp-related" data-in="" aria-labelledby="related">
          <h2 id="related" className="pp-h2">Keep praying</h2>
          <ul className="pp-related__cards">
            {related.slice(0, 4).map((r, i) => (
              <li key={r.slug} style={{ "--i": i } as React.CSSProperties}><PrayerCard slug={r.slug} title={r.title} href={`${base}/${r.slug}`} /></li>
            ))}
          </ul>
          <Link href={base === "/prayer" ? "/prayer" : "/pray/topics"} className="pp-all">See all {ALL_SLUGS.length} prayers</Link>
        </section>
      )}

      <p className="pp-note">
        Written by <Link href="/author/collins-asein">{AUTHOR}</Link> and reviewed by the PrayerKey editorial team in {REVIEW_DATE}. Every prayer on PrayerKey is built on Scripture.
      </p>
    </article>
  );
}
