import { ReactNode } from "react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { MobileNav } from "@/components/layout/MobileNav";
import { PageHeader } from "@/components/layout/PageHeader";

type AppShellProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  action?: ReactNode;
  hideHeader?: boolean;
  headerVariant?: "standard" | "compact";
  children: ReactNode;
};

export function AppShell({
  eyebrow,
  title,
  description,
  action,
  hideHeader = false,
  headerVariant = "standard",
  children,
}: AppShellProps) {
  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-warm-canvas text-primary">
        <Sidebar />
        <div className="min-w-0 lg:pl-72">
          <Navbar />
          <MobileNav />
          <main className="mx-auto grid min-w-0 w-full max-w-[1280px] gap-8 px-4 py-6 sm:px-6 lg:px-8 lg:py-10">
            {hideHeader ? null : (
              <PageHeader
                eyebrow={eyebrow}
                title={title}
                description={description}
                action={action}
                variant={headerVariant}
              />
            )}
            {children}
          </main>
        </div>
      </div>
    </ProtectedRoute>
  );
}
