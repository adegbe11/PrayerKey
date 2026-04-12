import type { BookData, BookChapter, Genre } from '@/types';

// ─────────────────────────────────────────
//  TEXT FORMATTER ENGINE
//  Converts raw text into structured BookData
// ─────────────────────────────────────────

// Number words for "spelled" chapter number style
const NUMBER_WORDS: Record<number, string> = {
  1: 'One', 2: 'Two', 3: 'Three', 4: 'Four', 5: 'Five',
  6: 'Six', 7: 'Seven', 8: 'Eight', 9: 'Nine', 10: 'Ten',
  11: 'Eleven', 12: 'Twelve', 13: 'Thirteen', 14: 'Fourteen', 15: 'Fifteen',
  16: 'Sixteen', 17: 'Seventeen', 18: 'Eighteen', 19: 'Nineteen', 20: 'Twenty',
};

// Roman numeral converter
function toRoman(num: number): string {
  const vals = [1000,900,500,400,100,90,50,40,10,9,5,4,1];
  const syms = ['M','CM','D','CD','C','XC','L','XL','X','IX','V','IV','I'];
  let result = '';
  let n = num;
  for (let i = 0; i < vals.length; i++) {
    while (n >= vals[i]) { result += syms[i]; n -= vals[i]; }
  }
  return result;
}

export function getChapterNumberDisplay(n: number, style: string): string {
  switch (style) {
    case 'roman': return toRoman(n);
    case 'spelled': return NUMBER_WORDS[n] ?? String(n);
    case 'none': return '';
    default: return String(n);
  }
}

// ─────────────────────────────────────────
//  MAIN FORMATTER
// ─────────────────────────────────────────

export function formatBook(rawText: string): BookData {
  // Step 1: Normalize
  const text = normalizeText(rawText);

  // Step 2: Extract title + author + front/back matter
  const {
    title,
    author,
    dedication,
    epigraph,
    epigraphAttribution,
    acknowledgments,
    aboutAuthor,
    bodyText,
  } = extractFrontMatter(text);

  // Step 3: Split chapters
  const rawChapters = detectAndSplitChapters(bodyText);

  // Step 4: Format each chapter
  const chapters: BookChapter[] = rawChapters.map((ch, i) => {
    const content = formatChapterContent(ch.body);
    const wordCount = countWords(ch.body);
    return {
      id: `chapter-${i + 1}`,
      number: i + 1,
      title: ch.title || `Chapter ${i + 1}`,
      content,
      wordCount,
    };
  });

  const totalWords = chapters.reduce((sum, c) => sum + c.wordCount, 0);

  // Step 5: Detect genre from full text
  const genre = detectGenre(rawText);

  return {
    title: title || 'Untitled',
    author: author || '',
    dedication: dedication || undefined,
    epigraph: epigraph || undefined,
    epigraphAttribution: epigraphAttribution || undefined,
    acknowledgments: acknowledgments || undefined,
    aboutAuthor: aboutAuthor || undefined,
    genre,
    chapters,
    metadata: {
      wordCount: totalWords,
      estimatedPages: Math.ceil(totalWords / 280),
      genre,
    },
  };
}

// ─────────────────────────────────────────
//  NORMALIZE
// ─────────────────────────────────────────

function normalizeText(text: string): string {
  return text
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\t/g, '    ')
    .replace(/[ \xA0]+/g, ' ')          // collapse spaces (incl. nbsp)
    .replace(/\n[ \t]+/g, '\n')         // strip leading whitespace from lines
    .replace(/[ \t]+\n/g, '\n')         // strip trailing whitespace from lines
    .replace(/\n{4,}/g, '\n\n\n')       // max 3 consecutive blank lines
    .replace(/[""]/g, '"')              // smart quotes
    .replace(/['']/g, "'")
    .replace(/—|--|---/g, '—')          // em dash
    .trim();
}

// ─────────────────────────────────────────
//  FRONT MATTER EXTRACTION
// ─────────────────────────────────────────

function extractFrontMatter(text: string): {
  title: string;
  author: string;
  dedication: string;
  epigraph: string;
  epigraphAttribution: string;
  acknowledgments: string;
  aboutAuthor: string;
  bodyText: string;
} {
  const lines = text.split('\n');
  let title = '';
  let author = '';
  let dedication = '';
  let epigraph = '';
  let epigraphAttribution = '';
  let acknowledgments = '';
  let aboutAuthor = '';
  let bodyStart = 0;

  // Find first non-empty line as potential title
  for (let i = 0; i < Math.min(lines.length, 15); i++) {
    const line = lines[i].trim();
    if (!line) continue;

    // Skip if it looks like a chapter heading
    if (/^(chapter|part|prologue|epilogue)\s/i.test(line)) break;
    if (/^\d+\.\s/.test(line)) break;

    // First non-empty, non-chapter line = title
    if (!title && line.length < 120) {
      title = line.replace(/^(title:|book:)\s*/i, '');
      bodyStart = i + 1;
      continue;
    }

    // Look for author (next meaningful line after title)
    if (title && !author && /^(by\s+.+|author:|written by\s+)/i.test(line)) {
      author = line.replace(/^(by\s+|author:\s*|written by\s+)/i, '');
      bodyStart = i + 1;
      continue;
    }

    // Standalone short line after title can be author too
    if (title && !author && line.length < 60 && !/[.?!,]$/.test(line) && !/^(the|a|an|this|that|in|on|at|for)\s/i.test(line)) {
      author = line;
      bodyStart = i + 1;
      continue;
    }

    // Dedication
    if ((title || author) && /^(dedication|to\s+my|for\s+my|dedicated)/i.test(line)) {
      const dedLines: string[] = [line.replace(/^dedication:\s*/i, '')];
      let j = i + 1;
      while (j < lines.length && lines[j].trim() && !isChapterHeading(lines[j])) {
        dedLines.push(lines[j].trim());
        j++;
      }
      dedication = dedLines.join('\n');
      bodyStart = j;
    }

    break;
  }

  // Extract remaining body text for further parsing
  let bodyText = lines.slice(bodyStart).join('\n').trim();

  // --- Extract epigraph from NEAR THE BEGINNING (before first chapter) ---
  // Look for a standalone quote (line starting with ", ', or —) before first chapter heading
  const bodyLines = bodyText.split('\n');
  const firstChapterIdx = bodyLines.findIndex(l => isChapterHeading(l));
  const searchEnd = firstChapterIdx >= 0 ? Math.min(firstChapterIdx, 30) : Math.min(bodyLines.length, 30);

  for (let i = 0; i < searchEnd; i++) {
    const line = bodyLines[i].trim();
    if (!line) continue;
    if (/^["'\u201C\u2018\u2014]/.test(line) && line.length > 15 && line.length < 300) {
      // Possible epigraph
      epigraph = line.replace(/^["'\u201C\u2018]/, '').replace(/["'\u201D\u2019]$/, '').trim();
      // Check next line for attribution (starts with — or - or "by ")
      if (i + 1 < searchEnd) {
        const nextLine = bodyLines[i + 1].trim();
        if (/^[\u2014\-]/.test(nextLine) || /^by\s/i.test(nextLine)) {
          epigraphAttribution = nextLine.replace(/^[\u2014\-]\s*/, '').replace(/^by\s+/i, '').trim();
          // Remove both lines from bodyText
          bodyLines.splice(i, 2);
        } else {
          bodyLines.splice(i, 1);
        }
      } else {
        bodyLines.splice(i, 1);
      }
      bodyText = bodyLines.join('\n').trim();
      break;
    }
  }

  // --- Extract "About the Author" from NEAR THE END ---
  const aboutMatch = extractSectionFromEnd(bodyText, /^(about\s+the\s+author|about\s+the\s+writer)$/i);
  if (aboutMatch) {
    aboutAuthor = aboutMatch.content;
    bodyText = aboutMatch.remaining;
  }

  // --- Extract "Acknowledgments" from NEAR THE END ---
  const ackMatch = extractSectionFromEnd(bodyText, /^(acknowledgments?|acknowledgements?)$/i);
  if (ackMatch) {
    acknowledgments = ackMatch.content;
    bodyText = ackMatch.remaining;
  }

  return {
    title,
    author,
    dedication,
    epigraph,
    epigraphAttribution,
    acknowledgments,
    aboutAuthor,
    bodyText,
  };
}

/**
 * Finds a section heading near the END of the text, extracts the paragraphs
 * after the heading, and returns both the extracted content and remaining text.
 */
function extractSectionFromEnd(
  text: string,
  pattern: RegExp
): { content: string; remaining: string } | null {
  const lines = text.split('\n');
  // Search last 200 lines for the heading
  const searchStart = Math.max(0, lines.length - 200);

  for (let i = lines.length - 1; i >= searchStart; i--) {
    const line = lines[i].trim();
    if (pattern.test(line)) {
      // Collect content after the heading
      const contentLines: string[] = [];
      for (let j = i + 1; j < lines.length; j++) {
        contentLines.push(lines[j]);
      }
      const content = contentLines.join('\n').trim();
      if (!content) return null;

      // Return remaining text without this section (include one blank line before heading for cleanliness)
      const remaining = lines.slice(0, i).join('\n').trim();
      return { content, remaining };
    }
  }
  return null;
}

// ─────────────────────────────────────────
//  CHAPTER DETECTION
// ─────────────────────────────────────────

const CHAPTER_PATTERNS = [
  /^chapter\s+\d+\b/i,
  /^chapter\s+[ivxlcdm]+\b/i,
  /^chapter\s+(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty)\b/i,
  /^part\s+\d+\b/i,
  /^part\s+[ivxlcdm]+\b/i,
  /^(prologue|epilogue|preface|foreword|introduction|afterword|author's note|acknowledgments?|conclusion)$/i,
];

function isChapterHeading(line: string): boolean {
  const trimmed = line.trim();
  return CHAPTER_PATTERNS.some((p) => p.test(trimmed));
}

function detectAndSplitChapters(text: string): { title: string; body: string }[] {
  const lines = text.split('\n');
  const splits: { lineIdx: number; heading: string }[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;

    // Explicit chapter heading
    if (isChapterHeading(line)) {
      // The chapter title might continue on the next non-empty line
      let heading = line;
      if (i + 1 < lines.length) {
        const nextLine = lines[i + 1].trim();
        // If next line is short and not another chapter heading, it's the title
        if (nextLine && nextLine.length < 80 && !isChapterHeading(nextLine) && !nextLine.match(/^[a-z]/)) {
          heading = `${line}\n${nextLine}`;
          i++; // skip the title line
        }
      }
      splits.push({ lineIdx: i, heading });
      continue;
    }

    // Detect standalone number at start of line (like "1" or "I" by itself)
    if (/^[IVX]+$/.test(line) || /^\d+$/.test(line)) {
      const prevEmpty = i === 0 || !lines[i - 1].trim();
      const nextEmpty = i + 1 >= lines.length || !lines[i + 1].trim();
      if (prevEmpty || nextEmpty) {
        splits.push({ lineIdx: i, heading: line });
      }
    }
  }

  if (splits.length === 0) {
    // No chapters detected — treat entire text as one chapter
    return [{ title: 'Chapter One', body: text }];
  }

  const chapters: { title: string; body: string }[] = [];

  // Text before first chapter
  if (splits[0].lineIdx > 0) {
    const preText = lines.slice(0, splits[0].lineIdx).join('\n').trim();
    if (preText) {
      chapters.push({ title: 'Prologue', body: preText });
    }
  }

  for (let i = 0; i < splits.length; i++) {
    const start = splits[i].lineIdx + 1;
    const end = i + 1 < splits.length ? splits[i + 1].lineIdx : lines.length;
    const body = lines.slice(start, end).join('\n').trim();
    chapters.push({ title: splits[i].heading, body });
  }

  return chapters.filter((c) => c.body.trim().length > 0);
}

// ─────────────────────────────────────────
//  CONTENT FORMATTER (text → HTML)
// ─────────────────────────────────────────

function formatChapterContent(text: string): string {
  if (!text.trim()) return '';

  const paragraphs = text.split(/\n{2,}/);
  const htmlParts: string[] = [];

  for (const para of paragraphs) {
    const trimmed = para.trim();
    if (!trimmed) continue;

    // Section break
    if (/^(\*\s*){2,}$/.test(trimmed) || /^(-\s*){3,}$/.test(trimmed) || /^(~\s*){3,}$/.test(trimmed) || trimmed === '* * *' || trimmed === '---') {
      htmlParts.push('<p class="section-break">✦ ✦ ✦</p>');
      continue;
    }

    // Multi-line paragraph — normalize internal newlines
    const lines = trimmed.split('\n').map((l) => l.trim()).filter(Boolean);
    const joined = lines.join(' ');

    // Apply inline formatting
    const formatted = applyInlineFormatting(joined);
    htmlParts.push(`<p>${formatted}</p>`);
  }

  return htmlParts.join('\n');
}

function applyInlineFormatting(text: string): string {
  return text
    // Bold: **text** or __text__
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/__(.+?)__/g, '<strong>$1</strong>')
    // Italic: *text* or _text_
    .replace(/\*([^*]+?)\*/g, '<em>$1</em>')
    .replace(/_([^_]+?)_/g, '<em>$1</em>')
    // En dash
    .replace(/ - /g, ' – ')
    // Ellipsis
    .replace(/\.\.\./g, '…');
}

// ─────────────────────────────────────────
//  UTILITIES
// ─────────────────────────────────────────

export function countWords(text: string): number {
  return text.trim().split(/\s+/).filter(Boolean).length;
}

export function estimateReadingTime(wordCount: number): string {
  const minutes = Math.ceil(wordCount / 250);
  if (minutes < 60) return `${minutes} min read`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}m read`;
}

// ─────────────────────────────────────────
//  GENRE DETECTION
// ─────────────────────────────────────────

export function detectGenre(text: string): Genre {
  const lower = text.toLowerCase();
  const scores: Partial<Record<Genre, number>> = {};

  // Religious scoring (high weight for specific terms)
  const religiousTerms = ['prayer', 'scripture', 'god', 'jesus', 'holy spirit', 'spiritual', 'faith', 'bible', 'psalm', 'lord', 'salvation', 'deliverance', 'intercession', 'worship', 'prophetic', 'anointing', 'breakthrough', 'warfare', 'blessing', 'divine', 'heaven', 'devil', 'demon', 'angel', 'church', 'ministry'];
  scores.religious = religiousTerms.reduce((sum, t) => sum + (lower.split(t).length - 1) * (t.length > 6 ? 3 : 1), 0);

  // Romance
  const romanceTerms = ['love', 'heart', 'romance', 'passion', 'desire', 'kiss', 'darling', 'beloved', 'embrace', 'sweep', 'attraction', 'fell for'];
  scores.romance = romanceTerms.reduce((sum, t) => sum + (lower.split(t).length - 1), 0);

  // Thriller/Mystery
  const thrillerTerms = ['murder', 'detective', 'killer', 'blood', 'crime', 'police', 'weapon', 'suspect', 'threat', 'danger', 'dead body', 'investigation'];
  scores.thriller = thrillerTerms.reduce((sum, t) => sum + (lower.split(t).length - 1) * 2, 0);

  // Business
  const businessTerms = ['strategy', 'leadership', 'business', 'revenue', 'profit', 'startup', 'entrepreneur', 'management', 'growth', 'team', 'market', 'brand', 'success', 'productivity'];
  scores.business = businessTerms.reduce((sum, t) => sum + (lower.split(t).length - 1), 0);

  // Self-Help
  const selfhelpTerms = ['habit', 'mindset', 'goal', 'success', 'overcome', 'mindfulness', 'self-improvement', 'potential', 'journey', 'transform', 'principles', 'steps to'];
  scores.selfhelp = selfhelpTerms.reduce((sum, t) => sum + (lower.split(t).length - 1), 0);

  // Memoir
  const memoirTerms = ['i was born', 'i remember', 'my father', 'my mother', 'growing up', 'childhood', 'years ago', 'looking back', 'i never forgot'];
  scores.memoir = memoirTerms.reduce((sum, t) => sum + (lower.split(t).length - 1) * 3, 0);

  // Poetry (structural detection)
  const lines = text.split('\n').filter(l => l.trim());
  const shortLineRatio = lines.filter(l => l.trim().length < 50).length / Math.max(lines.length, 1);
  if (shortLineRatio > 0.7 && !text.toLowerCase().includes('chapter')) scores.poetry = 20;

  // SciFi/Fantasy
  const scifiTerms = ['galaxy', 'spaceship', 'planet', 'alien', 'robot', 'future', 'wizard', 'magic', 'dragon', 'kingdom', 'spell', 'portal', 'dimension'];
  scores.scifi = scifiTerms.reduce((sum, t) => sum + (lower.split(t).length - 1) * 2, 0);

  // Academic
  const academicTerms = ['research', 'hypothesis', 'methodology', 'analysis', 'citation', 'bibliography', 'abstract', 'conclusion', 'findings', 'literature review'];
  scores.academic = academicTerms.reduce((sum, t) => sum + (lower.split(t).length - 1) * 2, 0);

  const sorted = Object.entries(scores).sort((a, b) => (b[1] as number) - (a[1] as number));
  const top = sorted[0];
  if (!top || (top[1] as number) < 2) return 'fiction';
  return top[0] as Genre;
}

// ─────────────────────────────────────────
//  DEMO TEXT (for first-load state)
// ─────────────────────────────────────────

export const DEMO_TEXT = `The Last Lighthouse

by Eleanor Marsh

Chapter One

The storm had been building for three days before it finally broke.

Mara stood at the window of the lighthouse, watching the gray Atlantic churn beneath a bruised sky. The keeper's cottage smelled of salt and old wood and the particular loneliness that collects in places where people have waited a long time for something.

She had come here to write. That was what she told herself, and what she told her editor, and what she told the friends who worried. But the truth—the honest, embarrassing truth—was that she had come here to disappear for a while.

The notebook on her desk was still blank.

Chapter Two

Three miles offshore, the fishing boat named Constance was in trouble.

Captain Reid Harmon had seen worse storms in his forty years on the water, but not many. The swells had climbed past fifteen feet sometime around midnight, and now the engine sputtered in a way that made his chest feel hollow.

He thought about the light from the lighthouse. About how it had always meant safety. About how strange it was that a single beam of light could mean so much to someone lost at sea.

He reached for the radio.

Chapter Three

The call came through at 2:47 in the morning.

Mara almost didn't answer. She had been lying awake in the narrow keeper's bed, listening to the storm attack the walls of the cottage, her notebook still blank on the desk across the room.

But something made her reach for the radio. Some instinct older than thought.

The voice on the other end was calm, the way people get calm when they're very afraid.

"This is the fishing vessel Constance. We are taking on water approximately three miles west-southwest of the Pelican Point light. I have two crew aboard. We need assistance."

Mara looked out the window at the screaming dark.

She reached for her jacket.`;
