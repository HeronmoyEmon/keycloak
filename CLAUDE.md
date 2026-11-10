# Build orchestration: IAM & Keycloak Mastery

You are building one static HTML course, defined by this file. Work spans many sessions because of
context limits. This file defines the vision, scope, content standards, the full curriculum, and the
session routine. `BUILD_STATUS.md` (in this folder) is the persistent record of progress and the task
queue — it is the thing to read first in every session, not this file in full (see §7).

## 1. Vision and who this is for

The user has **zero IAM background** and wants to become an **IAM specialist** — someone who can design,
operate, and secure identity systems professionally, not just click around an admin console. Keycloak is
the *vehicle*, not the destination: every concept chapter must show how Keycloak implements it, and every
Keycloak chapter must recall which general IAM concept it is an instance of. A reader who only did the
Keycloak chapters and skipped the concept chapters should come away unable to explain *why* Keycloak is
built the way it is — that's the failure mode this course exists to prevent.

Treat this like a paid, professional course, not a wiki page: depth, worked examples, hands-on labs
against a real local Keycloak instance, graded quizzes, a running set of case studies, and a defensible
capstone project a reader could put on a resume or walk through in an interview.

## 2. Non-negotiable content rules

1. **No invented facts.** Protocol/spec versions, current Keycloak version, current OWASP/compliance
   framework details must be live-verified before writing, the same discipline `sde-prep` uses. If a
   source disagrees with itself or can't be confirmed, say so in the chapter rather than asserting a
   precise claim.
2. **Concept ↔ implementation bridge, every chapter.** Concept chapters (Parts I–III) get an "In
   Keycloak" callout box per major concept, pointing at the exact admin-console screen or config field.
   Keycloak chapters (Parts IV+) open with a one-paragraph "What concept is this?" recall.
3. **Three running case-study orgs**, used for every scenario/example across all 50 chapters instead of
   one-off made-up companies, so the reader builds a mental model that compounds:
   - **Northwind Apps** — a B2B SaaS company (multi-tenant, needs CIAM + enterprise SSO for customers)
   - **Riverside Health** — a hospital network (HIPAA, patient portal, clinician workforce IAM)
   - **Meridian Trust** — a retail bank (PCI-DSS, step-up auth, fraud-aware authentication)
   These are the same three throughlines `sde-prep`'s V1 book uses (fintech/healthcare blocks) —
   deliberately, so the user's existing mental model transfers, but give each org its own name/identity
   here rather than referencing `sde-prep` directly (this site must stand alone).
4. **Original digests only.** Each chapter is written in this project's own words from primary sources
   (RFCs, official docs, named books/standards) — never copied text, per the same rule `sde-prep` follows.
5. **Hands-on is real, not narrated.** Where a chapter has a lab, the reader runs actual commands against
   an actual local Keycloak (Docker Compose) and gets actual output — don't fake a transcript. Whoever
   builds the chapter must actually run it first (Docker 29.x, Java 21, Python 3.12, Node 18 are
   confirmed available on the dev machine as of 2026-11-10).
6. **Clean room on assets.** Do not copy files from `~/Learning/sde-prep`. Build this site's own
   `assets/css`/`assets/js` from scratch — same proven architecture (static HTML, vanilla JS, no build
   step, localStorage-based progress, light/dark/print themes, a generic quiz engine) but original code
   and IAM/Keycloak-appropriate visual identity, not a copy-paste.

## 3. Repo structure

```
keycloak/
├── index.html, roadmap.html, glossary.html, quizzes.html, labs.html, case-studies.html
├── exams/                      exam-foundations.html, exam-protocols.html, exam-keycloak-core.html,
│                                exam-integration.html, exam-final.html
├── chapters/NN-slug.html       the 50 numbered chapters (00-50, see §5)
├── labs/                       docker-compose.yml templates, sample app code, SPI Java source,
│                                one subfolder per lab, referenced by the chapters that need them
├── case-studies/cs0N-slug.html the 3 running case studies, full write-ups (one per org in §2.3)
├── assets/css/book.css, print.css
├── assets/js/book.js, quiz.js, nav-data.js, glossary-data.js, quiz-data.js, roadmap-data.js
├── tools/check_links.py, validate_quiz.py, validate_glossary.py
├── reference/                  seed material (the original README, now `keycloak-admin-notes-seed.md`)
├── CLAUDE.md                   this file
├── BUILD_STATUS.md             progress tracker and task queue
└── README.md
```

Chapter pages live in `chapters/`, not `books/` (that's `sde-prep` V1's convention — this project has no
reason to match it since there's no shared chrome between the two sites).

## 4. Per-chapter content template and minimums

Every chapter page includes, in this order:

1. **Header**: chapter number/title, estimated minutes, which Part it belongs to, source(s) digested.
2. **Why this matters**: 2-4 sentences grounding the chapter in a real IAM specialist's job, not trivia.
3. **Concept sections**: prose + tables + diagrams. Concept chapters get "In Keycloak" callouts; Keycloak
   chapters get a "What concept is this?" recall box.
4. **Case-study tie-in**: at least one of Northwind/Riverside/Meridian worked into the chapter concretely
   (not just name-dropped).
5. **Hands-on lab** (where applicable — see §5's chapter table for which chapters get one): numbered
   steps against a real local Keycloak via that chapter's `labs/` folder, with real command output.
6. **Common mistakes / pitfalls**: things a beginner gets wrong, framed as "if you see X, the cause is Y".
7. **Takeaways**: 8+ bullets.
8. **Self-check**: 5+ open-ended questions (no answer shown inline — thinking prompts).
9. **Quiz pool**: 15+ questions in `quiz-data.js`, mixing concept/scenario/bug-spotting/compare types,
   at least 50% scenario-framed (tie to the three orgs).
10. **Deep dive links**: real links to RFCs/official docs only — verify they resolve.

**Minimums**: 3+ diagrams per chapter (inline SVG, accessible — title+desc, legible in light/dark/print),
15+ new glossary terms for a full topic chapter (operational/shorter chapters like 00 or the exam-prep
chapters can be lower — judgment call, note it), 15+ quiz questions, 8+ takeaways, 5+ self-check. Check
new glossary terms against the existing master glossary for name collisions before adding — extend an
existing term's `chapters` array instead of duplicating, same rule `sde-prep` follows.

**Diagrams**: inline SVG, not ASCII art (the existing seed README used ASCII boxes — fine for a first
draft, not for the shipped chapter; redraw as real SVG). Each diagram needs an accessible title/desc and
must read correctly in light, dark, and print themes.

## 5. Full curriculum (50 chapters + capstone)

Chapter numbers below are fixed identifiers for `nav-data.js`/cross-links. One-line descriptions here are
enough to plan and sequence the queue; **write each chapter's full brief into `BUILD_STATUS.md` when it's
queued up next**, the same "build each part just before you need it" discipline `sde-prep` uses — don't
front-load 50 detailed briefs into this file. Detailed briefs for the first reference set (Part 0 + Part I)
are in §6 since those are built first.

### Part 0 — Orientation
| # | Chapter | Lab? |
|---|---|---|
| 00 | How to use this course, grading, and environment setup (Docker/Java/Postman) | — |

### Part I — IAM Foundations (concept-only, vendor-agnostic)
| # | Chapter | Lab? |
|---|---|---|
| 01 | What is IAM? The business problem it solves | — |
| 02 | Identity fundamentals: identity, principal, subject, digital identity lifecycle | — |
| 03 | AuthN vs. AuthZ vs. Accounting (AAA), and audit | — |
| 04 | Directory services & identity stores (LDAP/X.500, Active Directory concepts) | — |
| 05 | Authentication factors & MFA (OTP, WebAuthn/FIDO2/passkeys, risk-based auth) | — |
| 06 | Authorization models (ACL, RBAC, ABAC, PBAC, ReBAC, Zero Trust) | — |
| 07 | SSO & federation concepts (IdP/SP/RP, brokering, JIT provisioning, trust) | — |
| 08 | Tokens & sessions, conceptually (bearer tokens, claims, revocation problem) | — |
| 09 | IAM threat landscape (credential stuffing, phishing, token theft/replay, MFA fatigue) | — |

### Part II — Identity Governance & Compliance
| # | Chapter | Lab? |
|---|---|---|
| 10 | Identity governance & administration (IGA): provisioning, SoD, joiner-mover-leaver | — |
| 11 | Compliance for IAM (GDPR, SOC2, HIPAA, PCI-DSS, ISO 27001 — what each demands of IAM) | — |
| 12 | The IAM market landscape: Workforce IAM vs. CIAM vs. PAM vs. IGA; Keycloak vs. Okta vs. Auth0/Entra vs. Ping/ForgeRock | — |

### Part III — Protocols deep dive
| # | Chapter | Lab? |
|---|---|---|
| 13 | OAuth 2.0 core: roles, grant types, authorization code + PKCE | curl-only, no Keycloak yet |
| 14 | OAuth 2.0 extended: device flow, client credentials, introspection/revocation, token exchange | curl-only |
| 15 | OpenID Connect: ID tokens, discovery, UserInfo, scopes & claims | curl-only |
| 16 | JWT & applied crypto: JWS/JWE/JWK, signing vs. encryption, key rotation, JWKS | — |
| 17 | SAML 2.0: assertions, bindings, SP/IdP-initiated flows, metadata; contrast with OIDC | — |
| 18 | OAuth/OIDC security best practices (the BCP RFCs, PKCE-everywhere, mix-up/CSRF defenses, token storage pitfalls) | — |

### Part IV — Keycloak core administration (Keycloak enters here)
| # | Chapter | Lab? |
|---|---|---|
| 19 | Keycloak architecture & deployment options (Quarkus distribution, DB backends) | — |
| 20 | Installing & running Keycloak (Docker Compose, production mode basics) | **yes — first real Keycloak lab** |
| 21 | Realms & realm settings | yes |
| 22 | Users & credentials management | yes |
| 23 | Roles & groups | yes |
| 24 | Clients, client scopes & protocol mappers | yes |
| 25 | Authentication flows & required actions (custom flow design) | yes |
| 26 | Themes, branding & localization | yes |

### Part V — Keycloak operations
| # | Chapter | Lab? |
|---|---|---|
| 27 | Admin REST API & kcadm CLI (automation) | yes |
| 28 | Events, auditing & monitoring (metrics, health checks, SIEM-style export) | yes |
| 29 | Sessions deep dive & SSO behavior in Keycloak | yes |
| 30 | Password policies, brute-force protection & MFA config in Keycloak | yes |

### Part VI — Federation & integration
| # | Chapter | Lab? |
|---|---|---|
| 31 | Identity brokering (social login, external OIDC/SAML IdPs) | yes |
| 32 | User federation: LDAP & Active Directory | yes (containerized LDAP) |
| 33 | Custom User Storage SPI (Java hands-on) | yes |
| 34 | Securing a real backend API (Spring Boot resource server) | yes |
| 35 | Securing a real frontend SPA (React + OIDC/PKCE) | yes |
| 36 | Service-to-service / machine-to-machine auth (client credentials, service accounts) | yes |

### Part VII — Fine-grained authorization
| # | Chapter | Lab? |
|---|---|---|
| 37 | Keycloak Authorization Services (UMA 2.0: resources, scopes, policies, permissions) | yes |
| 38 | Fine-grained access patterns: ABAC/ReBAC with Keycloak, brief OPA/Cedar comparison | — |

### Part VIII — Extending Keycloak
| # | Chapter | Lab? |
|---|---|---|
| 39 | Writing custom SPIs: authenticators, event listeners, protocol mappers (Java) | yes |
| 40 | Custom identity providers & required actions (advanced SPI) | yes |

### Part IX — Production, scale & hardening
| # | Chapter | Lab? |
|---|---|---|
| 41 | High availability & clustering (Infinispan caching, multi-node) | partial (single-node sim) |
| 42 | Keycloak on Kubernetes (Operator, Helm) | optional (kind/minikube) |
| 43 | Performance tuning & database considerations | — |
| 44 | Backup, disaster recovery & multi-region strategies | — |
| 45 | Security hardening checklist & upgrade/migration strategy | — |

### Part X — Case studies & capstone
| # | Chapter | Lab? |
|---|---|---|
| 46 | Case study: Northwind Apps — multi-tenant SaaS IAM design | full case-studies/ write-up |
| 47 | Case study: Riverside Health — HIPAA patient portal with step-up auth | full case-studies/ write-up |
| 48 | Case study: Meridian Trust — PCI-DSS banking app, fraud-aware auth | full case-studies/ write-up |
| — | **Capstone**: build and defend one multi-app Keycloak-secured system end to end | own project, not a chapter page |

### Part XI — Career & reference
| # | Chapter | Lab? |
|---|---|---|
| 49 | Becoming an IAM specialist: skills matrix, career paths, certifications, interview question bank | — |
| 50 | IAM & Keycloak master glossary and cheat sheets (reference chapter, feeds `glossary.html`) | — |

## 6. Detailed briefs for the reference set (build these first)

These establish the template (diagram style, quiz pattern, glossary mechanics, "In Keycloak" callout
format) before scaling to 50 chapters — same reason `sde-prep` builds one reference chapter per volume
before mass-producing. **GATE-1** sits right after chapter 02 (see §8).

- **Ch 00 — How to use this course.** What "mastery" means here, how grading/quizzes work, environment
  setup (Docker Desktop or Docker Engine, a JDK, `curl`/Postman, a browser), a map of the 11 parts, and
  how the three case-study orgs will recur. 2+ diagrams (course map, study-loop flow), no quiz-pool
  minimum below 10 is fine here (it's orientation, not a topic chapter) — flag the lower count in
  `BUILD_STATUS.md` rather than forcing padding.
- **Ch 01 — What is IAM?** The business problem (10 apps each re-implementing login), AuthN/AuthZ in one
  sentence each, why centralizing matters, pros/cons of adopting an IAM system, when *not* to use one
  (the seed README's "Why Keycloak Exists"/"Pros and Cons"/"When to use" tables are decent source
  material — mine them, rewrite in this project's own words, and generalize beyond Keycloak specifically
  since this chapter is vendor-agnostic).
- **Ch 02 — Identity fundamentals.** Identity vs. identifier vs. credential vs. attribute; principal vs.
  subject; the identity lifecycle (create → activate → modify → suspend → delete) generalized from the
  seed README's "User Lifecycle" diagram but reframed as a vendor-agnostic lifecycle, not Keycloak's
  specific one (that comes back concretely in chapter 22). Introduce Northwind/Riverside/Meridian here as
  the three running orgs, with one paragraph each on what kind of identities they manage (SaaS customers
  + internal staff; clinicians + patients; bank employees + retail customers).

Everything from chapter 03 onward: draft the full brief into `BUILD_STATUS.md`'s queue entry when that
chapter comes up next, following the one-line description in §5 plus the per-chapter template in §4.

## 7. Session routine

### At the start of every session
1. Read `BUILD_STATUS.md` first. Only read this file's relevant sections (not the whole thing) for the
   current unit — most sessions need just §4 (template/minimums) and the queue entry's own brief.
2. If "Current unit" shows work in progress, resume it; the disk is the truth if it disagrees with the
   status file.
3. Otherwise take the next unblocked queue item and confirm it in one line in chat.
4. Skim one finished chapter as a style reference once a few exist.

### While working
- A **unit** is one chapter with all its parts (diagrams, glossary entries, quiz pool, lab if it has one),
  or one shell page (index/roadmap/glossary/quizzes/labs/case-studies index), one lab's supporting code,
  one case study, or one QA pass.
- Before starting a unit, write it under "Current unit" in `BUILD_STATUS.md` with a short checklist; tick
  off steps as completed, not only at the end.
- Edit shared data files (`glossary-data.js`, `quiz-data.js`, `nav-data.js`, `roadmap-data.js`) with
  targeted edits/appends, never a full rewrite. Check for duplicate terms/ids first.
- After each unit, run `tools/check_links.py` and `tools/validate_quiz.py` (once they exist) and fix what
  they find.
- Commit after each unit if this is a git repository (it is) with a message like `ch03: AAA and audit
  complete`. Only push when the user explicitly says to.

### After each unit
Move the unit to "Done" in `BUILD_STATUS.md` with the date and a one-line note (figure/glossary/quiz
counts, anything deviating from the brief). Set the next item.

### Managing context
After about two units, or after automatic context compaction, finish the current checklist step, update
`BUILD_STATUS.md`, and stop at a clean point: "Stopped at a clean point. Next: <item>. Start a new session
and say 'continue'."

## 8. Gates

A gate is a hard stop for review. It is approved only when the user explicitly says so in chat (e.g.
"approve GATE-1") — never self-approve.

- **GATE-1 (template gate)**: after chapters 00-02 + a first pass of `assets/css`/`assets/js`. Confirms
  visual design, diagram style, quiz engine, glossary mechanics, and the "In Keycloak" callout format
  before scaling to 50 chapters.
- **GATE-2 (Part I complete)**: after chapters 03-09 plus `index.html`/`roadmap.html`/`glossary.html`/
  `quizzes.html`. Confirms the foundations arc reads well standalone before moving into governance/protocols.
- **GATE-3 (protocols complete)**: after Part III (ch 13-18). Confirms protocol depth is right before any
  Keycloak-specific content is written — Keycloak chapters will assume this depth, not re-teach it.
- **GATE-4 (can operate Keycloak)**: after Part IV (ch 19-26). The first milestone where the reader can
  actually run and administer a Keycloak realm. First real hands-on labs live here — test every command
  for real before shipping it.
- **GATE-5 (can integrate real apps)**: after Parts VI-VII (ch 31-38). The reader can now secure a real
  backend, frontend, and service account, and configure fine-grained authorization.
- **Final QA gate**: after the capstone and Part XI, before calling v1.0 complete — full link check, full
  quiz/glossary validation, every lab re-run from a clean Docker state, every "In Keycloak" callout spot-
  checked against a live admin console.

## 9. Data file schemas

- `nav-data.js`: `window.COURSE_NAV = [{number, slug, title, part, minutes, blurb, built, hasLab}]`
- `glossary-data.js`: `window.GLOSSARY = [{term, definition, category, chapters: [slug, ...]}]`
- `quiz-data.js`: `window.QUIZ_POOLS = {slug: [{id, type, question, options, answerIndex, explanation}]}`
  — `type` ∈ `concept | scenario | bug | compare`
- `roadmap-data.js`: `window.ROADMAP = [{part, weekRange, chapters: [slug, ...], deliverable, gate}]`

## 10. localStorage keys

Prefix everything `iam:` (e.g. `iam:theme`, `iam:font-scale`, `iam:quiz:<slug>`, `iam:roadmap:done`,
`iam:lab:<slug>`) so this project's browser data never collides with `sde-prep`'s `v2:`/`v3:`-prefixed
keys if both sites are ever opened in the same browser profile.

## 11. Suggested pace (shown on `roadmap.html`, adjustable)

Assuming ~8-10 hours/week part-time study, roughly 33 weeks (~8 months) end to end:

| Weeks | Part(s) |
|---|---|
| 1 | Part 0 |
| 1-4 | Part I |
| 5-6 | Part II |
| 7-10 | Part III |
| 11-14 | Part IV |
| 15-16 | Part V |
| 17-20 | Part VI |
| 21-22 | Part VII |
| 23-24 | Part VIII |
| 25-28 | Part IX |
| 29-32 | Part X (case studies + capstone) |
| 33+ | Part XI, ongoing reference |

State on the page that this is a suggestion, not a syllabus, same framing `sde-prep`'s series roadmap uses.

## 12. Deferred ideas (not in initial scope)

- A chat-widget "ask a question" feature like `sde-prep`'s (Gemini-backed serverless function) — possible
  later, not before the core 50 chapters exist.
- Progress export/import (`sde-prep`'s `progress-io.js` pattern) — add once there's enough tracked state
  (quiz scores + roadmap + lab checklists) to be worth exporting.
- A Keycloak version-pinning strategy for the labs (what happens when Keycloak ships a new major version
  mid-course) — decide when Part IV is reached, not now.

## 13. My short commands

- `continue`: run the session routine.
- `status`: summarize `BUILD_STATUS.md` in five lines, change nothing.
- `approve GATE-N: <feedback>`: record the approval, apply the feedback, continue.
- `reorder: <instruction>`: update the queue.
- `only shell today` / `only labs today`: restrict this session's work.
