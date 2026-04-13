import { BottomNav } from '@/components/nomapal/BottomNav'

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
        {children}
      </div>
      <BottomNav />
    </div>
  )
}
