import type { Template } from '@/types';

// ─────────────────────────────────────────
//  10 PROFESSIONAL BOOK TEMPLATES
// ─────────────────────────────────────────

export const templates: Template[] = [
  {
    id: 'classic-novel',
    name: 'Classic Novel',
    category: 'fiction',
    description: 'Traditional typesetting with timeless elegance.',
    previewColor: '#F9F6F1',
    previewAccent: '#2C1810',

    bodyFont: '"EB Garamond", Georgia, serif',
    headingFont: '"EB Garamond", Georgia, serif',
    bodySize: '12.5pt',
    headingSize: '18pt',
    lineHeight: '1.65',

    paragraphStyle: 'indent',
    textIndent: '1.5em',
    paragraphSpacing: '0',

    headingAlign: 'center',
    headingWeight: '400',
    headingTransform: 'none',
    chapterNumberStyle: 'roman',
    chapterNumberSize: '28pt',

    paperColor: '#F9F6F1',
    inkColor: '#1A1208',
    headingColor: '#1A1208',
    accentColor: '#8B5E3C',

    pageMarginH: '0.875in',
    pageMarginV: '0.875in',
    chapterStartMargin: '25%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'modern-fiction',
    name: 'Modern Fiction',
    category: 'fiction',
    description: 'Clean contemporary lines. Crisp and readable.',
    previewColor: '#FFFFFF',
    previewAccent: '#111111',

    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingFont: 'Inter, system-ui, sans-serif',
    bodySize: '11.5pt',
    headingSize: '14pt',
    lineHeight: '1.72',

    paragraphStyle: 'spaced',
    textIndent: '0',
    paragraphSpacing: '0.7em',

    headingAlign: 'left',
    headingWeight: '700',
    headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral',
    chapterNumberSize: '11pt',

    paperColor: '#FFFFFF',
    inkColor: '#111111',
    headingColor: '#111111',
    accentColor: '#333333',

    pageMarginH: '1in',
    pageMarginV: '1in',
    chapterStartMargin: '30%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'outer',
  },

  {
    id: 'business',
    name: 'Business Book',
    category: 'business',
    description: 'Professional and authoritative. Built for impact.',
    previewColor: '#F8F9FA',
    previewAccent: '#0D47A1',

    bodyFont: 'Inter, system-ui, sans-serif',
    headingFont: 'Inter, system-ui, sans-serif',
    bodySize: '11pt',
    headingSize: '16pt',
    lineHeight: '1.68',

    paragraphStyle: 'spaced',
    textIndent: '0',
    paragraphSpacing: '0.8em',

    headingAlign: 'left',
    headingWeight: '700',
    headingTransform: 'none',
    chapterNumberStyle: 'numeral',
    chapterNumberSize: '48pt',

    paperColor: '#F8F9FA',
    inkColor: '#1A1A2E',
    headingColor: '#0D2137',
    accentColor: '#0D47A1',

    pageMarginH: '1in',
    pageMarginV: '1in',
    chapterStartMargin: '20%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'outer',
  },

  {
    id: 'memoir',
    name: 'Memoir',
    category: 'memoir',
    description: 'Warm and personal. Reads like a conversation.',
    previewColor: '#FDF8F2',
    previewAccent: '#5C4033',

    bodyFont: '"Lora", Georgia, serif',
    headingFont: '"Playfair Display", Georgia, serif',
    bodySize: '12pt',
    headingSize: '22pt',
    lineHeight: '1.75',

    paragraphStyle: 'indent',
    textIndent: '1.2em',
    paragraphSpacing: '0',

    headingAlign: 'left',
    headingWeight: '400',
    headingTransform: 'none',
    chapterNumberStyle: 'spelled',
    chapterNumberSize: '11pt',

    paperColor: '#FDF8F2',
    inkColor: '#2D1F17',
    headingColor: '#3C2415',
    accentColor: '#7D4F35',

    pageMarginH: '0.875in',
    pageMarginV: '0.875in',
    chapterStartMargin: '20%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'romance',
    name: 'Romance',
    category: 'fiction',
    description: 'Elegant and inviting. Sets the perfect mood.',
    previewColor: '#FFF5F7',
    previewAccent: '#AD1457',

    bodyFont: '"Lora", Georgia, serif',
    headingFont: '"Playfair Display", Georgia, serif',
    bodySize: '12pt',
    headingSize: '20pt',
    lineHeight: '1.7',

    paragraphStyle: 'indent',
    textIndent: '1.5em',
    paragraphSpacing: '0',

    headingAlign: 'center',
    headingWeight: '400',
    headingTransform: 'none',
    chapterNumberStyle: 'roman',
    chapterNumberSize: '11pt',

    paperColor: '#FFF5F7',
    inkColor: '#1A0A0E',
    headingColor: '#AD1457',
    accentColor: '#D81B60',

    pageMarginH: '0.875in',
    pageMarginV: '0.875in',
    chapterStartMargin: '25%',

    pageSize: 'mass',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'self-help',
    name: 'Self-Help',
    category: 'nonfiction',
    description: 'Bold and actionable. Inspires readers to move.',
    previewColor: '#FFFBEB',
    previewAccent: '#D97706',

    bodyFont: 'Inter, system-ui, sans-serif',
    headingFont: 'Inter, system-ui, sans-serif',
    bodySize: '11.5pt',
    headingSize: '18pt',
    lineHeight: '1.7',

    paragraphStyle: 'spaced',
    textIndent: '0',
    paragraphSpacing: '0.75em',

    headingAlign: 'left',
    headingWeight: '800',
    headingTransform: 'none',
    chapterNumberStyle: 'numeral',
    chapterNumberSize: '11pt',

    paperColor: '#FFFBEB',
    inkColor: '#1C1100',
    headingColor: '#1C1100',
    accentColor: '#D97706',

    pageMarginH: '1in',
    pageMarginV: '1in',
    chapterStartMargin: '20%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'minimalist',
    name: 'Minimalist',
    category: 'nonfiction',
    description: 'Pure whitespace. Let the words breathe.',
    previewColor: '#FFFFFF',
    previewAccent: '#000000',

    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingFont: 'Inter, system-ui, sans-serif',
    bodySize: '12pt',
    headingSize: '13pt',
    lineHeight: '1.9',

    paragraphStyle: 'spaced',
    textIndent: '0',
    paragraphSpacing: '1em',

    headingAlign: 'center',
    headingWeight: '300',
    headingTransform: 'uppercase',
    chapterNumberStyle: 'none',
    chapterNumberSize: '11pt',

    paperColor: '#FFFFFF',
    inkColor: '#000000',
    headingColor: '#000000',
    accentColor: '#000000',

    pageMarginH: '1.25in',
    pageMarginV: '1.25in',
    chapterStartMargin: '35%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'thriller',
    name: 'Thriller',
    category: 'fiction',
    description: 'Dark, gripping tension on every page.',
    previewColor: '#F2F2F2',
    previewAccent: '#C62828',

    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingFont: 'Inter, system-ui, sans-serif',
    bodySize: '11.5pt',
    headingSize: '14pt',
    lineHeight: '1.6',

    paragraphStyle: 'indent',
    textIndent: '1.2em',
    paragraphSpacing: '0',

    headingAlign: 'left',
    headingWeight: '900',
    headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral',
    chapterNumberSize: '36pt',

    paperColor: '#F2F2F2',
    inkColor: '#0A0A0A',
    headingColor: '#0A0A0A',
    accentColor: '#C62828',

    pageMarginH: '0.875in',
    pageMarginV: '0.875in',
    chapterStartMargin: '25%',

    pageSize: 'trade',
    showPageNumbers: true,
    pageNumberAlign: 'outer',
  },

  {
    id: 'poetry',
    name: 'Poetry',
    category: 'poetry',
    description: 'Centered, spacious, and beautifully spare.',
    previewColor: '#F9F8FF',
    previewAccent: '#4527A0',

    bodyFont: '"EB Garamond", Georgia, serif',
    headingFont: '"Playfair Display", Georgia, serif',
    bodySize: '13pt',
    headingSize: '20pt',
    lineHeight: '2',

    paragraphStyle: 'spaced',
    textIndent: '0',
    paragraphSpacing: '1.5em',

    headingAlign: 'center',
    headingWeight: '400',
    headingTransform: 'none',
    chapterNumberStyle: 'roman',
    chapterNumberSize: '11pt',

    paperColor: '#F9F8FF',
    inkColor: '#1A1428',
    headingColor: '#4527A0',
    accentColor: '#4527A0',

    pageMarginH: '1.5in',
    pageMarginV: '1.25in',
    chapterStartMargin: '30%',

    pageSize: 'digest',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },

  {
    id: 'academic',
    name: 'Academic',
    category: 'nonfiction',
    description: 'Rigorous structure. Scholarly and precise.',
    previewColor: '#FAFAFA',
    previewAccent: '#1A237E',

    bodyFont: '"Times New Roman", Times, serif',
    headingFont: '"Times New Roman", Times, serif',
    bodySize: '12pt',
    headingSize: '14pt',
    lineHeight: '1.6',

    paragraphStyle: 'indent',
    textIndent: '0.5in',
    paragraphSpacing: '0',

    headingAlign: 'left',
    headingWeight: '700',
    headingTransform: 'none',
    chapterNumberStyle: 'numeral',
    chapterNumberSize: '12pt',

    paperColor: '#FAFAFA',
    inkColor: '#000000',
    headingColor: '#000000',
    accentColor: '#1A237E',

    pageMarginH: '1in',
    pageMarginV: '1in',
    chapterStartMargin: '15%',

    pageSize: 'a5',
    showPageNumbers: true,
    pageNumberAlign: 'center',
  },
];

export const defaultTemplate = templates[0];

export function getTemplate(id: string): Template {
  return templates.find((t) => t.id === id) ?? defaultTemplate;
}

// ─────────────────────────────────────────
//  RENDER TEMPLATE CSS
// ─────────────────────────────────────────

export function getTemplateCSS(template: Template): string {
  return `
    .book-page {
      background: ${template.paperColor};
      color: ${template.inkColor};
      font-family: ${template.bodyFont};
      font-size: ${template.bodySize};
      line-height: ${template.lineHeight};
      padding: ${template.pageMarginV} ${template.pageMarginH};
    }
    .book-page p {
      margin: 0;
      text-indent: ${template.paragraphStyle === 'indent' ? template.textIndent : '0'};
      margin-bottom: ${template.paragraphStyle === 'spaced' ? template.paragraphSpacing : '0'};
    }
    .book-page p + p {
      margin-top: ${template.paragraphStyle === 'indent' ? '0' : template.paragraphSpacing};
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
    .chapter-number {
      font-family: ${template.headingFont};
      font-size: ${template.chapterNumberSize};
      font-weight: ${template.headingWeight};
      text-align: ${template.headingAlign};
      color: ${template.accentColor};
      margin-bottom: 0.5em;
      display: block;
    }
    .chapter-start {
      padding-top: ${template.chapterStartMargin};
    }
    .section-break {
      text-align: center;
      color: ${template.accentColor};
      margin: 1.5em 0;
      font-size: 1.2em;
      letter-spacing: 0.5em;
    }
    .page-number {
      font-family: ${template.bodyFont};
      font-size: 9pt;
      text-align: ${template.pageNumberAlign === 'center' ? 'center' : 'right'};
      color: ${template.inkColor};
      opacity: 0.5;
    }
  `;
}
