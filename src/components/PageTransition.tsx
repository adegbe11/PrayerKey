'use client';

import { useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { usePathname } from 'next/navigation';

/**
 * Cinematic curtain-wipe on route change.
 * On path change: black+yellow panels slide in from top & bottom, meet in
 * the middle, then split apart revealing the new page.
 * Adds a beat of visual drama between pages (Awwwards signature).
 */
export default function PageTransition() {
  const pathname = usePathname();
  const [curtainKey, setCurtainKey] = useState(0);
  const [show, setShow] = useState(false);
  const didMount = useRef(false);

  useEffect(() => {
    // Skip the initial mount — Preloader already covers first paint.
    if (!didMount.current) {
      didMount.current = true;
      return;
    }
    setCurtainKey((k) => k + 1);
    setShow(true);
    const hideTimer = setTimeout(() => setShow(false), 1050);
    return () => clearTimeout(hideTimer);
  }, [pathname]);

  return (
    <AnimatePresence mode="wait">
      {show && (
        <motion.div
          key={curtainKey}
          aria-hidden="true"
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 9997,
            pointerEvents: 'none',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {/* Top panel */}
          <motion.div
            initial={{ y: '-100%' }}
            animate={{ y: ['-100%', '0%', '-100%'] }}
            exit={{ y: '-100%' }}
            transition={{
              duration: 1.0,
              times: [0, 0.45, 1],
              ease: [0.76, 0, 0.24, 1],
            }}
            style={{
              flex: 1,
              background: '#0A0910',
              borderBottom: '4px solid #FFE500',
            }}
          />
          {/* Center yellow flash bar */}
          <motion.div
            initial={{ scaleY: 0 }}
            animate={{ scaleY: [0, 1, 1, 0] }}
            exit={{ scaleY: 0 }}
            transition={{
              duration: 1.0,
              times: [0, 0.45, 0.55, 1],
              ease: [0.76, 0, 0.24, 1],
            }}
            style={{
              height: 8,
              background: '#FFE500',
              transformOrigin: 'center',
            }}
          />
          {/* Bottom panel */}
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: ['100%', '0%', '100%'] }}
            exit={{ y: '100%' }}
            transition={{
              duration: 1.0,
              times: [0, 0.45, 1],
              ease: [0.76, 0, 0.24, 1],
            }}
            style={{
              flex: 1,
              background: '#0A0910',
              borderTop: '4px solid #FFE500',
            }}
          />

          {/* Brand stamp that flashes at peak */}
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: [0, 1, 0], scale: [0.8, 1, 0.95] }}
            exit={{ opacity: 0 }}
            transition={{
              duration: 1.0,
              times: [0, 0.5, 1],
              ease: [0.16, 1, 0.3, 1],
            }}
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontFamily: 'var(--font-playfair), Georgia, serif',
              fontSize: 'clamp(32px, 6vw, 72px)',
              fontWeight: 900,
              color: '#FFE500',
              letterSpacing: '-0.02em',
              textShadow: '4px 4px 0 rgba(0,0,0,0.5)',
            }}
          >
            BOOKSANE
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
