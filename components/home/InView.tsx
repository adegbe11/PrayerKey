"use client";
import { useEffect } from "react";

// Marks each [data-in] element on the home page as it comes into view, once,
// so its entrance plays when the reader actually arrives. Until this runs
// (or without JavaScript) everything is simply shown where it belongs.
export default function InView() {
  useEffect(() => {
    const root = document.documentElement;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const els = [...document.querySelectorAll<HTMLElement>("[data-in]")];
    // anything already on screen stays as it is; only what is below waits for the reader
    const below = els.filter((el) => el.getBoundingClientRect().top > window.innerHeight * 0.9);
    below.forEach((el) => (el.dataset.in = "0"));
    root.classList.add("hs-js");
    const io = new IntersectionObserver(
      (entries) => entries.forEach((e) => {
        if (!e.isIntersecting) return;
        (e.target as HTMLElement).dataset.in = "1";
        io.unobserve(e.target);
      }),
      { rootMargin: "0px 0px -18% 0px", threshold: 0 },
    );
    below.forEach((el) => io.observe(el));
    return () => { io.disconnect(); root.classList.remove("hs-js"); els.forEach((el) => (el.dataset.in = "1")); };
  }, []);
  return null;
}
