'use client'

import { useState } from 'react'
import { motion, useMotionValue, useTransform, useAnimation } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'

const VERIFIED_LABELS = ['', 'Email', 'Phone', 'ID Verified', 'Verified Nomad']

interface SwipeCardProps {
  profile: TravelerProfile
  isTop: boolean
  stackIndex: number
  onLike: () => void
  onNope: () => void
  onSuperLike: () => void
  onDreamCrew: () => void
  onExpand: () => void
  color: string
  userDestinations?: string[]
}

export function SwipeCard({
  profile, isTop, stackIndex, onLike, onNope, onSuperLike, onDreamCrew, onExpand, color, userDestinations = [],
}: SwipeCardProps) {
  const [photoIndex, setPhotoIndex] = useState(0)
  const [isDragging, setIsDragging] = useState(false)

  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const controls = useAnimation()

  const rotate = useTransform(x, [-200, 0, 200], [-15, 0, 15])

  const likeOpacity  = useTransform(x, [0, 60, 120], [0, 0.6, 1])
  const nopeOpacity  = useTransform(x, [-120, -60, 0], [1, 0.6, 0])
  const superOpacity = useTransform(y, [-120, -60, 0], [1, 0.6, 0])
  const dreamOpacity = useTransform(y, [0, 60, 120], [0, 0.6, 1])

  const stackStyles = {
    0: { scale: 1,    y: 0,   zIndex: 30 },
    1: { scale: 0.95, y: -8,  zIndex: 20 },
    2: { scale: 0.90, y: -16, zIndex: 10 },
  }[stackIndex] ?? { scale: 0.85, y: -24, zIndex: 5 }

  // Detect shared destinations between user and profile
  const sharedDests = userDestinations.filter(ud =>
    profile.destinations.some(pd =>
      pd.toLowerCase().includes(ud.toLowerCase()) || ud.toLowerCase().includes(pd.toLowerCase())
    )
  )
  const hasDestMatch = sharedDests.length > 0

  async function handleDragEnd(_: unknown, info: { offset: { x: number; y: number }; velocity: { x: number; y: number } }) {
    const { x: ox, y: oy } = info.offset
    const { x: vx } = info.velocity

    // UP → Same Destination! (Trip Match)
    if (oy < -90 && Math.abs(ox) < 120) {
      await controls.start({ y: -900, opacity: 0, transition: { duration: 0.38 } })
      onSuperLike()
      return
    }

    // DOWN → Dream Crew (save for later)
    if (oy > 90 && Math.abs(ox) < 120) {
      await controls.start({ y: 900, opacity: 0, transition: { duration: 0.38 } })
      onDreamCrew()
      return
    }

    // LEFT / RIGHT
    if (Math.abs(ox) > 120 || Math.abs(vx) > 600) {
      const dir = ox > 0 ? 1 : -1
      await controls.start({
        x: dir * 800, rotate: dir * 20, opacity: 0,
        transition: { duration: 0.35, ease: [0.7, 0, 0.84, 0] },
      })
      if (dir > 0) onLike(); else onNope()
    } else {
      controls.start({ x: 0, y: 0, rotate: 0, transition: { type: 'spring', stiffness: 300, damping: 30 } })
    }
    setIsDragging(false)
  }

  const cardContent = (
    <div style={{ position: 'relative', width: '100%', height: '100%', borderRadius: '20px 20px 0 0', overflow: 'hidden' }}>

      {/* Full-bleed photo */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <img src={profile.photos[photoIndex]} alt={profile.name}
          style={{ width: '100%', height: '100%', objectFit: 'cover', pointerEvents: 'none' }} draggable={false} />
      </div>

      {/* Photo tap zones — left/right half tap to navigate */}
      <div style={{ position: 'absolute', left: 0, top: 0, width: '50%', height: '75%', zIndex: 5 }}
        onClick={e => { e.stopPropagation(); setPhotoIndex(i => Math.max(0, i - 1)) }} />
      <div style={{ position: 'absolute', right: 0, top: 0, width: '50%', height: '75%', zIndex: 5 }}
        onClick={e => { e.stopPropagation(); setPhotoIndex(i => Math.min(profile.photos.length - 1, i + 1)) }} />

      {/* Photo progress segments — inset so they don't look like a border */}
      {profile.photos.length > 1 && (
        <div style={{ position: 'absolute', top: 10, left: 10, right: 10, display: 'flex', gap: 4, zIndex: 10 }}>
          {profile.photos.map((_, i) => (
            <div key={i} style={{
              flex: 1, height: 3, borderRadius: 2,
              background: i === photoIndex ? 'rgba(255,255,255,0.95)' : 'rgba(255,255,255,0.30)',
              transition: 'background 200ms',
            }} />
          ))}
        </div>
      )}

      {/* Full gradient from bottom — covers profile info AND action bar */}
      <div style={{
        position: 'absolute', inset: 0, zIndex: 2,
        background: 'linear-gradient(to top, rgba(0,0,0,0.92) 0%, rgba(0,0,0,0.7) 35%, rgba(0,0,0,0.2) 60%, transparent 100%)',
      }} />

      {/* Destination match badge */}
      {hasDestMatch && (
        <div style={{
          position: 'absolute', top: 44, right: 14, zIndex: 15,
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '5px 10px', borderRadius: 9999,
          background: 'linear-gradient(135deg, #44CC44, #0088FF)',
          boxShadow: '0 4px 16px rgba(68,204,68,0.45)',
        }}>
          <span style={{ fontSize: 11 }}>✈️</span>
          <span style={{ fontSize: 11, fontWeight: 800, color: '#fff', letterSpacing: '0.03em' }}>DEST MATCH</span>
        </div>
      )}

      {/* Swipe stamps */}
      <motion.div className="stamp stamp-like"  style={{ opacity: likeOpacity  }}>WANDER</motion.div>
      <motion.div className="stamp stamp-nope"  style={{ opacity: nopeOpacity  }}>PASS</motion.div>
      <motion.div className="stamp stamp-super" style={{ opacity: superOpacity }}>SAME DEST!</motion.div>
      <motion.div className="stamp stamp-dream" style={{ opacity: dreamOpacity }}>DREAM CREW</motion.div>

      {/* Profile info */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        padding: '24px 16px 20px', zIndex: 5,
      }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8, marginBottom: 4 }}>
          <span style={{ fontSize: 26, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
            {profile.name}, {profile.age}
          </span>
          {profile.verified >= 2 && (
            <div style={{
              display: 'flex', alignItems: 'center', gap: 4,
              padding: '3px 8px', background: 'rgba(0,136,255,0.9)', borderRadius: 9999, marginBottom: 2,
            }}>
              <span style={{ fontSize: 10, fontWeight: 700, color: '#fff' }}>✓ {VERIFIED_LABELS[profile.verified]}</span>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8 }}>
          <span style={{ fontSize: 13, color: 'rgba(255,255,255,0.85)', fontWeight: 500 }}>📍 {profile.location}</span>
          {profile.distance && <span style={{ fontSize: 12, color: 'rgba(255,255,255,0.6)' }}>· {profile.distance}</span>}
        </div>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5, marginBottom: 8 }}>
          {profile.destinations.slice(0, 3).map(d => {
            const isShared = userDestinations.some(ud =>
              d.toLowerCase().includes(ud.toLowerCase()) || ud.toLowerCase().includes(d.toLowerCase())
            )
            return (
              <div key={d} style={{
                padding: '3px 9px',
                background: isShared ? 'rgba(68,204,68,0.3)' : 'rgba(255,255,255,0.15)',
                backdropFilter: 'blur(8px)', borderRadius: 9999,
                border: `1px solid ${isShared ? 'rgba(68,204,68,0.85)' : 'rgba(255,255,255,0.3)'}`,
                fontSize: 11, fontWeight: 700, color: '#fff',
              }}>
                ✈ {d}{isShared ? ' ✓' : ''}
              </div>
            )
          })}
          <div style={{
            padding: '3px 9px', background: 'rgba(255,255,255,0.15)',
            backdropFilter: 'blur(8px)', borderRadius: 9999,
            border: '1px solid rgba(255,255,255,0.3)',
            fontSize: 11, fontWeight: 600, color: 'rgba(255,255,255,0.8)',
          }}>
            {profile.travelDates}
          </div>
        </div>

        <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
          {profile.interests.slice(0, 3).map(i => (
            <span key={i} style={{
              fontSize: 11, color: 'rgba(255,255,255,0.75)', fontWeight: 500,
              background: 'rgba(255,255,255,0.1)', padding: '2px 7px', borderRadius: 6,
            }}>{i}</span>
          ))}
        </div>

        {profile.departureDaysFromNow !== undefined && (
          <div style={{
            marginTop: 6, display: 'inline-flex', alignItems: 'center', gap: 5,
            padding: '3px 9px', borderRadius: 9999,
            background: profile.departureDaysFromNow <= 7
              ? 'rgba(255,68,68,0.85)'
              : profile.departureDaysFromNow <= 14
              ? 'rgba(255,136,0,0.85)'
              : 'rgba(255,255,255,0.15)',
            backdropFilter: 'blur(8px)',
          }}>
            <span style={{ fontSize: 10 }}>✈️</span>
            <span style={{ fontSize: 11, fontWeight: 800, color: '#fff', letterSpacing: '0.01em' }}>
              {profile.departureDaysFromNow <= 1 ? 'Leaving tomorrow!' : `Leaves in ${profile.departureDaysFromNow} days`}
            </span>
          </div>
        )}

        {profile.reputationScore && (
          <div style={{ marginTop: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
            <span style={{ fontSize: 12, color: '#FFD700' }}>★</span>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#fff' }}>{profile.reputationScore}</span>
            <span style={{ fontSize: 11, color: 'rgba(255,255,255,0.55)' }}>({profile.reputationCount} trips)</span>
          </div>
        )}
      </div>

      {/* Up-arrow — tap to view full profile (7-color rainbow border) */}
      {isTop && (
        <button
          onClick={e => { e.stopPropagation(); onExpand() }}
          style={{
            position: 'absolute', bottom: 20, right: 16, zIndex: 20,
            width: 38, height: 38, borderRadius: '50%',
            padding: 2,
            background: 'conic-gradient(from 0deg, #FF0000, #FF7F00, #FFFF00, #00FF00, #0000FF, #8B00FF, #FF0000)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', border: 'none',
            boxShadow: '0 2px 12px rgba(0,0,0,0.4)',
          }}
        >
          <div style={{
            width: '100%', height: '100%', borderRadius: '50%',
            background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <svg width="15" height="15" viewBox="0 0 16 16" fill="none">
              <path d="M8 12V4M4 8l4-4 4 4" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
        </button>
      )}

    </div>
  )

  if (!isTop) {
    return (
      <motion.div style={{
        position: 'absolute', inset: 0,
        scale: stackStyles.scale, y: stackStyles.y, zIndex: stackStyles.zIndex,
        pointerEvents: 'none',
      }}>
        {cardContent}
      </motion.div>
    )
  }

  return (
    <motion.div
      drag
      dragConstraints={{ left: 0, right: 0, top: 0, bottom: 0 }}
      dragElastic={0.85}
      onDragStart={() => setIsDragging(true)}
      onDragEnd={handleDragEnd}
      animate={controls}
      style={{
        position: 'absolute', inset: 0,
        x, y, rotate,
        zIndex: stackStyles.zIndex,
        cursor: isDragging ? 'grabbing' : 'grab',
        touchAction: 'none', userSelect: 'none',
      }}
      whileTap={{ cursor: 'grabbing' }}
    >
      {cardContent}
    </motion.div>
  )
}
