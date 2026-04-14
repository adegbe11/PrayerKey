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
    title: 'Who can use Nomapal',
    body: 'You must be at least 18 years old to use Nomapal. By creating an account, you confirm that you are 18 or older and that the information you provide is accurate.',
  },
  {
    title: 'Your account',
    body: 'You are responsible for keeping your account details secure. Do not share your password with anyone. If you believe someone has accessed your account without permission, contact us immediately at hello@nomapal.com.',
  },
  {
    title: 'What you can and cannot do',
    body: 'You can use Nomapal to connect with other travelers, plan trips, and chat with matches. You may not use Nomapal to harass other users, post false information, spam other members, or use the app for any illegal purpose. We may suspend or remove accounts that break these rules.',
  },
  {
    title: 'Content you post',
    body: 'When you post photos, messages, or other content on Nomapal, you keep ownership of that content. You give us permission to display it within the app. Do not post anything that is offensive, illegal, or that belongs to someone else without their permission.',
  },
  {
    title: 'Subscriptions and payments',
    body: 'Some Nomapal features require a paid subscription. Subscriptions renew automatically unless you cancel before the renewal date. You can cancel at any time through your app store account. We do not offer refunds for partial billing periods.',
  },
  {
    title: 'Limitation of liability',
    body: 'Nomapal helps you find other travelers but is not responsible for what happens once you decide to meet or travel with someone. Always use your judgment, meet in public places first, and take the same safety precautions you would with any new person.',
  },
  {
    title: 'Changes to these terms',
    body: 'We may update these terms from time to time. If we make a major change, we will let you know through the app or by email before it takes effect. Continuing to use Nomapal after a change means you accept the updated terms.',
  },
  {
    title: 'Questions',
    body: 'If you have questions about these terms, email us at hello@nomapal.com.',
  },
]

export default function TermsPage() {
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
          Terms of Service
        </h1>
        <p style={{ fontSize: 15, color: '#888', marginBottom: 56 }}>Last updated April 2026</p>

        <p style={{ fontSize: 17, lineHeight: 1.8, color: '#555', marginBottom: 48 }}>
          These terms explain what you can expect from Nomapal and what we expect from you. Please read them before using the app.
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
          <a href="/landing/privacy" style={{ color: '#555', textDecoration: 'none' }}>Privacy</a>
        </p>
      </footer>
    </div>
  )
}
