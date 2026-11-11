package com.northwindapps.billing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts a Keycloak-issued JWT's {@code realm_access.roles} claim into Spring Security
 * {@link GrantedAuthority} instances, each prefixed {@code ROLE_} so {@code hasRole(...)} and
 * {@code @PreAuthorize("hasRole(...)")} work the way Spring Security expects.
 *
 * <p>This class exists because of a real, well-documented gap -- confirmed the hard way while
 * building this chapter's lab, not assumed up front: Spring Security's own default JWT-to-
 * authorities converter ({@code JwtGrantedAuthoritiesConverter}) looks for a {@code scope} or
 * {@code scp} claim (space-separated OAuth2 scopes), which is the shape a generic OAuth2
 * authorization server's access token carries. Keycloak's access tokens carry roles in a
 * differently-shaped, Keycloak-specific claim instead:
 *
 * <pre>
 * "realm_access": { "roles": ["billing-reader", "offline_access", "uma_authorization"] }
 * </pre>
 *
 * Without this converter, every endpoint protected with {@code hasRole("billing-reader")} or
 * similar returns 403 for every caller, no matter how correctly that caller's token was issued
 * and signed -- the token is valid and authenticated, but Spring Security never extracts any
 * authority named {@code ROLE_billing-reader} from it, because it was never looking at
 * {@code realm_access} in the first place. This chapter's "common mistakes" section covers the
 * exact 403 this produces and how it was diagnosed.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !(realmAccess.get(ROLES_CLAIM) instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority(ROLE_AUTHORITY_PREFIX + role))
                .collect(Collectors.toList());
    }
}
