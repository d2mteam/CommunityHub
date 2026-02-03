package com.m1.communityhub.config.security.dev;

import com.m1.communityhub.config.security.pro.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class DevHeaderAuthenticationFilter extends OncePerRequestFilter {
    public static final String HEADER_USER_ID = "X-Dev-User-Id";
    public static final String HEADER_USERNAME = "X-Dev-Username";
    public static final String HEADER_EMAIL = "X-Dev-Email";
    public static final String HEADER_ROLES = "X-Dev-Roles";
    public static final String HEADER_SCOPES = "X-Dev-Scopes";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = request.getHeader(HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                String username = request.getHeader(HEADER_USERNAME);
                if (username == null || username.isBlank()) {
                    username = userId;
                }
                String email = request.getHeader(HEADER_EMAIL);
                Set<String> roles = parseValues(request.getHeader(HEADER_ROLES));
                Set<String> scopes = parseValues(request.getHeader(HEADER_SCOPES));
                UserContext user = new UserContext(userId, username, email, roles, scopes);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private Set<String> parseValues(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split("[, ]+"))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .collect(Collectors.toSet());
    }
}
