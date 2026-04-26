/** Complete Bible book data for programmatic SEO verse pages */

export interface BibleBook {
  slug: string;       // url-safe: "genesis", "john", "1-corinthians"
  name: string;       // "Genesis", "John", "1 Corinthians"
  abbr: string;       // "Gen", "Jn", "1 Cor"
  testament: "old" | "new";
  chapters: number;
  verses: number[];   // verse count per chapter (index 0 = chapter 1)
}

export const BIBLE_BOOKS: BibleBook[] = [
  // ── OLD TESTAMENT ──────────────────────────────────────────
  { slug:"genesis",         name:"Genesis",         abbr:"Gen",   testament:"old", chapters:50,  verses:[31,25,24,26,32,22,24,22,29,32,32,20,18,24,21,16,27,33,38,18,34,24,20,67,34,35,46,22,35,43,55,32,20,31,29,43,36,30,23,23,57,38,34,34,28,34,31,22,33,26] },
  { slug:"exodus",          name:"Exodus",          abbr:"Ex",    testament:"old", chapters:40,  verses:[22,25,22,31,23,30,25,32,35,29,10,51,22,31,27,36,16,27,25,26,36,31,33,18,40,37,21,43,46,38,18,35,23,35,35,38,29,31,43,38] },
  { slug:"leviticus",       name:"Leviticus",       abbr:"Lev",   testament:"old", chapters:27,  verses:[17,16,17,35,19,30,38,36,24,20,47,8,59,57,33,34,16,30,24,16,34,4,36,28,27,27,34] },
  { slug:"numbers",         name:"Numbers",         abbr:"Num",   testament:"old", chapters:36,  verses:[54,34,51,49,31,27,89,26,23,36,35,16,33,45,41,50,13,32,22,29,35,41,30,25,18,65,23,31,40,16,54,42,56,29,34,13] },
  { slug:"deuteronomy",     name:"Deuteronomy",     abbr:"Deut",  testament:"old", chapters:34,  verses:[46,37,29,49,33,25,26,20,29,22,32,32,18,29,23,22,20,22,21,20,23,30,25,22,19,19,26,68,29,20,30,52,29,12] },
  { slug:"joshua",          name:"Joshua",          abbr:"Josh",  testament:"old", chapters:24,  verses:[18,24,17,24,15,27,26,35,27,43,23,24,33,15,63,10,18,28,51,9,45,34,16,33] },
  { slug:"judges",          name:"Judges",          abbr:"Judg",  testament:"old", chapters:21,  verses:[36,23,31,24,31,40,25,35,57,18,40,15,25,20,20,31,13,31,30,48,25] },
  { slug:"ruth",            name:"Ruth",            abbr:"Ruth",  testament:"old", chapters:4,   verses:[22,23,18,22] },
  { slug:"1-samuel",        name:"1 Samuel",        abbr:"1 Sam", testament:"old", chapters:31,  verses:[28,36,21,22,12,21,17,22,27,27,15,25,23,52,35,23,58,30,24,42,15,23,29,22,44,25,12,25,11,31,13] },
  { slug:"2-samuel",        name:"2 Samuel",        abbr:"2 Sam", testament:"old", chapters:24,  verses:[27,32,39,12,25,23,29,18,13,19,27,31,39,33,37,23,29,33,43,26,22,51,39,25] },
  { slug:"1-kings",         name:"1 Kings",         abbr:"1 Ki",  testament:"old", chapters:22,  verses:[53,46,28,34,18,38,51,66,28,29,43,33,34,31,34,34,24,46,21,43,29,53] },
  { slug:"2-kings",         name:"2 Kings",         abbr:"2 Ki",  testament:"old", chapters:25,  verses:[18,25,27,44,27,33,20,29,37,36,21,21,25,29,38,20,41,37,37,21,26,20,37,20,30] },
  { slug:"1-chronicles",    name:"1 Chronicles",    abbr:"1 Chr", testament:"old", chapters:29,  verses:[54,55,24,43,26,81,40,40,44,14,47,40,14,17,29,43,27,17,19,8,30,19,32,31,31,32,34,21,30] },
  { slug:"2-chronicles",    name:"2 Chronicles",    abbr:"2 Chr", testament:"old", chapters:36,  verses:[17,18,17,22,14,42,22,18,31,19,23,16,22,15,19,14,19,34,11,37,20,12,21,27,28,23,9,27,36,27,21,33,25,33,27,23] },
  { slug:"ezra",            name:"Ezra",            abbr:"Ezra",  testament:"old", chapters:10,  verses:[11,70,13,24,17,22,28,36,15,44] },
  { slug:"nehemiah",        name:"Nehemiah",        abbr:"Neh",   testament:"old", chapters:13,  verses:[11,20,32,23,19,19,73,18,38,39,36,47,31] },
  { slug:"esther",          name:"Esther",          abbr:"Est",   testament:"old", chapters:10,  verses:[22,23,15,17,14,14,10,17,32,3] },
  { slug:"job",             name:"Job",             abbr:"Job",   testament:"old", chapters:42,  verses:[22,13,26,21,27,30,21,22,35,22,20,25,28,22,35,22,16,21,29,29,34,30,17,25,6,14,23,28,25,31,40,22,33,37,16,33,24,41,30,24,34,17] },
  { slug:"psalms",          name:"Psalms",          abbr:"Ps",    testament:"old", chapters:150, verses:[6,12,8,8,12,10,17,9,20,18,7,8,6,7,5,11,15,50,14,9,13,31,6,10,22,12,14,9,11,12,24,11,22,22,28,12,40,22,13,17,13,11,5,20,28,22,35,22,20,43,46,6,9,24,9,27,5,6,16,10,25,12,9,39,5,23,6,14,16,17,14,9,8,50,22,24,14,9,43,26,5,6,26,37,25,23,11,6,10,21,13,23,17,12,16,16,15,13,15,12,11,30,27,28,11,23,4,21,18,17,5,5,12,14,10,5,12,9,17,15,10,11,36,21,3,21,13,14,22,18,28,10,22,12,13,40,7,14,41,31,4,13,7,14,12,16,7,12,3] },
  { slug:"proverbs",        name:"Proverbs",        abbr:"Prov",  testament:"old", chapters:31,  verses:[33,22,35,27,23,35,27,36,18,32,31,28,25,35,33,33,28,24,29,30,31,29,35,34,28,28,27,28,27,33,31] },
  { slug:"ecclesiastes",    name:"Ecclesiastes",    abbr:"Eccl",  testament:"old", chapters:12,  verses:[18,26,22,16,20,12,29,17,18,20,10,14] },
  { slug:"song-of-solomon", name:"Song of Solomon", abbr:"Song",  testament:"old", chapters:8,   verses:[17,17,11,16,16,13,13,14] },
  { slug:"isaiah",          name:"Isaiah",          abbr:"Isa",   testament:"old", chapters:66,  verses:[31,22,26,6,30,13,25,22,21,34,16,6,22,32,9,14,14,7,25,6,17,25,18,23,12,21,13,29,24,33,9,20,24,17,10,22,38,22,8,31,29,25,28,28,25,13,15,22,26,11,23,15,12,17,13,12,21,14,21,22,11,12,19,12,25,24] },
  { slug:"jeremiah",        name:"Jeremiah",        abbr:"Jer",   testament:"old", chapters:52,  verses:[19,37,25,31,31,30,34,22,26,25,23,17,27,22,21,21,27,23,15,18,14,30,40,10,38,24,22,17,32,24,40,44,26,22,19,32,21,28,18,16,18,22,13,30,5,28,7,47,39,46,64,34] },
  { slug:"lamentations",    name:"Lamentations",    abbr:"Lam",   testament:"old", chapters:5,   verses:[22,22,66,22,22] },
  { slug:"ezekiel",         name:"Ezekiel",         abbr:"Ezek",  testament:"old", chapters:48,  verses:[28,10,27,21,17,17,14,20,28,22,35,46,33,33,21,31,31,34,20,37,14,14,27,47,19,38,23,22,25,31,35,25,20,24,25,26,14,26,15,20,31,25,16,30,27,25,18,26] },
  { slug:"daniel",          name:"Daniel",          abbr:"Dan",   testament:"old", chapters:12,  verses:[21,49,30,37,31,28,28,27,27,21,45,13] },
  { slug:"hosea",           name:"Hosea",           abbr:"Hos",   testament:"old", chapters:14,  verses:[11,23,5,19,15,11,16,14,17,15,12,14,16,9] },
  { slug:"joel",            name:"Joel",            abbr:"Joel",  testament:"old", chapters:3,   verses:[20,32,21] },
  { slug:"amos",            name:"Amos",            abbr:"Amos",  testament:"old", chapters:9,   verses:[15,16,15,13,27,14,17,14,15] },
  { slug:"obadiah",         name:"Obadiah",         abbr:"Obad",  testament:"old", chapters:1,   verses:[21] },
  { slug:"jonah",           name:"Jonah",           abbr:"Jon",   testament:"old", chapters:4,   verses:[17,10,10,11] },
  { slug:"micah",           name:"Micah",           abbr:"Mic",   testament:"old", chapters:7,   verses:[16,13,12,13,15,16,20] },
  { slug:"nahum",           name:"Nahum",           abbr:"Nah",   testament:"old", chapters:3,   verses:[15,13,19] },
  { slug:"habakkuk",        name:"Habakkuk",        abbr:"Hab",   testament:"old", chapters:3,   verses:[17,20,19] },
  { slug:"zephaniah",       name:"Zephaniah",       abbr:"Zeph",  testament:"old", chapters:3,   verses:[18,15,20] },
  { slug:"haggai",          name:"Haggai",          abbr:"Hag",   testament:"old", chapters:2,   verses:[15,23] },
  { slug:"zechariah",       name:"Zechariah",       abbr:"Zech",  testament:"old", chapters:14,  verses:[21,13,10,14,11,15,14,23,17,12,17,14,9,21] },
  { slug:"malachi",         name:"Malachi",         abbr:"Mal",   testament:"old", chapters:4,   verses:[14,17,18,6] },

  // ── NEW TESTAMENT ──────────────────────────────────────────
  { slug:"matthew",         name:"Matthew",         abbr:"Matt",  testament:"new", chapters:28,  verses:[25,23,17,25,48,34,29,34,38,42,30,50,58,36,39,28,27,35,30,34,46,46,39,51,46,75,66,20] },
  { slug:"mark",            name:"Mark",            abbr:"Mk",    testament:"new", chapters:16,  verses:[45,28,35,41,43,56,37,38,50,52,33,44,37,72,47,20] },
  { slug:"luke",            name:"Luke",            abbr:"Lk",    testament:"new", chapters:24,  verses:[80,52,38,44,39,49,50,56,62,42,54,59,35,35,32,31,37,43,48,47,38,71,56,53] },
  { slug:"john",            name:"John",            abbr:"Jn",    testament:"new", chapters:21,  verses:[51,25,36,54,47,71,53,59,41,42,57,50,38,31,27,33,26,40,42,31,25] },
  { slug:"acts",            name:"Acts",            abbr:"Acts",  testament:"new", chapters:28,  verses:[26,47,26,37,20,32,36,59,38,24,55,34,28,30,36,21,34,36,47,30,59,40,31,31,31,38,50,28] },
  { slug:"romans",          name:"Romans",          abbr:"Rom",   testament:"new", chapters:16,  verses:[32,29,31,25,21,23,25,39,33,21,36,21,14,26,33,24] },
  { slug:"1-corinthians",   name:"1 Corinthians",   abbr:"1 Cor", testament:"new", chapters:16,  verses:[31,16,23,21,13,20,40,13,27,33,34,31,13,40,58,24] },
  { slug:"2-corinthians",   name:"2 Corinthians",   abbr:"2 Cor", testament:"new", chapters:13,  verses:[24,17,18,18,21,18,16,24,15,18,33,21,14] },
  { slug:"galatians",       name:"Galatians",       abbr:"Gal",   testament:"new", chapters:6,   verses:[24,21,29,31,26,18] },
  { slug:"ephesians",       name:"Ephesians",       abbr:"Eph",   testament:"new", chapters:6,   verses:[23,22,21,28,23,22] },
  { slug:"philippians",     name:"Philippians",     abbr:"Phil",  testament:"new", chapters:4,   verses:[30,23,21,23] },
  { slug:"colossians",      name:"Colossians",      abbr:"Col",   testament:"new", chapters:4,   verses:[29,23,25,18] },
  { slug:"1-thessalonians", name:"1 Thessalonians", abbr:"1 Th",  testament:"new", chapters:5,   verses:[10,20,13,18,28] },
  { slug:"2-thessalonians", name:"2 Thessalonians", abbr:"2 Th",  testament:"new", chapters:3,   verses:[12,17,18] },
  { slug:"1-timothy",       name:"1 Timothy",       abbr:"1 Tim", testament:"new", chapters:6,   verses:[20,15,16,16,25,21] },
  { slug:"2-timothy",       name:"2 Timothy",       abbr:"2 Tim", testament:"new", chapters:4,   verses:[18,26,17,22] },
  { slug:"titus",           name:"Titus",           abbr:"Titus", testament:"new", chapters:3,   verses:[16,15,15] },
  { slug:"philemon",        name:"Philemon",        abbr:"Phlm",  testament:"new", chapters:1,   verses:[25] },
  { slug:"hebrews",         name:"Hebrews",         abbr:"Heb",   testament:"new", chapters:13,  verses:[14,18,19,16,14,20,28,13,28,39,40,29,25] },
  { slug:"james",           name:"James",           abbr:"Jas",   testament:"new", chapters:5,   verses:[27,26,18,17,20] },
  { slug:"1-peter",         name:"1 Peter",         abbr:"1 Pet", testament:"new", chapters:5,   verses:[25,25,22,19,14] },
  { slug:"2-peter",         name:"2 Peter",         abbr:"2 Pet", testament:"new", chapters:3,   verses:[21,22,18] },
  { slug:"1-john",          name:"1 John",          abbr:"1 Jn",  testament:"new", chapters:5,   verses:[10,29,24,21,21] },
  { slug:"2-john",          name:"2 John",          abbr:"2 Jn",  testament:"new", chapters:1,   verses:[13] },
  { slug:"3-john",          name:"3 John",          abbr:"3 Jn",  testament:"new", chapters:1,   verses:[15] },
  { slug:"jude",            name:"Jude",            abbr:"Jude",  testament:"new", chapters:1,   verses:[25] },
  { slug:"revelation",      name:"Revelation",      abbr:"Rev",   testament:"new", chapters:22,  verses:[20,29,22,11,14,17,17,13,21,11,19,17,18,20,8,21,18,24,21,15,27,21] },
];

export const BOOK_MAP = new Map(BIBLE_BOOKS.map((b) => [b.slug, b]));
export const BOOK_MAP_BY_NAME = new Map(BIBLE_BOOKS.map((b) => [b.name.toLowerCase(), b]));

/** Convert a URL ref like "john-3-16" → { book: "John", chapter: 3, verse: 16 } */
export function parseRef(ref: string): { book: BibleBook; chapter: number; verse: number } | null {
  const parts  = ref.split("-");
  if (parts.length < 3) return null;

  const verse   = parseInt(parts[parts.length - 1], 10);
  const chapter = parseInt(parts[parts.length - 2], 10);
  const bookSlug = parts.slice(0, parts.length - 2).join("-");

  const book = BOOK_MAP.get(bookSlug);
  if (!book || isNaN(chapter) || isNaN(verse)) return null;
  if (chapter < 1 || chapter > book.chapters) return null;
  if (verse  < 1 || verse > (book.verses[chapter - 1] ?? 0)) return null;

  return { book, chapter, verse };
}

/** Total Bible verse count: sum of all verse counts */
export const TOTAL_VERSES = BIBLE_BOOKS.reduce(
  (acc, b) => acc + b.verses.reduce((s, v) => s + v, 0),
  0,
);

/** Build all refs for sitemap (returns array of ref strings like "john-3-16") */
export function getAllVerseRefs(): string[] {
  const refs: string[] = [];
  for (const book of BIBLE_BOOKS) {
    for (let c = 1; c <= book.chapters; c++) {
      const vCount = book.verses[c - 1] ?? 0;
      for (let v = 1; v <= vCount; v++) {
        refs.push(`${book.slug}-${c}-${v}`);
      }
    }
  }
  return refs;
}

/** Format "john-3-16" → "John 3:16" */
export function refToDisplay(ref: string): string {
  const parsed = parseRef(ref);
  if (!parsed) return ref;
  return `${parsed.book.name} ${parsed.chapter}:${parsed.verse}`;
}

/** Related prayer slugs for a given book + topic guess */
export function getRelatedPrayers(bookName: string): string[] {
  const map: Record<string, string[]> = {
    Psalms:       ["morning", "night", "anxiety", "depression", "praise", "daily"],
    Proverbs:     ["wisdom", "work", "guidance", "marriage"],
    John:         ["salvation", "faith", "healing", "eternal-life"],
    Romans:       ["salvation", "faith", "purpose", "strength"],
    Philippians:  ["anxiety", "peace", "joy", "gratitude"],
    Isaiah:       ["healing", "strength", "hope", "comfort"],
    Matthew:      ["prayer-for-today", "morning", "faith", "guidance"],
    Revelation:   ["hope", "comfort", "protection"],
  };
  return map[bookName] ?? ["morning", "healing", "strength", "peace"];
}
