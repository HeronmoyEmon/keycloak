import { useState } from "react";
import { useAuth } from "react-oidc-context";
import { billingApiBaseUrl } from "./oidcConfig";
import "./App.css";

// Northwind Apps' customer dashboard -- the capstone's real build of the "northwind-spa" client
// chapter 24 named but never built. react-oidc-context's useAuth() drives the real
// authorization-code + PKCE round trip against the capstone's Keycloak instance; once signed in,
// this calls the real billing-api (this capstone's own, adapted from chapter 34's lab) using the
// access token react-oidc-context already holds -- no separate login for the API, the same token
// that got this page past Keycloak is the same bearer token billing-api validates.
function App() {
  const auth = useAuth();
  const [reportResult, setReportResult] = useState(null);
  const [invoicesResult, setInvoicesResult] = useState(null);

  async function callBillingApi(path, setResult) {
    setResult({ loading: true });
    try {
      const res = await fetch(`${billingApiBaseUrl}${path}`, {
        headers: { Authorization: `Bearer ${auth.user.access_token}` },
      });
      const body = await res.json();
      setResult({ status: res.status, body });
    } catch (err) {
      setResult({ error: err.message });
    }
  }

  if (auth.isLoading) {
    return <p className="status">Loading Northwind Apps dashboard session...</p>;
  }

  if (auth.error) {
    return (
      <div className="card card--error">
        <h1>Northwind Apps Dashboard</h1>
        <p className="status status--error">Authentication error: {auth.error.message}</p>
        <button onClick={() => auth.signinRedirect()}>Try signing in again</button>
      </div>
    );
  }

  if (auth.isAuthenticated) {
    const { profile } = auth.user;
    return (
      <div className="card card--authenticated">
        <h1>Northwind Apps Dashboard</h1>
        <p className="status status--ok">
          Signed in as <strong>{profile.preferred_username ?? profile.sub}</strong>
        </p>
        <dl className="claims">
          <dt>sub</dt>
          <dd>{profile.sub}</dd>
          <dt>tenant_id (custom claim, chapter 39's SPI)</dt>
          <dd>{profile.tenant_id ?? "(not present on this token)"}</dd>
          <dt>realm roles</dt>
          <dd>{(auth.user.profile?.realm_access?.roles ?? []).join(", ") || "(none)"}</dd>
          <dt>exp</dt>
          <dd>{new Date(profile.exp * 1000).toLocaleString()}</dd>
        </dl>

        <div className="billing-actions">
          <button onClick={() => callBillingApi("/api/invoices", setInvoicesResult)}>
            GET /api/invoices (needs billing-reader or billing-admin)
          </button>
          <button onClick={() => callBillingApi("/api/invoices/premium-report", setReportResult)}>
            GET /api/invoices/premium-report (real UMA 2.0 check, needs billing-admin)
          </button>
        </div>

        {invoicesResult && (
          <pre className="result">
            invoices: HTTP {invoicesResult.status}{"\n"}
            {JSON.stringify(invoicesResult.body, null, 2)}
          </pre>
        )}
        {reportResult && (
          <pre className="result">
            premium-report: HTTP {reportResult.status}{"\n"}
            {JSON.stringify(reportResult.body, null, 2)}
          </pre>
        )}

        <button onClick={() => auth.removeUser()}>Sign out</button>
      </div>
    );
  }

  return (
    <div className="card">
      <h1>Northwind Apps Dashboard</h1>
      <p className="status">You are not signed in.</p>
      <button onClick={() => auth.signinRedirect()}>Sign in with Keycloak</button>
    </div>
  );
}

export default App;
