"use client";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { animate, motion, useMotionValue, useTransform } from "framer-motion";
import { ArrowRight, Check, Heart, Sparkles, X } from "lucide-react";

/* ───────────── The black Bible: the phone's cover, opening and turning its pages ───────────── */

// King James Version, public domain — the same text the app opens to.
const PAGES = [
  { ref: "Genesis 1:1", n: "1", text: "In the beginning God created the heaven and the earth." },
  { ref: "Genesis 1:3", n: "3", text: "And God said, Let there be light: and there was light." },
  { ref: "Psalm 23:1", n: "1", text: "The LORD is my shepherd; I shall not want." },
  { ref: "Psalm 23:4", n: "4", text: "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me." },
  { ref: "Matthew 6:9", n: "9", text: "After this manner therefore pray ye: Our Father which art in heaven, Hallowed be thy name." },
  { ref: "Matthew 11:28", n: "28", text: "Come unto me, all ye that labour and are heavy laden, and I will give you rest." },
  { ref: "John 3:16", n: "16", text: "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life." },
  { ref: "Philippians 4:6", n: "6", text: "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God." },
  { ref: "Revelation 21:4", n: "4", text: "And God shall wipe away all tears from their eyes; and there shall be no more death, neither sorrow, nor crying, neither shall there be any more pain: for the former things are passed away." },
];
const LEAVES = 4;

// Closed, cover opens, four leaves turn (each held long enough to read), then it closes the way it opened.
const STEPS: { open: boolean; turned: number; ms: number }[] = [
  { open: false, turned: 0, ms: 2600 },
  { open: true, turned: 0, ms: 2400 },
  { open: true, turned: 1, ms: 2600 },
  { open: true, turned: 2, ms: 2600 },
  { open: true, turned: 3, ms: 2600 },
  { open: true, turned: 4, ms: 3400 },
  { open: true, turned: 3, ms: 650 },
  { open: true, turned: 2, ms: 650 },
  { open: true, turned: 1, ms: 650 },
  { open: true, turned: 0, ms: 800 },
];

function Page({ i }: { i: number }) {
  const p = PAGES[i];
  return (
    <div className="bb-page">
      <span className="bb-page__ref">{p.ref.split(" ").slice(0, -1).join(" ")} {p.ref.split(" ").slice(-1)[0].split(":")[0]}</span>
      <p className="bb-page__text"><sup>{p.n}</sup> {p.text}</p>
    </div>
  );
}

export function BlackBible() {
  const [step, setStep] = useState(0);
  useEffect(() => {
    const id = setTimeout(() => setStep((s) => (s + 1) % STEPS.length), STEPS[step].ms);
    return () => clearTimeout(id);
  }, [step]);
  const { open, turned } = STEPS[step];

  return (
    <div className="bb-scene" role="img" aria-label="The black Bible opening and its pages turning">
      <div className={`bb-book ${open ? "is-open" : ""}`} onClick={() => setStep((s) => (s + 1) % STEPS.length)}>
        {/* the last page, which stays */}
        <div className="bb-face bb-face--page" style={{ transform: "translateZ(0px)" }}><Page i={LEAVES * 2} /></div>

        {Array.from({ length: LEAVES }, (_, k) => {
          const flipped = open && turned > k;
          const z = flipped ? k + 2 : LEAVES - k + 1;
          return (
            <div key={k} className="bb-leaf" style={{ transform: `translateZ(${z * 1.2}px) rotateY(${flipped ? -180 : 0}deg)` }}>
              <div className="bb-face bb-face--page"><Page i={k * 2} /></div>
              <div className="bb-face bb-face--page bb-face--back"><Page i={k * 2 + 1} /></div>
            </div>
          );
        })}

        {/* the board */}
        <div className="bb-leaf bb-cover" style={{ transform: `translateZ(${(LEAVES + 3) * 1.2}px) rotateY(${open ? -180 : 0}deg)` }}>
          <div className="bb-face bb-face--cover">
            <img className="bb-cross" src="/art/black-bible-cross.webp" alt="" />
            <span className="bb-title">
              <span className="bb-title__the">The</span>
              <span className="bb-title__bible">BIBLE</span>
              <span className="bb-title__kjv">King James Version</span>
            </span>
          </div>
          <div className="bb-face bb-face--back bb-face--inside">
            <span className="bb-inside">PrayerKey</span>
          </div>
        </div>
      </div>
      <span className="bb-shadow" aria-hidden />
    </div>
  );
}

/* ───────────── Pull: ten verses a day, drag each card away ───────────── */

export type PullVerse = { ref: string; text: string; book: string; chapter: number };
const ART = ["/art/dawn-mountain.webp", "/art/cover-moon.webp", "/art/heaven-hands.webp", "/art/morning-prayer.webp", "/art/reflection.webp", "/art/evening-prayer.webp", "/art/open-door.webp", "/art/window-prayer.webp", "/art/cover-candle.webp", "/art/glass-read.webp"];

function PullCard({ verse, art, top, auto, onGone }: { verse: PullVerse; art: string; top: boolean; auto: boolean; onGone: (saved: boolean) => void }) {
  const x = useMotionValue(0);
  const dragging = useRef(false);
  const rotate = useTransform(x, [-220, 220], [-12, 12]);
  const saveOpacity = useTransform(x, [30, 130], [0, 1]);
  const receiveOpacity = useTransform(x, [-130, -30], [1, 0]);
  const fling = (dir: 1 | -1, counts = true) => animate(x, dir * 620, { type: "spring", stiffness: 120, damping: 20, onComplete: () => onGone(counts && dir === 1) });

  // On its own, the top card gets pulled away every few seconds — until someone touches it.
  useEffect(() => {
    if (!top || !auto) return;
    const id = setTimeout(() => {
      if (dragging.current) return;
      // A little lift first, then it slides off.
      animate(x, -46, { duration: 0.45, ease: "easeOut" }).then(() => { if (!dragging.current) fling(-1, false); });
    }, 2800);
    return () => clearTimeout(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [top, auto]);
  return (
    <motion.div
      className="pl-card"
      style={{ x, rotate }}
      drag={top ? "x" : false}
      dragSnapToOrigin
      dragElastic={0.9}
      onDragStart={() => { dragging.current = true; }}
      onDragEnd={(_, info) => {
        dragging.current = false;
        if (info.offset.x > 110 || info.velocity.x > 600) fling(1);
        else if (info.offset.x < -110 || info.velocity.x < -600) fling(-1);
      }}
      data-top={top}
    >
      <img src={art} alt="" draggable={false} />
      <span className="pl-card__shade" />
      <motion.span className="pl-stamp pl-stamp--save" style={{ opacity: saveOpacity }}>Saved ♥</motion.span>
      <motion.span className="pl-stamp pl-stamp--receive" style={{ opacity: receiveOpacity }}>Next ✦</motion.span>
      <span className="pl-card__text">
        <span className="pl-card__verse pk-book">“{verse.text}”</span>
        <span className="pl-card__ref">{verse.ref}</span>
      </span>
    </motion.div>
  );
}

export function PullDeck({ verses }: { verses: PullVerse[] }) {
  const [i, setI] = useState(0);
  const [saved, setSaved] = useState(0);
  const [hover, setHover] = useState(false);
  const done = i >= verses.length;
  const current = verses[Math.min(i, verses.length - 1)];
  const next = (wasSaved: boolean) => { if (wasSaved) setSaved((n) => n + 1); setI((n) => n + 1); };

  // When the last one is gone, it starts over by itself.
  useEffect(() => {
    if (!done) return;
    const id = setTimeout(() => { setI(0); setSaved(0); }, 2600);
    return () => clearTimeout(id);
  }, [done]);

  return (
    <div className="pl">
      <div className="pl-deck" onPointerEnter={() => setHover(true)} onPointerLeave={() => setHover(false)}>
        {done ? (
          <div className="pl-done">
            <Check size={28} />
            <p className="pk-display">That’s all ten for today.</p>
            <p className="pl-done__sub">{saved} saved</p>
            <button type="button" className="pk-capsule pk-capsule--light" onClick={() => { setI(0); setSaved(0); }}>Start again</button>
          </div>
        ) : (
          [2, 1, 0].map((offset) => {
            const idx = i + offset;
            if (idx >= verses.length) return null;
            return (
              <div key={idx} className="pl-slot" style={{ ["--o" as string]: offset }}>
                <PullCard verse={verses[idx]} art={ART[idx % ART.length]} top={offset === 0} auto={!hover} onGone={next} />
              </div>
            );
          })
        )}
      </div>
      <div className="pl-bar">
        <div className="pl-dots" aria-label={`Pull ${Math.min(i + 1, verses.length)} of ${verses.length}`}>
          {verses.map((v, k) => <span key={v.ref} className={k < i ? "is-done" : k === i ? "is-now" : ""} />)}
        </div>
        {!done && (
          <div className="pl-actions">
            <button type="button" className="pl-round" aria-label="Next" onClick={() => next(false)}><X size={20} /></button>
            <button type="button" className="pl-round pl-round--heart" aria-label="Save" onClick={() => next(true)}><Heart size={20} /></button>
            <Link className="pl-round pl-round--pray" aria-label={`Pray ${current.ref}`} href={`/pray?topic=${encodeURIComponent("prayer based on " + current.ref)}`}><Sparkles size={20} /></Link>
          </div>
        )}
      </div>
    </div>
  );
}

