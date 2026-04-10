import type { Metadata } from "next";
import "./globals.css";
import { TitleBar } from "@/components/layout/TitleBar";
import { Sidebar } from "@/components/layout/Sidebar";
import { BottomBar } from "@/components/layout/BottomBar";

export const metadata: Metadata = {
  title: "PrayerKey — The Operating System for Churches",
  description:
    "AI-powered church operating system. Live sermon verse detection, prayer AI, church management, and community — all in one platform.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {/* Full-height flex column */}
        <div className="flex flex-col h-screen overflow-hidden" style={{ background: "var(--pk-deep)" }}>
          {/* Title Bar — 44px, frosted glass */}
          <TitleBar />

          {/* Middle: sidebar + content */}
          <div className="flex flex-1 overflow-hidden">
            {/* Sidebar — 196px */}
            <Sidebar />

            {/* Main content — pure white, scrollable */}
            <main
              className="flex-1 overflow-y-auto"
              style={{ background: "var(--pk-panel)" }}
            >
              {children}
            </main>
          </div>

          {/* Bottom Bar — 56px, frosted glass */}
          <BottomBar />
        </div>
      </body>
    </html>
  );
}
