"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, FolderKanban, LayoutDashboard, Settings, WalletCards } from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/transactions", label: "Movimientos", icon: WalletCards },
  { href: "/categories", label: "Categorias", icon: FolderKanban },
  { href: "/settings", label: "Ajustes", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="fixed inset-y-0 left-0 z-30 hidden w-72 border-r border-slate-200 bg-white lg:block">
      <div className="flex h-full flex-col px-5 py-5">
        <Link href="/" className="flex items-center gap-3 text-lg font-semibold text-slate-950">
          <span className="flex size-10 items-center justify-center rounded-lg bg-emerald-600 text-white">
            <BarChart3 size={21} />
          </span>
          AppSueldo
        </Link>
        <nav className="mt-8 grid gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex h-11 items-center gap-3 rounded-lg px-3 text-sm font-medium transition ${
                  active ? "bg-emerald-50 text-emerald-700" : "text-slate-600 hover:bg-slate-50 hover:text-slate-950"
                }`}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="mt-auto rounded-lg border border-slate-200 bg-slate-50 p-4">
          <p className="text-sm font-medium text-slate-950">Autenticacion preparada</p>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            El frontend apunta al backend para Google OAuth y perfil de usuario.
          </p>
        </div>
      </div>
    </aside>
  );
}
