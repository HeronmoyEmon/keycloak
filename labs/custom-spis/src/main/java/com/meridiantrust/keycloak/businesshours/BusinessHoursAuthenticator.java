package com.meridiantrust.keycloak.businesshours;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Meridian Trust's business-hours-only login gate.
 *
 * <p>A concrete, testable condition fitting a bank's fraud-aware authentication posture (chapter 9):
 * block login attempts entirely outside a configured window of hours, evaluated against the real
 * server clock in UTC. This runs <strong>during</strong> authentication, before the flow can succeed
 * or fail -- not an afterthought check following a successful login, and not a required action
 * (chapter 40 covers that different insertion point: a required action only ever runs after
 * authentication has already succeeded).</p>
 *
 * <p>{@link #authenticate(AuthenticationFlowContext)} and {@link #action(AuthenticationFlowContext)}
 * share one implementation on purpose: this authenticator never shows the user a form (it has
 * nothing to ask them), so {@code action()} -- which only ever runs in response to a form
 * submission -- is never actually invoked by the framework for this authenticator. It still has to
 * be implemented to satisfy the {@link Authenticator} interface, and delegating to
 * {@link #authenticate(AuthenticationFlowContext)} rather than leaving it empty keeps the two
 * methods from silently diverging if this class is ever extended to show a form later.</p>
 */
public class BusinessHoursAuthenticator implements Authenticator {

    static final String CONFIG_START_HOUR_UTC = "startHourUtc";
    static final String CONFIG_END_HOUR_UTC = "endHourUtc";
    static final int DEFAULT_START_HOUR_UTC = 9;
    static final int DEFAULT_END_HOUR_UTC = 17;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        int startHour = readHourConfig(configModel, CONFIG_START_HOUR_UTC, DEFAULT_START_HOUR_UTC);
        int endHour = readHourConfig(configModel, CONFIG_END_HOUR_UTC, DEFAULT_END_HOUR_UTC);

        int currentHourUtc = ZonedDateTime.now(ZoneOffset.UTC).getHour();

        if (isWithinWindow(currentHourUtc, startHour, endHour)) {
            context.success();
            return;
        }

        // Tag the login event's error field explicitly before failing the flow, so chapter 39's own
        // event-listener lab (Riverside Health's failed-login audit log) has a real, distinguishable
        // "access_denied" LOGIN_ERROR event to capture.
        context.getEvent().detail("meridian_business_hours_window",
                startHour + ":00-" + endHour + ":00 UTC (current hour: " + currentHourUtc + ")");
        context.getEvent().error(org.keycloak.events.Errors.ACCESS_DENIED);

        // A real bug hit while building this lab, worth keeping rather than quietly working around:
        // the single-argument context.failure(AuthenticationFlowError) overload DOES correctly emit
        // the LOGIN_ERROR event above, but then throws an uncaught AuthenticationFlowException on the
        // Direct Grant (Resource Owner Password Credentials) code path specifically -- confirmed live
        // via the server's own stack trace (DefaultAuthenticationFlow.processResult, called from
        // ResourceOwnerPasswordCredentialsGrantType.process), surfacing to the client as a bare
        // HTTP 500 "unknown_error" instead of a clean OAuth error. Direct Grant has no browser to
        // render an HTML challenge page for, and the single-argument overload appears to assume one
        // exists. The fix: always supply an explicit challenge Response, built here as a small JSON
        // body rather than via context.form() (which renders an HTML error page meant for the
        // browser flow, not a REST client) -- confirmed this produces a clean, client-readable error
        // for Direct Grant instead.
        Response challenge = Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\":\"access_denied\",\"error_description\":"
                        + "\"Meridian Trust online banking is only available " + startHour
                        + ":00-" + endHour + ":00 UTC. Current UTC hour: " + currentHourUtc + ".\"}")
                .build();
        context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        authenticate(context);
    }

    @Override
    public boolean requiresUser() {
        // Deliberately false: the business-hours check depends only on the wall clock, never on
        // which user is logging in, so this authenticator does not need the flow to have already
        // resolved a UserModel before it runs.
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions to attach -- this authenticator only ever allows or denies, it never
        // asks the user to do anything.
    }

    private static int readHourConfig(AuthenticatorConfigModel configModel, String key, int defaultValue) {
        if (configModel == null) {
            return defaultValue;
        }
        Map<String, String> config = configModel.getConfig();
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.get(key).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * True if {@code currentHour} falls in {@code [startHour, endHour)}, UTC, 0-23. Does not handle
     * a window that wraps past midnight (e.g. start=22, end=4) -- a deliberate scope limit for this
     * teaching lab, called out here rather than silently mishandled.
     */
    static boolean isWithinWindow(int currentHour, int startHour, int endHour) {
        return currentHour >= startHour && currentHour < endHour;
    }

    @Override
    public void close() {
        // No resources to release.
    }
}
