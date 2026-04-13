'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'

// Show onboarding by default — in production this checks auth state
export default function RootPage() {
  const router = useRouter()
  useEffect(() => {
    const done = sessionStorage.getItem('nomapal_onboarded')
    router.replace(done ? '/discover' : '/onboarding')
  }, [router])
  return null
}
