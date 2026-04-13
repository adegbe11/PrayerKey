import type { Metadata, Viewport } from 'next'
import './globals.css'
import { ColorProvider } from '@/components/nomapal/ColorProvider'

export const metadata: Metadata = {
  title: 'Nomapal — Find someone to travel with',
  description: 'Find your perfect travel partner. Not just someone to date.',
  metadataBase: new URL('https://nomapal.com'),
  openGraph: {
    title: 'Nomapal — Find someone to travel with',
    description: 'Travel-first social matching for solo travelers and digital nomads.',
    url: 'https://nomapal.com',
    siteName: 'Nomapal',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Nomapal — Find someone to travel with',
    description: 'Travel-first social matching for solo travelers and digital nomads.',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: 'cover',
  themeColor: '#FF4444',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <ColorProvider>
          <div className="mobile-frame">
            {children}
          </div>
        </ColorProvider>
      </body>
    </html>
  )
}
