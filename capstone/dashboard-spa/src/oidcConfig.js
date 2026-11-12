// OIDC / PKCE configuration for react-oidc-context's AuthProvider, adapted from chapter 35's
// patient-portal lab for the capstone: this points at the capstone's own isolated Keycloak
// instance (capstone/docker-compose.yml, project capstonekc, host port 18200), realm
// northwind-apps, the real public client "northwind-spa" -- named but never built with real
// running code until this capstone (chapter 24 named it only as a forward pointer; chapter 35's
// own concrete lab built "patient-portal" against Riverside Health instead).
export const oidcConfig = {
  authority: "http://localhost:18200/realms/northwind-apps",
  client_id: "northwind-spa",
  redirect_uri: "http://localhost:5173/",
  post_logout_redirect_uri: "http://localhost:5173/",
  // PUBLIC client -- no secret in this object. Client authentication: Off + PKCE required is
  // the only correct combination for a browser-based SPA (chapter 24/35).
  scope: "openid profile email",
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};

export const billingApiBaseUrl = "http://localhost:8081";
