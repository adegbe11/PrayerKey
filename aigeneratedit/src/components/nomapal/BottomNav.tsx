'use client'

import { usePathname, useRouter } from 'next/navigation'
import { useColor } from './ColorProvider'

const TABS = [
  {
    id: 'discover',
    href: '/discover',
    label: 'Discover',
    icon: (active: boolean, color: string) => (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="9" stroke={active ? color : '#999'} strokeWidth="2.2"/>
        <circle cx="12" cy="12" r="2" fill={active ? color : '#999'}/>
        <line x1="12" y1="3" x2="12" y2="7" stroke={active ? color : '#999'} strokeWidth="2" strokeLinecap="round"/>
        <line x1="12" y1="17" x2="12" y2="21" stroke={active ? color : '#999'} strokeWidth="2" strokeLinecap="round"/>
        <line x1="3" y1="12" x2="7" y2="12" stroke={active ? color : '#999'} strokeWidth="2" strokeLinecap="round"/>
        <line x1="17" y1="12" x2="21" y2="12" stroke={active ? color : '#999'} strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'explore',
    href: '/discover/explore',
    label: 'Explore',
    icon: (active: boolean, color: string) => (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <rect x="3" y="3" width="8" height="8" rx="2" fill={active ? color : 'none'} stroke={active ? color : '#999'} strokeWidth="2"/>
        <rect x="13" y="3" width="8" height="8" rx="2" fill={active ? color : 'none'} stroke={active ? color : '#999'} strokeWidth="2"/>
        <rect x="3" y="13" width="8" height="8" rx="2" fill={active ? color : 'none'} stroke={active ? color : '#999'} strokeWidth="2"/>
        <rect x="13" y="13" width="8" height="8" rx="2" fill={active ? color : 'none'} stroke={active ? color : '#999'} strokeWidth="2"/>
      </svg>
    ),
  },
  {
    id: 'logo',
    href: null,
    label: '',
    icon: (_active: boolean, color: string) => (
      <div style={{
        width: 44,
        height: 44,
        borderRadius: '50%',
        background: color,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: -16,
        boxShadow: `0 4px 20px ${color}55`,
        transition: `background 280ms cubic-bezier(0.16,1,0.3,1), box-shadow 280ms`,
      }}>
        <span style={{ color: '#fff', fontSize: 20, fontWeight: 900, letterSpacing: '-0.04em' }}>N</span>
      </div>
    ),
  },
  {
    id: 'messages',
    href: '/discover/messages',
    label: 'Messages',
    icon: (active: boolean, color: string) => (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2Z"
          fill={active ? color : 'none'}
          stroke={active ? color : '#999'}
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    ),
  },
  {
    id: 'profile',
    href: '/discover/profile',
    label: 'Profile',
    icon: (active: boolean, color: string) => (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="8" r="4" fill={active ? color : 'none'} stroke={active ? color : '#999'} strokeWidth="2"/>
        <path d="M4 20C4 17.8 7.6 16 12 16C16.4 16 20 17.8 20 20"
          stroke={active ? color : '#999'}
          strokeWidth="2"
          strokeLinecap="round"
        />
      </svg>
    ),
  },
]

export function BottomNav() {
  const pathname = usePathname()
  const router = useRouter()
  const { color, advance } = useColor()

  function isActive(href: string | null) {
    if (!href) return false
    if (href === '/discover') return pathname === '/discover'
    return pathname.startsWith(href)
  }

  function handleTab(tab: typeof TABS[number]) {
    if (!tab.href) return
    advance()
    router.push(tab.href)
  }

  return (
    <nav className="bottom-nav">
      {TABS.map(tab => {
        const active = isActive(tab.href)
        return (
          <button
            key={tab.id}
            onClick={() => handleTab(tab)}
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 4,
              background: 'none',
              border: 'none',
              cursor: tab.href ? 'pointer' : 'default',
              padding: '4px 0 0',
              position: 'relative',
            }}
          >
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              animation: active ? 'navBounce 200ms cubic-bezier(0.34,1.56,0.64,1)' : 'none',
            }}>
              {tab.icon(active, color)}
            </div>
            {tab.label && (
              <span style={{
                fontSize: 10,
                fontWeight: 500,
                color: active ? color : '#999',
                transition: 'color 280ms cubic-bezier(0.16,1,0.3,1)',
              }}>
                {tab.label}
              </span>
            )}
          </button>
        )
      })}
    </nav>
  )
}
