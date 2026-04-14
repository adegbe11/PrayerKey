'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'

const BRAND = '#FF5A5F'

function Nav() {
  return (
    <nav style={{
      position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100,
      padding: '0 5vw', height: 68,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      background: 'rgba(250,250,250,0.92)',
      backdropFilter: 'blur(16px)',
      borderBottom: '1px solid rgba(0,0,0,0.07)',
    }}>
      <a href="/landing" style={{ display: 'flex', alignItems: 'center', gap: 9, textDecoration: 'none' }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 32, height: 32, borderRadius: '50%' }} />
        <span style={{ fontSize: 18, fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A' }}>nomapal</span>
      </a>
      <a href="/landing" style={{
        padding: '10px 22px', background: BRAND, color: '#fff',
        borderRadius: 9999, fontSize: 14, fontWeight: 700,
        textDecoration: 'none',
      }}>Back to home</a>
    </nav>
  )
}

const FAQS = [
  {
    category: 'Getting started',
    questions: [
      {
        q: 'What is Nomapal?',
        a: 'Nomapal is a travel companion app. It helps you find real people who are heading to the same place at the same time as you. You create a profile, add your upcoming trips, and Nomapal shows you other travelers going your way. You can match, chat, and start planning together.',
      },
      {
        q: 'Is Nomapal a dating app?',
        a: 'No. Nomapal is built for travel, not dating. You can find travel friends, trip partners, or even a group to join. The focus is entirely on where you are going, not who you want to date.',
      },
      {
        q: 'How do I sign up?',
        a: 'Download the app from Google Play or the App Store, create your profile, and add at least one upcoming trip. Once your profile is set up, Nomapal starts showing you travelers heading to the same destinations.',
      },
      {
        q: 'Is Nomapal free to use?',
        a: 'You can sign up and browse for free. Some features like unlimited matches and advanced filters are part of a paid plan. You can always start free to see how it works.',
      },
    ],
  },
  {
    category: 'Matching and connecting',
    questions: [
      {
        q: 'How does the matching work?',
        a: 'When you add a destination and travel dates to your profile, Nomapal looks for other users going to the same place at the same time. You can then swipe through those matches and connect with anyone you want to travel with.',
      },
      {
        q: 'Can I find a group to join, or only one person?',
        a: 'Both. You can match with one person or connect with multiple travelers heading to the same destination. Some users form small groups and plan trips together.',
      },
      {
        q: 'What happens after I match with someone?',
        a: 'Once you both match, a chat opens up. From there you can talk, plan, share ideas, and decide how much of the trip you want to do together. There is no pressure to do everything as a pair.',
      },
      {
        q: 'Can I use Nomapal for trips that are still months away?',
        a: 'Yes. You can add trips as far in advance as you like. Many people use Nomapal to find companions months before they leave so they have plenty of time to get to know each other first.',
      },
    ],
  },
  {
    category: 'Safety and trust',
    questions: [
      {
        q: 'How does Nomapal verify users?',
        a: 'Every profile goes through a verification process when you sign up. Verified users get a badge on their profile. We also have a rating system so you can see how past travel companions rated each user after their trips.',
      },
      {
        q: 'What should I do if someone makes me feel uncomfortable?',
        a: 'You can block or report any user directly from their profile. Our team reviews every report. You can also stop chatting with anyone at any time with no explanation needed.',
      },
      {
        q: 'Is my personal information safe?',
        a: 'Yes. We do not share your personal data with third parties. You control what is visible on your public profile. Check our Privacy Policy for full details on how we handle your information.',
      },
    ],
  },
  {
    category: 'Account and billing',
    questions: [
      {
        q: 'How do I cancel my subscription?',
        a: 'You can cancel your subscription at any time through the app settings on your phone. On iOS, go to your Apple account subscriptions. On Android, go to Google Play subscriptions. Your access continues until the end of the billing period.',
      },
      {
        q: 'Can I delete my account?',
        a: 'Yes. Go to Settings in the app and select Delete Account. This permanently removes your profile and all your data from Nomapal. If you need help, email us at hello@nomapal.com.',
      },
      {
        q: 'I have a question that is not answered here. How do I get help?',
        a: 'Send us an email at hello@nomapal.com and we will get back to you within one business day. We read every message.',
      },
    ],
  },
]

function FAQItem({ q, a }: { q: string; a: string }) {
  const [open, setOpen] = useState(false)
  return (
    <div
      style={{
        borderBottom: '1px solid rgba(0,0,0,0.08)',
        cursor: 'pointer',
      }}
      onClick={() => setOpen(o => !o)}
    >
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '22px 0', gap: 16,
      }}>
        <span style={{ fontSize: 17, fontWeight: 600, color: '#0A0A0A', lineHeight: 1.4 }}>{q}</span>
        <span style={{
          fontSize: 20, color: BRAND, flexShrink: 0,
          transform: open ? 'rotate(45deg)' : 'none',
          transition: 'transform 200ms ease',
          display: 'inline-block',
        }}>+</span>
      </div>
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
            style={{ overflow: 'hidden' }}
          >
            <p style={{ fontSize: 16, lineHeight: 1.75, color: '#555', paddingBottom: 22 }}>{a}</p>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

export default function FAQPage() {
  return (
    <div style={{ background: '#FAFAFA', fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif", minHeight: '100vh' }}>
      <Nav />

      {/* Hero */}
      <section style={{ padding: '120px 5vw 64px', textAlign: 'center', maxWidth: 700, margin: '0 auto' }}>
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 8,
            padding: '7px 18px', borderRadius: 9999,
            background: `${BRAND}12`, border: `1px solid ${BRAND}30`,
            marginBottom: 28,
          }}>
            <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>HELP CENTER</span>
          </div>
          <h1 style={{ fontSize: 'clamp(36px, 6vw, 64px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A', lineHeight: 1.1, marginBottom: 20 }}>
            Frequently asked questions
          </h1>
          <p style={{ fontSize: 18, lineHeight: 1.7, color: '#666' }}>
            Everything you need to know about Nomapal. Can not find your answer?{' '}
            <a href="mailto:hello@nomapal.com" style={{ color: BRAND, textDecoration: 'none', fontWeight: 600 }}>Email us</a>.
          </p>
        </motion.div>
      </section>

      {/* FAQ sections */}
      <section style={{ padding: '0 5vw 96px', maxWidth: 800, margin: '0 auto' }}>
        {FAQS.map((section, si) => (
          <motion.div
            key={si}
            initial={{ opacity: 0, y: 32 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.55, delay: si * 0.05 }}
            style={{ marginBottom: 56 }}
          >
            <h2 style={{
              fontSize: 22, fontWeight: 800, color: '#0A0A0A',
              letterSpacing: '-0.02em', marginBottom: 8,
              paddingBottom: 16, borderBottom: `2px solid ${BRAND}`,
              display: 'inline-block',
            }}>
              {section.category}
            </h2>
            <div style={{ marginTop: 8 }}>
              {section.questions.map((item, qi) => (
                <FAQItem key={qi} q={item.q} a={item.a} />
              ))}
            </div>
          </motion.div>
        ))}

        {/* Still need help */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          style={{
            background: '#fff', borderRadius: 24, padding: '48px 40px',
            border: '1px solid rgba(0,0,0,0.07)',
            boxShadow: '0 4px 32px rgba(0,0,0,0.05)',
            textAlign: 'center',
          }}
        >
          <div style={{ fontSize: 40, marginBottom: 16 }}>✉️</div>
          <h3 style={{ fontSize: 24, fontWeight: 800, color: '#0A0A0A', marginBottom: 12 }}>Still need help?</h3>
          <p style={{ fontSize: 16, color: '#555', lineHeight: 1.7, marginBottom: 28 }}>
            Our team reads every message and replies within one business day.
          </p>
          <a href="mailto:hello@nomapal.com" style={{
            display: 'inline-block', padding: '14px 36px',
            background: BRAND, color: '#fff',
            borderRadius: 9999, fontSize: 15, fontWeight: 700,
            textDecoration: 'none',
          }}>
            Email hello@nomapal.com
          </a>
        </motion.div>
      </section>

      {/* Footer */}
      <footer style={{ background: '#0A0A0A', padding: '32px 5vw', textAlign: 'center' }}>
        <p style={{ fontSize: 13, color: '#555' }}>
          © {new Date().getFullYear()} Nomapal. All rights reserved.
          {' · '}
          <a href="/landing/about" style={{ color: '#555', textDecoration: 'none' }}>About</a>
          {' · '}
          <a href="/landing/privacy" style={{ color: '#555', textDecoration: 'none' }}>Privacy</a>
          {' · '}
          <a href="mailto:hello@nomapal.com" style={{ color: '#555', textDecoration: 'none' }}>Contact</a>
        </p>
      </footer>
    </div>
  )
}
