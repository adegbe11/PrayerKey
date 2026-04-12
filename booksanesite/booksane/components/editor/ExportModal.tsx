'use client';

import { useState } from 'react';
import { motion } from 'framer-motion';
import { X, FileDown, Book, CheckCircle2, Loader2, ExternalLink } from 'lucide-react';
import { getTemplate } from '@/lib/templates';
import type { BookData, BookPage } from '@/types';

interface ExportModalProps {
  bookData: BookData;
  templateId: string;
  pages: BookPage[];
  onClose: () => void;
}

type ExportState = 'idle' | 'generating' | 'done' | 'error';

export default function ExportModal({ bookData, templateId, pages, onClose }: ExportModalProps) {
  const [pdfState, setPdfState] = useState<ExportState>('idle');
  const [epubState, setEpubState] = useState<ExportState>('idle');
  const template = getTemplate(templateId);

  const handleExportPdf = async () => {
    setPdfState('generating');
    try {
      // Build the print HTML
      const printHtml = buildPrintHTML(bookData, template, pages);

      // Open in new window + trigger print
      const printWindow = window.open('', '_blank', 'width=800,height=600');
      if (!printWindow) throw new Error('Popup blocked');

      printWindow.document.write(printHtml);
      printWindow.document.close();

      setTimeout(() => {
        printWindow.focus();
        printWindow.print();
        setPdfState('done');
      }, 600);
    } catch (err) {
      console.error(err);
      setPdfState('error');
    }
  };

  const handleExportEpub = async () => {
    setEpubState('generating');
    try {
      const { generateEpub } = await import('@/lib/epub');
      const blob = await generateEpub(bookData, template);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${bookData.title.replace(/\s+/g, '-').toLowerCase()}.epub`;
      a.click();
      URL.revokeObjectURL(url);
      setEpubState('done');
    } catch (err) {
      console.error(err);
      setEpubState('error');
    }
  };

  return (
    <>
      {/* Backdrop */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50"
        style={{ background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(8px)' }}
        onClick={onClose}
      />

      {/* Modal */}
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 20 }}
        transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
        className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none"
      >
        <div
          className="pointer-events-auto w-full max-w-lg rounded-2xl overflow-hidden"
          style={{
            background: '#16142A',
            border: '1px solid rgba(255,255,255,0.1)',
            boxShadow: '0 40px 100px rgba(0,0,0,0.6)',
          }}
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div
            className="flex items-center justify-between px-6 py-5 border-b"
            style={{ borderColor: 'rgba(255,255,255,0.07)' }}
          >
            <div>
              <h2 className="text-base font-semibold" style={{ color: 'var(--text-primary)' }}>
                Export your book
              </h2>
              <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
                {bookData.title} · {bookData.metadata.wordCount.toLocaleString()} words
              </p>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-lg flex items-center justify-center transition-colors"
              style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}
            >
              <X size={15} />
            </button>
          </div>

          {/* Body */}
          <div className="p-6 flex flex-col gap-4">
            {/* Template info */}
            <div
              className="flex items-center gap-3 p-3 rounded-xl"
              style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.06)' }}
            >
              <div
                className="w-8 h-10 rounded shrink-0"
                style={{ background: template.paperColor, border: '1px solid rgba(0,0,0,0.1)' }}
              />
              <div>
                <div className="text-xs font-medium" style={{ color: 'var(--text-primary)' }}>
                  {template.name} template
                </div>
                <div className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
                  {template.pageSize.toUpperCase()} · {bookData.chapters.length} chapters · ~{bookData.metadata.estimatedPages} pages
                </div>
              </div>
            </div>

            {/* Export options */}
            <div className="grid grid-cols-2 gap-3">
              {/* PDF */}
              <ExportButton
                icon={<FileDown size={18} />}
                title="PDF"
                description="Print-ready. Open print dialog to save as PDF."
                note="Includes watermark on free plan"
                state={pdfState}
                accentColor="rgba(124,58,237,0.7)"
                onClick={handleExportPdf}
              />

              {/* EPUB */}
              <ExportButton
                icon={<Book size={18} />}
                title="EPUB"
                description="For Kindle, Apple Books, and all e-readers."
                note="Clean EPUB 3.0 format"
                state={epubState}
                accentColor="rgba(212,168,83,0.7)"
                onClick={handleExportEpub}
              />
            </div>

            {/* KDP checklist */}
            <div
              className="p-4 rounded-xl"
              style={{
                background: 'rgba(34,197,94,0.05)',
                border: '1px solid rgba(34,197,94,0.15)',
              }}
            >
              <p className="text-xs font-medium mb-3" style={{ color: '#4ADE80' }}>
                KDP Readiness Check
              </p>
              {[
                { label: 'Chapters detected', ok: bookData.chapters.length > 0 },
                { label: 'Title set', ok: !!bookData.title && bookData.title !== 'Untitled' },
                { label: 'Author set', ok: !!bookData.author },
                { label: 'Word count sufficient', ok: bookData.metadata.wordCount >= 2000 },
              ].map((item) => (
                <div key={item.label} className="flex items-center gap-2 mb-1.5">
                  <CheckCircle2
                    size={12}
                    color={item.ok ? '#4ADE80' : '#6B7280'}
                    fill={item.ok ? 'rgba(74,222,128,0.15)' : 'transparent'}
                  />
                  <span
                    className="text-xs"
                    style={{ color: item.ok ? 'var(--text-secondary)' : 'var(--text-muted)' }}
                  >
                    {item.label}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </motion.div>
    </>
  );
}

// ─────────────────────────────────────────
//  EXPORT BUTTON
// ─────────────────────────────────────────

function ExportButton({
  icon,
  title,
  description,
  note,
  state,
  accentColor,
  onClick,
}: {
  icon: React.ReactNode;
  title: string;
  description: string;
  note: string;
  state: ExportState;
  accentColor: string;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      disabled={state === 'generating'}
      className="flex flex-col gap-3 p-4 rounded-xl text-left transition-all duration-200"
      style={{
        background: 'rgba(255,255,255,0.03)',
        border: `1px solid ${state === 'done' ? 'rgba(34,197,94,0.3)' : 'rgba(255,255,255,0.08)'}`,
        cursor: state === 'generating' ? 'wait' : 'pointer',
        opacity: state === 'generating' ? 0.7 : 1,
      }}
    >
      <div
        className="w-10 h-10 rounded-xl flex items-center justify-center"
        style={{ background: `${accentColor}30`, color: 'var(--text-primary)' }}
      >
        {state === 'generating' ? (
          <Loader2 size={18} className="animate-spin" />
        ) : state === 'done' ? (
          <CheckCircle2 size={18} color="#4ADE80" />
        ) : (
          icon
        )}
      </div>

      <div>
        <div className="text-sm font-semibold" style={{ color: 'var(--text-primary)' }}>
          {title}
        </div>
        <div className="text-xs mt-0.5" style={{ color: 'var(--text-secondary)' }}>
          {state === 'done' ? 'Done! Check your downloads.' : description}
        </div>
        <div className="text-xs mt-1.5" style={{ color: 'var(--text-muted)', fontSize: '10px' }}>
          {note}
        </div>
      </div>
    </button>
  );
}

// ─────────────────────────────────────────
//  PDF HTML BUILDER
// ─────────────────────────────────────────

function buildPrintHTML(
  bookData: BookData,
  template: ReturnType<typeof getTemplate>,
  pages: BookPage[]
): string {
  const pageContents = pages.map((p) => p.content).join('\n<div class="page-break"></div>\n');

  return `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${bookData.title}</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=EB+Garamond:ital,wght@0,400;0,600;1,400&family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=Inter:wght@400;600;700&display=swap');

  * { box-sizing: border-box; margin: 0; padding: 0; }

  @page {
    size: 6in 9in;
    margin: ${template.pageMarginV} ${template.pageMarginH};
  }

  body {
    font-family: ${template.bodyFont};
    font-size: ${template.bodySize};
    line-height: ${template.lineHeight};
    color: ${template.inkColor};
    background: ${template.paperColor};
  }

  .book-page {
    background: ${template.paperColor};
    min-height: 100vh;
    padding: ${template.pageMarginV} ${template.pageMarginH};
    position: relative;
  }

  .page-break { page-break-before: always; }

  p {
    margin: 0;
    text-indent: ${template.paragraphStyle === 'indent' ? template.textIndent : '0'};
    margin-bottom: ${template.paragraphStyle === 'spaced' ? template.paragraphSpacing : '0'};
  }

  .chapter-start { padding-top: ${template.chapterStartMargin}; }

  .chapter-number {
    display: block;
    font-family: ${template.headingFont};
    font-weight: ${template.headingWeight};
    text-align: ${template.headingAlign};
    color: ${template.accentColor};
    font-size: ${template.chapterNumberSize};
    margin-bottom: 0.4em;
  }

  .chapter-heading {
    font-family: ${template.headingFont};
    font-size: ${template.headingSize};
    font-weight: ${template.headingWeight};
    text-align: ${template.headingAlign};
    text-transform: ${template.headingTransform};
    color: ${template.headingColor};
    margin-bottom: 1.5em;
  }

  .section-break {
    text-align: center;
    color: ${template.accentColor};
    margin: 1.5em 0;
    opacity: 0.5;
  }

  /* Watermark */
  .watermark {
    position: fixed;
    bottom: 0.5in;
    right: 0.5in;
    font-size: 8pt;
    color: rgba(0,0,0,0.2);
    font-family: sans-serif;
  }
</style>
</head>
<body>
${pageContents}
<div class="watermark">Formatted with Booksane.com</div>
</body>
</html>`;
}
