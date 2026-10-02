"use client";
import { useEffect, useState } from "react";
import { useReducedMotion as useFramerReducedMotion } from "framer-motion";

// The server never knows the visitor's motion setting, so the first browser render must match it.
// This answers "no preference" first and the real setting right after, which keeps hydration clean.
export function useReducedMotion(): boolean {
  const actual = useFramerReducedMotion();
  const [ready, setReady] = useState(false);
  useEffect(() => setReady(true), []);
  return ready ? !!actual : false;
}
