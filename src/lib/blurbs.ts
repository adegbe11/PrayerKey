import type { BookData, Genre } from '@/types';

/**
 * Pure-heuristic back-cover blurb & tagline generator.
 * No API calls. Extracts themes + key sentences from the manuscript
 * and assembles them into copy shaped like a real publisher blurb.
 */

const GENRE_HOOKS: Record<Genre, string[]> = {
  religious:  ['A journey of faith', 'A call to prayer', 'A spiritual awakening'],
  romance:    ['Two hearts collide', 'A love like no other', 'When hearts meet'],
  thriller:   ['Time is running out', 'Everyone is a suspect', 'The clock is ticking'],
  mystery:    ['Some questions demand an answer', 'The truth lies buried', 'Not everything is as it seems'],
  scifi:      ['In a future not so far away', 'The stars hold a secret', 'Tomorrow begins today'],
  fantasy:    ['A world unlike any other', 'Magic has a price', 'The old legends are true'],
  memoir:     ['A life, honestly told', 'The story she had to tell', 'Looking back with open eyes'],
  biography:  ['The definitive story', 'One life, one legacy', 'Behind the legend'],
  business:   ['A framework that works', 'The playbook for the next decade', 'From insight to impact'],
  selfhelp:   ['Small shifts, big change', 'A map for what comes next', 'The change starts here'],
  poetry:     ['Words to return to', 'Verses carved in quiet', 'A voice worth listening to'],
  academic:   ['A rigorous examination', 'The seminal work', 'Research that reshapes the field'],
  childrens:  ['An adventure for young readers', 'A story to read again and again', 'A bedtime favorite'],
  fiction:    ['A story that lingers', 'An unforgettable read', 'Fiction at its finest'],
};

const GENRE_CLOSERS: Record<Genre, string[]> = {
  religious:  ['Will you answer the call?', 'Faith finds you where you are.', 'The journey begins with a single prayer.'],
  romance:    ['Some loves change everything.', 'The heart knows what the mind cannot explain.', 'Will they find their way back?'],
  thriller:   ['Nothing will prepare you for the ending.', 'The truth will cost everything.', 'No one gets out clean.'],
  mystery:    ['The answers are closer than you think.', 'Every clue matters.', 'Justice has a long memory.'],
  scifi:      ['The future is already here.', 'Some questions only the stars can answer.', 'Progress has a cost.'],
  fantasy:    ['The legend was only the beginning.', 'Every myth has a truth.', 'The old powers are stirring.'],
  memoir:     ['This is a life, unflinchingly lived.', 'A story only she could tell.', 'Honest, raw, and unforgettable.'],
  biography:  ['A portrait worth knowing.', 'A life that changed a generation.', 'The definitive account.'],
  business:   ['Put these ideas to work today.', 'The next edge starts here.', 'Read it. Apply it. Win.'],
  selfhelp:   ['Your next chapter starts here.', 'Small change. Real results.', 'Step forward. One page at a time.'],
  poetry:     ['Sit with it slowly.', 'Read it aloud.', 'Some books you finish. This one stays with you.'],
  academic:   ['Essential reading for serious scholars.', 'A study to be reckoned with.', 'A landmark contribution.'],
  childrens:  ['The kind of book kids ask for again.', 'A story for the whole family.', 'Perfect for young readers.'],
  fiction:    ['A debut worth reading.', 'A story you will not forget.', 'Fiction that stays with you.'],
};

function topKeywords(text: string, n = 5): string[] {
  const stop = new Set<string>([
    'the','a','an','and','or','but','of','to','in','on','at','for','by','with','as','is','was','were','be','been','being',
    'this','that','these','those','there','here','not','no','so','if','then','than','from','it','its','it\u2019s','he','she',
    'him','her','his','hers','they','them','their','theirs','we','us','our','ours','you','your','yours','my','mine','i',
    'me','had','has','have','will','would','could','should','can','may','might','just','like','only','also','very','too',
    'more','most','much','many','some','any','all','one','two','three','what','when','where','who','why','how','do','did','does',
    'because','about','into','over','under','before','after','again','still','yet','up','down','off','out','which','chapter',
  ]);

  const counts = new Map<string, number>();
  const words = text
    .toLowerCase()
    .replace(/[^a-z\u2019'\s]/g, ' ')
    .split(/\s+/)
    .filter((w) => w.length > 3 && !stop.has(w));
  for (const w of words) counts.set(w, (counts.get(w) ?? 0) + 1);

  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, n)
    .map(([w]) => w);
}

function firstMeaningfulSentence(text: string): string {
  const stripped = text.replace(/\s+/g, ' ').trim();
  const match = stripped.match(/^([^.!?]{18,180}[.!?])/);
  return match ? match[1].trim() : stripped.slice(0, 160).trim();
}

function cap(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

function pick<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)];
}

export interface GeneratedBlurb {
  tagline: string;
  blurb: string;
  subtitleSuggestion: string;
}

export function generateBlurb(bookData: BookData): GeneratedBlurb {
  const genre = (bookData.genre || bookData.metadata.genre || 'fiction') as Genre;

  const firstChapter = bookData.chapters[0];
  const plain = firstChapter
    ? firstChapter.content.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim()
    : '';

  const hook = pick(GENRE_HOOKS[genre] || GENRE_HOOKS.fiction);
  const closer = pick(GENRE_CLOSERS[genre] || GENRE_CLOSERS.fiction);
  const opening = plain ? firstMeaningfulSentence(plain) : '';
  const kws = topKeywords(plain || bookData.title, 5);

  const tagline = hook + '.';
  const bridge = kws.length >= 2
    ? `A story of ${cap(kws[0])} and ${cap(kws[1])}${kws[2] ? `, where ${kws[2]}` : ''} meet${kws[2] ? 's' : ''} something unexpected.`
    : 'A story that refuses to be forgotten.';

  const blurb = [
    `${hook}. ${opening}`.trim().replace(/\.\./g, '.'),
    '',
    bridge,
    '',
    closer,
  ].join('\n');

  const subtitleSuggestion = kws.length >= 2
    ? `${cap(kws[0])} of ${cap(kws[1])}`
    : 'A Story of Becoming';

  return { tagline, blurb, subtitleSuggestion };
}
