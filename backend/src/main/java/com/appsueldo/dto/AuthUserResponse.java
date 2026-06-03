package com.appsueldo.dto;

import com.appsueldo.entity.User;

public record AuthUserResponse(AuthUserDto user) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(AuthUserDto.from(user));
    }
}
