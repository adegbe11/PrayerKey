'use client';

import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';
import { Upload, Wand2, Download } from 'lucide-react';
import Link from 'next/link';

const steps = [
  {
    number: '01',
    icon: Upload,
    title: 'Paste or upload',
    description: 'Drop your raw manuscript. Plain text, Word doc, or PDF. No formatting needed. The messier the better.',
    detail: 'Supports .docx · .txt · .pdf',
    accentColor: '#7C3AED',
    bgColor: 'rgba(124,58,237,0.08)',
  },
  {
    number: '02',
    icon: Wand2,
    title: 'Pick a template',
    description: 'Choose from 10 professionally designed templates. Booksane reads your chapter titles, fixes spacing, and lays out every page perfectly.',
    detail: '10 templates. Switch anytime.',
    accentColor: '#FFE500',
    bgColor: 'rgba(255,229,0,0.06)',
  },
  {
    number: '03',
    icon: Download,
    title: 'Export & publish',
    description: 'Download a print-ready PDF or a clean EPUB. KDP-compatible. Ready for Amazon, Apple Books, or any retailer.',
    detail: 'PDF + EPUB in seconds',
    accentColor: '#00FF7F',
    bgColor: 'rgba(0,255,127,0.06)',
  },
];

export default function HowItWorks() {
  const { ref, inView } = useInView({ threshold: 0.1, triggerOnce: true });

  return (
    <section id="how-it-works" ref={ref} className="section relative" style={{ background: '#0A0910' }}>
      <div className="container">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}} transition={{ duration: 0.6 }}
          className="mb-10"
        >
          <div className="brutal-tag mb-5" style={{ borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.5)' }}>
            The Process
          </div>
          <h2 className="font-display font-black leading-tight mb-4"
            style={{ fontSize: 'clamp(36px, 5vw, 60px)', color: '#fff' }}>
            Three steps from raw text
            <br />
            <span style={{ color: '#FFE500' }}>to published book.</span>
          </h2>
          <p className="text-lg max-w-xl" style={{ color: 'rgba(255,255,255,0.5)' }}>
            No design skills. No software. No waiting.
          </p>
        </motion.div>

        {/* Steps */}
        <div className="grid md:grid-cols-3 gap-6">
          {steps.map((step, i) => {
            const Icon = step.icon;
            return (
              <motion.div
                key={step.number}
                initial={{ opacity: 0, y: 32 }} animate={inView ? { opacity: 1, y: 0 } : {}}
                transition={{ duration: 0.5, delay: i * 0.12 + 0.2 }}
                className="relative flex flex-col gap-5 p-7"
                style={{
                  background: step.bgColor,
                  border: `3px solid ${step.accentColor}`,
                  boxShadow: `6px 6px 0px ${step.accentColor}40`,
                  transition: 'transform 0.15s, box-shadow 0.15s',
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLElement).style.transform = 'translate(-3px,-3px)';
                  (e.currentTarget as HTMLElement).style.boxShadow = `9px 9px 0px ${step.accentColor}50`;
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLElement).style.transform = '';
                  (e.currentTarget as HTMLElement).style.boxShadow = `6px 6px 0px ${step.accentColor}40`;
                }}
              >
                {/* Giant step number */}
                <div className="font-black font-display leading-none" style={{ fontSize: '80px', color: step.accentColor, opacity: 0.15 }}>
                  {step.number}
                </div>

                {/* Icon */}
                <div className="w-12 h-12 flex items-center justify-center -mt-8"
                  style={{ background: step.accentColor, border: '2px solid rgba(0,0,0,0.2)' }}>
                  <Icon size={22} color={step.accentColor === '#FFE500' ? '#000' : '#000'} strokeWidth={2} />
                </div>

                {/* Content */}
                <div>
                  <h3 className="text-xl font-black mb-2" style={{ color: '#fff' }}>{step.title}</h3>
                  <p className="text-sm leading-relaxed mb-4" style={{ color: 'rgba(255,255,255,0.55)' }}>{step.description}</p>
                  <span className="text-xs font-bold uppercase tracking-widest" style={{ color: step.accentColor }}>
                    {step.detail}
                  </span>
                </div>
              </motion.div>
            );
          })}
        </div>

        {/* CTA row */}
        <motion.div
          initial={{ opacity: 0, y: 16 }} animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.5, delay: 0.7 }}
          className="mt-12 flex flex-wrap items-center gap-4"
        >
          <Link href="/editor" className="brutal-btn-yellow" style={{ fontSize: '15px' }}>
            Try it free now →
          </Link>
          <p className="text-sm" style={{ color: 'rgba(255,255,255,0.35)' }}>No account needed · Works instantly</p>
        </motion.div>
      </div>
    </section>
  );
}
