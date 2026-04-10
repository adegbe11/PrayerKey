import { PrismaClient } from "@prisma/client";
import { PrismaPg } from "@prisma/adapter-pg";
// eslint-disable-next-line @typescript-eslint/no-require-imports
const { Pool } = require("pg") as typeof import("pg");
import bcrypt from "bcryptjs";

const pool    = new Pool({ connectionString: process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma  = new PrismaClient({ adapter } as never);

async function main() {
  console.log("🌱 Seeding PrayerKey database...");

  // ── Church ──────────────────────────────────────────────────────
  const church = await prisma.church.upsert({
    where:  { slug: "grace-community-church" },
    update: {},
    create: {
      name:    "Grace Community Church",
      slug:    "grace-community-church",
      city:    "Lagos",
      country: "Nigeria",
      plan:    "PLUS",
    },
  });

  console.log("✅ Church created:", church.name);

  // ── Seed users ───────────────────────────────────────────────────
  const users = [
    { name: "Pastor David Okafor",  email: "pastor@test.com",    password: "Test1234", role: "PASTOR"       as const },
    { name: "Admin Grace Nwosu",    email: "admin@test.com",     password: "Test1234", role: "CHURCH_ADMIN" as const },
    { name: "Adaeze Okonkwo",       email: "adaeze@test.com",    password: "Test1234", role: "MEMBER"       as const },
    { name: "Emmanuel Fadele",      email: "emmanuel@test.com",  password: "Test1234", role: "VOLUNTEER"    as const },
    { name: "Tunde Alabi",          email: "tunde@test.com",     password: "Test1234", role: "MEMBER"       as const },
    { name: "Blessing Eze",         email: "blessing@test.com",  password: "Test1234", role: "MEMBER"       as const },
    { name: "Chisom Nkemdirim",     email: "chisom@test.com",    password: "Test1234", role: "MEMBER"       as const },
  ];

  for (const u of users) {
    const hashed = await bcrypt.hash(u.password, 12);
    await prisma.user.upsert({
      where:  { email: u.email },
      update: {},
      create: {
        name:     u.name,
        email:    u.email,
        password: hashed,
        role:     u.role,
        churchId: church.id,
      },
    });
    console.log(`✅ User: ${u.name} (${u.role})`);
  }

  // ── Giving funds ─────────────────────────────────────────────────
  const funds = [
    { name: "General Fund",     goal: 5000000 },
    { name: "Building Project", goal: 20000000 },
    { name: "Missions",         goal: 2000000 },
  ];

  for (const f of funds) {
    await prisma.givingFund.upsert({
      where:  { id: `fund-${f.name.replace(/\s+/g, "-").toLowerCase()}` },
      update: {},
      create: {
        id:       `fund-${f.name.replace(/\s+/g, "-").toLowerCase()}`,
        name:     f.name,
        goal:     f.goal,
        churchId: church.id,
      },
    });
    console.log(`✅ Fund: ${f.name}`);
  }

  // ── Sample sermons ────────────────────────────────────────────────
  const sermons = [
    { title: "Walking by Faith",    verseCount: 8  },
    { title: "The Power of Prayer", verseCount: 12 },
    { title: "Grace Abounding",     verseCount: 7  },
    { title: "Renewed Strength",    verseCount: 6  },
  ];

  for (const s of sermons) {
    await prisma.sermon.create({
      data: {
        title:      s.title,
        date:       new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000),
        verseCount: s.verseCount,
        churchId:   church.id,
        duration:   Math.floor(2400 + Math.random() * 1800),
      },
    });
    console.log(`✅ Sermon: ${s.title}`);
  }

  console.log("\n🎉 Seed complete! Login with pastor@test.com / Test1234");
}

main()
  .catch((e) => { console.error(e); process.exit(1); })
  .finally(() => prisma.$disconnect());
