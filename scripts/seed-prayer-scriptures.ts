/** Default is validation only. --apply requires a separately configured test/project DB URL. */
import { readFileSync } from "node:fs";
import { createHash } from "node:crypto";
import pg from "pg";
type Book = { name: string; chapters: string[][] };
async function main() {
 const raw = readFileSync(new URL("../android/app/src/main/assets/kjv.json", import.meta.url));
 const books = JSON.parse(raw.toString("utf8")) as Book[];
 const corrections = JSON.parse(readFileSync(new URL("../android/app/src/main/assets/kjv-corrections.json", import.meta.url), "utf8")) as Record<string,string>;
 const verses = books.flatMap(book => book.chapters.flatMap((chapter, c) => chapter.map((raw, v) => ({
  reference: `${book.name} ${c + 1}:${v + 1}`, book: book.name, chapter: c + 1, verse: v + 1,
  text: (corrections[`${book.name} ${c + 1}:${v + 1}`] ?? raw).replace(/\{([^{}]*)\}/g, (_, inner: string) => inner.includes(":") ? "" : inner).replace(/\s+/g, " ").trim(),
 }))));
 if (books.length !== 66 || verses.length !== 31102 || new Set(verses.map(x => x.reference)).size !== 31102 || verses.some(x => !x.text || /[{}]/.test(x.text))) throw new Error("Bundled Bible validation failed.");
 console.log(`Validated ${books.length} books, ${verses.length} verses. SHA256 ${createHash("sha256").update(raw).digest("hex")}`);
 if (!process.argv.includes("--apply")) return;
 if (!process.env.SUPABASE_DB_URL) throw new Error("Set SUPABASE_DB_URL securely; no connection was attempted.");
 const client = new pg.Client({ connectionString: process.env.SUPABASE_DB_URL });
 await client.connect();
 try {
  await client.query("begin");
  for (let start = 0; start < verses.length; start += 500) {
   await client.query(`insert into public.pk_scriptures(reference,book,chapter,verse,text)
    select reference,book,chapter,verse,text from jsonb_to_recordset($1::jsonb) as r(reference text,book text,chapter int,verse int,text text)
    on conflict(reference) do nothing`, [JSON.stringify(verses.slice(start, start + 500))]);
  }
  await client.query("commit");
  console.log("Canonical Scripture import committed.");
 } catch (error) { await client.query("rollback"); throw error; }
 finally { await client.end(); }
}
main().catch(() => { console.error("Scripture validation/import failed. No connection details are logged."); process.exitCode = 1; });
