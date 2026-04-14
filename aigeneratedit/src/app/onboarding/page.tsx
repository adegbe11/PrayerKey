'use client'

import { useState, useEffect, useRef, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { PhotoUpload } from '@/components/nomapal/PhotoUpload'
import type { TravelStyle, TravelGoal, BudgetTier, OnboardingState } from '@/lib/types'
import {
  RecaptchaVerifier,
  signInWithPhoneNumber,
  signInWithPopup,
  GoogleAuthProvider,
  type ConfirmationResult,
} from 'firebase/auth'
import { auth } from '@/lib/firebase'
import { saveUserProfile } from '@/lib/db'
import { uploadPhoto } from '@/lib/uploadPhoto'

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

const ALL_DESTINATIONS = [
  // A
  'Abu Dhabi, UAE','Accra, Ghana','Adelaide, Australia','Agra, India','Aix-en-Provence, France',
  'Algarve, Portugal','Alicante, Spain','Almaty, Kazakhstan','Amsterdam, Netherlands','Anchorage, USA',
  'Andorra','Angeles City, Philippines','Ankara, Turkey','Antwerp, Belgium','Aruba',
  'Aspen, USA','Athens, Greece','Auckland, New Zealand','Austin, USA',
  // B
  'Baku, Azerbaijan','Bali, Indonesia','Bangkok, Thailand','Barcelona, Spain','Bath, UK',
  'Beijing, China','Beirut, Lebanon','Belgrade, Serbia','Berlin, Germany','Bogotá, Colombia',
  'Bologna, Italy','Bordeaux, France','Brisbane, Australia','Brussels, Belgium','Bucharest, Romania',
  'Budapest, Hungary','Buenos Aires, Argentina',
  // C
  'Cairo, Egypt','Calgary, Canada','Cancún, Mexico','Cape Town, South Africa','Cartagena, Colombia',
  'Casablanca, Morocco','Chiang Mai, Thailand','Chicago, USA','Colombo, Sri Lanka','Copenhagen, Denmark',
  'Córdoba, Argentina','Costa Rica','Crete, Greece','Croatia','Cusco, Peru',
  // D
  'Dakar, Senegal','Dallas, USA','Dar es Salaam, Tanzania','Delhi, India','Denver, USA',
  'Dhaka, Bangladesh','Doha, Qatar','Dubai, UAE','Dublin, Ireland','Dubrovnik, Croatia',
  // E
  'Edinburgh, UK','El Salvador','Essaouira, Morocco',
  // F
  'Florence, Italy','Fort Lauderdale, USA','Frankfurt, Germany','Fuerteventura, Spain',
  // G
  'Geneva, Switzerland','Genoa, Italy','Georgetown, Malaysia','Glasgow, UK','Granada, Spain',
  'Guatemala City, Guatemala',
  // H
  'Hamburg, Germany','Hanoi, Vietnam','Havana, Cuba','Helsinki, Finland','Ho Chi Minh City, Vietnam',
  'Hong Kong','Honolulu, USA',
  // I
  'Ibiza, Spain','Istanbul, Turkey',
  // J
  'Jakarta, Indonesia','Jaipur, India','Johannesburg, South Africa',
  // K
  'Kathmandu, Nepal','Kigali, Rwanda','Kraków, Poland','Kuala Lumpur, Malaysia','Kyoto, Japan',
  // L
  'Lagos, Nigeria','Lahore, Pakistan','Las Vegas, USA','Lhasa, Tibet','Libson, Portugal',
  'Lima, Peru','Lisbon, Portugal','Ljubljana, Slovenia','London, UK','Los Angeles, USA',
  'Luanda, Angola','Lusaka, Zambia','Luxembourg City, Luxembourg',
  // M
  'Madrid, Spain','Málaga, Spain','Male, Maldives','Marrakech, Morocco','Marseille, France',
  'Medellín, Colombia','Melbourne, Australia','Mexico City, Mexico','Miami, USA','Milan, Italy',
  'Minneapolis, USA','Monaco','Montevideo, Uruguay','Montreal, Canada','Moscow, Russia','Mumbai, India',
  'Munich, Germany','Muscat, Oman',
  // N
  'Nairobi, Kenya','Naples, Italy','Nashville, USA','New Delhi, India','New Orleans, USA',
  'New York, USA','Nice, France','Nicosia, Cyprus','Ningbo, China','Nur-Sultan, Kazakhstan',
  // O
  'Osaka, Japan','Oslo, Norway','Ottawa, Canada',
  // P
  'Palermo, Italy','Palma de Mallorca, Spain','Panama City, Panama','Paris, France','Penang, Malaysia',
  'Perth, Australia','Phnom Penh, Cambodia','Phuket, Thailand','Porto, Portugal','Prague, Czech Republic',
  // Q
  'Queenstown, New Zealand','Quito, Ecuador',
  // R
  'Reykjavik, Iceland','Riga, Latvia','Rio de Janeiro, Brazil','Rome, Italy','Rotterdam, Netherlands',
  // S
  'San Francisco, USA','San José, Costa Rica','San Juan, Puerto Rico','Santiago, Chile',
  'Santorini, Greece','São Paulo, Brazil','Sarajevo, Bosnia','Seattle, USA','Seoul, South Korea',
  'Seville, Spain','Shanghai, China','Singapore','Sofia, Bulgaria','Stockholm, Sweden','Sydney, Australia',
  // T
  'Taipei, Taiwan','Tallinn, Estonia','Tashkent, Uzbekistan','Tel Aviv, Israel','Thessaloniki, Greece',
  'Tokyo, Japan','Toronto, Canada','Tunis, Tunisia',
  // U
  'Ulaanbaatar, Mongolia','Utrecht, Netherlands',
  // V
  'Valencia, Spain','Vancouver, Canada','Venice, Italy','Vienna, Austria','Vilnius, Lithuania',
  // W
  'Warsaw, Poland','Washington DC, USA',
  // Y
  'Yerevan, Armenia',
  // Z
  'Zagreb, Croatia','Zanzibar, Tanzania','Zurich, Switzerland',
]

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

const SPECTRUM = ['#FF5A5F','#FF8800','#FFDD00','#44CC44','#0088FF','#3344CC','#9933CC']

// ─── Orbit rings ────────────────────────────────────────────────────────────
// Outer ring — 8 photos, radius 118px, slow clockwise
const ORBIT_OUTER = [
  { photo: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80', ci: 0 },
  { photo: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200&q=80', ci: 1 },
  { photo: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80', ci: 2 },
  { photo: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&q=80', ci: 3 },
  { photo: 'https://images.unsplash.com/photo-1552058544-f2b08422138a?w=200&q=80', ci: 4 },
  { photo: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&q=80', ci: 5 },
  { photo: 'https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=200&q=80', ci: 6 },
  { photo: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80', ci: 0 },
]
// Inner ring — 5 photos, radius 68px, slow counter-clockwise
const ORBIT_INNER = [
  { photo: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&q=80', ci: 3 },
  { photo: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&q=80', ci: 5 },
  { photo: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&q=80', ci: 1 },
  { photo: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&q=80', ci: 6 },
  { photo: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80', ci: 2 },
]

function OrbitRing({
  photos, radius, size, duration, clockwise, borderSize = 3,
}: {
  photos: typeof ORBIT_OUTER
  radius: number
  size: number
  duration: number
  clockwise: boolean
  borderSize?: number
}) {
  const count = photos.length
  return (
    <motion.div
      style={{
        position: 'absolute',
        width: radius * 2,
        height: radius * 2,
        borderRadius: '50%',
        border: '1px solid rgba(0,0,0,0.06)',
      }}
      animate={{ rotate: clockwise ? 360 : -360 }}
      transition={{ duration, repeat: Infinity, ease: 'linear' }}
    >
      {photos.map((p, i) => {
        const angleDeg = (360 / count) * i - 90 // start from top
        const angleRad = (angleDeg * Math.PI) / 180
        const x = radius + radius * Math.cos(angleRad) - size / 2
        const y = radius + radius * Math.sin(angleRad) - size / 2
        return (
          <motion.div
            key={i}
            style={{
              position: 'absolute',
              left: x,
              top: y,
              width: size,
              height: size,
              borderRadius: '50%',
              overflow: 'hidden',
              border: `${borderSize}px solid ${SPECTRUM[p.ci]}`,
              boxShadow: `0 4px 16px ${SPECTRUM[p.ci]}55`,
            }}
            // counter-rotate so faces stay upright
            animate={{ rotate: clockwise ? -360 : 360 }}
            transition={{ duration, repeat: Infinity, ease: 'linear' }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={p.photo} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover', pointerEvents: 'none' }} draggable={false} />
          </motion.div>
        )
      })}
    </motion.div>
  )
}

function WelcomeScreen({ color, onNext }: { color: string; onNext: () => void }) {
  return (
    <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', background: '#FAFAFA', overflow: 'hidden' }}>

      {/* ── Background gradient wash ── */}
      <div style={{
        position: 'absolute', inset: 0,
        background: `radial-gradient(ellipse 90% 55% at 50% 0%, ${color}20 0%, transparent 65%)`,
        transition: 'background 280ms',
        pointerEvents: 'none',
      }} />

      {/* ── Orbit zone ── */}
      <div style={{
        flex: '0 0 auto',
        paddingTop: 'max(var(--sat), 20px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        height: 300, position: 'relative',
      }}>
        {/* Outer ring: 8 photos, r=118, clockwise, 28s */}
        <OrbitRing photos={ORBIT_OUTER} radius={118} size={60} duration={28} clockwise borderSize={3} />
        {/* Inner ring: 5 photos, r=68, counter-clockwise, 20s */}
        <OrbitRing photos={ORBIT_INNER} radius={68} size={52} duration={20} clockwise={false} borderSize={3} />
        {/* Center app icon */}
        <div style={{
          position: 'absolute', width: 56, height: 56, borderRadius: '50%',
          overflow: 'hidden', border: '3px solid #fff',
          boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          zIndex: 2,
        }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
      </div>

      {/* ── Separator gradient ── */}
      <div style={{ height: 20, background: 'linear-gradient(to bottom, transparent, #FAFAFA)', flexShrink: 0 }} />

      {/* ── Text + buttons (bottom 50%) ── */}
      <div style={{ flex: 1, padding: '0 28px 36px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>

        {/* Wordmark */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.35 }}
          style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 28, height: 28, borderRadius: '50%', objectFit: 'cover' }} />
          <span style={{ fontSize: 18, fontWeight: 900, letterSpacing: '-0.04em', color, transition: 'color 280ms' }}>nomapal</span>
        </motion.div>

        {/* Headline */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3, duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          style={{ marginBottom: 10 }}
        >
          <div style={{
            fontSize: 36, fontWeight: 900, letterSpacing: '-0.03em',
            lineHeight: 1.1, color: '#141414',
          }}>
            Find Your Perfect
          </div>
          <div style={{ fontSize: 36, fontWeight: 900, letterSpacing: '-0.03em', lineHeight: 1.1 }}>
            <span style={{ color, transition: 'color 280ms' }}>Travel </span>
            <span style={{ color: '#141414' }}>Partner.</span>
          </div>
        </motion.div>

        {/* Subtitle */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.42, duration: 0.4 }}
          style={{ fontSize: 15, color: '#737373', marginBottom: 28, lineHeight: 1.5, fontWeight: 500 }}
        >
          Connect with travelers who go where you go.
        </motion.div>

        {/* Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.54, duration: 0.4 }}
          style={{ display: 'flex', flexDirection: 'column', gap: 10 }}
        >
          {/* Get Started */}
          <button
            onClick={onNext}
            style={{
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              padding: '18px 24px', background: color, color: '#fff',
              fontSize: 17, fontWeight: 800, borderRadius: 9999,
              border: 'none', cursor: 'pointer', letterSpacing: '-0.01em',
              transition: 'background 280ms, box-shadow 280ms',
              boxShadow: `0 8px 28px ${color}55`,
            }}
          >
            Get Started
            <span style={{ fontSize: 18 }}>→</span>
          </button>

          {/* Apple + Google side-by-side */}
          <div style={{ display: 'flex', gap: 10 }}>
            <button
              onClick={onNext}
              style={{
                flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                padding: '16px', background: '#141414', color: '#fff',
                fontSize: 15, fontWeight: 700, borderRadius: 9999,
                border: 'none', cursor: 'pointer',
              }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="white">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
              </svg>
              Apple
            </button>
            <button
              onClick={onNext}
              style={{
                flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                padding: '16px', background: '#fff', color: '#141414',
                fontSize: 15, fontWeight: 700, borderRadius: 9999,
                border: '2px solid #ebebeb', cursor: 'pointer',
              }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
          </div>

          {/* Sign in link */}
          <div style={{ textAlign: 'center', marginTop: 2 }}>
            <span style={{ fontSize: 14, color: '#999' }}>Already have an account? </span>
            <button
              onClick={onNext}
              style={{
                fontSize: 14, fontWeight: 700, color,
                background: 'none', border: 'none', cursor: 'pointer',
                transition: 'color 280ms',
              }}
            >
              Sign in
            </button>
          </div>
        </motion.div>
      </div>
    </div>
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

  const [uploadedPhotos, setUploadedPhotos] = useState<string[]>(Array(9).fill(''))

  const [otp, setOtp] = useState(['','','','','',''])
  const [phone, setPhone] = useState('')
  const [countryCode, setCountryCode] = useState('+1')
  const [destInput, setDestInput] = useState('')
  const [resendTimer, setResendTimer] = useState(0)
  const [authLoading, setAuthLoading] = useState(false)
  const [authError, setAuthError] = useState('')
  const otpRefs = useRef<(HTMLInputElement | null)[]>([])
  const recaptchaVerifier = useRef<RecaptchaVerifier | null>(null)
  const confirmationResult = useRef<ConfirmationResult | null>(null)

  // Advance color with each step forward
  useEffect(() => {
    setIndex((((step - 1) % 7)) as any)
  }, [step, setIndex])

  // Resend code countdown
  useEffect(() => {
    if (resendTimer <= 0) return
    const id = setInterval(() => {
      setResendTimer(t => {
        if (t <= 1) { clearInterval(id); return 0 }
        return t - 1
      })
    }, 1000)
    return () => clearInterval(id)
  }, [resendTimer])

  async function goNext() {
    if (step < TOTAL_STEPS) {
      setStep(s => s + 1)
      advance()
    } else {
      triggerRainbow()
      const uid = auth.currentUser?.uid
      if (uid) {
        try {
          // Upload photos to Firebase Storage, then save profile
          const uploadedUrls: string[] = []
          for (let i = 0; i < uploadedPhotos.length; i++) {
            const photo = uploadedPhotos[i]
            if (!photo) continue
            try {
              // Already a URL (Firebase) — keep it; otherwise upload base64
              if (photo.startsWith('http')) {
                uploadedUrls.push(photo)
              } else if (photo.startsWith('data:')) {
                const url = await uploadPhoto(photo, uid, i)
                uploadedUrls.push(url)
              }
            } catch { /* skip failed uploads */ }
          }

          const dobStr = `${state.dob.year}-${state.dob.month}-${state.dob.day}`
          const birthYear = parseInt(state.dob.year, 10)
          const age = new Date().getFullYear() - birthYear

          await saveUserProfile(uid, {
            uid,
            name: state.name,
            age: isNaN(age) ? 25 : age,
            dob: dobStr,
            gender: state.gender,
            showGender: state.showGender,
            lookingFor: state.lookingFor,
            location: state.location,
            radius: state.radius,
            destinations: state.destinations,
            travelDates: state.travelDates.map(td => `${td.start} – ${td.end}`).join(', '),
            travelStyle: state.travelStyles,
            budget: state.budget,
            travelGoal: state.travelGoal,
            interests: state.interests,
            photos: uploadedUrls,
            bio: state.bio,
            anthem: state.spotifyAnthem,
            verified: 1,
          })
        } catch (e) {
          console.error('Profile save failed', e)
        }
      }
      setTimeout(() => router.push('/discover'), 700)
    }
  }

  function goBack() {
    if (step > 1) {
      setStep(s => s - 1)
      retreat()
    }
  }

  // ── Firebase Auth ──────────────────────────────────────────────────────────
  function getOrCreateRecaptcha() {
    if (!recaptchaVerifier.current) {
      recaptchaVerifier.current = new RecaptchaVerifier(auth, 'recaptcha-container', {
        size: 'invisible',
        callback: () => {},
        'expired-callback': () => { recaptchaVerifier.current = null },
      })
    }
    return recaptchaVerifier.current
  }

  async function sendCode() {
    const digits = phone.replace(/\D/g, '')
    if (digits.length < 7) return
    setAuthError('')
    setAuthLoading(true)
    try {
      const verifier = getOrCreateRecaptcha()
      const fullPhone = `${countryCode}${digits}`
      const result = await signInWithPhoneNumber(auth, fullPhone, verifier)
      confirmationResult.current = result
      setResendTimer(30)
      goNext()
    } catch (err: any) {
      recaptchaVerifier.current = null
      setAuthError(
        err.code === 'auth/invalid-phone-number'
          ? 'Invalid phone number. Include your country code.'
          : err.code === 'auth/too-many-requests'
          ? 'Too many attempts. Please wait a few minutes.'
          : 'Could not send code. Please try again.'
      )
    } finally {
      setAuthLoading(false)
    }
  }

  async function verifyCode() {
    if (!confirmationResult.current || otp.join('').length < 6) return
    setAuthError('')
    setAuthLoading(true)
    try {
      await confirmationResult.current.confirm(otp.join(''))
      goNext()
    } catch {
      setAuthError('Incorrect code. Please try again.')
      setOtp(['','','','','',''])
      setTimeout(() => otpRefs.current[0]?.focus(), 100)
    } finally {
      setAuthLoading(false)
    }
  }

  async function resendCode() {
    if (resendTimer > 0) return
    setAuthError('')
    setAuthLoading(true)
    try {
      recaptchaVerifier.current = null
      const verifier = getOrCreateRecaptcha()
      const digits = phone.replace(/\D/g, '')
      const result = await signInWithPhoneNumber(auth, `${countryCode}${digits}`, verifier)
      confirmationResult.current = result
      setResendTimer(30)
      setOtp(['','','','','',''])
    } catch {
      setAuthError('Failed to resend code. Please try again.')
    } finally {
      setAuthLoading(false)
    }
  }

  async function signInWithGoogle() {
    setAuthError('')
    setAuthLoading(true)
    try {
      const provider = new GoogleAuthProvider()
      await signInWithPopup(auth, provider)
      // Skip phone steps — go straight to name
      setStep(4)
      advance()
    } catch (err: any) {
      if (err.code !== 'auth/popup-closed-by-user') {
        setAuthError('Google sign-in failed. Please try again.')
      }
    } finally {
      setAuthLoading(false)
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
      <div style={{ padding: 'max(var(--sat), 20px) 32px 0' }}>
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
          <WelcomeScreen color={color} onNext={goNext} />
        </StepWrap>
      )

      // ─── STEP 2: Phone verification ──────────────────────────────────────
      case 2: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header title="What's your number?" sub="We'll send a 6-digit code. Standard rates may apply." />

            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
                <select
                  value={countryCode}
                  onChange={e => setCountryCode(e.target.value)}
                  style={{
                    padding: '18px 10px', background: '#f5f5f5', borderRadius: 12, border: '2px solid #d6d6d6',
                    fontSize: 15, fontWeight: 600, cursor: 'pointer', outline: 'none', color: '#141414',
                  }}
                >
                  {[
                    ['+1','🇺🇸 +1'],['+44','🇬🇧 +44'],['+61','🇦🇺 +61'],['+91','🇮🇳 +91'],
                    ['+234','🇳🇬 +234'],['+27','🇿🇦 +27'],['+33','🇫🇷 +33'],['+49','🇩🇪 +49'],
                    ['+34','🇪🇸 +34'],['+55','🇧🇷 +55'],['+52','🇲🇽 +52'],['+81','🇯🇵 +81'],
                    ['+82','🇰🇷 +82'],['+86','🇨🇳 +86'],['+971','🇦🇪 +971'],['+966','🇸🇦 +966'],
                  ].map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
                <input
                  type="tel"
                  value={phone}
                  onChange={e => { setPhone(e.target.value); setAuthError('') }}
                  placeholder="Phone number"
                  style={{
                    flex: 1, padding: '18px 20px', background: '#f5f5f5', borderRadius: 12,
                    border: `2px solid ${phone ? color : '#d6d6d6'}`, fontSize: 16, fontWeight: 500,
                    outline: 'none', transition: 'border-color 280ms', color: '#141414',
                    boxSizing: 'border-box', fontFamily: 'inherit',
                  }}
                />
              </div>

              {authError ? (
                <div style={{ padding: '10px 14px', background: '#FFF0F0', borderRadius: 10, marginBottom: 16,
                  fontSize: 13, color: '#D32F2F', fontWeight: 500 }}>
                  {authError}
                </div>
              ) : null}

              <div style={{ textAlign: 'center', marginBottom: 16, fontSize: 14, color: '#999' }}>— or continue with —</div>

              <button
                onClick={signInWithGoogle}
                disabled={authLoading}
                style={{
                  width: '100%', padding: '16px', background: '#fff', borderRadius: 12,
                  border: '2px solid #e0e0e0', fontSize: 15, fontWeight: 700, cursor: 'pointer',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
                  color: '#141414', boxSizing: 'border-box',
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Continue with Google
              </button>
            </div>

            <CTA
              label={authLoading ? 'Sending…' : 'Send Code'}
              disabled={phone.replace(/\D/g,'').length < 7 || authLoading}
              onClick={sendCode}
            />
          </div>
        </StepWrap>
      )

      // ─── STEP 3: OTP ─────────────────────────────────────────────────────
      case 3: return (
        <StepWrap step={step}>
          <div style={{ padding: '40px 32px 40px', display: 'flex', flexDirection: 'column', gap: 32, height: '100%' }}>
            <Header
              title="Enter the code"
              sub={`We sent a 6-digit code to ${countryCode} ${phone}`}
            />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 32, paddingTop: 32 }}>
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

              {authError ? (
                <div style={{ padding: '10px 14px', background: '#FFF0F0', borderRadius: 10, width: '100%',
                  fontSize: 13, color: '#D32F2F', fontWeight: 500, textAlign: 'center' }}>
                  {authError}
                </div>
              ) : null}

              <button
                onClick={resendCode}
                disabled={resendTimer > 0 || authLoading}
                style={{
                  fontSize: 14,
                  color: resendTimer > 0 ? '#A0A0A8' : color,
                  background: 'none', border: 'none',
                  cursor: resendTimer > 0 ? 'default' : 'pointer',
                  fontWeight: 600, transition: 'color 280ms',
                }}
              >
                {resendTimer > 0 ? `Resend in ${resendTimer}s` : 'Resend code'}
              </button>
            </div>

            <CTA
              label={authLoading ? 'Verifying…' : 'Verify'}
              disabled={otp.join('').length < 6 || authLoading}
              onClick={verifyCode}
            />
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
              {/* Autocomplete input */}
              <div style={{ position: 'relative' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                  <input
                    value={destInput}
                    onChange={e => setDestInput(e.target.value)}
                    onKeyDown={e => {
                      if (e.key === 'Enter') {
                        const suggestions = ALL_DESTINATIONS.filter(d =>
                          d.toLowerCase().includes(destInput.toLowerCase()) &&
                          !state.destinations.includes(d)
                        )
                        addDestination(suggestions[0] ?? destInput)
                        setDestInput('')
                      }
                    }}
                    placeholder="Search city or country..."
                    autoComplete="off"
                    style={{
                      flex: 1, padding: '16px 18px', background: '#f5f5f5',
                      borderRadius: 12, border: `2px solid ${destInput ? color : '#d6d6d6'}`,
                      fontSize: 15, fontWeight: 500, outline: 'none', color: '#141414', transition: 'border-color 280ms',
                    }}
                  />
                  <button
                    onClick={() => { addDestination(destInput); setDestInput('') }}
                    style={{
                      padding: '16px 20px', background: color, color: '#fff',
                      borderRadius: 12, border: 'none', cursor: 'pointer',
                      fontSize: 20, fontWeight: 700, transition: 'background 280ms',
                    }}
                  >+</button>
                </div>

                {/* Suggestions dropdown */}
                {destInput.length >= 1 && (() => {
                  const suggestions = ALL_DESTINATIONS.filter(d =>
                    d.toLowerCase().includes(destInput.toLowerCase()) &&
                    !state.destinations.includes(d)
                  ).slice(0, 6)
                  return suggestions.length > 0 ? (
                    <div style={{
                      position: 'absolute', top: 'calc(100% + 6px)', left: 0, right: 48,
                      background: '#fff', borderRadius: 12,
                      border: '2px solid #ebebeb',
                      boxShadow: '0 8px 24px rgba(0,0,0,0.10)',
                      overflow: 'hidden', zIndex: 50,
                    }}>
                      {suggestions.map((s, i) => {
                        const query = destInput.toLowerCase()
                        const idx = s.toLowerCase().indexOf(query)
                        const before = s.slice(0, idx)
                        const match = s.slice(idx, idx + destInput.length)
                        const after = s.slice(idx + destInput.length)
                        return (
                          <button
                            key={s}
                            onMouseDown={e => { e.preventDefault(); addDestination(s); setDestInput('') }}
                            style={{
                              width: '100%', textAlign: 'left',
                              padding: '12px 16px', background: 'none', border: 'none',
                              borderTop: i > 0 ? '1px solid #f0f0f0' : 'none',
                              cursor: 'pointer', fontSize: 14, color: '#141414',
                              display: 'flex', alignItems: 'center', gap: 10,
                            }}
                          >
                            <span style={{ fontSize: 16 }}>✈️</span>
                            <span>
                              {before}
                              <strong style={{ color }}>{match}</strong>
                              {after}
                            </span>
                          </button>
                        )
                      })}
                    </div>
                  ) : null
                })()}
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
                  <PhotoUpload
                    key={i}
                    color={color}
                    index={i}
                    required={i === 0}
                    existingPhoto={uploadedPhotos[i] || undefined}
                    onPhoto={(url) => {
                      const p = [...uploadedPhotos]
                      p[i] = url
                      setUploadedPhotos(p)
                    }}
                  />
                ))}
              </div>

              {/* AI HD Enhancement banner */}
              <div style={{
                padding: '10px 14px',
                background: 'linear-gradient(135deg, rgba(68,204,68,0.08), rgba(0,136,255,0.08))',
                border: '1px solid rgba(0,136,255,0.18)',
                borderRadius: 10,
                display: 'flex', alignItems: 'center', gap: 8,
              }}>
                <span style={{ fontSize: 16 }}>✨</span>
                <div style={{ fontSize: 11, fontWeight: 700, color: '#3b3b3b', lineHeight: 1.4 }}>
                  AI HD Enhancement — every photo you upload is automatically enhanced to HD quality
                </div>
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

            <CTA label="Continue" disabled={!uploadedPhotos[0]} />
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
                  color: state.bio.length > 450 ? '#FF5A5F' : '#999',
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
      {/* Invisible recaptcha container — required by Firebase phone auth */}
      <div id="recaptcha-container" />

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
            position: 'absolute', top: 'max(var(--sat), 16px)', left: 20, zIndex: 60,
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
