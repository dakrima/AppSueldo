import Link from "next/link";
import {
  ArrowRight,
  CalendarDays,
  CircleCheck,
  LockKeyhole,
  MoreHorizontal,
  NotebookPen,
  ShieldCheck,
  SlidersHorizontal,
  WalletCards,
} from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";

const benefits = [
  {
    title: "Simple y directo",
    description: "Entiende tu dinero al instante. Sin métricas confusas, solo lo que necesitas saber para el mes.",
    icon: SlidersHorizontal,
  },
  {
    title: "Privacidad total",
    description: "Tus datos son tuyos. No almacenamos credenciales bancarias ni nos conectamos a tus cuentas.",
    icon: LockKeyhole,
  },
  {
    title: "MVP manual",
    description: "Ingresa tus movimientos manualmente para partir con orden y conciencia financiera.",
    icon: NotebookPen,
  },
];

export default function Home() {
  return (
    <main className="min-h-screen bg-warm-canvas text-primary">
      <header className="border-b border-border-soft bg-warm-canvas">
        <div className="mx-auto flex h-20 max-w-[1280px] items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link href="/" className="flex items-center gap-3 text-2xl font-semibold">
            <span className="flex size-9 items-center justify-center rounded-lg border-2 border-primary">
              <WalletCards size={21} />
            </span>
            AppSueldo
          </Link>
          <Button asChild variant="secondary" size="md" className="hidden sm:inline-flex">
            <Link href="/login">
              <span className="font-bold text-[#4285f4]">G</span>
              Entrar con Google
            </Link>
          </Button>
        </div>
      </header>

      <div className="border-b border-[#ecd6a5] bg-amber-bg px-4 py-3 text-center text-sm font-bold text-[#b46d0f]">
        <span className="inline-flex items-center gap-2">
          <ShieldCheck size={16} />
          No guardamos credenciales bancarias. Registro manual para mayor seguridad.
        </span>
      </div>

      <section className="mx-auto grid max-w-[1280px] gap-12 px-4 py-16 sm:px-6 lg:grid-cols-[0.9fr_1fr] lg:items-center lg:px-8 lg:py-28">
        <div>
          <h1 className="max-w-2xl text-5xl font-semibold leading-tight tracking-normal sm:text-6xl">
            Tu sueldo, explicado sin vueltas.
          </h1>
          <p className="mt-6 max-w-xl text-xl leading-8 text-text-secondary">
            Ordena ingresos, gastos y movimientos para tomar mejores decisiones cada mes.
            Sin jerga financiera, sin complicaciones.
          </p>
          <div className="mt-10 flex flex-col gap-4 sm:flex-row">
            <Button asChild size="lg">
              <Link href="/login">
                Comenzar ahora <ArrowRight size={22} />
              </Link>
            </Button>
            <Button asChild variant="secondary" size="lg">
              <Link href="/dashboard">Ver ejemplo</Link>
            </Button>
          </div>
        </div>

        <div className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[0_24px_60px_rgb(0_38_39_/_0.10)] sm:p-8">
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-3 text-xl font-semibold text-text-secondary">
              <CalendarDays size={24} />
              Junio 2026
            </div>
            <MoreHorizontal className="text-text-muted" size={24} />
          </div>
          <div className="mt-10">
            <p className="text-lg text-text-secondary">Te queda para el mes</p>
            <p className="mt-2 text-5xl font-semibold tracking-normal">$317.700</p>
            <p className="mt-4 text-xs font-bold uppercase tracking-[0.16em] text-text-muted">
              Considerando gastos pendientes
            </p>
          </div>
          <div className="my-8 border-t border-border-soft" />
          <div className="grid gap-6 sm:grid-cols-2">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-text-muted">Ingresos</p>
              <p className="mt-2 text-2xl font-semibold text-secondary">+$850.000</p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-text-muted">Gastos</p>
              <p className="mt-2 text-2xl font-semibold text-muted-coral">-$412.300</p>
            </div>
          </div>
          <div className="mt-8 rounded-lg border border-green-300 bg-mint-bg p-4">
            <div className="flex gap-3">
              <CircleCheck className="mt-1 text-secondary" size={22} />
              <div>
                <p className="font-semibold text-secondary">Vas bien</p>
                <p className="mt-1 text-base text-secondary">Tus gastos fijos principales ya están cubiertos.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="border-y border-border-soft bg-white/55">
        <div className="mx-auto max-w-[1280px] px-4 py-20 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-3xl text-center">
            <h2 className="text-3xl font-semibold tracking-normal">Control financiero en tus propios términos</h2>
            <p className="mt-4 text-lg leading-7 text-text-secondary">
              Diseñado para darte paz mental sin la complejidad de las apps bancarias tradicionales.
            </p>
          </div>
          <div className="mt-14 grid gap-6 md:grid-cols-3">
            {benefits.map((benefit) => {
              const Icon = benefit.icon;
              return (
                <article key={benefit.title} className="rounded-xl border border-border-soft bg-soft-card p-8 shadow-[var(--shadow-paper)]">
                  <span className="flex size-16 items-center justify-center rounded-lg border border-border-soft bg-white">
                    <Icon size={28} />
                  </span>
                  <h3 className="mt-8 text-xl font-semibold">{benefit.title}</h3>
                  <p className="mt-4 text-lg leading-8 text-text-secondary">{benefit.description}</p>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      <footer className="bg-muted-surface">
        <div className="mx-auto flex max-w-[1280px] flex-col gap-6 px-4 py-8 text-sm font-semibold sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
          <span className="text-2xl font-semibold">AppSueldo</span>
          <nav className="flex gap-6 text-text-secondary">
            <Link href="/">Inicio</Link>
            <Link href="/transactions">Movimientos</Link>
            <Link href="/categories">Categorías</Link>
          </nav>
          <Badge>Sin conexión bancaria por ahora</Badge>
        </div>
      </footer>
    </main>
  );
}
