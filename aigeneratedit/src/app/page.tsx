'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { motion, AnimatePresence } from 'framer-motion'

// Scattered travel icons in background
const WATERMARKS = [
  { icon: '✈️', top: '8%',  left: '12%', size: 28, rot: -20, opacity: 0.18 },
  { icon: '✈️', top: '15%', left: '74%', size: 20, rot:  35, opacity: 0.12 },
  { icon: '🧭', top: '28%', left: '6%',  size: 22, rot:   0, opacity: 0.14 },
  { icon: '✈️', top: '38%', left: '82%', size: 32, rot: -15, opacity: 0.15 },
  { icon: '🗺️', top: '55%', left: '10%', size: 24, rot:  10, opacity: 0.12 },
  { icon: '✈️', top: '65%', left: '70%', size: 20, rot:  40, opacity: 0.13 },
  { icon: '🧭', top: '75%', left: '30%', size: 18, rot: -10, opacity: 0.10 },
  { icon: '✈️', top: '82%', left: '85%', size: 26, rot:  25, opacity: 0.14 },
  { icon: '🗺️', top: '88%', left: '5%',  size: 20, rot:  -5, opacity: 0.11 },
]

function SplashScreen() {
  return (
    <motion.div
      initial={{ opacity: 1 }}
      exit={{ opacity: 0, scale: 1.04 }}
      transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
      style={{
        position: 'absolute', inset: 0,
        background: 'linear-gradient(160deg, #FF5A5F 0%, #FF6B35 55%, #FF5A5F 100%)',
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center',
        overflow: 'hidden',
      }}
    >
      {/* Watermarks */}
      {WATERMARKS.map((w, i) => (
        <div
          key={i}
          style={{
            position: 'absolute',
            top: w.top, left: w.left,
            fontSize: w.size,
            opacity: w.opacity,
            transform: `rotate(${w.rot}deg)`,
            pointerEvents: 'none',
            filter: 'grayscale(1) brightness(10)',
          }}
        >
          {w.icon}
        </div>
      ))}

      {/* Logo mark */}
      <motion.div
        initial={{ scale: 0.6, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ delay: 0.1, duration: 0.55, type: 'spring', stiffness: 260, damping: 20 }}
        style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14 }}
      >
        {/* N circle mark */}
        <div style={{
          width: 88, height: 88, borderRadius: '50%',
          background: 'rgba(255,255,255,0.18)',
          border: '2.5px solid rgba(255,255,255,0.35)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          backdropFilter: 'blur(8px)',
        }}>
          <span style={{
            fontSize: 44, fontWeight: 900, color: '#fff',
            letterSpacing: '-0.05em', lineHeight: 1,
          }}>N</span>
        </div>

        {/* Wordmark */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35, duration: 0.4 }}
          style={{
            fontSize: 26, fontWeight: 900, color: '#fff',
            letterSpacing: '-0.04em',
          }}
        >
          nomapal
        </motion.div>
      </motion.div>

      {/* Tagline */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 0.7 }}
        transition={{ delay: 0.55, duration: 0.4 }}
        style={{
          position: 'absolute', bottom: 48,
          fontSize: 13, color: '#fff', letterSpacing: '0.08em',
          fontWeight: 500, textTransform: 'uppercase',
        }}
      >
        Travel together
      </motion.div>
    </motion.div>
  )
}

export default function RootPage() {
  const router = useRouter()
  const [showSplash, setShowSplash] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => {
      setShowSplash(false)
      // Small delay to let exit animation finish before routing
      setTimeout(() => {
        router.replace('/discover')
      }, 500)
    }, 2000)
    return () => clearTimeout(timer)
  }, [router])

  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <AnimatePresence>
        {showSplash && <SplashScreen />}
      </AnimatePresence>
    </div>
  )
}
