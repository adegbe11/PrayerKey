import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        'nm-red':    '#FF4444',
        'nm-orange': '#FF8800',
        'nm-yellow': '#FFDD00',
        'nm-green':  '#44CC44',
        'nm-blue':   '#0088FF',
        'nm-indigo': '#3344CC',
        'nm-violet': '#9933CC',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"SF Pro Display"', '"SF Pro Text"', '"Segoe UI"', 'system-ui', 'sans-serif'],
      },
      keyframes: {
        confettiFall: {
          '0%':   { transform: 'translateY(-20px) rotate(0deg)',   opacity: '1' },
          '100%': { transform: 'translateY(110vh) rotate(720deg)', opacity: '0' },
        },
        rainbowSweep: {
          '0%':   { background: '#FF4444', opacity: '0.85' },
          '14%':  { background: '#FF8800' },
          '28%':  { background: '#FFDD00' },
          '42%':  { background: '#44CC44' },
          '57%':  { background: '#0088FF' },
          '71%':  { background: '#3344CC' },
          '85%':  { background: '#9933CC' },
          '100%': { opacity: '0' },
        },
        navBounce: {
          '0%':   { transform: 'scale(1)' },
          '50%':  { transform: 'scale(1.22)' },
          '100%': { transform: 'scale(1)' },
        },
        shimmer: {
          '0%':   { backgroundPosition: '200% 0' },
          '100%': { backgroundPosition: '-200% 0' },
        },
      },
      animation: {
        'confetti-fall':  'confettiFall var(--fall-duration, 2s) var(--fall-delay, 0s) ease-in both',
        'rainbow-sweep':  'rainbowSweep 600ms ease-out forwards',
        'nav-bounce':     'navBounce 200ms cubic-bezier(0.34,1.56,0.64,1)',
        'shimmer':        'shimmer 1.5s infinite',
      },
    },
  },
  plugins: [],
};

export default config;
