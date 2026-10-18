"use client";
import Link from "next/link";
import { useRef } from "react";

// A guide as a painted cover. It leans toward the pointer, and the light on it
// follows, as if you were tilting a book in your hands.
export default function GuideCover({ href, art, title, minutes, big = false, style }: {
  href: string; art: string; title: string; minutes: number; big?: boolean; style?: React.CSSProperties;
}) {
  const el = useRef<HTMLAnchorElement>(null);
  const move = (e: React.PointerEvent) => {
    if (e.pointerType !== "mouse" || !el.current) return;
    const r = el.current.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width - 0.5, y = (e.clientY - r.top) / r.height - 0.5;
    el.current.style.setProperty("--rx", `${(-y * 8).toFixed(2)}deg`);
    el.current.style.setProperty("--ry", `${(x * 10).toFixed(2)}deg`);
    el.current.style.setProperty("--lx", `${((x + 0.5) * 100).toFixed(1)}%`);
    el.current.style.setProperty("--ly", `${((y + 0.5) * 100).toFixed(1)}%`);
  };
  const leave = () => {
    el.current?.style.setProperty("--rx", "0deg");
    el.current?.style.setProperty("--ry", "0deg");
  };
  return (
    <Link ref={el} href={href} className={`gc ${big ? "gc--big" : ""}`} onPointerMove={move} onPointerLeave={leave} style={style}>
      <img src={art} alt="" loading={big ? "eager" : "lazy"} decoding="async" />
      <span className="gc__shine" aria-hidden />
      <span className="gc__text">
        <span className="gc__kicker">Guide · {minutes} min read</span>
        <span className="gc__title">{title}</span>
      </span>
    </Link>
  );
}
