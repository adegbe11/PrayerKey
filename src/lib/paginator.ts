import type { BookData, BookPage, Template, CoverConfig } from '@/types';
import { getChapterNumberDisplay } from './formatter';
import { renderCover } from './covers';
import { buildSectionBreak } from './templates';

// ─────────────────────────────────────────
//  BOOK PAGINATOR
//  Splits BookData into pages for preview.
//  Every page is crafted to match what a
//  professional press would produce.
// ─────────────────────────────────────────

// Approximate characters per page by page size
const CHARS_PER_PAGE: Record<string, number> = {
  trade: 1900,
  mass:  1600,
  digest: 1700,
  a5:    1800,
};

// Preview page dimensions — everything scaled to 260×380px
const PREVIEW_PAD_H  = '16px';
const PREVIEW_PAD_V  = '14px';
const PREVIEW_FONT   = '7.5pt';
const PREVIEW_LH     = '1.55';
const PREVIEW_INDENT = '1.2em';

// ─────────────────────────────────────────
//  MAIN ENTRY POINT
// ─────────────────────────────────────────

export function paginateBook(
  bookData: BookData,
  template: Template,
  coverConfig?: CoverConfig
): BookPage[] {
  const pages: BookPage[] = [];
  const cpp = CHARS_PER_PAGE[template.pageSize] ?? 1900;

  // ── Front cover ──────────────────────
  pages.push({
    pageNumber: 1,
    isCover: true,
    content: buildFrontCover(bookData, template, coverConfig),
  });

  // ── Half-title page ──────────────────
  pages.push({
    pageNumber: 2,
    content: buildHalfTitlePage(bookData, template),
  });

  // ── Full title page ──────────────────
  pages.push({
    pageNumber: 3,
    content: buildFullTitlePage(bookData, template),
  });

  // ── Copyright page ───────────────────
  pages.push({
    pageNumber: 4,
    content: buildCopyrightPage(bookData, template),
  });

  // ── Dedication ───────────────────────
  if (bookData.dedication) {
    pages.push({
      pageNumber: pages.length + 1,
      isDedication: true,
      content: buildDedicationPage(bookData.dedication, template),
    });
  }

  // ── Epigraph ─────────────────────────
  if (bookData.epigraph) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildEpigraphPage(bookData.epigraph, bookData.epigraphAttribution, template),
    });
  }

  // ── Table of Contents ────────────────
  if (bookData.chapters.length >= 2) {
    pages.push({
      pageNumber: pages.length + 1,
      isToc: true,
      content: buildTocPage(bookData, template, pages.length + 1),
    });
  }

  // ── Blank page (traditional typesetting) ─
  pages.push({
    pageNumber: pages.length + 1,
    isBlank: true,
    content: buildBlankPage(template),
  });

  // ── Chapters ─────────────────────────
  for (const chapter of bookData.chapters) {
    const chapterPages = paginateChapter(
      chapter,
      template,
      cpp,
      pages.length + 1,
      bookData.author
    );
    pages.push(...chapterPages);
  }

  // ── Acknowledgments ──────────────────
  if (bookData.acknowledgments) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildAcknowledgmentsPage(bookData.acknowledgments, template),
    });
  }

  // ── About the Author ─────────────────
  if (bookData.aboutAuthor) {
    pages.push({
      pageNumber: pages.length + 1,
      content: buildAboutAuthorPage(bookData.aboutAuthor, template),
    });
  }

  // ── Extras (any added elements with content) ──
  if (bookData.extras) {
    for (const [type, content] of Object.entries(bookData.extras)) {
      if (!content?.trim()) continue;
      pages.push({
        pageNumber: pages.length + 1,
        content: buildExtrasPage(type, content, template),
      });
    }
  }

  // ── "Before You Go" — Review CTA ─────  (highest-ROI page in any book)
  pages.push({
    pageNumber: pages.length + 1,
    content: buildReviewCTAPage(bookData, template),
  });

  // ── Newsletter CTA ────────────────────
  pages.push({
    pageNumber: pages.length + 1,
    content: buildNewsletterCTAPage(bookData, template),
  });

  // ── Back cover ───────────────────────
  pages.push({
    pageNumber: pages.length + 1,
    isCover: true,
    content: buildBackCover(bookData, template),
  });

  return pages;
}

// ─────────────────────────────────────────
//  FRONT COVER
// ─────────────────────────────────────────

function buildFrontCover(
  bookData: BookData,
  template: Template,
  coverConfig?: CoverConfig
): string {
  if (coverConfig?.type === 'uploaded' && coverConfig.uploadedUrl) {
    return `
      <div style="width:100%;height:100%;overflow:hidden;position:relative;">
        <img src="${coverConfig.uploadedUrl}" style="width:100%;height:100%;object-fit:cover;display:block;" />
      </div>
    `;
  }
  const templateId = coverConfig?.templateId ?? 'clean';
  return `
    <div style="width:100%;height:100%;overflow:hidden;">
      ${renderCover(templateId, bookData.title, bookData.author, bookData.subtitle)}
    </div>
  `;
}

// ─────────────────────────────────────────
//  HALF-TITLE PAGE
//  Traditional: title only, positioned in
//  the upper-center third of the page.
//  Nothing else on this page — ever.
// ─────────────────────────────────────────

export function buildHalfTitlePage(bookData: BookData, template: Template): string {
  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
      display:flex;flex-direction:column;align-items:center;
    ">
      <div style="flex:0.55;"></div>
      <div style="
        font-family:${template.headingFont};
        font-size:11pt;
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-align:center;
        line-height:1.2;
        text-transform:${template.headingTransform};
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.1em' : '0.02em'};
      ">
        ${esc(bookData.title)}
      </div>
      <div style="flex:1;"></div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  FULL TITLE PAGE
//  Professional publisher standard:
//  Large bold title (top half) /
//  subtitle flanked by thin rules /
//  generous white space /
//  author name prominent (lower section) /
//  publisher imprint small at foot.
// ─────────────────────────────────────────

export function buildFullTitlePage(bookData: BookData, template: Template): string {
  // Subtitle: each word stacked on its own line (HP-style "THE / COMPLETE / COLLECTION")
  const subtitleLines = bookData.subtitle
    ? bookData.subtitle.split(/\s+/).map(w => esc(w)).join('<br/>')
    : '';

  // Author in spaced uppercase for title-page gravitas
  const authorDisplay = (bookData.author || '').toUpperCase();

  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
      display:flex;flex-direction:column;align-items:center;
      text-align:center;
    ">

      <!-- Top spacer — title sits about 15% from top -->
      <div style="flex:0.25;min-height:8px;"></div>

      <!-- TITLE — large, dominant, bold -->
      <div style="
        font-family:${template.headingFont};
        font-size:20pt;
        font-weight:900;
        color:${template.headingColor};
        line-height:1.0;
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.04em' : '-0.01em'};
        text-transform:${template.headingTransform === 'none' ? 'uppercase' : template.headingTransform};
        text-align:center;
        word-break:break-word;
      ">
        ${esc(bookData.title)}
      </div>

      <!-- SUBTITLE flanked by thin rules -->
      ${bookData.subtitle ? `
      <div style="
        display:flex;align-items:center;justify-content:center;
        gap:8px;margin-top:0.55em;width:100%;
      ">
        <div style="flex:1;height:0.5px;background:${template.headingColor};opacity:0.35;max-width:36px;"></div>
        <div style="
          font-family:${template.headingFont};
          font-size:6.5pt;
          font-weight:${template.headingWeight};
          letter-spacing:0.22em;
          text-transform:uppercase;
          color:${template.headingColor};
          opacity:0.75;
          line-height:1.6;
          text-align:center;
        ">
          ${subtitleLines}
        </div>
        <div style="flex:1;height:0.5px;background:${template.headingColor};opacity:0.35;max-width:36px;"></div>
      </div>` : ''}

      <!-- Middle spacer — the generous white space that marks quality typography -->
      <div style="flex:1;min-height:20px;"></div>

      <!-- AUTHOR NAME — prominent, letter-spaced, not italic -->
      <div style="
        font-family:${template.bodyFont};
        font-size:10pt;
        font-weight:400;
        color:${template.inkColor};
        letter-spacing:0.12em;
        text-align:center;
        opacity:0.88;
      ">
        ${esc(authorDisplay)}
      </div>

      <!-- Bottom spacer before publisher mark -->
      <div style="flex:0.3;min-height:10px;"></div>

      <!-- PUBLISHER MARK — small at foot -->
      <div style="
        text-align:center;
        display:flex;flex-direction:column;align-items:center;gap:2px;
      ">
        <div style="
          font-family:${template.headingFont};
          font-size:7pt;
          font-weight:600;
          color:${template.inkColor};
          opacity:0.28;
          letter-spacing:0.18em;
          text-transform:uppercase;
        ">Booksane</div>
        <div style="
          font-family:${template.bodyFont};
          font-size:5.5pt;
          color:${template.inkColor};
          opacity:0.18;
          letter-spacing:0.06em;
        ">booksane.com</div>
      </div>

    </div>
  `;
}

// ─────────────────────────────────────────
//  COPYRIGHT PAGE
//  Verso (left/even page) of title page.
//  Small text, sits at the bottom third.
//  Genre-smart disclaimers. No branding.
// ─────────────────────────────────────────

const FICTION_GENRES = new Set(['fiction','romance','thriller','mystery','scifi','fantasy','horror','ya','childrens']);
const MEMOIR_GENRES  = new Set(['memoir','biography']);

export function buildCopyrightPage(bookData: BookData, template: Template): string {
  const year  = new Date().getFullYear();
  const genre = (bookData.genre ?? bookData.metadata?.genre ?? 'fiction') as string;

  // Genre-specific disclaimer
  const disclaimer = FICTION_GENRES.has(genre)
    ? `<p style="margin:0 0 0.55em;">This is a work of fiction. Names, characters, businesses, places, events, locales, and incidents are either the products of the author's imagination or used in a fictitious manner. Any resemblance to actual persons, living or dead, or actual events is purely coincidental.</p>`
    : MEMOIR_GENRES.has(genre)
    ? `<p style="margin:0 0 0.55em;">This work is based on the author's recollections and experiences. Some names and identifying details have been changed to protect the privacy of individuals.</p>`
    : genre === 'religious'
    ? `<p style="margin:0 0 0.55em;">Unless otherwise noted, scripture quotations are from the Holy Bible. All rights reserved.</p>`
    : '';

  return `
    <div style="
      display:flex;flex-direction:column;justify-content:flex-end;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <div style="
        font-family:${template.bodyFont};font-size:5.5pt;
        color:${template.inkColor};opacity:0.52;line-height:1.85;
      ">
        <p style="margin:0 0 0.8em;">First published ${year}</p>

        <p style="margin:0 0 0.3em;">Copyright &copy; ${year} ${bookData.author ? esc(bookData.author) : 'the Author'}</p>
        <p style="margin:0 0 0.8em;">All rights reserved.</p>

        <p style="margin:0 0 0.8em;">No part of this book may be reproduced in any form or by any electronic or mechanical means, including information storage and retrieval systems, without written permission from the author, except for the use of brief quotations in a book review.</p>

        ${disclaimer}

        <p style="margin:0 0 0.3em;">ISBN: 978-0-000000-00-0 (Paperback)</p>
        <p style="margin:0 0 0.8em;">ISBN: 978-0-000000-01-7 (eBook)</p>

        <p style="margin:0 0 0.3em;">First Edition</p>
        <p style="margin:0 0 0.8em;">Printed in the United States of America</p>

        <p style="margin:0;letter-spacing:0.08em;">10 &nbsp; 9 &nbsp; 8 &nbsp; 7 &nbsp; 6 &nbsp; 5 &nbsp; 4 &nbsp; 3 &nbsp; 2 &nbsp; 1</p>
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  DEDICATION
//  Centered, generous white space, italic.
//  Nothing else on the page — ever.
// ─────────────────────────────────────────

// ─────────────────────────────────────────
//  DEDICATION
//  Sits at 35% from top — traditional standard.
//  Italic, centered, nothing else on the page.
// ─────────────────────────────────────────

function buildDedicationPage(dedication: string, template: Template): string {
  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;
      display:flex;flex-direction:column;align-items:center;
    ">
      <div style="flex:0.65;"></div>
      <div style="
        font-family:${template.bodyFont};font-size:7.5pt;
        font-style:italic;color:${template.inkColor};opacity:0.75;
        max-width:80%;line-height:2;text-align:center;
      ">
        ${dedication.split('\n').filter(Boolean).map(l => `<p style="margin:0 0 0.35em;">${esc(l)}</p>`).join('')}
      </div>
      <div style="flex:1;"></div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  EPIGRAPH
//  Sits about 35% from top, right-aligned,
//  italic quote, attribution with em dash.
//  This is the Knopf / FSG standard.
// ─────────────────────────────────────────

export function buildEpigraphPage(
  epigraph: string | undefined,
  attribution: string | undefined,
  template: Template
): string {
  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;
      display:flex;flex-direction:column;align-items:flex-end;
    ">
      <div style="flex:0.65;"></div>
      <div style="max-width:76%;text-align:right;">
        <div style="
          font-family:${template.bodyFont};font-size:7.5pt;
          font-style:italic;color:${template.inkColor};opacity:0.75;
          line-height:1.9;margin-bottom:0.9em;
        ">
          &ldquo;${esc(epigraph ?? '')}&rdquo;
        </div>
        ${attribution ? `
        <div style="
          font-family:${template.bodyFont};font-size:6.5pt;
          color:${template.inkColor};opacity:0.48;
          letter-spacing:0.02em;
        ">&#8212; ${esc(attribution)}</div>` : ''}
      </div>
      <div style="flex:1;"></div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  TABLE OF CONTENTS
//  Leader dots, right-aligned page numbers.
//  "CONTENTS" in letter-spaced small caps.
// ─────────────────────────────────────────

export function buildTocPage(bookData: BookData, template: Template, tocPageNum: number): string {
  const frontMatterPages = tocPageNum; // approx pages before first chapter
  const items = bookData.chapters.map((ch, i) => {
    const pgNum = frontMatterPages + i + 1;
    const chNumLabel = template.chapterNumberStyle !== 'none'
      ? `<span style="opacity:0.45;margin-right:6px;font-size:5.5pt;letter-spacing:0.05em;">
           ${template.chapterNumberStyle === 'roman'  ? getChapterNumberDisplay(ch.number, 'roman') :
             template.chapterNumberStyle === 'spelled' ? getChapterNumberDisplay(ch.number, 'spelled') :
             ch.number}
         </span>`
      : '';
    const isMeaningful = !/^chapter\s+\d+$/i.test(ch.title);
    const titleText = isMeaningful ? ch.title : (template.chapterNumberStyle === 'none' ? `Chapter ${ch.number}` : '');
    return `
      <div style="
        display:flex;align-items:baseline;
        margin-bottom:0.55em;
        font-family:${template.bodyFont};font-size:6.5pt;
        color:${template.inkColor};
      ">
        <span style="display:flex;align-items:baseline;flex:1;overflow:hidden;">
          ${chNumLabel}
          <span style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(titleText || ch.title)}</span>
          <!-- Leader dots -->
          <span style="flex:1;border-bottom:1px dotted ${template.inkColor};opacity:0.2;margin:0 4px 2px;"></span>
        </span>
        <span style="opacity:0.45;font-size:6pt;flex-shrink:0;">${pgNum}</span>
      </div>
    `;
  }).join('');

  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};
      box-sizing:border-box;overflow:hidden;
    ">
      <!-- CONTENTS heading — letter-spaced, small caps, centered -->
      <div style="
        font-family:${template.headingFont};
        font-size:7pt;
        font-weight:${template.headingWeight};
        text-align:center;
        color:${template.headingColor};
        text-transform:uppercase;
        letter-spacing:0.25em;
        margin-bottom:0.2em;
        opacity:0.78;
      ">Contents</div>
      <!-- Thin rule under heading -->
      <div style="
        width:20px;height:0.5px;
        background:${template.accentColor};opacity:0.45;
        margin:0 auto 1.1em;
      "></div>
      <div>${items}</div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  BLANK PAGE
// ─────────────────────────────────────────

function buildBlankPage(template: Template): string {
  return `<div style="width:100%;height:100%;background:${template.paperColor};"></div>`;
}

// ─────────────────────────────────────────
//  ACKNOWLEDGMENTS
// ─────────────────────────────────────────

export function buildAcknowledgmentsPage(text: string, template: Template): string {
  const paragraphs = text.split(/\n{2,}/).map(p => p.trim()).filter(Boolean);
  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;overflow:hidden;
    ">
      <div style="
        font-family:${template.headingFont};font-size:8.5pt;
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-transform:${template.headingTransform};
        text-align:${template.headingAlign};
        margin-bottom:1em;letter-spacing:${template.headingTransform === 'uppercase' ? '0.08em' : '0'};
      ">
        Acknowledgments
      </div>
      <div style="
        font-family:${template.bodyFont};font-size:${PREVIEW_FONT};
        line-height:${PREVIEW_LH};color:${template.inkColor};opacity:0.88;
      ">
        ${paragraphs.map(p => `<p style="margin:0 0 0.5em;">${esc(p)}</p>`).join('')}
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  ABOUT THE AUTHOR
// ─────────────────────────────────────────

export function buildAboutAuthorPage(text: string, template: Template): string {
  const paragraphs = text.split(/\n{2,}/).map(p => p.trim()).filter(Boolean);
  return `
    <div style="
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;overflow:hidden;
    ">
      <div style="
        font-family:${template.headingFont};font-size:8.5pt;
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-transform:${template.headingTransform};
        text-align:${template.headingAlign};
        margin-bottom:1em;letter-spacing:${template.headingTransform === 'uppercase' ? '0.08em' : '0'};
      ">
        About the Author
      </div>
      <div style="
        font-family:${template.bodyFont};font-size:${PREVIEW_FONT};
        line-height:${PREVIEW_LH};color:${template.inkColor};opacity:0.88;
      ">
        ${paragraphs.map(p => `<p style="margin:0 0 0.5em;">${esc(p)}</p>`).join('')}
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  "BEFORE YOU GO" — REVIEW CTA
//  The highest-ROI page in any self-published
//  book. Placed after About the Author while
//  the reader is still emotionally engaged.
// ─────────────────────────────────────────

function buildReviewCTAPage(bookData: BookData, template: Template): string {
  return `
    <div style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;text-align:center;
    ">
      <!-- Ornament -->
      <div style="font-size:1.1em;color:${template.accentColor};margin-bottom:0.8em;opacity:0.7;">★</div>

      <!-- Heading -->
      <div style="
        font-family:${template.headingFont};font-size:9pt;
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-transform:${template.headingTransform};
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.08em' : '0'};
        margin-bottom:0.8em;
      ">
        Before You Go
      </div>

      <!-- Thin rule -->
      <div style="width:24px;height:1px;background:${template.accentColor};opacity:0.4;margin-bottom:1em;"></div>

      <!-- Body -->
      <div style="
        font-family:${template.bodyFont};font-size:6.5pt;
        line-height:1.85;color:${template.inkColor};opacity:0.75;
        max-width:86%;
      ">
        <p style="margin:0 0 0.6em;">
          If you enjoyed <em>${esc(bookData.title)}</em>, would you take a moment
          to leave a review?
        </p>
        <p style="margin:0 0 0.8em;">
          Reviews help other readers discover the book and mean more to an author
          than you might imagine. Even a sentence or two makes a real difference.
        </p>
      </div>

      <!-- CTA -->
      <div style="
        font-family:${template.headingFont};font-size:6.5pt;
        color:${template.accentColor};
        font-weight:700;
        letter-spacing:0.1em;text-transform:uppercase;
        opacity:0.85;
      ">
        Thank you
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  NEWSLETTER / READER LIST CTA
//  Free book offer converts at 8–15%.
//  Every career author needs this page.
// ─────────────────────────────────────────

function buildNewsletterCTAPage(bookData: BookData, template: Template): string {
  return `
    <div style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100%;padding:${PREVIEW_PAD_V} ${PREVIEW_PAD_H};
      background:${template.paperColor};box-sizing:border-box;text-align:center;
    ">
      <!-- Heading -->
      <div style="
        font-family:${template.headingFont};font-size:9pt;
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-transform:${template.headingTransform};
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.08em' : '0'};
        margin-bottom:0.5em;
      ">
        Get a Free Book
      </div>

      <div style="width:24px;height:1px;background:${template.accentColor};opacity:0.4;margin-bottom:0.9em;"></div>

      <div style="
        font-family:${template.bodyFont};font-size:6.5pt;
        line-height:1.85;color:${template.inkColor};opacity:0.72;
        max-width:84%;
      ">
        <p style="margin:0 0 0.6em;">
          Join ${esc(bookData.author || 'the author')}'s reader list and receive
          an exclusive free book, plus news of upcoming releases, behind-the-scenes
          stories, and reader-only giveaways.
        </p>
      </div>

      <!-- URL placeholder -->
      <div style="
        margin-top:0.9em;
        font-family:${template.headingFont};font-size:7pt;
        color:${template.accentColor};
        font-weight:600;
        letter-spacing:0.06em;
        border-bottom:1px solid ${template.accentColor};
        opacity:0.8;
        padding-bottom:1px;
      ">
        yourwebsite.com/free
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────
//  BACK COVER
// ─────────────────────────────────────────

export function buildBackCover(bookData: BookData, template: Template): string {
  let blurb = '';
  if (bookData.chapters.length > 0) {
    const plainText = bookData.chapters[0].content
      .replace(/<[^>]*>/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    const match = plainText.match(/^([^.!?]*[.!?][^.!?]*[.!?][^.!?]*[.!?])/);
    blurb = match ? match[1].trim() : plainText.slice(0, 200);
    if (blurb.length > 240) blurb = blurb.slice(0, 237) + '…';
  }

  return `
    <div style="
      width:100%;height:100%;
      background:#0A0910;
      display:flex;flex-direction:column;align-items:center;
      padding:22px 18px 16px;box-sizing:border-box;overflow:hidden;
    ">
      <div style="font-family:'Inter',system-ui;font-size:8pt;font-weight:700;color:#FFE500;text-align:center;letter-spacing:0.05em;margin-bottom:14px;text-transform:uppercase;">
        ${esc(bookData.title)}
      </div>
      <div style="width:30px;height:1px;background:rgba(255,255,255,0.18);margin-bottom:10px;"></div>
      ${blurb
        ? `<div style="font-family:Georgia,serif;font-size:6.5pt;font-style:italic;color:rgba(200,200,200,0.72);line-height:1.65;text-align:center;max-width:200px;margin-bottom:auto;">${esc(blurb)}</div>`
        : '<div style="flex:1;"></div>'
      }
      <div style="font-family:'Inter',system-ui;font-size:7pt;color:rgba(255,255,255,0.45);text-align:center;margin-top:16px;text-transform:uppercase;letter-spacing:0.1em;">
        ${esc(bookData.author || '')}
      </div>
      <div style="margin-top:12px;display:flex;flex-direction:column;align-items:center;gap:3px;">
        <div style="width:80px;height:20px;overflow:hidden;display:flex;gap:0.5px;">
          ${barcodeHtml()}
        </div>
        <div style="font-family:monospace;font-size:5.5pt;color:rgba(255,255,255,0.3);letter-spacing:0.05em;">978-0-000000-00-0</div>
      </div>
      <div style="margin-top:8px;font-family:'Inter',system-ui;font-size:6.5pt;color:rgba(255,255,255,0.2);letter-spacing:0.12em;text-transform:uppercase;">
        BOOKSANE
      </div>
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
  startPageNumber: number,
  authorName?: string
): BookPage[] {
  const pages: BookPage[] = [];

  // ── Chapter number display ─
  const numDisplay = buildChapterNumberBlock(chapter, template);

  // ── Chapter title (only if meaningfully different from "Chapter N") ─
  const isMeaningful = !/^chapter\s+\d+$/i.test(chapter.title);
  const titleDisplay = isMeaningful
    ? `<div style="
        font-family:${template.headingFont};font-size:9pt;
        font-weight:${template.headingWeight};
        text-align:${template.headingAlign};
        text-transform:${template.headingTransform};
        color:${template.headingColor};
        margin-bottom:0.6em;line-height:1.15;
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.06em' : '0'};
      ">${esc(chapter.title)}</div>`
    : '';

  // ── Thin ornament rule between number and title ─
  const dividerLine = (numDisplay && titleDisplay) ? `
    <div style="
      width:22px;height:1px;
      background:${template.accentColor};opacity:0.4;
      margin:0.4em ${template.headingAlign === 'center' ? 'auto' : '0'} 0.5em;
    "></div>` : '';

  // Chapter opening sits 18% down the preview page (≈ 33% of a real book page)
  const chapterHeader = `
    <div style="padding-top:18%;box-sizing:border-box;">
      ${numDisplay}
      ${dividerLine}
      ${titleDisplay}
      <div style="margin-top:${numDisplay || titleDisplay ? '0.8em' : '0'};"></div>
    </div>
  `;

  // ── Extract and process paragraphs ─
  const rawParas = extractParagraphs(chapter.content);

  let currentPageContent = '';
  let isFirstPage = true;
  let isFirstParagraph = true;
  let pageNum = startPageNumber;

  for (let i = 0; i < rawParas.length; i++) {
    let para = rawParas[i];

    // Section break — use template ornament
    const isSectionBreak = para.includes('class="section-break"');
    if (isSectionBreak) {
      const breakHtml = `<div style="margin:0.5em 0;">${buildSectionBreak(template)}</div>`;
      if (!currentPageContent) {
        currentPageContent = breakHtml;
      } else {
        currentPageContent += breakHtml;
      }
      continue;
    }

    // Drop cap on the very first paragraph of the chapter
    if (isFirstParagraph && template.dropCap) {
      para = applyDropCap(para, template);
      isFirstParagraph = false;
    } else {
      isFirstParagraph = false;
    }

    const paraText = stripHtml(para);

    if (!currentPageContent) {
      currentPageContent = isFirstPage
        ? chapterHeader + wrapParagraph(template, para, true)
        : wrapParagraph(template, para, false);
      isFirstPage = false;
    } else if (stripHtml(currentPageContent).length + paraText.length > charsPerPage) {
      pages.push({
        pageNumber: pageNum,
        content: wrapInPage(template, currentPageContent, pageNum, {
          isChapterStart: pages.length === 0,
          chapterTitle: isMeaningful ? chapter.title : `Chapter ${chapter.number}`,
          authorName,
        }),
        chapterId: chapter.id,
        isChapterStart: pages.length === 0,
      });
      pageNum++;
      currentPageContent = wrapParagraph(template, para, false);
      isFirstPage = false;
    } else {
      currentPageContent += '\n' + wrapParagraph(template, para, false);
    }
  }

  // Final page
  if (currentPageContent) {
    pages.push({
      pageNumber: pageNum,
      content: wrapInPage(template, currentPageContent, pageNum, {
        isChapterStart: pages.length === 0,
        chapterTitle: isMeaningful ? chapter.title : `Chapter ${chapter.number}`,
        authorName,
      }),
      chapterId: chapter.id,
      isChapterStart: pages.length === 0,
    });
  }

  return pages;
}

// ─────────────────────────────────────────
//  CHAPTER NUMBER BLOCK
// ─────────────────────────────────────────

function buildChapterNumberBlock(
  chapter: { number: number; title: string },
  template: Template
): string {
  if (template.chapterNumberStyle === 'none') return '';

  if (template.chapterNumberStyle === 'numeral') {
    // Large display numeral (Penguin Modern / HBR / Anchor / Tor style)
    const isLargeNum = parseFloat(template.chapterNumberSize) >= 30;
    if (isLargeNum) {
      return `
        <div style="
          font-family:${template.headingFont};
          font-size:22pt;
          font-weight:${template.headingWeight};
          color:${template.accentColor};
          text-align:${template.headingAlign};
          line-height:1;opacity:0.85;
          margin-bottom:0.15em;
        ">${chapter.number}</div>
      `;
    }
    return `
      <div style="
        font-family:${template.headingFont};font-size:8pt;
        font-weight:${template.headingWeight};
        text-align:${template.headingAlign};
        color:${template.accentColor};
        text-transform:${template.headingTransform};
        letter-spacing:${template.headingTransform === 'uppercase' ? '0.1em' : '0.04em'};
        margin-bottom:0.3em;opacity:0.9;
      ">Chapter ${chapter.number}</div>
    `;
  }

  if (template.chapterNumberStyle === 'roman') {
    return `
      <div style="
        font-family:${template.headingFont};font-size:8pt;
        font-weight:${template.headingWeight};
        text-align:${template.headingAlign};
        color:${template.accentColor};
        letter-spacing:0.1em;
        margin-bottom:0.3em;opacity:0.8;
      ">${getChapterNumberDisplay(chapter.number, 'roman')}</div>
    `;
  }

  if (template.chapterNumberStyle === 'spelled') {
    return `
      <div style="
        font-family:${template.headingFont};font-size:8pt;
        font-weight:300;
        text-align:${template.headingAlign};
        color:${template.accentColor};
        text-transform:uppercase;
        letter-spacing:0.18em;
        margin-bottom:0.3em;opacity:0.7;
      ">Chapter ${getChapterNumberDisplay(chapter.number, 'spelled')}</div>
    `;
  }

  return '';
}

// ─────────────────────────────────────────
//  DROP CAP
//  Extracts the first letter, floats it
//  large-left, and wraps the next few words
//  in small-caps — just like Knopf does.
// ─────────────────────────────────────────

function applyDropCap(paraHtml: string, template: Template): string {
  // Extract inner content of <p>
  const inner = paraHtml.replace(/^<p[^>]*>/, '').replace(/<\/p>\s*$/, '');
  if (!inner.trim()) return paraHtml;

  // Find first real letter (skip opening quotes and HTML entities)
  const m = inner.match(/^([^A-Za-z]*)([A-Za-z])([\s\S]*)$/);
  if (!m) return paraHtml;

  const prefix   = m[1]; // opening quote, if any
  const letter   = m[2]; // the drop-cap letter
  const rest     = m[3]; // remainder of paragraph

  // Get first 5 words for small-caps treatment
  let scEnd   = 0;
  let spaces  = 0;
  let inTag   = false;
  for (let i = 0; i < rest.length; i++) {
    if (rest[i] === '<') { inTag = true; continue; }
    if (rest[i] === '>') { inTag = false; continue; }
    if (!inTag && rest[i] === ' ') {
      spaces++;
      if (spaces >= 5) { scEnd = i; break; }
    }
  }
  if (scEnd === 0) scEnd = rest.length;

  const smallCapsWords = rest.slice(0, scEnd);
  const remainder      = rest.slice(scEnd);

  const dropCapStyle = [
    `float:left`,
    `font-family:${template.headingFont}`,
    `font-size:3em`,
    `line-height:0.82`,
    `font-weight:700`,
    `color:${template.accentColor}`,
    `padding-right:3px`,
    `margin-top:2px`,
  ].join(';');

  return `<p style="margin:0;overflow:visible;">${prefix}<span style="${dropCapStyle}">${letter}</span><span style="font-variant:small-caps;letter-spacing:0.05em;font-size:0.9em;">${smallCapsWords}</span>${remainder}</p>`;
}

// ─────────────────────────────────────────
//  PAGE WRAPPERS
// ─────────────────────────────────────────

interface PageOpts {
  isChapterStart?: boolean;
  chapterTitle?: string;
  authorName?: string;
  suppressAll?: boolean;
}

function wrapInPage(
  template: Template,
  content: string,
  pageNum: number,
  opts: PageOpts = {}
): string {
  const { isChapterStart, chapterTitle, authorName, suppressAll } = opts;
  const isEven  = pageNum % 2 === 0;
  const showHead = template.showPageNumbers && !isChapterStart && !suppressAll;

  // Running head — verso: author name, recto: chapter title
  const headText = showHead
    ? (isEven ? (authorName ?? '') : (chapterTitle ?? ''))
    : '';

  // Running head HTML (sits above main text, tiny)
  const runHead = showHead && headText
    ? `<div style="
        display:flex;
        justify-content:${isEven ? 'flex-start' : 'flex-end'};
        align-items:center;
        height:8px;
        margin-bottom:5px;
        border-bottom:0.4px solid ${template.inkColor};
        opacity:0.18;
        padding-bottom:3px;
      ">
        <span style="
          font-family:${template.bodyFont};font-size:5pt;
          color:${template.inkColor};
          font-variant:small-caps;letter-spacing:0.1em;
          opacity:0.6;
        ">${esc(headText)}</span>
      </div>`
    : '';

  // Page number — outer corner, not centered
  const folio = (template.showPageNumbers && !isChapterStart && !suppressAll)
    ? `<div style="
        position:absolute;
        bottom:6px;
        ${isEven ? 'left' : 'right'}:${PREVIEW_PAD_H};
        font-family:${template.bodyFont};font-size:6pt;
        color:${template.inkColor};opacity:0.38;
      ">${pageNum}</div>`
    : '';

  const topPad = (showHead && headText) ? '0' : PREVIEW_PAD_V;

  return `
    <div style="
      background:${template.paperColor};
      padding:${topPad} ${PREVIEW_PAD_H} ${PREVIEW_PAD_V};
      height:100%;
      overflow:hidden;
      position:relative;
      box-sizing:border-box;
    ">
      ${runHead}
      <div style="font-family:${template.bodyFont};font-size:${PREVIEW_FONT};line-height:${PREVIEW_LH};color:${template.inkColor};">${content}</div>
      ${folio}
    </div>
  `;
}

function wrapParagraph(template: Template, paraHtml: string, isFirst: boolean): string {
  const hasOwnStyle = paraHtml.startsWith('<p style="margin:0');
  if (hasOwnStyle) return paraHtml; // drop-cap paragraphs already have their own style

  const indent = template.paragraphStyle === 'indent' && !isFirst ? template.textIndent : '0';
  const mb = template.paragraphStyle === 'spaced' ? '0.6em' : '0';

  // Replace bare <p> with styled one
  return paraHtml.replace(
    /^<p([^>]*)>/,
    `<p$1 style="margin:0 0 ${mb};padding:0;text-indent:${indent};font-family:${template.bodyFont};font-size:${PREVIEW_FONT};line-height:${PREVIEW_LH};color:${template.inkColor};">`
  );
}

// ─────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────

function extractParagraphs(html: string): string[] {
  return html
    .split(/(?<=<\/p>)/)
    .map(s => s.trim())
    .filter(Boolean);
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '');
}

// ─────────────────────────────────────────
//  EXTRAS PAGE (any added nav element)
// ─────────────────────────────────────────

const EXTRAS_LABELS: Record<string, string> = {
  'foreword': 'Foreword',
  'preface': 'Preface',
  'introduction': 'Introduction',
  'epilogue': 'Epilogue',
  'afterword': 'Afterword',
  'authors-note': "Author's Note",
  'end-notes': 'End Notes',
  'note-on-sources': 'A Note on Sources',
  'note-on-type': 'A Note on the Type',
  'permissions': 'Permissions',
  'credits': 'Credits',
  'appendix': 'Appendix',
  'glossary': 'Glossary',
  'bibliography': 'Bibliography',
  'index': 'Index',
  'connect-with-author': 'Connect with the Author',
  'bonus-epilogue': 'Bonus Epilogue',
  'qa-with-author': 'Q&A with the Author',
  'also-by-back': 'Also By This Author',
  'reading-guide': 'Reading Group Guide',
  'next-book-preview': 'Preview of Next Book',
  'colophon': 'Colophon',
  'cast-of-characters': 'Cast of Characters',
  'pronunciation-guide': 'Pronunciation Guide',
  'timeline': 'Timeline',
  'family-tree': 'Family Tree',
  'content-warning': 'Content Warning',
  'publishers-note': "Publisher's Note",
  'translators-note': "Translator's Note",
  'editors-note': "Editor's Note",
  'interlude': 'Interlude',
  'part-title': 'Part',
};

function buildExtrasPage(type: string, content: string, template: Template): string {
  const heading = EXTRAS_LABELS[type] ?? type.replace(/-/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  return wrapInPage(template, `
    <div style="padding-top:14%;text-align:${template.headingAlign};">
      <div style="
        font-family:${template.headingFont};
        font-size:${template.headingSize};
        font-weight:${template.headingWeight};
        color:${template.headingColor};
        text-transform:${template.headingTransform};
        letter-spacing:0.03em;
        margin-bottom:10px;
      ">${esc(heading)}</div>
      <div style="width:22px;height:1px;background:${template.accentColor};margin:0 auto 12px;${template.headingAlign === 'left' ? 'margin-left:0;' : ''}"></div>
    </div>
    <div style="
      font-family:${template.bodyFont};
      font-size:${PREVIEW_FONT};
      line-height:${PREVIEW_LH};
      color:${template.inkColor};
    ">${content}</div>
  `, 0, { suppressAll: true });
}

function esc(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function barcodeHtml(): string {
  const w = [2,1,2,1,1,2,1,1,2,1,2,1,1,2,1,2,1,1,2,1,1,2,1,2,1,1,2,1,2,1];
  return w.map((n, i) =>
    `<div style="width:${n}px;height:100%;background:${i % 2 === 0 ? 'rgba(255,255,255,0.75)' : 'transparent'};flex-shrink:0;"></div>`
  ).join('');
}
