#!/usr/bin/env bash
# Reconstructs the full northwind-apps realm for the capstone's own isolated stack (project
# capstonekc, port 18200), as established across chapters 20-39 plus the UMA-protected resource
# this capstone adds on top. This is exactly what was actually run, in order, against a real
# running Keycloak -- including the fixes for two real bugs hit live (see capstone.html's "Real
# bugs found while building this" section for the full write-up of both). Idempotent-ish:
# re-running against an already-seeded realm will hit
# "already exists" errors on the create steps; tear down and re-up the stack for a clean reseed.
set -euo pipefail

KC="docker compose -p capstonekc exec keycloak /opt/keycloak/bin/kcadm.sh"
BASE_URL="http://localhost:18200"

$KC config credentials --server http://localhost:8080 --realm master --user admin --password admin_lab_password

echo "== realm =="
$KC create realms -r northwind-apps 2>/dev/null || true
$KC create realms -s realm=northwind-apps -s enabled=true \
  -s ssoSessionIdleTimeout=3600 -s ssoSessionMaxLifespan=28800

echo "== roles (chapters 23, 34) =="
$KC create roles -r northwind-apps -s name=tenant-admin -s 'description=Full admin access within one customer tenant'
$KC create roles -r northwind-apps -s name=support-agent -s 'description=Support tooling and tenant-facing queues'
$KC create roles -r northwind-apps -s name=platform-engineer -s 'description=Platform engineering tooling and infrastructure'
$KC create roles -r northwind-apps -s name=billing-reader -s 'description=Can view invoice data'
$KC create roles -r northwind-apps -s name=billing-admin -s 'description=Can create/modify invoice data'

echo "== groups (chapter 23) =="
$KC create groups -r northwind-apps -s name=Engineering
$KC create groups -r northwind-apps -s name=Support

echo "== BUG FOUND LIVE: declare tenant_id in the User Profile schema BEFORE setting it on any user =="
# Keycloak 26's declarative User Profile silently drops any user attribute that isn't part of
# its schema (no error, no warning -- the attribute just never appears on GET or in a token).
# tenant_id has to be declared here first, or every later 'attributes.tenant_id=[...]' call
# below is a no-op.
curl -s "$BASE_URL/admin/realms/northwind-apps/users/profile" \
  -H "Authorization: Bearer $($KC config credentials --server http://localhost:8080 --realm master --user admin --password admin_lab_password >/dev/null; true)" >/dev/null || true
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/realms/master/protocol/openid-connect/token" \
  --data-urlencode "grant_type=password" --data-urlencode "client_id=admin-cli" \
  --data-urlencode "username=admin" --data-urlencode "password=admin_lab_password" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
curl -s "$BASE_URL/admin/realms/northwind-apps/users/profile" -H "Authorization: Bearer $ADMIN_TOKEN" > /tmp/northwind-user-profile.json
python3 - "$ADMIN_TOKEN" <<'PYEOF'
import json, sys
with open("/tmp/northwind-user-profile.json") as f:
    profile = json.load(f)
if not any(a["name"] == "tenant_id" for a in profile["attributes"]):
    profile["attributes"].append({
        "name": "tenant_id",
        "displayName": "Tenant ID",
        "permissions": {"view": ["admin"], "edit": ["admin"]},
        "multivalued": False,
    })
with open("/tmp/northwind-user-profile-updated.json", "w") as f:
    json.dump(profile, f)
PYEOF
curl -s -X PUT "$BASE_URL/admin/realms/northwind-apps/users/profile" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d @/tmp/northwind-user-profile-updated.json > /dev/null

echo "== users (chapters 22, 23, 34) -- email is required: an unverified/emailless user hits VERIFY_PROFILE at login (second real bug found live) =="
for spec in "jane.doe:Jane:Doe:acme-corp" "priya.patel:Priya:Patel:acme-corp" "amara.reader:Amara:Reader:acme-corp" "devon.admin:Devon:Admin:acme-corp" "priya.sales:Priya:Sales:contoso"; do
  IFS=: read -r u f l t <<< "$spec"
  $KC create users -r northwind-apps -s username="$u" -s enabled=true -s firstName="$f" -s lastName="$l" \
    -s email="${u}@northwindapps.example" -s emailVerified=true
  $KC set-password -r northwind-apps --username "$u" --new-password 'CapstoneLab1!' --temporary=false
  USER_ID=$($KC get users -r northwind-apps -q username="$u" --fields id --format csv --noquotes)
  $KC update "users/$USER_ID" -r northwind-apps -s "attributes={\"tenant_id\":[\"$t\"]}"
done

echo "== role/group grants =="
$KC add-roles -r northwind-apps --uusername jane.doe --rolename tenant-admin
$KC add-roles -r northwind-apps --uusername priya.patel --rolename platform-engineer
PRIYA_ID=$($KC get users -r northwind-apps -q username=priya.patel --fields id --format csv --noquotes)
ENGINEERING_GID=$($KC get groups -r northwind-apps --fields id,name --format csv --noquotes | awk -F, '$2=="Engineering"{print $1}')
$KC update "users/$PRIYA_ID/groups/$ENGINEERING_GID" -r northwind-apps -b '{}'
$KC add-roles -r northwind-apps --uusername amara.reader --rolename billing-reader
$KC add-roles -r northwind-apps --uusername devon.admin --rolename billing-admin
# priya.sales deliberately gets no billing role and no tenant-side role.

echo "== clients =="
$KC create clients -r northwind-apps \
  -s clientId=northwind-dashboard -s protocol=openid-connect \
  -s publicClient=false -s standardFlowEnabled=true -s directAccessGrantsEnabled=false \
  -s serviceAccountsEnabled=false -s 'redirectUris=["http://localhost:8081/*"]'

$KC create clients -r northwind-apps \
  -s clientId=northwind-spa -s protocol=openid-connect \
  -s publicClient=true -s standardFlowEnabled=true -s directAccessGrantsEnabled=false \
  -s 'redirectUris=["http://localhost:5173/*"]' -s 'webOrigins=["http://localhost:5173"]' \
  -s 'attributes."pkce.code.challenge.method"=S256'

$KC create clients -r northwind-apps \
  -s clientId=billing-service-account -s protocol=openid-connect \
  -s publicClient=false -s standardFlowEnabled=false -s directAccessGrantsEnabled=false \
  -s serviceAccountsEnabled=true

$KC create clients -r northwind-apps \
  -s clientId=billing-direct-grant -s protocol=openid-connect \
  -s publicClient=false -s standardFlowEnabled=false -s directAccessGrantsEnabled=true \
  -s serviceAccountsEnabled=false

$KC create clients -r northwind-apps \
  -s clientId=billing-api -s protocol=openid-connect \
  -s publicClient=false -s standardFlowEnabled=false -s directAccessGrantsEnabled=false \
  -s serviceAccountsEnabled=true -s authorizationServicesEnabled=true

$KC add-roles -r northwind-apps --uusername service-account-billing-service-account --rolename billing-admin
$KC add-roles -r northwind-apps --uusername service-account-billing-api --rolename billing-admin

echo "== tenant-context client scope (chapter 39's TenantIdProtocolMapper) =="
$KC create client-scopes -r northwind-apps -s name=tenant-context -s protocol=openid-connect
TCS_ID=$($KC get client-scopes -r northwind-apps --fields id,name --format csv --noquotes | awk -F, '$2=="tenant-context"{print $1}')
$KC create "client-scopes/$TCS_ID/protocol-mappers/models" -r northwind-apps \
  -s name=tenant_id -s protocol=openid-connect -s protocolMapper=northwind-tenant-id-mapper \
  -s 'config."claim.name"=tenant_id' -s 'config."access.token.claim"=true' \
  -s 'config."id.token.claim"=true' -s 'config."userinfo.token.claim"=true'

DASH_ID=$($KC get clients -r northwind-apps -q clientId=northwind-dashboard --fields id --format csv --noquotes)
SPA_ID=$($KC get clients -r northwind-apps -q clientId=northwind-spa --fields id --format csv --noquotes)
$KC update "clients/$DASH_ID/default-client-scopes/$TCS_ID" -r northwind-apps -b '{}'
$KC update "clients/$SPA_ID/default-client-scopes/$TCS_ID" -r northwind-apps -b '{}'

echo "== billing-api: UMA resource/scope/policy/permission (mirrors chapter 37's proven pattern) =="
CID=$($KC get clients -r northwind-apps -q clientId=billing-api --fields id --format csv --noquotes)

# BUG FOUND LIVE: a resource URI with a path-segment wildcard ("/api/invoices/*/premium-report")
# is rejected -- wildcard is only accepted as a trailing "/*" or a "/*.ext" suffix. Use a fixed
# path instead (billing-api's actual endpoint is a fixed GET /api/invoices/premium-report, not
# parameterized per invoice id).
$KC create "clients/$CID/authz/resource-server/resource" -r northwind-apps \
  -s name="premium-invoice-report" -s displayName="Premium invoice analytics report" \
  -s 'uris=["/api/invoices/premium-report"]' -s type=urn:northwind:invoice-report
RID=$($KC get "clients/$CID/authz/resource-server/resource" -r northwind-apps --fields _id,name --format csv --noquotes | grep premium-invoice-report | cut -d, -f1)

$KC create "clients/$CID/authz/resource-server/scope" -r northwind-apps -s name=view
SID=$($KC get "clients/$CID/authz/resource-server/scope" -r northwind-apps --fields id,name --format csv --noquotes | grep ',view$' | cut -d, -f1)

# GOTCHA (also confirmed by chapter 37): a permission can't reference this resource+scope pair
# until the resource itself lists the scope.
$KC update "clients/$CID/authz/resource-server/resource/$RID" -r northwind-apps \
  -s name="premium-invoice-report" -s type=urn:northwind:invoice-report \
  -s 'uris=["/api/invoices/premium-report"]' -s "scopes=[{\"id\":\"$SID\"}]"

$KC create "clients/$CID/authz/resource-server/policy/role" -r northwind-apps \
  -s name="billing-admin-policy" -s 'roles=[{"id":"billing-admin"}]' -s logic=POSITIVE
PID=$($KC get "clients/$CID/authz/resource-server/policy" -r northwind-apps --fields id,name --format csv --noquotes | grep billing-admin-policy | cut -d, -f1)

$KC create "clients/$CID/authz/resource-server/permission/scope" -r northwind-apps \
  -s name="view-premium-invoice-report" -s "resources=[\"$RID\"]" -s "scopes=[\"$SID\"]" \
  -s "policies=[\"$PID\"]" -s decisionStrategy=UNANIMOUS

echo "== secrets (for the app config in the next steps -- these are fresh per seed run, never commit them) =="
for cid in billing-service-account billing-direct-grant billing-api northwind-dashboard; do
  ID=$($KC get clients -r northwind-apps -q clientId=$cid --fields id --format csv --noquotes)
  SECRET=$($KC get "clients/$ID/client-secret" -r northwind-apps --fields value --format csv --noquotes)
  echo "$cid: $SECRET"
done
echo "premium-invoice-report resource id: $RID"

echo "Seed complete."
