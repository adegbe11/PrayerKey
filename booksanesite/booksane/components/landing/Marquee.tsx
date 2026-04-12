export default function Marquee() {
  const items = [
    'PDF EXPORT', 'EPUB 3.0', '10 TEMPLATES', 'KDP-READY',
    'NO SIGNUP', 'AUTO CHAPTERS', 'FORMAT IN 3 SECONDS', 'PAPERBACK PREVIEW',
    'EBOOK READER', 'FIX MY BOOK', '100% FREE', 'PRINT-READY',
  ];
  const repeated = [...items, ...items]; // duplicate for seamless loop

  return (
    <div
      className="w-full overflow-hidden py-4"
      style={{
        background: '#FFE500',
        borderTop: '3px solid #000',
        borderBottom: '3px solid #000',
      }}
    >
      <div className="marquee-track">
        {repeated.map((item, i) => (
          <span
            key={i}
            className="flex items-center gap-2 shrink-0"
            style={{ paddingRight: '2.5rem', fontSize: '13px', fontWeight: 900, letterSpacing: '0.12em', color: '#000' }}
          >
            {item}
            <span style={{ color: 'rgba(0,0,0,0.35)', fontSize: '16px' }}>✦</span>
          </span>
        ))}
      </div>
    </div>
  );
}
