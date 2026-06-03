"use client";

import { createContext, ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { ApiError } from "@/lib/api-client";
import * as authApi from "@/lib/auth-api";
import type { AuthUser } from "@/lib/auth-api";

type AuthStatus = "loading" | "authenticated" | "unauthenticated";

type AuthContextValue = {
  user: AuthUser | null;
  status: AuthStatus;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
  validateSession: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

async function resolveSessionUser() {
  try {
    const me = await authApi.getMe();
    return me.user;
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      try {
        await authApi.refreshSession();
        const me = await authApi.getMe();
        return me.user;
      } catch {
        return null;
      }
    }
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [status, setStatus] = useState<AuthStatus>("loading");

  const validateSession = useCallback(async () => {
    setStatus("loading");
    const nextUser = await resolveSessionUser();
    setUser(nextUser);
    setStatus(nextUser ? "authenticated" : "unauthenticated");
  }, []);

  useEffect(() => {
    let active = true;
    const timeoutId = window.setTimeout(() => {
      void resolveSessionUser().then((nextUser) => {
        if (!active) {
          return;
        }
        setUser(nextUser);
        setStatus(nextUser ? "authenticated" : "unauthenticated");
      });
    }, 0);

    return () => {
      active = false;
      window.clearTimeout(timeoutId);
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password);
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const register = useCallback(async (name: string, email: string, password: string) => {
    const response = await authApi.register(name, email, password);
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const refreshSession = useCallback(async () => {
    const response = await authApi.refreshSession();
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
    setStatus("unauthenticated");
  }, []);

  const value = useMemo(
    () => ({ user, status, login, register, logout, refreshSession, validateSession }),
    [user, status, login, register, logout, refreshSession, validateSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
