"use client";
import { useEffect, useRef, ReactNode, CSSProperties } from "react";

interface ScrollRevealProps {
  children: ReactNode;
  delay?: number;       // ms delay before animating in
  className?: string;
  style?: CSSProperties;
  as?: keyof JSX.IntrinsicElements;
}

export default function ScrollReveal({
  children,
  delay = 0,
  className = "",
  style = {},
  as: Tag = "div",
}: ScrollRevealProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // Start hidden
    el.style.opacity = "0";
    el.style.transform = "translateY(28px)";
    el.style.transition = `opacity 600ms cubic-bezier(0.22,1,0.36,1) ${delay}ms, transform 600ms cubic-bezier(0.22,1,0.36,1) ${delay}ms`;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          el.style.opacity = "1";
          el.style.transform = "translateY(0)";
          observer.disconnect();
        }
      },
      { threshold: 0.1, rootMargin: "0px 0px -40px 0px" }
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, [delay]);

  return (
    // @ts-expect-error dynamic tag
    <Tag ref={ref} className={className} style={style}>
      {children}
    </Tag>
  );
}
