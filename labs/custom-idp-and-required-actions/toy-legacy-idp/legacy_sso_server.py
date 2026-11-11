#!/usr/bin/env python3
"""A tiny, deliberately minimal stand-in for Cascade Savings Bank's "Cascade SSO" legacy proprietary
single-sign-on system -- a toy protocol invented for chapter 40 of the IAM & Keycloak Mastery course,
NOT a real named vendor product. It exists purely so the chapter's custom Identity Provider SPI
(MeridianLegacySsoIdentityProvider) has something real to redirect to and get a real callback from.

Protocol (teaching-grade, not production-grade -- see the chapter's "common mistakes" section):

  GET /legacy-sso/authorize?client_id=...&redirect_uri=...&state=...
      Serves a plain HTML login form. The redirect_uri and state are carried through as hidden form
      fields so they survive the POST below.

  POST /legacy-sso/authorize  (same path)
      Body: username, password, client_id, redirect_uri, state.
      On a correct username/password match against this server's own in-memory user table (standing
      in for Cascade's real, un-migrated legacy user store -- this is exactly the kind of system an
      acquired institution keeps running after an acquisition, chapter 11's compliance chapter and
      chapter 9's threat-landscape chapter both flag "acquired-and-unaudited" systems like this one as
      a real risk category), responds with a 302 redirect to:
          {redirect_uri}?legacy_user=...&legacy_email=...&legacy_first_name=...&legacy_last_name=...
              &state=...&ts=<unix-seconds>&sig=<hex HMAC-SHA256>
      sig is HMAC-SHA256, keyed with a shared secret given to this process on the command line, over
      the exact string "{legacy_user}|{legacy_email}|{state}|{ts}" -- the same message format and
      algorithm MeridianLegacySsoIdentityProvider's Endpoint.callback() verifies on the Keycloak side.
      On a wrong username/password, re-renders the login form with an error message instead.

  GET /health
      Plain 200 OK, used by the lab's own readiness check before the browser-side round trip starts.

Run:
  python3 legacy_sso_server.py --port 18141 --secret <shared-secret>

Deliberately standard-library only (http.server, hmac, hashlib) -- no pip install needed, matching
this course's "Python 3.12 is available" baseline and keeping the lab's toy IdP auditable in one file.
"""
import argparse
import hashlib
import hmac
import html
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlencode, urlparse, parse_qs

# Cascade Savings Bank's own (toy, plaintext-for-teaching-clarity) legacy user table. A real 20-
# year-old proprietary SSO system's credential storage is exactly the kind of thing an acquiring
# bank's IAM specialist has to assess and eventually retire -- this plaintext dict is standing in for
# that same real-world "inherited, not-ideal, but still authoritative" legacy credential store chapter
# 33's CSV-backed staff directory already modeled for a different org.
CASCADE_USERS = {
    "jmartinez": {
        "password": "CascadeP@ss1",
        "email": "j.martinez@cascadesavings.example",
        "first_name": "Javier",
        "last_name": "Martinez",
    },
    "rwong": {
        "password": "CascadeP@ss2",
        "email": "r.wong@cascadesavings.example",
        "first_name": "Rachel",
        "last_name": "Wong",
    },
}

SECRET = None  # set from --secret at startup


def sign(legacy_user, legacy_email, state, ts):
    message = f"{legacy_user}|{legacy_email}|{state}|{ts}".encode("utf-8")
    return hmac.new(SECRET.encode("utf-8"), message, hashlib.sha256).hexdigest()


LOGIN_FORM_TEMPLATE = """<!doctype html>
<html><head><title>Cascade SSO (toy) &ndash; Sign in</title></head>
<body style="font-family: sans-serif; max-width: 420px; margin: 60px auto;">
<h2>Cascade SSO <small>(toy stand-in, chapter 40 lab)</small></h2>
{error}
<form method="POST" action="/legacy-sso/authorize">
  <input type="hidden" name="client_id" value="{client_id}">
  <input type="hidden" name="redirect_uri" value="{redirect_uri}">
  <input type="hidden" name="state" value="{state}">
  <p><label>Username<br><input type="text" name="username" autofocus></label></p>
  <p><label>Password<br><input type="password" name="password"></label></p>
  <p><button type="submit">Sign in to Cascade SSO</button></p>
</form>
</body></html>
"""


class Handler(BaseHTTPRequestHandler):
    server_version = "ToyCascadeSSO/1.0"

    def log_message(self, fmt, *args):
        print("[toy-legacy-idp] " + (fmt % args))

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/health":
            self._respond(200, b"ok", "text/plain")
            return
        if parsed.path == "/legacy-sso/authorize":
            qs = parse_qs(parsed.query)
            client_id = html.escape(qs.get("client_id", [""])[0])
            redirect_uri = html.escape(qs.get("redirect_uri", [""])[0])
            state = html.escape(qs.get("state", [""])[0])
            body = LOGIN_FORM_TEMPLATE.format(
                error="", client_id=client_id, redirect_uri=redirect_uri, state=state)
            self._respond(200, body.encode("utf-8"), "text/html")
            return
        self._respond(404, b"not found", "text/plain")

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path != "/legacy-sso/authorize":
            self._respond(404, b"not found", "text/plain")
            return
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length).decode("utf-8")
        form = parse_qs(body)
        username = form.get("username", [""])[0]
        password = form.get("password", [""])[0]
        client_id = form.get("client_id", [""])[0]
        redirect_uri = form.get("redirect_uri", [""])[0]
        state = form.get("state", [""])[0]

        user = CASCADE_USERS.get(username)
        if user is None or user["password"] != password:
            error_html = '<p style="color:red">Incorrect Cascade username or password.</p>'
            page = LOGIN_FORM_TEMPLATE.format(
                error=error_html,
                client_id=html.escape(client_id),
                redirect_uri=html.escape(redirect_uri),
                state=html.escape(state))
            self._respond(200, page.encode("utf-8"), "text/html")
            return

        ts = str(int(time.time()))
        sig = sign(username, user["email"], state, ts)
        params = {
            "legacy_user": username,
            "legacy_email": user["email"],
            "legacy_first_name": user["first_name"],
            "legacy_last_name": user["last_name"],
            "state": state,
            "ts": ts,
            "sig": sig,
        }
        location = redirect_uri + ("&" if "?" in redirect_uri else "?") + urlencode(params)
        self.send_response(302)
        self.send_header("Location", location)
        self.end_headers()

    def _respond(self, status, body_bytes, content_type):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body_bytes)))
        self.end_headers()
        self.wfile.write(body_bytes)


def main():
    parser = argparse.ArgumentParser(description="Toy Cascade SSO legacy IdP stand-in")
    parser.add_argument("--port", type=int, default=18141)
    parser.add_argument("--secret", required=True)
    parser.add_argument("--host", default="0.0.0.0")
    args = parser.parse_args()

    global SECRET
    SECRET = args.secret

    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print(f"[toy-legacy-idp] Cascade SSO (toy) listening on http://{args.host}:{args.port}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
