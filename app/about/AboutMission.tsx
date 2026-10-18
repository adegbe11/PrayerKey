"use client";
import { useEffect, useRef } from "react";

// The mission, read the way you would read it aloud: each line lights as it
// reaches the middle of the screen. Without JavaScript, or with reduced
// motion, every line is simply lit.
export default function AboutMission({ lines }: { lines: string[] }) {
  const el = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    const root = el.current;
    if (!root || window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const spans = [...root.querySelectorAll<HTMLElement>("span")];
    let raf = 0;
    const update = () => {
      raf = 0;
      const mark = window.innerHeight * 0.66;
      spans.forEach((s) => { s.dataset.lit = s.getBoundingClientRect().top < mark ? "1" : "0"; });
    };
    const onScroll = () => { if (!raf) raf = requestAnimationFrame(update); };
    root.classList.add("is-live");
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("resize", onScroll);
      cancelAnimationFrame(raf);
      root.classList.remove("is-live");
    };
  }, []);

  return (
    <p ref={el} className="ab-mission__text">
      {lines.map((l, i) => <span key={i}>{l} </span>)}
    </p>
  );
}
