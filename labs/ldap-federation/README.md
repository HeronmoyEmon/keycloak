# Chapter 32 lab environment: Keycloak + PostgreSQL + OpenLDAP

Used by chapter 32's (User Federation: LDAP & Active Directory) hands-on lab. A self-contained stack,
independent of `labs/docker/docker-compose.yml` (that one has no LDAP server and is used by chapters
20-30+). Confirmed working end-to-end (2026-11-11): both containers reach a ready state, the seed LDIF
loads into OpenLDAP for real, a Keycloak LDAP User Federation provider syncs 5 real LDAP users and 5 real
LDAP groups, and an LDAP-sourced user authenticates for real via a direct-grant token request.

## Start

```bash
cd labs/ldap-federation
docker compose up -d
```

Wait for both services to be ready:

```bash
# Keycloak — poll until the discovery document responds 200
for i in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18132/realms/master/.well-known/openid-configuration)
  echo "attempt $i: $code"
  if [ "$code" = "200" ]; then break; fi
  sleep 3
done

# OpenLDAP — confirm the seeded entry is really there
docker compose exec openldap ldapsearch -x -H ldap://localhost \
  -b "dc=meridiantrust,dc=example" -D "cn=admin,dc=meridiantrust,dc=example" \
  -w ldap_admin_lab_password "(uid=jmartinez)"
```

This compose file bakes in non-default host ports on purpose — Keycloak on **18132**, OpenLDAP on
**18389** — rather than the 8080 every other chapter's `labs/docker/docker-compose.yml` uses, so this
lab's stack can run alongside another chapter's `labs/docker` stack (or another chapter's own isolated
lab) without a port collision, with no separate override file needed.

## Access

- Admin Console: http://localhost:18132/admin — bootstrap admin `admin` / `admin_lab_password`
- OpenLDAP: `ldap://localhost:18389` — directory admin `cn=admin,dc=meridiantrust,dc=example` /
  `ldap_admin_lab_password`. TLS is deliberately off (`LDAP_TLS: "false"`) to keep this lab's connection
  string simple (`ldap://`, not `ldaps://`) — chapter 32's "Common mistakes" section covers why a real
  production directory should not be reached this way.

## Seeded directory structure

Base DN `dc=meridiantrust,dc=example`, loaded from `seed/50-meridian-seed.ldif` — stands in for Meridian
Trust's (the retail-bank case study) existing on-prem staff directory:

- `ou=staff,dc=meridiantrust,dc=example` — 5 `inetOrgPerson` entries (`jmartinez`, `dkim`, `rpatel`,
  `swilliams`, `aoconnor`), each with `uid`/`cn`/`sn`/`givenName`/`mail`/`employeeType` and a plaintext
  `userPassword` (fine for a disposable local lab; never do this against a real directory).
- `ou=groups,dc=meridiantrust,dc=example` — 5 `groupOfNames` entries (`tellers`, `loan-officers`,
  `fraud-analysts`, `branch-managers`, `it-admins`), one real staff member each.

## OpenLDAP image choice (live-verified 2026-11-11)

Pinned to **`osixia/openldap:1.5.0`**. The upstream project (renamed `osixia/container-openldap` on
GitHub, 110M+ Docker Hub pulls, still actively pushed as of 2026-11-02) is maintained, but its v2 line
(OpenLDAP 2.6.15) exists only as `alpha`/`beta`/`rc` prerelease tags as of this date, with the project's
own README carrying an explicit "v2 is a breaking release" warning. `1.5.0` (OpenLDAP 2.4.57) is the
newest tag that is an actual final release, so it's the one this lab pins rather than an unreleased
prerelease — re-verify this before reusing the compose file much later, since a stable v2 release may
exist by then.

## Seeding gotchas actually hit while building this lab (and why the compose file is shaped the way it is)

Two different, more "obvious" approaches to loading `seed/50-meridian-seed.ldif` were tried first and
both genuinely failed a real container start — documented here (and in chapter 32's lab) rather than
smoothed over:

1. **Bind-mounting the seed file directly as a single file** into the image's default custom-bootstrap
   path failed at container start with `sed: cannot rename ...: Device or resource busy` — the image's
   startup script does an in-place `sed` substitution on bootstrap LDIFs, which can't rename over a
   single-file bind mount.
2. **Bind-mounting a whole host directory** at that same custom-bootstrap path got past that error, but
   the image's bootstrap step *moves* (not copies) a successfully-loaded custom LDIF out of that
   directory — which, through the bind mount, deletes the seed file from the **host** after the very
   first successful `docker compose up`. A `docker compose down -v && up -d` cycle after that found an
   empty directory and silently seeded nothing. (A related side effect while debugging this: the
   container also `chown`s whatever it's given write access to, to its own internal `911:911` UID/GID —
   if you ever see a `seed/` file you can no longer edit as your own user after running this stack
   directly with `docker run` outside this compose file, that's why; `docker run --rm -v
   "$(pwd)/seed:/fix" alpine:3 chown -R "$(id -u):$(id -g)" /fix` gets ownership back.)

The fix actually used in `docker-compose.yml`: the image's own **`LDAP_SEED_INTERNAL_LDIF_PATH`**
environment variable, pointed at a *separate* mounted directory (`/ldap-seed-src`, mapped from
`./seed`) that is not the custom-bootstrap path at all. This mechanism explicitly **copies** files from
that path into the image's internal seeding location rather than consuming them — confirmed directly by
re-running the stack from a clean `down -v` and finding `seed/50-meridian-seed.ldif` untouched on the
host both times, with the LDAP entries still seeded correctly inside the container.

A third, unrelated gotcha hit while writing the seed file itself: the bootstrap script decides whether to
load a custom LDIF file with `ldapadd` (for a plain add-only file, which this one is) or `ldapmodify`
(for a file containing LDIF *change* records) by doing a plain text search for the word LDIF uses to mark
a change record — **anywhere in the file, including comments**. An earlier draft of `seed/
50-meridian-seed.ldif` had that exact word in an explanatory comment and was misdetected as a modify-style
file, failing with `ldapmodify: modify operation type is missing at line 2`. The fix was simply rewording
the comment.

## Stop

```bash
docker compose down        # keep data
docker compose down -v     # also delete both volumes — a clean slate; the seed file on the host is
                            # untouched either way, per the fix above
```

## Realm used by chapter 32's lab

Chapter 32 creates a `meridian-trust` realm, a public client `meridian-teller-app`
(`directAccessGrantsEnabled: true`, used only to prove an LDAP-sourced user can really get a token — see
chapter 13's and chapter 24's notes on this being a lab-only technique, never a real application
pattern), and one LDAP User Federation provider pointed at this stack's OpenLDAP container. Full
step-by-step REST calls are in the chapter itself, not duplicated here.
