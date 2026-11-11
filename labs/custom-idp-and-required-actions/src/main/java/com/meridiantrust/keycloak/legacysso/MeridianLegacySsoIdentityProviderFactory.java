package com.meridiantrust.keycloak.legacysso;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * Registers {@link MeridianLegacySsoIdentityProvider} with Keycloak under the realm's "Add provider"
 * Identity Provider picker, the same discovery mechanism chapter 33's User Storage SPI used: this
 * factory's fully qualified name is listed in
 * {@code META-INF/services/org.keycloak.broker.provider.IdentityProviderFactory}, and Keycloak scans
 * every jar in {@code /opt/keycloak/providers} for that file at build/startup time (chapter 39's
 * generic SPI-discovery diagram covers the common shape all of Keycloak's provider families share).
 */
public class MeridianLegacySsoIdentityProviderFactory
        extends AbstractIdentityProviderFactory<MeridianLegacySsoIdentityProvider> {

    public static final String PROVIDER_ID = "meridian-legacy-sso";

    @Override
    public String getName() {
        return "Meridian Legacy SSO (Cascade)";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public MeridianLegacySsoIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new MeridianLegacySsoIdentityProvider(session, model);
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new IdentityProviderModel();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty baseUrl = new ProviderConfigProperty();
        baseUrl.setName(MeridianLegacySsoIdentityProvider.CONFIG_LEGACY_BASE_URL);
        baseUrl.setLabel("Legacy SSO base URL");
        baseUrl.setHelpText("Base URL of the toy Cascade Savings Bank legacy SSO server, e.g. http://localhost:18141");
        baseUrl.setType(ProviderConfigProperty.STRING_TYPE);

        ProviderConfigProperty clientId = new ProviderConfigProperty();
        clientId.setName(MeridianLegacySsoIdentityProvider.CONFIG_CLIENT_ID);
        clientId.setLabel("Legacy client id");
        clientId.setHelpText("The client_id this Keycloak realm is registered under at the legacy SSO server.");
        clientId.setType(ProviderConfigProperty.STRING_TYPE);

        ProviderConfigProperty sharedSecret = new ProviderConfigProperty();
        sharedSecret.setName(MeridianLegacySsoIdentityProvider.CONFIG_SHARED_SECRET);
        sharedSecret.setLabel("Shared HMAC secret");
        sharedSecret.setHelpText("Secret shared out-of-band with the legacy SSO server, used to verify its callback signature.");
        sharedSecret.setType(ProviderConfigProperty.PASSWORD);
        sharedSecret.setSecret(true);

        return List.of(baseUrl, clientId, sharedSecret);
    }

    @Override
    public String getHelpText() {
        return "Brokers logins against the toy stand-in 'Cascade SSO' legacy protocol (chapter 40 lab).";
    }
}
