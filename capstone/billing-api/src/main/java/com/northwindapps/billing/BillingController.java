package com.northwindapps.billing;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Four endpoints at four different protection levels -- the first three exactly as chapter 34's
 * brief asked for, the fourth new for the capstone:
 *
 * <ul>
 *   <li>{@code GET /api/public/health} -- no token required at all.</li>
 *   <li>{@code GET /api/invoices} -- any caller holding the realm role {@code billing-reader}
 *       or {@code billing-admin} (enforced in {@link SecurityConfig}'s filter chain, before this
 *       method body ever runs).</li>
 *   <li>{@code POST /api/invoices} -- only a caller holding {@code billing-admin}, enforced here
 *       with method-level {@code @PreAuthorize} instead of the filter chain, to show both
 *       enforcement styles for real in the same small app.</li>
 *   <li>{@code GET /api/invoices/premium-report} -- gated not by a Spring role check but by a
 *       real server-side UMA 2.0 decision from Keycloak itself ({@link UmaPermissionService}),
 *       demonstrating resource-level authorization a static role check can't express as cleanly:
 *       this app asks Keycloak, per request, "is this specific caller allowed this specific
 *       scope on this specific resource" rather than baking the rule into its own code.</li>
 * </ul>
 *
 * <p>Invoice data is an in-memory stand-in (no database) -- this app's subject is the
 * security boundary in front of the API, not the billing domain behind it.
 */
@RestController
@RequestMapping("/api")
public class BillingController {

    private final List<Map<String, Object>> invoices = new java.util.concurrent.CopyOnWriteArrayList<>(List.of(
            Map.of("id", 1001, "customer", "Acme Robotics", "amountUsd", 4200.00, "status", "paid"),
            Map.of("id", 1002, "customer", "Blue Harbor Logistics", "amountUsd", 980.50, "status", "overdue")
    ));
    private final AtomicLong nextId = new AtomicLong(1003);
    private final UmaPermissionService umaPermissionService;

    public BillingController(UmaPermissionService umaPermissionService) {
        this.umaPermissionService = umaPermissionService;
    }

    @GetMapping("/public/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "up",
                "service", "northwind-apps-billing-api",
                "time", Instant.now().toString()
        ));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Map<String, Object>> listInvoices(JwtAuthenticationToken auth) {
        Jwt jwt = (Jwt) auth.getCredentials();
        return ResponseEntity.ok(Map.of(
                "invoices", invoices,
                "requestedBy", jwt.getClaimAsString("preferred_username") != null
                        ? jwt.getClaimAsString("preferred_username")
                        : jwt.getSubject(),
                "callerAuthorities", auth.getAuthorities().stream().map(Object::toString).toList()
        ));
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasRole('billing-admin')")
    public ResponseEntity<Map<String, Object>> createInvoice(@RequestBody Map<String, Object> body) {
        long id = nextId.getAndIncrement();
        Map<String, Object> invoice = Map.of(
                "id", id,
                "customer", body.getOrDefault("customer", "Unknown"),
                "amountUsd", body.getOrDefault("amountUsd", 0.0),
                "status", "draft"
        );
        invoices.add(invoice);
        return ResponseEntity.status(201).body(invoice);
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Map<String, Object>> getInvoice(@PathVariable long id) {
        return invoices.stream()
                .filter(inv -> ((Number) inv.get("id")).longValue() == id)
                .findFirst()
                .<ResponseEntity<Map<String, Object>>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Any authenticated caller reaches this method (the filter chain only requires a valid
     * token, see {@link SecurityConfig}) -- the real access decision happens inside
     * {@link UmaPermissionService}, against Keycloak's own UMA 2.0 resource/scope/policy/
     * permission model (the {@code premium-invoice-report} resource, {@code view} scope,
     * {@code billing-admin-policy}), not against anything hardcoded in this method.
     */
    @GetMapping("/invoices/premium-report")
    public ResponseEntity<Map<String, Object>> premiumReport(
            @org.springframework.web.bind.annotation.RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String callerToken = authorizationHeader.replaceFirst("(?i)^Bearer ", "");
        boolean permitted = umaPermissionService.callerMayViewPremiumReport(callerToken);
        if (!permitted) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "access_denied",
                    "detail", "Keycloak's UMA policy for the premium-invoice-report resource denied this caller."
            ));
        }
        double total = invoices.stream().mapToDouble(inv -> ((Number) inv.get("amountUsd")).doubleValue()).sum();
        return ResponseEntity.ok(Map.of(
                "report", "premium-invoice-analytics",
                "totalOutstandingAndPaidUsd", total,
                "invoiceCount", invoices.size(),
                "grantedVia", "UMA 2.0 RPT from Keycloak Authorization Services"
        ));
    }
}
