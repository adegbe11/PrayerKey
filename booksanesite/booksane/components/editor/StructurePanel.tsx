'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, BookOpen, Check, Minus, Image } from 'lucide-react';
import type { BookData, BookPage, TrimSize, Genre, CoverConfig } from '@/types';
import CoverPanel from './CoverPanel';

// ─────────────────────────────────────────
//  PROPS
// ─────────────────────────────────────────

interface StructurePanelProps {
  bookData: BookData | null;
  pages: BookPage[];
  currentSpread: number;
  onJumpToChapter: (pageIdx: number) => void;
  trimSize: TrimSize;
  onTrimSizeChange: (size: TrimSize) => void;
  coverConfig: CoverConfig;
  genre: Genre;
  onCoverChange: (config: CoverConfig) => void;
}

// ─────────────────────────────────────────
//  SECTION HEADER
// ─────────────────────────────────────────

interface SectionHeaderProps {
  label: string;
  isOpen: boolean;
  onToggle: () => void;
  count?: number;
  icon?: React.ReactNode;
}

function SectionHeader({ label, isOpen, onToggle, count, icon }: SectionHeaderProps) {
  return (
    <button
      onClick={onToggle}
      className="flex items-center justify-between w-full px-4 py-2 transition-colors"
      style={{
        background: 'transparent',
        borderBottom: '1px solid rgba(255,255,255,0.05)',
      }}
    >
      <div className="flex items-center gap-2">
        {icon && <span style={{ opacity: 0.5, display: 'flex', alignItems: 'center' }}>{icon}</span>}
        <span
          className="font-black uppercase tracking-widest"
          style={{ fontSize: '9px', color: 'var(--text-muted)', letterSpacing: '0.13em' }}
        >
          {label}
        </span>
        {count !== undefined && (
          <span
            className="font-bold"
            style={{
              fontSize: '9px',
              background: 'rgba(124,58,237,0.18)',
              color: '#A78BFA',
              padding: '1px 5px',
              borderRadius: '3px',
            }}
          >
            {count}
          </span>
        )}
      </div>
      <motion.div
        animate={{ rotate: isOpen ? 0 : -90 }}
        transition={{ duration: 0.18 }}
      >
        <ChevronDown size={11} color="var(--text-muted)" />
      </motion.div>
    </button>
  );
}

// ─────────────────────────────────────────
//  TRIM SIZE DATA
// ─────────────────────────────────────────

const TRIM_SIZES: { value: TrimSize; label: string; subtitle: string }[] = [
  { value: '5x8',    label: '5 × 8"',    subtitle: 'Compact' },
  { value: '5.5x8.5', label: '5.5 × 8.5"', subtitle: 'Digest' },
  { value: '6x9',    label: '6 × 9"',    subtitle: 'Trade' },
  { value: 'a5',     label: 'A5',         subtitle: "Int'l" },
  { value: '8.5x11', label: '8.5 × 11"', subtitle: 'Letter' },
];

// ─────────────────────────────────────────
//  MAIN COMPONENT
// ─────────────────────────────────────────

export default function StructurePanel({
  bookData,
  pages,
  currentSpread,
  onJumpToChapter,
  trimSize,
  onTrimSizeChange,
  coverConfig,
  genre,
  onCoverChange,
}: StructurePanelProps) {
  const [coverOpen, setCoverOpen] = useState(true);
  const [frontOpen, setFrontOpen] = useState(true);
  const [chaptersOpen, setChaptersOpen] = useState(true);
  const [backOpen, setBackOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(true);

  // Determine page index of each chapter start
  const chapterPageMap: Record<string, number> = {};
  pages.forEach((page, idx) => {
    if (page.isChapterStart && page.chapterId && !(page.chapterId in chapterPageMap)) {
      chapterPageMap[page.chapterId] = idx;
    }
  });

  const hasDedication = Boolean(bookData?.dedication);
  const hasEpigraph = Boolean(bookData?.epigraph);
  const hasToc = Boolean(bookData);
  const hasTitlePage = true;
  const hasAboutAuthor = Boolean(bookData?.aboutAuthor);
  const hasAcknowledgments = Boolean(bookData?.acknowledgments);

  return (
    <div
      className="flex flex-col shrink-0 overflow-y-auto overflow-x-hidden"
      style={{
        width: '255px',
        background: '#08070F',
        borderRight: '1px solid rgba(255,255,255,0.07)',
        height: '100%',
      }}
    >
      {/* ── PANEL HEADER ── */}
      <div
        className="flex items-center gap-2 px-4 py-3 shrink-0"
        style={{ borderBottom: '1px solid rgba(255,255,255,0.07)' }}
      >
        <BookOpen size={12} color="var(--text-muted)" />
        <span
          className="font-black uppercase tracking-widest"
          style={{ fontSize: '10px', color: 'var(--text-muted)', letterSpacing: '0.15em' }}
        >
          Book Structure
        </span>
      </div>

      {/* ── COVER ── */}
      <div style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
        <SectionHeader
          label="Cover"
          isOpen={coverOpen}
          onToggle={() => setCoverOpen(v => !v)}
          icon={<Image size={11} />}
        />
        <AnimatePresence initial={false}>
          {coverOpen && (
            <motion.div
              key="cover"
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.18 }}
              style={{ overflow: 'hidden' }}
            >
              <CoverPanel
                bookData={bookData}
                genre={genre}
                coverConfig={coverConfig}
                onCoverChange={onCoverChange}
              />
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ── FRONT MATTER ── */}
      <div style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
        <SectionHeader
          label="Front Matter"
          isOpen={frontOpen}
          onToggle={() => setFrontOpen(v => !v)}
        />
        <AnimatePresence initial={false}>
          {frontOpen && (
            <motion.div
              key="front"
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.18 }}
              style={{ overflow: 'hidden' }}
            >
              <div className="py-1">
                <FrontMatterItem label="Title Page" active={hasTitlePage} />
                <FrontMatterItem label="Dedication" active={hasDedication} />
                <FrontMatterItem label="Epigraph" active={hasEpigraph} />
                <FrontMatterItem label="Table of Contents" active={hasToc} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ── CHAPTERS ── */}
      <div style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
        <SectionHeader
          label="Chapters"
          isOpen={chaptersOpen}
          onToggle={() => setChaptersOpen(v => !v)}
          count={bookData?.chapters.length ?? 0}
        />
        <AnimatePresence initial={false}>
          {chaptersOpen && (
            <motion.div
              key="chapters"
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.18 }}
              style={{ overflow: 'hidden' }}
            >
              {!bookData ? (
                <div className="px-4 py-4">
                  <p
                    className="text-center text-xs leading-relaxed"
                    style={{ color: 'var(--text-muted)', opacity: 0.6 }}
                  >
                    Format your manuscript to see chapters
                  </p>
                </div>
              ) : (
                <div className="py-1">
                  {bookData.chapters.map((chapter) => {
                    const pageIdx = chapterPageMap[chapter.id] ?? 0;
                    const spreadIdx = Math.floor(pageIdx / 2);
                    const isActive = currentSpread === spreadIdx;

                    return (
                      <button
                        key={chapter.id}
                        onClick={() => onJumpToChapter(pageIdx)}
                        className="w-full flex items-center justify-between px-4 py-2 transition-all text-left group"
                        style={{
                          background: isActive ? 'rgba(124,58,237,0.1)' : 'transparent',
                          borderLeft: isActive ? '2px solid #7C3AED' : '2px solid transparent',
                        }}
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          <span
                            className="font-black shrink-0"
                            style={{
                              fontSize: '9px',
                              color: isActive ? '#A78BFA' : 'var(--text-muted)',
                              minWidth: '16px',
                            }}
                          >
                            {chapter.number}
                          </span>
                          <div className="min-w-0">
                            <p
                              className="text-xs font-medium truncate"
                              style={{
                                color: isActive ? 'var(--text-primary)' : 'var(--text-secondary)',
                                fontSize: '11px',
                              }}
                            >
                              {chapter.title}
                            </p>
                            <p
                              className="text-xs"
                              style={{ fontSize: '10px', color: 'var(--text-muted)', opacity: 0.7 }}
                            >
                              {chapter.wordCount.toLocaleString()} words
                            </p>
                          </div>
                        </div>
                        {isActive && (
                          <div
                            className="w-1.5 h-1.5 rounded-full shrink-0 ml-2"
                            style={{ background: '#A78BFA' }}
                          />
                        )}
                      </button>
                    );
                  })}
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ── BACK MATTER ── */}
      <div style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
        <SectionHeader
          label="Back Matter"
          isOpen={backOpen}
          onToggle={() => setBackOpen(v => !v)}
        />
        <AnimatePresence initial={false}>
          {backOpen && (
            <motion.div
              key="back"
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.18 }}
              style={{ overflow: 'hidden' }}
            >
              <div className="py-1">
                <FrontMatterItem label="About the Author" active={hasAboutAuthor} />
                <FrontMatterItem label="Also By" active={false} />
                <FrontMatterItem label="Acknowledgments" active={hasAcknowledgments} />
                <FrontMatterItem label="Back Cover" active={true} />
              </div>
              {(!hasAboutAuthor || !hasAcknowledgments) && (
                <p
                  className="px-4 pb-3 text-xs leading-relaxed"
                  style={{ fontSize: '10px', color: 'var(--text-muted)', opacity: 0.5 }}
                >
                  Add an &ldquo;About the Author&rdquo; or &ldquo;Acknowledgments&rdquo; section to your manuscript to enable
                </p>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ── TRIM SIZE ── */}
      <div style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
        <SectionHeader
          label="Trim Size"
          isOpen={settingsOpen}
          onToggle={() => setSettingsOpen(v => !v)}
        />
        <AnimatePresence initial={false}>
          {settingsOpen && (
            <motion.div
              key="settings"
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.18 }}
              style={{ overflow: 'hidden' }}
            >
              <div className="py-1">
                {TRIM_SIZES.map((ts) => {
                  const isActive = trimSize === ts.value;
                  return (
                    <button
                      key={ts.value}
                      onClick={() => onTrimSizeChange(ts.value)}
                      className="w-full flex items-center justify-between px-4 py-2 transition-all"
                      style={{
                        background: isActive ? 'rgba(255,229,0,0.05)' : 'transparent',
                        borderLeft: isActive ? '2px solid #FFE500' : '2px solid transparent',
                      }}
                    >
                      <div className="flex items-center gap-2">
                        <span
                          className="font-semibold"
                          style={{
                            fontSize: '11px',
                            color: isActive ? '#FFE500' : 'var(--text-secondary)',
                            opacity: isActive ? 1 : 0.6,
                          }}
                        >
                          {ts.label}
                        </span>
                        <span
                          style={{
                            fontSize: '10px',
                            color: isActive ? '#FFE500' : 'var(--text-muted)',
                            opacity: isActive ? 0.8 : 0.45,
                          }}
                        >
                          {ts.subtitle}
                        </span>
                      </div>
                      {isActive && (
                        <Check size={11} color="#FFE500" />
                      )}
                    </button>
                  );
                })}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────
//  FRONT MATTER ITEM
// ─────────────────────────────────────────

function FrontMatterItem({ label, active }: { label: string; active: boolean }) {
  return (
    <div className="flex items-center gap-2.5 px-4 py-1.5">
      {/* Checkbox indicator */}
      <div
        className="flex items-center justify-center shrink-0"
        style={{
          width: '16px',
          height: '16px',
          borderRadius: '3px',
          background: active ? 'rgba(124,58,237,0.2)' : 'rgba(255,255,255,0.05)',
          border: `1px solid ${active ? 'rgba(124,58,237,0.4)' : 'rgba(255,255,255,0.08)'}`,
        }}
      >
        {active ? (
          <Check size={9} color="#A78BFA" strokeWidth={2.5} />
        ) : (
          <Minus size={9} color="rgba(255,255,255,0.2)" strokeWidth={2} />
        )}
      </div>
      <span
        className="text-xs"
        style={{
          fontSize: '11px',
          color: active ? 'var(--text-secondary)' : 'var(--text-muted)',
          opacity: active ? 1 : 0.5,
        }}
      >
        {label}
      </span>
    </div>
  );
}
