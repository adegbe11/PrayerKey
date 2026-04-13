'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { motion } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { MOCK_MATCHES } from '@/lib/mockData'
import type { Match } from '@/lib/types'

function timeAgo(date: Date): string {
  const diff = Date.now() - date.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h`
  return `${Math.floor(hrs / 24)}d`
}

export default function MessagesPage() {
  const { color, advance } = useColor()
  const router = useRouter()
  const [matches] = useState<Match[]>(MOCK_MATCHES)

  const newMatches = matches.filter(m => !m.lastMessage)
  const conversations = matches.filter(m => m.lastMessage)

  function openChat(matchId: string) {
    advance()
    router.push(`/app/messages/${matchId}`)
  }

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{ padding: '20px 20px 12px', borderBottom: '1px solid #ebebeb' }}>
        <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: '-0.03em', color: '#141414' }}>Messages</div>
      </div>

      <div style={{ flex: 1, overflowY: 'auto' }} className="no-scrollbar">

        {/* New Matches row */}
        {newMatches.length > 0 && (
          <div style={{ padding: '16px 20px' }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: '#999', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 12 }}>
              New Matches
            </div>
            <div style={{ display: 'flex', gap: 16, overflowX: 'auto', paddingBottom: 4 }} className="no-scrollbar">
              {newMatches.map((match, i) => (
                <motion.div
                  key={match.id}
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: i * 0.06, type: 'spring', stiffness: 400, damping: 25 }}
                  onClick={() => openChat(match.id)}
                  style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6, cursor: 'pointer', flexShrink: 0 }}
                >
                  <div style={{ position: 'relative' }}>
                    <div style={{
                      width: 72, height: 72, borderRadius: '50%', overflow: 'hidden',
                      border: `3px solid ${color}`,
                      boxShadow: `0 0 0 2px rgba(255,255,255,1), 0 4px 16px ${color}33`,
                      transition: 'border-color 280ms, box-shadow 280ms',
                    }}>
                      <img src={match.profile.photos[0]} alt={match.profile.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    {/* New indicator */}
                    <div style={{
                      position: 'absolute', bottom: 0, right: 0,
                      width: 18, height: 18, borderRadius: '50%',
                      background: color, border: '2px solid #fff',
                      transition: 'background 280ms',
                    }} />
                  </div>
                  <span style={{ fontSize: 12, fontWeight: 600, color: '#3b3b3b' }}>{match.profile.name}</span>
                </motion.div>
              ))}
            </div>
          </div>
        )}

        {/* Divider */}
        <div style={{ height: 1, background: '#f5f5f5', margin: '0 20px' }} />

        {/* Conversations */}
        <div style={{ padding: '8px 0' }}>
          {conversations.length === 0 ? (
            <div style={{ padding: '40px 32px', textAlign: 'center' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>💬</div>
              <div style={{ fontSize: 18, fontWeight: 700, color: '#141414', marginBottom: 8 }}>No messages yet</div>
              <div style={{ fontSize: 14, color: '#737373' }}>When you match with someone, start a conversation here.</div>
            </div>
          ) : (
            conversations.map((match, i) => (
              <motion.div
                key={match.id}
                initial={{ opacity: 0, x: -16 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: i * 0.05 }}
                onClick={() => openChat(match.id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: 14,
                  padding: '14px 20px', cursor: 'pointer',
                  background: match.unread ? `${color}06` : 'transparent',
                  transition: 'background 280ms',
                }}
              >
                {/* Avatar */}
                <div style={{ position: 'relative', flexShrink: 0 }}>
                  <div style={{
                    width: 60, height: 60, borderRadius: '50%', overflow: 'hidden',
                    border: match.unread ? `2.5px solid ${color}` : '2.5px solid #ebebeb',
                    transition: 'border-color 280ms',
                  }}>
                    <img src={match.profile.photos[0]} alt={match.profile.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  </div>
                  {match.unread ? (
                    <div style={{
                      position: 'absolute', top: -2, right: -2,
                      width: 18, height: 18, borderRadius: '50%',
                      background: color, border: '2px solid #fff',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: 10, fontWeight: 800, color: '#fff',
                      transition: 'background 280ms',
                    }}>
                      {match.unread}
                    </div>
                  ) : null}
                </div>

                {/* Content */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 3 }}>
                    <span style={{ fontSize: 16, fontWeight: match.unread ? 800 : 600, color: '#141414' }}>
                      {match.profile.name}
                    </span>
                    <span style={{ fontSize: 12, color: '#999', flexShrink: 0 }}>
                      {match.lastMessage ? timeAgo(match.lastMessage.timestamp) : timeAgo(match.matchedAt)}
                    </span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    {match.lastMessage && match.lastMessage.senderId === 'me' && (
                      <span style={{ fontSize: 12, color: match.lastMessage.status === 'read' ? color : '#999', transition: 'color 280ms' }}>
                        {match.lastMessage.status === 'read' ? '✓✓' : match.lastMessage.status === 'delivered' ? '✓✓' : '✓'}
                      </span>
                    )}
                    <span style={{
                      fontSize: 14, color: match.unread ? '#141414' : '#999',
                      fontWeight: match.unread ? 600 : 400,
                      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                    }}>
                      {match.lastMessage?.text ?? `Matched ${timeAgo(match.matchedAt)} ago`}
                    </span>
                  </div>
                </div>

                {/* Destination badge */}
                <div style={{
                  padding: '4px 8px', background: '#f5f5f5', borderRadius: 8,
                  fontSize: 11, fontWeight: 700, color: '#737373',
                  flexShrink: 0, maxWidth: 70, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>
                  ✈ {match.profile.destinations[0]}
                </div>
              </motion.div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
