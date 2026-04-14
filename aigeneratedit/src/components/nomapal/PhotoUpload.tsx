'use client'

import { useState, useRef, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'

interface PhotoUploadProps {
  color: string
  onPhoto: (dataUrl: string) => void
  existingPhoto?: string
  index?: number
  required?: boolean
}

// ── Canvas-based HD enhancement ──────────────────────────────────────────────
function applySharpening(ctx: CanvasRenderingContext2D, w: number, h: number) {
  const src = ctx.getImageData(0, 0, w, h)
  const dst = ctx.createImageData(w, h)
  const s = src.data
  const d = dst.data
  const kernel = [0, -1, 0, -1, 5, -1, 0, -1, 0]

  for (let y = 1; y < h - 1; y++) {
    for (let x = 1; x < w - 1; x++) {
      let r = 0, g = 0, b = 0
      for (let ky = -1; ky <= 1; ky++) {
        for (let kx = -1; kx <= 1; kx++) {
          const idx = ((y + ky) * w + (x + kx)) * 4
          const kIdx = (ky + 1) * 3 + (kx + 1)
          r += s[idx]     * kernel[kIdx]
          g += s[idx + 1] * kernel[kIdx]
          b += s[idx + 2] * kernel[kIdx]
        }
      }
      const i = (y * w + x) * 4
      d[i]     = Math.max(0, Math.min(255, r))
      d[i + 1] = Math.max(0, Math.min(255, g))
      d[i + 2] = Math.max(0, Math.min(255, b))
      d[i + 3] = s[i + 3]
    }
  }
  ctx.putImageData(dst, 0, 0)
}

function contrastBoost(ctx: CanvasRenderingContext2D, w: number, h: number, factor = 1.12) {
  const img = ctx.getImageData(0, 0, w, h)
  const d = img.data
  for (let i = 0; i < d.length; i += 4) {
    d[i]     = Math.min(255, d[i]     * factor)
    d[i + 1] = Math.min(255, d[i + 1] * factor)
    d[i + 2] = Math.min(255, d[i + 2] * factor)
  }
  ctx.putImageData(img, 0, 0)
}

async function enhanceWithCanvas(file: File): Promise<string> {
  return new Promise((resolve) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const scale = Math.min(4, 3840 / Math.max(img.naturalWidth, 1))
      const w = Math.round(img.naturalWidth * scale)
      const h = Math.round(img.naturalHeight * scale)
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')!
      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'
      ctx.drawImage(img, 0, 0, w, h)
      contrastBoost(ctx, w, h)
      applySharpening(ctx, w, h)
      URL.revokeObjectURL(url)
      resolve(canvas.toDataURL('image/jpeg', 0.93))
    }
    img.src = url
  })
}

// ── Component ─────────────────────────────────────────────────────────────────
export function PhotoUpload({ color, onPhoto, existingPhoto, index = 0, required = false }: PhotoUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [status, setStatus] = useState<'idle' | 'enhancing' | 'done'>('idle')
  const [preview, setPreview] = useState<string | null>(existingPhoto ?? null)
  const [showBefore, setShowBefore] = useState(false)
  const [originalPreview, setOriginalPreview] = useState<string | null>(null)
  const [dots, setDots] = useState('.')

  // Animate dots while enhancing
  const startDots = useCallback(() => {
    let count = 1
    const t = setInterval(() => {
      count = (count % 3) + 1
      setDots('.'.repeat(count))
    }, 400)
    return t
  }, [])

  async function handleFile(file: File) {
    if (!file.type.startsWith('image/')) return

    // Show original immediately
    const reader = new FileReader()
    reader.onload = async (e) => {
      const origUrl = e.target?.result as string
      setOriginalPreview(origUrl)
      setPreview(origUrl)
      setStatus('enhancing')
      const dotsTimer = startDots()

      try {
        // Try server-side Replicate enhancement first
        const res = await fetch('/api/enhance-photo', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ imageBase64: origUrl }),
        })
        const data = await res.json()

        let finalUrl: string
        if (data.enhancedUrl) {
          finalUrl = data.enhancedUrl
        } else {
          // Fallback: canvas-based enhancement
          finalUrl = await enhanceWithCanvas(file)
        }

        clearInterval(dotsTimer)
        setPreview(finalUrl)
        setStatus('done')
        onPhoto(finalUrl)
      } catch {
        // Fallback on any error
        const finalUrl = await enhanceWithCanvas(file)
        clearInterval(dotsTimer)
        setPreview(finalUrl)
        setStatus('done')
        onPhoto(finalUrl)
      }
    }
    reader.readAsDataURL(file)
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (file) handleFile(file)
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    const file = e.dataTransfer.files?.[0]
    if (file) handleFile(file)
  }

  return (
    <div style={{ position: 'relative', aspectRatio: '3/4' }}>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        onChange={handleChange}
        style={{ display: 'none' }}
      />

      <div
        onClick={() => status === 'idle' || status === 'done' ? inputRef.current?.click() : undefined}
        onDrop={handleDrop}
        onDragOver={e => e.preventDefault()}
        style={{
          width: '100%', height: '100%', borderRadius: 12,
          border: `2px ${preview ? 'solid' : 'dashed'} ${preview ? 'transparent' : (required ? color : '#D0D0D4')}`,
          background: preview ? 'transparent' : '#F7F7F8',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: status === 'enhancing' ? 'default' : 'pointer',
          overflow: 'hidden', position: 'relative',
        }}
      >
        {/* Photo preview */}
        {preview && (
          <img
            src={showBefore && originalPreview ? originalPreview : preview}
            alt="Photo"
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        )}

        {/* Empty state */}
        {!preview && (
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: required ? 28 : 22, marginBottom: required ? 4 : 0 }}>
              {required ? '📷' : '+'}
            </div>
            {required && (
              <div style={{ fontSize: 10, fontWeight: 800, color, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Required
              </div>
            )}
          </div>
        )}

        {/* Enhancing overlay */}
        <AnimatePresence>
          {status === 'enhancing' && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              style={{
                position: 'absolute', inset: 0,
                background: 'rgba(0,0,0,0.72)',
                display: 'flex', flexDirection: 'column',
                alignItems: 'center', justifyContent: 'center',
                gap: 10,
              }}
            >
              {/* Scanning bar */}
              <motion.div
                animate={{ x: ['-100%', '200%'] }}
                transition={{ duration: 1.2, repeat: Infinity, ease: 'easeInOut' }}
                style={{
                  position: 'absolute', top: 0, left: 0, right: 0,
                  height: 2,
                  background: `linear-gradient(90deg, transparent, ${color}, transparent)`,
                }}
              />
              <div style={{ fontSize: 22 }}>✨</div>
              <div style={{ fontSize: 11, fontWeight: 800, color: '#fff', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                AI Enhancing{dots}
              </div>
              <div style={{ fontSize: 10, color: 'rgba(255,255,255,0.6)', fontWeight: 500 }}>
                Upgrading to HD
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* HD badge */}
        {status === 'done' && (
          <div style={{
            position: 'absolute', top: 6, left: 6,
            padding: '3px 7px',
            background: 'linear-gradient(135deg, #44CC44, #0088FF)',
            borderRadius: 5, fontSize: 9, fontWeight: 900,
            color: '#fff', letterSpacing: '0.06em',
          }}>
            HD✓
          </div>
        )}

        {/* Before/After toggle */}
        {status === 'done' && originalPreview && (
          <button
            onMouseDown={() => setShowBefore(true)}
            onMouseUp={() => setShowBefore(false)}
            onMouseLeave={() => setShowBefore(false)}
            onTouchStart={() => setShowBefore(true)}
            onTouchEnd={() => setShowBefore(false)}
            style={{
              position: 'absolute', bottom: 6, right: 6,
              padding: '3px 8px',
              background: 'rgba(0,0,0,0.55)',
              backdropFilter: 'blur(8px)',
              borderRadius: 5, fontSize: 9, fontWeight: 700,
              color: '#fff', border: 'none', cursor: 'pointer',
              letterSpacing: '0.02em',
            }}
          >
            {showBefore ? 'BEFORE' : 'AFTER'}
          </button>
        )}

        {/* Remove button */}
        {preview && status !== 'enhancing' && (
          <button
            onClick={e => { e.stopPropagation(); setPreview(null); setOriginalPreview(null); setStatus('idle') }}
            style={{
              position: 'absolute', top: 6, right: 6,
              width: 22, height: 22, borderRadius: '50%',
              background: 'rgba(0,0,0,0.5)',
              backdropFilter: 'blur(8px)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 11, color: '#fff', border: 'none', cursor: 'pointer',
            }}
          >
            ✕
          </button>
        )}
      </div>
    </div>
  )
}
