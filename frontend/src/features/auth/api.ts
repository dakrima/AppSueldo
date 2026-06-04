import { apiFetch } from "@/lib/api/client";

export type AuthUser = {
  id: number;
  name: string;
  email: string;
  pictureUrl: string | null;
  authProviders: string[];
};

export type AuthResponse = {
  user: AuthUser;
};

export function login(email: string, password: string) {
  return apiFetch<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function register(name: string, email: string, password: string) {
  return apiFetch<AuthResponse>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ name, email, password }),
  });
}

export function logout() {
  return apiFetch<void>("/api/auth/logout", { method: "POST" });
}

export function refreshSession() {
  return apiFetch<AuthResponse>("/api/auth/refresh", { method: "POST" });
}

export function getMe() {
  return apiFetch<AuthResponse>("/api/me");
}
