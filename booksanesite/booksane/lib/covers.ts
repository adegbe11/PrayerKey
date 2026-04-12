import type { Genre } from '@/types';

// ─────────────────────────────────────────
//  COVER TEMPLATES
//  10 beautiful CSS book cover templates
// ─────────────────────────────────────────

export interface CoverTemplate {
  id: string;
  name: string;
  genres: Genre[];
}

export const coverTemplates: CoverTemplate[] = [
  { id: 'divine',  name: 'Divine',   genres: ['religious'] },
  { id: 'bloom',   name: 'Bloom',    genres: ['romance'] },
  { id: 'edge',    name: 'Edge',     genres: ['thriller', 'mystery'] },
  { id: 'tower',   name: 'Tower',    genres: ['business', 'academic', 'biography'] },
  { id: 'chapter', name: 'Chapter',  genres: ['memoir', 'biography', 'fiction'] },
  { id: 'bold',    name: 'Bold',     genres: ['selfhelp'] },
  { id: 'verse',   name: 'Verse',    genres: ['poetry'] },
  { id: 'cosmos',  name: 'Cosmos',   genres: ['scifi', 'fantasy'] },
  { id: 'clean',   name: 'Clean',    genres: ['fiction'] },
  { id: 'paper',   name: 'Paper',    genres: ['academic'] },
];

// ─────────────────────────────────────────
//  TEMPLATE HTML DEFINITIONS
// ─────────────────────────────────────────

const TEMPLATE_HTML: Record<string, string> = {

  // 1. DIVINE — Religious/Spiritual
  divine: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:linear-gradient(160deg,#0A1628 0%,#132040 60%,#1A2D55 100%);
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:28px 24px;
">
  <!-- Radial glow -->
  <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-60%);width:180px;height:180px;background:radial-gradient(ellipse,rgba(212,168,83,0.12) 0%,transparent 70%);pointer-events:none;"></div>
  <!-- Vertical gold line above title -->
  <div style="width:2px;height:60px;background:#D4A853;margin-bottom:20px;opacity:0.7;"></div>
  <!-- Title -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:13pt;font-weight:700;color:#F5E6C8;text-align:center;line-height:1.3;z-index:1;margin-bottom:14px;">
    {title}
  </div>
  <!-- Gold horizontal line -->
  <div style="width:40px;height:1px;background:#D4A853;margin-bottom:12px;opacity:0.8;"></div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:8pt;color:#D4A853;text-transform:uppercase;letter-spacing:0.1em;text-align:center;margin-top:4px;">
    {author}
  </div>
  <!-- Bottom branding -->
  <div style="position:absolute;bottom:14px;left:0;right:0;text-align:center;font-family:Georgia,serif;font-size:7pt;color:rgba(212,168,83,0.4);letter-spacing:0.08em;">
    BOOKSANE
  </div>
</div>
`,

  // 2. BLOOM — Romance
  bloom: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:linear-gradient(170deg,#1A0510 0%,#4A0E28 50%,#7B1440 100%);
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:28px 24px;
">
  <!-- Large depth circles -->
  <div style="position:absolute;top:-40px;right:-40px;width:200px;height:200px;border-radius:50%;background:rgba(180,30,80,0.15);pointer-events:none;"></div>
  <div style="position:absolute;bottom:-60px;left:-30px;width:160px;height:160px;border-radius:50%;background:rgba(253,232,240,0.06);pointer-events:none;"></div>
  <!-- Title -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:14pt;font-style:italic;color:#FDE8F0;text-align:center;line-height:1.3;z-index:1;margin-bottom:12px;">
    {title}
  </div>
  <!-- Dot separator -->
  <div style="color:rgba(253,232,240,0.5);font-size:11pt;letter-spacing:0.3em;margin-bottom:12px;z-index:1;">· · ·</div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:8pt;color:rgba(253,232,240,0.65);text-transform:uppercase;letter-spacing:0.12em;text-align:center;z-index:1;margin-top:4px;">
    {author}
  </div>
</div>
`,

  // 3. EDGE — Thriller/Mystery
  edge: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#090909;
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:28px 20px;
">
  <!-- Red diagonal accent bar -->
  <div style="position:absolute;top:0;left:-40px;width:340px;height:3px;background:rgba(200,0,0,0.8);transform:rotate(-45deg) translateY(160px);transform-origin:center;pointer-events:none;"></div>
  <!-- Title -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:15pt;font-weight:900;color:#FFFFFF;text-align:center;line-height:1.1;text-transform:uppercase;letter-spacing:-0.02em;z-index:1;margin-bottom:16px;">
    {title}
  </div>
  <!-- Thin white line above author -->
  <div style="width:100%;height:1px;background:rgba(255,255,255,0.15);margin-bottom:12px;z-index:1;"></div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:8pt;color:rgba(255,255,255,0.5);text-transform:uppercase;letter-spacing:0.15em;text-align:center;z-index:1;">
    {author}
  </div>
</div>
`,

  // 4. TOWER — Business/Nonfiction
  tower: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#F8F7F4;
  display:flex;flex-direction:column;align-items:flex-start;justify-content:center;
  box-sizing:border-box;padding:28px 24px 52px 24px;
">
  <!-- Left border accent -->
  <div style="width:4px;height:60px;background:#000;margin-bottom:14px;"></div>
  <!-- Title -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:14pt;font-weight:900;color:#000;text-align:left;line-height:1.1;margin-bottom:14px;">
    {title}
  </div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:8pt;color:#555;text-transform:uppercase;letter-spacing:0.1em;text-align:left;">
    {author}
  </div>
  <!-- Black bottom band -->
  <div style="position:absolute;bottom:0;left:0;right:0;height:40px;background:#000;display:flex;align-items:center;justify-content:center;">
    <span style="font-family:'Inter',system-ui,sans-serif;font-size:7pt;color:#FFFFFF;letter-spacing:0.12em;text-transform:uppercase;">BOOKSANE</span>
  </div>
</div>
`,

  // 5. CHAPTER — Memoir/Literary Fiction
  chapter: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#F7F2EA;
  display:flex;flex-direction:column;align-items:center;justify-content:flex-start;
  box-sizing:border-box;padding:60px 28px 28px;
">
  <!-- Horizontal line -->
  <div style="width:100%;height:1.5px;background:#8B6F47;margin-bottom:0;position:absolute;top:110px;left:0;right:0;"></div>
  <!-- Spacer to push title below the line -->
  <div style="height:62px;"></div>
  <!-- Title -->
  <div style="font-family:Palatino,Georgia,'Times New Roman',serif;font-size:13pt;font-weight:400;color:#1A0F00;text-align:center;line-height:1.35;margin-bottom:16px;margin-top:12px;">
    {title}
  </div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:Palatino,Georgia,serif;font-size:8pt;color:#8B6F47;letter-spacing:0.08em;text-align:center;margin-top:auto;">
    {author}
  </div>
</div>
`,

  // 6. BOLD — Self-Help
  bold: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#FFE500;
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:28px 20px;
">
  <!-- MASSIVE title -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:16pt;font-weight:900;color:#000;text-align:center;line-height:1.0;text-transform:uppercase;margin-bottom:14px;">
    {title}
  </div>
  <!-- Black divider -->
  <div style="width:60px;height:3px;background:#000;margin-bottom:14px;"></div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:8pt;color:#000;font-weight:700;text-transform:uppercase;letter-spacing:0.1em;text-align:center;">
    {author}
  </div>
</div>
`,

  // 7. VERSE — Poetry
  verse: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#F0EEF8;
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:36px 28px;
">
  <!-- Thin purple decorative line above title -->
  <div style="width:30px;height:1px;background:#7C3AED;margin-bottom:18px;opacity:0.7;"></div>
  <!-- Title -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:12pt;font-weight:400;color:#2A2040;text-align:center;line-height:1.6;padding:0 8px;margin-bottom:20px;">
    {title}
  </div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:Georgia,'Times New Roman',serif;font-size:8pt;color:#7C3AED;font-style:italic;text-align:center;">
    {author}
  </div>
</div>
`,

  // 8. COSMOS — SciFi/Fantasy
  cosmos: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:linear-gradient(135deg,#050A1A 0%,#0D1B3E 40%,#1A0A3D 100%);
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:28px 24px;
">
  <!-- Stars -->
  <div style="position:absolute;top:18px;left:22px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.9;"></div>
  <div style="position:absolute;top:34px;left:80px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.7;"></div>
  <div style="position:absolute;top:55px;left:140px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.8;"></div>
  <div style="position:absolute;top:25px;left:200px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.6;"></div>
  <div style="position:absolute;top:70px;left:230px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.9;"></div>
  <div style="position:absolute;top:300px;left:15px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.7;"></div>
  <div style="position:absolute;top:320px;left:65px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.5;"></div>
  <div style="position:absolute;top:340px;left:180px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.8;"></div>
  <div style="position:absolute;top:290px;left:220px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.6;"></div>
  <div style="position:absolute;top:110px;left:10px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.5;"></div>
  <div style="position:absolute;top:145px;left:245px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.7;"></div>
  <div style="position:absolute;top:200px;left:5px;width:2px;height:2px;background:#fff;border-radius:50%;opacity:0.4;"></div>
  <!-- Title -->
  <div style="font-family:'Inter',system-ui,Georgia,serif;font-size:13pt;font-weight:700;color:#E8F4FF;text-align:center;line-height:1.3;letter-spacing:0.03em;z-index:1;margin-bottom:14px;">
    {title}
  </div>
  <!-- Purple accent line -->
  <div style="width:40px;height:1px;background:#7C3AED;margin-bottom:14px;z-index:1;"></div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Author -->
  <div style="font-family:'Inter',system-ui,sans-serif;font-size:8pt;color:rgba(232,244,255,0.6);text-transform:uppercase;letter-spacing:0.1em;text-align:center;z-index:1;">
    {author}
  </div>
</div>
`,

  // 9. CLEAN — General Fiction
  clean: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#FFFFFF;
  display:flex;flex-direction:column;align-items:center;justify-content:flex-start;
  box-sizing:border-box;padding:0;
">
  <!-- Dark top rectangle (45% height) -->
  <div style="width:100%;height:171px;background:#1A1828;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:20px;box-sizing:border-box;">
    <!-- Title inside dark rectangle -->
    <div style="font-family:Georgia,'Times New Roman',serif;font-size:13pt;font-weight:600;color:#FFFFFF;text-align:center;line-height:1.3;">
      {title}
    </div>
    {subtitleInsideDark}
  </div>
  <!-- Author section below rectangle -->
  <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:20px;">
    <div style="display:flex;align-items:center;gap:6px;margin-bottom:6px;">
      <!-- Small accent square -->
      <div style="width:8px;height:8px;background:#7C3AED;flex-shrink:0;"></div>
      <div style="font-family:'Inter',system-ui,sans-serif;font-size:9pt;color:#1A1828;text-transform:uppercase;letter-spacing:0.1em;">
        {author}
      </div>
    </div>
  </div>
</div>
`,

  // 10. PAPER — Academic
  paper: `
<div style="
  width:260px;height:380px;position:relative;overflow:hidden;
  background:#F2F2F0;
  display:flex;flex-direction:column;align-items:center;justify-content:center;
  box-sizing:border-box;padding:32px 28px 52px;
">
  <!-- Double horizontal lines (top of title area) -->
  <div style="width:100%;height:1px;background:#999;margin-bottom:3px;"></div>
  <div style="width:100%;height:1px;background:#999;margin-bottom:14px;"></div>
  <!-- Title between lines -->
  <div style="font-family:'Times New Roman',Times,serif;font-size:12pt;font-weight:700;color:#111;text-align:center;line-height:1.35;margin-bottom:14px;">
    {title}
  </div>
  <!-- Subtitle -->
  {subtitleHtml}
  <!-- Double horizontal lines (bottom of title area) -->
  <div style="width:100%;height:1px;background:#999;margin-top:4px;margin-bottom:3px;"></div>
  <div style="width:100%;height:1px;background:#999;margin-bottom:16px;"></div>
  <!-- Author -->
  <div style="font-family:'Times New Roman',Times,serif;font-size:9pt;color:#555;text-align:center;margin-bottom:4px;">
    {author}
  </div>
  <!-- Bottom logo -->
  <div style="position:absolute;bottom:18px;left:0;right:0;text-align:center;font-family:'Times New Roman',Times,serif;font-size:7pt;color:#999;letter-spacing:0.1em;text-transform:uppercase;">
    BOOKSANE PRESS
  </div>
</div>
`,
};

// ─────────────────────────────────────────
//  RENDER COVER
// ─────────────────────────────────────────

export function renderCover(
  templateId: string,
  title: string,
  author: string,
  subtitle?: string
): string {
  const templateHtml = TEMPLATE_HTML[templateId] ?? TEMPLATE_HTML['clean'];

  // Build subtitle HTML snippets
  const subtitleHtml = subtitle
    ? `<div style="font-family:Georgia,'Times New Roman',serif;font-size:9pt;font-style:italic;color:inherit;opacity:0.7;text-align:center;margin-bottom:8px;">${subtitle}</div>`
    : '';

  const subtitleInsideDark = subtitle
    ? `<div style="font-family:Georgia,'Times New Roman',serif;font-size:8pt;font-style:italic;color:rgba(255,255,255,0.6);text-align:center;margin-top:6px;">${subtitle}</div>`
    : '';

  return templateHtml
    .replace(/\{title\}/g, escapeHtml(title))
    .replace(/\{author\}/g, escapeHtml(author))
    .replace(/\{subtitleHtml\}/g, subtitleHtml)
    .replace(/\{subtitleInsideDark\}/g, subtitleInsideDark);
}

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// ─────────────────────────────────────────
//  GET DEFAULT COVER FOR GENRE
// ─────────────────────────────────────────

export function getDefaultCoverForGenre(genre: Genre): string {
  const map: Record<Genre, string> = {
    religious:  'divine',
    romance:    'bloom',
    thriller:   'edge',
    mystery:    'edge',
    scifi:      'cosmos',
    fantasy:    'cosmos',
    memoir:     'chapter',
    biography:  'chapter',
    business:   'tower',
    selfhelp:   'bold',
    poetry:     'verse',
    academic:   'paper',
    fiction:    'clean',
    childrens:  'bold',
  };
  return map[genre] ?? 'clean';
}
