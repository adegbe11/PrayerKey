'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { BookOpen, HelpCircle } from 'lucide-react';
import type { RecentBook } from '@/types';

const GENRE_COLOR_MAP: Record<string, string> = {
  religious: '#0A1628',
  romance: '#4A0E28',
  thriller: '#090909',
  business: '#F8F7F4',
  memoir: '#F7F2EA',
  selfhelp: '#FFE500',
  poetry: '#F0EEF8',
  scifi: '#0D1B3E',
  fiction: '#1A1828',
  academic: '#F2F2F0',
};

const LIGHT_GENRES = new Set(['business', 'memoir', 'academic', 'poetry', 'selfhelp']);

interface WelcomePageProps {
  onNewBook: () => void;
  onImportText: (text: string, filename: string) => void;
  onOpenRecent: (book: RecentBook) => void;
}

function loadRecentBooks(): RecentBook[] {
  if (typeof window === 'undefined') return [];
  try {
    return JSON.parse(localStorage.getItem('booksane_recent') || '[]') as RecentBook[];
  } catch {
    return [];
  }
}

function formatRelativeTime(ts: number): string {
  const diff = Date.now() - ts;
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days} days ago`;
  return new Date(ts).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function BooksaneIcon() {
  return (
    <svg width="110" height="110" viewBox="0 0 48 48" fill="none">
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

function CoverThumb({ color, genre, title }: { color: string; genre: string; title: string }) {
  const isLight = LIGHT_GENRES.has(genre);
  return (
    <div style={{
      width: 44, height: 60, background: color, borderRadius: 2, flexShrink: 0,
      border: '1px solid rgba(0,0,0,0.15)', boxShadow: '2px 2px 6px rgba(0,0,0,0.18)',
      display: 'flex', alignItems: 'flex-end', padding: 4, overflow: 'hidden', boxSizing: 'border-box',
    }}>
      <span style={{
        fontSize: 5, fontWeight: 700, color: isLight ? '#333' : 'rgba(255,255,255,0.7)',
        lineHeight: 1.2, wordBreak: 'break-word', overflow: 'hidden',
        display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical',
      }}>
        {title}
      </span>
    </div>
  );
}

function WelcomeBtn({ label, onClick }: { label: string; onClick: () => void }) {
  const [hovered, setHovered] = useState(false);
  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        background: hovered ? '#d4d0c9' : '#e0ddd7',
        border: '1px solid rgba(0,0,0,0.18)',
        color: '#222', fontSize: 13, fontWeight: 500,
        padding: '7px 18px', cursor: 'pointer', borderRadius: 4,
        transition: 'background 0.12s',
      }}
    >
      {label}
    </button>
  );
}

function TextBtn({ icon, label, onClick }: { icon: React.ReactNode; label: string; onClick: () => void }) {
  const [hovered, setHovered] = useState(false);
  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        display: 'flex', alignItems: 'center', gap: 7,
        background: 'transparent', border: 'none', cursor: 'pointer',
        fontSize: 13, color: '#777', textDecoration: hovered ? 'underline' : 'none', padding: 0,
      }}
    >
      {icon}{label}
    </button>
  );
}

function BookRow({ book, isSelected, onClick, onDoubleClick }: {
  book: RecentBook; isSelected: boolean; onClick: () => void; onDoubleClick: () => void;
}) {
  const [hovered, setHovered] = useState(false);
  const color = book.coverColor || GENRE_COLOR_MAP[book.genre] || '#1A1828';
  return (
    <div
      onClick={onClick}
      onDoubleClick={onDoubleClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '8px 16px',
        cursor: 'pointer',
        background: isSelected ? 'rgba(99,102,241,0.12)' : hovered ? 'rgba(0,0,0,0.03)' : 'transparent',
        borderLeft: isSelected ? '3px solid #6366f1' : '3px solid transparent',
        userSelect: 'none',
      }}
    >
      <CoverThumb color={color} genre={book.genre} title={book.title} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: '#1a1a1a', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {book.title}
        </div>
        <div style={{ fontSize: 11, color: '#888', marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {book.genre} &middot; {book.wordCount.toLocaleString()} words
        </div>
        <div style={{ fontSize: 10, color: '#bbb', marginTop: 1 }}>
          {formatRelativeTime(book.lastModified)}
        </div>
      </div>
    </div>
  );
}

function BarBtn({ label, onClick, disabled }: { label: string; onClick: () => void; disabled?: boolean }) {
  const [hovered, setHovered] = useState(false);
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        background: hovered && !disabled ? '#d4d0c9' : '#dedad3',
        border: '1px solid rgba(0,0,0,0.18)',
        color: disabled ? '#bbb' : '#333',
        fontSize: 12, fontWeight: 500, padding: '6px 14px',
        cursor: disabled ? 'not-allowed' : 'pointer',
        borderRadius: 4, opacity: disabled ? 0.7 : 1,
      }}
    >
      {label}
    </button>
  );
}

export default function WelcomePage({ onNewBook, onImportText, onOpenRecent }: WelcomePageProps) {
  const [recentBooks, setRecentBooks] = useState<RecentBook[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const books = loadRecentBooks();
    setRecentBooks(books);
    if (books.length > 0) setSelectedId(books[0].id);
  }, []);

  const handleImportClick = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleFileChange = useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const ext = file.name.split('.').pop()?.toLowerCase();
    if (ext === 'txt') {
      const reader = new FileReader();
      reader.onload = (ev) => {
        const text = ev.target?.result as string;
        if (text) onImportText(text, file.name);
      };
      reader.readAsText(file);
    } else if (ext === 'docx') {
      try {
        const mammoth = await import('mammoth');
        const arrayBuffer = await file.arrayBuffer();
        const result = await mammoth.extractRawText({ arrayBuffer });
        if (result.value) onImportText(result.value, file.name);
      } catch (err) {
        console.error('docx parse error:', err);
      }
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  }, [onImportText]);

  const handleOpenSelected = useCallback(() => {
    const book = recentBooks.find((b) => b.id === selectedId);
    if (book) onOpenRecent(book);
  }, [selectedId, recentBooks, onOpenRecent]);

  const hasBooks = recentBooks.length > 0;

  const leftPanel = (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
      padding: hasBooks ? '40px 48px' : 0, width: hasBooks ? 320 : '100%', flexShrink: 0,
    }}>
      <BooksaneIcon />
      <div style={{ textAlign: 'center', marginTop: 12, marginBottom: 28 }}>
        <div style={{ fontSize: 28, fontWeight: 900, letterSpacing: '0.12em', color: '#1a1a1a', lineHeight: 1 }}>
          BOOKSANE
        </div>
        <div style={{ fontSize: 12, color: '#888', marginTop: 6 }}>
          The world&apos;s fastest book formatter
        </div>
      </div>
      <div style={{ width: '100%', maxWidth: 260, height: 1, background: 'rgba(0,0,0,0.1)', marginBottom: 20 }} />
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10, alignItems: 'center', marginBottom: 20 }}>
        <TextBtn icon={<BookOpen size={14} strokeWidth={1.8} />} label="Format Your First Book" onClick={onNewBook} />
        <TextBtn icon={<HelpCircle size={14} strokeWidth={1.8} />} label="Watch How It Works" onClick={() => {}} />
      </div>
      <div style={{ width: '100%', maxWidth: 260, height: 1, background: 'rgba(0,0,0,0.1)', marginBottom: 20 }} />
      <div style={{ display: 'flex', gap: 8, marginBottom: 10 }}>
        <WelcomeBtn label="New Book" onClick={onNewBook} />
        <WelcomeBtn label="Import File..." onClick={handleImportClick} />
      </div>
      <div style={{ fontSize: 11, color: '#aaa' }}>Supports .txt and .docx files</div>
    </div>
  );

  return (
    <div style={{ width: '100%', height: '100vh', background: '#EDEBE5', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
      <input ref={fileInputRef} type="file" accept=".txt,.docx" style={{ display: 'none' }} onChange={handleFileChange} />

      {hasBooks ? (
        <div style={{
          display: 'flex', width: 860, maxWidth: '95vw', height: 580, maxHeight: '90vh',
          boxShadow: '0 8px 40px rgba(0,0,0,0.18)', borderRadius: 8, overflow: 'hidden',
          border: '1px solid rgba(0,0,0,0.12)',
        }}>
          <div style={{ background: '#EDEBE5', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            {leftPanel}
          </div>
          <div style={{ flex: 1, background: '#FFFFFF', display: 'flex', flexDirection: 'column', borderLeft: '1px solid rgba(0,0,0,0.1)', overflow: 'hidden' }}>
            <div style={{ padding: '14px 16px 10px', borderBottom: '1px solid rgba(0,0,0,0.08)', flexShrink: 0 }}>
              <span style={{ fontSize: 11, fontWeight: 700, color: '#999', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Recent Books
              </span>
            </div>
            <div style={{ flex: 1, overflowY: 'auto', padding: '8px 0' }}>
              {recentBooks.map((book) => (
                <BookRow
                  key={book.id}
                  book={book}
                  isSelected={selectedId === book.id}
                  onClick={() => setSelectedId(book.id)}
                  onDoubleClick={() => onOpenRecent(book)}
                />
              ))}
            </div>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: '10px 12px', borderTop: '1px solid rgba(0,0,0,0.08)',
              background: '#f7f6f3', flexShrink: 0,
            }}>
              <BarBtn label="Open Other..." onClick={handleImportClick} />
              <BarBtn label="Open Selected" onClick={handleOpenSelected} disabled={!selectedId} />
            </div>
          </div>
        </div>
      ) : (
        <div>
          {leftPanel}
        </div>
      )}
    </div>
  );
}
