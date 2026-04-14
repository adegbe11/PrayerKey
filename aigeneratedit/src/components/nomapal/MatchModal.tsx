'use client'

import { useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'
import { useColor } from './ColorProvider'

export type MatchType = 'travel' | 'destination' | 'trip'

const CONFETTI_COLORS = ['#FF5A5F','#FF8800','#FFDD00','#44CC44','#0088FF','#3344CC','#9933CC']

function Confetti({ count = 60 }: { count?: number }) {
  return (
    <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', overflow: 'hidden' }}>
      {Array.from({ length: count }, (_, i) => (
        <div key={i} style={{
          position: 'absolute',
          left: `${Math.random() * 100}%`,
          top: -20,
          width: Math.random() * 10 + 6,
          height: Math.random() * 10 + 6,
          borderRadius: Math.random() > 0.5 ? '50%' : '2px',
          background: CONFETTI_COLORS[i % CONFETTI_COLORS.length],
          animation: `confettiFall ${1.5 + Math.random() * 2}s ${Math.random() * 0.8}s ease-in both`,
        }} />
      ))}
    </div>
  )
}

const MATCH_CONFIG = {
  travel: {
    badge: '🤝 Travel Match',
    headline: (name: string) => `You and ${name} want to explore!`,
    sub: (name: string) => `${name} is open to traveling with you. Say hi and see where it goes.`,
    primaryCTA: 'Send Message',
    bg: 'rgba(0,0,0,0.93)',
    accentColor: null as null | string,
    centerIcon: '🌍',
    confetti: 50,
  },
  destination: {
    badge: '✈️ Destination Match',
    headline: (_: string, dest?: string) => `You're both headed to ${dest ?? 'the same place'}!`,
    sub: (name: string) => `You and ${name} have overlapping destinations. This is rare — don't miss it.`,
    primaryCTA: 'Plan the Trip',
    bg: 'rgba(0,16,0,0.95)',
    accentColor: '#44CC44',
    centerIcon: '🗺️',
    confetti: 70,
  },
  trip: {
    badge: '🚀 Trip Match',
    headline: (name: string) => `Perfect Trip Match with ${name}!`,
    sub: (_: string, dest?: string) => `Same destination, same vibe${dest ? ` — ${dest}` : ''}. This one's rare.`,
    primaryCTA: 'Start Planning Together',
    bg: 'rgba(0,0,18,0.95)',
    accentColor: '#0088FF',
    centerIcon: '🛫',
    confetti: 90,
  },
}

interface MatchModalProps {
  match: TravelerProfile
  matchType?: MatchType
  sharedDestination?: string
  myPhoto?: string
  onMessage: () => void
  onKeepSwiping: () => void
}

export function MatchModal({ match, matchType = 'travel', sharedDestination, myPhoto, onMessage, onKeepSwiping }: MatchModalProps) {
  const { color, triggerRainbow } = useColor()
  const cfg = MATCH_CONFIG[matchType]
  const accent = cfg.accentColor ?? color

  useEffect(() => { triggerRainbow() }, [triggerRainbow])

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        style={{
          position: 'absolute', inset: 0, zIndex: 200,
          background: cfg.bg,
          display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
        }}
      >
        <Confetti count={cfg.confetti} />

        {/* Tier badge */}
        <motion.div
          initial={{ scale: 0.5, opacity: 0, y: -16 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          transition={{ type: 'spring', stiffness: 320, damping: 22, delay: 0.05 }}
          style={{
            padding: '6px 18px', borderRadius: 9999, marginBottom: 20,
            background: `${accent}22`, border: `1.5px solid ${accent}55`,
          }}
        >
          <span style={{ fontSize: 13, fontWeight: 800, color: accent, letterSpacing: '0.05em' }}>
            {cfg.badge}
          </span>
        </motion.div>

        {/* Headline */}
        <motion.div
          initial={{ scale: 0.8, opacity: 0, y: -10 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          transition={{ type: 'spring', stiffness: 280, damping: 22, delay: 0.12 }}
          style={{ textAlign: 'center', marginBottom: 36, padding: '0 28px' }}
        >
          <div style={{ fontSize: 23, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em', lineHeight: 1.25, marginBottom: 10 }}>
            {cfg.headline(match.name, sharedDestination)}
          </div>
          <div style={{ fontSize: 14, color: 'rgba(255,255,255,0.5)', lineHeight: 1.55 }}>
            {cfg.sub(match.name, sharedDestination)}
          </div>
        </motion.div>

        {/* Photos */}
        <div style={{ display: 'flex', gap: 20, marginBottom: 44, alignItems: 'center' }}>
          <motion.div
            initial={{ x: -120, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 22, delay: 0.2 }}
          >
            <div style={{
              width: 110, height: 110, borderRadius: '50%', overflow: 'hidden',
              border: `4px solid ${accent}`,
              boxShadow: `0 0 0 4px rgba(255,255,255,0.08), 0 16px 48px ${accent}44`,
            }}>
              <img src={myPhoto ?? 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80'}
                alt="You" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          </motion.div>

          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 400, damping: 14, delay: 0.3 }}
            style={{
              width: 44, height: 44, borderRadius: '50%',
              background: matchType === 'travel'
                ? `conic-gradient(${CONFETTI_COLORS.join(', ')})`
                : matchType === 'destination'
                ? 'linear-gradient(135deg, #44CC44, #0088FF)'
                : 'linear-gradient(135deg, #0088FF, #9933CC)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: `0 0 28px ${accent}66`, fontSize: 20,
            }}
          >
            {cfg.centerIcon}
          </motion.div>

          <motion.div
            initial={{ x: 120, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 22, delay: 0.2 }}
          >
            <div style={{
              width: 110, height: 110, borderRadius: '50%', overflow: 'hidden',
              border: `4px solid ${accent}`,
              boxShadow: `0 0 0 4px rgba(255,255,255,0.08), 0 16px 48px ${accent}44`,
            }}>
              <img src={match.photos[0]} alt={match.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          </motion.div>
        </div>

        {/* CTAs */}
        <motion.div
          initial={{ y: 40, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.35 }}
          style={{ width: '100%', padding: '0 32px', display: 'flex', flexDirection: 'column', gap: 12 }}
        >
          <button onClick={onMessage} style={{
            padding: '18px', fontSize: 17, fontWeight: 700, borderRadius: 9999,
            border: 'none', cursor: 'pointer', color: '#fff',
            background: accent, boxShadow: `0 8px 32px ${accent}55`,
          }}>
            {cfg.primaryCTA}
          </button>
          <button onClick={onKeepSwiping} style={{
            padding: '18px', background: 'rgba(255,255,255,0.10)', color: '#fff',
            fontSize: 17, fontWeight: 600, borderRadius: 9999,
            border: '2px solid rgba(255,255,255,0.18)', cursor: 'pointer',
          }}>
            Keep Swiping
          </button>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}
