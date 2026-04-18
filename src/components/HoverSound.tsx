'use client';

import { useEffect, useRef, useState } from 'react';
import { Volume2, VolumeX } from 'lucide-react';

/**
 * Subtle tick on hover-over-cta + click. Uses WebAudio oscillator (no assets).
 * Off by default. A tiny pill appears bottom-right so users can enable it.
 * Honors prefers-reduced-motion.
 */
export default function HoverSound() {
  const ctxRef = useRef<AudioContext | null>(null);
  const [enabled, setEnabled] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    if (typeof window === 'undefined') return;
    // Load persisted pref
    try {
      const saved = localStorage.getItem('booksane:sound');
      if (saved === 'on') setEnabled(true);
    } catch { /* noop */ }
  }, []);

  useEffect(() => {
    if (!enabled) return;

    // Respect reduced-motion
    if (typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    const ensureCtx = (): AudioContext | null => {
      if (ctxRef.current) return ctxRef.current;
      try {
        const Ctx = (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext);
        ctxRef.current = new Ctx();
      } catch {
        return null;
      }
      return ctxRef.current;
    };

    const play = (freq: number, durMs: number, gain: number) => {
      const ctx = ensureCtx();
      if (!ctx) return;
      if (ctx.state === 'suspended') ctx.resume();
      const osc = ctx.createOscillator();
      const g = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.value = freq;
      g.gain.value = 0;
      osc.connect(g);
      g.connect(ctx.destination);
      const t0 = ctx.currentTime;
      g.gain.setValueAtTime(0, t0);
      g.gain.linearRampToValueAtTime(gain, t0 + 0.003);
      g.gain.exponentialRampToValueAtTime(0.0001, t0 + durMs / 1000);
      osc.start(t0);
      osc.stop(t0 + durMs / 1000);
    };

    const onOver = (e: MouseEvent) => {
      const t = e.target as HTMLElement | null;
      if (!t) return;
      if (t.closest('[data-magnetic], a, button')) play(1100, 60, 0.03);
    };
    const onDown = (e: MouseEvent) => {
      const t = e.target as HTMLElement | null;
      if (!t) return;
      if (t.closest('[data-magnetic], a, button')) play(520, 90, 0.05);
    };

    window.addEventListener('mouseover', onOver);
    window.addEventListener('mousedown', onDown);
    return () => {
      window.removeEventListener('mouseover', onOver);
      window.removeEventListener('mousedown', onDown);
    };
  }, [enabled]);

  const toggle = () => {
    setEnabled((v) => {
      const next = !v;
      try { localStorage.setItem('booksane:sound', next ? 'on' : 'off'); } catch { /* noop */ }
      return next;
    });
  };

  if (!mounted) return null;

  return (
    <button
      onClick={toggle}
      aria-label={enabled ? 'Disable interaction sound' : 'Enable interaction sound'}
      title={enabled ? 'Sound on' : 'Sound off'}
      data-magnetic="true"
      data-cursor-label={enabled ? 'Mute' : 'Unmute'}
      style={{
        position: 'fixed',
        bottom: 22,
        right: 22,
        width: 42,
        height: 42,
        borderRadius: 999,
        background: enabled ? '#FFE500' : 'rgba(22,20,36,0.8)',
        color: enabled ? '#000' : '#F0EEFE',
        border: enabled ? '2px solid #000' : '1px solid rgba(255,255,255,0.1)',
        boxShadow: enabled ? '3px 3px 0 #000' : '0 10px 30px rgba(0,0,0,0.4)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        zIndex: 40,
        transition: 'transform 0.15s cubic-bezier(0.16, 1, 0.3, 1), background 0.2s',
        backdropFilter: enabled ? 'none' : 'blur(18px)',
      }}
    >
      {enabled ? <Volume2 size={15} /> : <VolumeX size={15} />}
    </button>
  );
}
