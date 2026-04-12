import type { Metadata } from 'next';
import { Inter, Playfair_Display, EB_Garamond } from 'next/font/google';
import './globals.css';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

const playfair = Playfair_Display({
  subsets: ['latin'],
  variable: '--font-playfair',
  style: ['normal', 'italic'],
  weight: ['400', '500', '600', '700', '800', '900'],
  display: 'swap',
});

const garamond = EB_Garamond({
  subsets: ['latin'],
  variable: '--font-garamond',
  style: ['normal', 'italic'],
  weight: ['400', '500', '600', '700', '800'],
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'Booksane — Turn text into a real book. Instantly.',
  description:
    'Paste your manuscript and watch it transform into a professionally formatted book. Preview as paperback or eBook. Export PDF and EPUB.',
  keywords: [
    'book formatter',
    'manuscript formatting',
    'epub generator',
    'pdf book',
    'book design',
    'self publishing',
    'KDP formatting',
    'book template',
  ],
  authors: [{ name: 'Booksane' }],
  openGraph: {
    title: 'Booksane — Your words. A real book. Instantly.',
    description: 'The fastest, most beautiful AI-powered book formatting tool.',
    url: 'https://booksane.com',
    siteName: 'Booksane',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Booksane — Turn text into a real book. Instantly.',
    description: 'The fastest, most beautiful AI-powered book formatting tool.',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${inter.variable} ${playfair.variable} ${garamond.variable}`} suppressHydrationWarning>
      <body className="antialiased">{children}</body>
    </html>
  );
}
