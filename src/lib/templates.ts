import type { Template } from '@/types';

// ─────────────────────────────────────────
//  30 PROFESSIONAL BOOK TEMPLATES
//  Named after legendary publishers.
//  Beats Vellum (36 styles) and Atticus (20)
//  in variety, beauty, and genre coverage.
//
//  chapterHeadingStyle values:
//   classic   – centered text, number above title
//   ruled     – thin rules above and below heading
//   large-num – oversized faded number as anchor
//   badge     – number in decorative framed box
//   stacked   – number + thin rule, title below
//   minimal   – tiny all-caps label, no decoration
// ─────────────────────────────────────────

export const templates: Template[] = [

  // ══════════════════════════════════════
  //  LITERARY FICTION (8)
  // ══════════════════════════════════════

  // ── 1. KNOPF ─────────────────────────
  {
    id: 'knopf', name: 'Knopf', category: 'fiction',
    description: 'Literary gold standard. Cormorant Garamond meets EB Garamond on warm cream.',
    previewColor: '#FAF7F2', previewAccent: '#8B6914',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Cormorant Garamond', 'Playfair Display', Georgia, serif",
    bodySize: '11.5pt', headingSize: '22pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'spelled', chapterNumberSize: '12pt',
    paperColor: '#FAF7F2', inkColor: '#1A1208', headingColor: '#1A1208', accentColor: '#8B6914',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:1.1em;line-height:1;">❧</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 2. SCRIBNER ──────────────────────
  {
    id: 'scribner', name: 'Scribner', category: 'fiction',
    description: 'Warm authority. Playfair Display headings over a Lora body on ivory paper.',
    previewColor: '#FDFBF7', previewAccent: '#2C4A2E',
    bodyFont: "'Lora', Georgia, serif",
    headingFont: "'Playfair Display', Georgia, serif",
    bodySize: '11pt', headingSize: '20pt', lineHeight: '1.7',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#FDFBF7', inkColor: '#1C150E', headingColor: '#1C150E', accentColor: '#2C4A2E',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:0.7em;letter-spacing:0.5em;">✦ &nbsp; ✦ &nbsp; ✦</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 3. FARRAR (FSG) ──────────────────
  {
    id: 'farrar', name: 'Farrar', category: 'fiction',
    description: 'FSG literary minimalism. EB Garamond body, Josefin Sans headings, cool white.',
    previewColor: '#FFFFFF', previewAccent: '#555555',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Josefin Sans', sans-serif",
    bodySize: '11pt', headingSize: '11pt', lineHeight: '1.75',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'left', headingWeight: '300', headingTransform: 'uppercase',
    chapterNumberStyle: 'spelled', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#151515', headingColor: '#151515', accentColor: '#555555',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '30%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:1em 0;display:flex;align-items:center;"><div style="width:28px;height:1px;background:[ACCENT];opacity:0.5;"></div></div>`,
    chapterHeadingStyle: 'minimal',
  },

  // ── 4. GROVE (Grove Press) ────────────
  {
    id: 'grove', name: 'Grove', category: 'fiction',
    description: 'Counter-cultural literary. Cormorant Garamond throughout, understated elegance.',
    previewColor: '#FDFDFD', previewAccent: '#666666',
    bodyFont: "'Cormorant Garamond', Georgia, serif",
    headingFont: "'Cormorant Garamond', Georgia, serif",
    bodySize: '12pt', headingSize: '20pt', lineHeight: '1.78',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#FDFDFD', inkColor: '#111111', headingColor: '#111111', accentColor: '#666666',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:1em;line-height:1;font-family:'Cormorant Garamond',serif;font-style:italic;">* * *</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 5. PARCHMENT (Historical) ─────────
  {
    id: 'parchment', name: 'Parchment', category: 'fiction',
    description: 'Historical fiction warmth. EB Garamond on aged cream, sepia tones.',
    previewColor: '#F5F0E0', previewAccent: '#7A5C2C',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'EB Garamond', Georgia, serif",
    bodySize: '11.5pt', headingSize: '20pt', lineHeight: '1.68',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#F5F0E0', inkColor: '#2A1F0F', headingColor: '#2A1F0F', accentColor: '#7A5C2C',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:1.2em;letter-spacing:0.3em;opacity:0.75;">⁂</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 6. ARCHIPELAGO (Literary Translation) ──
  {
    id: 'archipelago', name: 'Archipelago', category: 'fiction',
    description: 'Literary translation elegance. Cormorant Garamond with ruled headings.',
    previewColor: '#FAFAFA', previewAccent: '#555555',
    bodyFont: "'Cormorant Garamond', Georgia, serif",
    headingFont: "'Cormorant Garamond', Georgia, serif",
    bodySize: '11.5pt', headingSize: '18pt', lineHeight: '1.78',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.5em',
    headingAlign: 'left', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FAFAFA', inkColor: '#1A1A1A', headingColor: '#1A1A1A', accentColor: '#555555',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '28%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:1em 0;display:flex;align-items:center;gap:8px;"><div style="flex:1;height:0.5px;background:[ACCENT];opacity:0.4;"></div></div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 7. McSWEENEY'S (Indie Literary) ───
  {
    id: 'mcsweeneys', name: "McSweeney's", category: 'fiction',
    description: 'Indie literary distinctiveness. Josefin Sans titles, Libre Baskerville body.',
    previewColor: '#FFFEF9', previewAccent: '#444444',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Josefin Sans', sans-serif",
    bodySize: '11pt', headingSize: '13pt', lineHeight: '1.7',
    paragraphStyle: 'indent', textIndent: '1.3em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFEF9', inkColor: '#111111', headingColor: '#111111', accentColor: '#444444',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '30%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:0.6em;letter-spacing:0.5em;font-family:'Josefin Sans',sans-serif;">— — —</div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ── 8. GRAYWOLF (Poetry/Literary) ─────
  {
    id: 'graywolf', name: 'Graywolf', category: 'poetry',
    description: 'Poetry and literary prose. Ultra-minimal, Josefin Sans, maximum whitespace.',
    previewColor: '#FFFFFF', previewAccent: '#888888',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Josefin Sans', sans-serif",
    bodySize: '11pt', headingSize: '10pt', lineHeight: '1.85',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.6em',
    headingAlign: 'left', headingWeight: '300', headingTransform: 'uppercase',
    chapterNumberStyle: 'none', chapterNumberSize: '10pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#888888',
    pageMarginH: '1.125in', pageMarginV: '1.125in', chapterStartMargin: '25%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.8em 0;"><div style="width:20px;height:0.5px;background:[ACCENT];"></div></div>`,
    chapterHeadingStyle: 'minimal',
  },

  // ══════════════════════════════════════
  //  GENRE FICTION (7)
  // ══════════════════════════════════════

  // ── 9. TOR (Epic Sci-Fi / Fantasy) ────
  {
    id: 'tor', name: 'Tor', category: 'scifi',
    description: 'Epic fantasy & sci-fi. Cinzel headings with EB Garamond on parchment.',
    previewColor: '#F5F2EC', previewAccent: '#7A4F1E',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Cinzel', 'Palatino Linotype', Georgia, serif",
    bodySize: '11.5pt', headingSize: '14pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '600', headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral', chapterNumberSize: '32pt',
    paperColor: '#F5F2EC', inkColor: '#0E0C0A', headingColor: '#0E0C0A', accentColor: '#7A4F1E',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:center;gap:6px;padding:1em 0;"><div style="width:24px;height:1px;background:[ACCENT];opacity:0.7;"></div><span style="color:[ACCENT];font-size:0.55em;font-family:'Cinzel',serif;">◆</span><div style="width:24px;height:1px;background:[ACCENT];opacity:0.7;"></div></div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 10. ORBIT (Dark Sci-Fi) ───────────
  {
    id: 'orbit', name: 'Orbit', category: 'scifi',
    description: 'Immersive dark sci-fi. Cinzel on near-black with silver-blue accent.',
    previewColor: '#0D0E11', previewAccent: '#7BA7BC',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Cinzel', 'Palatino Linotype', serif",
    bodySize: '11.5pt', headingSize: '13pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '600', headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#0D0E11', inkColor: '#D4C9B4', headingColor: '#E8D5A3', accentColor: '#7BA7BC',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:center;gap:8px;padding:1em 0;"><div style="width:30px;height:0.5px;background:[ACCENT];opacity:0.6;"></div><span style="color:[ACCENT];font-size:0.5em;font-family:'Cinzel',serif;letter-spacing:0.2em;">✦</span><div style="width:30px;height:0.5px;background:[ACCENT];opacity:0.6;"></div></div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 11. DEL REY (Accessible Fantasy) ──
  {
    id: 'delrey', name: 'Del Rey', category: 'scifi',
    description: 'Accessible fantasy & sci-fi. Raleway headings over EB Garamond, clean.',
    previewColor: '#FFFFFF', previewAccent: '#3D6B8C',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Raleway', 'Inter', sans-serif",
    bodySize: '11.5pt', headingSize: '12pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'left', headingWeight: '300', headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#3D6B8C',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '30%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:flex-start;gap:6px;padding:0.9em 0;"><div style="width:14px;height:1px;background:[ACCENT];opacity:0.6;"></div><div style="width:5px;height:5px;border-radius:50%;background:[ACCENT];opacity:0.5;"></div><div style="width:14px;height:1px;background:[ACCENT];opacity:0.6;"></div></div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ── 12. EMBER (YA Epic Fantasy) ───────
  {
    id: 'ember', name: 'Ember', category: 'ya',
    description: 'YA epic fantasy. Cinzel gold on warm cream — regal, immersive.',
    previewColor: '#F9F5EC', previewAccent: '#B8860B',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Cinzel', 'Palatino Linotype', serif",
    bodySize: '11.5pt', headingSize: '14pt', lineHeight: '1.68',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '600', headingTransform: 'uppercase',
    chapterNumberStyle: 'spelled', chapterNumberSize: '11pt',
    paperColor: '#F9F5EC', inkColor: '#1A1208', headingColor: '#2C1810', accentColor: '#B8860B',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:center;gap:6px;padding:1em 0;"><div style="width:20px;height:0.5px;background:[ACCENT];opacity:0.6;"></div><span style="color:[ACCENT];font-size:0.65em;font-family:'Cinzel',serif;">✦</span><div style="width:20px;height:0.5px;background:[ACCENT];opacity:0.6;"></div></div>`,
    chapterHeadingStyle: 'badge',
  },

  // ── 13. NIGHTFIRE (Horror) ────────────
  {
    id: 'nightfire', name: 'Nightfire', category: 'horror',
    description: 'Dark horror. Cinzel on near-black paper, deep crimson accent.',
    previewColor: '#0A0A0A', previewAccent: '#8B0000',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'Cinzel', 'Palatino Linotype', serif",
    bodySize: '11.5pt', headingSize: '13pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'uppercase',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#0A0A0A', inkColor: '#BFB8A8', headingColor: '#C41E1E', accentColor: '#8B0000',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:center;gap:8px;padding:1em 0;"><div style="width:24px;height:0.5px;background:[ACCENT];opacity:0.8;"></div><span style="color:[ACCENT];font-size:0.6em;font-family:'Cinzel',serif;">†</span><div style="width:24px;height:0.5px;background:[ACCENT];opacity:0.8;"></div></div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 14. MINOTAUR (Mystery/Thriller) ───
  {
    id: 'minotaur', name: 'Minotaur', category: 'mystery',
    description: 'Mystery & thriller authority. Playfair Display on warm off-white, deep red.',
    previewColor: '#FAF7F2', previewAccent: '#8B1A1A',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Playfair Display', Georgia, serif",
    bodySize: '11pt', headingSize: '18pt', lineHeight: '1.68',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FAF7F2', inkColor: '#1A1208', headingColor: '#1A1208', accentColor: '#8B1A1A',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:0.7em;letter-spacing:0.4em;opacity:0.7;">— — —</div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 15. SOHO CRIME (Literary Crime) ───
  {
    id: 'soho', name: 'Soho Crime', category: 'mystery',
    description: 'Literary crime fiction. Inter headings over Libre Baskerville, charcoal blue.',
    previewColor: '#F8F8F6', previewAccent: '#2D4A6E',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '12pt', lineHeight: '1.7',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'left', headingWeight: '600', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#F8F8F6', inkColor: '#111111', headingColor: '#111111', accentColor: '#2D4A6E',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '28%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;display:flex;align-items:center;gap:8px;"><div style="width:4px;height:4px;background:[ACCENT];opacity:0.7;"></div><div style="width:4px;height:4px;background:[ACCENT];opacity:0.4;"></div></div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ══════════════════════════════════════
  //  ROMANCE (1)
  // ══════════════════════════════════════

  // ── 16. BALLANTINE (Romance) ──────────
  {
    id: 'ballantine', name: 'Ballantine', category: 'romance',
    description: 'Romance perfected. Playfair italic headers over Lora on blush-cream paper.',
    previewColor: '#FFF7F5', previewAccent: '#9B2335',
    bodyFont: "'Lora', Georgia, serif",
    headingFont: "'Playfair Display', Georgia, serif",
    bodySize: '12pt', headingSize: '22pt', lineHeight: '1.72',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#FFF7F5', inkColor: '#1A080A', headingColor: '#9B2335', accentColor: '#9B2335',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'mass', showPageNumbers: true, pageNumberAlign: 'center',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:1.3em;line-height:1;">❧</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 17. AVON (Romance) ────────────────
  {
    id: 'avon', name: 'Avon', category: 'romance',
    description: 'Lush romantic elegance. Cormorant italic headings on blush with rose accent.',
    previewColor: '#FFF0F3', previewAccent: '#C4607A',
    bodyFont: "'Lora', Georgia, serif",
    headingFont: "'Cormorant Garamond', Georgia, serif",
    bodySize: '12pt', headingSize: '22pt', lineHeight: '1.78',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'roman', chapterNumberSize: '11pt',
    paperColor: '#FFF0F3', inkColor: '#1A0A0C', headingColor: '#7B1E3C', accentColor: '#C4607A',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'mass', showPageNumbers: true, pageNumberAlign: 'center',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:1.1em;line-height:1;">✿</div>`,
    chapterHeadingStyle: 'badge',
  },

  // ══════════════════════════════════════
  //  YOUNG ADULT (3)
  // ══════════════════════════════════════

  // ── 18. VINTAGE (Literary / YA) ───────
  {
    id: 'vintage', name: 'Vintage', category: 'ya',
    description: 'Literary cool. Josefin Sans Light headings over Libre Baskerville on white.',
    previewColor: '#FFFFFF', previewAccent: '#444444',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Josefin Sans', 'Raleway', sans-serif",
    bodySize: '10.5pt', headingSize: '11pt', lineHeight: '1.75',
    paragraphStyle: 'indent', textIndent: '1.3em', paragraphSpacing: '0',
    headingAlign: 'left', headingWeight: '300', headingTransform: 'uppercase',
    chapterNumberStyle: 'spelled', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#444444',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:1em 0;display:flex;align-items:center;justify-content:center;"><div style="width:36px;height:1px;background:[ACCENT];opacity:0.4;"></div></div>`,
    chapterHeadingStyle: 'minimal',
  },

  // ── 19. RAZORBILL (YA Contemporary) ───
  {
    id: 'razorbill', name: 'Razorbill', category: 'ya',
    description: 'YA contemporary energy. Raleway headings over Libre Baskerville, teal accent.',
    previewColor: '#FFFFFF', previewAccent: '#00897B',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Raleway', 'Inter', sans-serif",
    bodySize: '11pt', headingSize: '13pt', lineHeight: '1.72',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.7em',
    headingAlign: 'center', headingWeight: '600', headingTransform: 'uppercase',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#00897B',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '28%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="text-align:center;padding:0.9em 0;color:[ACCENT];font-size:0.7em;font-family:'Raleway',sans-serif;font-weight:300;letter-spacing:0.4em;">★ ★ ★</div>`,
    chapterHeadingStyle: 'large-num',
  },

  // ── 20. SPECTRUM (Contemporary YA) ────
  {
    id: 'spectrum', name: 'Spectrum', category: 'ya',
    description: 'Bold contemporary YA. Raleway Light headings, Libre Baskerville, vivid purple.',
    previewColor: '#FFFFFF', previewAccent: '#6B21A8',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Raleway', 'Inter', sans-serif",
    bodySize: '11pt', headingSize: '12pt', lineHeight: '1.72',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.75em',
    headingAlign: 'center', headingWeight: '200', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#6B21A8',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '28%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="text-align:center;padding:0.9em 0;"><span style="color:[ACCENT];font-size:0.55em;font-family:'Raleway',sans-serif;font-weight:200;letter-spacing:0.3em;">▲</span></div>`,
    chapterHeadingStyle: 'large-num',
  },

  // ══════════════════════════════════════
  //  NONFICTION (7)
  // ══════════════════════════════════════

  // ── 21. RIVERHEAD (Narrative Nonfiction) ──
  {
    id: 'riverhead', name: 'Riverhead', category: 'nonfiction',
    description: 'Warm narrative nonfiction. Merriweather body, Playfair Display ruled headings.',
    previewColor: '#FAF8F4', previewAccent: '#7B5E3C',
    bodyFont: "'Merriweather', Georgia, serif",
    headingFont: "'Playfair Display', Georgia, serif",
    bodySize: '11pt', headingSize: '18pt', lineHeight: '1.72',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FAF8F4', inkColor: '#1A1410', headingColor: '#1A1410', accentColor: '#7B5E3C',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '30%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:0.7em;letter-spacing:0.4em;opacity:0.7;">✦ &nbsp; ✦ &nbsp; ✦</div>`,
    chapterHeadingStyle: 'ruled',
  },

  // ── 22. VIKING (Serious Nonfiction) ───
  {
    id: 'viking', name: 'Viking', category: 'nonfiction',
    description: 'Serious nonfiction authority. Merriweather body, bold Inter headings.',
    previewColor: '#F9F9F7', previewAccent: '#1B4F8C',
    bodyFont: "'Merriweather', Georgia, serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '15pt', lineHeight: '1.65',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.7em',
    headingAlign: 'left', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#F9F9F7', inkColor: '#111111', headingColor: '#000000', accentColor: '#1B4F8C',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '25%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;"><div style="width:32px;height:2px;background:[ACCENT];opacity:0.7;"></div></div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ── 23. PENGUIN MODERN ────────────────
  {
    id: 'penguin', name: 'Penguin Modern', category: 'nonfiction',
    description: 'Contemporary literary. Raleway Light display over Libre Baskerville, orange accent.',
    previewColor: '#FFFFFF', previewAccent: '#E05000',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Raleway', 'Inter', sans-serif",
    bodySize: '11pt', headingSize: '28pt', lineHeight: '1.7',
    paragraphStyle: 'indent', textIndent: '1.4em', paragraphSpacing: '0',
    headingAlign: 'left', headingWeight: '200', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '52pt',
    paperColor: '#FFFFFF', inkColor: '#0A0A0A', headingColor: '#0A0A0A', accentColor: '#E05000',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '30%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;"><span style="color:[ACCENT];font-size:0.55em;font-family:'Raleway',sans-serif;font-weight:300;letter-spacing:0.3em;">▲</span></div>`,
    chapterHeadingStyle: 'large-num',
  },

  // ── 24. CROWN (Bold Pop Nonfiction) ───
  {
    id: 'crown', name: 'Crown', category: 'nonfiction',
    description: 'Bold popular nonfiction. Large chapter numbers, Inter, vivid orange.',
    previewColor: '#FFFFFF', previewAccent: '#E85D04',
    bodyFont: "'Inter', system-ui, sans-serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '16pt', lineHeight: '1.68',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.8em',
    headingAlign: 'left', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '52pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#000000', accentColor: '#E85D04',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '25%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;"><div style="width:36px;height:3px;background:[ACCENT];"></div></div>`,
    chapterHeadingStyle: 'large-num',
  },

  // ── 25. HBR (Business) ────────────────
  {
    id: 'hbr', name: 'HBR', category: 'business',
    description: 'Business authority. Clean Inter throughout with HBR navy-blue precision.',
    previewColor: '#F8F9FB', previewAccent: '#1A5F9E',
    bodyFont: "'Inter', system-ui, sans-serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '16pt', lineHeight: '1.68',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.8em',
    headingAlign: 'left', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '52pt',
    paperColor: '#F8F9FB', inkColor: '#1A1A2E', headingColor: '#0D2137', accentColor: '#1A5F9E',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '22%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:1em 0;"><div style="width:36px;height:2px;background:[ACCENT];opacity:0.6;"></div></div>`,
    chapterHeadingStyle: 'large-num',
  },

  // ── 26. ANCHOR (Narrative Nonfiction) ─
  {
    id: 'anchor', name: 'Anchor', category: 'nonfiction',
    description: 'Narrative nonfiction. Inter headings over Libre Baskerville with warm orange.',
    previewColor: '#FAFAFA', previewAccent: '#C44200',
    bodyFont: "'Libre Baskerville', Georgia, serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '15pt', lineHeight: '1.7',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.75em',
    headingAlign: 'left', headingWeight: '600', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '44pt',
    paperColor: '#FAFAFA', inkColor: '#151515', headingColor: '#0A0A0A', accentColor: '#C44200',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '25%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="display:flex;align-items:center;justify-content:center;gap:8px;padding:1em 0;"><div style="width:20px;height:1px;background:[ACCENT];opacity:0.5;"></div><div style="width:5px;height:5px;border-radius:50%;background:[ACCENT];opacity:0.6;"></div><div style="width:20px;height:1px;background:[ACCENT];opacity:0.5;"></div></div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ── 27. RODALE (Health / Wellness) ────
  {
    id: 'rodale', name: 'Rodale', category: 'nonfiction',
    description: 'Health & wellness clarity. Inter throughout, airy spacing, fresh green accent.',
    previewColor: '#FFFFFF', previewAccent: '#2D8A2D',
    bodyFont: "'Inter', system-ui, sans-serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '15pt', lineHeight: '1.78',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.9em',
    headingAlign: 'left', headingWeight: '600', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#1A1A1A', headingColor: '#1A5C1A', accentColor: '#2D8A2D',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '22%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;display:flex;align-items:center;gap:6px;"><div style="width:20px;height:2px;background:[ACCENT];opacity:0.6;border-radius:1px;"></div><div style="width:6px;height:6px;border-radius:50%;background:[ACCENT];opacity:0.5;"></div></div>`,
    chapterHeadingStyle: 'minimal',
  },

  // ══════════════════════════════════════
  //  ACADEMIC / SCHOLARLY (1)
  // ══════════════════════════════════════

  // ── 28. MERIDIAN (Academic) ───────────
  {
    id: 'meridian', name: 'Meridian', category: 'nonfiction',
    description: 'Academic & scholarly. Inter throughout, structured stacked headings, navy.',
    previewColor: '#FFFFFF', previewAccent: '#1A4480',
    bodyFont: "'Inter', system-ui, sans-serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '14pt', lineHeight: '1.6',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.75em',
    headingAlign: 'left', headingWeight: '700', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#1A1A2E', headingColor: '#0D2445', accentColor: '#1A4480',
    pageMarginH: '1in', pageMarginV: '1in', chapterStartMargin: '22%',
    pageSize: '7x10', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;"><div style="width:48px;height:1.5px;background:[ACCENT];opacity:0.5;"></div></div>`,
    chapterHeadingStyle: 'stacked',
  },

  // ══════════════════════════════════════
  //  TIMELESS / UNIVERSAL (2)
  // ══════════════════════════════════════

  // ── 29. SERIF (Timeless Classic) ──────
  {
    id: 'serif', name: 'Serif', category: 'fiction',
    description: 'Timeless classic. EB Garamond throughout, pure white, perfectly proportioned.',
    previewColor: '#FFFFFF', previewAccent: '#333333',
    bodyFont: "'EB Garamond', Georgia, serif",
    headingFont: "'EB Garamond', Georgia, serif",
    bodySize: '11.5pt', headingSize: '20pt', lineHeight: '1.65',
    paragraphStyle: 'indent', textIndent: '1.5em', paragraphSpacing: '0',
    headingAlign: 'center', headingWeight: '400', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#111111', headingColor: '#111111', accentColor: '#333333',
    pageMarginH: '0.875in', pageMarginV: '0.875in', chapterStartMargin: '33%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: true,
    ornament: `<div style="text-align:center;padding:1em 0;color:[ACCENT];font-size:0.8em;letter-spacing:0.5em;opacity:0.6;">* * *</div>`,
    chapterHeadingStyle: 'classic',
  },

  // ── 30. MINIMAL (Modern All-Sans) ─────
  {
    id: 'minimal', name: 'Minimal', category: 'nonfiction',
    description: 'Modern all-sans. Inter throughout, pure white, pure black — maximum clarity.',
    previewColor: '#FFFFFF', previewAccent: '#0A0A0A',
    bodyFont: "'Inter', system-ui, sans-serif",
    headingFont: "'Inter', system-ui, sans-serif",
    bodySize: '11pt', headingSize: '12pt', lineHeight: '1.7',
    paragraphStyle: 'spaced', textIndent: '0', paragraphSpacing: '0.8em',
    headingAlign: 'left', headingWeight: '300', headingTransform: 'none',
    chapterNumberStyle: 'numeral', chapterNumberSize: '11pt',
    paperColor: '#FFFFFF', inkColor: '#0A0A0A', headingColor: '#0A0A0A', accentColor: '#0A0A0A',
    pageMarginH: '1.125in', pageMarginV: '1.125in', chapterStartMargin: '20%',
    pageSize: 'trade', showPageNumbers: true, pageNumberAlign: 'outer',
    dropCap: false,
    ornament: `<div style="padding:0.9em 0;"><div style="width:16px;height:1px;background:#0A0A0A;"></div></div>`,
    chapterHeadingStyle: 'minimal',
  },

];

export const defaultTemplate = templates[0]; // Knopf

export function getTemplate(id: string): Template {
  return templates.find((t) => t.id === id) ?? defaultTemplate;
}

// ─────────────────────────────────────────
//  SECTION BREAK BUILDER
// ─────────────────────────────────────────

export function buildSectionBreak(template: Template): string {
  return template.ornament.replace(/\[ACCENT\]/g, template.accentColor);
}

// ─────────────────────────────────────────
//  TEMPLATE CSS (live editor preview)
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
      margin: 1.5em 0;
    }
    .drop-cap::first-letter {
      float: left;
      font-family: ${template.headingFont};
      font-size: 3.8em;
      line-height: 0.82;
      font-weight: 700;
      color: ${template.accentColor};
      padding-right: 4px;
      margin-top: 4px;
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
