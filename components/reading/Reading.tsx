import type { ReactNode } from "react";
import Link from "next/link";
import ReadingToc, { type TocItem } from "./ReadingToc";

// The reading kit shared by the manual, the legal pages and support:
// a quiet hero, a contents rail, calm sections, steps, tips and questions.
// Pages import "@/components/reading/reading.css" alongside it.

export type { TocItem };

export function ReadingHero({ crumbs, title, lede, meta }: {
  crumbs?: { href?: string; label: string }[];
  title: string;
  lede?: ReactNode;
  meta?: ReactNode;
}) {
  return (
    <header className="rd-hero">
      {crumbs && (
        <nav className="rd-crumbs" aria-label="Breadcrumb">
          {crumbs.map((c, i) => (
            <span key={c.label}>
              {i > 0 && <span aria-hidden className="rd-crumbs__sep">/</span>}
              {c.href ? <Link href={c.href}>{c.label}</Link> : <span aria-current="page">{c.label}</span>}
            </span>
          ))}
        </nav>
      )}
      <h1 className="rd-title">{title}</h1>
      {lede && <p className="rd-lede">{lede}</p>}
      {meta && <p className="rd-meta">{meta}</p>}
    </header>
  );
}

// Rail and body. Without contents it is a single centred column.
export function ReadingLayout({ toc, children }: { toc?: TocItem[]; children: ReactNode }) {
  if (!toc?.length) return <div className="rd-layout rd-layout--single"><div className="rd-body">{children}</div></div>;
  return (
    <div className="rd-layout">
      <ReadingToc items={toc} />
      <div className="rd-body">
        {/* on a phone the rail becomes a list you can open */}
        <details className="rd-contents">
          <summary>Contents<span className="rd-plus" aria-hidden /></summary>
          <ol>
            {toc.map((t) => <li key={t.id}><a href={`#${t.id}`}>{t.label}</a></li>)}
          </ol>
        </details>
        {children}
      </div>
    </div>
  );
}

export function Section({ id, title, children }: { id: string; title: ReactNode; children: ReactNode }) {
  return (
    <section id={id} className="rd-sec" data-in="">
      <h2 className="rd-h2">{title}</h2>
      {children}
    </section>
  );
}

export function Steps({ children }: { children: ReactNode }) {
  return <ol className="rd-steps">{children}</ol>;
}

export function Tip({ label = "Tip", children }: { label?: string; children: ReactNode }) {
  return (
    <aside className="rd-tip">
      <strong>{label}</strong>
      <p>{children}</p>
    </aside>
  );
}

export function Faq({ items }: { items: { q: string; a: ReactNode }[] }) {
  return (
    <div className="rd-faq">
      {items.map(({ q, a }) => (
        <details key={q}>
          <summary>{q}<span className="rd-plus" aria-hidden /></summary>
          <p>{a}</p>
        </details>
      ))}
    </div>
  );
}

// a short closing panel with one clear action
export function ReadingEnd({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="rd-end" data-in="">
      <h2 className="rd-end__title">{title}</h2>
      {children}
    </section>
  );
}
