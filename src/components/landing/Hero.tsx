'use client';

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { motion, useScroll, useTransform } from 'framer-motion';
import { ArrowRight } from 'lucide-react';
import Magnetic from '../Magnetic';
import ShaderBackground from '../ShaderBackground';

// ─────────────────────────────────────────
//  TYPING LAPTOP — animated manuscript that types itself
// ─────────────────────────────────────────
function TypingLaptop() {
  const lines = [
    'Chapter I',
    'The Storm',
    '',
    'The storm had been building',
    'for three days before it',
    'finally broke. Mara stood',
    'at the window of the',
    'lighthouse, watching the',
    'gray Atlantic churn beneath',
    'a bruised sky.',
  ];
  const [typed, setTyped] = useState('');
  const [lineIdx, setLineIdx] = useState(0);
  const [charIdx, setCharIdx] = useState(0);

  useEffect(() => {
    if (lineIdx >= lines.length) {
      const r = setTimeout(() => {
        setTyped('');
        setLineIdx(0);
        setCharIdx(0);
      }, 2600);
      return () => clearTimeout(r);
    }
    const currentLine = lines[lineIdx];
    if (charIdx < currentLine.length) {
      const t = setTimeout(() => {
        setTyped((s) => s + currentLine[charIdx]);
        setCharIdx((i) => i + 1);
      }, 42 + Math.random() * 58);
      return () => clearTimeout(t);
    }
    const next = setTimeout(() => {
      setTyped((s) => s + '\n');
      setLineIdx((i) => i + 1);
      setCharIdx(0);
    }, 260);
    return () => clearTimeout(next);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lineIdx, charIdx]);

  return (
    <motion.div
      animate={{ y: [0, -4, 0], rotate: [-2, -1.6, -2] }}
      transition={{ duration: 5.6, repeat: Infinity, ease: 'easeInOut' }}
      style={{ position: 'relative', width: 240, height: 170 }}
    >
      {/* Screen bezel */}
      <div style={{
        position: 'absolute', inset: '0 0 22px 0',
        background: 'linear-gradient(180deg, #1A1828 0%, #0B0916 100%)',
        borderRadius: '10px 10px 4px 4px',
        border: '2px solid #3E3B52',
        boxShadow: '0 18px 36px rgba(0,0,0,0.55), inset 0 0 0 3px #0A0910',
        padding: '14px 16px 10px',
        overflow: 'hidden',
      }}>
        {/* Fake menu dots */}
        <div style={{ position: 'absolute', top: 6, left: 10, display: 'flex', gap: 4 }}>
          {['#FF5F56', '#FFBD2E', '#27C93F'].map((c) => (
            <div key={c} style={{ width: 6, height: 6, borderRadius: 999, background: c, opacity: 0.8 }} />
          ))}
        </div>
        {/* Manuscript content */}
        <pre style={{
          marginTop: 8,
          fontFamily: 'var(--font-garamond), Georgia, serif',
          fontSize: 9,
          lineHeight: 1.5,
          color: '#F5F0E8',
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
          height: '100%',
          overflow: 'hidden',
        }}>
          {typed}
          <motion.span
            animate={{ opacity: [1, 0, 1] }}
            transition={{ duration: 0.9, repeat: Infinity }}
            style={{
              display: 'inline-block',
              width: 5,
              height: 10,
              background: '#FFE500',
              verticalAlign: '-1px',
              marginLeft: 1,
            }}
          />
        </pre>
      </div>
      {/* Laptop base */}
      <div style={{
        position: 'absolute', bottom: 0, left: -12, right: -12, height: 22,
        background: 'linear-gradient(180deg, #2A2740 0%, #15131F 100%)',
        borderRadius: '4px 4px 16px 16px',
        border: '2px solid #3E3B52',
        borderTop: 'none',
        boxShadow: '0 12px 24px rgba(0,0,0,0.5)',
      }}>
        {/* Keyboard slot */}
        <div style={{
          position: 'absolute', top: 2, left: '50%', transform: 'translateX(-50%)',
          width: '22%', height: 3, borderRadius: 2, background: '#4A4760',
        }} />
      </div>
    </motion.div>
  );
}

// ─────────────────────────────────────────
//  E-READER — Kindle-style device on the foreground
// ─────────────────────────────────────────
function EReader() {
  return (
    <motion.div
      animate={{ y: [0, -6, 0], rotate: [6, 4, 6] }}
      transition={{ duration: 6.8, repeat: Infinity, ease: 'easeInOut', delay: 0.8 }}
      style={{
        position: 'relative',
        width: 120,
        height: 170,
        background: 'linear-gradient(180deg, #2A2740 0%, #15131F 100%)',
        border: '2px solid #3E3B52',
        borderRadius: 14,
        boxShadow: '0 20px 40px rgba(0,0,0,0.55), 4px 4px 0 rgba(255,255,255,0.05)',
        padding: 10,
      }}
    >
      {/* Screen */}
      <div style={{
        background: '#EDE6D6',
        borderRadius: 4,
        width: '100%',
        height: '88%',
        padding: '8px 8px 6px',
        overflow: 'hidden',
        fontFamily: 'var(--font-garamond), Georgia, serif',
        color: '#1C1208',
        position: 'relative',
      }}>
        <div style={{ fontSize: 7, color: '#7D6A55', textAlign: 'center', letterSpacing: '0.1em', marginBottom: 2 }}>
          CHAPTER I
        </div>
        <div style={{ fontSize: 9, textAlign: 'center', fontStyle: 'italic', marginBottom: 6 }}>
          The Storm
        </div>
        <div style={{ height: 1, width: 16, margin: '0 auto 6px', background: '#9D8A75' }} />
        <div style={{ fontSize: 6.5, lineHeight: 1.45, textIndent: '1em' }}>
          The storm had been building for three days before it finally broke. Mara stood at the window, watching the gray Atlantic churn.
        </div>
        {/* Progress line */}
        <div style={{
          position: 'absolute', bottom: 6, left: 8, right: 8, height: 1,
          background: '#C4B9A8',
        }}>
          <motion.div
            animate={{ width: ['14%', '74%', '14%'] }}
            transition={{ duration: 7, repeat: Infinity, ease: 'easeInOut' }}
            style={{ height: '100%', background: '#7C3AED' }}
          />
        </div>
      </div>
      {/* Home button */}
      <div style={{
        width: 18, height: 4, borderRadius: 2,
        background: '#4A4760', margin: '4px auto 0',
      }} />
    </motion.div>
  );
}

// ─────────────────────────────────────────
//  PRINTER — books & pages print out the top
// ─────────────────────────────────────────
function Printer() {
  return (
    <motion.div
      animate={{ y: [0, 2, 0] }}
      transition={{ duration: 7, repeat: Infinity, ease: 'easeInOut' }}
      style={{ position: 'relative', width: 150, height: 170 }}
    >
      {/* Paper sheets shooting up — three staggered */}
      {[0, 1.5, 3.0].map((delay, i) => (
        <motion.div
          key={i}
          initial={{ y: 30, opacity: 0, rotate: -4 }}
          animate={{ y: [-10, -80, -80], opacity: [0, 1, 0], rotate: [-4, 6, 10] }}
          transition={{ duration: 3.5, repeat: Infinity, delay, ease: 'easeOut' }}
          style={{
            position: 'absolute',
            bottom: 100,
            left: `${24 + i * 12}px`,
            width: 60,
            height: 78,
            background: '#FAF5ED',
            border: '1px solid rgba(0,0,0,0.2)',
            boxShadow: '3px 3px 0 rgba(0,0,0,0.1)',
            padding: '6px 5px',
          }}
        >
          {/* Faux lines on the page */}
          {[80, 95, 70, 88, 92, 60].map((w, j) => (
            <div key={j} style={{
              height: 1.5,
              width: `${w}%`,
              background: 'rgba(28,18,8,0.35)',
              marginBottom: 3,
            }} />
          ))}
        </motion.div>
      ))}

      {/* Printer body */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0, height: 110,
        background: 'linear-gradient(180deg, #FFE500 0%, #F5C71E 100%)',
        border: '3px solid #000',
        borderRadius: 6,
        boxShadow: '6px 6px 0 #000',
      }}>
        {/* Paper tray slot */}
        <div style={{
          position: 'absolute', top: -4, left: '18%', right: '18%', height: 6,
          background: '#000',
          borderRadius: 2,
        }} />
        {/* Display */}
        <div style={{
          position: 'absolute', top: 14, left: 12, right: 12, height: 22,
          background: '#1A1828',
          borderRadius: 3,
          display: 'flex', alignItems: 'center', padding: '0 8px',
          fontFamily: 'var(--font-body)', fontSize: 7, letterSpacing: '0.1em',
          color: '#4ADE80', fontWeight: 800,
        }}>
          <motion.span
            animate={{ opacity: [1, 0.3, 1] }}
            transition={{ duration: 1.4, repeat: Infinity }}
          >
            \u2022 PRINTING
          </motion.span>
          <div style={{ flex: 1 }} />
          <span style={{ color: '#FFE500' }}>BOOKSANE</span>
        </div>
        {/* Progress bar */}
        <div style={{
          position: 'absolute', top: 44, left: 12, right: 12, height: 8,
          background: '#000', borderRadius: 2, overflow: 'hidden',
        }}>
          <motion.div
            animate={{ width: ['0%', '100%', '0%'] }}
            transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut' }}
            style={{ height: '100%', background: '#FF2D78' }}
          />
        </div>
        {/* Buttons */}
        <div style={{
          position: 'absolute', top: 62, left: 12, right: 12, display: 'flex', gap: 6,
        }}>
          {['#FF5F56', '#27C93F', '#3B82F6'].map((c, i) => (
            <div key={i} style={{
              width: 10, height: 10, borderRadius: 999,
              background: c, border: '2px solid #000',
            }} />
          ))}
        </div>
        {/* Output tray */}
        <div style={{
          position: 'absolute', bottom: -8, left: '10%', right: '10%', height: 10,
          background: '#F5C71E',
          border: '2px solid #000',
          borderRadius: '0 0 8px 8px',
          boxShadow: '3px 3px 0 #000',
        }} />
      </div>
    </motion.div>
  );
}

// ─────────────────────────────────────────
//  FLOATING BOOK STACK — small decorative pile
// ─────────────────────────────────────────
function FloatingBooks() {
  return (
    <motion.div
      animate={{ y: [0, -5, 0], rotate: [-4, -6, -4] }}
      transition={{ duration: 5.2, repeat: Infinity, ease: 'easeInOut', delay: 1.4 }}
      style={{ position: 'relative', width: 110, height: 90 }}
    >
      {[
        { y: 40, w: 96, h: 16, bg: '#7C3AED', shift: 0, text: 'MEMOIR' },
        { y: 22, w: 104, h: 18, bg: '#FFE500', shift: 6, text: 'NOVEL' },
        { y: 2,  w: 98,  h: 20, bg: '#FF2D78', shift: -4, text: 'POETRY' },
      ].map((b, i) => (
        <motion.div
          key={i}
          animate={{ x: [0, b.shift * 0.4, 0] }}
          transition={{ duration: 6 + i, repeat: Infinity, ease: 'easeInOut' }}
          style={{
            position: 'absolute',
            left: `calc(50% - ${b.w / 2}px + ${b.shift}px)`,
            top: b.y,
            width: b.w,
            height: b.h,
            background: b.bg,
            border: '2px solid #000',
            boxShadow: '3px 3px 0 #000',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 7,
            fontWeight: 900,
            letterSpacing: '0.15em',
            color: b.bg === '#FFE500' ? '#000' : '#FFF',
          }}
        >
          {b.text}
        </motion.div>
      ))}
    </motion.div>
  );
}

// ─────────────────────────────────────────
//  SPARKLES — drifting twinkles around the scene
// ─────────────────────────────────────────
function Sparkles() {
  const dots = [
    { x: '8%',  y: '16%', d: 0,   s: 4, c: '#FFE500' },
    { x: '92%', y: '22%', d: 1.2, s: 3, c: '#FFFFFF' },
    { x: '14%', y: '78%', d: 2.0, s: 3, c: '#7C3AED' },
    { x: '88%', y: '82%', d: 0.6, s: 4, c: '#FF2D78' },
    { x: '50%', y: '8%',  d: 1.6, s: 2, c: '#FFFFFF' },
    { x: '70%', y: '60%', d: 2.4, s: 3, c: '#FFE500' },
  ];
  return (
    <>
      {dots.map((d, i) => (
        <motion.div
          key={i}
          animate={{ opacity: [0.2, 1, 0.2], scale: [0.6, 1.2, 0.6] }}
          transition={{ duration: 2.8, repeat: Infinity, delay: d.d, ease: 'easeInOut' }}
          style={{
            position: 'absolute', left: d.x, top: d.y,
            width: d.s, height: d.s, borderRadius: 999, background: d.c,
            boxShadow: `0 0 ${d.s * 2}px ${d.c}`,
            pointerEvents: 'none',
          }}
        />
      ))}
    </>
  );
}

function BookMockup() {
  const [page, setPage] = useState(0);
  const pages = [
    {
      left: { isTitle: true, title: 'The Last Lighthouse', author: 'Eleanor Marsh' },
      right: {
        chapterNum: 'I', chapterTitle: 'The Storm',
        text: 'The storm had been building for three days before it finally broke.\n\nMara stood at the window of the lighthouse, watching the gray Atlantic churn beneath a bruised sky.',
      },
    },
    {
      left: {
        text: 'She had come here to write. That was what she told herself, and what she told her editor.\n\nBut the truth, the honest embarrassing truth, was that she had come here to disappear for a while.\n\nThe notebook on her desk was still blank.',
        pageNum: 3,
      },
      right: {
        chapterNum: 'II', chapterTitle: 'Three Miles Out',
        text: 'Captain Reid Harmon had seen worse storms in his forty years on the water, but not many. The swells had climbed past fifteen feet around midnight.',
      },
    },
  ];

  useEffect(() => {
    const t = setInterval(() => setPage(p => (p + 1) % pages.length), 4000);
    return () => clearInterval(t);
  }, []);

  const current = pages[page];

  return (
    <motion.div
      animate={{ y: [0, -6, 0] }}
      transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
      style={{
        border: '4px solid rgba(255,255,255,0.7)',
        boxShadow: '10px 10px 0px rgba(255,255,255,0.15)',
        display: 'inline-block',
      }}
    >
      <div className="flex" style={{ width: '520px', height: '360px' }}>
        {/* Spine */}
        <div className="absolute inset-y-0 z-10 pointer-events-none" style={{
          left: '50%', transform: 'translateX(-50%)', width: '10px',
          background: 'linear-gradient(90deg, rgba(0,0,0,0.2), rgba(0,0,0,0.05), rgba(0,0,0,0.2))',
        }} />
        {/* Left page */}
        <motion.div
          key={`l-${page}`} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.35 }}
          className="flex-1 p-7 relative overflow-hidden"
          style={{ background: '#F5F0E8', fontFamily: 'var(--font-garamond), Georgia, serif' }}
        >
          {(current.left as any).isTitle ? (
            <div className="h-full flex flex-col items-center justify-center text-center">
              <div className="text-lg font-semibold mb-1" style={{ fontFamily: 'var(--font-playfair), serif', color: '#1C1208', lineHeight: 1.3 }}>
                {(current.left as any).title}
              </div>
              <div className="text-xs mt-5" style={{ color: '#7D6A55', fontStyle: 'italic' }}>
                {(current.left as any).author}
              </div>
            </div>
          ) : (
            <div>
              <p className="text-xs leading-relaxed" style={{ color: '#1C1208', whiteSpace: 'pre-line', textIndent: '1.5em' }}>
                {(current.left as any).text}
              </p>
              <div className="absolute bottom-4 left-0 right-0 text-center" style={{ color: '#9D8A75', fontSize: '9px' }}>
                {(current.left as any).pageNum ?? ''}
              </div>
            </div>
          )}
        </motion.div>
        {/* Right page */}
        <motion.div
          key={`r-${page}`} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.35, delay: 0.1 }}
          className="flex-1 p-7 relative overflow-hidden"
          style={{ background: '#FAF5ED', fontFamily: 'var(--font-garamond), Georgia, serif' }}
        >
          {(current.right as any).chapterNum && (
            <div>
              <div className="text-xs text-center mb-0.5" style={{ color: '#9D8A75', letterSpacing: '0.08em' }}>
                Chapter {(current.right as any).chapterNum}
              </div>
              <div className="text-sm text-center mb-4" style={{ fontFamily: 'var(--font-playfair), serif', color: '#1C1208', fontStyle: 'italic' }}>
                {(current.right as any).chapterTitle}
              </div>
              <p className="text-xs leading-relaxed" style={{ color: '#1C1208', textIndent: '1.5em', whiteSpace: 'pre-line' }}>
                {(current.right as any).text}
              </p>
            </div>
          )}
        </motion.div>
      </div>
      {/* Page dots */}
      <div className="flex justify-center gap-2 py-3" style={{ background: '#F5F0E8', borderTop: '1px solid #E8E0D4' }}>
        {pages.map((_, i) => (
          <button key={i} onClick={() => setPage(i)}
            className="w-1.5 h-1.5 rounded-full transition-all"
            style={{ background: i === page ? '#7C3AED' : '#C4B9A8', transform: i === page ? 'scale(1.4)' : 'scale(1)' }}
          />
        ))}
      </div>
    </motion.div>
  );
}

export default function Hero() {
  const words = ['novel', 'memoir', 'business book', 'self-help guide', 'poetry collection', 'thriller'];
  const [wordIndex, setWordIndex] = useState(0);

  useEffect(() => {
    const t = setInterval(() => setWordIndex(i => (i + 1) % words.length), 2800);
    return () => clearInterval(t);
  }, []);

  // Parallax for the hero scene
  const sectionRef = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ['start start', 'end start'] });
  const sceneY = useTransform(scrollYProgress, [0, 1], [0, -80]);
  const headY = useTransform(scrollYProgress, [0, 1], [0, -40]);
  const statsY = useTransform(scrollYProgress, [0, 1], [0, -20]);

  return (
    <section ref={sectionRef} className="relative min-h-screen flex items-center overflow-hidden pt-16" style={{ background: '#0A0910' }}>
      {/* WebGL animated background — purple + gold fBm field */}
      <ShaderBackground />

      {/* Background aurora */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
        <motion.div
          animate={{ scale: [1, 1.1, 1], x: ['0%', '3%', '0%'] }}
          transition={{ duration: 20, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute"
          style={{ top: '-20%', left: '-10%', width: '70%', height: '80%', background: 'radial-gradient(ellipse, rgba(124,58,237,0.18) 0%, transparent 65%)' }}
        />
        <motion.div
          animate={{ scale: [1, 0.92, 1], x: ['0%', '-3%', '0%'] }}
          transition={{ duration: 25, repeat: Infinity, ease: 'easeInOut', delay: 4 }}
          className="absolute"
          style={{ bottom: '-20%', right: '-5%', width: '55%', height: '65%', background: 'radial-gradient(ellipse, rgba(255,229,0,0.06) 0%, transparent 65%)' }}
        />
        {/* Brutal grid */}
        <div className="absolute inset-0" style={{
          backgroundImage: 'linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px)',
          backgroundSize: '80px 80px',
        }} />
      </div>

      <div className="container-wide relative z-10 py-14">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left: Copy */}
          <motion.div style={{ y: headY }}>
            {/* MASSIVE Headline — Penguin-style system name */}
            <motion.div
              initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.2 }}
              className="mb-4"
            >
              {/* System label above — like Penguin's imprint labels */}
              <div className="font-black mb-2" style={{ fontSize: '11px', letterSpacing: '0.2em', color: 'rgba(255,255,255,0.35)', textTransform: 'uppercase' }}>
                The World's Fastest
              </div>
              <h1
                className="font-display font-black leading-[1.0]"
                style={{ fontSize: 'clamp(44px, 6.5vw, 84px)', color: '#FFFFFF' }}
              >
                Instant Book
                <br />
                <span style={{ color: '#FFE500', textShadow: '4px 4px 0px rgba(0,0,0,0.35)' }}>
                  Formatting
                </span>
                <br />
                <span style={{ color: '#FFE500' }}>
                  System.
                </span>
              </h1>
            </motion.div>

            {/* Subheadline */}
            <motion.p
              initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.35 }}
              className="text-lg font-semibold mb-2"
              style={{ color: 'rgba(255,255,255,0.5)', letterSpacing: '0.08em', textTransform: 'uppercase', fontSize: '12px' }}
            >
              Paste your manuscript. Get a formatted{' '}
              <motion.span
                key={wordIndex}
                initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }}
                transition={{ duration: 0.2 }}
                style={{ color: '#FFE500' }}
              >
                {words[wordIndex]}
              </motion.span>
              {' '}in seconds.
            </motion.p>

            {/* Social proof numbers */}
            <motion.div
              initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.45 }}
              className="flex flex-wrap gap-3 mb-6 mt-5"
              style={{ y: statsY }}
            >
              {[
                { num: '56,362+', label: 'Books Formatted', color: '#FFE500', textColor: '#000' },
                { num: '10', label: 'Templates', color: '#00FF7F', textColor: '#000' },
                { num: '< 3s', label: 'To Format', color: '#FF2D78', textColor: '#fff' },
              ].map(stat => (
                <div key={stat.label} className="brutal-card-dark px-4 py-3 flex items-center gap-3"
                  style={{ background: stat.color, borderColor: '#000', boxShadow: '4px 4px 0px #000' }}
                >
                  <span className="text-2xl font-black leading-none" style={{ color: stat.textColor }}>{stat.num}</span>
                  <span className="text-xs font-bold uppercase tracking-wide" style={{ color: stat.textColor, opacity: 0.7 }}>{stat.label}</span>
                </div>
              ))}
            </motion.div>

            {/* CTAs */}
            <motion.div
              initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.55 }}
              className="flex flex-wrap gap-4 items-center"
            >
              <Magnetic strength={0.4} data-cursor-label="Format">
                <Link href="/editor" className="brutal-btn-yellow" style={{ fontSize: '16px', padding: '16px 32px' }}>
                  Format My Book
                  <ArrowRight size={16} strokeWidth={2.5} />
                </Link>
              </Magnetic>
              <Magnetic strength={0.3}>
                <Link href="#how-it-works" className="brutal-btn-outline-white" style={{ fontSize: '15px', padding: '14px 28px' }}>
                  See how it works
                </Link>
              </Magnetic>
            </motion.div>

            {/* Author avatars */}
            <motion.div
              initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.8 }}
              className="flex items-center gap-3 mt-8"
            >
              <div className="flex -space-x-2">
                {['#8B5CF6', '#EC4899', '#F59E0B', '#10B981', '#3B82F6'].map((c, i) => (
                  <div key={i} className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white"
                    style={{ background: c, border: '2px solid #0A0910', zIndex: 5 - i }}
                  >
                    {['A', 'M', 'J', 'S', 'R'][i]}
                  </div>
                ))}
              </div>
              <div>
                <div className="text-sm font-bold text-white">Loved by authors</div>
                <div className="text-xs" style={{ color: 'rgba(255,255,255,0.45)' }}>56,362 books formatted and counting</div>
              </div>
            </motion.div>
          </motion.div>

          {/* Right: Animated scene — laptop typing + book spread + e-reader + printer */}
          <motion.div
            initial={{ opacity: 0, x: 40, scale: 0.92 }} animate={{ opacity: 1, x: 0, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.3, ease: [0.16, 1, 0.3, 1] }}
            className="relative"
            style={{ minHeight: '560px', width: '100%', y: sceneY }}
          >
            {/* Glow */}
            <div className="absolute inset-0 -z-10 blur-3xl opacity-40 rounded-full"
              style={{ background: 'radial-gradient(ellipse, rgba(255,229,0,0.35) 0%, transparent 70%)' }}
            />

            <Sparkles />

            {/* Laptop — back-left, peeking behind the book */}
            <div style={{
              position: 'absolute',
              top: '6%',
              left: '-2%',
              zIndex: 1,
            }}>
              <TypingLaptop />
            </div>

            {/* Printer — back-right */}
            <div style={{
              position: 'absolute',
              top: '2%',
              right: '2%',
              zIndex: 1,
            }}>
              <Printer />
            </div>

            {/* Book spread — centerpiece */}
            <div style={{
              position: 'absolute',
              top: '32%',
              left: '50%',
              transform: 'translate(-50%, -12%)',
              zIndex: 2,
            }}>
              <BookMockup />
            </div>

            {/* E-Reader — foreground right */}
            <div style={{
              position: 'absolute',
              bottom: '2%',
              right: '10%',
              zIndex: 3,
              filter: 'drop-shadow(0 16px 20px rgba(0,0,0,0.45))',
            }}>
              <EReader />
            </div>

            {/* Floating book stack — foreground left */}
            <div style={{
              position: 'absolute',
              bottom: '4%',
              left: '6%',
              zIndex: 3,
            }}>
              <FloatingBooks />
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
