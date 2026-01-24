package com.m1.communityhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record SignupRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 255) String password
    ) {
    }

    public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
    ) {
    }

    public record LoginResponse(String accessToken) {
    }

    public record MeResponse(Long id, String username, String email) {
    }
}
