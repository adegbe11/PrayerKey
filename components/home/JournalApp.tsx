"use client";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { ArrowRight, BookOpen, Bookmark, Church, ChevronLeft, ChevronRight, Home, MoreHorizontal, Plus, Settings, Sparkles } from "lucide-react";

/* The phone's Journal, screen by screen: a verse that writes itself, the closed Bible, the + button,
   then a Morning prayer book — cover, then one question per page. It plays by itself until touched. */

const REMEMBRANCE = [
  { text: "Write the vision, and make it plain upon tables, that he may run that readeth it.", ref: "Habakkuk 2:2" },
  { text: "I will remember the works of the LORD: surely I will remember thy wonders of old.", ref: "Psalm 77:11" },
  { text: "This shall be written for the generation to come.", ref: "Psalm 102:18" },
  { text: "Write it before them in a table, and note it in a book, that it may be for the time to come.", ref: "Isaiah 30:8" },
  { text: "These stones shall be for a memorial unto the children of Israel for ever.", ref: "Joshua 4:7" },
  { text: "A book of remembrance was written before him for them that feared the LORD.", ref: "Malachi 3:16" },
  { text: "Thou tellest my wanderings: put thou my tears into thy bottle: are they not in thy book?", ref: "Psalm 56:8" },
];

const BOOK = {
  title: "Morning prayer",
  summary: "Begin the day with God.",
  verse: "My voice shalt thou hear in the morning, O LORD; in the morning will I direct my prayer unto thee, and will look up.",
  verseRef: "PSALM 5:3",
  pages: [
    { heading: "ADORATION", q: "God, You are…", guide: "Begin with who God is, not with what you need.", starter: "Father, You are faithful…" },
    { heading: "GRATITUDE", q: "Before I ask, I remember.", guide: "Three things you are thankful for this morning.", starter: "Today, thank You for…" },
    { heading: "TODAY'S WORD", q: "What stands out to you?", guide: "Read it slowly, then write what caught you.", starter: "" },
    { heading: "PEOPLE ON MY HEART", q: "Who are you praying for?", guide: "Name them, and say what you are asking for them.", starter: "" },
    { heading: "I AM ASKING", q: "What do you need from God today?", guide: "Plainly. He is not put off by a direct request.", starter: "" },
    { heading: "MY PRAYER", q: "Father, this morning…", guide: "Everything above, in your own words.", starter: "" },
  ],
};

type Scene = "verse" | "book" | "cover" | "write" | "amen";

/** The verse arrives a word at a time; the word being written carries the gold. */
function Streamed({ text, onDone }: { text: string; onDone: () => void }) {
  const words = text.split(" ");
  const [head, setHead] = useState(0);
  const done = useRef(onDone);
  done.current = onDone;
  useEffect(() => {
    setHead(0);
  }, [text]);
  useEffect(() => {
    if (head >= words.length) { done.current(); return; }
    const id = setTimeout(() => setHead((h) => h + 1), 46 + words[head].length * 8 + 70);
    return () => clearTimeout(id);
  }, [head, words]);
  return (
    <p className="ja-verse">
      {words.map((w, i) => (
        <span key={i} className={i < head - 1 ? "is-ink" : i === head - 1 ? "is-now" : i === head ? "is-next" : "is-wait"}>{w}{i < words.length - 1 ? " " : ""}</span>
      ))}
    </p>
  );
}

/** The closed Bible, drawn the way the app draws it: white board, grey spine, gold cross and a ribbon. */
function ClosedBible() {
  return (
    <svg className="ja-bible" viewBox="0 0 150 172" width="150" height="172" aria-hidden>
      <rect x="25.05" y="20.42" width="111" height="141" rx="5.25" fill="#F2ECDC" />
      {Array.from({ length: 7 }, (_, i) => <line key={i} x1="132.7" x2="138.3" y1={15.48 + 141 * (0.12 + i * 0.11)} y2={15.48 + 141 * (0.12 + i * 0.11)} stroke="#CFC5AC" strokeWidth="1.2" />)}
      <rect x="19.5" y="15.48" width="111" height="141" rx="5.25" fill="#FFFFFF" />
      <rect x="19.5" y="15.48" width="12.2" height="141" rx="5.25" fill="#000" opacity="0.26" />
      <line x1="31.7" x2="31.7" y1="19.7" y2="152.3" stroke="#000" strokeOpacity="0.3" strokeWidth="1.4" />
      <rect x="40.6" y="26.8" width="75.5" height="118.5" rx="3" fill="none" stroke="#BB8A3E" strokeOpacity="0.38" strokeWidth="1.2" />
      <rect x="74.4" y="52.2" width="7.8" height="56.4" rx="1.5" fill="#BB8A3E" />
      <rect x="61.6" y="71.4" width="33.3" height="7.8" rx="1.5" fill="#BB8A3E" />
      <rect x="108.3" y="145.2" width="6.7" height="24" fill="#BB8A3E" opacity="0.85" />
    </svg>
  );
}

function Sheet({ children }: { children: React.ReactNode }) {
  return (
    <div className="ja-sheet">
      <span className="ja-sheet__edge" aria-hidden />
      <div className="ja-paper">
        <span className="ja-ribbon" aria-hidden />
        <div className="ja-paper__body">{children}</div>
      </div>
    </div>
  );
}

export default function JournalApp() {
  const [scene, setScene] = useState<Scene>("verse");
  const [page, setPage] = useState(0);
  const [verseAt, setVerseAt] = useState(0);
  const [verseDone, setVerseDone] = useState(false);
  const [tap, setTap] = useState(false);
  const [manual, setManual] = useState(false);
  const [text, setText] = useState<Record<number, string>>({});
  const [typing, setTyping] = useState(false);

  // A different remembrance each day, chosen in the browser so the first paint matches the server.
  useEffect(() => { setVerseAt(Math.floor(Date.now() / 86_400_000) % REMEMBRANCE.length); }, []);

  const line = REMEMBRANCE[verseAt];
  const leaf = BOOK.pages[page];

  // On its own: verse → Bible → tap + → cover → Begin → two pages written → start over.
  useEffect(() => {
    if (manual) return;
    const t: ReturnType<typeof setTimeout>[] = [];
    if (scene === "verse" && verseDone) t.push(setTimeout(() => { setScene("book"); setVerseDone(false); }, 1500));
    if (scene === "book") {
      t.push(setTimeout(() => setTap(true), 2600));
      t.push(setTimeout(() => { setTap(false); setScene("cover"); }, 3000));
    }
    if (scene === "cover") t.push(setTimeout(() => { setPage(0); setScene("write"); }, 3600));
    if (scene === "write") {
      const starter = leaf.starter;
      let n = 0;
      setTyping(true);
      const type = setInterval(() => {
        n += 1;
        setText((s) => ({ ...s, [page]: starter.slice(0, n) }));
        if (n >= starter.length) clearInterval(type);
      }, 70);
      const after = 700 + starter.length * 70 + 1500;
      t.push(setTimeout(() => {
        setTyping(false);
        if (page === 0) setPage(1);
        else { setScene("verse"); setPage(0); setText({}); }
      }, after));
      return () => { t.forEach(clearTimeout); clearInterval(type); setTyping(false); };
    }
    return () => t.forEach(clearTimeout);
  }, [scene, page, verseDone, manual, leaf.starter]);

  const touch = () => setManual(true);
  const go = (to: number) => {
    if (to < 0) { setScene("cover"); return; }
    if (to >= BOOK.pages.length) { setScene("amen"); return; }
    setPage(to); setScene("write");
  };

  return (
    <div className="ja">
      <div className="ja-phone" onPointerDown={touch}>
        <div className="ja-screen">
          <div className="ja-ui">
            <AnimatePresence mode="wait" initial={false}>
              {(scene === "verse" || scene === "book") && (
                <motion.div key="welcome" className="ja-welcome" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.35 }}>
                  <header className="ja-top"><h3 className="ja-title">Journal</h3><span className="ja-icons"><MoreHorizontal /><Settings /></span></header>
                  <div className="ja-center">
                    <AnimatePresence mode="wait">
                      {scene === "verse" ? (
                        <motion.div key={`v${verseAt}`} className="ja-verse-wrap" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0, y: -18 }} transition={{ duration: 0.5 }}>
                          <Streamed text={line.text} onDone={() => setVerseDone(true)} />
                          <motion.span className="ja-ref" initial={{ opacity: 0 }} animate={{ opacity: verseDone ? 1 : 0 }} transition={{ duration: 0.6 }}>{line.ref.toUpperCase()}</motion.span>
                        </motion.div>
                      ) : (
                        <motion.button type="button" key="book" className="ja-bookbtn" onClick={() => setScene("cover")} initial={{ opacity: 0, scale: 0.82 }} animate={{ opacity: 1, scale: 1 }} transition={{ type: "spring", stiffness: 190, damping: 11 }}>
                          <ClosedBible />
                          <span className="ja-vision">Write your vision here</span>
                        </motion.button>
                      )}
                    </AnimatePresence>
                  </div>
                  {scene === "book" && (
                    <button type="button" className={`ja-fab ${tap ? "is-tap" : ""}`} aria-label="New entry" onClick={() => setScene("cover")}>
                      <span className="ja-fab__ring" /><span className="ja-fab__ring ja-fab__ring--2" />
                      <span className="ja-fab__dot"><Plus strokeWidth={1.6} /></span>
                    </button>
                  )}
                  <nav className="ja-nav" aria-hidden>
                    {[[Home, "Home"], [BookOpen, "Bible"], [Sparkles, "Pray"], [Church, "Church"], [Bookmark, "Journal"]].map(([Icon, label]) => {
                      const I = Icon as typeof Home;
                      return <span key={label as string} className={label === "Journal" ? "is-on" : ""}><I /><b>{label as string}</b></span>;
                    })}
                  </nav>
                </motion.div>
              )}

              {scene === "cover" && (
                <motion.div key="cover" className="ja-book" initial={{ opacity: 0, rotateY: -50 }} animate={{ opacity: 1, rotateY: 0 }} exit={{ opacity: 0, rotateY: -60 }} transition={{ duration: 0.5, ease: [0.45, 0.05, 0.2, 1] }}>
                  <Sheet>
                    <div className="ja-cover">
                      <h4 className="ja-cover__title">{BOOK.title}</h4>
                      <p className="ja-cover__date">{new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long" })}</p>
                      <span className="ja-rule" />
                      <p className="ja-cover__verse">{BOOK.verse}</p>
                      <span className="ja-cover__ref">{BOOK.verseRef}</span>
                      <p className="ja-cover__sum">{BOOK.summary}</p>
                      <button type="button" className="ja-begin" onClick={() => go(0)}>Begin</button>
                    </div>
                  </Sheet>
                </motion.div>
              )}

              {scene === "write" && (
                <motion.div key={`w${page}`} className="ja-book" initial={{ opacity: 0, rotateY: -62 }} animate={{ opacity: 1, rotateY: 0 }} exit={{ opacity: 0, rotateY: -70 }} transition={{ duration: 0.5, ease: [0.45, 0.05, 0.2, 1] }}>
                  <Sheet>
                    <span className="ja-heading">{leaf.heading}</span>
                    <h4 className="ja-q">{leaf.q}</h4>
                    <p className="ja-guide">{leaf.guide}</p>
                    <textarea className="ja-lines" value={text[page] ?? ""} onChange={(e) => setText((s) => ({ ...s, [page]: e.target.value }))} aria-label={leaf.q} readOnly={typing} />
                    <div className="ja-folio">
                      <button type="button" aria-label="Back" onClick={() => go(page - 1)}><ChevronLeft /></button>
                      <span>{page + 1} of {BOOK.pages.length}</span>
                      <button type="button" aria-label="Next page" onClick={() => go(page + 1)}><ChevronRight /></button>
                    </div>
                  </Sheet>
                </motion.div>
              )}

              {scene === "amen" && (
                <motion.div key="amen" className="ja-book" initial={{ opacity: 0, rotateY: -62 }} animate={{ opacity: 1, rotateY: 0 }} exit={{ opacity: 0 }} transition={{ duration: 0.5 }}>
                  <Sheet>
                    <div className="ja-cover">
                      <h4 className="ja-cover__title">Amen.</h4>
                      <span className="ja-rule" />
                      <Link href="/download" className="ja-begin ja-begin--link">Get the app <ArrowRight size={14} /></Link>
                      <button type="button" className="ja-again" onClick={() => { setText({}); setPage(0); setScene("verse"); setManual(false); }}>Start again</button>
                    </div>
                  </Sheet>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}
