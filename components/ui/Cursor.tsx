"use client";
import { useEffect } from "react";

export default function Cursor() {
  useEffect(() => {
    const dot  = document.getElementById("pk-cursor");
    const ring = document.getElementById("pk-cursor-ring");
    if (!dot || !ring) return;

    let tx = 0, ty = 0; // target (mouse position)
    let rx = 0, ry = 0; // ring current position

    function onMove(e: MouseEvent) {
      tx = e.clientX;
      ty = e.clientY;
      dot!.style.left = tx + "px";
      dot!.style.top  = ty + "px";
    }

    let animId: number;
    function loop() {
      // Lerp ring toward mouse
      rx += (tx - rx) * 0.12;
      ry += (ty - ry) * 0.12;
      ring!.style.left = rx + "px";
      ring!.style.top  = ry + "px";
      animId = requestAnimationFrame(loop);
    }

    window.addEventListener("mousemove", onMove);
    animId = requestAnimationFrame(loop);

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
