package com.m1.communityhub.config.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final Set<String> requiredAudiences;

    public AudienceValidator(Set<String> requiredAudiences) {
        this.requiredAudiences = requiredAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (requiredAudiences == null || requiredAudiences.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }
        List<String> audiences = jwt.getAudience();
        boolean matches = audiences.stream().anyMatch(requiredAudiences::contains);
        if (matches) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error("invalid_token", "JWT audience is invalid", null);
        return OAuth2TokenValidatorResult.failure(error);
    }

    public static Set<String> parseAudiences(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .collect(Collectors.toSet());
    }
}
