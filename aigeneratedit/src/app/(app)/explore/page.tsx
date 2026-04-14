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
    <div style={{ position: 'absolute', inset: 0, background: '#f5f5f5', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{ background: '#fff', borderBottom: '1px solid #ebebeb', padding: '16px 20px 0' }}>
        <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: '-0.03em', color: '#141414', marginBottom: 14 }}>
          Explore
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: 0, borderBottom: '1px solid #ebebeb', marginBottom: -1 }}>
          {TABS.map((t, i) => (
            <button
              key={t}
              onClick={() => { setTab(i); advance() }}
              style={{
                flex: 1, padding: '10px 0', background: 'none', border: 'none',
                cursor: 'pointer', fontSize: 14, fontWeight: tab === i ? 700 : 500,
                color: tab === i ? color : '#999',
                borderBottom: tab === i ? `2px solid ${color}` : '2px solid transparent',
                transition: 'all 280ms',
              }}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div style={{ flex: 1, overflowY: 'auto' }} className="no-scrollbar">

        {/* Tab 0: For You */}
        {tab === 0 && (
          <div style={{ padding: '20px 0' }}>

            {/* Trending destinations */}
            <div style={{ marginBottom: 28 }}>
              <div style={{ padding: '0 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                <span style={{ fontSize: 17, fontWeight: 800, color: '#141414', letterSpacing: '-0.01em' }}>Trending Destinations</span>
                <button style={{ fontSize: 13, fontWeight: 600, color, background: 'none', border: 'none', cursor: 'pointer', transition: 'color 280ms' }}>See all</button>
              </div>
              <div style={{ display: 'flex', gap: 12, paddingLeft: 20, paddingRight: 20, overflowX: 'auto' }} className="no-scrollbar">
                {TRENDING_DESTINATIONS.map((dest, i) => (
                  <motion.div
                    key={dest.name}
                    initial={{ opacity: 0, y: 16 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.06 }}
                    style={{ flexShrink: 0, cursor: 'pointer', width: 100 }}
                  >
                    <div style={{ width: 100, height: 120, borderRadius: 16, overflow: 'hidden', marginBottom: 8, position: 'relative' }}>
                      <img src={dest.image} alt={dest.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      <div style={{
                        position: 'absolute', inset: 0,
                        background: 'linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 60%)',
                      }} />
                      <div style={{ position: 'absolute', bottom: 8, left: 8, fontSize: 18 }}>{dest.emoji}</div>
                    </div>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#141414' }}>{dest.name}</div>
                    <div style={{ fontSize: 11, color: '#999' }}>{dest.country}</div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Featured Trip Board */}
            <div style={{ margin: '0 20px 28px' }}>
              <div style={{ fontSize: 17, fontWeight: 800, color: '#141414', letterSpacing: '-0.01em', marginBottom: 14 }}>Featured Trip Board</div>
              <TripBoardCard board={MOCK_TRIP_BOARDS[0]} onApply={handleApply} applied={appliedBoards.includes(MOCK_TRIP_BOARDS[0].id)} color={color} />
            </div>

          </div>
        )}

        {/* Tab 1: Trip Boards */}
        {tab === 1 && (
          <div style={{ padding: '16px 20px', display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* Create board CTA */}
            <button style={{
              padding: '16px 20px',
              background: `${color}12`, borderRadius: 14,
              border: `2px dashed ${color}40`,
              cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 12,
              transition: 'all 280ms',
            }}>
              <div style={{
                width: 44, height: 44, borderRadius: '50%',
                background: color, display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 20, color: '#fff', transition: 'background 280ms',
              }}>+</div>
              <div style={{ textAlign: 'left' }}>
                <div style={{ fontSize: 15, fontWeight: 700, color }}>Create a Trip Board</div>
                <div style={{ fontSize: 13, color: '#999' }}>Post your trip, find travel companions</div>
              </div>
            </button>

            {MOCK_TRIP_BOARDS.map((board, i) => (
              <motion.div
                key={board.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.08 }}
              >
                <TripBoardCard board={board} onApply={handleApply} applied={appliedBoards.includes(board.id)} color={color} />
              </motion.div>
            ))}
          </div>
        )}

        {/* Tab 2: Top Picks */}
        {tab === 2 && (
          <div style={{ padding: '16px 20px' }}>
            <div style={{
              marginBottom: 16, padding: '14px 16px',
              background: `${color}10`, borderRadius: 12,
              border: `2px solid ${color}20`,
              display: 'flex', alignItems: 'center', gap: 10,
              transition: 'all 280ms',
            }}>
              <span style={{ fontSize: 20 }}>⭐</span>
              <div>
                <div style={{ fontSize: 14, fontWeight: 700, color, transition: 'color 280ms' }}>Today's Top Picks</div>
                <div style={{ fontSize: 12, color: '#999' }}>Refreshes daily · Highest compatibility profiles</div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              {MOCK_PROFILES.slice(0, 4).map((profile, i) => (
                <motion.div
                  key={profile.id}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: i * 0.07 }}
                  style={{ borderRadius: 16, overflow: 'hidden', position: 'relative', aspectRatio: '3/4', cursor: 'pointer' }}
                >
                  <img src={profile.photos[0]} alt={profile.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <div className="card-gradient" style={{ position: 'absolute', inset: 0 }} />
                  <div style={{ position: 'absolute', bottom: 10, left: 10, right: 10 }}>
                    <div style={{ fontSize: 15, fontWeight: 800, color: '#fff' }}>{profile.name}, {profile.age}</div>
                    <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.7)' }}>✈ {profile.destinations[0]}</div>
                  </div>
                  {/* Lock overlay for free users */}
                  {i >= 2 && (
                    <div style={{
                      position: 'absolute', inset: 0,
                      backdropFilter: 'blur(6px)',
                      background: 'rgba(0,0,0,0.3)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      flexDirection: 'column', gap: 6,
                    }}>
                      <div style={{ fontSize: 24, color: '#fff' }}>🔒</div>
                      <div style={{
                        padding: '6px 12px', background: color, color: '#fff',
                        borderRadius: 9999, fontSize: 12, fontWeight: 700,
                        transition: 'background 280ms',
                      }}>Nomapal+</div>
                    </div>
                  )}
                </motion.div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

function TripBoardCard({ board, onApply, applied, color }: { board: TripBoard; onApply: (id: string) => void; applied: boolean; color: string }) {
  const spotsLeft = board.spotsTotal - board.spotsFilled
  const fillPct = (board.spotsFilled / board.spotsTotal) * 100

  return (
    <div style={{ background: '#fff', borderRadius: 20, overflow: 'hidden', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
      {/* Cover image */}
      <div style={{ position: 'relative', height: 180 }}>
        <img src={board.coverImage} alt={board.destination} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div className="card-gradient" style={{ position: 'absolute', inset: 0 }} />
        <div style={{ position: 'absolute', top: 14, right: 14 }}>
          <span style={{
            padding: '5px 12px', background: 'rgba(0,0,0,0.55)',
            backdropFilter: 'blur(8px)',
            borderRadius: 9999, fontSize: 12, fontWeight: 700, color: '#fff',
          }}>
            {BUDGET_LABEL[board.budget]}
          </span>
        </div>
        <div style={{ position: 'absolute', bottom: 14, left: 16 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: '#fff', letterSpacing: '-0.01em' }}>{board.destination}</div>
          <div style={{ fontSize: 13, color: 'rgba(255,255,255,0.75)' }}>🗓 {board.dates}</div>
        </div>
      </div>

      {/* Host row */}
      <div style={{ padding: '14px 16px 0', display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{ width: 36, height: 36, borderRadius: '50%', overflow: 'hidden', border: `2px solid ${color}`, transition: 'border-color 280ms' }}>
          <img src={board.host.photos[0]} alt={board.host.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
        <div>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#141414' }}>{board.host.name}</div>
          <div style={{ fontSize: 11, color: '#999' }}>
            {board.host.verified >= 3 ? '✓ Verified Host' : 'Host'}
          </div>
        </div>
        <div style={{ marginLeft: 'auto', textAlign: 'right' }}>
          <div style={{ fontSize: 13, fontWeight: 800, color: spotsLeft <= 2 ? '#FF5A5F' : color, transition: 'color 280ms' }}>
            {spotsLeft} spot{spotsLeft !== 1 ? 's' : ''} left
          </div>
          <div style={{ fontSize: 11, color: '#999' }}>of {board.spotsTotal}</div>
        </div>
      </div>

      {/* Description */}
      <div style={{ padding: '10px 16px', fontSize: 14, color: '#737373', lineHeight: 1.5 }}>
        {board.description}
      </div>

      {/* Vibe tags */}
      <div style={{ padding: '0 16px 12px', display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {board.vibe.map(v => (
          <span key={v} style={{
            padding: '4px 10px', background: '#f5f5f5',
            borderRadius: 9999, fontSize: 12, fontWeight: 600, color: '#737373',
          }}>
            {v}
          </span>
        ))}
      </div>

      {/* Spots bar */}
      <div style={{ padding: '0 16px 14px' }}>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: `${fillPct}%` }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
          <span style={{ fontSize: 11, color: '#999' }}>{board.spotsFilled} joined</span>
          <span style={{ fontSize: 11, color: '#999' }}>{board.spotsTotal} max</span>
        </div>
      </div>

      {/* Apply button */}
      <div style={{ padding: '0 16px 16px' }}>
        <button
          onClick={() => !applied && onApply(board.id)}
          style={{
            width: '100%', padding: '14px',
            background: applied ? '#44CC44' : color,
            color: '#fff', fontSize: 15, fontWeight: 700,
            borderRadius: 9999, border: 'none', cursor: applied ? 'default' : 'pointer',
            transition: 'background 280ms',
          }}
        >
          {applied ? '✓ Applied' : 'Apply to Join'}
        </button>
      </div>
    </div>
  )
}
