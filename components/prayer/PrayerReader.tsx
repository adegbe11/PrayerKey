"use client";
import { useEffect, useMemo, useRef, useState } from "react";

// The prayer, set to be read slowly. Each sentence comes up from faint to full
// ink as it reaches the reading line, and Listen reads it aloud, one sentence
// at a time, with the one being spoken lit.
export default function PrayerReader({ text, title, url }: { text: string; title: string; url: string }) {
  const sentences = useMemo(() => text.match(/[^.!?]+[.!?]+["')\]]*\s*|[^.!?]+$/g)?.map((s) => s.trim()).filter(Boolean) ?? [text], [text]);
  const box = useRef<HTMLDivElement>(null);
  const [reading, setReading] = useState(false);
  const [lit, setLit] = useState(sentences.length);
  const [speaking, setSpeaking] = useState(-1);
  const [canSpeak, setCanSpeak] = useState(false);
  const [copied, setCopied] = useState(false);

  // light each sentence as it passes the reading line
  useEffect(() => {
    const el = box.current;
    if (!el || window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const spans = [...el.querySelectorAll<HTMLElement>("[data-s]")];
    let raf = 0;
    const update = () => {
      raf = 0;
      const line = window.innerHeight * 0.7;
      let n = 0;
      spans.forEach((s, i) => { if (s.getBoundingClientRect().top < line) n = i + 1; });
      setLit(n);
    };
    const onScroll = () => { if (!raf) raf = requestAnimationFrame(update); };
    setReading(true);
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => { window.removeEventListener("scroll", onScroll); window.removeEventListener("resize", onScroll); cancelAnimationFrame(raf); };
  }, [sentences]);

  useEffect(() => {
    setCanSpeak(typeof window !== "undefined" && "speechSynthesis" in window);
    return () => { if ("speechSynthesis" in window) window.speechSynthesis.cancel(); };
  }, []);

  const voice = () => {
    const voices = window.speechSynthesis.getVoices().filter((v) => v.lang.startsWith("en"));
    return voices.find((v) => /Google UK English Female|Samantha|Serena|Daniel|Google US English/.test(v.name)) ?? voices[0];
  };

  const listen = () => {
    const synth = window.speechSynthesis;
    if (speaking >= 0) { synth.cancel(); setSpeaking(-1); return; }
    synth.cancel();
    const say = (i: number) => {
      if (i >= sentences.length) { setSpeaking(-1); return; }
      const u = new SpeechSynthesisUtterance(sentences[i]);
      const v = voice();
      if (v) u.voice = v;
      u.rate = 0.92;
      u.pitch = 1;
      u.onstart = () => {
        setSpeaking(i);
        const s = box.current?.querySelector<HTMLElement>(`[data-s="${i}"]`);
        const r = s?.getBoundingClientRect();
        if (s && r && (r.top < 90 || r.bottom > window.innerHeight - 120)) s.scrollIntoView({ behavior: "smooth", block: "center" });
      };
      u.onend = () => say(i + 1);
      u.onerror = () => setSpeaking(-1);
      synth.speak(u);
    };
    say(0);
  };

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(`${title}\n\n${text}\n\n${url}`);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch { /* clipboard refused */ }
  };

  return (
    <div className="pp-reader">
      <div className="pp-tools">
        {canSpeak && (
          <button type="button" onClick={listen} className={speaking >= 0 ? "is-on" : ""} aria-pressed={speaking >= 0}>
            {speaking >= 0 ? (
              <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden fill="currentColor"><rect x="6" y="5" width="4" height="14" rx="1.2" /><rect x="14" y="5" width="4" height="14" rx="1.2" /></svg>
            ) : (
              <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden fill="currentColor"><path d="M8 5.5v13a1 1 0 0 0 1.5.86l10.6-6.5a1 1 0 0 0 0-1.72L9.5 4.64A1 1 0 0 0 8 5.5z" /></svg>
            )}
            {speaking >= 0 ? "Pause" : "Listen"}
          </button>
        )}
        <button type="button" onClick={copy}>
          <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="9" y="9" width="11" height="11" rx="2.5" /><path d="M5 15V6a2 2 0 0 1 2-2h9" /></svg>
          {copied ? "Copied" : "Copy"}
        </button>
      </div>
      <div ref={box} className={`pp-prayer ${reading ? "is-reading" : ""}`}>
        {sentences.map((s, i) => (
          <span key={i} data-s={i} className={`${i < lit ? "is-lit" : ""} ${i === speaking ? "is-spoken" : ""}`}>{s} </span>
        ))}
      </div>
    </div>
  );
}
