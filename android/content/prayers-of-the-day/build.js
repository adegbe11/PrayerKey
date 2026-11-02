// Checks and assembles the Prayer of the Day year.
// Usage: node build.js <kjv.json> <outDir> [--print MM]
const fs = require('fs');
const path = require('path');
const [kjvPath, outDir, flag, flagArg] = process.argv.slice(2);
const kjv = JSON.parse(fs.readFileSync(kjvPath, 'utf8').replace(/^﻿/, ''));
const NAMES = kjv.map((b) => b.name);
const alias = { psalm: 'Psalms', 'song of songs': 'Song of Solomon', 'song of solomon': 'Song of Solomon' };
function bookIndex(name) {
  const n = name.toLowerCase();
  const target = (alias[n] || name).toLowerCase();
  return NAMES.findIndex((x) => x.toLowerCase() === target);
}
const clean = (s) => s.replace(/\s*\{[^{}]*:[^{}]*\}/g, '').replace(/[{}]/g, '').replace(/\s+([,.;:!?])/g, '$1').replace(/\s{2,}/g, ' ').trim();
function verse(ref) {
  const m = ref.match(/^(.+?) (\d+):(\d+)(?:-(\d+))?$/);
  if (!m) throw new Error('bad ref ' + ref);
  const bi = bookIndex(m[1]);
  if (bi < 0) throw new Error('unknown book ' + m[1]);
  const ch = kjv[bi].chapters[+m[2] - 1];
  if (!ch) throw new Error('no chapter ' + ref);
  const from = +m[3], to = m[4] ? +m[4] : from;
  const out = [];
  for (let v = from; v <= to; v++) { if (!ch[v - 1]) throw new Error('no verse ' + ref); out.push(clean(ch[v - 1])); }
  return out.join(' ').replace(/[;:,]$/, '.');
}

const months = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
const all = [];
const problems = [];
const seenTitles = new Map();
const seenRefs = new Map();
for (let mo = 1; mo <= 12; mo++) {
  const f = path.join(__dirname, String(mo).padStart(2, '0') + '.txt');
  if (!fs.existsSync(f)) { problems.push(`month ${mo}: file missing`); continue; }
  const rows = fs.readFileSync(f, 'utf8').split(/\r?\n/).filter((l) => l.trim());
  const days = new Set();
  for (const row of rows) {
    const parts = row.split('|');
    if (parts.length !== 4) { problems.push(`bad row: ${row.slice(0, 50)}`); continue; }
    const [date, title, ref, body] = parts.map((p) => p.trim());
    const [mm, dd] = date.split('-').map(Number);
    if (mm !== mo || dd < 1 || dd > months[mo - 1]) problems.push(`${date}: date out of range`);
    if (days.has(dd)) problems.push(`${date}: duplicate day`);
    days.add(dd);
    const lines = body.split('/').map((l) => l.trim()).filter(Boolean);
    const words = lines.join(' ').split(/\s+/).length;
    if (/[–—]/.test(row)) problems.push(`${date}: contains a long dash`);
    if (!/Amen\.$/.test(lines[lines.length - 1])) problems.push(`${date}: does not end with Amen.`);
    if (words < 22 || words > 110) problems.push(`${date}: ${words} words`);
    if (seenTitles.has(title)) problems.push(`${date}: title repeats ${seenTitles.get(title)}`);
    seenTitles.set(title, date);
    if (seenRefs.has(ref)) problems.push(date + ': verse repeats ' + seenRefs.get(ref));
    seenRefs.set(ref, date);
    let text = '';
    try { text = verse(ref); } catch (e) { problems.push(`${date}: ${e.message}`); }
    all.push({ date, title, ref: ref.replace(/^Psalms /, 'Psalm '), verse: text, lines });
  }
  for (let d = 1; d <= months[mo - 1]; d++) if (!days.has(d)) problems.push(`${mo}-${d}: missing`);
}

// moveable days, keyed by name rather than date
const mf = path.join(__dirname, 'moveable.txt');
const moveable = [];
if (fs.existsSync(mf)) {
  for (const row of fs.readFileSync(mf, 'utf8').split(/\r?\n/).filter((l) => l.trim())) {
    const [key, title, ref, body] = row.split('|').map((p) => p.trim());
    const lines = body.split('/').map((l) => l.trim()).filter(Boolean);
    let text = '';
    try { text = verse(ref); } catch (e) { problems.push(`${key}: ${e.message}`); }
    moveable.push({ key, title, ref: ref.replace(/^Psalms /, 'Psalm '), verse: text, lines });
  }
}

if (flag === '--print') for (const p of all.filter((p) => p.date.startsWith(flagArg))) console.log(`${p.date} ${p.ref}: ${p.verse.slice(0, 110)}`);
console.log(`days written: ${all.length} of 366, moveable: ${moveable.length}`);
console.log(problems.length ? 'PROBLEMS:\n  ' + problems.slice(0, 60).join('\n  ') : 'no problems');
if (outDir && all.length === 366 && !problems.length) {
  fs.writeFileSync(path.join(outDir, 'prayers-of-the-day.json'), JSON.stringify({ version: 1, days: all, moveable }, null, 1));
  console.log('wrote prayers-of-the-day.json');
}
