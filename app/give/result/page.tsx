export const dynamic = "force-dynamic";

export default async function GivingResult({ searchParams }: { searchParams: { session_id?: string; cancelled?: string } }) {
  let paid = false;
  const cancelled = searchParams.cancelled === "1";
  const id = searchParams.session_id;
  if (!cancelled && id && /^cs_(test_|live_)?[a-zA-Z0-9]+$/.test(id) && process.env.STRIPE_SECRET_KEY) {
    try {
      const response = await fetch(`https://api.stripe.com/v1/checkout/sessions/${encodeURIComponent(id)}`, {
        headers: { Authorization: `Bearer ${process.env.STRIPE_SECRET_KEY}` },
        cache: "no-store", signal: AbortSignal.timeout(10000),
      });
      if (response.ok) {
        const session = await response.json();
        paid = session.payment_status === "paid" && session.metadata?.source === "android_home";
      }
    } catch { /* Never infer payment success from the return URL. */ }
  }
  return <main style={{ minHeight: "100vh", background: "#F5F5F7", color: "#1D1D1F", padding: "80px 24px", textAlign: "center" }}>
    <div style={{ maxWidth: 460, margin: "0 auto" }}>
      <p style={{ letterSpacing: 3 }}>PRAYERKEY</p>
      <h1>{paid ? "Thank you for your support." : cancelled ? "Checkout cancelled." : "Checking your contribution."}</h1>
      <p style={{ color: "#6E6E73", lineHeight: 1.7 }}>{paid ? "Your payment has been confirmed by Stripe. Your generosity helps keep PrayerKey available to everyone." : cancelled ? "You left checkout without completing it. You can return to PrayerKey whenever you are ready." : "We cannot confirm payment on this page yet. Check your Stripe receipt before starting another payment."}</p>
      <p>You can close this browser tab and return to PrayerKey.</p>
      <a href="/" style={{ color: "#B07C1F" }}>Visit PrayerKey</a>
    </div>
  </main>;
}
