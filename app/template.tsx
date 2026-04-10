"use client";
import { useEffect, useRef, ReactNode } from "react";
import { usePathname } from "next/navigation";

export default function Template({ children }: { children: ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  const pathname = usePathname();

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.style.opacity = "0";
    el.style.transform = "translateY(12px)";
    // Force reflow
    void el.offsetHeight;
    el.style.transition = "opacity 380ms cubic-bezier(0.22,1,0.36,1), transform 380ms cubic-bezier(0.22,1,0.36,1)";
    el.style.opacity = "1";
    el.style.transform = "translateY(0)";
  }, [pathname]);

  return (
    <div ref={ref} style={{ willChange: "opacity, transform" }}>
      {children}
    </div>
  );
}
