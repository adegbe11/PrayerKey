"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { Church, User, ChevronRight, Loader2, Check } from "lucide-react";

// ── Schemas ──────────────────────────────────────────────────────────
const individualSchema = z.object({
  name:            z.string().min(2, "Name is required"),
  email:           z.string().email("Enter a valid email"),
  password:        z.string().min(8, "At least 8 characters"),
  confirmPassword: z.string(),
}).refine((d) => d.password === d.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

const churchSchema = z.object({
  churchName:  z.string().min(2, "Church name required"),
  city:        z.string().min(2, "City required"),
  country:     z.string().min(2, "Country required"),
  size:        z.string().min(1, "Select congregation size"),
  adminName:   z.string().min(2, "Your name is required"),
  email:       z.string().email("Enter a valid email"),
  password:    z.string().min(8, "At least 8 characters"),
});

type IndividualForm = z.infer<typeof individualSchema>;
type ChurchForm     = z.infer<typeof churchSchema>;

type AccountType = "individual" | "church" | null;

const SIZES = ["1–50", "51–200", "201–500", "501–1,000", "1,000+"];

// ── Slide animation ───────────────────────────────────────────────────
const slideVariants = {
  enter:  { x: 40, opacity: 0 },
  center: { x: 0,  opacity: 1 },
  exit:   { x: -40, opacity: 0 },
};

// ── Input helper ─────────────────────────────────────────────────────
function Field({
  label, error, children,
}: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: "12px" }}>
      <label style={{ fontSize: "13px", fontWeight: 500, color: "var(--pk-t2)", display: "block", marginBottom: "5px" }}>
        {label}
      </label>
      {children}
      {error && <p style={{ fontSize: "12px", color: "var(--pk-live)", marginTop: "3px" }}>{error}</p>}
    </div>
  );
}

// ── Main component ───────────────────────────────────────────────────
export default function RegisterPage() {
  const router = useRouter();
  const [step, setStep]         = useState<1 | 2>(1);
  const [type, setType]         = useState<AccountType>(null);
  const [serverError, setServerError] = useState("");

  const individualForm = useForm<IndividualForm>({ resolver: zodResolver(individualSchema) });
  const churchForm     = useForm<ChurchForm>({ resolver: zodResolver(churchSchema) });

  // ── Submit individual ──────────────────────────────────────────────
  async function submitIndividual(data: IndividualForm) {
    setServerError("");
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type: "individual", ...data }),
    });
    if (!res.ok) {
      const j = await res.json().catch(() => ({}));
      setServerError((j as { error?: string }).error ?? "Something went wrong.");
      return;
    }
    router.push("/login?registered=1");
  }

  // ── Submit church ──────────────────────────────────────────────────
  async function submitChurch(data: ChurchForm) {
    setServerError("");
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type: "church", ...data }),
    });
    if (!res.ok) {
      const j = await res.json().catch(() => ({}));
      setServerError((j as { error?: string }).error ?? "Something went wrong.");
      return;
    }
    router.push("/login?registered=1");
  }

  return (
    <div style={{ width: "100%", maxWidth: "440px", padding: "0 20px" }}>

      {/* Logo */}
      <div className="flex items-center justify-center gap-2" style={{ marginBottom: "28px" }}>
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
          <rect x="10" y="2" width="4" height="20" rx="2" fill="var(--pk-gold)" />
          <rect x="2" y="9" width="20" height="4" rx="2" fill="var(--pk-gold)" />
        </svg>
        <span style={{ fontSize: "19px", fontWeight: 700, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
          Prayer<span style={{ color: "var(--pk-gold)" }}>Key</span>
        </span>
      </div>

      {/* Step indicator */}
      <div className="flex items-center justify-center gap-2" style={{ marginBottom: "24px" }}>
        {[1, 2].map((s) => (
          <div key={s} className="flex items-center gap-2">
            <div
              className="w-6 h-6 rounded-full flex items-center justify-center"
              style={{
                background: step >= s ? "#1D1D1F" : "var(--pk-b1)",
                transition: "background var(--pk-duration) var(--pk-ease)",
              }}
            >
              {step > s
                ? <Check size={11} strokeWidth={2.5} style={{ color: "#FFFFFF" }} />
                : <span style={{ fontSize: "11px", fontWeight: 700, color: step >= s ? "#FFFFFF" : "var(--pk-t3)" }}>{s}</span>
              }
            </div>
            {s < 2 && <div className="w-8 h-px" style={{ background: step > s ? "#1D1D1F" : "var(--pk-b1)" }} />}
          </div>
        ))}
      </div>

      {/* Card */}
      <div className="rounded-2xl overflow-hidden" style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-lg)" }}>
        <AnimatePresence mode="wait">

          {/* ── STEP 1: Choose type ──────────────────────────────── */}
          {step === 1 && (
            <motion.div
              key="step1"
              variants={slideVariants}
              initial="enter" animate="center" exit="exit"
              transition={{ duration: 0.24, ease: [0.4, 0, 0.6, 1] }}
              style={{ padding: "32px" }}
            >
              <h1 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-t1)", letterSpacing: "-0.003em", marginBottom: "6px" }}>
                Join PrayerKey
              </h1>
              <p style={{ fontSize: "14px", color: "var(--pk-t2)", marginBottom: "24px" }}>
                How would you like to use PrayerKey?
              </p>

              <div className="flex flex-col gap-3">
                {[
                  {
                    value: "individual" as AccountType,
                    icon: User,
                    title: "Individual believer",
                    desc: "Daily prayer AI, devotionals, and your spiritual journal.",
                  },
                  {
                    value: "church" as AccountType,
                    icon: Church,
                    title: "Church account",
                    desc: "Live sermon AI, member management, giving, and more.",
                  },
                ].map((opt) => {
                  const Icon = opt.icon;
                  const selected = type === opt.value;
                  return (
                    <button
                      key={opt.value}
                      onClick={() => setType(opt.value)}
                      className="flex items-start gap-4 rounded-xl text-left cursor-pointer"
                      style={{
                        padding: "16px",
                        background: selected ? "rgba(176,124,31,0.06)" : "var(--pk-deep)",
                        border: selected ? "1.5px solid var(--pk-gold-border)" : "1px solid var(--pk-b1)",
                        transition: "all var(--pk-duration) var(--pk-ease)",
                      }}
                    >
                      <div
                        className="rounded-xl flex items-center justify-center flex-shrink-0"
                        style={{
                          width: "40px", height: "40px",
                          background: selected ? "var(--pk-gold-dim)" : "var(--pk-b1)",
                        }}
                      >
                        <Icon size={18} style={{ color: selected ? "var(--pk-gold)" : "var(--pk-t3)" }} strokeWidth={1.6} />
                      </div>
                      <div>
                        <p style={{ fontSize: "14px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
                          {opt.title}
                        </p>
                        <p style={{ fontSize: "12px", color: "var(--pk-t2)", marginTop: "2px" }}>
                          {opt.desc}
                        </p>
                      </div>
                      {selected && (
                        <Check size={16} style={{ color: "var(--pk-gold)", marginLeft: "auto", flexShrink: 0 }} />
                      )}
                    </button>
                  );
                })}
              </div>

              <button
                onClick={() => type && setStep(2)}
                disabled={!type}
                className="w-full flex items-center justify-center gap-2 cursor-pointer"
                style={{
                  marginTop: "22px",
                  padding: "13px",
                  background: type ? "#1D1D1F" : "var(--pk-b1)",
                  color: type ? "#FFFFFF" : "var(--pk-t3)",
                  border: "none",
                  borderRadius: "980px",
                  fontSize: "15px",
                  fontWeight: 600,
                  letterSpacing: "-0.003em",
                  transition: "background var(--pk-duration) var(--pk-ease)",
                  cursor: type ? "pointer" : "not-allowed",
                }}
              >
                Continue <ChevronRight size={15} />
              </button>

              <p style={{ fontSize: "13px", color: "var(--pk-t2)", textAlign: "center", marginTop: "18px" }}>
                Already have an account?{" "}
                <Link href="/login" style={{ color: "var(--pk-blue)", textDecoration: "none", fontWeight: 500 }}>
                  Sign in
                </Link>
              </p>
            </motion.div>
          )}

          {/* ── STEP 2a: Individual form ─────────────────────────── */}
          {step === 2 && type === "individual" && (
            <motion.div
              key="step2-individual"
              variants={slideVariants}
              initial="enter" animate="center" exit="exit"
              transition={{ duration: 0.24, ease: [0.4, 0, 0.6, 1] }}
              style={{ padding: "32px" }}
            >
              <button
                onClick={() => setStep(1)}
                style={{ background: "none", border: "none", cursor: "pointer", padding: 0, marginBottom: "16px" }}
              >
                <span style={{ fontSize: "13px", color: "var(--pk-blue)" }}>← Back</span>
              </button>
              <h1 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-t1)", letterSpacing: "-0.003em", marginBottom: "6px" }}>
                Create your account
              </h1>
              <p style={{ fontSize: "13px", color: "var(--pk-t2)", marginBottom: "22px" }}>Individual believer</p>

              {serverError && (
                <div className="rounded-xl px-4 py-3" style={{ background: "rgba(255,59,48,0.06)", border: "0.5px solid rgba(255,59,48,0.2)", marginBottom: "14px" }}>
                  <p style={{ fontSize: "13px", color: "var(--pk-live)", fontWeight: 500 }}>{serverError}</p>
                </div>
              )}

              <form onSubmit={individualForm.handleSubmit(submitIndividual)}>
                <Field label="Full name" error={individualForm.formState.errors.name?.message}>
                  <input {...individualForm.register("name")} placeholder="Your full name" className="pk-input" />
                </Field>
                <Field label="Email" error={individualForm.formState.errors.email?.message}>
                  <input {...individualForm.register("email")} type="email" placeholder="you@email.com" className="pk-input" />
                </Field>
                <Field label="Password" error={individualForm.formState.errors.password?.message}>
                  <input {...individualForm.register("password")} type="password" placeholder="Min. 8 characters" className="pk-input" />
                </Field>
                <Field label="Confirm password" error={individualForm.formState.errors.confirmPassword?.message}>
                  <input {...individualForm.register("confirmPassword")} type="password" placeholder="Repeat password" className="pk-input" />
                </Field>
                <button
                  type="submit"
                  disabled={individualForm.formState.isSubmitting}
                  className="w-full flex items-center justify-center gap-2 cursor-pointer"
                  style={{
                    marginTop: "20px", padding: "13px",
                    background: "#1D1D1F", color: "#FFFFFF",
                    border: "none", borderRadius: "980px",
                    fontSize: "15px", fontWeight: 600, letterSpacing: "-0.003em",
                  }}
                >
                  {individualForm.formState.isSubmitting && <Loader2 size={15} className="animate-spin" />}
                  Create account
                </button>
              </form>
            </motion.div>
          )}

          {/* ── STEP 2b: Church form ─────────────────────────────── */}
          {step === 2 && type === "church" && (
            <motion.div
              key="step2-church"
              variants={slideVariants}
              initial="enter" animate="center" exit="exit"
              transition={{ duration: 0.24, ease: [0.4, 0, 0.6, 1] }}
              style={{ padding: "32px" }}
            >
              <button
                onClick={() => setStep(1)}
                style={{ background: "none", border: "none", cursor: "pointer", padding: 0, marginBottom: "16px" }}
              >
                <span style={{ fontSize: "13px", color: "var(--pk-blue)" }}>← Back</span>
              </button>
              <h1 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-t1)", letterSpacing: "-0.003em", marginBottom: "6px" }}>
                Set up your church
              </h1>
              <p style={{ fontSize: "13px", color: "var(--pk-t2)", marginBottom: "22px" }}>Church account · Free to start</p>

              {serverError && (
                <div className="rounded-xl px-4 py-3" style={{ background: "rgba(255,59,48,0.06)", border: "0.5px solid rgba(255,59,48,0.2)", marginBottom: "14px" }}>
                  <p style={{ fontSize: "13px", color: "var(--pk-live)", fontWeight: 500 }}>{serverError}</p>
                </div>
              )}

              <form onSubmit={churchForm.handleSubmit(submitChurch)}>
                <Field label="Church name" error={churchForm.formState.errors.churchName?.message}>
                  <input {...churchForm.register("churchName")} placeholder="Grace Community Church" className="pk-input" />
                </Field>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="City" error={churchForm.formState.errors.city?.message}>
                    <input {...churchForm.register("city")} placeholder="Lagos" className="pk-input" />
                  </Field>
                  <Field label="Country" error={churchForm.formState.errors.country?.message}>
                    <input {...churchForm.register("country")} placeholder="Nigeria" className="pk-input" />
                  </Field>
                </div>
                <Field label="Congregation size" error={churchForm.formState.errors.size?.message}>
                  <select {...churchForm.register("size")} className="pk-input" style={{ cursor: "pointer" }}>
                    <option value="">Select size</option>
                    {SIZES.map((s) => <option key={s} value={s}>{s} members</option>)}
                  </select>
                </Field>
                <div style={{ borderTop: "0.5px solid var(--pk-b1)", margin: "16px 0" }} />
                <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.04em", textTransform: "uppercase", marginBottom: "12px" }}>
                  Your admin account
                </p>
                <Field label="Your name" error={churchForm.formState.errors.adminName?.message}>
                  <input {...churchForm.register("adminName")} placeholder="Pastor John" className="pk-input" />
                </Field>
                <Field label="Email" error={churchForm.formState.errors.email?.message}>
                  <input {...churchForm.register("email")} type="email" placeholder="pastor@church.com" className="pk-input" />
                </Field>
                <Field label="Password" error={churchForm.formState.errors.password?.message}>
                  <input {...churchForm.register("password")} type="password" placeholder="Min. 8 characters" className="pk-input" />
                </Field>
                <button
                  type="submit"
                  disabled={churchForm.formState.isSubmitting}
                  className="w-full flex items-center justify-center gap-2 cursor-pointer"
                  style={{
                    marginTop: "20px", padding: "13px",
                    background: "#1D1D1F", color: "#FFFFFF",
                    border: "none", borderRadius: "980px",
                    fontSize: "15px", fontWeight: 600, letterSpacing: "-0.003em",
                  }}
                >
                  {churchForm.formState.isSubmitting && <Loader2 size={15} className="animate-spin" />}
                  Create church account
                </button>
              </form>
            </motion.div>
          )}

        </AnimatePresence>
      </div>
    </div>
  );
}
