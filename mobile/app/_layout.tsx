import { useEffect, useState, useCallback } from "react";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { AuthContext, type AuthUser } from "@/hooks/useAuth";
import { getToken, saveToken, clearToken, type LoginResponse } from "@/services/api";

export default function RootLayout() {
  const [user,    setUser]    = useState<AuthUser | null>(null);
  const [token,   setToken]   = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Rehydrate session on boot
  useEffect(() => {
    (async () => {
      try {
        const stored = await getToken();
        if (stored) {
          // Decode payload (no verification needed client-side — server verifies)
          const parts = stored.split(".");
          if (parts.length === 3) {
            const payload = JSON.parse(atob(parts[1])) as {
              sub: string; email: string; role: string; churchId: string | null;
            };
            setUser({ id: payload.sub, email: payload.email, name: null, role: payload.role, churchId: payload.churchId });
            setToken(stored);
          }
        }
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const signIn = useCallback(async (res: LoginResponse) => {
    await saveToken(res.token);
    setToken(res.token);
    setUser(res.user);
  }, []);

  const signOut = useCallback(async () => {
    await clearToken();
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, signIn, signOut, loading }}>
      <StatusBar style="dark" />
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="(auth)"  options={{ headerShown: false }} />
        <Stack.Screen name="(tabs)"  options={{ headerShown: false }} />
      </Stack>
    </AuthContext.Provider>
  );
}
