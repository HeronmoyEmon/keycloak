# The capstone: Northwind Apps, built end to end

A real, running, multi-app Keycloak-secured system — not narrated — assembling pieces this course
already proved independently (the `northwind-apps` realm from chapters 20-26, identity concepts from
chapter 2, UMA 2.0 authorization from chapter 37, the `billing-api` Spring Boot resource server from
chapter 34, the `TenantIdProtocolMapper` custom SPI from chapter 39) into one integrated stack. See
`capstone.html` at the repo root for the narrative write-up, architecture diagram, and interview talking
points; this file is the exact, reproducible command sequence.

## What's here

| Path | What it is |
|---|---|
| `docker-compose.yml` | Isolated Keycloak 26.8.2 + PostgreSQL 17 stack (project `capstonekc`, host port `18200`) |
| `seed-realm.sh` | Reconstructs the full `northwind-apps` realm — exactly what was run, in order, including the fixes for two real bugs hit live (see "Live-verified findings" below) |
| `billing-api/` | Adapted from chapter 34's `labs/spring-boot-resource-server` — same role-protected endpoints, plus one new endpoint gated by a real server-side UMA 2.0 check instead of a role |
| `dashboard-spa/` | Adapted from chapter 35's `labs/react-spa` — the real build of `northwind-spa`, the public PKCE client chapter 24 only named |
| `m2m-billing-job.sh` | A real client-credentials job using `billing-service-account`, standing in for chapter 36's nightly reconciliation job |

## 1. Start the stack

```bash
cd capstone
mvn -q -f ../labs/custom-spis/pom.xml clean package   # rebuild the custom SPI jar fresh
docker compose -p capstonekc up -d
```

Wait for Keycloak to be ready:

```bash
for i in $(seq 1 40); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18200/realms/master/.well-known/openid-configuration)
  echo "attempt $i: $code"
  if [ "$code" = "200" ]; then break; fi
  sleep 3
done
```

Admin Console: http://localhost:18200/admin — bootstrap admin `admin` / `admin_lab_password`.

## 2. Seed the realm

```bash
./seed-realm.sh
```

This prints each client's secret and the `premium-invoice-report` resource id at the end — copy them,
you need them for steps 3-5. **These are fresh per run; never commit real values from your own run.**

## 3. Build and run the billing API

```bash
cd billing-api
mvn -q clean package -DskipTests
BILLING_API_CLIENT_SECRET='<paste the billing-api secret from step 2>' \
PREMIUM_REPORT_RESOURCE_ID='<paste the resource id from step 2>' \
java -jar target/billing-api-1.0.0.jar
```

It listens on `http://localhost:8081`. Confirm: `curl http://localhost:8081/api/public/health`.

## 4. Build and run the dashboard SPA

```bash
cd dashboard-spa
npm install
npm run dev -- --port 5173
```

Open http://localhost:5173/ in a browser, sign in as `jane.doe` / `CapstoneLab1!` (or any of the users
seed-realm.sh created), and use the two buttons to call the billing API with the real access token
`react-oidc-context` holds.

## 5. Run the M2M job

```bash
BILLING_SERVICE_ACCOUNT_SECRET='<paste the billing-service-account secret from step 2>' \
./m2m-billing-job.sh
```

## Users seed-realm.sh creates (password `CapstoneLab1!` for all)

| Username | Realm role(s) | Group | tenant_id |
|---|---|---|---|
| `jane.doe` | `tenant-admin` | — | `acme-corp` |
| `priya.patel` | `platform-engineer` | Engineering | `acme-corp` |
| `amara.reader` | `billing-reader` | — | `acme-corp` |
| `devon.admin` | `billing-admin` | — | `acme-corp` |
| `priya.sales` | (none) | — | `contoso` |

## End-to-end verification actually run against this stack (2026-11-12)

Every result below is real output from this capstone's own running stack, not a plausible-looking
reconstruction — the coordinator independently re-ran a subset of these against the same live stack
before accepting this unit.

**UMA 2.0 round trip, direct against Keycloak** (mirrors chapter 37's proven wire sequence):
devon.admin (`billing-admin`) requesting a ticket for `premium-invoice-report`/`view` → UMA exchange
→ **`200`** with a real RPT. amara.reader (`billing-reader` only) → same exchange → **`403`**.

**Through the real running `billing-api`** (role-based endpoints + the new UMA-gated one):

| Caller | Request | Result |
|---|---|---|
| amara.reader (billing-reader) | `GET /api/invoices` | `200` |
| amara.reader | `POST /api/invoices` | `403` |
| devon.admin (billing-admin) | `POST /api/invoices` | `201` |
| amara.reader | `GET /api/invoices/premium-report` | `403` — real UMA denial, body: `{"error":"access_denied","detail":"Keycloak's UMA policy for the premium-invoice-report resource denied this caller."}` |
| devon.admin | `GET /api/invoices/premium-report` | `200` — body: `{"report":"premium-invoice-analytics","totalOutstandingAndPaidUsd":5735.75,"invoiceCount":3,"grantedVia":"UMA 2.0 RPT from Keycloak Authorization Services"}` |

**M2M job** (`billing-service-account`, client credentials, service account holds `billing-admin`):
`GET /api/invoices` → `200`; `GET /api/invoices/premium-report` → `200` (service account holds
`billing-admin`, so this is a real UMA-permitted call, not a bypass).

**Real authorization-code + PKCE login**, scripted end to end against `northwind-spa` (reusing the
scripted-login-form-POST technique chapter 13's lab proved works in this environment — no fallback to
Direct Access Grant was needed for this step): a real `/authorize` request with a real S256 PKCE
challenge → a real login form POST for `jane.doe` → a real `302` carrying a real authorization code →
a real `/token` exchange with the matching `code_verifier` → a real access token whose decoded payload
carries `"tenant_id": "acme-corp"` (the custom SPI's claim, correctly riding on this token because
`northwind-spa` has the `tenant-context` client scope attached) alongside `"realm_access":
{"roles":["tenant-admin", ...]}`. `billing-direct-grant` (Resource Owner Password Credentials) was used
only as the verification shortcut for the role-based billing-api/UMA/M2M checks above — clearly a
test-only client chapter 34 itself already established, never used by `northwind-spa` or
`northwind-dashboard`.

## Live-verified findings (real bugs hit and fixed while building this)

1. **A resource URI with a path-segment wildcard is rejected.** The first attempt registered
   `premium-invoice-report`'s URI as `/api/invoices/*/premium-report` (meant to match any invoice id) —
   Keycloak rejected it outright: `"URI [...] is not a valid template: wildcard '*' is only supported as
   trailing '/*' or as a suffix pattern '/*.ext' [invalid_request]"`. Fixed by making the endpoint a
   fixed path, `/api/invoices/premium-report` (an aggregate report, not per-invoice) — arguably the
   better design anyway.
2. **Keycloak 26's declarative User Profile silently drops undeclared user attributes — no error, no
   warning.** `seed-realm.sh`'s first pass set `tenant_id` on every user via `kcadm.sh update users/<id>
   -s 'attributes.tenant_id=["acme-corp"]'`; the command reported success, but the attribute never
   appeared on a subsequent `GET`, and the custom protocol mapper (which reads that exact attribute)
   never added the claim to any token. Root cause: the realm's User Profile schema (`GET
   /admin/realms/<realm>/users/profile`) only persists attributes it explicitly declares, and
   `tenant_id` wasn't one of them — this is default behavior in 26.x, not a misconfiguration. Fixed by
   adding `tenant_id` to the User Profile schema first (admin-only view/edit, not user-editable) before
   setting it on any user. A related, smaller finding on the way to diagnosing this: a `PUT
   /admin/realms/<realm>/users/<id>` with a partial body **replaces** the user representation rather
   than merging it — a bare `{"attributes": {...}}` body silently wiped `firstName`/`lastName`/`email`
   on the one user tested this way directly, before the cause above was found and the fix was redone
   through `kcadm.sh update` (which does its own get-merge-put round trip) instead.

## Tear down

```bash
docker compose -p capstonekc down -v
```
