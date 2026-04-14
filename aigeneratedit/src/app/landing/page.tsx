'use client'

import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'

// ─── Brand ─────────────────────────────────────────────────────────────────
const BRAND = '#FF5A5F'
const SPECTRUM = ['#FF5A5F','#FF8800','#FFDD00','#44CC44','#0088FF','#3344CC','#9933CC']

// ─── Orbit data ─────────────────────────────────────────────────────────────
const ORBIT_OUTER = [
  { photo: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&q=80', ci: 0 },
  { photo: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=300&q=80', ci: 2 },
  { photo: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80', ci: 4 },
  { photo: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80', ci: 1 },
  { photo: 'https://images.unsplash.com/photo-1552058544-f2b08422138a?w=300&q=80', ci: 5 },
  { photo: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300&q=80', ci: 3 },
  { photo: 'https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=300&q=80', ci: 6 },
  { photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80', ci: 2 },
]
const ORBIT_INNER = [
  { photo: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=300&q=80', ci: 3 },
  { photo: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=300&q=80', ci: 0 },
  { photo: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300&q=80', ci: 5 },
  { photo: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&q=80', ci: 1 },
  { photo: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&q=80', ci: 4 },
]

// ─── Orbit ring component ───────────────────────────────────────────────────
function OrbitRing({
  photos, radius, size, duration, clockwise, borderWidth = 3,
}: {
  photos: typeof ORBIT_OUTER
  radius: number
  size: number
  duration: number
  clockwise: boolean
  borderWidth?: number
}) {
  return (
    <motion.div
      style={{
        position: 'absolute',
        width: radius * 2,
        height: radius * 2,
        borderRadius: '50%',
        border: '1.5px solid rgba(0,0,0,0.08)',
      }}
      animate={{ rotate: clockwise ? 360 : -360 }}
      transition={{ duration, repeat: Infinity, ease: 'linear' }}
    >
      {photos.map((p, i) => {
        const angleDeg = (360 / photos.length) * i - 90
        const angleRad = (angleDeg * Math.PI) / 180
        const x = radius + radius * Math.cos(angleRad) - size / 2
        const y = radius + radius * Math.sin(angleRad) - size / 2
        return (
          <motion.div
            key={i}
            style={{
              position: 'absolute', left: x, top: y,
              width: size, height: size, borderRadius: '50%',
              overflow: 'hidden',
              border: `${borderWidth}px solid ${SPECTRUM[p.ci]}`,
              boxShadow: `0 6px 24px ${SPECTRUM[p.ci]}55`,
            }}
            animate={{ rotate: clockwise ? -360 : 360 }}
            transition={{ duration, repeat: Infinity, ease: 'linear' }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={p.photo} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} draggable={false} loading="lazy" decoding="async" width="50" height="50" />
          </motion.div>
        )
      })}
    </motion.div>
  )
}

// ─── Hero orbit "O" ─────────────────────────────────────────────────────────
function HeroOrbitO({ size }: { size: number }) {
  const outerRadius = size * 0.44
  const innerRadius = size * 0.26
  return (
    <div style={{
      position: 'relative',
      width: size, height: size,
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      flexShrink: 0,
    }}>
      <OrbitRing photos={ORBIT_OUTER} radius={outerRadius} size={size * 0.18} duration={26} clockwise borderWidth={2.5} />
      <OrbitRing photos={ORBIT_INNER} radius={innerRadius} size={size * 0.155} duration={18} clockwise={false} borderWidth={2} />
      {/* Center app icon */}
      <div style={{
        position: 'absolute', width: size * 0.21, height: size * 0.21,
        borderRadius: '50%', overflow: 'hidden',
        border: '3px solid #fff', boxShadow: '0 4px 20px rgba(0,0,0,0.18)',
        zIndex: 2,
      }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: '100%', height: '100%', objectFit: 'cover' }} decoding="async" fetchPriority="high" width="192" height="192" />
      </div>
    </div>
  )
}

// ─── Nav ────────────────────────────────────────────────────────────────────
function Nav() {
  return (
    <nav style={{
      position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100,
      padding: '0 5vw',
      height: 48,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      background: 'linear-gradient(90deg, #FFF7ED 0%, #FFFBF5 50%, #FFF7ED 100%)',
      borderBottom: '2.5px solid #F5C29A',
      boxShadow: '0 2px 12px rgba(230,130,60,0.10)',
    }}>
      {/* Logo */}
      <a href="/landing" style={{ display: 'flex', alignItems: 'center', gap: 8, textDecoration: 'none' }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 28, height: 28, borderRadius: '50%' }} decoding="async" fetchPriority="high" width="28" height="28" />
        <span style={{ fontSize: 17, fontWeight: 900, letterSpacing: '-0.04em', color: '#1A0A00' }}>nomapal</span>
      </a>

      {/* Nav links */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
        {[
          { label: 'Features', href: '#features', cls: 'nav-link' },
          { label: 'How it works', href: '#how-it-works', cls: 'nav-link' },
          { label: 'Reviews', href: '#reviews', cls: 'nav-link' },
          { label: 'About', href: '/landing/about', cls: 'nav-link nav-link-wide' },
          { label: 'FAQ', href: '/landing/faq', cls: 'nav-link nav-link-wide' },
        ].map(link => (
          <a key={link.label} href={link.href}
            style={{ fontSize: 13, fontWeight: 800, color: '#5C2E00', textDecoration: 'none', display: 'none', whiteSpace: 'nowrap', letterSpacing: '-0.01em' }}
            className={link.cls}
          >{link.label}</a>
        ))}
        <a
          href="#download"
          style={{
            padding: '7px 16px',
            background: 'linear-gradient(135deg, #FF5A5F 0%, #FF8C00 100%)',
            color: '#fff',
            borderRadius: 9999, fontSize: 13, fontWeight: 900,
            textDecoration: 'none', whiteSpace: 'nowrap',
            letterSpacing: '-0.01em',
            boxShadow: '0 3px 12px rgba(255,90,95,0.35), inset 0 1px 0 rgba(255,255,255,0.25)',
            border: '1.5px solid rgba(255,255,255,0.18)',
          }}
        >
          Download App
        </a>
      </div>
    </nav>
  )
}

// ─── Features ───────────────────────────────────────────────────────────────
const FEATURES = [
  {
    emoji: '✈️',
    title: 'Match on destinations, not just faces',
    body: 'Tell us where you are going. Nomapal finds people heading to the same country, city, or region at the same time. Stop swiping through strangers. Start meeting people who are already going your way.',
    color: '#FF5A5F',
    mockup: [
      'https://images.unsplash.com/photo-1552058544-f2b08422138a?w=400&q=80',
      'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=400&q=80',
    ],
    tag: 'Destination Matching',
  },
  {
    emoji: '🤝',
    title: 'Four ways to connect with other travelers',
    body: 'Skip someone, match with a traveler going your exact route, show that you are open to meeting anyone, or send an invite for a trip you have always wanted to take. Four simple choices and no confusion about what you want.',
    color: '#0088FF',
    mockup: [
      'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80',
      'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80',
    ],
    tag: 'How You Connect',
  },
  {
    emoji: '🛡️',
    title: 'Travel with people you can trust',
    body: 'Every Nomapal profile shows travel ratings and trip history. You will know who you are meeting before you book a single flight. Safety comes first and adventure comes right after.',
    color: '#44CC44',
    mockup: [
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80',
      'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80',
    ],
    tag: 'Verified Profiles',
  },
]

function FeatureCard({ feat, index }: { feat: typeof FEATURES[0]; index: number }) {
  const even = index % 2 === 0
  return (
    <motion.div
      initial={{ opacity: 0, y: 48 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-80px' }}
      transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: 48,
        alignItems: 'center',
        padding: '64px 0',
        borderBottom: '1px solid rgba(0,0,0,0.06)',
      }}
    >
      {/* Text side */}
      <div style={{ order: even ? 0 : 1 }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 8,
          padding: '6px 14px', borderRadius: 9999,
          background: `${feat.color}14`,
          marginBottom: 20,
        }}>
          <span>{feat.emoji}</span>
          <span style={{ fontSize: 13, fontWeight: 700, color: feat.color, letterSpacing: '0.02em' }}>{feat.tag}</span>
        </div>
        <h3 style={{ fontSize: 'clamp(26px, 3.5vw, 40px)', fontWeight: 900, letterSpacing: '-0.03em', color: '#0A0A0A', lineHeight: 1.15, marginBottom: 18 }}>
          {feat.title}
        </h3>
        <p style={{ fontSize: 17, lineHeight: 1.7, color: '#555', fontWeight: 400, maxWidth: 440 }}>
          {feat.body}
        </p>
      </div>

      {/* Visual side — two overlapping phone mockup photos */}
      <div style={{ position: 'relative', height: 'clamp(260px, 35vw, 340px)', order: even ? 1 : 0 }}>
        <div style={{
          position: 'absolute', right: even ? 'auto' : 0, left: even ? 0 : 'auto',
          top: 24, width: 200, height: 300, borderRadius: 24,
          overflow: 'hidden',
          border: '1px solid rgba(0,0,0,0.08)',
          boxShadow: '0 24px 64px rgba(0,0,0,0.12)',
          transform: `rotate(${even ? -3 : 3}deg)`,
        }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={feat.mockup[0]} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" decoding="async" />
          <div style={{ position: 'absolute', inset: 0, background: `linear-gradient(to top, ${feat.color}22, transparent 60%)` }} />
        </div>
        <div style={{
          position: 'absolute', right: even ? 0 : 'auto', left: even ? 'auto' : 0,
          top: 0, width: 180, height: 270, borderRadius: 24,
          overflow: 'hidden',
          border: '1px solid rgba(0,0,0,0.06)',
          boxShadow: '0 16px 40px rgba(0,0,0,0.1)',
          transform: `rotate(${even ? 4 : -4}deg)`,
        }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={feat.mockup[1]} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" decoding="async" />
        </div>
        {/* Floating badge */}
        <div style={{
          position: 'absolute', bottom: 8, left: '50%', transform: 'translateX(-50%)',
          background: '#fff', borderRadius: 16,
          padding: '10px 18px',
          boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
          display: 'flex', alignItems: 'center', gap: 10,
          whiteSpace: 'nowrap',
        }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: feat.color, flexShrink: 0, boxShadow: `0 0 8px ${feat.color}` }} />
          <span style={{ fontSize: 13, fontWeight: 700, color: '#0A0A0A' }}>{feat.tag} active</span>
        </div>
      </div>
    </motion.div>
  )
}

// ─── Steps ──────────────────────────────────────────────────────────────────
const STEPS = [
  { n: '01', title: 'Build your traveler profile', body: 'Add your travel style, upcoming destinations, and trip dates. The whole thing takes under 3 minutes.', color: '#FF5A5F' },
  { n: '02', title: 'Match with travelers going your way', body: 'Nomapal shows you real people heading to the same place at the same time. Swipe, match, start chatting.', color: '#0088FF' },
  { n: '03', title: 'Travel together', body: 'Plan the trip, share your itinerary, and go. From a quick airport meetup to a whole month traveling together.', color: '#44CC44' },
]

// ─── Testimonials ───────────────────────────────────────────────────────────
const TESTIMONIALS = [
  {
    quote: "Met my travel partner for a 3-week Southeast Asia trip through Nomapal. We've now done 7 countries together. Absolute game-changer.",
    name: 'Amara O.',
    detail: 'Lagos → Bangkok → Bali',
    photo: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80',
    rating: 5,
  },
  {
    quote: "I was terrified to travel solo. Nomapal helped me find three other people doing the same Portugal trip. We split costs and made memories.",
    name: 'Lucas M.',
    detail: 'São Paulo → Lisbon',
    photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80',
    rating: 5,
  },
  {
    quote: "The destination matching is scary accurate. Matched with someone flying into the same airport, same weekend. We shared a taxi and a week-long adventure.",
    name: 'Yuki T.',
    detail: 'Tokyo → Mexico City',
    photo: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&q=80',
    rating: 5,
  },
]

// ─── Download buttons (official store badges) ────────────────────────────────
function DownloadButtons({ centered = false, light = false }: { centered?: boolean; light?: boolean }) {
  const bg = light ? '#fff' : '#0A0A0A'
  const fg = light ? '#0A0A0A' : '#fff'
  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, justifyContent: centered ? 'center' : 'flex-start' }}>
      {/* Google Play */}
      <a href="#download" style={{
        display: 'flex', alignItems: 'center', gap: 11,
        padding: '11px 20px',
        background: bg, color: fg,
        borderRadius: 12, textDecoration: 'none',
        border: light ? '1.5px solid rgba(0,0,0,0.12)' : 'none',
        boxShadow: light ? 'none' : '0 4px 20px rgba(0,0,0,0.22)',
        transition: 'opacity 180ms',
      }}>
        {/* Official Google Play icon */}
        <svg width="24" height="24" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
          <path d="M48 432c0 17.7 19.4 28 34.4 18.8L456 256 82.4 61.2C67.4 52 48 62.3 48 80v352z" fill="#4CAF50"/>
          <path d="M48 256V80L261 256 48 432V256z" fill="#81C784"/>
          <path d="M48 80l213 176L456 256 82.4 61.2C67.4 52 48 62.3 48 80z" fill="#EA4335"/>
          <path d="M48 432l213-176 195 80-173.6 113.8C67.4 460 48 449.7 48 432z" fill="#FBBC05"/>
          <path d="M261 256L456 256 261 80v176z" fill="#4285F4"/>
        </svg>
        <div>
          <div style={{ fontSize: 9.5, opacity: 0.65, letterSpacing: '0.05em', textTransform: 'uppercase' }}>Get it on</div>
          <div style={{ fontSize: 16, fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.2 }}>Google Play</div>
        </div>
      </a>
      {/* App Store */}
      <a href="#download" style={{
        display: 'flex', alignItems: 'center', gap: 11,
        padding: '11px 20px',
        background: bg, color: fg,
        borderRadius: 12, textDecoration: 'none',
        border: light ? '1.5px solid rgba(0,0,0,0.12)' : 'none',
        boxShadow: light ? 'none' : '0 4px 20px rgba(0,0,0,0.22)',
      }}>
        {/* Official Apple logo */}
        <svg width="22" height="26" viewBox="0 0 814 1000" fill={fg} xmlns="http://www.w3.org/2000/svg">
          <path d="M788.1 340.9c-5.8 4.5-108.2 62.2-108.2 190.5 0 148.4 130.3 200.9 134.2 202.2-.6 3.2-20.7 71.9-68.7 141.9-42.8 61.6-87.5 123.1-155.5 123.1s-85.5-39.5-164-39.5c-76 0-103.7 40.8-165.9 40.8s-105.3-57.8-155.5-127.4C46 790.7 0 663 0 541.8c0-207.5 135.4-317.3 269-317.3 71 0 130.5 43.7 175.1 43.7 42.8 0 109.8-46.3 188.5-46.3 31.4 0 108.2 2.6 168.3 80.1zm-234-181.5c31.1-36.9 53.1-88.1 53.1-139.3 0-7.1-.6-14.3-1.9-20.1-50.6 1.9-110.8 33.7-147.1 75.8-28.5 32.4-55.1 83.6-55.1 135.5 0 7.8 1.3 15.6 1.9 18.1 3.2.6 8.4 1.3 13.6 1.3 45.4 0 102.5-30.4 135.5-71.3z"/>
        </svg>
        <div>
          <div style={{ fontSize: 9.5, opacity: 0.65, letterSpacing: '0.05em', textTransform: 'uppercase' }}>Download on the</div>
          <div style={{ fontSize: 16, fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.2 }}>App Store</div>
        </div>
      </a>
    </div>
  )
}

// ─── Traveler photo cards (fan stack) ────────────────────────────────────────
const TRAVELER_CARDS = [
  { src: 'https://images.unsplash.com/photo-1539635278303-d4002c07eae3?w=600&q=80', rot: -6,  z: 1 },
  { src: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&q=80', rot: -3,  z: 2 },
  { src: 'https://images.unsplash.com/photo-1530789253388-582c481c54b0?w=600&q=80', rot:  0,  z: 3 },
  { src: 'https://images.unsplash.com/photo-1502301103665-0b95cc738daf?w=600&q=80', rot:  3,  z: 4 },
  { src: 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=600&q=80', rot:  6,  z: 5 },
]

function TravelerCardStack() {
  return (
    <div style={{ position: 'relative', width: 'min(300px, calc(100vw - 80px))', aspectRatio: '3/4', margin: '0 auto' }}>
      {TRAVELER_CARDS.map((card, i) => (
        <motion.div
          key={i}
          initial={{ opacity: 0, scale: 0.85, rotate: card.rot }}
          whileInView={{ opacity: 1, scale: 1, rotate: card.rot }}
          viewport={{ once: true }}
          transition={{ duration: 0.55, delay: i * 0.08, ease: [0.16, 1, 0.3, 1] }}
          whileHover={{ scale: 1.04, rotate: 0, zIndex: 20, transition: { duration: 0.25 } }}
          style={{
            position: 'absolute',
            top: 0, left: 0,
            width: '100%', height: '100%',
            borderRadius: 20,
            overflow: 'hidden',
            boxShadow: '0 12px 40px rgba(0,0,0,0.18)',
            zIndex: card.z,
            cursor: 'pointer',
            border: '3px solid #fff',
            transformOrigin: 'bottom center',
          }}
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={card.src} alt="Nomapal traveler" style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" decoding="async" />
          {/* Nomapal sign overlay */}
          <div style={{
            position: 'absolute', bottom: 16, left: 16, right: 16,
            background: BRAND,
            borderRadius: 10, padding: '8px 14px',
            display: 'flex', alignItems: 'center', gap: 8,
          }}>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/icons/icon-192-round.png" alt="" style={{ width: 22, height: 22, borderRadius: '50%' }} loading="lazy" decoding="async" width="22" height="22" />
            <span style={{ color: '#fff', fontWeight: 800, fontSize: 14, letterSpacing: '-0.02em' }}>nomapal</span>
          </div>
        </motion.div>
      ))}
    </div>
  )
}

// ─── Community section (2-col) ───────────────────────────────────────────────
function CommunitySection() {
  return (
    <section className="community-section" style={{ background: '#fff', padding: '96px 5vw' }}>
      <div style={{
        maxWidth: 1100, margin: '0 auto',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: 'clamp(32px, 5vw, 64px)',
        alignItems: 'center',
      }}>
        {/* Left — text */}
        <motion.div
          initial={{ opacity: 0, x: -32 }}
          whileInView={{ opacity: 1, x: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
        >
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 8,
            padding: '6px 16px', borderRadius: 9999,
            background: `${BRAND}12`, marginBottom: 24,
          }}>
            <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>REAL NOMADS</span>
          </div>

          <h2 style={{
            fontSize: 'clamp(32px, 4.5vw, 52px)',
            fontWeight: 900, letterSpacing: '-0.04em',
            color: '#0A0A0A', lineHeight: 1.1, marginBottom: 20,
          }}>
            Connect with real<br />travelers in real life.
          </h2>

          <p style={{ fontSize: 17, lineHeight: 1.75, color: '#555', marginBottom: 16, maxWidth: 460 }}>
            Nomapal is built for people who actually go places. Every person you match with is a real traveler. They are verified, rated, and heading somewhere worth visiting.
          </p>
          <p style={{ fontSize: 17, lineHeight: 1.75, color: '#555', marginBottom: 36, maxWidth: 460 }}>
            Whether it is a weekend trip or six months on the road, find someone to travel with, split the costs, and come back with memories worth sharing.
          </p>

          <DownloadButtons />
        </motion.div>

        {/* Right — fanned photo cards */}
        <motion.div
          initial={{ opacity: 0, x: 32 }}
          whileInView={{ opacity: 1, x: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
          style={{ display: 'flex', justifyContent: 'center', padding: '40px 0', overflow: 'hidden' }}
        >
          <TravelerCardStack />
        </motion.div>
      </div>
    </section>
  )
}

// ─── Main page ───────────────────────────────────────────────────────────────
export default function LandingPage() {
  const [orbitSize, setOrbitSize] = useState(220)

  useEffect(() => {
    function calcSize() {
      const vw = window.innerWidth
      // O size = ~match the font's cap height: font is clamp(72,11vw,148)px
      const fontSize = Math.max(52, Math.min(vw * 0.13, 148))
      setOrbitSize(Math.round(fontSize * 1.05))
    }
    calcSize()
    window.addEventListener('resize', calcSize)
    return () => window.removeEventListener('resize', calcSize)
  }, [])

  return (
    <div style={{ background: '#FAFAFA', fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif", overflowX: 'hidden' }}>
      <Nav />

      {/* ── HERO ─────────────────────────────────────────────────────── */}
      <section className="hero-section" style={{
        minHeight: '100vh',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
        padding: '80px 5vw 60px',
        position: 'relative',
        textAlign: 'center',
        overflowX: 'hidden',
      }}>
        {/* Subtle gradient blob */}
        <div style={{
          position: 'absolute', top: 0, left: '50%', transform: 'translateX(-50%)',
          width: '80vw', height: '60vh',
          background: `radial-gradient(ellipse at 50% 30%, ${BRAND}18 0%, transparent 70%)`,
          pointerEvents: 'none',
        }} />

        {/* Eyebrow */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          style={{
            display: 'inline-flex', alignItems: 'center', gap: 8,
            padding: '7px 18px', borderRadius: 9999,
            background: `${BRAND}12`, border: `1px solid ${BRAND}30`,
            marginBottom: 28,
          }}
        >
          <span style={{ fontSize: 13 }}>✈️</span>
          <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>
            THE TRAVEL PARTNER APP
          </span>
        </motion.div>

        {/* ── NOMAPAL wordmark with orbit O ── */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.65, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
          style={{
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            lineHeight: 1,
            marginBottom: 8,
          }}
        >
          <span className="wordmark-text" style={{
            fontSize: `clamp(52px, 13vw, 148px)`,
            fontWeight: 900,
            letterSpacing: '-0.05em',
            color: '#0A0A0A',
          }}>N</span>

          <HeroOrbitO size={orbitSize} />

          <span className="wordmark-text" style={{
            fontSize: `clamp(52px, 13vw, 148px)`,
            fontWeight: 900,
            letterSpacing: '-0.05em',
            color: '#0A0A0A',
          }}>MAPAL</span>
        </motion.div>

        {/* Tagline */}
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.38, ease: [0.16, 1, 0.3, 1] }}
          style={{
            fontSize: 'clamp(18px, 2.5vw, 26px)',
            color: '#444',
            fontWeight: 400,
            lineHeight: 1.5,
            maxWidth: 520,
            marginBottom: 40,
          }}
        >
          Find someone to travel with.{' '}
          <span style={{ color: BRAND, fontWeight: 600 }}>Not just someone to date.</span>
        </motion.p>

        {/* Download buttons */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.52 }}
        >
          <DownloadButtons centered />
        </motion.div>

        {/* Social proof micro */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.72, duration: 0.5 }}
          style={{ display: 'flex', alignItems: 'center', gap: 16, marginTop: 32, flexWrap: 'wrap', justifyContent: 'center' }}
        >
          {/* Avatars */}
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {[
              'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=60&q=80',
              'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=60&q=80',
              'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=60&q=80',
              'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=60&q=80',
            ].map((src, i) => (
              <div key={i} style={{
                width: 32, height: 32, borderRadius: '50%', overflow: 'hidden',
                border: '2px solid #FAFAFA',
                marginLeft: i === 0 ? 0 : -10,
              }}>
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={src} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" decoding="async" width="32" height="32" />
              </div>
            ))}
          </div>
          <span style={{ fontSize: 14, color: '#666', fontWeight: 500 }}>
            <strong style={{ color: '#0A0A0A' }}>12,400+</strong> nomads already matched
          </span>
          <div style={{ display: 'flex', gap: 2 }}>
            {[1,2,3,4,5].map(s => <span key={s} style={{ color: '#FFDD00', fontSize: 14 }}>★</span>)}
          </div>
          <span style={{ fontSize: 14, color: '#666' }}>4.9 rating</span>
        </motion.div>

        {/* Scroll cue */}
        <motion.div
          animate={{ y: [0, 8, 0] }}
          transition={{ repeat: Infinity, duration: 2, ease: 'easeInOut' }}
          style={{ position: 'absolute', bottom: 28, left: '50%', transform: 'translateX(-50%)', color: '#bbb', fontSize: 20 }}
        >
          ↓
        </motion.div>
      </section>

      {/* ── SOCIAL PROOF BAR ──────────────────────────────────────────── */}
      <section className="stats-bar" style={{
        background: '#0A0A0A', padding: '28px 5vw',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexWrap: 'wrap', gap: '24px 48px',
      }}>
        {[
          { label: 'Nomads matched', value: '12,400+' },
          { label: 'Countries covered', value: '94' },
          { label: 'App Store rating', value: '4.9 ★' },
          { label: 'Trips planned', value: '3,200+' },
        ].map(stat => (
          <div key={stat.label} style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 'clamp(22px, 3vw, 32px)', fontWeight: 900, color: '#fff', letterSpacing: '-0.03em' }}>{stat.value}</div>
            <div style={{ fontSize: 13, color: '#666', fontWeight: 500, marginTop: 2 }}>{stat.label}</div>
          </div>
        ))}
      </section>

      {/* ── COMMUNITY 2-COL ──────────────────────────────────────────── */}
      <CommunitySection />

      {/* ── FEATURES ──────────────────────────────────────────────────── */}
      <section id="features" className="features-section" style={{ maxWidth: 1100, margin: '0 auto', padding: '80px 5vw' }}>
        <motion.div
          initial={{ opacity: 0, y: 32 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.55 }}
          style={{ textAlign: 'center', marginBottom: 8 }}
        >
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 8,
            padding: '6px 16px', borderRadius: 9999,
            background: `${BRAND}12`, marginBottom: 20,
          }}>
            <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>WHAT MAKES NOMAPAL DIFFERENT</span>
          </div>
          <h2 style={{ fontSize: 'clamp(32px, 5vw, 56px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A', lineHeight: 1.1 }}>
            Built for the way<br />real travelers think
          </h2>
        </motion.div>

        {FEATURES.map((feat, i) => (
          <FeatureCard key={i} feat={feat} index={i} />
        ))}
      </section>

      {/* ── HOW IT WORKS ─────────────────────────────────────────────── */}
      <section id="how-it-works" className="howitworks-section" style={{ background: '#0A0A0A', padding: '96px 5vw' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <motion.div
            initial={{ opacity: 0, y: 32 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.55 }}
            style={{ textAlign: 'center', marginBottom: 64 }}
          >
            <div style={{
              display: 'inline-flex', alignItems: 'center', gap: 8,
              padding: '6px 16px', borderRadius: 9999,
              background: `${BRAND}22`, marginBottom: 20,
            }}>
              <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>HOW IT WORKS</span>
            </div>
            <h2 style={{ fontSize: 'clamp(32px, 5vw, 56px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#fff', lineHeight: 1.1 }}>
              From profile to<br />plane ticket in minutes
            </h2>
          </motion.div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 32 }}>
            {STEPS.map((step, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 40 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-60px' }}
                transition={{ duration: 0.55, delay: i * 0.1 }}
                style={{
                  padding: '40px 32px',
                  background: 'rgba(255,255,255,0.04)',
                  borderRadius: 24,
                  border: '1px solid rgba(255,255,255,0.08)',
                }}
              >
                <div style={{
                  fontSize: 48, fontWeight: 900, letterSpacing: '-0.04em',
                  color: step.color, marginBottom: 20, lineHeight: 1,
                }}>{step.n}</div>
                <h3 style={{ fontSize: 22, fontWeight: 800, color: '#fff', marginBottom: 12, letterSpacing: '-0.02em', lineHeight: 1.25 }}>
                  {step.title}
                </h3>
                <p style={{ fontSize: 15, lineHeight: 1.65, color: '#888' }}>
                  {step.body}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ── TESTIMONIALS ─────────────────────────────────────────────── */}
      <section id="reviews" className="testimonials-section" style={{ padding: '96px 5vw', background: '#FAFAFA' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <motion.div
            initial={{ opacity: 0, y: 32 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.55 }}
            style={{ textAlign: 'center', marginBottom: 56 }}
          >
            <div style={{
              display: 'inline-flex', alignItems: 'center', gap: 8,
              padding: '6px 16px', borderRadius: 9999,
              background: `${BRAND}12`, marginBottom: 20,
            }}>
              <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>REAL STORIES</span>
            </div>
            <h2 style={{ fontSize: 'clamp(32px, 5vw, 52px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A', lineHeight: 1.1 }}>
              Trips that started<br />with a swipe
            </h2>
          </motion.div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 24 }}>
            {TESTIMONIALS.map((t, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 40 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-60px' }}
                transition={{ duration: 0.55, delay: i * 0.1 }}
                style={{
                  background: '#fff',
                  borderRadius: 24,
                  padding: '36px 32px',
                  border: '1px solid rgba(0,0,0,0.07)',
                  boxShadow: '0 4px 32px rgba(0,0,0,0.05)',
                }}
              >
                <div style={{ display: 'flex', gap: 2, marginBottom: 20 }}>
                  {Array.from({ length: t.rating }).map((_, s) => (
                    <span key={s} style={{ color: '#FFDD00', fontSize: 18 }}>★</span>
                  ))}
                </div>
                <p style={{ fontSize: 16, lineHeight: 1.7, color: '#333', fontStyle: 'italic', marginBottom: 28 }}>
                  &ldquo;{t.quote}&rdquo;
                </p>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div style={{ width: 44, height: 44, borderRadius: '50%', overflow: 'hidden', border: `2px solid ${BRAND}44`, flexShrink: 0 }}>
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={t.photo} alt={t.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" decoding="async" width="44" height="44" />
                  </div>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 15, color: '#0A0A0A' }}>{t.name}</div>
                    <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>✈ {t.detail}</div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* ── FINAL CTA ─────────────────────────────────────────────────── */}
      <section id="download" className="cta-section" style={{
        background: `linear-gradient(135deg, ${BRAND} 0%, #FF8800 100%)`,
        padding: '96px 5vw',
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}>
        {/* Decorative orbit in background */}
        <div style={{ position: 'absolute', right: -80, top: '50%', transform: 'translateY(-50%)', opacity: 0.15, pointerEvents: 'none' }}>
          <HeroOrbitO size={320} />
        </div>
        <div style={{ position: 'absolute', left: -80, top: '50%', transform: 'translateY(-50%)', opacity: 0.1, pointerEvents: 'none' }}>
          <HeroOrbitO size={240} />
        </div>

        <motion.div
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.65 }}
          style={{ position: 'relative', zIndex: 1 }}
        >
          <h2 style={{
            fontSize: 'clamp(36px, 6vw, 72px)', fontWeight: 900,
            letterSpacing: '-0.04em', color: '#fff', lineHeight: 1.1, marginBottom: 20,
          }}>
            Your travel partner<br />is already on Nomapal.
          </h2>
          <p style={{ fontSize: 18, color: 'rgba(255,255,255,0.8)', marginBottom: 40, fontWeight: 400 }}>
            Join 12,400+ people who stopped traveling alone.
          </p>
          <DownloadButtons centered />
        </motion.div>
      </section>

      {/* ── FOOTER ────────────────────────────────────────────────────── */}
      <footer className="footer-section" style={{ background: '#0A0A0A', padding: '48px 5vw 32px' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 24, marginBottom: 40, paddingBottom: 40, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
            {/* Logo */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 32, height: 32, borderRadius: '50%' }} loading="lazy" decoding="async" width="32" height="32" />
              <span style={{ fontSize: 18, fontWeight: 900, letterSpacing: '-0.04em', color: '#fff' }}>nomapal</span>
            </div>
            {/* Links */}
            <div style={{ display: 'flex', gap: 32, flexWrap: 'wrap' }}>
              {[
                { label: 'About', href: '/landing/about' },
                { label: 'FAQ', href: '/landing/faq' },
                { label: 'Privacy Policy', href: '/landing/privacy' },
                { label: 'Terms of Service', href: '/landing/terms' },
                { label: 'Contact', href: 'mailto:hello@nomapal.com' },
              ].map(link => (
                <a key={link.label} href={link.href} style={{ fontSize: 14, color: '#555', textDecoration: 'none', fontWeight: 500 }}>{link.label}</a>
              ))}
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 16 }}>
            <p style={{ fontSize: 13, color: '#444' }}>© {new Date().getFullYear()} Nomapal. All rights reserved.</p>
            <p style={{ fontSize: 13, color: '#444' }}>Made for wanderers, by wanderers. ✈</p>
          </div>
        </div>
      </footer>

      <style>{`
        * { box-sizing: border-box; }
        @media (min-width: 768px) {
          .nav-link { display: inline-block !important; }
          .nav-link-wide { display: none !important; }
        }
        @media (min-width: 1024px) {
          .nav-link-wide { display: inline-block !important; }
        }
        @media (max-width: 480px) {
          .hero-section { padding-left: 20px !important; padding-right: 20px !important; }
          .stats-bar { gap: 16px 24px !important; }
          .community-section { padding: 56px 20px !important; }
          .features-section { padding: 56px 20px !important; }
          .howitworks-section { padding: 64px 20px !important; }
          .testimonials-section { padding: 64px 20px !important; }
          .cta-section { padding: 64px 20px !important; }
          .footer-section { padding: 40px 20px 24px !important; }
        }
        @media (max-width: 360px) {
          .wordmark-text { font-size: 46px !important; }
        }
      `}</style>
    </div>
  )
}
