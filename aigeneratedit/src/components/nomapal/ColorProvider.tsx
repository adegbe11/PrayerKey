'use client'

import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'
import { COLORS, applyColor, nextColor, prevColor, type ColorIndex } from '@/lib/colors'

interface ColorContextValue {
  colorIndex: ColorIndex
  color: string
  advance: () => void
  retreat: () => void
  setIndex: (i: ColorIndex) => void
  triggerRainbow: () => void
  rainbowActive: boolean
}

const ColorContext = createContext<ColorContextValue>({
  colorIndex: 0,
  color: '#FF4444',
  advance: () => {},
  retreat: () => {},
  setIndex: () => {},
  triggerRainbow: () => {},
  rainbowActive: false,
})

export function ColorProvider({ children }: { children: ReactNode }) {
  const [colorIndex, setColorIndexState] = useState<ColorIndex>(0)
  const [rainbowActive, setRainbowActive] = useState(false)

  useEffect(() => {
    applyColor(colorIndex)
  }, [colorIndex])

  const advance = useCallback(() => {
    setColorIndexState(prev => nextColor(prev))
  }, [])

  const retreat = useCallback(() => {
    setColorIndexState(prev => prevColor(prev))
  }, [])

  const setIndex = useCallback((i: ColorIndex) => {
    setColorIndexState(i)
  }, [])

  const triggerRainbow = useCallback(() => {
    setRainbowActive(true)
    setTimeout(() => setRainbowActive(false), 700)
  }, [])

  return (
    <ColorContext.Provider
      value={{
        colorIndex,
        color: COLORS[colorIndex].hex,
        advance,
        retreat,
        setIndex,
        triggerRainbow,
        rainbowActive,
      }}
    >
      {children}
      {rainbowActive && <div className="rainbow-sweep" />}
    </ColorContext.Provider>
  )
}

export function useColor() {
  return useContext(ColorContext)
}
