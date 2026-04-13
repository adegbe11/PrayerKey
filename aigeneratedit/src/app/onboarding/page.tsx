'use client'

import { useState, useEffect, useRef, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import type { TravelStyle, TravelGoal, BudgetTier, OnboardingState } from '@/lib/types'

const TOTAL_STEPS = 20

const TRAVEL_STYLES: { id: TravelStyle; emoji: string; label: string; desc: string }[] = [
  { id: 'adventure',  emoji: '✈️',  label: 'Adventure Seeker',   desc: 'Hikes, extreme sports, off the beaten path' },
  { id: 'beach',      emoji: '🌊',  label: 'Beach & Chill',       desc: 'Sun, sea, good vibes, slow days' },
  { id: 'culture',    emoji: '🏛',  label: 'Culture Hunter',      desc: 'Museums, history, local food, art' },
  { id: 'content',    emoji: '📸',  label: 'Content Creator',     desc: 'Always shooting, aesthetic-first' },
  { id: 'party',      emoji: '🎉',  label: 'Party Traveler',      desc: 'Nightlife, festivals, meet locals' },
  { id: 'luxury',     emoji: '💼',  label: 'Luxury Explorer',     desc: '5-star stays, fine dining, premium' },
  { id: 'budget',     emoji: '🎒',  label: 'Budget Backpacker',   desc: 'Hostels, cheap eats, max countries' },
  { id: 'wellness',   emoji: '🧘',  label: 'Wellness Wanderer',   desc: 'Retreats, yoga, slow travel, mindful' },
]

const INTERESTS = [
  'Photography','Hiking','Cooking','Music','Surfing','Yoga','Nightlife',
  'Art','History','Gaming','Fitness','Reading','Foodie','Dancing',
  'Volunteering','Languages','Wildlife','Architecture','Festivals','Diving',
]

const GENDERS = [
  'Man','Woman','Non-binary','Transgender man','Transgender woman',
  'Gender fluid','Agender','Prefer not to say',
]

const TRENDING = ['Tokyo','Bali','Paris','New York','Santorini','Cape Town','Bangkok']

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

const slideVariants = {
  enter:  { x: 60, opacity: 0 },
  center: { x: 0,  opacity: 1 },
  exit:   { x: -60, opacity: 0 },
}

function StepWrap({ children, step }: { children: React.ReactNode; step: number }) {
  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={step}
        variants={slideVariants}
        initial="enter"
        animate="center"
        exit="exit"
        transition={{ duration: 0.24, ease: [0.16, 1, 0.3, 1] }}
        style={{ position: 'absolute', inset: 0, overflowY: 'auto', overflowX: 'hidden' }}
        className="no-scrollbar"
      >
        {children}
      </motion.div>
    </AnimatePresence>
  )
}

export default function OnboardingPage() {
  const router = useRouter()
  const { color, advance, retreat, triggerRainbow, colorIndex, setIndex } = useColor()

  const [step, setStep] = useState(1)
  const [state, setState] = useState<OnboardingState>({
    step: 1,
    name: '',
    dob: { day: '', month: '', year: '' },
    gender: '',
    showGender: true,
    lookingFor: ['Everyone'],
    location: 'Barcelona, Spain',
    radius: 50,
    travelStyles: [],
    destinations: [],
    travelDates: [],
    budget: 'midrange',
    travelGoal: 'open',
    interests: [],
    photos: [],
    bio: '',
    isPremium: false,
  })

  const [otp, setOtp] = useState(['','','','','',''])
  const [phone, setPhone] = useState('')
  const [destInput, setDestInput] = useState('')
  const otpRefs = useRef<(HTMLInputElement | null)[]>([])

  // Advance color with each step forward
  useEffect(() => {
    setIndex((((step - 1) % 7)) as any)
  }, [step, setIndex])

  function goNext() {
    if (step < TOTAL_STEPS) {
      setStep(s => s + 1)
      advance()
    } else {
      triggerRainbow()
      sessionStorage.setItem('nomapal_onboarded', '1')
      setTimeout(() => router.push('/discover'), 700)
    }
  }

  function goBack() {
    if (step > 1) {
      setStep(s => s - 1)
      retreat()
    }
  }

  function update<K extends keyof OnboardingState>(key: K, val: OnboardingState[K]) {
    setState(prev => ({ ...prev, [key]: val }))
  }

  function toggleStyle(id: TravelStyle) {
    setState(prev => {
      const has = prev.travelStyles.includes(id)
      if (has) return { ...prev, travelStyles: prev.travelStyles.filter(s => s !== id) }
      if (prev.travelStyles.length >= 3) return prev
      return { ...prev, travelStyles: [...prev.travelStyles, id] }
    })
  }

  function toggleInterest(i: string) {
    setState(prev => {
      const has = prev.interests.includes(i)
      if (has) return { ...prev, interests: prev.interests.filter(x => x !== i) }
      if (prev.interests.length >= 5) return prev
      return { ...prev, interests: [...prev.interests, i] }
    })
  }

  function addDestination(dest: string) {
    if (!dest.trim() || state.destinations.length >= 5) return
    setState(prev => ({ ...prev, destinations: [...prev.destinations, dest.trim()] }))
    setDestInput('')
  }

  function otpChange(i: number, val: string) {
    if (!/^\d?$/.test(val)) return
    const next = [...otp]
    next[i] = val
    setOtp(next)
    if (val && i < 5) otpRefs.current[i + 1]?.focus()
  }

  const progress = (step / TOTAL_STEPS) * 100

  // ── Shared header ──────────────────────────────────────────────────────────
  function Header({ title, sub }: { title: string; sub?: string }) {
    return (
      <div style={{ padding: '20px 32px 0' }}>
        <div style={{ fontWeight: 800, fontSize: 28, letterSpacing: '-0.03em', color: '#141414', lineHeight: 1.2 }}>
          {title}
        </div>
        {sub && (
          <div style={{ marginTop: 8, fontSize: 15, color: '#737373', lineHeight: 1.5 }}>{sub}</div>
        )}
      </div>
    )
  }

  // ── CTA button ──────────────────────────────────────────────────────────────
  function CTA({ label = 'Continue', disabled = false, onClick }: { label?: string; disabled?: boolean; onClick?: () => void }) {
    return (
      <button
        onClick={onClick ?? goNext}
        disabled={disabled}
        style={{
          width: '100%',
          padding: '18px 24px',
          background: disabled ? '#d6d6d6' : color,
          color: disabled ? '#999' : '#fff',
          fontSize: 17,
          fontWeight: 700,
          borderRadius: 9999,
          border: 'none',
          cursor: disabled ? 'not-allowed' : 'pointer',
          transition: 'background 280ms cubic-bezier(0.16,1,0.3,1)',
          letterSpacing: '-0.01em',
        }}
      >
        {label}
      </button>
    )
  }

  // ──────────────────────────────────────────────────────────────────────────
  // STEP RENDERERS
  // ──────────────────────────────────────────────────────────────────────────

  function renderStep() {
    switch (step) {

      // ─── STEP 1: Welcome ─────────────────────────────────────────────────
      case 1: return (
        <StepWrap step={step}>
          <div style={{ height: '100%', display: 'flex', flexDirection: 'column', padding: '0 32px 40px' }}>
            {/* Background tint */}
            <div style={{
              position: 'absolute', inset: 0,
              background: `linear-gradient(160deg, ${color}18 0%, transparent 60%)`,
              transition: 'background 280ms',
              pointerEvents: 'none',
            }} />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 16, textAlign: 'center', position: 'relative' }}>
              {/* Logo */}
              <motion.div
                initial={{ scale: 0, y: -30 }}
                animate={{ scale: 1, y: 0 }}
                transition={{ type: 'spring', stiffness: 300, damping: 20, delay: 0.1 }}
                style={{
                  width: 88, height: 88, borderRadius: '50%',
                  background: color, display: 'flex', alignItems: 'center', justifyContent: 'center',
                  boxShadow: `0 16px 48px ${color}55`,
                  transition: 'background 280ms, box-shadow 280ms',
                  marginBottom: 8,
                }}
              >
                <span style={{ color: '#fff', fontSize: 42, fontWeight: 900, letterSpacing: '-0.04em' }}>N</span>
              </motion.div>

              <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25, duration: 0.4 }}>
                <div style={{ fontSize: 34, fontWeight: 800, letterSpacing: '-0.03em', color: '#141414', lineHeight: 1.1 }}>
                  The world is better<br />with company.
                </div>
                <div style={{ marginTop: 12, fontSize: 17, color: '#737373', fontWeight: 500 }}>
                  Find your perfect travel partner.
                </div>
              </motion.div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12, position: 'relative' }}>
              <button onClick={goNext} style={{
                padding: '18px', background: color, color: '#fff',
                fontSize: 16, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer',
                transition: 'background 280ms', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              }}>
                <span>📱</span> Continue with Phone
              </button>
              <button onClick={goNext} style={{
                padding: '18px', background: '#141414', color: '#fff',
                fontSize: 16, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              }}>
                <span>🍎</span> Continue with Apple
              </button>
              <button onClick={goNext} style={{
                padding: '18px', background: '#fff', color: '#141414',
                fontSize: 16, fontWeight: 700, borderRadius: 9999, border: '2px solid #ebebeb', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              }}>
                <span>🌐</span> Continue with Google
              </button>
              <div style={{ textAlign: 'center', marginTop: 4 }}>
                <span style={{ fontSize: 14, color: '#999' }}>Already have an account? </span>
                <button style={{ fontSize: 14, fontWeight: 600, color: color, background: 'none', border: 'none', cursor: 'pointer', transition: 'color 280ms' }}>Log in</button>
              </div>
            </div>
          </div>
        </StepWrap>
      )

      // ─── STEP 2: Phone verification ──────────────────────────────────────
      case 2: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header title="What's your number?" sub="We'll send a 6-digit code. Standard rates may apply." />

            <div style={{ flex: 1, padding: '0 0' }}>
              <div style={{ display: 'flex', gap: 8, marginBottom: 32 }}>
                <div style={{
                  padding: '18px 14px', background: '#f5f5f5', borderRadius: 12, border: '2px solid #d6d6d6',
                  fontSize: 15, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap',
                }}>
                  🇺🇸 +1
                </div>
                <input
                  type="tel"
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  placeholder="Phone number"
                  style={{
                    flex: 1, padding: '18px 20px', background: '#f5f5f5', borderRadius: 12,
                    border: `2px solid ${phone ? color : '#d6d6d6'}`, fontSize: 16, fontWeight: 500,
                    outline: 'none', transition: 'border-color 280ms', color: '#141414',
                  }}
                />
              </div>

              <div style={{ textAlign: 'center', marginBottom: 20, fontSize: 14, color: '#999' }}>— or verify with email —</div>

              <input
                type="email"
                placeholder="Email address"
                style={{
                  width: '100%', padding: '18px 20px', background: '#f5f5f5', borderRadius: 12,
                  border: '2px solid #d6d6d6', fontSize: 16, fontWeight: 500, outline: 'none', color: '#141414',
                }}
              />
            </div>

            <CTA label="Send Code" />
          </div>
        </StepWrap>
      )

      // ─── STEP 3: OTP ─────────────────────────────────────────────────────
      case 3: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header title="Enter the code" sub="We sent a 6-digit code to +1 (555) 000-0000" />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 40, paddingTop: 40 }}>
              <div style={{ display: 'flex', gap: 10 }}>
                {otp.map((digit, i) => (
                  <input
                    key={i}
                    ref={el => { otpRefs.current[i] = el }}
                    className="otp-box"
                    value={digit}
                    onChange={e => otpChange(i, e.target.value)}
                    maxLength={1}
                    inputMode="numeric"
                    style={{ '--color-active': color } as any}
                  />
                ))}
              </div>
              <button style={{ fontSize: 14, color: color, background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600, transition: 'color 280ms' }}>
                Resend code
              </button>
            </div>

            <CTA label="Verify" disabled={otp.join('').length < 6} />
          </div>
        </StepWrap>
      )

      // ─── STEP 4: Name ─────────────────────────────────────────────────────
      case 4: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header title="What's your first name?" sub="This is how you'll appear on Nomapal. You can't change this later." />

            <div style={{ flex: 1, paddingTop: 20 }}>
              <input
                type="text"
                value={state.name}
                onChange={e => update('name', e.target.value.slice(0, 30))}
                placeholder="First name"
                autoFocus
                style={{
                  width: '100%', padding: '22px 24px', background: '#f5f5f5',
                  borderRadius: 16, border: `2px solid ${state.name ? color : '#d6d6d6'}`,
                  fontSize: 22, fontWeight: 600, outline: 'none', color: '#141414',
                  transition: 'border-color 280ms',
                }}
              />
              <div style={{ marginTop: 8, fontSize: 13, color: '#999', textAlign: 'right' }}>
                {state.name.length}/30
              </div>
            </div>

            <CTA disabled={state.name.trim().length < 2} />
          </div>
        </StepWrap>
      )

      // ─── STEP 5: Date of birth ────────────────────────────────────────────
      case 5: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header title="When were you born?" sub="Your age will be shown on your profile." />

            <div style={{ flex: 1, paddingTop: 20 }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
                {[
                  { label: 'Day',   key: 'day',   placeholder: 'DD',   max: 2 },
                  { label: 'Month', key: 'month', placeholder: 'MM',   max: 2 },
                  { label: 'Year',  key: 'year',  placeholder: 'YYYY', max: 4 },
                ].map(field => (
                  <div key={field.key}>
                    <div style={{ fontSize: 12, fontWeight: 600, color: '#999', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{field.label}</div>
                    <input
                      type="number"
                      value={state.dob[field.key as keyof typeof state.dob]}
                      onChange={e => update('dob', { ...state.dob, [field.key]: e.target.value.slice(0, field.max) })}
                      placeholder={field.placeholder}
                      style={{
                        width: '100%', padding: '18px 12px', background: '#f5f5f5',
                        borderRadius: 12, border: '2px solid #d6d6d6', fontSize: 18,
                        fontWeight: 700, outline: 'none', color: '#141414', textAlign: 'center',
                      }}
                    />
                  </div>
                ))}
              </div>

              {state.dob.day && state.dob.month && state.dob.year && (
                <motion.div
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  style={{ marginTop: 20, padding: '14px 20px', background: `${color}12`, borderRadius: 12, fontSize: 15, fontWeight: 600, color }}
                >
                  You are {2026 - parseInt(state.dob.year)} years old
                </motion.div>
              )}
            </div>

            <CTA disabled={!state.dob.day || !state.dob.month || !state.dob.year} />
          </div>
        </StepWrap>
      )

      // ─── STEP 6: Gender ───────────────────────────────────────────────────
      case 6: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="How do you identify?" />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 10, overflowY: 'auto' }} className="no-scrollbar">
              {GENDERS.map(g => (
                <button
                  key={g}
                  onClick={() => update('gender', g)}
                  style={{
                    padding: '18px 20px', borderRadius: 14, border: `2px solid ${state.gender === g ? color : '#ebebeb'}`,
                    background: state.gender === g ? `${color}08` : '#fff',
                    fontSize: 16, fontWeight: 600, color: state.gender === g ? color : '#3b3b3b',
                    cursor: 'pointer', textAlign: 'left', transition: 'all 280ms',
                  }}
                >
                  {g}
                </button>
              ))}

              <div style={{ marginTop: 8, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 4px' }}>
                <span style={{ fontSize: 14, color: '#737373' }}>Show my gender on profile</span>
                <div
                  onClick={() => update('showGender', !state.showGender)}
                  style={{
                    width: 48, height: 28, borderRadius: 14,
                    background: state.showGender ? color : '#d6d6d6',
                    position: 'relative', cursor: 'pointer', transition: 'background 280ms',
                  }}
                >
                  <div style={{
                    position: 'absolute', top: 2, left: state.showGender ? 22 : 2,
                    width: 24, height: 24, borderRadius: '50%', background: '#fff',
                    boxShadow: '0 1px 4px rgba(0,0,0,0.2)',
                    transition: 'left 240ms cubic-bezier(0.34,1.56,0.64,1)',
                  }} />
                </div>
              </div>
            </div>

            <CTA disabled={!state.gender} />
          </div>
        </StepWrap>
      )

      // ─── STEP 7: Who do you want to meet ──────────────────────────────────
      case 7: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Who do you want to travel with?" sub="You can change this anytime in settings." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 12 }}>
              {['Men','Women','Everyone','Non-binary people'].map(opt => {
                const sel = state.lookingFor.includes(opt)
                return (
                  <button
                    key={opt}
                    onClick={() => {
                      setState(prev => {
                        const has = prev.lookingFor.includes(opt)
                        return { ...prev, lookingFor: has ? prev.lookingFor.filter(x => x !== opt) : [...prev.lookingFor, opt] }
                      })
                    }}
                    style={{
                      padding: '20px', borderRadius: 14,
                      border: `2px solid ${sel ? color : '#ebebeb'}`,
                      background: sel ? `${color}08` : '#fff',
                      fontSize: 17, fontWeight: 700, color: sel ? color : '#3b3b3b',
                      cursor: 'pointer', textAlign: 'left', transition: 'all 280ms',
                      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    }}
                  >
                    {opt}
                    {sel && <span style={{ fontSize: 20 }}>✓</span>}
                  </button>
                )
              })}
            </div>

            <CTA disabled={state.lookingFor.length === 0} />
          </div>
        </StepWrap>
      )

      // ─── STEP 8: Location ─────────────────────────────────────────────────
      case 8: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Where are you now?" sub="Your distance will be visible to other travelers." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
              <button style={{
                padding: '18px 20px', background: `${color}12`, borderRadius: 14,
                border: `2px solid ${color}`, fontSize: 16, fontWeight: 600, color,
                cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 12, transition: 'all 280ms',
              }}>
                <span style={{ fontSize: 20 }}>📍</span>
                Use my current location
              </button>

              <div style={{ fontSize: 13, fontWeight: 600, color: '#999', textAlign: 'center', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                or search manually
              </div>

              <input
                type="text"
                value={state.location}
                onChange={e => update('location', e.target.value)}
                placeholder="Search city or country"
                style={{
                  width: '100%', padding: '18px 20px', background: '#f5f5f5',
                  borderRadius: 14, border: `2px solid ${state.location ? color : '#d6d6d6'}`,
                  fontSize: 16, fontWeight: 500, outline: 'none', color: '#141414', transition: 'border-color 280ms',
                }}
              />

              {/* Map placeholder */}
              <div style={{
                height: 160, borderRadius: 16, overflow: 'hidden',
                background: 'linear-gradient(135deg, #e8f4f8 0%, #d4edda 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                position: 'relative', border: '2px solid #ebebeb',
              }}>
                <div style={{ position: 'absolute', inset: 0, opacity: 0.3 }}>
                  {/* Stylized map grid */}
                  {Array.from({ length: 6 }).map((_, i) => (
                    <div key={i} style={{
                      position: 'absolute', left: 0, right: 0,
                      top: `${(i / 6) * 100}%`, height: 1, background: '#0088FF22',
                    }} />
                  ))}
                  {Array.from({ length: 8 }).map((_, i) => (
                    <div key={i} style={{
                      position: 'absolute', top: 0, bottom: 0,
                      left: `${(i / 8) * 100}%`, width: 1, background: '#0088FF22',
                    }} />
                  ))}
                </div>
                <div style={{ textAlign: 'center', position: 'relative' }}>
                  <div style={{ fontSize: 28, marginBottom: 4 }}>📍</div>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#3b3b3b' }}>{state.location || 'Your Location'}</div>
                </div>
              </div>

              {/* Radius slider */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                  <span style={{ fontSize: 14, fontWeight: 600, color: '#737373' }}>Match radius</span>
                  <span style={{ fontSize: 14, fontWeight: 700, color }}>{state.radius} km</span>
                </div>
                <input
                  type="range" min="1" max="100" value={state.radius}
                  onChange={e => update('radius', parseInt(e.target.value))}
                  style={{ width: '100%', accentColor: color }}
                />
              </div>
            </div>

            <CTA />
          </div>
        </StepWrap>
      )

      // ─── STEP 9: Travel Style ─────────────────────────────────────────────
      case 9: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="What's your travel style?" sub="Pick up to 3 that describe you best." />

            <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, overflowY: 'auto', alignContent: 'start' }} className="no-scrollbar">
              {TRAVEL_STYLES.map(ts => {
                const sel = state.travelStyles.includes(ts.id)
                const disabled = !sel && state.travelStyles.length >= 3
                return (
                  <button
                    key={ts.id}
                    onClick={() => !disabled && toggleStyle(ts.id)}
                    style={{
                      padding: '16px 14px', borderRadius: 14,
                      border: `2px solid ${sel ? color : '#ebebeb'}`,
                      background: sel ? `${color}08` : '#fff',
                      cursor: disabled ? 'not-allowed' : 'pointer',
                      textAlign: 'left', opacity: disabled ? 0.45 : 1,
                      transition: 'all 280ms',
                    }}
                  >
                    <div style={{ fontSize: 24, marginBottom: 6 }}>{ts.emoji}</div>
                    <div style={{ fontSize: 13, fontWeight: 700, color: sel ? color : '#262626', marginBottom: 3 }}>{ts.label}</div>
                    <div style={{ fontSize: 11, color: '#999', lineHeight: 1.3 }}>{ts.desc}</div>
                  </button>
                )
              })}
            </div>

            <CTA disabled={state.travelStyles.length === 0} />
          </div>
        </StepWrap>
      )

      // ─── STEP 10: Destinations ────────────────────────────────────────────
      case 10: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Where do you want to go?" sub="Add up to 5 destinations." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 16, overflowY: 'auto' }} className="no-scrollbar">
              <div style={{ display: 'flex', gap: 8 }}>
                <input
                  value={destInput}
                  onChange={e => setDestInput(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && addDestination(destInput)}
                  placeholder="Search destination..."
                  style={{
                    flex: 1, padding: '16px 18px', background: '#f5f5f5',
                    borderRadius: 12, border: `2px solid ${destInput ? color : '#d6d6d6'}`,
                    fontSize: 15, fontWeight: 500, outline: 'none', color: '#141414', transition: 'border-color 280ms',
                  }}
                />
                <button
                  onClick={() => addDestination(destInput)}
                  style={{
                    padding: '16px 20px', background: color, color: '#fff',
                    borderRadius: 12, border: 'none', cursor: 'pointer',
                    fontSize: 20, fontWeight: 700, transition: 'background 280ms',
                  }}
                >+</button>
              </div>

              {state.destinations.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {state.destinations.map(d => (
                    <div key={d} style={{
                      display: 'flex', alignItems: 'center', gap: 8,
                      padding: '8px 14px', background: `${color}12`,
                      borderRadius: 9999, border: `2px solid ${color}`,
                    }}>
                      <span style={{ fontSize: 14, fontWeight: 700, color }}>✈ {d}</span>
                      <button
                        onClick={() => setState(prev => ({ ...prev, destinations: prev.destinations.filter(x => x !== d) }))}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', color, fontSize: 16, lineHeight: 1 }}
                      >×</button>
                    </div>
                  ))}
                </div>
              )}

              <div style={{ fontSize: 13, fontWeight: 600, color: '#999', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                Trending destinations
              </div>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {TRENDING.map(dest => (
                  <button
                    key={dest}
                    onClick={() => addDestination(dest)}
                    style={{
                      padding: '8px 14px', borderRadius: 9999,
                      border: '2px solid #ebebeb', background: '#fff',
                      fontSize: 13, fontWeight: 600, color: '#3b3b3b',
                      cursor: 'pointer',
                    }}
                  >
                    {dest}
                  </button>
                ))}
              </div>

              <div style={{ marginTop: 4 }}>
                <button
                  onClick={goNext}
                  style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500 }}
                >
                  Not sure yet — show me everyone
                </button>
              </div>
            </div>

            <CTA disabled={state.destinations.length === 0} />
          </div>
        </StepWrap>
      )

      // ─── STEP 11: Travel dates ────────────────────────────────────────────
      case 11: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="When are you traveling?" sub="Set your travel window. You can add multiple trips." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
              <div style={{ background: '#f5f5f5', borderRadius: 16, padding: '20px', border: `2px solid ${color}` }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 16 }}>Trip 1</div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <div>
                    <div style={{ fontSize: 12, color: '#999', marginBottom: 6, fontWeight: 600 }}>FROM</div>
                    <input type="date" style={{ width: '100%', padding: '12px', borderRadius: 10, border: '2px solid #d6d6d6', fontSize: 14, fontWeight: 600, outline: 'none', background: '#fff', accentColor: color }} />
                  </div>
                  <div>
                    <div style={{ fontSize: 12, color: '#999', marginBottom: 6, fontWeight: 600 }}>TO</div>
                    <input type="date" style={{ width: '100%', padding: '12px', borderRadius: 10, border: '2px solid #d6d6d6', fontSize: 14, fontWeight: 600, outline: 'none', background: '#fff', accentColor: color }} />
                  </div>
                </div>
              </div>

              <button style={{
                padding: '16px', borderRadius: 12, border: `2px dashed ${color}`,
                background: 'transparent', fontSize: 14, fontWeight: 700, color,
                cursor: 'pointer', transition: 'all 280ms',
              }}>
                + Add another trip window
              </button>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '4px 0' }}>
                <span style={{ fontSize: 14, color: '#737373', fontWeight: 500 }}>Flexible on exact dates</span>
                <div style={{ width: 48, height: 28, borderRadius: 14, background: '#d6d6d6', position: 'relative', cursor: 'pointer' }}>
                  <div style={{ position: 'absolute', top: 2, left: 2, width: 24, height: 24, borderRadius: '50%', background: '#fff', boxShadow: '0 1px 4px rgba(0,0,0,0.2)' }} />
                </div>
              </div>
            </div>

            <CTA />
          </div>
        </StepWrap>
      )

      // ─── STEP 12: Budget ──────────────────────────────────────────────────
      case 12: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="What's your travel budget?" sub="We use this to find compatible travel partners." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 16 }}>
              {([
                { id: 'budget',      label: 'Budget',      desc: '$0–$50/day',      emoji: '🎒' },
                { id: 'midrange',    label: 'Mid-range',   desc: '$50–$150/day',    emoji: '✈️' },
                { id: 'comfortable', label: 'Comfortable', desc: '$150–$300/day',   emoji: '🏨' },
                { id: 'luxury',      label: 'Luxury',      desc: '$300+/day',       emoji: '💎' },
              ] as const).map(tier => {
                const sel = state.budget === tier.id
                return (
                  <button
                    key={tier.id}
                    onClick={() => update('budget', tier.id)}
                    style={{
                      padding: '18px 20px', borderRadius: 14,
                      border: `2px solid ${sel ? color : '#ebebeb'}`,
                      background: sel ? `${color}08` : '#fff',
                      cursor: 'pointer', textAlign: 'left',
                      display: 'flex', alignItems: 'center', gap: 14, transition: 'all 280ms',
                    }}
                  >
                    <span style={{ fontSize: 24 }}>{tier.emoji}</span>
                    <div>
                      <div style={{ fontSize: 16, fontWeight: 700, color: sel ? color : '#262626' }}>{tier.label}</div>
                      <div style={{ fontSize: 13, color: '#999', marginTop: 2 }}>{tier.desc}</div>
                    </div>
                    {sel && <span style={{ marginLeft: 'auto', color, fontSize: 20 }}>✓</span>}
                  </button>
                )
              })}
            </div>

            <CTA />
          </div>
        </StepWrap>
      )

      // ─── STEP 13: Travel goal ─────────────────────────────────────────────
      case 13: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="What are you looking for?" />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 14 }}>
              {([
                { id: 'friends', emoji: '🤝', label: 'Travel Friends',    desc: 'Someone to explore with, no strings' },
                { id: 'group',   emoji: '👥', label: 'Group Adventure',   desc: 'Join or build a travel group' },
                { id: 'open',    emoji: '💫', label: 'Open to Anything',  desc: 'See what happens' },
              ] as const).map(goal => {
                const sel = state.travelGoal === goal.id
                return (
                  <button
                    key={goal.id}
                    onClick={() => update('travelGoal', goal.id)}
                    style={{
                      padding: '22px 20px', borderRadius: 16,
                      border: `2px solid ${sel ? color : '#ebebeb'}`,
                      background: sel ? `${color}08` : '#fff',
                      cursor: 'pointer', textAlign: 'left',
                      display: 'flex', alignItems: 'center', gap: 16, transition: 'all 280ms',
                    }}
                  >
                    <span style={{ fontSize: 32 }}>{goal.emoji}</span>
                    <div>
                      <div style={{ fontSize: 17, fontWeight: 700, color: sel ? color : '#262626' }}>{goal.label}</div>
                      <div style={{ fontSize: 13, color: '#999', marginTop: 3 }}>{goal.desc}</div>
                    </div>
                  </button>
                )
              })}
            </div>

            <CTA />
          </div>
        </StepWrap>
      )

      // ─── STEP 14: Interests ───────────────────────────────────────────────
      case 14: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Your interests" sub={`Pick up to 5. ${state.interests.length}/5 selected.`} />

            <div style={{ flex: 1, display: 'flex', flexWrap: 'wrap', gap: 10, alignContent: 'start', overflowY: 'auto' }} className="no-scrollbar">
              {INTERESTS.map(interest => {
                const sel = state.interests.includes(interest)
                const disabled = !sel && state.interests.length >= 5
                return (
                  <button
                    key={interest}
                    onClick={() => !disabled && toggleInterest(interest)}
                    className="chip"
                    style={{
                      borderColor: sel ? color : '#d6d6d6',
                      color: sel ? color : '#3b3b3b',
                      background: sel ? `${color}08` : '#fff',
                      opacity: disabled ? 0.4 : 1,
                      cursor: disabled ? 'not-allowed' : 'pointer',
                    } as any}
                  >
                    {interest}
                  </button>
                )
              })}
            </div>

            <CTA disabled={state.interests.length === 0} />
          </div>
        </StepWrap>
      )

      // ─── STEP 15: Photos ──────────────────────────────────────────────────
      case 15: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Add your photos" sub="Start with your best photo. Add up to 9." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 16 }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
                {Array.from({ length: 9 }).map((_, i) => (
                  <div
                    key={i}
                    style={{
                      aspectRatio: '3/4', borderRadius: 12,
                      border: `2px dashed ${i === 0 ? color : '#d6d6d6'}`,
                      background: '#f5f5f5', display: 'flex',
                      alignItems: 'center', justifyContent: 'center',
                      cursor: 'pointer', position: 'relative',
                      fontSize: i === 0 ? 28 : 22,
                    }}
                  >
                    {i === 0 ? (
                      <div style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: 28, marginBottom: 4 }}>📷</div>
                        <div style={{ fontSize: 11, fontWeight: 700, color, textTransform: 'uppercase', letterSpacing: '0.04em' }}>Required</div>
                      </div>
                    ) : (
                      <span style={{ color: '#d6d6d6', fontSize: 28 }}>+</span>
                    )}
                  </div>
                ))}
              </div>

              <div style={{ padding: '14px 16px', background: '#f5f5f5', borderRadius: 12 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#3b3b3b', marginBottom: 6 }}>Photo tips</div>
                {['Clear face', 'Recent photo', 'No group shots as first photo'].map(tip => (
                  <div key={tip} style={{ fontSize: 13, color: '#737373', display: 'flex', gap: 6, marginBottom: 3 }}>
                    <span style={{ color }}>✓</span> {tip}
                  </div>
                ))}
              </div>
            </div>

            <CTA label="Continue" />
          </div>
        </StepWrap>
      )

      // ─── STEP 16: Bio ─────────────────────────────────────────────────────
      case 16: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Write your bio" sub="Tell travelers why you're the perfect companion." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div style={{ position: 'relative' }}>
                <textarea
                  value={state.bio}
                  onChange={e => update('bio', e.target.value.slice(0, 500))}
                  placeholder="I travel because..."
                  rows={5}
                  style={{
                    width: '100%', padding: '18px 20px', background: '#f5f5f5',
                    borderRadius: 14, border: `2px solid ${state.bio ? color : '#d6d6d6'}`,
                    fontSize: 16, fontWeight: 500, outline: 'none', color: '#141414',
                    resize: 'none', lineHeight: 1.6, transition: 'border-color 280ms',
                  }}
                />
                <div style={{
                  position: 'absolute', bottom: 12, right: 16,
                  fontSize: 12, fontWeight: 700,
                  color: state.bio.length > 450 ? '#FF4444' : '#999',
                }}>
                  {state.bio.length}/500
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Prompts</div>
                {[
                  'Ask me about my best trip to...',
                  'My travel style is...',
                  'The most underrated destination I\'ve been to is...',
                  'I travel because...',
                ].map(prompt => (
                  <button
                    key={prompt}
                    onClick={() => update('bio', state.bio + (state.bio ? ' ' : '') + prompt)}
                    style={{
                      padding: '10px 14px', borderRadius: 10, border: '2px solid #ebebeb',
                      background: '#fff', fontSize: 13, fontWeight: 500, color: '#737373',
                      cursor: 'pointer', textAlign: 'left',
                    }}
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            </div>

            <CTA label="Continue" />
          </div>
        </StepWrap>
      )

      // ─── STEP 17: Spotify ─────────────────────────────────────────────────
      case 17: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Add your travel anthem" sub="The song that plays when you pack your bag." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 24 }}>
              <div style={{
                width: 120, height: 120, borderRadius: '50%',
                background: 'linear-gradient(135deg, #1DB954, #169C45)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                boxShadow: '0 16px 48px rgba(29,185,84,0.4)',
              }}>
                <span style={{ fontSize: 48 }}>🎵</span>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 18, fontWeight: 700, color: '#141414', marginBottom: 6 }}>Connect Spotify</div>
                <div style={{ fontSize: 14, color: '#737373' }}>Show your travel anthem on your profile</div>
              </div>
              <button style={{
                padding: '18px 40px', background: '#1DB954', color: '#fff',
                fontSize: 16, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer',
                boxShadow: '0 8px 24px rgba(29,185,84,0.3)',
              }}>
                Connect Spotify
              </button>
            </div>

            <div style={{ textAlign: 'center' }}>
              <button onClick={goNext} style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500 }}>
                Skip for now
              </button>
            </div>
          </div>
        </StepWrap>
      )

      // ─── STEP 18: Verified Nomad ──────────────────────────────────────────
      case 18: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <Header title="Become a Verified Nomad" sub="Verified profiles get 3× more matches." />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 12 }}>
              {[
                { level: 1, label: 'Email verified',     done: true,  action: null },
                { level: 2, label: 'Phone verified',     done: true,  action: null },
                { level: 3, label: 'ID verification',    done: false, action: 'Start now' },
                { level: 4, label: 'Community reviews',  done: false, action: 'Unlocked after first trip' },
              ].map(item => (
                <div
                  key={item.level}
                  style={{
                    padding: '16px 20px', borderRadius: 14,
                    border: `2px solid ${item.done ? color : '#ebebeb'}`,
                    background: item.done ? `${color}08` : '#f5f5f5',
                    display: 'flex', alignItems: 'center', gap: 14,
                  }}
                >
                  <div style={{
                    width: 36, height: 36, borderRadius: '50%',
                    background: item.done ? color : '#d6d6d6',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 14, fontWeight: 800, color: '#fff', flexShrink: 0,
                    transition: 'background 280ms',
                  }}>
                    {item.done ? '✓' : item.level}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 15, fontWeight: 700, color: item.done ? color : '#3b3b3b', transition: 'color 280ms' }}>
                      Level {item.level} — {item.label}
                    </div>
                  </div>
                  {item.action && !item.done && (
                    <button style={{
                      padding: '8px 14px', background: color, color: '#fff',
                      borderRadius: 8, border: 'none', cursor: 'pointer',
                      fontSize: 12, fontWeight: 700, whiteSpace: 'nowrap', transition: 'background 280ms',
                    }}>
                      {item.action}
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <CTA label="Start ID Verification" />
              <button onClick={goNext} style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500, padding: '8px' }}>
                Skip for now
              </button>
            </div>
          </div>
        </StepWrap>
      )

      // ─── STEP 19: Notifications ───────────────────────────────────────────
      case 19: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 24, height: '100%' }}>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 28, textAlign: 'center' }}>
              <div style={{ fontSize: 80 }}>🔔</div>
              <div>
                <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: '-0.03em', color: '#141414', marginBottom: 12 }}>
                  Don't miss your match.
                </div>
                <div style={{ fontSize: 16, color: '#737373', lineHeight: 1.6 }}>
                  We'll let you know when someone's going your way.
                </div>
              </div>

              <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 12 }}>
                {['New match', 'New message', 'Super Match received', 'Trip board updates'].map(notif => (
                  <div key={notif} style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    padding: '14px 18px', background: '#f5f5f5', borderRadius: 12,
                  }}>
                    <span style={{ fontSize: 14, fontWeight: 600, color: '#3b3b3b' }}>{notif}</span>
                    <div style={{ width: 40, height: 24, borderRadius: 12, background: color, position: 'relative', transition: 'background 280ms' }}>
                      <div style={{ position: 'absolute', top: 2, right: 2, width: 20, height: 20, borderRadius: '50%', background: '#fff' }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <CTA label="Turn on Notifications" />
              <button onClick={goNext} style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500, padding: '8px' }}>
                Maybe later
              </button>
            </div>
          </div>
        </StepWrap>
      )

      // ─── STEP 20: Nomapal+ upsell ─────────────────────────────────────────
      case 20: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 20, height: '100%', overflowY: 'auto' }} className="no-scrollbar">
            <div style={{ textAlign: 'center' }}>
              <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '6px 16px', background: `${color}12`, borderRadius: 9999, marginBottom: 12 }}>
                <span style={{ fontSize: 14, fontWeight: 800, color, textTransform: 'uppercase', letterSpacing: '0.06em' }}>✦ Nomapal+</span>
              </div>
              <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: '-0.02em', color: '#141414', lineHeight: 1.2 }}>
                Travel further,<br />match better.
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {[
                { label: 'See who liked you',    free: false },
                { label: 'Unlimited Super Matches', free: false },
                { label: 'Global mode',          free: false },
                { label: 'Advanced filters',     free: false },
                { label: 'Rewind last swipe',    free: false },
                { label: 'Profile Boost',        free: false },
                { label: 'Swipe',                free: true  },
                { label: 'Match & message',      free: true  },
                { label: 'Trip Boards',          free: true  },
              ].map(feature => (
                <div key={feature.label} style={{
                  display: 'flex', alignItems: 'center', gap: 12,
                  padding: '13px 16px', borderRadius: 12,
                  background: feature.free ? '#f5f5f5' : `${color}08`,
                  border: `1.5px solid ${feature.free ? '#ebebeb' : `${color}30`}`,
                }}>
                  <span style={{ fontSize: 16, color: feature.free ? '#44CC44' : color }}>
                    {feature.free ? '✓' : '⭐'}
                  </span>
                  <span style={{ fontSize: 14, fontWeight: 600, color: feature.free ? '#737373' : '#262626' }}>
                    {feature.label}
                  </span>
                  {!feature.free && (
                    <span style={{ marginLeft: 'auto', fontSize: 11, fontWeight: 700, color, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                      PLUS
                    </span>
                  )}
                </div>
              ))}
            </div>

            <div style={{ background: `${color}12`, borderRadius: 16, padding: '20px', border: `2px solid ${color}30` }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <div>
                  <div style={{ fontSize: 22, fontWeight: 800, color: '#141414' }}>$12.99<span style={{ fontSize: 14, fontWeight: 500, color: '#999' }}>/month</span></div>
                  <div style={{ fontSize: 13, color: '#737373' }}>Or $79.99/year — save 49%</div>
                </div>
                <div style={{
                  padding: '6px 12px', background: color, color: '#fff',
                  borderRadius: 8, fontSize: 12, fontWeight: 700,
                  transition: 'background 280ms',
                }}>
                  BEST VALUE
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <button
                onClick={goNext}
                style={{
                  padding: '18px', background: color, color: '#fff',
                  fontSize: 17, fontWeight: 700, borderRadius: 9999, border: 'none', cursor: 'pointer',
                  transition: 'background 280ms',
                }}
              >
                Start 3-Day Free Trial
              </button>
              <button
                onClick={goNext}
                style={{ fontSize: 14, color: '#999', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500, padding: '8px' }}
              >
                Continue with free
              </button>
            </div>
          </div>
        </StepWrap>
      )

      default: return null
    }
  }

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', display: 'flex', flexDirection: 'column' }}>
      {/* Progress bar */}
      {step > 1 && step < 20 && (
        <div style={{ position: 'absolute', top: 0, left: 0, right: 0, zIndex: 50 }}>
          <div className="progress-track" style={{ borderRadius: 0 }}>
            <div className="progress-fill" style={{ width: `${progress}%` }} />
          </div>
        </div>
      )}

      {/* Back button */}
      {step > 1 && step <= 19 && (
        <button
          onClick={goBack}
          style={{
            position: 'absolute', top: 16, left: 20, zIndex: 60,
            width: 40, height: 40, borderRadius: '50%',
            background: '#f5f5f5', border: 'none', cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 18, color: '#3b3b3b',
          }}
        >
          ←
        </button>
      )}

      {/* Step content */}
      <div style={{ position: 'relative', flex: 1, overflow: 'hidden' }}>
        {renderStep()}
      </div>
    </div>
  )
}
