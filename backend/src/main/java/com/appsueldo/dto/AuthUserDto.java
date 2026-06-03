package com.appsueldo.dto;

import com.appsueldo.entity.User;
import java.util.ArrayList;
import java.util.List;

public record AuthUserDto(
    Long id,
    String name,
    String email,
    String pictureUrl,
    List<String> authProviders
) {
    public static AuthUserDto from(User user) {
        List<String> providers = new ArrayList<>();
        if (user.getGoogleId() != null && !user.getGoogleId().isBlank()) {
            providers.add("GOOGLE");
        }
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            providers.add("LOCAL");
        }
        return new AuthUserDto(user.getId(), user.getName(), user.getEmail(), user.getPictureUrl(), providers);
    }
}
