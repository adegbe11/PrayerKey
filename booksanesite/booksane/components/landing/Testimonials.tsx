'use client';

import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';

const testimonials = [
  {
    quote: "My prayer book manuscript sat untouched for four months because I could not face the formatting. Booksane had it ready in under two minutes. I uploaded to KDP the same day.",
    author: 'Rolake Bello',
    role: 'Prayer book author, Lagos',
    initials: 'RB',
    avatarColor: '#FF2D78',
    cardColor: '#fff',
    accentColor: '#FF2D78',
  },
  {
    quote: "The KDP export went straight through. No errors, no rejections. First time in three book releases that actually happened without back and forth.",
    author: 'Bart Muwanguzi',
    role: 'Memoir writer, Kampala',
    initials: 'BM',
    avatarColor: '#000',
    cardColor: '#FFE500',
    accentColor: '#000',
  },
  {
    quote: "Booksane reads your chapters, fixes the spacing and lays everything out like a real published book. I had no idea something this good was completely free.",
    author: 'Chinwe Eze',
    role: 'Self-help author, Enugu',
    initials: 'CE',
    avatarColor: '#00A854',
    cardColor: '#fff',
    accentColor: '#00A854',
  },
  {
    quote: "I was paying for formatting software that still required me to do most of the work. Booksane does everything. I switched and I am not going back.",
    author: 'Gifty Asante',
    role: 'Romance novelist, Accra',
    initials: 'GA',
    avatarColor: '#7C3AED',
    cardColor: '#fff',
    accentColor: '#7C3AED',
  },
  {
    quote: "Ten templates and I could flip between them instantly while seeing my actual words on the page. No other tool does this. Not even the expensive ones.",
    author: 'Obinna Nwosu',
    role: 'Business author, Abuja',
    initials: 'ON',
    avatarColor: '#000',
    cardColor: '#FFE500',
    accentColor: '#000',
  },
  {
    quote: "Took my 80,000 word manuscript and formatted it in seconds. Every chapter heading came out clean. Downloaded the PDF and sent it straight to the printer.",
    author: 'Lola Adjei',
    role: 'Spiritual author, Kumasi',
    initials: 'LA',
    avatarColor: '#00C8FF',
    cardColor: '#fff',
    accentColor: '#00C8FF',
  },
];

export default function Testimonials() {
  const { ref, inView } = useInView({ threshold: 0.1, triggerOnce: true });

  return (
    <section ref={ref} className="section" style={{ background: '#0A0910' }}>
      <div className="container">
        <motion.div
          initial={{ opacity: 0, y: 24 }} animate={inView ? { opacity: 1, y: 0 } : {}} transition={{ duration: 0.6 }}
          className="mb-10"
        >
          <div className="brutal-tag mb-5" style={{ borderColor: 'rgba(255,255,255,0.3)', color: 'rgba(255,255,255,0.5)' }}>
            What authors say
          </div>
          <h2 className="font-display font-black leading-tight" style={{ fontSize: 'clamp(36px, 5vw, 60px)', color: '#fff' }}>
            Real authors.
            <br />
            <span style={{ color: '#FFE500' }}>Real books.</span>
          </h2>
        </motion.div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-5">
          {testimonials.map((t, i) => (
            <motion.div
              key={t.author}
              initial={{ opacity: 0, y: 32 }} animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.5, delay: i * 0.09 + 0.2 }}
              className="brutal-card-light flex flex-col gap-4 p-6"
              style={{ background: t.cardColor }}
            >
              {/* Giant quote mark */}
              <div className="font-black leading-none" style={{ fontSize: '64px', color: t.accentColor, opacity: 0.2, lineHeight: 0.8 }}>
                &ldquo;
              </div>

              <p className="text-sm font-semibold leading-relaxed -mt-2" style={{ color: t.cardColor === '#FFE500' ? '#000' : '#1a1a2e' }}>
                {t.quote}
              </p>

              <div className="flex items-center gap-3 mt-auto pt-4" style={{ borderTop: `2px solid ${t.accentColor}25` }}>
                <div className="w-9 h-9 flex items-center justify-center text-xs font-black text-white"
                  style={{ background: t.avatarColor, border: '2px solid #000', boxShadow: '2px 2px 0px #000', flexShrink: 0, color: t.avatarColor === '#FFE500' || t.avatarColor === '#00A854' || t.avatarColor === '#00C8FF' ? '#000' : '#fff' }}>
                  {t.initials}
                </div>
                <div>
                  <div className="text-sm font-black" style={{ color: t.cardColor === '#FFE500' ? '#000' : '#1a1a2e' }}>{t.author}</div>
                  <div className="text-xs font-medium" style={{ color: t.cardColor === '#FFE500' ? 'rgba(0,0,0,0.5)' : 'rgba(26,26,46,0.5)' }}>{t.role}</div>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
