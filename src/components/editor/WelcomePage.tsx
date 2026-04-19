'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { BookOpen, ArrowRight, ArrowLeft, Upload } from 'lucide-react';
import type { RecentBook, NewBookMeta, Genre } from '@/types';

// ─── constants ───────────────────────────────────────────────────
const GENRE_OPTIONS: { value: Genre; label: string; sub: string; bg: string; fg: string }[] = [
  { value: 'fiction',   label: 'Fiction',          sub: 'Novel · Short Stories',  bg: '#1A1828', fg: '#fff' },
  { value: 'romance',   label: 'Romance',           sub: 'Contemporary · Historical', bg: '#4A1228', fg: '#fff' },
  { value: 'thriller',  label: 'Thriller / Mystery',sub: 'Crime · Suspense · Horror', bg: '#0A0A0A', fg: '#fff' },
  { value: 'fantasy',   label: 'Fantasy / Sci-Fi',  sub: 'Epic · Urban · Space Opera', bg: '#1A2840', fg: '#fff' },
  { value: 'memoir',    label: 'Memoir',            sub: 'Personal · Biography',    bg: '#F5EFE6', fg: '#1a1a1a' },
  { value: 'business',  label: 'Business',          sub: 'Leadership · Strategy',   bg: '#EEF2F8', fg: '#1a1a1a' },
  { value: 'selfhelp',  label: 'Self-Help',         sub: 'Motivation · Wellness',   bg: '#FFF8E0', fg: '#1a1a1a' },
  { value: 'academic',  label: 'Nonfiction',        sub: 'History · Science · Essay', bg: '#F0F0F0', fg: '#1a1a1a' },
];

const GENRE_COVER_COLOR: Record<string, string> = {
  fiction: '#1A1828', romance: '#4A0E28', thriller: '#090909',
  fantasy: '#0D1B3E', memoir: '#F7F2EA', business: '#F8F7F4',
  selfhelp: '#FFE500', academic: '#F2F2F0',
};

function formatRelativeTime(ts: number): string {
  const diff = Date.now() - ts;
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days}d ago`;
  return new Date(ts).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function loadRecentBooks(): RecentBook[] {
  if (typeof window === 'undefined') return [];
  try { return JSON.parse(localStorage.getItem('booksane_recent') || '[]') as RecentBook[]; }
  catch { return []; }
}

// ─── logo ─────────────────────────────────────────────────────────
function BooksaneIcon({ size = 72 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 48 48" fill="none">
      <path d="M24 26 C18 22, 10 20, 4 22 C8 18, 16 16, 24 20 Z" fill="#1a1a1a" />
      <path d="M24 26 C17 19, 8 15, 2 16 C6 12, 15 12, 24 18 Z" fill="#1a1a1a" opacity="0.6" />
      <path d="M24 26 C19 16, 12 10, 5 10 C9 7, 17 9, 24 16 Z" fill="#1a1a1a" opacity="0.35" />
      <path d="M24 26 C30 22, 38 20, 44 22 C40 18, 32 16, 24 20 Z" fill="#1a1a1a" />
      <path d="M24 26 C31 19, 40 15, 46 16 C42 12, 33 12, 24 18 Z" fill="#1a1a1a" opacity="0.6" />
      <path d="M24 26 C29 16, 36 10, 43 10 C39 7, 31 9, 24 16 Z" fill="#1a1a1a" opacity="0.35" />
      <rect x="21" y="22" width="6" height="16" rx="1" fill="#1a1a1a" />
      <ellipse cx="24" cy="19" rx="4.5" ry="4" fill="#1a1a1a" />
      <circle cx="25.5" cy="18.5" r="1.2" fill="#FFE500" />
      <path d="M24 21.5 L26 23 L22 23 Z" fill="#1a1a1a" />
      <path d="M21 37 L18 43 M24 38 L24 44 M27 37 L30 43" stroke="#1a1a1a" strokeWidth="1.5" strokeLinecap="round" />
      <line x1="21" y1="27" x2="27" y2="27" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="30" x2="27" y2="30" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="33" x2="27" y2="33" stroke="#FFE500" strokeWidth="0.8" />
    </svg>
  );
}

// ─── cover thumb ──────────────────────────────────────────────────
function CoverThumb({ color, title, genre }: { color: string; title: string; genre: string }) {
  const isLight = ['memoir','business','selfhelp','academic'].includes(genre);
  return (
    <div style={{
      width: 40, height: 56, background: color, borderRadius: 2, flexShrink: 0,
      border: '1px solid rgba(0,0,0,0.15)', boxShadow: '2px 2px 5px rgba(0,0,0,0.2)',
      display: 'flex', alignItems: 'flex-end', padding: '3px 4px', overflow: 'hidden', boxSizing: 'border-box',
    }}>
      <span style={{
        fontSize: 4.5, fontWeight: 700, lineHeight: 1.2, wordBreak: 'break-word',
        color: isLight ? 'rgba(0,0,0,0.6)' : 'rgba(255,255,255,0.7)',
        overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical',
      }}>{title}</span>
    </div>
  );
}

// ─── props ────────────────────────────────────────────────────────
interface WelcomePageProps {
  onNewBook: (meta: NewBookMeta) => void;
  onImportText: (text: string, filename: string) => void;
  onOpenRecent: (book: RecentBook) => void;
}

// ─── main component ───────────────────────────────────────────────
export default function WelcomePage({ onNewBook, onImportText, onOpenRecent }: WelcomePageProps) {
  const [screen, setScreen] = useState<'splash' | 'new-book'>('splash');
  const [recentBooks, setRecentBooks] = useState<RecentBook[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [importError, setImportError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // new-book form state
  const [nbTitle, setNbTitle] = useState('');
  const [nbAuthor, setNbAuthor] = useState('');
  const [nbSubtitle, setNbSubtitle] = useState('');
  const [nbGenre, setNbGenre] = useState<Genre>('fiction');
  const [nbStep, setNbStep] = useState<1 | 2>(1); // step 1: title/author, step 2: genre

  useEffect(() => {
    const books = loadRecentBooks();
    setRecentBooks(books);
    if (books.length > 0) setSelectedId(books[0].id);
  }, []);

  const handleImportClick = useCallback(() => fileInputRef.current?.click(), []);

  const handleFileChange = useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const ext = file.name.split('.').pop()?.toLowerCase();
    setImportError(null);
    if (ext === 'txt') {
      const reader = new FileReader();
      reader.onload = (ev) => { const t = ev.target?.result as string; if (t) onImportText(t, file.name); };
      reader.onerror = () => { setImportError('Could not read file. Try again.'); setTimeout(() => setImportError(null), 5000); };
      reader.readAsText(file);
    } else if (ext === 'docx') {
      try {
        const mammoth = await import('mammoth');
        const ab = await file.arrayBuffer();
        const result = await mammoth.extractRawText({ arrayBuffer: ab });
        if (result.value) {
          onImportText(result.value, file.name);
        } else {
          setImportError('DOCX appears empty. Try saving as .txt first.');
          setTimeout(() => setImportError(null), 5000);
        }
      } catch {
        setImportError('Could not read DOCX. Try saving as .txt first.');
        setTimeout(() => setImportError(null), 5000);
      }
    } else {
      setImportError('Unsupported file type. Use .txt or .docx.');
      setTimeout(() => setImportError(null), 5000);
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  }, [onImportText]);

  const handleStartWriting = useCallback(() => {
    if (!nbTitle.trim() || !nbAuthor.trim()) return;
    onNewBook({ title: nbTitle.trim(), author: nbAuthor.trim(), subtitle: nbSubtitle.trim() || undefined, genre: nbGenre });
  }, [nbTitle, nbAuthor, nbSubtitle, nbGenre, onNewBook]);

  const handleOpenSelected = useCallback(() => {
    const book = recentBooks.find(b => b.id === selectedId);
    if (book) onOpenRecent(book);
  }, [selectedId, recentBooks, onOpenRecent]);

  const hasRecent = recentBooks.length > 0;

  // ── New Book screen ────────────────────────────────────────────
  if (screen === 'new-book') {
    return (
      <div style={{
        width: '100%', height: '100vh', background: '#EDEBE5',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <div style={{
          width: 640, maxWidth: '95vw',
          background: '#fff', border: '1px solid rgba(0,0,0,0.12)',
          boxShadow: '0 8px 40px rgba(0,0,0,0.14)', borderRadius: 8, overflow: 'hidden',
        }}>
          {/* Header */}
          <div style={{ background: '#1a1a1a', padding: '20px 32px', display: 'flex', alignItems: 'center', gap: 12 }}>
            <BooksaneIcon size={28} />
            <div>
              <div style={{ fontSize: 14, fontWeight: 700, color: '#FFE500', letterSpacing: '0.1em', textTransform: 'uppercase' }}>New Book</div>
              <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.5)', marginTop: 1 }}>
                Step {nbStep} of 2 — {nbStep === 1 ? 'Book Details' : 'Choose Genre'}
              </div>
            </div>
            <button onClick={() => { setScreen('splash'); setNbStep(1); }} style={{ marginLeft: 'auto', background: 'transparent', border: 'none', cursor: 'pointer', color: 'rgba(255,255,255,0.4)', fontSize: 18, lineHeight: 1 }}>×</button>
          </div>

          <div style={{ padding: '36px 40px' }}>
            {nbStep === 1 ? (
              <>
                {/* Title */}
                <div style={{ marginBottom: 24 }}>
                  <label style={{ display: 'block', fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', textTransform: 'uppercase', color: '#888', marginBottom: 8 }}>
                    Book Title *
                  </label>
                  <input
                    autoFocus
                    value={nbTitle}
                    onChange={e => setNbTitle(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter' && nbTitle.trim() && nbAuthor.trim()) setNbStep(2); }}
                    placeholder="The Title of Your Book"
                    style={{
                      width: '100%', fontSize: 22, fontWeight: 700,
                      fontFamily: "'Playfair Display', Georgia, serif",
                      border: 'none', borderBottom: '2px solid #1a1a1a',
                      outline: 'none', background: 'transparent',
                      padding: '6px 0', color: '#1a1a1a', boxSizing: 'border-box',
                    }}
                  />
                </div>

                {/* Author */}
                <div style={{ marginBottom: 24 }}>
                  <label style={{ display: 'block', fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', textTransform: 'uppercase', color: '#888', marginBottom: 8 }}>
                    Author Name *
                  </label>
                  <input
                    value={nbAuthor}
                    onChange={e => setNbAuthor(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter' && nbTitle.trim() && nbAuthor.trim()) setNbStep(2); }}
                    placeholder="Your Name"
                    style={{
                      width: '100%', fontSize: 16,
                      border: 'none', borderBottom: '1.5px solid rgba(0,0,0,0.2)',
                      outline: 'none', background: 'transparent',
                      padding: '6px 0', color: '#1a1a1a', boxSizing: 'border-box',
                    }}
                  />
                </div>

                {/* Subtitle */}
                <div style={{ marginBottom: 36 }}>
                  <label style={{ display: 'block', fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', textTransform: 'uppercase', color: '#888', marginBottom: 8 }}>
                    Subtitle <span style={{ fontWeight: 400, textTransform: 'none', letterSpacing: 0 }}>(optional)</span>
                  </label>
                  <input
                    value={nbSubtitle}
                    onChange={e => setNbSubtitle(e.target.value)}
                    placeholder="A subtitle for your book"
                    style={{
                      width: '100%', fontSize: 14,
                      border: 'none', borderBottom: '1.5px solid rgba(0,0,0,0.12)',
                      outline: 'none', background: 'transparent',
                      padding: '6px 0', color: '#555', boxSizing: 'border-box', fontStyle: 'italic',
                    }}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <button onClick={() => { setScreen('splash'); setNbStep(1); }} style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 13, color: '#999', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <ArrowLeft size={14} /> Back
                  </button>
                  <button
                    onClick={() => setNbStep(2)}
                    disabled={!nbTitle.trim() || !nbAuthor.trim()}
                    style={{
                      background: (!nbTitle.trim() || !nbAuthor.trim()) ? '#ddd' : '#1a1a1a',
                      color: (!nbTitle.trim() || !nbAuthor.trim()) ? '#aaa' : '#FFE500',
                      border: 'none', cursor: (!nbTitle.trim() || !nbAuthor.trim()) ? 'not-allowed' : 'pointer',
                      fontSize: 13, fontWeight: 700, padding: '10px 24px', borderRadius: 4,
                      display: 'flex', alignItems: 'center', gap: 8,
                      letterSpacing: '0.05em', textTransform: 'uppercase',
                    }}
                  >
                    Choose Genre <ArrowRight size={14} />
                  </button>
                </div>
              </>
            ) : (
              <>
                <div style={{ fontSize: 13, color: '#888', marginBottom: 20 }}>
                  What genre is <strong style={{ color: '#1a1a1a' }}>{nbTitle}</strong>? This sets the best default template and formatting.
                </div>

                {/* Genre grid */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 32 }}>
                  {GENRE_OPTIONS.map(g => (
                    <button
                      key={g.value}
                      onClick={() => setNbGenre(g.value)}
                      style={{
                        background: g.bg,
                        border: nbGenre === g.value ? '2.5px solid #FFE500' : '2px solid transparent',
                        borderRadius: 6, padding: '14px 16px', cursor: 'pointer', textAlign: 'left',
                        boxShadow: nbGenre === g.value ? '0 0 0 2px #1a1a1a' : 'none',
                        transition: 'box-shadow 0.15s, border-color 0.15s',
                      }}
                    >
                      <div style={{ fontSize: 13, fontWeight: 700, color: g.fg, marginBottom: 3 }}>{g.label}</div>
                      <div style={{ fontSize: 10, color: g.fg, opacity: 0.6 }}>{g.sub}</div>
                    </button>
                  ))}
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <button onClick={() => setNbStep(1)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 13, color: '#999', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <ArrowLeft size={14} /> Back
                  </button>
                  <button
                    onClick={handleStartWriting}
                    style={{
                      background: '#FFE500', color: '#1a1a1a',
                      border: '2px solid #1a1a1a', cursor: 'pointer',
                      fontSize: 13, fontWeight: 800, padding: '11px 28px', borderRadius: 4,
                      display: 'flex', alignItems: 'center', gap: 8,
                      letterSpacing: '0.05em', textTransform: 'uppercase',
                      boxShadow: '3px 3px 0 #1a1a1a',
                    }}
                  >
                    Start Writing <ArrowRight size={14} />
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  // ── Splash screen ──────────────────────────────────────────────
  return (
    <div style={{ width: '100%', height: '100vh', background: '#EDEBE5', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
      <input ref={fileInputRef} type="file" accept=".txt,.docx" style={{ display: 'none' }} onChange={handleFileChange} />

      <div style={{
        display: 'flex',
        width: hasRecent ? 860 : 420,
        maxWidth: '95vw',
        maxHeight: '90vh',
        boxShadow: '0 8px 40px rgba(0,0,0,0.18)',
        borderRadius: 8, overflow: 'hidden',
        border: '1px solid rgba(0,0,0,0.12)',
        transition: 'width 0.3s',
      }}>
        {/* Left panel */}
        <div style={{
          background: '#EDEBE5', display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
          padding: '48px 40px', flexShrink: 0, width: hasRecent ? 340 : '100%',
        }}>
          <BooksaneIcon size={90} />
          <div style={{ textAlign: 'center', marginTop: 16, marginBottom: 28 }}>
            <div style={{ fontSize: 28, fontWeight: 900, letterSpacing: '0.12em', color: '#1a1a1a' }}>BOOKSANE</div>
            <div style={{ fontSize: 12, color: '#888', marginTop: 6 }}>The world&apos;s fastest book formatter</div>
          </div>

          <div style={{ width: '100%', maxWidth: 240, height: 1, background: 'rgba(0,0,0,0.1)', marginBottom: 18 }} />

          <div style={{ display: 'flex', flexDirection: 'column', gap: 9, alignItems: 'center', marginBottom: 18 }}>
            <TextLink icon={<BookOpen size={13} strokeWidth={1.8} />} label="Format Your First Book" onClick={() => setScreen('new-book')} />
            <TextLink icon={<Upload size={13} strokeWidth={1.8} />} label="Import a Manuscript" onClick={handleImportClick} />
          </div>

          <div style={{ width: '100%', maxWidth: 240, height: 1, background: 'rgba(0,0,0,0.1)', marginBottom: 18 }} />

          <div style={{ display: 'flex', gap: 8 }}>
            <SplashBtn primary label="New Book" onClick={() => setScreen('new-book')} />
            <SplashBtn label="Import File…" onClick={handleImportClick} />
          </div>
          {importError
            ? <div style={{ fontSize: 11, color: '#c0392b', marginTop: 8, textAlign: 'center', maxWidth: 240 }}>{importError}</div>
            : <div style={{ fontSize: 11, color: '#aaa', marginTop: 8 }}>Supports .txt and .docx files</div>
          }
        </div>

        {/* Right panel — recent books */}
        {hasRecent && (
          <div style={{ flex: 1, background: '#fff', display: 'flex', flexDirection: 'column', borderLeft: '1px solid rgba(0,0,0,0.1)', overflow: 'hidden', minHeight: 520 }}>
            <div style={{ padding: '14px 16px 10px', borderBottom: '1px solid rgba(0,0,0,0.08)', flexShrink: 0 }}>
              <span style={{ fontSize: 10, fontWeight: 700, color: '#aaa', letterSpacing: '0.1em', textTransform: 'uppercase' }}>Recent Books</span>
            </div>
            <div style={{ flex: 1, overflowY: 'auto', padding: '6px 0' }}>
              {recentBooks.map(book => (
                <RecentRow
                  key={book.id}
                  book={book}
                  isSelected={selectedId === book.id}
                  onClick={() => setSelectedId(book.id)}
                  onDoubleClick={() => onOpenRecent(book)}
                />
              ))}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 12px', borderTop: '1px solid rgba(0,0,0,0.08)', background: '#f7f6f3', flexShrink: 0 }}>
              <SplashBtn label="Open Other…" onClick={handleImportClick} />
              <SplashBtn primary label="Open Selected" onClick={handleOpenSelected} disabled={!selectedId} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── sub-components ───────────────────────────────────────────────

function SplashBtn({ label, onClick, primary, disabled }: { label: string; onClick: () => void; primary?: boolean; disabled?: boolean }) {
  const [hov, setHov] = useState(false);
  return (
    <button
      onClick={onClick} disabled={disabled}
      onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{
        background: primary ? (hov ? '#d4cf00' : '#FFE500') : (hov ? '#d4d0c9' : '#e0ddd7'),
        border: primary ? '1.5px solid rgba(0,0,0,0.3)' : '1px solid rgba(0,0,0,0.18)',
        color: disabled ? '#aaa' : '#222', fontSize: 12, fontWeight: primary ? 700 : 500,
        padding: '7px 16px', cursor: disabled ? 'not-allowed' : 'pointer', borderRadius: 4,
        opacity: disabled ? 0.6 : 1, transition: 'background 0.1s',
      }}
    >{label}</button>
  );
}

function TextLink({ icon, label, onClick }: { icon: React.ReactNode; label: string; onClick: () => void }) {
  const [hov, setHov] = useState(false);
  return (
    <button onClick={onClick} onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{ display: 'flex', alignItems: 'center', gap: 7, background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 12, color: '#777', textDecoration: hov ? 'underline' : 'none', padding: 0 }}>
      {icon}{label}
    </button>
  );
}

function RecentRow({ book, isSelected, onClick, onDoubleClick }: { book: RecentBook; isSelected: boolean; onClick: () => void; onDoubleClick: () => void }) {
  const [hov, setHov] = useState(false);
  const color = book.coverColor || GENRE_COVER_COLOR[book.genre] || '#1A1828';
  return (
    <div onClick={onClick} onDoubleClick={onDoubleClick}
      onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '9px 16px', cursor: 'pointer',
        background: isSelected ? 'rgba(99,102,241,0.1)' : hov ? 'rgba(0,0,0,0.03)' : 'transparent',
        borderLeft: isSelected ? '3px solid #6366f1' : '3px solid transparent', userSelect: 'none',
      }}>
      <CoverThumb color={color} title={book.title} genre={book.genre} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: '#1a1a1a', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{book.title}</div>
        <div style={{ fontSize: 11, color: '#888', marginTop: 1 }}>{book.genre} · {book.wordCount.toLocaleString()} words</div>
        <div style={{ fontSize: 10, color: '#bbb', marginTop: 1 }}>{formatRelativeTime(book.lastModified)}</div>
      </div>
    </div>
  );
}
