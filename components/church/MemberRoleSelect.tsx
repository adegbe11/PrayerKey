"use client";

import { useState } from "react";

const ROLES = ["MEMBER", "CHURCH_ADMIN", "PASTOR"] as const;
type Role = (typeof ROLES)[number];

const ROLE_COLORS: Record<string, string> = {
  PASTOR:       "#B07C1F",
  CHURCH_ADMIN: "#0071E3",
  MEMBER:       "#6E6E73",
};

interface Props {
  memberId:   string;
  currentRole: string;
  viewerRole:  string;
}

export default function MemberRoleSelect({ memberId, currentRole, viewerRole }: Props) {
  const [role, setRole]       = useState(currentRole);
  const [saving, setSaving]   = useState(false);

  // Only PASTOR / SUPER_ADMIN can assign PASTOR role
  const availableRoles = viewerRole === "PASTOR" || viewerRole === "SUPER_ADMIN"
    ? ROLES
    : ROLES.filter((r) => r !== "PASTOR");

  async function handleChange(newRole: Role) {
    setSaving(true);
    setRole(newRole);
    try {
      await fetch(`/api/church/members/${memberId}/role`, {
        method:  "PATCH",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ role: newRole }),
      });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div style={{ position: "relative", display: "inline-block" }}>
      <select
        value={role}
        onChange={(e) => handleChange(e.target.value as Role)}
        disabled={saving}
        style={{
          appearance:   "none",
          padding:      "4px 24px 4px 10px",
          borderRadius: "100px",
          border:       `1.5px solid ${ROLE_COLORS[role] ?? "#6E6E73"}40`,
          background:   `${ROLE_COLORS[role] ?? "#6E6E73"}10`,
          color:        ROLE_COLORS[role] ?? "#6E6E73",
          fontSize:     "11px",
          fontWeight:   600,
          cursor:       "pointer",
          outline:      "none",
          fontFamily:   "inherit",
          opacity:      saving ? 0.6 : 1,
          minWidth:     "90px",
        }}
      >
        {availableRoles.map((r) => (
          <option key={r} value={r}>{r}</option>
        ))}
      </select>
      {/* Chevron */}
      <span
        style={{
          position:       "absolute",
          right:          "8px",
          top:            "50%",
          transform:      "translateY(-50%)",
          fontSize:       "8px",
          color:          ROLE_COLORS[role] ?? "#6E6E73",
          pointerEvents:  "none",
        }}
      >
        ▾
      </span>
    </div>
  );
}
