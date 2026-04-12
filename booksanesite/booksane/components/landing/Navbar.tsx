'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { motion, AnimatePresence } from 'framer-motion';
import { Menu, X } from 'lucide-react';

// ─── Booksane Bird-Book Logo ───────────────
function BooksaneLogo({ size = 40 }: { size?: number }) {
  return (
    <div
      className="flex items-center justify-center shrink-0 overflow-hidden transition-all duration-150"
      style={{
        width: size, height: size,
        background: '#FFE500',
        border: '2px solid #000',
        boxShadow: '3px 3px 0px #000',
      }}
    >
      <BirdBookIcon size={size * 0.72} />
    </div>
  );
}

// Bird whose wings are open book pages
function BirdBookIcon({ size = 28 }: { size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      {/* Left wing — open book pages fanning left */}
      <path d="M24 26 C18 22, 10 20, 4 22 C8 18, 16 16, 24 20 Z" fill="#000" />
      <path d="M24 26 C17 19, 8 15, 2 16 C6 12, 15 12, 24 18 Z" fill="#000" opacity="0.6" />
      <path d="M24 26 C19 16, 12 10, 5 10 C9 7, 17 9, 24 16 Z" fill="#000" opacity="0.35" />

      {/* Right wing — mirror */}
      <path d="M24 26 C30 22, 38 20, 44 22 C40 18, 32 16, 24 20 Z" fill="#000" />
      <path d="M24 26 C31 19, 40 15, 46 16 C42 12, 33 12, 24 18 Z" fill="#000" opacity="0.6" />
      <path d="M24 26 C29 16, 36 10, 43 10 C39 7, 31 9, 24 16 Z" fill="#000" opacity="0.35" />

      {/* Body — book spine */}
      <rect x="21" y="22" width="6" height="16" rx="1" fill="#000" />

      {/* Head */}
      <ellipse cx="24" cy="19" rx="4.5" ry="4" fill="#000" />

      {/* Eye */}
      <circle cx="25.5" cy="18.5" r="1.2" fill="#FFE500" />

      {/* Beak */}
      <path d="M24 21.5 L26 23 L22 23 Z" fill="#000" />

      {/* Tail feathers */}
      <path d="M21 37 L18 43 M24 38 L24 44 M27 37 L30 43" stroke="#000" strokeWidth="1.5" strokeLinecap="round" />

      {/* Book pages line on body */}
      <line x1="21" y1="27" x2="27" y2="27" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="30" x2="27" y2="30" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="33" x2="27" y2="33" stroke="#FFE500" strokeWidth="0.8" />
    </svg>
  );
}

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handler, { passive: true });
    return () => window.removeEventListener('scroll', handler);
  }, []);

  const links = [
    { label: 'How it works', href: '#how-it-works' },
    { label: 'Templates', href: '#templates' },
  ];

  return (
    <>
      <motion.nav
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.4 }}
        className="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
        style={{
          background: scrolled ? 'rgba(10,9,16,0.96)' : 'transparent',
          backdropFilter: scrolled ? 'blur(12px)' : 'none',
          borderBottom: scrolled ? '2px solid rgba(255,255,255,0.12)' : 'none',
        }}
      >
        <div className="container">
          <div className="flex items-center justify-between h-16">

            {/* Logo — Penguin-style: icon + wordmark */}
            <Link href="/" className="flex items-center gap-3 group">
              <div className="group-hover:-translate-x-0.5 group-hover:-translate-y-0.5 transition-transform duration-150">
                <BooksaneLogo size={36} />
              </div>
              {/* Wordmark: two-line like Penguin Random House */}
              <div style={{ borderLeft: '1.5px solid rgba(255,255,255,0.2)', paddingLeft: '12px' }}>
                <div className="font-black leading-none tracking-tight" style={{ fontSize: '13px', color: '#fff', letterSpacing: '0.04em' }}>
                  BOOKSANE
                </div>
                <div className="font-medium leading-none mt-0.5" style={{ fontSize: '9px', color: 'rgba(255,255,255,0.45)', letterSpacing: '0.12em', textTransform: 'uppercase' }}>
                  Formatting System
                </div>
              </div>
            </Link>

            {/* Desktop nav links */}
            <div className="hidden md:flex items-center gap-1">
              {links.map((link) => (
                <a
                  key={link.label}
                  href={link.href}
                  className="px-4 py-2 text-sm font-semibold transition-colors hover:text-white"
                  style={{ color: 'rgba(255,255,255,0.6)' }}
                >
                  {link.label}
                </a>
              ))}
            </div>

            {/* CTA */}
            <div className="hidden md:flex items-center gap-3">
              <Link href="/editor" className="brutal-btn-yellow" style={{ padding: '8px 20px', fontSize: '13px' }}>
                Format Free →
              </Link>
            </div>

            <button
              className="md:hidden p-2"
              style={{ color: 'white' }}
              onClick={() => setMobileOpen(!mobileOpen)}
            >
              {mobileOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>
      </motion.nav>

      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            className="fixed top-16 left-4 right-4 z-40 p-5 flex flex-col gap-2"
            style={{
              background: '#0F0E1A',
              border: '3px solid rgba(255,255,255,0.5)',
              boxShadow: '6px 6px 0px rgba(255,255,255,0.1)',
            }}
          >
            {links.map((link) => (
              <a
                key={link.label}
                href={link.href}
                className="px-3 py-3 text-base font-semibold text-white hover:text-yellow-300 transition-colors"
                onClick={() => setMobileOpen(false)}
              >
                {link.label}
              </a>
            ))}
            <div className="h-px my-2" style={{ background: 'rgba(255,255,255,0.1)' }} />
            <Link
              href="/editor"
              className="brutal-btn-yellow justify-center"
              style={{ width: '100%', justifyContent: 'center', padding: '12px 20px' }}
              onClick={() => setMobileOpen(false)}
            >
              Format for free →
            </Link>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
