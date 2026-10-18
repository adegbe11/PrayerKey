import Link from "next/link";

// A prayer as a card: one solid colour, the subject set large. The colour
// comes from the prayer itself, so the card and the page it opens always match.

export const COLOURS = ["#0f8b8d", "#e4572e", "#1e2a5a", "#e8892b", "#3a4256", "#2f7d4f", "#d4a017", "#2456c9", "#16a3b8", "#6200ed", "#b8336a", "#5b3a29"];

export function colourOf(slug: string) {
  let h = 0;
  for (let i = 0; i < slug.length; i++) h = (h * 31 + slug.charCodeAt(i)) >>> 0;
  return COLOURS[h % COLOURS.length];
}

// "Prayer for Healing" -> lead "A prayer for", subject "Healing"
export function splitTitle(title: string) {
  const m = title.match(/^Prayer (for|before|after|against|of|when|during|on|in|to)\s+(.*)$/i);
  if (m) return { lead: `A prayer ${m[1].toLowerCase()}`, subject: m[2] };
  const n = title.match(/^(.*?)\s+Prayer$/i);
  if (n) return { lead: "A prayer", subject: n[1] };
  return { lead: "A prayer", subject: title.replace(/^Prayer\s+/i, "") };
}

// the longest word must fit across the card, and long subjects step down
export function subjectSize(subject: string) {
  const longest = Math.max(...subject.split(/\s+/).map((w) => w.length), 5);
  return Math.min(21, 150 / longest, subject.length > 30 ? 12 : subject.length > 18 ? 15 : 21);
}

export default function PrayerCard({ slug, title, href, className = "", style, tabIndex }: {
  slug: string; title: string; href: string; className?: string; style?: React.CSSProperties; tabIndex?: number;
}) {
  const { lead, subject } = splitTitle(title);
  return (
    <Link
      href={href}
      className={`ph-card ${className}`}
      tabIndex={tabIndex}
      aria-label={title}
      style={{ "--bg": colourOf(slug), "--fs": `${subjectSize(subject).toFixed(1)}cqi`, ...style } as React.CSSProperties}
    >
      <span className="ph-card__top"><img src="/pk-mark.png" alt="" width={11} height={20} /><span>{lead}</span></span>
      <span className="ph-card__subject">{subject}</span>
      <span className="ph-card__brand">PrayerKey</span>
    </Link>
  );
}
