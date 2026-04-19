'use client';
import type { BookData } from '@/types';
import type { Template } from '@/types';

// ─────────────────────────────────────────
//  EPUB 3 GENERATOR
//  Creates a valid .epub file via JSZip
// ─────────────────────────────────────────

export async function generateEpub(bookData: BookData, template: Template): Promise<Blob> {
  const JSZip = (await import('jszip')).default;
  const zip = new JSZip();

  const slug = bookData.title.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  const uid = `urn:uuid:${generateUUID()}`;

  // ── mimetype (MUST be first, uncompressed)
  zip.file('mimetype', 'application/epub+zip', { compression: 'STORE' });

  // ── META-INF/container.xml
  zip.folder('META-INF')!.file(
    'container.xml',
    `<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>`
  );

  const oebps = zip.folder('OEBPS')!;

  // ── Stylesheet
  oebps.folder('styles')!.file('book.css', buildEpubCSS(template));

  // ── Front matter pages
  const frontMatterItems: { id: string; filename: string; label: string }[] = [];

  // Half-title page
  oebps.file('content/half-title.xhtml', buildHalfTitleXHTML(bookData, template));
  frontMatterItems.push({ id: 'half-title', filename: 'content/half-title.xhtml', label: bookData.title });

  // Title page (always present)
  oebps.folder('content');
  oebps.file('content/title-page.xhtml', buildTitlePageXHTML(bookData, template));
  frontMatterItems.push({ id: 'title-page', filename: 'content/title-page.xhtml', label: 'Title Page' });

  // Copyright page (always present)
  oebps.file('content/copyright.xhtml', buildCopyrightXHTML(bookData, template));
  frontMatterItems.push({ id: 'copyright', filename: 'content/copyright.xhtml', label: 'Copyright' });

  // Dedication (if present)
  if (bookData.dedication) {
    oebps.file('content/dedication.xhtml', buildDedicationXHTML(bookData, template));
    frontMatterItems.push({ id: 'dedication', filename: 'content/dedication.xhtml', label: 'Dedication' });
  }

  // Epigraph (if present)
  if (bookData.epigraph) {
    oebps.file('content/epigraph.xhtml', buildEpigraphXHTML(bookData, template));
    frontMatterItems.push({ id: 'epigraph', filename: 'content/epigraph.xhtml', label: 'Epigraph' });
  }

  // ── Chapter files
  const chapterItems = bookData.chapters.map((ch, i) => ({
    id: `chapter-${i + 1}`,
    filename: `content/chapter-${i + 1}.xhtml`,
  }));

  for (let i = 0; i < bookData.chapters.length; i++) {
    const ch = bookData.chapters[i];
    oebps.file(`content/chapter-${i + 1}.xhtml`, buildChapterXHTML(ch, template));
  }

  // ── Back matter: acknowledgments, about author, extras
  const backMatterItems: { id: string; filename: string; label: string }[] = [];

  if (bookData.acknowledgments) {
    oebps.file('content/acknowledgments.xhtml', buildSimplePageXHTML('Acknowledgments', bookData.acknowledgments, template));
    backMatterItems.push({ id: 'acknowledgments', filename: 'content/acknowledgments.xhtml', label: 'Acknowledgments' });
  }
  if (bookData.aboutAuthor) {
    oebps.file('content/about-author.xhtml', buildSimplePageXHTML('About the Author', bookData.aboutAuthor, template));
    backMatterItems.push({ id: 'about-author', filename: 'content/about-author.xhtml', label: 'About the Author' });
  }
  if (bookData.extras) {
    for (const [key, value] of Object.entries(bookData.extras)) {
      if (!value) continue;
      const label = key.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
      const safeId = `extra-${key}`;
      oebps.file(`content/${safeId}.xhtml`, buildSimplePageXHTML(label, value, template));
      backMatterItems.push({ id: safeId, filename: `content/${safeId}.xhtml`, label });
    }
  }

  const allBodyItems = [...frontMatterItems, ...chapterItems, ...backMatterItems];

  // ── Nav / TOC
  oebps.file(
    'nav.xhtml',
    buildNavXHTML(bookData, frontMatterItems, chapterItems, backMatterItems)
  );

  // ── content.opf
  oebps.file('content.opf', buildContentOPF(bookData, uid, allBodyItems, chapterItems));

  // ── toc.ncx (for backwards compat)
  oebps.file('toc.ncx', buildTocNCX(bookData, uid, chapterItems));

  return zip.generateAsync({ type: 'blob', mimeType: 'application/epub+zip' });
}

// ─────────────────────────────────────────
//  BUILDERS
// ─────────────────────────────────────────

function buildEpubCSS(template: Template): string {
  const dropCapCss = template.dropCap ? `
.drop-cap::first-letter {
  float: left;
  font-family: ${template.headingFont};
  font-size: 3.6em;
  line-height: 0.82;
  font-weight: 700;
  color: ${template.accentColor};
  padding-right: 4px;
  margin-top: 4px;
}` : '';

  return `
@charset "UTF-8";
body {
  font-family: ${template.bodyFont};
  font-size: 1em;
  line-height: ${template.lineHeight};
  color: ${template.inkColor};
  background: ${template.paperColor};
  margin: 0;
  padding: 1em 1.5em;
}
/* Chapter-level headings */
h1, h2.chapter-title {
  font-family: ${template.headingFont};
  font-size: 1.8em;
  font-weight: ${template.headingWeight};
  text-align: ${template.headingAlign};
  text-transform: ${template.headingTransform};
  color: ${template.headingColor};
  margin-bottom: 2em;
}
/* In-chapter section headings (H2/H3 added by author in Tiptap) */
.chapter-body h1 {
  font-family: ${template.headingFont};
  font-size: 1.35em;
  font-weight: ${template.headingWeight};
  color: ${template.headingColor};
  text-transform: ${template.headingTransform};
  margin: 1.5em 0 0.5em;
}
.chapter-body h2 {
  font-family: ${template.headingFont};
  font-size: 1.15em;
  font-weight: ${template.headingWeight};
  color: ${template.headingColor};
  margin: 1.3em 0 0.4em;
}
.chapter-body h3 {
  font-family: ${template.bodyFont};
  font-size: 1em;
  font-weight: 700;
  font-style: italic;
  color: ${template.headingColor};
  margin: 1.1em 0 0.3em;
}
p {
  margin: 0;
  text-indent: ${template.paragraphStyle === 'indent' ? template.textIndent : '0'};
  margin-bottom: ${template.paragraphStyle === 'spaced' ? template.paragraphSpacing : '0'};
}
p + p {
  margin-top: ${template.paragraphStyle === 'indent' ? '0' : template.paragraphSpacing};
}
h1 + p, h2 + p, h3 + p, .chapter-start + p {
  text-indent: 0;
}
.chapter-body p:first-child {
  text-indent: 0;
}
.chapter-start {
  padding-top: 3em;
}
.chapter-number {
  display: block;
  font-family: ${template.headingFont};
  font-size: 1.2em;
  font-weight: ${template.headingWeight};
  text-align: ${template.headingAlign};
  color: ${template.accentColor};
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin-bottom: 0.3em;
}
.chapter-title {
  font-family: ${template.headingFont};
  font-size: 1.8em;
  font-weight: ${template.headingWeight};
  text-align: ${template.headingAlign};
  text-transform: ${template.headingTransform};
  color: ${template.headingColor};
  margin-bottom: 2em;
}
.section-break {
  text-align: center;
  margin: 1.5em 0;
  color: ${template.accentColor};
  opacity: 0.7;
}
${dropCapCss}
`;
}

function applyDropCap(content: string, template: Template): string {
  if (!template.dropCap) return content;
  // Add drop-cap class to the first <p> that has actual text content
  return content.replace(/<p([^>]*)>/, (match, attrs) => {
    if (/class="section-break"/.test(attrs)) return match;
    return `<p${attrs} class="drop-cap">`;
  });
}

function epubChNum(n: number, style: string): string {
  if (style === 'roman') {
    const v=[1000,900,500,400,100,90,50,40,10,9,5,4,1],s=['M','CM','D','CD','C','XC','L','XL','X','IX','V','IV','I'];
    let r=''; for(let i=0;i<v.length;i++) while(n>=v[i]){r+=s[i];n-=v[i];} return r;
  }
  const SP=['','One','Two','Three','Four','Five','Six','Seven','Eight','Nine','Ten',
    'Eleven','Twelve','Thirteen','Fourteen','Fifteen','Sixteen','Seventeen','Eighteen','Nineteen','Twenty'];
  if (style === 'spelled') return n < SP.length ? SP[n] : String(n);
  return String(n);
}

function buildChapterXHTML(
  chapter: { id: string; number: number; title: string; content: string },
  template: Template
): string {
  const numStyle  = template.chapterNumberStyle || 'numeral';
  const numLabel  = numStyle === 'numeral' ? `Chapter ${chapter.number}` : epubChNum(chapter.number, numStyle);
  const numDisplay = numStyle !== 'none'
    ? `<span class="chapter-number">${numLabel}</span>`
    : '';

  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>${escapeXml(chapter.title)}</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
</head>
<body>
  <div class="chapter-start">
    ${numDisplay}
    <h2 class="chapter-title">${escapeXml(chapter.title)}</h2>
  </div>
  <div class="chapter-body">
    ${applyDropCap(chapter.content, template)}
  </div>
</body>
</html>`;
}

function buildHalfTitleXHTML(bookData: BookData, template: Template): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>${escapeXml(bookData.title)}</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
  <style>
    .half-title-page { display:flex; align-items:center; justify-content:center; min-height:90vh; text-align:center; }
    .half-title-text { font-family:${template.headingFont}; font-size:1.4em; font-weight:${template.headingWeight}; color:${template.inkColor}; text-transform:${template.headingTransform}; }
  </style>
</head>
<body>
  <div class="half-title-page">
    <div class="half-title-text">${escapeXml(bookData.title)}</div>
  </div>
</body>
</html>`;
}

function buildTitlePageXHTML(bookData: BookData, template: Template): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Title Page</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
  <style>
    .title-page { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:90vh; text-align:center; padding: 2em; }
    .book-title { font-family:${template.headingFont}; font-size:2.4em; font-weight:${template.headingWeight}; color:${template.headingColor}; margin-bottom:0.3em; line-height:1.2; }
    .book-subtitle { font-family:${template.headingFont}; font-size:1.2em; color:${template.accentColor}; margin-bottom:2em; font-style:italic; }
    .book-author { font-family:${template.bodyFont}; font-size:1.1em; color:${template.inkColor}; margin-top:2em; }
  </style>
</head>
<body>
  <div class="title-page">
    <div class="book-title">${escapeXml(bookData.title)}</div>
    ${bookData.subtitle ? `<div class="book-subtitle">${escapeXml(bookData.subtitle)}</div>` : ''}
    <div class="book-author">${escapeXml(bookData.author || '')}</div>
  </div>
</body>
</html>`;
}

function buildCopyrightXHTML(bookData: BookData, template: Template): string {
  const year = new Date().getFullYear();
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Copyright</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
  <style>
    .copyright-page { display:flex; flex-direction:column; justify-content:flex-end; min-height:90vh; padding:2em; }
    .copyright-page p { font-size:0.75em; color:${template.inkColor}; line-height:1.6; margin-bottom:0.5em; opacity:0.7; }
  </style>
</head>
<body>
  <div class="copyright-page">
    <p>Copyright © ${year} ${escapeXml(bookData.author || 'the Author')}</p>
    <p>All rights reserved. No part of this publication may be reproduced, distributed, or transmitted in any form or by any means, including photocopying, recording, or other electronic or mechanical methods, without the prior written permission of the publisher.</p>
    <p>Published by the author. Formatted with Booksane.</p>
  </div>
</body>
</html>`;
}

function buildDedicationXHTML(bookData: BookData, template: Template): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Dedication</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
  <style>
    .dedication-page { display:flex; align-items:center; justify-content:center; min-height:80vh; text-align:center; padding:2em; }
    .dedication-text { font-family:${template.bodyFont}; font-style:italic; font-size:1.05em; line-height:1.8; color:${template.inkColor}; max-width:30em; }
  </style>
</head>
<body>
  <div class="dedication-page">
    <p class="dedication-text">${escapeXml(bookData.dedication || '')}</p>
  </div>
</body>
</html>`;
}

function buildEpigraphXHTML(bookData: BookData, template: Template): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Epigraph</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
  <style>
    .epigraph-page { display:flex; align-items:center; justify-content:center; min-height:80vh; padding:2em; }
    .epigraph-inner { max-width:28em; }
    .epigraph-quote { font-family:${template.bodyFont}; font-style:italic; font-size:1em; line-height:1.8; color:${template.inkColor}; margin-bottom:0.5em; }
    .epigraph-attr { font-family:${template.bodyFont}; font-size:0.85em; color:${template.accentColor}; text-align:right; }
  </style>
</head>
<body>
  <div class="epigraph-page">
    <div class="epigraph-inner">
      <p class="epigraph-quote">"${escapeXml(bookData.epigraph || '')}"</p>
      ${bookData.epigraphAttribution ? `<p class="epigraph-attr">— ${escapeXml(bookData.epigraphAttribution)}</p>` : ''}
    </div>
  </div>
</body>
</html>`;
}

function buildSimplePageXHTML(heading: string, content: string, template: Template): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>${escapeXml(heading)}</title>
  <link rel="stylesheet" type="text/css" href="../styles/book.css"/>
</head>
<body>
  <div class="chapter-start">
    <h2 class="chapter-title">${escapeXml(heading)}</h2>
  </div>
  <div class="chapter-body">${content}</div>
</body>
</html>`;
}

function buildNavXHTML(
  bookData: BookData,
  frontMatter: { id: string; filename: string; label: string }[],
  chapters: { id: string; filename: string }[],
  backMatter: { id: string; filename: string; label: string }[]
): string {
  const frontItems = frontMatter
    .map((f) => `    <li><a href="${f.filename}">${escapeXml(f.label)}</a></li>`)
    .join('\n');

  const chapterItems = chapters
    .map(
      (ch, i) =>
        `    <li><a href="${ch.filename}">${escapeXml(bookData.chapters[i].title)}</a></li>`
    )
    .join('\n');

  const backItems = backMatter
    .map((b) => `    <li><a href="${b.filename}">${escapeXml(b.label)}</a></li>`)
    .join('\n');

  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Table of Contents</title>
</head>
<body>
  <nav epub:type="toc" id="toc">
    <h1>Contents</h1>
    <ol>
${frontItems}
${chapterItems}
${backItems}
    </ol>
  </nav>
</body>
</html>`;
}

function buildContentOPF(
  bookData: BookData,
  uid: string,
  allItems: { id: string; filename: string }[],
  _chapters: { id: string; filename: string }[]
): string {
  const now = new Date().toISOString().split('.')[0] + 'Z';

  const manifestItems = [
    `    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>`,
    `    <item id="css" href="styles/book.css" media-type="text/css"/>`,
    ...allItems.map(
      (item) => `    <item id="${item.id}" href="${item.filename}" media-type="application/xhtml+xml"/>`
    ),
    `    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>`,
  ].join('\n');

  const spineItems = allItems
    .map((item) => `    <itemref idref="${item.id}"/>`)
    .join('\n');

  return `<?xml version="1.0" encoding="UTF-8"?>
<package version="3.0" xmlns="http://www.idpf.org/2007/opf" unique-identifier="uid">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:identifier id="uid">${uid}</dc:identifier>
    <dc:title>${escapeXml(bookData.title)}</dc:title>
    <dc:creator>${escapeXml(bookData.author || 'Unknown')}</dc:creator>
    <dc:language>en</dc:language>
    <meta property="dcterms:modified">${now}</meta>
  </metadata>
  <manifest>
${manifestItems}
  </manifest>
  <spine toc="ncx">
    <itemref idref="nav"/>
${spineItems}
  </spine>
</package>`;
}

function buildTocNCX(
  bookData: BookData,
  uid: string,
  chapters: { id: string; filename: string }[]
): string {
  const navPoints = chapters
    .map(
      (ch, i) => `
  <navPoint id="${ch.id}" playOrder="${i + 1}">
    <navLabel><text>${escapeXml(bookData.chapters[i].title)}</text></navLabel>
    <content src="${ch.filename}"/>
  </navPoint>`
    )
    .join('');

  return `<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
  <head>
    <meta name="dtb:uid" content="${uid}"/>
  </head>
  <docTitle><text>${escapeXml(bookData.title)}</text></docTitle>
  <navMap>${navPoints}
  </navMap>
</ncx>`;
}

// ─────────────────────────────────────────
//  UTILITIES
// ─────────────────────────────────────────

function escapeXml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
