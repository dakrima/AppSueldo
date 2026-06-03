package com.appsueldo.dto;

import com.appsueldo.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank @Size(max = 120) String name,
    @NotNull CategoryType type,
    @Size(max = 32) String color,
    @Size(max = 80) String icon
) {
}
