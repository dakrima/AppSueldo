package com.appsueldo.dto;

import com.appsueldo.entity.User;

public record UserProfileDto(
    Long id,
    String googleId,
    String email,
    String name,
    String pictureUrl
) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
            user.getId(),
            user.getGoogleId(),
            user.getEmail(),
            user.getName(),
            user.getPictureUrl()
        );
    }
}
