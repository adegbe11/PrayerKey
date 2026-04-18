import type { Metadata } from 'next';
import {
  Inter,
  Playfair_Display,
  EB_Garamond,
  Cormorant_Garamond,
  Lora,
  Libre_Baskerville,
  Cinzel,
  Josefin_Sans,
  Raleway,
} from 'next/font/google';
import './globals.css';
import Preloader from '@/components/Preloader';
import SmoothScroll from '@/components/SmoothScroll';
import Cursor from '@/components/Cursor';

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

const cormorant = Cormorant_Garamond({
  subsets: ['latin'],
  variable: '--font-cormorant',
  style: ['normal', 'italic'],
  weight: ['300', '400', '500', '600', '700'],
  display: 'swap',
});

const lora = Lora({
  subsets: ['latin'],
  variable: '--font-lora',
  style: ['normal', 'italic'],
  weight: ['400', '500', '600', '700'],
  display: 'swap',
});

const baskerville = Libre_Baskerville({
  subsets: ['latin'],
  variable: '--font-baskerville',
  style: ['normal', 'italic'],
  weight: ['400', '700'],
  display: 'swap',
});

const cinzel = Cinzel({
  subsets: ['latin'],
  variable: '--font-cinzel',
  weight: ['400', '500', '600', '700', '800', '900'],
  display: 'swap',
});

const josefin = Josefin_Sans({
  subsets: ['latin'],
  variable: '--font-josefin',
  style: ['normal', 'italic'],
  weight: ['100', '200', '300', '400', '500', '600', '700'],
  display: 'swap',
});

const raleway = Raleway({
  subsets: ['latin'],
  variable: '--font-raleway',
  style: ['normal', 'italic'],
  weight: ['100', '200', '300', '400', '500', '600', '700', '800', '900'],
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
    <html
      lang="en"
      className={`${inter.variable} ${playfair.variable} ${garamond.variable} ${cormorant.variable} ${lora.variable} ${baskerville.variable} ${cinzel.variable} ${josefin.variable} ${raleway.variable}`}
      suppressHydrationWarning
    >
      <body className="antialiased">
        <SmoothScroll />
        <Preloader />
        <Cursor />
        {children}
      </body>
    </html>
  );
}
