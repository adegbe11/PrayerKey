'use client';

import { useRef } from 'react';
import { Upload } from 'lucide-react';
import { coverTemplates } from '@/lib/covers';
import type { BookData, Genre, CoverConfig } from '@/types';

// ─────────────────────────────────────────
//  PROPS
// ─────────────────────────────────────────

interface CoverPanelProps {
  bookData: BookData | null;
  genre: Genre;
  coverConfig: CoverConfig;
  onCoverChange: (config: CoverConfig) => void;
}

// ─────────────────────────────────────────
//  TEMPLATE SWATCH COLORS
//  Maps templateId → approximate background color for the swatch
// ─────────────────────────────────────────

const SWATCH_COLORS: Record<string, { bg: string; textColor: string }> = {
  divine:  { bg: '#0A1628',  textColor: '#D4A853' },
  bloom:   { bg: '#4A0E28',  textColor: '#FDE8F0' },
  edge:    { bg: '#090909',  textColor: '#FFFFFF' },
  tower:   { bg: '#F8F7F4',  textColor: '#000000' },
  chapter: { bg: '#F7F2EA',  textColor: '#8B6F47' },
  bold:    { bg: '#FFE500',  textColor: '#000000' },
  verse:   { bg: '#F0EEF8',  textColor: '#7C3AED' },
  cosmos:  { bg: '#0D1B3E',  textColor: '#E8F4FF' },
  clean:   { bg: '#1A1828',  textColor: '#FFFFFF' },
  paper:   { bg: '#F2F2F0',  textColor: '#555555' },
};

// ─────────────────────────────────────────
//  MAIN COMPONENT
// ─────────────────────────────────────────

export default function CoverPanel({
  bookData,
  genre,
  coverConfig,
  onCoverChange,
}: CoverPanelProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const displayTitle = bookData?.title || 'Untitled';
  const truncatedTitle = displayTitle.length > 28
    ? displayTitle.slice(0, 26) + '…'
    : displayTitle;

  // Determine preview thumbnail background
  const activeSwatchColor = SWATCH_COLORS[coverConfig.templateId] ?? { bg: '#1A1828', textColor: '#FFF' };

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    const objectUrl = URL.createObjectURL(file);
    onCoverChange({
      type: 'uploaded',
      templateId: coverConfig.templateId,
      uploadedUrl: objectUrl,
    });
    // Reset input so same file can be re-selected
    e.target.value = '';
  }

  function handleTemplateClick(templateId: string) {
    onCoverChange({ type: 'template', templateId });
  }

  return (
    <div style={{ padding: '10px 14px 12px' }}>

      {/* ── TOP ROW: Preview thumbnail + Upload button + Genre badge ── */}
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', marginBottom: '12px' }}>

        {/* Current cover thumbnail */}
        <div
          style={{
            width: '65px',
            height: '90px',
            flexShrink: 0,
            border: '2px solid rgba(255,255,255,0.15)',
            boxShadow: '3px 3px 0px rgba(0,0,0,0.3)',
            overflow: 'hidden',
            position: 'relative',
            borderRadius: '1px',
          }}
        >
          {coverConfig.type === 'uploaded' && coverConfig.uploadedUrl ? (
            <img
              src={coverConfig.uploadedUrl}
              alt="Cover"
              style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
            />
          ) : (
            <div
              style={{
                width: '100%',
                height: '100%',
                background: activeSwatchColor.bg,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '6px',
                boxSizing: 'border-box',
              }}
            >
              <div
                style={{
                  fontFamily: 'Georgia, serif',
                  fontSize: '6pt',
                  color: activeSwatchColor.textColor,
                  textAlign: 'center',
                  lineHeight: '1.3',
                  overflow: 'hidden',
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical' as React.CSSProperties['WebkitBoxOrient'],
                  maxHeight: '28px',
                }}
              >
                {truncatedTitle}
              </div>
            </div>
          )}
        </div>

        {/* Right side: upload button + genre badge */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>

          {/* Upload cover button */}
          <div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              style={{ display: 'none' }}
              id="cover-upload-input"
            />
            <label
              htmlFor="cover-upload-input"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                padding: '5px 10px',
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid rgba(255,255,255,0.12)',
                color: 'var(--text-secondary)',
                fontSize: '10px',
                fontWeight: 600,
                cursor: 'pointer',
                borderRadius: '2px',
                transition: 'background 0.15s',
                userSelect: 'none',
                whiteSpace: 'nowrap',
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLLabelElement).style.background = 'rgba(255,255,255,0.09)';
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLLabelElement).style.background = 'rgba(255,255,255,0.05)';
              }}
            >
              <Upload size={11} />
              Upload Cover
            </label>
          </div>

          {/* Auto badge — detected genre */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px', flexWrap: 'wrap' }}>
            <span
              style={{
                fontSize: '9px',
                color: 'var(--text-muted)',
                opacity: 0.6,
              }}
            >
              Auto:
            </span>
            <span
              style={{
                fontSize: '9px',
                background: 'rgba(255,229,0,0.1)',
                color: '#FFE500',
                border: '1px solid rgba(255,229,0,0.25)',
                padding: '1px 6px',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.08em',
                borderRadius: '2px',
              }}
            >
              {genre}
            </span>
          </div>

          {/* Uploaded indicator */}
          {coverConfig.type === 'uploaded' && (
            <button
              onClick={() => onCoverChange({ type: 'template', templateId: coverConfig.templateId })}
              style={{
                fontSize: '9px',
                color: 'rgba(255,100,100,0.7)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: 0,
                textAlign: 'left',
                textDecoration: 'underline',
              }}
            >
              Remove upload
            </button>
          )}
        </div>
      </div>

      {/* ── TEMPLATE GRID ── */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(2, 1fr)',
          gap: '6px',
        }}
      >
        {coverTemplates.map((ts) => {
          const swatch = SWATCH_COLORS[ts.id] ?? { bg: '#1A1828', textColor: '#FFF' };
          const isActive = coverConfig.type === 'template' && coverConfig.templateId === ts.id;

          return (
            <button
              key={ts.id}
              onClick={() => handleTemplateClick(ts.id)}
              title={ts.name}
              style={{
                width: '100%',
                height: '60px',
                background: swatch.bg,
                border: isActive
                  ? '2px solid #FFE500'
                  : '2px solid rgba(255,255,255,0.07)',
                boxShadow: isActive ? '0 0 0 1px rgba(255,229,0,0.3)' : 'none',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'flex-end',
                padding: '0 4px 5px',
                boxSizing: 'border-box',
                transition: 'border-color 0.15s, box-shadow 0.15s',
                borderRadius: '1px',
                position: 'relative',
                overflow: 'hidden',
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  (e.currentTarget as HTMLButtonElement).style.borderColor = 'rgba(255,255,255,0.2)';
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  (e.currentTarget as HTMLButtonElement).style.borderColor = 'rgba(255,255,255,0.07)';
                }
              }}
            >
              {/* Template name */}
              <span
                style={{
                  fontSize: '7pt',
                  color: swatch.textColor,
                  opacity: 0.85,
                  fontWeight: isActive ? 700 : 500,
                  letterSpacing: '0.04em',
                  textTransform: 'uppercase',
                  textAlign: 'center',
                  lineHeight: 1,
                  position: 'relative',
                  zIndex: 1,
                }}
              >
                {ts.name}
              </span>

              {/* Active indicator dot */}
              {isActive && (
                <div
                  style={{
                    position: 'absolute',
                    top: '5px',
                    right: '5px',
                    width: '6px',
                    height: '6px',
                    borderRadius: '50%',
                    background: '#FFE500',
                    boxShadow: '0 0 4px rgba(255,229,0,0.6)',
                  }}
                />
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
