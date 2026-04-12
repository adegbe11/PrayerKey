import Link from 'next/link';

// Bird-book icon — same as Navbar
function BirdBookIcon({ size = 28 }: { size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path d="M24 26 C18 22, 10 20, 4 22 C8 18, 16 16, 24 20 Z" fill="#000" />
      <path d="M24 26 C17 19, 8 15, 2 16 C6 12, 15 12, 24 18 Z" fill="#000" opacity="0.6" />
      <path d="M24 26 C19 16, 12 10, 5 10 C9 7, 17 9, 24 16 Z" fill="#000" opacity="0.35" />
      <path d="M24 26 C30 22, 38 20, 44 22 C40 18, 32 16, 24 20 Z" fill="#000" />
      <path d="M24 26 C31 19, 40 15, 46 16 C42 12, 33 12, 24 18 Z" fill="#000" opacity="0.6" />
      <path d="M24 26 C29 16, 36 10, 43 10 C39 7, 31 9, 24 16 Z" fill="#000" opacity="0.35" />
      <rect x="21" y="22" width="6" height="16" rx="1" fill="#000" />
      <ellipse cx="24" cy="19" rx="4.5" ry="4" fill="#000" />
      <circle cx="25.5" cy="18.5" r="1.2" fill="#FFE500" />
      <path d="M24 21.5 L26 23 L22 23 Z" fill="#000" />
      <path d="M21 37 L18 43 M24 38 L24 44 M27 37 L30 43" stroke="#000" strokeWidth="1.5" strokeLinecap="round" />
      <line x1="21" y1="27" x2="27" y2="27" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="30" x2="27" y2="30" stroke="#FFE500" strokeWidth="0.8" />
      <line x1="21" y1="33" x2="27" y2="33" stroke="#FFE500" strokeWidth="0.8" />
    </svg>
  );
}

export default function Footer() {
  return (
    <footer style={{ background: '#0A0910', borderTop: '3px solid rgba(255,255,255,0.15)' }}>
      {/* Final CTA strip */}
      <div
        className="w-full py-12 px-6"
        style={{
          background: '#FF2D78',
          borderBottom: '3px solid #000',
        }}
      >
        <div className="container flex flex-col md:flex-row items-center justify-between gap-8">
          <div>
            <p className="font-display font-black text-white leading-tight" style={{ fontSize: 'clamp(28px, 4vw, 52px)' }}>
              Stop procrastinating.
              <br />
              <span style={{ color: '#FFE500' }}>Format your book today.</span>
            </p>
            <p className="text-base font-semibold mt-2" style={{ color: 'rgba(255,255,255,0.65)' }}>
              Free forever. No credit card. No account.
            </p>
          </div>
          <Link
            href="/editor"
            className="brutal-btn-yellow shrink-0"
            style={{ fontSize: '16px', padding: '18px 36px', whiteSpace: 'nowrap' }}
          >
            Format My Book →
          </Link>
        </div>
      </div>

      {/* Footer body */}
      <div className="container py-10">
        <div className="grid md:grid-cols-4 gap-10 mb-12">
          <div className="md:col-span-2">
            <Link href="/" className="flex items-center gap-3 mb-5">
              <div className="w-10 h-10 flex items-center justify-center shrink-0 overflow-hidden"
                style={{ background: '#FFE500', border: '2px solid #000', boxShadow: '3px 3px 0px #000' }}>
                <BirdBookIcon size={28} />
              </div>
              <div style={{ borderLeft: '1.5px solid rgba(255,255,255,0.15)', paddingLeft: '12px' }}>
                <div className="font-black leading-none" style={{ fontSize: '13px', color: '#fff', letterSpacing: '0.04em' }}>BOOKSANE</div>
                <div className="font-medium leading-none mt-0.5" style={{ fontSize: '9px', color: 'rgba(255,255,255,0.35)', letterSpacing: '0.12em', textTransform: 'uppercase' }}>Formatting System</div>
              </div>
            </Link>
            <p className="text-sm leading-relaxed max-w-xs" style={{ color: 'rgba(255,255,255,0.4)' }}>
              The fastest, most beautiful book formatting tool in the world. Turn your text into a published book in seconds.
            </p>
          </div>

          <div>
            <p className="text-xs font-black uppercase tracking-widest mb-5" style={{ color: 'rgba(255,255,255,0.35)' }}>Product</p>
            <ul className="flex flex-col gap-3">
              {[{ label: 'Editor', href: '/editor' }, { label: 'Templates', href: '#templates' }, { label: 'How it works', href: '#how-it-works' }, { label: 'Pricing', href: '#pricing' }].map(link => (
                <li key={link.label}>
                  <Link href={link.href} className="text-sm font-semibold hover:text-white transition-colors" style={{ color: 'rgba(255,255,255,0.4)' }}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <p className="text-xs font-black uppercase tracking-widest mb-5" style={{ color: 'rgba(255,255,255,0.35)' }}>Company</p>
            <ul className="flex flex-col gap-3">
              {[{ label: 'About', href: '#' }, { label: 'Privacy', href: '#' }, { label: 'Terms', href: '#' }, { label: 'Contact', href: 'mailto:hello@booksane.com' }].map(link => (
                <li key={link.label}>
                  <Link href={link.href} className="text-sm font-semibold hover:text-white transition-colors" style={{ color: 'rgba(255,255,255,0.4)' }}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="pt-6 flex flex-col md:flex-row items-center justify-between gap-4" style={{ borderTop: '1px solid rgba(255,255,255,0.08)' }}>
          <p className="text-xs font-semibold" style={{ color: 'rgba(255,255,255,0.3)' }}>
            © {new Date().getFullYear()} Booksane. All rights reserved.
          </p>
          <p className="text-xs font-semibold" style={{ color: 'rgba(255,255,255,0.3)' }}>
            Made for authors everywhere. ✦
          </p>
        </div>
      </div>
    </footer>
  );
}
