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

  // ── Chapter files
  const chapterItems = bookData.chapters.map((ch, i) => ({
    id: `chapter-${i + 1}`,
    filename: `content/chapter-${i + 1}.xhtml`,
  }));

  oebps.folder('content');
  for (let i = 0; i < bookData.chapters.length; i++) {
    const ch = bookData.chapters[i];
    oebps.file(`content/chapter-${i + 1}.xhtml`, buildChapterXHTML(ch, template));
  }

  // ── Nav / TOC
  oebps.file(
    'nav.xhtml',
    buildNavXHTML(bookData, chapterItems)
  );

  // ── content.opf
  oebps.file('content.opf', buildContentOPF(bookData, uid, chapterItems));

  // ── toc.ncx (for backwards compat)
  oebps.file('toc.ncx', buildTocNCX(bookData, uid, chapterItems));

  return zip.generateAsync({ type: 'blob', mimeType: 'application/epub+zip' });
}

// ─────────────────────────────────────────
//  BUILDERS
// ─────────────────────────────────────────

function buildEpubCSS(template: Template): string {
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
h1, h2, h3 {
  font-family: ${template.headingFont};
  font-weight: ${template.headingWeight};
  text-align: ${template.headingAlign};
  text-transform: ${template.headingTransform};
  color: ${template.headingColor};
}
p {
  margin: 0;
  text-indent: ${template.paragraphStyle === 'indent' ? template.textIndent : '0'};
  margin-bottom: ${template.paragraphStyle === 'spaced' ? template.paragraphSpacing : '0'};
}
p + p {
  margin-top: ${template.paragraphStyle === 'indent' ? '0' : template.paragraphSpacing};
}
.chapter-start {
  padding-top: 3em;
}
.chapter-number {
  display: block;
  font-family: ${template.headingFont};
  font-size: 1.5em;
  font-weight: ${template.headingWeight};
  text-align: ${template.headingAlign};
  color: ${template.accentColor};
  margin-bottom: 0.3em;
}
.chapter-title {
  font-family: ${template.headingFont};
  font-size: 1.8em;
  margin-bottom: 2em;
}
.section-break {
  text-align: center;
  margin: 2em 0;
  color: ${template.accentColor};
}
`;
}

function buildChapterXHTML(
  chapter: { id: string; number: number; title: string; content: string },
  template: Template
): string {
  const numDisplay = template.chapterNumberStyle !== 'none'
    ? `<span class="chapter-number">Chapter ${chapter.number}</span>`
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
    ${chapter.content}
  </div>
</body>
</html>`;
}

function buildNavXHTML(
  bookData: BookData,
  chapters: { id: string; filename: string }[]
): string {
  const items = chapters
    .map(
      (ch, i) =>
        `    <li><a href="${ch.filename}">${escapeXml(bookData.chapters[i].title)}</a></li>`
    )
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
${items}
    </ol>
  </nav>
</body>
</html>`;
}

function buildContentOPF(
  bookData: BookData,
  uid: string,
  chapters: { id: string; filename: string }[]
): string {
  const now = new Date().toISOString().split('.')[0] + 'Z';

  const manifestItems = [
    `    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>`,
    `    <item id="css" href="styles/book.css" media-type="text/css"/>`,
    ...chapters.map(
      (ch) => `    <item id="${ch.id}" href="${ch.filename}" media-type="application/xhtml+xml"/>`
    ),
    `    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>`,
  ].join('\n');

  const spineItems = chapters
    .map((ch) => `    <itemref idref="${ch.id}"/>`)
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
