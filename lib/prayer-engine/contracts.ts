import { z } from "zod";

export const matterId = z.string().uuid();
export const createMatter = z.object({
  id: matterId,
  title: z.string().trim().min(1).max(300),
  intention: z.string().trim().min(1).max(10000),
  topics: z.array(z.string().trim().min(1).max(80)).max(20).default([]),
}).strict();
export const reviseMatter = z.object({
  version: z.number().int().positive(),
  title: z.string().trim().min(1).max(300).optional(),
  intention: z.string().trim().min(1).max(10000).optional(),
  topics: z.array(z.string().trim().min(1).max(80)).max(20).optional(),
  stage: z.enum(["praying", "waiting", "acting", "released"]).optional(),
  next_action: z.string().trim().max(3000).optional(),
}).strict().refine(value => Object.keys(value).length > 1);
const text = z.string().trim().min(1);
export const appendMatter = z.discriminatedUnion("kind", [
  z.object({ kind: z.literal("session"), id: matterId, prayer: text.max(30000), source: z.enum(["pray", "scripture", "journal", "sermon", "morning", "evening", "challenge", "fast"]), completed_at: z.string().datetime({ offset: true }).nullable().default(null) }).strict(),
  z.object({ kind: z.literal("journal"), id: matterId, body: text.max(30000) }).strict(),
  z.object({ kind: z.literal("point"), id: matterId, body: text.max(3000) }).strict(),
  z.object({ kind: z.literal("update"), id: matterId, body: text.max(10000) }).strict(),
  z.object({ kind: z.literal("scripture"), reference: text.max(100) }).strict(),
  z.object({ kind: z.literal("person"), target_id: matterId }).strict(),
  z.object({ kind: z.literal("sermon"), target_id: matterId }).strict(),
  z.object({ kind: z.literal("routine"), target_id: matterId }).strict(),
  z.object({ kind: z.literal("outcome"), version: z.number().int().positive(), stage: z.enum(["answered", "answered_differently"]), testimony: text.max(10000) }).strict(),
]);
