'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { motion } from 'framer-motion';
import { ArrowRight } from 'lucide-react';

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

  return (
    <section className="relative min-h-screen flex items-center overflow-hidden pt-16" style={{ background: '#0A0910' }}>
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
          <div>
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
            >
              {[
                { num: '12,847+', label: 'Books Formatted', color: '#FFE500', textColor: '#000' },
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
              <Link href="/editor" className="brutal-btn-yellow" style={{ fontSize: '16px', padding: '16px 32px' }}>
                Format My Book
                <ArrowRight size={16} strokeWidth={2.5} />
              </Link>
              <Link href="#how-it-works" className="brutal-btn-outline-white" style={{ fontSize: '15px', padding: '14px 28px' }}>
                See how it works
              </Link>
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
                <div className="text-xs" style={{ color: 'rgba(255,255,255,0.45)' }}>12,847 books formatted and counting</div>
              </div>
            </motion.div>
          </div>

          {/* Right: Book mockup */}
          <motion.div
            initial={{ opacity: 0, x: 40, scale: 0.92 }} animate={{ opacity: 1, x: 0, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.3, ease: [0.16, 1, 0.3, 1] }}
            className="flex justify-center lg:justify-end relative"
          >
            {/* Glow */}
            <div className="absolute inset-0 -z-10 blur-3xl opacity-30 rounded-full"
              style={{ background: 'radial-gradient(ellipse, rgba(255,229,0,0.4) 0%, transparent 70%)' }}
            />
            <BookMockup />
          </motion.div>
        </div>
      </div>
    </section>
  );
}
