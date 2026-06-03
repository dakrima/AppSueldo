"use client";

import { useRouter } from "next/navigation";
import { ReactNode, useEffect } from "react";
import { SessionLoading } from "@/components/auth/SessionLoading";
import { useAuth } from "@/hooks/useAuth";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { status } = useAuth();

  useEffect(() => {
    if (status === "unauthenticated") {
      router.replace("/login");
    }
  }, [router, status]);

  if (status === "loading" || status === "unauthenticated") {
    return <SessionLoading />;
  }

  return <>{children}</>;
}
