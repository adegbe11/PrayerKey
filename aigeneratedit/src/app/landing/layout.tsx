import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'NomaPal | Match. Travel. Go. Find someone to travel with.',
  description: 'Find someone to travel with, match with people going your way, and start planning your trip together the moment you connect.',
}

export default function LandingLayout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ position: 'fixed', inset: 0, overflow: 'auto', background: '#FAFAFA' }}>
      {children}
    </div>
  )
}
