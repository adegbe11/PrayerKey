import { BIBLE_BOOKS } from "@/lib/seo/bible-books";

// Each book is bound in one leather, the same on the shelf and on its own page.
export const LEATHER = ["#3b0a7a", "#24104f", "#4a1450", "#5a1fb0", "#2a1838", "#5a2a14", "#1c1840", "#4b0f2e"];

const INDEX = new Map(BIBLE_BOOKS.map((b, i) => [b.slug, i]));

export function leatherOf(slug: string) {
  return LEATHER[(INDEX.get(slug) ?? 0) % LEATHER.length];
}

// a spine's height follows the book's length, so Psalms stands tallest
export function spineHeight(chapters: number, scale = 1) {
  return Math.round((150 + Math.sqrt(chapters) * 17) * scale);
}
