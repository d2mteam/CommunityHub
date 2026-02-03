package com.m1.communityhub.config.security.pro;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserContext(
    String userId,
    String username,
    String email,
    Set<String> roles,
    Set<String> scopes
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null && scopes == null) {
            return Collections.emptySet();
        }
        Set<String> roleValues = roles == null ? Set.of() : roles;
        Set<String> scopeValues = scopes == null ? Set.of() : scopes;
        java.util.Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        authorities.addAll(
            roleValues.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toSet())
        );
        authorities.addAll(
            scopeValues.stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).collect(Collectors.toSet())
        );
        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }
}
