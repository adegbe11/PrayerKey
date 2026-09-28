import { FORMATS, type DailyPrayer, type FormatId } from "./daily";

// Draws the "Prayer of the Day" share image in the app's look: a night-violet sky with a
// soft glow of light, the reference in white and gold, the verse in serif and a short prayer line.

function fontVar(name: string, fallback: string) {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  return value ? `${value}, ${fallback}` : fallback;
}

export async function downloadPrayerCard(prayer: DailyPrayer, formatId: FormatId) {
  const f = FORMATS.find((x) => x.id === formatId)!;
  const W = f.width, H = f.height;
  const canvas = document.createElement("canvas");
  canvas.width = W; canvas.height = H;
  const ctx = canvas.getContext("2d")!;
  await document.fonts?.ready;

  const display = fontVar("--font-display", "Georgia, serif");
  const book = fontVar("--font-book", "Georgia, serif");
  const sans = fontVar("--font-sans", "system-ui, sans-serif");

  const S = W / 1080;
  const ratio = H / W;
  // Wide frames shrink the type; tall ones (Story, Pin) get more of it so the block fills the frame.
  const TS = ratio < 0.72 ? S * (ratio / 0.72) : ratio > 1.4 ? S * 1.3 : S;
  const PAD = Math.round(W * 0.1);
  const CW = W - PAD * 2;
  const CX = W / 2;

  const wrap = (text: string, maxW: number, font: string) => {
    ctx.font = font;
    const out: string[] = []; let line = "";
    for (const word of text.split(" ")) {
      const test = line ? `${line} ${word}` : word;
      if (ctx.measureText(test).width > maxW && line) { out.push(line); line = word; } else line = test;
    }
    if (line) out.push(line);
    return out;
  };

  // Sky
  const sky = ctx.createLinearGradient(0, 0, 0, H);
  sky.addColorStop(0, "#160022"); sky.addColorStop(0.55, "#2D0A5C"); sky.addColorStop(1, "#4A12A0");
  ctx.fillStyle = sky; ctx.fillRect(0, 0, W, H);
  // Light breaking through above
  const light = ctx.createRadialGradient(CX, H * 0.22, 0, CX, H * 0.22, Math.max(W, H) * 0.7);
  light.addColorStop(0, "rgba(255,236,190,0.55)"); light.addColorStop(0.25, "rgba(232,201,140,0.18)"); light.addColorStop(1, "rgba(232,201,140,0)");
  ctx.fillStyle = light; ctx.fillRect(0, 0, W, H);
  // Horizon warmth
  const horizon = ctx.createRadialGradient(CX, H * 1.05, 0, CX, H * 1.05, W * 0.9);
  horizon.addColorStop(0, "rgba(255,170,120,0.28)"); horizon.addColorStop(1, "rgba(255,170,120,0)");
  ctx.fillStyle = horizon; ctx.fillRect(0, 0, W, H);

  const ref = prayer.ref.match(/^(.*?)\s+(\d+[:\d–-]*)$/);
  const bookName = (ref ? ref[1] : prayer.ref).toUpperCase();
  const chapterVerse = ref ? ref[2] : "";
  const shortPrayer = prayer.prayer.replace(/\n/g, " ").split(/(?<=[.!?])\s+/)[0];

  // Lay the block out once to measure it, then draw it centred in the frame.
  const layout = (draw: boolean, top: number) => {
    let y = top;
    const text = (value: string, font: string, color: string, x: number, baseline: number) => { if (draw) { ctx.font = font; ctx.fillStyle = color; ctx.fillText(value, x, baseline); } };
    ctx.textAlign = "center";
    text("PRAYER OF THE DAY", `600 ${Math.round(14 * S)}px ${sans}`, "#E8C98C", CX, y + Math.round(14 * S));
    y += Math.round(14 * S) + Math.round(36 * S);

    const bookFS = Math.round((ratio < 0.72 ? 70 : ratio > 1.4 ? 110 : 92) * TS);
    const bookFont = `700 ${bookFS}px ${display}`;
    for (const line of wrap(bookName, CW, bookFont)) { text(line, bookFont, "#FFFFFF", CX, y + bookFS * 0.8); y += Math.round(bookFS * 0.98); }

    const cvFS = Math.round((ratio < 0.72 ? 58 : ratio > 1.4 ? 92 : 78) * TS);
    text(chapterVerse, `600 ${cvFS}px ${display}`, "#E8C98C", CX, y + cvFS * 0.82);
    y += Math.round(cvFS * 0.95) + Math.round(26 * S);

    if (draw) { ctx.strokeStyle = "rgba(232,201,140,0.45)"; ctx.lineWidth = Math.max(1, Math.round(1.5 * S)); ctx.beginPath(); ctx.moveTo(CX - Math.round(70 * S), y); ctx.lineTo(CX + Math.round(70 * S), y); ctx.stroke(); }
    y += Math.round(34 * S);

    const vFS = Math.round((ratio < 0.72 ? 19 : ratio > 1.4 ? 27 : 24) * TS);
    const vLH = Math.round(vFS * 1.6);
    const vFont = `400 ${vFS}px ${book}`;
    const verseLines = wrap(`“${prayer.verse}”`, CW, vFont);
    verseLines.forEach((line, i) => text(line, vFont, "rgba(255,255,255,0.92)", CX, y + vFS * 0.85 + vLH * i));
    y += verseLines.length * vLH + Math.round(26 * S);

    const pFS = Math.round((ratio < 0.72 ? 16 : ratio > 1.4 ? 22 : 20) * TS);
    const pLH = Math.round(pFS * 1.7);
    const pFont = `italic 400 ${pFS}px ${book}`;
    const prayerLines = wrap(shortPrayer, CW * 0.9, pFont);
    prayerLines.forEach((line, i) => text(line, pFont, "#E8C98C", CX, y + pFS * 0.85 + pLH * i));
    y += prayerLines.length * pLH + Math.round(56 * S);

    text("PRAYERKEY.COM", `700 ${Math.round(15 * S)}px ${sans}`, "rgba(255,255,255,0.7)", CX, y + Math.round(15 * S));
    y += Math.round(15 * S);
    return y - top;
  };
  const height = layout(false, 0);
  layout(true, Math.max(Math.round(PAD * 0.6), Math.round((H - height) / 2)));

  const link = document.createElement("a");
  link.download = `prayerkey-${formatId}-${new Date().toISOString().slice(0, 10)}.png`;
  link.href = canvas.toDataURL("image/png");
  link.click();
}
