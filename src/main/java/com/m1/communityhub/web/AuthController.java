package com.m1.communityhub.web;

import com.m1.communityhub.domain.UserEntity;
import com.m1.communityhub.dto.AuthDtos;
import com.m1.communityhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.MeResponse signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        UserEntity user = authService.signup(request);
        return new AuthDtos.MeResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        String token = authService.login(request);
        return new AuthDtos.LoginResponse(token);
    }
}
