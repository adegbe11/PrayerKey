'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { useRouter } from 'next/navigation'

const MOCK_USER = {
  name: 'You',
  age: 27,
  photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80',
  location: 'Barcelona, Spain',
  destinations: ['Bali', 'Tokyo'],
  travelDates: 'Aug – Sep 2026',
  bio: 'Ask me about my best trip to Morocco.',
  interests: ['Photography', 'Hiking', 'Foodie', 'Art', 'Languages'],
  verified: 2,
  reputationScore: 4.8,
  reputationCount: 3,
  job: 'Product Designer',
  education: 'Universitat Pompeu Fabra',
  languages: ['English', 'Spanish'],
}

type Section = 'main' | 'edit' | 'settings' | 'upgrade' | 'safety'

export default function ProfilePage() {
  const { color, advance } = useColor()
  const router = useRouter()
  const [section, setSection] = useState<Section>('main')
  const [isPremium, setIsPremium] = useState(false)

  function nav(s: Section) { setSection(s); advance() }

  if (section === 'edit') return <EditProfile onBack={() => nav('main')} color={color} />
  if (section === 'settings') return <SettingsScreen onBack={() => nav('main')} color={color} />
  if (section === 'upgrade') return <UpgradeScreen onBack={() => nav('main')} color={color} onUpgrade={() => { setIsPremium(true); nav('main') }} />
  if (section === 'safety') return <SafetyScreen onBack={() => nav('main')} color={color} />

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#f5f5f5', overflowY: 'auto' }} className="no-scrollbar">
      {/* Profile card preview */}
      <div style={{ position: 'relative', height: 380 }}>
        <img src={MOCK_USER.photo} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div className="card-gradient" style={{ position: 'absolute', inset: 0 }} />

        {/* Edit button */}
        <button
          onClick={() => nav('edit')}
          style={{
            position: 'absolute', top: 20, right: 20,
            padding: '8px 16px', background: 'rgba(255,255,255,0.9)',
            backdropFilter: 'blur(8px)', borderRadius: 9999,
            border: 'none', cursor: 'pointer',
            fontSize: 13, fontWeight: 700, color: '#141414',
          }}
        >
          Edit Profile
        </button>

        {/* Profile info */}
        <div style={{ position: 'absolute', bottom: 20, left: 20, right: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
            <span style={{ fontSize: 28, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
              {MOCK_USER.name}, {MOCK_USER.age}
            </span>
            {MOCK_USER.verified >= 2 && (
              <div style={{
                padding: '3px 8px', background: 'rgba(0,136,255,0.9)',
                borderRadius: 9999, fontSize: 10, fontWeight: 700, color: '#fff',
              }}>
                ✓ Phone Verified
              </div>
            )}
          </div>
          <div style={{ fontSize: 14, color: 'rgba(255,255,255,0.8)' }}>📍 {MOCK_USER.location}</div>
        </div>
      </div>

      {/* Profile completion */}
      <div style={{ margin: '16px 20px', padding: '16px', background: '#fff', borderRadius: 16, boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
          <span style={{ fontSize: 14, fontWeight: 700, color: '#3b3b3b' }}>Profile Strength</span>
          <span style={{ fontSize: 14, fontWeight: 800, color }}>72%</span>
        </div>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: '72%' }} />
        </div>
        <div style={{ marginTop: 8, fontSize: 13, color: '#999' }}>Add ID verification to unlock Verified Nomad badge</div>
      </div>

      {/* Nomapal+ upgrade banner */}
      {!isPremium && (
        <button
          onClick={() => nav('upgrade')}
          style={{
            display: 'block', width: 'calc(100% - 40px)', margin: '0 20px 16px',
            padding: '16px 20px', borderRadius: 16,
            background: `linear-gradient(135deg, ${color} 0%, #9933CC 100%)`,
            border: 'none', cursor: 'pointer', textAlign: 'left',
            transition: 'background 280ms',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: 16, fontWeight: 800, color: '#fff', marginBottom: 2 }}>✦ Upgrade to Nomapal+</div>
              <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.75)' }}>See who liked you · Global mode · Rewind</div>
            </div>
            <span style={{ fontSize: 24, color: '#fff' }}>→</span>
          </div>
        </button>
      )}

      {/* Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, margin: '0 20px 16px' }}>
        {[
          { label: 'Likes',     value: '23',  emoji: '♥' },
          { label: 'Matches',   value: '8',   emoji: '✈' },
          { label: 'Rep Score', value: '4.8', emoji: '★' },
        ].map(stat => (
          <div key={stat.label} style={{
            background: '#fff', borderRadius: 14, padding: '14px 12px',
            textAlign: 'center', boxShadow: '0 2px 10px rgba(0,0,0,0.05)',
          }}>
            <div style={{ fontSize: 20, marginBottom: 2, color }}>{stat.emoji}</div>
            <div style={{ fontSize: 22, fontWeight: 800, color: '#141414', letterSpacing: '-0.02em' }}>{stat.value}</div>
            <div style={{ fontSize: 12, color: '#999', fontWeight: 500 }}>{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Menu items */}
      <div style={{ margin: '0 20px 32px', background: '#fff', borderRadius: 16, overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
        {[
          { icon: '⚙️', label: 'Settings & Privacy', action: () => nav('settings') },
          { icon: '🛡', label: 'Safety Center',        action: () => nav('safety') },
          { icon: '✦',  label: 'Nomapal+',             action: () => nav('upgrade'), highlight: true },
          { icon: '🔔', label: 'Notifications',        action: () => {} },
          { icon: '❓', label: 'Help & Feedback',      action: () => {} },
          { icon: '🚪', label: 'Log Out',               action: () => router.push('/onboarding'), danger: true },
        ].map((item, i, arr) => (
          <button
            key={item.label}
            onClick={item.action}
            style={{
              width: '100%', padding: '16px 20px',
              background: 'transparent', border: 'none',
              borderBottom: i < arr.length - 1 ? '1px solid #f5f5f5' : 'none',
              cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 14,
              textAlign: 'left',
            }}
          >
            <span style={{ fontSize: 20, width: 28, textAlign: 'center' }}>{item.icon}</span>
            <span style={{
              flex: 1, fontSize: 15, fontWeight: 600,
              color: (item as any).danger ? '#FF4444' : (item as any).highlight ? color : '#3b3b3b',
              transition: 'color 280ms',
            }}>
              {item.label}
            </span>
            {!(item as any).danger && (
              <span style={{ fontSize: 16, color: '#d6d6d6' }}>›</span>
            )}
          </button>
        ))}
      </div>
    </div>
  )
}

function EditProfile({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', overflowY: 'auto' }} className="no-scrollbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '16px 20px', borderBottom: '1px solid #ebebeb' }}>
        <button onClick={onBack} style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 16 }}>←</button>
        <span style={{ fontSize: 18, fontWeight: 700 }}>Edit Profile</span>
      </div>
      <div style={{ padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: 20 }}>
        {/* Photo grid */}
        <div>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>Photos</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
            <div style={{ aspectRatio: '3/4', borderRadius: 12, overflow: 'hidden', position: 'relative' }}>
              <img src={MOCK_USER.photo} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              <div style={{ position: 'absolute', top: 6, right: 6, width: 24, height: 24, borderRadius: '50%', background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, cursor: 'pointer' }}>✕</div>
            </div>
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} style={{ aspectRatio: '3/4', borderRadius: 12, border: '2px dashed #d6d6d6', background: '#f5f5f5', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22, color: '#d6d6d6', cursor: 'pointer' }}>+</div>
            ))}
          </div>
        </div>

        {/* Bio */}
        <div>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 }}>Bio</div>
          <textarea
            defaultValue={MOCK_USER.bio}
            rows={4}
            style={{ width: '100%', padding: '16px', background: '#f5f5f5', borderRadius: 12, border: `2px solid ${color}`, fontSize: 15, resize: 'none', outline: 'none', color: '#141414', transition: 'border-color 280ms', lineHeight: 1.6 }}
          />
        </div>

        {/* Destinations */}
        <div>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 }}>Destinations</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {MOCK_USER.destinations.map(d => (
              <div key={d} style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '8px 14px', background: `${color}12`, border: `2px solid ${color}30`, borderRadius: 9999, transition: 'all 280ms' }}>
                <span style={{ fontSize: 13, fontWeight: 700, color }}>✈ {d}</span>
                <button style={{ background: 'none', border: 'none', cursor: 'pointer', color, fontSize: 16, lineHeight: 1 }}>×</button>
              </div>
            ))}
            <button style={{ padding: '8px 14px', borderRadius: 9999, border: '2px dashed #d6d6d6', background: '#fff', fontSize: 13, fontWeight: 700, color: '#999', cursor: 'pointer' }}>+ Add</button>
          </div>
        </div>

        <button style={{ padding: '18px', background: color, color: '#fff', fontSize: 16, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer', transition: 'background 280ms' }}>
          Save Changes
        </button>
      </div>
    </div>
  )
}

function SettingsScreen({ onBack, color }: { onBack: () => void; color: string }) {
  const [radius, setRadius] = useState(50)
  const [ageMin, setAgeMin] = useState(22)
  const [ageMax, setAgeMax] = useState(38)

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', overflowY: 'auto' }} className="no-scrollbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '16px 20px', borderBottom: '1px solid #ebebeb' }}>
        <button onClick={onBack} style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 16 }}>←</button>
        <span style={{ fontSize: 18, fontWeight: 700 }}>Settings & Discovery</span>
      </div>

      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* Distance */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ fontSize: 15, fontWeight: 700, color: '#141414' }}>Match Radius</span>
            <span style={{ fontSize: 15, fontWeight: 800, color }}>{radius} km</span>
          </div>
          <input type="range" min="1" max="100" value={radius} onChange={e => setRadius(+e.target.value)} style={{ width: '100%', accentColor: color }} />
        </div>

        {/* Age range */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ fontSize: 15, fontWeight: 700, color: '#141414' }}>Age Range</span>
            <span style={{ fontSize: 15, fontWeight: 800, color }}>{ageMin}–{ageMax}</span>
          </div>
          <input type="range" min="18" max={ageMax} value={ageMin} onChange={e => setAgeMin(+e.target.value)} style={{ width: '100%', accentColor: color }} />
          <input type="range" min={ageMin} max="65" value={ageMax} onChange={e => setAgeMax(+e.target.value)} style={{ width: '100%', accentColor: color, marginTop: 8 }} />
        </div>

        {/* Toggles */}
        {[
          { label: 'Hide my profile', sub: 'Stop appearing in discovery' },
          { label: 'Show on distance', sub: 'Show my distance to others' },
          { label: 'Show age', sub: 'Show my age on profile' },
          { label: 'Global mode', sub: 'See travelers worldwide', premium: true },
        ].map(item => (
          <div key={item.label} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: 16, borderBottom: '1px solid #f5f5f5' }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 600, color: '#141414' }}>
                {item.label}
                {(item as any).premium && (
                  <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, color, padding: '2px 6px', background: `${color}12`, borderRadius: 4, transition: 'all 280ms' }}>+</span>
                )}
              </div>
              <div style={{ fontSize: 13, color: '#999' }}>{item.sub}</div>
            </div>
            <div style={{ width: 48, height: 28, borderRadius: 14, background: '#d6d6d6', position: 'relative', cursor: 'pointer' }}>
              <div style={{ position: 'absolute', top: 2, left: 2, width: 24, height: 24, borderRadius: '50%', background: '#fff', boxShadow: '0 1px 4px rgba(0,0,0,0.2)' }} />
            </div>
          </div>
        ))}

        {/* Show me */}
        <div>
          <div style={{ fontSize: 15, fontWeight: 700, color: '#141414', marginBottom: 10 }}>Show me</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {['Everyone', 'Men', 'Women', 'Non-binary'].map(opt => (
              <button key={opt} style={{
                padding: '8px 16px', borderRadius: 9999,
                border: `2px solid ${opt === 'Everyone' ? color : '#ebebeb'}`,
                background: opt === 'Everyone' ? `${color}12` : '#fff',
                fontSize: 13, fontWeight: 600,
                color: opt === 'Everyone' ? color : '#737373',
                cursor: 'pointer', transition: 'all 280ms',
              }}>
                {opt}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function UpgradeScreen({ onBack, color, onUpgrade }: { onBack: () => void; color: string; onUpgrade: () => void }) {
  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', overflowY: 'auto' }} className="no-scrollbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '16px 20px' }}>
        <button onClick={onBack} style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 16 }}>←</button>
      </div>

      {/* Header with gradient */}
      <div style={{
        padding: '20px 32px 32px', textAlign: 'center',
        background: `linear-gradient(160deg, ${color}20 0%, transparent 70%)`,
        transition: 'background 280ms',
      }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>✦</div>
        <div style={{ fontSize: 30, fontWeight: 800, letterSpacing: '-0.03em', color: '#141414', marginBottom: 8 }}>Nomapal+</div>
        <div style={{ fontSize: 16, color: '#737373' }}>Travel further, match better.</div>
      </div>

      <div style={{ padding: '0 20px 32px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        {[
          { emoji: '👁', label: 'See who liked you',       free: false },
          { emoji: '⭐', label: 'Unlimited Super Matches',  free: false },
          { emoji: '🌍', label: 'Global mode',             free: false },
          { emoji: '🔧', label: 'Advanced filters',        free: false },
          { emoji: '↩', label: 'Rewind last swipe',        free: false },
          { emoji: '🔥', label: 'Profile Boost',           free: false },
          { emoji: '✓', label: 'Swipe & Match',            free: true  },
          { emoji: '✓', label: 'Trip Boards',              free: true  },
          { emoji: '✓', label: 'Group planning',           free: true  },
        ].map(f => (
          <div key={f.label} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '13px 16px', borderRadius: 12,
            background: f.free ? '#f5f5f5' : `${color}08`,
            border: `1.5px solid ${f.free ? '#ebebeb' : color + '25'}`,
            transition: 'all 280ms',
          }}>
            <span style={{ fontSize: 20, width: 28, textAlign: 'center' }}>{f.emoji}</span>
            <span style={{ fontSize: 14, fontWeight: 600, flex: 1, color: f.free ? '#999' : '#262626' }}>{f.label}</span>
            {!f.free && (
              <span style={{ fontSize: 11, fontWeight: 800, color, textTransform: 'uppercase', letterSpacing: '0.06em', transition: 'color 280ms' }}>PLUS</span>
            )}
          </div>
        ))}
      </div>

      <div style={{ padding: '0 20px 32px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div style={{ padding: '20px', background: `${color}10`, borderRadius: 16, border: `2px solid ${color}25`, transition: 'all 280ms' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <div style={{ fontSize: 24, fontWeight: 800, color: '#141414' }}>$79.99<span style={{ fontSize: 14, fontWeight: 500, color: '#999' }}>/year</span></div>
              <div style={{ fontSize: 13, color: '#44CC44', fontWeight: 600 }}>Save 49% vs monthly</div>
            </div>
            <div style={{ padding: '6px 12px', background: color, color: '#fff', borderRadius: 8, fontSize: 12, fontWeight: 700, transition: 'background 280ms' }}>BEST VALUE</div>
          </div>
        </div>

        <button onClick={onUpgrade} style={{ padding: '18px', background: color, color: '#fff', fontSize: 17, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer', transition: 'background 280ms', boxShadow: `0 8px 32px ${color}44` }}>
          Start 3-Day Free Trial
        </button>
        <button onClick={onBack} style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500, padding: '8px' }}>
          Continue with free
        </button>
      </div>
    </div>
  )
}

function SafetyScreen({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', overflowY: 'auto' }} className="no-scrollbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '16px 20px', borderBottom: '1px solid #ebebeb' }}>
        <button onClick={onBack} style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 16 }}>←</button>
        <span style={{ fontSize: 18, fontWeight: 700 }}>Safety Center</span>
      </div>

      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        {[
          { emoji: '👁‍🗨', label: 'Photo Verification',     desc: 'Verify your profile with a selfie match',    cta: 'Start Now' },
          { emoji: '📤', label: 'Emergency Share',         desc: 'Share your trip details with a trusted contact', cta: 'Set Up' },
          { emoji: '🚨', label: 'Panic Button',            desc: 'One-tap call to local emergency services', cta: 'Configure' },
          { emoji: '🚫', label: 'Block & Report',          desc: 'Block anyone and report bad behavior',     cta: null },
          { emoji: '📚', label: 'Safety Tips',             desc: 'Best practices for safe solo travel',      cta: 'Read' },
        ].map(item => (
          <div key={item.label} style={{ padding: '16px', background: '#f5f5f5', borderRadius: 14, display: 'flex', alignItems: 'center', gap: 14 }}>
            <span style={{ fontSize: 28, width: 44, textAlign: 'center' }}>{item.emoji}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15, fontWeight: 700, color: '#141414', marginBottom: 3 }}>{item.label}</div>
              <div style={{ fontSize: 13, color: '#999' }}>{item.desc}</div>
            </div>
            {item.cta && (
              <button style={{ padding: '8px 14px', background: color, color: '#fff', borderRadius: 9999, border: 'none', cursor: 'pointer', fontSize: 13, fontWeight: 700, flexShrink: 0, transition: 'background 280ms' }}>
                {item.cta}
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
