# Chapter 37 lab environment: Keycloak + PostgreSQL, isolated for Authorization Services

Used by chapter 37's (Keycloak Authorization Services) hands-on lab. A self-contained stack, independent
of `labs/docker/docker-compose.yml` (that one has no Authorization Services setup and is used by chapters
20-30+). Confirmed working end-to-end (2026-11-11), re-run fresh at the end of authoring this chapter to
confirm reproducibility: a real confidential client became a resource server, a resource + authorization
scope + role policy + scope-based permission were created for real via the Admin REST API, and the full
UMA 2.0 flow ran for real — a real permission ticket from the Protection API, a real RPT for a user holding
the required role (200 OK), and a real denial for a user who doesn't (403 Forbidden).

## Start

```bash
cd labs/authorization-services
docker compose -p ch37kc up -d
```

Wait for Keycloak to be ready:

```bash
for i in $(seq 1 40); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18137/realms/master/.well-known/openid-configuration)
  echo "attempt $i: $code"
  if [ "$code" = "200" ]; then break; fi
  sleep 3
done
```

This compose file uses project name **`ch37kc`** and host port **18137** (Keycloak only — no second
service port is needed here), so it never collides with another chapter's own isolated stack or with the
shared `labs/docker/docker-compose.yml` used by chapters 20-30+.

## Access

- Admin Console: http://localhost:18137/admin — bootstrap admin `admin` / `admin_lab_password`

## Reproducing the lab exactly

All commands below are exactly what this chapter's own lab ran, in order. Substitute the IDs you get back
from your own run — Keycloak assigns fresh UUIDs every time.

```bash
# 1. Authenticate kcadm and create the realm
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password admin_lab_password
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create realms -s realm=northwind-apps -s enabled=true

# 2. Create the resource-server client
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create clients -r northwind-apps \
  -s clientId=northwind-docs-api -s protocol=openid-connect \
  -s publicClient=false -s standardFlowEnabled=false \
  -s serviceAccountsEnabled=true -s directAccessGrantsEnabled=true \
  -s authorizationServicesEnabled=true
CID=$(docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh get clients -r northwind-apps \
  -q clientId=northwind-docs-api --fields id --format csv --noquotes)
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh get "clients/$CID/client-secret" -r northwind-apps

# 3. Role + two users (one gets the role, one deliberately doesn't)
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create roles -r northwind-apps \
  -s name=northwind-editor -s 'description=Can edit shared Northwind documents'
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create users -r northwind-apps \
  -s username=amara.diallo -s enabled=true -s firstName=Amara -s lastName=Diallo
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create users -r northwind-apps \
  -s username=sam.chen -s enabled=true -s firstName=Sam -s lastName=Chen
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh set-password -r northwind-apps \
  --username amara.diallo --new-password 'LabPassw0rd!' --temporary=false
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh set-password -r northwind-apps \
  --username sam.chen --new-password 'LabPassw0rd!' --temporary=false
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh add-roles -r northwind-apps \
  --uusername amara.diallo --rolename northwind-editor

# 4. Resource + authorization scope
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create \
  "clients/$CID/authz/resource-server/resource" -r northwind-apps \
  -s name="quarterly-report" -s displayName="Northwind Q3 report (shared doc)" \
  -s 'uris=["/documents/quarterly-report"]' -s type=urn:northwind:document
RID=$(docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh get \
  "clients/$CID/authz/resource-server/resource" -r northwind-apps -q name=quarterly-report \
  --fields _id --format csv --noquotes)
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create \
  "clients/$CID/authz/resource-server/scope" -r northwind-apps -s name=view
SID=$(docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh get \
  "clients/$CID/authz/resource-server/scope" -r northwind-apps -q name=view --fields id --format csv --noquotes)

# GOTCHA, confirmed live: a permission can't reference this resource+scope pair until the resource
# itself lists the scope. Do this BEFORE creating the permission, or it fails with
# "Scope [view] is not associated with any of the resources set to the permission".
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh update \
  "clients/$CID/authz/resource-server/resource/$RID" -r northwind-apps \
  -s name="quarterly-report" -s type=urn:northwind:document -s 'uris=["/documents/quarterly-report"]' \
  -s "scopes=[{\"id\":\"$SID\"}]"

# 5. Role policy + scope-based permission
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create \
  "clients/$CID/authz/resource-server/policy/role" -r northwind-apps \
  -s name="northwind-editor-policy" -s 'roles=[{"id":"northwind-editor"}]' -s logic=POSITIVE
PID=$(docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh get \
  "clients/$CID/authz/resource-server/policy" -r northwind-apps -q name=northwind-editor-policy \
  --fields id --format csv --noquotes)
docker compose -p ch37kc exec keycloak /opt/keycloak/bin/kcadm.sh create \
  "clients/$CID/authz/resource-server/permission/scope" -r northwind-apps \
  -s name="view-quarterly-report" -s "resources=[\"$RID\"]" -s "scopes=[\"$SID\"]" \
  -s "policies=[\"$PID\"]" -s decisionStrategy=UNANIMOUS

# 6. The UMA flow itself
SECRET='<paste the client secret from step 2>'

RS_TOKEN=$(curl -s -X POST "http://localhost:18137/realms/northwind-apps/protocol/openid-connect/token" \
  --data-urlencode "grant_type=client_credentials" --data-urlencode "client_id=northwind-docs-api" \
  --data-urlencode "client_secret=$SECRET" | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

TICKET=$(curl -s -X POST "http://localhost:18137/realms/northwind-apps/authz/protection/permission" \
  -H "Authorization: Bearer $RS_TOKEN" -H "Content-Type: application/json" \
  -d "[{\"resource_id\":\"$RID\",\"resource_scopes\":[\"view\"]}]" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['ticket'])")

AMARA_TOKEN=$(curl -s -X POST "http://localhost:18137/realms/northwind-apps/protocol/openid-connect/token" \
  --data-urlencode "grant_type=password" --data-urlencode "client_id=northwind-docs-api" \
  --data-urlencode "client_secret=$SECRET" --data-urlencode "username=amara.diallo" \
  --data-urlencode "password=LabPassw0rd!" | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

# Permitted — expect 200 with a real RPT
curl -s -i -X POST "http://localhost:18137/realms/northwind-apps/protocol/openid-connect/token" \
  -H "Authorization: Bearer $AMARA_TOKEN" \
  --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
  --data-urlencode "ticket=$TICKET" --data-urlencode "client_id=northwind-docs-api" \
  --data-urlencode "client_secret=$SECRET"

# Repeat the ticket + token steps for sam.chen (no role) — expect 403 access_denied
```

## Live-verified findings from this run (2026-11-11, Keycloak 26.8.2)

- **Resource must carry its scope before a scope-based permission can reference it.** Creating a resource
  and a scope independently does not link them — confirmed by a real `invalid_request` error on the first
  attempt, fixed by updating the resource's own `scopes` array.
- **JavaScript ("script") policies do not exist in this version's REST API at all.** The live policy-provider
  list (`GET .../authz/resource-server/policy/providers`) returns only `regex, role, resource, scope,
  client, time, user, client-scope, group, aggregate` — no `js`/`script` entry. A direct attempt to create
  one via `.../policy/js` returned a flat `404 Resource not found for url`, not a feature-flag rejection.
- **The UMA ticket-exchange wire format**, confirmed live: `POST /realms/<realm>/protocol/openid-connect/token`
  with `grant_type=urn:ietf:params:oauth:grant-type:uma-ticket`, `ticket=<ticket>`, `client_id`/`client_secret`
  for the resource-server client, and the **requesting party's own access token** passed as the
  `Authorization: Bearer` header — that header is how Keycloak learns who is actually asking.
- **Decision strategy genuinely changes outcomes.** With a second (trivially-true) client policy added to
  the permission, the identical request from a role-less user returned 403 under `UNANIMOUS` and 200 under
  `AFFIRMATIVE` — reverted back to a single role policy under `UNANIMOUS` afterward as the actual
  least-privilege baseline.
- **A denied UMA exchange returns `403 {"error":"access_denied","error_description":"request_submitted"}`**
  even when the resource is not owner-managed (`ownerManagedAccess: false`) and nothing is actually pending
  anywhere for a human to approve — the description text does not appear to carry a stable, documented
  contract; application error handling should key off the HTTP status and `error` field only.

## Tear down

```bash
docker compose -p ch37kc down -v
```
