"use client";

import { useRouter } from "next/navigation";
import { ReactNode, useEffect } from "react";
import { SessionLoading } from "@/components/auth/SessionLoading";
import { useAuth } from "@/hooks/useAuth";

export function PublicOnlyRoute({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { status } = useAuth();

  useEffect(() => {
    if (status === "authenticated") {
      router.replace("/dashboard");
    }
  }, [router, status]);

  if (status === "loading" || status === "authenticated") {
    return <SessionLoading />;
  }

  return <>{children}</>;
}
