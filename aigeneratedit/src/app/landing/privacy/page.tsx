'use client'

const BRAND = '#FF5A5F'

function Nav() {
  return (
    <nav style={{
      position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100,
      padding: '0 5vw', height: 68,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      background: 'rgba(250,250,250,0.92)',
      backdropFilter: 'blur(16px)',
      borderBottom: '1px solid rgba(0,0,0,0.07)',
    }}>
      <a href="/landing" style={{ display: 'flex', alignItems: 'center', gap: 9, textDecoration: 'none' }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/icons/icon-192-round.png" alt="Nomapal" style={{ width: 32, height: 32, borderRadius: '50%' }} />
        <span style={{ fontSize: 18, fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A' }}>nomapal</span>
      </a>
      <a href="/landing" style={{
        padding: '10px 22px', background: BRAND, color: '#fff',
        borderRadius: 9999, fontSize: 14, fontWeight: 700, textDecoration: 'none',
      }}>Back to home</a>
    </nav>
  )
}

const SECTIONS = [
  {
    title: 'What information we collect',
    body: 'We collect the information you give us when you sign up, such as your name, email address, and profile photo. We also collect trip details you add to your profile, like destinations and travel dates. When you use the app, we collect usage data to help us improve the product.',
  },
  {
    title: 'How we use your information',
    body: 'We use your information to show you relevant travel matches, send you notifications about new matches and messages, and keep your account secure. We do not sell your personal information to anyone.',
  },
  {
    title: 'What we share and with whom',
    body: 'Your public profile information is visible to other Nomapal users. We share limited data with service providers who help us run the app, like our hosting and analytics partners. All of these partners are required to handle your data securely.',
  },
  {
    title: 'Your choices',
    body: 'You can update or delete your profile information at any time in the app settings. You can also delete your account entirely, which removes all your data from our systems. To request a copy of your data or ask questions about how we use it, email us at hello@nomapal.com.',
  },
  {
    title: 'Cookies',
    body: 'Our website uses cookies to remember your preferences and understand how visitors use the site. You can turn off cookies in your browser settings, though some parts of the site may not work as well if you do.',
  },
  {
    title: 'Changes to this policy',
    body: 'We may update this privacy policy from time to time. If we make a significant change, we will notify you through the app or by email before the change takes effect.',
  },
  {
    title: 'Contact us',
    body: 'If you have questions about this privacy policy or how we handle your information, email us at hello@nomapal.com. We aim to reply within two business days.',
  },
]

export default function PrivacyPage() {
  return (
    <div style={{ background: '#FAFAFA', fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', system-ui, sans-serif", minHeight: '100vh' }}>
      <Nav />
      <div style={{ maxWidth: 760, margin: '0 auto', padding: '120px 5vw 96px' }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 8,
          padding: '7px 18px', borderRadius: 9999,
          background: `${BRAND}12`, border: `1px solid ${BRAND}30`,
          marginBottom: 28,
        }}>
          <span style={{ fontSize: 13, fontWeight: 700, color: BRAND, letterSpacing: '0.04em' }}>LEGAL</span>
        </div>
        <h1 style={{ fontSize: 'clamp(36px, 5vw, 56px)', fontWeight: 900, letterSpacing: '-0.04em', color: '#0A0A0A', marginBottom: 12 }}>
          Privacy Policy
        </h1>
        <p style={{ fontSize: 15, color: '#888', marginBottom: 56 }}>Last updated April 2026</p>

        <p style={{ fontSize: 17, lineHeight: 1.8, color: '#555', marginBottom: 48 }}>
          At Nomapal, your privacy matters to us. This policy explains what information we collect, how we use it, and what choices you have.
        </p>

        {SECTIONS.map((s, i) => (
          <div key={i} style={{ marginBottom: 40 }}>
            <h2 style={{ fontSize: 22, fontWeight: 800, color: '#0A0A0A', marginBottom: 12, letterSpacing: '-0.02em' }}>
              {s.title}
            </h2>
            <p style={{ fontSize: 16, lineHeight: 1.8, color: '#555' }}>{s.body}</p>
          </div>
        ))}
      </div>

      <footer style={{ background: '#0A0A0A', padding: '32px 5vw', textAlign: 'center' }}>
        <p style={{ fontSize: 13, color: '#555' }}>
          © {new Date().getFullYear()} Nomapal. All rights reserved.
          {' · '}
          <a href="/landing/about" style={{ color: '#555', textDecoration: 'none' }}>About</a>
          {' · '}
          <a href="/landing/faq" style={{ color: '#555', textDecoration: 'none' }}>FAQ</a>
          {' · '}
          <a href="/landing/terms" style={{ color: '#555', textDecoration: 'none' }}>Terms</a>
        </p>
      </footer>
    </div>
  )
}
