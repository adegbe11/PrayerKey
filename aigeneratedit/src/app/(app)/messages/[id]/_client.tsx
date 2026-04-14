'use client'

import { useState, useRef, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { motion, AnimatePresence } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { MOCK_MATCHES, MOCK_CONVERSATIONS } from '@/lib/mockData'
import type { Message } from '@/lib/types'

function timeStr(date: Date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export default function ChatPageClient() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const { color, advance } = useColor()

  const match = MOCK_MATCHES.find(m => m.id === id)
  const [messages, setMessages] = useState<Message[]>(MOCK_CONVERSATIONS[id] ?? [])
  const [input, setInput] = useState('')
  const [typing, setTyping] = useState(false)

  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  if (!match) {
    return <div style={{ padding: 32 }}>Match not found</div>
  }

  function sendMessage() {
    if (!input.trim()) return
    const msg: Message = {
      id: `msg_${Date.now()}`,
      senderId: 'me',
      text: input.trim(),
      timestamp: new Date(),
      status: 'sent',
    }
    setMessages(prev => [...prev, msg])
    setInput('')
    advance()

    setTimeout(() => setTyping(true), 1000)
    setTimeout(() => {
      setTyping(false)
      const replies = [
        'That sounds amazing! 🙌',
        'I was thinking the same thing!',
        `${match!.profile.destinations[0]} is going to be incredible`,
        'When are you thinking of flying out?',
        "We should plan this properly — what's your timeline?",
      ]
      const reply: Message = {
        id: `reply_${Date.now()}`,
        senderId: match!.profile.id,
        text: replies[Math.floor(Math.random() * replies.length)],
        timestamp: new Date(),
        status: 'read',
      }
      setMessages(prev => [...prev, reply])
    }, 3000)
  }

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#fff', display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px', borderBottom: '1px solid #ebebeb', background: '#fff', zIndex: 10 }}>
        <button onClick={() => router.back()} style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 16, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>←</button>
        <div style={{ width: 44, height: 44, borderRadius: '50%', overflow: 'hidden', border: `2px solid ${color}`, transition: 'border-color 280ms' }}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={match.profile.photos[0]} alt={match.profile.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 16, fontWeight: 700, color: '#141414' }}>{match.profile.name}</div>
          <div style={{ fontSize: 12, color: '#999' }}>✈ {match.profile.destinations[0]} · Matched {new Date(match.matchedAt).toLocaleDateString()}</div>
        </div>
        <button style={{ padding: '8px 14px', background: `${color}12`, border: `2px solid ${color}30`, borderRadius: 9999, fontSize: 12, fontWeight: 700, color, cursor: 'pointer', transition: 'all 280ms' }}>🗺 Plan Trip</button>
        <button style={{ width: 36, height: 36, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#737373' }}>⋯</button>
      </div>

      <div style={{ flex: 1, overflowY: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: 12 }} className="no-scrollbar">
        <div style={{ textAlign: 'center', margin: '8px 0' }}>
          <span style={{ display: 'inline-block', padding: '6px 14px', background: '#f5f5f5', borderRadius: 9999, fontSize: 12, fontWeight: 600, color: '#999' }}>
            You matched ✈ Both going to {match.profile.destinations[0]}
          </span>
        </div>

        {messages.map((msg) => {
          const isMe = msg.senderId === 'me'
          return (
            <motion.div key={msg.id} initial={{ opacity: 0, y: 10, scale: 0.95 }} animate={{ opacity: 1, y: 0, scale: 1 }} transition={{ duration: 0.2 }} style={{ display: 'flex', justifyContent: isMe ? 'flex-end' : 'flex-start', alignItems: 'flex-end', gap: 8 }}>
              {!isMe && (
                <div style={{ width: 28, height: 28, borderRadius: '50%', overflow: 'hidden', flexShrink: 0 }}>
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={match.profile.photos[0]} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
              )}
              <div style={{ maxWidth: '72%', display: 'flex', flexDirection: 'column', gap: 3, alignItems: isMe ? 'flex-end' : 'flex-start' }}>
                <div style={{ padding: '12px 16px', background: isMe ? color : '#f5f5f5', color: isMe ? '#fff' : '#141414', borderRadius: isMe ? '20px 20px 4px 20px' : '20px 20px 20px 4px', fontSize: 15, lineHeight: 1.5, fontWeight: 500, transition: 'background 280ms' }}>{msg.text}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <span style={{ fontSize: 11, color: '#bbb' }}>{timeStr(msg.timestamp)}</span>
                  {isMe && <span style={{ fontSize: 11, color: msg.status === 'read' ? color : '#bbb', transition: 'color 280ms' }}>{msg.status === 'read' ? '✓✓' : '✓'}</span>}
                </div>
              </div>
            </motion.div>
          )
        })}

        <AnimatePresence>
          {typing && (
            <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }} style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}>
              <div style={{ width: 28, height: 28, borderRadius: '50%', overflow: 'hidden' }}>
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={match.profile.photos[0]} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              </div>
              <div style={{ padding: '14px 18px', background: '#f5f5f5', borderRadius: '20px 20px 20px 4px', display: 'flex', gap: 4, alignItems: 'center' }}>
                {[0, 0.2, 0.4].map((delay, i) => (
                  <motion.div key={i} animate={{ y: [0, -4, 0] }} transition={{ repeat: Infinity, duration: 0.8, delay, ease: 'easeInOut' }} style={{ width: 8, height: 8, borderRadius: '50%', background: '#bbb' }} />
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
        <div ref={endRef} />
      </div>

      <div style={{ padding: '12px 16px 24px', borderTop: '1px solid #ebebeb', background: '#fff', display: 'flex', gap: 10, alignItems: 'flex-end' }}>
        <button style={{ width: 40, height: 40, borderRadius: '50%', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: 18, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>😊</button>
        <div style={{ flex: 1, background: '#f5f5f5', borderRadius: 20, border: `2px solid ${input ? color : 'transparent'}`, padding: '10px 16px', transition: 'border-color 280ms' }}>
          <input value={input} onChange={e => setInput(e.target.value)} onKeyDown={e => e.key === 'Enter' && sendMessage()} placeholder="Message..." style={{ width: '100%', background: 'none', border: 'none', outline: 'none', fontSize: 15, color: '#141414', fontWeight: 500 }} />
        </div>
        <button onClick={sendMessage} disabled={!input.trim()} style={{ width: 40, height: 40, borderRadius: '50%', background: input.trim() ? color : '#d6d6d6', border: 'none', cursor: input.trim() ? 'pointer' : 'default', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16, color: '#fff', flexShrink: 0, transition: 'background 280ms' }}>↑</button>
      </div>
    </div>
  )
}
