"use client";
import { useState } from "react";
import PrayerCard from "./PrayerCard";

type Group = { name: string; desc: string; items: { slug: string; title: string }[] };

const FIRST = 12;

// Every prayer, one shelf at a time, each one a card. All of them are in the
// page for anyone (or any search engine) that wants them; the tabs only
// choose what is shown.
export default function PrayerLibrary({ groups, base = "/prayer" }: { groups: Group[]; base?: string }) {
  const [open, setOpen] = useState(0);
  const [all, setAll] = useState<Record<number, boolean>>({});
  return (
    <div className="ph-lib">
      <div className="ph-tabs" role="tablist" aria-label="Prayer categories">
        {groups.map((g, i) => (
          <button
            key={g.name}
            type="button"
            role="tab"
            id={`ph-tab-${i}`}
            aria-selected={open === i}
            aria-controls={`ph-panel-${i}`}
            className={open === i ? "is-on" : ""}
            onClick={() => setOpen(i)}
          >
            {g.name}
          </button>
        ))}
      </div>
      {groups.map((g, i) => (
        <div key={g.name} id={`ph-panel-${i}`} role="tabpanel" aria-labelledby={`ph-tab-${i}`} hidden={open !== i} className={`ph-panel ${all[i] ? "is-all" : ""}`}>
          <ul className="ph-cards">
            {g.items.map((p, j) => (
              <li key={p.slug} className={j >= FIRST ? "ph-cards__more" : ""} style={{ "--j": Math.min(j, FIRST) } as React.CSSProperties}>
                <PrayerCard slug={p.slug} title={p.title} href={`${base}/${p.slug}`} />
              </li>
            ))}
          </ul>
          {g.items.length > FIRST && !all[i] && (
            <button type="button" className="ph-cards__all" onClick={() => setAll((a) => ({ ...a, [i]: true }))}>
              Show all {g.items.length}
            </button>
          )}
        </div>
      ))}
    </div>
  );
}
