import Link from "next/link";
import { ArrowRight, CheckCircle2, WalletCards } from "lucide-react";
import { Button } from "@/components/ui/Button";

const highlights = [
  "Registro manual de movimientos para el MVP",
  "Categorias por usuario desde el backend",
  "Base lista para Google OAuth y tokens seguros",
];

export default function Home() {
  return (
    <main className="min-h-screen bg-white">
      <section className="mx-auto flex min-h-screen w-full max-w-6xl flex-col px-5 py-6 sm:px-8">
        <nav className="flex items-center justify-between">
          <Link href="/" className="flex items-center gap-3 text-lg font-semibold text-slate-950">
            <span className="flex size-10 items-center justify-center rounded-lg bg-emerald-600 text-white">
              <WalletCards size={21} />
            </span>
            AppSueldo
          </Link>
          <Button asChild variant="secondary" size="sm">
            <Link href="/login">Iniciar sesion</Link>
          </Button>
        </nav>

        <div className="grid flex-1 items-center gap-10 py-12 lg:grid-cols-[1fr_0.86fr]">
          <div className="max-w-2xl">
            <h1 className="text-balance text-4xl font-semibold leading-tight tracking-normal text-slate-950 sm:text-5xl">
              Transforma movimientos desordenados en decisiones claras.
            </h1>
            <p className="mt-5 max-w-xl text-lg leading-8 text-slate-600">
              AppSueldo parte como un MVP manual para ordenar ingresos, gastos,
              categorias y saldo disponible sin conectar credenciales bancarias todavia.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Button asChild>
                <Link href="/dashboard">
                  Ver dashboard <ArrowRight size={18} />
                </Link>
              </Button>
              <Button asChild variant="secondary">
                <Link href="/login">Continuar con Google</Link>
              </Button>
            </div>
            <ul className="mt-8 grid gap-3 text-sm text-slate-700">
              {highlights.map((item) => (
                <li key={item} className="flex items-center gap-3">
                  <CheckCircle2 className="text-emerald-600" size={18} />
                  {item}
                </li>
              ))}
            </ul>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm">
            <div className="rounded-lg border border-slate-200 bg-white p-5">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div>
                  <p className="text-sm text-slate-500">Saldo disponible</p>
                  <p className="mt-1 text-3xl font-semibold">$1.245.900</p>
                </div>
                <span className="rounded-md bg-emerald-50 px-3 py-1 text-sm font-medium text-emerald-700">
                  +12%
                </span>
              </div>
              <div className="mt-5 grid gap-3 sm:grid-cols-2">
                {[
                  ["Ingresos", "$2.200.000"],
                  ["Gastos", "$954.100"],
                  ["Ahorro", "$420.000"],
                  ["Categorias", "8 activas"],
                ].map(([label, value]) => (
                  <div key={label} className="rounded-lg border border-slate-100 bg-white p-4">
                    <p className="text-sm text-slate-500">{label}</p>
                    <p className="mt-2 text-xl font-semibold">{value}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
