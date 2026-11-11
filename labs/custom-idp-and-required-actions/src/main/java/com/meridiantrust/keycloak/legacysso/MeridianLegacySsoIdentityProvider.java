package com.meridiantrust.keycloak.legacysso;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.UserAuthenticationIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.time.Instant;

/**
 * A custom Keycloak Identity Provider that brokers logins against a toy stand-in for "Cascade SSO" --
 * a fictional legacy, proprietary single-sign-on system belonging to Cascade Savings Bank, a smaller
 * institution Meridian Trust has just acquired. Cascade SSO is NOT a real named vendor product; it is
 * a deliberately minimal protocol invented for this chapter so the redirect-out/callback-in shape
 * every broker needs can be built and exercised end to end without a real external dependency. See
 * chapter 40's text for why this scenario (an acquired institution's un-migrated legacy IdP) is a
 * realistic reason a real IAM specialist would ever need to write a brand-new broker type instead of
 * reaching for one of Keycloak's built-in OIDC/SAML/social broker implementations (chapter 31).
 *
 * <p>The protocol this class speaks, end to end:</p>
 * <ol>
 *   <li>{@link #performLogin} redirects the browser to the toy IdP's
 *       {@code GET /legacy-sso/authorize} endpoint, carrying {@code client_id}, the
 *       Keycloak-computed {@code redirect_uri} (the fixed broker-endpoint convention from chapter 31 --
 *       {@code /realms/{realm}/broker/{alias}/endpoint}), and the opaque, Keycloak-minted
 *       {@code state} value unchanged.</li>
 *   <li>The toy IdP authenticates the user itself (its own login form, its own user table -- Keycloak
 *       never sees the Cascade credential) and 302-redirects the browser back to that exact
 *       {@code redirect_uri}, appending {@code legacy_user}, {@code legacy_email}, {@code legacy_first_name},
 *       {@code legacy_last_name}, the same {@code state} it was given, a unix-seconds {@code ts}, and an
 *       HMAC-SHA256 {@code sig} over those fields, keyed with a secret shared out-of-band with this
 *       provider's configuration.</li>
 *   <li>Keycloak's generic broker endpoint resolves this provider and calls {@link #callback}, which
 *       hands back {@link Endpoint} -- a small JAX-RS sub-resource that verifies the signature and the
 *       timestamp, resolves the real Keycloak {@link AuthenticationSessionModel} behind the state value,
 *       and -- only if all of that checks out -- builds a {@link BrokeredIdentityContext} and calls
 *       {@code callback.authenticated(...)}, which is what actually lets Keycloak proceed to its own
 *       First Broker Login / JIT-provisioning logic (chapter 31) for the Cascade identity.</li>
 * </ol>
 */
public class MeridianLegacySsoIdentityProvider extends AbstractIdentityProvider<IdentityProviderModel> {

    static final String CONFIG_LEGACY_BASE_URL = "legacyBaseUrl";
    static final String CONFIG_CLIENT_ID = "legacyClientId";
    static final String CONFIG_SHARED_SECRET = "sharedSecret";

    /** Callback signatures older than this many seconds are rejected as a stale/replayed redirect. */
    private static final long MAX_CLOCK_SKEW_SECONDS = 300;

    public MeridianLegacySsoIdentityProvider(KeycloakSession session, IdentityProviderModel config) {
        super(session, config);
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        IdentityProviderModel config = getConfig();
        String legacyBaseUrl = config.getConfig().get(CONFIG_LEGACY_BASE_URL);
        String clientId = config.getConfig().get(CONFIG_CLIENT_ID);
        if (legacyBaseUrl == null || legacyBaseUrl.isBlank()) {
            throw new IdentityBrokerException(
                    "Meridian legacy SSO provider '" + config.getAlias() + "' has no " + CONFIG_LEGACY_BASE_URL
                            + " configured.");
        }
        UriBuilder authorizeUri = UriBuilder.fromUri(legacyBaseUrl)
                .path("legacy-sso/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", request.getRedirectUri())
                .queryParam("state", request.getState().getEncoded());
        return Response.seeOther(authorizeUri.build()).build();
    }

    @Override
    public Object callback(RealmModel realm, UserAuthenticationIdentityProvider.AuthenticationCallback callback,
                            EventBuilder event) {
        return new Endpoint(realm, callback, event, this);
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel federatedIdentityModel) {
        // Cascade SSO is a pure redirect-and-assert protocol with nothing resembling an OAuth access
        // token to hand back -- there is no further token to retrieve. This method exists only
        // because UserAuthenticationIdentityProvider declares it abstract for providers (like the
        // OIDC/OAuth2 brokers) that DO have something to return here; a token-less broker like this
        // one legitimately has nothing to put in the body.
        return Response.ok("{\"note\":\"Cascade legacy SSO has no retrievable token.\"}").build();
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel federatedIdentityModel,
                                   UserSessionModel userSession, UserModel user) {
        return retrieveToken(session, federatedIdentityModel);
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
                               BrokeredIdentityContext context) {
        // Mark provenance on the JIT-provisioned local user so an auditor (chapter 11's compliance
        // concerns are exactly this) can tell, years later, that this account's identity originated
        // from the acquired Cascade Savings Bank system rather than a native Meridian Trust signup.
        user.setSingleAttribute("legacySource", "cascade-sso");
        super.importNewUser(session, realm, user, context);
    }

    /**
     * The JAX-RS sub-resource Keycloak's generic broker endpoint dispatches to for every request that
     * lands on {@code /realms/{realm}/broker/{alias}/endpoint} for this provider -- the "callback-in"
     * half of the redirect-out/callback-in sequence.
     */
    public static class Endpoint {

        private final RealmModel realm;
        private final UserAuthenticationIdentityProvider.AuthenticationCallback callback;
        private final EventBuilder event;
        private final MeridianLegacySsoIdentityProvider provider;

        Endpoint(RealmModel realm, UserAuthenticationIdentityProvider.AuthenticationCallback callback,
                 EventBuilder event, MeridianLegacySsoIdentityProvider provider) {
            this.realm = realm;
            this.callback = callback;
            this.event = event;
            this.provider = provider;
        }

        @GET
        @Path("")
        public Response callback(@Context UriInfo uriInfo) {
            String legacyUser = uriInfo.getQueryParameters().getFirst("legacy_user");
            String legacyEmail = uriInfo.getQueryParameters().getFirst("legacy_email");
            String legacyFirstName = uriInfo.getQueryParameters().getFirst("legacy_first_name");
            String legacyLastName = uriInfo.getQueryParameters().getFirst("legacy_last_name");
            String state = uriInfo.getQueryParameters().getFirst("state");
            String ts = uriInfo.getQueryParameters().getFirst("ts");
            String sig = uriInfo.getQueryParameters().getFirst("sig");

            IdentityProviderModel config = provider.getConfig();
            if (state == null || ts == null || sig == null || legacyUser == null) {
                return callback.error(config, "missingCallbackParameters");
            }

            String secret = config.getConfig().get(CONFIG_SHARED_SECRET);
            String expectedMessage = legacyUser + "|" + (legacyEmail == null ? "" : legacyEmail) + "|" + state + "|" + ts;
            String expectedSig = LegacySsoUtil.hmacSha256Hex(secret, expectedMessage);
            if (!LegacySsoUtil.constantTimeEquals(expectedSig, sig)) {
                return callback.error(config, "invalidLegacySsoSignature");
            }

            long tsSeconds;
            try {
                tsSeconds = Long.parseLong(ts);
            } catch (NumberFormatException e) {
                return callback.error(config, "invalidLegacySsoTimestamp");
            }
            long nowSeconds = Instant.now().getEpochSecond();
            if (Math.abs(nowSeconds - tsSeconds) > MAX_CLOCK_SKEW_SECONDS) {
                return callback.error(config, "expiredLegacySsoCallback");
            }

            AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
            if (authSession == null) {
                return callback.error(config, "invalidLegacySsoState");
            }
            // "session" is AbstractIdentityProvider's own protected field; Endpoint, as a nested class
            // of MeridianLegacySsoIdentityProvider, can reach it through a provider instance.
            provider.session.getContext().setAuthenticationSession(authSession);
            provider.session.getContext().setRealm(realm);

            BrokeredIdentityContext context = new BrokeredIdentityContext(legacyUser, config);
            context.setIdp(provider);
            context.setUsername(legacyUser);
            context.setEmail(legacyEmail);
            context.setFirstName(legacyFirstName);
            context.setLastName(legacyLastName);
            context.setBrokerUserId(legacyUser);
            context.setAuthenticationSession(authSession);

            return this.callback.authenticated(context);
        }
    }
}
