'use client';

import { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';

/**
 * Awwwards-style entry preloader.
 * Shows "BOOKSANE" with letters revealing one-by-one,
 * a progress counter ticking to 100, and a final reveal curtain.
 * Only shown once per session (sessionStorage).
 */
export default function Preloader() {
  const [visible, setVisible] = useState(true);
  const [percent, setPercent] = useState(0);

  useEffect(() => {
    // Skip if we've shown it this session (soft UX — reappears next tab)
    if (typeof window !== 'undefined' && sessionStorage.getItem('booksane:preloaded') === '1') {
      setVisible(false);
      return;
    }

    let current = 0;
    const start = performance.now();
    const duration = 1650; // ms

    const tick = () => {
      const elapsed = performance.now() - start;
      // Ease-out towards 100
      const p = Math.min(1, elapsed / duration);
      const eased = 1 - Math.pow(1 - p, 2.2);
      current = Math.floor(eased * 100);
      setPercent(current);
      if (current < 100) {
        requestAnimationFrame(tick);
      } else {
        setTimeout(() => {
          sessionStorage.setItem('booksane:preloaded', '1');
          setVisible(false);
        }, 380);
      }
    };
    requestAnimationFrame(tick);
  }, []);

  const letters = 'BOOKSANE'.split('');

  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          exit={{ y: '-100%' }}
          transition={{ duration: 0.9, ease: [0.77, 0, 0.175, 1] }}
          aria-hidden="true"
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 9998,
            background: '#0A0910',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 32,
          }}
        >
          {/* Grid backdrop */}
          <div
            aria-hidden="true"
            style={{
              position: 'absolute',
              inset: 0,
              backgroundImage:
                'linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)',
              backgroundSize: '80px 80px',
              pointerEvents: 'none',
            }}
          />

          {/* Aurora */}
          <div
            aria-hidden="true"
            style={{
              position: 'absolute',
              top: '-20%',
              left: '-10%',
              width: '70%',
              height: '80%',
              background:
                'radial-gradient(ellipse, rgba(124,58,237,0.22) 0%, transparent 65%)',
              filter: 'blur(20px)',
              pointerEvents: 'none',
            }}
          />

          {/* Brand mark reveal */}
          <div
            style={{
              display: 'flex',
              fontFamily: 'var(--font-playfair), Georgia, serif',
              color: '#FFFFFF',
              fontWeight: 900,
              fontSize: 'clamp(48px, 8vw, 120px)',
              letterSpacing: '-0.02em',
              lineHeight: 1,
              position: 'relative',
              zIndex: 1,
            }}
          >
            {letters.map((ch, i) => (
              <motion.span
                key={i}
                initial={{ y: '110%', opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{
                  duration: 0.9,
                  delay: 0.05 + i * 0.06,
                  ease: [0.77, 0, 0.175, 1],
                }}
                style={{ display: 'inline-block' }}
              >
                {ch}
              </motion.span>
            ))}
            <motion.span
              initial={{ scale: 0, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: letters.length * 0.06 + 0.3, duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
              style={{
                display: 'inline-block',
                width: '0.18em',
                height: '0.18em',
                background: '#FFE500',
                alignSelf: 'flex-end',
                marginBottom: '0.2em',
                marginLeft: '0.08em',
                borderRadius: '50%',
              }}
            />
          </div>

          {/* Progress bar + counter */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 18,
              position: 'relative',
              zIndex: 1,
            }}
          >
            <div
              style={{
                width: 260,
                height: 2,
                background: 'rgba(255,255,255,0.1)',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  width: `${percent}%`,
                  height: '100%',
                  background: '#FFE500',
                  boxShadow: '0 0 14px rgba(255,229,0,0.55)',
                  transition: 'width 0.1s linear',
                }}
              />
            </div>
            <div
              style={{
                fontFamily: 'var(--font-body), system-ui, sans-serif',
                fontSize: 12,
                fontWeight: 800,
                color: '#FFE500',
                letterSpacing: '0.1em',
                minWidth: 36,
                textAlign: 'right',
              }}
            >
              {String(percent).padStart(3, '0')}
            </div>
          </div>

          <div
            style={{
              fontFamily: 'var(--font-body), system-ui, sans-serif',
              fontSize: 10,
              fontWeight: 700,
              color: 'rgba(255,255,255,0.35)',
              letterSpacing: '0.2em',
              textTransform: 'uppercase',
              position: 'relative',
              zIndex: 1,
            }}
          >
            Formatting your experience
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
