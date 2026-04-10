"use client";

import { Search } from "lucide-react";

export function TitleBar() {
  return (
    <header
      className="flex items-center justify-between px-4 flex-shrink-0"
      style={{
        height: "44px",
        background: "var(--pk-nav-bg)",
        backdropFilter: "var(--pk-nav-blur)",
        WebkitBackdropFilter: "var(--pk-nav-blur)",
        borderBottom: "0.5px solid var(--pk-b1)",
        position: "relative",
        zIndex: 100,
      }}
    >
      {/* macOS traffic light dots */}
      <div className="flex items-center gap-1.5">
        <button
          className="w-[11px] h-[11px] rounded-full cursor-pointer transition-opacity hover:opacity-70"
          style={{ background: "#FF5F56" }}
          aria-label="Close"
        />
        <button
          className="w-[11px] h-[11px] rounded-full cursor-pointer transition-opacity hover:opacity-70"
          style={{ background: "#FFBD2E" }}
          aria-label="Minimize"
        />
        <button
          className="w-[11px] h-[11px] rounded-full cursor-pointer transition-opacity hover:opacity-70"
          style={{ background: "#27C93F" }}
          aria-label="Expand"
        />
      </div>

      {/* Global search — centred, Apple-spec pill */}
      <div
        className="flex items-center gap-2 px-3 rounded-lg"
        style={{
          background: "rgba(0,0,0,0.05)",
          border: "0.5px solid var(--pk-b1)",
          height: "28px",
          width: "240px",
          cursor: "text",
          transition: "background var(--pk-duration) var(--pk-ease)",
        }}
      >
        <Search size={12} style={{ color: "var(--pk-t3)" }} strokeWidth={2} />
        <span style={{ fontSize: "13px", color: "var(--pk-t3)", letterSpacing: "-0.003em" }}>
          Search verses, prayers, sermons...
        </span>
      </div>

      {/* User avatar — gold-to-purple gradient */}
      <div
        className="w-[30px] h-[30px] rounded-full flex items-center justify-center cursor-pointer"
        style={{
          background: "linear-gradient(135deg, var(--pk-gold) 0%, var(--pk-purple) 100%)",
          fontSize: "11px",
          fontWeight: 600,
          color: "#FFFFFF",
          boxShadow: "0 1px 3px rgba(0,0,0,0.15)",
        }}
      >
        PA
      </div>
    </header>
  );
}
