import { useAuth } from "react-oidc-context";
import "./App.css";

// Riverside Health's patient portal: the minimal shell this chapter's lab
// actually builds and runs. react-oidc-context's useAuth() hook exposes the
// same authorization-code + PKCE round trip chapters 13/15 taught by hand --
// isAuthenticated, user.profile (decoded ID token claims), and
// signinRedirect()/removeUser() driving the real /authorize and /token calls.
function App() {
  const auth = useAuth();

  if (auth.isLoading) {
    return <p className="status">Loading patient portal session...</p>;
  }

  if (auth.error) {
    return (
      <div className="card card--error">
        <h1>Riverside Health Patient Portal</h1>
        <p className="status status--error">Authentication error: {auth.error.message}</p>
        <button onClick={() => auth.signinRedirect()}>Try signing in again</button>
      </div>
    );
  }

  if (auth.isAuthenticated) {
    const { profile } = auth.user;
    return (
      <div className="card card--authenticated">
        <h1>Riverside Health Patient Portal</h1>
        <p className="status status--ok">
          Signed in as <strong>{profile.preferred_username ?? profile.sub}</strong>
        </p>
        <dl className="claims">
          <dt>sub</dt>
          <dd>{profile.sub}</dd>
          <dt>email</dt>
          <dd>{profile.email ?? "(email scope not granted)"}</dd>
          <dt>iss</dt>
          <dd>{profile.iss}</dd>
          <dt>exp</dt>
          <dd>{new Date(profile.exp * 1000).toLocaleString()}</dd>
        </dl>
        <button onClick={() => auth.removeUser()}>Sign out</button>
      </div>
    );
  }

  return (
    <div className="card">
      <h1>Riverside Health Patient Portal</h1>
      <p className="status">You are not signed in.</p>
      <button onClick={() => auth.signinRedirect()}>Sign in with Keycloak</button>
    </div>
  );
}

export default App;
