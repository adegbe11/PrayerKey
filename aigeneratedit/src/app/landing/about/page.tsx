'use client'

import { motion } from 'framer-motion'

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

const TEAM = [
  { name: 'Kai Rivera', role: 'Co-founder and CEO', bio: 'Kai has visited 58 countries and spent years trying to find travel companions the hard way. Nomapal is the app he always wished existed.', photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80' },
  { name: 'Amara Osei', role: 'Co-founder and Product', bio: 'Amara built community apps for five years before realizing the biggest community problem was that travelers had no good way to find each other.', photo: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&q=80' },
  { name: 'Lucas Moreira', role: 'Head of Engineering', bio: 'Lucas loves building products people use every day. He joined Nomapal after meeting his best friend on a random flight to Lisbon.', photo: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80' },
]

const VALUES = [
  { emoji: '🌍', title: 'Travel should bring people together', body: 'We believe some of the best friendships start in airports, on trains, and at hostels. Nomapal makes those connections happen before you even leave home.' },
  { emoji: '🛡️', title: 'Safety is not optional', body: 'Every person on Nomapal goes through verification. We take your safety seriously because the best trips are the ones where you feel comfortable from day one.' },
  { emoji: '🤝', title: 'Real people, real trips', body: 'We are not a social network. We are a tool for people who actually travel. Every feature we build is designed to help you get out there, not scroll more.' },
]

export default function AboutPage() {
  return (
    <div style={{ background: '#FAFAFA', fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif", minHeight: '100vh' }}>
      <Nav />

      {/* Hero */}
      <section style={{ padding: '120px 5vw 80px', textAlign: 'center', maxWidth: 800, margin: '0 auto' }}>
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
            <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>OUR STORY</span>
          </div>
          <h1 style={{ fontSize: 'clamp(40px, 6vw, 72px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A', lineHeight: 1.1, marginBottom: 24 }}>
            We built the app<br />we always needed
          </h1>
          <p style={{ fontSize: 20, lineHeight: 1.7, color: '#555', marginBottom: 16 }}>
            Nomapal started because finding someone to travel with was harder than booking a flight, finding a hotel, or packing a bag.
          </p>
          <p style={{ fontSize: 18, lineHeight: 1.7, color: '#666' }}>
            Dating apps were full of people who just wanted to date. Facebook groups moved too slowly. Reddit was hit or miss. There was no dedicated place for travelers to find each other based on where they were actually going.
          </p>
        </motion.div>
      </section>

      {/* Mission */}
      <section style={{ background: '#0A0A0A', padding: '80px 5vw' }}>
        <motion.div
          initial={{ opacity: 0, y: 32 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          style={{ maxWidth: 800, margin: '0 auto', textAlign: 'center' }}
        >
          <p style={{ fontSize: 'clamp(22px, 3.5vw, 36px)', fontWeight: 700, color: '#fff', lineHeight: 1.5, letterSpacing: '-0.02em' }}>
            Our mission is simple. Make it easy for anyone, anywhere in the world, to find a travel companion they actually want to travel with.
          </p>
        </motion.div>
      </section>

      {/* Values */}
      <section style={{ padding: '96px 5vw' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.55 }}
            style={{ textAlign: 'center', marginBottom: 64 }}
          >
            <h2 style={{ fontSize: 'clamp(32px, 5vw, 52px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A' }}>
              What we believe in
            </h2>
          </motion.div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 32 }}>
            {VALUES.map((v, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 32 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.55, delay: i * 0.1 }}
                style={{
                  background: '#fff', borderRadius: 24, padding: '40px 32px',
                  border: '1px solid rgba(0,0,0,0.07)',
                  boxShadow: '0 4px 32px rgba(0,0,0,0.05)',
                }}
              >
                <div style={{ fontSize: 40, marginBottom: 20 }}>{v.emoji}</div>
                <h3 style={{ fontSize: 20, fontWeight: 800, color: '#0A0A0A', marginBottom: 12, letterSpacing: '-0.02em' }}>{v.title}</h3>
                <p style={{ fontSize: 15, lineHeight: 1.7, color: '#555' }}>{v.body}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Team */}
      <section style={{ background: '#fff', padding: '96px 5vw' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            style={{ textAlign: 'center', marginBottom: 64 }}
          >
            <h2 style={{ fontSize: 'clamp(32px, 5vw, 52px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A' }}>
              The people behind Nomapal
            </h2>
          </motion.div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 40 }}>
            {TEAM.map((member, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 32 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.55, delay: i * 0.1 }}
                style={{ textAlign: 'center' }}
              >
                <div style={{ width: 100, height: 100, borderRadius: '50%', overflow: 'hidden', margin: '0 auto 20px', border: `3px solid ${BRAND}33` }}>
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={member.photo} alt={member.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
                <div style={{ fontWeight: 800, fontSize: 18, color: '#0A0A0A', marginBottom: 4 }}>{member.name}</div>
                <div style={{ fontSize: 13, fontWeight: 600, color: BRAND, marginBottom: 14, letterSpacing: '0.02em' }}>{member.role}</div>
                <p style={{ fontSize: 15, lineHeight: 1.7, color: '#555' }}>{member.bio}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section style={{
        background: `linear-gradient(135deg, ${BRAND} 0%, #FF8800 100%)`,
        padding: '80px 5vw', textAlign: 'center',
      }}>
        <h2 style={{ fontSize: 'clamp(28px, 4vw, 52px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#fff', marginBottom: 16 }}>
          Ready to find your travel partner?
        </h2>
        <p style={{ fontSize: 18, color: 'rgba(255,255,255,0.85)', marginBottom: 36 }}>
          Thousands of travelers are already on Nomapal. Your next trip buddy is waiting.
        </p>
        <a href="/landing#download" style={{
          display: 'inline-block', padding: '16px 40px',
          background: '#fff', color: BRAND,
          borderRadius: 9999, fontSize: 16, fontWeight: 800,
          textDecoration: 'none', letterSpacing: '-0.02em',
        }}>
          Download Nomapal
        </a>
      </section>

      {/* Footer */}
      <footer style={{ background: '#0A0A0A', padding: '32px 5vw', textAlign: 'center' }}>
        <p style={{ fontSize: 13, color: '#555' }}>
          © {new Date().getFullYear()} Nomapal. All rights reserved.
          {' · '}
          <a href="/landing/faq" style={{ color: '#555', textDecoration: 'none' }}>FAQ</a>
          {' · '}
          <a href="/landing/privacy" style={{ color: '#555', textDecoration: 'none' }}>Privacy</a>
          {' · '}
          <a href="mailto:hello@nomapal.com" style={{ color: '#555', textDecoration: 'none' }}>Contact</a>
        </p>
      </footer>
    </div>
  )
}
