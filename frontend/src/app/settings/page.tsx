import { AppShell } from "@/components/layout/AppShell";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

export default function SettingsPage() {
  return (
    <AppShell title="Ajustes" description="Preferencias iniciales del perfil conectado con Google.">
      <section className="max-w-2xl rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4">
          <Input label="Nombre visible" placeholder="David Kripper" />
          <Input label="Email" placeholder="usuario@gmail.com" disabled />
          <Input label="Moneda principal" placeholder="CLP" />
          <Button className="w-fit">Guardar cambios</Button>
        </div>
      </section>
    </AppShell>
  );
}
