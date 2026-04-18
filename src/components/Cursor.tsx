'use client';

import { useEffect, useRef, useState } from 'react';

/**
 * Awwwards-style cursor:
 *   - a small solid dot that follows the pointer exactly
 *   - a larger soft ring that trails behind with spring easing
 *   - on hover over [data-magnetic], ring scales + text color, dot snaps to element center
 *   - hidden on coarse pointers (touch)
 */
export default function Cursor() {
  const dotRef = useRef<HTMLDivElement>(null);
  const ringRef = useRef<HTMLDivElement>(null);
  const labelRef = useRef<HTMLDivElement>(null);
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    const coarse = window.matchMedia('(pointer: coarse)').matches;
    if (coarse) return;
    setEnabled(true);

    let ringX = window.innerWidth / 2;
    let ringY = window.innerHeight / 2;
    let targetX = ringX;
    let targetY = ringY;
    let hoverState: 'idle' | 'hover' | 'press' = 'idle';
    let label = '';

    const onMove = (e: MouseEvent) => {
      targetX = e.clientX;
      targetY = e.clientY;
      if (dotRef.current) {
        dotRef.current.style.transform = `translate3d(${targetX - 3}px, ${targetY - 3}px, 0)`;
      }
    };

    const onOver = (e: MouseEvent) => {
      const t = e.target as HTMLElement | null;
      if (!t) return;
      const mag = t.closest<HTMLElement>('[data-magnetic], a, button');
      const lbl = t.closest<HTMLElement>('[data-cursor-label]')?.dataset.cursorLabel ?? '';
      if (mag) {
        hoverState = 'hover';
        label = lbl;
      } else {
        hoverState = 'idle';
        label = '';
      }
      if (labelRef.current) labelRef.current.textContent = label;
    };

    const onDown = () => { hoverState = 'press'; };
    const onUp = () => { hoverState = hoverState === 'press' ? 'hover' : hoverState; };

    let rafId = 0;
    const tick = () => {
      ringX += (targetX - ringX) * 0.17;
      ringY += (targetY - ringY) * 0.17;
      if (ringRef.current) {
        const scale =
          hoverState === 'press' ? 0.75 :
          hoverState === 'hover' ? 2.2 : 1;
        ringRef.current.style.transform = `translate3d(${ringX - 20}px, ${ringY - 20}px, 0) scale(${scale})`;
      }
      if (labelRef.current) {
        labelRef.current.style.transform = `translate3d(${ringX + 26}px, ${ringY + 16}px, 0)`;
        labelRef.current.style.opacity = label ? '1' : '0';
      }
      rafId = requestAnimationFrame(tick);
    };
    rafId = requestAnimationFrame(tick);

    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseover', onOver);
    window.addEventListener('mousedown', onDown);
    window.addEventListener('mouseup', onUp);
    document.documentElement.style.cursor = 'none';

    return () => {
      cancelAnimationFrame(rafId);
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseover', onOver);
      window.removeEventListener('mousedown', onDown);
      window.removeEventListener('mouseup', onUp);
      document.documentElement.style.cursor = '';
    };
  }, []);

  if (!enabled) return null;

  return (
    <>
      <div
        ref={ringRef}
        aria-hidden="true"
        style={{
          position: 'fixed', top: 0, left: 0,
          width: 40, height: 40,
          borderRadius: 999,
          border: '1.5px solid rgba(255, 229, 0, 0.9)',
          mixBlendMode: 'difference',
          pointerEvents: 'none',
          zIndex: 9999,
          transition: 'border-color 0.2s',
          willChange: 'transform',
          transform: 'translate3d(-100px,-100px,0)',
        }}
      />
      <div
        ref={dotRef}
        aria-hidden="true"
        style={{
          position: 'fixed', top: 0, left: 0,
          width: 6, height: 6,
          borderRadius: 999,
          background: '#FFE500',
          mixBlendMode: 'difference',
          pointerEvents: 'none',
          zIndex: 10000,
          willChange: 'transform',
          transform: 'translate3d(-100px,-100px,0)',
        }}
      />
      <div
        ref={labelRef}
        aria-hidden="true"
        style={{
          position: 'fixed', top: 0, left: 0,
          padding: '4px 10px',
          borderRadius: 999,
          background: '#FFE500',
          color: '#000',
          fontSize: 10,
          fontWeight: 900,
          letterSpacing: '0.1em',
          textTransform: 'uppercase',
          fontFamily: 'var(--font-body), system-ui, sans-serif',
          opacity: 0,
          pointerEvents: 'none',
          zIndex: 10000,
          transition: 'opacity 0.2s',
          willChange: 'transform',
          transform: 'translate3d(-100px,-100px,0)',
          boxShadow: '3px 3px 0 #000',
          border: '2px solid #000',
        }}
      />
    </>
  );
}
