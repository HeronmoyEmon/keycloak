package com.riversidehealth.keycloak.legacyhr;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts one {@link StaffRecord} from Riverside Health's legacy HR directory into the
 * {@link UserModel} shape Keycloak core actually works with.
 *
 * <p>{@code AbstractUserAdapter} (from {@code keycloak-model-storage}) implements every
 * {@code UserModel} method except {@link #getUsername()} with a reasonable read-only default, and
 * every write method ({@code setEmail}, {@code setFirstName}, ...) throws
 * {@code org.keycloak.storage.ReadOnlyException} unless overridden. That is exactly the right fit
 * here: this lab's provider has no way to write back into the real legacy HR system even if it
 * wanted to, so there is nothing to override on the write side at all -- the read-only default
 * behavior <em>is</em> the correct behavior, not a shortcut taken to save code.</p>
 */
public class LegacyStaffUserAdapter extends AbstractUserAdapter {

    private final StaffRecord record;

    public LegacyStaffUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel,
                                   StaffRecord record) {
        super(session, realm, storageProviderModel);
        this.record = record;
    }

    @Override
    public String getUsername() {
        return record.username();
    }

    @Override
    public String getFirstName() {
        return record.firstName();
    }

    @Override
    public String getLastName() {
        return record.lastName();
    }

    @Override
    public String getEmail() {
        return record.email();
    }

    @Override
    public boolean isEmailVerified() {
        // The legacy HR system is itself the source of truth for staff email addresses -- treat
        // every address it hands over as already verified rather than re-running Keycloak's own
        // email-verification required action against staff who never signed up through Keycloak.
        return true;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(UserModel.USERNAME, List.of(getUsername()));
        attributes.put(UserModel.FIRST_NAME, List.of(record.firstName()));
        attributes.put(UserModel.LAST_NAME, List.of(record.lastName()));
        attributes.put(UserModel.EMAIL, List.of(record.email()));
        // Two attributes the legacy system carries that Keycloak's built-in UserModel fields have
        // no slot for at all -- exactly the kind of extra, organization-specific data a custom SPI
        // provider can expose that Keycloak's native LDAP federation (chapter 32) would need an
        // explicit attribute mapper configured for instead.
        attributes.put("legacyEmployeeId", List.of(record.employeeId()));
        attributes.put("department", List.of(record.department()));
        return attributes;
    }

    @Override
    public List<String> getAttribute(String name) {
        return getAttributes().getOrDefault(name, List.of());
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        // UserModel.credentialManager() is abstract all the way down -- AbstractUserAdapter does
        // not implement it on a subclass's behalf (confirmed the hard way: this class failed to
        // compile without this override, with javac naming the missing method directly). Keycloak
        // core's own org.keycloak.credential.UserCredentialManager (from keycloak-model-storage) is
        // the standard bridge that forwards isValid()/isConfiguredFor() calls straight through to
        // this provider's own CredentialInputValidator implementation, so a federated, legacy-style
        // provider like this one never has to implement SubjectCredentialManager's full surface
        // itself.
        return new UserCredentialManager(session, realm, this);
    }
}
