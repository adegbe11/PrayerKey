'use client'

import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'

const SPECTRUM = ['#FF5A5F','#FF8800','#FFDD00','#44CC44','#0088FF','#3344CC','#9933CC']

const SUSPENSE_LINES = [
  'Checking travel vibes…',
  'Scanning destinations…',
  'Calculating compatibility…',
  'Analysing trip overlap…',
]

interface MatchAnticipationProps {
  profile: TravelerProfile
  myPhoto?: string
  onReveal: () => void
}

export function MatchAnticipation({ profile, myPhoto, onReveal }: MatchAnticipationProps) {
  const [lineIndex, setLineIndex] = useState(0)
  const [pulse, setPulse] = useState(false)

  useEffect(() => {
    // Cycle through suspense lines
    const lineInterval = setInterval(() => {
      setLineIndex(i => (i + 1) % SUSPENSE_LINES.length)
    }, 420)

    // Pulse the photos
    const pulseInterval = setInterval(() => setPulse(p => !p), 600)

    // Reveal after 1.9s
    const reveal = setTimeout(() => {
      clearInterval(lineInterval)
      clearInterval(pulseInterval)
      onReveal()
    }, 1900)

    return () => {
      clearInterval(lineInterval)
      clearInterval(pulseInterval)
      clearTimeout(reveal)
    }
  }, [onReveal])

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0, scale: 0.96 }}
      transition={{ duration: 0.22 }}
      style={{
        position: 'absolute', inset: 0, zIndex: 190,
        background: 'rgba(0,0,0,0.96)',
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 0,
      }}
    >
      {/* Photos with pulsing ring */}
      <div style={{ display: 'flex', gap: 24, alignItems: 'center', marginBottom: 48 }}>
        {/* My photo */}
        <motion.div
          animate={{ scale: pulse ? 1.06 : 1 }}
          transition={{ duration: 0.5, ease: 'easeInOut' }}
          style={{ position: 'relative' }}
        >
          {/* Rotating spectrum ring */}
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
            style={{
              position: 'absolute', inset: -5,
              borderRadius: '50%',
              background: `conic-gradient(${SPECTRUM.join(', ')}, ${SPECTRUM[0]})`,
            }}
          />
          <div style={{
            position: 'relative',
            width: 96, height: 96, borderRadius: '50%', overflow: 'hidden',
            border: '3px solid #000',
          }}>
            <img
              src={myPhoto ?? 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80'}
              alt="You"
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </div>
        </motion.div>

        {/* Scanning dots */}
        <div style={{ display: 'flex', gap: 6 }}>
          {[0, 1, 2].map(i => (
            <motion.div
              key={i}
              animate={{ opacity: [0.2, 1, 0.2], scale: [0.8, 1.2, 0.8] }}
              transition={{ duration: 0.8, repeat: Infinity, delay: i * 0.18 }}
              style={{
                width: 8, height: 8, borderRadius: '50%',
                background: SPECTRUM[i * 2],
              }}
            />
          ))}
        </div>

        {/* Their photo */}
        <motion.div
          animate={{ scale: pulse ? 1 : 1.06 }}
          transition={{ duration: 0.5, ease: 'easeInOut' }}
          style={{ position: 'relative' }}
        >
          <motion.div
            animate={{ rotate: -360 }}
            transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
            style={{
              position: 'absolute', inset: -5,
              borderRadius: '50%',
              background: `conic-gradient(${[...SPECTRUM].reverse().join(', ')}, ${SPECTRUM[6]})`,
            }}
          />
          <div style={{
            position: 'relative',
            width: 96, height: 96, borderRadius: '50%', overflow: 'hidden',
            border: '3px solid #000',
          }}>
            <img
              src={profile.photos[0]}
              alt={profile.name}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </div>
        </motion.div>
      </div>

      {/* Scanning bar */}
      <div style={{
        width: 200, height: 3, background: 'rgba(255,255,255,0.1)',
        borderRadius: 2, marginBottom: 20, overflow: 'hidden',
      }}>
        <motion.div
          animate={{ x: ['-100%', '100%'] }}
          transition={{ duration: 0.9, repeat: Infinity, ease: 'easeInOut' }}
          style={{
            height: '100%', width: '40%',
            background: `linear-gradient(90deg, transparent, #fff, transparent)`,
            borderRadius: 2,
          }}
        />
      </div>

      {/* Cycling suspense text */}
      <AnimatePresence mode="wait">
        <motion.div
          key={lineIndex}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -8 }}
          transition={{ duration: 0.18 }}
          style={{ fontSize: 14, color: 'rgba(255,255,255,0.55)', fontWeight: 600, letterSpacing: '0.02em' }}
        >
          {SUSPENSE_LINES[lineIndex]}
        </motion.div>
      </AnimatePresence>
    </motion.div>
  )
}
