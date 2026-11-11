package com.meridiantrust.keycloak.legacysso;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.HexFormat;

/**
 * Shared HMAC-SHA256 signing/verification helper for the toy "Cascade Legacy SSO" protocol.
 *
 * <p>This is deliberately the ONLY cryptographic ingredient the toy protocol uses: a shared secret,
 * known to both this Keycloak provider and the toy legacy IdP server
 * ({@code toy-legacy-idp/legacy_sso_server.py}), used to sign the handful of fields the legacy IdP
 * hands back on its callback redirect ({@code legacy_user}, {@code legacy_email}, {@code state},
 * {@code ts}). Without this signature, anything could forge a "successful legacy login" callback by
 * simply crafting the right query string and sending the browser's own request straight at Keycloak's
 * broker endpoint &mdash; the whole point of brokering trust (chapter 7's IdP/SP trust triangle) is
 * that the relying side only accepts an assertion it can verify came from the trusted IdP, not merely
 * one that *looks* right.</p>
 */
final class LegacySsoUtil {

    private LegacySsoUtil() {
    }

    static String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }

    /** Constant-time comparison so a timing side-channel can't leak the expected signature. */
    static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
