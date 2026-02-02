package com.m1.communityhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class UserDtos {
    private UserDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeResponse {
        private String id;
        private String username;
        private String email;
        private java.util.Set<String> roles;
        private java.util.Set<String> scopes;
    }
}
