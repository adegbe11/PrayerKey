"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Eye, EyeOff, Loader2 } from "lucide-react";

const loginSchema = z.object({
  email:    z.string().email("Enter a valid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});
type LoginForm = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError]   = useState("");
  const [googleLoading, setGoogleLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(data: LoginForm) {
    setServerError("");
    const res = await signIn("credentials", {
      email:    data.email,
      password: data.password,
      redirect: false,
    });
    if (res?.error) {
      setServerError("Incorrect email or password. Please try again.");
    } else {
      router.push("/");
      router.refresh();
    }
  }

  async function handleGoogle() {
    setGoogleLoading(true);
    await signIn("google", { callbackUrl: "/" });
  }

  return (
    <div style={{ width: "100%", maxWidth: "400px", padding: "0 20px" }}>

      {/* Logo */}
      <div className="flex items-center justify-center gap-2" style={{ marginBottom: "32px" }}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <rect x="10" y="2" width="4" height="20" rx="2" fill="var(--pk-gold)" />
          <rect x="2" y="9" width="20" height="4" rx="2" fill="var(--pk-gold)" />
        </svg>
        <span
          style={{
            fontSize: "20px",
            fontWeight: 700,
            color: "var(--pk-t1)",
            letterSpacing: "-0.003em",
            fontFamily: '"SF Pro Display", -apple-system, sans-serif',
          }}
        >
          Prayer<span style={{ color: "var(--pk-gold)" }}>Key</span>
        </span>
      </div>

      {/* Card */}
      <div
        className="rounded-2xl"
        style={{
          background: "#FFFFFF",
          boxShadow: "var(--pk-shadow-lg)",
          padding: "32px",
        }}
      >
        <h1
          style={{
            fontSize: "22px",
            fontWeight: 700,
            color: "var(--pk-t1)",
            letterSpacing: "-0.003em",
            marginBottom: "6px",
          }}
        >
          Sign in
        </h1>
        <p style={{ fontSize: "14px", color: "var(--pk-t2)", marginBottom: "24px" }}>
          Welcome back to your spiritual home.
        </p>

        {/* Google OAuth */}
        <button
          onClick={handleGoogle}
          disabled={googleLoading}
          className="w-full flex items-center justify-center gap-2.5 rounded-xl cursor-pointer"
          style={{
            padding: "11px",
            background: "var(--pk-deep)",
            border: "0.5px solid var(--pk-b2)",
            fontSize: "14px",
            fontWeight: 500,
            color: "var(--pk-t1)",
            marginBottom: "20px",
            transition: "background var(--pk-duration) var(--pk-ease)",
          }}
        >
          {googleLoading ? (
            <Loader2 size={16} className="animate-spin" style={{ color: "var(--pk-t3)" }} />
          ) : (
            <svg width="16" height="16" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
            </svg>
          )}
          Continue with Google
        </button>

        {/* Divider */}
        <div className="flex items-center gap-3" style={{ marginBottom: "20px" }}>
          <div className="flex-1 h-px" style={{ background: "var(--pk-b1)" }} />
          <span style={{ fontSize: "12px", color: "var(--pk-t3)" }}>or</span>
          <div className="flex-1 h-px" style={{ background: "var(--pk-b1)" }} />
        </div>

        {/* Server error */}
        {serverError && (
          <div
            className="rounded-xl px-4 py-3"
            style={{
              background: "rgba(255,59,48,0.06)",
              border: "0.5px solid rgba(255,59,48,0.2)",
              marginBottom: "16px",
            }}
          >
            <p style={{ fontSize: "13px", color: "var(--pk-live)", fontWeight: 500 }}>{serverError}</p>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)}>
          {/* Email */}
          <div style={{ marginBottom: "14px" }}>
            <label style={{ fontSize: "13px", fontWeight: 500, color: "var(--pk-t2)", display: "block", marginBottom: "6px" }}>
              Email
            </label>
            <input
              {...register("email")}
              type="email"
              placeholder="pastor@church.com"
              className="pk-input"
              style={{
                border: errors.email ? "1px solid var(--pk-live)" : undefined,
              }}
            />
            {errors.email && (
              <p style={{ fontSize: "12px", color: "var(--pk-live)", marginTop: "4px" }}>
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Password */}
          <div style={{ marginBottom: "8px" }}>
            <div className="flex items-center justify-between" style={{ marginBottom: "6px" }}>
              <label style={{ fontSize: "13px", fontWeight: 500, color: "var(--pk-t2)" }}>
                Password
              </label>
              <a
                href="/forgot-password"
                style={{ fontSize: "12px", color: "var(--pk-blue)", textDecoration: "none" }}
              >
                Forgot password?
              </a>
            </div>
            <div className="relative">
              <input
                {...register("password")}
                type={showPassword ? "text" : "password"}
                placeholder="Enter your password"
                className="pk-input"
                style={{
                  paddingRight: "44px",
                  border: errors.password ? "1px solid var(--pk-live)" : undefined,
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer"
                style={{ background: "none", border: "none", padding: 0 }}
              >
                {showPassword
                  ? <EyeOff size={16} style={{ color: "var(--pk-t3)" }} />
                  : <Eye size={16} style={{ color: "var(--pk-t3)" }} />
                }
              </button>
            </div>
            {errors.password && (
              <p style={{ fontSize: "12px", color: "var(--pk-live)", marginTop: "4px" }}>
                {errors.password.message}
              </p>
            )}
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full flex items-center justify-center gap-2 cursor-pointer"
            style={{
              marginTop: "22px",
              padding: "13px",
              background: isSubmitting ? "var(--pk-t3)" : "#1D1D1F",
              color: "#FFFFFF",
              border: "none",
              borderRadius: "980px",
              fontSize: "15px",
              fontWeight: 600,
              letterSpacing: "-0.003em",
              transition: "background var(--pk-duration) var(--pk-ease)",
            }}
          >
            {isSubmitting && <Loader2 size={15} className="animate-spin" />}
            Sign in
          </button>
        </form>

        {/* Register link */}
        <p
          style={{
            fontSize: "13px",
            color: "var(--pk-t2)",
            textAlign: "center",
            marginTop: "20px",
          }}
        >
          New to PrayerKey?{" "}
          <Link href="/register" style={{ color: "var(--pk-blue)", textDecoration: "none", fontWeight: 500 }}>
            Join as a church
          </Link>
        </p>
      </div>
    </div>
  );
}
