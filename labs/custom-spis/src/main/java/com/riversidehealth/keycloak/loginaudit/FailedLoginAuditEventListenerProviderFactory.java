package com.riversidehealth.keycloak.loginaudit;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for {@link FailedLoginAuditEventListenerProvider}. Discovered once, at server boot, via
 * {@code META-INF/services/org.keycloak.events.EventListenerProviderFactory}.
 *
 * <p>{@link #getId()}'s return value ({@code riverside-failed-login-audit}) is the id a realm's
 * Events config (chapter 28) must list under "Event Listeners" to actually enable this provider for
 * that realm -- enabling a provider id for a realm is what causes Keycloak to call
 * {@link #create(KeycloakSession)} for that realm's requests at all; an un-enabled provider sits
 * registered (visible in {@code serverinfo}) but is never invoked.</p>
 */
public class FailedLoginAuditEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String PROVIDER_ID = "riverside-failed-login-audit";

    private static final FailedLoginAuditEventListenerProvider SINGLETON =
            new FailedLoginAuditEventListenerProvider();

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // Nothing to load.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No-op.
    }

    @Override
    public void close() {
        // No resources to release.
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
