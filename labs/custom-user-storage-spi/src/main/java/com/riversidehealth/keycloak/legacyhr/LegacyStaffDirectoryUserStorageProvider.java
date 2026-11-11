package com.riversidehealth.keycloak.legacyhr;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

/**
 * Keycloak-facing provider instance for Riverside Health's legacy staff directory.
 *
 * <p>This is the object chapter 33's diagram calls "the provider" -- created fresh per Keycloak
 * transaction by {@link LegacyStaffDirectoryUserStorageProviderFactory#create}, sharing the one
 * factory-owned, already-loaded {@link LegacyStaffDirectoryStore} rather than re-reading the CSV
 * on every call. It implements the two SPI capabilities this lab actually needs:</p>
 * <ul>
 *   <li>{@link UserLookupProvider} -- so Keycloak core can resolve a username/id/email to a
 *       {@code UserModel} it didn't create or store itself.</li>
 *   <li>{@link CredentialInputValidator} -- so Keycloak's own direct-grant and browser login flows
 *       can ask "is this password correct for this user?" without this provider needing to
 *       implement an entire custom authenticator.</li>
 * </ul>
 */
public class LegacyStaffDirectoryUserStorageProvider implements UserStorageProvider, UserLookupProvider,
        CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final LegacyStaffDirectoryStore store;

    public LegacyStaffDirectoryUserStorageProvider(KeycloakSession session, ComponentModel model,
                                                     LegacyStaffDirectoryStore store) {
        this.session = session;
        this.model = model;
        this.store = store;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        // Keycloak hands federated users an id of the form "f:<componentId>:<externalId>"
        // (org.keycloak.storage.StorageId's own convention) wherever a user's UUID would normally
        // go. StorageId.externalId() strips the "f:<componentId>:" prefix back off.
        String externalId = StorageId.externalId(id);
        return getUserByUsername(realm, externalId);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        StaffRecord record = store.findByUsername(username);
        if (record == null) {
            return null;
        }
        return new LegacyStaffUserAdapter(session, realm, model, record);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        StaffRecord record = store.findByEmail(email);
        if (record == null) {
            return null;
        }
        return new LegacyStaffUserAdapter(session, realm, model, record);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && store.findByUsername(user.getUsername()) != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())) {
            return false;
        }
        StaffRecord record = store.findByUsername(user.getUsername());
        if (record == null) {
            return false;
        }
        // A real provider for a real legacy system would call that system's own authentication
        // API or compare against whatever hash format it actually uses. This toy store's plaintext
        // comparison is the honest, simplest possible stand-in -- see StaffRecord's javadoc and
        // this chapter's pitfall callout for why that's a real-world risk, not a code shortcut to
        // ignore.
        return record.password().equals(input.getChallengeResponse());
    }

    @Override
    public void close() {
        // Nothing provider-instance-scoped to release: the CSV-backed store is loaded once by the
        // factory and is shared, read-only, across every per-transaction provider instance.
    }
}
