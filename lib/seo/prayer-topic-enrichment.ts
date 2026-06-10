/**
 * Hand-written enrichment for the highest-traffic prayer topics.
 * Replaces template-generated FAQs/how-to/context with unique content
 * so top pages stop sharing near-identical copy (thin-content risk).
 * Merged into PRAYER_TOPICS at load time in prayer-topics.ts.
 */

import type { PrayerTopic } from "./prayer-topics";

type Enrichment = Pick<PrayerTopic, "faqs" | "context" | "howToPray">;

export const TOPIC_ENRICHMENT: Record<string, Enrichment> = {
  "healing": {
    context: "Prayers for healing are among the most searched prayers in the world — prayed in hospital corridors, beside sickbeds, and in quiet desperation at 3am. Scripture records Jesus healing every kind of disease, and James instructs believers to pray over the sick. This prayer joins a tradition as old as faith itself.",
    howToPray: [
      "Name the sickness specifically before God — vagueness is not faith. Tell Him exactly what the doctors said and exactly what you are asking Him to do.",
      "Anchor your request in a healing scripture like Isaiah 53:5 or Jeremiah 30:17. Speak the verse aloud over the situation.",
      "Thank God for medical staff and ask Him to guide their hands — prayer and medicine are partners, not rivals.",
      "Pray persistently. Jesus told the parable of the persistent widow specifically so we would not give up after one prayer (Luke 18:1).",
      "Close by entrusting the outcome and the timeline to God, while continuing to expect and watch for improvement.",
    ],
    faqs: [
      { q: "Does God still heal people today?", a: "Yes. James 5:14–15 instructs the church in every generation to pray over the sick, and millions of believers testify to recoveries that surprised their doctors. God heals through miracles, through medicine, through time — and sometimes He gives sustaining grace instead. Healing is His nature: 'I am the Lord who heals you' (Exodus 15:26)." },
      { q: "Should I keep taking medicine while praying for healing?", a: "Absolutely. Scripture never opposes medicine — Luke, who wrote a quarter of the New Testament, was a physician. Pray for healing AND keep every appointment. Many answered healing prayers arrive through the very treatment God guides." },
      { q: "Why hasn't God healed me yet?", a: "Delay is not denial. Scripture shows healing coming instantly (Matthew 8:3), gradually (Mark 8:24–25), and through perseverance (2 Kings 5:14). Paul himself lived with a thorn while God's grace sustained him. Keep praying, keep treatment, and let God write the timeline." },
      { q: "Can I pray for healing on behalf of someone else?", a: "Yes — intercessory prayer for the sick is commanded and powerful. The friends who lowered the paralytic through the roof prayed with their actions, and Jesus responded to THEIR faith (Mark 2:5). Your prayer for a sick loved one matters." },
      { q: "What is the best Bible verse to pray for healing?", a: "Isaiah 53:5 — 'by his wounds we are healed' — is the foundational healing promise, fulfilled in Christ. Jeremiah 30:17, Psalm 103:2–3, and James 5:15 are also powerful verses to declare over sickness." },
    ],
  },
  "anxiety": {
    context: "Anxiety disorders affect hundreds of millions of people, and 'prayer for anxiety' spikes in searches every Sunday night and every exam season. The Bible's most direct answer is Philippians 4:6–7 — a practical exchange: bring God your requests with thanksgiving, and He guards your heart and mind with peace.",
    howToPray: [
      "Start by physically slowing down — sit, breathe, and read Philippians 4:6–7 once before you pray a single word.",
      "Tell God the specific worry, not just 'I'm anxious.' Name the meeting, the bill, the diagnosis, the relationship.",
      "Add thanksgiving — it is the part of Philippians 4:6 everyone skips, and the part that shifts perspective from threat to trust.",
      "Hand over the outcome explicitly: 'God, this result belongs to You now.' Anxiety is usually ownership of something God never asked you to carry.",
      "When the worry returns in an hour — and it may — repeat the exchange. Casting your cares (1 Peter 5:7) is a practice, not a one-time event.",
    ],
    faqs: [
      { q: "Is anxiety a sin?", a: "No. Anxiety is a human response to feeling unsafe — Jesus Himself sweated blood under anguish in Gethsemane. Scripture's commands to 'not be anxious' are invitations to bring worry to God, not condemnations for feeling it. God meets anxious people with compassion, not rebuke." },
      { q: "Can prayer really calm anxiety?", a: "Yes — both spiritually and measurably. Philippians 4:6–7 promises peace that 'transcends understanding' to those who pray with thanksgiving. Clinically, prayerful breathing and gratitude practices lower stress responses. Prayer is not a replacement for therapy or medication when needed, but it is real help." },
      { q: "Should Christians take medication for anxiety?", a: "If a doctor recommends it, yes — medication for anxiety is no different from insulin for diabetes. God heals through means as well as miracles. Pray, see a professional, and refuse the false guilt that says faith and treatment can't coexist." },
      { q: "What should I pray when anxiety hits at night?", a: "Pray Psalm 4:8 — 'In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.' Name the worry, hand it to God for the night, and tell Him you'll take it up with Him in the morning if it still matters. Most 3am fears shrink at sunrise." },
      { q: "How is prayer different from just positive thinking?", a: "Positive thinking talks to yourself; prayer talks to Someone with actual power over your situation. You're not generating optimism — you're transferring weight to a Father who 'cares for you' (1 Peter 5:7) and can act." },
    ],
  },
  "morning": {
    context: "Morning prayer is the oldest daily rhythm in Scripture — David wrote 'in the morning, Lord, you hear my voice' (Psalm 5:3) and Jesus rose before daylight to pray (Mark 1:35). Starting the day with prayer sets your heart's direction before the world sets it for you.",
    howToPray: [
      "Pray before you check your phone. The first voice you hear shapes the whole day — make it God's, not your notifications.",
      "Open with thanksgiving for the simple fact of waking: His mercies are new every morning (Lamentations 3:22–23).",
      "Commit the day's specific events to Him — the meeting, the commute, the conversation you're dreading.",
      "Ask for one thing to carry: wisdom, patience, courage — whatever today actually requires.",
      "Keep it short if you must. Two honest minutes at 6am beat twenty distracted ones. Consistency builds the altar.",
    ],
    faqs: [
      { q: "Why is morning prayer so powerful?", a: "Because it claims the day's first-fruits. Jesus prayed 'very early in the morning, while it was still dark' (Mark 1:35), and David promised 'in the morning I lay my requests before you and wait expectantly' (Psalm 5:3). Praying first means every decision that follows starts from a settled heart." },
      { q: "What time should I wake up to pray?", a: "There is no biblical alarm clock — the principle is 'first,' not 'five a.m.' Whether you rise at 5 or 8, give God the opening minutes before email, news, and noise. The hour matters less than the order." },
      { q: "What should I include in a morning prayer?", a: "A simple pattern: thank God for the new day, commit your specific schedule to Him, ask for what today requires, and pray for one person besides yourself. Many believers finish with the Lord's Prayer as a frame." },
      { q: "How long should morning prayer last?", a: "Start with five minutes you'll actually do rather than thirty you'll abandon by Thursday. As the habit roots, it tends to grow on its own — most lifelong morning pray-ers started small." },
    ],
  },
  "protection": {
    context: "Prayers for protection surge during travel seasons, school years, and troubled times — parents pray them over children, spouses over households, travelers over journeys. Psalm 91 has been the church's protection psalm for two millennia: a declaration of shelter under the wings of the Almighty.",
    howToPray: [
      "Begin by declaring who God is before what you fear: refuge, fortress, shield (Psalm 91:1–2). Protection prayer starts with His character.",
      "Name the people you're covering — say their actual names. 'Protect my family' becomes powerful when it becomes 'protect Daniel on his commute.'",
      "Pray Scripture directly: Psalm 91 over your household, Isaiah 54:17 over threats, Psalm 121 over journeys.",
      "Ask for angelic protection — Psalm 91:11 promises angels 'charge over you in all your ways.'",
      "End by replacing fear with trust: protection prayer should leave you feeling covered, not more afraid. 'I will lie down and sleep in peace' (Psalm 4:8).",
    ],
    faqs: [
      { q: "What is the strongest prayer for protection?", a: "Psalm 91 is widely considered the Bible's ultimate protection prayer — soldiers have carried it into battle and parents have prayed it over children for centuries. Praying it aloud, personalized with names, is a time-honored practice: 'He is MY refuge, MY fortress.'" },
      { q: "Can I pray protection over someone far away?", a: "Yes. Prayer is not limited by distance — the centurion's servant was healed from afar by a word (Matthew 8:13). Praying Psalm 91 or Numbers 6:24–26 over a distant child, spouse, or friend places them under the same Almighty shadow." },
      { q: "Does praying for protection guarantee nothing bad will happen?", a: "Scripture promises God's presence in trouble, not exemption from it: 'WHEN you pass through the waters, I will be with you' (Isaiah 43:2). Protection prayer covers what it covers — and where trials still come, He turns them for good (Romans 8:28) and walks through them with you." },
      { q: "Should I pray protection every day?", a: "Many believers do — morning prayer over the household and travel mercies before journeys. Daily protection prayer isn't superstition; it's dependence, the same way daily bread was asked for daily." },
    ],
  },
  "marriage": {
    context: "Searches for marriage prayer peak in January and after holidays — seasons when strain shows. God designed marriage and wrote its manual: love that is patient and kind (1 Corinthians 13), a cord of three strands not quickly broken (Ecclesiastes 4:12). Prayer invites the third strand in.",
    howToPray: [
      "Pray FOR your spouse before you pray ABOUT them. Bless first; bring complaints second. The order changes your heart.",
      "Confess your own contribution honestly — even 10% owned in prayer softens 90% of the standoff.",
      "Pray specifics: the recurring argument, the silence after dinner, the intimacy that faded. God works in details.",
      "If possible, pray together even briefly — couples who pray together report dramatically higher marital satisfaction. Start with one sentence each at bedtime.",
      "Persist through the dry season. Marriage restoration is usually a slow miracle — keep watering it in prayer.",
    ],
    faqs: [
      { q: "Can prayer really save a struggling marriage?", a: "Countless restored couples say yes. Prayer changes the two people doing the praying — softening pride, renewing patience, reviving affection — and invites God's direct work where counseling alone reaches its limits. Pair prayer WITH counseling for the strongest path back." },
      { q: "How do I pray for my spouse when I'm angry at them?", a: "Start with one honest sentence: 'God, I don't feel like praying for them — help me anyway.' Then bless them before you vent. Praying blessing over someone you're angry at is the fastest anger-dissolver Scripture offers (Matthew 5:44 works inside marriage too)." },
      { q: "What if my spouse won't pray with me?", a: "Pray alone — faithfully and without announcing it as a guilt trip. 1 Peter 3:1–2 speaks of spouses won 'without words' by quiet conduct. One praying partner has changed many marriages." },
      { q: "Which Bible verse should I pray over my marriage?", a: "1 Corinthians 13:4–7 prayed as a request — 'Lord, make our love patient, make it kind…' — turns the love chapter into a powerful marriage prayer. Ecclesiastes 4:12 is the classic verse for inviting God in as the third strand." },
    ],
  },
  "financial-breakthrough": {
    context: "Financial breakthrough prayers rise with every economic downturn and every month's end. Scripture speaks to money over 2,000 times — God as provider (Philippians 4:19), the open floodgates of Malachi 3:10, and bread asked for daily. This prayer brings real bills before a real Provider.",
    howToPray: [
      "Bring the actual numbers to God — the debt figure, the rent amount, the shortfall. Faith is not vagueness about reality; it is honesty plus trust.",
      "Thank Him for what He HAS provided before asking for what's missing — gratitude is the soil of provision prayer.",
      "Ask for both miracle and strategy: unexpected provision AND wisdom for budgeting, work, and opportunity. God typically supplies through both.",
      "Deal honestly with giving. Malachi 3:10 ties open floodgates to open hands — stinginess and breakthrough rarely share an address.",
      "Refuse shame. Needing provision is not spiritual failure — 'give us this day our daily bread' is in the Lord's own prayer.",
    ],
    faqs: [
      { q: "Does God really care about my finances?", a: "Yes — Jesus talked about money more than about heaven and hell combined, and Philippians 4:19 promises God 'will meet all your needs according to the riches of his glory.' Your rent, debts, and bills are not too small or too worldly for prayer." },
      { q: "Why pray for money instead of just working harder?", a: "It's not either/or. Deuteronomy 8:18 says God 'gives you the ABILITY to produce wealth' — prayer invites His favor onto your work, opens doors effort alone can't, and protects you from the exhaustion of self-reliance. Pray like it depends on God; work like He answers through your hands." },
      { q: "What is blocking my financial breakthrough?", a: "Sometimes nothing — seasons of lack happen to the faithful (Paul knew 'need' in Philippians 4:12). But Scripture does name common blockages worth honest prayer: no plan (Proverbs 21:5), closed hands toward God and the poor (Malachi 3:10, Proverbs 19:17), or waste of past provision. Ask God to show you which applies, if any." },
      { q: "How long should I pray before giving up on a financial miracle?", a: "Don't set a deadline for God. Keep praying AND keep acting — applying, budgeting, asking for help where needed. George Müller logged thousands of specific financial answers, many arriving the morning they were needed. Provision often comes at the last hour, not before it." },
    ],
  },
  "depression": {
    context: "Searches for depression prayers happen disproportionately at night. Scripture never shames the depressed — Elijah asked to die, David wrote from pits of despair, and Jesus was 'a man of sorrows.' The Psalms give depression a vocabulary, and God promises closeness to the crushed in spirit (Psalm 34:18).",
    howToPray: [
      "Lower the bar: one honest sentence counts. 'God, I'm not okay' is a complete prayer when that's all you have.",
      "Borrow words when yours are gone — Psalm 42, Psalm 88, and Psalm 13 were written for exactly this. Read them aloud as your own.",
      "Don't perform. God responded to Elijah's suicidal exhaustion with food and sleep before any sermon (1 Kings 19:5–7). Tell Him the unfiltered truth.",
      "Ask for one day's strength, not a cured lifetime. 'New mercies every morning' (Lamentations 3:23) is a daily ration by design.",
      "Pray for the courage to reach out — to a doctor, counselor, or trusted friend. Asking for help IS often the answer to the prayer.",
    ],
    faqs: [
      { q: "Is depression a sign of weak faith?", a: "No. Elijah, Job, David, and Jeremiah — spiritual giants — all experienced deep depression in Scripture. Charles Spurgeon, history's most famous preacher, battled it for life. Depression is a human affliction, not a faith report card, and God is 'close to the brokenhearted' (Psalm 34:18), not disappointed in them." },
      { q: "Can prayer cure depression?", a: "Prayer brings real comfort, hope, and sometimes dramatic lifting — and God also heals through therapy, medication, community, and time. Treat prayer as essential support alongside professional care, not a replacement for it. Using the help available IS faith, not the lack of it." },
      { q: "What do I pray when I feel nothing at all?", a: "Numbness is common in depression, and prayer doesn't require feelings to be valid. Pray flat: 'God, I feel nothing. I'm showing up anyway.' Psalm 88 — the darkest psalm — ends without resolution, and God put it in the Bible. Showing up numb is still showing up." },
      { q: "If I have suicidal thoughts, what should I do?", a: "Tell someone today — a crisis line, a doctor, a trusted person. In the US, call or text 988. God's care most often arrives through people, and reaching out is the bravest prayer-answer you can act on. Your life matters; Elijah's lowest moment was not his last chapter, and yours isn't either." },
    ],
  },
  "strength": {
    context: "Prayers for strength are prayed by caregivers, students in finals week, grievers, and anyone simply running on empty. Isaiah 40:31 is the anchor promise: those who wait on the Lord renew their strength. Biblical strength is received, not manufactured.",
    howToPray: [
      "Admit the empty tank first. 'My strength is gone' is the prerequisite — God's power is 'made perfect in weakness' (2 Corinthians 12:9).",
      "Wait before you ask. Isaiah 40:31 ties renewed strength to waiting on the Lord — two unhurried minutes of silence before God is part of the prayer.",
      "Ask for strength for the next step only, not the whole staircase. Manna came daily; strength usually does too.",
      "Declare a strength verse aloud: Philippians 4:13, Isaiah 41:10, or Nehemiah 8:10. Spoken Scripture stiffens the spine.",
      "Receive rest as God's answer when it comes. Sometimes 'be strong' is delivered as 'sleep, eat, and let me handle tonight' (1 Kings 19:5–7).",
    ],
    faqs: [
      { q: "How do I get strength from God when I'm completely exhausted?", a: "Start where Elijah did — God's first prescription for His burned-out prophet was food and sleep, not effort (1 Kings 19). Then wait on the Lord in unhurried quiet: Isaiah 40:31 promises renewed strength specifically to those who wait, not those who strive harder." },
      { q: "What does 'I can do all things through Christ' really mean?", a: "In context, Philippians 4:13 is about contentment in every circumstance — Paul writing from prison about facing plenty and hunger alike. It promises strength to endure whatever God assigns, not superpowers for whatever we attempt. Within God's assignment, the supply is unlimited." },
      { q: "Can I pray for emotional strength, not just physical?", a: "Yes — most biblical strength language is inner. Ephesians 3:16 prays for strengthening 'with power through his Spirit in your INNER being,' and Nehemiah 8:10 locates strength in joy. Emotional and spiritual stamina are exactly what these prayers are for." },
    ],
  },
  "forgiveness": {
    context: "Forgiveness prayers carry two directions of traffic: receiving God's forgiveness and releasing someone else's debt. Both run on the same engine — 'forgive us our debts, AS we forgive our debtors.' Psalm 51, David's confession, has been the church's forgiveness prayer for 3,000 years.",
    howToPray: [
      "Be specific in confession — 'forgive my sins' costs nothing; naming the actual lie, the actual betrayal, breaks its grip (1 John 1:9).",
      "Receive forgiveness, don't re-earn it. After confessing, thank God it is DONE — repeated begging for the same sin is unbelief in His promise.",
      "To forgive someone else, start with a decision, not a feeling: 'God, I choose to release them.' Feelings follow choices, sometimes slowly.",
      "Pray blessing on the person who hurt you (Luke 6:28). It feels impossible the first time and gets freer every repetition.",
      "Repeat as needed — forgiveness of deep wounds is usually a daily re-release, the seventy-times-seven Jesus described, often toward the same offense.",
    ],
    faqs: [
      { q: "Will God forgive me for what I've done?", a: "Yes — 1 John 1:9 is unconditional on the sin's size: 'If we confess our sins, he is faithful and just and will forgive us.' David was forgiven adultery and murder; Peter, denying Christ; Paul, persecuting the church. No sincere confession has ever been refused." },
      { q: "How do I forgive someone who isn't sorry?", a: "Forgiveness doesn't require their apology — it's a release you perform before God, for your own freedom, whether or not they ever acknowledge the wrong. Jesus forgave from the cross people actively mocking Him. Reconciliation needs two people; forgiveness needs only you and God." },
      { q: "Does forgiving mean I have to trust them again?", a: "No. Forgiveness cancels the debt; trust is rebuilt by changed behavior over time — and some relationships should keep boundaries permanently. You can fully release someone before God and still protect yourself wisely." },
      { q: "I keep remembering the sin I confessed. Has God really forgiven me?", a: "Yes. Memory is not unforgiveness — God removes the RECORD 'as far as the east is from the west' (Psalm 103:12), not necessarily the memory. When it resurfaces, don't re-confess; instead thank Him it's already covered. Gratitude, not guilt, is the correct response to a settled debt." },
    ],
  },
  "salvation": {
    context: "The salvation prayer is the doorway prayer of the Christian faith — and prayers for loved ones' salvation are among the most persistent in any praying family. Romans 10:9 makes the path stunningly simple: confess Jesus as Lord, believe God raised Him, and you will be saved.",
    howToPray: [
      "For your own salvation: speak Romans 10:9 as a personal declaration — confess Jesus as Lord aloud, believe in the resurrection, receive forgiveness. It can be this simple and this immediate.",
      "For someone else: pray their eyes be opened (2 Corinthians 4:4 says unbelief is a blindness) before praying their behavior changes.",
      "Ask God to arrange divine encounters — the right person, podcast, crisis, or kindness that cracks the door. Most conversions arrive through arranged circumstances.",
      "Pray with a long horizon. Monica prayed 17 years for Augustine, who became one of history's greatest Christians. Persistence outlasts resistance.",
      "Live the sermon. 1 Peter 3:1 says some are won 'without words' — your changed life is part of the prayer's answer.",
    ],
    faqs: [
      { q: "What is the sinner's prayer and do I have to say exact words?", a: "There are no magic words — salvation is the heart's transaction described in Romans 10:9: confessing Jesus as Lord and believing God raised Him from the dead. Any honest version counts: 'Jesus, I believe You died and rose for me. Forgive me. Be my Lord.' The thief on the cross was saved with one sentence." },
      { q: "Can I really be saved instantly?", a: "Yes — every conversion in Acts is immediate: the thief on the cross, the Philippian jailer ('believe… and you will be saved' — that same night), the Ethiopian official. Growth takes a lifetime; salvation takes a sincere moment." },
      { q: "How do I pray for a family member who rejects God?", a: "Pray for opened eyes rather than argued points (2 Corinthians 4:4), ask God to send other voices they can hear, and keep loving them without scorekeeping. Augustine's mother prayed 17 years. The prodigal's father kept watching the road. Persistence and warmth win more souls than winning arguments." },
      { q: "Is it too late for someone who has resisted God for decades?", a: "Never. Jesus' parable of the vineyard workers pays the 5pm hire the same as the dawn one (Matthew 20:9) — deathbed conversions are real conversions. While someone breathes, your prayer for them is live." },
    ],
  },
  "sleep": {
    context: "Sleep prayers are searched at midnight and 3am by the racing-minded, the grieving, and the anxious. Psalm 4:8 is the Bible's bedtime promise — 'in peace I will lie down and sleep' — and Psalm 127:2 says God 'grants sleep to those he loves.' Rest is a gift to receive, not a performance to achieve.",
    howToPray: [
      "Close the day deliberately: thank God for two or three specific things from today, however small. Gratitude is a natural sedative.",
      "Hand over tomorrow by name — the meeting, the appointment, the conversation. Tell God you'll pick them up from Him in the morning.",
      "Pray Psalm 4:8 aloud as you lie down: 'In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.'",
      "If thoughts race, give them a verse to race on — repeat 'Be still and know that I am God' (Psalm 46:10) slower each time.",
      "When you wake at 3am, don't fight it — use it: pray for one person, then hand the watch back to the One who never sleeps (Psalm 121:4).",
    ],
    faqs: [
      { q: "What is the best prayer for sleep?", a: "Psalm 4:8 prayed aloud at lights-out is the classic: 'In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.' Pair it with explicitly handing tomorrow's worries to God — most sleeplessness is tomorrow leaking into tonight." },
      { q: "Why do I keep waking up at 3am with anxious thoughts?", a: "Night magnifies worry because there's no daylight action to take. Psalm 121:3–4 answers it directly: 'He who watches over you will not slumber.' Someone competent is already on watch — your shift is over. Many believers turn 3am wakings into brief intercession, then sleep returns more easily." },
      { q: "Can prayer help with nightmares?", a: "Many believers pray Psalm 91 over their sleep specifically against troubling dreams — 'you will not fear the terror of night' (91:5). Praying before bed, filling the last waking minutes with Scripture rather than screens, measurably changes what the mind processes overnight." },
    ],
  },
  "fear": {
    context: "'Do not fear' appears in Scripture hundreds of times — not as a scolding but as a repeated reassurance, almost always attached to a reason: 'for I am with you.' Fear prayers don't deny danger; they relocate confidence from the situation to the God standing in it with you.",
    howToPray: [
      "Name the fear precisely. Generalized dread shrinks when it's forced into one sentence: 'I am afraid that ____.'",
      "Counter it with presence, not probability: the biblical answer to fear is never 'it probably won't happen' but 'I am with you' (Isaiah 41:10).",
      "Declare 2 Timothy 1:7 aloud: God gave you power, love, and a sound mind — fear is the intruder, not the resident.",
      "Do the feared thing prayed-up rather than waiting for fear to vanish first. Courage is fear that has been handed to God and walked through.",
      "Repeat at every recurrence. David said 'WHEN I am afraid, I put my trust in you' (Psalm 56:3) — when, not if. Re-trusting is the practice.",
    ],
    faqs: [
      { q: "Why does the Bible say 'do not fear' so many times?", a: "Because God knows we keep needing it. The command appears across Scripture — to Abraham, Moses, Joshua, Mary, the disciples — and is almost always paired with a promise: 'for I am with you.' It's less a prohibition than a repeated parental reassurance for every new scary thing." },
      { q: "What's the difference between healthy caution and sinful fear?", a: "Caution sees real risk and acts wisely; paralyzing fear sees risk and stops trusting God's presence. Locking your door is caution. Refusing to live, decide, or obey because of what might happen is the fear God keeps telling His people to hand over." },
      { q: "How do I pray against constant fear about the future?", a: "Pray Matthew 6:34 into practice — ask for today's bread, today's grace, today's courage only. Future-fear borrows tomorrow's troubles without tomorrow's grace, which only arrives on the day itself. Jeremiah 29:11 is the anchor: the future is already planned by Someone who intends your good." },
    ],
  },
  "breakthrough": {
    context: "Breakthrough prayers come from people who have pushed and waited — for the job, the diagnosis to change, the door to finally open. The word itself is biblical: David named God 'Lord of the breakthrough' at Baal Perazim (2 Samuel 5:20), where God broke out against the obstacle 'like a flood.'",
    howToPray: [
      "Define the wall. 'I need a breakthrough' is vague; 'this application has been rejected four times' gives faith a target.",
      "Recall past breakthroughs first — David asked 'as before?' at Baal Perazim because God had done it before. Your history with God is evidence.",
      "Pray Isaiah 43:19 over the dead end: God makes ways in the wilderness — His specialty is routes that don't exist yet.",
      "Pair persistence with expectancy: keep knocking (Luke 11:9–10) AND keep checking the door. Many miss breakthroughs by no longer watching.",
      "Prepare for the answer practically — Elisha had the widow gather jars BEFORE oil flowed (2 Kings 4:3). Position yourself as if it's coming.",
    ],
    faqs: [
      { q: "What does breakthrough mean in prayer?", a: "It's the moment a long-standing barrier — financial, medical, relational, spiritual — suddenly gives way to God's power. The term comes from 2 Samuel 5:20, where David said 'as waters break out, the Lord has broken out against my enemies before me' and named the place 'Lord of the Breakthrough.'" },
      { q: "Why is my breakthrough taking so long?", a: "Scripture shows delays that were preparation (Joseph's 13 years before the palace), timing (Lazarus' four days before greater glory), and contested answers (Daniel's 21 days while the answer was already dispatched — Daniel 10:12–13). Delay in Scripture is consistently not denial. Keep praying; the answer often matures in the waiting." },
      { q: "Should I fast for a breakthrough?", a: "Fasting has accompanied breakthrough prayer throughout Scripture — Esther before approaching the king, Jehoshaphat before impossible odds, Jesus before ministry. It doesn't twist God's arm; it focuses yours. If something has resisted ordinary prayer, Jesus' words about 'prayer and fasting' (Mark 9:29) are an invitation." },
    ],
  },
  "gratitude": {
    context: "Gratitude prayer is the one form of prayer commanded for ALL circumstances — 'give thanks in all circumstances; for this is God's will for you' (1 Thessalonians 5:18). It's also the entrance protocol of worship: 'enter his gates with thanksgiving' (Psalm 100:4). Thankfulness rewires what worry unravels.",
    howToPray: [
      "Be embarrassingly specific. 'Thank You for everything' evaporates; 'thank You for my daughter's laugh at dinner' lands and lasts.",
      "Thank God for things you normally invoice Him for — the unanswered prayers that protected you, the closed doors that redirected you.",
      "Practice thanks IN hard circumstances, not FOR them — 1 Thessalonians 5:18 says 'in all,' not 'for all.' Find the one preserved thing inside the storm.",
      "Keep a written record. Israel built memorial stones; a gratitude list is yours — read it on the day faith runs low.",
      "Let thanksgiving lead every other prayer: Psalm 100:4 puts the thank-you gate before the request throne. Order matters.",
    ],
    faqs: [
      { q: "Why does gratitude matter so much in prayer?", a: "It's the only prayer posture commanded for every circumstance (1 Thessalonians 5:18) and the doorway posture of Psalm 100:4 — 'enter his gates with thanksgiving.' Practically, gratitude shifts attention from what's missing to Who is present, which changes both the prayer and the pray-er." },
      { q: "How can I be thankful when life is genuinely hard?", a: "Scripture asks for thanks IN all circumstances, not FOR all of them — a crucial difference. Paul sang in prison not because prison was good but because God in prison was. Hunt for the preserved thing: the friend who stayed, the strength that held, the mercy that's 'new every morning' even in Lamentations." },
      { q: "What's a simple daily gratitude practice?", a: "Three specifics every night — written or spoken — that happened that day. Not categories ('family, health') but moments ('the text from Mom'). Thirty days of this measurably shifts default mood, and as a prayer it fulfils 1 Thessalonians 5:18 one evening at a time." },
    ],
  },
  "peace": {
    context: "Peace prayers are prayed in waiting rooms, war zones, and sleepless beds. The Bible's peace — shalom — is not the absence of trouble but the presence of wholeness inside it. Jesus' parting gift was exactly this: 'My peace I give you… not as the world gives' (John 14:27).",
    howToPray: [
      "Exchange, don't suppress: Philippians 4:6–7 trades specific anxieties (presented with thanksgiving) for peace that guards heart and mind. Make the trade item by item.",
      "Pray for peace WITH the storm still up — Jesus slept in the boat before He stilled the sea. Inner calm precedes outer calm.",
      "Fix the mind deliberately: 'You will keep in perfect peace those whose minds are steadfast' (Isaiah 26:3). Choose one truth about God and hold it like a handrail.",
      "Breathe slowly while praying short phrases — 'Lord, Your peace' on the exhale. The body and the soul calm together.",
      "Become what you ask: pray to be a peacemaker (Matthew 5:9) in one specific conflict you touch. Peace given is peace multiplied.",
    ],
    faqs: [
      { q: "What is the peace that passes understanding?", a: "Philippians 4:7 describes a calm that makes no logical sense given the circumstances — peace in the hospital corridor, in the layoff, in the diagnosis. It 'transcends understanding' because its source is God's presence rather than improved conditions, and it arrives through the prayer-with-thanksgiving exchange of verse 6." },
      { q: "How is God's peace different from just relaxing?", a: "Relaxation is the nervous system settling; God's peace is the soul settling — and it works in circumstances no spa could touch. Jesus offered 'MY peace… not as the world gives' (John 14:27): the world's peace requires removing the problem; His coexists with it." },
      { q: "Can I pray for peace in my home and family?", a: "Yes — speak it as a blessing over the household, the biblical way: 'Peace to this house' (Luke 10:5). Pair the prayer with becoming its first answer: lowering your own voice, dropping one recurring quarrel, making peace 'as far as it depends on you' (Romans 12:18)." },
    ],
  },
  "hope": {
    context: "Hope prayers rise from waiting rooms and ruins — prayed by people who need a reason to face tomorrow. Biblical hope is not optimism or wishful thinking; it is confident expectation anchored to God's character. 'May the God of hope fill you with all joy and peace as you trust' (Romans 15:13).",
    howToPray: [
      "Aim the prayer at God's character, not the odds: hope built on probabilities collapses with bad news; hope built on 'God is faithful' survives it.",
      "Borrow Lamentations 3:21 — 'THIS I call to mind, and therefore I have hope.' List what you know of God deliberately, as an act of will.",
      "Ask to be FILLED, not just helped: Romans 15:13 prays for overflow — 'that you may OVERFLOW with hope by the power of the Holy Spirit.'",
      "Anchor it in resurrection: Christian hope's bedrock is a tomb that's already empty (1 Peter 1:3). Whatever else is uncertain, that has already happened.",
      "Hope out loud for someone else — speaking hope to a discouraged person is one of the fastest ways God re-ignites your own.",
    ],
    faqs: [
      { q: "What is the difference between hope and optimism?", a: "Optimism predicts that circumstances will improve; biblical hope trusts that God will be faithful whatever circumstances do. Optimism dies with bad news. Hope survives it — because it was never anchored to the forecast but to a Person with a track record (Hebrews 10:23: 'he who promised is faithful')." },
      { q: "How do I find hope when everything looks hopeless?", a: "Do what Lamentations 3 does from inside the rubble of Jerusalem: deliberately call truth to mind — 'His compassions never fail; they are new every morning' — as an act of will before it's a feeling. Hope in Scripture is repeatedly REMEMBERED into existence. Start with God's past faithfulness to you, however small the instance." },
      { q: "Can hope really be restored after deep loss?", a: "Yes — though usually gradually, like dawn rather than a light switch. Psalm 30:5 promises joy 'comes in the morning,' and 1 Peter 1:3 calls Christian hope 'LIVING' because it's tied to resurrection: God's pattern is life out of graves. Grieve honestly; hope will return wearing different clothes." },
    ],
  },
};
