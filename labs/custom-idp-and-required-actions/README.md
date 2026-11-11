# Custom Identity Provider + Required Action SPI lab (chapter 40)

Two independent, real, Maven-compiled Keycloak SPI providers in one jar:

1. **`MeridianLegacySsoIdentityProvider`** — a custom Identity Provider that brokers logins against a
   toy stand-in for "Cascade SSO," a fictional legacy, proprietary single-sign-on system belonging to
   Cascade Savings Bank, a smaller institution Meridian Trust has just acquired. This is a teaching
   stand-in protocol, not a real named vendor product.
2. **`RiversideHipaaAcknowledgmentRequiredAction`** — a custom Required Action that forces a one-time
   HIPAA data-handling acknowledgment onto a Riverside Health clinician's session before it can
   complete, blocking login until the user clicks "I Acknowledge."

See chapter 40 (`chapters/40-custom-idp-and-required-actions.html`) for the full walkthrough, the live
Maven-coordinate verification trail (`javap` against the real jars inside `quay.io/keycloak/keycloak:26.8.2`),
and the complete real request-by-request trace this lab actually produced — including every real
gotcha hit along the way (see "Gotchas actually hit" below).

## Build

```
cd labs/custom-idp-and-required-actions
mvn clean package
```

Produces `target/custom-idp-and-required-action-spi-1.0.0.jar`, containing both providers plus the
`theme-resources/templates/riverside-hipaa-consent.ftl` page the required action renders. Build it
**before** bringing the stack up — the compose file's bind mount fails silently (an empty/missing file
inside the container) if the jar doesn't exist yet at that host path.

## Run

```
docker compose -p ch40kc up -d
# wait for Keycloak to answer on http://localhost:18140
```

Keycloak is published on host port **18140**, isolated from the shared `labs/docker/` stack and every
other chapter's own stack (chapter 33 uses `ch33kc`/18133, chapter 39 uses `ch39kc`/18139). Admin
console: `http://localhost:18140` (`admin` / `admin_lab_password`).

Start the toy legacy IdP as a plain host process (not in Docker — see "Why the toy IdP isn't in
Docker" below):

```
python3 toy-legacy-idp/legacy_sso_server.py --port 18141 --secret ch40-shared-secret-abc123
```

## Component A: the custom Identity Provider — Meridian Trust / Cascade SSO

Register it on a realm via the Admin REST API (`providerId: "meridian-legacy-sso"`):

```
curl -X POST "http://localhost:18140/admin/realms/<realm>/identity-provider/instances" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "alias": "cascade-legacy-sso",
    "displayName": "Cascade Savings Bank (legacy SSO)",
    "providerId": "meridian-legacy-sso",
    "enabled": true,
    "trustEmail": true,
    "firstBrokerLoginFlowAlias": "first broker login",
    "config": {
      "legacyBaseUrl": "http://localhost:18141",
      "legacyClientId": "meridian-trust-broker",
      "sharedSecret": "ch40-shared-secret-abc123"
    }
  }'
```

Toy Cascade SSO user table (`toy-legacy-idp/legacy_sso_server.py`): `jmartinez` / `CascadeP@ss1`,
`rwong` / `CascadeP@ss2`.

Drive a real browser-equivalent round trip with `kc_idp_hint` to skip the login picker
(`GET /realms/<realm>/protocol/openid-connect/auth?...&kc_idp_hint=cascade-legacy-sso`), following
each `Location` header by hand with a cookie jar (`curl -c/-b`) exactly like chapter 31's lab did. The
chapter text has the full six-hop trace; the short version:

```
Keycloak auth endpoint
  -> Keycloak broker/login kickoff (kc_idp_hint resolved)
  -> 303 to the toy IdP's GET /legacy-sso/authorize   (performLogin() ran)
  -> toy IdP's real login form, POST real credentials
  -> 302 back to Keycloak's broker endpoint with legacy_user/legacy_email/state/ts/sig
     (Endpoint.callback() verified the HMAC + state, then callback.authenticated(...))
  -> Keycloak's First Broker Login -> after-first-broker-login -> app redirect_uri with a real code
  -> POST to the token endpoint -> real access_token/id_token, decoded claims match the Cascade user
```

Verify JIT provisioning and provenance:

```
curl "http://localhost:18140/admin/realms/<realm>/users/<id>/federated-identity" -H "Authorization: Bearer $TOKEN"
# -> [{"identityProvider":"cascade-legacy-sso","userId":"jmartinez","userName":"jmartinez"}]
```

### Why the toy IdP isn't in Docker

Every redirect in this round trip is followed by the **browser** (curl, standing in for one here),
never by the Keycloak container reaching the toy IdP server-to-server. `performLogin()` only builds a
`Location` header string — it makes no outbound network call. So the toy IdP only ever needs to be
reachable from whatever machine is driving the curl commands (the host), and no
`extra_hosts`/`host.docker.internal` container-to-host bridging is needed at all.

## Component B: the custom Required Action — Riverside Health HIPAA acknowledgment

Custom required actions are **not** auto-enabled per realm just because the provider jar is deployed
— register it explicitly on each realm that should offer it (see "Gotchas actually hit" below):

```
curl -X POST "http://localhost:18140/admin/realms/<realm>/authentication/register-required-action" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"providerId":"riverside-hipaa-ack","name":"Riverside Health – HIPAA Data Handling Acknowledgment"}'
```

Then assign it to a specific user (the same per-user pattern chapter 22 used for `priya.patel`'s
`CONFIGURE_TOTP`), log that user in for real, and confirm the session stops at the consent page instead
of reaching the app's `redirect_uri`:

```
curl -X PUT "http://localhost:18140/admin/realms/<realm>/users/<id>" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"requiredActions": ["riverside-hipaa-ack"]}'
```

A correct password redirects to `/login-actions/required-action?execution=riverside-hipaa-ack&...`
instead of the app — login has **not** completed yet. Submitting the decline button
(`cancel=Decline`) ends the attempt with a hard `400` error page; submitting accept
(`submitAction=I Acknowledge`) redirects on to the app's `redirect_uri` with a real authorization code,
and the user's `requiredActions` list is empty afterward (Keycloak clears a completed one-time action
automatically).

## Gotchas actually hit (all real, all reproduced below)

1. **XML comment parse error in the POM** — the same one chapter 33 hit: a bare `--` inside an XML
   comment (used here while explaining the dependency trail in prose) breaks `mvn` with
   `Non-parseable POM ... in comment after two dashes (--) next character must be > not`. Fixed by
   replacing `--` with an em dash in comment prose.
2. **`jakarta.ws.rs-api` is not pulled in transitively.** The initial POM assumed Maven would resolve
   it transitively from `keycloak-server-spi-private`'s own POM (as scope `provided`, following the
   provided+compile=provided mediation rule) — `mvn clean package` failed with
   `package jakarta.ws.rs does not exist` until it was added as an explicit dependency, pinned to
   `3.1.0` (the exact version bundled in the running 26.8.2 image).
3. **`org.keycloak.Config`/`Config.Scope` (needed by `RequiredActionFactory.init()`) lives in
   `keycloak-core`, not either `keycloak-server-spi*` artifact.** Confirmed by grepping the real jars;
   added `keycloak-core` as a third `provided` dependency.
4. **Declarative User Profile's `unmanagedAttributePolicy` silently drops custom attributes** —
   the exact gotcha chapter 33 already flagged, reproduced independently here for a completely
   different provider: `MeridianLegacySsoIdentityProvider.importNewUser()` calls
   `user.setSingleAttribute("legacySource", "cascade-sso")` with no error, but a `GET` of the
   JIT-provisioned user shows no `attributes` field at all until the realm's user profile has
   `unmanagedAttributePolicy` set to `ENABLED` (`PUT /admin/realms/<realm>/users/profile`). Reproduced
   twice: `jmartinez` (provisioned before the fix) has no `legacySource` attribute; `rwong`
   (provisioned after `PUT .../users/profile` with `unmanagedAttributePolicy: "ENABLED"`) has it.
5. **A custom Required Action is not auto-registered on any realm just because the provider jar is
   deployed.** Unlike the Identity Provider SPI (which shows up directly in the "Add provider" picker
   once discovered), a freshly discovered custom Required Action sits in
   `GET /admin/realms/<realm>/authentication/unregistered-required-actions` until explicitly
   `POST`ed to `.../authentication/register-required-action` on *each* realm that should offer it —
   setting a user's `requiredActions` to include its provider ID silently does nothing until that
   registration step has run. This is a genuinely different gotcha from anything chapter 33 hit, since
   chapter 33's SPI type (User Storage) has no equivalent per-realm "is this provider turned on here"
   switch.
6. **`context.failure(messageCode)`'s `messageCode` argument is a realm message-bundle key, not display
   text.** Declining the HIPAA acknowledgment calls `context.failure("riversideHipaaAckDeclined")`, but
   since no realm message bundle defines that key, the reader sees Keycloak's generic
   `data-page-id="login-error"` / "An error occurred, please login again through your application"
   page rather than anything HIPAA-specific — the call still correctly blocks the session, it just
   doesn't customize the error text without an accompanying `messages_en.properties` entry (left out of
   this lab deliberately, to keep the one new mechanism being taught — a required action that can
   fail the session — separate from realm message-bundle customization, a different topic).

## Tear down

```
docker compose -p ch40kc down -v
```

And stop the toy legacy IdP process (it was started in the foreground/background on the host, not in
Docker — `kill` whatever PID `python3 toy-legacy-idp/legacy_sso_server.py` printed, or
`pkill -f legacy_sso_server.py`).
