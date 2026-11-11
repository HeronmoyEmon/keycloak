// OIDC / PKCE configuration for react-oidc-context's AuthProvider.
//
// This is the READER-FACING default: it points at the plain `docker compose up -d`
// Keycloak instance from labs/docker/docker-compose.yml, running on the default
// http://localhost:8080, in a realm/client the reader creates themselves in this
// chapter's lab steps (realm "riverside-health", public client "patient-portal").
//
// For the chapter author's own isolated verification run (ch35kc project, remapped
// to host port 18135), this value was temporarily overridden to
// "http://localhost:18135" via a local, uncommitted edit -- never commit a
// non-default authority here. See chapter 35's "Common mistakes" slide for why an
// exact, port-correct redirect_uri matters as much as the authority itself.
export const oidcConfig = {
  authority: "http://localhost:8080/realms/riverside-health",
  client_id: "patient-portal",
  redirect_uri: "http://localhost:5173/",
  post_logout_redirect_uri: "http://localhost:5173/",
  // No client_secret anywhere in this object -- this is a PUBLIC client. A secret
  // embedded in a browser bundle would be readable by anyone who opens devtools,
  // so Keycloak's "Client authentication: Off" + "PKCE required" is the only
  // correct combination for a browser-based SPA (chapter 24 covers the admin
  // console fields; this chapter is where the consequence becomes concrete).
  scope: "openid profile email",
  // Keep tokens in memory, not localStorage/sessionStorage -- see this chapter's
  // "token storage tradeoffs" slide for why. oidc-client-ts defaults to an
  // in-memory WebStorageStateStore when none is supplied, which is what we want
  // here, so no userStore override is set.
  onSigninCallback: () => {
    // Strip the ?code=...&state=... the authorization server appended to our
    // redirect_uri so a page refresh doesn't try to replay a single-use code.
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
