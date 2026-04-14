import type { Metadata, Viewport } from 'next'
import './globals.css'
import { ColorProvider } from '@/components/nomapal/ColorProvider'
import { AuthProvider } from '@/lib/auth-context'

export const metadata: Metadata = {
  title: 'Nomapal — Find someone to travel with',
  description: 'Find your perfect travel partner. Not just someone to date.',
  metadataBase: new URL('https://nomapal.com'),
  icons: {
    icon: [
      { url: '/icons/icon.png', type: 'image/png' },
      { url: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' },
    ],
    apple: [
      { url: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
    ],
    shortcut: '/icons/icon.png',
  },
  openGraph: {
    title: 'Nomapal — Find someone to travel with',
    description: 'Travel-first social matching for solo travelers and digital nomads.',
    url: 'https://nomapal.com',
    siteName: 'Nomapal',
    images: [{ url: '/icons/icon-512.png', width: 512, height: 512, alt: 'Nomapal' }],
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Nomapal — Find someone to travel with',
    description: 'Travel-first social matching for solo travelers and digital nomads.',
    images: ['/icons/icon-512.png'],
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: 'cover',
  themeColor: '#FF5A5F',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <ColorProvider>
            <div className="mobile-frame">
              {children}
            </div>
          </ColorProvider>
        </AuthProvider>
      </body>
    </html>
  )
}
