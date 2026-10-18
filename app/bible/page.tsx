"use client";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import InView from "@/components/home/InView";
import { leatherOf, spineHeight } from "@/lib/bible/leather";
import { BIBLE_BOOKS } from "@/lib/seo/bible-books";
import "./bible.css";

type Result = { ref: string; text: string; match: string };
type CrossRef = { ref: string; text: string; reason: string };

// the page the lamp moves over: a few verses about the Word itself (KJV)
const PAGE = [
  "In the beginning was the Word, and the Word was with God, and the Word was God.",
  "Thy word is a lamp unto my feet, and a light unto my path.",
  "The grass withereth, the flower fadeth: but the word of our God shall stand for ever.",
  "Heaven and earth shall pass away, but my words shall not pass away.",
  "All scripture is given by inspiration of God, and is profitable for doctrine, for reproof, for correction, for instruction in righteousness.",
  "For the word of God is quick, and powerful, and sharper than any twoedged sword.",
  "Man shall not live by bread alone, but by every word that proceedeth out of the mouth of God.",
  "The entrance of thy words giveth light; it giveth understanding unto the simple.",
  "Thy word have I hid in mine heart, that I might not sin against thee.",
  "So shall my word be that goeth forth out of my mouth: it shall not return unto me void.",
  "Every word of God is pure: he is a shield unto them that put their trust in him.",
  "Sanctify them through thy truth: thy word is truth.",
].join("  ·  ");

const HINTS = ["John 3:16", "do not fear", "Psalm 23", "grace", "peace"];

function Shelf({ books }: { books: typeof BIBLE_BOOKS }) {
  return (
    <div className="bh-shelf">
      <div className="bh-shelf__books">
        {books.map((b, i) => (
          <Link
            key={b.slug}
            href={`/bible/${b.slug}`}
            className="bh-spine"
            title={`${b.name}, ${b.chapters} chapter${b.chapters === 1 ? "" : "s"}`}
            style={{
              "--h": `${spineHeight(b.chapters)}px`,
              "--c": leatherOf(b.slug),
              "--i": i,
            } as React.CSSProperties}
          >
            <span className="bh-spine__name">{b.name}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default function BiblePage() {
  const [query, setQuery] = useState("");
  const [asked, setAsked] = useState("");
  const [results, setResults] = useState<Result[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState<string | null>(null);
  const [crossRefs, setCrossRefs] = useState<CrossRef[]>([]);
  const [crLoading, setCrLoading] = useState(false);
  const hero = useRef<HTMLElement>(null);
  const resultsRef = useRef<HTMLElement>(null);

  // the lamp: follows the pointer, and drifts on its own when nothing is guiding it
  useEffect(() => {
    const el = hero.current;
    if (!el) return;
    const still = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    let tx = 0.5, ty = 0.55, x = 0.5, y = 0.55, guided = 0, raf = 0, visible = true;
    const move = (e: PointerEvent) => {
      const r = el.getBoundingClientRect();
      tx = (e.clientX - r.left) / r.width; ty = (e.clientY - r.top) / r.height;
      guided = performance.now();
    };
    const tick = (t: number) => {
      raf = 0;
      if (performance.now() - guided > 2500) {
        // a slow figure of eight across the page
        tx = 0.5 + Math.sin(t / 3400) * 0.34; ty = 0.55 + Math.sin(t / 1700) * 0.2;
      }
      x += (tx - x) * 0.08; y += (ty - y) * 0.08;
      el.style.setProperty("--mx", `${(x * 100).toFixed(2)}%`);
      el.style.setProperty("--my", `${(y * 100).toFixed(2)}%`);
      if (visible && !still) raf = requestAnimationFrame(tick);
    };
    const io = new IntersectionObserver(([e]) => { visible = e.isIntersecting; if (visible && !raf && !still) raf = requestAnimationFrame(tick); });
    io.observe(el);
    el.addEventListener("pointermove", move);
    if (still) { el.style.setProperty("--mx", "50%"); el.style.setProperty("--my", "50%"); }
    return () => { cancelAnimationFrame(raf); io.disconnect(); el.removeEventListener("pointermove", move); };
  }, []);

  async function search(q = query) {
    const term = q.trim();
    if (!term) return;
    setQuery(term);
    setAsked(term);
    setLoading(true);
    setResults([]);
    setOpen(null);
    setCrossRefs([]);
    requestAnimationFrame(() => resultsRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }));
    try {
      const res = await fetch(`/api/bible/search?q=${encodeURIComponent(term)}&translation=NIV`);
      const data = await res.json();
      setResults(data.results ?? []);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  }

  async function related(v: Result) {
    if (open === v.ref) { setOpen(null); return; }
    setOpen(v.ref);
    setCrossRefs([]);
    setCrLoading(true);
    try {
      const res = await fetch(`/api/bible/cross-refs?ref=${encodeURIComponent(v.ref)}&translation=NIV`);
      const data = await res.json();
      setCrossRefs(data.refs ?? []);
    } finally {
      setCrLoading(false);
    }
  }

  const ot = BIBLE_BOOKS.filter((b) => b.testament === "old");
  const nt = BIBLE_BOOKS.filter((b) => b.testament === "new");

  return (
    <div className="bh">
      <InView />

      {/* a page of scripture in the dark, and a lamp over it */}
      <section ref={hero} className="bh-hero pk-bleed" data-nav="dark">
        <div className="bh-page" aria-hidden>{PAGE}  ·  {PAGE}</div>
        <div className="bh-page bh-page--lit" aria-hidden>{PAGE}  ·  {PAGE}</div>
        <div className="bh-lamp" aria-hidden />

        <div className="bh-hero__in">
          <p className="bh-kicker">The Bible</p>
          <h1 className="bh-title"><span>Search the</span> <span>whole Bible.</span></h1>
          <form className="bh-search" onSubmit={(e) => { e.preventDefault(); search(); }} role="search">
            <label htmlFor="bh-q" className="sr-only">Search the Bible</label>
            <svg className="bh-search__icon" viewBox="0 0 24 24" width="20" height="20" aria-hidden fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></svg>
            <input id="bh-q" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="A verse, a word, or a feeling" autoComplete="off" />
            <button type="submit" disabled={!query.trim() || loading} aria-label="Search">
              {loading ? <span className="bh-spin" /> : <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14M13 6l6 6-6 6" /></svg>}
            </button>
          </form>
          <p className="bh-hints">
            {HINTS.map((h) => <button key={h} type="button" onClick={() => search(h)}>{h}</button>)}
          </p>
        </div>
      </section>

      {/* what was found */}
      <section ref={resultsRef} className={`bh-results ${asked ? "is-on" : ""}`} aria-live="polite">
        {loading && (
          <div className="bh-skeleton" aria-label="Searching">
            {[0, 1, 2].map((i) => <div key={i} style={{ "--i": i } as React.CSSProperties}><i /><i /><i /></div>)}
          </div>
        )}
        {!loading && asked && results.length === 0 && (
          <p className="bh-empty">Nothing found for {asked}. Try a reference like Romans 8:28, or a single word.</p>
        )}
        {!loading && results.length > 0 && (
          <ol className="bh-verses">
            {results.map((r, i) => (
              <li key={r.ref} style={{ "--i": i } as React.CSSProperties} className={open === r.ref ? "is-open" : ""}>
                <button type="button" className="bh-verse" onClick={() => related(r)} aria-expanded={open === r.ref}>
                  <span className="bh-verse__ref">{r.ref}</span>
                  <span className="bh-verse__text">{r.text}</span>
                  <span className="bh-verse__more">{open === r.ref ? "Close" : "Related verses"}</span>
                </button>
                {open === r.ref && (
                  <div className="bh-related">
                    {crLoading && <p className="bh-related__wait"><span className="bh-spin bh-spin--dark" /> Finding verses that speak to it</p>}
                    {crossRefs.map((c, j) => (
                      <div key={c.ref} className="bh-related__item" style={{ "--j": j } as React.CSSProperties}>
                        <span className="bh-verse__ref">{c.ref}</span>
                        <span className="bh-related__text">{c.text}</span>
                      </div>
                    ))}
                  </div>
                )}
              </li>
            ))}
          </ol>
        )}
      </section>

      {/* the library */}
      <section className="bh-books" data-in="" aria-labelledby="bh-books">
        <h2 id="bh-books" className="bh-h2">66 books.</h2>
        <p className="bh-shelf__label">Old Testament</p>
        <Shelf books={ot.slice(0, 20)} />
        <Shelf books={ot.slice(20)} />
        <p className="bh-shelf__label">New Testament</p>
        <Shelf books={nt} />
      </section>
    </div>
  );
}
