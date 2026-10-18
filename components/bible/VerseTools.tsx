"use client";
import Link from "next/link";
import { useEffect, useState } from "react";

// Listen, copy, and pray the verse.
export default function VerseTools({ text, display, url }: { text: string; display: string; url: string }) {
  const [canSpeak, setCanSpeak] = useState(false);
  const [speaking, setSpeaking] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    setCanSpeak("speechSynthesis" in window);
    return () => { if ("speechSynthesis" in window) window.speechSynthesis.cancel(); };
  }, []);

  const listen = () => {
    const synth = window.speechSynthesis;
    if (speaking) { synth.cancel(); setSpeaking(false); return; }
    synth.cancel();
    const u = new SpeechSynthesisUtterance(`${text} ${display}.`);
    const voices = synth.getVoices().filter((v) => v.lang.startsWith("en"));
    const v = voices.find((x) => /Google UK English Male|Daniel|Google UK English Female|Samantha/.test(x.name)) ?? voices[0];
    if (v) u.voice = v;
    u.rate = 0.88;
    u.onend = () => setSpeaking(false);
    u.onerror = () => setSpeaking(false);
    setSpeaking(true);
    synth.speak(u);
  };

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(`${text}\n${display} (KJV)\n\n${url}`);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch { /* clipboard refused */ }
  };

  return (
    <div className="vs-tools">
      {canSpeak && <button type="button" onClick={listen} aria-pressed={speaking}>{speaking ? "Pause" : "Listen"}</button>}
      <button type="button" onClick={copy}>{copied ? "Copied" : "Copy"}</button>
      <Link href={`/pray?topic=${encodeURIComponent(`${text} (${display})`)}`} className="vs-tools__pray">Pray this verse</Link>
    </div>
  );
}
