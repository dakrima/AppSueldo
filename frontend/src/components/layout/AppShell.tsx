"use client";

import { ReactNode } from "react";
import { Navbar } from "@/components/layout/Navbar";
import { Sidebar } from "@/components/layout/Sidebar";
import { MobileNav } from "@/components/layout/MobileNav";

type AppShellProps = {
  title: string;
  description: string;
  children: ReactNode;
};

export function AppShell({ title, description, children }: AppShellProps) {
  return (
    <div className="min-h-screen bg-slate-50">
      <Sidebar />
      <div className="lg:pl-72">
        <Navbar />
        <MobileNav />
        <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-5 sm:px-6 lg:px-8">
          <header className="flex flex-col gap-2">
            <h1 className="text-2xl font-semibold tracking-normal text-slate-950 sm:text-3xl">{title}</h1>
            <p className="max-w-2xl text-sm leading-6 text-slate-600">{description}</p>
          </header>
          {children}
        </main>
      </div>
    </div>
  );
}
