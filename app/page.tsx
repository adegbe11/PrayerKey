import {
  Radio,
  Sparkles,
  BookOpen,
  TrendingUp,
  Heart,
  Clock,
  ChevronRight,
} from "lucide-react";

const QUICK_ACTIONS = [
  { icon: Radio,    label: "Start Live Sermon", href: "/live",           color: "var(--pk-live)",   bg: "rgba(255,59,48,0.08)"   },
  { icon: Sparkles, label: "Generate Prayer",   href: "/prayer",         color: "var(--pk-purple)", bg: "rgba(175,82,222,0.08)"  },
  { icon: BookOpen, label: "View Sermons",       href: "/church/sermons", color: "var(--pk-teal)",   bg: "rgba(0,199,190,0.08)"   },
  { icon: Heart,    label: "Church Giving",      href: "/church/giving",  color: "var(--pk-coral)",  bg: "rgba(255,105,97,0.08)"  },
];

const RECENT_SERMONS = [
  { title: "Walking by Faith",   verse: "Hebrews 11:1",    date: "Mar 16", verses: 8  },
  { title: "The Power of Prayer",verse: "Matthew 6:9-13",  date: "Mar 9",  verses: 12 },
  { title: "Grace Abounding",    verse: "Romans 5:20",     date: "Mar 2",  verses: 7  },
  { title: "Renewed Strength",   verse: "Isaiah 40:31",    date: "Feb 23", verses: 6  },
];

const SERMON_PALETTE = ["#B07C1F", "#AF52DE", "#00C7BE", "#FF6961"];
function sermonColor(title: string) { return SERMON_PALETTE[title.length % SERMON_PALETTE.length]; }

const STATS = [
  { label: "Members",           value: "1,247",  delta: "+12 this week",       deltaColor: "var(--pk-teal)"   },
  { label: "Monthly Giving",    value: "$24,810", delta: "+8.2% vs last month", deltaColor: "var(--pk-teal)"   },
  { label: "Attendance",        value: "87%",     delta: "vs 81% last week",    deltaColor: "var(--pk-gold)"   },
  { label: "Events This Month", value: "6",       delta: "2 upcoming",          deltaColor: "var(--pk-purple)" },
];

const MEMBERS = [
  { name: "Adaeze Okonkwo",  role: "Member",    joined: "Mar 18", initials: "AO", color: "#AF52DE" },
  { name: "Emmanuel Fadele", role: "Volunteer", joined: "Mar 15", initials: "EF", color: "#00C7BE" },
  { name: "Grace Nwosu",     role: "Member",    joined: "Mar 12", initials: "GN", color: "#B07C1F" },
  { name: "Tunde Alabi",     role: "Youth",     joined: "Mar 10", initials: "TA", color: "#FF6961" },
  { name: "Blessing Eze",    role: "Member",    joined: "Mar 8",  initials: "BE", color: "#AF52DE" },
];

const FUNDS = [
  { fund: "General Fund",    pct: 72, color: "var(--pk-gold)"   },
  { fund: "Building Project",pct: 45, color: "var(--pk-teal)"   },
  { fund: "Missions",        pct: 88, color: "var(--pk-purple)" },
];

export default function HomePage() {
  return (
    <div style={{ padding: "28px 28px 40px" }}>

      {/* ── Page header ── */}
      <div style={{ marginBottom: "28px" }}>
        <h1
          style={{
            fontSize: "28px",
            fontWeight: 600,
            color: "var(--pk-t1)",
            letterSpacing: "-0.003em",
            lineHeight: 1.2,
            fontFamily: '"SF Pro Display", -apple-system, sans-serif',
          }}
        >
          Good morning, Pastor David
        </h1>
        <p style={{ fontSize: "15px", color: "var(--pk-t2)", marginTop: "4px", letterSpacing: "-0.003em" }}>
          Sunday, March 21 · Grace Community Church
        </p>
      </div>

      {/* ── Stat row ── */}
      <div className="grid grid-cols-4 gap-3" style={{ marginBottom: "20px" }}>
        {STATS.map((s) => (
          <div
            key={s.label}
            className="pk-hover-card"
            style={{ padding: "18px 20px" }}
          >
            <p
              style={{
                fontSize: "11px",
                fontWeight: 600,
                letterSpacing: "0.06em",
                textTransform: "uppercase",
                color: "var(--pk-t3)",
                marginBottom: "6px",
              }}
            >
              {s.label}
            </p>
            <p
              style={{
                fontSize: "28px",
                fontWeight: 700,
                color: "var(--pk-gold)",
                letterSpacing: "-0.003em",
                lineHeight: 1,
                marginBottom: "4px",
                fontFamily: '"SF Pro Display", -apple-system, sans-serif',
              }}
            >
              {s.value}
            </p>
            <p style={{ fontSize: "12px", color: s.deltaColor, fontWeight: 500 }}>{s.delta}</p>
          </div>
        ))}
      </div>

      {/* ── AI Insight + Quick Actions ── */}
      <div className="grid grid-cols-3 gap-3" style={{ marginBottom: "20px" }}>

        {/* AI Insight — 2-col, left purple accent border */}
        <div
          className="col-span-2 rounded-2xl"
          style={{
            background: "#FFFFFF",
            boxShadow: "var(--pk-shadow-sm)",
            borderLeft: "3px solid var(--pk-purple)",
            padding: "20px 24px",
          }}
        >
          <div className="flex items-center gap-2" style={{ marginBottom: "10px" }}>
            <Sparkles size={13} style={{ color: "var(--pk-purple)" }} />
            <p
              style={{
                fontSize: "11px",
                fontWeight: 600,
                letterSpacing: "0.06em",
                textTransform: "uppercase",
                color: "var(--pk-purple)",
              }}
            >
              AI Weekly Insight
            </p>
          </div>
          <p style={{ fontSize: "14px", color: "var(--pk-t1)", lineHeight: 1.6, letterSpacing: "-0.003em" }}>
            Your congregation engagement is up{" "}
            <strong style={{ color: "var(--pk-t1)", fontWeight: 600 }}>14%</strong> this month. The sermon
            series on faith has resonated deeply — verse detection shows{" "}
            <strong style={{ color: "var(--pk-gold)", fontWeight: 600 }}>Hebrews 11</strong> referenced
            across 3 services. Consider a dedicated prayer night to sustain this momentum.
          </p>
          <div className="flex items-center gap-8" style={{ marginTop: "16px" }}>
            {[
              { label: "Sermons this month", val: "4" },
              { label: "Verses detected",    val: "33" },
              { label: "Prayers generated",  val: "218" },
            ].map((m) => (
              <div key={m.label}>
                <p
                  style={{
                    fontSize: "22px",
                    fontWeight: 700,
                    color: "var(--pk-t1)",
                    letterSpacing: "-0.003em",
                    lineHeight: 1,
                  }}
                >
                  {m.val}
                </p>
                <p style={{ fontSize: "11px", color: "var(--pk-t3)", marginTop: "2px" }}>{m.label}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Quick actions */}
        <div className="flex flex-col gap-2">
          <p
            style={{
              fontSize: "11px",
              fontWeight: 600,
              letterSpacing: "0.06em",
              textTransform: "uppercase",
              color: "var(--pk-t3)",
              marginBottom: "2px",
            }}
          >
            Quick Actions
          </p>
          {QUICK_ACTIONS.map((a) => {
            const Icon = a.icon;
            return (
              <a
                key={a.label}
                href={a.href}
                className="pk-hover-link flex items-center gap-3"
                style={{ padding: "10px 14px" }}
              >
                <div
                  className="rounded-xl flex items-center justify-center flex-shrink-0"
                  style={{ width: "30px", height: "30px", background: a.bg }}
                >
                  <Icon size={14} style={{ color: a.color }} strokeWidth={1.8} />
                </div>
                <span
                  style={{
                    fontSize: "13px",
                    color: "var(--pk-t1)",
                    fontWeight: 500,
                    letterSpacing: "-0.003em",
                  }}
                >
                  {a.label}
                </span>
                <ChevronRight size={13} style={{ color: "var(--pk-t3)", marginLeft: "auto" }} strokeWidth={1.5} />
              </a>
            );
          })}
        </div>
      </div>

      {/* ── Sermons + Members ── */}
      <div className="grid grid-cols-3 gap-3">

        {/* Recent Sermons — 2-col grid */}
        <div className="col-span-2">
          <div className="flex items-center justify-between" style={{ marginBottom: "12px" }}>
            <p style={{ fontSize: "17px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
              Recent Sermons
            </p>
            <a
              href="/church/sermons"
              style={{ fontSize: "14px", color: "var(--pk-blue)", textDecoration: "none", fontWeight: 400 }}
            >
              View all
            </a>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {RECENT_SERMONS.map((s) => {
              const c = sermonColor(s.title);
              return (
                <div key={s.title} className="pk-hover-card" style={{ padding: "14px" }}>
                  {/* Art square */}
                  <div
                    className="w-full rounded-xl flex items-center justify-center"
                    style={{
                      height: "60px",
                      marginBottom: "10px",
                      background: `linear-gradient(135deg, ${c}14, ${c}28)`,
                      border: `0.5px solid ${c}28`,
                    }}
                  >
                    <BookOpen size={20} style={{ color: c }} strokeWidth={1.4} />
                  </div>
                  <p
                    style={{
                      fontSize: "13px",
                      fontWeight: 600,
                      color: "var(--pk-t1)",
                      letterSpacing: "-0.003em",
                    }}
                  >
                    {s.title}
                  </p>
                  <p style={{ fontSize: "12px", color: "var(--pk-t3)", marginTop: "2px" }}>{s.verse}</p>
                  <div className="flex items-center justify-between" style={{ marginTop: "8px" }}>
                    <span style={{ fontSize: "11px", color: "var(--pk-t3)" }}>{s.date}</span>
                    <span
                      className="rounded-full px-2 py-0.5"
                      style={{
                        fontSize: "10px",
                        fontWeight: 600,
                        color: "var(--pk-teal)",
                        background: "rgba(0,199,190,0.08)",
                        letterSpacing: "0.01em",
                      }}
                    >
                      {s.verses} verses
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right: Members + Funds */}
        <div>
          {/* Members list */}
          <div className="flex items-center justify-between" style={{ marginBottom: "12px" }}>
            <p style={{ fontSize: "17px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
              New Members
            </p>
            <a
              href="/church/members"
              style={{ fontSize: "14px", color: "var(--pk-blue)", textDecoration: "none" }}
            >
              View all
            </a>
          </div>
          <div
            className="rounded-2xl overflow-hidden"
            style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)", marginBottom: "12px" }}
          >
            {MEMBERS.map((m, i) => (
              <div
                key={m.name}
                className="pk-hover-row flex items-center gap-3 px-4 py-2.5"
                style={{ borderTop: i > 0 ? "0.5px solid var(--pk-b1)" : "none" }}
              >
                <div
                  className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                  style={{
                    background: `${m.color}14`,
                    border: `0.5px solid ${m.color}28`,
                  }}
                >
                  <span style={{ fontSize: "11px", fontWeight: 700, color: m.color }}>{m.initials}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="truncate" style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
                    {m.name}
                  </p>
                  <p style={{ fontSize: "10px", color: "var(--pk-t3)" }}>{m.role}</p>
                </div>
                <div className="flex items-center gap-1 flex-shrink-0">
                  <Clock size={10} style={{ color: "var(--pk-t3)" }} strokeWidth={1.5} />
                  <span style={{ fontSize: "10px", color: "var(--pk-t3)" }}>{m.joined}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Giving funds */}
          <div
            className="rounded-2xl"
            style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)", padding: "16px 18px" }}
          >
            <div className="flex items-center gap-2" style={{ marginBottom: "14px" }}>
              <TrendingUp size={14} style={{ color: "var(--pk-gold)" }} strokeWidth={1.8} />
              <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
                Giving Funds
              </p>
            </div>
            {FUNDS.map((f) => (
              <div key={f.fund} style={{ marginBottom: "12px" }}>
                <div className="flex justify-between" style={{ marginBottom: "5px" }}>
                  <span style={{ fontSize: "12px", color: "var(--pk-t2)", fontWeight: 400 }}>{f.fund}</span>
                  <span style={{ fontSize: "12px", color: f.color, fontWeight: 600 }}>{f.pct}%</span>
                </div>
                <div
                  className="w-full rounded-full overflow-hidden"
                  style={{ height: "4px", background: "rgba(0,0,0,0.06)" }}
                >
                  <div
                    className="h-full rounded-full"
                    style={{ width: `${f.pct}%`, background: f.color }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
