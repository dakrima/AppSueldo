"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FolderKanban, Home, Settings, WalletCards } from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Inicio", icon: Home },
  { href: "/transactions", label: "Movimientos", icon: WalletCards },
  { href: "/categories", label: "Categorías", icon: FolderKanban },
  { href: "/settings", label: "Ajustes", icon: Settings },
];

export function MobileNav() {
  const pathname = usePathname();

  return (
    <nav className="sticky top-16 z-20 grid grid-cols-4 border-b border-border-soft bg-muted-surface px-2 py-2 lg:hidden">
      {navItems.map((item) => {
        const Icon = item.icon;
        const active = item.href === "/transactions" ? pathname.startsWith("/transactions") : pathname === item.href;
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? "page" : undefined}
            className={`grid min-w-0 justify-items-center gap-1 rounded-lg px-1 py-2 text-[11px] font-semibold sm:text-xs ${
              active
                ? "border border-border-strong bg-soft-card text-primary shadow-[var(--shadow-paper)]"
                : "border border-transparent text-text-secondary"
            }`}
          >
            <Icon size={18} />
            <span className="max-w-full truncate">{item.label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
