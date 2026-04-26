"use client";
import { useEffect, useState } from "react";

export default function DarkModeToggle() {
  const [dark, setDark] = useState(false);
  const [mounted, setMounted] = useState(false);

  /* Read saved preference on mount */
  useEffect(() => {
    setMounted(true);
    const saved = localStorage.getItem("pk-theme");
    if (saved === "dark") {
      setDark(true);
    } else {
      // Default is light — make sure html attribute is set
      document.documentElement.setAttribute("data-theme", "light");
    }
  }, []);

  function toggle() {
    const next = !dark;
    setDark(next);
    const theme = next ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("pk-theme", theme);
  }

  /* Avoid hydration mismatch */
  if (!mounted) {
    return (
      <button
        className="pk-theme-toggle"
        aria-label="Toggle theme"
        style={{ opacity: 0, pointerEvents: "none" }}
      >
        <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="5" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" /></svg>
      </button>
    );
  }

  return (
    <button
      onClick={toggle}
      className="pk-theme-toggle"
      aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}
      title={dark ? "Light mode" : "Dark mode"}
    >
      {dark ? (
        /* Sun icon — shown in dark mode to switch to light */
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="5" />
          <line x1="12" y1="1"  x2="12" y2="3" />
          <line x1="12" y1="21" x2="12" y2="23" />
          <line x1="4.22" y1="4.22"   x2="5.64" y2="5.64" />
          <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
          <line x1="1"  y1="12" x2="3"  y2="12" />
          <line x1="21" y1="12" x2="23" y2="12" />
          <line x1="4.22" y1="19.78"  x2="5.64" y2="18.36" />
          <line x1="18.36" y1="5.64"  x2="19.78" y2="4.22" />
        </svg>
      ) : (
        /* Moon icon — shown in light mode to switch to dark */
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      )}
    </button>
  );
}
