# Build status

Last updated: 2026-11-10 | Docker: 29.6.2 | Java: openjdk 21.0.13.0.3 | Node: v18.20.4 | Python: 3.12.3 |
Git: yes, pushed to a GitHub remote (branch `main`). Deploy target: Vercel (not yet connected — do that
once `index.html` exists, see Open questions). Commits happen per unit; pushes only when the user
explicitly says to (standing instruction from the user — do not push without being asked).

## Current unit

**GATE-1 approved 2026-11-10** ("approve GATE-1, continue with chapters 03-09" — no feedback given,
approved as built). Confirms the parallel-subagent-then-central-merge working mode as the default going
forward (Open questions item 4).

**Chapters 03-09 complete 2026-11-10** (7 chapters, 7 parallel subagents, centrally merged and verified —
see Done below for full detail). This finishes Part I's chapter content.

**In progress now: queue item 5, the four shell pages** (`index.html`, `roadmap.html`, `glossary.html`,
`quizzes.html`) — built in parallel by 4 subagents, each read-only against the shared data files (no
file-conflict risk, unlike the glossary/quiz merge). These are the last piece needed to reach **GATE-2**
(Part I complete) per CLAUDE.md §8.

Chapters 03-09 briefs below (kept for the record, first time these were drafted in full, per CLAUDE.md
§7's "just before you need it" rule — chapter
00-02's briefs were already in CLAUDE.md §6, these are new). All extend existing glossary terms from
chapters 00-02 where the concept is the same (forward-referenced there with "full depth in chapter N") —
each brief names which existing term(s) it should extend rather than duplicate.

- [x] Ch 03 — AuthN vs. AuthZ vs. Accounting (and audit). Extends: `Authentication (AuthN)`,
      `Authorization (AuthZ)`, `Audit trail` (all currently chapter-1-only, forward-pointing here).
      Brief: the formal AAA model; where "Audit" sits relative to "Accounting" (the recording act vs.
      the after-the-fact review — note any terminology ambiguity honestly); real bugs from conflating
      AuthN/AuthZ (e.g. checking "is logged in" when the real question is "is allowed to do X"); forward
      pointers to ch05 (AuthN mechanisms) and ch06 (AuthZ models). Case-study: Meridian Trust / Riverside
      Health audit-trail requirements.
- [x] Ch 04 — Directory services & identity stores. New territory (LDAP/X.500 concepts, Active
      Directory), vendor-agnostic. Brief: directory information tree, distinguished names, entries/
      objectClasses/attributes (ties to ch02's `Attribute` concept, concretely), the bind operation; why
      most orgs already have a directory before adopting any IAM tool (motivates federation, ch07, and
      user federation, ch32); keep Kerberos to a one-line mention, not a deep dive (out of this course's
      scope). Case-study: Riverside Health's clinician AD; Northwind's staff-only AD vs. its CIAM
      customer store (not in AD).
- [x] Ch 05 — Authentication factors & MFA. Extends: `Multi-factor authentication (MFA)` (chapter-1-
      only so far, forward-pointing here). Brief: factor categories (knowledge/possession/inherence, and
      newer context/behavior signals); OTP (TOTP/HOTP) at a conceptual level, not crypto depth (that's
      ch16's job); WebAuthn/FIDO2/passkeys as phishing-resistant, public-key-based auth; risk-based/
      adaptive/step-up auth. Case-study: Meridian Trust step-up before a wire transfer (set up in ch02's
      org intro — pay this off concretely); Riverside clinician badge-tap as a possession factor.
- [x] Ch 06 — Authorization models. New territory: ACL, RBAC, ABAC (ties to ch02's `Attribute`), PBAC,
      ReBAC, Zero Trust/least privilege. Brief: a comparison table/diagram across all five models and
      their trade-offs; forward pointers to ch23 (Keycloak roles/groups = RBAC) and ch37/ch38 (Keycloak
      Authorization Services, ABAC/ReBAC). Case-study: Northwind's per-tenant RBAC; Meridian's ABAC
      transaction limits; Riverside's ReBAC "care team" chart-access relationships.
- [x] Ch 07 — SSO & federation concepts. Extends: `Single Sign-On (SSO)`, `Identity broker`,
      `Federation (identity federation)` (all chapter-1-only so far, forward-pointing here). Brief:
      formal IdP/SP/RP roles (and that SP is SAML terminology, RP is OIDC terminology, for roughly the
      same role — a common beginner confusion worth naming directly); brokering vs. federation revisited
      in full depth; JIT (just-in-time) provisioning as a new term, tied back to ch02's "federated/
      brokered creation" lifecycle entry point; "trust" between IdP and SP/RP (metadata/certs, setting
      up ch15's discovery and ch17's SAML metadata). Case-study: Northwind's enterprise customer SSO;
      Riverside brokering clinician logins via the hospital's AD-backed IdP.
- [x] Ch 08 — Tokens & sessions, conceptually. Can lightly extend `Subject` (ch02) if natural — a token
      is one concrete representation of a subject. Brief: bearer tokens (anyone holding it can use it —
      the security implication); claims (conceptual, not JWT format yet — that's ch16); stateless vs.
      stateful auth architecture trade-offs; the revocation problem (a stateless bearer token can't be
      easily invalidated before expiry) and a preview of mitigations, forward-pointing to ch14
      (introspection/revocation) and ch16 (JWKS/key rotation). Case-study: Meridian's short-lived banking
      API tokens; Northwind's refresh-token lifetime trade-offs for SaaS sessions.
- [x] Ch 09 — IAM threat landscape. New territory: credential stuffing, phishing, token theft/replay
      (ties to ch08), session hijacking, MFA fatigue/push bombing. Brief: tie each threat to the defense
      that answers it later in the course (ch05 MFA, ch18 OAuth/OIDC security, ch30 Keycloak brute-force
      config) so this reads as motivation, not just a list. Case-study: one believable incident sketch
      per org — Meridian (credential stuffing), Riverside (phishing targeting clinicians), Northwind
      (token theft via a customer-side XSS bug).

All seven: full topic-chapter minimums (3+ diagrams, 15+ glossary terms, 15+ quiz questions, 8+
takeaways, 5+ self-check), Part I badge, no lab. `bug`-type quiz questions fit naturally in 03 (AuthN/
AuthZ conflation), 06 (misconfigured model), 08 (revocation-problem bugs), 09 (missing-defense bugs) —
use them there. Quiz id prefixes: `aaa-`, `dir-`, `mfa-`, `azm-`, `sso-`, `tok-`, `thr-` (no collision
with existing `htuc-`/`wii-`/`idf-`).

## Queue

Ordered; one item at a time. Each chapter's full brief gets drafted into this file (replacing its queue
line) when it becomes "Current unit" — see `CLAUDE.md` §6-7. Lab-bearing chapters are marked.

### Reference set (builds the template — stop at **GATE-1** after item 3) — DONE, see Done below
1. ~~`assets/css/book.css` + `assets/css/print.css` + `assets/js/book.js`/`quiz.js`: the shell design
   system~~ — done 2026-11-10.
2. ~~Chapter 00 — How to use this course~~ — done 2026-11-10.
3. ~~Chapters 01-02 — What is IAM? / Identity fundamentals~~ — done 2026-11-10. Chapter 02 introduces
   Northwind Apps / Riverside Health / Meridian Trust.
   **→ GATE-1 reached. Stopped here for review — see Gates below.**

### Part I completion (requires GATE-1 approved)
4. ~~Chapters 03-09~~ — done 2026-11-10 (7 parallel subagents + central merge; see Done below).
5. **In progress**: `index.html`, `roadmap.html`, `glossary.html`, `quizzes.html` — built in parallel by
   4 subagents, each read-only against the shared data files. `nav-data.js`/`roadmap-data.js` already
   populated for all 50 chapters since the GATE-1 round (even unbuilt ones, marked `built: false` per the
   `sde-prep` index convention of disabling not-yet-built links instead of dead-linking them).
   **→ GATE-2: stop here for review once these land.**

### Part II (requires GATE-2 approved)
6. Chapters 10-12.

### Part III (protocols)
7. Chapters 13-18 (13-15 are curl-only labs against public test IdPs or a throwaway Keycloak instance —
   decide which when chapter 13 is drafted).
   **→ GATE-3: stop here for review.**

### Part IV (Keycloak core — first real Keycloak labs)
8. `labs/docker/docker-compose.yml` (Keycloak + Postgres, the base lab environment every Part IV+ chapter
   reuses) + `labs.html` index page.
9. Chapters 19-26.
   **→ GATE-4: stop here for review.**

### Part V (Keycloak operations)
10. Chapters 27-30.

### Part VI (federation & integration)
11. `labs/spring-boot-resource-server/`, `labs/react-spa/` scaffolding (built alongside chapters 34-35,
    not before — no point scaffolding a lab before its chapter is drafted).
12. Chapters 31-36.

### Part VII (fine-grained authorization)
13. Chapters 37-38.
    **→ GATE-5: stop here for review.**

### Part VIII (extending Keycloak)
14. `labs/spi-examples/` (Java SPI source, built alongside chapters 39-40).
15. Chapters 39-40.

### Part IX (production, scale, hardening)
16. Chapters 41-45.

### Part X (case studies & capstone)
17. `case-studies.html` index + `case-studies/cs01-cs03` (chapters 46-48's full write-ups).
18. Capstone project spec and build.

### Part XI (career & reference)
19. Chapters 49-50; chapter 50 is also the source for the final `glossary.html` sweep.

### Final
20. Full QA pass (CLAUDE.md §8 final gate): link check, quiz/glossary validation, every lab re-run from a
    clean Docker state, every "In Keycloak" callout spot-checked against a live admin console. README.md
    update to reflect final chapter count and lab list.

## Gates

- **GATE-1: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-2: not yet reached — chapters 03-09 done, awaiting the 4 shell pages (queue item 5).**
- GATE-3: not reached
- GATE-4: not reached
- GATE-5: not reached
- Final QA gate: not reached

### GATE-1 review summary

**What was built:**
- `assets/css/book.css` + `print.css`, `assets/js/book.js` + `quiz.js` — the shell design system. Three
  themes (light/dark/sepia, dark also follows `prefers-color-scheme`), sidebar nav grouped by Part,
  mobile drawer nav (no page-level horizontal scroll — built the fix sde-prep had to retrofit, in from
  the start), the full callout vocabulary (`key-idea`/`in-keycloak`/`pitfall`/`try-it`/`case-study`/
  `deep-dive`/`concept-recall`), diagram/table/quiz/lab-step components, and a per-chapter glossary
  renderer that filters the shared `glossary-data.js` by chapter slug (so chapters never hardcode their
  own term lists).
- `assets/js/nav-data.js` pre-populated with all 51 entries (chapter 0 + chapters 1-50), `built: true`
  now set only for chapters 0-2; `roadmap-data.js` populated with the full 11-Part pace plan from
  CLAUDE.md §11.
- Three chapters, built in parallel by three subagents, then centrally merged and verified:
  - `chapters/00-how-to-use-this-course.html` — 2 diagrams, 0 glossary terms (correct per brief —
    orientation, not conceptual), 12 quiz questions, 8 takeaways, 5 self-check.
  - `chapters/01-what-is-iam.html` — 3 diagrams, 17 glossary terms, 18 quiz questions, 9 takeaways,
    5 self-check.
  - `chapters/02-identity-fundamentals.html` — 4 diagrams, 18 glossary terms, 16 quiz questions,
    10 takeaways, 6 self-check. Introduces the three running case-study orgs (Northwind Apps, Riverside
    Health, Meridian Trust) with one paragraph each (full text preserved in that subagent's report for
    later-chapter consistency).
- `assets/js/glossary-data.js` and `quiz-data.js` merged centrally from all three chapters' proposed
  content (not written in parallel by the subagents, specifically to avoid concurrent-write conflicts on
  shared files — each subagent returned its proposed terms/questions as JSON in its report instead).

**Independent verification done by the coordinator (not just trusting the subagent reports):**
- All three chapter HTML files parse cleanly (Python `html.parser`, zero errors) and have balanced
  `<div>`/`<figure>`/`<svg>` tags (counted directly, not estimated).
- Confirmed required mount points/scripts present in every chapter: `sidebar-nav`, `chapter-glossary-
  mount`, `quiz-mount`, `chapter-nav-footer`, and the 5 expected `<script src>` tags in order.
- Served the whole site locally (`python3 -m http.server`) and `curl`-confirmed all three chapter pages
  and all 7 shared asset files return `200`.
- All of chapter 02's 4 NIST deep-dive links live-curled just now and confirmed `200` (the subagent that
  wrote them flagged it had not verified them live — now verified).
- Merged `glossary-data.js`/`quiz-data.js` checked programmatically (Node, with a `window` shim): 46 quiz
  questions total across the 3 chapters, zero duplicate quiz ids; 34 glossary terms, zero duplicate term
  names after the merge decision below; every `answerIndex` in bounds and every question has exactly 4
  options; 51 nav entries, zero duplicate slugs.
- `node --check` clean on all 6 JS files.

**One merge decision made during the merge** (not a correction, a judgment call): chapter 01 proposed a
standalone glossary term "Offboarding (deprovisioning)"; chapter 02 proposed "Deprovisioning" as part of
the identity lifecycle. These are the same concept. Kept chapter 02's lifecycle-stage framing as the
canonical entry (it's chapter 2's actual topic) and added `"what-is-iam"` to its `chapters` array instead
of shipping a duplicate term under a different name.

**What's NOT yet verified (no browser tool available in this environment):**
- Nobody has actually looked at the rendered pages in a browser — no visual check of the three themes,
  no check that the mobile drawer nav actually opens/closes correctly, no check that the quiz UI's
  click-through flow actually works end to end in a live DOM. Everything above is static/structural
  verification (HTML parses, assets resolve over HTTP, JS data is well-formed) — strongly suggestive but
  not the same as a real render. **Recommend the user open `chapters/00-how-to-use-this-course.html`
  (via `python3 -m http.server` from the repo root, not by double-clicking the file) locally and look at
  it before approving** — that's the one check this session genuinely cannot do itself.

**Files to open:** the three chapter files above (serve over HTTP first), plus `CLAUDE.md` and this
file's queue/curriculum sections if you want to sanity-check the plan itself, not just these three
chapters.

**Known gaps / not gaps, by design:** there is no `index.html` yet, so the chapters' "IAM & Keycloak
Mastery" brand link in the top bar points at a page that doesn't exist yet — expected, it's queue item 5
(after GATE-1 approval). `tools/check_links.py` doesn't exist yet either, so no automated link-check ran
— the checks above were done by hand instead for this first unit.

## Done

### Reference set (chapters 00-02, GATE-1)
- 2026-11-10 shell design system complete: `assets/css/book.css`/`print.css`, `assets/js/book.js`/
  `quiz.js`. Clean-room build (CLAUDE.md §2.6), not copied from `sde-prep`. Three themes, mobile-safe
  layout from the start, full callout/diagram/table/quiz component vocabulary, per-chapter glossary
  filter-render.
- 2026-11-10 `assets/js/nav-data.js` (51 entries, all 50 chapters + orientation), `roadmap-data.js` (11
  Parts, full pace plan) written by the coordinator directly from CLAUDE.md §5/§11.
- 2026-11-10 chapters 00, 01, 02 complete — built in parallel by three subagents (one per chapter, no
  shared-file writes to avoid conflicts), then centrally merged (`glossary-data.js`, `quiz-data.js`,
  `nav-data.js`'s `built` flags) and independently re-verified by the coordinator (see GATE-1 summary
  above for full detail: HTML parse checks, asset-resolution checks, live link checks, data-integrity
  checks). GATE-1 reached, awaiting the user's approval.
- 2026-11-10 **GATE-1 approved** by the user ("approve GATE-1, continue with chapters 03-09"), no
  feedback given — built content stands as-is.

### Part I completion (chapters 03-09)
- 2026-11-10 chapters 03-09 complete — 7 chapters, 7 parallel subagents (one per chapter, no shared-file
  writes), then centrally merged and independently re-verified by the coordinator:
  - Structural: all 7 files parse cleanly (Python `html.parser`, zero errors), balanced `<div>`/`<svg>`
    tags confirmed by direct count, all 4 required mount points + 5 script tags present on every page,
    every chapter page's `renderQuiz()` call slug matches its own `CURRENT_CHAPTER`.
  - Live-served the whole site (`python3 -m http.server`) and `curl`-confirmed all 7 new chapter pages
    return `200`.
  - Merged 93 new glossary terms (one proposed duplicate, "Phishing-resistant authentication" from both
    ch05 and ch09, resolved by keeping ch05's as canonical and extending its `chapters` array rather than
    shipping two entries) plus 7 extend-existing-term requests applied to chapter 1/2's forward-pointing
    entries (`Authentication (AuthN)`, `Authorization (AuthZ)`, `Audit trail`, `Multi-factor
    authentication (MFA)`, `Single Sign-On (SSO)`, `Identity broker`, `Federation (identity federation)`
    all got their chapters-array extended and, for most, a fuller definition from the chapter that now
    covers them in depth) — total glossary now **127 terms, zero duplicates** (checked programmatically).
  - Merged 119 new quiz questions (total now **165, zero duplicate ids** — checked programmatically
    across all 10 built chapters' pools, using per-chapter id prefixes `aaa-`/`dir-`/`mfa-`/`azm-`/
    `sso-`/`tok-`/`thr-` alongside the existing `htuc-`/`wii-`/`idf-`). Every question's `answerIndex` is
    in bounds and every question has exactly 4 options (checked programmatically).
  - Per-chapter diagram counts: ch03 3, ch04 5, ch05 4, ch06 3, ch07 3, ch08 3, ch09 4 — all exceed the
    3+ minimum.
  - Each chapter's own subagent live-curled its deep-dive links before finalizing (RFCs, OWASP cheat
    sheets, MITRE ATT&CK, W3C/FIDO Alliance, Microsoft Learn, ITU-T) and caught/fixed several dead links
    itself before shipping (ch03 swapped two guessed URLs that 404'd; ch04 swapped one wrong Microsoft
    Learn path; ch09 dropped one CISA link that 403'd rather than attempting a bot-check workaround).
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 3-9 by the coordinator only after all of
    the above checks passed (confirmed via a Node script with a `window` shim, same approach as GATE-1).
  - This completes **Part I — IAM Foundations** (chapters 00-09) in full.

## Verify items
- The visual/rendered check noted above under GATE-1 ("What's NOT yet verified") — no browser tool is
  available in this environment, so nobody has actually looked at the rendered pages yet. Recommend the
  user do this before approving GATE-1.
- Chapter 02's three NIST SP 800-63-3/-A/-B links and the CSRC glossary link were live-verified by the
  coordinator (200 OK each) — no outstanding concern, listed here only for the record.

## Deviations from CLAUDE.md
- None in content/structure. One process deviation, intentional: CLAUDE.md §7 describes a single-queue
  session routine: in practice, the 3 reference-set chapters were built by 3 parallel subagents (at the
  user's explicit request to "use multiple agents to speed up the work") rather than sequentially one at
  a time, with glossary/quiz merging done centrally afterward specifically to avoid shared-file write
  conflicts. This worked cleanly and is a reasonable model for future rounds once GATE-1 is approved —
  see "Open questions" item 4 below, which anticipated this.

## Cross-volume / shared-asset notes

N/A — this is a single-volume project, unlike `sde-prep`. If a future chat-widget or other feature is
ever lifted from `sde-prep`'s pattern (CLAUDE.md §12), note the adaptation here.

## Open questions for me

1. **Vercel connection**: not yet set up — do this once there's an `index.html` to deploy (end of the
   reference-set unit or after GATE-1, whichever the user prefers). Needs the user's GitHub remote
   confirmed and a go-ahead to import the repo into Vercel.
2. **GitHub remote**: assumed to exist already (per the git status at session start showing a clean repo
   tracking `origin/main`) — confirm the remote URL before the Vercel import step.
3. **Chat widget** (CLAUDE.md §12): deferred by default; ask again once the core 50 chapters exist.
4. **Working mode**: updated 2026-11-10 — at the user's request, the reference-set chapters were built
   by 3 parallel subagents (one per chapter) instead of sequentially, with shared-file merging done
   centrally afterward. It worked cleanly with no conflicts. Proposed default from here on: continue
   fanning out multiple chapters per round (bounded by how many chapters in the current Part are
   mutually independent), as long as each round's merge step keeps doing the same checks GATE-1's did
   (structural HTML checks, live link checks, data-integrity checks, duplicate-term/id dedup) before
   marking chapters built. Confirm this is still the right default when approving GATE-1.
