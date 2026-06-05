"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { Bell, ChevronDown, LogOut, Settings, WalletCards } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";

export function Navbar() {
  const router = useRouter();
  const { user, logout } = useAuth();
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const notificationsRef = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);
  const userName = user?.name?.trim() || "Usuario";
  const userEmail = user?.email?.trim() || "";
  const initials = userName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("") || "US";

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      const target = event.target as Node;
      if (!notificationsRef.current?.contains(target)) {
        setNotificationsOpen(false);
      }
      if (!profileRef.current?.contains(target)) {
        setProfileOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setNotificationsOpen(false);
        setProfileOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, []);

  async function handleLogout() {
    setIsLoggingOut(true);
    setProfileOpen(false);
    await logout();
    router.replace("/login");
  }

  return (
    <header className="sticky top-0 z-20 border-b border-border-soft bg-warm-canvas">
      <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:h-20 lg:justify-end lg:px-8">
        <div className="flex items-center gap-3 lg:hidden">
          <span className="flex size-9 items-center justify-center rounded-lg border border-border-soft bg-soft-card">
            <WalletCards size={19} aria-hidden="true" />
          </span>
          <span className="text-base font-semibold text-primary">AppSueldo</span>
        </div>

        <div className="flex items-center gap-2 sm:gap-3">
          <div ref={notificationsRef} className="relative">
            <button
              type="button"
              aria-label="Abrir notificaciones"
              aria-expanded={notificationsOpen}
              aria-controls="notifications-popover"
              onClick={() => {
                setNotificationsOpen((open) => !open);
                setProfileOpen(false);
              }}
              className="flex size-11 items-center justify-center rounded-lg border border-border-soft bg-soft-card text-primary shadow-[var(--shadow-paper)] transition hover:border-border-strong focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <Bell size={19} aria-hidden="true" />
            </button>

            {notificationsOpen ? (
              <div
                id="notifications-popover"
                className="absolute right-0 top-14 z-40 w-[min(20rem,calc(100vw-2rem))] rounded-xl border border-border-soft bg-soft-card p-5 text-primary shadow-[0_18px_50px_rgb(0_38_39_/_0.12)]"
              >
                <p className="text-base font-semibold">Notificaciones</p>
                <p className="mt-3 text-sm font-medium text-primary">Aún no tienes notificaciones.</p>
                <p className="mt-2 text-sm leading-6 text-text-secondary">
                  Cuando conectes tus cuentas, aquí aparecerán avisos importantes.
                </p>
              </div>
            ) : null}
          </div>

          <div ref={profileRef} className="relative">
            <button
              type="button"
              aria-label="Abrir menú de perfil"
              aria-haspopup="menu"
              aria-expanded={profileOpen}
              aria-controls="profile-menu"
              onClick={() => {
                setProfileOpen((open) => !open);
                setNotificationsOpen(false);
              }}
              className="flex h-11 max-w-[13rem] items-center gap-2 rounded-lg border border-border-soft bg-soft-card px-2 text-left shadow-[var(--shadow-paper)] transition hover:border-border-strong focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary sm:max-w-xs sm:px-3"
            >
              {user?.pictureUrl ? (
                <span
                  aria-label={`Foto de ${userName}`}
                  role="img"
                  className="size-8 shrink-0 rounded-lg bg-muted-surface bg-cover bg-center"
                  style={{ backgroundImage: `url("${user.pictureUrl}")` }}
                />
              ) : (
                <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary text-xs font-bold text-white">
                  {initials}
                </span>
              )}
              <span className="hidden min-w-0 max-w-40 truncate text-sm font-semibold text-primary sm:block">{userName}</span>
              <ChevronDown className="shrink-0 text-text-muted" size={17} aria-hidden="true" />
            </button>

            {profileOpen ? (
              <div
                id="profile-menu"
                role="menu"
                className="absolute right-0 top-14 z-40 w-[min(18rem,calc(100vw-2rem))] overflow-hidden rounded-xl border border-border-soft bg-soft-card text-primary shadow-[0_18px_50px_rgb(0_38_39_/_0.12)]"
              >
                <div className="border-b border-border-soft px-5 py-4">
                  <p className="truncate text-base font-semibold">{userName}</p>
                  {userEmail ? <p className="mt-1 truncate text-sm text-text-secondary">{userEmail}</p> : null}
                </div>
                <div className="grid p-2">
                  <Link
                    href="/settings"
                    role="menuitem"
                    onClick={() => setProfileOpen(false)}
                    className="flex h-11 items-center gap-3 rounded-lg px-3 text-sm font-semibold text-text-secondary transition hover:bg-muted-surface/70 hover:text-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
                  >
                    <Settings size={17} aria-hidden="true" />
                    Ajustes
                  </Link>
                  <button
                    type="button"
                    role="menuitem"
                    onClick={handleLogout}
                    disabled={isLoggingOut}
                    className="flex h-11 items-center gap-3 rounded-lg px-3 text-left text-sm font-semibold text-danger transition hover:bg-soft-coral-bg focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-danger disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    <LogOut size={17} aria-hidden="true" />
                    {isLoggingOut ? "Cerrando sesión..." : "Cerrar sesión"}
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </header>
  );
}
