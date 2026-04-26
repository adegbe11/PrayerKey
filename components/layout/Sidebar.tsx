"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { motion } from "framer-motion";
import {
  Home,
  Radio,
  Sparkles,
  Compass,
  LayoutDashboard,
  Users,
  Heart,
  BookOpen,
  BookMarked,
  MessageSquare,
} from "lucide-react";

const NAV_SECTIONS = [
  {
    label: "Main",
    items: [
      { href: "/", icon: Home, label: "Home" },
      { href: "/live", icon: Radio, label: "Live Sermon", live: true },
      { href: "/prayer", icon: Sparkles, label: "Prayer AI" },
      { href: "/discover", icon: Compass, label: "Discover" },
    ],
  },
  {
    label: "Church",
    items: [
      { href: "/church", icon: LayoutDashboard, label: "Dashboard" },
      { href: "/church/members", icon: Users, label: "Members" },
      { href: "/church/giving", icon: Heart, label: "Giving" },
      { href: "/church/sermons", icon: BookOpen, label: "Sermons" },
    ],
  },
  {
    label: "Personal",
    items: [
      { href: "/journal", icon: BookMarked, label: "Prayer Journal" },
      { href: "/community", icon: MessageSquare, label: "Community" },
    ],
  },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside
      className="flex flex-col h-full overflow-y-auto"
      style={{
        width: "196px",
        minWidth: "196px",
        background: "var(--pk-sidebar)",
        borderRight: "0.5px solid var(--pk-b1)",
      }}
    >
      {/* Logo */}
      <div className="flex items-center gap-2 px-4 py-5">
        <img src="/prayerkey-logo.png" alt="PrayerKey" width={28} height={28} style={{ borderRadius: "6px", objectFit: "cover", display: "block" }} />
        <span
          style={{
            fontSize: "15px",
            fontWeight: 700,
            letterSpacing: "-0.02em",
            color: "var(--pk-t1)",
          }}
        >
          Prayer<span style={{ color: "var(--pk-gold)" }}>Key</span>
        </span>
      </div>

      {/* Nav sections */}
      <nav className="flex-1 px-2 pb-4">
        {NAV_SECTIONS.map((section) => (
          <div key={section.label} className="mb-5">
            {/* Apple-spec section label */}
            <p
              className="px-2 mb-1"
              style={{
                fontSize: "11px",
                fontWeight: 600,
                letterSpacing: "0.06em",
                textTransform: "uppercase",
                color: "var(--pk-t3)",
              }}
            >
              {section.label}
            </p>

            {section.items.map((item) => {
              const isActive =
                item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
              const Icon = item.icon;

              return (
                <motion.div key={item.href} whileHover={{ scale: 1.005 }} transition={{ duration: 0.12 }}>
                  <Link
                    href={item.href}
                    className="flex items-center gap-2.5 px-2.5 py-1.5 rounded-lg mb-0.5"
                    style={{
                      background: isActive ? "var(--pk-gold-dim)" : "transparent",
                      color: isActive ? "var(--pk-gold)" : "var(--pk-t2)",
                      textDecoration: "none",
                      transition: "background var(--pk-duration) var(--pk-ease), color var(--pk-duration) var(--pk-ease)",
                      cursor: "pointer",
                    }}
                  >
                    <Icon
                      size={14}
                      strokeWidth={isActive ? 2 : 1.6}
                      style={{ color: isActive ? "var(--pk-gold)" : "var(--pk-t3)", flexShrink: 0 }}
                    />
                    <span
                      style={{
                        fontSize: "13px",
                        fontWeight: isActive ? 600 : 400,
                        letterSpacing: "-0.003em",
                      }}
                    >
                      {item.label}
                    </span>

                    {/* Live dot */}
                    {item.live && (
                      <span
                        className="ml-auto w-1.5 h-1.5 rounded-full animate-live-pulse"
                        style={{ background: "var(--pk-live)", flexShrink: 0 }}
                      />
                    )}
                  </Link>
                </motion.div>
              );
            })}
          </div>
        ))}
      </nav>

      {/* Church info card — elevated white card */}
      <div
        className="mx-2 mb-3 p-3 rounded-xl"
        style={{
          background: "#FFFFFF",
          boxShadow: "var(--pk-shadow-sm)",
        }}
      >
        <div className="flex items-center gap-2 mb-2">
          <div
            className="w-6 h-6 rounded-md flex items-center justify-center flex-shrink-0"
            style={{
              background: "var(--pk-gold-dim)",
              border: "0.5px solid var(--pk-gold-border)",
            }}
          >
            <span style={{ fontSize: "11px" }}>✝</span>
          </div>
          <div className="min-w-0">
            <p
              className="truncate"
              style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}
            >
              Grace Community
            </p>
            <p style={{ fontSize: "10px", color: "var(--pk-t3)" }}>Lagos, Nigeria</p>
          </div>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full" style={{ background: "var(--pk-teal)" }} />
          <span style={{ fontSize: "10px", color: "var(--pk-teal)", fontWeight: 500 }}>
            No active service
          </span>
        </div>
      </div>
    </aside>
  );
}
