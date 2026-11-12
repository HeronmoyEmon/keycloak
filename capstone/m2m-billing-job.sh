#!/usr/bin/env bash
# A real service-to-service job: billing-service-account (client_credentials, holds billing-admin
# via its service account) calls the running billing-api with no human and no browser involved,
# standing in for a nightly billing-reconciliation job -- chapter 36's pattern, wired into the
# capstone's actual running stack.
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:18200}"
BILLING_API_URL="${BILLING_API_URL:-http://localhost:8081}"
CLIENT_SECRET="${BILLING_SERVICE_ACCOUNT_SECRET:?Set BILLING_SERVICE_ACCOUNT_SECRET first (see README.md)}"

echo "== requesting a client-credentials token for billing-service-account =="
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/northwind-apps/protocol/openid-connect/token" \
  --data-urlencode "grant_type=client_credentials" \
  --data-urlencode "client_id=billing-service-account" \
  --data-urlencode "client_secret=$CLIENT_SECRET" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
echo "token acquired (${#TOKEN} chars)"

echo "== GET $BILLING_API_URL/api/invoices as the service account =="
curl -s -w "\nHTTP %{http_code}\n" "$BILLING_API_URL/api/invoices" -H "Authorization: Bearer $TOKEN"

echo "== GET $BILLING_API_URL/api/invoices/premium-report as the service account (holds billing-admin, so this is a real UMA-permitted call) =="
curl -s -w "\nHTTP %{http_code}\n" "$BILLING_API_URL/api/invoices/premium-report" -H "Authorization: Bearer $TOKEN"
