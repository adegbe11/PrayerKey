import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        pk: {
          // Backgrounds — Apple production light
          deep:    "#F5F5F7",
          sidebar: "#F5F5F7",
          panel:   "#FFFFFF",
          card:    "#FFFFFF",
          hover:   "#F5F5F7",
          // Brand gold (darkened for 4.5:1 contrast on white)
          gold:         "#B07C1F",
          "gold-dim":   "rgba(176,124,31,0.08)",
          "gold-border":"rgba(176,124,31,0.2)",
          // Accents — Apple system colours
          teal:   "#00C7BE",
          purple: "#AF52DE",
          coral:  "#FF6961",
          live:   "#FF3B30",
          blue:   "#0071E3",
          // Text — Apple spec
          t1: "#1D1D1F",
          t2: "#6E6E73",
          t3: "#86868B",
          // Borders — Apple spec
          b1: "rgba(0,0,0,0.08)",
          b2: "rgba(0,0,0,0.15)",
        },
      },
      fontFamily: {
        sans: [
          '"SF Pro Text"', '"SF Pro Icons"', "-apple-system",
          "BlinkMacSystemFont", '"Helvetica Neue"', "Helvetica", "Arial", "sans-serif",
        ],
        display: [
          '"SF Pro Display"', '"SF Pro Icons"', "-apple-system",
          "BlinkMacSystemFont", '"Helvetica Neue"', "Helvetica", "Arial", "sans-serif",
        ],
        mono: ['"SF Mono"', '"Courier New"', "monospace"],
      },
      animation: {
        "live-pulse": "livePulse 1.2s cubic-bezier(.4,0,.6,1) infinite",
        "wave-1": "wave 1.2s cubic-bezier(.4,0,.6,1) infinite",
        "wave-2": "wave 1.2s cubic-bezier(.4,0,.6,1) 0.2s infinite",
        "wave-3": "wave 1.2s cubic-bezier(.4,0,.6,1) 0.4s infinite",
        "wave-4": "wave 1.2s cubic-bezier(.4,0,.6,1) 0.6s infinite",
        "wave-5": "wave 1.2s cubic-bezier(.4,0,.6,1) 0.8s infinite",
      },
      keyframes: {
        livePulse: {
          "0%, 100%": { opacity: "1" },
          "50%": { opacity: "0.3" },
        },
        wave: {
          "0%, 100%": { transform: "scaleY(0.35)" },
          "50%": { transform: "scaleY(1)" },
        },
      },
    },
  },
  plugins: [],
};
export default config;
