"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FolderKanban, Home, Settings, WalletCards } from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Inicio", icon: Home },
  { href: "/transactions", label: "Mov.", icon: WalletCards },
  { href: "/categories", label: "Cat.", icon: FolderKanban },
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
            className={`grid justify-items-center gap-1 rounded-lg px-2 py-2 text-xs font-semibold ${
              active ? "bg-soft-card text-primary shadow-[var(--shadow-paper)]" : "text-text-secondary"
            }`}
          >
            <Icon size={18} />
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}
