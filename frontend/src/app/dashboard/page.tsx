import { DashboardWorkspace } from "@/components/dashboard/DashboardWorkspace";
import { AppShell } from "@/components/layout/AppShell";

export default function DashboardPage() {
  return (
    <AppShell title="Dashboard" hideHeader>
      <DashboardWorkspace />
    </AppShell>
  );
}
