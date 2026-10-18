"use client";
import { useState } from "react";

// The chapter around the verse. A few verses either side are shown; the rest
// are in the page and open with one tap.
export default function ChapterWindow({ children, total, label }: { children: React.ReactNode; total: number; label: string }) {
  const [all, setAll] = useState(false);
  return (
    <div className={`vs-chapter ${all ? "is-all" : ""}`}>
      <div className="vs-chapter__text">{children}</div>
      {!all && total > 9 && (
        <button type="button" className="vs-chapter__all" onClick={() => setAll(true)}>
          Read all of {label}
        </button>
      )}
    </div>
  );
}
