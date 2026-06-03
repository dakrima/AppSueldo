package com.appsueldo.controller;

import com.appsueldo.dto.UserProfileDto;
import com.appsueldo.service.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public UserProfileDto me() {
        return UserProfileDto.from(currentUserService.currentUser());
    }
}
