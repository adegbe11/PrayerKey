'use client';

import { useState, useCallback, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Link from 'next/link';
import { ArrowLeft, Star, FileDown, BookOpen, FileText } from 'lucide-react';
import InputPanel from './InputPanel';
import PaperbackPreview from './PaperbackPreview';
import EbookPreview from './EbookPreview';
import StructurePanel from './StructurePanel';
import TemplatePanel from './TemplatePanel';
import ExportModal from './ExportModal';
import { formatBook, DEMO_TEXT, detectGenre } from '@/lib/formatter';
import { getDefaultCoverForGenre } from '@/lib/covers';
import { getTemplate } from '@/lib/templates';
import { paginateBook } from '@/lib/paginator';
import type { BookData, BookPage, PreviewMode, TrimSize, Genre, CoverConfig } from '@/types';

export default function EditorApp() {
  const [rawText, setRawText] = useState('');
  const [bookData, setBookData] = useState<BookData | null>(null);
  const [pages, setPages] = useState<BookPage[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState('classic-novel');
  const [previewMode, setPreviewMode] = useState<PreviewMode>('paperback');
  const [currentSpread, setCurrentSpread] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [ebookFontSize, setEbookFontSize] = useState(16);
  const [trimSize, setTrimSize] = useState<TrimSize>('6x9');
  const [bookTitle, setBookTitle] = useState('');
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [splitRatio, setSplitRatio] = useState(0.60);
  const [coverConfig, setCoverConfig] = useState<CoverConfig>({ type: 'template', templateId: 'clean' });
  const [detectedGenre, setDetectedGenre] = useState<Genre>('fiction');
  const titleInputRef = useRef<HTMLInputElement>(null);
  const centerRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef(false);

  useEffect(() => {
    if (isEditingTitle) titleInputRef.current?.select();
  }, [isEditingTitle]);

  useEffect(() => {
    handleFormat(DEMO_TEXT);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (bookData) {
      const template = getTemplate(selectedTemplateId);
      setPages(paginateBook(bookData, template, coverConfig));
      setCurrentSpread(0);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTemplateId, bookData]);

  // Re-paginate when coverConfig changes
  useEffect(() => {
    if (bookData) {
      const template = getTemplate(selectedTemplateId);
      setPages(paginateBook(bookData, template, coverConfig));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [coverConfig]);

  const handleFormat = useCallback((text: string) => {
    if (!text.trim()) return;
    setRawText(text);
    try {
      const data = formatBook(text);
      const genre = detectGenre(text);
      const template = getTemplate(selectedTemplateId);

      setBookData(data);
      setDetectedGenre(genre);

      // Update cover template to match genre (only if not using uploaded cover)
      setCoverConfig(prev => {
        if (prev.type === 'uploaded') return prev;
        return { type: 'template', templateId: getDefaultCoverForGenre(genre) };
      });

      setPages(paginateBook(data, template, coverConfig));
      setCurrentSpread(0);
      setBookTitle(prev => prev || data.title);
    } catch (err) {
      console.error('Format error:', err);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTemplateId]);

  const handleFixMyBook = useCallback(() => {
    if (!rawText) return;
    setIsProcessing(true);
    setTimeout(() => {
      try {
        const data = formatBook(rawText);
        const genre = detectGenre(rawText);
        const template = getTemplate(selectedTemplateId);

        setBookData(data);
        setDetectedGenre(genre);
        setCoverConfig(prev => {
          if (prev.type === 'uploaded') return prev;
          return { type: 'template', templateId: getDefaultCoverForGenre(genre) };
        });
        setPages(paginateBook(data, template, coverConfig));
        setCurrentSpread(0);
      } finally {
        setIsProcessing(false);
      }
    }, 500);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rawText, selectedTemplateId]);

  const handleDragStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    isDragging.current = true;
    const onMove = (ev: MouseEvent) => {
      if (!isDragging.current || !centerRef.current) return;
      const rect = centerRef.current.getBoundingClientRect();
      const ratio = (ev.clientY - rect.top) / rect.height;
      setSplitRatio(Math.min(0.82, Math.max(0.25, ratio)));
    };
    const onUp = () => {
      isDragging.current = false;
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
  }, []);

  const totalSpreads = Math.ceil(pages.length / 2);
  const displayTitle = bookTitle || bookData?.title || 'Untitled Book';

  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ background: '#0A0910', color: 'var(--text-primary)' }}>

      {/* ─── TOP BAR ─── */}
      <div
        className="flex items-center gap-2.5 px-4 shrink-0 border-b"
        style={{ height: '52px', background: '#08070F', borderColor: 'rgba(255,255,255,0.07)' }}
      >
        {/* Back + Logo */}
        <Link href="/" className="flex items-center gap-2 group shrink-0">
          <ArrowLeft size={13} color="var(--text-muted)" className="group-hover:text-white transition-colors" />
          <div
            className="w-6 h-6 flex items-center justify-center shrink-0 font-black"
            style={{ background: '#FFE500', border: '1.5px solid rgba(0,0,0,0.5)', fontSize: '11px', color: '#000', fontFamily: 'serif' }}
          >
            B
          </div>
        </Link>

        <div className="w-px h-4 shrink-0" style={{ background: 'rgba(255,255,255,0.1)' }} />

        {/* Editable title */}
        {isEditingTitle ? (
          <input
            ref={titleInputRef}
            value={bookTitle || bookData?.title || ''}
            onChange={(e) => setBookTitle(e.target.value)}
            onBlur={() => setIsEditingTitle(false)}
            onKeyDown={(e) => e.key === 'Enter' && setIsEditingTitle(false)}
            className="text-sm font-semibold bg-transparent border-b focus:outline-none"
            style={{ color: 'var(--text-primary)', borderColor: '#FFE500', minWidth: '140px', maxWidth: '260px' }}
          />
        ) : (
          <button
            onClick={() => setIsEditingTitle(true)}
            className="text-sm font-semibold transition-opacity hover:opacity-60 truncate text-left"
            style={{ color: 'var(--text-primary)', maxWidth: '220px' }}
            title="Click to rename"
          >
            {displayTitle}
          </button>
        )}

        {/* Genre badge */}
        {detectedGenre && detectedGenre !== 'fiction' && (
          <span style={{
            fontSize: '9px',
            background: 'rgba(255,229,0,0.15)',
            color: '#FFE500',
            border: '1px solid rgba(255,229,0,0.3)',
            padding: '2px 8px',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.1em',
          }}>
            {detectedGenre}
          </span>
        )}

        {bookData?.author && (
          <span className="text-xs hidden xl:block shrink-0" style={{ color: 'var(--text-muted)' }}>
            by {bookData.author}
          </span>
        )}

        <div className="flex-1" />

        {/* Book stats */}
        {bookData && (
          <div className="hidden lg:flex items-center gap-4 text-xs shrink-0 mr-1" style={{ color: 'var(--text-muted)' }}>
            <span>{bookData.metadata.wordCount.toLocaleString()} words</span>
            <span>{bookData.metadata.estimatedPages} pages</span>
            <span>{bookData.chapters.length} chapters</span>
          </div>
        )}

        <div className="w-px h-4 shrink-0" style={{ background: 'rgba(255,255,255,0.1)' }} />

        {/* Preview mode toggle */}
        <div
          className="flex shrink-0 overflow-hidden"
          style={{ border: '1px solid rgba(255,255,255,0.1)', borderRadius: '4px' }}
        >
          {(['paperback', 'ebook'] as PreviewMode[]).map((mode, idx) => (
            <button
              key={mode}
              onClick={() => { setPreviewMode(mode); setCurrentSpread(0); }}
              className="px-3 py-1.5 text-xs font-medium transition-all"
              style={{
                background: previewMode === mode ? 'rgba(124,58,237,0.25)' : 'rgba(255,255,255,0.03)',
                color: previewMode === mode ? '#C4B5FD' : 'var(--text-muted)',
                borderRight: idx === 0 ? '1px solid rgba(255,255,255,0.08)' : 'none',
              }}
            >
              {mode === 'paperback' ? '📖 Paperback' : '📱 eBook'}
            </button>
          ))}
        </div>

        <div className="w-px h-4 shrink-0" style={{ background: 'rgba(255,255,255,0.1)' }} />

        {/* Fix My Book — neo-brutalism yellow */}
        <button
          onClick={handleFixMyBook}
          disabled={isProcessing || !bookData}
          className="flex items-center gap-1.5 text-xs font-black shrink-0 transition-all"
          style={{
            background: '#FFE500',
            color: '#000',
            border: '2px solid #000',
            boxShadow: '3px 3px 0px #000',
            padding: '5px 12px',
            opacity: (isProcessing || !bookData) ? 0.5 : 1,
          }}
          onMouseEnter={(e) => {
            if (!isProcessing && bookData) {
              const el = e.currentTarget as HTMLElement;
              el.style.transform = 'translate(-1px,-1px)';
              el.style.boxShadow = '4px 4px 0px #000';
            }
          }}
          onMouseLeave={(e) => {
            const el = e.currentTarget as HTMLElement;
            el.style.transform = '';
            el.style.boxShadow = '3px 3px 0px #000';
          }}
        >
          <Star size={10} fill="#000" color="#000" />
          {isProcessing ? 'Fixing…' : 'Fix My Book'}
        </button>

        {/* Export */}
        <button
          onClick={() => setShowExportModal(true)}
          disabled={!bookData}
          className="flex items-center gap-1.5 text-xs font-bold shrink-0 transition-all"
          style={{
            background: 'transparent',
            color: !bookData ? 'var(--text-muted)' : 'var(--text-primary)',
            border: '1px solid rgba(255,255,255,0.18)',
            padding: '5px 12px',
            opacity: !bookData ? 0.35 : 1,
            borderRadius: '3px',
          }}
        >
          <FileDown size={11} />
          Export
        </button>
      </div>

      {/* ─── MAIN 3-PANEL LAYOUT ─── */}
      <div className="flex flex-1 overflow-hidden">

        {/* LEFT: STRUCTURE PANEL */}
        <StructurePanel
          bookData={bookData}
          pages={pages}
          currentSpread={currentSpread}
          onJumpToChapter={(idx) => setCurrentSpread(Math.floor(idx / 2))}
          trimSize={trimSize}
          onTrimSizeChange={setTrimSize}
          coverConfig={coverConfig}
          genre={detectedGenre}
          onCoverChange={setCoverConfig}
        />

        {/* CENTER: PREVIEW (top) + MANUSCRIPT (bottom), split vertically */}
        <div
          ref={centerRef}
          className="flex-1 min-w-0 flex flex-col overflow-hidden"
          style={{ background: '#0A0910' }}
        >

          {/* PREVIEW ZONE */}
          <div
            className="flex flex-col overflow-hidden shrink-0"
            style={{ height: `${splitRatio * 100}%` }}
          >
            <div
              className="flex items-center justify-between px-4 shrink-0 border-b"
              style={{ height: '36px', background: '#08070F', borderColor: 'rgba(255,255,255,0.07)' }}
            >
              <div className="flex items-center gap-2">
                <BookOpen size={11} color="var(--text-muted)" />
                <span className="text-xs font-black uppercase tracking-widest" style={{ color: 'var(--text-muted)', letterSpacing: '0.14em', fontSize: '10px' }}>
                  Live Preview
                </span>
              </div>
              {bookData && pages.length > 0 && (
                <span className="text-xs" style={{ color: 'var(--text-muted)', opacity: 0.5 }}>
                  Spread {currentSpread + 1} / {totalSpreads}
                </span>
              )}
            </div>

            <div className="flex-1 overflow-hidden flex items-center justify-center" style={{ padding: '8px' }}>
              <AnimatePresence mode="wait">
                {isProcessing ? (
                  <ProcessingState key="proc" />
                ) : bookData && pages.length > 0 ? (
                  <motion.div
                    key="prev"
                    initial={{ opacity: 0, scale: 0.97 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.3 }}
                    className="w-full h-full"
                  >
                    {previewMode === 'paperback' ? (
                      <PaperbackPreview
                        pages={pages}
                        currentSpread={currentSpread}
                        onSpreadChange={setCurrentSpread}
                        totalSpreads={totalSpreads}
                      />
                    ) : (
                      <EbookPreview
                        bookData={bookData}
                        templateId={selectedTemplateId}
                        fontSize={ebookFontSize}
                        onFontSizeChange={setEbookFontSize}
                      />
                    )}
                  </motion.div>
                ) : (
                  <EmptyPreviewState key="empty" onLoadDemo={() => handleFormat(DEMO_TEXT)} />
                )}
              </AnimatePresence>
            </div>
          </div>

          {/* RESIZE HANDLE */}
          <div
            className="flex items-center justify-center shrink-0 cursor-row-resize group select-none"
            style={{ height: '10px', background: '#08070F', borderTop: '1px solid rgba(255,255,255,0.06)', borderBottom: '1px solid rgba(255,255,255,0.06)' }}
            onMouseDown={handleDragStart}
          >
            <div
              className="w-10 h-1 rounded-full transition-colors"
              style={{ background: 'rgba(255,255,255,0.08)' }}
            />
          </div>

          {/* MANUSCRIPT ZONE */}
          <div className="flex-1 min-h-0 flex flex-col overflow-hidden">
            <div
              className="flex items-center justify-between px-4 shrink-0 border-b"
              style={{ height: '36px', background: '#08070F', borderColor: 'rgba(255,255,255,0.07)' }}
            >
              <div className="flex items-center gap-2">
                <FileText size={11} color="var(--text-muted)" />
                <span className="text-xs font-black uppercase tracking-widest" style={{ color: 'var(--text-muted)', letterSpacing: '0.14em', fontSize: '10px' }}>
                  Manuscript
                </span>
              </div>
              {bookData && (
                <div className="flex items-center gap-3 text-xs" style={{ color: 'var(--text-muted)', opacity: 0.5 }}>
                  <span>{bookData.metadata.wordCount.toLocaleString()} words</span>
                  <span>{bookData.chapters.length} chapters</span>
                </div>
              )}
            </div>
            <div className="flex-1 overflow-hidden">
              <InputPanel
                rawText={rawText}
                onFormat={handleFormat}
                isProcessing={isProcessing}
                bookData={bookData}
              />
            </div>
          </div>
        </div>

        {/* RIGHT: TEMPLATE PANEL */}
        <div
          className="flex flex-col border-l overflow-hidden shrink-0"
          style={{ width: '240px', background: '#08070F', borderColor: 'rgba(255,255,255,0.07)' }}
        >
          <TemplatePanel
            selectedId={selectedTemplateId}
            onSelect={setSelectedTemplateId}
          />
        </div>
      </div>

      {/* EXPORT MODAL */}
      <AnimatePresence>
        {showExportModal && bookData && (
          <ExportModal
            bookData={bookData}
            templateId={selectedTemplateId}
            pages={pages}
            onClose={() => setShowExportModal(false)}
          />
        )}
      </AnimatePresence>
    </div>
  );
}

// ─────────────────────────────────────────
//  PROCESSING STATE
// ─────────────────────────────────────────

function ProcessingState() {
  const [step, setStep] = useState(0);
  const steps = ['Parsing manuscript…', 'Detecting chapters…', 'Applying template…', 'Rendering pages…'];

  useEffect(() => {
    const t = setInterval(() => setStep(s => (s + 1) % steps.length), 380);
    return () => clearInterval(t);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="flex flex-col items-center gap-4"
    >
      <div className="relative" style={{ width: '48px', height: '64px' }}>
        <div className="absolute inset-0 rounded-sm" style={{ background: '#F5F0E8', boxShadow: '4px 4px 0 rgba(0,0,0,0.4)' }} />
        {[0, 1, 2].map(i => (
          <motion.div
            key={i}
            className="absolute"
            style={{ bottom: `${6 + i * 5}px`, right: `-${2 + i * 2}px`, width: '100%', height: '1.5px', background: 'rgba(124,58,237,0.5)', borderRadius: '1px' }}
            animate={{ opacity: [0.2, 1, 0.2] }}
            transition={{ duration: 0.9, delay: i * 0.25, repeat: Infinity }}
          />
        ))}
      </div>
      <AnimatePresence mode="wait">
        <motion.p
          key={step}
          initial={{ opacity: 0, y: 5 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -5 }}
          transition={{ duration: 0.15 }}
          className="text-xs"
          style={{ color: 'var(--text-muted)' }}
        >
          {steps[step]}
        </motion.p>
      </AnimatePresence>
    </motion.div>
  );
}

// ─────────────────────────────────────────
//  EMPTY PREVIEW STATE
// ─────────────────────────────────────────

function EmptyPreviewState({ onLoadDemo }: { onLoadDemo: () => void }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col items-center gap-4 text-center"
    >
      <div
        style={{
          width: '40px',
          height: '56px',
          background: 'rgba(124,58,237,0.07)',
          border: '1.5px dashed rgba(124,58,237,0.2)',
          borderRadius: '2px',
        }}
      />
      <div>
        <p className="text-sm font-semibold mb-1" style={{ color: 'var(--text-secondary)' }}>
          Paste your manuscript below
        </p>
        <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
          Your formatted book appears here instantly
        </p>
      </div>
      <button
        onClick={onLoadDemo}
        className="btn btn-outline text-xs"
      >
        Load demo book
      </button>
    </motion.div>
  );
}
