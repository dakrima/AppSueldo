import { apiFetch } from "@/lib/api/client";
import type { Category, CategoryType } from "@/types/finance";

export type CreateCategoryRequest = {
  name: string;
  type: CategoryType;
  color?: string | null;
  icon?: string | null;
};

export function listCategories() {
  return apiFetch<Category[]>("/api/categories");
}

export function createCategory(request: CreateCategoryRequest) {
  return apiFetch<Category>("/api/categories", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
