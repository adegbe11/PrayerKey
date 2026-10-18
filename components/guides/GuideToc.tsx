"use client";
import { useEffect, useState } from "react";

// A thin gold line across the top fills as the guide is read, and the
// contents beside it follow along, marking the section you are in.
export default function GuideToc({ headings }: { headings: string[] }) {
  const [at, setAt] = useState(-1);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    let raf = 0;
    const update = () => {
      raf = 0;
      const body = document.querySelector<HTMLElement>(".gd-body");
      if (body) {
        const r = body.getBoundingClientRect();
        setProgress(Math.min(1, Math.max(0, -r.top / Math.max(1, r.height - window.innerHeight * 0.6))));
      }
      let current = -1;
      headings.forEach((_, i) => {
        const s = document.getElementById(`s${i + 1}`);
        if (s && s.getBoundingClientRect().top < window.innerHeight * 0.35) current = i;
      });
      setAt(current);
    };
    const onScroll = () => { if (!raf) raf = requestAnimationFrame(update); };
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => { window.removeEventListener("scroll", onScroll); window.removeEventListener("resize", onScroll); cancelAnimationFrame(raf); };
  }, [headings]);

  return (
    <>
      <div className="gd-progress" aria-hidden style={{ transform: `scaleX(${progress})` }} />
      <nav className="gd-toc" aria-label="On this page">
        <p>On this page</p>
        <ol>
          {headings.map((h, i) => (
            <li key={h} className={i === at ? "is-here" : i < at ? "is-read" : ""}>
              <a href={`#s${i + 1}`}><span>{i + 1}</span>{h}</a>
            </li>
          ))}
        </ol>
      </nav>
    </>
  );
}
