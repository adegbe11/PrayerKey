'use client'

import { useState } from 'react'
import { motion, useMotionValue, useTransform, useAnimation } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'

const BUDGETS: Record<string, string> = {
  budget: '🎒 Budget', midrange: '✈️ Mid-range',
  comfortable: '🏨 Comfortable', luxury: '💎 Luxury',
}
const GOALS: Record<string, string> = {
  friends: '🤝 Travel Friends', group: '👥 Group', open: '💫 Open',
}
const VERIFIED_LABELS = ['', 'Email', 'Phone', 'ID Verified', 'Verified Nomad']

interface SwipeCardProps {
  profile: TravelerProfile
  isTop: boolean
  stackIndex: number  // 0=top, 1=mid, 2=back
  onLike: () => void
  onNope: () => void
  onSuperLike: () => void
  onExpand: () => void
  color: string
}

export function SwipeCard({
  profile, isTop, stackIndex, onLike, onNope, onSuperLike, onExpand, color,
}: SwipeCardProps) {
  const [photoIndex, setPhotoIndex] = useState(0)
  const [isDragging, setIsDragging] = useState(false)

  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const controls = useAnimation()

  // Rotation: ±15 degrees at edge
  const rotate = useTransform(x, [-200, 0, 200], [-15, 0, 15])

  // Stamp opacities
  const likeOpacity  = useTransform(x, [0, 60, 120], [0, 0.5, 1])
  const nopeOpacity  = useTransform(x, [-120, -60, 0], [1, 0.5, 0])
  const superOpacity = useTransform(y, [-120, -60, 0], [1, 0.5, 0])

  // Stack positioning
  const stackStyles = {
    0: { scale: 1,    y: 0,   zIndex: 30 },
    1: { scale: 0.95, y: -8,  zIndex: 20 },
    2: { scale: 0.90, y: -16, zIndex: 10 },
  }[stackIndex] ?? { scale: 0.85, y: -24, zIndex: 5 }

  async function handleDragEnd(_: unknown, info: { offset: { x: number; y: number }; velocity: { x: number; y: number } }) {
    const swipeX = Math.abs(info.offset.x)
    const swipeY = info.offset.y
    const velX   = Math.abs(info.velocity.x)

    // Super like: fast up drag
    if (swipeY < -80 && info.offset.y < -60) {
      await controls.start({ y: -800, opacity: 0, transition: { duration: 0.4 } })
      onSuperLike()
      return
    }

    const threshold = 120
    if (swipeX > threshold || velX > 600) {
      const dir = info.offset.x > 0 ? 1 : -1
      await controls.start({
        x: dir * 800, rotate: dir * 20, opacity: 0,
        transition: { duration: 0.35, ease: [0.7, 0, 0.84, 0] },
      })
      if (dir > 0) onLike(); else onNope()
    } else {
      // Spring back to center
      controls.start({
        x: 0, y: 0, rotate: 0,
        transition: { type: 'spring', stiffness: 300, damping: 30 },
      })
    }
    setIsDragging(false)
  }

  const cardContent = (
    <div style={{ position: 'relative', width: '100%', height: '100%', borderRadius: 20, overflow: 'hidden' }}>
      {/* Photo */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <img
          src={profile.photos[photoIndex]}
          alt={profile.name}
          style={{ width: '100%', height: '100%', objectFit: 'cover', pointerEvents: 'none' }}
          draggable={false}
        />
      </div>

      {/* Photo dots + tap zones */}
      {profile.photos.length > 1 && (
        <>
          {/* Left/right tap zones for photo navigation */}
          <div
            style={{ position: 'absolute', left: 0, top: 0, width: '40%', height: '70%', zIndex: 5 }}
            onClick={e => { e.stopPropagation(); setPhotoIndex(i => Math.max(0, i - 1)) }}
          />
          <div
            style={{ position: 'absolute', right: 0, top: 0, width: '40%', height: '70%', zIndex: 5 }}
            onClick={e => { e.stopPropagation(); setPhotoIndex(i => Math.min(profile.photos.length - 1, i + 1)) }}
          />

          {/* Photo progress dots */}
          <div style={{
            position: 'absolute', top: 12, left: 12, right: 12,
            display: 'flex', gap: 4, zIndex: 10,
          }}>
            {profile.photos.map((_, i) => (
              <div key={i} style={{
                flex: 1, height: 3, borderRadius: 2,
                background: i === photoIndex ? '#fff' : 'rgba(255,255,255,0.45)',
                transition: 'background 200ms',
              }} />
            ))}
          </div>
        </>
      )}

      {/* Gradient overlay */}
      <div className="card-gradient" style={{ position: 'absolute', inset: 0, zIndex: 2 }} />

      {/* Stamp: LIKE */}
      <motion.div className="stamp stamp-like" style={{ opacity: likeOpacity }}>
        LIKE
      </motion.div>

      {/* Stamp: NOPE */}
      <motion.div className="stamp stamp-nope" style={{ opacity: nopeOpacity }}>
        NOPE
      </motion.div>

      {/* Stamp: SUPER */}
      <motion.div className="stamp stamp-super" style={{ opacity: superOpacity }}>
        SUPER
      </motion.div>

      {/* Profile info */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        padding: '24px 20px 20px', zIndex: 5,
      }}
        onClick={onExpand}
      >
        {/* Name + age + verified */}
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 10, marginBottom: 6 }}>
          <span style={{ fontSize: 28, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
            {profile.name}, {profile.age}
          </span>
          {profile.verified >= 2 && (
            <div style={{
              display: 'flex', alignItems: 'center', gap: 4,
              padding: '3px 8px', background: 'rgba(0,136,255,0.9)',
              borderRadius: 9999, marginBottom: 2,
            }}>
              <span style={{ fontSize: 10, fontWeight: 700, color: '#fff' }}>
                ✓ {VERIFIED_LABELS[profile.verified]}
              </span>
            </div>
          )}
        </div>

        {/* Location + destination */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 10 }}>
          <span style={{ fontSize: 14, color: 'rgba(255,255,255,0.85)', fontWeight: 500 }}>
            📍 {profile.location}
          </span>
          {profile.distance && (
            <span style={{ fontSize: 13, color: 'rgba(255,255,255,0.6)' }}>· {profile.distance}</span>
          )}
        </div>

        {/* Destinations */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 10 }}>
          {profile.destinations.slice(0, 3).map(d => (
            <div key={d} style={{
              padding: '4px 10px', background: 'rgba(255,255,255,0.15)',
              backdropFilter: 'blur(8px)', borderRadius: 9999,
              border: '1px solid rgba(255,255,255,0.3)',
              fontSize: 12, fontWeight: 700, color: '#fff',
            }}>
              ✈ {d}
            </div>
          ))}
          <div style={{
            padding: '4px 10px', background: 'rgba(255,255,255,0.15)',
            backdropFilter: 'blur(8px)', borderRadius: 9999,
            border: '1px solid rgba(255,255,255,0.3)',
            fontSize: 12, fontWeight: 600, color: 'rgba(255,255,255,0.8)',
          }}>
            {profile.travelDates}
          </div>
        </div>

        {/* Interests */}
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {profile.interests.slice(0, 3).map(i => (
            <span key={i} style={{
              fontSize: 12, color: 'rgba(255,255,255,0.75)', fontWeight: 500,
              background: 'rgba(255,255,255,0.1)', padding: '3px 8px', borderRadius: 6,
            }}>
              {i}
            </span>
          ))}
        </div>

        {/* Reputation */}
        {profile.reputationScore && (
          <div style={{ marginTop: 8, display: 'flex', alignItems: 'center', gap: 4 }}>
            <span style={{ fontSize: 13, color: '#FFD700' }}>★</span>
            <span style={{ fontSize: 13, fontWeight: 700, color: '#fff' }}>{profile.reputationScore}</span>
            <span style={{ fontSize: 12, color: 'rgba(255,255,255,0.55)' }}>({profile.reputationCount} trips)</span>
          </div>
        )}
      </div>
    </div>
  )

  if (!isTop) {
    return (
      <motion.div
        style={{
          position: 'absolute', inset: 0,
          scale: stackStyles.scale,
          y: stackStyles.y,
          zIndex: stackStyles.zIndex,
          borderRadius: 20,
          pointerEvents: 'none',
        }}
      >
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
        touchAction: 'none',
        userSelect: 'none',
      }}
      whileTap={{ cursor: 'grabbing' }}
    >
      {cardContent}
    </motion.div>
  )
}
