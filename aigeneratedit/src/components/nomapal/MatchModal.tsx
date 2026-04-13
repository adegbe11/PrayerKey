'use client'

import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useRouter } from 'next/navigation'
import type { TravelerProfile } from '@/lib/types'
import { useColor } from './ColorProvider'

const CONFETTI_COLORS = ['#FF4444','#FF8800','#FFDD00','#44CC44','#0088FF','#3344CC','#9933CC']

function Confetti() {
  const pieces = Array.from({ length: 60 }, (_, i) => i)
  return (
    <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', overflow: 'hidden' }}>
      {pieces.map(i => (
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

interface MatchModalProps {
  match: TravelerProfile
  myPhoto?: string
  onMessage: () => void
  onKeepSwiping: () => void
}

export function MatchModal({ match, myPhoto, onMessage, onKeepSwiping }: MatchModalProps) {
  const { color, triggerRainbow } = useColor()
  const [show, setShow] = useState(false)

  useEffect(() => {
    triggerRainbow()
    setTimeout(() => setShow(true), 100)
  }, [triggerRainbow])

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        style={{
          position: 'absolute', inset: 0, zIndex: 200,
          background: 'rgba(0,0,0,0.92)',
          display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
        }}
      >
        <Confetti />

        {/* Headline */}
        <motion.div
          initial={{ scale: 0.7, opacity: 0, y: -20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          transition={{ type: 'spring', stiffness: 300, damping: 20, delay: 0.1 }}
          style={{ textAlign: 'center', marginBottom: 40, padding: '0 24px' }}
        >
          <div style={{
            fontSize: 13, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.2em',
            color: color, marginBottom: 8, transition: 'color 280ms',
          }}>
            It's a Match
          </div>
          <div style={{
            fontSize: 28, fontWeight: 800, color: '#fff',
            letterSpacing: '-0.02em', lineHeight: 1.2,
          }}>
            You're both heading<br />
            <span style={{ color }}>{match.destinations[0] ?? 'the same way'}</span>
          </div>
        </motion.div>

        {/* Photos */}
        <div style={{ display: 'flex', gap: 20, marginBottom: 48, alignItems: 'center' }}>
          <motion.div
            initial={{ x: -120, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 22, delay: 0.25 }}
          >
            <div style={{
              width: 120, height: 120, borderRadius: '50%', overflow: 'hidden',
              border: `4px solid ${color}`,
              boxShadow: `0 0 0 4px rgba(255,255,255,0.1), 0 16px 48px ${color}44`,
              transition: 'border-color 280ms, box-shadow 280ms',
            }}>
              <img
                src={myPhoto ?? 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80'}
                alt="You"
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>
          </motion.div>

          {/* Rainbow burst in center */}
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 400, damping: 15, delay: 0.35 }}
            style={{
              width: 40, height: 40, borderRadius: '50%',
              background: `conic-gradient(${CONFETTI_COLORS.join(', ')})`,
              boxShadow: '0 0 24px rgba(255,255,255,0.4)',
            }}
          />

          <motion.div
            initial={{ x: 120, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 22, delay: 0.25 }}
          >
            <div style={{
              width: 120, height: 120, borderRadius: '50%', overflow: 'hidden',
              border: `4px solid ${color}`,
              boxShadow: `0 0 0 4px rgba(255,255,255,0.1), 0 16px 48px ${color}44`,
              transition: 'border-color 280ms, box-shadow 280ms',
            }}>
              <img
                src={match.photos[0]}
                alt={match.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>
          </motion.div>
        </div>

        {/* CTA buttons */}
        <motion.div
          initial={{ y: 40, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.45, duration: 0.35 }}
          style={{ width: '100%', padding: '0 32px', display: 'flex', flexDirection: 'column', gap: 12 }}
        >
          <button
            onClick={onMessage}
            style={{
              padding: '18px', background: color, color: '#fff',
              fontSize: 17, fontWeight: 700, borderRadius: 9999,
              border: 'none', cursor: 'pointer', transition: 'background 280ms',
              boxShadow: `0 8px 32px ${color}55`,
            }}
          >
            Send Message
          </button>
          <button
            onClick={onKeepSwiping}
            style={{
              padding: '18px', background: 'rgba(255,255,255,0.12)', color: '#fff',
              fontSize: 17, fontWeight: 600, borderRadius: 9999,
              border: '2px solid rgba(255,255,255,0.2)', cursor: 'pointer',
            }}
          >
            Keep Swiping
          </button>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}
