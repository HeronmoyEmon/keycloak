package com.riversidehealth.keycloak.hipaaconsent;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;

import java.time.Instant;
import java.util.List;

/**
 * A custom Keycloak Required Action that forces a one-time HIPAA patient-data-handling
 * acknowledgment onto Riverside Health clinicians before their login session is allowed to complete.
 *
 * <p>This is the mechanism underneath chapter 25's built-in required-action configuration: chapter 25
 * configured and ordered <em>built-in</em> actions (Configure OTP, Update Password, Verify Email);
 * this class is a brand-new one, discovered the same way (a {@code *Factory} registered in
 * {@code META-INF/services/org.keycloak.authentication.RequiredActionFactory}, scanned from
 * {@code /opt/keycloak/providers}) but with code this course wrote rather than code Keycloak ships.</p>
 *
 * <p><strong>Required action vs. authenticator, the distinction chapter 39 flags from the other
 * side:</strong> a required action (this class) runs <em>after</em> the credential check has already
 * succeeded -- it can never cause a correct password/OTP to be rejected, it can only insert a
 * mandatory extra step between "authentication succeeded" and "session handed to the application."
 * A custom authenticator (chapter 39) runs <em>during</em> the credential check itself and can refuse
 * to let authentication succeed at all. Both can block a login from completing, but at genuinely
 * different points in the timeline -- confusing the two is the single most common beginner mistake
 * with Keycloak's authentication flow model (chapter 25's "common mistakes" section already flagged
 * the built-in-action version of this same confusion).</p>
 *
 * <p>Follows the exact shape of Keycloak's own built-in
 * {@code org.keycloak.authentication.requiredactions.TermsAndConditions} (decompiled from the running
 * 26.8.2 image's {@code keycloak-services} jar as this chapter's reference, not copied from any
 * external source -- Keycloak's built-in class is itself the same one-class-implements-both-interfaces
 * pattern used here): one class implements both {@link RequiredActionProvider} (the per-invocation
 * behavior) and {@link RequiredActionFactory} (the registration/discovery metadata), with
 * {@code create(session)} simply returning {@code this} since this provider is stateless.</p>
 */
public class RiversideHipaaAcknowledgmentRequiredAction implements RequiredActionProvider, RequiredActionFactory {

    public static final String PROVIDER_ID = "riverside-hipaa-ack";

    /** User attribute recording when (epoch seconds) the acknowledgment was accepted. */
    static final String ACKNOWLEDGED_AT_ATTRIBUTE = "riversideHipaaAckAt";

    private static final String TEMPLATE = "riverside-hipaa-consent.ftl";
    private static final String FORM_FIELD_CANCEL = "cancel";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        // Deliberately a no-op: this lab assigns the action to one specific test user via the Admin
        // REST API (the same per-user assignment chapter 22 did for priya.patel's CONFIGURE_TOTP),
        // not as a realm-wide default-for-every-new-user action. A real Riverside Health deployment
        // could instead check something like "has this clinician ever acknowledged the current policy
        // version" here and call context.getUser().addRequiredAction(PROVIDER_ID) automatically --
        // left out of this lab to keep the one mechanism being taught (the required action itself)
        // separate from a second, independent policy decision (who gets it assigned, and when).
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
                .setAttribute("user", context.getUser())
                .createForm(TEMPLATE);
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey(FORM_FIELD_CANCEL)) {
            context.failure("riversideHipaaAckDeclined");
            return;
        }
        UserModel user = context.getUser();
        user.setAttribute(ACKNOWLEDGED_AT_ATTRIBUTE, List.of(Long.toString(Instant.now().getEpochSecond())));
        context.success();
    }

    @Override
    public String getDisplayText() {
        return "Riverside Health – HIPAA Data Handling Acknowledgment";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Config.Scope config) {
        // no-op: this provider needs no server-wide configuration.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op: stateless, nothing to release.
    }
}
