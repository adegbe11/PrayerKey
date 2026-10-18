"use client";
import { useEffect } from "react";

// Slides the little shelf sideways so the book you are on is in the middle,
// without moving the page itself.
export default function ShelfHere() {
  useEffect(() => {
    const shelf = document.querySelector<HTMLElement>(".bk-spines");
    const here = shelf?.querySelector<HTMLElement>(".is-here");
    if (!shelf || !here) return;
    shelf.scrollLeft = here.offsetLeft - shelf.clientWidth / 2 + here.offsetWidth / 2;
  }, []);
  return null;
}
