"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { BookOpen, HandHeart, Home, Menu, Radio, Sparkles, X } from "lucide-react";

// Public pages only: the prayer wall, testimonies and giving are for signed-in church members.
import DarkModeToggle from "@/components/ui/DarkModeToggle";

// The web version of the app's chrome: a floating glass capsule on desktop,
// and on phones the app's own violet tab bar at the bottom of the screen.

const LINKS = [
  { href: "/pray",   label: "Pray" },
  { href: "/bible",  label: "Bible" },
  { href: "/prayer", label: "Prayers" },
  { href: "/verse-of-the-day", label: "Verse of the Day" },
  { href: "/live",   label: "Live Sermon" },
  { href: "/guides", label: "Guides" },
  { href: "/download", label: "App" },
];

const DOCK = [
  { href: "/",      label: "Home",   Icon: Home },
  { href: "/bible", label: "Bible",  Icon: BookOpen },
  { href: "/pray",  label: "Pray",   Icon: Sparkles },
  { href: "/live",  label: "Church", Icon: Radio },
  { href: "/prayer", label: "Prayers", Icon: HandHeart },
];

const MENU = [
  ...LINKS,
  { href: "/about", label: "About" },
];

// Full-screen tools draw their own chrome.
const BARE = ["/live/projector"];
// Pages that open on a full-bleed night hero; the nav floats over it with no spacer.
const FULL_BLEED = ["/", "/download"];

function matches(path: string, href: string) {
  return href === "/" ? path === "/" : path === href || path.startsWith(href + "/");
}
/** Only the most specific link lights up: /pray/topics is Topics, not Pray. */
function activeIn(path: string, links: { href: string }[]) {
  return links.filter((l) => matches(path, l.href)).sort((a, b) => b.href.length - a.href.length)[0]?.href;
}

export default function CapsuleNav() {
  const path = usePathname() || "/";
  const [open, setOpen] = useState(false);
  const [overDark, setOverDark] = useState(path === "/");

  useEffect(() => { setOpen(false); }, [path]);
  useEffect(() => {
    // Locking the page re-lays out all of it. Do that the frame after the menu
    // shows, so the tap answers at once instead of waiting on the whole page.
    let frame = requestAnimationFrame(() => {
      frame = requestAnimationFrame(() => { document.body.style.overflow = open ? "hidden" : ""; });
    });
    return () => { cancelAnimationFrame(frame); document.body.style.overflow = ""; };
  }, [open]);
  // The nav turns to dark glass whenever it sits over a section marked data-nav="dark".
  // The browser tells us when a dark section crosses a thin line 40px down,
  // rather than the nav measuring every section on every scroll frame, which
  // kept a phone too busy to answer taps right after scrolling.
  useEffect(() => {
    let io: IntersectionObserver | null = null;
    const under = new Set<Element>();
    const watch = () => {
      io?.disconnect();
      under.clear();
      const below = Math.max(0, window.innerHeight - 41);
      io = new IntersectionObserver((entries) => {
        for (const e of entries) e.isIntersecting ? under.add(e.target) : under.delete(e.target);
        setOverDark(under.size > 0);
      }, { rootMargin: `-40px 0px -${below}px 0px`, threshold: 0 });
      const dark = document.querySelectorAll('[data-nav="dark"]');
      dark.forEach((el) => io!.observe(el));
      if (!dark.length) setOverDark(false);
    };
    watch();
    // the page below may still be arriving on the first frame; look again shortly after
    const later = [setTimeout(watch, 120), setTimeout(watch, 600)];
    window.addEventListener("resize", watch);
    return () => { io?.disconnect(); later.forEach(clearTimeout); window.removeEventListener("resize", watch); };
  }, [path]);

  if (BARE.some((p) => path.startsWith(p))) return null;
  const activeLink = activeIn(path, MENU);
  const activeTab = activeIn(path, DOCK);
  const dark = overDark && !open;

  return (
    <>
      <header className={`pk-nav ${dark ? "pk-nav--dark" : ""}`}>
        <div className="pk-nav__bar">
          <Link href="/" className="pk-nav__brand" aria-label="PrayerKey home">
            <img src="/pk-mark.png" alt="" width={15} height={26} />
            <span>PrayerKey</span>
          </Link>

          <nav className="pk-nav__links" aria-label="Main">
            {LINKS.map((l) => (
              <Link key={l.href} href={l.href} className={activeLink === l.href ? "is-active" : ""}>{l.label}</Link>
            ))}
          </nav>

          <div className="pk-nav__end">
            <DarkModeToggle />
            <Link href="/pray" className="pk-capsule pk-capsule--violet pk-nav__cta">Pray now</Link>
            <button type="button" className="pk-nav__menu" aria-label={open ? "Close menu" : "Open menu"} aria-expanded={open} onClick={() => setOpen((o) => !o)}>
              {open ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>
      </header>
      {!FULL_BLEED.includes(path) && <div aria-hidden className="pk-nav__spacer" />}

      {/* Phone menu: every section, large and calm. */}
      <div className={`pk-sheet ${open ? "is-open" : ""}`} aria-hidden={!open}>
        <nav aria-label="Menu">
          {MENU.map((l, i) => (
            <Link key={l.href} href={l.href} tabIndex={open ? 0 : -1} style={{ transitionDelay: open ? `${60 + i * 40}ms` : "0ms" }} className={activeLink === l.href ? "is-active" : ""}>{l.label}</Link>
          ))}
        </nav>
      </div>

      {/* The app's tab bar, on the web - but not on the home page, which is
          a story about the app and has one thing to say, not five places to go. */}
      {path !== "/" && <nav className="pk-dock" aria-label="Tabs">
        {DOCK.map(({ href, label, Icon }) => {
          const on = activeTab === href;
          return (
            <Link key={href} href={href} className={on ? "is-active" : ""} aria-current={on ? "page" : undefined}>
              <Icon size={21} strokeWidth={on ? 2.4 : 2} />
              <span>{label}</span>
            </Link>
          );
        })}
      </nav>}
    </>
  );
}
