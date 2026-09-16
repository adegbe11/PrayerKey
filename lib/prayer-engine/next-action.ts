/** Deterministic orchestration. Recommendations never assert God's intent or an outcome. */
export type MatterStage = "praying" | "waiting" | "acting" | "released" | "answered" | "answered_differently";
export interface MatterContext {
  id: string;
  intention: string;
  topics: string[];
  stage: MatterStage;
  next_action: string;
}
export interface RememberedMatter {
  id: string;
  title: string;
  topics: string[];
  stage: MatterStage;
  testimony: string;
}
export interface PrayerContext {
  matter: MatterContext;
  scriptures: { reference: string; text: string; translation: "KJV" }[];
  completedSessions: number;
  activeRoutines: { id: string; kind: "fast" | "challenge" }[];
  memories: RememberedMatter[];
}
export function nextPrayerAction(context: PrayerContext) {
  const matter = context.matter;
  const memories = context.memories.filter(item => item.id !== matter.id &&
    ["answered", "answered_differently"].includes(item.stage) && item.testimony.trim() &&
    item.topics.some(topic => matter.topics.includes(topic)))
    .slice(0, 3).map(item => ({ matterId: item.id, label: `Remember: ${item.title}`, testimony: item.testimony }));
  const action = ["answered", "answered_differently"].includes(matter.stage)
    ? { kind: "remember", label: "Remember what happened", matterId: matter.id }
    : matter.stage === "released"
    ? { kind: "reflect", label: "Take a moment to reflect", matterId: matter.id }
    : matter.next_action.trim()
    ? { kind: "action", label: "Your next step", detail: matter.next_action, matterId: matter.id }
    : context.activeRoutines.length
    ? { kind: "routine", label: "Continue your prayer rhythm", routineId: context.activeRoutines[0].id, matterId: matter.id }
    : { kind: "pray", label: context.completedSessions ? "Keep praying" : "Bring this to God", matterId: matter.id };
  return { action, memories, scriptures: context.scriptures };
}
