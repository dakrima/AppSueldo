"use client";

import Link from "next/link";
import { ArrowLeft, ShieldCheck, WalletCards } from "lucide-react";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { PublicOnlyRoute } from "@/components/auth/PublicOnlyRoute";
import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/ErrorMessage";
import { FormActions } from "@/components/ui/FormActions";
import { Input } from "@/components/ui/Input";
import { useAuth } from "@/hooks/useAuth";
import { API_URL } from "@/lib/api/client";

function userFacingError(error: unknown, fallback: string) {
  if (!(error instanceof Error)) {
    return fallback;
  }

  if (/api|backend|server|servidor|token|jwt|stack|fetch/i.test(error.message)) {
    return fallback;
  }

  return error.message;
}

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);
    try {
      await login(email, password);
      router.replace("/dashboard");
    } catch (error) {
      setError(userFacingError(error, "No pudimos iniciar sesión. Revisa tus datos e inténtalo nuevamente."));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <PublicOnlyRoute>
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
            Continúa con Google o usa tu cuenta propia de AppSueldo.
          </p>

          <Button asChild size="lg" className="mt-8 w-full">
            <a href={`${API_URL}/api/auth/google`}>
              <span className="flex size-6 items-center justify-center rounded bg-white text-sm font-bold text-[#4285f4]">
                G
              </span>
              Continuar con Google
            </a>
          </Button>

          <div className="my-7 flex items-center gap-3 text-sm text-text-muted">
            <span className="h-px flex-1 bg-border-soft" />
            o entra con email
            <span className="h-px flex-1 bg-border-soft" />
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4">
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="david@example.com"
              required
            />
            <Input
              label="Contraseña"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Tu contraseña"
              required
            />
            {error ? <ErrorMessage title="No pudimos iniciar sesión" message={error} /> : null}
            <FormActions
              primary={
                <Button type="submit" size="lg" disabled={isSubmitting} className="w-full">
                  {isSubmitting ? "Validando datos..." : "Iniciar sesión"}
                </Button>
              }
            />
          </form>

          <p className="mt-5 text-center text-sm text-text-secondary">
            ¿No tienes cuenta?{" "}
            <Link href="/register" className="font-semibold text-primary underline underline-offset-4">
              Crear cuenta
            </Link>
          </p>

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
    </PublicOnlyRoute>
  );
}
