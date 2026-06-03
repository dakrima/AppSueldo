package com.appsueldo.dto;

import com.appsueldo.entity.Category;
import com.appsueldo.entity.CategoryType;

public record CategoryResponse(
    Long id,
    String name,
    CategoryType type,
    String color,
    String icon
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getType(),
            category.getColor(),
            category.getIcon()
        );
    }
}
