"use client";

import { usePathname } from "next/navigation";
import { Bell, CircleUserRound, WalletCards } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/hooks/useAuth";

function currentSection(pathname: string) {
  if (pathname.startsWith("/transactions")) {
    return "Movimientos";
  }
  if (pathname === "/categories") {
    return "Categorías";
  }
  if (pathname === "/settings") {
    return "Ajustes";
  }
  return "Inicio";
}

export function Navbar() {
  const { user } = useAuth();
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-20 border-b border-border-soft bg-warm-canvas/95 backdrop-blur">
      <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:h-20 lg:px-8">
        <div className="flex items-center gap-3 lg:hidden">
          <span className="flex size-9 items-center justify-center rounded-lg border border-border-soft bg-soft-card">
            <WalletCards size={19} />
          </span>
          <span className="text-base font-semibold text-primary">AppSueldo</span>
        </div>
        <div className="hidden text-sm font-medium text-text-secondary lg:block">
          <span className="mr-4 font-semibold text-primary">{currentSection(pathname)}</span>
          <span className="inline-flex items-center rounded-md bg-amber-bg px-2.5 py-1 text-xs font-bold text-[#7a4b00]">
            MVP manual
          </span>
          <span className="ml-3">Preparado para futura integración bancaria</span>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            aria-label="Notificaciones próximamente"
            title="Notificaciones próximamente"
            disabled
          >
            <Bell size={20} />
          </Button>
          <div className="flex items-center gap-2 rounded-lg border border-border-soft bg-soft-card px-3 py-2 text-sm shadow-[var(--shadow-paper)]">
            <CircleUserRound className="text-primary" size={19} />
            <span className="hidden font-semibold text-primary sm:inline">{user?.name ?? "Usuario"}</span>
          </div>
        </div>
      </div>
    </header>
  );
}
