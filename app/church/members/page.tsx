
import { prisma } from "@/lib/prisma";
import { redirect } from "next/navigation";
import MemberRoleSelect from "@/components/church/MemberRoleSelect";

const ROLE_COLORS: Record<string, string> = {
  PASTOR:       "#B07C1F",
  CHURCH_ADMIN: "#0071E3",
  MEMBER:       "#6E6E73",
  SUPER_ADMIN:  "#AF52DE",
};

export default async function MembersPage() {

  const viewer = await prisma.user.findUnique({
    where:  { id: "anonymous" },
    select: { churchId: true, role: true },
  });

  if (!viewer?.churchId) redirect("/");

  const isAdmin = ["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(viewer.role);

  const members = await prisma.user.findMany({
    where:   { churchId: viewer.churchId },
    orderBy: [{ role: "asc" }, { id: "asc" }],
    select:  {
      id:          true,
      name:        true,
      email:       true,
      role:        true,
      avatarUrl:   true,
      prayerStreak: true,
      _count: { select: { prayers: true } },
    },
  });

  return (
    <div style={{ padding: "32px 24px", maxWidth: "900px", margin: "0 auto" }}>

      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "28px" }}>
        <div>
          <p style={{ fontSize: "12px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "6px" }}>
            Church Management
          </p>
          <h1 style={{ fontSize: "28px", fontWeight: 700, color: "var(--pk-t1)", margin: 0 }}>
            Members
          </h1>
          <p style={{ fontSize: "14px", color: "var(--pk-t2)", margin: "4px 0 0" }}>
            {members.length} total member{members.length !== 1 ? "s" : ""}
          </p>
        </div>
      </div>

      {/* Member table */}
      <div
        style={{
          background:   "#FFFFFF",
          borderRadius: "16px",
          overflow:     "hidden",
          boxShadow:    "var(--pk-shadow-sm)",
        }}
      >
        {/* Table header */}
        <div
          style={{
            display:             "grid",
            gridTemplateColumns: "1fr 200px 120px 80px 100px",
            padding:             "12px 20px",
            borderBottom:        "1px solid var(--pk-b1)",
            background:          "#FAFAFA",
          }}
        >
          {["Member", "Email", "Role", "Prayers", "Streak"].map((h) => (
            <p key={h} style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", margin: 0 }}>
              {h}
            </p>
          ))}
        </div>

        {/* Rows */}
        {members.map((m, i) => (
          <div
            key={m.id}
            className="pk-hover-row"
            style={{
              display:             "grid",
              gridTemplateColumns: "1fr 200px 120px 80px 100px",
              padding:             "14px 20px",
              alignItems:          "center",
              borderBottom:        i < members.length - 1 ? "1px solid var(--pk-b1)" : "none",
            }}
          >
            {/* Avatar + name */}
            <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
              <div
                style={{
                  width:          "36px",
                  height:         "36px",
                  borderRadius:   "50%",
                  background:     "linear-gradient(135deg, var(--pk-gold), #AF52DE)",
                  display:        "flex",
                  alignItems:     "center",
                  justifyContent: "center",
                  fontSize:       "13px",
                  fontWeight:     700,
                  color:          "#FFFFFF",
                  flexShrink:     0,
                }}
              >
                {(m.name ?? m.email).slice(0, 1).toUpperCase()}
              </div>
              <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                {m.name ?? m.email.split("@")[0]}
              </p>
            </div>

            {/* Email */}
            <p style={{ fontSize: "13px", color: "var(--pk-t2)", margin: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {m.email}
            </p>

            {/* Role — editable if admin */}
            {isAdmin ? (
              <MemberRoleSelect
                memberId={m.id}
                currentRole={m.role}
                viewerRole={viewer.role}
              />
            ) : (
              <span
                style={{
                  fontSize:     "11px",
                  fontWeight:   600,
                  padding:      "3px 10px",
                  borderRadius: "100px",
                  background:   `${ROLE_COLORS[m.role] ?? "#6E6E73"}14`,
                  color:        ROLE_COLORS[m.role] ?? "#6E6E73",
                  display:      "inline-block",
                }}
              >
                {m.role}
              </span>
            )}

            {/* Prayers count */}
            <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>
              {m._count.prayers}
            </p>

            {/* Prayer streak */}
            <div style={{ display: "flex", alignItems: "center", gap: "4px" }}>
              <span style={{ fontSize: "14px" }}>🔥</span>
              <p style={{ fontSize: "14px", fontWeight: 600, color: m.prayerStreak > 0 ? "#FF9F0A" : "var(--pk-t3)", margin: 0 }}>
                {m.prayerStreak}
              </p>
            </div>
          </div>
        ))}

        {members.length === 0 && (
          <p style={{ padding: "32px", textAlign: "center", color: "var(--pk-t3)", fontSize: "14px" }}>
            No members found
          </p>
        )}
      </div>
    </div>
  );
}
