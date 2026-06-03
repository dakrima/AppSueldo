import Link from "next/link";
import { WalletCards } from "lucide-react";
import { Button } from "@/components/ui/Button";

const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export default function LoginPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-5 py-10">
      <section className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <Link href="/" className="flex items-center gap-3 text-lg font-semibold text-slate-950">
          <span className="flex size-10 items-center justify-center rounded-lg bg-emerald-600 text-white">
            <WalletCards size={21} />
          </span>
          AppSueldo
        </Link>
        <h1 className="mt-8 text-3xl font-semibold tracking-normal text-slate-950">
          Ingresa a tu cuenta
        </h1>
        <p className="mt-3 leading-7 text-slate-600">
          Usa Google para crear o actualizar tu perfil en el backend. No se usa
          login con email y contrasena en este MVP.
        </p>
        <Button asChild className="mt-8 w-full justify-center">
          <a href={`${apiUrl}/api/auth/google`}>
            <span className="flex size-5 items-center justify-center rounded bg-white text-xs font-bold text-slate-900">
              G
            </span>
            Continuar con Google
          </a>
        </Button>
        <p className="mt-5 text-sm leading-6 text-slate-500">
          Al continuar, el backend verificara tu identidad con Google y creara
          tu usuario si todavia no existe.
        </p>
      </section>
    </main>
  );
}
