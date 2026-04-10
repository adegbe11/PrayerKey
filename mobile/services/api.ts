import * as SecureStore from "expo-secure-store";

// ── Base URL ──────────────────────────────────────────────────────────────────
// For local dev: use your machine's LAN IP (not localhost — device can't reach it)
// For prod: set EXPO_PUBLIC_API_URL in .env
const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? "http://localhost:3000";

const TOKEN_KEY = "pk_session_token";

// ── Token helpers ─────────────────────────────────────────────────────────────

export async function saveToken(token: string) {
  await SecureStore.setItemAsync(TOKEN_KEY, token);
}

export async function getToken(): Promise<string | null> {
  return SecureStore.getItemAsync(TOKEN_KEY);
}

export async function clearToken() {
  await SecureStore.deleteItemAsync(TOKEN_KEY);
}

// ── Core fetch wrapper ────────────────────────────────────────────────────────

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = await getToken();

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({})) as { error?: string };
    throw new Error(body.error ?? `HTTP ${res.status}`);
  }

  return res.json() as Promise<T>;
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export interface LoginResponse {
  token:     string;
  user: {
    id:       string;
    name:     string | null;
    email:    string;
    role:     string;
    churchId: string | null;
  };
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  return apiFetch<LoginResponse>("/api/mobile/auth/login", {
    method: "POST",
    body:   JSON.stringify({ email, password }),
  });
}

export async function register(data: {
  name: string; email: string; password: string; churchCode?: string;
}): Promise<LoginResponse> {
  return apiFetch<LoginResponse>("/api/mobile/auth/register", {
    method: "POST",
    body:   JSON.stringify(data),
  });
}

// ── Church & funds ────────────────────────────────────────────────────────────

export interface GivingFund {
  id: string; name: string; description: string | null; goal: number | null; raised: number;
}

export async function getGivingFunds(): Promise<GivingFund[]> {
  return apiFetch<GivingFund[]>("/api/give");
}

export async function submitDonation(fundId: string, amount: number, currency: string) {
  return apiFetch("/api/give", {
    method: "POST",
    body:   JSON.stringify({ fundId, amount, currency }),
  });
}

// ── Prayer ────────────────────────────────────────────────────────────────────

export interface GeneratedPrayer {
  id:           string;
  title:        string;
  prayer:       string;
  verses:       Array<{ ref: string; text: string; translation: string }>;
  encouragement: string;
}

export async function generatePrayer(userInput: string, moods: string[]): Promise<GeneratedPrayer> {
  return apiFetch<GeneratedPrayer>("/api/prayer/generate", {
    method: "POST",
    body:   JSON.stringify({ userInput, moods }),
  });
}

export async function bookmarkPrayer(id: string) {
  return apiFetch(`/api/prayer/${id}/bookmark`, { method: "POST" });
}

// ── Community ─────────────────────────────────────────────────────────────────

export interface Testimony {
  id: string; title: string; story: string; tags: string[];
  amenCount: number; anonymous: boolean; createdAt: string;
  user: { name: string | null };
}

export interface PrayerRequest {
  id: string; title: string; body: string; prayCount: number;
  anonymous: boolean; createdAt: string; answered: boolean;
  user: { name: string | null };
}

export async function getTestimonies(): Promise<Testimony[]> {
  return apiFetch<Testimony[]>("/api/mobile/community/testimonies");
}

export async function getPrayerRequests(): Promise<PrayerRequest[]> {
  return apiFetch<PrayerRequest[]>("/api/mobile/community/prayer-requests");
}

export async function amenTestimony(id: string) {
  return apiFetch(`/api/community/testimony/${id}/amen`, { method: "POST" });
}

export async function prayForRequest(id: string) {
  return apiFetch(`/api/community/prayer-request/${id}/pray`, { method: "POST" });
}

export async function submitTestimony(data: { title: string; story: string; tags: string[]; anonymous: boolean }) {
  return apiFetch("/api/community/testimony", { method: "POST", body: JSON.stringify(data) });
}

export async function submitPrayerRequest(data: { title: string; body: string; anonymous: boolean }) {
  return apiFetch("/api/community/prayer-request", { method: "POST", body: JSON.stringify(data) });
}

// ── Live service ──────────────────────────────────────────────────────────────

export async function startService(title?: string) {
  return apiFetch<{ serviceId: string }>("/api/service/start", {
    method: "POST",
    body:   JSON.stringify({ title }),
  });
}

export async function endService(serviceId: string) {
  return apiFetch(`/api/service/${serviceId}/end`, { method: "POST" });
}
