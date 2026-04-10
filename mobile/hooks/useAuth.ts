import { createContext, useContext } from "react";
import type { LoginResponse } from "@/services/api";

export interface AuthUser {
  id:       string;
  name:     string | null;
  email:    string;
  role:     string;
  churchId: string | null;
}

export interface AuthContextValue {
  user:    AuthUser | null;
  token:   string | null;
  signIn:  (res: LoginResponse) => Promise<void>;
  signOut: () => Promise<void>;
  loading: boolean;
}

export const AuthContext = createContext<AuthContextValue>({
  user:    null,
  token:   null,
  signIn:  async () => {},
  signOut: async () => {},
  loading: true,
});

export function useAuth() {
  return useContext(AuthContext);
}
