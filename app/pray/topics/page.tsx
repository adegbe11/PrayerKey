import type { Metadata } from "next";
import Link from "next/link";
import InView from "@/components/home/InView";
import PrayerCard from "@/components/prayer/PrayerCard";
import PrayerLibrary from "@/components/prayer/PrayerLibrary";
import PrayerSearch from "@/components/prayer/PrayerSearch";
import { bySlug, exists, lite, shelves } from "@/lib/prayer/shelves";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";
import "../../prayer/prayer-hub.css";
import "@/components/prayer/cards.css";
import "./topics.css";

export const metadata: Metadata = {
  title: "All Prayer Topics | PrayerKey — Prayers for Every Situation",
  description: `Browse ${PRAYER_TOPICS.length} prayer topics: healing, anxiety, marriage, finances, grief and more. Find a prayer for every situation, free.`,
  alternates: { canonical: "https://www.prayerkey.com/pray/topics" },
};

// the hand that is dealt when the page opens
const FAN = ["anxiety", "strength", "healing", "marriage", "hope"];

export default function TopicsPage() {
  const fan = FAN.filter(exists);
  return (
    <div className="ph pt">
      <InView />

      <section className="pt-hero">
        <div className="pt-fan" aria-hidden>
          {fan.map((slug, i) => (
            <PrayerCard key={slug} slug={slug} title={bySlug.get(slug)!.title} href={`/pray/${slug}`} tabIndex={-1} className="pt-fan__card" style={{ "--k": i - (fan.length - 1) / 2 } as React.CSSProperties} />
          ))}
        </div>
        <h1 className="ph-title pt-title"><span>Prayers for</span><span>every situation.</span></h1>
        <PrayerSearch items={PRAYER_TOPICS.map(lite)} base="/pray" />
      </section>

      <section className="ph-shelf" data-in="" aria-label="All prayer topics">
        <PrayerLibrary groups={shelves()} base="/pray" />
      </section>

      <section className="ph-own pk-bleed" data-nav="dark" data-in="">
        <h2 className="ph-own__title"><span>Not here?</span><span>Write your own.</span></h2>
        <p className="ph-own__line">Tell PrayerKey what you are facing and the free prayer generator builds a prayer for it from Scripture.</p>
        <Link href="/pray" className="ph-own__cta">Open the prayer generator</Link>
      </section>
    </div>
  );
}
