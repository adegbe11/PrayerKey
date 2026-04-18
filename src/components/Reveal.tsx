'use client';

import { motion, type HTMLMotionProps } from 'framer-motion';
import { useRef, useEffect, useState } from 'react';

type RevealProps = Omit<HTMLMotionProps<'div'>, 'initial' | 'animate' | 'transition'> & {
  children: React.ReactNode;
  delay?: number;
  duration?: number;
  y?: number;
  once?: boolean;
};

/**
 * Fade + slide up on enter viewport.
 * Uses IntersectionObserver, no extra deps.
 */
export default function Reveal({
  children,
  delay = 0,
  duration = 0.8,
  y = 40,
  once = true,
  ...rest
}: RevealProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!ref.current) return;
    const el = ref.current;
    const obs = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          if (once) obs.disconnect();
        } else if (!once) {
          setVisible(false);
        }
      },
      { threshold: 0.15, rootMargin: '0px 0px -8% 0px' }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, [once]);

  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y }}
      animate={visible ? { opacity: 1, y: 0 } : { opacity: 0, y }}
      transition={{ duration, delay, ease: [0.16, 1, 0.3, 1] }}
      {...rest}
    >
      {children}
    </motion.div>
  );
}

/** Splits a string into word-level <span>s that stagger in from below. */
export function SplitReveal({
  text,
  className,
  style,
  delay = 0,
  stagger = 0.04,
  duration = 0.9,
}: {
  text: string;
  className?: string;
  style?: React.CSSProperties;
  delay?: number;
  stagger?: number;
  duration?: number;
}) {
  const ref = useRef<HTMLSpanElement>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!ref.current) return;
    const el = ref.current;
    const obs = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          obs.disconnect();
        }
      },
      { threshold: 0.3 }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, []);

  const words = text.split(' ');

  return (
    <span ref={ref} className={className} style={{ ...style, display: 'inline-block' }}>
      {words.map((w, i) => (
        <span
          key={i}
          style={{ display: 'inline-block', overflow: 'hidden', verticalAlign: 'top' }}
        >
          <motion.span
            initial={{ y: '105%' }}
            animate={visible ? { y: 0 } : { y: '105%' }}
            transition={{
              duration,
              delay: delay + i * stagger,
              ease: [0.77, 0, 0.175, 1],
            }}
            style={{ display: 'inline-block', whiteSpace: 'pre' }}
          >
            {w}
            {i < words.length - 1 ? '\u00A0' : ''}
          </motion.span>
        </span>
      ))}
    </span>
  );
}
