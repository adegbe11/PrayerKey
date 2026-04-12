'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useInView } from 'react-intersection-observer';
import Link from 'next/link';

// ─────────────────────────────────────────
//  MINI TEMPLATE PREVIEWS
// ─────────────────────────────────────────

const templatePreviews = [
  {
    id: 'classic-novel',
    name: 'Classic Novel',
    category: 'Fiction',
    bg: '#F9F6F1',
    ink: '#1A1208',
    accent: '#8B5E3C',
    headingFont: '"EB Garamond", Georgia, serif',
    bodyFont: '"EB Garamond", Georgia, serif',
    headingAlign: 'center' as const,
    headingWeight: '400',
    headingTransform: 'none' as const,
    indent: true,
  },
  {
    id: 'modern-fiction',
    name: 'Modern Fiction',
    category: 'Fiction',
    bg: '#FFFFFF',
    ink: '#111111',
    accent: '#333333',
    headingFont: 'Inter, sans-serif',
    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingAlign: 'left' as const,
    headingWeight: '700',
    headingTransform: 'uppercase' as const,
    indent: false,
  },
  {
    id: 'business',
    name: 'Business Book',
    category: 'Nonfiction',
    bg: '#F8F9FA',
    ink: '#1A1A2E',
    accent: '#0D47A1',
    headingFont: 'Inter, sans-serif',
    bodyFont: 'Inter, sans-serif',
    headingAlign: 'left' as const,
    headingWeight: '800',
    headingTransform: 'none' as const,
    indent: false,
  },
  {
    id: 'memoir',
    name: 'Memoir',
    category: 'Memoir',
    bg: '#FDF8F2',
    ink: '#2D1F17',
    accent: '#7D4F35',
    headingFont: '"Playfair Display", Georgia, serif',
    bodyFont: '"Lora", Georgia, serif',
    headingAlign: 'left' as const,
    headingWeight: '400',
    headingTransform: 'none' as const,
    indent: true,
  },
  {
    id: 'romance',
    name: 'Romance',
    category: 'Fiction',
    bg: '#FFF5F7',
    ink: '#1A0A0E',
    accent: '#AD1457',
    headingFont: '"Playfair Display", Georgia, serif',
    bodyFont: '"Lora", Georgia, serif',
    headingAlign: 'center' as const,
    headingWeight: '400',
    headingTransform: 'none' as const,
    indent: true,
  },
  {
    id: 'self-help',
    name: 'Self-Help',
    category: 'Nonfiction',
    bg: '#FFFBEB',
    ink: '#1C1100',
    accent: '#D97706',
    headingFont: 'Inter, sans-serif',
    bodyFont: 'Inter, sans-serif',
    headingAlign: 'left' as const,
    headingWeight: '900',
    headingTransform: 'none' as const,
    indent: false,
  },
  {
    id: 'minimalist',
    name: 'Minimalist',
    category: 'Nonfiction',
    bg: '#FFFFFF',
    ink: '#000000',
    accent: '#000000',
    headingFont: 'Inter, sans-serif',
    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingAlign: 'center' as const,
    headingWeight: '300',
    headingTransform: 'uppercase' as const,
    indent: false,
  },
  {
    id: 'thriller',
    name: 'Thriller',
    category: 'Fiction',
    bg: '#F2F2F2',
    ink: '#0A0A0A',
    accent: '#C62828',
    headingFont: 'Inter, sans-serif',
    bodyFont: '"Libre Baskerville", Georgia, serif',
    headingAlign: 'left' as const,
    headingWeight: '900',
    headingTransform: 'uppercase' as const,
    indent: true,
  },
  {
    id: 'poetry',
    name: 'Poetry',
    category: 'Poetry',
    bg: '#F9F8FF',
    ink: '#1A1428',
    accent: '#4527A0',
    headingFont: '"Playfair Display", Georgia, serif',
    bodyFont: '"EB Garamond", Georgia, serif',
    headingAlign: 'center' as const,
    headingWeight: '400',
    headingTransform: 'none' as const,
    indent: false,
  },
  {
    id: 'academic',
    name: 'Academic',
    category: 'Nonfiction',
    bg: '#FAFAFA',
    ink: '#000000',
    accent: '#1A237E',
    headingFont: '"Times New Roman", Times, serif',
    bodyFont: '"Times New Roman", Times, serif',
    headingAlign: 'left' as const,
    headingWeight: '700',
    headingTransform: 'none' as const,
    indent: true,
  },
];

// ─────────────────────────────────────────
//  MINI BOOK PAGE PREVIEW
// ─────────────────────────────────────────

function MiniBookPage({ template }: { template: typeof templatePreviews[0] }) {
  return (
    <div
      className="w-full h-full p-5 overflow-hidden"
      style={{
        background: template.bg,
        fontFamily: template.bodyFont,
      }}
    >
      {/* Chapter heading area */}
      <div
        className="mb-4"
        style={{ paddingTop: '20%', textAlign: template.headingAlign }}
      >
        <div
          className="text-xs mb-1"
          style={{
            color: template.accent,
            fontFamily: template.headingFont,
            fontWeight: template.headingWeight,
            textTransform: template.headingTransform,
            letterSpacing: template.headingTransform === 'uppercase' ? '0.1em' : '0',
            fontSize: '8px',
          }}
        >
          Chapter One
        </div>
        <div
          className="text-sm font-medium"
          style={{
            fontFamily: template.headingFont,
            color: template.ink,
            fontWeight: template.headingWeight,
            textTransform: template.headingTransform,
            fontSize: '11px',
            lineHeight: 1.3,
          }}
        >
          The Beginning
        </div>
      </div>

      {/* Text paragraphs */}
      {[
        'The storm had been building for three days before it finally broke.',
        'She stood at the window, watching the sea.',
        'The notebook on her desk was still blank.',
      ].map((text, i) => (
        <p
          key={i}
          className="mb-1"
          style={{
            fontSize: '7px',
            lineHeight: 1.6,
            color: template.ink,
            opacity: 0.75,
            textIndent: template.indent && i > 0 ? '1em' : '0',
            marginBottom: template.indent ? '0' : '3px',
          }}
        >
          {text}
        </p>
      ))}

      {/* Page number */}
      <div
        className="absolute bottom-3 left-0 right-0 text-center"
        style={{ fontSize: '6px', color: template.ink, opacity: 0.3 }}
      >
        1
      </div>
    </div>
  );
}

// ─────────────────────────────────────────
//  TEMPLATE CARD
// ─────────────────────────────────────────

function TemplateCard({
  template,
  selected,
  onClick,
  index,
  inView,
}: {
  template: typeof templatePreviews[0];
  selected: boolean;
  onClick: () => void;
  index: number;
  inView: boolean;
}) {
  return (
    <motion.button
      initial={{ opacity: 0, y: 24 }}
      animate={inView ? { opacity: 1, y: 0 } : {}}
      transition={{ duration: 0.5, delay: index * 0.07 }}
      onClick={onClick}
      className="group relative flex flex-col text-left focus:outline-none"
      style={{ cursor: 'pointer' }}
    >
      {/* Page preview */}
      <div
        className="relative rounded-xl overflow-hidden transition-all duration-300"
        style={{
          width: '100%',
          aspectRatio: '2/3',
          border: selected
            ? '2px solid var(--accent)'
            : '1px solid rgba(255,255,255,0.07)',
          boxShadow: selected
            ? '0 0 0 4px rgba(124,58,237,0.15), 0 16px 40px rgba(0,0,0,0.4)'
            : '0 8px 24px rgba(0,0,0,0.3)',
          transform: selected ? 'translateY(-4px)' : 'translateY(0)',
          transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
        }}
      >
        <div className="absolute inset-0">
          <MiniBookPage template={template} />
        </div>

        {/* Hover overlay */}
        <div
          className="absolute inset-0 transition-opacity duration-200"
          style={{
            background: 'rgba(124,58,237,0.08)',
            opacity: selected ? 0 : 0,
          }}
        />
      </div>

      {/* Label */}
      <div className="mt-2.5 px-0.5">
        <div
          className="text-sm font-medium"
          style={{ color: selected ? 'var(--accent-light)' : 'var(--text-primary)' }}
        >
          {template.name}
        </div>
        <div className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
          {template.category}
        </div>
      </div>
    </motion.button>
  );
}

// ─────────────────────────────────────────
//  TEMPLATE SHOWCASE
// ─────────────────────────────────────────

export default function TemplateShowcase() {
  const [selected, setSelected] = useState(0);
  const { ref, inView } = useInView({ threshold: 0.1, triggerOnce: true });

  return (
    <section id="templates" ref={ref} className="section" style={{ background: 'var(--void-2)' }}>
      <div className="container">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.6 }}
          className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10"
        >
          <div>
            <div className="section-label mb-4">10 Templates</div>
            <h2 className="section-title">
              Pick your style.
              <br />
              <span className="text-gradient">Switch instantly.</span>
            </h2>
          </div>
          <Link href="/editor" className="btn btn-primary">
            Try all templates →
          </Link>
        </motion.div>

        {/* Template grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {templatePreviews.map((t, i) => (
            <TemplateCard
              key={t.id}
              template={t}
              selected={selected === i}
              onClick={() => setSelected(i)}
              index={i}
              inView={inView}
            />
          ))}
        </div>

        {/* Selected template detail */}
        <AnimatePresence mode="wait">
          <motion.div
            key={selected}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            transition={{ duration: 0.3 }}
            className="mt-8 p-4 rounded-xl flex items-center gap-4"
            style={{
              background: 'rgba(124,58,237,0.05)',
              border: '1px solid rgba(124,58,237,0.15)',
            }}
          >
            <div
              className="w-2 h-2 rounded-full"
              style={{ background: 'var(--accent)', flexShrink: 0 }}
            />
            <div>
              <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>
                {templatePreviews[selected].name}
              </span>
              <span className="text-sm ml-2" style={{ color: 'var(--text-muted)' }}>
                · {templatePreviews[selected].category} ·{' '}
                {templatePreviews[selected].indent ? 'First-line indent' : 'Block paragraph'} style
              </span>
            </div>
            <Link href="/editor" className="ml-auto btn btn-ghost text-sm py-1.5 px-3">
              Use this template →
            </Link>
          </motion.div>
        </AnimatePresence>
      </div>
    </section>
  );
}
