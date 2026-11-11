package com.riversidehealth.keycloak.legacyhr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A tiny read-only, in-memory stand-in for Riverside Health's real legacy HR system's own data
 * access layer.
 *
 * <p>In real life this class's five methods (or something very like them) would instead issue a
 * query against that system's own proprietary database or API. The point of this lab is the
 * Keycloak-facing SPI code built on top, not the toy store underneath it — a flat CSV bundled as a
 * classpath resource is the simplest thing that is still a genuinely separate, external user
 * store Keycloak itself does not understand natively (unlike LDAP, which chapter 32 covered with
 * Keycloak's own built-in federation provider, requiring no custom code at all).</p>
 */
public final class LegacyStaffDirectoryStore {

    private final Map<String, StaffRecord> byUsername;
    private final Map<String, StaffRecord> byEmail;

    private LegacyStaffDirectoryStore(Map<String, StaffRecord> byUsername, Map<String, StaffRecord> byEmail) {
        this.byUsername = byUsername;
        this.byEmail = byEmail;
    }

    /**
     * Loads every row of the bundled CSV once. Called exactly once, from the provider factory's
     * {@code init()}, and the resulting store is shared read-only across every provider instance
     * {@code create()} hands back for the lifetime of this deployment — there is no per-request
     * re-parsing.
     */
    public static LegacyStaffDirectoryStore loadFromClasspath(String resourcePath) {
        Map<String, StaffRecord> byUsername = new HashMap<>();
        Map<String, StaffRecord> byEmail = new HashMap<>();

        try (InputStream in = LegacyStaffDirectoryStore.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Bundled legacy staff directory CSV not found on the classpath at " + resourcePath
                                + " -- check that it's under src/main/resources and the JAR actually contains it.");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // header row, discarded
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] fields = line.split(",", -1);
                    if (fields.length != 7) {
                        throw new IllegalStateException("Malformed row in " + resourcePath + ": " + line);
                    }
                    StaffRecord record = new StaffRecord(
                            fields[0].trim(),  // username
                            fields[1].trim(),  // password (plaintext in this toy store -- see StaffRecord's javadoc)
                            fields[2].trim(),  // firstName
                            fields[3].trim(),  // lastName
                            fields[4].trim(),  // email
                            fields[5].trim(),  // employeeId
                            fields[6].trim()); // department
                    byUsername.put(record.username().toLowerCase(), record);
                    byEmail.put(record.email().toLowerCase(), record);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading bundled legacy staff directory CSV at " + resourcePath, e);
        }

        return new LegacyStaffDirectoryStore(Map.copyOf(byUsername), Map.copyOf(byEmail));
    }

    public StaffRecord findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return byUsername.get(username.toLowerCase());
    }

    public StaffRecord findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return byEmail.get(email.toLowerCase());
    }

    public int size() {
        return byUsername.size();
    }
}
