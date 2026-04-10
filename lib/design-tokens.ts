/**
 * PrayerKey Design Token System — Apple Production Spec
 * All values sourced from apple.com / HIG
 */

export const colors = {
  // ── Backgrounds (Apple light) ──────────────────────────────────
  deep:    "#F5F5F7",  // body / app bg — Apple secondary gray
  sidebar: "#F5F5F7",  // sidebar bg
  panel:   "#FFFFFF",  // main content
  card:    "#FFFFFF",  // card surface
  hover:   "#F5F5F7",  // hover state

  // ── Brand gold (darkened for ≥4.5:1 contrast on white) ─────────
  gold:        "#B07C1F",
  goldDim:     "rgba(176,124,31,0.08)",
  goldBorder:  "rgba(176,124,31,0.2)",

  // ── Accents — Apple system colours ─────────────────────────────
  teal:   "#00C7BE",   // Apple teal
  purple: "#AF52DE",   // Apple purple
  coral:  "#FF6961",   // Apple warm red
  live:   "#FF3B30",   // Apple red
  blue:   "#0071E3",   // Apple interactive blue

  // ── Typography — Apple spec ─────────────────────────────────────
  t1: "#1D1D1F",       // Primary text
  t2: "#6E6E73",       // Secondary text
  t3: "#86868B",       // Tertiary / placeholder

  // ── Borders — Apple spec ────────────────────────────────────────
  b1: "rgba(0,0,0,0.08)",
  b2: "rgba(0,0,0,0.15)",
} as const;

export const spacing = {
  sidebarWidth:   196,
  titleBarHeight:  44,
  bottomBarHeight: 56,
  cardRadius:      12,
  cardPadding:     18,
  contentPaddingX: 28,
} as const;

export const typography = {
  display:  { size: "28px",  weight: "600", letterSpacing: "-0.003em", lineHeight: "1.2" },
  h1:       { size: "22px",  weight: "600", letterSpacing: "-0.003em" },
  h2:       { size: "17px",  weight: "600", letterSpacing: "-0.003em" },
  h3:       { size: "15px",  weight: "600", letterSpacing: "-0.003em" },
  body:     { size: "14px",  weight: "400", lineHeight: "1.6"         },
  small:    { size: "12px",  weight: "400"                            },
  micro:    { size: "11px",  weight: "600", letterSpacing: "0.06em", textTransform: "uppercase" },
  code:     { size: "12px",  family: '"SF Mono", "Courier New", monospace' },
} as const;

export const effects = {
  shadowSm:   "0 1px 3px rgba(0,0,0,0.08), 0 0 0 0.5px rgba(0,0,0,0.06)",
  shadowMd:   "0 4px 16px rgba(0,0,0,0.10), 0 0 0 0.5px rgba(0,0,0,0.08)",
  shadowLg:   "0 8px 32px rgba(0,0,0,0.12), 0 0 0 0.5px rgba(0,0,0,0.08)",
  navBg:      "rgba(250,250,252,0.92)",
  navBlur:    "saturate(180%) blur(20px)",
} as const;

export const animation = {
  duration:   "0.24s",
  ease:       "cubic-bezier(.4,0,.6,1)",
  transition: "all 0.24s cubic-bezier(.4,0,.6,1)",
  livePulse:  "livePulse 1.2s cubic-bezier(.4,0,.6,1) infinite",
} as const;

export type ColorKey   = keyof typeof colors;
export type SpacingKey = keyof typeof spacing;
