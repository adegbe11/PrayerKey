"use client";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import PlayBadge from "@/components/app/PlayBadge";

// The end of every page: the page lifts away like a curtain and leaves a night
// sky. Tap the key and lanterns rise from it, one prayer each; hold it and they
// keep going up. Everything is drawn on one canvas and only runs while the sky
// can be seen.

const LINKS = [
  { href: "/download", label: "Get the App" },
  { href: "/about", label: "About" },
  { href: "/author/collins-asein", label: "Author" },
  { href: "/prayer", label: "All Prayers" },
  { href: "/verse-of-the-day", label: "Verse of the Day" },
  { href: "/guides", label: "Guides" },
  { href: "/bible", label: "Bible" },
  { href: "/support", label: "Support" },
  { href: "/docs", label: "Docs" },
  { href: "/terms", label: "Terms" },
  { href: "/privacy", label: "Privacy" },
];

const ROWS = 7;
const PHRASE = "PRAY WITHOUT CEASING";

type Lantern = { x: number; y: number; vx: number; vy: number; s: number; rot: number; sway: number; age: number; z: number; rise: number };
type Spark = { x: number; y: number; vx: number; vy: number; age: number; life: number; r: number };
type Ring = { x: number; y: number; age: number };
type Star = { x: number; y: number; r: number; tw: number; z: number };

// a small bell, made of three partials, climbing a pentatonic scale tap by tap
const SCALE = [523.25, 587.33, 659.25, 783.99, 880, 1046.5, 1174.66, 1318.51];
function ring(ctx: AudioContext, step: number) {
  const f = SCALE[step % SCALE.length];
  const t = ctx.currentTime;
  const out = ctx.createGain();
  out.gain.value = 0.14;
  out.connect(ctx.destination);
  for (const [ratio, level, decay] of [[1, 1, 2.4], [2.76, 0.32, 1.1], [5.4, 0.12, 0.5]] as const) {
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = "sine";
    o.frequency.value = f * ratio;
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(level, t + 0.008);
    g.gain.exponentialRampToValueAtTime(0.0001, t + decay);
    o.connect(g).connect(out);
    o.start(t);
    o.stop(t + decay + 0.05);
  }
}

export default function SkyFooter() {
  const footer = useRef<HTMLElement>(null);
  const stage = useRef<HTMLDivElement>(null);
  const canvas = useRef<HTMLCanvasElement>(null);
  const key = useRef<HTMLButtonElement>(null);
  const engine = useRef<{ burst: (n: number) => void } | null>(null);
  const audio = useRef<AudioContext | null>(null);
  const step = useRef(0);
  const holding = useRef<number | null>(null);
  const [sound, setSound] = useState(false);
  const [tapped, setTapped] = useState(false);
  const [lifted, setLifted] = useState(0);

  // the curtain: how far the page has lifted off the sky, 0 to 1
  useEffect(() => {
    const el = footer.current;
    const sheet = el?.previousElementSibling as HTMLElement | null;
    if (!el || !sheet) return;
    let frame = 0;
    const update = () => {
      frame = 0;
      const vh = window.innerHeight;
      const p = Math.min(1, Math.max(0, 1 - sheet.getBoundingClientRect().bottom / vh));
      el.style.setProperty("--lift", p.toFixed(4));
    };
    const onScroll = () => { if (!frame) frame = requestAnimationFrame(update); };
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => { window.removeEventListener("scroll", onScroll); window.removeEventListener("resize", onScroll); cancelAnimationFrame(frame); };
  }, []);

  // the sky itself
  useEffect(() => {
    const cv = canvas.current, st = stage.current;
    if (!cv || !st) return;
    const ctx = cv.getContext("2d");
    if (!ctx) return;
    const still = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    let w = 0, h = 0, dpr = 1;
    let stars: Star[] = [];
    const lanterns: Lantern[] = [];
    const sparks: Spark[] = [];
    const rings: Ring[] = [];
    const pointer = { x: -999, y: -999, px: 0, py: 0, tx: 0, ty: 0 };
    let raf = 0, visible = false, last = 0, ambient = 0, greeted = false;

    const size = () => {
      const r = st.getBoundingClientRect();
      dpr = Math.min(window.devicePixelRatio || 1, 2);
      w = r.width; h = r.height;
      cv.width = Math.round(w * dpr); cv.height = Math.round(h * dpr);
      cv.style.width = `${w}px`; cv.style.height = `${h}px`;
      const count = Math.round((w * h) / 9000);
      stars = Array.from({ length: Math.min(count, 220) }, () => ({ x: Math.random() * w, y: Math.random() * h, r: Math.random() * 1.2 + 0.25, tw: Math.random() * Math.PI * 2, z: Math.random() * 0.8 + 0.2 }));
      if (!raf) draw(performance.now());
    };

    const origin = () => {
      const k = key.current?.getBoundingClientRect(), s = st.getBoundingClientRect();
      if (!k) return { x: w / 2, y: h / 2 };
      return { x: k.left - s.left + k.width / 2, y: k.top - s.top + k.height * 0.42 };
    };

    const launch = (x: number, y: number, power: number, z = Math.random() * 0.6 + 0.55) => {
      const a = -Math.PI / 2 + (Math.random() - 0.5) * 2.5;
      const v = (Math.random() * 7 + 6) * power;
      lanterns.push({ x, y, vx: Math.cos(a) * v, vy: Math.sin(a) * v, s: (Math.random() * 9 + 15) * z, rot: 0, sway: Math.random() * Math.PI * 2, age: 0, z, rise: -(Math.random() * 0.8 + 1.0) * z });
      if (lanterns.length > 140) lanterns.splice(0, lanterns.length - 140);
    };

    const burst = (n: number) => {
      const o = origin();
      for (let i = 0; i < n; i++) launch(o.x, o.y, still ? 0.4 : 1);
      if (!still) {
        rings.push({ x: o.x, y: o.y, age: 0 });
        for (let i = 0; i < 26; i++) {
          const a = Math.random() * Math.PI * 2, v = Math.random() * 5 + 1.5;
          sparks.push({ x: o.x, y: o.y, vx: Math.cos(a) * v, vy: Math.sin(a) * v - 1.5, age: 0, life: Math.random() * 40 + 35, r: Math.random() * 1.6 + 0.6 });
        }
      }
      if (!raf && visible) raf = requestAnimationFrame(draw);
    };
    engine.current = { burst };

    const lantern = (l: Lantern, alpha: number, t: number) => {
      const flick = 0.82 + Math.sin(t * 0.018 + l.sway * 3) * 0.08 + Math.random() * 0.1;
      const s = l.s;
      ctx.save();
      ctx.translate(l.x, l.y);
      ctx.rotate(l.rot);
      // the warm light it throws on the sky and the words behind it
      ctx.globalCompositeOperation = "lighter";
      const glow = ctx.createRadialGradient(0, s * 0.25, 0, 0, s * 0.25, s * 3.4);
      glow.addColorStop(0, `rgba(255,176,72,${0.34 * alpha * flick})`);
      glow.addColorStop(0.4, `rgba(255,120,40,${0.1 * alpha * flick})`);
      glow.addColorStop(1, "rgba(255,90,20,0)");
      ctx.fillStyle = glow;
      ctx.beginPath(); ctx.arc(0, s * 0.25, s * 3.4, 0, Math.PI * 2); ctx.fill();
      ctx.globalCompositeOperation = "source-over";
      // the paper body: wider at the top, lit from the flame below
      const top = -s * 0.75, bot = s * 0.75, tw = s * 0.62, bw = s * 0.42;
      const paper = ctx.createLinearGradient(0, top, 0, bot);
      paper.addColorStop(0, `rgba(196,74,22,${alpha})`);
      paper.addColorStop(0.45, `rgba(246,148,52,${alpha})`);
      paper.addColorStop(1, `rgba(255,226,150,${alpha})`);
      ctx.fillStyle = paper;
      ctx.beginPath();
      ctx.moveTo(-tw, top + s * 0.08);
      ctx.quadraticCurveTo(0, top - s * 0.14, tw, top + s * 0.08);
      ctx.lineTo(bw, bot);
      ctx.quadraticCurveTo(0, bot + s * 0.08, -bw, bot);
      ctx.closePath();
      ctx.fill();
      // ribs in the paper
      ctx.strokeStyle = `rgba(150,50,10,${0.28 * alpha})`;
      ctx.lineWidth = Math.max(0.6, s * 0.04);
      ctx.beginPath(); ctx.moveTo(-tw * 0.34, top + s * 0.02); ctx.lineTo(-bw * 0.34, bot); ctx.moveTo(tw * 0.34, top + s * 0.02); ctx.lineTo(bw * 0.34, bot); ctx.stroke();
      // the flame
      ctx.globalCompositeOperation = "lighter";
      const fl = ctx.createRadialGradient(0, bot - s * 0.18, 0, 0, bot - s * 0.18, s * 0.5);
      fl.addColorStop(0, `rgba(255,250,220,${alpha * flick})`);
      fl.addColorStop(0.5, `rgba(255,200,90,${0.55 * alpha * flick})`);
      fl.addColorStop(1, "rgba(255,140,40,0)");
      ctx.fillStyle = fl;
      ctx.beginPath(); ctx.arc(0, bot - s * 0.18, s * 0.5, 0, Math.PI * 2); ctx.fill();
      ctx.restore();
    };

    const draw = (t: number) => {
      raf = 0;
      const dt = Math.min(2.5, last ? (t - last) / 16.67 : 1);
      last = t;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      ctx.clearRect(0, 0, w, h);

      // stars lean a little toward the pointer, deeper ones further
      pointer.px += (pointer.tx - pointer.px) * 0.05 * dt;
      pointer.py += (pointer.ty - pointer.py) * 0.05 * dt;
      for (const s of stars) {
        const a = still ? 0.6 : 0.35 + Math.sin(t * 0.0016 * (0.6 + s.z) + s.tw) * 0.3;
        ctx.fillStyle = `rgba(255,244,220,${Math.max(0.05, a) * s.z})`;
        ctx.beginPath();
        ctx.arc(s.x + pointer.px * s.z * 14, s.y + pointer.py * s.z * 10, s.r, 0, Math.PI * 2);
        ctx.fill();
      }

      // a lantern from far away now and then, so the sky is never empty
      if (!still && visible && t - ambient > 2600) {
        ambient = t;
        const z = Math.random() * 0.3 + 0.3;
        lanterns.push({ x: Math.random() * w, y: h + 30, vx: 0, vy: -0.4, s: (Math.random() * 6 + 12) * z, rot: 0, sway: Math.random() * 6, age: 0, z, rise: -(Math.random() * 0.3 + 0.35) });
      }

      for (const r of rings) {
        r.age += dt;
        const k = r.age / 50;
        ctx.strokeStyle = `rgba(255,214,140,${Math.max(0, 0.55 * (1 - k))})`;
        ctx.lineWidth = 2.2 * (1 - k) + 0.3;
        ctx.beginPath(); ctx.arc(r.x, r.y, 30 + k * Math.min(w, 520) * 0.5, 0, Math.PI * 2); ctx.stroke();
      }
      for (let i = rings.length - 1; i >= 0; i--) if (rings[i].age > 50) rings.splice(i, 1);

      ctx.globalCompositeOperation = "lighter";
      for (const p of sparks) {
        p.age += dt; p.vx *= 0.96; p.vy = p.vy * 0.96 + 0.06 * dt;
        p.x += p.vx * dt; p.y += p.vy * dt;
        const a = Math.max(0, 1 - p.age / p.life);
        ctx.fillStyle = `rgba(255,214,140,${a})`;
        ctx.beginPath(); ctx.arc(p.x, p.y, p.r * (0.6 + a * 0.6), 0, Math.PI * 2); ctx.fill();
      }
      ctx.globalCompositeOperation = "source-over";
      for (let i = sparks.length - 1; i >= 0; i--) if (sparks[i].age > sparks[i].life) sparks.splice(i, 1);

      // nearer lanterns are drawn last, over the far ones
      lanterns.sort((a, b) => a.z - b.z);
      for (const l of lanterns) {
        l.age += dt;
        l.sway += 0.02 * dt;
        // thrown out fast, then the warm air takes over and carries it up
        l.vx = l.vx * Math.pow(0.955, dt) + Math.sin(l.sway) * 0.012 * dt;
        l.vy = l.vy * Math.pow(0.955, dt) + (l.rise - l.vy) * 0.025 * dt;
        // the pointer moves them aside like a hand through warm air
        const dx = l.x - pointer.x, dy = l.y - pointer.y, d2 = dx * dx + dy * dy;
        if (d2 < 14400 && d2 > 1) { const f = (1 - Math.sqrt(d2) / 120) * 0.35 * dt; l.vx += (dx / Math.sqrt(d2)) * f; l.vy += (dy / Math.sqrt(d2)) * f * 0.5; }
        l.x += l.vx * dt; l.y += l.vy * dt;
        l.rot += ((l.vx * 0.05 + Math.sin(l.sway) * 0.06) - l.rot) * 0.08 * dt;
        const fadeIn = Math.min(1, l.age / 8);
        const fadeOut = Math.min(1, Math.max(0, (l.y + 40) / (h * 0.32)));
        lantern(l, fadeIn * fadeOut * (0.55 + l.z * 0.45), t);
      }
      for (let i = lanterns.length - 1; i >= 0; i--) if (lanterns[i].y < -60) lanterns.splice(i, 1);

      if (visible && (!still || lanterns.length || sparks.length || rings.length)) raf = requestAnimationFrame(draw);
    };

    const move = (e: PointerEvent) => {
      const r = st.getBoundingClientRect();
      pointer.x = e.clientX - r.left; pointer.y = e.clientY - r.top;
      pointer.tx = (pointer.x / w - 0.5) * 2; pointer.ty = (pointer.y / h - 0.5) * 2;
      if (!still && e.pointerType === "mouse" && Math.random() < 0.35) sparks.push({ x: pointer.x, y: pointer.y, vx: (Math.random() - 0.5) * 0.8, vy: -Math.random() * 0.8, age: 0, life: 40, r: Math.random() * 1.1 + 0.4 });
    };
    const leave = () => { pointer.x = -999; pointer.y = -999; pointer.tx = 0; pointer.ty = 0; };
    st.addEventListener("pointermove", move);
    st.addEventListener("pointerleave", leave);

    const io = new IntersectionObserver(([e]) => {
      visible = e.isIntersecting;
      if (visible && !raf) { last = 0; raf = requestAnimationFrame(draw); }
      // the first time the sky opens, a few prayers are already on their way up
      if (visible && e.intersectionRatio > 0.5 && !greeted) { greeted = true; setTimeout(() => burst(3), 500); }
    }, { threshold: [0, 0.5] });
    io.observe(st);
    const ro = new ResizeObserver(size);
    ro.observe(st);
    size();

    return () => { cancelAnimationFrame(raf); io.disconnect(); ro.disconnect(); st.removeEventListener("pointermove", move); st.removeEventListener("pointerleave", leave); engine.current = null; };
  }, []);

  const send = (n: number) => {
    engine.current?.burst(n);
    setTapped(true);
    setLifted((c) => c + n);
    if (sound && audio.current) ring(audio.current, step.current++);
    // the key turns in the lock
    key.current?.animate(
      [{ transform: "translateY(0) rotateY(0deg) scale(1)" }, { transform: "translateY(4px) rotateY(0deg) scale(0.9)", offset: 0.15 }, { transform: "translateY(-10px) rotateY(180deg) scale(1.06)", offset: 0.6 }, { transform: "translateY(0) rotateY(360deg) scale(1)" }],
      { duration: 820, easing: "cubic-bezier(.3,1.4,.5,1)" },
    );
    if ("vibrate" in navigator) navigator.vibrate?.(8);
  };
  const press = (e: React.PointerEvent) => {
    if (e.button !== 0) return;
    send(6);
    // hold the key and the prayers keep rising
    holding.current = window.setInterval(() => { engine.current?.burst(2); setLifted((c) => c + 2); if (sound && audio.current) ring(audio.current, step.current++); }, 260);
  };
  const release = () => { if (holding.current) { clearInterval(holding.current); holding.current = null; } };
  useEffect(() => release, []);

  const toggleSound = () => {
    if (!audio.current) {
      const Ctx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (Ctx) audio.current = new Ctx();
    }
    audio.current?.resume();
    setSound((s) => {
      if (!s && audio.current) ring(audio.current, step.current++);
      return !s;
    });
  };

  const row = Array.from({ length: 4 }, () => PHRASE).join("  ·  ");

  return (
    <footer ref={footer} className="sky" data-nav="dark">
      <div ref={stage} className="sky__stage">
        <div className="sky__words" aria-hidden>
          {Array.from({ length: ROWS }, (_, i) => (
            <div key={i} className={`sky__row ${i % 2 ? "sky__row--back" : ""} ${i === 3 ? "sky__row--lit" : ""}`} style={{ "--i": i } as React.CSSProperties}>
              <span>{row}  ·  </span><span>{row}  ·  </span>
            </div>
          ))}
        </div>
        <canvas ref={canvas} className="sky__canvas" aria-hidden />

        <div className="sky__center">
          <button
            ref={key}
            type="button"
            className="sky__key"
            aria-label="Send up a prayer"
            onPointerDown={press}
            onPointerUp={release}
            onPointerLeave={release}
            onPointerCancel={release}
            onClick={(e) => { if (e.detail === 0) send(6); }}
            onContextMenu={(e) => e.preventDefault()}
          >
            <span className="sky__halo" aria-hidden />
            <img src="/pk-mark.png" alt="" width={146} height={256} draggable={false} />
          </button>
          <span className={`sky__hint ${tapped ? "is-gone" : ""}`} aria-hidden>tap the key</span>
          <p className="sky__verse">
            <span className="sky__count" aria-live="polite">{lifted ? `${lifted} ${lifted === 1 ? "prayer" : "prayers"} lifted` : "Pray without ceasing."}</span>
            <span className="sky__ref">{lifted ? "Pray without ceasing. 1 Thessalonians 5:17" : "1 Thessalonians 5:17"}</span>
          </p>
        </div>

        <button type="button" className={`sky__sound ${sound ? "is-on" : ""}`} onClick={toggleSound} aria-pressed={sound} aria-label={sound ? "Turn sound off" : "Turn sound on"}>
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M4 9.5v5h3.5L12 19V5L7.5 9.5H4z" fill="currentColor" stroke="none" />
            {sound ? <><path d="M15.5 9a4 4 0 0 1 0 6" /><path d="M18 6.5a7.5 7.5 0 0 1 0 11" /></> : <path d="M16 9.5l5 5m0-5l-5 5" />}
          </svg>
        </button>
      </div>

      <div className="sky__get">
        <div className="sky__panel">
          <div>
            <h2 className="sky__title">Pray every day.</h2>
            <p className="sky__sub">Free on Android. Coming soon to iPhone.</p>
          </div>
          <PlayBadge source="footer" size="lg" />
        </div>
        <div className="sky__qr" title="Scan to get PrayerKey">
          <img src="/app/qr-play.svg" alt="QR code for PrayerKey on Google Play" width={96} height={96} />
        </div>
      </div>

      <nav className="sky__links" aria-label="Footer">
        <span className="sky__copy">{new Date().getFullYear()} © PrayerKey</span>
        <div>{LINKS.map((l) => <Link key={l.href} href={l.href}>{l.label}</Link>)}</div>
      </nav>
    </footer>
  );
}
