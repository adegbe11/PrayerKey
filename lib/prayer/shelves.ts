import { PRAYER_TOPICS, PrayerTopic } from "@/lib/seo/prayer-topics";

// The prayers arranged on shelves, shared by every page that shows the whole
// library, so they always agree on where each prayer lives.

export const CATEGORIES: [string, string][] = [
  ["Health", "Healing, recovery and strength for the body."],
  ["Mental Health", "Peace for an anxious, weary or heavy mind."],
  ["Daily Prayer", "Morning, night and the hours in between."],
  ["Family", "Children, parents, home and unity."],
  ["Finance", "Provision, debt and breakthrough."],
  ["Work", "Jobs, business, interviews and the workplace."],
  ["Protection", "Safety, travel and spiritual warfare."],
  ["Faith", "Trust, belief and walking with God."],
  ["Grief", "Loss, mourning and comfort."],
  ["Celebration", "Birthdays, milestones and new beginnings."],
  ["Education", "Exams, students and wisdom."],
  ["Salvation", "Repentance, forgiveness and new life."],
  ["Purpose", "Direction, calling and the next step."],
  ["Relationships", "Marriage, friendship and reconciliation."],
  ["Thanksgiving", "Gratitude and praise."],
  ["Ministry", "The church, pastors and the mission."],
  ["Nation", "Leaders, peace and revival."],
];

export const POPULAR = [
  "healing", "anxiety", "strength", "protection", "morning", "marriage", "financial-breakthrough", "depression",
  "forgiveness", "salvation", "gratitude", "sleep", "fear", "hope", "night",
];

// smaller groups sit on the nearest shelf, so every prayer has a home
const SHELF: Record<string, string> = {
  Marriage: "Relationships", Travel: "Protection", Deliverance: "Protection", "Spiritual Life": "Faith", Church: "Ministry", Youth: "Family",
};
const shelfOf = (t: PrayerTopic) => SHELF[t.category] ?? t.category;

export const bySlug = new Map(PRAYER_TOPICS.map((t) => [t.slug, t]));
export const exists = (slug: string) => bySlug.has(slug);
export const lite = (t: PrayerTopic) => ({ slug: t.slug, title: t.title });

export type Shelf = { name: string; desc: string; items: { slug: string; title: string }[] };

export function shelves(): Shelf[] {
  return [
    { name: "Most prayed", desc: "The prayers people come back to most.", items: POPULAR.filter(exists).map((s) => lite(bySlug.get(s)!)) },
    ...CATEGORIES.map(([name, desc]) => ({ name, desc, items: PRAYER_TOPICS.filter((t) => shelfOf(t) === name).map(lite) })).filter((g) => g.items.length),
  ];
}
