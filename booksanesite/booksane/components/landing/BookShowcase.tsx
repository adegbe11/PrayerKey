'use client';

import { useRef } from 'react';
import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';

const BOOKS = [
  {
    title: 'DANGEROUS PRAYERS THAT STOP THE DEVIL',
    asin: 'B0GWRDWWBN',
    url: 'https://www.amazon.com/dp/B0GWRDWWBN',
    accent: '#FF2D78',
  },
  {
    title: 'I WILL PRAY',
    asin: 'B0GWQXCPHN',
    url: 'https://www.amazon.com/dp/B0GWQXCPHN',
    accent: '#FFE500',
  },
  {
    title: 'PRAYER: Powerful Prayers for Every Situation',
    asin: 'B0GK6WV6TF',
    url: 'https://www.amazon.com/dp/B0GK6WV6TF',
    accent: '#7C3AED',
  },
  {
    title: '50 NATURAL WAYS TO ENLARGE YOUR MANHOOD',
    asin: 'B0FFG597WW',
    url: 'https://www.amazon.com/dp/B0FFG597WW',
    accent: '#00FF7F',
  },
  {
    title: 'Prayers That Cover My Child',
    asin: 'B0GVZDKF5F',
    url: 'https://www.amazon.com/dp/B0GVZDKF5F',
    accent: '#FF2D78',
  },
  {
    title: 'Spiritual Warfare Prayers for Women',
    asin: 'B0GVZ6P5TJ',
    url: 'https://www.amazon.com/dp/B0GVZ6P5TJ',
    accent: '#FFE500',
  },
  {
    title: 'Blood of Jesus Prayer Points with Scriptures',
    asin: 'B0GPKCD4TF',
    url: 'https://www.amazon.com/dp/B0GPKCD4TF',
    accent: '#7C3AED',
  },
  {
    title: 'Dream Interpretation Book for Christians',
    asin: 'B0GTQXPDHN',
    url: 'https://www.amazon.com/dp/B0GTQXPDHN',
    accent: '#00FF7F',
  },
  {
    title: '5000 Prayer Points for Women',
    asin: 'B0GT5PRKBY',
    url: 'https://www.amazon.com/dp/B0GT5PRKBY',
    accent: '#FF2D78',
  },
  {
    title: "ALIENS: They're Not Coming. They're Already Here",
    asin: 'B0GQMXNTTJ',
    url: 'https://www.amazon.com/dp/B0GQMXNTTJ',
    accent: '#00FF7F',
  },
];

// Duplicate books for seamless infinite loop
const LOOP_BOOKS = [...BOOKS, ...BOOKS];

function BookCard({ book, index }: { book: typeof BOOKS[0]; index: number }) {
  return (
    <a
      href={book.url}
      target="_blank"
      rel="noopener noreferrer"
      className="shrink-0 group relative"
      style={{ width: '130px' }}
      title={book.title}
    >
      <div
        className="relative overflow-hidden transition-all duration-200 group-hover:-translate-y-2"
        style={{
          width: '130px',
          height: '185px',
          border: '3px solid #000',
          boxShadow: '5px 5px 0px #000',
        }}
      >
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={`https://images-na.ssl-images-amazon.com/images/P/${book.asin}.01._SX300_.jpg`}
          alt={book.title}
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
          onError={(e) => {
            // Fallback to a styled placeholder if image fails
            const img = e.currentTarget;
            img.style.display = 'none';
            const parent = img.parentElement;
            if (parent && !parent.querySelector('.book-fallback')) {
              const fb = document.createElement('div');
              fb.className = 'book-fallback';
              fb.style.cssText = `
                width:100%;height:100%;display:flex;flex-direction:column;
                align-items:center;justify-content:center;padding:12px;
                background:${book.accent};text-align:center;
              `;
              const title = document.createElement('p');
              title.textContent = book.title;
              title.style.cssText = 'font-size:9px;font-weight:900;color:#000;line-height:1.3;text-transform:uppercase;letter-spacing:0.04em;';
              fb.appendChild(title);
              parent.appendChild(fb);
            }
          }}
        />
        {/* Hover overlay */}
        <div
          className="absolute inset-0 flex items-end opacity-0 group-hover:opacity-100 transition-opacity duration-200"
          style={{ background: 'linear-gradient(to top, rgba(0,0,0,0.85) 0%, transparent 50%)' }}
        >
          <div className="p-2 w-full">
            <div
              className="w-full text-center text-xs font-black py-1"
              style={{ background: book.accent, color: '#000', fontSize: '9px', letterSpacing: '0.05em' }}
            >
              VIEW ON AMAZON →
            </div>
          </div>
        </div>
      </div>
    </a>
  );
}

export default function BookShowcase() {
  const { ref, inView } = useInView({ threshold: 0.1, triggerOnce: true });
  const trackRef = useRef<HTMLDivElement>(null);

  return (
    <section
      ref={ref}
      className="section overflow-hidden"
      style={{ background: '#0A0910', borderTop: '3px solid rgba(255,255,255,0.08)' }}
    >
      {/* Header */}
      <div className="container mb-10">
        <div className="grid lg:grid-cols-2 gap-10 items-end">
          {/* Left */}
          <motion.div
            initial={{ opacity: 0, y: 28 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.6 }}
          >
            <div
              className="brutal-tag mb-5"
              style={{ borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.5)' }}
            >
              Real Books. Real Authors.
            </div>
            <h2
              className="font-display font-black leading-tight"
              style={{ fontSize: 'clamp(36px, 5vw, 60px)', color: '#fff' }}
            >
              What Will
              <br />
              <span style={{ color: '#FFE500' }}>You Create?</span>
            </h2>
          </motion.div>

          {/* Right */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.55, delay: 0.2 }}
          >
            <p className="text-base leading-relaxed mb-6" style={{ color: 'rgba(255,255,255,0.5)', maxWidth: '440px' }}>
              Prayer books, spiritual warfare, self-help, memoir, thriller, children&apos;s books. Whatever your genre, Booksane formats it beautifully and gets it ready to publish.
            </p>

            {/* Author quote */}
            <div
              className="p-5 relative"
              style={{
                background: 'rgba(255,255,255,0.04)',
                border: '2px solid rgba(255,255,255,0.12)',
                boxShadow: '4px 4px 0px rgba(255,255,255,0.06)',
              }}
            >
              {/* Giant quote mark */}
              <div
                className="absolute -top-4 left-4 font-display font-black leading-none select-none"
                style={{ fontSize: '56px', color: '#FFE500', opacity: 0.7, lineHeight: 1 }}
              >
                &ldquo;
              </div>
              <p
                className="text-sm font-semibold leading-relaxed pt-4"
                style={{ color: 'rgba(255,255,255,0.75)', fontStyle: 'italic' }}
              >
                Booksane turned my manuscript into a KDP-ready book in under a minute.
                It looked exactly like a professionally typeset book.
              </p>
              <div className="mt-3 flex items-center gap-3">
                <div
                  className="w-8 h-8 flex items-center justify-center text-xs font-black flex-shrink-0"
                  style={{ background: '#FFE500', border: '2px solid #000', color: '#000' }}
                >
                  CA
                </div>
                <div>
                  <div className="text-xs font-black text-white">Collins Asein</div>
                  <div
                    className="text-xs font-semibold uppercase tracking-wider"
                    style={{ color: 'rgba(255,255,255,0.35)', fontSize: '9px', letterSpacing: '0.12em' }}
                  >
                    Author of 10+ Published Books
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </div>

      {/* Auto-scrolling book strip */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={inView ? { opacity: 1 } : {}}
        transition={{ duration: 0.6, delay: 0.35 }}
        className="relative"
      >
        {/* Left/right fade masks */}
        <div
          className="absolute left-0 top-0 bottom-0 z-10 pointer-events-none"
          style={{ width: '80px', background: 'linear-gradient(to right, #0A0910 0%, transparent 100%)' }}
        />
        <div
          className="absolute right-0 top-0 bottom-0 z-10 pointer-events-none"
          style={{ width: '80px', background: 'linear-gradient(to left, #0A0910 0%, transparent 100%)' }}
        />

        {/* Scrolling track */}
        <div className="overflow-hidden">
          <div
            ref={trackRef}
            className="book-scroll-track flex gap-5 pb-4"
            style={{ width: 'max-content' }}
          >
            {LOOP_BOOKS.map((book, i) => (
              <BookCard key={`${book.asin}-${i}`} book={book} index={i} />
            ))}
          </div>
        </div>
      </motion.div>

      {/* Bottom strip */}
      <div
        className="mt-12 py-5 flex items-center justify-center gap-8 flex-wrap"
        style={{ borderTop: '2px solid rgba(255,255,255,0.07)' }}
      >
        {[
          { num: '10', label: 'Published Books', color: '#FFE500' },
          { num: 'Amazon', label: 'KDP Ready', color: '#00FF7F' },
          { num: '< 1min', label: 'Format Time', color: '#FF2D78' },
        ].map(stat => (
          <div key={stat.label} className="flex items-center gap-3">
            <span
              className="font-black font-display"
              style={{ fontSize: '22px', color: stat.color }}
            >
              {stat.num}
            </span>
            <span
              className="text-xs font-bold uppercase tracking-widest"
              style={{ color: 'rgba(255,255,255,0.35)', letterSpacing: '0.12em' }}
            >
              {stat.label}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}
