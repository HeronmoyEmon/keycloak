package com.riversidehealth.keycloak.legacyhr;

/**
 * One row of Riverside Health's legacy HR staff directory, exactly as the real proprietary system
 * would hand it over (minus everything this toy lab doesn't need).
 *
 * <p>The password field is intentionally plaintext in the backing CSV, not hashed. That is a
 * deliberately honest stand-in for a real-world failure mode this chapter calls out directly: a
 * 20-year-old internal HR system built long before argon2/bcrypt were standard practice, storing
 * plaintext or weakly-reversible credentials, that a hospital cannot simply "turn off" overnight.
 * A custom User Storage SPI provider is how Keycloak sits in front of exactly that kind of legacy
 * store without being able to fix its internals — see the chapter's pitfall callout on this.</p>
 */
public record StaffRecord(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String employeeId,
        String department) {
}
