import type { BookData, BookPage, Template, CoverConfig } from '@/types';
import { getChapterNumberDisplay } from './formatter';
import { renderCover } from './covers';

// ─────────────────────────────────────────
//  BOOK PAGINATOR
//  Splits BookData into pages for preview
// ─────────────────────────────────────────

// Approximate characters per page by page size + font size
const CHARS_PER_PAGE: Record<string, number> = {
  trade: 1900,
  mass: 1600,
  digest: 1700,
  a5: 1800,
};

// Preview pages are 260×380px — use compact margins (not real book inches)
const PREVIEW_PAD_H = '18px';
const PREVIEW_PAD_V = '16px';
const PREVIEW_FONT_SIZE = '7.5pt';
const PREVIEW_LINE_HEIGHT = '1.55';

export function paginateBook(
  bookData: BookData,
  template: Template,
  coverConfig?: CoverConfig
): BookPage[] {
  const pages: BookPage[] = [];
  const cpp = CHARS_PER_PAGE[template.pageSize] ?? 1900;

  // Page 1: Front cover
  pages.push({
    pageNumber: 1,
    isCover: true,
    content: buildFrontCover(bookData, template, coverConfig),
  });

  // Page 2: Half-title page
  pages.push({
    pageNumber: 2,
    content: buildHalfTitlePage(bookData, template),
  });

  // Page 3: Full title page
  pages.push({
    pageNumber: 3,
    content: buildFullTitlePage(bookData, template),
  });

  // Page 4: Copyright page
  pages.push({
    pageNumber: 4,
    content: buildCopyrightPage(bookData, template),
  });

  // Dedication (if exists)
  if (bookData.dedication) {
    pages.push({
      pageNumber: pages.length + 1,
      isDedication: true,
      content: buildDedicationPage(bookData.dedication, template),
    });
  }

  // Epigraph (if exists)
  if (bookData.epigraph) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildEpigraphPage(bookData.epigraph, bookData.epigraphAttribution, template),
    });
  }

  // Table of Contents (if 2+ chapters)
  if (bookData.chapters.length >= 2) {
    pages.push({
      pageNumber: pages.length + 1,
      isToc: true,
      content: buildTocPage(bookData, template),
    });
  }

  // Blank page before first chapter (traditional typesetting)
  pages.push({
    pageNumber: pages.length + 1,
    isBlank: true,
    content: '<div class="blank-page"></div>',
  });

  // Chapter pages
  for (const chapter of bookData.chapters) {
    const chapterPages = paginateChapter(chapter, template, cpp, pages.length + 1);
    pages.push(...chapterPages);
  }

  // Acknowledgments page (if exists)
  if (bookData.acknowledgments) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildAcknowledgmentsPage(bookData.acknowledgments, template),
    });
  }

  // About Author page (if exists)
  if (bookData.aboutAuthor) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildAboutAuthorPage(bookData.aboutAuthor, template),
    });
  }

  // Back cover (always last)
  pages.push({
    pageNumber: pages.length + 1,
    isCover: true,
    content: buildBackCover(bookData, template),
  });

  return pages;
}

// ─────────────────────────────────────────
//  PAGE BUILDERS
// ─────────────────────────────────────────

function buildFrontCover(
  bookData: BookData,
  template: Template,
  coverConfig?: CoverConfig
): string {
  // If uploaded image
  if (coverConfig?.type === 'uploaded' && coverConfig.uploadedUrl) {
    return `
      <div class="cover-page book-page" style="width:100%;height:100%;overflow:hidden;position:relative;">
        <img src="${coverConfig.uploadedUrl}" style="width:100%;height:100%;object-fit:cover;display:block;" />
      </div>
    `;
  }

  // Template-based cover
  const templateId = coverConfig?.templateId ?? 'clean';
  return `
    <div class="cover-page book-page" style="width:100%;height:100%;overflow:hidden;">
      ${renderCover(templateId, bookData.title, bookData.author, bookData.subtitle)}
    </div>
  `;
}

export function buildHalfTitlePage(bookData: BookData, template: Template): string {
  return `
    <div class="book-page" style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};color:${template.inkColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="
        font-family:${template.headingFont};font-size:10pt;font-weight:${template.headingWeight};
        color:${template.headingColor};text-align:center;line-height:1.2;
        text-transform:${template.headingTransform};
      ">
        ${bookData.title}
      </div>
    </div>
  `;
}

export function buildFullTitlePage(bookData: BookData, template: Template): string {
  return `
    <div class="book-page" style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};color:${template.inkColor};
      box-sizing:border-box;overflow:hidden;text-align:center;
    ">
      <div style="margin-bottom:0.5em;font-family:${template.headingFont};font-size:11pt;font-weight:${template.headingWeight};color:${template.headingColor};text-transform:${template.headingTransform};line-height:1.2;">
        ${bookData.title}
      </div>
      ${bookData.subtitle ? `<div style="font-family:${template.bodyFont};font-size:8pt;color:${template.inkColor};opacity:0.7;margin-bottom:1em;">${bookData.subtitle}</div>` : ''}
      <!-- Rule -->
      <div style="width:36px;height:1px;background:${template.accentColor};opacity:0.5;margin:1em auto;"></div>
      <!-- Author -->
      <div style="font-family:${template.bodyFont};font-size:8pt;color:${template.inkColor};opacity:0.75;font-style:italic;">
        ${bookData.author ? bookData.author : ''}
      </div>
      <!-- Publisher at bottom -->
      <div style="position:absolute;bottom:14px;left:0;right:0;text-align:center;font-family:${template.bodyFont};font-size:7pt;color:${template.inkColor};opacity:0.3;letter-spacing:0.1em;text-transform:uppercase;">
        Booksane
      </div>
    </div>
  `;
}

export function buildCopyrightPage(bookData: BookData, template: Template): string {
  const year = new Date().getFullYear();
  return `
    <div class="book-page" style="
      display:flex;flex-direction:column;align-items:flex-start;justify-content:flex-end;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};color:${template.inkColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="font-family:${template.bodyFont};font-size:6.5pt;color:${template.inkColor};opacity:0.55;line-height:1.8;">
        <p style="margin:0 0 0.5em;">Copyright &copy; ${year}${bookData.author ? ' by ' + bookData.author : ''}</p>
        <p style="margin:0 0 0.5em;">All rights reserved. No part of this publication may be reproduced, distributed, or transmitted in any form or by any means without the prior written permission of the publisher.</p>
        <p style="margin:0 0 0.5em;">ISBN: 978-0-000000-00-0 (Paperback)<br/>ISBN: 978-0-000000-01-7 (eBook)</p>
        <p style="margin:0;">Formatted by Booksane &mdash; booksane.com</p>
      </div>
    </div>
  `;
}

function buildDedicationPage(dedication: string, template: Template): string {
  return `
    <div class="dedication-page book-page" style="
      display:flex;flex-direction:column;justify-content:center;align-items:center;
      text-align:center;height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
    ">
      <div style="font-family:${template.bodyFont};font-size:7.5pt;font-style:italic;color:${template.inkColor};opacity:0.75;max-width:80%;line-height:1.8;">
        ${dedication.split('\n').map((l) => `<p style="margin:0 0 0.5em">${l}</p>`).join('')}
      </div>
    </div>
  `;
}

export function buildEpigraphPage(
  epigraph: string | undefined,
  attribution: string | undefined,
  template: Template
): string {
  return `
    <div class="book-page" style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="max-width:80%;text-align:center;">
        <div style="font-family:${template.bodyFont};font-size:7.5pt;font-style:italic;color:${template.inkColor};opacity:0.75;line-height:1.8;margin-bottom:0.8em;">
          &ldquo;${epigraph ?? ''}&rdquo;
        </div>
        ${attribution ? `<div style="font-family:${template.bodyFont};font-size:7pt;color:${template.inkColor};opacity:0.5;text-align:right;">&mdash; ${attribution}</div>` : ''}
      </div>
    </div>
  `;
}

export function buildAboutAuthorPage(text: string, template: Template): string {
  const paragraphs = text.split(/\n{2,}/).map(p => p.trim()).filter(Boolean);
  return `
    <div class="book-page" style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="font-family:${template.headingFont};font-size:8.5pt;font-weight:${template.headingWeight};color:${template.headingColor};text-transform:${template.headingTransform};text-align:${template.headingAlign};margin-bottom:0.8em;">
        About the Author
      </div>
      <div style="font-family:${template.bodyFont};font-size:${PREVIEW_FONT_SIZE};line-height:${PREVIEW_LINE_HEIGHT};color:${template.inkColor};opacity:0.85;">
        ${paragraphs.map(p => `<p style="margin:0 0 0.6em;">${p}</p>`).join('')}
      </div>
    </div>
  `;
}

export function buildAcknowledgmentsPage(text: string, template: Template): string {
  const paragraphs = text.split(/\n{2,}/).map(p => p.trim()).filter(Boolean);
  return `
    <div class="book-page" style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="font-family:${template.headingFont};font-size:8.5pt;font-weight:${template.headingWeight};color:${template.headingColor};text-transform:${template.headingTransform};text-align:${template.headingAlign};margin-bottom:0.8em;">
        Acknowledgments
      </div>
      <div style="font-family:${template.bodyFont};font-size:${PREVIEW_FONT_SIZE};line-height:${PREVIEW_LINE_HEIGHT};color:${template.inkColor};opacity:0.85;">
        ${paragraphs.map(p => `<p style="margin:0 0 0.6em;">${p}</p>`).join('')}
      </div>
    </div>
  `;
}

export function buildBackCover(bookData: BookData, template: Template): string {
  // Extract first 2-3 sentences from first chapter as blurb
  let blurb = '';
  if (bookData.chapters.length > 0) {
    const firstChapterContent = bookData.chapters[0].content;
    // Strip HTML tags and get plain text
    const plainText = firstChapterContent.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
    // Get first 2-3 sentences
    const sentenceMatch = plainText.match(/^([^.!?]*[.!?][^.!?]*[.!?][^.!?]*[.!?])/);
    blurb = sentenceMatch ? sentenceMatch[1].trim() : plainText.slice(0, 200);
    if (blurb.length > 240) blurb = blurb.slice(0, 237) + '…';
  }

  return `
    <div class="book-page" style="
      width:100%;height:100%;
      background:#0A0910;
      display:flex;flex-direction:column;align-items:center;
      padding:22px 18px 16px;box-sizing:border-box;overflow:hidden;
    ">
      <!-- Book title at top -->
      <div style="font-family:'Inter',system-ui,Georgia,serif;font-size:8pt;font-weight:700;color:#FFE500;text-align:center;letter-spacing:0.05em;margin-bottom:14px;text-transform:uppercase;">
        ${escapeHtml(bookData.title)}
      </div>
      <!-- About this book label -->
      <div style="font-family:'Inter',system-ui,sans-serif;font-size:7pt;font-weight:700;color:#FFFFFF;text-align:center;letter-spacing:0.1em;text-transform:uppercase;margin-bottom:8px;opacity:0.8;">
        About this book
      </div>
      <!-- Thin divider -->
      <div style="width:30px;height:1px;background:rgba(255,255,255,0.2);margin-bottom:10px;"></div>
      <!-- Blurb -->
      ${blurb ? `<div style="font-family:Georgia,'Times New Roman',serif;font-size:6.5pt;font-style:italic;color:rgba(200,200,200,0.75);line-height:1.65;text-align:center;max-width:200px;margin-bottom:auto;">${escapeHtml(blurb)}</div>` : '<div style="flex:1;"></div>'}
      <!-- Author name -->
      <div style="font-family:'Inter',system-ui,sans-serif;font-size:7pt;color:rgba(255,255,255,0.5);text-align:center;margin-top:16px;text-transform:uppercase;letter-spacing:0.1em;">
        ${escapeHtml(bookData.author || '')}
      </div>
      <!-- ISBN barcode placeholder -->
      <div style="margin-top:12px;display:flex;flex-direction:column;align-items:center;gap:3px;">
        <div style="width:80px;height:20px;overflow:hidden;display:flex;gap:0.5px;">
          ${generateBarcodeBars()}
        </div>
        <div style="font-family:monospace;font-size:5.5pt;color:rgba(255,255,255,0.35);letter-spacing:0.05em;">978-0-000000-00-0</div>
      </div>
      <!-- Booksane branding -->
      <div style="margin-top:8px;font-family:'Inter',system-ui,sans-serif;font-size:6.5pt;color:rgba(255,255,255,0.25);letter-spacing:0.12em;text-transform:uppercase;">
        BOOKSANE
      </div>
    </div>
  `;
}

function generateBarcodeBars(): string {
  // Simple deterministic striped rectangle for ISBN barcode appearance
  const widths = [2,1,2,1,1,2,1,1,2,1,2,1,1,2,1,2,1,1,2,1,1,2,1,2,1,1,2,1,2,1];
  return widths.map((w, i) =>
    `<div style="width:${w}px;height:100%;background:${i % 2 === 0 ? 'rgba(255,255,255,0.8)' : 'transparent'};flex-shrink:0;"></div>`
  ).join('');
}

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function buildTocPage(bookData: BookData, template: Template): string {
  const items = bookData.chapters
    .map(
      (ch, i) => `
      <div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:0.6em;font-family:${template.bodyFont};font-size:11pt;color:${template.inkColor};">
        <span style="flex:1;">${ch.title}</span>
        <span style="opacity:0.4;margin-left:1em;font-size:10pt;">${i + 3}</span>
      </div>
    `
    )
    .join('');

  return `
    <div class="toc-page book-page" style="padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};background:${template.paperColor};height:100%;overflow:hidden;box-sizing:border-box;">
      <div style="font-family:${template.headingFont};font-size:9pt;font-weight:${template.headingWeight};text-align:${template.headingAlign};color:${template.headingColor};text-transform:${template.headingTransform};margin-bottom:1em;">
        Contents
      </div>
      <div style="font-size:7pt;">${items}</div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  CHAPTER PAGINATOR
// ─────────────────────────────────────────

function paginateChapter(
  chapter: { id: string; number: number; title: string; content: string; wordCount: number },
  template: Template,
  charsPerPage: number,
  startPageNumber: number
): BookPage[] {
  const pages: BookPage[] = [];

  // Build chapter header HTML (scaled for 260×380px preview)
  const numDisplay = template.chapterNumberStyle !== 'none'
    ? `<span style="display:block;font-family:${template.headingFont};font-size:8pt;font-weight:${template.headingWeight};text-align:${template.headingAlign};color:${template.accentColor};margin-bottom:0.3em;text-transform:${template.headingTransform};letter-spacing:0.05em;">
        ${template.chapterNumberStyle === 'numeral' ? `Chapter ${chapter.number}` :
          template.chapterNumberStyle === 'roman' ? `Chapter ${getChapterNumberDisplay(chapter.number, 'roman')}` :
          template.chapterNumberStyle === 'spelled' ? `Chapter ${getChapterNumberDisplay(chapter.number, 'spelled')}` : ''}
       </span>`
    : '';

  const isTitleMeaningful = !/^chapter\s+\d+$/i.test(chapter.title);
  const titleDisplay = isTitleMeaningful
    ? `<div style="font-family:${template.headingFont};font-size:9pt;font-weight:${template.headingWeight};text-align:${template.headingAlign};text-transform:${template.headingTransform};color:${template.headingColor};margin-bottom:0.8em;line-height:1.2;">${chapter.title}</div>`
    : '';

  // Scale chapter start margin: template says e.g. "25%" of full page, preview uses 12-20%
  const startMargin = '14%';
  const chapterHeader = `
    <div style="padding-top:${startMargin};">
      ${numDisplay}
      ${titleDisplay}
    </div>
  `;

  // Extract paragraphs from content HTML
  const paragraphs = extractParagraphs(chapter.content);

  // Build pages by accumulating paragraphs until we hit cpp limit
  let currentPageContent = '';
  let isFirstPage = true;
  let pageNum = startPageNumber;

  for (let i = 0; i < paragraphs.length; i++) {
    const para = paragraphs[i];
    const paraText = stripHtml(para);

    if (!currentPageContent) {
      // Start new page
      currentPageContent = isFirstPage
        ? chapterHeader + wrapInPageBody(template, para)
        : wrapInPageBody(template, para);
      isFirstPage = false;
    } else if (currentPageContent.replace(/<[^>]*>/g, '').length + paraText.length > charsPerPage) {
      // Save current page and start new one
      pages.push({
        pageNumber: pageNum++,
        content: wrapInPage(template, currentPageContent, pageNum - 1),
        chapterId: chapter.id,
        isChapterStart: pages.length === 0,
      });
      currentPageContent = wrapInPageBody(template, para);
    } else {
      currentPageContent += '\n' + para;
    }
  }

  // Save last page
  if (currentPageContent) {
    pages.push({
      pageNumber: pageNum,
      content: wrapInPage(template, currentPageContent, pageNum),
      chapterId: chapter.id,
      isChapterStart: pages.length === 0,
    });
  }

  return pages;
}

// ─────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────

function extractParagraphs(html: string): string[] {
  // Split by closing </p> tags
  return html
    .split(/(?<=<\/p>)/)
    .map((s) => s.trim())
    .filter(Boolean);
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '');
}

function wrapInPageBody(template: Template, content: string): string {
  return `<div class="page-body" style="font-family:${template.bodyFont};font-size:${PREVIEW_FONT_SIZE};line-height:${PREVIEW_LINE_HEIGHT};color:${template.inkColor};">${content}</div>`;
}

function wrapInPage(template: Template, content: string, pageNum: number): string {
  return `
    <div class="book-page" style="
      background:${template.paperColor};
      padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      height:100%;
      overflow:hidden;
      position:relative;
      box-sizing:border-box;
    ">
      ${content}
      ${template.showPageNumbers ? `<div style="position:absolute;bottom:8px;left:0;right:0;text-align:center;font-family:${template.bodyFont};font-size:7pt;color:${template.inkColor};opacity:0.4;">${pageNum}</div>` : ''}
    </div>
  `;
}
