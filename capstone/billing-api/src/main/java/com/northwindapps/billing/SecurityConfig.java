package com.northwindapps.billing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Wires this app up as an OAuth2 Resource Server: every request is authenticated (or rejected)
 * based solely on a bearer JWT's validity against the realm named in
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri} -- no session, no cookie, no
 * login form of its own. This is "What concept is this?" territory recalled concretely: chapter
 * 8 named the bearer-token model and chapter 16 named signature verification against a JWKS;
 * this class is where Spring Security actually performs both, automatically, once
 * {@code spring-boot-starter-oauth2-resource-server} is on the classpath and an issuer-uri is
 * configured -- fetching {@code <issuer-uri>/.well-known/openid-configuration} to learn the
 * token issuer and jwks_uri, then verifying every incoming token's signature, issuer, and
 * expiry against that JWKS automatically on every request (see this chapter's validation-flow
 * diagram).
 *
 * <p>The one piece Spring Security does <em>not</em> do automatically, correctly, out of the
 * box against a Keycloak-issued token is turn that token's roles into Spring Security
 * authorities -- see {@link KeycloakRealmRoleConverter}'s javadoc for the real gotcha this
 * chapter's lab hit and how {@code jwtAuthenticationConverter()} below fixes it.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // This API is a pure resource server: every caller presents a fresh bearer token on
            // every request, so there is no server-side session to create or maintain.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // A resource server validating only bearer tokens (never a browser form submission)
            // has no CSRF-vulnerable session-cookie-based state to protect -- CSRF protection is
            // a defense for cookie-authenticated, browser-based requests, which this API never
            // accepts (chapter 18 covers CSRF defenses for flows that genuinely need them).
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/invoices/**").hasAnyRole("billing-reader", "billing-admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    /**
     * Combines Spring Security's default JWT claim extraction ({@code sub} as the principal
     * name) with {@link KeycloakRealmRoleConverter}'s realm-role-to-authority mapping, instead
     * of the default {@code JwtGrantedAuthoritiesConverter} that looks for a {@code scope}/
     * {@code scp} claim Keycloak's access tokens don't carry in that shape.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}
