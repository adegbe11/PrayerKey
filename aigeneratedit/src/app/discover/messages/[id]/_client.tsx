'use client'

import { useState, useRef, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { useAuth } from '@/lib/auth-context'
import {
  getMatch, subscribeToMessages, sendMessage, markMessagesRead,
  type MatchDoc, type MessageDoc,
} from '@/lib/db'
import { MOCK_MATCHES, MOCK_CONVERSATIONS } from '@/lib/mockData'
import { Timestamp } from 'firebase/firestore'

function timeStr(date: Date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function tsToDate(ts: Timestamp | null | undefined): Date {
  if (!ts) return new Date()
  if (ts instanceof Timestamp) return ts.toDate()
  return new Date()
}

export default function ChatPageClient() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const { color, advance } = useColor()
  const { user } = useAuth()

  const [firestoreMatch, setFirestoreMatch] = useState<MatchDoc | null>(null)
  const [messages, setMessages] = useState<MessageDoc[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [loadingMatch, setLoadingMatch] = useState(true)

  const endRef = useRef<HTMLDivElement>(null)
  const uid = user?.uid ?? 'me'

  // Load match document
  useEffect(() => {
    getMatch(id).then(m => {
      setFirestoreMatch(m)
      setLoadingMatch(false)
    }).catch(() => setLoadingMatch(false))
  }, [id])

  // Subscribe to real-time messages
  useEffect(() => {
    if (!firestoreMatch) return
    const unsub = subscribeToMessages(id, (msgs) => {
      setMessages(msgs)
    })
    markMessagesRead(id, uid).catch(() => {})
    return unsub
  }, [firestoreMatch, id, uid])

  // Auto-scroll to bottom
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Mock fallback
  const mockMatch = MOCK_MATCHES.find(m => m.id === id)
  const [mockMessages, setMockMessages] = useState(MOCK_CONVERSATIONS[id] ?? [])
  const [mockTyping, setMockTyping] = useState(false)

  const useFirestore = !!firestoreMatch

  let otherName = ''
  let otherPhoto = ''
  let otherDest = ''
  let matchedAtDate = new Date()

  if (useFirestore && firestoreMatch) {
    const other = firestoreMatch.profiles[0]?.uid === uid
      ? firestoreMatch.profiles[1]
      : firestoreMatch.profiles[0]
    otherName = other?.name ?? ''
    otherPhoto = other?.photos?.[0] ?? ''
    otherDest = other?.destinations?.[0] ?? ''
    matchedAtDate = tsToDate(firestoreMatch.createdAt)
  } else if (mockMatch) {
    otherName = mockMatch.profile.name
    otherPhoto = mockMatch.profile.photos[0]
    otherDest = mockMatch.profile.destinations[0]
    matchedAtDate = mockMatch.matchedAt
  }

  if (!loadingMatch && !useFirestore && !mockMatch) {
    return <div style={{ padding: 32, color: '#999' }}>Match not found</div>
  }

  async function handleSend() {
    if (!input.trim() || sending) return
    advance()

    if (useFirestore && firestoreMatch) {
      const other = firestoreMatch.profiles[0]?.uid === uid
        ? firestoreMatch.profiles[1]
        : firestoreMatch.profiles[0]
      setSending(true)
      try {
        await sendMessage(id, uid, input.trim(), other?.uid ?? '')
        setInput('')
      } catch (e) {
        console.error('Send failed', e)
      } finally {
        setSending(false)
      }
    } else {
      // Mock mode with simulated reply
      const msg = {
        id: `msg_${Date.now()}`,
        senderId: 'me',
        text: input.trim(),
        timestamp: new Date(),
        status: 'sent' as const,
      }
      setMockMessages(prev => [...prev, msg])
      setInput('')

      setTimeout(() => setMockTyping(true), 1000)
      setTimeout(() => {
        setMockTyping(false)
        const replies = [
          'That sounds amazing! 🙌',
          'I was thinking the same thing!',
          `${otherDest} is going to be incredible`,
          'When are you thinking of flying out?',
          "Let's plan this — what's your timeline?",
        ]
        setMockMessages(prev => [...prev, {
          id: `reply_${Date.now()}`,
          senderId: mockMatch?.profile.id ?? 'other',
          text: replies[Math.floor(Math.random() * replies.length)],
          timestamp: new Date(),
          status: 'read' as const,
        }])
      }, 3000)
    }
  }

  const displayMessages = useFirestore
    ? messages.map(m => ({
        id: m.id,
        senderId: m.senderId,
        text: m.text,
        timestamp: tsToDate(m.timestamp),
        status: m.status,
      }))
    : mockMessages.map(m => ({
        id: m.id,
        senderId: m.senderId,
        text: m.text ?? '',
        timestamp: m.timestamp instanceof Date ? m.timestamp : new Date(),
        status: m.status,
      }))

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12,
        padding: 'max(var(--sat), 14px) 16px 14px',
        borderBottom: '1px solid #ebebeb', background: '#fff', zIndex: 10,
      }}>
        <button onClick={() => router.back()} style={{
          width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5',
          border: 'none', cursor: 'pointer', fontSize: 16,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>←</button>

        {otherPhoto && (
          <div style={{ width: 44, height: 44, borderRadius: '50%', overflow: 'hidden', border: `2px solid ${color}`, transition: 'border-color 280ms', flexShrink: 0 }}>
            <img src={otherPhoto} alt={otherName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          </div>
        )}

        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 16, fontWeight: 700, color: '#141414' }}>{otherName || 'Loading...'}</div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {otherDest && `✈ ${otherDest} · `}Matched {matchedAtDate.toLocaleDateString()}
          </div>
        </div>

        <button style={{
          padding: '8px 14px',
          background: `${color}12`, border: `2px solid ${color}30`,
          borderRadius: 9999, fontSize: 12, fontWeight: 700, color,
          cursor: 'pointer', transition: 'all 280ms',
        }}>
          🗺 Plan Trip
        </button>
      </div>

      {/* Messages */}
      <div style={{
        flex: 1, overflowY: 'auto', padding: '16px',
        display: 'flex', flexDirection: 'column', gap: 12,
      }} className="no-scrollbar">

        <div style={{ textAlign: 'center', margin: '8px 0' }}>
          <span style={{
            display: 'inline-block', padding: '6px 14px',
            background: '#f5f5f5', borderRadius: 9999,
            fontSize: 12, fontWeight: 600, color: '#999',
          }}>
            {otherDest ? `You matched ✈ Both going to ${otherDest}` : `You matched with ${otherName}`}
          </span>
        </div>

        {displayMessages.map((msg) => {
          const isMe = msg.senderId === uid
          return (
            <motion.div
              key={msg.id}
              initial={{ opacity: 0, y: 10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ duration: 0.2 }}
              style={{ display: 'flex', justifyContent: isMe ? 'flex-end' : 'flex-start', alignItems: 'flex-end', gap: 8 }}
            >
              {!isMe && otherPhoto && (
                <div style={{ width: 28, height: 28, borderRadius: '50%', overflow: 'hidden', flexShrink: 0 }}>
                  <img src={otherPhoto} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
              )}
              <div style={{ maxWidth: '72%', display: 'flex', flexDirection: 'column', gap: 3, alignItems: isMe ? 'flex-end' : 'flex-start' }}>
                <div style={{
                  padding: '12px 16px',
                  background: isMe ? color : '#f5f5f5',
                  color: isMe ? '#fff' : '#141414',
                  borderRadius: isMe ? '20px 20px 4px 20px' : '20px 20px 20px 4px',
                  fontSize: 15, lineHeight: 1.5, fontWeight: 500,
                  transition: 'background 280ms',
                }}>
                  {msg.text}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <span style={{ fontSize: 11, color: '#bbb' }}>{timeStr(msg.timestamp)}</span>
                  {isMe && (
                    <span style={{ fontSize: 11, color: msg.status === 'read' ? color : '#bbb', transition: 'color 280ms' }}>
                      {msg.status === 'read' ? '✓✓' : msg.status === 'delivered' ? '✓✓' : '✓'}
                    </span>
                  )}
                </div>
              </div>
            </motion.div>
          )
        })}

        <AnimatePresence>
          {mockTyping && (
            <motion.div
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}
            >
              {otherPhoto && (
                <div style={{ width: 28, height: 28, borderRadius: '50%', overflow: 'hidden' }}>
                  <img src={otherPhoto} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
              )}
              <div style={{ padding: '14px 18px', background: '#f5f5f5', borderRadius: '20px 20px 20px 4px', display: 'flex', gap: 4, alignItems: 'center' }}>
                {[0, 0.2, 0.4].map((delay, i) => (
                  <motion.div
                    key={i}
                    animate={{ y: [0, -4, 0] }}
                    transition={{ repeat: Infinity, duration: 0.8, delay, ease: 'easeInOut' }}
                    style={{ width: 8, height: 8, borderRadius: '50%', background: '#bbb' }}
                  />
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        <div ref={endRef} />
      </div>

      {/* Input */}
      <div style={{
        padding: `12px 16px max(var(--sab), 20px)`,
        borderTop: '1px solid #ebebeb', background: '#fff',
        display: 'flex', gap: 10, alignItems: 'flex-end',
      }}>
        <button style={{
          width: 40, height: 40, borderRadius: '50%', background: '#f5f5f5',
          border: 'none', cursor: 'pointer', fontSize: 18,
          display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
        }}>😊</button>

        <div style={{
          flex: 1, background: '#f5f5f5', borderRadius: 20,
          border: `2px solid ${input ? color : 'transparent'}`,
          padding: '10px 16px', transition: 'border-color 280ms',
        }}>
          <input
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && !e.shiftKey && handleSend()}
            placeholder="Message..."
            style={{ width: '100%', background: 'none', border: 'none', outline: 'none', fontSize: 15, color: '#141414', fontWeight: 500 }}
          />
        </div>

        <button
          onClick={handleSend}
          disabled={!input.trim() || sending}
          style={{
            width: 40, height: 40, borderRadius: '50%',
            background: input.trim() ? color : '#d6d6d6',
            border: 'none', cursor: input.trim() ? 'pointer' : 'default',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 16, color: '#fff', flexShrink: 0, transition: 'background 280ms',
          }}
        >
          ↑
        </button>
      </div>
    </div>
  )
}
