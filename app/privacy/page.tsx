import InView from "@/components/home/InView";
import { ReadingHero, ReadingLayout, Section } from "@/components/reading/Reading";
import "@/components/reading/reading.css";

export const metadata = {
  title: "Privacy Policy — PrayerKey",
  description: "PrayerKey privacy policy. No account, no tracking, no data stored.",
};

const TOC = [
  { id: "commitment",  label: "Our Commitment" },
  { id: "not-collected", label: "What We Do Not Collect" },
  { id: "processed",   label: "What Is Processed During Use" },
  { id: "app",         label: "The PrayerKey App" },
  { id: "cookies",     label: "Cookies and Local Storage" },
  { id: "third-party", label: "Third-Party Services" },
  { id: "children",    label: "Children's Privacy" },
  { id: "security",    label: "Data Security" },
  { id: "rights",      label: "Your Rights" },
  { id: "links",       label: "Links to Other Websites" },
  { id: "changes",     label: "Changes to This Policy" },
  { id: "contact",     label: "Contact Us" },
];

export default function PrivacyPage() {
  return (
    <div className="rd">
      <InView />
      <ReadingHero
        crumbs={[{ href: "/", label: "Home" }, { label: "Privacy Policy" }]}
        title="Privacy Policy"
        meta="Last updated: October 2026"
      />

      <ReadingLayout toc={TOC}>

        <Section id="commitment" title="Our Commitment to Your Privacy">
          <p>
            PrayerKey is built on a simple principle: you should be able to use a church tool without handing over
            your personal information to do it. We do not require you to create an account. We do not ask for your
            name, email address or phone number. We designed PrayerKey from the ground up to be as
            privacy-respecting as possible while still delivering useful features. This Privacy Policy explains
            what information is processed when you use PrayerKey, how it is used, and what your rights are.
          </p>
        </Section>

        <Section id="not-collected" title="Information We Do Not Collect">
          <p>
            Because PrayerKey requires no account or registration, we do not collect or store your name, email
            address, phone number, date of birth or any other personal identifier. We do not build a user profile
            for you. We do not store your prayer requests or the text of prayers generated for you after your
            session ends. We do not retain audio from live sermon sessions. Your personal faith journey is your own
            and PrayerKey has no interest in recording or monetising it.
          </p>
        </Section>

        <Section id="processed" title="Information That Is Processed During Use">
          <p>When you use PrayerKey, certain information is necessarily processed in order to deliver the service.</p>
          <p>
            When you use the prayer generator on the website, your prayer is composed in your browser from
            Scripture-based prayer patterns, and the text you type is not sent to PrayerKey to do this. Where a
            prayer is composed on our servers instead, the text you provide is processed in real time to produce
            the response you see on screen. It is not stored by PrayerKey after the request is completed.
          </p>
          <p>
            When you use the live sermon verse detection feature, audio from your microphone is processed by a
            speech recognition service that converts it to text. That text is then analysed to detect Bible verse
            references. The audio itself is not recorded or stored. The transcription is processed in memory and
            used only to identify verses in the moment.
          </p>
          <p>
            When you use Bible search, your search query is sent to our servers and, for some translations, to a
            third-party search service, to retrieve relevant Bible verses and cross-references. The query is not
            stored after the response is returned.
          </p>
        </Section>

        <Section id="app" title="The PrayerKey App (iPhone and Android)">
          <p>
            The PrayerKey app has no account and no sign-in. Everything you write or keep in it, your journal,
            prayers, sermon notes, saved verses, memory verses, photos you attach, streaks and settings, is stored
            only on your phone. PrayerKey does not upload it, cannot see it, and has no copy of it. Deleting the app
            deletes it. If you want a copy, the app can create a backup file that you save or share yourself.
          </p>
          <p>
            A few features need the internet. When you ask for a prayer, the words you type are sent to
            prayerkey.com, a prayer is composed from Scripture and sent back, and the request is not stored. Bible
            search sends your search words to prayerkey.com. Translations other than the King James Version are
            fetched from bible-api.com using the verse reference. The audio Bible streams public-domain recordings
            from publicdomainaudiobibles.com. None of these requests carries your name or any account, because
            there is none.
          </p>
          <p>
            Church notes and spoken journal entries use the microphone, only while you are recording, and only
            after you allow it. Speech is turned into text by your phone: on iPhone by Apple&apos;s speech
            recognition, on the device itself where your iPhone supports it; on Android by your phone&apos;s speech
            service or an offline model downloaded once to your phone. A recording of the sermon is kept on your
            phone so the transcript can be improved later, and deleting the note deletes the recording. PrayerKey
            never receives your audio.
          </p>
          <p>
            Reminders are scheduled on your phone. If you turn on the journal lock, your phone&apos;s own Face ID,
            Touch ID or passcode check is used; PrayerKey never sees biometric data. The app contains no
            advertising, no analytics and no tracking of any kind, and it does not share data with anyone.
          </p>
        </Section>

        <Section id="cookies" title="Cookies and Local Storage">
          <p>
            PrayerKey may use browser cookies or local storage for basic session functionality, such as remembering
            your preferred Bible translation during a live sermon session. These are functional cookies only. We do
            not use tracking cookies. We do not use advertising cookies. We do not use cookies to follow you across
            other websites or build a behavioural profile of you.
          </p>
        </Section>

        <Section id="third-party" title="Third-Party Services">
          <p>
            PrayerKey uses third-party services, such as speech recognition and search providers, to power some of
            its features. These providers receive the minimum data necessary to process your request, specifically
            the text you provide or the audio from your microphone during a live session. These providers are bound
            by their own privacy policies and do not use your data to train their public models based on your
            individual requests. PrayerKey does not share your data with advertisers, data brokers or any unrelated
            third parties.
          </p>
        </Section>

        <Section id="children" title="Children's Privacy">
          <p>
            PrayerKey does not knowingly collect any information from children under the age of 13. The platform is
            intended for use by adults and church leaders. If you believe a child has submitted personal information
            through PrayerKey, please contact us and we will take appropriate action.
          </p>
        </Section>

        <Section id="security" title="Data Security">
          <p>
            All communication between your browser and PrayerKey is encrypted using HTTPS. We take reasonable
            technical measures to protect any data that passes through our systems. Because we do not store personal
            user data, the risk of a data breach exposing your information is significantly lower than platforms that
            build and maintain user databases.
          </p>
        </Section>

        <Section id="rights" title="Your Rights">
          <p>
            Because PrayerKey does not store personal data linked to your identity, there is generally no personal
            data for us to retrieve, correct or delete on your behalf. If you have used PrayerKey and have a specific
            concern about data that may have been retained, you may contact us at prayerkey.com and we will respond
            to your request in good faith.
          </p>
          <p>
            If you are located in the European Union or United Kingdom, you have rights under the GDPR and UK GDPR
            including the right to access, rectify and erase personal data. If you are located in California, you
            have rights under the CCPA. We respect these rights and will respond to legitimate requests.
          </p>
        </Section>

        <Section id="links" title="Links to Other Websites">
          <p>
            PrayerKey may contain links to external websites or resources. We are not responsible for the privacy
            practices of those sites. We encourage you to read the privacy policies of any external site you visit
            from a link on PrayerKey.
          </p>
        </Section>

        <Section id="changes" title="Changes to This Privacy Policy">
          <p>
            We may update this Privacy Policy from time to time to reflect changes in the service or in applicable
            law. When we make changes, we will update the date at the top of this page. We encourage you to review
            this page periodically. Continued use of PrayerKey after changes are posted means you accept the updated
            policy.
          </p>
        </Section>

        <Section id="contact" title="Contact Us">
          <p>
            If you have any questions about this Privacy Policy or how your data is handled when you use PrayerKey,
            please reach out to us through the PrayerKey website at prayerkey.com. We take privacy seriously and
            will respond to all genuine enquiries.
          </p>
        </Section>

        <p className="rd-after">
          Questions about this policy? Write to <a href="mailto:support@prayerkey.com">support@prayerkey.com</a>, or read
          the <a href="/terms">Terms of Service</a>.
        </p>

      </ReadingLayout>
    </div>
  );
}
