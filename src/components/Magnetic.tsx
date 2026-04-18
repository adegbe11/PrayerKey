'use client';

import { useRef, type ReactNode, type HTMLAttributes, type ElementType } from 'react';

interface MagneticProps extends HTMLAttributes<HTMLElement> {
  children: ReactNode;
  strength?: number; // 0-1. 0.3 is subtle, 0.6 is pronounced
  as?: ElementType;
}

/**
 * Magnetic wrapper — content leans toward the cursor on hover.
 * Uses CSS transform on child, not the wrapper, so layout stays stable.
 */
export default function Magnetic({
  children,
  strength = 0.35,
  as: Tag = 'span',
  ...rest
}: MagneticProps) {
  const ref = useRef<HTMLElement>(null);

  const handleMove = (e: React.MouseEvent<HTMLElement>) => {
    const el = ref.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const relX = e.clientX - rect.left - rect.width / 2;
    const relY = e.clientY - rect.top - rect.height / 2;
    el.style.transform = `translate(${relX * strength}px, ${relY * strength}px)`;
  };

  const handleLeave = () => {
    const el = ref.current;
    if (!el) return;
    el.style.transform = 'translate(0, 0)';
  };

  return (
    <Tag
      {...rest}
      data-magnetic="true"
      onMouseMove={handleMove}
      onMouseLeave={handleLeave}
      style={{ display: 'inline-block', ...(rest.style ?? {}) }}
    >
      <span
        ref={ref as React.Ref<HTMLSpanElement>}
        style={{
          display: 'inline-block',
          transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
          willChange: 'transform',
        }}
      >
        {children}
      </span>
    </Tag>
  );
}
