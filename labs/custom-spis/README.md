# Custom SPIs lab (chapter 39)

Three real, independent Keycloak SPI providers, one per case-study org, built as one Maven project
producing one jar (see `pom.xml`'s own comment, and the chapter text, for why one project rather than
three):

- **Meridian Trust** — a custom **Authenticator** (`meridian-business-hours`): denies login entirely
  outside a configured window of hours (UTC, real server clock).
- **Riverside Health** — a custom **EventListenerProvider** (`riverside-failed-login-audit`): logs
  every real `LOGIN_ERROR` event for a realm it's enabled on, in a distinguishable format.
- **Northwind Apps** — a custom **ProtocolMapper** (`northwind-tenant-id-mapper`): injects a
  `tenant_id` claim into the access token, ID token, and UserInfo response, sourced from the user's
  own `tenant_id` attribute.

See chapter 39 (`chapters/39-custom-spis.html`) for the full walkthrough, the live SPI-interface
verification trail, and every real command/output this lab actually produced — including the real
bugs hit along the way (a POM XML-comment `--` parse error, a bind-mounted-JAR-goes-stale-after-
`mvn clean` gotcha distinct from chapter 33's "becomes a directory" gotcha, an uncaught
`AuthenticationFlowException`/HTTP 500 from the single-argument `context.failure()` overload on the
Direct Grant code path, a "Account is not fully set up" red herring unrelated to the authenticator,
and a "Lightweight access token not allowed for userinfo endpoint" surprise from `admin-cli`'s
default client settings).

## Build

```
cd labs/custom-spis
mvn clean package
```

Produces `target/custom-spis-1.0.0.jar`. Build it **before** bringing the stack up, and if you ever
rebuild while the stack is already running, recreate the Keycloak container rather than just
restarting it — see the chapter's "common mistakes" section for why a plain `restart` silently keeps
serving the old jar.

## Run

```
docker compose -p ch39kc up -d
# wait for Keycloak to answer on http://localhost:18139, then:
docker compose -p ch39kc exec keycloak /opt/keycloak/bin/kc.sh build
docker compose -p ch39kc restart keycloak
```

Keycloak is published on host port **18139**, isolated from the shared `labs/docker/` stack and
every other chapter's own stack. Admin console: `http://localhost:18139` (admin /
`admin_lab_password`).

If you rebuild the jar while the stack is already up:

```
mvn clean package
docker compose -p ch39kc up -d --force-recreate keycloak   # NOT just `restart` -- see the chapter text
docker compose -p ch39kc exec keycloak /opt/keycloak/bin/kc.sh build
docker compose -p ch39kc restart keycloak
```

## What the chapter's lab actually configures, per org

- **`meridian-trust` realm**: a copy of the built-in "direct grant" flow (`meridian direct grant`)
  with `meridian-business-hours` added as a `REQUIRED` execution, bound as the realm's
  `directGrantFlow`. Its `ProviderConfigProperty` window (`startHourUtc`/`endHourUtc`, default
  9-17 UTC) is set per-execution via an `AuthenticatorConfigModel`.
- **`riverside-health` realm**: `eventsEnabled: true`, `eventsListeners` includes
  `riverside-failed-login-audit`. A real failed login (wrong password) produces a
  `RIVERSIDE-HIPAA-LOGIN-AUDIT | ...` line in `docker compose -p ch39kc logs keycloak`.
- **`northwind-apps` realm**: `unmanagedAttributePolicy: ENABLED` on the realm's User Profile (same
  gotcha chapters 22/24/33 already hit), a user with a real `tenant_id` attribute, and the
  `northwind-tenant-id-mapper` protocol mapper attached directly to the `admin-cli` client (with its
  default "Always use lightweight access token" setting turned off — see the chapter's common
  mistakes section for why that mattered for the UserInfo call specifically).

## Tear down

```
docker compose -p ch39kc down -v
```
