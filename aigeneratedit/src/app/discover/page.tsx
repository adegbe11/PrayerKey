'use client'

import { useState, useCallback, useEffect } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { SwipeCard } from '@/components/nomapal/SwipeCard'
import { MatchModal, type MatchType } from '@/components/nomapal/MatchModal'
import { MatchAnticipation } from '@/components/nomapal/MatchAnticipation'
import { ProfileExpand } from '@/components/nomapal/ProfileExpand'
import { useColor } from '@/components/nomapal/ColorProvider'
import { MOCK_PROFILES } from '@/lib/mockData'
import { useRouter } from 'next/navigation'
import type { TravelerProfile } from '@/lib/types'
import { useAuth } from '@/lib/auth-context'
import { getDiscoverProfiles, recordSwipe, saveToDreamCrew, type UserProfile } from '@/lib/db'

/** Map Firestore UserProfile to the UI's TravelerProfile shape */
function toUiProfile(p: UserProfile): TravelerProfile {
  return {
    id: p.uid,
    name: p.name,
    age: p.age,
    photos: p.photos?.length ? p.photos : ['https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&q=80'],
    location: p.location,
    destinations: p.destinations ?? [],
    travelDates: p.travelDates ?? '',
    travelStyle: (p.travelStyle ?? []) as any,
    travelGoal: (p.travelGoal ?? 'open') as any,
    bio: p.bio ?? '',
    interests: p.interests ?? [],
    budget: (p.budget ?? 'midrange') as any,
    languages: p.languages ?? [],
    height: p.height,
    education: p.education,
    job: p.job,
    verified: (p.verified ?? 1) as any,
    anthem: p.anthem,
    reputationScore: p.reputationScore ?? undefined,
    reputationCount: p.reputationCount ?? undefined,
    pronouns: p.pronouns,
    zodiac: p.zodiac,
    familyPlans: p.familyPlans,
    pets: p.pets,
    drinking: p.drinking,
    smoking: p.smoking,
    workout: p.workout,
    diet: p.diet,
    loveLanguage: p.loveLanguage,
    communicationStyle: p.communicationStyle,
    relationshipGoal: p.relationshipGoal,
    prompts: p.prompts,
  }
}

function detectMatchType(profile: TravelerProfile, isSwipeUp: boolean, myDests: string[]): {
  type: MatchType
  sharedDest?: string
} {
  const shared = myDests.find(ud =>
    profile.destinations.some(pd =>
      pd.toLowerCase().includes(ud.toLowerCase()) || ud.toLowerCase().includes(pd.toLowerCase())
    )
  )
  if (isSwipeUp) return { type: 'trip', sharedDest: shared ?? profile.destinations[0] }
  if (shared) return { type: 'destination', sharedDest: shared }
  return { type: 'travel' }
}

export default function DiscoverPage() {
  const { color, advance, retreat, triggerRainbow } = useColor()
  const router = useRouter()
  const { user, profile: myProfile } = useAuth()

  // Start with mock profiles for instant render, then overlay real ones
  const [profiles, setProfiles] = useState<TravelerProfile[]>([...MOCK_PROFILES])
  const [rawProfiles, setRawProfiles] = useState<UserProfile[]>([])
  const myDestinations = myProfile?.destinations ?? ['Bali', 'Tokyo', 'Santorini', 'Paris']

  useEffect(() => {
    if (!user) return
    getDiscoverProfiles(user.uid).then(realProfiles => {
      if (realProfiles.length > 0) {
        setRawProfiles(realProfiles)
        setProfiles(realProfiles.map(toUiProfile))
      }
      // else keep mock profiles for a good demo experience
    }).catch(() => { /* keep mock profiles */ })
  }, [user])
  const [match, setMatch] = useState<{ profile: TravelerProfile; type: MatchType; sharedDest?: string } | null>(null)
  const [anticipating, setAnticipating] = useState<TravelerProfile | null>(null)
  const [pendingMatch, setPendingMatch] = useState<{ profile: TravelerProfile; type: MatchType; sharedDest?: string } | null>(null)
  const [expanded, setExpanded] = useState<TravelerProfile | null>(null)
  const [dreamCrew, setDreamCrew] = useState<TravelerProfile[]>([])
  const [boostActive, setBoostActive] = useState(false)
  const [boostSeconds, setBoostSeconds] = useState(0)
  const [dreamToast, setDreamToast] = useState<string | null>(null)

  const [likeAnim, setLikeAnim] = useState(false)
  const [nopeAnim, setNopeAnim] = useState(false)
  const [superAnim, setSuperAnim] = useState(false)
  const [dreamAnim, setDreamAnim] = useState(false)

  const [lastSwiped, setLastSwiped] = useState<TravelerProfile | null>(null)
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [globalToast, setGlobalToast] = useState(false)
  const [filterState, setFilterState] = useState<{
    showMe: 'Everyone' | 'Men' | 'Women'
    budget: 'All' | 'Budget' | 'Mid-range' | 'Luxury'
    travelGoal: 'Friends' | 'Group' | 'Open'
  }>({ showMe: 'Everyone', budget: 'All', travelGoal: 'Open' })

  function triggerMatch(profile: TravelerProfile, isSwipeUp: boolean) {
    const { type, sharedDest } = detectMatchType(profile, isSwipeUp, myDestinations)
    setAnticipating(profile)
    setPendingMatch({ profile, type, sharedDest })
  }

  async function saveSwipe(toProfile: TravelerProfile, action: 'like' | 'nope' | 'superlike' | 'dreamcrew') {
    if (!user || !myProfile) return
    try {
      const rawTo = rawProfiles.find(r => r.uid === toProfile.id)
      if (rawTo) {
        await recordSwipe(user.uid, toProfile.id, action, myProfile, rawTo)
      }
    } catch { /* non-blocking */ }
  }

  function handleLike() {
    setLikeAnim(true)
    setTimeout(() => setLikeAnim(false), 300)
    advance()
    const top = profiles[0]
    setLastSwiped(top ?? null)
    if (top) {
      saveSwipe(top, 'like')
      if (Math.random() < 0.45) setTimeout(() => triggerMatch(top, false), 380)
    }
    setProfiles(prev => prev.slice(1))
  }

  function handleNope() {
    setNopeAnim(true)
    setTimeout(() => setNopeAnim(false), 300)
    retreat()
    const top = profiles[0]
    if (top) saveSwipe(top, 'nope')
    setLastSwiped(top ?? null)
    setProfiles(prev => prev.slice(1))
  }

  function handleSuperLike() {
    setSuperAnim(true)
    setTimeout(() => setSuperAnim(false), 300)
    advance()
    const top = profiles[0]
    setLastSwiped(top ?? null)
    if (top) {
      saveSwipe(top, 'superlike')
      setTimeout(() => triggerMatch(top, true), 380)
    }
    setProfiles(prev => prev.slice(1))
  }

  function handleAnticipationReveal() {
    setAnticipating(null)
    if (pendingMatch) {
      triggerRainbow()
      setMatch(pendingMatch)
      setPendingMatch(null)
    }
  }

  function handleDreamCrew() {
    setDreamAnim(true)
    setTimeout(() => setDreamAnim(false), 300)
    const top = profiles[0]
    if (top) {
      saveSwipe(top, 'dreamcrew')
      if (user) saveToDreamCrew(user.uid, top.id).catch(() => {})
      setDreamCrew(prev => [...prev, top])
      setDreamToast(top.name)
      setTimeout(() => setDreamToast(null), 2500)
    }
    setLastSwiped(profiles[0] ?? null)
    setProfiles(prev => prev.slice(1))
  }

  function handleBoost() {
    setBoostActive(true)
    setBoostSeconds(1800)
    const interval = setInterval(() => {
      setBoostSeconds(s => {
        if (s <= 1) { clearInterval(interval); setBoostActive(false); return 0 }
        return s - 1
      })
    }, 1000)
    advance()
  }

  function handleRewind() {
    if (!lastSwiped) return
    setProfiles(prev => [lastSwiped, ...prev])
    setLastSwiped(null)
    retreat()
  }

  function handleGoGlobal() {
    setProfiles([...MOCK_PROFILES])
    setGlobalToast(true)
    setTimeout(() => setGlobalToast(false), 2800)
  }

  const visibleProfiles = profiles.slice(0, 3)
  const empty = profiles.length === 0

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#0D0A1A', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

      {/* ── DISCO AURORA BACKGROUND ─────────────────────────────────────── */}
      {/* Base dark layer */}
      <div style={{ position: 'absolute', inset: 0, background: 'radial-gradient(ellipse at 50% 50%, #1A0A2E 0%, #0D0A1A 100%)', zIndex: 0 }} />
      {/* Animated rainbow orbs */}
      <motion.div
        style={{ position: 'absolute', inset: 0, zIndex: 0, pointerEvents: 'none' }}
        animate={{ filter: ['hue-rotate(0deg) brightness(1)', 'hue-rotate(360deg) brightness(1.15)', 'hue-rotate(0deg) brightness(1)'] }}
        transition={{ duration: 9, repeat: Infinity, ease: 'linear' }}
      >
        <div style={{ position: 'absolute', inset: 0, background: `
          radial-gradient(ellipse 280px 320px at 12% 18%, #FF5A5F88 0%, transparent 65%),
          radial-gradient(ellipse 240px 280px at 88% 22%, #9933CC88 0%, transparent 60%),
          radial-gradient(ellipse 300px 260px at 55% 78%, #0088FF88 0%, transparent 65%),
          radial-gradient(ellipse 220px 200px at 18% 82%, #44CC4488 0%, transparent 55%),
          radial-gradient(ellipse 200px 240px at 82% 72%, #FF880088 0%, transparent 55%),
          radial-gradient(ellipse 180px 160px at 45% 40%, #FFDD0055 0%, transparent 50%)
        `}} />
      </motion.div>
      {/* Slow drifting extra orbs */}
      <motion.div
        style={{ position: 'absolute', width: 260, height: 260, borderRadius: '50%', filter: 'blur(70px)', background: '#FF5A5F', opacity: 0.22, zIndex: 0, pointerEvents: 'none' }}
        animate={{ x: [0, 40, -20, 60, 0], y: [0, -30, 50, -10, 0] }}
        transition={{ duration: 14, repeat: Infinity, ease: 'easeInOut' }}
        initial={{ left: '5%', top: '10%' }}
      />
      <motion.div
        style={{ position: 'absolute', width: 220, height: 220, borderRadius: '50%', filter: 'blur(60px)', background: '#9933CC', opacity: 0.28, zIndex: 0, pointerEvents: 'none' }}
        animate={{ x: [0, -50, 30, -30, 0], y: [0, 40, -20, 30, 0] }}
        transition={{ duration: 11, repeat: Infinity, ease: 'easeInOut', delay: 2 }}
        initial={{ right: '5%', top: '15%' }}
      />
      <motion.div
        style={{ position: 'absolute', width: 280, height: 200, borderRadius: '50%', filter: 'blur(80px)', background: '#0088FF', opacity: 0.22, zIndex: 0, pointerEvents: 'none' }}
        animate={{ x: [0, 30, -40, 20, 0], y: [0, -25, 40, -15, 0] }}
        transition={{ duration: 16, repeat: Infinity, ease: 'easeInOut', delay: 4 }}
        initial={{ left: '25%', bottom: '10%' }}
      />

      {/* Top bar */}
      <div style={{
        position: 'relative', display: 'flex', alignItems: 'center',
        justifyContent: 'space-between',
        padding: 'max(var(--sat), 12px) 16px 6px',
        zIndex: 10,
      }}>
        {/* Left: filter button */}
        <button onClick={() => setFiltersOpen(true)} style={{
          width: 42, height: 42, borderRadius: '50%',
          background: 'rgba(255,255,255,0.08)', backdropFilter: 'blur(16px)',
          border: '1.5px solid rgba(255,255,255,0.18)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer',
          boxShadow: '0 2px 16px rgba(0,0,0,0.35)',
          flexShrink: 0,
        }}>
          <svg width="18" height="14" viewBox="0 0 18 14" fill="none">
            <rect y="0" width="18" height="2" rx="1" fill="rgba(255,255,255,0.85)" />
            <rect y="6" width="12" height="2" rx="1" fill="rgba(255,255,255,0.85)" />
            <rect y="12" width="7" height="2" rx="1" fill="rgba(255,255,255,0.85)" />
          </svg>
        </button>

        {/* Center: logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 30, height: 30, borderRadius: '50%', objectFit: 'cover', boxShadow: `0 0 14px ${color}99` }} />
          <span style={{ fontSize: 22, fontWeight: 900, letterSpacing: '-0.04em', color: '#fff', textShadow: `0 0 24px ${color}aa` }}>nomapal</span>
        </div>

        {/* Right: boost */}
        <button onClick={handleBoost} style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '8px 14px',
          background: boostActive ? color : 'rgba(255,255,255,0.08)',
          backdropFilter: 'blur(16px)',
          borderRadius: 9999,
          border: `1.5px solid ${boostActive ? color : 'rgba(255,255,255,0.18)'}`,
          cursor: 'pointer', fontSize: 13, fontWeight: 800,
          color: '#fff', letterSpacing: '0.01em',
          boxShadow: boostActive ? `0 4px 20px ${color}77` : '0 2px 12px rgba(0,0,0,0.3)',
          transition: 'all 280ms',
          flexShrink: 0,
        }}>
          <span style={{ fontSize: 15 }}>🔥</span>
          {boostActive ? `${Math.floor(boostSeconds / 60)}:${String(boostSeconds % 60).padStart(2, '0')}` : 'Boost'}
        </button>
      </div>

      {/* Card + action buttons — one unified visual block */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', margin: '4px 10px 8px', minHeight: 0 }}>

        {/* Card deck */}
        <div style={{ flex: 1, position: 'relative', zIndex: 5, minHeight: 0 }}>
          {empty ? (
            <div style={{
              position: 'absolute', inset: 0,
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              gap: 16, textAlign: 'center', padding: '32px',
            }}>
              <div style={{ fontSize: 64 }}>✈️</div>
              <div style={{ fontSize: 24, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
                You&apos;ve seen everyone nearby.
              </div>
              <div style={{ fontSize: 15, color: 'rgba(255,255,255,0.55)' }}>
                Expand your radius or turn on Global mode.
              </div>
              <button onClick={handleGoGlobal} style={{
                marginTop: 8, padding: '16px 28px', background: color, color: '#fff',
                borderRadius: 9999, border: 'none', cursor: 'pointer',
                fontSize: 16, fontWeight: 700, transition: 'background 280ms',
              }}>Go Global</button>
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
                  onDreamCrew={handleDreamCrew}
                  onExpand={() => isTop && setExpanded(profile)}
                  color={color}
                  userDestinations={myDestinations}
                />
              )
            })
          )}
        </div>

        {/* Action buttons — flush against card bottom, flat top corners */}
        {!empty && (
          <div style={{
            flexShrink: 0,
            background: 'rgba(13,10,26,0.88)',
            backdropFilter: 'blur(32px)',
            WebkitBackdropFilter: 'blur(32px)',
            borderRadius: '0 0 24px 24px',
            border: '1px solid rgba(255,255,255,0.10)',
            borderTop: 'none',
            padding: '16px 8px 14px',
            zIndex: 10,
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-evenly' }}>

              {/* Rewind */}
              <button onClick={handleRewind} style={{
                width: 56, height: 56, borderRadius: '50%',
                background: 'rgba(255,255,255,0.08)', border: '2.5px solid rgba(255,255,255,0.20)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                cursor: lastSwiped ? 'pointer' : 'not-allowed', fontSize: 22, color: '#fff',
                opacity: lastSwiped ? 1 : 0.3, transition: 'all 120ms',
              }}>↩</button>

              {/* PASS */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                <button onClick={handleNope} style={{
                  width: 80, height: 80, borderRadius: '50%',
                  background: nopeAnim ? 'rgba(255,90,95,0.30)' : 'rgba(255,90,95,0.12)',
                  border: `3px solid ${nopeAnim ? '#FF5A5F' : 'rgba(255,90,95,0.6)'}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                  transform: nopeAnim ? 'scale(0.92)' : 'scale(1)', transition: 'all 120ms',
                }}>
                  <span style={{ color: '#FF5A5F', fontWeight: 900, fontSize: 32 }}>✕</span>
                </button>
                <span style={{ fontSize: 9, fontWeight: 800, color: 'rgba(255,90,95,0.9)', letterSpacing: '0.06em', textTransform: 'uppercase' }}>Pass</span>
              </div>

              {/* SAME DEST */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                <button onClick={handleSuperLike} style={{
                  width: 68, height: 68, borderRadius: '50%',
                  background: superAnim ? 'rgba(0,136,255,0.30)' : 'rgba(0,136,255,0.12)',
                  border: `3px solid ${superAnim ? '#0088FF' : 'rgba(0,136,255,0.6)'}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                  transform: superAnim ? 'scale(0.92)' : 'scale(1)', transition: 'all 120ms',
                }}>
                  <span style={{ fontSize: 26 }}>✈️</span>
                </button>
                <span style={{ fontSize: 9, fontWeight: 800, color: 'rgba(0,136,255,0.95)', letterSpacing: '0.06em', textTransform: 'uppercase' }}>Same Dest!</span>
              </div>

              {/* WANDER */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                <button onClick={handleLike} style={{
                  width: 80, height: 80, borderRadius: '50%',
                  background: likeAnim ? `${color}33` : `${color}14`,
                  border: `3px solid ${likeAnim ? color : `${color}77`}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                  transform: likeAnim ? 'scale(0.92)' : 'scale(1)', transition: 'all 120ms',
                }}>
                  <span style={{ color, fontSize: 34 }}>♥</span>
                </button>
                <span style={{ fontSize: 9, fontWeight: 800, color, letterSpacing: '0.06em', textTransform: 'uppercase' }}>Wander</span>
              </div>

              {/* DREAM CREW */}
              <div style={{ position: 'relative' }}>
                <button onClick={handleDreamCrew} style={{
                  width: 56, height: 56, borderRadius: '50%',
                  background: dreamAnim ? 'rgba(153,51,204,0.30)' : 'rgba(153,51,204,0.12)',
                  border: `2.5px solid ${dreamAnim ? '#9933CC' : 'rgba(153,51,204,0.6)'}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                  transform: dreamAnim ? 'scale(0.92)' : 'scale(1)', transition: 'all 120ms',
                }}>
                  <span style={{ fontSize: 24 }}>🌙</span>
                </button>
                {dreamCrew.length > 0 && (
                  <div style={{
                    position: 'absolute', top: -4, right: -4, width: 20, height: 20, borderRadius: '50%',
                    background: '#9933CC', color: '#fff', fontSize: 10, fontWeight: 800,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>{dreamCrew.length}</div>
                )}
              </div>

            </div>
          </div>
        )}
      </div>

      {/* Dream Crew toast */}
      <AnimatePresence>
        {dreamToast && (
          <motion.div
            key="dream-toast"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            style={{
              position: 'absolute', bottom: 100, left: '50%', transform: 'translateX(-50%)',
              background: '#9933CC', color: '#fff', padding: '10px 20px',
              borderRadius: 9999, fontSize: 13, fontWeight: 700,
              boxShadow: '0 8px 24px rgba(153,51,204,0.4)', zIndex: 100,
              whiteSpace: 'nowrap',
            }}
          >
            🌙 {dreamToast} added to Dream Crew
          </motion.div>
        )}
      </AnimatePresence>

      {/* Global mode toast */}
      <AnimatePresence>
        {globalToast && (
          <motion.div
            key="global-toast"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            style={{
              position: 'absolute', bottom: 100, left: '50%', transform: 'translateX(-50%)',
              background: '#141414', color: '#fff', padding: '10px 20px',
              borderRadius: 9999, fontSize: 13, fontWeight: 700,
              boxShadow: '0 8px 24px rgba(0,0,0,0.28)', zIndex: 100,
              whiteSpace: 'nowrap',
            }}
          >
            🌍 Global mode unlocked
          </motion.div>
        )}
      </AnimatePresence>

      {/* Match anticipation — suspense before reveal */}
      <AnimatePresence>
        {anticipating && (
          <MatchAnticipation
            profile={anticipating}
            onReveal={handleAnticipationReveal}
          />
        )}
      </AnimatePresence>

      {/* Match modal */}
      <AnimatePresence>
        {match && (
          <MatchModal
            match={match.profile}
            matchType={match.type}
            sharedDestination={match.sharedDest}
            onMessage={() => { setMatch(null); router.push('/discover/messages') }}
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

      {/* Filters panel */}
      <AnimatePresence>
        {filtersOpen && (
          <>
            {/* Dark overlay */}
            <motion.div
              key="filter-overlay"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2 }}
              onClick={() => setFiltersOpen(false)}
              style={{
                position: 'fixed', inset: 0,
                background: 'rgba(0,0,0,0.5)',
                zIndex: 200,
              }}
            />

            {/* Slide-up panel */}
            <motion.div
              key="filter-panel"
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 28, stiffness: 300 }}
              style={{
                position: 'fixed', bottom: 0, left: 0, right: 0,
                background: '#fff',
                borderRadius: '24px 24px 0 0',
                padding: '0 24px 40px',
                zIndex: 201,
                boxShadow: '0 -8px 40px rgba(0,0,0,0.16)',
              }}
            >
              {/* Drag handle */}
              <div style={{ display: 'flex', justifyContent: 'center', padding: '12px 0 4px' }}>
                <div style={{ width: 40, height: 4, borderRadius: 2, background: '#e0e0e0' }} />
              </div>

              {/* Header */}
              <div style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '12px 0 24px',
              }}>
                <span style={{ fontSize: 20, fontWeight: 800, color: '#141414', letterSpacing: '-0.02em' }}>
                  Filters
                </span>
                <button
                  onClick={() => setFiltersOpen(false)}
                  style={{
                    width: 32, height: 32, borderRadius: '50%',
                    background: '#f5f5f5', border: 'none',
                    cursor: 'pointer', fontSize: 16,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}
                >✕</button>
              </div>

              {/* Show Me */}
              <div style={{ marginBottom: 28 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#737373', letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 12 }}>
                  Show Me
                </div>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                  {(['Everyone', 'Men', 'Women'] as const).map(opt => (
                    <button
                      key={opt}
                      onClick={() => setFilterState(f => ({ ...f, showMe: opt }))}
                      style={{
                        padding: '8px 18px', borderRadius: 9999,
                        border: `2px solid ${filterState.showMe === opt ? color : '#e0e0e0'}`,
                        background: filterState.showMe === opt ? color : '#fff',
                        color: filterState.showMe === opt ? '#fff' : '#3b3b3b',
                        fontSize: 13, fontWeight: 700, cursor: 'pointer', transition: 'all 180ms',
                      }}
                    >
                      {opt}
                    </button>
                  ))}
                </div>
              </div>

              {/* Budget */}
              <div style={{ marginBottom: 28 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#737373', letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 12 }}>
                  Budget
                </div>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                  {([
                    { val: 'All', label: 'All' },
                    { val: 'Budget', label: '🎒 Budget' },
                    { val: 'Mid-range', label: '✈️ Mid-range' },
                    { val: 'Luxury', label: '💎 Luxury' },
                  ] as const).map(({ val, label }) => (
                    <button
                      key={val}
                      onClick={() => setFilterState(f => ({ ...f, budget: val }))}
                      style={{
                        padding: '8px 18px', borderRadius: 9999,
                        border: `2px solid ${filterState.budget === val ? color : '#e0e0e0'}`,
                        background: filterState.budget === val ? color : '#fff',
                        color: filterState.budget === val ? '#fff' : '#3b3b3b',
                        fontSize: 13, fontWeight: 700, cursor: 'pointer', transition: 'all 180ms',
                      }}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Travel Goal */}
              <div style={{ marginBottom: 32 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#737373', letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 12 }}>
                  Travel Goal
                </div>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                  {([
                    { val: 'Friends', label: '👫 Friends' },
                    { val: 'Group', label: '👥 Group Trip' },
                    { val: 'Open', label: '🌍 Open' },
                  ] as const).map(({ val, label }) => (
                    <button
                      key={val}
                      onClick={() => setFilterState(f => ({ ...f, travelGoal: val }))}
                      style={{
                        padding: '8px 18px', borderRadius: 9999,
                        border: `2px solid ${filterState.travelGoal === val ? color : '#e0e0e0'}`,
                        background: filterState.travelGoal === val ? color : '#fff',
                        color: filterState.travelGoal === val ? '#fff' : '#3b3b3b',
                        fontSize: 13, fontWeight: 700, cursor: 'pointer', transition: 'all 180ms',
                      }}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Apply */}
              <button
                onClick={() => setFiltersOpen(false)}
                style={{
                  width: '100%', padding: '16px',
                  background: color, color: '#fff',
                  border: 'none', borderRadius: 14,
                  fontSize: 16, fontWeight: 800,
                  cursor: 'pointer', transition: 'background 280ms',
                  letterSpacing: '-0.01em',
                }}
              >
                Apply Filters
              </button>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}
