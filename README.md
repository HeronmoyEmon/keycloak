# IAM & Keycloak Mastery

A self-built, offline-first course for going from **zero knowledge to IAM (Identity and Access Management)
specialist**, using Keycloak as the hands-on vehicle. It teaches the IAM concepts and the Keycloak
implementation together, chapter by chapter, so neither one is ever learned in a vacuum: every concept
chapter shows how Keycloak implements it, and every Keycloak chapter recalls which concept it's an
instance of.

Like [sde-prep](../sde-prep), this is a small, static HTML "book": chapters with diagrams, a searchable
glossary, graded quizzes, hands-on labs against a real local Keycloak instance, five milestone exams,
three running case studies (a SaaS company, a hospital network, a bank) that carry through the whole
course, and a real, running multi-app capstone build. No backend, no build step, no npm — just HTML,
CSS, and vanilla JavaScript, so it works offline and opens straight from the filesystem or any static
host.

**Every lab is fully self-contained on the page itself** — every file a lab needs (compose files,
Java/React/Spring source, configs) is pasted inline with a copy button and a "files you'll need"
checklist, so following a lab never requires cloning this repository. The only local requirements are
Docker, a JDK, Node, and `curl` (chapter 0 covers environment setup) — nothing from this repo's own
`labs/`/`capstone/` folders is ever assumed.

## What's inside

This is one course in eleven parts — 50 chapters, a capstone, five milestone exams, and three
full case-study write-ups — see [`CLAUDE.md`](CLAUDE.md) for the full curriculum table and content
standards.

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

All 50 chapters, the capstone, the 5 exams, and the 3 case studies are built and verified — see
[`BUILD_STATUS.md`](BUILD_STATUS.md) for the full build log, every real bug found and fixed along the
way, and the current Final QA pass.

## Quick start

Because quiz scores and reading progress are saved with `localStorage`, serve the site over HTTP
rather than double-clicking files:

```bash
python3 -m http.server 8000
```

Then open `http://localhost:8000`. For the hands-on labs, each chapter's own lab section gives you
every file you need directly on the page — create them in a new local folder and follow that chapter's
own commands; the deployed site stays static, and only your own machine ever runs Keycloak.

## How progress and quizzes work

There's no account system and no server — every quiz score, roadmap checkbox, and lab checklist lives
only in **your own browser's `localStorage`**, under `iam:`-prefixed keys (so this project's data never
collides with `sde-prep`'s `v2:`/`v3:` keys if both are ever opened in the same browser). Clearing a
page's site data resets that page's progress.

## Project structure

```
keycloak/
├── index.html              course home (visual roadmap)
├── roadmap.html            phase-by-phase pace tracker with progress tracking
├── glossary.html           searchable master glossary (670+ IAM + Keycloak terms)
├── quizzes.html            score dashboard
├── labs.html               hands-on lab overview
├── case-studies.html       case study index
├── capstone.html           the real, running multi-app capstone build
├── exams.html              the 5 milestone exams index
├── exams/                  exam-foundations/protocols/keycloak-core/integration/final
├── chapters/               the 50 numbered chapters — every lab is fully self-contained on its own page
├── labs/                   this repo's own copy of each lab's source (not needed to follow a lab — see above)
├── capstone/               this repo's own copy of the capstone's source (same note)
├── case-studies/           the 3 running case studies, full write-ups
├── assets/css, assets/js   the site's own design system and quiz engine (built fresh for this project)
├── tools/                  check_links.py, validate_quiz.py, validate_glossary.py
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
