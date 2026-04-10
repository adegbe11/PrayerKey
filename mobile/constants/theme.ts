// PrayerKey mobile design tokens — mirrors the web Apple-spec system

export const Colors = {
  // Backgrounds
  bg:     "#F5F5F7",
  card:   "#FFFFFF",
  panel:  "#FFFFFF",

  // Text
  t1:     "#1D1D1F",
  t2:     "#6E6E73",
  t3:     "#86868B",

  // Brand
  gold:   "#B07C1F",
  goldDim:"rgba(176,124,31,0.10)",

  // System
  blue:   "#0071E3",
  green:  "#34C759",
  red:    "#FF3B30",
  orange: "#FF9F0A",
  purple: "#AF52DE",
  teal:   "#00C7BE",

  // Borders
  border: "rgba(0,0,0,0.08)",
  border2:"rgba(0,0,0,0.15)",

  // Shadows
  shadowColor: "#000",
} as const;

export const Spacing = {
  xs:  4,
  sm:  8,
  md:  16,
  lg:  24,
  xl:  32,
  xxl: 48,
} as const;

export const Radius = {
  sm:   8,
  md:   12,
  lg:   16,
  xl:   20,
  pill: 980,
} as const;

export const FontSize = {
  xs:   11,
  sm:   12,
  base: 14,
  md:   15,
  lg:   17,
  xl:   20,
  xxl:  24,
  hero: 28,
} as const;
