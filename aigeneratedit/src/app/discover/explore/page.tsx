'use client'

import { useState } from 'react'
import { motion } from 'framer-motion'
import { useColor } from '@/components/nomapal/ColorProvider'
import { MOCK_TRIP_BOARDS, TRENDING_DESTINATIONS, MOCK_PROFILES } from '@/lib/mockData'
import type { TripBoard } from '@/lib/types'

const BUDGET_LABEL: Record<string, string> = {
  budget: '🎒 Budget', midrange: '✈️ Mid-range',
  comfortable: '🏨 Comfortable', luxury: '💎 Luxury',
}

const TABS = ['For You', 'Trip Boards', 'Top Picks']

export default function ExplorePage() {
  const { color, advance } = useColor()
  const [tab, setTab] = useState(0)
  const [appliedBoards, setAppliedBoards] = useState<string[]>([])

  function handleApply(boardId: string) {
    setAppliedBoards(prev => [...prev, boardId])
    advance()
  }

  return (
    <div style={{ position: 'absolute', inset: 0, background: '#F7F7F8', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{ background: '#fff', borderBottom: '1px solid #F3F3F5', padding: 'max(var(--sat), 16px) 20px 0' }}>
        <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: '-0.03em', color: '#1a1a1a', marginBottom: 14 }}>
          Explore
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex' }}>
          {TABS.map((t, i) => (
            <button
              key={t}
              onClick={() => { setTab(i); advance() }}
              style={{
                flex: 1, padding: '10px 0', background: 'none', border: 'none',
                cursor: 'pointer', fontSize: 14, fontWeight: tab === i ? 700 : 500,
                color: tab === i ? color : '#A0A0A8',
                borderBottom: tab === i ? `2.5px solid ${color}` : '2.5px solid transparent',
                transition: 'all 250ms',
              }}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div style={{ flex: 1, overflowY: 'auto' }} className="no-scrollbar">

        {/* ── For You ─────────────────────────────────────────────────── */}
        {tab === 0 && (
          <div style={{ padding: '24px 0' }}>

            {/* Trending destinations */}
            <div style={{ marginBottom: 28 }}>
              <div style={{ padding: '0 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                <span style={{ fontSize: 17, fontWeight: 800, color: '#1a1a1a', letterSpacing: '-0.01em' }}>Trending Now</span>
                <button style={{ fontSize: 13, fontWeight: 600, color, background: 'none', border: 'none', cursor: 'pointer', transition: 'color 280ms' }}>
                  See all →
                </button>
              </div>
              <div style={{ display: 'flex', gap: 12, paddingLeft: 20, overflowX: 'auto', paddingBottom: 4 }} className="no-scrollbar">
                {TRENDING_DESTINATIONS.map((dest, i) => (
                  <motion.div
                    key={dest.name}
                    initial={{ opacity: 0, y: 16 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.06 }}
                    style={{ flexShrink: 0, cursor: 'pointer', width: 110 }}
                  >
                    <div style={{
                      width: 110, height: 140, borderRadius: 18,
                      overflow: 'hidden', marginBottom: 9, position: 'relative',
                      boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
                    }}>
                      <img src={dest.image} alt={dest.name}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      <div style={{
                        position: 'absolute', inset: 0,
                        background: 'linear-gradient(to top, rgba(0,0,0,0.65) 0%, transparent 55%)',
                      }} />
                      <div style={{ position: 'absolute', bottom: 10, left: 10 }}>
                        <div style={{ fontSize: 18, marginBottom: 2 }}>{dest.emoji}</div>
                        <div style={{ fontSize: 13, fontWeight: 800, color: '#fff', letterSpacing: '-0.01em' }}>{dest.name}</div>
                        <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.72)' }}>{dest.country}</div>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Featured trip board */}
            <div style={{ margin: '0 16px 24px' }}>
              <div style={{ fontSize: 17, fontWeight: 800, color: '#1a1a1a', letterSpacing: '-0.01em', marginBottom: 14 }}>
                Featured Trip Board
              </div>
              <TripBoardCard
                board={MOCK_TRIP_BOARDS[0]}
                onApply={handleApply}
                applied={appliedBoards.includes(MOCK_TRIP_BOARDS[0].id)}
                color={color}
              />
            </div>

            {/* Nomad near you */}
            <div>
              <div style={{ padding: '0 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                <span style={{ fontSize: 17, fontWeight: 800, color: '#1a1a1a', letterSpacing: '-0.01em' }}>Nomads Near You</span>
                <span style={{ fontSize: 12, color: '#A0A0A8', fontWeight: 600 }}>Within 10 km</span>
              </div>
              <div style={{ display: 'flex', gap: 12, paddingLeft: 20, overflowX: 'auto', paddingBottom: 4 }} className="no-scrollbar">
                {MOCK_PROFILES.slice(0, 4).map((p, i) => (
                  <motion.div
                    key={p.id}
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: i * 0.06 }}
                    style={{ flexShrink: 0, cursor: 'pointer', width: 90 }}
                  >
                    <div style={{ position: 'relative', width: 90, height: 90, borderRadius: '50%', marginBottom: 8, overflow: 'hidden', border: `2.5px solid ${color}`, boxShadow: `0 4px 14px ${color}28`, transition: 'border-color 280ms, box-shadow 280ms' }}>
                      <img src={p.photos[0]} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#1a1a1a', textAlign: 'center' }}>{p.name}</div>
                    <div style={{ fontSize: 11, color: '#A0A0A8', textAlign: 'center' }}>{p.distance}</div>
                  </motion.div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* ── Trip Boards ─────────────────────────────────────────────── */}
        {tab === 1 && (
          <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
            {/* Create CTA */}
            <motion.button
              whileTap={{ scale: 0.98 }}
              style={{
                padding: '18px 20px',
                background: `${color}0F`,
                borderRadius: 18,
                border: `2px dashed ${color}40`,
                cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 14,
                transition: 'all 280ms', width: '100%',
              }}
            >
              <div style={{
                width: 48, height: 48, borderRadius: '50%',
                background: color,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 22, color: '#fff', transition: 'background 280ms',
                boxShadow: `0 6px 18px ${color}33`,
              }}>+</div>
              <div style={{ textAlign: 'left' }}>
                <div style={{ fontSize: 15, fontWeight: 700, color, transition: 'color 280ms' }}>Create a Trip Board</div>
                <div style={{ fontSize: 13, color: '#A0A0A8' }}>Post your trip · Find travel companions</div>
              </div>
            </motion.button>

            {MOCK_TRIP_BOARDS.map((board, i) => (
              <motion.div
                key={board.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.07 }}
              >
                <TripBoardCard board={board} onApply={handleApply} applied={appliedBoards.includes(board.id)} color={color} />
              </motion.div>
            ))}
          </div>
        )}

        {/* ── Top Picks ───────────────────────────────────────────────── */}
        {tab === 2 && (
          <div style={{ padding: '16px' }}>
            <div style={{
              marginBottom: 14, padding: '14px 16px',
              background: '#fff',
              borderRadius: 16,
              border: '1px solid #ECECEF',
              display: 'flex', alignItems: 'center', gap: 12,
              boxShadow: '0 1px 8px rgba(0,0,0,0.05)',
            }}>
              <div style={{
                width: 40, height: 40, borderRadius: 12,
                background: `linear-gradient(135deg, ${color}, #FFB800)`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 18, transition: 'background 280ms',
              }}>⭐</div>
              <div>
                <div style={{ fontSize: 14, fontWeight: 800, color: '#1a1a1a' }}>Today's Top Picks</div>
                <div style={{ fontSize: 12, color: '#A0A0A8', marginTop: 2 }}>Refreshes daily · Highest compatibility</div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              {MOCK_PROFILES.slice(0, 4).map((profile, i) => (
                <motion.div
                  key={profile.id}
                  initial={{ opacity: 0, scale: 0.94 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: i * 0.07 }}
                  style={{
                    borderRadius: 18, overflow: 'hidden',
                    position: 'relative', aspectRatio: '3/4',
                    cursor: 'pointer',
                    boxShadow: '0 4px 16px rgba(0,0,0,0.10)',
                  }}
                >
                  <img src={profile.photos[0]} alt={profile.name}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <div className="card-gradient" style={{ position: 'absolute', inset: 0 }} />
                  <div style={{ position: 'absolute', bottom: 12, left: 12, right: 12 }}>
                    <div style={{ fontSize: 15, fontWeight: 800, color: '#fff', letterSpacing: '-0.01em' }}>
                      {profile.name}, {profile.age}
                    </div>
                    <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.72)', marginTop: 2 }}>
                      ✈ {profile.destinations[0]}
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

/* ─── Trip Board Card ─────────────────────────────────────────────────────── */

function TripBoardCard({ board, onApply, applied, color }: {
  board: TripBoard; onApply: (id: string) => void; applied: boolean; color: string
}) {
  const spotsLeft = board.spotsTotal - board.spotsFilled
  const fillPct = (board.spotsFilled / board.spotsTotal) * 100

  return (
    <div style={{
      background: '#fff', borderRadius: 22, overflow: 'hidden',
      boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
    }}>
      {/* Cover */}
      <div style={{ position: 'relative', height: 190 }}>
        <img src={board.coverImage} alt={board.destination}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div style={{
          position: 'absolute', inset: 0,
          background: 'linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, transparent 40%, rgba(0,0,0,0.6) 100%)',
        }} />
        {/* Budget badge */}
        <div style={{ position: 'absolute', top: 14, right: 14 }}>
          <span style={{
            padding: '5px 12px',
            background: 'rgba(0,0,0,0.45)',
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            borderRadius: 9999, fontSize: 12, fontWeight: 700, color: '#fff',
          }}>
            {BUDGET_LABEL[board.budget]}
          </span>
        </div>
        {/* Destination + dates */}
        <div style={{ position: 'absolute', bottom: 14, left: 16 }}>
          <div style={{ fontSize: 21, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
            {board.destination}
          </div>
          <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.78)', marginTop: 3 }}>🗓 {board.dates}</div>
        </div>
      </div>

      {/* Host row */}
      <div style={{ padding: '14px 18px 0', display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{
          width: 38, height: 38, borderRadius: '50%', overflow: 'hidden',
          border: `2px solid ${color}`, transition: 'border-color 280ms',
          flexShrink: 0,
        }}>
          <img src={board.host.photos[0]} alt={board.host.name}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: '#1a1a1a' }}>{board.host.name}</div>
          <div style={{ fontSize: 11, color: '#A0A0A8' }}>
            {board.host.verified >= 3 ? '✓ Verified Host' : 'Host'}
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{
            fontSize: 14, fontWeight: 800,
            color: spotsLeft <= 2 ? '#FF5A5F' : color,
            transition: 'color 280ms',
          }}>
            {spotsLeft} spot{spotsLeft !== 1 ? 's' : ''} left
          </div>
          <div style={{ fontSize: 11, color: '#A0A0A8' }}>of {board.spotsTotal}</div>
        </div>
      </div>

      {/* Description */}
      <div style={{ padding: '10px 18px', fontSize: 14, color: '#737378', lineHeight: 1.55 }}>
        {board.description}
      </div>

      {/* Vibe tags */}
      <div style={{ padding: '0 18px 12px', display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {board.vibe.map(v => (
          <span key={v} style={{
            padding: '5px 12px', background: '#F3F3F5',
            borderRadius: 9999, fontSize: 12, fontWeight: 600, color: '#737378',
          }}>
            {v}
          </span>
        ))}
      </div>

      {/* Fill bar */}
      <div style={{ padding: '0 18px 14px' }}>
        <div style={{ height: 5, background: '#F0F0F2', borderRadius: 9999, overflow: 'hidden' }}>
          <div style={{
            height: '100%', width: `${fillPct}%`,
            background: color, borderRadius: 9999,
            transition: 'width 0.6s ease, background 280ms',
          }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 5 }}>
          <span style={{ fontSize: 11, color: '#A0A0A8' }}>{board.spotsFilled} joined</span>
          <span style={{ fontSize: 11, color: '#A0A0A8' }}>{board.spotsTotal} max</span>
        </div>
      </div>

      {/* Apply button */}
      <div style={{ padding: '0 18px 18px' }}>
        <button
          onClick={() => !applied && onApply(board.id)}
          style={{
            width: '100%', padding: '15px',
            background: applied ? '#44CC44' : color,
            color: '#fff', fontSize: 15, fontWeight: 700,
            borderRadius: 9999, border: 'none',
            cursor: applied ? 'default' : 'pointer',
            boxShadow: applied ? '0 6px 20px rgba(68,204,68,0.3)' : `0 6px 20px ${color}33`,
            transition: 'all 280ms',
          }}
        >
          {applied ? '✓ Applied' : 'Apply to Join'}
        </button>
      </div>
    </div>
  )
}
