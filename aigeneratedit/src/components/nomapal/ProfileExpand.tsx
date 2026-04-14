'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import type { TravelerProfile } from '@/lib/types'
import { useColor } from './ColorProvider'

const STYLE_LABELS: Record<string, string> = {
  adventure: '✈️ Adventure', beach: '🌊 Beach & Chill',
  culture: '🏛 Culture', content: '📸 Content Creator',
  party: '🎉 Party', luxury: '💼 Luxury',
  budget: '🎒 Budget', wellness: '🧘 Wellness',
}

const BUDGET_LABELS: Record<string, string> = {
  budget: '🎒 Budget', midrange: '💳 Mid-range',
  comfortable: '💰 Comfortable', luxury: '✨ Luxury',
}

const GOAL_LABELS: Record<string, string> = {
  friends: '👯 Travel Friends', group: '🧳 Group Trip', open: '🌍 Open to anything',
}

const VERIFIED_LABELS = ['', 'Email', 'Phone', 'ID Verified', 'Verified Nomad']

interface ProfileExpandProps {
  profile: TravelerProfile
  onClose: () => void
  onLike: () => void
  onNope: () => void
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      fontSize: 11, fontWeight: 800, color: 'rgba(255,255,255,0.38)',
      textTransform: 'uppercase', letterSpacing: '0.10em', marginBottom: 12,
    }}>
      {children}
    </div>
  )
}

function InfoCard({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return (
    <div style={{
      background: 'rgba(255,255,255,0.06)',
      border: '1px solid rgba(255,255,255,0.10)',
      borderRadius: 14, padding: '14px 16px',
      ...style,
    }}>
      {children}
    </div>
  )
}

export function ProfileExpand({ profile, onClose, onLike, onNope }: ProfileExpandProps) {
  const { color } = useColor()
  const [photoIndex, setPhotoIndex] = useState(0)

  const lifestyleTags = [
    profile.pets, profile.drinking, profile.smoking,
    profile.workout, profile.diet,
  ].filter(Boolean) as string[]

  const personalStats = [
    profile.height    && { icon: '📏', label: 'Height',       value: profile.height },
    profile.pronouns  && { icon: '🏳️', label: 'Pronouns',     value: profile.pronouns },
    profile.zodiac    && { icon: '✨', label: 'Star Sign',     value: profile.zodiac },
    profile.familyPlans && { icon: '👶', label: 'Family Plans', value: profile.familyPlans },
    profile.loveLanguage && { icon: '💬', label: 'Love Language', value: profile.loveLanguage },
    profile.communicationStyle && { icon: '🤝', label: 'Communication', value: profile.communicationStyle },
    profile.relationshipGoal && { icon: '🎯', label: 'Looking for', value: profile.relationshipGoal },
  ].filter(Boolean) as { icon: string; label: string; value: string }[]

  return (
    <motion.div
      initial={{ y: '100%' }}
      animate={{ y: 0 }}
      exit={{ y: '100%' }}
      transition={{ type: 'spring', stiffness: 280, damping: 32 }}
      style={{
        position: 'absolute', inset: 0, zIndex: 150,
        background: 'linear-gradient(180deg, #0D0A1A 0%, #0A0818 100%)',
        overflowY: 'auto',
      }}
      className="no-scrollbar"
    >

      {/* ── Photo carousel ── */}
      <div style={{ position: 'relative', height: 520, flexShrink: 0 }}>
        <AnimatePresence mode="wait">
          <motion.img
            key={photoIndex}
            src={profile.photos[photoIndex]}
            alt={profile.name}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.22 }}
            style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </AnimatePresence>

        {/* Photo tap zones */}
        <div style={{ position: 'absolute', left: 0, top: 0, width: '40%', height: '80%', zIndex: 5 }}
          onClick={() => setPhotoIndex(i => Math.max(0, i - 1))} />
        <div style={{ position: 'absolute', right: 0, top: 0, width: '40%', height: '80%', zIndex: 5 }}
          onClick={() => setPhotoIndex(i => Math.min(profile.photos.length - 1, i + 1))} />

        {/* Photo progress dots */}
        {profile.photos.length > 1 && (
          <div style={{
            position: 'absolute', top: 14, left: 0, right: 0,
            display: 'flex', justifyContent: 'center', gap: 5, zIndex: 10,
          }}>
            {profile.photos.map((_, i) => (
              <div key={i} onClick={() => setPhotoIndex(i)} style={{
                width: i === photoIndex ? 22 : 6, height: 6, borderRadius: 3,
                background: i === photoIndex ? '#fff' : 'rgba(255,255,255,0.35)',
                transition: 'all 260ms cubic-bezier(0.34,1.56,0.64,1)',
                cursor: 'pointer', zIndex: 10,
              }} />
            ))}
          </div>
        )}

        {/* Dark gradient over photo */}
        <div style={{
          position: 'absolute', inset: 0, zIndex: 2,
          background: 'linear-gradient(to top, rgba(13,10,26,1) 0%, rgba(13,10,26,0.55) 45%, rgba(0,0,0,0.15) 100%)',
        }} />

        {/* Back button */}
        <button onClick={onClose} style={{
          position: 'absolute', top: 52, left: 16, zIndex: 20,
          width: 40, height: 40, borderRadius: '50%',
          background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(12px)',
          border: '1px solid rgba(255,255,255,0.2)',
          cursor: 'pointer', color: '#fff', fontSize: 20,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          ←
        </button>

        {/* Name / age overlay */}
        <div style={{ position: 'absolute', bottom: 24, left: 20, right: 20, zIndex: 5 }}>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 10, flexWrap: 'wrap', marginBottom: 6 }}>
            <span style={{ fontSize: 34, fontWeight: 900, color: '#fff', letterSpacing: '-0.03em', lineHeight: 1 }}>
              {profile.name}, {profile.age}
            </span>
            {profile.verified >= 2 && (
              <div style={{
                display: 'flex', alignItems: 'center', gap: 4,
                padding: '4px 10px', background: 'rgba(0,136,255,0.85)',
                borderRadius: 9999, marginBottom: 3,
              }}>
                <span style={{ fontSize: 11, fontWeight: 700, color: '#fff' }}>✓ {VERIFIED_LABELS[profile.verified]}</span>
              </div>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            <span style={{ fontSize: 14, color: 'rgba(255,255,255,0.75)', fontWeight: 500 }}>
              📍 {profile.location}
            </span>
            {profile.distance && (
              <span style={{ fontSize: 13, color: 'rgba(255,255,255,0.45)' }}>· {profile.distance} away</span>
            )}
          </div>

          {profile.job && (
            <div style={{ marginTop: 5, fontSize: 14, color: 'rgba(255,255,255,0.65)', fontWeight: 500 }}>
              💼 {profile.job}{profile.company ? ` · ${profile.company}` : ''}
            </div>
          )}
          {profile.education && (
            <div style={{ marginTop: 3, fontSize: 14, color: 'rgba(255,255,255,0.55)', fontWeight: 500 }}>
              🎓 {profile.education}
            </div>
          )}
        </div>
      </div>

      {/* ── Content ── */}
      <div style={{ padding: '8px 20px 160px' }}>

        {/* Reputation */}
        {profile.reputationScore && (
          <InfoCard style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
            <span style={{ fontSize: 28, color: '#FFD700', lineHeight: 1 }}>★</span>
            <div>
              <div style={{ fontSize: 22, fontWeight: 900, color: '#fff', lineHeight: 1 }}>
                {profile.reputationScore}
                <span style={{ fontSize: 13, fontWeight: 500, color: 'rgba(255,255,255,0.45)', marginLeft: 6 }}>/ 5</span>
              </div>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.45)', marginTop: 2 }}>
                {profile.reputationCount} trip reviews
              </div>
            </div>
          </InfoCard>
        )}

        {/* Bio */}
        <InfoCard style={{ marginBottom: 20 }}>
          <div style={{ fontSize: 15, color: 'rgba(255,255,255,0.80)', lineHeight: 1.65, fontStyle: 'italic' }}>
            "{profile.bio}"
          </div>
        </InfoCard>

        {/* Prompts */}
        {profile.prompts && profile.prompts.length > 0 && (
          <div style={{ marginBottom: 20 }}>
            {profile.prompts.map((p, i) => (
              <InfoCard key={i} style={{ marginBottom: 10 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color, textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 8 }}>
                  {p.question}
                </div>
                <div style={{ fontSize: 15, color: '#fff', fontWeight: 600, lineHeight: 1.5 }}>
                  {p.answer}
                </div>
              </InfoCard>
            ))}
          </div>
        )}

        {/* Going to */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel>Going to</SectionLabel>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.destinations.map(d => (
              <div key={d} style={{
                padding: '8px 14px',
                background: `${color}18`,
                border: `1.5px solid ${color}50`,
                borderRadius: 9999,
                fontSize: 13, fontWeight: 700, color,
              }}>
                ✈ {d}
              </div>
            ))}
          </div>
          <div style={{ marginTop: 10, fontSize: 13, color: 'rgba(255,255,255,0.45)', fontWeight: 500 }}>
            🗓 {profile.travelDates}
            {profile.departureDaysFromNow !== undefined && (
              <span style={{
                marginLeft: 10,
                padding: '2px 8px', borderRadius: 9999,
                background: profile.departureDaysFromNow <= 7
                  ? 'rgba(255,68,68,0.75)' : 'rgba(255,255,255,0.12)',
                color: '#fff', fontSize: 11, fontWeight: 700,
              }}>
                {profile.departureDaysFromNow <= 1 ? 'Leaves tomorrow!' : `Leaves in ${profile.departureDaysFromNow}d`}
              </span>
            )}
          </div>
        </div>

        {/* Travel goal */}
        {profile.travelGoal && (
          <div style={{ marginBottom: 20 }}>
            <SectionLabel>Looking for</SectionLabel>
            <InfoCard style={{ display: 'inline-flex', padding: '10px 16px' }}>
              <span style={{ fontSize: 14, fontWeight: 700, color: '#fff' }}>
                {GOAL_LABELS[profile.travelGoal] ?? profile.travelGoal}
              </span>
            </InfoCard>
          </div>
        )}

        {/* Travel style */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel>Travel Style</SectionLabel>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.travelStyle.map(s => (
              <span key={s} style={{
                padding: '8px 14px',
                background: 'rgba(255,255,255,0.08)',
                border: '1px solid rgba(255,255,255,0.15)',
                borderRadius: 9999, fontSize: 13, fontWeight: 600, color: '#fff',
              }}>
                {STYLE_LABELS[s] ?? s}
              </span>
            ))}
            <span style={{
              padding: '8px 14px',
              background: 'rgba(255,255,255,0.08)',
              border: '1px solid rgba(255,255,255,0.15)',
              borderRadius: 9999, fontSize: 13, fontWeight: 600, color: '#fff',
            }}>
              {BUDGET_LABELS[profile.budget] ?? profile.budget}
            </span>
          </div>
        </div>

        {/* Interests */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel>Interests & Passions</SectionLabel>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {profile.interests.map(i => (
              <span key={i} style={{
                padding: '8px 14px',
                background: `${color}18`,
                border: `1.5px solid ${color}40`,
                borderRadius: 9999, fontSize: 13, fontWeight: 600, color,
              }}>
                {i}
              </span>
            ))}
          </div>
        </div>

        {/* Lifestyle */}
        {lifestyleTags.length > 0 && (
          <div style={{ marginBottom: 20 }}>
            <SectionLabel>Lifestyle</SectionLabel>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              {lifestyleTags.map(tag => (
                <span key={tag} style={{
                  padding: '8px 14px',
                  background: 'rgba(255,255,255,0.07)',
                  border: '1px solid rgba(255,255,255,0.13)',
                  borderRadius: 9999, fontSize: 13, fontWeight: 500, color: 'rgba(255,255,255,0.80)',
                }}>
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Personal stats grid */}
        {personalStats.length > 0 && (
          <div style={{ marginBottom: 20 }}>
            <SectionLabel>About me</SectionLabel>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
              {personalStats.map(stat => (
                <InfoCard key={stat.label}>
                  <div style={{ fontSize: 10, fontWeight: 700, color: 'rgba(255,255,255,0.35)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>
                    {stat.icon} {stat.label}
                  </div>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#fff' }}>{stat.value}</div>
                </InfoCard>
              ))}
              {/* Languages */}
              <InfoCard style={{ gridColumn: profile.languages.length > 2 ? '1 / -1' : undefined }}>
                <div style={{ fontSize: 10, fontWeight: 700, color: 'rgba(255,255,255,0.35)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>
                  🗣 Languages
                </div>
                <div style={{ fontSize: 14, fontWeight: 700, color: '#fff' }}>{profile.languages.join(' · ')}</div>
              </InfoCard>
            </div>
          </div>
        )}

        {/* Anthem */}
        {profile.anthem && (
          <div style={{ marginBottom: 20 }}>
            <SectionLabel>My Anthem</SectionLabel>
            <InfoCard style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
              <div style={{
                width: 48, height: 48, borderRadius: 10, flexShrink: 0,
                background: 'linear-gradient(135deg, #1DB954, #11823B)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22,
              }}>🎵</div>
              <div>
                <div style={{ fontSize: 15, fontWeight: 700, color: '#fff' }}>{profile.anthem.title}</div>
                <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.5)', marginTop: 2 }}>{profile.anthem.artist}</div>
              </div>
            </InfoCard>
          </div>
        )}

        {/* Divider */}
        <div style={{ height: 1, background: 'rgba(255,255,255,0.07)', margin: '24px 0' }} />

        {/* Share / Block / Report */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <button style={{
            width: '100%', padding: '14px', borderRadius: 14,
            background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.12)',
            color: 'rgba(255,255,255,0.75)', fontSize: 14, fontWeight: 600, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          }}>
            <span>↗</span> Share Profile
          </button>
          <button style={{
            width: '100%', padding: '14px', borderRadius: 14,
            background: 'rgba(255,68,68,0.08)', border: '1px solid rgba(255,68,68,0.20)',
            color: 'rgba(255,100,100,0.85)', fontSize: 14, fontWeight: 600, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          }}>
            🚫 Block {profile.name}
          </button>
          <button style={{
            width: '100%', padding: '14px', borderRadius: 14,
            background: 'rgba(255,100,0,0.07)', border: '1px solid rgba(255,100,0,0.18)',
            color: 'rgba(255,140,60,0.85)', fontSize: 14, fontWeight: 600, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          }}>
            ⚠️ Report {profile.name}
          </button>
        </div>
      </div>

      {/* ── Sticky action bar ── */}
      <div style={{
        position: 'sticky', bottom: 0, left: 0, right: 0,
        padding: '14px 20px 32px',
        background: 'linear-gradient(to top, rgba(10,8,24,1) 70%, transparent)',
        display: 'flex', gap: 12,
      }}>
        <button
          onClick={() => { onNope(); onClose() }}
          style={{
            width: 64, height: 64, borderRadius: '50%', flexShrink: 0,
            background: 'rgba(255,255,255,0.08)',
            border: '2px solid rgba(255,90,90,0.55)',
            color: '#FF5A5F', fontSize: 24, fontWeight: 700,
            cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
        >
          ✕
        </button>
        <button
          onClick={() => { onLike(); onClose() }}
          style={{
            flex: 1, height: 64, borderRadius: 32,
            background: color, color: '#fff',
            fontSize: 17, fontWeight: 800, border: 'none', cursor: 'pointer',
            boxShadow: `0 8px 32px ${color}55`,
            transition: 'background 280ms, box-shadow 280ms',
            letterSpacing: '0.02em',
          }}
        >
          ♥ Wander Together
        </button>
      </div>
    </motion.div>
  )
}
