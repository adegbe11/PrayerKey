'use client';

import { useState } from 'react';
import { motion } from 'framer-motion';
import { X } from 'lucide-react';
import { getTemplate, templates } from '@/lib/templates';
import type { BookData, BookPage } from '@/types';

interface ExportModalProps {
  bookData: BookData;
  templateId: string;
  pages: BookPage[];
  onClose: () => void;
  onTemplateChange?: (id: string) => void;
}

type ExportState = 'idle' | 'generating' | 'done' | 'error';

// Map trim size values to CSS page sizes
const TRIM_SIZE_CSS: Record<string, string> = {
  '5x8': '5in 8in',
  '5.5x8.5': '5.5in 8.5in',
  '6x9': '6in 9in',
  'digest': '5.06in 7.81in',
  'mass': '4.25in 6.87in',
  'a5': '148mm 210mm',
  '7x10': '7in 10in',
  '8.5x11': '8.5in 11in',
};

const TRIM_SIZES = [
  { value: '5x8', label: '5" × 8" — Trade Paperback (popular fiction)' },
  { value: '5.5x8.5', label: '5.5" × 8.5" — Trade Paperback' },
  { value: '6x9', label: '6" × 9" — Standard Trade (most common)' },
  { value: 'digest', label: '5.06" × 7.81" — Digest' },
  { value: 'mass', label: '4.25" × 6.87" — Mass Market Paperback' },
  { value: 'a5', label: 'A5 — 5.83" × 8.27" (European standard)' },
  { value: '7x10', label: '7" × 10" — Large Format / Textbook' },
  { value: '8.5x11', label: '8.5" × 11" — Workbook / Large Format' },
];

const PLATFORMS = [
  {
    id: 'kdp',
    name: 'Amazon KDP',
    sub: 'Kindle + Print-on-Demand',
    color: '#FF9900',
    formats: ['EPUB', 'PDF'],
    url: 'https://kdp.amazon.com/en_US/title-setup/kindle/new/details',
  },
  {
    id: 'ingram',
    name: 'IngramSpark',
    sub: 'Global print + ebook distribution',
    color: '#00518A',
    formats: ['EPUB', 'PDF'],
    url: 'https://myaccount.ingramspark.com/Titles/Create',
  },
  {
    id: 'apple',
    name: 'Apple Books',
    sub: 'iOS, Mac & iPad readers',
    color: '#555',
    formats: ['EPUB'],
    url: 'https://authors.apple.com',
  },
  {
    id: 'kobo',
    name: 'Kobo Writing Life',
    sub: 'Rakuten Kobo e-readers',
    color: '#e51937',
    formats: ['EPUB'],
    url: 'https://www.kobo.com/us/en/p/writinglife',
  },
  {
    id: 'bn',
    name: 'Barnes & Noble Press',
    sub: 'Nook ebook + print',
    color: '#1A5C38',
    formats: ['EPUB', 'PDF'],
    url: 'https://press.barnesandnoble.com',
  },
  {
    id: 'd2d',
    name: 'Draft2Digital',
    sub: 'Multi-store distribution',
    color: '#2D2D2D',
    formats: ['EPUB'],
    url: 'https://www.draft2digital.com',
  },
  {
    id: 'smash',
    name: 'Smashwords',
    sub: 'Ebook retail + library',
    color: '#CC3300',
    formats: ['EPUB', 'DOCX'],
    url: 'https://www.smashwords.com/upload',
  },
  {
    id: 'pd',
    name: 'PublishDrive',
    sub: 'AI-powered distribution',
    color: '#6B46C1',
    formats: ['EPUB'],
    url: 'https://publishdrive.com',
  },
];

const SECTION_DIVIDER = (
  <div style={{ height: 1, background: 'rgba(0,0,0,0.08)', flexShrink: 0 }} />
);

const SECTION_LABEL_STYLE: React.CSSProperties = {
  fontSize: 9,
  fontWeight: 700,
  color: '#666',
  textTransform: 'uppercase',
  letterSpacing: '0.1em',
  marginBottom: 10,
};

export default function ExportModal({
  bookData,
  templateId,
  pages,
  onClose,
  onTemplateChange,
}: ExportModalProps) {
  const [exportState, setExportState] = useState<ExportState>('idle');
  const [dropCaps, setDropCaps] = useState(true);
  const [hideChapterNumbers, setHideChapterNumbers] = useState(false);
  const [trimSize, setTrimSize] = useState('6x9');
  const [selectedTemplateId, setSelectedTemplateId] = useState(templateId);
  const [templateCategory, setTemplateCategory] = useState<string>('all');
  const [epubSelected, setEpubSelected] = useState(true);
  const [pdfSelected, setPdfSelected] = useState(true);
  const [wordSelected, setWordSelected] = useState(false);

  const template = getTemplate(selectedTemplateId);

  const handleTemplateSelect = (id: string) => {
    setSelectedTemplateId(id);
    onTemplateChange?.(id);
  };

  const [publishingTo, setPublishingTo] = useState<string | null>(null);
  const [publishError, setPublishError] = useState<string | null>(null);

  const handlePublishTo = async (platform: typeof PLATFORMS[0]) => {
    setPublishingTo(platform.id);
    setPublishError(null);
    try {
      const JSZip = (await import('jszip')).default;
      const zip = new JSZip();
      const slug = bookData.title.replace(/\s+/g, '-').toLowerCase().replace(/[^a-z0-9-]/g, '');
      const needsEpub = platform.formats.includes('EPUB');
      const needsPdf = platform.formats.includes('PDF');
      const needsDocx = platform.formats.includes('DOCX');

      if (needsEpub) {
        const { generateEpub } = await import('@/lib/epub');
        const blob = await generateEpub(bookData, template);
        zip.file(`${slug}.epub`, blob);
      }
      if (needsPdf) {
        const pdfBlob = await generatePdfBlob(bookData, template, pages, trimSize, { dropCaps, hideChapterNumbers });
        zip.file(`${slug}.pdf`, pdfBlob);
      }
      if (needsDocx) {
        const { generateDocx } = await import('@/lib/docx-export');
        const blob = await generateDocx(bookData, template);
        zip.file(`${slug}.docx`, blob);
      }

      zip.file('README.txt', buildReadme(bookData, { epub: needsEpub, pdf: needsPdf, word: needsDocx }));

      const zipBlob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(zipBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${slug}-${platform.id}.zip`;
      a.click();
      URL.revokeObjectURL(url);

      // Open platform upload page
      window.open(platform.url, '_blank', 'noopener,noreferrer');
    } catch (err) {
      console.error(err);
      setPublishError('Export failed. Check your book has chapters and try again.');
      setTimeout(() => setPublishError(null), 6000);
    } finally {
      setPublishingTo(null);
    }
  };

  const handleExport = async () => {
    if (!epubSelected && !pdfSelected && !wordSelected) return;
    setExportState('generating');
    try {
      const JSZip = (await import('jszip')).default;
      const zip = new JSZip();
      const slug = bookData.title.replace(/\s+/g, '-').toLowerCase().replace(/[^a-z0-9-]/g, '');

      // ── EPUB
      if (epubSelected) {
        const { generateEpub } = await import('@/lib/epub');
        const blob = await generateEpub(bookData, template);
        zip.file(`${slug}.epub`, blob);
      }

      // ── Word (.docx)
      if (wordSelected) {
        const { generateDocx } = await import('@/lib/docx-export');
        const blob = await generateDocx(bookData, template);
        zip.file(`${slug}.docx`, blob);
      }

      // ── PDF (via jsPDF + html2canvas)
      if (pdfSelected) {
        const pdfBlob = await generatePdfBlob(bookData, template, pages, trimSize, { dropCaps, hideChapterNumbers });
        zip.file(`${slug}.pdf`, pdfBlob);
      }

      // ── README
      zip.file('README.txt', buildReadme(bookData, { epub: epubSelected, pdf: pdfSelected, word: wordSelected }));

      // ── Download ZIP
      const zipBlob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(zipBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${slug}-formatted.zip`;
      a.click();
      URL.revokeObjectURL(url);

      setExportState('done');
    } catch (err) {
      console.error(err);
      setExportState('error');
    }
  };

  const selectedCount = [epubSelected, pdfSelected, wordSelected].filter(Boolean).length;
  const exportButtonText =
    exportState === 'generating'
      ? 'Building your ZIP...'
      : exportState === 'done'
      ? '✓ ZIP Downloaded!'
      : exportState === 'error'
      ? '✗ Error — Try Again'
      : `Export ${selectedCount > 1 ? `${selectedCount} formats` : 'Book'} as ZIP →`;

  const nothingSelected = !epubSelected && !pdfSelected && !wordSelected;

  return (
    <>
      {/* Backdrop */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 50,
          background: 'rgba(0,0,0,0.7)',
        }}
        onClick={onClose}
      />

      {/* Modal */}
      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: 24 }}
        transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 50,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '16px',
          pointerEvents: 'none',
        }}
      >
        <div
          style={{
            pointerEvents: 'auto',
            width: '100%',
            maxWidth: 680,
            maxHeight: '93vh',
            overflowY: 'auto',
            background: '#FFFFFF',
            border: '3px solid #000',
            boxShadow: '8px 8px 0 #000',
            display: 'flex',
            flexDirection: 'column',
          }}
          onClick={(e) => e.stopPropagation()}
        >
          {/* ── HEADER ── */}
          <div
            style={{
              background: '#000',
              padding: '20px 24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexShrink: 0,
              borderBottom: '3px solid #000',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
              {/* Mini book cover */}
              <div style={{
                width: 36,
                height: 50,
                background: template.paperColor,
                border: '2px solid #FFE500',
                flexShrink: 0,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: 4,
                overflow: 'hidden',
                boxShadow: '3px 3px 0 #FFE500',
              }}>
                <div style={{
                  fontFamily: template.headingFont,
                  color: template.headingColor,
                  fontSize: 4.5,
                  fontWeight: 700,
                  textAlign: 'center',
                  lineHeight: 1.2,
                  wordBreak: 'break-word',
                }}>{bookData.title}</div>
                <div style={{
                  fontFamily: template.bodyFont,
                  color: template.inkColor,
                  fontSize: 3.5,
                  textAlign: 'center',
                  marginTop: 2,
                  opacity: 0.7,
                }}>{bookData.author}</div>
              </div>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <span style={{
                    background: '#FFE500',
                    border: '1.5px solid #FFE500',
                    color: '#000',
                    fontSize: 8,
                    fontWeight: 900,
                    letterSpacing: '0.14em',
                    textTransform: 'uppercase',
                    padding: '2px 8px',
                  }}>Export</span>
                </div>
                <h2 style={{ fontSize: 16, fontWeight: 900, color: '#fff', margin: 0, lineHeight: 1.2 }}>
                  {bookData.title}
                </h2>
                <p style={{ fontSize: 11, color: 'rgba(255,255,255,0.45)', margin: '3px 0 0', fontWeight: 500 }}>
                  {bookData.author} · {bookData.metadata.wordCount.toLocaleString()} words
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              style={{
                width: 34,
                height: 34,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: 'transparent',
                border: '2px solid rgba(255,255,255,0.25)',
                cursor: 'pointer',
                color: '#fff',
                flexShrink: 0,
                transition: 'all 0.1s',
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.background = '#FFE500';
                (e.currentTarget as HTMLElement).style.borderColor = '#FFE500';
                (e.currentTarget as HTMLElement).style.color = '#000';
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.background = 'transparent';
                (e.currentTarget as HTMLElement).style.borderColor = 'rgba(255,255,255,0.25)';
                (e.currentTarget as HTMLElement).style.color = '#fff';
              }}
            >
              <X size={15} />
            </button>
          </div>

          {/* ── SECTION: CHOOSE TEMPLATE ── */}
          <div style={{ borderBottom: '2px solid #000' }}>
            <div style={{
              padding: '14px 24px 0',
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              marginBottom: 10,
            }}>
              <div style={{
                background: '#000',
                color: '#FFE500',
                fontSize: 8,
                fontWeight: 900,
                letterSpacing: '0.14em',
                textTransform: 'uppercase',
                padding: '4px 10px',
              }}>01</div>
              <span style={{ fontSize: 11, fontWeight: 800, color: '#000', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Template
              </span>
              <span style={{ fontSize: 9, color: '#888', fontWeight: 600, marginLeft: 2 }}>
                {templates.length} styles
              </span>
            </div>

            {/* Category filter tabs */}
            {(() => {
              const cats = [
                { id: 'all', label: 'All' },
                { id: 'fiction', label: 'Literary' },
                { id: 'scifi', label: 'Sci-Fi & Fantasy' },
                { id: 'mystery', label: 'Mystery' },
                { id: 'romance', label: 'Romance' },
                { id: 'ya', label: 'YA' },
                { id: 'horror', label: 'Horror' },
                { id: 'nonfiction', label: 'Nonfiction' },
                { id: 'business', label: 'Business' },
                { id: 'poetry', label: 'Poetry' },
              ];
              const filtered = templateCategory === 'all'
                ? templates
                : templates.filter(t => t.category === templateCategory);
              return (
                <>
                  <div style={{
                    display: 'flex',
                    gap: 0,
                    overflowX: 'auto',
                    padding: '0 24px',
                    marginBottom: 0,
                    borderTop: '1.5px solid #eee',
                    scrollbarWidth: 'none',
                  }}>
                    {cats.map(cat => {
                      const count = cat.id === 'all' ? templates.length : templates.filter(t => t.category === cat.id).length;
                      if (count === 0) return null;
                      const isActive = templateCategory === cat.id;
                      return (
                        <button key={cat.id} onClick={() => setTemplateCategory(cat.id)} style={{
                          padding: '6px 10px',
                          border: 'none',
                          borderBottom: isActive ? '2px solid #000' : '2px solid transparent',
                          background: 'transparent',
                          fontSize: 9,
                          fontWeight: isActive ? 800 : 600,
                          color: isActive ? '#000' : '#888',
                          cursor: 'pointer',
                          whiteSpace: 'nowrap',
                          letterSpacing: '0.04em',
                          textTransform: 'uppercase',
                          flexShrink: 0,
                          transition: 'all 0.1s',
                        }}>
                          {cat.label} <span style={{ opacity: 0.5, fontSize: 8 }}>({count})</span>
                        </button>
                      );
                    })}
                  </div>

                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(5, 1fr)',
                    gap: 0,
                    maxHeight: 220,
                    overflowY: 'auto',
                    borderTop: '1.5px solid #eee',
                  }}>
                    {filtered.map((tmpl) => {
                      const isSelected = selectedTemplateId === tmpl.id;
                      return (
                        <button
                          key={tmpl.id}
                          onClick={() => handleTemplateSelect(tmpl.id)}
                          title={tmpl.description}
                          style={{
                            height: 72,
                            background: isSelected ? tmpl.paperColor : '#fafafa',
                            border: 'none',
                            borderRight: '1.5px solid #eee',
                            borderBottom: isSelected ? '3px solid #000' : '3px solid transparent',
                            cursor: 'pointer',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 4,
                            padding: '8px 6px',
                            transition: 'all 0.12s',
                            position: 'relative',
                          }}
                        >
                          {isSelected && (
                            <div style={{
                              position: 'absolute',
                              top: 4, right: 4,
                              width: 7, height: 7,
                              background: '#000',
                              borderRadius: '50%',
                            }} />
                          )}
                          <span style={{
                            fontFamily: tmpl.headingFont,
                            color: tmpl.headingColor,
                            fontSize: 9,
                            fontWeight: 700,
                            textAlign: 'center',
                            lineHeight: 1.2,
                          }}>
                            Ch. One
                          </span>
                          <span style={{
                            fontSize: 7,
                            color: isSelected ? '#000' : '#999',
                            fontWeight: 700,
                            textTransform: 'uppercase',
                            letterSpacing: '0.04em',
                            textAlign: 'center',
                            lineHeight: 1.3,
                          }}>
                            {tmpl.name}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </>
              );
            })()}
          </div>

          {/* ── SECTION: SETTINGS ROW ── */}
          <div style={{ borderBottom: '2px solid #000', padding: '14px 24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
              <div style={{
                background: '#000', color: '#FFE500',
                fontSize: 8, fontWeight: 900, letterSpacing: '0.14em',
                textTransform: 'uppercase', padding: '4px 10px',
              }}>02</div>
              <span style={{ fontSize: 11, fontWeight: 800, color: '#000', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Settings
              </span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
              {/* Trim size */}
              <div style={{ gridColumn: '1 / -1' }}>
                <div style={{ fontSize: 9, fontWeight: 800, color: '#666', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: 6 }}>
                  Trim Size
                </div>
                <select
                  value={trimSize}
                  onChange={(e) => setTrimSize(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '9px 12px',
                    fontSize: 12,
                    fontWeight: 600,
                    color: '#000',
                    background: '#fff',
                    border: '2px solid #000',
                    cursor: 'pointer',
                    outline: 'none',
                    appearance: 'none',
                    backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23000' strokeWidth='2' fill='none'/%3E%3C/svg%3E")`,
                    backgroundRepeat: 'no-repeat',
                    backgroundPosition: 'right 12px center',
                    paddingRight: 36,
                  }}
                >
                  {TRIM_SIZES.map((t) => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </div>

              {/* Drop caps toggle */}
              <label style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                cursor: 'pointer',
                padding: '10px 12px',
                border: dropCaps ? '2px solid #000' : '2px solid #ddd',
                background: dropCaps ? '#FFE500' : '#fafafa',
                boxShadow: dropCaps ? '2px 2px 0 #000' : 'none',
                transition: 'all 0.1s',
                userSelect: 'none',
              }}>
                <input
                  type="checkbox"
                  checked={dropCaps}
                  onChange={(e) => setDropCaps(e.target.checked)}
                  style={{ display: 'none' }}
                />
                <div style={{
                  width: 16, height: 16,
                  border: '2px solid #000',
                  background: dropCaps ? '#000' : '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  {dropCaps && <div style={{ width: 6, height: 6, background: '#FFE500' }} />}
                </div>
                <span style={{ fontSize: 11, fontWeight: 700, color: '#000', lineHeight: 1.3 }}>
                  Drop Caps
                </span>
              </label>

              {/* Hide chapter numbers toggle */}
              <label style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                cursor: 'pointer',
                padding: '10px 12px',
                border: hideChapterNumbers ? '2px solid #000' : '2px solid #ddd',
                background: hideChapterNumbers ? '#FFE500' : '#fafafa',
                boxShadow: hideChapterNumbers ? '2px 2px 0 #000' : 'none',
                transition: 'all 0.1s',
                userSelect: 'none',
                gridColumn: 'span 2',
              }}>
                <input
                  type="checkbox"
                  checked={hideChapterNumbers}
                  onChange={(e) => setHideChapterNumbers(e.target.checked)}
                  style={{ display: 'none' }}
                />
                <div style={{
                  width: 16, height: 16,
                  border: '2px solid #000',
                  background: hideChapterNumbers ? '#000' : '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  {hideChapterNumbers && <div style={{ width: 6, height: 6, background: '#FFE500' }} />}
                </div>
                <span style={{ fontSize: 11, fontWeight: 700, color: '#000', lineHeight: 1.3 }}>
                  Hide Chapter Numbers
                </span>
              </label>
            </div>
          </div>

          {/* ── SECTION: EXPORT FORMAT ── */}
          <div style={{ borderBottom: '2px solid #000', padding: '14px 24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
              <div style={{
                background: '#000', color: '#FFE500',
                fontSize: 8, fontWeight: 900, letterSpacing: '0.14em',
                textTransform: 'uppercase', padding: '4px 10px',
              }}>03</div>
              <span style={{ fontSize: 11, fontWeight: 800, color: '#000', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Formats
              </span>
              <span style={{ fontSize: 10, color: '#999', fontWeight: 600, marginLeft: 2 }}>
                — all go into your ZIP
              </span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
              {/* EPUB */}
              <button
                onClick={() => setEpubSelected((v) => !v)}
                style={{
                  padding: '14px 14px',
                  background: epubSelected ? '#000' : '#fafafa',
                  border: '2px solid #000',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.1s',
                  boxShadow: epubSelected ? '3px 3px 0 #FFE500' : 'none',
                  position: 'relative',
                  overflow: 'hidden',
                }}
              >
                <div style={{
                  width: 28, height: 28,
                  background: epubSelected ? '#FFE500' : '#eee',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  marginBottom: 10,
                  fontSize: 12, fontWeight: 900, color: '#000',
                }}>E</div>
                <div style={{ fontSize: 13, fontWeight: 900, color: epubSelected ? '#FFE500' : '#111', marginBottom: 4, letterSpacing: '-0.01em' }}>
                  EPUB
                </div>
                <div style={{ fontSize: 9, color: epubSelected ? 'rgba(255,255,255,0.55)' : '#999', lineHeight: 1.5, fontWeight: 500 }}>
                  Kindle · Apple Books · Kobo · all e-readers
                </div>
                {epubSelected && (
                  <div style={{
                    position: 'absolute', top: 8, right: 8,
                    width: 18, height: 18,
                    background: '#FFE500',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 10, fontWeight: 900, color: '#000',
                  }}>✓</div>
                )}
              </button>

              {/* PDF */}
              <button
                onClick={() => setPdfSelected((v) => !v)}
                style={{
                  padding: '14px 14px',
                  background: pdfSelected ? '#000' : '#fafafa',
                  border: '2px solid #000',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.1s',
                  boxShadow: pdfSelected ? '3px 3px 0 #FFE500' : 'none',
                  position: 'relative',
                  overflow: 'hidden',
                }}
              >
                <div style={{
                  width: 28, height: 28,
                  background: pdfSelected ? '#FF2D78' : '#eee',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  marginBottom: 10,
                  fontSize: 12, fontWeight: 900, color: '#fff',
                }}>P</div>
                <div style={{ fontSize: 13, fontWeight: 900, color: pdfSelected ? '#FFE500' : '#111', marginBottom: 4, letterSpacing: '-0.01em' }}>
                  Print PDF
                </div>
                <div style={{ fontSize: 9, color: pdfSelected ? 'rgba(255,255,255,0.55)' : '#999', lineHeight: 1.5, fontWeight: 500 }}>
                  KDP Print · IngramSpark · POD
                </div>
                {pdfSelected && (
                  <div style={{
                    position: 'absolute', top: 8, right: 8,
                    width: 18, height: 18,
                    background: '#FFE500',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 10, fontWeight: 900, color: '#000',
                  }}>✓</div>
                )}
              </button>

              {/* DOCX */}
              <button
                onClick={() => setWordSelected((v) => !v)}
                style={{
                  padding: '14px 14px',
                  background: wordSelected ? '#000' : '#fafafa',
                  border: '2px solid #000',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.1s',
                  boxShadow: wordSelected ? '3px 3px 0 #FFE500' : 'none',
                  position: 'relative',
                  overflow: 'hidden',
                }}
              >
                <div style={{
                  width: 28, height: 28,
                  background: '#2B5CE6',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  marginBottom: 10,
                  fontSize: 12, fontWeight: 900, color: '#fff',
                }}>W</div>
                <div style={{ fontSize: 13, fontWeight: 900, color: wordSelected ? '#FFE500' : '#111', marginBottom: 4, letterSpacing: '-0.01em' }}>
                  Word
                </div>
                <div style={{ fontSize: 9, color: wordSelected ? 'rgba(255,255,255,0.55)' : '#999', lineHeight: 1.5, fontWeight: 500 }}>
                  Microsoft Word · Google Docs
                </div>
                {wordSelected && (
                  <div style={{
                    position: 'absolute', top: 8, right: 8,
                    width: 18, height: 18,
                    background: '#FFE500',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 10, fontWeight: 900, color: '#000',
                  }}>✓</div>
                )}
              </button>
            </div>
          </div>

          {/* ── SECTION: DISTRIBUTE TO PLATFORM ── */}
          <div style={{ borderBottom: '2px solid #000', padding: '14px 24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
              <div style={{
                background: '#000', color: '#FFE500',
                fontSize: 8, fontWeight: 900, letterSpacing: '0.14em',
                textTransform: 'uppercase', padding: '4px 10px',
              }}>04</div>
              <span style={{ fontSize: 11, fontWeight: 800, color: '#000', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Publish Directly To
              </span>
            </div>
            <p style={{ fontSize: 10, color: '#888', margin: '0 0 12px', fontWeight: 500 }}>
              Downloads the right files + opens the platform's upload page
            </p>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
              {PLATFORMS.map((p) => {
                const isLoading = publishingTo === p.id;
                return (
                  <button
                    key={p.id}
                    onClick={() => handlePublishTo(p)}
                    disabled={!!publishingTo}
                    style={{
                      display: 'flex',
                      alignItems: 'stretch',
                      background: '#fff',
                      border: '2px solid #000',
                      overflow: 'hidden',
                      cursor: publishingTo ? 'not-allowed' : 'pointer',
                      opacity: publishingTo && !isLoading ? 0.4 : 1,
                      textAlign: 'left',
                      padding: 0,
                      transition: 'all 0.1s',
                      boxShadow: isLoading ? '3px 3px 0 #000' : 'none',
                    }}
                    onMouseEnter={(e) => {
                      if (!publishingTo) {
                        (e.currentTarget as HTMLElement).style.boxShadow = `3px 3px 0 ${p.color}`;
                        (e.currentTarget as HTMLElement).style.transform = 'translate(-1px,-1px)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.boxShadow = isLoading ? '3px 3px 0 #000' : 'none';
                      (e.currentTarget as HTMLElement).style.transform = '';
                    }}
                  >
                    {/* Color stripe */}
                    <div style={{ width: 6, background: p.color, flexShrink: 0 }} />
                    <div style={{ padding: '10px 12px', flex: 1, minWidth: 0 }}>
                      <div style={{
                        fontSize: 11, fontWeight: 800, color: '#000', marginBottom: 2,
                        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      }}>
                        {isLoading ? '⏳ Preparing…' : p.name}
                      </div>
                      <div style={{
                        fontSize: 9, color: '#888', marginBottom: 6,
                        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                        fontWeight: 500,
                      }}>
                        {p.sub}
                      </div>
                      <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                        {p.formats.map(f => (
                          <span key={f} style={{
                            fontSize: 8, fontWeight: 800,
                            background: p.color,
                            color: '#fff',
                            padding: '2px 6px',
                            letterSpacing: '0.04em',
                          }}>{f}</span>
                        ))}
                      </div>
                    </div>
                    <div style={{
                      padding: '0 12px',
                      display: 'flex', alignItems: 'center',
                      fontSize: 16, color: '#000', flexShrink: 0,
                      fontWeight: 900,
                    }}>→</div>
                  </button>
                );
              })}
            </div>
            {publishError && (
              <div style={{
                marginTop: 10, padding: '8px 12px',
                background: '#fff0f0', border: '2px solid #c0392b',
                fontSize: 10, fontWeight: 700, color: '#c0392b',
              }}>
                {publishError}
              </div>
            )}
          </div>

          {/* ── SECTION: DOWNLOAD ZIP ── */}
          <div style={{ padding: '20px 24px', background: '#000' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
              <div style={{
                background: '#FFE500', color: '#000',
                fontSize: 8, fontWeight: 900, letterSpacing: '0.14em',
                textTransform: 'uppercase', padding: '4px 10px',
              }}>05</div>
              <span style={{ fontSize: 11, fontWeight: 800, color: '#fff', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                Download ZIP
              </span>
            </div>

            <button
              onClick={handleExport}
              disabled={exportState === 'generating' || nothingSelected}
              style={{
                width: '100%',
                padding: '16px 20px',
                background: exportState === 'done'
                  ? '#00C853'
                  : exportState === 'error'
                  ? '#FF2D78'
                  : nothingSelected
                  ? '#555'
                  : '#FFE500',
                border: '2px solid',
                borderColor: exportState === 'done' ? '#00C853' : exportState === 'error' ? '#FF2D78' : nothingSelected ? '#555' : '#FFE500',
                boxShadow: exportState === 'generating' || nothingSelected ? 'none' : '4px 4px 0 #FFE500',
                fontSize: 15,
                fontWeight: 900,
                color: exportState === 'done' || exportState === 'error' ? '#fff' : '#000',
                cursor: exportState === 'generating' || nothingSelected ? 'not-allowed' : 'pointer',
                opacity: nothingSelected ? 0.4 : 1,
                transition: 'all 0.1s',
                letterSpacing: '0.01em',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 10,
              }}
              onMouseEnter={(e) => {
                if (exportState !== 'generating' && !nothingSelected) {
                  (e.currentTarget as HTMLElement).style.transform = 'translate(-2px,-2px)';
                  (e.currentTarget as HTMLElement).style.boxShadow = '6px 6px 0 #FFE500';
                }
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.transform = '';
                (e.currentTarget as HTMLElement).style.boxShadow = exportState === 'generating' || nothingSelected ? 'none' : '4px 4px 0 #FFE500';
              }}
            >
              {exportState === 'generating' ? (
                <><span style={{ display: 'inline-block', animation: 'spin 1s linear infinite' }}>⟳</span> Building your ZIP…</>
              ) : exportState === 'done' ? (
                <>✓ ZIP Downloaded!</>
              ) : exportState === 'error' ? (
                <>✗ Error — Try Again</>
              ) : (
                <>
                  <span style={{
                    background: '#000', color: '#FFE500',
                    fontSize: 10, fontWeight: 900,
                    padding: '3px 8px', letterSpacing: '0.06em',
                  }}>
                    {selectedCount} {selectedCount === 1 ? 'FORMAT' : 'FORMATS'}
                  </span>
                  Export as ZIP →
                </>
              )}
            </button>
            <div style={{ display: 'flex', justifyContent: 'center', gap: 20, marginTop: 12 }}>
              {['Free forever', 'No watermarks', 'Pro quality'].map(tag => (
                <span key={tag} style={{
                  fontSize: 9, fontWeight: 700,
                  color: 'rgba(255,255,255,0.35)',
                  letterSpacing: '0.1em',
                  textTransform: 'uppercase',
                  display: 'flex', alignItems: 'center', gap: 5,
                }}>
                  <span style={{ color: '#FFE500', fontSize: 8 }}>✦</span> {tag}
                </span>
              ))}
            </div>
          </div>
        </div>
      </motion.div>
    </>
  );
}

// ─────────────────────────────────────────
//  PDF HTML BUILDER
// ─────────────────────────────────────────

// ── Drop cap: extract first text character from HTML safely ──────────
// naive `str.replace(letter, '')` breaks tag names; this walks the string.
function extractDropCapLetter(html: string): { letter: string; rest: string } | null {
  let inTag = false;
  for (let i = 0; i < html.length; i++) {
    const ch = html[i];
    if (ch === '<') { inTag = true; continue; }
    if (ch === '>') { inTag = false; continue; }
    if (!inTag && ch.trim()) {
      return { letter: ch, rest: html.slice(0, i) + html.slice(i + 1) };
    }
  }
  return null;
}

// Roman numerals
function toRomanNumeral(n: number): string {
  const vals = [1000,900,500,400,100,90,50,40,10,9,5,4,1];
  const syms = ['M','CM','D','CD','C','XC','L','XL','X','IX','V','IV','I'];
  let r = '';
  for (let i = 0; i < vals.length; i++) { while (n >= vals[i]) { r += syms[i]; n -= vals[i]; } }
  return r;
}
const SPELLED_NUMS = ['','One','Two','Three','Four','Five','Six','Seven','Eight','Nine',
  'Ten','Eleven','Twelve','Thirteen','Fourteen','Fifteen','Sixteen','Seventeen',
  'Eighteen','Nineteen','Twenty','Twenty-One','Twenty-Two','Twenty-Three','Twenty-Four','Twenty-Five'];
function formatChNum(n: number, style: string): string {
  if (style === 'roman') return toRomanNumeral(n);
  if (style === 'spelled') return n < SPELLED_NUMS.length ? SPELLED_NUMS[n] : String(n);
  return String(n);
}

// Parse Tiptap HTML into block elements (p, h1, h2, h3)
interface HtmlBlock { type: 'p'|'h1'|'h2'|'h3'; inner: string; attrs: string; }
function parseBlocks(html: string): HtmlBlock[] {
  const blocks: HtmlBlock[] = [];
  const re = /<(h[1-3]|p)([^>]*)>([\s\S]*?)<\/\1>/gi;
  let m: RegExpExecArray | null;
  while ((m = re.exec(html)) !== null) {
    blocks.push({ type: m[1].toLowerCase() as HtmlBlock['type'], attrs: m[2], inner: m[3] });
  }
  return blocks;
}

function buildPrintHTML(
  bookData: BookData,
  template: ReturnType<typeof getTemplate>,
  _pages: BookPage[],
  trimSize = '6x9',
  options: { dropCaps: boolean; hideChapterNumbers: boolean } = { dropCaps: true, hideChapterNumbers: false }
): string {
  const pageSize = TRIM_SIZE_CSS[trimSize] ?? '6in 9in';

  // Trim size -> pixel dimensions (pt * 96/72)
  const TRIM_PT: Record<string, [number, number]> = {
    '5x8':     [360, 576],  '5.5x8.5': [396, 612],  '6x9':    [432, 648],
    'digest':  [364, 562],  'mass':     [306, 495],  'a5':     [420, 595],
    '7x10':    [504, 720],  '8.5x11':  [612, 792],
  };
  const [ptW, ptH] = TRIM_PT[trimSize] ?? [432, 648];
  const pxW = Math.round(ptW * 96 / 72);
  const pxH = Math.round(ptH * 96 / 72);

  // Typography at real print scale (not the tiny preview scale)
  const bodyPx    = Math.round(11   * 96 / 72); // 11pt  ~15px
  const headPx    = Math.round(22   * 96 / 72); // 22pt  ~29px
  const chapNumPx = Math.round(10   * 96 / 72); // 10pt  ~13px
  const rulerPx   = Math.round(7.5  * 96 / 72); // 7.5pt ~10px
  const lineH     = parseFloat(template.lineHeight) || 1.6;

  // Margins: inner (gutter) wider than outer for binding
  const mTop   = Math.round(pxH * 0.111); // ~0.9in on 6x9
  const mBot   = Math.round(pxH * 0.111);
  const mOuter = Math.round(pxW * 0.125); // ~0.75in
  const mInner = Math.round(pxW * 0.146); // ~0.875in gutter

  // Content zone
  const headH    = Math.round(rulerPx * 2.4 + 8);
  const folioH   = Math.round(bodyPx  * 2.2);
  const contentW = pxW - mOuter - mInner;
  const contentH = pxH - mTop - mBot - headH - folioH;

  // Pagination heuristic
  const charsPerLine = Math.max(30, Math.floor(contentW / (bodyPx * 0.48)));
  const linesPerPage = Math.max(10, Math.floor(contentH / (bodyPx * lineH)));

  let pageNum = 1;
  const allPages: string[] = [];

  function addPage(
    content: string,
    opts: { isChapterStart?: boolean; chapterTitle?: string; suppressHead?: boolean } = {}
  ): void {
    const even     = pageNum % 2 === 0; // even = verso, odd = recto
    const showHead = !opts.suppressHead && !opts.isChapterStart;
    const headTxt  = showHead
      ? (even ? (bookData.author || '') : (opts.chapterTitle || ''))
      : '';
    // Verso: outer margin on left.  Recto: gutter on left.
    const cLeft = even ? mOuter : mInner;

    const runHead = headTxt
      ? '<div style="position:absolute;top:' + mTop + 'px;'
        + (even ? 'left:' + mOuter + 'px;' : 'right:' + mOuter + 'px;')
        + 'width:' + contentW + 'px;height:' + headH + 'px;'
        + 'display:flex;align-items:flex-end;'
        + (even ? 'justify-content:flex-start;' : 'justify-content:flex-end;')
        + 'padding-bottom:3px;border-bottom:0.5px solid ' + template.inkColor + '55;">'
        + '<span style="font-family:' + template.bodyFont + ';font-size:' + rulerPx + 'px;'
        + 'color:' + template.inkColor + ';font-variant:small-caps;'
        + 'letter-spacing:0.09em;opacity:0.65;">' + headTxt + '</span>'
        + '</div>'
      : '';

    const folio = showHead
      ? '<div style="position:absolute;bottom:' + Math.round(mBot * 0.45) + 'px;'
        + (even ? 'left:' + mOuter + 'px' : 'right:' + mOuter + 'px') + ';'
        + 'font-family:' + template.bodyFont + ';font-size:' + rulerPx + 'px;'
        + 'color:' + template.inkColor + ';opacity:0.5;">' + pageNum + '</div>'
      : '';

    const cTop = mTop + (opts.isChapterStart ? 0 : headH);
    const cH   = pxH - cTop - Math.round(mBot * 0.9);

    allPages.push(
      '<div class="book-page" style="width:' + pxW + 'px;height:' + pxH + 'px;'
      + 'position:relative;overflow:hidden;background:' + (template.paperColor || '#fff') + ';">'
      + runHead
      + '<div style="position:absolute;top:' + cTop + 'px;left:' + cLeft + 'px;'
      + 'width:' + contentW + 'px;height:' + cH + 'px;overflow:hidden;">'
      + content
      + '</div>'
      + folio
      + '</div>'
    );
    pageNum++;
  }

  function addBlank(): void   { addPage('', { suppressHead: true }); }
  function ensureRecto(): void { if (pageNum % 2 === 0) addBlank(); }

  const yr = new Date().getFullYear();

  // ---- Half-title page (recto, page 1) ──────────────────────────────
  if (pageNum % 2 === 0) addBlank();
  addPage(
    '<div style="display:flex;flex-direction:column;align-items:center;'
    + 'justify-content:center;height:100%;text-align:center;">'
    + '<div style="font-family:' + template.headingFont + ';'
    + 'font-size:' + Math.round(bodyPx * 1.25) + 'px;'
    + 'font-weight:' + template.headingWeight + ';color:' + template.inkColor + ';'
    + 'text-transform:' + template.headingTransform + ';letter-spacing:0.04em;">'
    + escapeHtml(bookData.title) + '</div></div>',
    { suppressHead: true }
  );

  // ---- Title page (recto, page 3) ────────────────────────────────────
  ensureRecto();
  addPage(
    '<div style="display:flex;flex-direction:column;align-items:center;'
    + 'justify-content:center;height:100%;text-align:center;">'
    + '<div style="font-family:' + template.headingFont + ';'
    + 'font-size:' + Math.round(headPx * 1.45) + 'px;'
    + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
    + 'line-height:1.2;margin-bottom:0.25em;text-transform:' + template.headingTransform + ';">'
    + escapeHtml(bookData.title) + '</div>'
    + (bookData.subtitle
        ? '<div style="font-family:' + template.headingFont + ';'
          + 'font-size:' + Math.round(bodyPx * 1.1) + 'px;font-style:italic;'
          + 'color:' + template.accentColor + ';margin-bottom:1.8em;">'
          + escapeHtml(bookData.subtitle) + '</div>'
        : '')
    + '<div style="font-family:' + template.bodyFont + ';'
    + 'font-size:' + Math.round(bodyPx * 1.05) + 'px;'
    + 'color:' + template.inkColor + ';margin-top:2.5em;">'
    + escapeHtml(bookData.author || '') + '</div></div>',
    { suppressHead: true }
  );

  // ---- Copyright page (verso, page 4) ────────────────────────────────
  addPage(
    '<div style="position:absolute;bottom:' + (folioH + 4) + 'px;left:0;right:0;'
    + 'font-size:' + Math.round(bodyPx * 0.72) + 'px;line-height:1.75;'
    + 'color:' + template.inkColor + ';opacity:0.72;">'
    + '<p style="margin-bottom:0.5em;">Copyright &copy; ' + yr + ' '
    + escapeHtml(bookData.author || 'the Author') + '</p>'
    + '<p style="margin-bottom:0.5em;">All rights reserved. No part of this publication may be '
    + 'reproduced, distributed, or transmitted in any form or by any means without the prior '
    + 'written permission of the publisher.</p>'
    + '<p>Formatted with <em>Booksane</em>.</p></div>',
    { suppressHead: true }
  );

  // ---- Dedication ─────────────────────────────────────────────────────
  if (bookData.dedication) {
    ensureRecto();
    addPage(
      '<div style="display:flex;flex-direction:column;align-items:center;'
      + 'justify-content:center;height:100%;text-align:center;">'
      + '<p style="font-style:italic;font-size:' + bodyPx + 'px;line-height:1.9;'
      + 'max-width:' + Math.round(contentW * 0.82) + 'px;color:' + template.inkColor + ';">'
      + escapeHtml(bookData.dedication) + '</p></div>',
      { suppressHead: true }
    );
  }

  // ---- Epigraph ───────────────────────────────────────────────────────
  if (bookData.epigraph) {
    ensureRecto();
    addPage(
      '<div style="display:flex;flex-direction:column;justify-content:center;'
      + 'height:100%;max-width:' + Math.round(contentW * 0.85) + 'px;">'
      + '<p style="font-style:italic;font-size:' + bodyPx + 'px;line-height:1.9;'
      + 'margin-bottom:0.6em;color:' + template.inkColor + ';">'
      + '&ldquo;' + escapeHtml(bookData.epigraph) + '&rdquo;</p>'
      + (bookData.epigraphAttribution
          ? '<p style="font-size:' + Math.round(bodyPx * 0.85) + 'px;'
            + 'color:' + template.accentColor + ';text-align:right;">'
            + '\u2014 ' + escapeHtml(bookData.epigraphAttribution) + '</p>'
          : '')
      + '</div>',
      { suppressHead: true }
    );
  }

  // ---- TOC layout metrics (calculated here so we can reserve the right number of pages)
  const tocTitlePx     = Math.round(bodyPx * 1.3);
  const tocEntryPx     = Math.round(bodyPx * 0.9);
  const tocGap         = Math.round(tocEntryPx * 0.55);
  const entriesPerPage = Math.max(10, Math.floor(
    (contentH - tocTitlePx * 3) / (tocEntryPx + tocGap)
  ));
  // Reserve exactly enough pages for all chapters, always at least 2
  const tocPageCount   = Math.max(2, Math.ceil(bookData.chapters.length / entriesPerPage) + 1);

  // ---- TOC placeholder (recto) ──────────────────────────────────────
  ensureRecto();
  const tocSlotIdx     = allPages.length;
  const tocStartPgNum  = pageNum;
  for (let _ti = 0; _ti < tocPageCount; _ti++) {
    allPages.push(`__TOC_PLACEHOLDER_${_ti}__`);
  }
  pageNum += tocPageCount;

  // ---- Chapters ─────────────────────────────────────────────────────
  const tocEntries: Array<{ label: string; page: number }> = [];

  for (const chapter of bookData.chapters) {
    ensureRecto(); // chapters always start on a right-hand (odd) page

    // Record this chapter's start page for the TOC
    const isMeaningful = chapter.title &&
      !/^chapter\s+\d+$/i.test(chapter.title.trim());
    const chTitle = isMeaningful ? chapter.title.trim() : 'Chapter ' + chapter.number;
    const chNumDisplay = formatChNum(chapter.number, template.chapterNumberStyle || 'numeral');
    const tocLabel = options.hideChapterNumbers
      ? chTitle
      : (template.chapterNumberStyle === 'numeral'
          ? ('Chapter ' + chNumDisplay + (isMeaningful ? ': ' + chTitle : ''))
          : (chNumDisplay + (isMeaningful ? '. ' + chTitle : '')));
    tocEntries.push({ label: tocLabel, page: pageNum });

    const startDrop   = Math.round(contentH * 0.25);
    const headStyle   = (template as ReturnType<typeof getTemplate> & { chapterHeadingStyle?: string }).chapterHeadingStyle ?? 'classic';
    const showNum     = !options.hideChapterNumbers && template.chapterNumberStyle !== 'none';
    const numPrefix   = template.chapterNumberStyle === 'numeral' ? 'Chapter ' : '';
    const numLabel    = showNum ? (numPrefix + chNumDisplay) : '';
    const ruleColor   = template.accentColor + '88'; // 53% opacity rule
    const ruleW       = Math.round(contentW * 0.55);

    // ── Build heading block by style ─────────────────────────────────
    let headingBlock = '';

    if (headStyle === 'ruled') {
      // Thin rules above and below the title block
      headingBlock =
        '<div style="text-align:' + template.headingAlign + ';padding-top:' + startDrop + 'px;">'
        + '<div style="border-top:0.75px solid ' + ruleColor + ';margin-bottom:0.65em;width:' + ruleW + 'px;'
        + (template.headingAlign === 'center' ? 'margin-left:auto;margin-right:auto;' : '') + '"></div>'
        + (numLabel ? '<div style="font-family:' + template.headingFont + ';font-size:' + chapNumPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.accentColor + ';'
          + 'letter-spacing:0.2em;text-transform:uppercase;margin-bottom:0.3em;">' + numLabel + '</div>' : '')
        + (isMeaningful ? '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
          + 'text-transform:' + template.headingTransform + ';line-height:1.2;margin-bottom:0.6em;">'
          + escapeHtml(chTitle) + '</div>' : '')
        + '<div style="border-bottom:0.75px solid ' + ruleColor + ';margin-top:0.3em;margin-bottom:1.4em;'
        + 'width:' + ruleW + 'px;'
        + (template.headingAlign === 'center' ? 'margin-left:auto;margin-right:auto;' : '') + '"></div>'
        + '</div>';

    } else if (headStyle === 'large-num') {
      // Huge faded chapter number as visual anchor, title below
      const bigNumPx = Math.round(headPx * 3.5);
      headingBlock =
        '<div style="text-align:' + template.headingAlign + ';padding-top:' + startDrop + 'px;">'
        + (showNum ? '<div style="font-family:' + template.headingFont + ';font-size:' + bigNumPx + 'px;'
          + 'font-weight:700;color:' + template.accentColor + ';opacity:0.13;line-height:0.9;'
          + 'margin-bottom:-' + Math.round(bigNumPx * 0.18) + 'px;letter-spacing:-0.02em;">'
          + chNumDisplay + '</div>' : '')
        + (isMeaningful ? '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
          + 'text-transform:' + template.headingTransform + ';line-height:1.2;'
          + 'position:relative;margin-bottom:1.4em;">' + escapeHtml(chTitle) + '</div>' : '')
        + (showNum && !isMeaningful ? '<div style="margin-bottom:1.4em;"></div>' : '')
        + '</div>';

    } else if (headStyle === 'badge') {
      // Chapter number in a decorative framed box, then title
      const badgePx = Math.round(chapNumPx * 1.3);
      const badgeSz = Math.round(badgePx * 2.4);
      headingBlock =
        '<div style="text-align:' + template.headingAlign + ';padding-top:' + startDrop + 'px;">'
        + (showNum ? '<div style="display:inline-block;width:' + badgeSz + 'px;height:' + badgeSz + 'px;'
          + 'border:1.5px solid ' + template.accentColor + ';margin-bottom:0.75em;'
          + 'display:flex;align-items:center;justify-content:center;'
          + (template.headingAlign === 'center' ? 'margin-left:auto;margin-right:auto;' : '') + '">'
          + '<span style="font-family:' + template.headingFont + ';font-size:' + badgePx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.accentColor + ';'
          + 'line-height:1;">' + chNumDisplay + '</span>'
          + '</div>' : '')
        + (isMeaningful ? '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
          + 'text-transform:' + template.headingTransform + ';line-height:1.2;margin-bottom:1.4em;">'
          + escapeHtml(chTitle) + '</div>' : '<div style="margin-bottom:1.4em;"></div>')
        + '</div>';

    } else if (headStyle === 'stacked') {
      // Number label, thin rule underneath, then title
      const ruleW2 = Math.round(contentW * 0.18);
      headingBlock =
        '<div style="text-align:' + template.headingAlign + ';padding-top:' + startDrop + 'px;">'
        + (numLabel ? '<div style="font-family:' + template.headingFont + ';font-size:' + chapNumPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.accentColor + ';'
          + 'letter-spacing:0.2em;text-transform:uppercase;margin-bottom:0.5em;">' + numLabel + '</div>' : '')
        + '<div style="width:' + ruleW2 + 'px;height:1px;background:' + template.accentColor + ';opacity:0.5;'
        + 'margin-bottom:0.65em;'
        + (template.headingAlign === 'center' ? 'margin-left:auto;margin-right:auto;' : '') + '"></div>'
        + (isMeaningful ? '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
          + 'text-transform:' + template.headingTransform + ';line-height:1.2;margin-bottom:1.4em;">'
          + escapeHtml(chTitle) + '</div>' : '<div style="margin-bottom:1.4em;"></div>')
        + '</div>';

    } else if (headStyle === 'minimal') {
      // Tiny all-caps label, no decoration, maximum whitespace
      headingBlock =
        '<div style="text-align:' + template.headingAlign + ';padding-top:' + startDrop + 'px;">'
        + (numLabel ? '<div style="font-family:' + template.headingFont + ';font-size:' + Math.round(chapNumPx * 0.85) + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.accentColor + ';'
          + 'letter-spacing:0.22em;text-transform:uppercase;margin-bottom:0.7em;opacity:0.7;">' + numLabel + '</div>' : '')
        + (isMeaningful ? '<div style="font-family:' + template.headingFont + ';font-size:' + Math.round(headPx * 0.88) + 'px;'
          + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
          + 'text-transform:' + template.headingTransform + ';line-height:1.2;margin-bottom:1.6em;">'
          + escapeHtml(chTitle) + '</div>' : '<div style="margin-bottom:1.6em;"></div>')
        + '</div>';

    } else {
      // classic (default): centered, number above title
      headingBlock =
        '<div style="padding-top:' + startDrop + 'px;text-align:' + template.headingAlign + ';">'
        + (numLabel ? '<span style="display:block;font-family:' + template.headingFont + ';'
          + 'font-size:' + chapNumPx + 'px;font-weight:' + template.headingWeight + ';'
          + 'color:' + template.accentColor + ';letter-spacing:0.18em;'
          + 'text-transform:uppercase;margin-bottom:0.55em;">' + numLabel + '</span>' : '')
        + (isMeaningful
            ? '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
              + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
              + 'text-transform:' + template.headingTransform + ';line-height:1.2;'
              + 'margin-bottom:1.4em;">' + escapeHtml(chTitle) + '</div>'
            : '<div style="margin-bottom:1.4em;"></div>')
        + '</div>';
    }

    const chapterHeader = headingBlock;

    // Estimated lines consumed by the header block
    const headerLines = Math.round(startDrop / (bodyPx * lineH))
      + (isMeaningful ? Math.ceil((headPx * 1.2) / (bodyPx * lineH)) + 1 : 0)
      + 3;

    // Parse Tiptap HTML: handles <p>, <h1>, <h2>, <h3>
    const blocks = parseBlocks(chapter.content);

    let pageContent   = chapterHeader;
    let linesUsed     = headerLines;
    let isChapterPage = true;
    let isFirstPara   = true;

    // Drop cap size: 3 lines tall
    const dropCapPx = Math.round(bodyPx * lineH * 3);

    for (const block of blocks) {
      const plainText = block.inner.replace(/<[^>]*>/g, '').trim();

      // ── Section break ──
      const isSectionBreak = /class="section-break"/.test(block.attrs + block.inner)
        || /^[*\u2217]{1,3}$/.test(plainText);
      if (isSectionBreak) {
        // Use template ornament, fall back to * * *
        const ornamentHtml = template.ornament
          ? template.ornament.replace(/\[ACCENT\]/g, template.accentColor)
          : '<div style="text-align:center;margin:1.2em 0;color:' + template.accentColor
            + ';font-size:' + bodyPx + 'px;opacity:0.55;">* * *</div>';
        pageContent += '<div style="margin:' + Math.round(bodyPx * 0.9) + 'px 0;">'
          + ornamentHtml + '</div>';
        linesUsed += 1.8;
        isFirstPara = false;
        continue;
      }

      if (!plainText) continue;

      // ── Heading block (H1/H2/H3 from Tiptap paragraph styles) ──
      if (block.type !== 'p') {
        const hSizes: Record<string, number> = {
          h1: Math.round(headPx * 1.1),
          h2: Math.round(bodyPx * 1.45),
          h3: Math.round(bodyPx * 1.15),
        };
        const hSize  = hSizes[block.type] || headPx;
        const hCost  = Math.ceil((hSize * 1.3) / (bodyPx * lineH)) + 1;

        if (linesUsed + hCost > linesPerPage) {
          addPage(pageContent, { isChapterStart: isChapterPage, chapterTitle: chTitle });
          isChapterPage = false; pageContent = ''; linesUsed = 0;
        }

        pageContent +=
          '<div style="font-family:' + template.headingFont + ';'
          + 'font-size:' + hSize + 'px;'
          + 'font-weight:' + template.headingWeight + ';'
          + 'color:' + template.headingColor + ';'
          + 'text-transform:' + (block.type === 'h1' ? template.headingTransform : 'none') + ';'
          + 'text-align:' + template.headingAlign + ';'
          + 'margin:' + Math.round(hSize * 0.9) + 'px 0 ' + Math.round(hSize * 0.45) + 'px;'
          + 'line-height:1.2;">'
          + block.inner + '</div>';
        linesUsed  += hCost;
        isFirstPara = true; // reset indent after a heading
        continue;
      }

      // ── Body paragraph ──
      const textLen  = plainText.length;
      const paraCost = Math.max(1, Math.ceil(textLen / charsPerLine))
        + (template.paragraphStyle === 'spaced' ? 0.7 : 0);

      const remainingLines = linesPerPage - linesUsed;
      // Orphan: paragraph would contribute ≥2 lines total but only 1 line fits → push to next page
      const wouldOrphan = paraCost >= 2 && remainingLines < 2;
      if (linesUsed > headerLines && (linesUsed + paraCost > linesPerPage || wouldOrphan)) {
        addPage(pageContent, { isChapterStart: isChapterPage, chapterTitle: chTitle });
        isChapterPage = false; pageContent = ''; linesUsed = 0;
      }

      const indent = template.paragraphStyle === 'indent' && !isFirstPara
        ? Math.round(bodyPx * 1.8) + 'px' : '0';
      const mb = template.paragraphStyle === 'spaced'
        ? Math.round(bodyPx * 0.65) + 'px' : '0';

      // Drop cap on first paragraph of each chapter
      const dc = (options.dropCaps && isFirstPara && plainText.length > 0)
        ? extractDropCapLetter(block.inner) : null;
      if (dc) {
        pageContent +=
          '<p style="margin:0 0 ' + mb + ';padding:0;text-indent:0;overflow:hidden;'
          + 'font-family:' + template.bodyFont + ';font-size:' + bodyPx + 'px;'
          + 'line-height:' + lineH + ';color:' + template.inkColor + ';">'
          + '<span style="float:left;font-family:' + template.headingFont + ';'
          + 'font-size:' + dropCapPx + 'px;font-weight:700;'
          + 'line-height:0.82;padding-right:4px;margin-top:4px;color:' + template.accentColor + ';">'
          + escapeHtml(dc.letter) + '</span>'
          + dc.rest + '</p>';
      } else {
        pageContent +=
          '<p style="margin:0 0 ' + mb + ';padding:0;text-indent:' + indent + ';'
          + 'font-family:' + template.bodyFont + ';font-size:' + bodyPx + 'px;'
          + 'line-height:' + lineH + ';color:' + template.inkColor + ';">'
          + block.inner + '</p>';
      }
      linesUsed  += paraCost;
      isFirstPara = false;
    }

    // Last (or only) page of chapter
    if (pageContent) {
      addPage(pageContent, { isChapterStart: isChapterPage, chapterTitle: chTitle });
    }
  }

  // ---- Back matter ──────────────────────────────────────────────────
  const addBackSection = (heading: string, html: string) => {
    ensureRecto();
    const blocks = parseBlocks(html);
    const headerDropPx = Math.round(contentH * 0.18);
    let bContent =
      '<div style="padding-top:' + headerDropPx + 'px;'
      + 'text-align:' + template.headingAlign + ';margin-bottom:' + Math.round(headPx * 1.2) + 'px;">'
      + '<div style="font-family:' + template.headingFont + ';font-size:' + headPx + 'px;'
      + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
      + 'text-transform:' + template.headingTransform + ';line-height:1.2;">'
      + escapeHtml(heading) + '</div></div>';
    let bLines = Math.round(headerDropPx / (bodyPx * lineH)) + 3;
    let bmFirst = true;
    for (const block of blocks) {
      const plain = block.inner.replace(/<[^>]*>/g, '').trim();
      if (!plain) continue;
      const cost = Math.max(1, Math.ceil(plain.length / charsPerLine));
      if (bLines + cost > linesPerPage) {
        addPage(bContent, { chapterTitle: heading });
        bContent = ''; bLines = 0;
      }
      const mb = template.paragraphStyle === 'spaced' ? Math.round(bodyPx * 0.65) + 'px' : '0';
      const ind = template.paragraphStyle === 'indent' && !bmFirst ? Math.round(bodyPx * 1.8) + 'px' : '0';
      bContent +=
        '<p style="margin:0 0 ' + mb + ';padding:0;text-indent:' + ind + ';'
        + 'font-family:' + template.bodyFont + ';font-size:' + bodyPx + 'px;'
        + 'line-height:' + lineH + ';color:' + template.inkColor + ';">'
        + block.inner + '</p>';
      bLines += cost;
      bmFirst = false;
    }
    if (bContent) addPage(bContent, { chapterTitle: heading });
  };

  if (bookData.acknowledgments) addBackSection('Acknowledgments', bookData.acknowledgments);
  if (bookData.aboutAuthor)     addBackSection('About the Author', bookData.aboutAuthor);
  if (bookData.extras) {
    for (const [key, value] of Object.entries(bookData.extras)) {
      if (!value) continue;
      const label = key.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
      addBackSection(label, value);
    }
  }

  // ---- Build and insert TOC ──────────────────────────────────────────

  const buildTocPageHtml = (
    entries: Array<{ label: string; page: number }>,
    pgNum: number,
    showHeading: boolean
  ): string => {
    const even  = pgNum % 2 === 0;
    const cLeft = even ? mOuter : mInner;
    const entryHtml = entries.map(e => {
      const dots = '&thinsp;.&thinsp;'.repeat(40); // will be clipped
      return '<div style="display:flex;align-items:baseline;'
        + 'font-family:' + template.bodyFont + ';font-size:' + tocEntryPx + 'px;'
        + 'color:' + template.inkColor + ';margin-bottom:' + tocGap + 'px;line-height:1.2;">'
        + '<span style="white-space:nowrap;overflow:hidden;text-overflow:clip;'
        + 'max-width:' + Math.round(contentW * 0.78) + 'px;">' + escapeHtml(e.label) + '</span>'
        + '<span style="flex:1;overflow:hidden;color:' + template.inkColor + ';opacity:0.3;'
        + 'font-size:' + Math.round(tocEntryPx * 0.75) + 'px;padding:0 4px;'
        + 'letter-spacing:0.15em;white-space:nowrap;">' + dots + '</span>'
        + '<span style="white-space:nowrap;min-width:2.2em;text-align:right;">'
        + e.page + '</span></div>';
    }).join('');

    return '<div class="book-page" style="width:' + pxW + 'px;height:' + pxH + 'px;'
      + 'position:relative;overflow:hidden;background:' + (template.paperColor || '#fff') + ';">'
      + '<div style="position:absolute;top:' + (mTop + headH) + 'px;left:' + cLeft + 'px;'
      + 'width:' + contentW + 'px;">'
      + (showHeading
          ? '<div style="font-family:' + template.headingFont + ';font-size:' + tocTitlePx + 'px;'
            + 'font-weight:' + template.headingWeight + ';color:' + template.headingColor + ';'
            + 'text-transform:' + template.headingTransform + ';text-align:' + template.headingAlign + ';'
            + 'margin-bottom:' + Math.round(tocTitlePx * 1.2) + 'px;">Contents</div>'
          : '')
      + entryHtml
      + '</div></div>';
  };

  // Fill every reserved TOC slot — handles any number of chapters
  for (let _tp = 0; _tp < tocPageCount; _tp++) {
    const sliceStart = _tp * entriesPerPage;
    const sliceEnd   = sliceStart + entriesPerPage;
    const pageEntries = tocEntries.slice(sliceStart, sliceEnd);
    allPages[tocSlotIdx + _tp] = pageEntries.length > 0 || _tp === 0
      ? buildTocPageHtml(pageEntries, tocStartPgNum + _tp, _tp === 0)
      : '<div class="book-page" style="width:' + pxW + 'px;height:' + pxH + 'px;'
        + 'background:' + (template.paperColor || '#fff') + ';"></div>';
  }

  // ---- Assemble final HTML ------------------------------------------
  // All fonts used across all 8 templates
  const gfUrl = 'https://fonts.googleapis.com/css2?family=EB+Garamond:ital,wght@0,400;0,600;1,400'
    + '&family=Cormorant+Garamond:ital,wght@0,400;0,600;1,400'   // Knopf
    + '&family=Playfair+Display:ital,wght@0,400;0,700;1,400'     // Scribner, Ballantine
    + '&family=Lora:ital,wght@0,400;0,600;1,400'                 // Scribner, Ballantine
    + '&family=Cinzel:wght@400;600'                              // Tor
    + '&family=Libre+Baskerville:ital,wght@0,400;0,700;1,400'   // Vintage, Penguin, Anchor
    + '&family=Josefin+Sans:wght@300;400;600'                   // Vintage
    + '&family=Raleway:wght@200;300;400;600'                    // Penguin Modern
    + '&family=Merriweather:ital,wght@0,300;0,400;1,300'
    + '&family=Inter:wght@400;600;700&display=swap';

  return '<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n'
    + '<title>' + escapeHtml(bookData.title) + '</title>\n'
    + '<style>\n'
    + '  @import url("' + gfUrl + '");\n'
    + '  * { box-sizing: border-box; margin: 0; padding: 0; }\n'
    + '  @page { size: ' + pageSize + '; margin: 0; }\n'
    + '  html, body { margin: 0; padding: 0; background: #fff; }\n'
    + '  .book-page { display: block; }\n'
    + '</style>\n</head>\n<body>\n'
    + allPages.join('\n')
    + '\n</body>\n</html>';
}
function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ─────────────────────────────────────────
//  PDF BLOB GENERATOR (jsPDF + html2canvas)
// ─────────────────────────────────────────

async function generatePdfBlob(
  bookData: BookData,
  template: ReturnType<typeof getTemplate>,
  pages: BookPage[],
  trimSize: string,
  options: { dropCaps: boolean; hideChapterNumbers: boolean } = { dropCaps: true, hideChapterNumbers: false }
): Promise<Blob> {
  const { default: jsPDF } = await import('jspdf');
  const { default: html2canvas } = await import('html2canvas');

  // Trim size → inches → points (1in = 72pt)
  const TRIM_PX: Record<string, [number, number]> = {
    '5x8':     [360, 576],
    '5.5x8.5': [396, 612],
    '6x9':     [432, 648],
    'digest':  [364.3, 562.3],
    'mass':    [306, 494.6],
    'a5':      [419.5, 595.3],
    '7x10':    [504, 720],
    '8.5x11':  [612, 792],
  };
  const [ptW, ptH] = TRIM_PX[trimSize] ?? [432, 648];

  // Render the book HTML into a hidden off-screen container
  const printHtml = buildPrintHTML(bookData, template, pages, trimSize, options);

  const container = document.createElement('div');
  container.style.cssText = `position:fixed;left:-9999px;top:0;width:${ptW * (96/72)}px;background:#fff;`;
  container.innerHTML = printHtml;
  document.body.appendChild(container);

  // Wait for fonts / images
  await new Promise(r => setTimeout(r, 800));

  const pdf = new jsPDF({ unit: 'pt', format: [ptW, ptH], orientation: 'portrait' });

  // Capture each "page" div separately
  const pageDivs = container.querySelectorAll<HTMLElement>('.book-page, .front-page');
  const elements = pageDivs.length > 0 ? Array.from(pageDivs) : [container];

  for (let i = 0; i < elements.length; i++) {
    if (i > 0) pdf.addPage([ptW, ptH]);
    const canvas = await html2canvas(elements[i] as HTMLElement, {
      scale: 3,
      useCORS: true,
      backgroundColor: template.paperColor || '#ffffff',
      logging: false,
      imageTimeout: 0,
      removeContainer: true,
    });
    const imgData = canvas.toDataURL('image/png');
    pdf.addImage(imgData, 'PNG', 0, 0, ptW, ptH);
  }

  document.body.removeChild(container);
  return pdf.output('blob');
}

// ─────────────────────────────────────────
//  README BUILDER
// ─────────────────────────────────────────

function buildReadme(
  bookData: BookData,
  formats: { epub: boolean; pdf: boolean; word: boolean }
): string {
  const slug = bookData.title.replace(/\s+/g, '-').toLowerCase().replace(/[^a-z0-9-]/g, '');
  const lines = [
    `${bookData.title}`,
    `by ${bookData.author || 'Unknown Author'}`,
    `Formatted with Booksane — booksane.com`,
    ``,
    `─────────────────────────────────────────`,
    `FILES IN THIS ZIP`,
    `─────────────────────────────────────────`,
  ];
  if (formats.epub) lines.push(
    ``,
    `📚 ${slug}.epub`,
    `   E-book file. Upload to Amazon KDP, Apple Books, Kobo, Barnes & Noble`,
    `   Press, Draft2Digital, Smashwords, or any EPUB-compatible platform.`,
  );
  if (formats.pdf) lines.push(
    ``,
    `🖨  ${slug}.pdf`,
    `   Print-ready PDF. Upload directly to Amazon KDP Print, IngramSpark,`,
    `   or any print-on-demand service. Check trim size matches your order.`,
  );
  if (formats.word) lines.push(
    ``,
    `📝 ${slug}.docx`,
    `   Editable Word document. Open in Microsoft Word or Google Docs for`,
    `   further editing or submission to agents and publishers.`,
  );
  lines.push(
    ``,
    `─────────────────────────────────────────`,
    `PUBLISHING CHECKLIST`,
    `─────────────────────────────────────────`,
    ``,
    `  Amazon KDP     → Upload .epub (eBook) or .pdf (Print)`,
    `  IngramSpark    → Upload .epub + .pdf`,
    `  Apple Books    → Upload .epub`,
    `  Kobo           → Upload .epub`,
    `  Draft2Digital  → Upload .epub`,
    ``,
    `Good luck with your book!`,
    `— The Booksane Team`,
  );
  return lines.join('\n');
}
