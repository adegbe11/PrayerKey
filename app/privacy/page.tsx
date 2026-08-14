import type { ReactNode } from "react";

export const metadata = {
  title: "Privacy Policy — PrayerKey",
  description: "How PrayerKey processes and protects your information.",
};

export default function PrivacyPage() {
  return (
    <article style={{ maxWidth: "760px", margin: "0 auto", padding: "0 0 100px" }}>

      {/* ── Hero ── */}
      <div className="animate-fadeUp" style={{ marginBottom: "64px" }}>
        <div style={{
          display:      "inline-flex",
          alignItems:   "center",
          padding:      "4px 12px",
          border:       "1.5px solid var(--pk-border-2)",
          borderRadius: "4px",
          marginBottom: "24px",
          background:   "var(--pk-card)",
          boxShadow:    "3px 3px 0 0 var(--pk-border)",
        }}>
          <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-text-3)", letterSpacing: "0.12em", textTransform: "uppercase" }}>
            Legal
          </span>
        </div>

        <h1 style={{
          fontSize:      "clamp(32px, 5vw, 56px)",
          fontWeight:    800,
          color:         "var(--pk-text)",
          margin:        "0 0 16px",
          letterSpacing: "-0.03em",
          lineHeight:    1.05,
        }}>
          Privacy Policy
        </h1>
        <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: 0, letterSpacing: "0.02em" }}>
          Last updated: August 9, 2026
        </p>
      </div>

      {/* ── Sections ── */}
      <div style={{ display: "flex", flexDirection: "column", gap: "0" }}>

        <LegalSection title="Our Commitment to Your Privacy">
          PrayerKey is built on a simple principle: you should be able to use a church tool without handing over your personal information to do it. We do not require you to create an account. We do not ask for your name, email address or phone number. We designed PrayerKey from the ground up to be as privacy-respecting as possible while still delivering powerful AI-driven features. This Privacy Policy explains what information is processed when you use PrayerKey, how it is used, and what your rights are.
        </LegalSection>

        <LegalSection title="Information We Do Not Collect">
          The Android app does not require an account and does not use advertising trackers. Prayer requests, journal entries, saved verses, preferences, sermon notes and optional sermon audio backups are stored locally on your device so the app can work offline. PrayerKey does not sell this information or use it for advertising.
        </LegalSection>

        <LegalSection title="Information That Is Processed During Use">
          When you use PrayerKey, certain information is necessarily processed in order to deliver the service.

          {"\n\n"}When you use the prayer generator, the text you type is sent to an AI language model to generate your prayer. This text is processed in real time to produce the response you see on screen. It is not stored by PrayerKey after the request is completed.

          {"\n\n"}When you start Church listening, microphone audio is recorded to a private file on your device and streamed to our transcription provider to produce live text. The resulting transcript is arranged into sermon notes and stored locally. The local audio backup protects an active session during network interruption. You can remove locally stored app data by deleting it in the app or uninstalling PrayerKey.

          {"\n\n"}When an online Bible source is needed, the requested Bible reference and translation are sent to that provider. Most Bible reading and daily content is bundled for offline use.
        </LegalSection>

        <LegalSection title="Cookies and Local Storage">
          PrayerKey may use browser cookies or local storage for basic session functionality, such as remembering your preferred Bible translation during a live sermon session. These are functional cookies only. We do not use tracking cookies. We do not use advertising cookies. We do not use cookies to follow you across other websites or build a behavioural profile of you.
        </LegalSection>

        <LegalSection title="Third-Party AI Services">
          PrayerKey uses third-party services to provide transcription and selected online features. Those providers receive only the information needed to complete the requested feature, such as microphone audio during an active Church session or a Bible reference during an online lookup. Their handling of that information is governed by their own terms and privacy policies. PrayerKey does not share your data with advertisers or data brokers.
        </LegalSection>

        <LegalSection title="Children's Privacy">
          PrayerKey does not knowingly collect any information from children under the age of 13. The platform is intended for use by adults and church leaders. If you believe a child has submitted personal information through PrayerKey, please contact us and we will take appropriate action.
        </LegalSection>

        <LegalSection title="Data Security">
          Network communication uses encrypted HTTPS or secure WebSocket connections in production. Android app data is stored in the app's private storage and Android backup is disabled. If you enable Journal Lock, PrayerKey also hides journal screens from screenshots and recent-app previews. Device compromise, rooting or exporting content can reduce these protections.
        </LegalSection>

        <LegalSection title="Your Rights">
          Most Android data is stored only on your device and can be edited or deleted in the app. Uninstalling PrayerKey removes its private local data. If you have used an online feature and have a question about information processed by our service, contact us through prayerkey.com.

          {"\n\n"}If you are located in the European Union or United Kingdom, you have rights under the GDPR and UK GDPR including the right to access, rectify and erase personal data. If you are located in California, you have rights under the CCPA. We respect these rights and will respond to legitimate requests.
        </LegalSection>

        <LegalSection title="Links to Other Websites">
          PrayerKey may contain links to external websites or resources. We are not responsible for the privacy practices of those sites. We encourage you to read the privacy policies of any external site you visit from a link on PrayerKey.
        </LegalSection>

        <LegalSection title="Changes to This Privacy Policy">
          We may update this Privacy Policy from time to time to reflect changes in the service or in applicable law. When we make changes, we will update the date at the top of this page. We encourage you to review this page periodically. Continued use of PrayerKey after changes are posted means you accept the updated policy.
        </LegalSection>

        <LegalSection title="Contact Us" last>
          If you have any questions about this Privacy Policy or how your data is handled when you use PrayerKey, please reach out to us through the PrayerKey website at prayerkey.com. We take privacy seriously and will respond to all genuine enquiries.
        </LegalSection>

      </div>
    </article>
  );
}

function LegalSection({ title, children, last = false }: { title: string; children: ReactNode; last?: boolean }) {
  return (
    <div style={{
      padding:      "clamp(28px, 4vw, 44px) 0",
      borderBottom: last ? "none" : "1px solid var(--pk-border)",
    }}>
      <h2 style={{
        fontSize:      "clamp(17px, 2vw, 21px)",
        fontWeight:    700,
        color:         "var(--pk-text)",
        margin:        "0 0 14px",
        letterSpacing: "-0.02em",
        lineHeight:    1.25,
      }}>
        {title}
      </h2>
      <div style={{
        fontSize:   "clamp(14px, 1.3vw, 16px)",
        color:      "var(--pk-text-2)",
        lineHeight: 1.85,
        whiteSpace: "pre-wrap",
      }}>
        {children}
      </div>
    </div>
  );
}
