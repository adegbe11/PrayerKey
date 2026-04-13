'use client'

import { motion } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'
import { useColor } from './ColorProvider'

const STYLE_LABELS: Record<string, string> = {
  adventure: '✈️ Adventure', beach: '🌊 Beach & Chill',
  culture: '🏛 Culture', content: '📸 Content Creator',
  party: '🎉 Party', luxury: '💼 Luxury',
  budget: '🎒 Budget', wellness: '🧘 Wellness',
}

interface ProfileExpandProps {
  profile: TravelerProfile
  onClose: () => void
  onLike: () => void
  onNope: () => void
}

export function ProfileExpand({ profile, onClose, onLike, onNope }: ProfileExpandProps) {
  const { color } = useColor()

  return (
    <motion.div
      initial={{ y: '100%' }}
      animate={{ y: 0 }}
      exit={{ y: '100%' }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
      style={{
        position: 'absolute', inset: 0, zIndex: 150,
        background: '#fff', overflowY: 'auto',
      }}
      className="no-scrollbar"
    >
      {/* Hero photo */}
      <div style={{ position: 'relative', height: 500 }}>
        <img
          src={profile.photos[0]}
          alt={profile.name}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
        <div className="card-gradient" style={{ position: 'absolute', inset: 0 }} />

        {/* Back button */}
        <button
          onClick={onClose}
          style={{
            position: 'absolute', top: 20, left: 20,
            width: 40, height: 40, borderRadius: '50%',
            background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(8px)',
            border: 'none', cursor: 'pointer', color: '#fff', fontSize: 18,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
        >
          ←
        </button>

        {/* Name overlay */}
        <div style={{ position: 'absolute', bottom: 24, left: 24, right: 24 }}>
          <div style={{ fontSize: 32, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
            {profile.name}, {profile.age}
          </div>
          <div style={{ fontSize: 15, color: 'rgba(255,255,255,0.8)', marginTop: 4 }}>
            📍 {profile.location} · {profile.distance ?? 'Worldwide'}
          </div>
        </div>
      </div>

      {/* Photo grid */}
      {profile.photos.length > 1 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 2, margin: '2px 0' }}>
          {profile.photos.slice(1).map((p, i) => (
            <div key={i} style={{ aspectRatio: '1', overflow: 'hidden' }}>
              <img src={p} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          ))}
        </div>
      )}

      {/* Content */}
      <div style={{ padding: '24px 24px 120px' }}>

        {/* Destinations */}
        <div style={{ marginBottom: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>
            Going to
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.destinations.map(d => (
              <div key={d} style={{
                padding: '8px 14px', background: `${color}10`,
                border: `2px solid ${color}30`, borderRadius: 9999,
                fontSize: 13, fontWeight: 700, color,
                transition: 'all 280ms',
              }}>
                ✈ {d}
              </div>
            ))}
          </div>
          <div style={{ marginTop: 8, fontSize: 14, color: '#737373', fontWeight: 500 }}>
            🗓 {profile.travelDates}
          </div>
        </div>

        {/* Bio */}
        <div style={{ marginBottom: 24, padding: '16px', background: '#f5f5f5', borderRadius: 14 }}>
          <div style={{ fontSize: 15, color: '#3b3b3b', lineHeight: 1.6 }}>{profile.bio}</div>
        </div>

        {/* Travel style */}
        <div style={{ marginBottom: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>
            Travel Style
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.travelStyle.map(s => (
              <span key={s} style={{
                padding: '7px 14px', background: '#f5f5f5',
                borderRadius: 9999, fontSize: 13, fontWeight: 600, color: '#3b3b3b',
              }}>
                {STYLE_LABELS[s] ?? s}
              </span>
            ))}
          </div>
        </div>

        {/* Interests */}
        <div style={{ marginBottom: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>
            Interests
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.interests.map(i => (
              <span key={i} style={{
                padding: '7px 14px', background: `${color}10`,
                border: `1.5px solid ${color}30`, borderRadius: 9999,
                fontSize: 13, fontWeight: 600, color,
                transition: 'all 280ms',
              }}>
                {i}
              </span>
            ))}
          </div>
        </div>

        {/* Details */}
        <div style={{ marginBottom: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>
            About
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            {[
              { label: 'Languages', value: profile.languages.join(', ') },
              { label: 'Budget', value: { budget: 'Budget', midrange: 'Mid-range', comfortable: 'Comfortable', luxury: 'Luxury' }[profile.budget] },
              ...(profile.height ? [{ label: 'Height', value: profile.height }] : []),
              ...(profile.education ? [{ label: 'Education', value: profile.education }] : []),
              ...(profile.job ? [{ label: 'Job', value: `${profile.job}${profile.company ? ` at ${profile.company}` : ''}` }] : []),
            ].map(detail => (
              <div key={detail.label} style={{ padding: '12px', background: '#f5f5f5', borderRadius: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 3 }}>{detail.label}</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: '#262626' }}>{detail.value}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Anthem */}
        {profile.anthem && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 14,
            padding: '14px 16px', background: '#f5f5f5', borderRadius: 14, marginBottom: 24,
          }}>
            <div style={{
              width: 44, height: 44, borderRadius: 10,
              background: 'linear-gradient(135deg, #1DB954, #169C45)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 20, flexShrink: 0,
            }}>🎵</div>
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#262626' }}>{profile.anthem.title}</div>
              <div style={{ fontSize: 12, color: '#999' }}>{profile.anthem.artist}</div>
            </div>
          </div>
        )}

        {/* Reputation */}
        {profile.reputationScore && (
          <div style={{ marginBottom: 24, padding: '16px', background: '#f5f5f5', borderRadius: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ fontSize: 20, color: '#FFD700' }}>★</span>
              <span style={{ fontSize: 22, fontWeight: 800, color: '#141414' }}>{profile.reputationScore}</span>
              <span style={{ fontSize: 14, color: '#999' }}>/ 5 · {profile.reputationCount} trip reviews</span>
            </div>
          </div>
        )}
      </div>

      {/* Sticky action buttons */}
      <div style={{
        position: 'sticky', bottom: 0, left: 0, right: 0,
        padding: '16px 24px 24px',
        background: 'linear-gradient(to top, #fff 80%, transparent)',
        display: 'flex', gap: 16,
      }}>
        <button
          onClick={() => { onNope(); onClose() }}
          style={{
            flex: 1, padding: '18px', background: '#fff', color: '#FF4444',
            fontSize: 28, fontWeight: 700, borderRadius: 9999,
            border: '2px solid #ebebeb', cursor: 'pointer',
            boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
          }}
        >
          ✕
        </button>
        <button
          onClick={() => { onLike(); onClose() }}
          style={{
            flex: 2, padding: '18px', background: color, color: '#fff',
            fontSize: 17, fontWeight: 700, borderRadius: 9999,
            border: 'none', cursor: 'pointer', transition: 'background 280ms',
            boxShadow: `0 8px 24px ${color}44`,
          }}
        >
          ♥ Like
        </button>
      </div>
    </motion.div>
  )
}
