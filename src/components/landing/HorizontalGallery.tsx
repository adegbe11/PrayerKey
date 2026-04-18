'use client';

import { useRef } from 'react';
import { motion, useScroll, useTransform } from 'framer-motion';

/**
 * Awwwards-style sticky horizontal scroll section.
 * A tall outer wrapper stays sticky while the inner strip
 * translates horizontally based on scroll progress.
 */

const COVERS = [
  { title: 'The Last Lighthouse', author: 'Eleanor Marsh',   bg: '#0A1628', fg: '#F5E6C8', accent: '#D4A853', category: 'Literary', style: 'classic' },
  { title: 'Ember & Ash',        author: 'Lena Hart',        bg: '#FFF5F7', fg: '#1A0A0E', accent: '#D81B60', category: 'Romance',  style: 'elegant' },
  { title: 'The Quiet Hour',     author: 'Jonas Pine',       bg: '#F9F6F1', fg: '#1A1208', accent: '#8B5E3C', category: 'Memoir',   style: 'classic' },
  { title: 'Break the Script',   author: 'Kira Ozumba',      bg: '#FFE500', fg: '#000000', accent: '#000000', category: 'Business', style: 'bold' },
  { title: 'Signal Lost',        author: 'R. M. Chen',       bg: '#090909', fg: '#FFFFFF', accent: '#C62828', category: 'Thriller', style: 'bold' },
  { title: 'Breath, Verses',     author: 'Ada Okun',         bg: '#F9F8FF', fg: '#1A1428', accent: '#4527A0', category: 'Poetry',   style: 'elegant' },
  { title: 'How to Begin',       author: 'Sam Werner',       bg: '#FFFBEB', fg: '#1C1100', accent: '#D97706', category: 'Self-Help', style: 'modern' },
  { title: 'After the Rain',     author: 'Helena Moss',      bg: '#FDF8F2', fg: '#2D1F17', accent: '#7D4F35', category: 'Memoir',   style: 'classic' },
];

export default function HorizontalGallery() {
  const containerRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ['start start', 'end end'],
  });

  // translate horizontally: from 0 to -(covers width - viewport width)
  // we approximate by using a large negative translate.
  const x = useTransform(scrollYProgress, [0, 1], ['6%', '-76%']);
  const eyeX = useTransform(scrollYProgress, [0, 1], ['-2%', '102%']);

  return (
    <section ref={containerRef} style={{ height: '340vh', position: 'relative', background: '#0A0910' }}>
      <div style={{
        position: 'sticky',
        top: 0,
        height: '100vh',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
      }}>
        {/* Section eyebrow */}
        <div style={{ padding: '0 6vw', marginBottom: 28 }}>
          <div style={{
            fontSize: 11,
            letterSpacing: '0.22em',
            color: 'rgba(255,255,255,0.35)',
            textTransform: 'uppercase',
            fontWeight: 900,
            marginBottom: 14,
          }}>
            10 TEMPLATES / 14 ORNAMENTS / 19 FONTS
          </div>
          <h2
            className="font-display font-black"
            style={{
              fontSize: 'clamp(40px, 6vw, 82px)',
              lineHeight: 0.96,
              color: '#FFFFFF',
              letterSpacing: '-0.02em',
              margin: 0,
            }}
          >
            Ten looks.
            <br />
            <span style={{ color: '#FFE500', textShadow: '4px 4px 0 rgba(0,0,0,0.35)' }}>
              Zero compromise.
            </span>
          </h2>
        </div>

        {/* Horizontal strip */}
        <motion.div
          style={{
            x,
            display: 'flex',
            gap: 36,
            flexShrink: 0,
            willChange: 'transform',
            padding: '20px 0',
          }}
        >
          {COVERS.map((c, i) => (
            <CoverCard key={i} cover={c} index={i} />
          ))}
        </motion.div>

        {/* Scroll progress line */}
        <div style={{ padding: '28px 6vw 0', display: 'flex', alignItems: 'center', gap: 18 }}>
          <span style={{
            fontSize: 10,
            fontWeight: 900,
            color: 'rgba(255,255,255,0.35)',
            letterSpacing: '0.2em',
            textTransform: 'uppercase',
          }}>
            Scroll
          </span>
          <div style={{
            flex: 1,
            height: 2,
            background: 'rgba(255,255,255,0.08)',
            position: 'relative',
            overflow: 'hidden',
          }}>
            <motion.div
              style={{
                position: 'absolute',
                left: eyeX,
                top: -4,
                width: 10,
                height: 10,
                borderRadius: 999,
                background: '#FFE500',
                boxShadow: '0 0 14px rgba(255,229,0,0.6)',
              }}
            />
          </div>
          <span style={{
            fontSize: 10,
            fontWeight: 900,
            color: 'rgba(255,255,255,0.35)',
            letterSpacing: '0.2em',
            textTransform: 'uppercase',
          }}>
            8 Covers
          </span>
        </div>
      </div>
    </section>
  );
}

function CoverCard({
  cover,
  index,
}: {
  cover: typeof COVERS[number];
  index: number;
}) {
  const isElegant = cover.style === 'elegant';
  const isBold = cover.style === 'bold';

  return (
    <motion.div
      whileHover={{ y: -8, scale: 1.02 }}
      transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
      data-magnetic="true"
      style={{
        flexShrink: 0,
        width: 280,
        height: 400,
        position: 'relative',
        cursor: 'pointer',
      }}
    >
      {/* Cover */}
      <div style={{
        width: '100%',
        height: '100%',
        background: cover.bg,
        border: isBold ? '3px solid #000' : '1px solid rgba(0,0,0,0.1)',
        boxShadow: isBold
          ? '8px 8px 0 #000'
          : '0 30px 60px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.05)',
        padding: '38px 28px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: isElegant ? 'center' : 'flex-start',
        justifyContent: 'center',
        textAlign: isElegant ? 'center' : 'left',
        position: 'relative',
        overflow: 'hidden',
      }}>
        {isElegant && (
          <div style={{
            width: 2, height: 40, background: cover.accent,
            marginBottom: 22, opacity: 0.7,
          }} />
        )}

        <div style={{
          fontSize: isBold ? 10 : 9,
          fontWeight: 700,
          letterSpacing: '0.22em',
          textTransform: 'uppercase',
          color: cover.accent,
          marginBottom: 18,
          opacity: 0.9,
        }}>
          {cover.category}
        </div>

        <h3 style={{
          fontFamily: isBold ? 'var(--font-inter)' : 'var(--font-playfair), Georgia, serif',
          fontSize: isBold ? 32 : cover.title.length > 18 ? 28 : 34,
          fontWeight: isBold ? 900 : 700,
          fontStyle: isElegant ? 'italic' : 'normal',
          lineHeight: 1.05,
          color: cover.fg,
          margin: 0,
          marginBottom: isElegant ? 24 : 20,
          letterSpacing: isBold ? '-0.01em' : 'normal',
          textTransform: isBold ? 'uppercase' : 'none',
        }}>
          {cover.title}
        </h3>

        {isElegant && (
          <div style={{
            width: 30, height: 1, background: cover.accent, opacity: 0.5,
            marginBottom: 16,
          }} />
        )}

        <div style={{
          fontFamily: isBold ? 'var(--font-inter)' : 'var(--font-playfair), Georgia, serif',
          fontSize: 12,
          fontStyle: isElegant ? 'italic' : 'normal',
          fontWeight: isBold ? 800 : 400,
          color: cover.fg,
          opacity: isBold ? 1 : 0.65,
          letterSpacing: isBold ? '0.1em' : 'normal',
          textTransform: isBold ? 'uppercase' : 'none',
        }}>
          {cover.author}
        </div>

        {/* Spine highlight */}
        <div style={{
          position: 'absolute',
          left: 0,
          top: 0,
          bottom: 0,
          width: 8,
          background: 'linear-gradient(90deg, rgba(0,0,0,0.15), transparent)',
          pointerEvents: 'none',
        }} />
      </div>

      {/* Number badge below */}
      <div style={{
        marginTop: 14,
        display: 'flex',
        alignItems: 'center',
        gap: 10,
        color: 'rgba(255,255,255,0.45)',
        fontSize: 10,
        fontWeight: 900,
        letterSpacing: '0.2em',
        textTransform: 'uppercase',
      }}>
        <span>0{index + 1}</span>
        <span style={{ flex: 1, height: 1, background: 'rgba(255,255,255,0.1)' }} />
        <span>{cover.style}</span>
      </div>
    </motion.div>
  );
}
