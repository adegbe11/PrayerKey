import type { CSSProperties } from "react";

// A real app screenshot in a clean phone frame.
export default function Phone({ src, alt, className = "", style }: { src: string; alt: string; className?: string; style?: CSSProperties }) {
  return (
    <span className={`pk-phone ${className}`} style={style}>
      <span className="pk-phone__screen">
        <img src={src} alt={alt} loading="lazy" />
      </span>
    </span>
  );
}
