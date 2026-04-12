'use client';

import { useState, useMemo } from 'react';
import { motion } from 'framer-motion';
import { Minus, Plus, Moon, Sun } from 'lucide-react';
import { getTemplate } from '@/lib/templates';
import type { BookData } from '@/types';

interface EbookPreviewProps {
  bookData: BookData;
  templateId: string;
  fontSize: number;
  onFontSizeChange: (size: number) => void;
}

export default function EbookPreview({
  bookData,
  templateId,
  fontSize,
  onFontSizeChange,
}: EbookPreviewProps) {
  const [darkReader, setDarkReader] = useState(false);
  const [currentChapter, setCurrentChapter] = useState(0);
  const template = getTemplate(templateId);

  const bg = darkReader ? '#1A1208' : template.paperColor;
  const color = darkReader ? '#D4C9B8' : template.inkColor;
  const headingColor = darkReader ? '#D4A853' : template.headingColor;
  const accentColor = darkReader ? '#D4A853' : template.accentColor;

  const progress = useMemo(() => {
    const total = bookData.chapters.length;
    return total > 1 ? ((currentChapter + 1) / total) * 100 : 100;
  }, [currentChapter, bookData.chapters.length]);

  const chapter = bookData.chapters[currentChapter];

  return (
    <div
      className="flex flex-col items-center gap-4 w-full max-w-2xl"
    >
      {/* Controls */}
      <div
        className="flex items-center justify-between w-full px-4 py-2 rounded-xl"
        style={{
          background: 'rgba(255,255,255,0.04)',
          border: '1px solid rgba(255,255,255,0.07)',
        }}
      >
        {/* Chapter nav */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setCurrentChapter((c) => Math.max(0, c - 1))}
            disabled={currentChapter === 0}
            className="text-xs px-2 py-1 rounded-md transition-colors"
            style={{
              color: currentChapter === 0 ? 'var(--text-muted)' : 'var(--text-secondary)',
              background: 'rgba(255,255,255,0.05)',
              opacity: currentChapter === 0 ? 0.4 : 1,
            }}
          >
            ← Prev
          </button>
          <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
            Chapter {currentChapter + 1} / {bookData.chapters.length}
          </span>
          <button
            onClick={() => setCurrentChapter((c) => Math.min(bookData.chapters.length - 1, c + 1))}
            disabled={currentChapter === bookData.chapters.length - 1}
            className="text-xs px-2 py-1 rounded-md transition-colors"
            style={{
              color: currentChapter === bookData.chapters.length - 1 ? 'var(--text-muted)' : 'var(--text-secondary)',
              background: 'rgba(255,255,255,0.05)',
              opacity: currentChapter === bookData.chapters.length - 1 ? 0.4 : 1,
            }}
          >
            Next →
          </button>
        </div>

        {/* Right controls */}
        <div className="flex items-center gap-3">
          {/* Font size */}
          <div className="flex items-center gap-1">
            <button
              onClick={() => onFontSizeChange(Math.max(12, fontSize - 1))}
              className="w-6 h-6 rounded-md flex items-center justify-center text-xs transition-colors"
              style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}
            >
              <Minus size={10} />
            </button>
            <span className="text-xs w-6 text-center" style={{ color: 'var(--text-muted)' }}>
              {fontSize}
            </span>
            <button
              onClick={() => onFontSizeChange(Math.min(24, fontSize + 1))}
              className="w-6 h-6 rounded-md flex items-center justify-center transition-colors"
              style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}
            >
              <Plus size={10} />
            </button>
          </div>

          {/* Dark/light */}
          <button
            onClick={() => setDarkReader(!darkReader)}
            className="w-6 h-6 rounded-md flex items-center justify-center transition-colors"
            style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}
          >
            {darkReader ? <Sun size={11} /> : <Moon size={11} />}
          </button>
        </div>
      </div>

      {/* Progress bar */}
      <div className="w-full h-0.5 rounded-full overflow-hidden" style={{ background: 'rgba(255,255,255,0.07)' }}>
        <motion.div
          className="h-full rounded-full"
          style={{ background: 'var(--accent)' }}
          animate={{ width: `${progress}%` }}
          transition={{ duration: 0.3 }}
        />
      </div>

      {/* eBook body */}
      <motion.div
        key={`${currentChapter}-${templateId}`}
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="w-full rounded-2xl overflow-hidden"
        style={{
          background: bg,
          boxShadow: `0 20px 60px rgba(0,0,0,0.5), 0 0 0 1px rgba(0,0,0,0.1)`,
          maxWidth: '600px',
          minHeight: '500px',
        }}
      >
        {/* Kindle-style top bar */}
        <div
          className="flex items-center justify-between px-6 py-3 border-b"
          style={{
            borderColor: `${color}10`,
            background: `${bg}`,
          }}
        >
          <span
            className="text-xs"
            style={{
              fontFamily: template.bodyFont,
              color: `${color}60`,
              fontSize: '10px',
              letterSpacing: '0.05em',
            }}
          >
            {bookData.author.toUpperCase()}
          </span>
          <span
            className="text-xs"
            style={{
              fontFamily: template.bodyFont,
              color: `${color}60`,
              fontSize: '10px',
            }}
          >
            {bookData.title}
          </span>
        </div>

        {/* Content */}
        <div
          className="px-12 py-10 overflow-y-auto"
          style={{
            fontFamily: template.bodyFont,
            fontSize: `${fontSize}px`,
            lineHeight: template.lineHeight,
            color,
            maxHeight: '520px',
          }}
        >
          {/* Chapter header */}
          <div
            className="mb-8"
            style={{
              paddingTop: '2em',
              textAlign: template.headingAlign,
            }}
          >
            {template.chapterNumberStyle !== 'none' && (
              <div
                className="mb-2"
                style={{
                  fontFamily: template.headingFont,
                  fontSize: `${fontSize * 0.75}px`,
                  color: accentColor,
                  fontWeight: template.headingWeight,
                  textTransform: template.headingTransform,
                  letterSpacing: template.headingTransform === 'uppercase' ? '0.15em' : '0',
                }}
              >
                {template.chapterNumberStyle === 'roman' ? `Chapter ${toRoman(chapter.number)}` :
                 template.chapterNumberStyle === 'spelled' ? `Chapter ${numberToWord(chapter.number)}` :
                 `Chapter ${chapter.number}`}
              </div>
            )}
            <h2
              style={{
                fontFamily: template.headingFont,
                fontSize: `${fontSize * 1.4}px`,
                fontWeight: template.headingWeight,
                color: headingColor,
                textTransform: template.headingTransform,
                lineHeight: 1.2,
                marginBottom: '0.5em',
              }}
            >
              {!/^chapter\s+\d+$/i.test(chapter.title) ? chapter.title : ''}
            </h2>
          </div>

          {/* Chapter body */}
          <div
            className="ebook-body"
            style={{
              fontFamily: template.bodyFont,
              fontSize: `${fontSize}px`,
              lineHeight: template.lineHeight,
            }}
            dangerouslySetInnerHTML={{
              __html: injectEbookStyles(chapter.content, template, fontSize, color, darkReader),
            }}
          />
        </div>

        {/* Bottom progress */}
        <div
          className="flex items-center justify-between px-8 py-3 border-t"
          style={{ borderColor: `${color}10` }}
        >
          <span className="text-xs" style={{ fontFamily: template.bodyFont, color: `${color}50`, fontSize: '10px' }}>
            {chapter.wordCount.toLocaleString()} words
          </span>
          <span className="text-xs" style={{ fontFamily: template.bodyFont, color: `${color}50`, fontSize: '10px' }}>
            {Math.round(chapter.wordCount / 250)} min read
          </span>
        </div>
      </motion.div>

      {/* Chapter switcher dots */}
      {bookData.chapters.length > 1 && (
        <div className="flex items-center gap-2">
          {bookData.chapters.map((_, i) => (
            <button
              key={i}
              onClick={() => setCurrentChapter(i)}
              className="rounded-full transition-all duration-200"
              style={{
                width: i === currentChapter ? '20px' : '6px',
                height: '6px',
                background: i === currentChapter ? 'var(--accent-light)' : 'rgba(255,255,255,0.15)',
              }}
            />
          ))}
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────

function injectEbookStyles(
  html: string,
  template: { paragraphStyle: string; textIndent: string; paragraphSpacing: string; accentColor: string },
  fontSize: number,
  color: string,
  dark: boolean
): string {
  const indent = template.paragraphStyle === 'indent' ? template.textIndent : '0';
  const spacing = template.paragraphStyle === 'spaced' ? template.paragraphSpacing : '0';

  return html
    .replace(/<p>/g, `<p style="margin:0 0 ${spacing};text-indent:${indent};color:${color};font-size:${fontSize}px;">`)
    .replace(/<p class="section-break">/g, `<p class="section-break" style="text-align:center;color:${template.accentColor};margin:1.5em 0;opacity:0.5;">`);
}

function toRoman(num: number): string {
  const vals = [1000,900,500,400,100,90,50,40,10,9,5,4,1];
  const syms = ['M','CM','D','CD','C','XC','L','XL','X','IX','V','IV','I'];
  let r = '', n = num;
  vals.forEach((v, i) => { while (n >= v) { r += syms[i]; n -= v; } });
  return r;
}

function numberToWord(n: number): string {
  const words: Record<number, string> = {
    1:'One',2:'Two',3:'Three',4:'Four',5:'Five',6:'Six',7:'Seven',8:'Eight',9:'Nine',10:'Ten',
    11:'Eleven',12:'Twelve',13:'Thirteen',14:'Fourteen',15:'Fifteen',16:'Sixteen',
    17:'Seventeen',18:'Eighteen',19:'Nineteen',20:'Twenty',
  };
  return words[n] ?? String(n);
}
