import type { Metadata } from "next";
import Link from "next/link";
import "./not-found.css";

export const metadata: Metadata = {
  title:       "Page Not Found — PrayerKey",
  description: "We couldn't find that page. Try the free prayer generator, Bible search, the live sermon projector, or the full documentation.",
  robots:      { index: false, follow: true },
};

// where most people were heading
const WAYS: [string, string][] = [
  ["/prayer", "Prayers"],
  ["/bible", "The Bible"],
  ["/pray", "Write a prayer"],
  ["/live", "Live Sermon"],
  ["/docs", "Documentation"],
];

export default function NotFound() {
  return (
    <section className="nf" aria-labelledby="nf-title">
      <img className="nf-mark" src="/pk-mark.png" alt="" width={56} height={56} />
      <p className="nf-kicker">404</p>
      <h1 id="nf-title" className="nf-title">
        <span>This page</span> <span>is not here.</span>
      </h1>
      <p className="nf-line">
        The link may be broken, or the page may have moved. Here are a few good places to go instead.
      </p>
      <Link href="/" className="nf-home">Back to home</Link>
      <nav className="nf-ways" aria-label="Where to go instead">
        {WAYS.map(([href, label]) => (
          <Link key={href} href={href}>{label}</Link>
        ))}
      </nav>
    </section>
  );
}
