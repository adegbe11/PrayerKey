'use client';

import { templates } from '@/lib/templates';
import TiptapEditor from './TiptapEditor';
import type { BookData, CoverConfig, Genre } from '@/types';
import type { NavItem, StyleCategory } from './Navigator';
import CoverPanel from './CoverPanel';

// ─────────────────────────────────────────
//  POPULAR TEMPLATE IDS
// ─────────────────────────────────────────

function filterTemplates(category: StyleCategory) {
  switch (category) {
    case 'Literary':
      return templates.filter((t) => ['knopf', 'scribner', 'tor', 'ballantine', 'vintage'].includes(t.id));
    case 'Romance':
      return templates.filter((t) => ['ballantine', 'scribner'].includes(t.id));
    case 'Fantasy':
      return templates.filter((t) => ['tor', 'knopf'].includes(t.id));
    case 'Business':
      return templates.filter((t) => ['hbr', 'penguin', 'anchor'].includes(t.id));
    case 'All':
    default:
      return templates;
  }
}

// ─────────────────────────────────────────
//  PROPS
// ─────────────────────────────────────────

interface ChapterEditorProps {
  bookData: BookData | null;
  selectedItem: NavItem | null;
  navMode: 'contents' | 'styles';
  styleCategory: StyleCategory;
  selectedStyleId: string;
  onSelectStyle: (id: string) => void;
  onUpdateChapterContent: (chapterIdx: number, newContent: string) => void;
  onUpdateSection?: (field: string, value: string) => void;
  coverConfig?: CoverConfig;
  onCoverChange?: (config: CoverConfig) => void;
  detectedGenre?: Genre;
}

// ─────────────────────────────────────────
//  STYLES GRID
// ─────────────────────────────────────────

function StylesGrid({
  styleCategory,
  selectedStyleId,
  onSelectStyle,
}: {
  styleCategory: StyleCategory;
  selectedStyleId: string;
  onSelectStyle: (id: string) => void;
}) {
  const filtered = filterTemplates(styleCategory);

  return (
    <div
      style={{
        flex: 1,
        background: '#F5F2EC',
        overflow: 'auto',
        padding: 28,
      }}
    >
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
          gap: 16,
        }}
      >
        {filtered.map((tmpl) => {
          const isSelected = tmpl.id === selectedStyleId;
          return (
            <button
              key={tmpl.id}
              onClick={() => onSelectStyle(tmpl.id)}
              style={{
                display: 'flex',
                flexDirection: 'column',
                background: '#fff',
                border: isSelected
                  ? '2.5px solid #1a1a1a'
                  : '1.5px solid rgba(0,0,0,0.12)',
                boxShadow: isSelected
                  ? '4px 4px 0 #1a1a1a'
                  : '2px 2px 0 rgba(0,0,0,0.06)',
                cursor: 'pointer',
                padding: 0,
                overflow: 'hidden',
                textAlign: 'left',
                transition: 'box-shadow 0.15s, border-color 0.15s',
              }}
            >
              {/* Preview area */}
              <div
                style={{
                  height: 120,
                  background: tmpl.paperColor,
                  padding: '18px 16px 12px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 8,
                }}
              >
                {/* Heading preview */}
                <div
                  style={{
                    fontFamily: tmpl.headingFont,
                    color: tmpl.headingColor,
                    fontWeight: tmpl.headingWeight,
                    textTransform: tmpl.headingTransform,
                    fontSize: 13,
                    textAlign: tmpl.headingAlign,
                    lineHeight: 1.2,
                    overflow: 'hidden',
                    whiteSpace: 'nowrap',
                    textOverflow: 'ellipsis',
                  }}
                >
                  Chapter One
                </div>
                {/* Body lines */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4, marginTop: 4 }}>
                  {[100, 85, 92].map((w, i) => (
                    <div
                      key={i}
                      style={{
                        height: 2,
                        width: `${w}%`,
                        background: tmpl.inkColor,
                        opacity: 0.18,
                        borderRadius: 1,
                      }}
                    />
                  ))}
                </div>
              </div>

              {/* Bottom strip */}
              <div
                style={{
                  padding: '8px 12px',
                  background: isSelected ? '#1a1a1a' : '#f7f6f3',
                  borderTop: '1px solid rgba(0,0,0,0.08)',
                }}
              >
                <div
                  style={{
                    fontSize: 11,
                    fontWeight: 700,
                    color: isSelected ? '#fff' : '#1a1a1a',
                    lineHeight: 1.3,
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {tmpl.name}
                </div>
                <div
                  style={{
                    fontSize: 10,
                    color: isSelected ? 'rgba(255,255,255,0.6)' : '#888',
                    marginTop: 2,
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {tmpl.description}
                </div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────
//  CONTENTS PANEL (individual item views)
// ─────────────────────────────────────────

function TitlePageView({ bookData }: { bookData: BookData | null }) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '60px 40px',
        minHeight: 400,
        textAlign: 'center',
      }}
    >
      <div
        style={{
          fontSize: 28,
          fontWeight: 700,
          fontFamily: 'Georgia, serif',
          color: '#1a1a1a',
          lineHeight: 1.25,
          marginBottom: 12,
        }}
      >
        {bookData?.title || 'Untitled Book'}
      </div>
      {bookData?.subtitle && (
        <div
          style={{
            fontSize: 14,
            color: '#666',
            fontFamily: 'Georgia, serif',
            fontStyle: 'italic',
            marginBottom: 12,
          }}
        >
          {bookData.subtitle}
        </div>
      )}
      <div
        style={{
          fontSize: 15,
          color: '#555',
          fontFamily: 'Georgia, serif',
          marginTop: 8,
        }}
      >
        {bookData?.author || ''}
      </div>
    </div>
  );
}

function CopyrightView({ bookData }: { bookData: BookData | null }) {
  const year = new Date().getFullYear();
  return (
    <div style={{ padding: '40px 40px', fontFamily: 'Georgia, serif', color: '#555', fontSize: 12, lineHeight: 1.8 }}>
      <p>Copyright © {year} {bookData?.author || 'the author'}. All rights reserved.</p>
      <p style={{ marginTop: 12 }}>No part of this publication may be reproduced, distributed, or transmitted in any form or by any means, including photocopying, recording, or other electronic or mechanical methods, without the prior written permission of the publisher.</p>
      <p style={{ marginTop: 12 }}>First Edition</p>
    </div>
  );
}

function DedicationView({ bookData, onUpdateSection }: { bookData: BookData | null; onUpdateSection?: (field: string, value: string) => void }) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '60px 40px',
        minHeight: 300,
        textAlign: 'center',
      }}
    >
      <p style={{ fontSize: 11, color: '#aaa', marginBottom: 12, fontStyle: 'italic' }}>Click to edit dedication</p>
      <div
        contentEditable
        suppressContentEditableWarning
        onBlur={(e) => onUpdateSection?.('dedication', e.currentTarget.innerText.trim())}
        style={{
          fontFamily: 'Georgia, serif',
          fontSize: 16,
          fontStyle: 'italic',
          color: '#444',
          lineHeight: 1.8,
          maxWidth: 380,
          outline: 'none',
          minHeight: 40,
          borderBottom: '1px dashed rgba(0,0,0,0.15)',
          paddingBottom: 4,
          cursor: 'text',
        }}
        dangerouslySetInnerHTML={{ __html: bookData?.dedication || 'For everyone who dared to dream.' }}
      />
    </div>
  );
}

function EpigraphView({ bookData, onUpdateSection }: { bookData: BookData | null; onUpdateSection?: (field: string, value: string) => void }) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '60px 40px',
        minHeight: 300,
        textAlign: 'center',
        gap: 12,
      }}
    >
      <p style={{ fontSize: 11, color: '#aaa', marginBottom: 0, fontStyle: 'italic' }}>Click to edit epigraph</p>
      <div
        contentEditable
        suppressContentEditableWarning
        onBlur={(e) => onUpdateSection?.('epigraph', e.currentTarget.innerText.trim())}
        style={{
          fontFamily: 'Georgia, serif',
          fontSize: 15,
          fontStyle: 'italic',
          color: '#333',
          lineHeight: 1.8,
          maxWidth: 400,
          outline: 'none',
          borderBottom: '1px dashed rgba(0,0,0,0.15)',
          paddingBottom: 4,
          cursor: 'text',
        }}
        dangerouslySetInnerHTML={{ __html: bookData?.epigraph || 'Enter your epigraph here.' }}
      />
      <div
        contentEditable
        suppressContentEditableWarning
        onBlur={(e) => onUpdateSection?.('epigraphAttribution', e.currentTarget.innerText.trim())}
        style={{
          fontSize: 12,
          color: '#888',
          fontFamily: 'Georgia, serif',
          outline: 'none',
          cursor: 'text',
        }}
        dangerouslySetInnerHTML={{ __html: bookData?.epigraphAttribution ? `— ${bookData.epigraphAttribution}` : '— Attribution' }}
      />
    </div>
  );
}

function TocView({ bookData }: { bookData: BookData | null }) {
  return (
    <div style={{ padding: '40px' }}>
      <div
        style={{
          fontFamily: 'Georgia, serif',
          fontSize: 20,
          fontWeight: 700,
          color: '#1a1a1a',
          marginBottom: 24,
        }}
      >
        Contents
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
        {bookData?.chapters.map((ch, idx) => (
          <div
            key={ch.id}
            style={{
              display: 'flex',
              alignItems: 'baseline',
              gap: 0,
              paddingBottom: 10,
              paddingTop: 10,
              borderBottom: '1px solid rgba(0,0,0,0.06)',
            }}
          >
            <span
              style={{
                fontFamily: 'Georgia, serif',
                fontSize: 13,
                color: '#1a1a1a',
                flexShrink: 0,
              }}
            >
              {ch.title || `Chapter ${ch.number}`}
            </span>
            <span
              style={{
                flex: 1,
                borderBottom: '1px dotted rgba(0,0,0,0.2)',
                margin: '0 8px',
                height: 1,
                alignSelf: 'flex-end',
                marginBottom: 4,
              }}
            />
            <span
              style={{
                fontFamily: 'Georgia, serif',
                fontSize: 13,
                color: '#888',
                flexShrink: 0,
              }}
            >
              {idx * 18 + 1}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function SimpleTextView({ text, placeholder, onUpdate }: { text: string; placeholder?: string; onUpdate?: (val: string) => void }) {
  return (
    <div style={{ padding: '40px', display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
      {onUpdate && (
        <p style={{ fontSize: 11, color: '#aaa', fontStyle: 'italic', margin: 0 }}>Click to edit</p>
      )}
      <div
        contentEditable={!!onUpdate}
        suppressContentEditableWarning
        onBlur={(e) => onUpdate?.(e.currentTarget.innerHTML)}
        style={{
          fontFamily: 'Georgia, serif',
          fontSize: 15,
          lineHeight: 1.8,
          color: '#1a1a1a',
          outline: 'none',
          minHeight: 120,
          cursor: onUpdate ? 'text' : 'default',
        }}
        dangerouslySetInnerHTML={{ __html: text || placeholder || '' }}
      />
    </div>
  );
}

// ─────────────────────────────────────────
//  CHAPTER CONTENT EDITOR  (Tiptap-powered)
// ─────────────────────────────────────────

function ChapterContentEditor({
  bookData,
  selectedItem,
  onUpdateChapterContent,
}: {
  bookData: BookData | null;
  selectedItem: NavItem;
  onUpdateChapterContent: (chapterIdx: number, newContent: string) => void;
}) {
  const chapterIdx = selectedItem.chapterIdx;
  const chapter    = chapterIdx !== undefined ? bookData?.chapters[chapterIdx] : undefined;

  if (!chapter) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#aaa', fontSize: 13 }}>
        Chapter not found.
      </div>
    );
  }

  return (
    <TiptapEditor
      key={chapter.id}          // remount only when chapter id changes
      content={chapter.content}
      chapterId={chapter.id}
      chapterTitle={chapter.title}
      chapterNumber={chapter.number}
      onChange={(html) => {
        if (chapterIdx !== undefined) onUpdateChapterContent(chapterIdx, html);
      }}
    />
  );
}

// ─────────────────────────────────────────
//  CHAPTER EDITOR MAIN
// ─────────────────────────────────────────

export default function ChapterEditor({
  bookData,
  selectedItem,
  navMode,
  styleCategory,
  selectedStyleId,
  onSelectStyle,
  onUpdateChapterContent,
  onUpdateSection,
  coverConfig,
  onCoverChange,
  detectedGenre = 'fiction',
}: ChapterEditorProps) {
  // ── Styles mode ──
  if (navMode === 'styles') {
    return (
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <StylesGrid
          styleCategory={styleCategory}
          selectedStyleId={selectedStyleId}
          onSelectStyle={onSelectStyle}
        />
      </div>
    );
  }

  // ── Contents mode ──
  if (!selectedItem) {
    return (
      <div
        style={{
          flex: 1,
          background: '#FAFAF8',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#aaa',
          fontSize: 13,
        }}
      >
        Select a chapter from the navigator
      </div>
    );
  }

  const renderItem = () => {
    switch (selectedItem.type) {
      case 'cover':
        return coverConfig && onCoverChange ? (
          <div style={{ flex: 1, overflowY: 'auto', background: '#1A1828', padding: 32 }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,0.4)', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: 20 }}>Book Cover</div>
            <CoverPanel
              bookData={bookData}
              genre={detectedGenre}
              coverConfig={coverConfig}
              onCoverChange={onCoverChange}
            />
          </div>
        ) : null;
      case 'title-page':
        return <TitlePageView bookData={bookData} />;
      case 'copyright':
        return <CopyrightView bookData={bookData} />;
      case 'dedication':
        return <DedicationView bookData={bookData} onUpdateSection={onUpdateSection} />;
      case 'epigraph':
        return <EpigraphView bookData={bookData} onUpdateSection={onUpdateSection} />;
      case 'toc':
        return <TocView bookData={bookData} />;
      case 'chapter':
        return (
          <ChapterContentEditor
            bookData={bookData}
            selectedItem={selectedItem}
            onUpdateChapterContent={onUpdateChapterContent}
          />
        );
      case 'acknowledgments':
        return <SimpleTextView text={bookData?.acknowledgments || ''} placeholder="Write your acknowledgments here." onUpdate={(v) => onUpdateSection?.('acknowledgments', v)} />;
      case 'about-author':
        return <SimpleTextView text={bookData?.aboutAuthor || ''} placeholder="Write your author bio here." onUpdate={(v) => onUpdateSection?.('aboutAuthor', v)} />;
      default: {
        // All other added elements — generic editable section
        const extrasContent = (bookData?.extras ?? {})[selectedItem.type] ?? '';
        return (
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <div style={{ padding: '24px 40px 0', borderBottom: '1px solid rgba(0,0,0,0.07)', flexShrink: 0 }}>
              <div style={{ fontSize: 22, fontFamily: 'Georgia, serif', fontWeight: 700, color: '#1a1a1a', marginBottom: 12 }}>
                {selectedItem.label}
              </div>
            </div>
            <SimpleTextView
              text={extrasContent}
              placeholder={`Write your ${selectedItem.label.toLowerCase()} here...`}
              onUpdate={(v) => onUpdateSection?.(selectedItem.type, v)}
            />
          </div>
        );
      }
    }
  };

  return (
    <div
      style={{
        flex: 1,
        background: '#FAFAF8',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {renderItem()}
    </div>
  );
}
