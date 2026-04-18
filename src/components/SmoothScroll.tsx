'use client';

import { useEffect } from 'react';

export default function SmoothScroll() {
  useEffect(() => {
    let destroyed = false;
    let rafId = 0;
    let lenis: { raf: (t: number) => void; destroy: () => void } | null = null;

    (async () => {
      try {
        const Lenis = (await import('lenis')).default;
        if (destroyed) return;
        lenis = new Lenis({
          duration: 1.15,
          easing: (t: number) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
          smoothWheel: true,
          touchMultiplier: 1.4,
          wheelMultiplier: 1,
        });
        const raf = (time: number) => {
          lenis?.raf(time);
          rafId = requestAnimationFrame(raf);
        };
        rafId = requestAnimationFrame(raf);
      } catch {
        // Lenis missing or unsupported environment — fall back silently to native scroll.
      }
    })();

    return () => {
      destroyed = true;
      if (rafId) cancelAnimationFrame(rafId);
      lenis?.destroy();
    };
  }, []);

  return null;
}
