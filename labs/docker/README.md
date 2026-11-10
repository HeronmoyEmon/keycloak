# Base lab environment: Keycloak + PostgreSQL

Used by every Part IV+ chapter's hands-on labs. Confirmed working end-to-end (2026-11-10): Docker Compose
stack boots, Keycloak 26.8.2 reaches ready state, `kcadm.sh` logs in with the bootstrap admin and can
create realms/users/roles/clients.

## Start

```bash
cd labs/docker
docker compose up -d
```

Wait about 15-20 seconds for Keycloak to finish booting (Postgres has a healthcheck Keycloak waits on,
but Keycloak's own startup still takes a few seconds after that). Confirm it's up:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/realms/master/.well-known/openid-configuration
# 200 once ready
```

## Access

- Admin Console: http://localhost:8080/admin — bootstrap admin `admin` / `admin_lab_password`
- Admin REST API / `kcadm.sh` base URL: http://localhost:8080
- Management interface (health/metrics, chapter 28's subject): http://localhost:9000 — `/health`,
  `/health/ready`, `/health/live`, `/metrics`. Served on a separate port from the Admin Console/REST
  API; the identical paths 404 on port 8080. `KC_HEALTH_ENABLED`/`KC_METRICS_ENABLED` are both set in
  `docker-compose.yml` and port 9000 is published, so this works out of the box as of 2026-11-10 — an
  earlier version of this compose file set `KC_HEALTH_ENABLED` but never published port 9000, which
  chapter 28's lab caught and this file's own fix corrected.

## Using `kcadm.sh` (the scriptable path chapters use to verify lab steps)

```bash
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password admin_lab_password
```

Then run any `kcadm.sh` command, e.g.:

```bash
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh get realms --fields realm,enabled
```

Full `kcadm.sh` usage is chapter 27's subject — chapters 20-26 use it only as a way to confirm a step
actually worked, alongside the Admin Console UI steps that are each chapter's main content.

## Stop

```bash
docker compose down        # keep data (the Postgres volume persists)
docker compose down -v     # also delete the Postgres volume — a clean slate for the next chapter/lab
```

## Credentials note

`admin_lab_password` and `keycloak_lab_password` are deliberately simple, visible-in-this-repo
development credentials for a local, throwaway lab instance — never use `start-dev` or these credentials
for anything reachable from outside your own machine. Chapter 20 covers this distinction (dev mode vs.
production mode) explicitly.

## Realm used across chapters 20-26

Chapter 20 creates a `northwind-apps` realm, used as the running example for every chapter through 26 —
see `BUILD_STATUS.md`'s Part IV planning notes for the exact users/roles/groups/clients each chapter adds.
Each chapter's own lab starts from a fresh `docker compose up` (not a shared running instance across
chapters) and recreates just enough prior state to make sense standalone, while narrating continuity with
earlier chapters.
