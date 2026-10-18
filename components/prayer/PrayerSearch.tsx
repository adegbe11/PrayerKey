"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";

type Item = { slug: string; title: string };

// One field, the first thing on the page: type what you are facing and the
// matching prayers appear under it. Enter opens the first one.
export default function PrayerSearch({ items, base = "/prayer" }: { items: Item[]; base?: string }) {
  const router = useRouter();
  const [q, setQ] = useState("");
  const [active, setActive] = useState(0);
  const [focus, setFocus] = useState(false);

  const results = useMemo(() => {
    const words = q.toLowerCase().trim().split(/\s+/).filter((w) => w.length > 1 && !["for", "the", "my", "prayer", "a", "an", "to", "of"].includes(w));
    if (!words.length) return [];
    return items
      .map((it) => {
        const t = it.title.toLowerCase();
        const score = words.reduce((s, w) => s + (t.includes(w) ? (new RegExp(`\\b${w}`).test(t) ? 3 : 1) : 0), 0);
        return { it, score };
      })
      .filter((r) => r.score > 0)
      .sort((a, b) => b.score - a.score || a.it.title.length - b.it.title.length)
      .slice(0, 7)
      .map((r) => r.it);
  }, [q, items]);

  const onKey = (e: React.KeyboardEvent) => {
    if (e.key === "ArrowDown") { e.preventDefault(); setActive((a) => Math.min(a + 1, results.length - 1)); }
    else if (e.key === "ArrowUp") { e.preventDefault(); setActive((a) => Math.max(a - 1, 0)); }
    else if (e.key === "Enter") {
      e.preventDefault();
      const pick = results[active];
      router.push(pick ? `${base}/${pick.slug}` : `/pray?topic=${encodeURIComponent(q.trim())}`);
    } else if (e.key === "Escape") setQ("");
  };

  return (
    <div className={`ph-search ${q.trim() && focus ? "is-open" : ""}`}>
      <label htmlFor="ph-q" className="sr-only">What do you need prayer for?</label>
      <svg className="ph-search__icon" viewBox="0 0 24 24" width="20" height="20" aria-hidden fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></svg>
      <input
        id="ph-q"
        value={q}
        onChange={(e) => { setQ(e.target.value); setActive(0); }}
        onKeyDown={onKey}
        onFocus={() => setFocus(true)}
        // let a tap on a result land before the list closes
        onBlur={() => setTimeout(() => setFocus(false), 160)}
        placeholder="What do you need prayer for?"
        autoComplete="off"
        role="combobox"
        aria-expanded={focus && results.length > 0}
        aria-controls="ph-results"
      />
      {q.trim() && focus && (
        <ul id="ph-results" className="ph-search__results" role="listbox">
          {results.map((r, i) => (
            <li key={r.slug} role="option" aria-selected={i === active}>
              <Link href={`${base}/${r.slug}`} className={i === active ? "is-active" : ""} onMouseEnter={() => setActive(i)}>{r.title}</Link>
            </li>
          ))}
          <li className="ph-search__own">
            <Link href={`/pray?topic=${encodeURIComponent(q.trim())}`}>
              Write a prayer for <strong>{q.trim()}</strong>
            </Link>
          </li>
        </ul>
      )}
    </div>
  );
}
