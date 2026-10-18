"use client";
import { useEffect, useState } from "react";

export type TocItem = { id: string; label: string };

// The contents rail beside a long page. A thin gold line across the top
// fills as the page is read, and the rail marks the section you are in.
export default function ReadingToc({ items, label = "On this page" }: { items: TocItem[]; label?: string }) {
  const [at, setAt] = useState(-1);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    let raf = 0;
    const update = () => {
      raf = 0;
      const body = document.querySelector<HTMLElement>(".rd-body");
      if (body) {
        const r = body.getBoundingClientRect();
        setProgress(Math.min(1, Math.max(0, -r.top / Math.max(1, r.height - window.innerHeight * 0.6))));
      }
      let current = -1;
      items.forEach(({ id }, i) => {
        const s = document.getElementById(id);
        if (s && s.getBoundingClientRect().top < window.innerHeight * 0.35) current = i;
      });
      setAt(current);
    };
    const onScroll = () => { if (!raf) raf = requestAnimationFrame(update); };
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => { window.removeEventListener("scroll", onScroll); window.removeEventListener("resize", onScroll); cancelAnimationFrame(raf); };
  }, [items]);

  return (
    <>
      <div className="rd-progress" aria-hidden style={{ transform: `scaleX(${progress})` }} />
      <nav className="rd-toc" aria-label={label}>
        <p>{label}</p>
        <ol>
          {items.map((it, i) => (
            <li key={it.id} className={i === at ? "is-here" : i < at ? "is-read" : ""}>
              <a href={`#${it.id}`} aria-current={i === at ? "location" : undefined}>{it.label}</a>
            </li>
          ))}
        </ol>
      </nav>
    </>
  );
}
