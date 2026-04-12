// ─────────────────────────────────────────
//  BOOK DATA TYPES
// ─────────────────────────────────────────

export type Genre =
  | 'religious' | 'romance' | 'thriller' | 'mystery'
  | 'scifi' | 'fantasy' | 'memoir' | 'biography'
  | 'business' | 'selfhelp' | 'poetry' | 'academic'
  | 'childrens' | 'fiction';

export interface CoverConfig {
  type: 'template' | 'uploaded';
  templateId: string;
  uploadedUrl?: string;
}

export interface BookChapter {
  id: string;
  number: number;
  title: string;
  content: string; // HTML
  wordCount: number;
}

export interface BookData {
  title: string;
  author: string;
  subtitle?: string;
  dedication?: string;
  genre?: Genre;
  epigraph?: string;
  epigraphAttribution?: string;
  acknowledgments?: string;
  aboutAuthor?: string;
  chapters: BookChapter[];
  metadata: {
    wordCount: number;
    estimatedPages: number;
    genre?: string;
  };
}

export interface BookPage {
  pageNumber: number;
  content: string; // HTML
  isChapterStart?: boolean;
  chapterId?: string;
  isCover?: boolean;
  isToc?: boolean;
  isDedication?: boolean;
  isBlank?: boolean;
}

// ─────────────────────────────────────────
//  TEMPLATE TYPES
// ─────────────────────────────────────────

export type TemplateCategory = 'fiction' | 'nonfiction' | 'business' | 'poetry' | 'memoir';
export type HeadingAlign = 'center' | 'left' | 'right';
export type HeadingTransform = 'none' | 'uppercase' | 'capitalize';
export type ChapterNumberStyle = 'numeral' | 'roman' | 'spelled' | 'none';
export type ParagraphStyle = 'indent' | 'spaced';
export type PageSize = 'trade' | 'digest' | 'a5' | 'mass';

export interface Template {
  id: string;
  name: string;
  category: TemplateCategory;
  description: string;
  previewColor: string; // background color for thumbnail
  previewAccent: string; // accent color for thumbnail

  // Typography
  bodyFont: string;
  headingFont: string;
  bodySize: string;
  headingSize: string;
  lineHeight: string;

  // Paragraph style
  paragraphStyle: ParagraphStyle;
  textIndent: string;
  paragraphSpacing: string;

  // Chapter headings
  headingAlign: HeadingAlign;
  headingWeight: string;
  headingTransform: HeadingTransform;
  chapterNumberStyle: ChapterNumberStyle;
  chapterNumberSize: string;

  // Colors
  paperColor: string;
  inkColor: string;
  headingColor: string;
  accentColor: string;

  // Spacing
  pageMarginH: string;
  pageMarginV: string;
  chapterStartMargin: string; // how far down chapter starts

  // Page
  pageSize: PageSize;
  showPageNumbers: boolean;
  pageNumberAlign: 'center' | 'outer';
}

// ─────────────────────────────────────────
//  EDITOR TYPES
// ─────────────────────────────────────────

export type PreviewMode = 'paperback' | 'ebook';

export interface EditorState {
  rawText: string;
  bookData: BookData | null;
  selectedTemplateId: string;
  previewMode: PreviewMode;
  currentSpread: number;
  isProcessing: boolean;
  showExportModal: boolean;
  ebookFontSize: number;
  leftTab: 'input' | 'chapters';
}

export type TrimSize = '5x8' | '5.5x8.5' | '6x9' | 'a5' | '8.5x11';
