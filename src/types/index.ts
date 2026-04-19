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
  extras?: Record<string, string>; // content for any added element by type key
  metadata: {
    wordCount: number;
    estimatedPages: number;
    genre?: string;
  };
}

// Used when creating a new book from scratch
export interface NewBookMeta {
  title: string;
  author: string;
  subtitle?: string;
  genre: Genre;
  dedication?: string;
  epigraph?: string;
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

export type TemplateCategory = 'fiction' | 'nonfiction' | 'business' | 'poetry' | 'memoir' | 'ya' | 'horror' | 'mystery' | 'romance' | 'scifi';
export type HeadingAlign = 'center' | 'left' | 'right';
export type HeadingTransform = 'none' | 'uppercase' | 'capitalize';
export type ChapterNumberStyle = 'numeral' | 'roman' | 'spelled' | 'none';
export type ParagraphStyle = 'indent' | 'spaced';
export type PageSize = 'trade' | 'digest' | 'a5' | 'mass' | '7x10';
// Visual treatment of the chapter heading block in PDF and EPUB
// classic   – centered text, number above title (default)
// ruled     – thin horizontal rules above and below the heading
// large-num – oversized faded chapter number as visual anchor
// badge     – chapter number in a decorative framed box
// stacked   – number + thin rule, then title underneath
// minimal   – tiny all-caps label, no decoration, left-aligned
export type ChapterHeadingStyle = 'classic' | 'ruled' | 'large-num' | 'badge' | 'stacked' | 'minimal';

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

  // Premium features
  dropCap: boolean;                // drop cap on first letter of each chapter
  ornament: string;                // HTML template for section breaks ([ACCENT] placeholder)
  chapterHeadingStyle: ChapterHeadingStyle; // visual treatment of chapter heading block
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

// ─────────────────────────────────────────
//  RECENT BOOK TYPE
// ─────────────────────────────────────────

export interface RecentBook {
  id: string;
  title: string;
  author: string;
  wordCount: number;
  genre: string;
  coverColor: string;
  lastModified: number;
  rawText: string;
}
