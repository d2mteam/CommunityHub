package com.m1.communityhub.config;

import com.m1.communityhub.security.AudienceValidator;
import com.m1.communityhub.security.KeycloakJwtAuthenticationConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );
        return http.build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }

    @Bean
    public JwtDecoder jwtDecoder(
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuer,
        @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String jwkSetUri,
        @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location:}") Resource publicKeyLocation,
        @Value("${app.security.jwt.audience:}") String audience
    ) {
        JwtDecoder decoder = createJwtDecoder(issuer, jwkSetUri, publicKeyLocation);
        OAuth2TokenValidator<Jwt> issuerValidator = issuer == null || issuer.isBlank()
            ? JwtValidators.createDefault()
            : JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            issuerValidator,
            new AudienceValidator(AudienceValidator.parseAudiences(audience))
        );
        if (decoder instanceof org.springframework.security.oauth2.jwt.NimbusJwtDecoder nimbusDecoder) {
            nimbusDecoder.setJwtValidator(validator);
        }
        return decoder;
    }

    private JwtDecoder createJwtDecoder(String issuer, String jwkSetUri, Resource publicKeyLocation) {
        if (publicKeyLocation != null && publicKeyLocation.exists()) {
            try {
                return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withPublicKey(
                    loadPublicKey(publicKeyLocation)
                ).build();
            } catch (IOException | GeneralSecurityException ex) {
                throw new IllegalStateException("Failed to read public key for JWT validation.", ex);
            }
        }
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Either issuer-uri or jwk-set-uri must be configured for JWT validation.");
        }
        return JwtDecoders.fromIssuerLocation(issuer);
    }

    private RSAPublicKey loadPublicKey(Resource publicKeyLocation) throws IOException, GeneralSecurityException {
        String pem = new String(publicKeyLocation.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String sanitized = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(sanitized);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
}
