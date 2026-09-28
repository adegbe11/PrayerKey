import { playUrl } from "@/lib/app-store";

// "Get it on Google Play", drawn in the badge's own black-and-white style so it stays sharp at any size.
export default function PlayBadge({ source = "download", size = "md", className = "" }: { source?: Parameters<typeof playUrl>[0]; size?: "md" | "lg"; className?: string }) {
  return (
    <a href={playUrl(source)} target="_blank" rel="noopener" className={`pk-play pk-play--${size} ${className}`} aria-label="Get PrayerKey on Google Play">
      <svg viewBox="0 0 28 30" aria-hidden width="26" height="28">
        <path d="M1.1 1.3C.8 1.7.6 2.3.6 3v24c0 .7.2 1.3.5 1.7L14.8 15 1.1 1.3z" fill="#00D7FE" />
        <path d="M19.4 19.6 14.8 15l4.6-4.6 5.5 3.1c1.6.9 1.6 2.3 0 3.2l-5.5 2.9z" fill="#FFCE00" />
        <path d="M19.4 19.6 14.8 15 1.1 28.7c.5.5 1.3.6 2.3.1l16-9.2" fill="#FF3A44" />
        <path d="M19.4 10.4 3.4 1.2C2.4.6 1.6.7 1.1 1.3L14.8 15l4.6-4.6z" fill="#00F076" />
      </svg>
      <span className="pk-play__text"><small>GET IT ON</small><strong>Google Play</strong></span>
    </a>
  );
}
