package com.meridiantrust.keycloak.businesshours;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * Factory for {@link BusinessHoursAuthenticator}. Discovered once, at server boot, via
 * {@code META-INF/services/org.keycloak.authentication.AuthenticatorFactory} (the standard Java
 * {@link java.util.ServiceLoader} convention every Keycloak SPI factory is discovered through --
 * see chapter 33's own account of this mechanism, which applies identically here).
 *
 * <p>Unlike chapter 33's User Storage SPI factory, this one has nothing expensive to load in
 * {@link #init(Config.Scope)} -- there is no external store to connect to, only a wall-clock check
 * -- so {@link #create(KeycloakSession)} simply builds a fresh, stateless
 * {@link BusinessHoursAuthenticator} per call. The two-class Factory/Provider split still exists
 * here even though this factory does no real setup work, because every Authenticator SPI provider
 * follows the same shape -- the split is part of the SPI's contract, not an optimization a simple
 * provider gets to skip.</p>
 */
public class BusinessHoursAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "meridian-business-hours";

    private static final BusinessHoursAuthenticator SINGLETON = new BusinessHoursAuthenticator();

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = List.of(
            new ProviderConfigProperty(
                    BusinessHoursAuthenticator.CONFIG_START_HOUR_UTC,
                    "Window start hour (UTC, 0-23)",
                    "The first hour (inclusive, UTC, 24-hour clock) during which login is permitted. "
                            + "Default: " + BusinessHoursAuthenticator.DEFAULT_START_HOUR_UTC + ".",
                    ProviderConfigProperty.STRING_TYPE,
                    String.valueOf(BusinessHoursAuthenticator.DEFAULT_START_HOUR_UTC)),
            new ProviderConfigProperty(
                    BusinessHoursAuthenticator.CONFIG_END_HOUR_UTC,
                    "Window end hour (UTC, 0-23)",
                    "The hour (exclusive, UTC, 24-hour clock) after which login is denied again. "
                            + "Default: " + BusinessHoursAuthenticator.DEFAULT_END_HOUR_UTC + ".",
                    ProviderConfigProperty.STRING_TYPE,
                    String.valueOf(BusinessHoursAuthenticator.DEFAULT_END_HOUR_UTC))
    );

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Meridian Trust: Business Hours Gate";
    }

    @Override
    public String getReferenceCategory() {
        return "business-hours";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Denies login entirely outside a configured window of hours (UTC, evaluated against "
                + "the real server clock) -- Meridian Trust's fraud-aware, business-hours-only login "
                + "gate for online banking.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // Nothing to load -- no external store, no expensive setup. Contrast with chapter 33's
        // factory, which parses an entire CSV file here because it has real state to build once.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No-op.
    }

    @Override
    public void close() {
        // No resources to release.
    }
}
