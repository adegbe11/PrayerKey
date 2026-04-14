'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/lib/auth-context'
import { saveUserProfile } from '@/lib/db'

type Section = 'main' | 'edit' | 'settings' | 'safety' | 'notifications' | 'help' | 'privacy-policy' | 'cookie-policy' | 'privacy-preferences' | 'terms'

export default function ProfilePage() {
  const { color, advance } = useColor()
  const router = useRouter()
  const { user, profile: authProfile, signOut, refreshProfile } = useAuth()
  const [section, setSection] = useState<Section>('main')

  function nav(s: Section) { setSection(s); advance() }

  // Use real profile from Firestore, fall back to reasonable defaults
  const displayUser = {
    name: authProfile?.name ?? 'Traveler',
    age: authProfile?.age ?? 0,
    photo: authProfile?.photos?.[0] ?? 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&q=80',
    location: authProfile?.location ?? '',
    destinations: authProfile?.destinations ?? [],
    travelDates: authProfile?.travelDates ?? '',
    bio: authProfile?.bio ?? '',
    interests: authProfile?.interests ?? [],
    verified: authProfile?.verified ?? 1,
    reputationScore: authProfile?.reputationScore,
    reputationCount: authProfile?.reputationCount ?? 0,
    job: authProfile?.job,
    education: authProfile?.education,
    languages: authProfile?.languages ?? [],
    photos: authProfile?.photos ?? [],
  }

  if (section === 'edit') return <EditProfile onBack={() => nav('main')} color={color} profile={displayUser} uid={user?.uid ?? ''} onSaved={refreshProfile} />
  if (section === 'settings') return <SettingsScreen onBack={() => nav('main')} color={color} onNav={nav} onSignOut={signOut} />
  if (section === 'safety') return <SafetyScreen onBack={() => nav('main')} color={color} />
  if (section === 'notifications') return <NotificationsScreen onBack={() => nav('main')} color={color} />
  if (section === 'help') return <HelpScreen onBack={() => nav('main')} color={color} />
  if (section === 'privacy-policy') return <PrivacyPolicyScreen onBack={() => nav('settings')} color={color} />
  if (section === 'cookie-policy') return <CookiePolicyScreen onBack={() => nav('settings')} color={color} />
  if (section === 'privacy-preferences') return <PrivacyPreferencesScreen onBack={() => nav('settings')} color={color} />
  if (section === 'terms') return <TermsScreen onBack={() => nav('settings')} color={color} />

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">

      {/* Full-bleed hero */}
      <div style={{ position: 'relative', height: 420 }}>
        <img src={displayUser.photo} alt="Profile"
          style={{ width: '100%', height: '100%', objectFit: 'cover', objectPosition: 'center top' }} />

        {/* Multi-stop gradient overlay */}
        <div style={{
          position: 'absolute', inset: 0,
          background: 'linear-gradient(to bottom, rgba(0,0,0,0.15) 0%, transparent 30%, rgba(0,0,0,0.05) 50%, rgba(0,0,0,0.72) 100%)',
        }} />

        {/* Edit button — glassmorphic top-right */}
        <button onClick={() => nav('edit')} style={{
          position: 'absolute', top: 'max(var(--sat), 20px)', right: 16,
          padding: '9px 18px',
          background: 'rgba(255,255,255,0.18)',
          backdropFilter: 'blur(16px)',
          WebkitBackdropFilter: 'blur(16px)',
          borderRadius: 9999,
          border: '1px solid rgba(255,255,255,0.4)',
          cursor: 'pointer', fontSize: 13, fontWeight: 700, color: '#fff',
          letterSpacing: '0.01em',
        }}>
          Edit Profile
        </button>

        {/* Profile info at bottom of hero */}
        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, padding: '0 20px 20px' }}>
          {/* Name + verified */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
            <span style={{ fontSize: 30, fontWeight: 800, color: '#fff', letterSpacing: '-0.03em' }}>
              {displayUser.name}, {displayUser.age}
            </span>
            {displayUser.verified >= 2 && (
              <div style={{
                display: 'flex', alignItems: 'center', gap: 4,
                padding: '4px 10px',
                background: 'rgba(0,136,255,0.85)',
                backdropFilter: 'blur(8px)',
                borderRadius: 9999,
              }}>
                <span style={{ fontSize: 10, fontWeight: 800, color: '#fff' }}>✓ Verified</span>
              </div>
            )}
          </div>

          {/* Location */}
          <div style={{ fontSize: 14, color: 'rgba(255,255,255,0.82)', fontWeight: 500, marginBottom: 12 }}>
            📍 {displayUser.location}
          </div>

          {/* Destination pills */}
          <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
            {displayUser.destinations.map(d => (
              <div key={d} style={{
                padding: '5px 12px',
                background: 'rgba(255,255,255,0.18)',
                backdropFilter: 'blur(12px)',
                WebkitBackdropFilter: 'blur(12px)',
                border: '1px solid rgba(255,255,255,0.35)',
                borderRadius: 9999,
                fontSize: 12, fontWeight: 700, color: '#fff',
              }}>
                ✈ {d}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Profile completion card */}
      <div style={{ margin: '16px 16px 0', padding: '16px 18px', background: '#fff', borderRadius: 18, boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
          <span style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a' }}>Profile Strength</span>
          <span style={{ fontSize: 14, fontWeight: 800, color, transition: 'color 280ms' }}>72%</span>
        </div>
        <div style={{ height: 5, background: '#F0F0F2', borderRadius: 9999, overflow: 'hidden' }}>
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: '72%' }}
            transition={{ duration: 0.9, ease: [0.34, 1.56, 0.64, 1] }}
            style={{ height: '100%', background: color, borderRadius: 9999, transition: 'background 280ms' }}
          />
        </div>
        <div style={{ marginTop: 8, fontSize: 12, color: '#A0A0A8', fontWeight: 500 }}>
          Add ID verification to unlock Verified Nomad badge
        </div>
      </div>


      {/* Stats row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, margin: '12px 16px 0' }}>
        {[
          { label: 'Likes',     value: '23',  icon: '♥',  iconColor: '#FF5A5F' },
          { label: 'Matches',   value: '8',   icon: '✈',  iconColor: color },
          { label: 'Rep Score', value: '4.8', icon: '★',  iconColor: '#FFB800' },
        ].map(stat => (
          <div key={stat.label} style={{
            background: '#fff', borderRadius: 18, padding: '16px 12px',
            textAlign: 'center', boxShadow: '0 1px 8px rgba(0,0,0,0.05)',
          }}>
            <div style={{ fontSize: 22, marginBottom: 4, color: stat.iconColor, transition: 'color 280ms' }}>
              {stat.icon}
            </div>
            <div style={{ fontSize: 24, fontWeight: 800, color: '#1a1a1a', letterSpacing: '-0.03em', lineHeight: 1 }}>
              {stat.value}
            </div>
            <div style={{ fontSize: 11, color: '#A0A0A8', fontWeight: 600, marginTop: 3, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              {stat.label}
            </div>
          </div>
        ))}
      </div>

      {/* Menu */}
      <div style={{ margin: '12px 16px 32px', background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
        {[
          { icon: '⚙️', bg: '#F0F0F2', label: 'Settings & Privacy',  action: () => nav('settings') },
          { icon: '🛡',  bg: '#E8F4FF', label: 'Safety Center',       action: () => nav('safety') },
          { icon: '🔔', bg: '#FFF4E0', label: 'Notifications',        action: () => nav('notifications') },
          { icon: '❓', bg: '#F0F0F2', label: 'Help & Feedback',      action: () => nav('help') },
          { icon: '🚪', bg: '#FFE8E8', label: 'Log Out',              action: async () => { await signOut(); window.location.href = '/onboarding' }, danger: true },
        ].map((item, i, arr) => (
          <motion.button
            key={item.label}
            whileTap={{ background: '#F7F7F8' }}
            onClick={item.action}
            style={{
              width: '100%', padding: '14px 18px',
              background: 'transparent', border: 'none',
              borderBottom: i < arr.length - 1 ? '1px solid #F3F3F5' : 'none',
              cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 14,
              textAlign: 'left',
            }}
          >
            {/* Icon square */}
            <div style={{
              width: 38, height: 38, borderRadius: 11,
              background: item.bg,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 18, flexShrink: 0,
              transition: 'background 280ms',
            }}>
              {item.icon}
            </div>
            <span style={{
              flex: 1, fontSize: 15, fontWeight: 600,
              color: (item as any).danger ? '#FF5A5F' : (item as any).highlight ? color : '#1a1a1a',
              transition: 'color 280ms',
            }}>
              {item.label}
            </span>
            {!(item as any).danger && (
              <svg width="7" height="12" viewBox="0 0 7 12" fill="none">
                <path d="M1 1L6 6L1 11" stroke="#C8C8CE" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            )}
          </motion.button>
        ))}
      </div>
    </div>
  )
}

/* ─── Sub-screens ──────────────────────────────────────────────────────────── */

function SubHeader({ title, onBack }: { title: string; onBack: () => void }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: 'max(var(--sat), 16px) 16px 14px',
      borderBottom: '1px solid #F3F3F5',
      background: '#fff',
    }}>
      <button onClick={onBack} style={{
        width: 36, height: 36, borderRadius: '50%',
        background: '#F3F3F5', border: 'none',
        cursor: 'pointer', fontSize: 18, display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: '#1a1a1a',
      }}>‹</button>
      <span style={{ fontSize: 18, fontWeight: 700, color: '#1a1a1a', letterSpacing: '-0.01em' }}>{title}</span>
    </div>
  )
}

const TRAVEL_INTERESTS = [
  '🏄 Surfing','🏔 Hiking','📸 Photography','🍜 Foodie','🎨 Art',
  '🎵 Music','🏋 Fitness','🌿 Nature','🎒 Backpacking','💎 Luxury',
  '🚵 Adventure','🧘 Wellness','🌊 Diving','🎭 Culture','🍷 Wine',
  '📚 Books','🎮 Gaming','🔭 Astrology','🏕 Camping','🌃 Nightlife',
  '🚴 Cycling','🤿 Snorkeling','🎪 Festivals','🏖 Beach','🌏 Digital Nomad',
]

const PROMPTS_LIST = [
  'My most underrated destination is...',
  'You\'ll never catch me traveling without...',
  'My travel style in 3 words...',
  'We\'d be perfect travel partners if you...',
  'The trip that changed me was...',
  'My go-to travel hack is...',
  'The quickest way to my heart is...',
  'I\'m most alive when I\'m...',
  'Best local dish I\'ve ever had...',
  'My ideal travel day looks like...',
]

const VITAL_OPTIONS: Record<string, string[]> = {
  'Trip Style':     ['Budget 🎒', 'Mid-range ✈️', 'Luxury 💎'],
  'Travel Pace':    ['Slow & Deep', 'Balanced', 'Fast & Full'],
  'Planning':       ['Full Planner 📋', 'Flexible', 'Spontaneous 🎲'],
  'Accommodation':  ['Hostels', 'Airbnb', 'Hotels', 'Camping'],
  'Trip Length':    ['Weekend Trips', '1–2 weeks', 'Month+', 'Full Nomad'],
  'Drinking':       ['Never', 'Rarely', 'Socially', 'Regularly'],
  'Smoking':        ['Never', 'Sometimes', 'Yes'],
}

function VitalPills({ label, options, value, color, onChange }: {
  label: string; options: string[]; value: string; color: string; onChange: (v: string) => void
}) {
  return (
    <div style={{ marginBottom: 18 }}>
      <div style={{ fontSize: 12, fontWeight: 700, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 8 }}>{label}</div>
      <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
        {options.map(opt => {
          const active = value === opt
          return (
            <button key={opt} onClick={() => onChange(opt)} style={{
              padding: '8px 14px', borderRadius: 9999, cursor: 'pointer',
              border: `2px solid ${active ? color : '#E4E4E8'}`,
              background: active ? `${color}12` : '#F7F7F8',
              fontSize: 12, fontWeight: 600,
              color: active ? color : '#737378',
              transition: 'all 200ms',
            }}>{opt}</button>
          )
        })}
      </div>
    </div>
  )
}

interface EditProfileData {
  name: string; age: number; photo: string; location: string;
  destinations: string[]; travelDates: string; bio: string;
  interests: string[]; verified: number; reputationScore?: number;
  reputationCount: number; job?: string; education?: string; languages: string[]; photos: string[];
}

function EditProfile({ onBack, color, profile, uid, onSaved }: {
  onBack: () => void; color: string;
  profile: EditProfileData; uid: string; onSaved: () => Promise<void>
}) {
  const [photos, setPhotos] = useState<string[]>([profile.photo, '', '', '', '', '', '', '', ''])
  const [tagline, setTagline] = useState('I find the best street food in every city')
  const [bio, setBio] = useState(profile.bio)
  const [job, setJob] = useState(profile.job ?? '')
  const [company, setCompany] = useState('Freelance')
  const [school, setSchool] = useState(profile.education ?? '')
  const [basedIn, setBasedIn] = useState(profile.location)
  const [passport, setPassport] = useState('United States')
  const [gender, setGender] = useState('Man')
  const [orientation, setOrientation] = useState('Straight')
  const [interests, setInterests] = useState<string[]>(profile.interests.length ? profile.interests : ['📸 Photography', '🏔 Hiking', '🍜 Foodie', '🎨 Art', '📚 Books'])
  const [destinations, setDestinations] = useState<string[]>(profile.destinations)
  const [destInput, setDestInput] = useState('')
  const [saving, setSaving] = useState(false)
  const [prompts, setPrompts] = useState<{q: string; a: string}[]>([
    { q: 'My travel style in 3 words...', a: 'Light, curious, fed.' },
    { q: 'You\'ll never catch me traveling without...', a: '' },
  ])
  const [addingPrompt, setAddingPrompt] = useState(false)
  const [vitals, setVitals] = useState<Record<string, string>>({
    'Trip Style': 'Mid-range ✈️', 'Travel Pace': 'Balanced', 'Planning': 'Flexible',
    'Accommodation': 'Airbnb', 'Trip Length': '1–2 weeks', 'Drinking': 'Socially', 'Smoking': 'Never',
  })
  const [anthemQuery, setAnthemQuery] = useState('')
  const [anthem, setAnthem] = useState<{title: string; artist: string} | null>(null)

  function toggleInterest(i: string) {
    setInterests(prev => {
      if (prev.includes(i)) return prev.filter(x => x !== i)
      if (prev.length >= 5) return prev
      return [...prev, i]
    })
  }

  function addDest() {
    if (!destInput.trim() || destinations.length >= 5) return
    setDestinations(prev => [...prev, destInput.trim()])
    setDestInput('')
  }

  function updatePromptAnswer(idx: number, val: string) {
    setPrompts(prev => prev.map((p, i) => i === idx ? { ...p, a: val } : p))
  }

  function addPrompt(q: string) {
    if (prompts.length >= 3) return
    setPrompts(prev => [...prev, { q, a: '' }])
    setAddingPrompt(false)
  }

  function removePrompt(idx: number) {
    setPrompts(prev => prev.filter((_, i) => i !== idx))
  }

  function setPhoto(idx: number, url: string) {
    setPhotos(prev => { const n = [...prev]; n[idx] = url; return n })
  }

  const bioLeft = 500 - bio.length

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Edit Profile" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>

        {/* ── Media Gallery ─────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Photos & Clips</div>
            <div style={{ fontSize: 11, color: '#A0A0A8' }}>{photos.filter(Boolean).length}/9</div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
            {photos.map((photo, idx) => (
              <div key={idx} style={{ aspectRatio: '3/4', borderRadius: 12, overflow: 'hidden', position: 'relative',
                border: photo ? 'none' : `2px dashed ${idx === 0 ? color : '#E0E0E4'}`,
                background: photo ? 'transparent' : '#F7F7F8',
                display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
              }}>
                {photo ? (
                  <>
                    <img src={photo} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    {idx === 0 && (
                      <div style={{ position: 'absolute', bottom: 6, left: 6, background: color, borderRadius: 5,
                        fontSize: 9, fontWeight: 800, color: '#fff', padding: '3px 7px', letterSpacing: '0.05em' }}>
                        MAIN
                      </div>
                    )}
                    <button onClick={() => setPhoto(idx, '')} style={{
                      position: 'absolute', top: 5, right: 5, width: 22, height: 22, borderRadius: '50%',
                      background: 'rgba(0,0,0,0.55)', border: 'none', cursor: 'pointer',
                      fontSize: 11, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    }}>✕</button>
                  </>
                ) : (
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: idx === 0 ? 24 : 20, color: idx === 0 ? color : '#C8C8CE' }}>
                      {idx === 0 ? '📷' : '+'}
                    </div>
                    {idx === 0 && <div style={{ fontSize: 9, fontWeight: 800, color, textTransform: 'uppercase', marginTop: 2 }}>Required</div>}
                  </div>
                )}
              </div>
            ))}
          </div>
          <div style={{ marginTop: 12, padding: '10px 12px', background: `${color}0A`, borderRadius: 10,
            display: 'flex', alignItems: 'center', gap: 8, border: `1px solid ${color}20` }}>
            <span style={{ fontSize: 14 }}>✨</span>
            <span style={{ fontSize: 12, color: '#737378', flex: 1 }}>AI Photo Selector — tap to scan your camera roll for your best shots</span>
            <button style={{ fontSize: 11, fontWeight: 800, color, background: 'none', border: 'none', cursor: 'pointer' }}>Scan</button>
          </div>
        </div>

        {/* ── Travel Tagline ─────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 }}>Travel Tagline</div>
          <div style={{ fontSize: 11, color: '#A0A0A8', marginBottom: 10 }}>One line shown on your card. Make it unforgettable.</div>
          <input
            value={tagline}
            onChange={e => setTagline(e.target.value.slice(0, 80))}
            placeholder="e.g. I find the best street food in every city"
            style={{
              width: '100%', padding: '13px 15px',
              background: '#F7F7F8', borderRadius: 12,
              border: `2px solid ${tagline ? color : '#E4E4E8'}`,
              fontSize: 15, outline: 'none', color: '#1a1a1a',
              fontWeight: 500, boxSizing: 'border-box', transition: 'border-color 250ms',
              fontFamily: 'inherit',
            }}
          />
        </div>

        {/* ── Bio ───────────────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em' }}>About Me</div>
            <div style={{ fontSize: 11, color: bioLeft < 50 ? '#FF5A5F' : '#A0A0A8' }}>{bioLeft} left</div>
          </div>
          <textarea
            value={bio}
            onChange={e => setBio(e.target.value.slice(0, 500))}
            rows={4}
            placeholder="Describe yourself. What makes you a great travel partner?"
            style={{
              width: '100%', padding: '13px 15px',
              background: '#F7F7F8', borderRadius: 12,
              border: `2px solid ${bio ? color : '#E4E4E8'}`,
              fontSize: 15, resize: 'none', outline: 'none',
              color: '#1a1a1a', lineHeight: 1.6, fontFamily: 'inherit',
              transition: 'border-color 250ms', boxSizing: 'border-box',
            }}
          />
        </div>

        {/* ── Prompts ───────────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
            <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Prompts</div>
            <div style={{ fontSize: 11, color: '#A0A0A8' }}>{prompts.length}/3</div>
          </div>

          {prompts.map((p, idx) => (
            <div key={idx} style={{ marginBottom: 14, background: '#F7F7F8', borderRadius: 14, overflow: 'hidden' }}>
              <div style={{ padding: '10px 14px', display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                borderBottom: '1px solid #ECECEF' }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: color, flex: 1 }}>{p.q}</div>
                <button onClick={() => removePrompt(idx)} style={{
                  background: 'none', border: 'none', cursor: 'pointer', color: '#C0C0C8', fontSize: 16, padding: '0 0 0 8px',
                }}>×</button>
              </div>
              <textarea
                value={p.a}
                onChange={e => updatePromptAnswer(idx, e.target.value)}
                placeholder="Your answer..."
                rows={2}
                style={{
                  width: '100%', padding: '10px 14px',
                  background: 'transparent', border: 'none', outline: 'none',
                  fontSize: 14, resize: 'none', color: '#1a1a1a', lineHeight: 1.5,
                  fontFamily: 'inherit', boxSizing: 'border-box',
                }}
              />
            </div>
          ))}

          {prompts.length < 3 && !addingPrompt && (
            <button onClick={() => setAddingPrompt(true)} style={{
              width: '100%', padding: '12px', background: '#F7F7F8',
              border: `2px dashed ${color}40`, borderRadius: 12,
              fontSize: 13, fontWeight: 700, color, cursor: 'pointer',
            }}>+ Add a Prompt</button>
          )}

          {addingPrompt && (
            <div style={{ background: '#F7F7F8', borderRadius: 14, overflow: 'hidden' }}>
              <div style={{ padding: '10px 14px 6px', fontSize: 12, fontWeight: 700, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Choose a prompt
              </div>
              {PROMPTS_LIST.filter(q => !prompts.find(p => p.q === q)).map(q => (
                <button key={q} onClick={() => addPrompt(q)} style={{
                  width: '100%', padding: '11px 14px', background: 'none', border: 'none',
                  borderTop: '1px solid #ECECEF', cursor: 'pointer', textAlign: 'left',
                  fontSize: 14, fontWeight: 500, color: '#1a1a1a',
                }}>{q}</button>
              ))}
              <button onClick={() => setAddingPrompt(false)} style={{
                width: '100%', padding: '11px 14px', background: 'none', border: 'none',
                borderTop: '1px solid #ECECEF', cursor: 'pointer',
                fontSize: 13, fontWeight: 600, color: '#A0A0A8',
              }}>Cancel</button>
            </div>
          )}
        </div>

        {/* ── Interests ─────────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Interests</div>
            <div style={{ fontSize: 11, color: interests.length >= 5 ? color : '#A0A0A8' }}>{interests.length}/5</div>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {TRAVEL_INTERESTS.map(tag => {
              const active = interests.includes(tag)
              const disabled = !active && interests.length >= 5
              return (
                <button key={tag} onClick={() => !disabled && toggleInterest(tag)} style={{
                  padding: '8px 14px', borderRadius: 9999, cursor: disabled ? 'default' : 'pointer',
                  border: `2px solid ${active ? color : '#E4E4E8'}`,
                  background: active ? `${color}12` : '#F7F7F8',
                  fontSize: 13, fontWeight: 600,
                  color: active ? color : disabled ? '#C0C0C8' : '#737378',
                  transition: 'all 200ms',
                  opacity: disabled ? 0.5 : 1,
                }}>{tag}</button>
              )
            })}
          </div>
        </div>

        {/* ── Essentials ────────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 14 }}>Essentials</div>
          {[
            { label: 'Job Title', val: job, set: setJob, placeholder: 'Product Designer' },
            { label: 'Company', val: company, set: setCompany, placeholder: 'Freelance / Company name' },
            { label: 'School', val: school, set: setSchool, placeholder: 'University or College' },
            { label: 'Based In', val: basedIn, set: setBasedIn, placeholder: 'Your home city' },
            { label: 'Passport Country', val: passport, set: setPassport, placeholder: 'Country of citizenship' },
          ].map(f => (
            <div key={f.label} style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: '#A0A0A8', marginBottom: 5, textTransform: 'uppercase', letterSpacing: '0.04em' }}>{f.label}</div>
              <input
                value={f.val}
                onChange={e => f.set(e.target.value)}
                placeholder={f.placeholder}
                style={{
                  width: '100%', padding: '12px 14px',
                  background: '#F7F7F8', borderRadius: 10,
                  border: `2px solid ${f.val ? color : '#E4E4E8'}`,
                  fontSize: 15, outline: 'none', color: '#1a1a1a',
                  fontWeight: 500, boxSizing: 'border-box', transition: 'border-color 250ms',
                  fontFamily: 'inherit',
                }}
              />
            </div>
          ))}

          <div style={{ marginBottom: 12 }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#A0A0A8', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.04em' }}>Gender</div>
            <div style={{ display: 'flex', gap: 8 }}>
              {['Man', 'Woman', 'Non-binary', 'Other'].map(g => (
                <button key={g} onClick={() => setGender(g)} style={{
                  flex: 1, padding: '10px 4px', borderRadius: 10, cursor: 'pointer',
                  border: `2px solid ${gender === g ? color : '#E4E4E8'}`,
                  background: gender === g ? `${color}12` : '#F7F7F8',
                  fontSize: 11, fontWeight: 600,
                  color: gender === g ? color : '#737378', transition: 'all 200ms',
                }}>{g}</button>
              ))}
            </div>
          </div>

          <div>
            <div style={{ fontSize: 11, fontWeight: 700, color: '#A0A0A8', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.04em' }}>Orientation</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {['Straight', 'Gay', 'Lesbian', 'Bisexual', 'Open'].map(o => (
                <button key={o} onClick={() => setOrientation(o)} style={{
                  padding: '8px 14px', borderRadius: 9999, cursor: 'pointer',
                  border: `2px solid ${orientation === o ? color : '#E4E4E8'}`,
                  background: orientation === o ? `${color}12` : '#F7F7F8',
                  fontSize: 12, fontWeight: 600,
                  color: orientation === o ? color : '#737378', transition: 'all 200ms',
                }}>{o}</button>
              ))}
            </div>
          </div>
        </div>

        {/* ── Travel Vitals ─────────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 16 }}>Travel Vitals</div>
          {Object.entries(VITAL_OPTIONS).map(([label, options]) => (
            <VitalPills key={label} label={label} options={options}
              value={vitals[label] ?? ''} color={color}
              onChange={v => setVitals(prev => ({ ...prev, [label]: v }))} />
          ))}
        </div>

        {/* ── Dream Destinations ────────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Dream Destinations</div>
            <div style={{ fontSize: 11, color: '#A0A0A8' }}>{destinations.length}/5</div>
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 12 }}>
            {destinations.map(d => (
              <div key={d} style={{
                display: 'flex', alignItems: 'center', gap: 5,
                padding: '7px 12px', background: `${color}0F`,
                border: `1.5px solid ${color}30`, borderRadius: 9999,
              }}>
                <span style={{ fontSize: 12, fontWeight: 700, color }}>✈ {d}</span>
                <button onClick={() => setDestinations(prev => prev.filter(x => x !== d))}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color, fontSize: 14, lineHeight: 1, padding: 0 }}>×</button>
              </div>
            ))}
          </div>
          {destinations.length < 5 && (
            <div style={{ display: 'flex', gap: 8 }}>
              <input value={destInput} onChange={e => setDestInput(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && addDest()}
                placeholder="Add a destination..."
                style={{
                  flex: 1, padding: '10px 14px', background: '#F7F7F8', borderRadius: 10,
                  border: `2px solid ${destInput ? color : '#E4E4E8'}`,
                  fontSize: 14, outline: 'none', color: '#1a1a1a', fontFamily: 'inherit',
                  transition: 'border-color 250ms', boxSizing: 'border-box',
                }}
              />
              <button onClick={addDest} style={{
                padding: '10px 16px', background: color, color: '#fff',
                borderRadius: 10, border: 'none', cursor: 'pointer',
                fontSize: 14, fontWeight: 700, transition: 'background 280ms',
              }}>Add</button>
            </div>
          )}
        </div>

        {/* ── Connected Integrations ─────────────────────────────────────────── */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ fontSize: 12, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 14 }}>Connected Integrations</div>

          {/* Spotify Anthem */}
          <div style={{ marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: '#1DB954',
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, flexShrink: 0 }}>♫</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a' }}>Spotify Anthem</div>
                <div style={{ fontSize: 11, color: '#A0A0A8' }}>Plays when someone views your profile</div>
              </div>
            </div>
            {anthem ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px',
                background: '#F7F7F8', borderRadius: 10, border: '2px solid #1DB95440' }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a' }}>{anthem.title}</div>
                  <div style={{ fontSize: 12, color: '#737378' }}>{anthem.artist}</div>
                </div>
                <button onClick={() => setAnthem(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#A0A0A8', fontSize: 16 }}>✕</button>
              </div>
            ) : (
              <div style={{ display: 'flex', gap: 8 }}>
                <input value={anthemQuery} onChange={e => setAnthemQuery(e.target.value)}
                  placeholder="Search for a song..."
                  style={{
                    flex: 1, padding: '10px 14px', background: '#F7F7F8', borderRadius: 10,
                    border: `2px solid ${anthemQuery ? '#1DB954' : '#E4E4E8'}`,
                    fontSize: 14, outline: 'none', color: '#1a1a1a', fontFamily: 'inherit',
                    transition: 'border-color 250ms', boxSizing: 'border-box',
                  }}
                />
                <button onClick={() => anthemQuery && setAnthem({ title: anthemQuery, artist: 'Spotify' })} style={{
                  padding: '10px 16px', background: '#1DB954', color: '#fff',
                  borderRadius: 10, border: 'none', cursor: 'pointer', fontSize: 13, fontWeight: 700,
                }}>Add</button>
              </div>
            )}
          </div>

          {/* Instagram */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '12px 14px',
            background: 'linear-gradient(135deg, #F7F7F800, #F7F7F8)',
            border: '2px dashed #E4E4E8', borderRadius: 12, cursor: 'pointer' }}>
            <div style={{ width: 36, height: 36, borderRadius: 10,
              background: 'linear-gradient(135deg, #F77737, #E1306C, #833AB4)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, flexShrink: 0 }}>📷</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a' }}>Link Instagram</div>
              <div style={{ fontSize: 11, color: '#A0A0A8' }}>Show your 6 latest posts (handle stays private)</div>
            </div>
            <div style={{ fontSize: 14, color: '#A0A0A8' }}>→</div>
          </div>
        </div>

        {/* ── Save ──────────────────────────────────────────────────────────── */}
        <button
          onClick={async () => {
            if (!uid || saving) return
            setSaving(true)
            try {
              await saveUserProfile(uid, {
                bio,
                job: job || undefined,
                education: school || undefined,
                location: basedIn,
                destinations,
                interests,
              })
              await onSaved()
            } catch (e) {
              console.error('Save failed', e)
            } finally {
              setSaving(false)
              onBack()
            }
          }}
          disabled={saving}
          style={{
            padding: '18px', background: saving ? '#999' : color, color: '#fff',
            fontSize: 16, fontWeight: 700, borderRadius: 9999,
            border: 'none', cursor: saving ? 'default' : 'pointer',
            boxShadow: `0 8px 24px ${color}33`,
            transition: 'background 280ms, box-shadow 280ms',
            marginBottom: 8,
          }}
        >
          {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </div>
  )
}

function IOSToggle({ on, color, onChange }: { on: boolean; color: string; onChange: () => void }) {
  return (
    <div onClick={onChange} style={{
      width: 50, height: 30, borderRadius: 15,
      background: on ? color : '#D8D8DC',
      position: 'relative', cursor: 'pointer',
      transition: 'background 280ms', flexShrink: 0,
    }}>
      <div style={{
        position: 'absolute', top: 3, left: on ? 23 : 3,
        width: 24, height: 24, borderRadius: '50%', background: '#fff',
        boxShadow: '0 1px 4px rgba(0,0,0,0.25)',
        transition: 'left 220ms cubic-bezier(0.34,1.56,0.64,1)',
      }} />
    </div>
  )
}

function SettingsSection({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 4 }}>
      <div style={{ fontSize: 11, fontWeight: 800, color: '#A0A0A8', textTransform: 'uppercase', letterSpacing: '0.07em', padding: '0 4px 8px' }}>{label}</div>
      <div style={{ background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}
        className="settings-section">
        {children}
      </div>
    </div>
  )
}

function SettingsRow({ label, sub, value, premium, color, chevron, danger, onPress, toggle, toggleOn, toggleColor }: {
  label: string; sub?: string; value?: string; premium?: boolean; color?: string;
  chevron?: boolean; danger?: boolean; onPress?: () => void;
  toggle?: boolean; toggleOn?: boolean; toggleColor?: string;
}) {
  return (
    <div onClick={onPress} style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '14px 18px', cursor: onPress || toggle ? 'pointer' : 'default',
      borderBottom: '1px solid #F3F3F5',
    }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 15, fontWeight: 600, color: danger ? '#FF5A5F' : '#1a1a1a' }}>
          {label}
          {premium && color && (
            <span style={{ fontSize: 10, fontWeight: 800, color, padding: '2px 7px', background: `${color}15`, borderRadius: 5 }}>PLUS</span>
          )}
        </div>
        {sub && <div style={{ fontSize: 12, color: '#A0A0A8', marginTop: 1 }}>{sub}</div>}
      </div>
      {value && <div style={{ fontSize: 14, color: '#A0A0A8', flexShrink: 0 }}>{value}</div>}
      {toggle !== undefined && <IOSToggle on={!!toggleOn} color={toggleColor ?? '#44CC44'} onChange={onPress ?? (() => {})} />}
      {chevron && <div style={{ fontSize: 14, color: '#C0C0C8', flexShrink: 0 }}>›</div>}
    </div>
  )
}

function SettingsScreen({ onBack, color, onNav, onSignOut }: { onBack: () => void; color: string; onNav: (s: Section) => void; onSignOut: () => Promise<void> }) {
  const [radius, setRadius] = useState(50)
  const [ageMin, setAgeMin] = useState(22)
  const [ageMax, setAgeMax] = useState(38)
  const [showMe, setShowMe] = useState('Everyone')
  const [darkMode, setDarkMode] = useState('System')
  const [language, setLanguage] = useState('English')
  const [toggles, setToggles] = useState({
    hide: false, distance: true, age: true, global: false,
    incognito: false, blockContacts: false, readReceipts: true, activityStatus: true,
    videoAutoplay: true,
  })

  function tog(k: keyof typeof toggles) {
    setToggles(t => ({ ...t, [k]: !t[k] }))
  }

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Settings" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 16 }}>

        {/* ── Account ───────────────────────────────────────────────────────── */}
        <SettingsSection label="Account">
          <SettingsRow label="Phone Number" value="+1 (555) 000-0000" chevron onPress={() => {}} />
          <SettingsRow label="Email" value="alex@email.com" chevron onPress={() => {}} />
          <SettingsRow label="Username" value="@alex.nomad" chevron onPress={() => {}} />
          <SettingsRow label="Connected Accounts" sub="Google · Apple" chevron onPress={() => {}} />
        </SettingsSection>

        {/* ── Discovery ─────────────────────────────────────────────────────── */}
        <SettingsSection label="Discovery">

          <div style={{ padding: '16px 18px', borderBottom: '1px solid #F3F3F5' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
              <span style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a' }}>Match Radius</span>
              <span style={{ fontSize: 15, fontWeight: 800, color, transition: 'color 280ms' }}>{radius} km</span>
            </div>
            <input type="range" min="1" max="200" value={radius}
              onChange={e => setRadius(+e.target.value)}
              style={{ width: '100%', accentColor: color, height: 4 }} />
          </div>

          <div style={{ padding: '16px 18px', borderBottom: '1px solid #F3F3F5' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
              <span style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a' }}>Age Range</span>
              <span style={{ fontSize: 15, fontWeight: 800, color, transition: 'color 280ms' }}>{ageMin}–{ageMax}</span>
            </div>
            <input type="range" min="18" max={ageMax} value={ageMin}
              onChange={e => setAgeMin(+e.target.value)}
              style={{ width: '100%', accentColor: color, height: 4 }} />
            <input type="range" min={ageMin} max="65" value={ageMax}
              onChange={e => setAgeMax(+e.target.value)}
              style={{ width: '100%', accentColor: color, marginTop: 8, height: 4 }} />
          </div>

          <div style={{ padding: '14px 18px', borderBottom: '1px solid #F3F3F5' }}>
            <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a', marginBottom: 10 }}>Show Me</div>
            <div style={{ display: 'flex', gap: 7, flexWrap: 'wrap' }}>
              {['Everyone', 'Men', 'Women', 'Non-binary', 'Groups'].map(opt => (
                <button key={opt} onClick={() => setShowMe(opt)} style={{
                  padding: '8px 14px', borderRadius: 9999,
                  border: `2px solid ${opt === showMe ? color : '#E0E0E4'}`,
                  background: opt === showMe ? `${color}0F` : '#fff',
                  fontSize: 12, fontWeight: 600,
                  color: opt === showMe ? color : '#737378',
                  cursor: 'pointer', transition: 'all 200ms',
                }}>{opt}</button>
              ))}
            </div>
          </div>

          <SettingsRow label="Location / Passport" sub="Change your swiping city" value="Barcelona" chevron onPress={() => {}} />
          <SettingsRow label="Trip Date Overlap" sub="Only show travelers whose dates overlap yours" toggle toggleOn={toggles.distance} toggleColor={color} onPress={() => tog('distance')} />
          <SettingsRow label="Global Mode" sub="See travelers worldwide" toggle toggleOn={toggles.global} toggleColor={color} onPress={() => tog('global')} />
        </SettingsSection>

        {/* ── Privacy ───────────────────────────────────────────────────────── */}
        <SettingsSection label="Privacy & Visibility">
          <SettingsRow label="Show me on Nomapal" sub="Turn off to hide your profile from discovery" toggle toggleOn={!toggles.hide} toggleColor={color} onPress={() => tog('hide')} />
          <SettingsRow label="Incognito Mode" sub="Only people you've Liked can see you" toggle toggleOn={toggles.incognito} toggleColor={color} onPress={() => tog('incognito')} />
          <SettingsRow label="Show My Distance" toggle toggleOn={toggles.distance} toggleColor={color} onPress={() => tog('distance')} />
          <SettingsRow label="Show My Age" toggle toggleOn={toggles.age} toggleColor={color} onPress={() => tog('age')} />
          <SettingsRow label="Read Receipts" sub="Let matches see when you've read messages" toggle toggleOn={toggles.readReceipts} toggleColor={color} onPress={() => tog('readReceipts')} />
          <SettingsRow label="Activity Status" sub="Show the Recently Active dot on your profile" toggle toggleOn={toggles.activityStatus} toggleColor={color} onPress={() => tog('activityStatus')} />
          <SettingsRow label="Block Contacts" sub="Hide your profile from people in your contacts" chevron onPress={() => {}} />
          <SettingsRow label="Privacy Preferences" sub="Manage how Nomapal uses your data" chevron onPress={() => onNav('privacy-preferences')} />
        </SettingsSection>

        {/* ── Notifications ─────────────────────────────────────────────────── */}
        <SettingsSection label="Notifications">
          <SettingsRow label="Push Notifications" sub="New matches, messages, likes" chevron onPress={() => onNav('notifications')} />
          <SettingsRow label="Email Notifications" sub="Manage email alerts" chevron onPress={() => {}} />
        </SettingsSection>

        {/* ── Safety ────────────────────────────────────────────────────────── */}
        <SettingsSection label="Safety & Verification">
          <SettingsRow label="Safety Center" sub="Reports, guides & emergency tools" chevron onPress={() => onNav('safety')} />
          <SettingsRow label="Photo Verification" sub="Get your blue checkmark" value="Unverified" chevron onPress={() => onNav('safety')} />
          <SettingsRow label="ID Verification" sub="Adds extra trust to your profile" value="Not set up" chevron onPress={() => {}} />
        </SettingsSection>

        {/* ── Subscriptions ─────────────────────────────────────────────────── */}
        <SettingsSection label="Payment & Subscriptions">
          <SettingsRow label="Plan" sub="Nomapal — Free forever" />
          <SettingsRow label="Payment Account" sub="Manage billing info" chevron onPress={() => {}} />
          <SettingsRow label="Restore Purchases" chevron onPress={() => {}} />
        </SettingsSection>

        {/* ── App Experience ────────────────────────────────────────────────── */}
        <SettingsSection label="App Experience">
          <div style={{ padding: '14px 18px', borderBottom: '1px solid #F3F3F5' }}>
            <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a', marginBottom: 10 }}>Dark Mode</div>
            <div style={{ display: 'flex', gap: 8 }}>
              {['Light', 'Dark', 'System'].map(m => (
                <button key={m} onClick={() => setDarkMode(m)} style={{
                  flex: 1, padding: '9px 6px', borderRadius: 10, cursor: 'pointer',
                  border: `2px solid ${darkMode === m ? color : '#E0E0E4'}`,
                  background: darkMode === m ? `${color}0F` : '#fff',
                  fontSize: 13, fontWeight: 600,
                  color: darkMode === m ? color : '#737378', transition: 'all 200ms',
                }}>{m}</button>
              ))}
            </div>
          </div>
          <SettingsRow label="Video Autoplay" sub="Auto-play Travel Clips in discovery" toggle toggleOn={toggles.videoAutoplay} toggleColor={color} onPress={() => tog('videoAutoplay')} />
          <div style={{ padding: '14px 18px', borderBottom: '1px solid #F3F3F5' }}>
            <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a', marginBottom: 10 }}>Language</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {['English', 'Español', 'Français', 'Português', '日本語'].map(l => (
                <button key={l} onClick={() => setLanguage(l)} style={{
                  padding: '8px 14px', borderRadius: 9999, cursor: 'pointer',
                  border: `2px solid ${language === l ? color : '#E0E0E4'}`,
                  background: language === l ? `${color}0F` : '#fff',
                  fontSize: 12, fontWeight: 600,
                  color: language === l ? color : '#737378', transition: 'all 200ms',
                }}>{l}</button>
              ))}
            </div>
          </div>
        </SettingsSection>

        {/* ── Legal ─────────────────────────────────────────────────────────── */}
        <SettingsSection label="Legal">
          <SettingsRow label="Privacy Policy" chevron onPress={() => onNav('privacy-policy')} />
          <SettingsRow label="Cookie Policy" chevron onPress={() => onNav('cookie-policy')} />
          <SettingsRow label="Terms of Service" chevron onPress={() => onNav('terms')} />
        </SettingsSection>

        {/* ── Danger Zone ───────────────────────────────────────────────────── */}
        <SettingsSection label="Account Actions">
          <SettingsRow label="Share Nomapal" sub="Invite friends to join" chevron onPress={() => {}} />
          <SettingsRow label="Log Out" chevron onPress={async () => { await onSignOut(); window.location.href = '/onboarding' }} />
          <SettingsRow label="Delete Account" sub="Permanently erase your profile and data" danger chevron onPress={() => {}} />
        </SettingsSection>

        <div style={{ height: 8 }} />
      </div>
    </div>
  )
}

function UpgradeScreen({ onBack, color, onUpgrade }: { onBack: () => void; color: string; onUpgrade: () => void }) {
  const [selectedPlan, setSelectedPlan] = useState<'month' | 'year'>('year')

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <div style={{ padding: 'max(var(--sat), 16px) 16px 0' }}>
        <button onClick={onBack} style={{
          width: 36, height: 36, borderRadius: '50%',
          background: '#F0F0F2', border: 'none',
          cursor: 'pointer', fontSize: 18, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#1a1a1a',
        }}>‹</button>
      </div>

      {/* Hero */}
      <div style={{
        padding: '24px 24px 32px', textAlign: 'center',
        background: `linear-gradient(170deg, ${color}18 0%, transparent 70%)`,
        transition: 'background 280ms',
      }}>
        <div style={{
          width: 72, height: 72, borderRadius: 22,
          background: `linear-gradient(135deg, ${color}, #9933CC)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 32, margin: '0 auto 16px',
          boxShadow: `0 12px 32px ${color}33`,
          transition: 'background 280ms, box-shadow 280ms',
        }}>✦</div>
        <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: '-0.03em', color: '#1a1a1a', marginBottom: 8 }}>Nomapal+</div>
        <div style={{ fontSize: 15, color: '#737378' }}>Travel further. Match better.</div>
      </div>

      <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 16 }}>
        {[
          { icon: '👁', label: 'See who liked you',        plus: true },
          { icon: '⭐', label: 'Unlimited Same Dest!',     plus: true },
          { icon: '🌍', label: 'Global mode',              plus: true },
          { icon: '🔧', label: 'Advanced travel filters',  plus: true },
          { icon: '↩', label: 'Rewind last swipe',         plus: true },
          { icon: '🔥', label: 'Profile Boost',            plus: true },
          { icon: '♥', label: 'Swipe & Match',             plus: false },
          { icon: '🗺️', label: 'Trip Boards',              plus: false },
          { icon: '✈', label: 'Group planning',            plus: false },
        ].map(f => (
          <div key={f.label} style={{
            display: 'flex', alignItems: 'center', gap: 14,
            padding: '14px 16px', borderRadius: 14,
            background: f.plus ? '#fff' : '#F7F7F8',
            border: `1.5px solid ${f.plus ? '#ECECEF' : 'transparent'}`,
            boxShadow: f.plus ? '0 1px 6px rgba(0,0,0,0.05)' : 'none',
          }}>
            <span style={{ fontSize: 20, width: 28, textAlign: 'center' }}>{f.icon}</span>
            <span style={{ fontSize: 14, fontWeight: 600, flex: 1, color: f.plus ? '#1a1a1a' : '#A0A0A8' }}>{f.label}</span>
            {f.plus
              ? <span style={{ fontSize: 11, fontWeight: 800, color, transition: 'color 280ms', letterSpacing: '0.05em' }}>PLUS</span>
              : <span style={{ fontSize: 11, fontWeight: 700, color: '#A0A0A8' }}>FREE</span>
            }
          </div>
        ))}
      </div>

      {/* Plan picker */}
      <div style={{ padding: '0 16px', display: 'flex', gap: 10, marginBottom: 14 }}>
        {[
          { key: 'month', label: 'Monthly', price: '$12.99/mo', badge: null },
          { key: 'year',  label: 'Annual',  price: '$79.99/yr', badge: 'Save 49%' },
        ].map(plan => (
          <div key={plan.key}
            onClick={() => setSelectedPlan(plan.key as 'month' | 'year')}
            style={{
              flex: 1, padding: '16px', borderRadius: 16, cursor: 'pointer',
              border: `2px solid ${selectedPlan === plan.key ? color : '#E0E0E4'}`,
              background: selectedPlan === plan.key ? `${color}0A` : '#fff',
              transition: 'all 220ms',
              position: 'relative', overflow: 'hidden',
            }}
          >
            {plan.badge && (
              <div style={{
                position: 'absolute', top: 0, right: 0,
                background: color, color: '#fff',
                fontSize: 10, fontWeight: 800, padding: '4px 8px',
                borderBottomLeftRadius: 10, letterSpacing: '0.04em',
                transition: 'background 280ms',
              }}>{plan.badge}</div>
            )}
            <div style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a', marginBottom: 4 }}>{plan.label}</div>
            <div style={{ fontSize: 18, fontWeight: 800, color: selectedPlan === plan.key ? color : '#1a1a1a', transition: 'color 280ms' }}>{plan.price}</div>
          </div>
        ))}
      </div>

      <div style={{ padding: '0 16px 40px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <button onClick={onUpgrade} style={{
          padding: '18px', background: color, color: '#fff',
          fontSize: 17, fontWeight: 700, borderRadius: 9999,
          border: 'none', cursor: 'pointer',
          boxShadow: `0 10px 32px ${color}44`,
          transition: 'background 280ms, box-shadow 280ms',
        }}>
          Start 3-Day Free Trial
        </button>
        <button onClick={onBack} style={{
          fontSize: 14, color: '#A0A0A8',
          background: 'none', border: 'none', cursor: 'pointer',
          fontWeight: 500, padding: '10px',
        }}>
          Continue with free plan
        </button>
      </div>
    </div>
  )
}

function NotificationsScreen({ onBack, color }: { onBack: () => void; color: string }) {
  const [toggles, setToggles] = useState({
    newMatch: true,
    newMessage: true,
    tripBoard: true,
    superMatch: true,
    destMatch: false,
  })

  const items: { key: keyof typeof toggles; label: string; sub: string }[] = [
    { key: 'newMatch',   label: 'New Match',              sub: 'Get notified when you match with someone' },
    { key: 'newMessage', label: 'New Message',            sub: 'Alerts for new messages from matches' },
    { key: 'tripBoard',  label: 'Trip Board Updates',     sub: 'Activity on trip boards you follow' },
    { key: 'superMatch', label: 'Super Match',            sub: 'When someone Super Matches you' },
    { key: 'destMatch',  label: 'Destination Match',      sub: 'When a new traveler shares your destination' },
  ]

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Notifications" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div style={{ background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          {items.map((item, i) => {
            const on = toggles[item.key]
            return (
              <div key={item.key} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '16px 20px',
                borderBottom: i < items.length - 1 ? '1px solid #F3F3F5' : 'none',
              }}>
                <div style={{ flex: 1, paddingRight: 16 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a' }}>{item.label}</div>
                  <div style={{ fontSize: 12, color: '#A0A0A8', marginTop: 2 }}>{item.sub}</div>
                </div>
                {/* iOS-style toggle */}
                <div
                  onClick={() => setToggles(t => ({ ...t, [item.key]: !t[item.key] }))}
                  style={{
                    width: 50, height: 30, borderRadius: 15,
                    background: on ? color : '#D8D8DC',
                    position: 'relative', cursor: 'pointer',
                    transition: 'background 280ms',
                    flexShrink: 0,
                  }}
                >
                  <div style={{
                    position: 'absolute', top: 3,
                    left: on ? 23 : 3,
                    width: 24, height: 24, borderRadius: '50%',
                    background: '#fff',
                    boxShadow: '0 1px 4px rgba(0,0,0,0.25)',
                    transition: 'left 220ms cubic-bezier(0.34,1.56,0.64,1)',
                  }} />
                </div>
              </div>
            )
          })}
        </div>

        <div style={{ padding: '12px 16px', background: `${color}10`, borderRadius: 14, border: `1px solid ${color}20` }}>
          <div style={{ fontSize: 13, color: '#737378', lineHeight: 1.5, transition: 'color 280ms' }}>
            You can also manage push notification settings in your device's Settings app.
          </div>
        </div>
      </div>
    </div>
  )
}

function HelpScreen({ onBack, color }: { onBack: () => void; color: string }) {
  const [openIndex, setOpenIndex] = useState<number | null>(null)

  const faqs = [
    {
      q: 'How does matching work?',
      a: 'Nomapal uses your destination overlap, travel dates, travel style, and interests to suggest compatible travel partners. Swipe right to like someone, and if they like you back it\'s a match! You can then message each other directly.',
    },
    {
      q: 'What is Same Dest!?',
      a: 'Same Dest! is our signature super-like feature. When you tap Same Dest! on someone\'s profile, they\'re instantly notified that you\'re heading to the same destination. You get 3 per day on the free plan, or unlimited with Nomapal+.',
    },
    {
      q: 'How do I verify my ID?',
      a: 'Go to Profile → Safety Center → Photo Verification. You\'ll be guided through a quick real-time selfie match against your ID. Verified profiles show a blue Verified Nomad badge and receive up to 3× more matches.',
    },
    {
      q: 'Can I hide my profile?',
      a: 'Yes. Go to Profile → Settings & Privacy and toggle "Hide my profile". While hidden, you won\'t appear in discovery but you can still chat with existing matches and browse profiles yourself.',
    },
    {
      q: 'How do I report someone?',
      a: 'Open the person\'s profile and tap the flag icon (⚑) in the top-right corner. Choose a reason and submit. Our Trust & Safety team reviews all reports within 24 hours. You can also block someone directly from the same menu.',
    },
  ]

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Help & Feedback" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 10 }}>

        {/* FAQ accordion */}
        <div style={{ background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          {faqs.map((faq, i) => {
            const isOpen = openIndex === i
            return (
              <div key={i} style={{ borderBottom: i < faqs.length - 1 ? '1px solid #F3F3F5' : 'none' }}>
                <button
                  onClick={() => setOpenIndex(isOpen ? null : i)}
                  style={{
                    width: '100%', padding: '16px 18px',
                    background: 'transparent', border: 'none',
                    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12,
                    textAlign: 'left',
                  }}
                >
                  <span style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a', flex: 1 }}>{faq.q}</span>
                  <motion.span
                    animate={{ rotate: isOpen ? 180 : 0 }}
                    transition={{ duration: 0.2 }}
                    style={{ fontSize: 14, color: '#A0A0A8', flexShrink: 0, display: 'inline-block' }}
                  >
                    ▾
                  </motion.span>
                </button>

                <AnimatePresence initial={false}>
                  {isOpen && (
                    <motion.div
                      key="answer"
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.22, ease: [0.16, 1, 0.3, 1] }}
                      style={{ overflow: 'hidden' }}
                    >
                      <div style={{ padding: '0 18px 16px', fontSize: 14, color: '#737378', lineHeight: 1.6, fontWeight: 400 }}>
                        {faq.a}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            )
          })}
        </div>

        {/* Contact Support button */}
        <div style={{ marginTop: 8 }}>
          <button
            onClick={() => {}}
            style={{
              width: '100%', padding: '18px',
              background: color, color: '#fff',
              fontSize: 16, fontWeight: 700, borderRadius: 9999,
              border: 'none', cursor: 'pointer',
              boxShadow: `0 8px 24px ${color}33`,
              transition: 'background 280ms, box-shadow 280ms',
            }}
          >
            Contact Support
          </button>
          <div style={{ textAlign: 'center', marginTop: 10, fontSize: 13, color: '#A0A0A8' }}>
            We typically reply within 24 hours
          </div>
        </div>
      </div>
    </div>
  )
}

function SafetyScreen({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Safety Center" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 10 }}>

        {/* Safety score */}
        <div style={{
          background: '#fff', borderRadius: 18, padding: '18px',
          boxShadow: '0 1px 8px rgba(0,0,0,0.05)',
          display: 'flex', alignItems: 'center', gap: 16,
        }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'linear-gradient(135deg, #44CC44, #0088FF)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 24, flexShrink: 0,
          }}>🛡</div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 800, color: '#1a1a1a', marginBottom: 3 }}>Safety Score: Good</div>
            <div style={{ fontSize: 13, color: '#A0A0A8' }}>Complete verification to reach Excellent</div>
          </div>
        </div>

        {[
          { emoji: '👁‍🗨', label: 'Photo Verification',     desc: 'Verify your profile with a real-time selfie match', cta: 'Start', ctaColor: color },
          { emoji: '📤', label: 'Emergency Share',         desc: 'Share live trip details with a trusted contact',      cta: 'Set Up', ctaColor: '#0088FF' },
          { emoji: '🚨', label: 'Panic Button',            desc: 'One-tap call to local emergency services',           cta: 'Configure', ctaColor: '#FF5A5F' },
          { emoji: '🚫', label: 'Block & Report',          desc: 'Block anyone and report concerning behavior',        cta: null, ctaColor: color },
          { emoji: '📚', label: 'Safety Tips',             desc: 'Expert guides for safe solo and group travel',       cta: 'Read', ctaColor: '#737378' },
        ].map(item => (
          <div key={item.label} style={{
            background: '#fff', borderRadius: 18, padding: '16px 18px',
            boxShadow: '0 1px 8px rgba(0,0,0,0.05)',
            display: 'flex', alignItems: 'center', gap: 14,
          }}>
            <div style={{
              width: 46, height: 46, borderRadius: 13,
              background: '#F3F3F5',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 22, flexShrink: 0,
            }}>
              {item.emoji}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 15, fontWeight: 700, color: '#1a1a1a', marginBottom: 3 }}>{item.label}</div>
              <div style={{ fontSize: 12, color: '#A0A0A8', lineHeight: 1.4 }}>{item.desc}</div>
            </div>
            {item.cta && (
              <button style={{
                padding: '8px 16px', background: item.ctaColor, color: '#fff',
                borderRadius: 9999, border: 'none', cursor: 'pointer',
                fontSize: 13, fontWeight: 700, flexShrink: 0,
                transition: 'background 280ms',
              }}>
                {item.cta}
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

/* ─── Legal Document Shared Components ────────────────────────────────────── */

function LegalDoc({ title, onBack, children, lastUpdated }: {
  title: string; onBack: () => void; children: React.ReactNode; lastUpdated: string
}) {
  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title={title} onBack={onBack} />
      <div style={{ padding: '16px 20px 48px' }}>
        <div style={{ fontSize: 12, color: '#A0A0A8', marginBottom: 20 }}>Last updated: {lastUpdated}</div>
        {children}
      </div>
    </div>
  )
}

function LegalSection({ heading, children }: { heading: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 24 }}>
      <div style={{ fontSize: 15, fontWeight: 800, color: '#1a1a1a', marginBottom: 8, letterSpacing: '-0.01em' }}>{heading}</div>
      <div style={{ fontSize: 14, color: '#4A4A52', lineHeight: 1.7, fontWeight: 400 }}>{children}</div>
    </div>
  )
}

/* ─── Privacy Policy ──────────────────────────────────────────────────────── */

function PrivacyPolicyScreen({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <LegalDoc title="Privacy Policy" onBack={onBack} lastUpdated="1 January 2026">
      <div style={{ background: `${color}0D`, border: `1px solid ${color}25`, borderRadius: 14, padding: '14px 16px', marginBottom: 24 }}>
        <div style={{ fontSize: 13, color: '#4A4A52', lineHeight: 1.6 }}>
          Nomapal Ltd. ("Nomapal", "we", "our", or "us") is committed to protecting your personal data. This Privacy Policy explains how we collect, use, disclose, and safeguard information when you use our application and services. It applies globally and complies with the <strong>EU General Data Protection Regulation (GDPR)</strong>, the <strong>UK GDPR</strong>, the <strong>California Consumer Privacy Act (CCPA/CPRA)</strong>, and other applicable data protection laws.
        </div>
      </div>

      <LegalSection heading="1. Data Controller">
        Nomapal Ltd. is the data controller for personal data processed under this policy.{'\n\n'}
        Registered address: 123 Nomad Street, London, EC1A 1AA, United Kingdom.{'\n'}
        Data Protection Officer: dpo@nomapal.com{'\n'}
        General enquiries: privacy@nomapal.com
      </LegalSection>

      <LegalSection heading="2. Information We Collect">
        <strong>Information you provide directly:</strong>{'\n'}
        • Account registration data: name, date of birth, phone number, email address, and password.{'\n'}
        • Profile information: photos, travel destinations, bio, interests, travel style preferences, job title, and other details you choose to add.{'\n'}
        • Communications: messages you exchange with other users through our platform.{'\n'}
        • Identity verification data: government-issued ID and facial images, processed solely for verification purposes.{'\n\n'}
        <strong>Information collected automatically:</strong>{'\n'}
        • Device identifiers (device ID, IP address, OS type and version, browser type).{'\n'}
        • Usage data (features accessed, swipe actions, session duration, clicks, and in-app activity).{'\n'}
        • Location data (approximate or precise, based on your consent settings).{'\n'}
        • Log data and crash reports.{'\n\n'}
        <strong>Information from third parties:</strong>{'\n'}
        • Social login providers (Google, Apple, Facebook) if you choose to connect them.{'\n'}
        • Spotify (anthem and top artists, if integrated).{'\n'}
        • Instagram (recent posts, if integrated).
      </LegalSection>

      <LegalSection heading="3. How We Use Your Information">
        We process your personal data on the following legal bases (GDPR Article 6):{'\n\n'}
        <strong>Contract performance (Art. 6(1)(b)):</strong>{'\n'}
        • Creating and managing your account.{'\n'}
        • Facilitating connections, matches, and messaging between users.{'\n'}
        • Processing subscription payments and providing premium features.{'\n\n'}
        <strong>Legitimate interests (Art. 6(1)(f)):</strong>{'\n'}
        • Improving and personalising the app experience.{'\n'}
        • Preventing fraud, abuse, and ensuring platform safety.{'\n'}
        • Internal analytics and business intelligence.{'\n\n'}
        <strong>Consent (Art. 6(1)(a)):</strong>{'\n'}
        • Sending marketing communications.{'\n'}
        • Processing location data for enhanced matching features.{'\n'}
        • Using analytics and advertising cookies.{'\n\n'}
        <strong>Legal obligation (Art. 6(1)(c)):</strong>{'\n'}
        • Complying with court orders, regulatory requirements, and law enforcement requests.
      </LegalSection>

      <LegalSection heading="4. Special Category Data">
        Certain information you provide — such as sexual orientation (for "Show Me" preferences) — may constitute special category data under GDPR Article 9. We process such data only on the basis of your explicit consent, which you may withdraw at any time in Privacy Preferences.
      </LegalSection>

      <LegalSection heading="5. Sharing Your Information">
        We do not sell your personal data. We may share information with:{'\n\n'}
        • <strong>Other users:</strong> profile information you choose to make visible.{'\n'}
        • <strong>Service providers:</strong> cloud hosting (AWS), payment processors (Stripe), analytics providers, and email delivery services — all bound by data processing agreements.{'\n'}
        • <strong>Verification partners:</strong> identity verification services, for the purpose of fraud prevention.{'\n'}
        • <strong>Law enforcement:</strong> where legally required or necessary to protect safety.{'\n'}
        • <strong>Business transfers:</strong> in the event of a merger, acquisition, or sale of assets, with advance notice to you.
      </LegalSection>

      <LegalSection heading="6. International Data Transfers">
        Your data may be transferred to and processed in countries outside the European Economic Area (EEA) and UK. Where we do so, we rely on Standard Contractual Clauses (SCCs) approved by the European Commission, adequacy decisions, or other lawful mechanisms under GDPR Chapter V.
      </LegalSection>

      <LegalSection heading="7. Data Retention">
        We retain your personal data only as long as necessary:{'\n\n'}
        • Active account data: retained for the duration of your account.{'\n'}
        • Deleted account data: most data deleted within 30 days of account deletion; certain data (e.g., safety reports, transaction records) may be retained for up to 7 years to comply with legal obligations.{'\n'}
        • Chat messages: deleted within 30 days of account deletion unless subject to a legal hold.
      </LegalSection>

      <LegalSection heading="8. Your Rights">
        Depending on your jurisdiction, you have the following rights:{'\n\n'}
        <strong>Under GDPR / UK GDPR (EEA & UK residents):</strong>{'\n'}
        • Right of access (Art. 15) — obtain a copy of your data.{'\n'}
        • Right to rectification (Art. 16) — correct inaccurate data.{'\n'}
        • Right to erasure (Art. 17) — "right to be forgotten".{'\n'}
        • Right to restriction (Art. 18) — limit processing in certain cases.{'\n'}
        • Right to data portability (Art. 20) — receive your data in a structured format.{'\n'}
        • Right to object (Art. 21) — object to processing based on legitimate interests or direct marketing.{'\n'}
        • Right to withdraw consent — at any time, without affecting prior lawful processing.{'\n\n'}
        <strong>Under CCPA/CPRA (California residents):</strong>{'\n'}
        • Right to know what personal information is collected and how it is used.{'\n'}
        • Right to delete personal information.{'\n'}
        • Right to opt-out of the sale or sharing of personal information.{'\n'}
        • Right to correct inaccurate personal information.{'\n'}
        • Right to limit use of sensitive personal information.{'\n'}
        • Right to non-discrimination for exercising your rights.{'\n\n'}
        To exercise any right, contact privacy@nomapal.com or use the in-app Privacy Preferences panel. We will respond within 30 days (GDPR) or 45 days (CCPA).
      </LegalSection>

      <LegalSection heading="9. Children's Privacy">
        Nomapal is not intended for use by individuals under the age of 18. We do not knowingly collect personal data from minors. If you believe a minor has provided us with personal data, contact privacy@nomapal.com and we will delete it promptly.
      </LegalSection>

      <LegalSection heading="10. Security">
        We implement technical and organisational measures to protect your data, including TLS encryption in transit, AES-256 encryption at rest, access controls, and regular security audits. No system is 100% secure; in the event of a data breach, we will notify affected users and relevant authorities as required by law.
      </LegalSection>

      <LegalSection heading="11. Changes to This Policy">
        We may update this Privacy Policy periodically. We will notify you of material changes via in-app notification or email at least 30 days before changes take effect. Continued use of Nomapal after the effective date constitutes acceptance of the updated policy.
      </LegalSection>

      <LegalSection heading="12. Contact & Complaints">
        Privacy enquiries: privacy@nomapal.com{'\n'}
        Data Protection Officer: dpo@nomapal.com{'\n\n'}
        If you are located in the EEA, you have the right to lodge a complaint with your local supervisory authority. UK residents may contact the Information Commissioner's Office (ICO) at ico.org.uk.
      </LegalSection>
    </LegalDoc>
  )
}

/* ─── Cookie Policy ───────────────────────────────────────────────────────── */

function CookiePolicyScreen({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <LegalDoc title="Cookie Policy" onBack={onBack} lastUpdated="1 January 2026">
      <LegalSection heading="1. What Are Cookies?">
        Cookies are small text files placed on your device when you visit a website or use an application. They are widely used to make services work efficiently, improve user experience, and provide information to service operators. Similar technologies — such as pixels, local storage, and device fingerprinting — may also be used and are covered by this policy.
      </LegalSection>

      <LegalSection heading="2. Cookies We Use">
        <strong>A. Strictly Necessary Cookies</strong>{'\n'}
        These cookies are essential for the app to function and cannot be disabled. They include authentication tokens, session management, and security cookies.{'\n'}
        Legal basis: Legitimate interest (GDPR Art. 6(1)(f)) / exempted from consent under ePrivacy Directive.{'\n\n'}
        <strong>B. Functional Cookies</strong>{'\n'}
        These cookies remember your preferences (e.g., language, display settings) to provide a personalised experience.{'\n'}
        Legal basis: Consent (GDPR Art. 6(1)(a)).{'\n\n'}
        <strong>C. Analytics Cookies</strong>{'\n'}
        We use analytics tools (including privacy-respecting first-party analytics) to understand how users interact with our app — which features are most used, session lengths, and error patterns. This data is aggregated and anonymised where possible.{'\n'}
        Legal basis: Consent (GDPR Art. 6(1)(a)).{'\n'}
        Providers may include: Google Analytics (with IP anonymisation), Mixpanel, Amplitude.{'\n\n'}
        <strong>D. Marketing & Advertising Cookies</strong>{'\n'}
        These cookies are used to show you relevant advertisements and measure ad effectiveness. They may be placed by third-party advertising partners.{'\n'}
        Legal basis: Consent (GDPR Art. 6(1)(a)).{'\n'}
        Providers may include: Meta Pixel, Google Ads, Snap Pixel.
      </LegalSection>

      <LegalSection heading="3. Third-Party Cookies">
        Some cookies are set by third-party services embedded in our app (e.g., Spotify, Instagram, payment processors). These third parties have their own privacy and cookie policies, which we encourage you to review. We are not responsible for the content or practices of these third parties.
      </LegalSection>

      <LegalSection heading="4. Cookie Duration">
        • <strong>Session cookies:</strong> deleted when you close the app or browser.{'\n'}
        • <strong>Persistent cookies:</strong> remain on your device for a set period (typically 30 days to 2 years) or until deleted manually.{'\n'}
        Specific expiry dates are available upon request at privacy@nomapal.com.
      </LegalSection>

      <LegalSection heading="5. Managing Your Cookie Preferences">
        You may manage your cookie preferences at any time through:{'\n\n'}
        • <strong>In-app Privacy Preferences:</strong> Settings → Privacy & Visibility → Privacy Preferences.{'\n'}
        • <strong>Device settings:</strong> most mobile operating systems allow you to limit ad tracking and clear app data.{'\n'}
        • <strong>Browser settings:</strong> if accessing Nomapal via a web browser, you can manage cookies through your browser's settings menu.{'\n\n'}
        Please note that disabling non-essential cookies may affect some app functionality and your experience.
      </LegalSection>

      <LegalSection heading="6. Consent & Withdrawal">
        We request your consent for non-essential cookies when you first launch the app. You may withdraw or update your consent at any time in Privacy Preferences without affecting the lawfulness of prior processing.{'\n\n'}
        For California residents (CCPA/CPRA): you may opt out of the "sharing" of personal information via cookies for cross-context behavioural advertising by using the "Limit the use of sensitive personal information" setting in Privacy Preferences.
      </LegalSection>

      <LegalSection heading="7. Contact">
        Cookie-related queries: privacy@nomapal.com{'\n'}
        Postal: Nomapal Ltd., 123 Nomad Street, London, EC1A 1AA, United Kingdom.
      </LegalSection>
    </LegalDoc>
  )
}

/* ─── Privacy Preferences ─────────────────────────────────────────────────── */

function PrivacyPreferencesScreen({ onBack, color }: { onBack: () => void; color: string }) {
  const [prefs, setPrefs] = useState({
    functional: true,
    analytics: false,
    marketing: false,
    thirdPartySharing: false,
    locationPrecise: false,
  })
  const [saved, setSaved] = useState(false)

  function toggle(k: keyof typeof prefs) {
    setPrefs(p => ({ ...p, [k]: !p[k] }))
    setSaved(false)
  }

  function save() {
    setSaved(true)
    setTimeout(() => setSaved(false), 2500)
  }

  const prefItems = [
    {
      key: 'functional' as const,
      label: 'Functional Cookies',
      sub: 'Remember your preferences (language, display settings) to personalise your experience.',
      required: false,
    },
    {
      key: 'analytics' as const,
      label: 'Analytics & Performance',
      sub: 'Help us understand how you use Nomapal so we can improve features and fix issues. Data is aggregated and anonymised.',
      required: false,
    },
    {
      key: 'marketing' as const,
      label: 'Marketing & Advertising',
      sub: 'Allow us and our advertising partners to show you relevant ads based on your interests on Nomapal and other platforms.',
      required: false,
    },
    {
      key: 'thirdPartySharing' as const,
      label: 'Third-Party Data Sharing',
      sub: 'Share aggregated, pseudonymised data with trusted research and analytics partners to improve travel matching globally.',
      required: false,
    },
    {
      key: 'locationPrecise' as const,
      label: 'Precise Location',
      sub: 'Allow Nomapal to access your precise GPS location for more accurate nearby traveller matching. You can use approximate location instead.',
      required: false,
    },
  ]

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', overflowY: 'auto' }} className="no-scrollbar">
      <SubHeader title="Privacy Preferences" onBack={onBack} />

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>

        {/* Info banner */}
        <div style={{ background: `${color}0D`, border: `1px solid ${color}25`, borderRadius: 14, padding: '14px 16px' }}>
          <div style={{ fontSize: 13, color: '#4A4A52', lineHeight: 1.6 }}>
            You are in control of your data. These settings let you choose what personal information Nomapal collects and how it is used. Strictly necessary cookies cannot be disabled — all others are optional. Your choices are saved and applied immediately.
          </div>
        </div>

        {/* Strictly Necessary — always on, cannot toggle */}
        <div style={{ background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ padding: '16px 18px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16 }}>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                <div style={{ fontSize: 15, fontWeight: 700, color: '#1a1a1a' }}>Strictly Necessary</div>
                <div style={{ fontSize: 10, fontWeight: 800, background: '#E8F5E9', color: '#2E7D32', padding: '2px 8px', borderRadius: 5, letterSpacing: '0.04em' }}>ALWAYS ON</div>
              </div>
              <div style={{ fontSize: 12, color: '#A0A0A8', lineHeight: 1.5 }}>
                Essential for the app to work. Includes authentication, security, and core features. These cannot be disabled.
              </div>
            </div>
            <div style={{ width: 50, height: 30, borderRadius: 15, background: '#44CC44', flexShrink: 0, position: 'relative' }}>
              <div style={{ position: 'absolute', top: 3, left: 23, width: 24, height: 24, borderRadius: '50%', background: '#fff', boxShadow: '0 1px 4px rgba(0,0,0,0.25)' }} />
            </div>
          </div>
        </div>

        {/* Optional toggles */}
        <div style={{ background: '#fff', borderRadius: 18, overflow: 'hidden', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}
          className="settings-section">
          {prefItems.map((item, i) => {
            const on = prefs[item.key]
            return (
              <div key={item.key} style={{
                padding: '16px 18px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16,
                borderBottom: i < prefItems.length - 1 ? '1px solid #F3F3F5' : 'none',
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a', marginBottom: 4 }}>{item.label}</div>
                  <div style={{ fontSize: 12, color: '#A0A0A8', lineHeight: 1.5 }}>{item.sub}</div>
                </div>
                <IOSToggle on={on} color={color} onChange={() => toggle(item.key)} />
              </div>
            )
          })}
        </div>

        {/* Your rights */}
        <div style={{ background: '#fff', borderRadius: 18, padding: '18px', boxShadow: '0 1px 8px rgba(0,0,0,0.05)' }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#1a1a1a', marginBottom: 10 }}>Your Data Rights</div>
          {[
            { label: 'Request a copy of your data', action: 'Data Export' },
            { label: 'Correct your personal data', action: 'Edit Profile' },
            { label: 'Delete your account and all data', action: 'Delete Account' },
          ].map(r => (
            <div key={r.label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 10, marginBottom: 10, borderBottom: '1px solid #F3F3F5' }}>
              <div style={{ fontSize: 13, color: '#4A4A52', flex: 1 }}>{r.label}</div>
              <button style={{ fontSize: 12, fontWeight: 700, color, background: `${color}0D`, border: `1px solid ${color}20`, borderRadius: 8, padding: '5px 12px', cursor: 'pointer' }}>
                {r.action}
              </button>
            </div>
          ))}
          <div style={{ fontSize: 11, color: '#A0A0A8', lineHeight: 1.5, borderTop: 'none', marginBottom: 0 }}>
            To submit a formal data request, contact privacy@nomapal.com. We will respond within 30 days (GDPR) or 45 days (CCPA). California residents may also opt out of data sharing under the CPRA.
          </div>
        </div>

        {/* Save button */}
        <button onClick={save} style={{
          padding: '18px', background: saved ? '#44CC44' : color, color: '#fff',
          fontSize: 16, fontWeight: 700, borderRadius: 9999,
          border: 'none', cursor: 'pointer',
          boxShadow: `0 8px 24px ${saved ? '#44CC44' : color}33`,
          transition: 'background 280ms, box-shadow 280ms',
        }}>
          {saved ? '✓ Preferences Saved' : 'Save Preferences'}
        </button>

        <div style={{ textAlign: 'center', fontSize: 12, color: '#A0A0A8', paddingBottom: 8 }}>
          You can update these preferences at any time.{'\n'}
          For more details, see our{' '}
          <span style={{ color, fontWeight: 600 }}>Privacy Policy</span>
          {' '}and{' '}
          <span style={{ color, fontWeight: 600 }}>Cookie Policy</span>.
        </div>
      </div>
    </div>
  )
}

/* ─── Terms of Service ────────────────────────────────────────────────────── */

function TermsScreen({ onBack, color }: { onBack: () => void; color: string }) {
  return (
    <LegalDoc title="Terms of Service" onBack={onBack} lastUpdated="1 January 2026">
      <div style={{ background: `${color}0D`, border: `1px solid ${color}25`, borderRadius: 14, padding: '14px 16px', marginBottom: 24 }}>
        <div style={{ fontSize: 13, color: '#4A4A52', lineHeight: 1.6 }}>
          Please read these Terms of Service ("Terms") carefully before using the Nomapal application. By creating an account or using Nomapal, you agree to be bound by these Terms. If you do not agree, do not use the service.
        </div>
      </div>

      <LegalSection heading="1. Acceptance of Terms">
        These Terms constitute a legally binding agreement between you and Nomapal Ltd. ("Nomapal", "we", "our", "us"), registered in England and Wales (Company No. 12345678), with its registered office at 123 Nomad Street, London, EC1A 1AA, United Kingdom.{'\n\n'}
        By accessing or using Nomapal — including our mobile application, website, and related services (collectively, the "Service") — you confirm that you have read, understood, and agree to these Terms and our Privacy Policy.
      </LegalSection>

      <LegalSection heading="2. Eligibility">
        You must be at least 18 years of age to use Nomapal. By using the Service, you represent and warrant that:{'\n\n'}
        • You are at least 18 years old.{'\n'}
        • You have the legal capacity to enter into a binding contract.{'\n'}
        • You are not prohibited from using the Service under applicable law.{'\n'}
        • You have not been convicted of a felony or sex crime, or any crime involving violence or dishonesty.{'\n\n'}
        We reserve the right to verify your age and identity at any time.
      </LegalSection>

      <LegalSection heading="3. Account Registration & Security">
        To use the Service, you must create an account. You agree to:{'\n\n'}
        • Provide accurate, current, and complete information during registration.{'\n'}
        • Maintain the security of your account credentials.{'\n'}
        • Notify us immediately of any unauthorised access at support@nomapal.com.{'\n'}
        • Not share your account with any third party.{'\n\n'}
        You are responsible for all activity that occurs under your account. We may suspend or terminate accounts that we determine, in our sole discretion, to have provided false information or to be in violation of these Terms.
      </LegalSection>

      <LegalSection heading="4. User Conduct">
        You agree not to use Nomapal to:{'\n\n'}
        • Harass, stalk, bully, or intimidate other users.{'\n'}
        • Post or transmit obscene, abusive, or harmful content.{'\n'}
        • Impersonate any person or entity, or misrepresent your identity or affiliation.{'\n'}
        • Solicit passwords, personal information, or financial information from other users.{'\n'}
        • Promote or engage in prostitution, escort services, or human trafficking.{'\n'}
        • Post content that is unlawful, defamatory, fraudulent, or infringes third-party rights.{'\n'}
        • Use automated means (bots, scrapers) to access or collect data from the Service.{'\n'}
        • Interfere with or disrupt the integrity or performance of the Service.{'\n'}
        • Circumvent any security or access control measures.{'\n\n'}
        Nomapal reserves the right to investigate violations and take any appropriate action, including removal of content, account suspension, and referral to law enforcement.
      </LegalSection>

      <LegalSection heading="5. Content & Intellectual Property">
        <strong>Your content:</strong> You retain ownership of content you submit (photos, text, messages). By posting content, you grant Nomapal a worldwide, non-exclusive, royalty-free, sublicensable licence to use, reproduce, modify, adapt, and display that content solely for the purpose of operating, improving, and promoting the Service.{'\n\n'}
        <strong>Nomapal's content:</strong> All intellectual property rights in the Service — including software, design, trademarks, and brand assets — belong to Nomapal or its licensors. You may not copy, modify, distribute, or create derivative works without our prior written consent.{'\n\n'}
        <strong>Feedback:</strong> Any feedback, suggestions, or ideas you provide about the Service may be used by Nomapal without obligation to you.
      </LegalSection>

      <LegalSection heading="6. Subscriptions & Payments">
        Nomapal offers free and paid subscription tiers (e.g., Nomapal+). By subscribing:{'\n\n'}
        • You authorise us to charge your payment method on a recurring basis at the then-current subscription rate.{'\n'}
        • Subscriptions auto-renew unless cancelled at least 24 hours before the renewal date.{'\n'}
        • Prices are inclusive of applicable taxes where required by law.{'\n'}
        • All charges are non-refundable except where required by applicable law (e.g., EU Consumer Rights Directive 14-day cooling-off period for digital services, where applicable).{'\n\n'}
        Subscription management and cancellation is available in Settings → Payment & Subscriptions, or via your App Store / Play Store account.
      </LegalSection>

      <LegalSection heading="7. Disclaimer of Warranties">
        THE SERVICE IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS, IMPLIED, OR STATUTORY. TO THE FULLEST EXTENT PERMITTED BY LAW, NOMAPAL DISCLAIMS ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.{'\n\n'}
        We do not guarantee that:{'\n'}
        • The Service will be uninterrupted, error-free, or free from harmful components.{'\n'}
        • Matches or connections made through the Service will result in safe or satisfactory real-world interactions.{'\n\n'}
        You acknowledge that any travel plans made as a result of connections on Nomapal are at your own risk.
      </LegalSection>

      <LegalSection heading="8. Limitation of Liability">
        TO THE FULLEST EXTENT PERMITTED BY LAW, NOMAPAL AND ITS OFFICERS, DIRECTORS, EMPLOYEES, AND AFFILIATES SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES ARISING FROM YOUR USE OF OR INABILITY TO USE THE SERVICE.{'\n\n'}
        Our total aggregate liability to you shall not exceed the greater of: (a) the amount you paid to Nomapal in the 12 months preceding the event giving rise to the claim, or (b) £100 / €100 / $100 (depending on your jurisdiction).{'\n\n'}
        Nothing in these Terms limits liability that cannot be limited by law, including liability for death or personal injury caused by negligence, or for fraud.
      </LegalSection>

      <LegalSection heading="9. Indemnification">
        You agree to defend, indemnify, and hold harmless Nomapal and its affiliates, officers, agents, employees, and licensors from and against any claims, damages, obligations, losses, liabilities, costs, or debt arising from: (a) your use of the Service; (b) your violation of these Terms; or (c) your violation of any third-party right, including intellectual property or privacy rights.
      </LegalSection>

      <LegalSection heading="10. Governing Law & Dispute Resolution">
        <strong>EU & UK residents:</strong> These Terms are governed by the laws of England and Wales. Disputes shall be subject to the exclusive jurisdiction of the courts of England and Wales, without prejudice to your rights as a consumer under local mandatory law.{'\n\n'}
        <strong>US residents:</strong> You agree to resolve disputes through binding individual arbitration under the rules of the American Arbitration Association (AAA), except that either party may bring claims in small claims court. You waive any right to participate in a class action.{'\n\n'}
        <strong>Australian residents:</strong> These Terms are subject to Australian Consumer Law. You may have non-excludable rights under that law which these Terms do not affect.{'\n\n'}
        <strong>All users:</strong> You may contact us to resolve any dispute informally before initiating formal proceedings. Contact: support@nomapal.com.
      </LegalSection>

      <LegalSection heading="11. Changes to These Terms">
        We may update these Terms from time to time. We will notify you of material changes via in-app notification or email at least 30 days before they take effect. Your continued use of the Service after the effective date constitutes acceptance of the updated Terms. If you do not agree to the updated Terms, you must stop using the Service.
      </LegalSection>

      <LegalSection heading="12. Termination">
        We may suspend or permanently terminate your account if you violate these Terms, without prior notice and without liability to you. Upon termination, your right to use the Service ceases immediately. Sections relating to intellectual property, disclaimers, limitation of liability, and governing law survive termination.{'\n\n'}
        You may delete your account at any time in Settings → Account Actions → Delete Account.
      </LegalSection>

      <LegalSection heading="13. Contact Us">
        Nomapal Ltd.{'\n'}
        123 Nomad Street, London, EC1A 1AA, United Kingdom{'\n'}
        Email: support@nomapal.com{'\n'}
        Legal: legal@nomapal.com{'\n\n'}
        For users in the EU, Nomapal's EU representative is available at eu-rep@nomapal.com.
      </LegalSection>
    </LegalDoc>
  )
}
