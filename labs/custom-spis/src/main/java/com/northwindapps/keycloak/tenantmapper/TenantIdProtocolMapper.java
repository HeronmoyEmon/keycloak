package com.northwindapps.keycloak.tenantmapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects a {@code tenant_id} claim, sourced from the authenticating user's own {@code tenant_id}
 * user attribute, into the access token, ID token, and UserInfo response alike -- Northwind Apps'
 * B2B multi-tenant SaaS model (chapter 2) made concrete: every one of Northwind's downstream
 * applications can read which tenant a given customer-facing login belongs to straight out of the
 * token, without a separate lookup call.
 *
 * <p><strong>A real structural asymmetry worth naming</strong>: both chapter 39's other two SPIs
 * (Authenticator, EventListenerProvider) need a separate Factory class, because each models two
 * genuinely different lifecycle stages (discovered once at boot vs. created per transaction).
 * {@link ProtocolMapper} does not split that way -- it extends
 * {@code ProviderFactory<ProtocolMapper>} itself, and {@link AbstractOIDCProtocolMapper#create}
 * simply hands back {@code this} (confirmed directly by inspecting that method on the real
 * 26.8.2 jar before writing this class). A protocol mapper is cheap and stateless enough that
 * Keycloak's own SPI design collapses Factory and Provider into one class here -- this is not a
 * shortcut this lab took, it is how the real interface is actually shaped.</p>
 *
 * <p>Deliberate simplification for this teaching lab: the source user attribute name
 * ({@code tenant_id}) is hardcoded rather than exposed as its own admin-configurable property.
 * Keycloak's own built-in {@code UserAttributeMapper} (chapter 24) already covers the general
 * "pick any user attribute, map it to any claim name" case with its own config UI; re-deriving that
 * generic machinery here would duplicate chapter 24's own concept rather than teach this chapter's
 * actual subject, which is how to write a protocol mapper's token-transformation methods, not how to
 * build a generic attribute-picker form.</p>
 */
public class TenantIdProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "northwind-tenant-id-mapper";

    /** The user attribute this mapper reads from -- see the class javadoc's "deliberate simplification" note. */
    static final String USER_ATTRIBUTE_NAME = "tenant_id";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(CONFIG_PROPERTIES);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(CONFIG_PROPERTIES, TenantIdProtocolMapper.class);
        for (ProviderConfigProperty property : CONFIG_PROPERTIES) {
            if (OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME.equals(property.getName())) {
                property.setDefaultValue(USER_ATTRIBUTE_NAME);
            }
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Northwind Apps: Tenant ID Claim";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Injects a tenant_id claim into the access token, ID token, and UserInfo response, "
                + "sourced from the user's own tenant_id attribute -- Northwind Apps' B2B "
                + "multi-tenant SaaS model, made concrete in a real token.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
                                             KeycloakSession session, UserSessionModel userSession,
                                             ClientSessionContext clientSessionCtx) {
        setTenantClaim(token, mappingModel, userSession);
        return token;
    }

    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel,
                                     KeycloakSession session, UserSessionModel userSession,
                                     ClientSessionContext clientSessionCtx) {
        setTenantClaim(token, mappingModel, userSession);
        return token;
    }

    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel,
                                               KeycloakSession session, UserSessionModel userSession,
                                               ClientSessionContext clientSessionCtx) {
        setTenantClaim(token, mappingModel, userSession);
        return token;
    }

    private static void setTenantClaim(IDToken token, ProtocolMapperModel mappingModel,
                                        UserSessionModel userSession) {
        UserModel user = userSession.getUser();
        if (user == null) {
            return;
        }
        String tenantId = user.getFirstAttribute(USER_ATTRIBUTE_NAME);
        if (tenantId == null || tenantId.isEmpty()) {
            return;
        }
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, tenantId);
    }
}
