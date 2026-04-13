'use client'

import { useState, useCallback } from 'react'
import { AnimatePresence } from 'framer-motion'
import { SwipeCard } from '@/components/nomapal/SwipeCard'
import { MatchModal } from '@/components/nomapal/MatchModal'
import { ProfileExpand } from '@/components/nomapal/ProfileExpand'
import { useColor } from '@/components/nomapal/ColorProvider'
import { MOCK_PROFILES } from '@/lib/mockData'
import { useRouter } from 'next/navigation'
import type { TravelerProfile } from '@/lib/types'

export default function DiscoverPage() {
  const { color, advance, retreat, triggerRainbow } = useColor()
  const router = useRouter()

  const [profiles, setProfiles] = useState<TravelerProfile[]>([...MOCK_PROFILES])
  const [match, setMatch] = useState<TravelerProfile | null>(null)
  const [expanded, setExpanded] = useState<TravelerProfile | null>(null)
  const [boostActive, setBoostActive] = useState(false)
  const [boostSeconds, setBoostSeconds] = useState(0)

  // Action buttons visibility
  const [likeAnim, setLikeAnim] = useState(false)
  const [nopeAnim, setNopeAnim] = useState(false)
  const [superAnim, setSuperAnim] = useState(false)

  function handleLike() {
    setLikeAnim(true)
    setTimeout(() => setLikeAnim(false), 300)
    advance()
    // 35% match chance
    const top = profiles[0]
    if (top && Math.random() < 0.35) {
      setTimeout(() => {
        triggerRainbow()
        setMatch(top)
      }, 400)
    }
    setProfiles(prev => prev.slice(1))
  }

  function handleNope() {
    setNopeAnim(true)
    setTimeout(() => setNopeAnim(false), 300)
    retreat()
    setProfiles(prev => prev.slice(1))
  }

  function handleSuperLike() {
    setSuperAnim(true)
    setTimeout(() => setSuperAnim(false), 300)
    advance()
    const top = profiles[0]
    if (top) {
      setTimeout(() => { triggerRainbow(); setMatch(top) }, 400)
    }
    setProfiles(prev => prev.slice(1))
  }

  function handleBoost() {
    setBoostActive(true)
    setBoostSeconds(1800) // 30 min
    const interval = setInterval(() => {
      setBoostSeconds(s => {
        if (s <= 1) { clearInterval(interval); setBoostActive(false); return 0 }
        return s - 1
      })
    }, 1000)
    advance()
  }

  const visibleProfiles = profiles.slice(0, 3)
  const empty = profiles.length === 0

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#f5f5f5', display: 'flex', flexDirection: 'column' }}>
      {/* Active color background tint */}
      <div style={{
        position: 'absolute', inset: 0,
        background: `${color}06`,
        transition: 'background 180ms',
        pointerEvents: 'none',
      }} />

      {/* Top bar */}
      <div style={{
        position: 'relative', display: 'flex', alignItems: 'center',
        justifyContent: 'space-between', padding: '16px 20px 8px',
        background: 'transparent', zIndex: 10,
      }}>
        {/* Logo */}
        <div style={{
          fontSize: 28, fontWeight: 900, letterSpacing: '-0.04em',
          color, transition: 'color 280ms',
        }}>
          nomapal
        </div>

        {/* Boost button */}
        <button
          onClick={handleBoost}
          style={{
            display: 'flex', alignItems: 'center', gap: 6,
            padding: '8px 14px',
            background: boostActive ? color : 'rgba(255,255,255,0.9)',
            borderRadius: 9999,
            border: `2px solid ${boostActive ? color : '#ebebeb'}`,
            cursor: 'pointer', fontSize: 13, fontWeight: 700,
            color: boostActive ? '#fff' : '#3b3b3b',
            boxShadow: boostActive ? `0 4px 16px ${color}44` : '0 2px 8px rgba(0,0,0,0.08)',
            transition: 'all 280ms',
          }}
        >
          <span>🔥</span>
          {boostActive
            ? `${Math.floor(boostSeconds / 60)}:${String(boostSeconds % 60).padStart(2, '0')}`
            : 'Boost'
          }
        </button>

        {/* Filter button */}
        <button style={{
          width: 40, height: 40, borderRadius: '50%',
          background: 'rgba(255,255,255,0.9)',
          border: '2px solid #ebebeb',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer', fontSize: 18,
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        }}>
          ⚡
        </button>
      </div>

      {/* Card deck area */}
      <div style={{ flex: 1, position: 'relative', margin: '8px 16px 16px' }}>
        {empty ? (
          <div style={{
            position: 'absolute', inset: 0,
            display: 'flex', flexDirection: 'column',
            alignItems: 'center', justifyContent: 'center',
            gap: 16, textAlign: 'center', padding: '32px',
          }}>
            <div style={{ fontSize: 64 }}>✈️</div>
            <div style={{ fontSize: 24, fontWeight: 800, color: '#141414', letterSpacing: '-0.02em' }}>
              You've seen everyone nearby.
            </div>
            <div style={{ fontSize: 15, color: '#737373' }}>
              Expand your radius or turn on Global mode to see travelers worldwide.
            </div>
            <button style={{
              marginTop: 8, padding: '16px 28px',
              background: color, color: '#fff', borderRadius: 9999,
              border: 'none', cursor: 'pointer',
              fontSize: 16, fontWeight: 700, transition: 'background 280ms',
            }}>
              Go Global
            </button>
          </div>
        ) : (
          [...visibleProfiles].reverse().map((profile, reversedIndex) => {
            const stackIndex = (visibleProfiles.length - 1) - reversedIndex as 0 | 1 | 2
            const isTop = stackIndex === 0
            return (
              <SwipeCard
                key={profile.id}
                profile={profile}
                isTop={isTop}
                stackIndex={stackIndex}
                onLike={handleLike}
                onNope={handleNope}
                onSuperLike={handleSuperLike}
                onExpand={() => isTop && setExpanded(profile)}
                color={color}
              />
            )
          })
        )}
      </div>

      {/* Action buttons */}
      {!empty && (
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          gap: 16, padding: '0 24px 16px', position: 'relative', zIndex: 10,
        }}>
          {/* Rewind */}
          <button style={{
            width: 44, height: 44, borderRadius: '50%',
            background: 'white', border: '2px solid #ebebeb',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', fontSize: 18,
            boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
          }}>
            ↩
          </button>

          {/* Nope */}
          <button
            onClick={handleNope}
            style={{
              width: 64, height: 64, borderRadius: '50%',
              background: nopeAnim ? '#FF4444' : 'white',
              border: '2px solid #ebebeb',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer', fontSize: 28,
              boxShadow: '0 4px 20px rgba(0,0,0,0.10)',
              transform: nopeAnim ? 'scale(0.92)' : 'scale(1)',
              transition: 'all 120ms',
            }}
          >
            <span style={{ color: nopeAnim ? '#fff' : '#FF4444' }}>✕</span>
          </button>

          {/* Super Like */}
          <button
            onClick={handleSuperLike}
            style={{
              width: 56, height: 56, borderRadius: '50%',
              background: superAnim ? '#0088FF' : 'white',
              border: '2px solid #ebebeb',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer', fontSize: 24,
              boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
              transform: superAnim ? 'scale(0.92)' : 'scale(1)',
              transition: 'all 120ms',
            }}
          >
            <span style={{ color: superAnim ? '#fff' : '#0088FF' }}>★</span>
          </button>

          {/* Like */}
          <button
            onClick={handleLike}
            style={{
              width: 64, height: 64, borderRadius: '50%',
              background: likeAnim ? color : 'white',
              border: '2px solid #ebebeb',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer', fontSize: 28,
              boxShadow: `0 4px 20px ${likeAnim ? color + '44' : 'rgba(0,0,0,0.10)'}`,
              transform: likeAnim ? 'scale(0.92)' : 'scale(1)',
              transition: 'all 120ms',
            }}
          >
            <span style={{ color: likeAnim ? '#fff' : color, transition: 'color 120ms' }}>♥</span>
          </button>

          {/* Boost mini */}
          <button style={{
            width: 44, height: 44, borderRadius: '50%',
            background: 'white', border: '2px solid #ebebeb',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', fontSize: 18,
            boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
          }}>
            ⚡
          </button>
        </div>
      )}

      {/* Match modal */}
      <AnimatePresence>
        {match && (
          <MatchModal
            match={match}
            onMessage={() => {
              setMatch(null)
              router.push('/app/messages')
            }}
            onKeepSwiping={() => setMatch(null)}
          />
        )}
      </AnimatePresence>

      {/* Profile expand */}
      <AnimatePresence>
        {expanded && (
          <ProfileExpand
            profile={expanded}
            onClose={() => setExpanded(null)}
            onLike={() => handleLike()}
            onNope={() => handleNope()}
          />
        )}
      </AnimatePresence>
    </div>
  )
}
