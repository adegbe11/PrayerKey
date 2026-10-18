"use client";
import dynamic from "next/dynamic";
import { useEffect, useMemo, useRef, useState } from "react";
import { composePrayer } from "@/lib/prayer-engine/compose";
import "./pray.css";

const PrayerCardModal = dynamic(() => import("@/components/ui/PrayerCardModal"), { ssr: false });

const MOODS = ["Grateful", "Anxious", "Sad", "Hopeful", "Confused", "Joyful", "Sick", "Tired"];

// what the empty page suggests, one at a time
const EXAMPLES = [
  "My mother goes into surgery on Friday.",
  "I start a new job on Monday and I am nervous.",
  "Thank You for bringing my son home safely.",
  "I can't sleep. My mind won't stop racing.",
  "We have been praying for a child for three years.",
  "Rent is due and I don't know how we'll pay it.",
];

// "a", "a and b", "a, b and c"
function listOf(items: string[]) {
  return items.length < 2 ? items.join("") : `${items.slice(0, -1).join(", ")} and ${items[items.length - 1]}`;
}

type Prayer = { title: string; prayer: string; encouragement: string; verses: { ref: string; text: string }[] };
type Stage = "idle" | "rising" | "writing" | "done";

// a few dozen sparks for the moment the words are carried up
const SPARKS = Array.from({ length: 34 }, (_, i) => ({
  x: (i * 37) % 100,
  y: 30 + ((i * 53) % 60),
  dx: ((i * 29) % 60) - 30,
  d: (i % 9) * 0.06,
  s: 2 + (i % 4),
}));

export default function PrayPage() {
  const [input, setInput] = useState("");
  const [moods, setMoods] = useState<string[]>([]);
  const [prayer, setPrayer] = useState<Prayer | null>(null);
  const [stage, setStage] = useState<Stage>("idle");
  const [lifted, setLifted] = useState("");
  const [example, setExample] = useState(0);
  const [showCard, setShowCard] = useState(false);
  const [copied, setCopied] = useState(false);
  const [speaking, setSpeaking] = useState(false);
  const [canSpeak, setCanSpeak] = useState(false);
  const page = useRef<HTMLDivElement>(null);
  const field = useRef<HTMLTextAreaElement>(null);

  // the suggestion under the cursor changes every few seconds while the page is empty
  useEffect(() => {
    if (input) return;
    const t = setInterval(() => setExample((e) => (e + 1) % EXAMPLES.length), 3600);
    return () => clearInterval(t);
  }, [input]);

  useEffect(() => {
    setCanSpeak("speechSynthesis" in window);
    return () => { if ("speechSynthesis" in window) window.speechSynthesis.cancel(); };
  }, []);

  // links from elsewhere arrive as /pray?topic=...; pray it straight away
  useEffect(() => {
    const topic = new URLSearchParams(window.location.search).get("topic")?.trim().slice(0, 500);
    if (topic) { setInput(topic); pray(topic); }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function pray(text: string = input) {
    const words = text.trim();
    if (!words || stage === "rising") return;
    window.speechSynthesis?.cancel();
    setSpeaking(false);
    // composed on the device from prayer patterns built on Scripture; nothing leaves the page
    const moodText = moods.length ? ` I am feeling ${moods.join(" and ").toLowerCase()}.` : "";
    const result = composePrayer(words + moodText);
    const next: Prayer = {
      title: `A prayer for ${listOf(result.topics.map((t) => t.toLowerCase()))}`,
      prayer: result.prayer + "\n\nAmen.",
      encouragement: "",
      verses: result.verses,
    };
    const still = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    setLifted(words);
    setPrayer(null);
    setStage(still ? "writing" : "rising");
    // the words rise, and then the prayer is written
    setTimeout(() => {
      setPrayer(next);
      setStage("writing");
      requestAnimationFrame(() => page.current?.scrollIntoView({ behavior: "smooth", block: "start" }));
    }, still ? 0 : 1300);
  }

  const paragraphs = useMemo(() => {
    if (!prayer) return [];
    let n = 0;
    return prayer.prayer.split(/\n\n+/).map((p) => p.split(/\s+/).map((w) => ({ w, i: n++ })));
  }, [prayer]);
  const total = paragraphs.reduce((a, p) => a + p.length, 0);
  // the whole prayer is written in about six seconds, however long it is
  const step = total ? Math.min(55, 6000 / total) : 0;

  useEffect(() => {
    if (stage !== "writing" || !total) return;
    const t = setTimeout(() => setStage("done"), total * step + 700);
    return () => clearTimeout(t);
  }, [stage, total, step]);

  function listen() {
    if (!prayer) return;
    const synth = window.speechSynthesis;
    if (speaking) { synth.cancel(); setSpeaking(false); return; }
    synth.cancel();
    const u = new SpeechSynthesisUtterance(prayer.prayer.replace(/\n+/g, " "));
    const voices = synth.getVoices().filter((v) => v.lang.startsWith("en"));
    const v = voices.find((x) => /Google UK English Female|Samantha|Serena|Daniel|Google US English/.test(x.name)) ?? voices[0];
    if (v) u.voice = v;
    u.rate = 0.92;
    u.onend = () => setSpeaking(false);
    u.onerror = () => setSpeaking(false);
    setSpeaking(true);
    synth.speak(u);
  }

  async function copy() {
    if (!prayer) return;
    try {
      await navigator.clipboard.writeText(`${prayer.prayer}\n\n${prayer.verses.map((v) => `${v.text} ${v.ref}`).join("\n")}\n\nprayerkey.com/pray`);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch { /* clipboard refused */ }
  }

  function again() {
    window.speechSynthesis?.cancel();
    setSpeaking(false);
    setPrayer(null);
    setStage("idle");
    setInput("");
    setMoods([]);
    window.scrollTo({ top: 0, behavior: "smooth" });
    setTimeout(() => field.current?.focus(), 500);
  }

  return (
    <div className="py">
      <section className="py-stage pk-bleed" data-nav="dark">
        <div className="py-glow" aria-hidden />

        <div className={`py-ask ${stage === "rising" ? "is-rising" : ""}`}>
          <h1 className="py-title">What&apos;s on your heart?</h1>

          <div className="py-field">
            <label htmlFor="py-q" className="sr-only">What do you want to pray about?</label>
            <textarea
              id="py-q"
              ref={field}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => { if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) pray(); }}
              rows={3}
              maxLength={500}
            />
            {!input && (
              <span key={example} className="py-example" aria-hidden>{EXAMPLES[example]}</span>
            )}
            {/* the words, carried up */}
            {stage === "rising" && (
              <div className="py-lift" aria-hidden>
                <p>{lifted}</p>
                {SPARKS.map((s, i) => (
                  <i key={i} style={{ "--x": `${s.x}%`, "--y": `${s.y}%`, "--dx": `${s.dx}px`, "--d": `${s.d}s`, "--s": `${s.s}px` } as React.CSSProperties} />
                ))}
              </div>
            )}
          </div>

          <div className="py-moods" role="group" aria-label="How are you feeling">
            {MOODS.map((m) => (
              <button key={m} type="button" aria-pressed={moods.includes(m)} className={moods.includes(m) ? "is-on" : ""}
                onClick={() => setMoods((p) => (p.includes(m) ? p.filter((x) => x !== m) : [...p, m]))}>
                {m}
              </button>
            ))}
          </div>

          <button type="button" className="py-go" onClick={() => pray()} disabled={!input.trim() || stage === "rising"}>
            Pray
          </button>
        </div>
      </section>

      {/* the prayer, written onto the page */}
      {prayer && (
        <section ref={page} className="py-result" aria-live="polite">
          <article className={`py-page ${stage === "done" ? "is-done" : ""}`}>
            <p className="py-page__kicker">{prayer.title}</p>
            <div className="py-words">
              {paragraphs.map((p, k) => (
                <p key={k}>
                  {p.map(({ w, i }) => (
                    <span key={i} style={{ animationDelay: `${(i * step) / 1000}s` }}>{w} </span>
                  ))}
                </p>
              ))}
            </div>

            {prayer.verses.length > 0 && (
              <ul className="py-verses" style={{ "--after": `${(total * step) / 1000 + 0.2}s` } as React.CSSProperties}>
                {prayer.verses.map((v, i) => (
                  <li key={v.ref} style={{ "--i": i } as React.CSSProperties}>
                    <p>{v.text}</p>
                    <span>{v.ref}</span>
                  </li>
                ))}
              </ul>
            )}
          </article>

          <div className={`py-actions ${stage === "done" ? "is-on" : ""}`}>
            {canSpeak && (
              <button type="button" onClick={listen} className="py-actions__main" aria-pressed={speaking}>
                {speaking ? "Pause" : "Listen"}
              </button>
            )}
            <button type="button" onClick={copy}>{copied ? "Copied" : "Copy"}</button>
            <button type="button" onClick={() => setShowCard(true)}>Make a card</button>
            <button type="button" onClick={again}>Pray again</button>
          </div>
        </section>
      )}

      {showCard && prayer && <PrayerCardModal prayer={prayer} onClose={() => setShowCard(false)} />}
    </div>
  );
}
