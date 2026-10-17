import type { ReactNode } from "react";

export const metadata = {
  title: "Support — PrayerKey",
  description: "Help with the PrayerKey app: your journal, reminders, church notes, backups and contacting us.",
};

const EMAIL = "support@prayerkey.com";

export default function SupportPage() {
  return (
    <article style={{ maxWidth: "760px", margin: "0 auto", padding: "0 0 100px" }}>
      <div className="animate-fadeUp" style={{ marginBottom: "48px" }}>
        <h1 style={{
          fontSize: "clamp(32px, 5vw, 56px)", fontWeight: 800, color: "var(--pk-text)",
          margin: "0 0 16px", letterSpacing: "-0.03em", lineHeight: 1.05,
        }}>
          Support
        </h1>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, lineHeight: 1.7 }}>
          Write to us at <a href={`mailto:${EMAIL}`} style={{ color: "#6200ED", fontWeight: 600 }}>{EMAIL}</a> and
          a person will answer, usually within two working days. Tell us which phone you use and what happened.
        </p>
      </div>

      <Question title="Where is my journal kept?">
        On your phone, and only there. PrayerKey has no account and no copy of anything you write. If you change
        phones, make a backup first: in the app open You, choose Export my data, and save the file somewhere you
        trust. On the new phone, open You, choose Restore my data and pick that file.
      </Question>

      <Question title="I deleted the app. Can you get my journal back?">
        We cannot, because it was never sent to us. Deleting the app deletes what was in it. If you made a backup
        file before (You, Export my data), you can restore it.
      </Question>

      <Question title="My reminders are not arriving">
        Check that notifications are allowed for PrayerKey in your phone&apos;s Settings, then open the app once. On
        iPhone the coming days&apos; reminders are planned each time the app is opened, so opening it now and then
        keeps them coming. On Android, battery saver can delay reminders; allowing PrayerKey to run in the
        background helps.
      </Question>

      <Question title="Church notes are not hearing the sermon">
        Allow the microphone when asked, keep the phone face up and near the speaker, and choose the language the
        preacher speaks. If you turned the microphone off, turn it back on for PrayerKey in Settings.
      </Question>

      <Question title="How do I lock my journal?">
        In the app open You and turn on Lock journal. Your phone&apos;s own Face ID, fingerprint or passcode opens it.
        If your phone has no lock set, there is nothing to lock it with.
      </Question>

      <Question title="Is PrayerKey free?" last>
        Yes. There are no adverts and no subscriptions, and nothing you write is sold or shared.
        Read our <a href="/privacy" style={{ color: "#6200ED", fontWeight: 600 }}>privacy policy</a> for the details.
      </Question>
    </article>
  );
}

function Question({ title, children, last = false }: { title: string; children: ReactNode; last?: boolean }) {
  return (
    <section style={{ padding: "clamp(24px, 3.5vw, 36px) 0", borderBottom: last ? "none" : "1px solid var(--pk-border)" }}>
      <h2 style={{
        fontSize: "clamp(17px, 2vw, 21px)", fontWeight: 700, color: "var(--pk-text)",
        margin: "0 0 12px", letterSpacing: "-0.02em", lineHeight: 1.25,
      }}>
        {title}
      </h2>
      <p style={{ fontSize: "clamp(14px, 1.3vw, 16px)", color: "var(--pk-text-2)", lineHeight: 1.85, margin: 0 }}>
        {children}
      </p>
    </section>
  );
}
