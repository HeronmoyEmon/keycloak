# dashboard-spa — Northwind Apps dashboard (capstone)

Adapted from chapter 35's `labs/react-spa` lab (same toolchain, same `react-oidc-context` wiring) for
the capstone: this is the real build of **`northwind-spa`**, the public PKCE client chapter 24 only
named and never built with real running code — chapter 35's own concrete lab built `patient-portal`
against Riverside Health instead. See `../README.md` (the capstone's own README) and `../../capstone.html`
for the full architecture and verification write-up.

## Toolchain pins (unchanged from chapter 35 — still correct on this machine)

This machine runs **Node v18.20.4**. `vite@6.4.6` / `@vitejs/plugin-react@4.7.0` / `react@19.1.0` /
`react-oidc-context@3.3.1` / `oidc-client-ts@3.5.0` — all declare Node `>=18` or looser. Re-verified for
the capstone: `npm install` and `npm run build` both ran cleanly with Node 18.20.4, no engine warnings.

## Running it

```
npm install
npm run dev -- --port 5173
```

Before signing in, bring up the capstone's own Keycloak stack and seed the `northwind-apps` realm (see
`../README.md`) — this app points at `http://localhost:18200/realms/northwind-apps`, client
`northwind-spa`, not chapter 35's `riverside-health`/`patient-portal`.

## Files

- `src/oidcConfig.js` — the `AuthProvider` config (authority, client_id, redirect_uri, scope) plus the
  capstone's `billingApiBaseUrl`. No client secret anywhere — this is a public client.
- `src/main.jsx` — wraps the app in `<AuthProvider>`.
- `src/App.jsx` — sign-in/sign-out UI, decoded token claims (including the custom `tenant_id` claim),
  and two buttons that call the real `billing-api` with the access token `react-oidc-context` holds.
