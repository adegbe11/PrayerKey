'use client';

import { useState, useCallback, useEffect, useMemo, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, Star, FileDown, Eye, X, Zap, Command, Wand2, ChevronDown, Loader2 } from 'lucide-react';
import PaperbackPreview from './PaperbackPreview';
import EbookPreview from './EbookPreview';
import ExportModal from './ExportModal';
import Navigator, { buildNavItems } from './Navigator';
import type { NavItem, NavMode, StyleCategory } from './Navigator';
import ChapterEditor from './ChapterEditor';
import AiSettingsModal from './AiSettingsModal';
import CommandPalette, { type Command as PaletteCommand } from './CommandPalette';
import { formatBook, DEMO_TEXT, detectGenre } from '@/lib/formatter';
import { getDefaultCoverForGenre } from '@/lib/covers';
import { getTemplate } from '@/lib/templates';
import { paginateBook } from '@/lib/paginator';
import { useKeyboardShortcuts } from '@/lib/useKeyboardShortcuts';
import { loadGroqKey, loadGroqModel } from '@/lib/storage';
import {
  generateBlurbSmart,
  polishChapterTitles,
  extractCharacters,
  grammarCheck,
  suggestTitles,
} from '@/lib/aiAssist';
import type { BookData, BookPage, PreviewMode, TrimSize, Genre, CoverConfig, RecentBook, NewBookMeta } from '@/types';

const GENRE_COLOR_MAP: Record<string, string> = {
  religious: '#0A1628', romance: '#4A0E28', thriller: '#090909',
  business: '#F8F7F4', memoir: '#F7F2EA', selfhelp: '#FFE500',
  poetry: '#F0EEF8', scifi: '#0D1B3E', fiction: '#1A1828', academic: '#F2F2F0',
};

interface EditorAppProps {
  initialText?: string;
  initialFilename?: string;
  initialMeta?: NewBookMeta | null;
  onGoHome?: () => void;
}

// ─── Create a blank book from NewBookMeta ───
function createBlankBook(meta: NewBookMeta): BookData {
  return {
    title: meta.title,
    author: meta.author,
    subtitle: meta.subtitle,
    dedication: meta.dedication,
    epigraph: meta.epigraph,
    genre: meta.genre,
    chapters: [
      {
        id: crypto.randomUUID(),
        number: 1,
        title: 'Chapter One',
        content: '<p>Begin your story here.</p>',
        wordCount: 4,
      },
    ],
    metadata: {
      wordCount: 4,
      estimatedPages: 1,
      genre: meta.genre,
    },
  };
}

export default function EditorApp({ initialText = '', initialFilename = '', initialMeta = null, onGoHome }: EditorAppProps) {
  // ─── core state (preserved) ───
  const [rawText, setRawText] = useState('');
  const [bookData, setBookData] = useState<BookData | null>(null);
  const [pages, setPages] = useState<BookPage[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState('knopf');
  const [previewMode, setPreviewMode] = useState<PreviewMode>('paperback');
  const [currentSpread, setCurrentSpread] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [ebookFontSize, setEbookFontSize] = useState(16);
  // trimSize kept for potential future use / ExportModal compatibility
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [trimSize, setTrimSize] = useState<TrimSize>('6x9');
  const [bookTitle, setBookTitle] = useState('');
  const [coverConfig, setCoverConfig] = useState<CoverConfig>({ type: 'template', templateId: 'clean' });
  const [detectedGenre, setDetectedGenre] = useState<Genre>('fiction');
  const titleInputRef = useRef<HTMLInputElement>(null);

  // ─── new UI state ───
  const [selectedItemId, setSelectedItemId] = useState('title-page');
  const [navMode, setNavMode] = useState<NavMode>('contents');
  const [styleCategory, setStyleCategory] = useState<StyleCategory>('All');
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [addedElements, setAddedElements] = useState<NavItem[]>([]);
  const [navItems, setNavItems] = useState<NavItem[]>(() => buildNavItems(null, []));

  // ─── preview drawer state ───
  const [showPreview, setShowPreview] = useState(false);

  // ─── AI state ───
  const [showAiSettings, setShowAiSettings] = useState(false);
  const [showPalette, setShowPalette] = useState(false);
  const [showAiMenu, setShowAiMenu] = useState(false);
  const [aiBusy, setAiBusy] = useState<string | null>(null);
  const [aiToast, setAiToast] = useState<{ title: string; body: string } | null>(null);

  const runAi = useCallback(async <T,>(taskId: string, task: () => Promise<T>): Promise<T | null> => {
    setAiBusy(taskId);
    try {
      return await task();
    } catch (err) {
      setAiToast({
        title: 'AI error',
        body: err instanceof Error ? err.message : 'Request failed',
      });
      return null;
    } finally {
      setAiBusy(null);
    }
  }, []);

  const handleAiBlurb = useCallback(async () => {
    if (!bookData) return;
    const key = loadGroqKey();
    const model = loadGroqModel();
    const result = await runAi('blurb', () => generateBlurbSmart(bookData, { apiKey: key, model }));
    if (!result) return;
    const body = `Tagline:\n${result.tagline}\n\nBlurb:\n${result.blurb}`;
    try {
      await navigator.clipboard.writeText(body);
      setAiToast({ title: 'Blurb copied to clipboard', body });
    } catch {
      setAiToast({ title: 'Blurb generated', body });
    }
  }, [bookData, runAi]);

  const handleAiTitles = useCallback(async () => {
    if (!bookData) return;
    const key = loadGroqKey();
    if (!key) { setShowAiSettings(true); return; }
    const model = loadGroqModel();
    const result = await runAi('titles', () => suggestTitles(bookData, { apiKey: key, model }));
    if (!result) return;
    const body = `Titles:\n${result.titles.map((t, i) => `${i + 1}. ${t}`).join('\n')}\n\nSubtitles:\n${result.subtitles.map((t, i) => `${i + 1}. ${t}`).join('\n')}`;
    setAiToast({ title: 'Title ideas', body });
  }, [bookData, runAi]);

  const handleAiPolishChapters = useCallback(async () => {
    if (!bookData) return;
    const key = loadGroqKey();
    if (!key) { setShowAiSettings(true); return; }
    const model = loadGroqModel();
    const map = await runAi('polish', () => polishChapterTitles(bookData, { apiKey: key, model }));
    if (!map) return;
    const changed = Object.keys(map).length;
    if (!changed) {
      setAiToast({ title: 'Nothing to polish', body: 'All chapters already have custom titles.' });
      return;
    }
    setBookData((bd) => {
      if (!bd) return bd;
      return {
        ...bd,
        chapters: bd.chapters.map((c) => (map[c.id] ? { ...c, title: map[c.id] } : c)),
      };
    });
    setAiToast({ title: `Polished ${changed} chapter title${changed > 1 ? 's' : ''}`, body: 'Applied to your book.' });
  }, [bookData, runAi]);

  const handleAiCharacters = useCallback(async () => {
    if (!bookData) return;
    const key = loadGroqKey();
    if (!key) { setShowAiSettings(true); return; }
    const model = loadGroqModel();
    const cast = await runAi('cast', () => extractCharacters(bookData, { apiKey: key, model }));
    if (!cast) return;
    const body = cast.length
      ? cast.map((c) => `\u2022 ${c.name}${c.role ? ` (${c.role})` : ''} \u2014 ${c.description}`).join('\n')
      : 'No named characters detected.';
    setAiToast({ title: `Character cast (${cast.length})`, body });
  }, [bookData, runAi]);

  const handleAiGrammar = useCallback(async () => {
    if (!bookData) return;
    const key = loadGroqKey();
    if (!key) { setShowAiSettings(true); return; }
    const model = loadGroqModel();
    const issues = await runAi('grammar', () => grammarCheck(bookData, { apiKey: key, model }));
    if (!issues) return;
    const body = issues.length
      ? issues.slice(0, 20).map((i) => `[${i.severity}] "${i.excerpt}"\n  \u2192 ${i.suggestion}\n  ${i.reason}`).join('\n\n')
      : 'No issues detected.';
    setAiToast({ title: `Grammar & style (${issues.length} issues)`, body });
  }, [bookData, runAi]);

  // handleFixMyBook is declared further down; we reference it inside closures so TDZ won't trip.
  const paletteCommands: PaletteCommand[] = useMemo(() => [
    { id: 'ai-blurb',    label: 'Write blurb & tagline (copy)', hint: 'AI back-cover copy', group: 'AI', icon: <Wand2 size={12} />, run: () => handleAiBlurb() },
    { id: 'ai-titles',   label: 'Suggest titles',               hint: 'Title + subtitle ideas', group: 'AI', icon: <Wand2 size={12} />, run: () => handleAiTitles() },
    { id: 'ai-polish',   label: 'Polish chapter titles',        hint: 'Rewrite generic chapter names', group: 'AI', icon: <Wand2 size={12} />, run: () => handleAiPolishChapters() },
    { id: 'ai-cast',     label: 'Extract character cast',       hint: 'Auto-index characters', group: 'AI', icon: <Wand2 size={12} />, run: () => handleAiCharacters() },
    { id: 'ai-grammar',  label: 'Grammar & style pass',         hint: 'Find issues to fix', group: 'AI', icon: <Wand2 size={12} />, run: () => handleAiGrammar() },
    { id: 'ai-settings', label: 'AI settings\u2026',            hint: 'Connect Groq API', group: 'AI', icon: <Zap size={12} />, run: () => setShowAiSettings(true) },
    { id: 'export',      label: 'Export book\u2026',            hint: 'PDF, EPUB, DOCX', shortcut: '\u2318E', group: 'Actions', icon: <FileDown size={12} />, run: () => { if (bookData) setShowExportModal(true); } },
    { id: 'preview',     label: 'Toggle preview',               shortcut: '\u2318P', group: 'Actions', icon: <Eye size={12} />, run: () => setShowPreview((v) => !v) },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [bookData, handleAiBlurb, handleAiTitles, handleAiPolishChapters, handleAiCharacters, handleAiGrammar]);

  useKeyboardShortcuts([
    { combo: 'mod+k', handler: () => setShowPalette(true) },
    { combo: 'mod+e', handler: () => { if (bookData) setShowExportModal(true); } },
    { combo: 'mod+p', handler: () => setShowPreview((v) => !v) },
  ]);

  useEffect(() => {
    if (isEditingTitle) titleInputRef.current?.select();
  }, [isEditingTitle]);

  // ─── initial load ───
  useEffect(() => {
    if (initialMeta) {
      // New book from metadata — start blank
      const data = createBlankBook(initialMeta);
      const coverTmplId = getDefaultCoverForGenre(initialMeta.genre);
      const cover: CoverConfig = { type: 'template', templateId: coverTmplId };
      const template = getTemplate('knopf');
      setBookData(data);
      setDetectedGenre(initialMeta.genre);
      setBookTitle(initialMeta.title);
      setCoverConfig(cover);
      setPages(paginateBook(data, template, cover));
      setCurrentSpread(0);
      const items = buildNavItems(data);
      const firstCh = items.find(i => i.type === 'chapter');
      if (firstCh) setSelectedItemId(firstCh.id);
    } else {
      const textToLoad = initialText || DEMO_TEXT;
      if (initialFilename) setBookTitle(initialFilename.replace(/\.(txt|docx)$/i, ''));
      handleFormat(textToLoad);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ─── auto-save to localStorage when bookData changes ───
  useEffect(() => {
    if (!bookData || !rawText) return;
    try {
      const stored = JSON.parse(localStorage.getItem('booksane_recent') || '[]') as RecentBook[];
      const existing = stored.findIndex(b => b.title === bookData.title && b.author === bookData.author);

      const entry: RecentBook = {
        id: existing >= 0 ? stored[existing].id : crypto.randomUUID(),
        title: bookTitle || bookData.title,
        author: bookData.author,
        wordCount: bookData.metadata.wordCount,
        genre: detectedGenre,
        coverColor: GENRE_COLOR_MAP[detectedGenre] ?? '#1A1828',
        lastModified: Date.now(),
        rawText,
      };

      const updated = existing >= 0
        ? [entry, ...stored.filter((_, i) => i !== existing)]
        : [entry, ...stored];

      localStorage.setItem('booksane_recent', JSON.stringify(updated.slice(0, 8)));
    } catch {
      // ignore localStorage errors
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookData]);

  // ─── re-paginate when template or bookData changes ───
  useEffect(() => {
    if (bookData) {
      const template = getTemplate(selectedTemplateId);
      setPages(paginateBook(bookData, template, coverConfig));
      setCurrentSpread(0);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTemplateId, bookData]);

  // ─── re-paginate when coverConfig changes ───
  useEffect(() => {
    if (bookData) {
      const template = getTemplate(selectedTemplateId);
      setPages(paginateBook(bookData, template, coverConfig));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [coverConfig]);

  // ─── handleFormat (preserved) ───
  const handleFormat = useCallback((text: string) => {
    if (!text.trim()) return;
    setRawText(text);
    try {
      const data = formatBook(text);
      const genre = detectGenre(text);
      const template = getTemplate(selectedTemplateId);

      setBookData(data);
      setDetectedGenre(genre);

      setCoverConfig(prev => {
        if (prev.type === 'uploaded') return prev;
        return { type: 'template', templateId: getDefaultCoverForGenre(genre) };
      });

      setPages(paginateBook(data, template, coverConfig));
      setCurrentSpread(0);
      setBookTitle(prev => prev || data.title);

      // Auto-select first chapter in navigator
      const navItems = buildNavItems(data);
      const firstChapter = navItems.find(item => item.type === 'chapter');
      if (firstChapter) {
        setSelectedItemId(firstChapter.id);
      }
    } catch (err) {
      console.error('Format error:', err);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTemplateId]);

  // ─── handleFixMyBook (preserved) ───
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

  // ─── sync navItems when bookData or addedElements changes ───
  useEffect(() => {
    setNavItems(prev => {
      const fresh = buildNavItems(bookData, addedElements);
      if (prev.length === 0) return fresh;
      const prevIds = new Set(prev.map(i => i.id));
      const freshMap = new Map(fresh.map(i => [i.id, i]));
      const kept = prev.filter(i => freshMap.has(i.id)).map(i => freshMap.get(i.id)!);
      const added = fresh.filter(i => !prevIds.has(i.id));
      return [...kept, ...added];
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookData, addedElements]);

  const handleAddElement = useCallback((item: NavItem) => {
    setAddedElements(prev => prev.some(e => e.type === item.type) ? prev : [...prev, item]);
    setSelectedItemId(item.id);
  }, []);

  const handleReorder = useCallback((items: NavItem[]) => {
    setNavItems(items);
  }, []);

  // ─── nav selection ───
  const handleSelectNavItem = useCallback((item: NavItem) => {
    setSelectedItemId(item.id);
    if (item.type === 'chapter' && item.chapterIdx !== undefined && bookData) {
      const chId = bookData.chapters[item.chapterIdx]?.id;
      if (chId) {
        const pageIdx = pages.findIndex(p => p.chapterId === chId);
        if (pageIdx >= 0) setCurrentSpread(Math.floor(pageIdx / 2));
      }
    }
  }, [pages, bookData]);

  // ─── section field update ───
  // Known top-level fields update directly; everything else goes into bookData.extras
  const KNOWN_FIELDS = new Set(['title', 'author', 'subtitle', 'dedication', 'epigraph', 'epigraphAttribution', 'acknowledgments', 'aboutAuthor']);
  const handleUpdateSection = useCallback((field: string, value: string) => {
    setBookData(prev => {
      if (!prev) return prev;
      if (KNOWN_FIELDS.has(field)) return { ...prev, [field]: value };
      return { ...prev, extras: { ...(prev.extras ?? {}), [field]: value } };
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ─── chapter content update ───
  const handleUpdateChapterContent = useCallback((chapterIdx: number, newContent: string) => {
    setBookData(prev => {
      if (!prev) return prev;
      const chapters = [...prev.chapters];
      const words = newContent.replace(/<[^>]*>/g, ' ').trim().split(/\s+/).filter(Boolean).length;
      chapters[chapterIdx] = { ...chapters[chapterIdx], content: newContent, wordCount: words };
      const totalWords = chapters.reduce((sum, ch) => sum + ch.wordCount, 0);
      return { ...prev, chapters, metadata: { ...prev.metadata, wordCount: totalWords } };
    });
  }, []);

  const totalSpreads = Math.ceil(pages.length / 2);
  const displayTitle = bookTitle || bookData?.title || 'Untitled Book';

  // Build current selected nav item
  const selectedItem = navItems.find(item => item.id === selectedItemId) ?? null;

  // Word count
  const wordCount = bookData?.metadata.wordCount ?? 0;

  return (
    <div
      style={{
        background: '#F5F2EC',
        height: '100vh',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* ─── TOP BAR ─── */}
      <div
        style={{
          height: 52,
          background: '#F0EDE7',
          borderBottom: '1px solid rgba(0,0,0,0.1)',
          flexShrink: 0,
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          padding: '0 16px',
        }}
      >
        {/* Back + Logo */}
        <button
          onClick={() => onGoHome?.()}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            padding: 0,
            flexShrink: 0,
          }}
        >
          <ArrowLeft size={13} color="#999" />
          <div
            style={{
              width: 24,
              height: 24,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: '#FFE500',
              border: '1.5px solid rgba(0,0,0,0.4)',
              fontSize: 11,
              fontWeight: 900,
              color: '#000',
              fontFamily: 'serif',
              flexShrink: 0,
            }}
          >
            B
          </div>
        </button>

        {/* Divider */}
        <div style={{ width: 1, height: 18, background: 'rgba(0,0,0,0.1)', flexShrink: 0 }} />

        {/* Editable title */}
        {isEditingTitle ? (
          <input
            ref={titleInputRef}
            value={bookTitle || bookData?.title || ''}
            onChange={(e) => setBookTitle(e.target.value)}
            onBlur={() => setIsEditingTitle(false)}
            onKeyDown={(e) => e.key === 'Enter' && setIsEditingTitle(false)}
            style={{
              fontSize: 13,
              fontWeight: 600,
              background: 'transparent',
              border: 'none',
              borderBottom: '1.5px solid #FFE500',
              outline: 'none',
              color: '#1a1a1a',
              minWidth: 140,
              maxWidth: 260,
            }}
          />
        ) : (
          <button
            onClick={() => setIsEditingTitle(true)}
            title="Click to rename"
            style={{
              fontSize: 13,
              fontWeight: 600,
              color: '#1a1a1a',
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
              padding: 0,
              maxWidth: 220,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {displayTitle}
          </button>
        )}

        {/* Edited status */}
        <span style={{ fontSize: 11, color: '#aaa', flexShrink: 0 }}>Edited</span>

        {/* Genre badge */}
        {detectedGenre && detectedGenre !== 'fiction' && (
          <span
            style={{
              fontSize: 9,
              background: 'rgba(0,0,0,0.07)',
              color: '#555',
              border: '1px solid rgba(0,0,0,0.15)',
              padding: '2px 8px',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.1em',
              flexShrink: 0,
            }}
          >
            {detectedGenre}
          </span>
        )}

        <div style={{ flex: 1 }} />

        {/* Word count */}
        <span style={{ fontSize: 11, color: '#999', flexShrink: 0 }}>
          {wordCount.toLocaleString()} Words
        </span>

        {/* Divider */}
        <div style={{ width: 1, height: 18, background: 'rgba(0,0,0,0.1)', flexShrink: 0 }} />

        {/* Command palette trigger */}
        <button
          onClick={() => setShowPalette(true)}
          title="Command palette (\u2318K)"
          style={{
            display: 'flex', alignItems: 'center', gap: 4,
            border: '1px solid rgba(0,0,0,0.18)',
            background: 'transparent',
            color: '#333',
            fontSize: 11,
            padding: '5px 10px',
            borderRadius: 3,
            cursor: 'pointer',
            fontWeight: 600,
            flexShrink: 0,
          }}
        >
          <Command size={11} />
          K
        </button>

        {/* AI menu trigger */}
        <div style={{ position: 'relative', flexShrink: 0 }}>
          <button
            onClick={() => setShowAiMenu((v) => !v)}
            title="AI Studio"
            style={{
              display: 'flex', alignItems: 'center', gap: 4,
              border: '1px solid rgba(0,0,0,0.18)',
              background: showAiMenu ? 'linear-gradient(135deg,#7C3AED,#EC4899)' : 'transparent',
              color: showAiMenu ? '#fff' : '#333',
              fontSize: 11,
              padding: '5px 10px',
              borderRadius: 3,
              cursor: 'pointer',
              fontWeight: 700,
            }}
          >
            <Zap size={11} />
            AI
            <ChevronDown size={9} />
          </button>
          <AnimatePresence>
            {showAiMenu && (
              <>
                <div
                  onClick={() => setShowAiMenu(false)}
                  style={{ position: 'fixed', inset: 0, zIndex: 40 }}
                />
                <motion.div
                  initial={{ opacity: 0, y: -4, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -4, scale: 0.98 }}
                  transition={{ duration: 0.15, ease: [0.16, 1, 0.3, 1] }}
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 6px)',
                    right: 0,
                    zIndex: 50,
                    background: 'rgba(20,18,32,0.94)',
                    color: '#F0EEFE',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: 12,
                    boxShadow: '0 20px 50px rgba(0,0,0,0.4)',
                    backdropFilter: 'blur(24px) saturate(160%)',
                    WebkitBackdropFilter: 'blur(24px) saturate(160%)',
                    padding: 6,
                    width: 240,
                    fontSize: 12,
                  }}
                >
                  {([
                    { id: 'blurb',   label: 'Write blurb & tagline',  run: () => handleAiBlurb() },
                    { id: 'titles',  label: 'Suggest titles',         run: () => handleAiTitles() },
                    { id: 'polish',  label: 'Polish chapter titles',  run: () => handleAiPolishChapters() },
                    { id: 'cast',    label: 'Extract character cast', run: () => handleAiCharacters() },
                    { id: 'grammar', label: 'Grammar & style pass',   run: () => handleAiGrammar() },
                  ] as const).map((item) => (
                    <button
                      key={item.id}
                      onClick={() => { setShowAiMenu(false); item.run(); }}
                      disabled={!bookData || aiBusy !== null}
                      style={{
                        width: '100%',
                        display: 'flex', alignItems: 'center', gap: 8,
                        padding: '8px 10px',
                        background: 'transparent',
                        border: 'none',
                        color: 'var(--text-primary, #F0EEFE)',
                        borderRadius: 8,
                        cursor: !bookData || aiBusy ? 'wait' : 'pointer',
                        textAlign: 'left',
                        fontSize: 12,
                        opacity: !bookData ? 0.45 : 1,
                      }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'rgba(255,255,255,0.06)'; }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                    >
                      {aiBusy === item.id ? <Loader2 size={12} className="animate-spin" /> : <Wand2 size={12} />}
                      {item.label}
                    </button>
                  ))}
                  <div style={{ height: 1, background: 'rgba(255,255,255,0.08)', margin: '4px 6px' }} />
                  <button
                    onClick={() => { setShowAiMenu(false); setShowAiSettings(true); }}
                    style={{
                      width: '100%',
                      display: 'flex', alignItems: 'center', gap: 8,
                      padding: '8px 10px',
                      background: 'transparent',
                      border: 'none',
                      color: '#A78BFA',
                      borderRadius: 8,
                      cursor: 'pointer',
                      textAlign: 'left',
                      fontSize: 12,
                    }}
                    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'rgba(124,58,237,0.15)'; }}
                    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    <Zap size={12} />
                    AI settings&hellip;
                  </button>
                </motion.div>
              </>
            )}
          </AnimatePresence>
        </div>

        {/* Preview button */}
        <button
          onClick={() => setShowPreview((v) => !v)}
          disabled={!bookData}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            border: '1px solid rgba(0,0,0,0.18)',
            background: showPreview ? '#000' : 'transparent',
            color: showPreview ? '#fff' : (bookData ? '#333' : '#bbb'),
            fontSize: 11,
            padding: '5px 12px',
            borderRadius: 3,
            cursor: bookData ? 'pointer' : 'not-allowed',
            fontWeight: 600,
            flexShrink: 0,
            transition: 'background 0.12s, color 0.12s',
          }}
        >
          <Eye size={11} />
          Preview
        </button>

        {/* Export button */}
        <button
          onClick={() => setShowExportModal(true)}
          disabled={!bookData}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            border: '1px solid rgba(0,0,0,0.18)',
            background: 'transparent',
            color: bookData ? '#333' : '#bbb',
            fontSize: 11,
            padding: '5px 12px',
            borderRadius: 3,
            cursor: bookData ? 'pointer' : 'not-allowed',
            fontWeight: 600,
            flexShrink: 0,
          }}
        >
          <FileDown size={11} />
          Export
        </button>

        {/* Generate / Re-Format — only useful when rawText exists (imported books) */}
        {rawText && (
          <button
            onClick={handleFixMyBook}
            disabled={isProcessing}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              background: '#FFE500',
              border: '2px solid #000',
              boxShadow: '3px 3px 0 #000',
              padding: '6px 18px',
              fontWeight: 900,
              fontSize: 12,
              color: '#000',
              cursor: isProcessing ? 'not-allowed' : 'pointer',
              opacity: isProcessing ? 0.5 : 1,
              flexShrink: 0,
              transition: 'transform 0.1s, box-shadow 0.1s',
            }}
            onMouseEnter={(e) => {
              if (!isProcessing) {
                const el = e.currentTarget as HTMLElement;
                el.style.transform = 'translate(-1px,-1px)';
                el.style.boxShadow = '4px 4px 0 #000';
              }
            }}
            onMouseLeave={(e) => {
              const el = e.currentTarget as HTMLElement;
              el.style.transform = '';
              el.style.boxShadow = '3px 3px 0 #000';
            }}
          >
            <Star size={10} fill="#000" color="#000" />
            {isProcessing ? 'Fixing…' : 'Re-Format'}
          </button>
        )}
      </div>

      {/* ─── MAIN 2-PANEL AREA ─── */}
      <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>

        {/* LEFT: Navigator */}
        <Navigator
          bookData={bookData}
          navItems={navItems}
          selectedId={selectedItemId}
          onSelect={handleSelectNavItem}
          onReorder={handleReorder}
          navMode={navMode}
          onNavModeChange={setNavMode}
          styleCategory={styleCategory}
          onStyleCategoryChange={setStyleCategory}
          onAddElement={handleAddElement}
        />

        {/* CENTER: Chapter Editor */}
        <ChapterEditor
          bookData={bookData}
          selectedItem={selectedItem}
          navMode={navMode}
          styleCategory={styleCategory}
          selectedStyleId={selectedTemplateId}
          onSelectStyle={setSelectedTemplateId}
          onUpdateChapterContent={handleUpdateChapterContent}
          onUpdateSection={handleUpdateSection}
          coverConfig={coverConfig}
          onCoverChange={setCoverConfig}
          detectedGenre={detectedGenre}
        />
      </div>

      {/* ─── PREVIEW DRAWER (fixed overlay, slides from right) ─── */}
      <AnimatePresence>
        {showPreview && bookData && pages.length > 0 && (
          <motion.div
            initial={{ x: 400, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            exit={{ x: 400, opacity: 0 }}
            transition={{ type: 'spring', damping: 28, stiffness: 300 }}
            style={{
              position: 'fixed',
              right: 0,
              top: 52,
              bottom: 0,
              width: 380,
              background: '#E8E5DE',
              borderLeft: '1px solid rgba(0,0,0,0.12)',
              zIndex: 30,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
            }}
          >
            {/* Preview Drawer Header */}
            <div
              style={{
                height: 40,
                display: 'flex',
                alignItems: 'center',
                padding: '0 14px',
                borderBottom: '1px solid rgba(0,0,0,0.08)',
                background: '#E0DDD7',
                gap: 6,
                flexShrink: 0,
              }}
            >
              {/* Preview mode toggle pills */}
              <div
                style={{
                  display: 'flex',
                  background: 'rgba(0,0,0,0.07)',
                  borderRadius: 4,
                  padding: 2,
                  gap: 1,
                }}
              >
                {(['paperback', 'ebook'] as PreviewMode[]).map((mode) => (
                  <button
                    key={mode}
                    onClick={() => { setPreviewMode(mode); setCurrentSpread(0); }}
                    style={{
                      padding: '3px 10px',
                      fontSize: 10,
                      fontWeight: previewMode === mode ? 700 : 500,
                      color: previewMode === mode ? '#1a1a1a' : '#888',
                      background: previewMode === mode ? '#fff' : 'transparent',
                      border: 'none',
                      borderRadius: 3,
                      cursor: 'pointer',
                      boxShadow: previewMode === mode ? '0 1px 2px rgba(0,0,0,0.1)' : 'none',
                      transition: 'all 0.15s',
                    }}
                  >
                    {mode === 'paperback' ? 'Paperback' : 'eBook'}
                  </button>
                ))}
              </div>

              <div style={{ flex: 1 }} />

              {/* Spread indicator */}
              {pages.length > 0 && (
                <span style={{ fontSize: 10, color: '#999' }}>
                  Spread {currentSpread + 1} / {totalSpreads}
                </span>
              )}

              {/* Close button */}
              <button
                onClick={() => setShowPreview(false)}
                style={{
                  width: 24,
                  height: 24,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  color: '#888',
                  borderRadius: 4,
                  flexShrink: 0,
                }}
              >
                <X size={13} />
              </button>
            </div>

            {/* Preview body */}
            <div
              style={{
                flex: 1,
                overflow: 'hidden',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: 8,
              }}
            >
              <AnimatePresence mode="wait">
                {isProcessing ? (
                  <ProcessingState key="proc" />
                ) : (
                  <motion.div
                    key="prev"
                    initial={{ opacity: 0, scale: 0.97 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.25 }}
                    style={{ width: '100%', height: '100%' }}
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
                )}
              </AnimatePresence>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* EXPORT MODAL */}
      <AnimatePresence>
        {showExportModal && bookData && (
          <ExportModal
            bookData={bookData}
            templateId={selectedTemplateId}
            pages={pages}
            onClose={() => setShowExportModal(false)}
            onTemplateChange={setSelectedTemplateId}
          />
        )}
      </AnimatePresence>

      {/* COMMAND PALETTE */}
      <AnimatePresence>
        {showPalette && (
          <CommandPalette
            open={showPalette}
            commands={paletteCommands}
            onClose={() => setShowPalette(false)}
          />
        )}
      </AnimatePresence>

      {/* AI SETTINGS */}
      <AnimatePresence>
        {showAiSettings && (
          <AiSettingsModal
            open={showAiSettings}
            onClose={() => setShowAiSettings(false)}
          />
        )}
      </AnimatePresence>

      {/* AI RESULT TOAST */}
      <AnimatePresence>
        {aiToast && (
          <motion.div
            initial={{ opacity: 0, y: 14, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 14 }}
            transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
            className="fixed z-[62] bottom-5 right-5 max-w-md"
            style={{
              background: 'rgba(15,13,27,0.92)',
              color: '#F0EEFE',
              border: '1px solid rgba(255,255,255,0.1)',
              borderRadius: '14px',
              boxShadow: '0 24px 60px rgba(0,0,0,0.5)',
              backdropFilter: 'blur(24px) saturate(160%)',
              WebkitBackdropFilter: 'blur(24px) saturate(160%)',
              padding: '14px 16px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
              <div
                style={{
                  width: 28, height: 28, borderRadius: 8,
                  background: 'linear-gradient(135deg,#7C3AED,#EC4899)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                }}
              >
                <Zap size={13} color="#fff" />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: 700, marginBottom: 4 }}>
                  {aiToast.title}
                </div>
                <div style={{ fontSize: 11, whiteSpace: 'pre-wrap', lineHeight: 1.5, color: 'rgba(255,255,255,0.75)', maxHeight: 320, overflowY: 'auto' }}>
                  {aiToast.body}
                </div>
              </div>
              <button
                onClick={() => setAiToast(null)}
                style={{ color: 'rgba(255,255,255,0.5)', background: 'transparent', border: 'none', cursor: 'pointer', padding: 2 }}
              >
                <X size={13} />
              </button>
            </div>
          </motion.div>
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
      style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}
    >
      <div style={{ position: 'relative', width: 40, height: 56 }}>
        <div
          style={{
            position: 'absolute',
            inset: 0,
            borderRadius: 2,
            background: '#F5F0E8',
            boxShadow: '3px 3px 0 rgba(0,0,0,0.15)',
          }}
        />
        {[0, 1, 2].map((i) => (
          <motion.div
            key={i}
            style={{
              position: 'absolute',
              bottom: `${6 + i * 5}px`,
              right: `${-2 - i * 2}px`,
              width: '100%',
              height: 1.5,
              background: 'rgba(0,0,0,0.3)',
              borderRadius: 1,
            }}
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
          style={{ fontSize: 11, color: '#888', margin: 0 }}
        >
          {steps[step]}
        </motion.p>
      </AnimatePresence>
    </motion.div>
  );
}

