package com.m1.communityhub.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserDtos {


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
