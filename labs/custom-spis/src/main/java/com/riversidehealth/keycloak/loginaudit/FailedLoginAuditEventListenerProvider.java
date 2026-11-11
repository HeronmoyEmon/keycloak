package com.riversidehealth.keycloak.loginaudit;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import java.time.Instant;
import java.util.Map;

/**
 * Logs every real {@code LOGIN_ERROR} event, for this provider's configured realm, in a
 * distinguishable format -- Riverside Health's HIPAA-relevant audit trail (chapter 11) for failed
 * authentication attempts against clinician/administrative accounts.
 *
 * <p>Deliberately scoped narrow for this teaching lab: only {@code LOGIN_ERROR} user events are
 * handled, not every event type, and admin events ({@link #onEvent(AdminEvent, boolean)}) are left
 * as a documented no-op rather than implemented, since this chapter's brief is the failed-login
 * audit trail specifically, not a general-purpose event mirror -- chapter 28 already covers
 * Keycloak's own built-in event store/listener configuration for the general case.</p>
 *
 * <p>Logging mechanism: plain {@code System.out.println}, on purpose, not a logging framework call.
 * A real production listener would hook into the organization's actual logging/SIEM pipeline (e.g.
 * {@code org.jboss.logging.Logger}, or pushing directly to a HIPAA-compliant log aggregator) rather
 * than writing to stdout -- this lab uses stdout specifically because it is guaranteed to land in
 * {@code docker compose logs}/{@code docker logs} output regardless of how the server's own logging
 * framework happens to be configured, which is what this chapter's lab needs to capture a real,
 * verifiable transcript. That trade-off is named here rather than left implicit.</p>
 */
public class FailedLoginAuditEventListenerProvider implements EventListenerProvider {

    /** A distinguishable prefix, grep-able in container logs, per this chapter's brief. */
    static final String AUDIT_PREFIX = "RIVERSIDE-HIPAA-LOGIN-AUDIT";

    // No realm filter is implemented here on purpose: Keycloak only ever invokes an event listener
    // provider's onEvent() for a realm that has that provider's id enabled under that realm's own
    // Events config (chapter 28) -- the "for this provider's configured realm" scoping in this
    // class's javadoc is enforced by the realm's own Events settings, not by code in this class.
    // Confirmed live in this chapter's lab: this listener was enabled only on the riverside-health
    // realm, and never received an event generated against a different realm in the same server.

    FailedLoginAuditEventListenerProvider() {
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() != EventType.LOGIN_ERROR) {
            return;
        }
        System.out.println(formatLine(event));
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Deliberate no-op -- see class javadoc. Admin events are a different, separate stream
        // (chapter 28) this chapter's brief does not ask this provider to audit.
    }

    static String formatLine(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append(AUDIT_PREFIX).append(" | ");
        sb.append("time=").append(Instant.ofEpochMilli(event.getTime())).append(" | ");
        sb.append("realm=").append(event.getRealmName()).append(" | ");
        sb.append("clientId=").append(event.getClientId()).append(" | ");
        sb.append("userId=").append(event.getUserId() == null ? "(unresolved)" : event.getUserId()).append(" | ");
        sb.append("ipAddress=").append(event.getIpAddress()).append(" | ");
        sb.append("error=").append(event.getError());
        Map<String, String> details = event.getDetails();
        if (details != null && !details.isEmpty()) {
            sb.append(" | details=").append(details);
        }
        return sb.toString();
    }

    @Override
    public void close() {
        // No resources to release.
    }
}
