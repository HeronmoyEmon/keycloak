# IAM & Keycloak Mastery

A self-built, offline-first course for going from **zero knowledge to IAM (Identity and Access Management)
specialist**, using Keycloak as the hands-on vehicle. It teaches the IAM concepts and the Keycloak
implementation together, chapter by chapter, so neither one is ever learned in a vacuum: every concept
chapter shows how Keycloak implements it, and every Keycloak chapter recalls which concept it's an
instance of.

Like [sde-prep](../sde-prep), this is a small, static HTML "book": chapters with diagrams, a searchable
glossary, graded quizzes, hands-on labs against a real local Keycloak instance, and three running case
studies (a SaaS company, a hospital network, a bank) that carry through the whole course. No backend, no
build step, no npm — just HTML, CSS, and vanilla JavaScript, so it works offline and opens straight from
the filesystem or any static host. (The hands-on labs need Docker and a JDK locally — see
[`labs/README.md`](labs/README.md) once the labs are built.)

## What's inside

This is one course in eleven parts, 50 chapters plus a capstone — see [`CLAUDE.md`](CLAUDE.md) for the
full curriculum table and content standards.

| Part | Focus |
|---|---|
| 0 | Orientation: how to use the course, environment setup |
| I | IAM Foundations: identity, AuthN/AuthZ, directories, MFA, authorization models, SSO/federation, tokens, threats |
| II | Identity Governance & Compliance: IGA, GDPR/SOC2/HIPAA/PCI-DSS, the IAM market (Keycloak vs. Okta vs. Auth0/Entra vs. Ping) |
| III | Protocols: OAuth 2.0, OpenID Connect, JWT & crypto, SAML 2.0, OAuth/OIDC security best practices |
| IV | Keycloak Core Administration: install, realms, users, roles/groups, clients, auth flows, themes |
| V | Keycloak Operations: admin REST API/CLI, events & auditing, sessions, brute-force & MFA config |
| VI | Federation & Integration: identity brokering, LDAP/AD federation, custom SPIs, securing a real backend/frontend/service |
| VII | Fine-Grained Authorization: Keycloak Authorization Services (UMA 2.0), ABAC/ReBAC patterns |
| VIII | Extending Keycloak: custom authenticators, event listeners, protocol mappers, identity providers |
| IX | Production, Scale & Hardening: HA/clustering, Kubernetes, performance, DR, security hardening, upgrades |
| X | Case Studies & Capstone: multi-tenant SaaS, healthcare (HIPAA), fintech (PCI-DSS), a full capstone build |
| XI | Career & Reference: the IAM specialist role, interview question bank, master glossary & cheat sheets |

This is a living, in-progress project — see [`BUILD_STATUS.md`](BUILD_STATUS.md) for exactly what's
built, what's next, and any open questions.

## Quick start (once the shell exists)

Because quiz scores and reading progress are saved with `localStorage`, serve the site over HTTP
rather than double-clicking files:

```bash
python3 -m http.server 8000
```

Then open `http://localhost:8000`. For the hands-on labs, each lab's own instructions bring up a local
Keycloak via Docker Compose — the deployed site stays static; only your own machine runs Keycloak.

## How progress and quizzes work

There's no account system and no server — every quiz score, roadmap checkbox, and lab checklist lives
only in **your own browser's `localStorage`**, under `iam:`-prefixed keys (so this project's data never
collides with `sde-prep`'s `v2:`/`v3:` keys if both are ever opened in the same browser). Clearing a
page's site data resets that page's progress.

## Project structure

```
keycloak/
├── index.html              course home
├── roadmap.html            phase-by-phase timeline with progress tracking
├── glossary.html           searchable master glossary (IAM + Keycloak terms)
├── quizzes.html            score dashboard
├── labs.html               hands-on lab index (Docker Compose Keycloak setups)
├── case-studies.html       case study index
├── chapters/               the 50 numbered chapters
├── labs/                   supporting code for hands-on exercises (docker-compose, sample apps, SPI source)
├── case-studies/           the 3 running case studies, full write-ups
├── assets/css, assets/js   the site's own design system and quiz engine (built fresh for this project)
├── tools/                  validator scripts (link checker, quiz/glossary checks)
├── reference/              seed material, including the original single-file README (see its own header)
├── CLAUDE.md               the build orchestration brief this project follows
└── BUILD_STATUS.md         build progress tracker and task queue
```

## Deploying

Any static host works — Vercel, Netlify, GitHub Pages, Cloudflare Pages. For Vercel: import this repo,
Framework Preset "Other", Root Directory = repo root, no build command.

## Sources and credit

Each chapter is an original digest, written in its own words, of specific official documentation, RFCs,
and well-known references — named at the top of the chapter. These are study notes for personal
learning, not a replacement for the primary sources.
