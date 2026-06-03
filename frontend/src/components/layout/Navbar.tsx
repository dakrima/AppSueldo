import { Bell, UserCircle } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function Navbar() {
  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="lg:hidden">
          <p className="text-base font-semibold text-slate-950">AppSueldo</p>
        </div>
        <div className="hidden text-sm text-slate-500 lg:block">MVP manual sin conexion bancaria</div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" aria-label="Notificaciones">
            <Bell size={18} />
          </Button>
          <div className="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
            <UserCircle className="text-slate-500" size={18} />
            <span className="hidden font-medium text-slate-700 sm:inline">Usuario demo</span>
          </div>
        </div>
      </div>
    </header>
  );
}
