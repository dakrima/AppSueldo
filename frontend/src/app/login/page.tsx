import Link from "next/link";
import { ArrowLeft, ShieldCheck, WalletCards } from "lucide-react";
import { Button } from "@/components/ui/Button";

const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export default function LoginPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-warm-canvas px-4 py-10 text-primary">
      <section className="w-full max-w-md rounded-2xl border border-border-soft bg-soft-card p-7 shadow-[0_24px_60px_rgb(0_38_39_/_0.10)] sm:p-9">
        <Link href="/" className="inline-flex items-center gap-3 text-2xl font-semibold">
          <span className="flex size-10 items-center justify-center rounded-lg border-2 border-primary">
            <WalletCards size={22} />
          </span>
          AppSueldo
        </Link>
        <h1 className="mt-10 text-4xl font-semibold leading-tight tracking-normal">Entra a tu espacio financiero</h1>
        <p className="mt-4 text-lg leading-8 text-text-secondary">
          Usamos Google para crear o actualizar tu perfil de forma segura desde el backend.
        </p>
        <Button asChild size="lg" className="mt-8 w-full">
          <a href={`${apiUrl}/api/auth/google`}>
            <span className="flex size-6 items-center justify-center rounded bg-white text-sm font-bold text-[#4285f4]">
              G
            </span>
            Continuar con Google
          </a>
        </Button>
        <div className="mt-6 rounded-lg border border-green-300 bg-mint-bg p-4 text-secondary">
          <div className="flex gap-3">
            <ShieldCheck className="mt-1 shrink-0" size={20} />
            <p className="text-sm leading-6">
              En este MVP no conectamos bancos ni guardamos credenciales bancarias.
            </p>
          </div>
        </div>
        <Button asChild variant="ghost" className="mt-6">
          <Link href="/">
            <ArrowLeft size={18} />
            Volver al inicio
          </Link>
        </Button>
      </section>
    </main>
  );
}
