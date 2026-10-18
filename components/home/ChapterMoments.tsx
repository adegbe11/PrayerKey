// One moment per chapter, laid over the phone, that acts out what the feature
// does: the pulls filling, the Bible working with no signal, a prayer marked
// answered, the scriptures caught during a sermon.

type Pos = React.CSSProperties & Record<`--${string}`, string>;

const Check = () => (
  <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12.5l4.5 4.5L19 7.5" /></svg>
);

function Chip({ children, pos, className = "" }: { children: React.ReactNode; pos: Pos; className?: string }) {
  return (
    <div className={`hs-chip ${className}`} style={pos} aria-hidden>
      <span className="hs-chip__in">{children}</span>
    </div>
  );
}

export default function ChapterMoments({ id }: { id: string }) {
  if (id === "pray") {
    return (
      <>
        <Chip className="hs-chip--pulls" pos={{ "--x": "-6%", "--y": "16%", "--fx": "-40px", "--fy": "10px", "--delay": "0.5s" }}>
          <span className="hs-pulls">{Array.from({ length: 10 }, (_, i) => <i key={i} style={{ "--p": String(i) } as Pos} />)}</span>
          <span className="hs-chip__label">Ten pulls</span>
        </Chip>
        <Chip pos={{ "--x": "64%", "--y": "70%", "--fx": "40px", "--fy": "20px", "--delay": "0.9s" }}>
          <span className="hs-pull-arrow" aria-hidden>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"><path d="M12 5v14M6 13l6 6 6-6" /></svg>
          </span>
          <span className="hs-chip__label">Pull for the next</span>
        </Chip>
      </>
    );
  }
  if (id === "read") {
    return (
      <>
        <Chip pos={{ "--x": "-4%", "--y": "8%", "--fx": "-30px", "--fy": "-20px", "--delay": "1s" }}>
          <span className="hs-chip__icon">
            <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor" aria-hidden><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5z" /></svg>
          </span>
          <span className="hs-chip__label">Works with no signal</span>
        </Chip>
        <Chip className="hs-chip--gold" pos={{ "--x": "62%", "--y": "82%", "--fx": "30px", "--fy": "30px", "--delay": "1.25s" }}>
          <span className="hs-chip__label">Lamentations 3:33</span>
        </Chip>
      </>
    );
  }
  if (id === "remember") {
    return (
      <>
        <div className="hs-stamp" aria-hidden>
          <span className="hs-stamp__in"><Check /> Answered</span>
        </div>
        <Chip pos={{ "--x": "-8%", "--y": "60%", "--fx": "-40px", "--fy": "0px", "--delay": "1.6s" }}>
          <span className="hs-chip__icon hs-chip__icon--violet"><Check /></span>
          <span className="hs-chip__label">Prayed for since March</span>
        </Chip>
      </>
    );
  }
  if (id === "church") {
    const refs = ["Romans 8:28", "Isaiah 40:31", "John 14:27"];
    return (
      <>
        {refs.map((r, i) => (
          <Chip key={r} className="hs-chip--caught" pos={{ "--x": "64%", "--y": `${22 + i * 15}%`, "--fx": "180px", "--fy": `${-30 + i * 20}px`, "--delay": `${0.6 + i * 0.45}s` }}>
            <span className="hs-chip__dot" />
            <span className="hs-chip__label">{r}</span>
          </Chip>
        ))}
      </>
    );
  }
  return null;
}
