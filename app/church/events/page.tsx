import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import CreateEventForm from "@/components/church/CreateEventForm";

export default async function EventsPage() {
  const session = await auth();
  if (!session?.user) redirect("/login");

  const viewer = await prisma.user.findUnique({
    where:  { id: session.user.id as string },
    select: { churchId: true, role: true },
  });

  if (!viewer?.churchId) redirect("/");

  const isAdmin = ["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(viewer.role);

  const now    = new Date();
  const events = await prisma.event.findMany({
    where:   { churchId: viewer.churchId },
    orderBy: { date: "asc" },
  });

  const upcoming = events.filter((e) => new Date(e.date) >= now);
  const past     = events.filter((e) => new Date(e.date) < now);

  const formatDate = (d: Date) =>
    new Date(d).toLocaleDateString("en-US", {
      weekday: "long", year: "numeric", month: "long", day: "numeric",
    });

  const formatTime = (d: Date) =>
    new Date(d).toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });

  function EventRow({ event, dim = false }: { event: typeof events[0]; dim?: boolean }) {
    return (
      <div
        className="pk-hover-row"
        style={{
          display:      "flex",
          gap:          "16px",
          alignItems:   "center",
          padding:      "16px 20px",
          opacity:      dim ? 0.55 : 1,
        }}
      >
        {/* Date badge */}
        <div
          style={{
            width:          "52px",
            height:         "52px",
            background:     dim ? "var(--pk-b1)" : "rgba(176,124,31,0.08)",
            borderRadius:   "12px",
            display:        "flex",
            flexDirection:  "column",
            alignItems:     "center",
            justifyContent: "center",
            flexShrink:     0,
          }}
        >
          <p style={{ fontSize: "18px", fontWeight: 800, color: dim ? "var(--pk-t3)" : "var(--pk-gold)", margin: 0, lineHeight: 1 }}>
            {new Date(event.date).getDate()}
          </p>
          <p style={{ fontSize: "9px", fontWeight: 600, color: dim ? "var(--pk-t3)" : "var(--pk-gold)", margin: 0, textTransform: "uppercase" }}>
            {new Date(event.date).toLocaleString("default", { month: "short" })}
          </p>
        </div>

        {/* Info */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", margin: "0 0 3px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
            {event.title}
          </p>
          <p style={{ fontSize: "13px", color: "var(--pk-t3)", margin: 0 }}>
            {formatDate(event.date)} at {formatTime(event.date)}
            {event.location ? ` · ${event.location}` : ""}
          </p>
          {event.description && (
            <p style={{ fontSize: "12px", color: "var(--pk-t3)", margin: "4px 0 0", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {event.description}
            </p>
          )}
        </div>

        {/* Attendees */}
        {event.attendees > 0 && (
          <div
            style={{
              display:      "flex",
              alignItems:   "center",
              gap:          "5px",
              background:   "var(--pk-b1)",
              borderRadius: "100px",
              padding:      "4px 10px",
              flexShrink:   0,
            }}
          >
            <span style={{ fontSize: "12px" }}>👥</span>
            <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t2)", margin: 0 }}>{event.attendees}</p>
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={{ padding: "32px 24px", maxWidth: "900px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ marginBottom: "28px" }}>
        <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
          Church Management
        </p>
        <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>Events</h1>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: isAdmin ? "1fr 340px" : "1fr", gap: "28px", alignItems: "start" }}>

        {/* Event list */}
        <div>

          {/* Upcoming */}
          <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
            Upcoming · {upcoming.length}
          </p>
          <div style={{ background: "#FFFFFF", borderRadius: "16px", overflow: "hidden", boxShadow: "var(--pk-shadow-sm)", marginBottom: "28px" }}>
            {upcoming.map((ev, i) => (
              <div key={ev.id} style={{ borderBottom: i < upcoming.length - 1 ? "1px solid var(--pk-b1)" : "none" }}>
                <EventRow event={ev} />
              </div>
            ))}
            {upcoming.length === 0 && (
              <p style={{ padding: "28px", textAlign: "center", color: "var(--pk-t3)", fontSize: "13px", margin: 0 }}>
                No upcoming events
              </p>
            )}
          </div>

          {/* Past */}
          {past.length > 0 && (
            <>
              <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "12px" }}>
                Past · {past.length}
              </p>
              <div style={{ background: "#FFFFFF", borderRadius: "16px", overflow: "hidden", boxShadow: "var(--pk-shadow-sm)" }}>
                {past.slice(0, 5).map((ev, i) => (
                  <div key={ev.id} style={{ borderBottom: i < Math.min(past.length, 5) - 1 ? "1px solid var(--pk-b1)" : "none" }}>
                    <EventRow event={ev} dim />
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Create event form (admin only) */}
        {isAdmin && <CreateEventForm churchId={viewer.churchId} />}
      </div>
    </div>
  );
}
