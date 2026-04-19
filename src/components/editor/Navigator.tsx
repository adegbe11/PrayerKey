'use client';

import { useState, useRef, useEffect, useCallback } from 'react';
import { Plus, X, GripVertical, Trash2 } from 'lucide-react';
import type { BookData } from '@/types';

export type NavMode = 'contents' | 'styles';
export type StyleCategory = 'All' | 'Literary' | 'Romance' | 'Fantasy' | 'Business';

export type NavItemType =
  | 'cover'
  | 'half-title' | 'series-page' | 'also-by-front' | 'publishers-note'
  | 'translators-note' | 'editors-note' | 'title-page' | 'copyright'
  | 'content-warning' | 'dedication' | 'epigraph' | 'toc'
  | 'list-of-figures' | 'list-of-tables' | 'cast-of-characters' | 'map'
  | 'pronunciation-guide' | 'timeline' | 'family-tree' | 'foreword'
  | 'preface' | 'introduction' | 'part-title' | 'chapter' | 'interlude'
  | 'epilogue' | 'afterword' | 'acknowledgments' | 'authors-note'
  | 'end-notes' | 'note-on-sources' | 'note-on-type' | 'permissions'
  | 'credits' | 'appendix' | 'glossary' | 'bibliography' | 'index'
  | 'about-author' | 'connect-with-author' | 'newsletter-cta'
  | 'bonus-epilogue' | 'qa-with-author' | 'also-by-back' | 'reading-guide'
  | 'next-book-preview' | 'colophon';

export interface NavItem {
  id: string;
  type: NavItemType;
  label: string;
  chapterIdx?: number;
}

interface AddableElement {
  type: NavItemType;
  label: string;
  group: 'Front Matter' | 'Body' | 'Back Matter';
}

export const ADDABLE_ELEMENTS: AddableElement[] = [
  { type: 'half-title',          label: 'Half-Title Page',             group: 'Front Matter' },
  { type: 'series-page',         label: 'Series Page',                 group: 'Front Matter' },
  { type: 'also-by-front',       label: 'Also By This Author',         group: 'Front Matter' },
  { type: 'publishers-note',     label: "Publisher's Note",            group: 'Front Matter' },
  { type: 'translators-note',    label: "Translator's Note",           group: 'Front Matter' },
  { type: 'editors-note',        label: "Editor's Note",               group: 'Front Matter' },
  { type: 'content-warning',     label: 'Content Warning',             group: 'Front Matter' },
  { type: 'dedication',          label: 'Dedication',                  group: 'Front Matter' },
  { type: 'epigraph',            label: 'Epigraph',                    group: 'Front Matter' },
  { type: 'list-of-figures',     label: 'List of Figures',             group: 'Front Matter' },
  { type: 'list-of-tables',      label: 'List of Tables',              group: 'Front Matter' },
  { type: 'cast-of-characters',  label: 'Cast of Characters',          group: 'Front Matter' },
  { type: 'map',                 label: 'Map',                         group: 'Front Matter' },
  { type: 'pronunciation-guide', label: 'Pronunciation Guide',         group: 'Front Matter' },
  { type: 'timeline',            label: 'Timeline / Chronology',       group: 'Front Matter' },
  { type: 'family-tree',         label: 'Family Tree',                 group: 'Front Matter' },
  { type: 'foreword',            label: 'Foreword',                    group: 'Front Matter' },
  { type: 'preface',             label: 'Preface',                     group: 'Front Matter' },
  { type: 'introduction',        label: 'Introduction',                group: 'Front Matter' },
  { type: 'part-title',          label: 'Part Title Page',             group: 'Body' },
  { type: 'interlude',           label: 'Interlude',                   group: 'Body' },
  { type: 'epilogue',            label: 'Epilogue',                    group: 'Body' },
  { type: 'afterword',           label: 'Conclusion / Afterword',      group: 'Body' },
  { type: 'acknowledgments',     label: 'Acknowledgments',             group: 'Back Matter' },
  { type: 'authors-note',        label: "Author's Note",               group: 'Back Matter' },
  { type: 'end-notes',           label: 'End Notes',                   group: 'Back Matter' },
  { type: 'note-on-sources',     label: 'A Note on Sources',           group: 'Back Matter' },
  { type: 'note-on-type',        label: 'A Note on the Type',          group: 'Back Matter' },
  { type: 'permissions',         label: 'Permissions',                 group: 'Back Matter' },
  { type: 'credits',             label: 'Credits',                     group: 'Back Matter' },
  { type: 'appendix',            label: 'Appendix',                    group: 'Back Matter' },
  { type: 'glossary',            label: 'Glossary',                    group: 'Back Matter' },
  { type: 'bibliography',        label: 'Bibliography / References',   group: 'Back Matter' },
  { type: 'index',               label: 'Index',                       group: 'Back Matter' },
  { type: 'about-author',        label: 'About the Author',            group: 'Back Matter' },
  { type: 'connect-with-author', label: 'Connect with the Author',     group: 'Back Matter' },
  { type: 'newsletter-cta',      label: 'Newsletter / Call to Action', group: 'Back Matter' },
  { type: 'bonus-epilogue',      label: 'Bonus / Extended Epilogue',   group: 'Back Matter' },
  { type: 'qa-with-author',      label: 'Q&A with the Author',         group: 'Back Matter' },
  { type: 'also-by-back',        label: 'Also By This Author',         group: 'Back Matter' },
  { type: 'reading-guide',       label: 'Reading Group Guide',         group: 'Back Matter' },
  { type: 'next-book-preview',   label: 'Preview of Next Book',        group: 'Back Matter' },
  { type: 'colophon',            label: 'Colophon',                    group: 'Back Matter' },
];

export function buildNavItems(bookData: BookData | null, addedElements: NavItem[] = []): NavItem[] {
  const addedTypes = new Set(addedElements.map(e => e.type));
  const has = (type: NavItemType) => addedTypes.has(type);
  const items: NavItem[] = [];
  const push = (type: NavItemType, label: string) => items.push({ id: type, type, label });

  push('cover', 'Cover');
  if (has('half-title'))          push('half-title',          'Half-Title Page');
  if (has('series-page'))         push('series-page',         'Series Page');
  if (has('also-by-front'))       push('also-by-front',       'Also By This Author');
  if (has('publishers-note'))     push('publishers-note',     "Publisher's Note");
  if (has('translators-note'))    push('translators-note',    "Translator's Note");
  if (has('editors-note'))        push('editors-note',        "Editor's Note");
  push('title-page', 'Title Page');
  push('copyright',  'Copyright');
  if (has('content-warning'))     push('content-warning',     'Content Warning');
  if (bookData?.dedication || has('dedication'))  push('dedication', 'Dedication');
  if (bookData?.epigraph   || has('epigraph'))    push('epigraph',   'Epigraph');
  if (bookData && bookData.chapters.length > 0)   push('toc',        'Table of Contents');
  if (has('list-of-figures'))     push('list-of-figures',     'List of Figures');
  if (has('list-of-tables'))      push('list-of-tables',      'List of Tables');
  if (has('cast-of-characters'))  push('cast-of-characters',  'Cast of Characters');
  if (has('map'))                 push('map',                 'Map');
  if (has('pronunciation-guide')) push('pronunciation-guide', 'Pronunciation Guide');
  if (has('timeline'))            push('timeline',            'Timeline / Chronology');
  if (has('family-tree'))         push('family-tree',         'Family Tree');
  if (has('foreword'))            push('foreword',            'Foreword');
  if (has('preface'))             push('preface',             'Preface');
  if (has('introduction'))        push('introduction',        'Introduction');
  if (has('part-title'))          push('part-title',          'Part One');
  if (bookData) {
    bookData.chapters.forEach((ch, idx) => {
      items.push({ id: `chapter-${ch.id}`, type: 'chapter', label: ch.title || `Chapter ${ch.number}`, chapterIdx: idx });
    });
  }
  if (has('interlude'))           push('interlude',           'Interlude');
  if (has('epilogue'))            push('epilogue',            'Epilogue');
  if (has('afterword'))           push('afterword',           'Conclusion / Afterword');
  if (bookData?.acknowledgments || has('acknowledgments'))    push('acknowledgments', 'Acknowledgments');
  if (has('authors-note'))        push('authors-note',        "Author's Note");
  if (has('end-notes'))           push('end-notes',           'End Notes');
  if (has('note-on-sources'))     push('note-on-sources',     'A Note on Sources');
  if (has('permissions'))         push('permissions',         'Permissions');
  if (has('credits'))             push('credits',             'Credits');
  if (has('appendix'))            push('appendix',            'Appendix');
  if (has('glossary'))            push('glossary',            'Glossary');
  if (has('bibliography'))        push('bibliography',        'Bibliography / References');
  if (has('index'))               push('index',               'Index');
  if (bookData?.aboutAuthor || has('about-author'))           push('about-author', 'About the Author');
  if (has('connect-with-author')) push('connect-with-author', 'Connect with the Author');
  if (has('newsletter-cta'))      push('newsletter-cta',      'Newsletter / Call to Action');
  if (has('bonus-epilogue'))      push('bonus-epilogue',      'Bonus / Extended Epilogue');
  if (has('qa-with-author'))      push('qa-with-author',      'Q&A with the Author');
  if (has('also-by-back'))        push('also-by-back',        'Also By This Author');
  if (has('reading-guide'))       push('reading-guide',       'Reading Group Guide');
  if (has('next-book-preview'))   push('next-book-preview',   'Preview of Next Book');
  if (has('note-on-type'))        push('note-on-type',        'A Note on the Type');
  if (has('colophon'))            push('colophon',            'Colophon');

  return items;
}

interface NavigatorProps {
  bookData: BookData | null;
  navItems: NavItem[];
  selectedId: string;
  onSelect: (item: NavItem) => void;
  onReorder: (items: NavItem[]) => void;
  navMode: NavMode;
  onNavModeChange: (mode: NavMode) => void;
  styleCategory: StyleCategory;
  onStyleCategoryChange: (cat: StyleCategory) => void;
  onAddElement: (item: NavItem) => void;
  onDeleteChapter?: (chapterId: string) => void;
  onDeleteElement?: (itemId: string) => void;
}

const STYLE_CATEGORIES: StyleCategory[] = ['All', 'Literary', 'Romance', 'Fantasy', 'Business'];

// Types of items that can be removed (added elements, not core items)
const CORE_ITEM_TYPES = new Set<NavItemType>(['cover', 'title-page', 'copyright', 'toc', 'chapter']);

export default function Navigator({
  bookData,
  navItems,
  selectedId,
  onSelect,
  onReorder,
  navMode,
  onNavModeChange,
  styleCategory,
  onStyleCategoryChange,
  onAddElement,
  onDeleteChapter,
  onDeleteElement,
}: NavigatorProps) {
  const [showMenu, setShowMenu] = useState(false);
  const [menuPos, setMenuPos] = useState<{ top: number; left: number; width: number; openUp: boolean } | null>(null);
  const btnRef = useRef<HTMLButtonElement>(null);
  const dragIdx = useRef<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);
  const [hoveredId, setHoveredId] = useState<string | null>(null);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);

  const presentTypes = new Set(navItems.map(i => i.type));
  const available = ADDABLE_ELEMENTS.filter(el => !presentTypes.has(el.type));
  const groups = ['Front Matter', 'Body', 'Back Matter'] as const;

  const handleOpenMenu = useCallback(() => {
    if (showMenu) { setShowMenu(false); setMenuPos(null); return; }
    if (btnRef.current) {
      const rect = btnRef.current.getBoundingClientRect();
      const openUp = rect.top > window.innerHeight / 2;
      setMenuPos({ top: openUp ? rect.top : rect.bottom + 4, left: rect.left, width: rect.width, openUp });
    }
    setShowMenu(true);
  }, [showMenu]);

  useEffect(() => {
    if (!showMenu) return;
    const handler = (e: MouseEvent) => {
      const target = e.target as Node;
      if (btnRef.current?.contains(target)) return;
      if (document.getElementById('nav-add-menu')?.contains(target)) return;
      setShowMenu(false);
      setMenuPos(null);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [showMenu]);

  const handleDragStart = useCallback((idx: number) => { dragIdx.current = idx; }, []);
  const handleDragOver = useCallback((e: React.DragEvent, idx: number) => { e.preventDefault(); setDragOverIdx(idx); }, []);
  const handleDrop = useCallback((e: React.DragEvent, toIdx: number) => {
    e.preventDefault();
    const fromIdx = dragIdx.current;
    if (fromIdx === null || fromIdx === toIdx) { setDragOverIdx(null); return; }
    const next = [...navItems];
    const [moved] = next.splice(fromIdx, 1);
    next.splice(toIdx, 0, moved);
    onReorder(next);
    dragIdx.current = null;
    setDragOverIdx(null);
  }, [navItems, onReorder]);
  const handleDragEnd = useCallback(() => { dragIdx.current = null; setDragOverIdx(null); }, []);

  return (
    <>
      <div style={{
        width: 220, flexShrink: 0, background: '#FFFFFF',
        borderRight: '1px solid rgba(0,0,0,0.1)',
        display: 'flex', flexDirection: 'column', overflow: 'hidden',
      }}>
        {/* Tabs */}
        <div style={{ display: 'flex', borderBottom: '1px solid rgba(0,0,0,0.1)', flexShrink: 0 }}>
          {(['contents', 'styles'] as NavMode[]).map((mode) => (
            <button key={mode} onClick={() => onNavModeChange(mode)} style={{
              flex: 1, height: 36,
              background: navMode === mode ? '#FFFFFF' : '#F5F5F5',
              border: 'none',
              borderBottom: navMode === mode ? '2.5px solid #C8FF00' : '2.5px solid transparent',
              cursor: 'pointer', fontSize: 11,
              fontWeight: navMode === mode ? 700 : 500,
              color: navMode === mode ? '#0A0A0A' : '#777',
              letterSpacing: '0.04em', textTransform: 'uppercase',
            }}>
              {mode === 'contents' ? 'Contents' : 'Styles'}
            </button>
          ))}
        </div>

        {navMode === 'contents' && (
          <>
            <div style={{ padding: '14px 16px 12px', borderBottom: '1px solid rgba(0,0,0,0.07)', flexShrink: 0 }}>
              <div style={{ fontSize: 12, fontWeight: 700, color: '#0A0A0A', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {bookData?.title || 'Untitled Book'}
              </div>
              {bookData?.author && (
                <div style={{ fontSize: 10, color: '#888', marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {bookData.author}
                </div>
              )}
            </div>

            <div style={{ flex: 1, overflowY: 'auto', padding: '4px 0' }}>
              {navItems.map((item, idx) => {
                const isSelected = item.id === selectedId;
                const isDragOver = dragOverIdx === idx;
                const isHovered = hoveredId === item.id;
                const isPendingDelete = pendingDeleteId === item.id;
                const isDeletable = item.type === 'chapter'
                  ? !!onDeleteChapter && (bookData?.chapters.length ?? 0) > 1
                  : !CORE_ITEM_TYPES.has(item.type) && !!onDeleteElement;

                // ── pending delete confirmation row ──
                if (isPendingDelete) {
                  return (
                    <div
                      key={item.id}
                      style={{
                        display: 'flex', alignItems: 'center', gap: 6,
                        padding: '6px 10px',
                        background: '#fee2e2',
                        borderLeft: '3px solid #ef4444',
                      }}
                    >
                      <span style={{ flex: 1, fontSize: 11, color: '#991b1b', fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        Delete &ldquo;{item.label}&rdquo;?
                      </span>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setPendingDeleteId(null);
                          if (item.type === 'chapter' && item.chapterIdx !== undefined) {
                            const ch = bookData?.chapters[item.chapterIdx];
                            if (ch) onDeleteChapter?.(ch.id);
                          } else {
                            onDeleteElement?.(item.id);
                          }
                        }}
                        style={{ fontSize: 10, fontWeight: 800, background: '#ef4444', color: '#fff', border: 'none', borderRadius: 3, padding: '3px 8px', cursor: 'pointer', flexShrink: 0 }}
                      >
                        Delete
                      </button>
                      <button
                        onClick={(e) => { e.stopPropagation(); setPendingDeleteId(null); }}
                        style={{ fontSize: 10, fontWeight: 600, background: 'transparent', color: '#666', border: '1px solid #ccc', borderRadius: 3, padding: '3px 7px', cursor: 'pointer', flexShrink: 0 }}
                      >
                        Cancel
                      </button>
                    </div>
                  );
                }

                return (
                  <div
                    key={item.id}
                    draggable
                    onDragStart={() => handleDragStart(idx)}
                    onDragOver={(e) => handleDragOver(e, idx)}
                    onDrop={(e) => handleDrop(e, idx)}
                    onDragEnd={handleDragEnd}
                    onClick={() => { setPendingDeleteId(null); onSelect(item); }}
                    onMouseEnter={() => setHoveredId(item.id)}
                    onMouseLeave={() => setHoveredId(null)}
                    style={{
                      display: 'flex', alignItems: 'center',
                      background: isSelected ? '#0A0A0A' : isDragOver ? 'rgba(0,0,0,0.08)' : isHovered ? 'rgba(0,0,0,0.05)' : 'transparent',
                      borderLeft: isSelected ? '3px solid #C8FF00' : '3px solid transparent',
                      borderTop: isDragOver ? '2px solid #C8FF00' : '2px solid transparent',
                      cursor: 'pointer',
                    }}
                  >
                    <div style={{ width: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, opacity: 0.3, cursor: 'grab', color: isSelected ? '#fff' : '#333' }}>
                      <GripVertical size={12} />
                    </div>
                    <div style={{ flex: 1, padding: '7px 4px 7px 0', fontSize: 13, color: isSelected ? '#fff' : '#333', lineHeight: 1.4, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {item.type === 'chapter' && (
                        <span style={{ fontSize: 10, color: isSelected ? 'rgba(255,255,255,0.5)' : '#aaa', marginRight: 5 }}>
                          {item.chapterIdx !== undefined ? item.chapterIdx + 1 : ''}
                        </span>
                      )}
                      {item.label}
                    </div>
                    {/* Trash icon — visible on hover for deletable items */}
                    {isDeletable && (isHovered || isSelected) && (
                      <button
                        title={item.type === 'chapter' ? 'Delete chapter' : 'Remove element'}
                        onClick={(e) => {
                          e.stopPropagation();
                          setPendingDeleteId(item.id);
                        }}
                        style={{
                          width: 26, height: 26, flexShrink: 0,
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          background: 'transparent', border: 'none', cursor: 'pointer',
                          color: isSelected ? 'rgba(255,255,255,0.45)' : '#aaa',
                          borderRadius: 4, marginRight: 4,
                          transition: 'color 0.1s, background 0.1s',
                        }}
                        onMouseEnter={(e) => {
                          (e.currentTarget as HTMLElement).style.color = '#ef4444';
                          (e.currentTarget as HTMLElement).style.background = 'rgba(239,68,68,0.1)';
                        }}
                        onMouseLeave={(e) => {
                          (e.currentTarget as HTMLElement).style.color = isSelected ? 'rgba(255,255,255,0.45)' : '#aaa';
                          (e.currentTarget as HTMLElement).style.background = 'transparent';
                        }}
                      >
                        <Trash2 size={12} />
                      </button>
                    )}
                  </div>
                );
              })}
            </div>

            <div style={{ padding: '10px 12px', borderTop: '1px solid rgba(0,0,0,0.08)', flexShrink: 0 }}>
              <button
                ref={btnRef}
                onClick={handleOpenMenu}
                style={{
                  width: '100%', padding: '7px 0',
                  background: showMenu ? '#0A0A0A' : 'transparent',
                  border: showMenu ? '1.5px solid #0A0A0A' : '1.5px dashed rgba(0,0,0,0.25)',
                  borderRadius: 4, fontSize: 12, fontWeight: 600,
                  color: showMenu ? '#C8FF00' : '#666',
                  cursor: 'pointer',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
                }}
              >
                {showMenu ? <X size={12} /> : <Plus size={12} />}
                {showMenu ? 'Close' : '+ Add Element'}
              </button>
            </div>
          </>
        )}

        {navMode === 'styles' && (
          <div style={{ flex: 1, overflowY: 'auto', padding: '6px 0' }}>
            {STYLE_CATEGORIES.map((cat) => {
              const isSelected = cat === styleCategory;
              return (
                <button key={cat} onClick={() => onStyleCategoryChange(cat)} style={{
                  width: '100%', display: 'block', textAlign: 'left', padding: '8px 16px',
                  fontSize: 13, border: 'none',
                  borderLeft: isSelected ? '3px solid #C8FF00' : '3px solid transparent',
                  background: isSelected ? '#0A0A0A' : 'transparent',
                  color: isSelected ? '#fff' : '#333', cursor: 'pointer',
                }}
                onMouseEnter={(e) => { if (!isSelected) (e.currentTarget as HTMLElement).style.background = 'rgba(0,0,0,0.05)'; }}
                onMouseLeave={(e) => { if (!isSelected) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                >
                  {cat}
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* Fixed dropdown portal — outside overflow:hidden container */}
      {showMenu && menuPos && (
        <div
          id="nav-add-menu"
          style={{
            position: 'fixed',
            ...(menuPos.openUp
              ? { bottom: `${window.innerHeight - menuPos.top + 4}px` }
              : { top: `${menuPos.top}px` }),
            left: menuPos.left,
            width: Math.max(menuPos.width, 220),
            background: '#fff',
            border: '2px solid #0A0A0A',
            borderRadius: 6,
            boxShadow: '4px 4px 0 #0A0A0A',
            zIndex: 9999,
            maxHeight: 400,
            overflowY: 'auto',
          }}
        >
          {available.length === 0 ? (
            <div style={{ padding: 16, fontSize: 12, color: '#aaa', textAlign: 'center' }}>All elements added</div>
          ) : (
            groups.map((group) => {
              const groupItems = available.filter(e => e.group === group);
              if (groupItems.length === 0) return null;
              return (
                <div key={group}>
                  <div style={{ padding: '8px 14px 5px', fontSize: 9, fontWeight: 800, letterSpacing: '0.12em', textTransform: 'uppercase', color: '#999', background: '#f7f6f3', borderBottom: '1px solid rgba(0,0,0,0.07)', position: 'sticky', top: 0 }}>
                    {group}
                  </div>
                  {groupItems.map((el) => (
                    <button
                      key={el.type}
                      onClick={() => {
                        onAddElement({ id: el.type, type: el.type, label: el.label });
                        setShowMenu(false);
                        setMenuPos(null);
                      }}
                      style={{ display: 'block', width: '100%', textAlign: 'left', padding: '9px 14px', fontSize: 12, fontWeight: 500, color: '#222', background: 'transparent', border: 'none', borderBottom: '1px solid rgba(0,0,0,0.05)', cursor: 'pointer' }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = '#F5F5F5'; }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                    >
                      {el.label}
                    </button>
                  ))}
                </div>
              );
            })
          )}
        </div>
      )}
    </>
  );
}
