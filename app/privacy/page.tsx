import type { ReactNode } from "react";

export const metadata = {
  title: "Privacy Policy — PrayerKey",
  description: "PrayerKey privacy policy. No account, no tracking, no data stored.",
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
          Last updated: April 2026
        </p>
      </div>

      {/* ── Sections ── */}
      <div style={{ display: "flex", flexDirection: "column", gap: "0" }}>

        <LegalSection title="Our Commitment to Your Privacy">
          PrayerKey is built on a simple principle: you should be able to use a church tool without handing over your personal information to do it. We do not require you to create an account. We do not ask for your name, email address or phone number. We designed PrayerKey from the ground up to be as privacy-respecting as possible while still delivering powerful AI-driven features. This Privacy Policy explains what information is processed when you use PrayerKey, how it is used, and what your rights are.
        </LegalSection>

        <LegalSection title="Information We Do Not Collect">
          Because PrayerKey requires no account or registration, we do not collect or store your name, email address, phone number, date of birth or any other personal identifier. We do not build a user profile for you. We do not store your prayer requests or the text of prayers generated for you after your session ends. We do not retain audio from live sermon sessions. Your personal faith journey is your own and PrayerKey has no interest in recording or monetising it.
        </LegalSection>

        <LegalSection title="Information That Is Processed During Use">
          When you use PrayerKey, certain information is necessarily processed in order to deliver the service.

          {"\n\n"}When you use the prayer generator, the text you type is sent to an AI language model to generate your prayer. This text is processed in real time to produce the response you see on screen. It is not stored by PrayerKey after the request is completed.

          {"\n\n"}When you use the live sermon verse detection feature, audio from your microphone is processed by a speech recognition service that converts it to text. That text is then analysed to detect Bible verse references. The audio itself is not recorded or stored. The transcription is processed in memory and used only to identify verses in the moment.

          {"\n\n"}When you use Bible search, your search query is sent to an AI service to retrieve relevant Bible verses and cross-references. The query is not stored after the response is returned.
        </LegalSection>

        <LegalSection title="Cookies and Local Storage">
          PrayerKey may use browser cookies or local storage for basic session functionality, such as remembering your preferred Bible translation during a live sermon session. These are functional cookies only. We do not use tracking cookies. We do not use advertising cookies. We do not use cookies to follow you across other websites or build a behavioural profile of you.
        </LegalSection>

        <LegalSection title="Third-Party AI Services">
          PrayerKey uses third-party artificial intelligence services to power its features. These providers receive the minimum data necessary to process your request, specifically the text you provide or the audio from your microphone during a live session. These providers are bound by their own privacy policies and do not use your data to train their public models based on your individual requests. PrayerKey does not share your data with advertisers, data brokers or any unrelated third parties.
        </LegalSection>

        <LegalSection title="Children's Privacy">
          PrayerKey does not knowingly collect any information from children under the age of 13. The platform is intended for use by adults and church leaders. If you believe a child has submitted personal information through PrayerKey, please contact us and we will take appropriate action.
        </LegalSection>

        <LegalSection title="Data Security">
          All communication between your browser and PrayerKey is encrypted using HTTPS. We take reasonable technical measures to protect any data that passes through our systems. Because we do not store personal user data, the risk of a data breach exposing your information is significantly lower than platforms that build and maintain user databases.
        </LegalSection>

        <LegalSection title="Your Rights">
          Because PrayerKey does not store personal data linked to your identity, there is generally no personal data for us to retrieve, correct or delete on your behalf. If you have used PrayerKey and have a specific concern about data that may have been retained, you may contact us at prayerkey.com and we will respond to your request in good faith.

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
