import { PrismaClient } from "@prisma/client";
import { PrismaPg } from "@prisma/adapter-pg";
import pg from "pg";
import bcrypt from "bcryptjs";

const { Pool } = pg;
const pool    = new Pool({ connectionString: process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma  = new PrismaClient({ adapter });

async function main() {
  console.log("🌱 Seeding PrayerKey database...");

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
  console.log("✅ Church:", church.name);

  const users = [
    { name: "Pastor David Okafor", email: "pastor@test.com",   password: "Test1234", role: "PASTOR"       },
    { name: "Admin Grace Nwosu",   email: "admin@test.com",    password: "Test1234", role: "CHURCH_ADMIN" },
    { name: "Adaeze Okonkwo",      email: "adaeze@test.com",   password: "Test1234", role: "MEMBER"       },
    { name: "Emmanuel Fadele",     email: "emmanuel@test.com", password: "Test1234", role: "VOLUNTEER"    },
    { name: "Tunde Alabi",         email: "tunde@test.com",    password: "Test1234", role: "MEMBER"       },
    { name: "Blessing Eze",        email: "blessing@test.com", password: "Test1234", role: "MEMBER"       },
  ];

  for (const u of users) {
    const hashed = await bcrypt.hash(u.password, 12);
    await prisma.user.upsert({
      where:  { email: u.email },
      update: {},
      create: { name: u.name, email: u.email, password: hashed, role: u.role, churchId: church.id },
    });
    console.log(`✅ ${u.name} (${u.role})`);
  }

  const funds = [
    { name: "General Fund",     goal: 5000000  },
    { name: "Building Project", goal: 20000000 },
    { name: "Missions",         goal: 2000000  },
  ];

  for (const f of funds) {
    const id = `fund-${f.name.replace(/\s+/g, "-").toLowerCase()}`;
    await prisma.givingFund.upsert({
      where:  { id },
      update: {},
      create: { id, name: f.name, goal: f.goal, churchId: church.id },
    });
    console.log(`✅ Fund: ${f.name}`);
  }

  const sermons = [
    "Walking by Faith", "The Power of Prayer", "Grace Abounding", "Renewed Strength",
  ];
  for (const title of sermons) {
    await prisma.sermon.create({
      data: {
        title, churchId: church.id,
        date:       new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000),
        verseCount: Math.floor(6 + Math.random() * 8),
        duration:   Math.floor(2400 + Math.random() * 1800),
      },
    });
    console.log(`✅ Sermon: ${title}`);
  }

  console.log("\n🎉 Done! Login: pastor@test.com / Test1234");
}

main()
  .catch((e) => { console.error(e); process.exit(1); })
  .finally(() => prisma.$disconnect());
