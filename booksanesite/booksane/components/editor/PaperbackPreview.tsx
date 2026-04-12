'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import { motion, AnimatePresence, useAnimation } from 'framer-motion';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import type { BookPage } from '@/types';

interface PaperbackPreviewProps {
  pages: BookPage[];
  currentSpread: number;
  onSpreadChange: (spread: number) => void;
  totalSpreads: number;
}

// Page dimensions (logical — will be scaled to fit container)
const PAGE_W = 260;
const PAGE_H = 380;
const BOOK_SPREAD_W = PAGE_W * 2 + 8; // spine = 8px

export default function PaperbackPreview({
  pages,
  currentSpread,
  onSpreadChange,
  totalSpreads,
}: PaperbackPreviewProps) {
  const [isFlipping, setIsFlipping] = useState(false);
  const [flipDir, setFlipDir] = useState<'forward' | 'backward'>('forward');
  const [flipContent, setFlipContent] = useState({ front: '', back: '' });
  const [scale, setScale] = useState(1);
  const containerRef = useRef<HTMLDivElement>(null);
  const controls = useAnimation();

  // Scale book to fit available container width
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    const observer = new ResizeObserver(([entry]) => {
      const available = entry.contentRect.width - 8; // 8px breathing room
      const newScale = Math.min(1, available / BOOK_SPREAD_W);
      setScale(newScale > 0 ? newScale : 1);
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  // Get the two pages for the current spread
  const leftPageIdx = currentSpread * 2;
  const rightPageIdx = leftPageIdx + 1;
  const leftPage = pages[leftPageIdx];
  const rightPage = pages[rightPageIdx];

  // ─── PAGE FLIP ANIMATION ──────────────

  const flipForward = useCallback(async () => {
    if (isFlipping || currentSpread >= totalSpreads - 1) return;
    setIsFlipping(true);
    setFlipDir('forward');

    // The flipping element shows: front = current right page, back = next left page
    const nextLeftPage = pages[(currentSpread + 1) * 2];
    setFlipContent({
      front: rightPage?.content ?? '',
      back: nextLeftPage?.content ?? '',
    });

    await controls.start({
      rotateY: -180,
      transition: { duration: 0.65, ease: [0.4, 0, 0.2, 1] },
    });

    controls.set({ rotateY: 0 });
    onSpreadChange(currentSpread + 1);
    setIsFlipping(false);
  }, [isFlipping, currentSpread, totalSpreads, pages, rightPage, controls, onSpreadChange]);

  const flipBackward = useCallback(async () => {
    if (isFlipping || currentSpread <= 0) return;
    setIsFlipping(true);
    setFlipDir('backward');

    const prevRightPage = pages[(currentSpread - 1) * 2 + 1];
    setFlipContent({
      front: prevRightPage?.content ?? '',
      back: leftPage?.content ?? '',
    });

    await controls.start({
      rotateY: 180,
      transition: { duration: 0.65, ease: [0.4, 0, 0.2, 1] },
    });

    controls.set({ rotateY: 0 });
    onSpreadChange(currentSpread - 1);
    setIsFlipping(false);
  }, [isFlipping, currentSpread, pages, leftPage, controls, onSpreadChange]);

  // Keyboard navigation
  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') flipForward();
    if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') flipBackward();
  }, [flipForward, flipBackward]);

  const isFirst = currentSpread === 0;
  const isLast = currentSpread >= totalSpreads - 1;

  return (
    <div
      ref={containerRef}
      className="flex flex-col items-center gap-6 focus:outline-none w-full"
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      {/* Book — scaled to fit container */}
      <div
        className="relative"
        style={{
          perspective: '2000px',
          transform: `scale(${scale})`,
          transformOrigin: 'top center',
          // Maintain layout height after scaling
          marginBottom: scale < 1 ? `${(PAGE_H * scale - PAGE_H)}px` : 0,
        }}
      >
        {/* Drop shadow */}
        <div
          className="absolute -inset-8 -z-10 rounded-3xl blur-3xl opacity-30"
          style={{ background: 'radial-gradient(ellipse, rgba(0,0,0,0.8) 0%, transparent 70%)' }}
        />

        {/* Book wrapper */}
        <div
          className="relative flex rounded-sm overflow-visible book-shadow"
          style={{
            width: PAGE_W * 2 + 8, // spine
            height: PAGE_H,
            boxShadow: '0 30px 70px rgba(0,0,0,0.7), 0 10px 30px rgba(0,0,0,0.4)',
          }}
        >
          {/* Left page */}
          <div
            className="relative overflow-hidden rounded-l-sm"
            style={{ width: PAGE_W, height: PAGE_H, background: '#F5F0E8' }}
            dangerouslySetInnerHTML={{ __html: leftPage?.content ?? buildBlankPage() }}
          />

          {/* Spine */}
          <div
            className="relative z-20 shrink-0"
            style={{
              width: '8px',
              background: 'linear-gradient(90deg, rgba(0,0,0,0.18) 0%, rgba(255,255,255,0.06) 40%, rgba(0,0,0,0.12) 100%)',
              boxShadow: 'inset -2px 0 4px rgba(0,0,0,0.1), inset 2px 0 4px rgba(0,0,0,0.1)',
            }}
          />

          {/* Right page */}
          <div
            className="relative overflow-hidden rounded-r-sm"
            style={{ width: PAGE_W, height: PAGE_H, background: '#FAF5ED' }}
            dangerouslySetInnerHTML={{ __html: rightPage?.content ?? buildBlankPage() }}
          />

          {/* Flipping page overlay */}
          <AnimatePresence>
            {isFlipping && (
              <motion.div
                className="absolute z-30 overflow-hidden"
                style={{
                  width: PAGE_W,
                  height: PAGE_H,
                  right: 8, // offset for spine
                  top: 0,
                  transformOrigin: 'left center',
                  transformStyle: 'preserve-3d',
                  borderRadius: flipDir === 'forward' ? '0 2px 2px 0' : '2px 0 0 2px',
                }}
                animate={controls}
              >
                {/* Front face */}
                <div
                  className="absolute inset-0"
                  style={{
                    backfaceVisibility: 'hidden',
                    WebkitBackfaceVisibility: 'hidden',
                    background: '#FAF5ED',
                  }}
                  dangerouslySetInnerHTML={{ __html: flipContent.front }}
                />
                {/* Back face */}
                <div
                  className="absolute inset-0"
                  style={{
                    backfaceVisibility: 'hidden',
                    WebkitBackfaceVisibility: 'hidden',
                    transform: 'rotateY(180deg)',
                    background: '#F5F0E8',
                  }}
                  dangerouslySetInnerHTML={{ __html: flipContent.back }}
                />
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>

      {/* Navigation */}
      <div className="flex items-center gap-6">
        <button
          onClick={flipBackward}
          disabled={isFirst || isFlipping}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium transition-all"
          style={{
            background: isFirst ? 'rgba(255,255,255,0.03)' : 'rgba(255,255,255,0.07)',
            border: '1px solid rgba(255,255,255,0.08)',
            color: isFirst ? 'var(--text-muted)' : 'var(--text-secondary)',
            opacity: isFirst ? 0.4 : 1,
          }}
        >
          <ChevronLeft size={15} />
          Prev
        </button>

        {/* Page indicator */}
        <div className="flex flex-col items-center gap-1">
          <div className="text-xs" style={{ color: 'var(--text-muted)' }}>
            Spread {currentSpread + 1} of {totalSpreads}
          </div>
          {/* Dot indicators */}
          <div className="flex gap-1">
            {Array.from({ length: Math.min(totalSpreads, 10) }).map((_, i) => {
              const spread = Math.floor((i / Math.min(totalSpreads, 10)) * totalSpreads);
              const isActive = i === Math.floor((currentSpread / totalSpreads) * Math.min(totalSpreads, 10));
              return (
                <div
                  key={i}
                  className="rounded-full transition-all duration-200"
                  style={{
                    width: isActive ? '16px' : '4px',
                    height: '4px',
                    background: isActive ? 'var(--accent-light)' : 'rgba(255,255,255,0.15)',
                  }}
                />
              );
            })}
          </div>
        </div>

        <button
          onClick={flipForward}
          disabled={isLast || isFlipping}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium transition-all"
          style={{
            background: isLast ? 'rgba(255,255,255,0.03)' : 'rgba(255,255,255,0.07)',
            border: '1px solid rgba(255,255,255,0.08)',
            color: isLast ? 'var(--text-muted)' : 'var(--text-secondary)',
            opacity: isLast ? 0.4 : 1,
          }}
        >
          Next
          <ChevronRight size={15} />
        </button>
      </div>

      {/* Keyboard hint */}
      <p className="text-xs" style={{ color: 'var(--text-muted)', opacity: 0.4 }}>
        Use ← → arrow keys to navigate
      </p>
    </div>
  );
}

function buildBlankPage(): string {
  return `<div style="width:100%;height:100%;background:#F5F0E8;"></div>`;
}
