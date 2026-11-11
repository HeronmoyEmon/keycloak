# Custom User Storage SPI lab (chapter 33)

A real, buildable Keycloak `UserStorageProvider` that bridges a toy stand-in for Riverside Health's
legacy, proprietary HR staff directory (a bundled CSV file) into a running Keycloak realm — for
read-only lookup and password validation, without migrating or rewriting that legacy system.

See chapter 33 (`chapters/33-custom-user-storage-spi.html`) for the full walkthrough, the live
Maven-coordinate verification trail, and every real command/output this lab actually produced,
including the real mistakes hit along the way (an XML-comment POM parse error, a missing
`credentialManager()` override, a Docker bind-mount gotcha, a wrong `parentId`, and a Declarative
User Profile `unmanagedAttributePolicy` surprise).

## Build

```
cd labs/custom-user-storage-spi
mvn clean package
```

Produces `target/legacy-staff-directory-spi-1.0.0.jar`. Build it **before** bringing the stack up —
see the chapter's "common mistakes" section for what happens to the bind mount if you don't.

## Run

```
docker compose -p ch33kc up -d
# wait for Keycloak to answer on http://localhost:18133, then:
docker compose -p ch33kc exec keycloak /opt/keycloak/bin/kc.sh build
docker compose -p ch33kc restart keycloak
```

Keycloak is published on host port **18133**, isolated from the shared `labs/docker/` stack and
every other chapter's own stack. Admin console: `http://localhost:18133` (admin /
`admin_lab_password`).

## Toy staff directory

`src/main/resources/riverside-legacy-staff.csv` — three users, columns
`username,password,firstName,lastName,email,employeeId,department`. The password column is
intentionally plaintext, standing in for a real 20-year-old HR system's own (often weak) credential
storage — see `StaffRecord`'s javadoc.

## Tear down

```
docker compose -p ch33kc down -v
```
