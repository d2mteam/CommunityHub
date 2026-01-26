package com.m1.communityhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class AuthDtos {
    private AuthDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupRequest {
        @NotBlank
        @Size(max = 100)
        private String username;

        @NotBlank
        @Email
        @Size(max = 255)
        private String email;

        @NotBlank
        @Size(min = 6, max = 255)
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String usernameOrEmail;

        @NotBlank
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeResponse {
        private Long id;
        private String username;
        private String email;
    }
}
