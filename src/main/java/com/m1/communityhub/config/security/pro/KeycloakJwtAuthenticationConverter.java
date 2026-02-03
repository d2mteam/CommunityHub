package com.m1.communityhub.config.security.pro;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String USERNAME_CLAIM = "preferred_username";
    private static final String EMAIL_CLAIM = "email";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scope";
    private static final String SCP_CLAIM = "scp";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userId = extractUserId(jwt);
        String username = jwt.getClaimAsString(USERNAME_CLAIM);
        if (username == null || username.isBlank()) {
            username = jwt.getSubject();
        }
        String email = jwt.getClaimAsString(EMAIL_CLAIM);
        Set<String> roles = extractRoles(jwt);
        Set<String> scopes = extractScopes(jwt);
        UserContext user = new UserContext(userId, username, email, roles, scopes);
        return new UsernamePasswordAuthenticationToken(user, jwt, user.getAuthorities());
    }

    private String extractUserId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtException("Missing required claim: sub");
        }
        return subject;
    }

    private Set<String> extractRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object roles = realmAccessMap.get(ROLES_CLAIM);
            if (roles instanceof Collection<?> collection) {
                Set<String> roleValues = new HashSet<>();
                for (Object role : collection) {
                    if (role != null) {
                        roleValues.add(role.toString());
                    }
                }
                return roleValues;
            }
        }
        return Collections.emptySet();
    }

    private Set<String> extractScopes(Jwt jwt) {
        Object scopes = jwt.getClaim(SCOPE_CLAIM);
        if (scopes instanceof String scopeString) {
            if (scopeString.isBlank()) {
                return Collections.emptySet();
            }
            Set<String> scopeValues = new HashSet<>();
            for (String scope : scopeString.split(" ")) {
                if (!scope.isBlank()) {
                    scopeValues.add(scope);
                }
            }
            return scopeValues;
        }
        Object scp = jwt.getClaim(SCP_CLAIM);
        if (scp instanceof Collection<?> collection) {
            Set<String> scopeValues = new HashSet<>();
            for (Object scope : collection) {
                if (scope != null) {
                    scopeValues.add(scope.toString());
                }
            }
            return scopeValues;
        }
        return Collections.emptySet();
    }
}
