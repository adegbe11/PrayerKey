import InView from "@/components/home/InView";
import { ReadingHero, ReadingLayout, Section, Faq, ReadingEnd } from "@/components/reading/Reading";
import "@/components/reading/reading.css";

export const metadata = {
  title: "Support — PrayerKey",
  description: "Help with the PrayerKey app: your journal, reminders, church notes, backups and contacting us.",
};

const EMAIL = "support@prayerkey.com";

const QUESTIONS = [
  { q: "Where is my journal kept?",
    a: <>On your phone, and only there. PrayerKey has no account and no copy of anything you write. If you change
      phones, make a backup first: in the app open You, choose Export my data, and save the file somewhere you
      trust. On the new phone, open You, choose Restore my data and pick that file.</> },
  { q: "I deleted the app. Can you get my journal back?",
    a: <>We cannot, because it was never sent to us. Deleting the app deletes what was in it. If you made a backup
      file before (You, Export my data), you can restore it.</> },
  { q: "My reminders are not arriving",
    a: <>Check that notifications are allowed for PrayerKey in your phone&apos;s Settings, then open the app once. On
      iPhone the coming days&apos; reminders are planned each time the app is opened, so opening it now and then
      keeps them coming. On Android, battery saver can delay reminders; allowing PrayerKey to run in the
      background helps.</> },
  { q: "Church notes are not hearing the sermon",
    a: <>Allow the microphone when asked, keep the phone face up and near the speaker, and choose the language the
      preacher speaks. If you turned the microphone off, turn it back on for PrayerKey in Settings.</> },
  { q: "How do I lock my journal?",
    a: <>In the app open You and turn on Lock journal. Your phone&apos;s own Face ID, fingerprint or passcode opens it.
      If your phone has no lock set, there is nothing to lock it with.</> },
  { q: "Is PrayerKey free?",
    a: <>Yes. There are no adverts and no subscriptions, and nothing you write is sold or shared.
      Read our <a href="/privacy">privacy policy</a> for the details.</> },
];

export default function SupportPage() {
  return (
    <div className="rd">
      <InView />
      <ReadingHero
        crumbs={[{ href: "/", label: "Home" }, { label: "Support" }]}
        title="Support"
        lede={<>Write to us at <a href={`mailto:${EMAIL}`}>{EMAIL}</a> and a person will answer, usually within two
          working days. Tell us which phone you use and what happened.</>}
      />

      <ReadingLayout>
        <Section id="questions" title="Common questions">
          <Faq items={QUESTIONS} />
        </Section>

        <ReadingEnd title="Still need help?">
          <p>A person reads every message. Include which phone you use and what you were doing when it happened.</p>
          <a className="rd-end__mail" href={`mailto:${EMAIL}`}>{EMAIL}</a>
          <div className="rd-end__row">
            <a href={`mailto:${EMAIL}?subject=PrayerKey%20support`} className="rd-btn">Write to us</a>
            <a href="/privacy" className="rd-quiet">Privacy policy</a>
          </div>
        </ReadingEnd>
      </ReadingLayout>
    </div>
  );
}
