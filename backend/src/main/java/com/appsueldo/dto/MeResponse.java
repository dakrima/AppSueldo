package com.appsueldo.dto;

import com.appsueldo.entity.User;

public record MeResponse(AuthUserDto user) {
    public static MeResponse from(User user) {
        return new MeResponse(AuthUserDto.from(user));
    }
}
