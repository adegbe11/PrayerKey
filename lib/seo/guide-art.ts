// The painting on each guide's cover.
const ART: Record<string, string> = {
  "how-to-pray": "/art/window-prayer.webp",
  "morning-prayers": "/art/dawn-mountain.webp",
  "bible-verses-about-healing": "/art/heaven-hands.webp",
  "prayers-for-anxiety-and-stress": "/art/reflection.webp",
  "financial-breakthrough-prayers": "/art/open-door.webp",
  "prayers-for-marriage-and-family": "/art/morning-prayer.webp",
  "how-to-read-the-bible": "/art/bible-key.webp",
  "night-prayers": "/art/evening-prayer.webp",
};

export function guideArt(slug: string) {
  return ART[slug] ?? "/art/window-prayer.webp";
}
