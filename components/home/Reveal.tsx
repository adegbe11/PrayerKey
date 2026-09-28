"use client";
import { motion, useReducedMotion } from "framer-motion";
import type { CSSProperties, ReactNode } from "react";

// Content rises into place with a soft spring the first time it scrolls into view.
export default function Reveal({ children, delay = 0, y = 32, className, style }: { children: ReactNode; delay?: number; y?: number; className?: string; style?: CSSProperties }) {
  const reduce = useReducedMotion();
  return (
    <motion.div
      className={className}
      style={style}
      initial={reduce ? false : { opacity: 0, y }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-60px" }}
      transition={{ type: "spring", stiffness: 80, damping: 20, mass: 0.9, delay }}
    >
      {children}
    </motion.div>
  );
}

/** Words of a verse arriving one after another, like it is being read aloud. */
export function RevealWords({ text, className, style }: { text: string; className?: string; style?: CSSProperties }) {
  const reduce = useReducedMotion();
  const words = text.split(" ");
  return (
    <motion.span
      className={className}
      style={style}
      initial={reduce ? false : "hidden"}
      whileInView="shown"
      viewport={{ once: true, margin: "-80px" }}
      transition={{ staggerChildren: 0.045 }}
    >
      {words.map((word, i) => (
        <motion.span
          key={i}
          style={{ display: "inline-block", whiteSpace: "pre" }}
          variants={{ hidden: { opacity: 0, y: 14, filter: "blur(6px)" }, shown: { opacity: 1, y: 0, filter: "blur(0px)" } }}
          transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
        >
          {word}{i < words.length - 1 ? " " : ""}
        </motion.span>
      ))}
    </motion.span>
  );
}
