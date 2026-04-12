'use client';

import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';
import { Check, Zap, Star } from 'lucide-react';
import Link from 'next/link';

const plans = [
  {
    name: 'Free',
    price: '$0',
    period: 'forever',
    description: 'Everything you need to format and preview.',
    cta: 'Get started free',
    href: '/editor',
    highlighted: false,
    features: ['Full formatting engine', '10 professional templates', 'Paperback & eBook preview', 'PDF export (watermark)', 'EPUB export', 'Unlimited books', 'No account required'],
  },
  {
    name: 'Pro',
    price: '$9',
    period: 'per book',
    description: 'Clean exports, no watermarks. Ready to publish.',
    cta: 'Get Pro — Publish Now',
    href: '/editor',
    highlighted: true,
    badge: 'MOST POPULAR',
    features: ['Everything in Free', 'Clean PDF (no watermark)', 'Print-ready bleed & crop marks', 'Multiple trim sizes (5×8, 6×9, A5)', 'KDP-ready formatting check', 'MOBI + DOCX export', 'Priority support'],
  },
];

export default function Pricing() {
  const { ref, inView } = useInView({ threshold: 0.1, triggerOnce: true });

  return (
    <section id="pricing" ref={ref} className="section" style={{ background: '#0A0910' }}>
      <div className="container">
        <motion.div
          initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}} transition={{ duration: 0.6 }}
          className="mb-14"
        >
          <div className="brutal-tag mb-5" style={{ borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.5)' }}>
            Simple pricing
          </div>
          <h2 className="font-display font-black leading-tight" style={{ fontSize: 'clamp(36px, 5vw, 60px)', color: '#fff' }}>
            Start free.
            <br />
            <span style={{ color: '#FFE500' }}>Pay when you publish.</span>
          </h2>
          <p className="text-lg mt-4 max-w-lg" style={{ color: 'rgba(255,255,255,0.45)' }}>
            No monthly fees. No subscriptions. Pay once per book when you're ready.
          </p>
        </motion.div>

        <div className="grid md:grid-cols-2 gap-6 max-w-2xl">
          {plans.map((plan, i) => (
            <motion.div
              key={plan.name}
              initial={{ opacity: 0, y: 32 }} animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.55, delay: i * 0.15 + 0.2 }}
              className="relative flex flex-col p-8"
              style={plan.highlighted ? {
                background: '#FFE500',
                border: '4px solid #000',
                boxShadow: '8px 8px 0px #000',
              } : {
                background: '#0A0910',
                border: '3px solid rgba(255,255,255,0.25)',
                boxShadow: '5px 5px 0px rgba(255,255,255,0.08)',
              }}
            >
              {/* Badge */}
              {plan.badge && (
                <div className="absolute -top-4 left-6 px-3 py-1 text-xs font-black tracking-widest text-white"
                  style={{ background: '#FF2D78', border: '2px solid #000', boxShadow: '2px 2px 0px #000' }}>
                  {plan.badge}
                </div>
              )}

              <div className="mb-7">
                <div className="flex items-center gap-2 mb-3">
                  {plan.highlighted && <Star size={14} fill="#000" color="#000" />}
                  <div className="text-sm font-black uppercase tracking-widest"
                    style={{ color: plan.highlighted ? '#000' : 'rgba(255,255,255,0.5)' }}>
                    {plan.name}
                  </div>
                </div>
                <div className="flex items-baseline gap-2">
                  <span className="font-black font-display leading-none" style={{ fontSize: '64px', color: plan.highlighted ? '#000' : '#fff' }}>
                    {plan.price}
                  </span>
                  <span className="text-sm font-bold" style={{ color: plan.highlighted ? 'rgba(0,0,0,0.5)' : 'rgba(255,255,255,0.4)' }}>
                    / {plan.period}
                  </span>
                </div>
                <p className="text-sm font-semibold mt-2" style={{ color: plan.highlighted ? 'rgba(0,0,0,0.65)' : 'rgba(255,255,255,0.45)' }}>
                  {plan.description}
                </p>
              </div>

              <ul className="flex flex-col gap-3 mb-8 flex-1">
                {plan.features.map(feat => (
                  <li key={feat} className="flex items-center gap-3 text-sm">
                    <div className="w-5 h-5 flex items-center justify-center flex-shrink-0"
                      style={{ background: plan.highlighted ? '#000' : 'rgba(255,255,255,0.1)', border: `1px solid ${plan.highlighted ? '#000' : 'rgba(255,255,255,0.15)'}` }}>
                      <Check size={11} color={plan.highlighted ? '#FFE500' : '#00FF7F'} strokeWidth={3} />
                    </div>
                    <span className="font-semibold" style={{ color: plan.highlighted ? '#000' : 'rgba(255,255,255,0.7)' }}>{feat}</span>
                  </li>
                ))}
              </ul>

              <Link
                href={plan.href}
                className={plan.highlighted ? 'brutal-btn-yellow' : 'brutal-btn-outline-white'}
                style={plan.highlighted ? {
                  background: '#000', color: '#FFE500', borderColor: '#000', width: '100%', justifyContent: 'center', fontSize: '15px', padding: '14px 20px'
                } : {
                  width: '100%', justifyContent: 'center', fontSize: '15px', padding: '14px 20px'
                }}
              >
                {plan.highlighted && <Zap size={15} />}
                {plan.cta}
              </Link>
            </motion.div>
          ))}
        </div>

        <motion.p
          initial={{ opacity: 0 }} animate={inView ? { opacity: 1 } : {}} transition={{ duration: 0.6, delay: 0.6 }}
          className="text-sm mt-8" style={{ color: 'rgba(255,255,255,0.35)' }}
        >
          Questions? Email{' '}
          <a href="mailto:hello@booksane.com" className="underline" style={{ color: 'rgba(255,255,255,0.55)' }}>
            hello@booksane.com
          </a>
        </motion.p>
      </div>
    </section>
  );
}
