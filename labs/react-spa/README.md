# react-spa — Riverside Health patient portal (chapter 35 lab)

A minimal real React single-page app securing itself with OpenID Connect
authorization-code + PKCE against Keycloak, using `react-oidc-context`
(wrapping `oidc-client-ts`). Built for chapter 35, "Securing a Frontend SPA" —
see that chapter for the full walkthrough and the version-pinning reasoning.

## Toolchain pins (why these exact versions)

This machine runs **Node v18.20.4**. The current Vite major (7.x, at the time
this lab was built) requires Node `^20.19.0 || >=22.12.0` and will refuse to
run here. Scaffolding with `npm create vite@6.5.0 . -- --template react`
(an explicitly pinned, still-Node-18-compatible major) generates a
`package.json` pinning `vite: ^6.3.5` and `@vitejs/plugin-react: ^4.4.1`,
both of which declare Node `^18.0.0` (vite) / `^14.18.0 || >=16.0.0`
(plugin-react) support. Live-verified: `npm install` and `npm run dev` both
ran cleanly with Node 18.20.4, no engine warnings, no errors.

- `vite@6.4.6` (resolved under the `^6.3.5` range)
- `@vitejs/plugin-react@4.7.0` (resolved under `^4.4.1`)
- `react@19.1.0` / `react-dom@19.1.0`
- `react-oidc-context@3.3.1` (peer: `oidc-client-ts ^3.1.0`, `react >=16.14.0`)
- `oidc-client-ts@3.5.0`

All five declare Node `>=18` or looser — none require Node 20+.

## Running it

```
npm install
npm run dev
```

Vite's dev server serves on `http://localhost:5173/` by default (confirmed
live). Before signing in, create the `riverside-health` realm and a public
`patient-portal` client in a running Keycloak instance (`docker compose up -d`
from `labs/docker/`, default `http://localhost:8080`) with a valid redirect
URI of `http://localhost:5173/*` — chapter 35's lab walks through this
exactly. `src/oidcConfig.js` already points at that plain default.

## Files

- `src/oidcConfig.js` — the `AuthProvider` config (authority, client_id,
  redirect_uri, scope). No client secret anywhere — this is a public client.
- `src/main.jsx` — wraps the app in `<AuthProvider>`.
- `src/App.jsx` — sign-in/sign-out UI and decoded ID token claims via
  `useAuth()`.
