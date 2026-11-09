"use client";
import { useEffect, useRef, ReactNode } from "react";
import { usePathname } from "next/navigation";

export default function Template({ children }: { children: ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  const pathname = usePathname();

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.style.willChange = "opacity, transform";
    el.style.opacity = "0";
    el.style.transform = "translateY(12px)";
    // Force reflow
    void el.offsetHeight;
    el.style.transition = "opacity 380ms cubic-bezier(0.22,1,0.36,1), transform 380ms cubic-bezier(0.22,1,0.36,1)";
    el.style.opacity = "1";
    el.style.transform = "translateY(0)";
    // Once the page has arrived, let it go back to being an ordinary page.
    // Left on, the whole page stays one giant layer and every tap repaints it.
    const done = window.setTimeout(() => {
      el.style.willChange = "";
      el.style.transform = "";
      el.style.transition = "";
    }, 450);
    return () => window.clearTimeout(done);
  }, [pathname]);

  return <div ref={ref}>{children}</div>;
}
