package com.northwindapps.billing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

/**
 * Makes this app a real UMA 2.0 Policy Enforcement Point against Keycloak's own Protection API
 * and token endpoint -- the exact wire-level round trip chapter 37's lab already proved out with
 * raw curl, now driven by the resource server itself instead of staying a standalone transcript.
 *
 * <p>Two calls, in order, for every protected request:
 * <ol>
 *   <li>Using this app's own client-credentials token, ask the Protection API for a permission
 *       ticket naming the resource + scope the caller is trying to reach.</li>
 *   <li>Exchange that ticket for a Requesting Party Token (RPT) at the normal token endpoint,
 *       using {@code grant_type=urn:ietf:params:oauth:grant-type:uma-ticket} and forwarding the
 *       caller's own bearer token in the {@code Authorization} header -- that header is how
 *       Keycloak learns who is actually asking (confirmed live in chapter 37's lab).</li>
 * </ol>
 * A 200 here means Keycloak's policies (the {@code billing-admin-policy} role policy, in this
 * app's case) permitted the request; any other status means denied, and this service treats
 * every non-200 response as a denial rather than trying to distinguish error shapes, since
 * chapter 37's lab found the denial error body has no documented stable contract.
 */
@Service
public class UmaPermissionService {

    private final RestClient restClient = RestClient.create();

    @Value("${northwind.uma.token-endpoint}")
    private String tokenEndpoint;

    @Value("${northwind.uma.protection-endpoint}")
    private String protectionEndpoint;

    @Value("${northwind.uma.client-id}")
    private String clientId;

    @Value("${northwind.uma.client-secret}")
    private String clientSecret;

    @Value("${northwind.uma.premium-report-resource-id}")
    private String premiumReportResourceId;

    public boolean callerMayViewPremiumReport(String callerBearerToken) {
        String rsToken = fetchResourceServerToken();
        String ticket = requestPermissionTicket(rsToken, premiumReportResourceId, "view");
        return exchangeTicketForRpt(callerBearerToken, ticket);
    }

    private String fetchResourceServerToken() {
        Map<String, Object> body = restClient.post()
                .uri(tokenEndpoint)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .body(Map.class);
        return (String) body.get("access_token");
    }

    private String requestPermissionTicket(String rsToken, String resourceId, String scope) {
        Map<String, Object> body = restClient.post()
                .uri(protectionEndpoint)
                .header("Authorization", "Bearer " + rsToken)
                .header("Content-Type", "application/json")
                .body(List.of(Map.of("resource_id", resourceId, "resource_scopes", List.of(scope))))
                .retrieve()
                .body(Map.class);
        return (String) body.get("ticket");
    }

    private boolean exchangeTicketForRpt(String callerBearerToken, String ticket) {
        try {
            restClient.post()
                    .uri(tokenEndpoint)
                    .header("Authorization", "Bearer " + callerBearerToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("grant_type=urn:ietf:params:oauth:grant-type:uma-ticket"
                            + "&ticket=" + ticket
                            + "&client_id=" + clientId
                            + "&client_secret=" + clientSecret)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            // Keycloak returns 403 access_denied for a denied UMA exchange (chapter 37's finding,
            // reconfirmed here) -- any 4xx here is a real denial, not an application error.
            return false;
        }
    }
}
