import { ReactNode } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { MobileNav } from "@/components/layout/MobileNav";

type AppShellProps = {
  title: string;
  description?: string;
  action?: ReactNode;
  hideHeader?: boolean;
  children: ReactNode;
};

export function AppShell({ title, description, action, hideHeader = false, children }: AppShellProps) {
  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-warm-canvas text-primary">
        <Sidebar />
        <div className="lg:pl-72">
          <Navbar />
          <MobileNav />
          <main className="mx-auto grid w-full max-w-[1280px] gap-8 px-4 py-6 sm:px-6 lg:px-8 lg:py-10">
            {hideHeader ? null : (
              <header className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="grid gap-2">
                  <h1 className="text-4xl font-semibold leading-tight tracking-normal text-primary sm:text-5xl">
                    {title}
                  </h1>
                  {description ? <p className="max-w-2xl text-lg leading-7 text-text-secondary">{description}</p> : null}
                </div>
                {action ? <div className="shrink-0">{action}</div> : null}
              </header>
            )}
            {children}
          </main>
        </div>
      </div>
    </ProtectedRoute>
  );
}
