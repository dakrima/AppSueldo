"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FolderKanban, Home, Plus, Settings, WalletCards } from "lucide-react";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { Button } from "@/components/ui/Button";

const navItems = [
  { href: "/dashboard", label: "Inicio", icon: Home },
  { href: "/transactions", label: "Movimientos", icon: WalletCards },
  { href: "/categories", label: "Categorías", icon: FolderKanban },
  { href: "/settings", label: "Ajustes", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();
  const creatingTransaction = pathname === "/transactions/new";

  return (
    <aside className="fixed inset-y-0 left-0 z-30 hidden w-72 border-r border-border-soft bg-muted-surface lg:block">
      <div className="flex h-full flex-col px-6 py-8">
        <Link href="/" className="grid gap-1 text-primary">
          <span className="text-4xl font-semibold leading-none tracking-normal">AppSueldo</span>
          <span className="text-base text-text-secondary">Control y alivio</span>
        </Link>

        <nav className="mt-12 grid gap-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = item.href === "/transactions" ? pathname.startsWith("/transactions") : pathname === item.href;

            return (
              <Link
                key={item.href}
                href={item.href}
                aria-current={active ? "page" : undefined}
                className={`flex h-12 items-center gap-4 rounded-lg px-4 text-base font-semibold transition ${
                  active
                    ? "border-r-4 border-primary bg-soft-card text-primary shadow-[var(--shadow-paper)]"
                    : "text-text-secondary hover:bg-soft-card/70 hover:text-primary"
                }`}
              >
                <Icon size={22} />
                <span className="min-w-0 flex-1">{item.label}</span>
                {active ? <span className="text-xs font-bold text-text-muted">Actual</span> : null}
              </Link>
            );
          })}
        </nav>

        {creatingTransaction ? (
          <Button size="lg" className="mt-auto w-full" disabled>
            <Plus size={22} />
            Creando movimiento
          </Button>
        ) : (
          <Button asChild size="lg" className="mt-auto w-full">
            <Link href="/transactions/new">
              <Plus size={22} />
              Agregar movimiento
            </Link>
          </Button>
        )}

        <div className="mt-8 border-t border-border-soft pt-6">
          <LogoutButton />
        </div>
      </div>
    </aside>
  );
}
