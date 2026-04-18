'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { Search, CornerDownLeft } from 'lucide-react';

export interface Command {
  id: string;
  label: string;
  hint?: string;
  shortcut?: string;
  group?: string;
  icon?: React.ReactNode;
  run: () => void;
}

interface Props {
  open: boolean;
  commands: Command[];
  onClose: () => void;
}

function fuzzyScore(query: string, target: string): number {
  if (!query) return 1;
  const q = query.toLowerCase();
  const t = target.toLowerCase();
  if (t.includes(q)) return 100 + (t.startsWith(q) ? 50 : 0) - (t.length - q.length) * 0.5;
  let qi = 0;
  let score = 0;
  let consec = 0;
  for (let ti = 0; ti < t.length && qi < q.length; ti++) {
    if (t[ti] === q[qi]) {
      score += 8 + consec * 3;
      consec++;
      qi++;
    } else {
      consec = 0;
    }
  }
  if (qi < q.length) return 0;
  return score;
}

export default function CommandPalette({ open, commands, onClose }: Props) {
  const [query, setQuery] = useState('');
  const [index, setIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (open) {
      setQuery('');
      setIndex(0);
      setTimeout(() => inputRef.current?.focus(), 30);
    }
  }, [open]);

  const filtered = useMemo(() => {
    return commands
      .map((c) => ({ c, score: fuzzyScore(query, c.label + ' ' + (c.hint ?? '') + ' ' + (c.group ?? '')) }))
      .filter((r) => r.score > 0)
      .sort((a, b) => b.score - a.score)
      .map((r) => r.c);
  }, [query, commands]);

  useEffect(() => {
    if (index >= filtered.length) setIndex(Math.max(0, filtered.length - 1));
  }, [filtered.length, index]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        setIndex((i) => Math.min(filtered.length - 1, i + 1));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setIndex((i) => Math.max(0, i - 1));
      } else if (e.key === 'Enter') {
        e.preventDefault();
        const cmd = filtered[index];
        if (cmd) {
          cmd.run();
          onClose();
        }
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, filtered, index, onClose]);

  if (!open) return null;

  const groups = new Map<string, Command[]>();
  for (const c of filtered) {
    const g = c.group ?? 'Actions';
    if (!groups.has(g)) groups.set(g, []);
    groups.get(g)!.push(c);
  }

  let running = -1;

  return (
    <>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        transition={{ duration: 0.15 }}
        className="fixed inset-0 z-[60]"
        style={{
          background: 'rgba(5,3,12,0.55)',
          backdropFilter: 'blur(28px) saturate(160%)',
          WebkitBackdropFilter: 'blur(28px) saturate(160%)',
        }}
        onClick={onClose}
      />
      <motion.div
        initial={{ opacity: 0, y: -10, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        exit={{ opacity: 0, y: -10, scale: 0.98 }}
        transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
        className="fixed left-1/2 top-[14vh] z-[61] w-full max-w-xl -translate-x-1/2 overflow-hidden glass-strong"
        style={{ borderRadius: '18px' }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="flex items-center gap-2 px-4"
          style={{ borderBottom: '1px solid rgba(255,255,255,0.06)', height: '48px' }}
        >
          <Search size={14} color="var(--text-muted)" />
          <input
            ref={inputRef}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search commands…"
            className="flex-1 bg-transparent focus:outline-none text-sm"
            style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-body)' }}
          />
          <span
            className="font-semibold"
            style={{
              fontSize: '9px',
              color: 'var(--text-muted)',
              padding: '2px 6px',
              background: 'rgba(255,255,255,0.05)',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: '4px',
              letterSpacing: '0.08em',
            }}
          >
            ESC
          </span>
        </div>

        <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
          {filtered.length === 0 ? (
            <p className="px-4 py-10 text-center text-xs" style={{ color: 'var(--text-muted)' }}>
              No matches
            </p>
          ) : (
            Array.from(groups.entries()).map(([group, cmds]) => (
              <div key={group}>
                <div
                  className="px-4 pt-3 pb-1 font-black uppercase tracking-widest"
                  style={{ fontSize: '9px', color: 'var(--text-muted)', letterSpacing: '0.13em' }}
                >
                  {group}
                </div>
                {cmds.map((c) => {
                  running++;
                  const isActive = running === index;
                  return (
                    <button
                      key={c.id}
                      onClick={() => {
                        c.run();
                        onClose();
                      }}
                      onMouseEnter={() => setIndex(filtered.indexOf(c))}
                      className="w-full flex items-center gap-3 px-4 py-2 text-left transition-colors"
                      style={{
                        background: isActive ? 'rgba(124,58,237,0.18)' : 'transparent',
                      }}
                    >
                      {c.icon && (
                        <span
                          className="flex items-center justify-center shrink-0"
                          style={{
                            width: '22px',
                            height: '22px',
                            borderRadius: '6px',
                            background: 'rgba(255,255,255,0.05)',
                            color: isActive ? '#C4B5FD' : 'var(--text-muted)',
                          }}
                        >
                          {c.icon}
                        </span>
                      )}
                      <div className="flex-1 min-w-0">
                        <p
                          className="text-sm font-medium truncate"
                          style={{
                            color: isActive ? 'var(--text-primary)' : 'var(--text-secondary)',
                            fontSize: '13px',
                          }}
                        >
                          {c.label}
                        </p>
                        {c.hint && (
                          <p
                            className="text-xs truncate"
                            style={{
                              color: 'var(--text-muted)',
                              fontSize: '11px',
                              opacity: 0.7,
                            }}
                          >
                            {c.hint}
                          </p>
                        )}
                      </div>
                      {c.shortcut && (
                        <span
                          className="font-semibold shrink-0"
                          style={{
                            fontSize: '10px',
                            color: 'var(--text-muted)',
                            padding: '2px 6px',
                            background: 'rgba(255,255,255,0.04)',
                            border: '1px solid rgba(255,255,255,0.07)',
                            borderRadius: '4px',
                            letterSpacing: '0.08em',
                          }}
                        >
                          {c.shortcut}
                        </span>
                      )}
                      {isActive && !c.shortcut && (
                        <CornerDownLeft size={12} color="var(--text-muted)" />
                      )}
                    </button>
                  );
                })}
              </div>
            ))
          )}
        </div>
      </motion.div>
    </>
  );
}
