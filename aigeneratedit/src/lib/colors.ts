export const COLORS = [
  { id: 1, hex: '#FF4444', rgb: '255,68,68',   name: 'Red'    },
  { id: 2, hex: '#FF8800', rgb: '255,136,0',   name: 'Orange' },
  { id: 3, hex: '#FFDD00', rgb: '255,221,0',   name: 'Yellow' },
  { id: 4, hex: '#44CC44', rgb: '68,204,68',   name: 'Green'  },
  { id: 5, hex: '#0088FF', rgb: '0,136,255',   name: 'Blue'   },
  { id: 6, hex: '#3344CC', rgb: '51,68,204',   name: 'Indigo' },
  { id: 7, hex: '#9933CC', rgb: '153,51,204',  name: 'Violet' },
] as const

export type ColorIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6

export function applyColor(index: ColorIndex) {
  const c = COLORS[index]
  const root = document.documentElement
  root.style.setProperty('--color-active', c.hex)
  root.style.setProperty('--color-active-rgb', c.rgb)
  root.style.setProperty('--color-active-bg', `rgba(${c.rgb}, 0.05)`)
  root.style.setProperty('--color-active-bg-strong', `rgba(${c.rgb}, 0.12)`)
}

export function nextColor(current: ColorIndex): ColorIndex {
  return ((current + 1) % 7) as ColorIndex
}

export function prevColor(current: ColorIndex): ColorIndex {
  return ((current + 6) % 7) as ColorIndex
}

export function getColorHex(index: ColorIndex): string {
  return COLORS[index].hex
}
