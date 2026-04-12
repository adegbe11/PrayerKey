'use client';

import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';
import { BookOpen, Zap, FileDown, Eye, Wand2, Globe, Layers, PenTool } from 'lucide-react';
import Link from 'next/link';

const features = [
  { icon: Zap, title: 'Instant formatting', description: 'Paste your text. Get a professionally laid-out book in under 3 seconds.', accent: '#FFE500', dark: true },
  { icon: Eye, title: 'Live dual preview', description: 'See your book as a paperback spread and as a Kindle eBook. Both. Side by side.', accent: '#A78BFA', dark: false },
  { icon: BookOpen, title: 'Chapter detection', description: 'Booksane reads your chapter headings and builds the structure for you.', accent: '#00FF7F', dark: true },
  { icon: Wand2, title: 'Fix My Book', description: 'One click. Messy manuscript in, clean formatted book out. Every time.', accent: '#FF2D78', dark: false },
  { icon: FileDown, title: 'PDF + EPUB export', description: 'Print-ready PDF with proper margins. Clean EPUB for Kindle and Apple Books.', accent: '#00C8FF', dark: true },
  { icon: Layers, title: '10 templates', description: 'Novel, Business, Memoir, Romance, Poetry, Thriller. Switch between any of them in one click.', accent: '#FFE500', dark: false },
  { icon: Globe, title: 'Upload anything', description: 'Accepts .docx, .txt, and .pdf files. Or just paste your text directly.', accent: '#00FF7F', dark: true },
  { icon: PenTool, title: 'Completely free', description: 'No account. No credit card. No watermarks. Format as many books as you want.', accent: '#FF2D78', dark: false },
];

export default function Features() {
  const { ref, inView } = useInView({ threshold: 0.08, triggerOnce: true });

  return (
    <section className="section" ref={ref} style={{ background: '#0A0910' }}>
      <div className="container">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}} transition={{ duration: 0.6 }}
          className="mb-10"
        >
          <div className="brutal-tag mb-5" style={{ borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.5)' }}>
            Everything included
          </div>
          <h2 className="font-display font-black leading-tight" style={{ fontSize: 'clamp(36px, 5vw, 60px)', color: '#fff' }}>
            Faster than InDesign.
            <br />
            <span style={{ color: '#FFE500' }}>Simpler than everything else.</span>
          </h2>
        </motion.div>

        {/* Grid */}
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {features.map((feat, i) => {
            const Icon = feat.icon;
            return (
              <motion.div
                key={feat.title}
                initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.45, delay: i * 0.06 + 0.1 }}
                className="p-6 flex flex-col gap-4 group"
                style={{
                  background: '#0A0910',
                  border: '3px solid rgba(255,255,255,0.12)',
                  borderTop: `3px solid ${feat.accent}`,
                  boxShadow: `0px 4px 0px ${feat.accent}30`,
                  transition: 'transform 0.15s, box-shadow 0.15s, border-color 0.15s',
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLElement).style.transform = 'translateY(-4px)';
                  (e.currentTarget as HTMLElement).style.borderColor = feat.accent;
                  (e.currentTarget as HTMLElement).style.boxShadow = `0px 8px 0px ${feat.accent}50`;
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLElement).style.transform = '';
                  (e.currentTarget as HTMLElement).style.borderColor = 'rgba(255,255,255,0.12)';
                  (e.currentTarget as HTMLElement).style.borderTopColor = feat.accent;
                  (e.currentTarget as HTMLElement).style.boxShadow = `0px 4px 0px ${feat.accent}30`;
                }}
              >
                <div className="w-10 h-10 flex items-center justify-center"
                  style={{ background: `${feat.accent}20`, border: `2px solid ${feat.accent}40` }}>
                  <Icon size={18} color={feat.accent} strokeWidth={2} />
                </div>
                <div>
                  <h3 className="text-sm font-black mb-1.5" style={{ color: '#fff' }}>{feat.title}</h3>
                  <p className="text-xs leading-relaxed" style={{ color: 'rgba(255,255,255,0.45)' }}>{feat.description}</p>
                </div>
              </motion.div>
            );
          })}
        </div>

        {/* Brutal CTA block */}
        <motion.div
          initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.6, delay: 0.55 }}
          className="mt-10 p-8 flex flex-col sm:flex-row items-center justify-between gap-6"
          style={{
            background: '#FFE500',
            border: '3px solid #000',
            boxShadow: '6px 6px 0px #000',
          }}
        >
          <div>
            <p className="text-2xl font-black text-black leading-tight">Your book won&apos;t format itself.</p>
            <p className="text-sm font-semibold mt-1" style={{ color: 'rgba(0,0,0,0.55)' }}>
              No account. No credit card. Completely free.
            </p>
          </div>
          <Link href="/editor" className="brutal-btn-yellow shrink-0" style={{ borderColor: '#000', background: '#000', color: '#FFE500' }}>
            Format for free →
          </Link>
        </motion.div>
      </div>
    </section>
  );
}
