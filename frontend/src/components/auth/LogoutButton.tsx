"use client";

import { LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/hooks/useAuth";

type LogoutButtonProps = {
  variant?: "button" | "link";
};

export function LogoutButton({ variant = "link" }: LogoutButtonProps) {
  const router = useRouter();
  const { logout } = useAuth();

  async function handleLogout() {
    await logout();
    router.replace("/login");
  }

  if (variant === "button") {
    return (
      <Button type="button" onClick={handleLogout} variant="secondary">
        <LogOut size={18} />
        Cerrar sesión
      </Button>
    );
  }

  return (
    <button
      type="button"
      onClick={handleLogout}
      className="flex h-11 items-center gap-3 rounded-lg px-2 text-base font-medium text-text-secondary transition hover:text-primary"
    >
      <LogOut size={21} />
      Cerrar sesión
    </button>
  );
}
