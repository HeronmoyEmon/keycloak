package com.riversidehealth.keycloak.legacyhr;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * Factory for {@link LegacyStaffDirectoryUserStorageProvider}.
 *
 * <p>{@link #getId()}'s return value is the provider id that shows up as an option in the Admin
 * Console's "Add provider" dropdown under User Federation, and is also the {@code providerId}
 * value a {@code kcadm.sh}/REST-created component must specify. This class is wired to Keycloak
 * through {@code META-INF/services/org.keycloak.storage.UserStorageProviderFactory}, a plain text
 * file (the standard Java {@link java.util.ServiceLoader} convention, which is how Keycloak itself
 * discovers every SPI provider factory on its classpath) listing this class's fully qualified
 * name -- see that file for the real, necessary detail: get its filename or contents wrong and
 * Keycloak never finds this provider at all, with no error message pointing at why.</p>
 */
public class LegacyStaffDirectoryUserStorageProviderFactory
        implements UserStorageProviderFactory<LegacyStaffDirectoryUserStorageProvider> {

    public static final String PROVIDER_ID = "riverside-legacy-staff-directory";

    /**
     * Loaded once, from {@link #init(Config.Scope)}, and shared read-only across every provider
     * instance {@link #create} hands back for this deployment's lifetime.
     */
    private LegacyStaffDirectoryStore store;

    @Override
    public void init(Config.Scope config) {
        this.store = LegacyStaffDirectoryStore.loadFromClasspath("/riverside-legacy-staff.csv");
    }

    @Override
    public LegacyStaffDirectoryUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new LegacyStaffDirectoryUserStorageProvider(session, model, store);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Bridges Keycloak to Riverside Health's legacy, proprietary HR staff directory "
                + "(a bundled CSV standing in for the real system) for read-only lookup and password "
                + "validation, without migrating or rewriting that system.";
    }
}
