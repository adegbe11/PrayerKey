'use client';

import { useEffect } from 'react';

export interface Shortcut {
  combo: string; // e.g. "mod+s", "mod+e", "mod+k", "mod+shift+n"
  handler: (e: KeyboardEvent) => void;
  description?: string;
}

const isMac = typeof navigator !== 'undefined' && /Mac|iPhone|iPad|iPod/.test(navigator.platform);

function matchCombo(e: KeyboardEvent, combo: string): boolean {
  const parts = combo.toLowerCase().split('+').map((s) => s.trim());
  const key = parts.pop() || '';
  const needMod = parts.includes('mod') || parts.includes('ctrl') || parts.includes('cmd');
  const needShift = parts.includes('shift');
  const needAlt = parts.includes('alt') || parts.includes('option');

  const mod = isMac ? e.metaKey : e.ctrlKey;
  if (needMod !== mod) return false;
  if (needShift !== e.shiftKey) return false;
  if (needAlt !== e.altKey) return false;

  return e.key.toLowerCase() === key;
}

export function useKeyboardShortcuts(shortcuts: Shortcut[], enabled = true): void {
  useEffect(() => {
    if (!enabled) return;
    const onKey = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement | null;
      const tag = target?.tagName;
      const isEditable =
        tag === 'INPUT' || tag === 'TEXTAREA' ||
        (target && 'isContentEditable' in target && (target as HTMLElement).isContentEditable);

      for (const sc of shortcuts) {
        if (!matchCombo(e, sc.combo)) continue;
        // Only run while typing if combo explicitly uses mod (e.g. Cmd+S still works in textarea)
        const needMod = sc.combo.toLowerCase().includes('mod');
        if (isEditable && !needMod) continue;
        e.preventDefault();
        sc.handler(e);
        return;
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [shortcuts, enabled]);
}
