'use client'

import Link from 'next/link'

export default function NotFound() {
  return (
    <div style={{
      position: 'absolute', inset: 0,
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      gap: 20, padding: 32, background: '#fff', textAlign: 'center',
    }}>
      <div style={{ fontSize: 64 }}>✈️</div>
      <div style={{ fontSize: 28, fontWeight: 800, color: '#141414', letterSpacing: '-0.03em' }}>
        Lost in transit.
      </div>
      <div style={{ fontSize: 15, color: '#737373', lineHeight: 1.6 }}>
        This page doesn't exist — but your next adventure does.
      </div>
      <Link href="/onboarding" style={{
        padding: '16px 28px', background: 'var(--color-active, #FF4444)',
        color: '#fff', borderRadius: 9999, textDecoration: 'none',
        fontSize: 16, fontWeight: 700, transition: 'background 280ms',
      }}>
        Back to Nomapal
      </Link>
    </div>
  )
}
