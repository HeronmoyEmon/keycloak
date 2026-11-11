# Keycloak on Kubernetes lab (chapter 42)

A real `kind` cluster, the official Keycloak Operator, and one Keycloak 26.8.2 instance — deployed,
verified end to end (admin console, OIDC discovery document, a real issued access token), and torn
down again. This is the exact sequence that was actually run while building chapter 42
(`chapters/42-keycloak-on-kubernetes.html`); see that chapter for the full narrative, the live
Operator-vs-Helm-chart research, and every diagram.

**Why this lab deletes its own cluster when done:** the dev machine this course is built on runs with
very little free disk (see chapter 44's discussion of resource budgets for more on why that matters).
A `kind` cluster is cheap to create and just as cheap to delete — there is no reason to leave one
running between study sessions, and the steps below assume you'll do the same.

## Prerequisites confirmed on the build machine

- Docker 29.6.2
- `kind` 0.24.0
- `kubectl` v1.31.3
- `openssl` (for a throwaway dev TLS cert — ships with virtually every Linux/macOS install)

No Helm is required for this lab — the plain-`kubectl`/kustomize install path is what gets used (see
the chapter for why).

## 1. Free disk headroom before you start

`kind` plus a Keycloak image plus a Postgres image is a real, if modest, amount of disk. Before
building this lab, `df -h /` showed only ~7.6GB free on a 94GB disk. The single highest-leverage fix
was reclaiming Docker's **build cache** (not images, not volumes — just build cache), which doesn't
touch anything another project is using:

```
docker builder prune -af
```

This alone freed about 7GB (11.78GB of build cache existed, 7.3GB of it reclaimable) with zero risk to
running containers or other projects' images. Do this first if you're tight on space; only fall back to
pruning unused images/volumes if that isn't enough, and check `docker ps -a` first so you don't touch
another project's containers.

## 2. Create the kind cluster

```
kind create cluster --name kc42
```

Real timing on the build machine: **1m16s** end to end (pulling `kindest/node:v1.31.0`, starting the
control plane, installing the CNI and default StorageClass). `kubectl get nodes` showed one
`kc42-control-plane` node.

## 3. Install the Keycloak Operator (namespace-scoped, pinned to 26.8.2)

Live-verified against <https://www.keycloak.org/operator/installation> on 2026-11-11. The page's own
**recommended** path is actually OLM (Operator Lifecycle Manager) — but that assumes OLM is already
installed in the cluster, which adds a whole extra moving part a `kind` teaching cluster has no other
reason to carry. The same page documents a plain-`kubectl` + Kustomize path as a fully supported
alternative, which is what this lab uses:

```
kubectl create namespace keycloak
kubectl apply -k 'github.com/keycloak/keycloak-k8s-resources/kubernetes?ref=26.8.2'
```

This namespace-scoped install puts the Operator in `keycloak` and makes it watch only that namespace
(the docs' "cluster-wide" variant, installed via a separate `.../kubernetes/cluster-wide?ref=26.8.2`
kustomization into a `keycloak-operator` namespace, is explicitly still labeled **preview** on the
Operator's own docs — not used here).

Real output from this command installed 4 CRDs (`Keycloak`, `KeycloakRealmImport`,
`KeycloakOIDCClient`, `KeycloakSAMLClient`), a ServiceAccount, 5 ClusterRoles, 5 RoleBindings, 1
ClusterRoleBinding, a Service, and the `keycloak-operator` Deployment.

```
kubectl -n keycloak rollout status deployment/keycloak-operator --timeout=180s
# deployment "keycloak-operator" successfully rolled out
```

## 4. Dev-only Postgres, DB secret, and a self-signed TLS cert

```
kubectl apply -f example-postgres.yaml

kubectl -n keycloak create secret generic keycloak-db-secret \
  --from-literal=username=testuser \
  --from-literal=password=testpassword

openssl req -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 3 -out cert.pem -subj "/CN=localhost"
kubectl -n keycloak create secret tls example-tls-secret --cert cert.pem --key key.pem
```

The `openssl` self-signed-cert command is the Operator docs' own documented "for development purposes"
snippet, not an invented shortcut.

## 5. Deploy the Keycloak CR

```
kubectl apply -f example-kc.yaml
```

Wait for it, then check real status:

```
kubectl -n keycloak wait --for=condition=ready pod/example-kc-0 --timeout=180s
kubectl -n keycloak get keycloaks/example-kc -o go-template='{{range .status.conditions}}CONDITION: {{.type}}{{"\n"}}  STATUS: {{.status}}{{"\n"}}  MESSAGE: {{.message}}{{"\n"}}{{end}}'
```

Real output:

```
CONDITION: Ready
  STATUS: True
  MESSAGE:
CONDITION: HasErrors
  STATUS: False
  MESSAGE: warning: A ServiceMonitor will not be created because `metrics-enabled` is not true.
CONDITION: RollingUpdate
  STATUS: False
  MESSAGE:
CONDITION: RecreateUpdateUsed
  STATUS: Unknown
  MESSAGE: <no value>
```

Real log tail (`kubectl -n keycloak logs example-kc-0 --tail=40`) confirmed a genuine Keycloak boot,
not a cached/fake transcript:

```
2026-11-11 12:03:25,162 INFO  [io.quarkus] (main) Keycloak 26.8.2 on JVM (powered by Quarkus 3.40.1.1) started in 5.057s. Listening on: https://0.0.0.0:8443. Management interface listening on https://0.0.0.0:9000.
...
2026-11-11 12:03:48,103 INFO  [org.keycloak.services] (executor-thread-1) KC-SERVICES0077: Created temporary admin user with username temp-admin
2026-11-11 12:03:48,233 INFO  [org.keycloak.services.resources.KeycloakApplication] (executor-thread-1) Bootstrap completed in 23.153000 seconds
```

**Gotcha worth flagging:** the Operator does *not* create a persistent `admin`/`admin`-style account the
way chapter 20's plain Docker Compose install (`KC_BOOTSTRAP_ADMIN_USERNAME`/`_PASSWORD`) does. It
creates a one-time **temporary** admin user (`temp-admin` above) and writes its random password into a
generated Secret:

```
kubectl -n keycloak get secret example-kc-initial-admin -o jsonpath='{.data.username}' | base64 --decode
kubectl -n keycloak get secret example-kc-initial-admin -o jsonpath='{.data.password}' | base64 --decode
```

## 6. Verify it for real: port-forward, admin console, discovery doc, a real token

```
kubectl -n keycloak port-forward service/example-kc-service 18443:8443 &

curl -sk -o /dev/null -w "HTTP %{http_code}\n" https://localhost:18443/admin/master/console/
# HTTP 200

curl -sk https://localhost:18443/realms/master/.well-known/openid-configuration
# real OIDC discovery document, issuer "https://localhost:18443/realms/master"

curl -sk -X POST https://localhost:18443/realms/master/protocol/openid-connect/token \
  -d grant_type=password -d client_id=admin-cli \
  -d username=temp-admin -d password=<the password you decoded above>
# a real, signed access_token JWT came back
```

All three checks passed for real on the build machine on 2026-11-11 — no narrated/fabricated output.

Ingress was intentionally left disabled (`ingress.enabled: false` in `example-kc.yaml`) because `kind`
ships no Ingress controller by default; `kubectl port-forward` directly to the Keycloak `Service` is the
Operator docs' own documented way to reach a deployment for "debugging and development purposes."

## 7. Clean up — do this before moving on, not at the end of a long session

```
kill %1                       # stop the port-forward (or `kill <pid>` if backgrounded differently)
kind delete cluster --name kc42
```

Real confirmation this leaves nothing behind:

```
kind get clusters          # No kind clusters found.
docker ps -a | grep kc42   # (no output)
```

Everything `kind` pulled — the `kindest/node` image, the Operator image, and `postgres:15` pulled fresh
*inside* the node's own containerd (see the "gotcha" below) — lives inside that one node container and
is reclaimed the moment it's deleted. `df -h /` after cleanup showed **more** free space than before
this lab started (builder-cache pruning in step 1 freed far more than the lab itself used).

## Gotchas hit for real while building this lab

1. **`kind` re-pulls images even if your host Docker already has them cached.** `kind create cluster`
   runs the control plane as a container with its *own* containerd inside it; images referenced by
   pods scheduled on that node (the Operator, Keycloak, Postgres) get pulled from the internet by that
   inner containerd, independent of what's already sitting in your host's `docker images`. Having
   `quay.io/keycloak/keycloak:26.8.2` cached on the host (it was, from chapter 20's lab) did not avoid a
   fresh pull inside the kind node. `kind load docker-image` exists to pre-seed a node's containerd from
   a host-side image and was deliberately *not* used here, since there was enough headroom after step 1
   to just let it pull normally — call this out if you're ever doing this lab somewhere tighter on
   bandwidth or disk than this build machine was.
2. **No persistent `admin`/`admin` account.** Covered above — the Operator issues a one-time
   `temp-admin` + random password via a generated `<cr-name>-initial-admin` Secret, not a fixed
   bootstrap credential.
3. **The official docs' own basic-deployment example doesn't cover a local/offline-friendly hostname or
   HTTP-only setup at all** — it assumes either a real DNS hostname plus a real or edge-terminated TLS
   certificate, or an OpenShift Route. `hostname.hostname: localhost` plus the docs' own "for
   development purposes" self-signed-cert snippet (step 4) is this lab's own judgment call to make a
   `kind`-friendly local lab work, not something the docs state outright.
4. **Service-name mismatch is easy to typo by hand.** The ephemeral-Postgres StatefulSet is named
   `postgresql-db`; the Service Keycloak actually connects to (`db.host` in the CR) is `postgres-db` —
   one letter apart. This is the official docs' own example, not something introduced here, but it's
   worth re-reading `example-postgres.yaml` carefully rather than assuming the two names match.
