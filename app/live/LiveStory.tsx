import InView from "@/components/home/InView";

// The story under the live tool: what it is, how it works, how it compares,
// who it serves, and the questions churches ask. One idea per section.

const STEPS: { title: string; text: string }[] = [
  { title: "Open prayerkey.com/live", text: "in Chrome or Edge on the laptop you'll connect to your projector." },
  { title: "Tap to start listening", text: "and allow microphone access when the browser asks." },
  { title: "Click Projector", text: "to open the verse window. Drag it to your second display and click it once for full screen." },
  { title: "Turn on Auto", text: "so verses go on the screen by themselves, or leave it off and choose each one." },
  { title: "Let it run", text: "PrayerKey transcribes the sermon, identifies every Bible verse the pastor quotes, direct references and paraphrases alike, and presents them on the projector automatically." },
];

// the comparison, row by row: [feature, PrayerKey, Pewbeam, ProPresenter, EasyWorship]
const COMPARE: [string, string, string, string, string][] = [
  ["Price", "Free forever", "$20+/mo", "$399/yr", "$396/yr"],
  ["Auto verse detection", "Yes, real-time", "Yes", "Manual only", "Manual only"],
  ["Operator required", "No", "Optional", "Yes", "Yes"],
  ["Account / sign-up", "Not required", "Required", "Required", "Required"],
  ["Install software", "None, runs in the browser", "Browser", "Desktop app", "Desktop app"],
  ["Bible translations", "King James Version", "20+", "30+ (paid)", "20+"],
  ["Setup time", "~3 min", "~10 min", "~1 hour", "~1 hour"],
  ["Best for", "Small to mid churches", "Mid churches with budget", "Megachurches", "Traditional churches"],
];

const WHO: [string, string][] = [
  ["Small churches", "that can't afford ProPresenter or EasyWorship."],
  ["Home churches and house fellowships", "that meet in living rooms."],
  ["Bible study and small groups", "show every verse referenced."],
  ["Sunday school and youth ministries", "keep kids following along."],
  ["Funerals, weddings, and special services", "display verses without a tech operator."],
  ["Online streaming churches", "verses overlay on the broadcast feed."],
  ["Congregants who miss the verses pastors quote", "look them up later."],
];

const FAQ: { q: string; a: string }[] = [
  { q: "How does PrayerKey detect Bible verses during a sermon?",
    a: "PrayerKey listens through your laptop or USB microphone, transcribes the sermon in real time with speech recognition, and matches what's spoken against the entire Bible. Direct references like John 3:16 are matched almost instantly. Paraphrased quotes are caught by a second pass that compares the meaning of what was said with every verse." },
  { q: "Is PrayerKey really free for churches?",
    a: "Yes. PrayerKey is 100% free, with no subscription, no premium tier, no ads, and no account. Open prayerkey.com/live and start using it." },
  { q: "Pewbeam vs PrayerKey: what's the difference?",
    a: "Pewbeam is paid software starting around $20/month per church. PrayerKey provides the same automatic verse detection and projection workflow for $0. Both run in a browser. PrayerKey requires no account, no install, and no credit card." },
  { q: "Do I need a tech operator to run PrayerKey during the sermon?",
    a: "No. PrayerKey can run in fully automatic mode. Press Start Listening, enable Auto, and every verse the pastor quotes appears on the projector with no clicks needed. A volunteer can also run it manually if your service prefers human pacing." },
  { q: "What if my pastor paraphrases instead of quoting Bible verses directly?",
    a: "PrayerKey runs two detection layers. The first matches direct references and exact quotes. The second compares the meaning of what was said with every verse in the Bible, so a line like God will never leave you is matched to Hebrews 13:5 even though those exact words don't appear in the verse." },
  { q: "Will it work on a Chromebook, Mac, or Windows laptop?",
    a: "Yes. PrayerKey works in any Chromium browser (Chrome or Edge) on Mac, Windows, Chromebook, and Linux. There's nothing to install. Mobile devices can use the prayer generator and Bible search, but the live sermon dashboard is desktop-first because it needs a microphone and a second display." },
  { q: "Does PrayerKey work for Catholic, Baptist, Methodist, or Pentecostal services?",
    a: "PrayerKey is denomination-neutral and used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, Reformed, and non-denominational churches around the world. Verses are shown in the King James Version, which is read in churches of every tradition." },
  { q: "How accurate is the Bible verse detection?",
    a: "Direct references are matched with near-100% accuracy. Paraphrases typically score 70 to 95% confidence. Each detection card shows a colored confidence dot: green for 90%+, yellow for 70 to 89%, orange for 60 to 69%." },
  { q: "Is PrayerKey a free alternative to ProPresenter or EasyWorship?",
    a: "Yes. ProPresenter, EasyWorship, and MediaShout cost between $200 and $2,000 per year and require a trained operator typing or clicking each verse. PrayerKey is free and detects verses automatically." },
  { q: "What microphone do I need?",
    a: "Any clear microphone works. Best options: a USB lapel mic worn by the pastor or a line-in feed from your church PA. Built-in laptop mics work if the laptop is within ten feet of the speaker." },
  { q: "Does PrayerKey record or store my sermon audio?",
    a: "No. PrayerKey never records or stores your audio. Your browser's built-in speech recognition turns the sermon into text as it is spoken (in Chrome and Edge this is done by the browser maker's speech service). Only that text is sent to PrayerKey for verse matching, and it is discarded the moment your session ends." },
  { q: "Can I use this for small churches or home churches?",
    a: "Absolutely. PrayerKey was built specifically so small churches, house churches, and independent ministries get the same quality of tools as well-funded megachurches, without paying $200 to $2,000 per year for traditional church software." },
];

const MORE: [string, string][] = [
  ["/docs", "How to use PrayerKey"],
  ["/pray", "The free prayer generator"],
  ["/bible", "Bible search and cross-references"],
  ["/prayer", "The library of 543 prayers"],
  ["/about", "About PrayerKey"],
];

export default function LiveStory() {
  return (
    <div className="lv">
      <InView />

      {/* what it is */}
      <section className="lv-lede" data-in="">
        <p className="lv-kicker">PrayerKey Live</p>
        <h2 className="lv-h2 lv-h2--xl">The free, real-time Bible verse projector for live sermons.</h2>
        <p className="lv-body lv-body--lg">
          PrayerKey Live is a browser-based church projection tool that listens with speech recognition,
          detects every Bible verse a pastor quotes during a sermon, and displays it on a connected projector
          or second screen in real time, with no operator, no install, and no cost.
        </p>
        <p className="lv-body">
          It&apos;s the free alternative to <strong>Pewbeam</strong>, <strong>ProPresenter</strong>,{" "}
          <strong>EasyWorship</strong>, and <strong>MediaShout</strong>, built for churches that don&apos;t
          have a $2,000-a-year software budget.
        </p>
      </section>

      {/* how it works */}
      <section className="lv-sec" id="how-it-works" data-in="">
        <h2 className="lv-h2">How real-time Bible verse detection works</h2>
        <p className="lv-body lv-center">
          PrayerKey turns any laptop with a microphone into a fully automatic verse display. The workflow is simple.
        </p>
        <ol className="lv-steps">
          {STEPS.map((s, i) => (
            <li key={s.title} style={{ "--i": i } as React.CSSProperties}>
              <span className="lv-steps__n" aria-hidden>{i + 1}</span>
              <p><strong>{s.title}</strong> {s.text}</p>
            </li>
          ))}
        </ol>
      </section>

      {/* how it compares */}
      <section className="lv-sec lv-sec--wide" id="alternatives" data-in="">
        <h2 className="lv-h2">PrayerKey vs Pewbeam vs ProPresenter vs EasyWorship</h2>
        <p className="lv-body lv-center">An honest comparison of features, pricing, and what each tool does well.</p>
        <div className="lv-table" role="region" aria-label="Comparison of church projection tools" tabIndex={0}>
          <table>
            <thead>
              <tr>
                <th scope="col"><span className="lv-sr">Feature</span></th>
                <th scope="col" className="lv-pk">PrayerKey</th>
                <th scope="col">Pewbeam</th>
                <th scope="col">ProPresenter</th>
                <th scope="col">EasyWorship</th>
              </tr>
            </thead>
            <tbody>
              {COMPARE.map(([label, ...cells]) => (
                <tr key={label}>
                  <th scope="row">{label}</th>
                  {cells.map((c, j) => <td key={j} className={j === 0 ? "lv-pk" : undefined}>{c}</td>)}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="lv-body lv-center lv-after">
          PrayerKey was built for the churches that <em>can&apos;t</em> spend $400 to $2,000 per year on software.
          Independent pastors, house churches, rural ministries, and small congregations get the same quality of
          tooling as a megachurch tech booth, at zero cost.
        </p>
      </section>

      {/* who it serves */}
      <section className="lv-sec" id="who" data-in="">
        <h2 className="lv-h2">Who PrayerKey Live is for</h2>
        <ul className="lv-who">
          {WHO.map(([who, why], i) => (
            <li key={who} style={{ "--i": i } as React.CSSProperties}>
              <strong>{who}</strong>
              <span>{why}</span>
            </li>
          ))}
        </ul>
      </section>

      {/* translations */}
      <section className="lv-sec lv-sec--wide" id="translations" data-in="">
        <h2 className="lv-h2">The King James Version, in full</h2>
        <p className="lv-body lv-center">
          Every verse PrayerKey finds and puts on the screen comes from the King James Version, all 66 books,
          word for word. It is in the public domain, so any church can project it freely.
        </p>
      </section>

      {/* privacy */}
      <section className="lv-quiet pk-bleed" id="privacy" data-nav="dark" data-in="">
        <h2 className="lv-quiet__title">
          <span>Privacy:</span> <span>we don&apos;t record your sermons.</span>
        </h2>
        <p className="lv-quiet__line">
          PrayerKey never records or stores your audio. Your browser&apos;s built-in speech recognition turns the
          sermon into text as it is spoken; in Chrome and Edge, that is done by the browser maker&apos;s speech service.
          Only the text reaches PrayerKey, for verse matching, and it is discarded the moment your session ends.
          No accounts, no storage, no analytics tied to your sermons.
        </p>
      </section>

      {/* questions */}
      <section className="lv-sec" id="faq" data-in="">
        <h2 className="lv-h2">Frequently asked questions</h2>
        <div className="lv-faq">
          {FAQ.map((f) => (
            <details key={f.q}>
              <summary>
                <span>{f.q}</span>
                <i aria-hidden />
              </summary>
              <p>{f.a}</p>
            </details>
          ))}
        </div>
      </section>

      {/* where next */}
      <nav className="lv-more" aria-label="Continue exploring PrayerKey" data-in="">
        <h2 className="lv-more__title">Continue exploring PrayerKey</h2>
        <ul>
          {MORE.map(([href, label]) => (
            <li key={href}><a href={href}>{label}</a></li>
          ))}
        </ul>
      </nav>

      {/* one last invitation, back to the tool */}
      <section className="lv-end pk-bleed" data-nav="dark" data-in="">
        <h2 className="lv-end__title"><span>Preach.</span> <span>The verse is already up.</span></h2>
        <p className="lv-end__line">Open it on the church laptop before Sunday. It takes about three minutes to set up.</p>
        <a className="lv-end__cta" href="#live-top">Start a live sermon</a>
      </section>
    </div>
  );
}
