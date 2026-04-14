'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { BottomNav } from '@/components/nomapal/BottomNav'
import { useAuth } from '@/lib/auth-context'

export default function DiscoverLayout({ children }: { children: React.ReactNode }) {
  const { user, profile, loading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (loading) return
    // Not logged in at all → onboarding
    if (!user) { router.replace('/onboarding'); return }
    // Logged in but no profile saved yet → onboarding
    if (!profile) { router.replace('/onboarding'); return }
  }, [user, profile, loading, router])

  // Show nothing while checking auth
  if (loading || !user || !profile) {
    return (
      <div style={{
        position: 'absolute', inset: 0, display: 'flex',
        alignItems: 'center', justifyContent: 'center',
        background: '#0D0A1A',
      }}>
        <div style={{ fontSize: 32 }}>✈️</div>
      </div>
    )
  }

  return (
    <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, minHeight: 0, position: 'relative', overflow: 'hidden' }}>
        {children}
      </div>
      <BottomNav />
    </div>
  )
}
