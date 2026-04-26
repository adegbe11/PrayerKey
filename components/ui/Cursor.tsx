"use client";
import { useEffect } from "react";

export default function Cursor() {
  useEffect(() => {
    // Skip on touch-primary devices — cursor is hidden in CSS but this also
    // prevents the rAF loop from running on mobile, saving main-thread time.
    if (window.matchMedia("(hover: none)").matches) return;

    const dot  = document.getElementById("pk-cursor");
    const ring = document.getElementById("pk-cursor-ring");
    if (!dot || !ring) return;

    let tx = 0, ty = 0; // mouse target
    let rx = 0, ry = 0; // ring lerp position
    let moved = false;   // dirty flag — only start rAF once mouse has moved

    function onMove(e: MouseEvent) {
      tx = e.clientX;
      ty = e.clientY;
      dot!.style.left = tx + "px";
      dot!.style.top  = ty + "px";
      if (!moved) {
        moved = true;
        animId = requestAnimationFrame(loop);
      }
    }

    let animId: number;
    function loop() {
      const dx = tx - rx;
      const dy = ty - ry;

      // Only paint if the ring needs to move more than 0.1px
      if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
        rx += dx * 0.12;
        ry += dy * 0.12;
        ring!.style.left = rx + "px";
        ring!.style.top  = ry + "px";
      }

      animId = requestAnimationFrame(loop);
    }

    window.addEventListener("mousemove", onMove, { passive: true });

    return () => {
      window.removeEventListener("mousemove", onMove);
      cancelAnimationFrame(animId);
    };
  }, []);

  return (
    <>
      <div id="pk-cursor" />
      <div id="pk-cursor-ring" />
    </>
  );
}
