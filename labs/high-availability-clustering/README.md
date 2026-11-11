# Chapter 41 lab — two-node Keycloak cluster, real session replication

**Isolated as Docker Compose project `ch41kc`.** Host ports: **18141** (node A), **18142** (node B).
Postgres is internal-only (no published host port), shared by both nodes.

## What this lab actually proves, and what it doesn't

This is the "partial (single-node sim)" lab CLAUDE.md's curriculum calls for: **two real Keycloak 26.8.2
server processes**, each its own container with its own embedded Infinispan cache, clustered together
over JGroups, both pointed at the same PostgreSQL database — a genuine multi-*node* simulation on one
Docker host.

- **Proves (honestly, with live evidence below):** cross-node session-cache replication. A session
  created by logging in through node A's own host port is recognized by node B's own host port, with no
  second login, no shared filesystem trick, nothing narrated.
- **Does NOT prove:** real multi-host failover, a network partition, or anything about a node actually
  dying. Both "nodes" share one kernel, one Docker bridge network, and one Postgres instance. A real
  production cluster's hardest problems — WAN latency between data centers, a host really going down,
  split-brain during a partition — need more than one Docker host (or a real multi-node Kubernetes
  deployment, chapter 42) to demonstrate honestly. This lab doesn't claim to cover them.

## Why `KC_HOSTNAME` is forced to the same value on both nodes

Both services set `KC_HOSTNAME: http://localhost:18141` — including node B, even though node B actually
listens on port 18142. This was **not** the first thing tried; the first run of this lab set no
`KC_HOSTNAME` at all and hit a genuine bug worth knowing about: each node computed its own token
`iss` (issuer) claim from whatever `Host` header the request arrived on, so a token minted via node A
(`iss=http://localhost:18141/realms/...`) was rejected by node B with
`invalid_grant` / `"Invalid token issuer. Expected 'http://localhost:18142/realms/...'"` — a real
issuer mismatch, not a session-replication failure. Every real multi-node Keycloak deployment sits
behind one load balancer with one public hostname for exactly this reason; forcing a single
`KC_HOSTNAME` here is this lab's stand-in for that load balancer, without adding a third container.

## Reproduction

```bash
cd labs/high-availability-clustering
docker compose -p ch41kc up -d
```

Wait for both nodes to answer (roughly 30-45s each on this machine):

```bash
until curl -sf http://localhost:18141/realms/master/.well-known/openid-configuration >/dev/null; do sleep 3; done
until curl -sf http://localhost:18142/realms/master/.well-known/openid-configuration >/dev/null; do sleep 3; done
```

Confirm the two-node cluster actually formed (JDBC_PING discovery, JGroups channel `ISPN`):

```bash
docker logs ch41kc-keycloak-a-1 2>&1 | grep -i "cluster view\|JDBC_PING\|mTLS"
```

Real output from this run:

```
INFO  [org.keycloak.spi.infinispan.impl.embedded.JGroupsConfigurator] JGroups JDBC_PING discovery enabled.
INFO  [org.keycloak.spi.infinispan.impl.embedded.JGroupsConfigurator] JGroups Encryption enabled (mTLS).
INFO  [org.infinispan.CLUSTER] ISPN000094: Received new cluster view for channel ISPN: [node-b(v=16.0.15)|1] (2) [node-b(v=16.0.15), node-a(v=16.0.15)]
```

Two nodes, discovered each other through the shared Postgres (no static host list, no multicast), and
JGroups traffic between them is mTLS-encrypted by default — all three confirmed live, not from docs.

### Set up a realm, client, and user (once)

```bash
ADMIN_TOKEN_A=$(curl -s -X POST http://localhost:18141/realms/master/protocol/openid-connect/token \
  -d grant_type=password -d client_id=admin-cli -d username=admin -d password=admin_lab_password \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

curl -s -X POST http://localhost:18141/admin/realms \
  -H "Authorization: Bearer $ADMIN_TOKEN_A" -H "Content-Type: application/json" \
  -d '{"realm":"ch41-meridian-trust","enabled":true}'

curl -s -X POST http://localhost:18141/admin/realms/ch41-meridian-trust/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN_A" -H "Content-Type: application/json" \
  -d '{"clientId":"ch41-teller-app","publicClient":true,"directAccessGrantsEnabled":true,"enabled":true,"standardFlowEnabled":false}'

curl -s -X POST http://localhost:18141/admin/realms/ch41-meridian-trust/users \
  -H "Authorization: Bearer $ADMIN_TOKEN_A" -H "Content-Type: application/json" \
  -d '{"username":"priya.teller","enabled":true,"credentials":[{"type":"password","value":"ch41-lab-password","temporary":false}]}'
```

**A second real gotcha hit here:** the direct-grant login below initially failed with
`invalid_grant` / `"Account is not fully set up"` even though the user had a password credential and an
empty `requiredActions` list. Cause: the realm's **VERIFY_PROFILE** required action (enabled by default,
`defaultAction: false`) still gets dynamically resolved for any user whose profile is missing attributes
the realm's User Profile configuration expects (here: email/first/last name) — it doesn't need to appear
in the user's own `requiredActions` array to fire, and a direct grant can't satisfy an interactive
required action, so it's rejected outright. Fixed for this lab by disabling it realm-wide (acceptable for
a teaching lab user with no email on file; a real deployment would instead populate the profile or make
the attributes optional):

```bash
curl -s -X PUT "http://localhost:18141/admin/realms/ch41-meridian-trust/authentication/required-actions/VERIFY_PROFILE" \
  -H "Authorization: Bearer $ADMIN_TOKEN_A" -H "Content-Type: application/json" \
  -d '{"alias":"VERIFY_PROFILE","name":"Verify Profile","providerId":"VERIFY_PROFILE","enabled":false,"defaultAction":false,"priority":100,"config":{}}'
```

### The session-replication proof (real HTTP transcript from this run)

**Step 1 — log in via node A only:**

```bash
LOGIN_A=$(curl -s -X POST http://localhost:18141/realms/ch41-meridian-trust/protocol/openid-connect/token \
  -d grant_type=password -d client_id=ch41-teller-app -d username=priya.teller -d password=ch41-lab-password)
```

Access token's `sid` claim: `i06ag07NudMsZ2EjTCebMfG0` — this is the session Keycloak created, entirely
inside node A's process.

**Step 2 — present node A's refresh token to node B, which never saw the login:**

```bash
curl -s -X POST http://localhost:18142/realms/ch41-meridian-trust/protocol/openid-connect/token \
  -d grant_type=refresh_token -d client_id=ch41-teller-app -d refresh_token="$REFRESH_A"
```

Real response (trimmed): `200 OK`, a brand-new access token whose `sid` claim is
**`i06ag07NudMsZ2EjTCebMfG0`** — identical to the one node A minted. Node B located, validated, and
extended a session it never created, by reading it out of the replicated `sessions` Infinispan cache.
No login form, no credentials resent — exactly the proof the brief asks for.

**Step 3 — list the session via node B's own Admin REST API, revoke it through node B, confirm via node
A:**

```bash
curl -s "http://localhost:18142/admin/realms/ch41-meridian-trust/users/$USER_ID/sessions" -H "Authorization: Bearer $ADMIN_TOKEN_B"
curl -s -X DELETE "http://localhost:18142/admin/realms/ch41-meridian-trust/sessions/$SESSION_ID" -H "Authorization: Bearer $ADMIN_TOKEN_B"   # 204
curl -s "http://localhost:18141/admin/realms/ch41-meridian-trust/users/$USER_ID/sessions" -H "Authorization: Bearer $ADMIN_TOKEN_A"
```

Real result: of three active sessions, the one deleted **through node B** was immediately absent from
node A's own session listing, while the other two (untouched) were still present on both. Replication is
confirmed in both directions — reads and writes — not just one.

### Tear down

```bash
docker compose -p ch41kc down -v
```

`-v` removes the named Postgres volume (`ch41kc_ch41_pg_data`) too — this lab's data is disposable.

## Files

- `docker-compose.yml` — the two-node + shared-Postgres stack described above.

## Environment this was actually run on

Docker 29.6.2, Keycloak image `quay.io/keycloak/keycloak:26.8.2` (already present locally — no pull
needed), Postgres image `postgres:17-alpine` (already present locally). Host disk was within ~7-8 GB
free throughout; no new images were pulled for this lab, and the stack is torn down (`down -v`) as the
final step of building this chapter.
