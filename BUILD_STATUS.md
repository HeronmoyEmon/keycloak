# Build status

Last updated: 2026-11-12 | Docker: 29.6.2 | Java: openjdk 21.0.13.0.3 | Maven: 3.8.7 | Node: v18.20.4 |
npm: 10.8.2 | Python: 3.12.3 |
Git: yes, pushed to a GitHub remote (branch `main`). Deploy target: Vercel (not yet connected — do that
once `index.html` exists, see Open questions). Commits happen per unit; pushes only when the user
explicitly says to (standing instruction from the user — do not push without being asked).

## Current unit

**Self-contained-labs retrofit (user-requested, off-queue, ahead of the Final QA pass) — in progress,
started 2026-11-12.** The user will only ever share this site's deployed Vercel link, never this GitHub
repo — but every lab chapter currently tells the reader to `cd labs/<folder>` or references a file like
`labs/docker/docker-compose.yml` as if they had the repo cloned. Fix: every lab becomes fully
self-contained — real file contents pasted directly into the chapter with a copy button, a "files you'll
need" menu before the first step, and commands that start from an empty folder, never a repo path.

- **New site-wide infra, built and approved (user said "proceed" after reviewing the pattern on
  chapter 20):** `assets/js/book.js`'s `initCodeBlocks()` auto-wraps every `<pre>` on every page with a
  copy button (works retroactively across all ~535 existing `<pre>` blocks sitewide, zero markup changes
  needed for plain command blocks); a `<pre data-filename="...">` attribute gets a header bar naming the
  file instead. New `.callout--files-needed` style in `book.css` for the "files you'll need" menu
  (anchor-links to each file's block, `id="file-<slugified-name>"`). Hidden/adjusted cleanly in
  `print.css`.
- **Reference chapter done:** chapter 20 rewritten — real `docker-compose.yml` pasted inline, a
  "files you'll need" box, `mkdir keycloak-lab && cd keycloak-lab` instead of a repo path, and the compose
  output transcript corrected to match (Docker names containers/network/volume after the folder name).
  `labs.html` also fixed (same anti-pattern, a file-path table) — replaced with a "no repository
  required" note pointing at chapter 20.
- **Scope, confirmed with the user:** all 28 `hasLab:true` chapters (13-15 are curl-only against a public
  demo server, already self-contained, no fix needed) plus the capstone. Split by actual content shape,
  found via grepping each chapter for `labs/` before writing briefs:
  - **Lightweight** (15 chapters: 21-31, 36, 46-48) — all reuse the *same, already-shown* chapter-20
    `docker-compose.yml` unmodified, no new files of their own. Fix is prose/command edits only: drop
    every `labs/docker` path reference, cross-link back to chapter 20 for the file (an internal site
    link, not a repo path — fine to reuse rather than re-pasting the same 37-line file 15 times), and
    change any leftover `cd labs/docker` commands to assume the same `keycloak-lab` folder. Four of these
    (21, 24, 27, 29) also have old `docker-postgres-1`/`docker_default`-style transcript output that needs
    the same `keycloak-lab-*` rename chapter 20's transcript got.
  - **Heavy** (9 chapters: 32 ldap-federation, 33 custom-user-storage-spi, 34 spring-boot-resource-server,
    35 react-spa, 37 authorization-services, 39 custom-spis, 40 custom-idp-and-required-actions,
    41 high-availability-clustering, 42 keycloak-on-kubernetes) — each has its own real `labs/<folder>`
    with files that exist nowhere else on the site; every one of those files gets fully inlined.
  - **Capstone** (`capstone.html`) — currently the worst offender (defers almost entirely to
    `capstone/README.md`/`docker-compose.yml`/`seed-realm.sh`/`billing-api`/`dashboard-spa`). Biggest,
    most interconnected, handled as its own dedicated pass, not parallelized, same as its original build.
- **Execution plan:** parallel fork agents, grouped into waves (lightweight chapters bundled 3-per-agent
  since the fix is small and uniform per chapter; heavy chapters one agent each since each introduces
  distinct real files; capstone last, alone). Each agent edits only its own chapter file directly — no
  shared-data-file collisions this time (no new glossary/quiz content, pure content/markup rewrite), so
  no central-merge splicing step is needed, only a verification pass (tag balance, internal links,
  `data-filename`/menu-anchor consistency) before committing each wave.
- **Wave 1 (lightweight, 12 chapters touched + ch26 needed no change) — DONE, committed `465b60a`.**
  Chapters 21-25, 27-31, 36, 48: dropped every `cd labs/...`/`labs/docker/...` reference, cross-linked
  back to chapter 20 instead of re-pasting its file, and renamed stale `docker-postgres-1`/
  `docker_default`-style transcript naming in 21/24/27/29 to the `keycloak-lab-*` convention. 5 parallel
  fork agents, verified centrally (tag balance, zero remaining `labs/` references, link resolution) —
  no shared-data-file collisions since this is pure markup/prose, so no splice step was needed.
- **Wave 2 (heavy, 9 chapters) — DONE, committed `58caba1` (ch39, self-committed by its own agent —
  independently re-verified, see below) + a second commit for the other 8.** 32 ldap-federation,
  33 custom-user-storage-spi, 34 spring-boot-resource-server, 35 react-spa (which also re-ran a real
  `npm install` to capture accurate transcript output, per CLAUDE.md §2 rule 5), 37 authorization-
  services (whose compose file turned out to be a genuinely separate isolated stack from chapter 20's,
  not a duplicate — correctly inlined in full rather than cross-linked), 39 custom-spis,
  40 custom-idp-and-required-actions, 41 high-availability-clustering, 42 keycloak-on-kubernetes.
  9 parallel fork agents (41+42 bundled as one, both small). Coordinator independently re-verified
  (not just trusted the reports): zero remaining `labs/` references across all 9 files; every
  files-needed-menu anchor matches a real `id` (programmatic check, 50 anchors/ids total across the 9
  files, zero mismatches); byte-exact diff of a spot-check sample (7 of the heaviest inlined
  Java/XML/Python/FTL files) against the real source on disk — all matched exactly; nested
  `mkdir -p` directory structures in 33/39/40 checked against the real Java package paths — correct.
  One agent (ch33) also fixed a stale repo-path comment inside the *source*
  `labs/custom-user-storage-spi/docker-compose.yml` itself, so the file a reader copies is clean too —
  reviewed and kept. Process note: the ch39 agent committed on its own initiative (not instructed to);
  reviewed its commit message (no attribution-line violation) and its diff independently before
  accepting it rather than re-doing the work.
- **Wave 3 (capstone) — DONE, committed.** The biggest single piece: `capstone.html`'s "How to run
  it" section went from one paragraph deferring entirely to `capstone/README.md` to a 5-step,
  fully self-contained walkthrough inlining all 19 real files (infra: `docker-compose.yml`,
  `seed-realm.sh`, `m2m-billing-job.sh`; `billing-api`: 7 Maven/Spring files; `dashboard-spa`: 9
  React/Vite files), grouped files-needed menus per component, real `mkdir -p` nested-package
  steps, and the exact real command sequence preserved from the README. **The one genuine
  cross-lab dependency** (the compose file mounts chapter 39's built SPI jar) is handled honestly
  rather than hidden: the reader is told to build chapter 39's self-contained lab in a sibling
  folder named `custom-spis-lab` (matching chapter 39's own instructions) so the relative mount
  path resolves — a real dependency between two labs, stated plainly, not a repo-path shortcut.
  The agent also found and fixed 7 real pre-existing bugs in the source files while preparing to
  inline them (a leftover wrong page title from the file this was adapted from; several stale
  `labs/`/`capstone/README.md` comments inside the source files themselves, which would otherwise
  have shipped onto the live page verbatim). Coordinator independently re-verified: tag balance
  OK; all 19 files-needed anchors match a real `id` (19/19); all 19 inlined files byte-exact
  against the (now-fixed) real source on disk (scripted check); the `custom-spis-lab` folder-name
  cross-reference confirmed accurate against chapter 39's own text; zero remaining
  `capstone/`/`labs/` references anywhere on the page. **Found and fixed one more real bug during
  this verification pass** that the agent correctly left alone (out of its stated scope): the
  real `docker-compose.yml`'s own header comment said to run `mvn -q -f billing-api/pom.xml clean
  package # build the billing API jar first` before `docker compose up` — wrong on two counts
  (the mount is the custom-SPI jar, not billing-api's, and building billing-api isn't needed until
  step 3). Fixed in both the live source file and its inlined copy in `capstone.html` to the
  correct `mvn -q -f ../custom-spis-lab/pom.xml clean package`, matching the real, previously
  human-verified README sequence. Re-verified byte-match held after this fix.
- **Self-contained-labs retrofit is now COMPLETE** (chapter 20 reference + wave 1's 12 lightweight
  chapters + wave 2's 8 heavy chapters + wave 3's capstone = all 28 `hasLab:true` chapters plus the
  capstone; chapters 13-15 needed no change, already self-contained against a public demo server).

## Final QA pass (queue item 21) — in progress, started 2026-11-12

Per CLAUDE.md §8: full link check, full quiz/glossary validation, every lab re-run from a clean Docker
state, every "In Keycloak" callout spot-checked against a live admin console, README.md update. The
user also explicitly flagged a diagram-contrast bug to look for and fix first, plus any other visual
discrepancies — done as the first sub-item, see below.

- **Diagram contrast audit/fix — DONE.** User report: some diagrams have a terracotta-colored
  background with black text, unreadable. Root cause confirmed real and systemic: `book.css`'s
  `.diagram-fill-accent-strong` (the one *solid*, non-soft fill — `var(--accent)`, a medium terracotta
  brown in light/sepia theme) needs its text in `.diagram-label-on-accent` (light/white), not the
  default dark `.diagram-label`/`.diagram-label-muted` — many diagrams across the site had the wrong
  one. Audited all 186 inline SVG diagrams site-wide (chapters, case studies, exams, capstone) with a
  geometric scanner (parses every `<rect>`'s real x/y/width/height + fill class, and every `<text>`'s
  real x/y, and checks which box's bounding box the text's coordinates actually fall inside — far more
  reliable than a sequential "last-shape-wins" heuristic, which produced real false positives along the
  way, caught and corrected before committing: 75 connector-caption labels that sit *between* boxes on
  the plain diagram background, not actually inside the preceding box, which the first heuristic pass
  wrongly flipped to light text — reverted with the same geometric method once caught). Final result,
  independently re-verified: **0 geometric mismatches across all 1,299 diagram texts carrying a label
  class** (2,443 total `<text>` elements exist across 186 diagrams; 1,299 of those carry a
  `diagram-label*` class and were checked for fill-vs-text-color correctness), 0 tag-balance errors
  across all 67 HTML files site-wide, exact same total label-class count before and after (1,299 → 1,299
  — every fix reclassified a text's color, none were lost or duplicated). 30 chapter/page HTML files
  touched. Fixes applied:
  - 17 single-class texts switched from dark (`diagram-label`/`diagram-label-muted`) to light
    (`diagram-label-on-accent`/new `diagram-label-on-accent-muted`) where they genuinely sit on a solid
    `diagram-fill-accent-strong` box but were still carrying the wrong, dark-text class.
  - 113 texts had **both** a plain and an on-accent class on the same element simultaneously (a
    pre-existing mess predating this session, from whichever earlier agent built each diagram — CSS
    cascade source order in `book.css` happened to make the on-accent one win regardless of which was
    actually correct for that box, so some of these were already rendering right by accident and some
    weren't). All 113 resolved down to the single genuinely-correct class per the geometric check — the
    large majority to `diagram-label-on-accent` (confirming they really did sit on a solid box), 4 of
    them (chapter 2's three org-intro header bars, chapter 45's one checklist-footer bar) resolved to
    plain `diagram-label` instead, below.
  - Added `.diagram-label-on-accent-muted` to `book.css` (didn't exist before — only a non-muted
    on-accent variant did), `fill: var(--accent-contrast)` at 0.82 opacity, so muted captions on a solid
    box keep the same visual hierarchy as muted captions on a plain background instead of all reading at
    full brightness.
  - **capstone.html's main architecture diagram** had the same inverse bug (soft fill + on-accent text)
    on its biggest box (the "Keycloak — northwind-apps realm" hub, deliberately the most visually
    prominent node in the
    diagram): filled with the soft `diagram-fill-accent` while its 9 text lines were already
    `diagram-label-on-accent`. Judgment call: rather than darken the text (which would make the
    intentionally-prominent hub box look like every other plain box), changed the box's own fill to
    `diagram-fill-accent-strong` to match its already-white text and its evident design intent as the
    diagram's visual focal point.
  - **One real near-miss, caught and reverted before it shipped**: an early cleanup pass's blind
    sitewide regex for `class="..."` briefly matched a JavaScript string-concatenation expression inside
    `roadmap.html`'s inline `<script>` (`'<div class="' + chipClasses + '" ...'`), corrupting it into
    invalid JS. Caught immediately via `git diff` review (empty diff where 7 "fixes" were expected
    signaled something was wrong; the next script version's diff showed the actual corruption), reverted
    that one line by hand, then re-verified no other touched file had a `class="` match landing inside a
    `<script>` block before trusting the rest of the batch.
  - Not yet checked: whether this same contrast issue exists in any way outside inline SVGs (e.g. a
    hardcoded color in a non-diagram context) — scoped this pass to diagrams specifically, per the
    user's report; flag if anything else surfaces during the rest of the Final QA pass.

**Part XI (queue item 20): chapters 49-50 — COMPLETE, 2026-11-12.** See the Done entry below for full
detail. **Queue item 21, the Final QA pass, is next and last** once this retrofit is done — closes out
CLAUDE.md §8's Final QA gate and v1.0. The brief that built chapters 49-50 is preserved below for
reference.

- **Ch 49 — Becoming an IAM Specialist** (`chapters/49-becoming-an-iam-specialist.html`, slug
  `becoming-an-iam-specialist`, already placeholdered in `nav-data.js`). Skills matrix (vendor-neutral
  IAM concepts from Parts I-III vs. Keycloak-specific skills from Parts IV-X — a table, not a diagram),
  career paths (IAM engineer/architect, CIAM specialist, IGA analyst, PAM engineer — brief, since this
  isn't the chapter's main focus), a certifications section, and an interview question bank (mix of
  concept-recall and scenario questions pulling from all three case-study orgs, since it's a synthesis
  chapter). CLAUDE.md §2 rule 1 requires live verification here rather than asserted-from-training-data
  claims — verified live via WebSearch on 2026-11-12, cite what was actually found rather than a stale
  mental list:
  - Vendor-neutral: **CISSP** (IAM is Domain 5 of 8 domains — correct the common "10 domains" figure,
    which is outdated; CISSP itself carries weight mainly for senior/leadership roles, not as an IAM-entry
    credential), **IDPro CIDPro** (the closest thing to a vendor-neutral IAM-practitioner credential),
    **CIAM** (Certified Identity and Access Manager, Identity Management Institute — governance/program
    management focus, easily confused by name with "Customer IAM," which it is not).
  - Vendor-specific: **Microsoft SC-300** (Identity and Access Administrator — Entra ID), **Okta**
    certification track (Professional/Administrator/Consultant/Developer), **SailPoint Certified
    Identity Security Administrator** (newly launched 2026-02-17 — note the recency), **CyberArk
    Defender/Sentry** (PAM-specific, named honestly as adjacent-not-core to this course's IAM/Keycloak
    scope).
  - **Keycloak/Red Hat specifically: no dedicated Keycloak certification exists.** Red Hat's closest
    credential is the Identity Management specialist exam (EX362, "Red Hat Certified Specialist in
    Directory Services and Authentication"), which covers Red Hat IdM/FreeIPA, **not** Keycloak — say
    this plainly rather than implying a Keycloak cert exists or papering over the gap. This is itself a
    useful, honest data point for a reader: Keycloak expertise is demonstrated by a portfolio/capstone
    (link back to `capstone.html`), not a vendor badge.
  - Agent should re-verify these live again before writing (search results shift) and flag anything that
    no longer checks out, same discipline as every other live-verification chapter.
- **Ch 50 — Master Glossary & Cheat Sheets** (`chapters/50-master-glossary-and-cheatsheets.html`, slug
  `master-glossary-and-cheatsheets`). A reference chapter, not a normal topic chapter — organizes
  existing `GLOSSARY` entries (671+ terms already in `glossary-data.js`, do not duplicate — this chapter
  curates/cross-references, it doesn't re-define) into a handful of named cheat sheets (e.g. "OAuth grant
  types at a glance," "Keycloak realm/client/scope relationships," "token types compared," "authorization
  model comparison ACL/RBAC/ABAC/PBAC/ReBAC"), each as a `table-compare`-eligible table or small diagram
  pulling from already-established facts (grep existing chapters, don't invent new ones). This chapter is
  explicitly **the source for `glossary.html`'s final sweep** (queue item 21) — agent should note in its
  report any glossary term it noticed during research that looks miscategorized, duplicated, or orphaned
  (no `chapters` entries), for the Final QA pass to act on, rather than fixing `glossary-data.js` itself
  (shared-file edits stay centralized).
- Both chapters: lower diagram/quiz minimums are fine with a one-line note, same judgment call as ch00 —
  these are reference/synthesis chapters, not new-concept chapters. Both still get a 15+ question quiz
  pool (concept + scenario, cumulative/synthesis-style like the exams rather than narrow recall) and 5+
  self-check questions with real model answers, per the floor CLAUDE.md §4 sets.
- Each agent proposes its glossary/quiz additions in its report rather than editing shared files
  directly (write-collision avoidance, standard practice this build). Central merge sets `built: true`
  for both in `nav-data.js`, runs the usual tag-stack/link/dup-id checks, and updates this file.

**GATE-1 approved 2026-11-10. GATE-2 approved 2026-11-10. GATE-3 approved 2026-11-10. GATE-4 approved
2026-11-10** ("proceed" — sent in the first message of a new session, immediately after a CLI restart,
with no other pending question in play and the GATE-4 summary already the last thing presented in the
prior session; treated as approval by the same convention GATE-3's "continue" used. No feedback given
on any of the four gates). Confirms the parallel-subagent-then-central-merge working mode as the
permanent default.

**Part IV — Keycloak Core Administration (chapters 19-26) is complete and GATE-4 is approved.** All 8
chapters merged, verified, `built:true`, committed. See the GATE-4 review summary under Gates, and the
Done entry for chapters 21-26, for full detail.

**Part V — Keycloak Operations (chapters 27-30) is now also complete.** All 4 chapters merged, verified,
`built:true`, committed — built as a second parallel-subagent round, each agent pre-assigned its own
isolated Docker Compose project name/port (`ch27kc`→18127, `ch28kc`→18128, `ch29kc`→18129,
`ch30kc`→18130) per the process note GATE-4 surfaced, avoiding the collision 5 of 6 chapter-21-26 agents
hit. No gate sits between Part IV and Part V, or between Part V and Part VI, per CLAUDE.md §8 (the next
one, GATE-5, sits after Part VII) — so no stop was needed here. See the Done entry for chapters 27-30
for full detail, including a real shared-environment fix: chapter 28's lab found `labs/docker/docker-
compose.yml` set `KC_HEALTH_ENABLED` but never published port 9000, so `/health`/`/metrics` were
unreachable from the host even though the env var suggested they should work. Fixed directly in the
shared compose file (added port 9000 and `KC_METRICS_ENABLED`), personally re-verified end to end
(`/health`→200, `/metrics`→200 on 9000; `/health`→404 on 8080, confirming the port split), and
documented in `labs/docker/README.md`.

**Redesign rollout — COMPLETE as of 2026-11-10.** The slide-deck + roadmap redesign (see the Done-
section design note below for full detail: slide chunking, `#mark-all-btn`, click-to-reveal self-check
answers, `table-compare` icons, the diagram bug classes to check for, and the "prefer more
visuals/examples" standing rule) is no longer a pending proposal — CLAUDE.md §4 has been rewritten to
describe it as the official template, and all 29 already-built chapters (02-30) now match chapter 1's
structure. Converted via the usual parallel-subagent-then-merge pattern, Part by Part: Part I (chapters
02-09), Part II (10-12), Part III (13-18), and all of Part IV (19-26) and Part V (27-30) are done — see
Done below for each round's detail (slide/self-check counts, real bugs found+fixed, table-compare
judgment calls). **Part VI's new content (chapters 31-36) is next per the Queue.**

**Part VI (federation & integration) — COMPLETE: chapters 31-36, all `built:true`, committed.**
Unlike chapters 02-30, these are brand-new chapters, built straight into the slide-deck format with real
hands-on labs, per CLAUDE.md §2.5/§4. Built in two rounds (Round 1: chapters 31-33, Round 2: chapters
34-36), same "establish-then-scale" shape Part IV used — see the Done entries below for full detail:
real bugs found/fixed in each chapter, new `labs/ldap-federation/`, `labs/custom-user-storage-spi/`,
`labs/spring-boot-resource-server/`, and `labs/react-spa/` folders, and glossary/quiz merge counts.
**Part VII (fine-grained authorization, chapters 37-38) is COMPLETE.** Built as two parallel subagents
per the drafted briefs, then centrally merged and independently re-verified — see the Done entry below
for full detail (real bugs found in ch37's live UMA 2.0 lab, OPA/Cedar live-verification findings,
glossary/quiz merge counts, link checks). **GATE-5 approved 2026-11-11** — confirms the reader can
secure a real backend/frontend/service account (Part VI) *and* configure fine-grained authorization
(Part VII). **Part VIII (extending Keycloak, chapters 39-40) is COMPLETE.** Built as two parallel
subagents per the drafted briefs, each with a real Java-SPI Docker lab (`ch39kc`/18139,
`ch40kc`/18140), then centrally merged and independently re-verified — see the Done entry below for
full detail. No gate sits immediately after Part VIII — **Part IX (production, scale & hardening,
chapters 41-45) is COMPLETE.** Round 1 (ch41 HA/clustering, ch42 Keycloak-on-Kubernetes) and Round 2
(ch43 performance tuning, ch44 backup/DR/multi-region, ch45 security hardening/upgrades) are both built,
merged, and independently re-verified — see the Done entries below for full detail. Part IX closes out
real, named findings across the board: a deprecated Keycloak multi-cluster topology (v1), a feature
promoted from preview to supported mid-course (`stateless` mode, 26.8.0), and five real CVEs in the
26.8.x line, one explicitly incompatible with a rolling upgrade. **No gate sits after Part IX — Part X
(case studies & capstone, chapters 46-48 + the capstone) is next per the Queue.**

**Shell fixes (2026-11-11, user-requested, off-queue):** the site-wide sidebar nav (`.sidebar`/
`#sidebar-nav` in `book.js`/`book.css`, used by `roadmap.html`/`glossary.html`/`quizzes.html`/
`labs.html` and — until this fix — chapter 00) scrolled with the page and always listed all 50 chapters
flat. Fixed: `.sidebar` is now `position: sticky` with its own internal scroll, and each Part renders as
a collapsible native `<details>`/`<summary>` group (open only for the Part containing the current
chapter). Also fixed: the theme was silently following the OS/browser `prefers-color-scheme`, overriding
a reader's choice on return visits — removed that media query from `book.css` and made `book.js`
`initThemeAndFont()` default explicitly to `light` and always apply a theme, so it now only changes when
the reader clicks the theme toggle. Committed `3364b2b`, pushed. **Chapter 00 then converted to the
slide-deck template** (it was the last chapter still on the old book-shell layout — see Done below).

**Part X case studies — chapters 46-48 + `case-studies.html` + `case-studies/cs01-cs03` — COMPLETE
(2026-11-12).** Built as 3 parallel fork agents, one per org, each grepping the repo first for every
existing mention of its org (34/29/34 chapters already referenced Northwind/Riverside/Meridian by name
before this unit) to stay consistent with established facts rather than inventing new canon, then
writing `chapters/4X-case-study-<org>.html` (full slide-deck template; the "hands-on" slot points at
the full write-up instead of a Docker lab, per CLAUDE.md §5's Part X table) and
`case-studies/cs0N-<org>.html` (new directory — long-form write-up on the shell/book-page template,
`labs.html`'s layout, not the slide deck one). Each agent returned proposed glossary/quiz additions in
its report rather than editing shared files directly; central merge done by the coordinator.

**Per-chapter results:** ch46 Northwind (4 diagrams, 9 takeaways, 5 self-check, 15 quiz, 2 new glossary
terms — "Tenant isolation", "Realm-per-tenant model" — plus `case-study-northwind` added to 2 existing
terms' `chapters` arrays), ch47 Riverside (3 diagrams, 9 takeaways, 6 self-check, 15 quiz, 0 new
glossary terms — a deliberate judgment call for a synthesis chapter, same one chapter 0 made — 6
existing terms extended), ch48 Meridian (3 diagrams, 9 takeaways, 5 self-check, 15 quiz, 0 new glossary
terms, 3 existing terms extended). 10 diagrams total across the unit, all inline SVG using only the
shared `diagram-*` classes, spot-checked by the coordinator against both named bug classes (no
connector crosses a box it doesn't connect to; no text estimate exceeds its box).

**One real design gap each agent closed rather than ignored:** ch47's agent resolved an open self-check
question from chapter 32 (whether Riverside's clinician federation uses READ_ONLY or WRITABLE edit
mode) by committing to READ_ONLY, consistent with that self-check's own model answer — chapter 32's
primary LDAP lab is actually Meridian Trust's staff directory, not Riverside's, so this was a genuine
gap in established canon, not a contradiction to resolve.

**Independent verification done by the coordinator, not just trusting the subagent reports:**
- All 8 touched/new HTML files (`chapters/46-48`, `case-studies/cs01-03`, `case-studies.html`,
  `index.html`) parsed with a proper tag-stack checker (not substring counting) — zero mismatches.
- All required mount points/script order confirmed present and in the same order as chapter 1's
  reference template in all three new chapters (`#mark-all-btn`, `#deck-progress-mini`,
  `#slide-outline`, `#quiz-mount`, `#chapter-glossary-mount`, the 6-script include order, matching
  `QuizEngine.renderQuiz`/`SlideDeck.init` calls, correct `CURRENT_CHAPTER`/`SITE_ROOT` values).
- Every internal link across the 8 files resolves to a real file (checked programmatically, not by
  eye) — including each chapter's forward link to its own case-studies write-up and each write-up's
  link back to `../case-studies.html`, which didn't exist yet when the agents wrote those links.
- All 13 deep-dive links across the three chapters (Keycloak docs, NIST SP 800-66r2/800-63B/800-162,
  Cornell LII 45 CFR §164.312/§164.502, PCI SSC, OpenFGA docs, the original Zanzibar paper) re-curled
  live by the coordinator: 13/13 returned `200`.
- Merged `glossary-data.js`/`quiz-data.js` checked in Node with a `window` shim: 671 glossary terms,
  zero duplicate term names; the 3 new quiz pools (`case-study-northwind/riverside/meridian`, 15
  questions each, 45 total) all have exactly 4 options and an in-bounds `answerIndex`, zero duplicate
  question ids anywhere in the merged file; 51 nav entries, zero duplicate slugs, and chapters 46-48
  now `built: true`.
- `node --check` clean on `nav-data.js`, `glossary-data.js`, `quiz-data.js` after every edit.

**What's NOT yet verified (same standing limitation as every prior gate):** no browser tool exists in
this environment, so nobody has visually rendered the three new chapters, the three write-ups, or the
new `case-studies.html` index. All verification above is structural/programmatic. Recommend opening
`case-studies.html` and at least one chapter+write-up pair locally before considering this unit fully
closed from a visual-design standpoint — same recommendation every prior unit has carried.

**Capstone (queue item 18) is next per the Queue.**

**The capstone — COMPLETE (2026-11-12).** Per CLAUDE.md §5's Part X row, this is "own project, not a
chapter page" — a real, running, multi-app system, not narrated. Built by a single agent end to end
(not a parallel round — it's one integrated system, not independently parallelizable work), then
independently re-verified by the coordinator against the live stack. **Org: Northwind Apps** — already
the richest-instrumented org across the labs, so the capstone's job was integration: assemble
already-proven pieces (realm `northwind-apps` ch20-26, UMA authorization ch37, the `billing-api` Spring
Boot resource server ch34, the `TenantIdProtocolMapper` custom SPI ch39) into one actually-running
stack rather than building new pieces from scratch.

**What was built:** `capstone/docker-compose.yml` (isolated Keycloak 26.8.2 + PostgreSQL 17, project
`capstonekc`, port 18200, the already-built `custom-spis-1.0.0.jar` rebuilt fresh and mounted);
`capstone/seed-realm.sh` (reconstructs the full realm — roles, groups, 5 users, 5 clients
`northwind-dashboard`/`northwind-spa`/`billing-service-account`/`billing-direct-grant`/`billing-api`,
the `tenant-context` client scope, and a real UMA resource/scope/policy/permission on `billing-api`
mirroring chapter 37's pattern); `capstone/billing-api/` (chapter 34's resource server, copied not
mutated, extended with a real server-side UMA 2.0 PEP — `UmaPermissionService` — gating a new
`GET /api/invoices/premium-report` endpoint); `capstone/dashboard-spa/` (chapter 35's React SPA, copied
not mutated, repointed at `northwind-spa`); `capstone/m2m-billing-job.sh`; `capstone.html` +
`capstone/README.md`. Linked from `index.html`'s quick-links nav, `case-studies.html`, and chapter 46.
**Cut, documented honestly rather than faked:** the Acme Corp brokering/JIT stretch goal — chapter 31
already proves that mechanism works, and this capstone's job was integrating pieces that weren't wired
together yet, not re-proving ones that already were.

**Two real bugs found and fixed while building it** (both documented in `capstone.html`'s own pitfall
callout and `capstone/README.md`): (1) a UMA resource URI with a path-segment wildcard
(`/api/invoices/*/premium-report`) is rejected outright — Keycloak only accepts a trailing `/*` or
`/*.ext`, fixed by using a fixed path; (2) Keycloak 26's declarative User Profile silently drops any
user attribute not declared in its schema — no error, the attribute (and therefore the mapped claim)
just never appears — fixed by declaring `tenant_id` in the realm's User Profile schema.

**Independent verification done by the coordinator against the live stack** (not just trusting the
agent's report — the stack was left running specifically for this): confirmed `capstonekc` containers
up, `billing-api` and the SPA dev server both actually running (`ps`-confirmed PIDs), Keycloak discovery
`200`. Re-fetched real client secrets via `kcadm`/Admin REST API and independently re-ran the full
permission matrix myself from scratch (not reusing the agent's tokens): `amara.reader` (billing-reader)
→ `GET /api/invoices` 200, `POST /api/invoices` 403, `GET /api/invoices/premium-report` 403;
`devon.admin`/billing-service-account (billing-admin) → `POST /api/invoices` 201,
`GET /api/invoices/premium-report` 200 via a real UMA RPT exchange; the M2M job re-run fresh end to
end, both endpoints 200. Decoded a freshly-issued token and confirmed `tenant_id: acme-corp` genuinely
rides on it for a client carrying the `tenant-context` scope (temporarily enabled direct-access-grants
on `northwind-dashboard` purely to generate a verification token without redoing the full browser-form-
POST PKCE flow, then reverted the setting immediately after — confirmed `204` on both the toggle and
the revert). Verified the User Profile schema fix is real (`tenant_id` present in the realm's user-
profile attribute list) and that the underlying user attribute is genuinely persisted (an initial
`kcadm --fields attributes` check misleadingly showed `{}` — a `kcadm` fields-filter quirk, not a real
data problem; the raw Admin REST API call showed the attribute correctly set). All 5 touched/new HTML
files parse cleanly (tag-stack checker) and every internal link added this unit resolves to a real file.

**What's NOT independently re-verified:** the full real authorization-code+PKCE browser-form-POST login
against `northwind-spa` (re-running that specific flow was judged not worth repeating given the
`tenant_id`-on-token claim was independently confirmed by an equivalent, lower-cost check against a
different client carrying the same scope) and the React SPA's actual rendered UI (no browser tool in
this environment — same standing limitation every prior unit has carried).

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

### Part I completion (requires GATE-1 approved) — DONE
4. ~~Chapters 03-09~~ — done 2026-11-10 (7 parallel subagents + central merge; see Done below).
5. ~~`index.html`, `roadmap.html`, `glossary.html`, `quizzes.html`~~ — done 2026-11-10 (4 parallel
   subagents, each read-only against the shared data files).
   **→ GATE-2 reached, approved 2026-11-10.**

### Part II (requires GATE-2 approved) — DONE
6. ~~Chapters 10-12~~ — done 2026-11-10 (3 parallel subagents + central merge; see Done below). No gate
   sits between Part II and Part III — approval isn't blocking the next unit, but item 7 below has an
   open design decision the user hasn't weighed in on yet (see Open questions).

### Part III (protocols) — DONE
7. ~~Chapters 13-18~~ — done 2026-11-10 (6 parallel subagents + central merge; see Done below).
   **→ GATE-3 reached, approved 2026-11-10.**

### Part IV (Keycloak core — first real Keycloak labs) — DONE
8. ~~`labs/docker/docker-compose.yml` + `README.md` + `labs.html`~~ — done 2026-11-10, coordinator-built
   and personally verified end-to-end (see Done below).
9. ~~Chapters 19-26~~ — done 2026-11-10 (8 chapters total: 19-20 as a first round of 2 parallel
   subagents, 21-26 as a second round of 6 parallel subagents, both centrally merged; see Done below).
   **→ GATE-4 reached, awaiting approval.**

### Part V (Keycloak operations) — DONE
10. ~~Chapters 27-30~~ — done 2026-11-10 (4 parallel subagents, each pre-assigned an isolated Docker
    project/port, then centrally merged; see Done below).

### Slide-deck/roadmap redesign rollout — in progress, ahead of Part VI
Converting all 29 already-built chapters (02-30) to the slide-deck template chapter 1 established,
per CLAUDE.md §4. One sub-item per Part, done via the usual parallel-subagent-then-merge pattern; each
agent re-verifies diagram geometry, adds self-check model answers, and is explicitly told to add more
visuals/examples where it helps, not just re-chunk existing prose.
10a. ~~Part I (chapters 02-09, 8 chapters)~~ — done 2026-11-10, see Done below.
10b. ~~Part II (chapters 10-12, 3 chapters)~~ — done 2026-11-10, see Done below.
10c. ~~Part III (chapters 13-18, 6 chapters)~~ — done 2026-11-10, see Done below.
10d. ~~Part IV (chapters 19-26, 8 chapters)~~ — done 2026-11-10, see Done below.
10e. ~~Part V (chapters 27-30, 4 chapters)~~ — done 2026-11-10, see Done below. **Redesign rollout
     complete — Part VI (chapters 31-36) is next.**

### Part VI (federation & integration) — DONE
12a. ~~Chapters 31-33 (identity brokering, LDAP federation, custom User Storage SPI)~~ — done 2026-11-11,
     see Done below.
11. ~~`labs/spring-boot-resource-server/`, `labs/react-spa/` scaffolding~~ — done 2026-11-11, built
    alongside chapters 34-35 as planned, see Done below.
12b. ~~Chapters 34-36 (securing a backend API, securing a frontend SPA, service-to-service/M2M auth)~~ —
     done 2026-11-11, see Done below. Part VI is now fully complete (chapters 31-36).

### Part VII (fine-grained authorization) — DONE
13. ~~Chapters 37-38~~ — done 2026-11-11, see Done below. **GATE-5 approved 2026-11-11** ("approve
    GATE-5", no feedback given).

### Part VIII (extending Keycloak) — DONE
14-15. ~~Chapters 39-40~~ — done 2026-11-11, see Done below.

### Part IX (production, scale, hardening) — DONE
16. ~~Chapters 41-45~~ — done 2026-11-11, see Done below. No gate sits after Part IX.

### Part X (case studies & capstone)
17. ~~Chapters 46-48 + `case-studies.html` index + `case-studies/cs01-cs03` (full write-ups)~~ — done
    2026-11-12, see Current unit above / Done below for full detail. No gate sits here per CLAUDE.md
    §8 — the capstone is the next and last item before Part XI.
18. ~~Capstone project spec and build~~ — done 2026-11-12, see Current unit / Done below. **Part X is
    now fully complete.**

### Exams (gap found 2026-11-12, user approved adding it now)
CLAUDE.md §3 specifies an `exams/` directory (`exam-foundations.html`, `exam-protocols.html`,
`exam-keycloak-core.html`, `exam-integration.html`, `exam-final.html`) and §4 references "exam-prep
chapters" as if they already existed — but no exam page was ever drafted into this queue at any Part
boundary across the whole build. Flagged to the user before closing out Part XI/Final QA; they chose
"add them now" over dropping the feature or deferring it to CLAUDE.md §12. Maps 1:1 onto the four
content gates plus the final gate: `exam-foundations` ↔ GATE-2 (Part I), `exam-protocols` ↔ GATE-3
(Part III), `exam-keycloak-core` ↔ GATE-4 (Part IV), `exam-integration` ↔ GATE-5 (Parts VI-VII),
`exam-final` ↔ the Final QA gate (comprehensive, Parts VIII-XI + cumulative). GATE-1 has no exam — it's
a template/build gate, not a content-mastery one.
19. ~~`exams/exam-foundations.html`, `exams/exam-protocols.html`, `exams/exam-keycloak-core.html`,
    `exams/exam-integration.html`, `exams/exam-final.html` + an `exams.html` index page~~ — **done
    2026-11-12.** Built as 5 parallel fork agents, one per exam, each grounding its graded questions in
    the actual chapters it covers (grepped for real realm/client/user facts rather than inventing them)
    and proposing its quiz pool in its report for central merge — see Done below for full detail.
    `exams.html` built centrally (predictable, lightweight — same shell-page template as `labs.html`/
    `case-studies.html`), linked from `index.html`'s quick-links nav and from `roadmap.html`'s
    GATE-2/3/4/5 and Final-QA-gate badges (now real links to the matching exam; GATE-1 stays unlinked —
    it's a template/build gate, not a content-mastery one).

### Part XI (career & reference) — DONE
20. ~~Chapters 49-50; chapter 50 is also the source for the final `glossary.html` sweep.~~ — done
    2026-11-12, see Done below. **Final QA pass (item 21) is next and last.**

### Final
21. Full QA pass (CLAUDE.md §8 final gate): link check, quiz/glossary validation, every lab re-run from a
    clean Docker state, every "In Keycloak" callout spot-checked against a live admin console. README.md
    update to reflect final chapter count and lab list.

## Gates

- **GATE-1: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-2: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-3: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-4: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-5: approved 2026-11-11** ("approve GATE-5"), no feedback given (approved as built). See summary
  below.
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

### GATE-2 review summary

**What was built, beyond the GATE-1 reference set:**
- Chapters 03-09 (7 chapters, see the Done entry below for full detail): completes Part I's content.
- The four shell pages, each built independently in parallel (read-only against the shared data files,
  so no merge step was needed the way the glossary/quiz data needed one):
  - `index.html` — course home: hero + pitch, 3 live stat tiles (chapters built/50, Parts with ≥1 built
    chapter, built chapters with a lab), a `callout--key-idea` introducing the three case-study orgs,
    quick links to the other three pages, and "The 11 Parts" rendered live from `COURSE_NAV` (grouped in
    array order, not alphabetized) — built chapters link out, unbuilt ones show plainly, no dead links.
  - `roadmap.html` — the suggested-pace page: intro framing it as a suggestion not a syllabus, an overall
    progress bar, one `.part-card` per `ROADMAP` entry with its week range/gate badge/deliverable, and a
    per-chapter `.progress-chip` checkbox (localStorage `iam:roadmap:done`, a JSON array of slugs) the
    reader can tick off — disabled for unbuilt chapters so they can't be marked done early.
  - `glossary.html` — the searchable master glossary: live search (substring, case-insensitive, over
    term+definition) and a category filter, both combining with AND; live result count; a no-results
    state; every entry shows which chapter(s) it came from, linked only if that chapter is built.
  - `quizzes.html` — the score dashboard: reads every score through `QuizEngine.readScores()` (no
    reimplemented localStorage parsing), shows best/last/attempts per built chapter, an average over only
    attempted chapters, a weakest-3 list, and a two-step (not native-`confirm()`) reset-all that clears
    every real `iam:quiz:*` key rather than a hardcoded list.

**Independent verification done by the coordinator (beyond what each subagent already verified itself):**
- All four shell pages re-parsed cleanly (Python `html.parser`, zero errors) now that all four exist
  together.
- Re-served the whole site locally and `curl`-confirmed, now that all four pages co-exist: every one of
  the four pages, both sample chapter pages, and all 5 shared JS/CSS asset files return `200` — and,
  specifically, every cross-link between the four shell pages (`index.html`'s links to the other three,
  and all three's "brand" links back to `index.html`) resolves to a real file now that the chicken-and-
  egg "the other pages don't exist yet" situation each subagent flagged is resolved.
- Each subagent had already independently hand-traced its own page's core logic against the real data
  (e.g. `glossary.html`'s search traced against 5 concrete queries including a 21-result case and an
  AND-combination case; `quizzes.html`'s average/weakest-3 math hand-traced against a constructed 2-
  chapter scenario and cross-checked by actually running the extracted logic in Node against the real
  quiz-data.js) — the coordinator did not redo this work, since re-deriving it from scratch wouldn't add
  anything beyond re-reading what's already a specific, checkable trace in each report.

**What's still NOT verified (same standing limitation as GATE-1 — no browser tool in this environment):**
nobody has visually looked at any of the four new pages, or clicked through the roadmap checkboxes /
glossary search box / quizzes reset-confirmation flow in a live DOM. All verification above is
structural (parsing, link resolution, hand-traced/Node-replicated logic), not a real render. **Recommend
opening all four pages locally (`python3 -m http.server` from the repo root, not double-clicking) before
approving** — in particular, try the roadmap checkboxes, the glossary search box, and the quizzes
reset-all two-step flow, since those are the three pieces of real interactivity this round added that a
structural check can't fully substitute for.

**Files to open:** `index.html`, `roadmap.html`, `glossary.html`, `quizzes.html`, plus chapters 03-09 if
you haven't looked at those yet either.

**One minor inconsistency across the four pages worth knowing about, not a bug:** `roadmap.html`
excludes chapter 0 (orientation) from its "50 real chapters" denominator for the overall progress
bar/stats, while `index.html`'s stat tiles count chapters built against the full 50 non-orientation
chapters too but phrase it slightly differently ("X / 50"). Both subagents made the same reasonable
choice independently (chapter 0 isn't one of the 50 numbered curriculum chapters), so the numbers agree
in practice — flagging only because nobody enforced this as a single shared rule, so a future page should
match the same convention rather than reinvent it.

### GATE-3 review summary

**What was built:** Chapters 13-18 (Part III — Protocols). First unit with real hands-on labs. Full
per-chapter counts are in the Done entry below; highlights here are what needed independent judgment.

**The lab target decision, and how it played out:** CLAUDE.md §5 locked in Duende Software's public
demo IdentityServer. The anticipated risk — no documented redirect_uri for the interactive (auth-code)
clients — turned out not to block chapter 13 at all: that subagent found a working one
(`https://demo.duendesoftware.com/callback`) and completed a full, real authorization-code+PKCE round
trip, real tokens included. Chapter 14's device-flow lab (the planned fallback) still got built in full
as planned, so the course doesn't depend on chapter 13's lucky find holding up forever.

**Two things the coordinator personally re-verified, not just accepted from a subagent's report:**
1. Chapter 13 reported a genuinely surprising security finding: the demo server's `interactive.public`
   client doesn't enforce RFC 6749's exact-match redirect_uri validation — an authorization code could
   be sent to an attacker-controlled domain (`evil.example.com`) and still exchanged for real tokens.
   The coordinator independently re-ran the relevant part of this test directly (three separate
   redirect_uri values against `/connect/authorize`, isolating just that one variable) and reproduced
   the same result each time: the authorize step never rejects an unregistered redirect_uri for this
   client, regardless of value. The chapter's framing — a live, reproducible, non-production-
   representative finding, not a general claim about IdentityServer software — holds up.
2. **A real discrepancy surfaced between two subagents and is worth tracking**: chapter 15's subagent
   reported that its own attempt to script the device-flow login step (POSTing `bob`/`bob` to the login
   form) was explicitly blocked by this environment's own tool-permission classifier before it reached
   the network. Chapter 13's subagent reported successfully doing exactly this kind of thing (scripted
   POST of discovered `bob`/`bob` credentials) to complete a real login as part of its authorization-
   code lab. Both reports are detailed and internally consistent (chapter 13's PKCE values were cross-
   checked two independent ways and matched), so this isn't dismissed as one subagent simply lying —
   most likely the permission classifier's behavior isn't deterministic across invocations, or reacts to
   how the action was phrased in the agent's own tool calls. Not resolved further this round; noted here
   so a future session doesn't assume either "this always works" or "this is always blocked."

**Cross-chapter dedup catches during merge** (both subagents had already flagged the risk themselves):
chapter 13 and chapter 18 both proposed a `State parameter (CSRF defense)` glossary term — kept ch18's
fuller security-reasoning version as canonical, extended its `chapters` array to include `oauth2-core`,
dropped ch13's duplicate. Chapter 13 and chapter 14 both proposed brief/full versions of `Client
credentials grant` and `Device authorization grant` (ch13 as grant-survey previews, ch14 as full
definitions) — kept ch14's fuller versions, extended their `chapters` arrays to include `oauth2-core`.

**A genuinely good live finding, not just a risk to flag:** chapter 14's lab revoked a self-contained
JWT access token via `/connect/revocation` (got `HTTP 200`), then immediately introspected the same
token and got `active: true` back — a real, reproducible demonstration of chapter 8's "revocation
problem" caught live against a real server, not just asserted in prose.

**Live-verified facts worth recording for future sessions:** RFC 9700/BCP 240 (OAuth 2.0 Security BCP)
is a published final RFC (January 2025), not a draft, and its PKCE guidance is MUST for public clients
but only RECOMMENDED (not MUST) for confidential clients — several secondary sources overstate this as
a blanket MUST, and chapter 18 correctly did not repeat that overstatement. RFC 10017 (OAuth 2.0 for
Browser-Based Applications) reached final RFC status in August 2026 — new enough that training-data
recall would likely be wrong or missing, and it's the actual source behind this chapter's BFF-pattern
token-storage guidance. SAML 2.0 is confirmed still in live production use alongside OIDC as of 2026
(not deprecated), per live research — chapter 17 cites this correctly rather than asserting staleness.

**Verification method (same as every prior round):** all 6 files re-parsed cleanly, re-served and
`curl`-confirmed `200`, all deep-dive links live-checked by their own subagent (several caught and
fixed dead links themselves before shipping — same pattern as ch03/ch04/ch16 in earlier rounds). Full
glossary/quiz merge checked programmatically: 279 glossary terms / 322 quiz questions, zero duplicates
of either kind, confirmed via a Node script with a `window` shim, same approach every round has used.

### GATE-4 review summary

**What was built:** Chapters 19-26 (Part IV — Keycloak Core Administration) in full, plus the shared
`labs/docker` Keycloak+PostgreSQL lab environment that every later Keycloak chapter now depends on.
This is the course's first unit where Keycloak itself exists and is administered for real, start to
finish, against the `northwind-apps` realm established in chapter 20. Full per-chapter counts are in
the Done entry below; highlights here are what needed independent judgment.

**The headline payoff — chapter 23's Mover-event reproduction:** chapter 10 (Part II, governance) used
Priya moving from Northwind's support team to its platform-engineering team as a worked example of the
JML "Mover" event, warning that treating it as purely additive (granting new access, never removing
old) is the most common beginner mistake. Chapter 23's lab reproduces this literally against a real
running realm: `priya.patel` genuinely held `support-agent`/`Support` before, genuinely holds
`platform-engineer`/`Engineering` after, and the chapter captured both states via independent read-only
`kcadm.sh` queries (not inferred from the mutating commands' own exit codes) proving the old role and
group membership were completely absent afterward, not merely superseded. A chapter 2 scenario and a
chapter 10 lesson both pay off hands-on here — exactly the concept-to-Keycloak bridge CLAUDE.md asks for.

**A real gotcha, found independently and cross-confirmed by two different subagents:** chapters 22 and
24 each — independently, days apart in wall-clock terms but the same session — hit the identical real
Keycloak 26 behavior: writing a custom user attribute (`department`) via `kcadm.sh` returns success
with zero error output, but the attribute is silently never persisted. Both root-caused it the same way
(Keycloak's Declarative User Profile only persists attributes it has declared in its schema; anything
else is an "unmanaged attribute," dropped under the default `unmanagedAttributePolicy`), both fixed it
the same way (`kcadm.sh update users/profile -s unmanagedAttributePolicy=ENABLED`), and both verified
the fix the same way (a follow-up `get` showing the attribute now present). Two subagents independently
converging on the same mechanism, the same fix, and the same verification method is strong
corroboration on its own — the coordinator judged this did not need a third, separate re-verification
pass before merging the (deduplicated) `Declarative User Profile` / `Unmanaged attribute (Keycloak)`
glossary entries. Chapter 24 additionally minted a real token end-to-end and decoded it, confirming the
custom `department` claim actually appeared in the ID token, the access token, and a live UserInfo
response — not just that the write succeeded.

**Another real, confirmed finding — Keycloak's theme/REST layer validates nothing:** chapter 26's lab
set a realm's `adminTheme` to `base`, a theme explicitly marked `abstract=true` (meant only to be
inherited from, never selected directly) in its own `theme.properties`. The write succeeded and
persisted with no error. The chapter confirmed this is a UI-only restriction (the Admin Console's
Themes-tab dropdown hides abstract themes) — the Admin REST API itself enforces no sanity check on
theme names at all, a real finding with practical consequences (a `loginTheme` typo would persist
silently and only surface the next time someone actually looks at the rendered login page).

**The Docker Compose project-name/port collision** (detailed above under "Current unit" and folded into
the `## Deviations from CLAUDE.md` section below) was this round's one real process friction: 5 of the 6
parallel ch21-26 subagents independently hit it and independently converged on the same workaround.
Nothing here affected chapter content quality — every affected chapter's actual lab run completed
cleanly on its isolated stack — but it's worth standardizing on `-p <chapter-slug>kc` project names up
front for Part V's parallel rounds rather than relying on each agent to rediscover the same fix.

**Cross-chapter dedup catches during merge** (this was the largest merge round so far — 102 new terms
proposed across 6 chapters before dedup): `Client role` (ch23) and `Client role (Keycloak)` (ch24)
merged into one entry under ch23's shorter name, combining both chapters' framing (namespace vs. REST
resource); `Declarative User Profile` and `Unmanaged attribute (Keycloak)` each proposed independently
and near-identically by ch22 and ch24, merged into one entry each (see above); `Required action` (ch22)
and `Required action (Keycloak)` (ch25) merged under ch22's name; `Default required action (Keycloak)`
(ch22) and `Default required action` (ch25) merged the same way. Plus 15 separate extend-existing-term
requests applied during this merge (`SSO session`, `Role`, `Role change (mover event)`, `Client (OAuth
role)`, `Scope (OAuth)`, `Custom claim`, `Redirect URI (registered)`, `Step-up authentication`,
`Multi-factor authentication (MFA)`, `Identifier`, `Credential`, `Attribute`, `Identity lifecycle`,
`Self-service (account management)`, `Extensibility (plugin architecture)`).

**Verification method (same as every prior round):** all 6 new chapter files re-parsed cleanly (Python
`html.parser`, zero errors), balanced tags, all 4 required mount points and 5 script tags present in
order, re-served locally and `curl`-confirmed `200` for every chapter plus the 4 shell pages and all 3
shared data files. Full glossary/quiz merge checked programmatically via a Node script with a `window`
shim: **413 glossary terms, zero duplicates, zero dangling chapter-slug references**; **457 quiz
questions across 27 pools, zero duplicate ids** (new prefixes `rst-`/`usr-`/`rlg-`/`cli-`/`afl-`/`thm-`,
checked against all prior prefixes). `nav-data.js`'s `built` flag flipped to `true` for all 6 chapters
only after every check passed. Confirmed via `docker ps -a`/`docker volume ls`/`docker network ls` that
no stray containers, volumes, or networks from this round's lab work (shared or any sibling's isolated
project) remain.

### GATE-5 review summary

**What was built:** Parts VI-VII in full (chapters 31-38) — the reader can now secure a real backend
API, a real frontend SPA, a real service account, broker an external IdP, federate with LDAP, write a
custom User Storage SPI, and configure fine-grained authorization via Keycloak's Authorization Services
(UMA 2.0) through to the honest limits of that model versus a dedicated policy/relationship engine. Full
per-chapter detail is in the Done entries for Part VI (two rounds) and Part VII above; this summary is
what needed independent judgment across the two chapters that actually close this gate.

**The headline payoff — a real, reproducible UMA 2.0 permit/deny pair:** chapter 37's lab didn't just
narrate the UMA grant, it ran it twice against the same live permission (one role-holding user, one
not), decoded a real RPT's `authorization.permissions` claim, then re-ran the exact same two requests
fresh at the very end — after deliberately perturbing and reverting the permission's decision strategy
in between — to confirm the transcript in the chapter is genuinely reproducible, not a one-off capture
quietly hand-edited afterward. The decision-strategy experiment (flipping Unanimous→Affirmative with
zero other change and watching sam.chen's identical request flip from 403 to 200) is the kind of "the
reader can predict this before being told" demonstration CLAUDE.md's hands-on-is-real rule is for.

**A finding that would have been easy to get wrong by trusting an older source:** older Keycloak
tutorials and some still-circulating documentation describe a JavaScript/"script" authorization policy
type. Chapter 37 didn't assume that still holds — it tested the live REST surface directly on Keycloak
26.8.2 and found the policy-provider list never lists a js/script type, and the legacy endpoint itself
now 404s. The chapter states this as a removal, not a deprecation or a hidden feature flag, because
that's what the live behavior actually showed.

**A genuine, unresolved ambiguity surfaced rather than flattened:** chapter 38 needed a current Cedar
version/status claim and found AWS's own "Cedar 4" framing (a language-spec version) doesn't cleanly
match the cedar-policy GitHub repo's own release tags (already past v4.14.1 as an engine/crate version)
— two different things both casually called "version 4" in different official sources. Per CLAUDE.md §2
rule 1 ("if a source disagrees with itself... say so"), the chapter says this plainly and points the
reader at both sources to check live, rather than picking one number and asserting it with false
confidence. The same honesty appears in how the chapter treats ReBAC: it states flatly that Keycloak has
no native relationship-tuple concept, then costs out three real approximation strategies rather than
pretending any of them is free.

**Cross-chapter dedup catches during merge:** `Attribute-Based Access Control (ABAC)` and
`Relationship-Based Access Control (ReBAC)` were each independently requested for extension by *both*
chapter 37 and chapter 38 (chapter 6's original concept-level entries) — merged into one combined
`chapters` array addition per term rather than applied twice or shipped as competing edits. `Scope
(OAuth)`'s existing definition also got a one-sentence cross-reference added, pointing at chapter 37's
new, deliberately distinct `Authorization scope (Keycloak)` term, per chapter 37's own flagged request.

**Verification method (same as every prior round):** both new chapter files re-parsed cleanly (Python
`html.parser`, zero errors), balanced tags, both required mount points and the full deck-outline/
deck-pager/`SlideDeck.init` markup present, re-served locally and `curl`-confirmed `200` for both
chapters. Full glossary/quiz merge checked programmatically via a Node script with a `window` shim:
**583 glossary terms, zero duplicates** (up from 563); **656 quiz questions across 39 pools, zero
duplicate ids** (up from 623/37, new prefixes `kas-`/`fgap-` checked against all prior prefixes).
`nav-data.js`'s `built` flag flipped to `true` for both chapters only after every check passed. All 9
external deep-dive links across both chapters (Kantara UMA 2.0 specs, Keycloak Authorization Services
guide and Admin REST API reference, OPA docs, Cedar site, Google's Zanzibar paper, OpenFGA docs)
live-curled and confirmed `200`. Confirmed via `docker ps -a`/`docker volume ls`/`docker network ls` that
chapter 37's isolated `ch37kc` lab stack left no stray containers, volumes, or networks behind.

## Done

### Part XI: chapters 49-50 — Becoming an IAM Specialist / Master Glossary & Cheat Sheets (2026-11-12)
- Built as 2 parallel fork agents (no lab in either chapter, per CLAUDE.md §5), then centrally merged.
  Both set `built: true` in `nav-data.js`; full brief is preserved above under the former "Current unit"
  section this entry replaces.
- **Ch 49 — Becoming an IAM Specialist**: skills matrix (vendor-neutral concept ↔ Keycloak-specific
  skill, e.g. chapter 6's authorization models ↔ chapter 37's UMA 2.0), brief career-path section (IAM
  Engineer/Architect, CIAM Specialist, IGA Analyst, PAM Engineer — the last named honestly as adjacent,
  only surveyed in ch12, not built in a lab), a certifications section live-verified via WebSearch on
  2026-11-12 (CISSP — corrected a stale "10 domains" figure to the current 8, IAM is Domain 5; IDPro's
  CIDPro; the Identity Management Institute's CIAM, explicitly disambiguated from the unrelated
  "Customer IAM" market category sharing the same acronym; Microsoft SC-300; Okta's
  Professional/Administrator/Consultant/Developer track; SailPoint's Certified Identity Security
  Administrator, newly launched 2026-02-17; CyberArk Defender/Sentry), and a genuinely honest finding:
  **no dedicated Keycloak certification exists** — Red Hat's closest credential (EX362) certifies a
  different product (RHEL IdM/FreeIPA), not Keycloak, so the chapter frames a real capstone walkthrough
  as the substitute for the vendor badge Keycloak doesn't offer. 10-slide deck, 2 diagrams, 16-question
  quiz pool (`becoming-an-iam-specialist`, ids `ch49-01..16`), interview question bank (mix of
  concept-recall and scenario questions reusing all three orgs + the capstone), 5+ self-check with model
  answers, 8+ takeaways. 1 new glossary term ("IAM skills matrix" — checked for collisions, none found).
  All 7 live cert/standards links re-verified 200 by the coordinator.
- **Ch 50 — Master Glossary & Cheat Sheets**: a reference chapter, not a new-concept one — curates
  existing material into 6 named cheat sheets (OAuth grant types, realm/client/scope relationships,
  token types compared, authorization-model comparison, SAML-vs-OIDC quick contrast, the three orgs at a
  glance) plus a bonus kcadm quick-reference and a "where to look it up" index pointing at
  `glossary.html`/`quizzes.html`/`labs.html`/`exams.html` for ongoing post-course use — all grounded in
  grepped facts from chapters 13-14, 17, 20-24, 27, 37-38, 46-48, and the capstone, no invented facts.
  3 diagrams, 16-question quiz pool (`master-glossary-and-cheatsheets`, ids `mgc-01..16`, deliberately
  cumulative/scenario-heavy like the exams rather than narrow recall), 5+ self-check, 8+ takeaways.
  **Zero new glossary terms** (by design — this chapter cross-references, it doesn't redefine); the
  coordinator extended the `chapters` array of 14 existing terms (Access/Refresh/ID tokens, RPT, Default/
  Optional client scope, Protocol mapper, ACL/RBAC/ABAC/PBAC/ReBAC, Realm role, Client role) to include
  this chapter's slug so its glossary-mount isn't empty. No miscategorized/duplicate/orphaned terms were
  flagged during this pass — the Final QA sweep (queue item 21) starts clean on that front. One flagged
  false alarm resolved: the agent thought "CIAM" had no glossary term despite chapter 12 defining it;
  the coordinator confirmed the term does exist (`CIAM (Customer Identity and Access Management)`,
  already correctly scoped to `what-is-iam`/`iam-market-landscape`) — no action needed.
- **Central merge verification** (coordinator, independent of agent self-reports): spliced both quiz
  pools into `quiz-data.js` (`node --check` passed; confirmed 962 unique quiz ids site-wide, zero
  duplicates, up from 930); added the 1 new glossary term and extended 14 existing terms (672 total
  terms, zero duplicate names); flipped `built:true` for both chapters in `nav-data.js` (`node --check`
  passed); ran the tag-stack balance checker against both new chapter files (clean); confirmed all
  internal links in both chapters resolve on disk; live-curled all 11 external deep-dive links across
  both chapters (all 200).
- **Closed a stale cross-reference**: `exams/exam-final.html` (built before these two chapters existed)
  had explicitly said chapters 49-50 "were not yet built" and deferred their coverage — updated both
  mentions now that the chapters exist, pointing directly at `chapters/49-...html`/`chapters/50-...html`
  instead of the stale caveat. Re-verified `exam-final.html` still parses and its links resolve.
- **Part XI is now fully complete.** Not yet verified in a real browser (no browser tool in this
  session) — standing caveat, same as every other chapter this build.

### Exams: exam-foundations/protocols/keycloak-core/integration/final + exams.html (2026-11-12)
- **Gap found and closed**: CLAUDE.md §3 specified an `exams/` directory that was never scheduled into
  this queue across the whole build. Flagged to the user before closing Part XI/Final QA; they chose to
  add it now (see queue item 19's note) rather than drop or defer the feature.
- Built as 5 parallel fork agents, one per exam, each scoped to a cumulative chapter range matching one
  of the course's named gates (see the queue item 19 note for the exact mapping) and instructed to
  ground questions in real, grepped chapter/lab facts rather than inventing them, and to propose its
  quiz pool in its report rather than editing `quiz-data.js` directly (avoiding a 5-way write conflict).
  Each page: an intro (scope, time budget, suggested passing bar, explicitly framed as self-check not
  gatekeeper), a graded cumulative quiz section (20 questions for the first four exams, 25 for
  `exam-final`), and a 5-question essay section (6 for `exam-final`) in the same click-to-reveal
  `<details>` pattern every chapter's self-check already uses — "defend your answer to an interviewer,"
  with full model answers, not blank thinking prompts.
- **exam-foundations** (chapters 0-9, GATE-2): 20 questions, 60% scenario, tied to all three orgs.
- **exam-protocols** (chapters 10-18, GATE-3): 20 questions, 65% scenario, mixing governance (SoD,
  compliance-framework mapping) with real protocol mechanics (PKCE, mix-up attacks, JWKS/`kid` rotation,
  a live-verified redirect_uri validation gap from chapter 13).
- **exam-keycloak-core** (chapters 19-26, GATE-4): 20 questions grounded in the real `northwind-apps`
  realm chapters 19-26 actually built (kcadm commands, protocol-mapper troubleshooting order, the
  `conditional-user-role` authenticator, the four realm-level theme settings).
- **exam-integration** (chapters 27-38, GATE-5): 20 questions deliberately spread across all 12
  chapters with no cluster (admin API/CLI, events/auditing, sessions, brute-force policy, brokering,
  LDAP federation, custom SPI, backend/frontend security, M2M, UMA 2.0, ABAC/ReBAC/OPA).
- **exam-final** (chapters 39-48 + the capstone + a cumulative course-wide review, Final QA gate): 25
  questions — ~10 on chapters 39-45, ~9 synthesis questions bridging a Part I-III concept to its later
  Keycloak implementation, ~6 grounded directly in the real, running capstone build (both documented
  bugs, the UMA permission matrix, the `tenant_id` claim). Confirmed chapters 49/50 don't exist yet and
  said so explicitly rather than guessing at their content.
- **`exams.html`**: new index page (coordinator-built — predictable, static content, no agent needed),
  one `.part-card` per exam naming its scope and matching gate. Added an "Exams" entry to `index.html`'s
  quick-links nav, and made `roadmap.html`'s GATE-2/3/4/5 and "Final QA gate" badges real links to the
  matching exam page (GATE-1 stays a plain, unlinked badge — verified the link-building logic for all
  five gate values, including the null/no-gate case, by extracting and running it directly in Node).
- **Independent verification done by the coordinator**: all 5 exam quiz pools (105 questions total)
  checked in Node for duplicate ids, 4-option/in-bounds-`answerIndex` validity, and zero collisions
  against any of the site's other 49 quiz pools after merge (930 questions site-wide). All 5 exam HTML
  files plus `exams.html`/`index.html`/`roadmap.html` parsed with a proper tag-stack checker (zero
  mismatches) and every internal link resolves to a real file. Confirmed each exam's script order
  deliberately omits `slides.js` (these are one-shot exam pages, not slide decks) and that each
  `QuizEngine.renderQuiz` call's slug matches its own pool exactly.
- **One process note**: the `exam-final` agent's first report described its quiz pool as "in the report
  body above" without actually including it — asked it to resend the full JSON in a follow-up message
  (`SendMessage` to the same agent), which it did correctly on request. Worth watching for on future
  large-report agents.
- **What's NOT yet verified**: no browser tool in this environment — nobody has visually rendered any
  of the 5 exam pages, `exams.html`, or clicked through a live quiz attempt. Same standing limitation as
  every prior unit.

### The capstone: Northwind Apps end-to-end multi-app platform — Part X COMPLETE (2026-11-12)
See "The capstone — COMPLETE" under Current unit above for full detail (what was built, the two real
bugs found and fixed, and everything the coordinator independently re-verified against the live stack).
Summary: `capstone/` (docker-compose, seed script, `billing-api/`, `dashboard-spa/`, M2M job, README)
plus `capstone.html`, linked from `index.html`/`case-studies.html`/chapter 46. The Acme Corp brokering
stretch goal was deliberately cut and documented as such. This closes Part X entirely — no gate sits
immediately after it per CLAUDE.md §8; Part XI (chapters 49-50) is next, then the Final QA gate.

### Part X case studies: chapters 46-48 + case-studies.html + cs01-cs03 (2026-11-12)
- Built as 3 parallel fork agents, one per running org (Northwind Apps → ch46/cs01, Riverside Health →
  ch47/cs02, Meridian Trust → ch48/cs03), each grepping the repo first for every existing mention of its
  org (34/29/34 chapters already referenced it by name) to stay consistent with established canon
  (realms, roles, users, clients, prior incidents) rather than inventing new facts. Each agent wrote a
  full slide-deck chapter (same template as every other chapter; the "hands-on" slot points at the full
  write-up instead of a Docker lab, per CLAUDE.md §5's Part X table) plus a new long-form write-up page
  in a new `case-studies/` directory, built on the shell/book-page template (`labs.html`'s layout, not
  the slide-deck one, since it's a reference document meant to be read start-to-finish or skimmed, not
  a lesson). Agents proposed glossary/quiz additions in their reports rather than editing shared files
  directly, per the standing anti-collision pattern; the coordinator merged centrally.
- **Chapter 46 — Case Study: Northwind Apps** (`chapters/46-case-study-northwind.html` +
  `case-studies/cs01-northwind.html`): 4 diagrams, 9 takeaways, 5 self-check, 15 quiz questions, 2 new
  glossary terms ("Tenant isolation", "Realm-per-tenant model") plus `case-study-northwind` added to 2
  existing terms' `chapters` arrays (`Northwind Apps role model...`, `JIT (just-in-time) provisioning`).
  Synthesizes the shared-realm-plus-`tenant_id` tenancy decision, enterprise SSO brokering (Acme Corp),
  RBAC+UMA layered authorization, and the chapter-9 token-blast-radius incident into one design
  narrative.
- **Chapter 47 — Case Study: Riverside Health** (`chapters/47-case-study-riverside.html` +
  `case-studies/cs02-riverside.html`): 3 diagrams, 9 takeaways, 6 self-check, 15 quiz questions, 0 new
  glossary terms (deliberate judgment call for a synthesis chapter, same one chapter 0 made — noted
  rather than padded), 6 existing terms extended (`Step-up authentication`, `Care team relationship`,
  `Break-glass access`, `Minimum necessary standard`, `READ_ONLY edit mode`, `Decision strategy`).
  Resolved a genuine open gap in established canon: chapter 32's self-check asked a hypothetical about
  Riverside using WRITABLE vs. READ_ONLY LDAP edit mode (chapter 32's own primary lab is actually
  Meridian's staff directory, not Riverside's) — this chapter commits Riverside to READ_ONLY,
  consistent with that self-check's own model answer, closing the gap rather than leaving it open or
  contradicting it.
- **Chapter 48 — Case Study: Meridian Trust** (`chapters/48-case-study-meridian.html` +
  `case-studies/cs03-meridian.html`): 3 diagrams, 9 takeaways, 5 self-check, 15 quiz questions, 0 new
  glossary terms, 3 existing terms extended (`Cardholder Data Environment (CDE)`, `Zanzibar model
  (relationship-tuple authorization)`, `Relationship-Based Access Control (ReBAC)`). Synthesizes the
  single-realm/split-by-client-and-role design, the transfer-creator/transfer-approver SoD conflict
  from chapter 10/23, risk-based step-up tied to the chapter-9 credential-stuffing incident, and the
  RBAC→ABAC→ReBAC layering from chapter 38 into one design narrative.
- **`case-studies.html`**: new index page (coordinator-built, no agent needed — static, predictable
  content), shell/book-page template, one `.part-card` per org with an `org-chip` badge and links to
  both that org's chapter and its write-up. Added a "Case studies" entry to `index.html`'s top
  quick-links nav (previously only Study pace/Glossary/Quizzes/Labs).
- **Independent verification done by the coordinator:** all 8 touched/new HTML files parsed with a
  proper tag-stack checker (zero mismatches); every required mount point and the 6-script include
  order confirmed present and in the same order as chapter 1's reference template in all three new
  chapters; every internal link across all 8 files resolves to a real file (checked programmatically);
  all 13 deep-dive links across the three chapters re-curled live by the coordinator (13/13 → `200`);
  merged `glossary-data.js`/`quiz-data.js` checked in Node with a `window` shim (671 glossary terms,
  zero duplicate term names; 3 new 15-question quiz pools, zero duplicate ids anywhere in the merged
  file, every option count/answerIndex in bounds; 51 nav entries, zero duplicate slugs, chapters 46-48
  now `built: true`); `node --check` clean on all three edited data files.
- **What's NOT yet verified:** no browser tool in this environment — nobody has visually rendered the
  three new chapters, the three write-ups, or the new index page. Same standing
  limitation as every prior gate; recommend a visual pass before considering this unit fully closed.

### Part IX Round 2: chapters 43-45, performance/backup-DR/hardening — Part IX COMPLETE (2026-11-11)
- Built as three parallel subagents (all no-lab/research-heavy, safe to fully parallelize — no Docker/K8s
  contention), then centrally merged (`glossary-data.js`, `quiz-data.js`, `nav-data.js`) and independently
  re-verified (html.parser tag-balance on all three files, mount points + deck markup present, HTTP 200
  served, zero duplicate glossary terms/quiz ids after merge, all 20 unique deep-dive links across the
  three chapters live-curled to 200). One real syntax bug caught and fixed during merge: an edit to the
  `Multi-site (multi-cluster) deployment` extension used literal unescaped double quotes inside a
  double-quoted JS string, which `node --check` caught immediately — fixed before it ever reached a commit.
- **Chapter 43 — Performance Tuning & Database Considerations** (`chapters/43-performance-tuning.html`,
  15 slides, 4 diagrams, no lab per the curriculum table). Live-verified Keycloak 26.8.2's actual tested
  DB versions (PostgreSQL 18, MariaDB 11.8, MySQL 8.4, MSSQL 2022, Oracle 23.5 — a real refresh over the
  existing glossary entry's vague "as of 26.x" text, applied during merge) and — since Keycloak's own
  docs never name the connection-pool library in prose — confirmed it's **Agroal**, not HikariCP, from the
  `agroal_*`-prefixed metric names in Keycloak's own observability guide. Found and used a genuinely
  current "bonus" fact as its central cache-tuning trade-off: Keycloak's `stateless` feature (moves
  session/action-token state from Infinispan into the database) was **promoted from preview to supported
  in 26.8.0**, with a documented, concrete cost (~8-10ms added auth latency, ~2x DB CPU/IOPS, single-region
  only) — paired directly against chapter 41's Infinispan material rather than re-explaining it. Chose
  Meridian Trust as its case study specifically because its traffic shapes map onto Keycloak's own HA
  capacity-sizing guide, and reused that guide's own real throughput numbers rather than inventing figures.
- **Chapter 44 — Backup, Disaster Recovery & Multi-Region** (`chapters/44-backup-and-disaster-recovery.html`,
  13 slides, 4 diagrams, no lab). Live-verified `kc.sh export`/`import`'s real current flags and documented
  gap (does not capture events, persisted sessions, workflow state, or revoked tokens — and the Admin
  Console's partial-export path is explicitly documented as **not suitable for backups**). Confirmed
  Keycloak's own docs name multi-cluster v1 as **deprecated and scheduled for removal**, in favor of
  multi-cluster v2 — and that neither "active-active" nor "active-passive" terminology appears anywhere in
  Keycloak's own multi-site docs, which the chapter says plainly rather than forcing that framing onto it.
  Honestly flagged a real documentation gap: no published RTO/RPO numbers, no failover runbook, and an
  explicit statement that without load-balancer health probing "site failures are not detected
  automatically... until manual intervention." HIPAA tie-in verified directly from 45 CFR §164.308(a)(7)'s
  actual regulatory text (Cornell LII), correctly distinguishing its three Required specs from its two
  Addressable ones.
- **Chapter 45 — Security Hardening & Upgrade Strategy** (`chapters/45-security-hardening-and-upgrades.html`,
  14 slides, 2 diagrams — intentionally lower per the curriculum's own allowance for a checklist-style
  chapter — no lab). A genuine synthesis chapter: every checklist row cites back to where it was actually
  covered (chapters 9/18/21/30) rather than re-deriving it. Live-verified two real, current, named CVEs
  (CVE-2025-10939 — admin-path exposure via reverse-proxy path normalization; CVE-2024-11736 — an
  env-var-leak via URL placeholder expressions, fixed in 26.0.8) plus five more in the 26.8.x line from the
  real upgrading guide, one of which (CVE-2026-109081's client-registration-policy fix) is **explicitly
  flagged as unsafe for a rolling upgrade** — a concrete, current reason to read a target version's own
  release notes rather than assume any upgrade can simply roll node-by-node. Honestly reported one research
  limitation: couldn't fetch the full admin-guide body text verbatim for every checklist subsection (a
  ~690K-char merged doc page), and said so rather than fabricating quotes for the gaps.
- Glossary: merged 32 new terms (14 from ch43, 13 from ch44, 5 from ch45 — ch45's low count was deliberate,
  per its own brief, since it's mostly a cross-reference chapter) plus extensions to 12 existing terms,
  three of which (`Infinispan`, `Single-cluster HA vs. multi-site HA`, `Split-brain (cluster partition)`)
  were extended a second time on top of Part IX Round 1's own additions, correctly layering rather than
  overwriting. One enhancement applied during merge beyond a plain chapters-array append: `Database backend
  (Keycloak)`'s stale "as of Keycloak 26.x" text was refreshed with ch43's live-verified, dated tested-
  version numbers. Glossary grew 637 → 669 terms, zero duplicates.
- Quiz: merged 3 new pools (`performance-tuning`: 18 questions; `backup-and-disaster-recovery`: 18;
  `security-hardening-and-upgrades`: 18), all independently confirmed at or above 50% scenario-framed with
  no coordinator retyping needed this round. Quiz pools grew 43 → 46, questions 726 → 780, zero duplicate
  ids.
- `nav-data.js`: `built` flipped `true` for chapters 43, 44, and 45. **Part IX (chapters 41-45) is now
  fully complete.**

### Part IX Round 1: chapters 41-42, HA/clustering and Keycloak on Kubernetes (2026-11-11)
- Dev-machine tooling confirmed before dispatch: Docker 29.6.2, `kind` 0.24.0, `kubectl` v1.31.3, `helm`
  v3.16.3 present (no `minikube`, but `kind` satisfies the curriculum's lab requirement for real, not
  narrated). Disk was tight going in (7.6G free of 94G, 92% used) — both agents were told to check free
  space, clean up promptly, and degrade honestly if space got critical. Neither had to degrade: disk
  ended at **16G free (83% used)**, better than the starting point, mostly thanks to ch42's `docker
  builder prune -af` freeing ~7GB of build cache as its first move.
- Built as two parallel subagents against the drafted briefs, then centrally merged
  (`glossary-data.js`, `quiz-data.js`, `nav-data.js`) and independently re-verified by the coordinator
  (html.parser tag-balance on both files, both mount points + deck markup present, HTTP 200 served, zero
  duplicate glossary terms/quiz ids after merge, all 9 unique deep-dive links across both chapters
  live-curled to 200 — including one `http://` link, `jgroups.org`, caught by re-checking with a
  protocol-agnostic grep after the first pass missed it — `docker ps -a`/`kind get clusters`/`docker
  volume ls` confirmed clean for both).
- **Chapter 41 — High Availability & Clustering** (`chapters/41-high-availability-clustering.html`, 18
  slides, 4 diagrams). Honestly scoped as the curriculum's own "partial (single-node sim)" label
  requires: two real Keycloak 26.8.2 containers on one Docker host (`ch41kc`, ports 18141/18142), one
  shared Postgres, real JGroups/Infinispan clustering — a genuine multi-node simulation, explicitly not
  real multi-host failover (the chapter and its new "Split-brain" glossary entry both say so directly).
  Live-verified Keycloak 26.8.2's real `--cache`/`--cache-stack` defaults and deprecations directly from
  `start --help-all` rather than assuming older-version docs: `jdbc-ping` is the current non-Kubernetes
  default (not a static TCP/UDP host list), `--cache=local` (start-dev's own default) silently disables
  clustering with no warning, and JGroups traffic is mTLS-encrypted by default with auto-rotated ephemeral
  certs. Real two-node proof, run fresh twice: a refresh-token grant against node B for a session created
  only on node A returned a fresh token with the identical `sid`; a session revoked through node B was
  confirmed gone via node A while untouched sessions remained visible on both — bidirectional replication
  (read and write), not narrated. Two real bugs found: the recurring `VERIFY_PROFILE` dynamic-required-
  action trap (a 4th chapter to hit it, confirmed unrelated to clustering), and a genuinely new one — a
  cross-node token-issuer mismatch from each node computing `iss` off its own `Host` header, fixed by
  forcing one shared `KC_HOSTNAME`, modeling "behind one load balancer."
- **Chapter 42 — Keycloak on Kubernetes** (`chapters/42-keycloak-on-kubernetes.html`, 16 slides, 3
  diagrams). Live-verified the current (2026-11-11) officially-recommended deployment path directly from
  Keycloak's own docs rather than an old blog post: the modern Keycloak Operator via `kubectl apply -k`
  (OLM is the docs' own "recommended" label but needs OLM already running, judged unnecessary weight for
  a one-off teaching cluster) over the Bitnami Helm chart, whose free images moved to a legacy/paid
  repository around August 2025 — a real, dated maturity gap, not a style preference. Real lab: a `kind`
  cluster, the Operator installed, an ephemeral Postgres StatefulSet, a self-signed dev cert, and a
  Keycloak CR — real boot log (`Bootstrap completed in 23.153 seconds`, `Ready: True`), real `kubectl
  port-forward` verification (admin console 200, live OIDC discovery doc, a real signed token from a
  password-grant call), real `kind delete cluster` teardown. Four real gotchas documented: `kind`'s node
  has its own containerd image store separate from the host's (re-pulls images even when host-cached); the
  Operator issues a one-time `temp-admin` bootstrap account via a generated Secret, not chapter 20's
  persistent env-var admin; a one-letter Service/StatefulSet name mismatch in the docs' own example
  manifest; and the docs not covering a local/offline-friendly hostname setup at all (this build's own
  judgment call, flagged as such).
- Glossary: merged 25 new terms (13 from ch41's JGroups/Infinispan/clustering vocabulary; 12 from ch42's
  Operator/CRD/Kubernetes vocabulary) plus extensions to 5 existing terms (`Infinispan`, `Clustering
  (Keycloak)`, `High availability (Keycloak context)`, and `VERIFY_PROFILE (dynamic required action)` for
  ch41; `Kubernetes deployment (Keycloak)` for ch42) — each enriched with the chapter's own live-verified
  findings rather than just appending a chapter slug. Glossary grew 612 → 637 terms, zero duplicates.
- Quiz: merged 2 new pools (`high-availability-clustering`: 18 questions; `keycloak-on-kubernetes`: 18
  questions), both independently confirmed at exactly/over 50% scenario-framed with no coordinator fix
  needed this round. Quiz pools grew 41 → 43, questions 690 → 726, zero duplicate ids.
- `nav-data.js`: `built` flipped `true` for chapters 41 and 42.

### Part VIII complete: chapters 39-40, extending Keycloak via custom SPIs (2026-11-11)
- Built as two parallel subagents against the full briefs drafted into the Queue above, then centrally
  merged (`glossary-data.js`, `quiz-data.js`, `nav-data.js`) and independently re-verified by the
  coordinator (html.parser tag-balance on both files, both mount points + deck markup present, HTTP 200
  served, zero duplicate glossary terms/quiz ids after merge, all unique deep-dive links across both
  chapters live-curled to 200, `docker ps -a`/`volume ls`/`network ls` confirmed clean for both isolated
  stacks, confirmed no stray process left listening on either lab's ports).
- **Chapter 39 — Writing Custom SPIs** (`chapters/39-custom-spis.html`, 20 slides, 4 diagrams). One Maven
  project, three independent Java SPI providers (Authenticator, EventListenerProvider, ProtocolMapper),
  one per case-study org, all actually compiled and deployed into an isolated Docker stack (`ch39kc`,
  host port 18139). Live-verified that these three SPIs are split across four Keycloak Maven artifacts
  at `scope=provided` (`keycloak-server-spi`, `keycloak-server-spi-private`, `keycloak-services`,
  `keycloak-core`) — a different, larger dependency list than chapter 33's two. Real exercises: Meridian
  Trust's business-hours Authenticator denied/allowed real Direct Grant logins against the *real* server
  clock (only the authenticator's configured window changed, never the clock); Riverside Health's
  EventListenerProvider captured a real failed-login line in `docker compose logs`, confirmed
  realm-scoped (zero matching lines against a different realm); Northwind Apps' ProtocolMapper's
  `tenant_id` claim decoded out of a real access token, ID token, and `/userinfo` response, confirmed
  cleanly *absent* (not null) for a user with no attribute set. Five real bugs/gotchas hit and documented
  honestly: the recurring XML-comment-dash POM trap (chapter 33's bug, hit again independently); a new
  Docker finding — a rebuilt provider JAR isn't picked up by `docker compose restart`, only by
  `up -d --force-recreate` (bind mounts don't re-resolve on a plain restart); a real server bug where
  `context.failure()`'s single-argument overload threw an uncaught exception on the Direct Grant path
  (fixed with the two-argument overload); a red-herring "Account is not fully set up" error that was
  actually chapters 22/24's pre-existing Declarative User Profile gap, confirmed by isolating the new
  authenticator out of the failure; and a `Lightweight access token` client-setting gotcha breaking a
  `/userinfo` call unrelated to the new mapper's own code. Also cross-confirmed, independently, Keycloak
  26.8.2's `KC-SERVICES0047` "internal SPI" boot warning against the Server Developer Guide's own prose.
- **Chapter 40 — Custom Identity Providers & Required Actions** (`chapters/40-custom-idp-and-required-
  actions.html`, 19 slides, 3 diagrams). Two independent Java SPI providers, isolated as `ch40kc`/18140:
  a custom Identity Provider broker for Meridian Trust, modeling an invented, explicitly-labeled-as-toy
  "Cascade SSO" protocol for a fictional acquired bank (no genuinely simple real legacy-SSO protocol was
  found worth reaching for instead); and a custom Required Action for Riverside Health (a HIPAA
  acknowledgment step). Live-verified both SPI contracts live in `keycloak-server-spi-private` — the same
  artifact, despite being unrelated SPI families, and a different split than chapter 33's User Storage
  SPI needed. Real exercises: a full redirect-out → toy-IdP login → HMAC-signed callback-in → First
  Broker Login → JIT-provisioned real Keycloak user round trip, run with two different users plus a
  third re-run confirming repeat-login recognizes the existing federated identity instead of
  re-provisioning; the HIPAA required action actually intercepted two different clinicians' logins before
  their sessions completed, with both a real decline (400) and a real accept (cleared `requiredActions`,
  issued a real auth code) exercised. A genuinely new finding beyond chapter 33's precedent: a custom
  Required Action needs explicit per-realm registration (`POST .../register-required-action`) before it
  does anything on that realm, even if a user's `requiredActions` list already names it — the Identity
  Provider SPI has no equivalent per-realm switch at all. Toy IdP run as a real host Python process
  (deliberately not containerized — every redirect in the round trip is browser/curl-driven, never
  server-to-server, so no container-to-host bridging was needed), killed cleanly after the lab.
- Glossary: merged 29 new terms (15 from ch39's Authenticator/Event-Listener/Protocol-Mapper vocabulary;
  14 from ch40's Identity-Provider/Required-Action vocabulary) plus extensions to 10 existing terms, two
  of which (`Required action`, `Unmanaged attribute (Keycloak)`) were requested by both chapters and
  merged into single combined `chapters`-array additions rather than applied twice. One existing term's
  prose was tightened during merge: `Authenticator SPI`'s old "forward-pointer to chapters 39-40" was
  corrected to name only chapter 39, since chapter 40 implements the Identity Provider/Required Action
  SPIs, not the Authenticator SPI — a real inaccuracy chapter 40's agent flagged rather than silently
  carried forward. Glossary grew 583 → 612 terms, zero duplicates.
- Quiz: merged 2 new pools (`custom-spis`: 18 questions; `custom-idp-and-required-actions`: 16
  questions). One coordinator fix during merge: chapter 39's proposed pool was only 38.9% scenario-typed
  (7/18), short of the 50% floor — retyped two already case-study-org-framed `bug` questions
  (`cspi-06`, `cspi-09`) to `scenario` to reach exactly 9/18 = 50%, the same judgment call this project
  made for chapter 34. Quiz pools grew 39 → 41, questions 656 → 690, zero duplicate ids.
- `nav-data.js`: `built` flipped `true` for chapters 39 and 40.

### Part VII complete: chapters 37-38, fine-grained authorization (2026-11-11) — GATE-5 reached
- Built as two parallel subagents against the full briefs drafted into the Queue above, then centrally
  merged (`glossary-data.js`, `quiz-data.js`, `nav-data.js`) and independently re-verified by the
  coordinator (html.parser tag-balance on both files, both mount points + deck markup present, HTTP 200
  served, zero duplicate glossary terms/quiz ids after merge, all 9 external deep-dive links across both
  chapters live-curled to 200).
- **Chapter 37 — Keycloak Authorization Services** (`chapters/37-keycloak-authorization-services.html`,
  21 slides). Real lab: isolated Docker stack (`ch37kc`, host port 18137, Keycloak 26.8.2 + Postgres 17,
  scratch Compose override — shared `labs/docker/docker-compose.yml` untouched), built a Northwind Apps
  resource server (client `northwind-docs-api`), a resource/scope/role-policy/permission, and ran the real
  UMA 2.0 grant end to end with curl: a real permission ticket exchanged for a real RPT, one genuine 200
  (amara.diallo, holds the role) and one genuine 403 (sam.chen, doesn't), reproduced fresh at the end for a
  confirmed-reproducible transcript. Live-verified finding: Keycloak 26.8.2 has fully removed JS/"script"
  policies from the REST API (`.../policy/js` now 404s, not merely disabled) — stated plainly rather than
  assuming older-tutorial behavior still holds. Two other real bugs hit and documented: a resource's own
  `scopes` array must list a scope before any permission can reference it (resource+scope creation doesn't
  implicitly link them), and a denied UMA exchange's `error_description` reads `"request_submitted"` even
  when nothing is actually pending for approval — flagged as not a stable field to code against. Decision-
  strategy mechanics (Unanimous/Affirmative/Consensus) demonstrated live by flipping one permission's
  strategy and watching the identical request's outcome change. Torn down cleanly after
  (`docker compose -p ch37kc down -v`, confirmed no lingering containers/networks/volumes). Lab files:
  `labs/authorization-services/docker-compose.yml` + `README.md`.
- **Chapter 38 — Fine-Grained Access Patterns** (`chapters/38-fine-grained-access-patterns.html`, 15
  slides, no lab per the curriculum table). States plainly and repeatedly that Keycloak has no native
  ReBAC (no first-class relationship-tuple concept), walking through three honestly-costed approximation
  strategies (group-per-relationship, custom-attribute encoding, custom Policy SPI + external relationship
  store) against Meridian Trust's "manager-of-branch" example, then what a purpose-built engine
  (OpenFGA/Zanzibar model) buys instead. Live-verified OPA (CNCF Graduated since 2021-01-29, Rego as its
  language) and surfaced a genuine, unresolved ambiguity in Cedar's version numbering (the language-spec
  version vs. the cedar-policy crate version both get called "version 4" in different AWS/GitHub sources)
  rather than flattening it to one confident number, per CLAUDE.md §2 rule 1.
- Glossary: merged 20 new terms (16 from ch37's Authorization Services vocabulary — Resource server,
  Resource/Authorization scope/Policy/Permission (Keycloak-specific), Decision strategy, UMA 2.0,
  Permission ticket, RPT, PEP, PDP, Protection API, Policy evaluate tool, Owner-managed access, Aggregated
  policy, and a dedicated Script-policy-removal entry; 4 from ch38 — OPA, Rego, Cedar (policy language),
  Zanzibar model) plus 8 extend-existing-term requests, two of which (ABAC, ReBAC) were requested by both
  chapters and merged into a single combined `chapters` array rather than applied twice. Glossary grew
  563 → 583 terms, zero duplicates.
- Quiz: merged 2 new pools (`keycloak-authorization-services`: 16 questions, 50% scenario;
  `fine-grained-access-patterns`: 17 questions, 53% scenario incl. 2 compare-type) — both heavily grounded
  in each chapter's own real lab/research findings rather than generic trivia. Quiz pools grew 37 → 39,
  questions 623 → 656, zero duplicate ids.
- `nav-data.js`: `built` flipped `true` for chapters 37 and 38.

### Chapter 00 converted to the slide-deck template (2026-11-11)
- Chapter 00 was the last chapter left on the old book-shell layout (the reference-set build on
  2026-11-10 built it as a book page, before the slide-deck redesign existed; it was never swept up into
  the 02-30 redesign rollout since that rollout's scope was explicitly "already-built content chapters").
  Converted now at the user's request, same content/order, re-chunked into 12 slides (`intro`, `mastery`,
  `grading`, `environment-setup`, `course-map`, `study-loop`, `three-orgs`, `glossary`, `takeaways`,
  `self-check`, `quiz`, `deep-dive`).
- Self-check questions (5) previously had no shown answer (pre-dating the 2026-11-10 click-to-reveal
  rule) — all 5 now have real, chapter-grounded model answers in the `<details>` pattern.
- Fixed a pre-existing miscount carried over from the original prose: the chapter said "11 Parts" in two
  places while its own table/takeaways listed 12 (0 through XI) — corrected both to "12 Parts" to match
  the table, CLAUDE.md §5's own Part list, and chapter 1's "Where Keycloak fits" framing.
- No glossary/quiz changes needed: 0 glossary terms and a 12-question pool were already correctly
  flagged low on purpose (orientation chapter, per CLAUDE.md §6's explicit allowance) and remain
  unchanged.
- Verified: `html.parser` tag-balance clean, 12 `data-slide` ids present, both mount points
  (`chapter-glossary-mount`, `quiz-mount`) present, HTTP 200 served locally, `SlideDeck.init`/outline
  panel present in the served HTML.

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

### Shell pages (index/roadmap/glossary/quizzes, GATE-2)
- 2026-11-10 `index.html`, `roadmap.html`, `glossary.html`, `quizzes.html` complete — 4 pages, 4 parallel
  subagents (each read-only against the shared data files, no merge step needed), then independently
  re-verified by the coordinator once all four existed together (see GATE-2 summary above for full
  detail: re-parse, re-served + re-curled including cross-link resolution between the four pages, plus
  each subagent's own hand-traced/Node-replicated logic checks). **GATE-2 approved 2026-11-10.** This
  completes Part I end to end (content + shell pages).

### Part II — Governance & Compliance (chapters 10-12)
- 2026-11-10 chapters 10-12 complete — 3 chapters, 3 parallel subagents, then centrally merged and
  independently re-verified by the coordinator:
  - Structural: all 3 files parse cleanly, balanced tags, all mount points/script tags present, served
    and `curl`-confirmed 200.
  - Merged 49 new glossary terms (ch10: 15, ch11: 16, ch12: 18, no collisions among them or against the
    existing 127) plus 5 extend-existing-term requests applied (`Provisioning`, `Deprovisioning` from
    ch10; `Compliance-readiness` from ch11; `Workforce IAM`, `CIAM` from ch12) — total glossary now
    **176 terms, zero duplicates** (checked programmatically).
  - Merged 55 new quiz questions (ch10: 17, ch11: 20, ch12: 18) — total now **220, zero duplicate ids**
    (id prefixes `iga-`/`cmp-`/`mkt-`, checked programmatically alongside all 10 existing prefixes).
  - Ch10 defined `IGA` as the governance/process discipline; ch12 correctly only referenced it (market/
    tooling category) without redefining it — the cross-chapter dependency this round specifically
    planned for worked as intended.
  - Ch11's live compliance-framework research (GDPR, SOC 2 TSC, HIPAA Security Rule, PCI-DSS, ISO
    27001:2022) is summarized in its Done note below with explicit hedges where sources disagreed
    (SOC 2's draft revision, HIPAA's NPRM slippage, PCI-DSS's rumored successor version) — matches the
    no-invented-facts discipline.
  - **Ch12's most surprising claim — Palo Alto Networks acquiring CyberArk (Feb 2026, ~$25B) and
    rebranding it "Idira" (May 2026) — was independently re-verified by the coordinator**, not just
    accepted from the subagent's report: confirmed via a direct SEC 8-K filing search plus multiple
    independent news sources (247wallst, DigiTimes, pulse.bot), and the `paloaltonetworks.com/idira`
    URL the subagent cited was live-curled to `200` by the coordinator directly. This is exactly the
    kind of surprising-but-checkable claim worth re-verifying rather than trusting on the strength of
    a subagent's own citation list alone.
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 10-12 only after all checks passed.
  - This completes **Part II — Governance & Compliance** (chapters 10-12) in full. No gate sits between
    Part II and Part III in this project's plan (CLAUDE.md §8) — approval of this round isn't a hard
    blocker on starting Part III, but see Open questions for a real design decision Part III needs
    before chapter 13 can be drafted.

### Part III — Protocols (chapters 13-18, GATE-3)
- 2026-11-10 chapters 13-18 complete — 6 chapters, 6 parallel subagents, then centrally merged and
  independently re-verified by the coordinator (see GATE-3 summary above for full detail: the
  redirect_uri-validation finding re-tested directly by the coordinator, the login-attempt discrepancy
  between two subagents noted for future tracking, the live-verified RFC 9700/RFC 10017/SAML-currency
  facts, and the revocation-problem finding caught live in chapter 14's lab).
  - Diagram counts: ch13 3, ch14 4, ch15 3, ch16 4, ch17 3, ch18 3 — all meet or exceed the 3+ minimum.
  - Merged 103 new glossary terms after deduping 3 cross-chapter near-duplicates (`State parameter
    (CSRF defense)`, `Client credentials grant`, `Device authorization grant` — kept the fuller
    canonical version in each case, extended its `chapters` array rather than shipping a second entry)
    plus 5 extend-existing-term requests (`Redirect URI (registered)`, `Token introspection`, `Signing-
    key rotation` from Part I; `Assertion`, `Metadata (IdP/SP metadata)` from chapter 7) — total
    glossary now **279 terms, zero duplicates** (checked programmatically).
  - Merged 102 new quiz questions (ch13: 18, ch14: 16, ch15: 18, ch16: 16, ch17: 18, ch18: 16) — total
    now **322, zero duplicate ids** (prefixes `oac-`/`oax-`/`oidc-`/`jwt-`/`saml-`/`sec-`, checked
    against all 13 existing prefixes programmatically). Every `answerIndex` in bounds, every question
    has exactly 4 options.
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 13-18 only after all checks passed.
  - This completes **Part III — Protocols** (chapters 13-18) in full, and reaches **GATE-3**.

### Part IV, started — lab environment + chapters 19-20
- 2026-11-10 `labs/docker/docker-compose.yml`, `labs/docker/README.md`, `labs.html` complete — built
  directly by the coordinator, not delegated, and personally verified end-to-end before any chapter
  agent touched the environment: confirmed the real Keycloak image version (`docker run
  quay.io/keycloak/keycloak:26.8.2 --version`, not assumed), brought the real Docker Compose stack up,
  confirmed Keycloak reached ready state, logged in with `kcadm.sh` using the bootstrap admin, created
  a real test realm, confirmed it via a separate `get realms` call, then tore everything down (`docker
  compose down -v`) before handing the environment off. This meant every chapter 19-20 agent started
  from a known-working baseline instead of each independently discovering the same setup problems.
- 2026-11-10 chapters 19 (Keycloak architecture) and 20 (installing Keycloak) complete — 2 chapters, 2
  parallel subagents, then centrally merged and independently re-verified:
  - Structural: both files parse cleanly, balanced tags, all mount points/script tags present, served
    and `curl`-confirmed `200`. Chapter 20's agent personally tore its own test stack down afterward
    (`docker compose down -v`) — the coordinator independently confirmed via `docker ps -a` and `docker
    volume ls` that no stray containers or volumes were left behind before merging.
  - Chapter 20 is the course's first chapter with a real, running Keycloak instance. Its agent could not
    personally click through the Admin Console (no browser tool) and handled this exactly like Part
    III's device-flow labs did: everything machine-verifiable (the Docker stack, `kcadm.sh` login, real
    realm creation, a `get realms` confirmation) was actually run with real captured output; the Admin
    Console's exact screen layout is described from Keycloak's current official documentation and
    labeled as such, not claimed as personally observed. It also honestly reported being unable to curl
    the `/health/ready` endpoint from inside the container (the Keycloak image has no curl/wget, and the
    management port isn't exposed in this project's compose file) rather than fabricating sample output
    — and confirmed this matches Keycloak's own documented limitation.
  - Caught and resolved 3 cross-chapter glossary collisions during merge (`start-dev mode`, `Production
    mode (Keycloak)`, `Bootstrap admin`) — kept chapter 20's more mechanically detailed version of each
    (env vars, exact "only once" condition) as canonical, extended its `chapters` array to include
    `keycloak-architecture`, dropped chapter 19's duplicates. Chapter 19's agent had already proactively
    checked that "Realm" didn't already exist as a term before proposing `Realm (Keycloak)`; chapter
    20's own realm-creation-mechanics terms (`Realm creation (kcadm.sh / Admin Console)`, `Current realm
    (Admin Console)`) are differently scoped and were kept as separate, non-duplicate entries.
  - Merged 32 new glossary terms (ch19: 14 kept after dropping 3 duplicates, ch20: 18) plus 2 extend-
    existing-term requests (`Authorization Server` from ch13, `Admin console` from ch01) — total
    glossary now **311 terms, zero duplicates** (checked programmatically).
  - Merged 32 new quiz questions (ch19: 15, ch20: 17) — total now **354, zero duplicate ids** (prefixes
    `kca-`/`ick-`, checked against all 15 existing prefixes programmatically).
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 19-20 only after all checks passed.
  - The `northwind-apps` realm this project's Part IV plan depends on now genuinely exists as of this
    chapter's real lab run (though each later chapter's own lab recreates it fresh, per the plan).

### Part IV, completed — chapters 21-26 (GATE-4)
- 2026-11-10 chapters 21 (realms), 22 (users & credentials), 23 (roles & groups), 24 (clients & client
  scopes), 25 (authentication flows), 26 (themes & branding) complete — 6 chapters, 6 parallel
  subagents, then centrally merged and independently re-verified:
  - Structural: all 6 files parse cleanly (Python `html.parser`, zero errors), balanced tags, all 4
    required mount points and 5 script tags present in order, re-served locally and `curl`-confirmed
    `200` for every chapter plus the 4 shell pages and all 3 shared data files. All deep-dive links
    live-curled `200` by their own subagent before shipping.
  - Each chapter ran a real lab against the shared `labs/docker` Keycloak 26.8.2 + PostgreSQL 17 stack
    (or an isolated copy of it — see the Docker collision note below), continuing the `northwind-apps`
    realm chapter 20 created. Chapter 23 literally reproduces chapter 10's Priya "Mover event" scenario
    against real before/after state (see GATE-4 review summary above for detail). Chapter 24 minted a
    real token and decoded it to confirm a custom claim actually appeared in the ID token, access
    token, and UserInfo response. Chapter 25 cloned the real browser flow, added and configured a real
    `conditional-user-role` execution, and rebound the realm to the clone. Chapter 26 inspected
    Keycloak's actual bundled themes JAR (`org.keycloak.keycloak-themes-26.8.2.jar`) via Python's
    `zipfile` module rather than assuming directory layout, since the image ships no `unzip`.
  - **Real finding, cross-confirmed by two independent subagents**: chapters 22 and 24 each
    independently hit and correctly root-caused the same Keycloak 26 behavior — a custom user attribute
    write succeeds with no error but is silently dropped unless the realm's Declarative User Profile
    either declares the attribute or has `unmanagedAttributePolicy` set away from its default. Both
    fixed and verified it the same way. Treated as strong corroboration; not independently re-verified a
    third time by the coordinator (see GATE-4 review summary above for the reasoning).
  - **Real finding, chapter 26**: the Admin REST API performs zero validation that a `loginTheme` /
    `accountTheme` / `adminTheme` / `emailTheme` value names a real, directly-usable theme — it even
    accepted an `abstract=true` theme (`base`) as a direct realm setting. The "abstract" restriction is
    enforced only by the Admin Console's own dropdown UI.
  - **Docker Compose project-name/port collision** (see "Current unit" above for the full note): 5 of
    the 6 subagents this round independently hit it and independently converged on the same fix —
    isolating their own test stack under a custom `-p <chapter-slug>kc` project name and alternate host
    port, while keeping each chapter's reader-facing documented commands as the plain, standard form.
    No chapter's actual content or verified lab results were compromised by this; flagged here as a
    process note for Part V onward.
  - Caught and resolved 4 cross-chapter glossary collisions during merge: `Client role` (ch23) / `Client
    role (Keycloak)` (ch24) merged under ch23's name; `Declarative User Profile` (ch22/ch24, proposed
    near-identically by both) merged into one entry; `Unmanaged attribute (Keycloak)` (ch22/ch24) merged
    the same way; `Required action` (ch22) / `Required action (Keycloak)` (ch25) merged under ch22's
    name; `Default required action (Keycloak)` (ch22) / `Default required action` (ch25) merged under
    ch22's name. (That's 5 merges across what were 6 originally-independent proposals — `Client role`
    counted once above plus the other 4.)
  - Merged 102 new glossary terms (ch21: 15, ch22: 17, ch23: 15, ch24: 21 after dropping 3 duplicates,
    ch25: 16 after dropping 2 duplicates, ch26: 18) plus 15 extend-existing-term requests (`SSO
    session`, `Role`, `Role change (mover event)`, `Client (OAuth role)`, `Scope (OAuth)`, `Custom
    claim`, `Redirect URI (registered)`, `Step-up authentication`, `Multi-factor authentication (MFA)`,
    `Identifier`, `Credential`, `Attribute`, `Identity lifecycle`, `Self-service (account management)`,
    `Extensibility (plugin architecture)`) — total glossary now **413 terms, zero duplicates, zero
    dangling chapter-slug references** (checked programmatically).
  - Merged 103 new quiz questions (ch21: 15, ch22: 18, ch23: 18, ch24: 18, ch25: 18, ch26: 16) — total
    now **457 questions across 27 pools, zero duplicate ids** (prefixes `rst-`/`usr-`/`rlg-`/`cli-`/
    `afl-`/`thm-`, checked against all prior prefixes programmatically).
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 21-26 only after all checks passed.
  - **Part IV is now complete** (chapters 19-26, 8 chapters total). **→ GATE-4 reached.**

### Part V, completed — chapters 27-30 (no gate; next gate is GATE-5 after Part VII)
- 2026-11-10 chapters 27 (Admin REST API & kcadm CLI), 28 (events, auditing & monitoring), 29 (sessions
  deep dive & SSO behavior), 30 (password policies, brute-force & MFA config) complete — 4 chapters, 4
  parallel subagents, each pre-assigned an isolated Docker Compose project name/port up front in its
  brief (`ch27kc`→18127, `ch28kc`→18128, `ch29kc`→18129, `ch30kc`→18130), applying GATE-4's process
  note before any collision could happen rather than after — no collisions reported this round (one
  agent explicitly confirmed a sibling's isolated stack running concurrently on its own port with no
  interference).
  - Structural: all 4 files parse cleanly (Python `html.parser`, zero errors), balanced tags, all 4
    required mount points and 5 script tags present in order, re-served locally and `curl`-confirmed
    `200` for every chapter plus the 4 shell pages and all 3 shared data files. All deep-dive links
    live-curled `200` by their own subagent before shipping.
  - Each chapter ran a real lab against an isolated copy of the shared `labs/docker` stack, continuing
    the `northwind-apps` realm. Chapter 27 deliberately reproduced a real governance gap: a bulk-
    onboarding script's accounts left zero admin-event audit trail until `adminEventsEnabled` was
    turned on, and turning it on afterward did not retroactively record what already happened. Chapter
    28 enabled both event streams, generated a real admin event (replaying chapter 23's role-change
    scenario) and a real login event, and fetched both back via the REST API. Chapter 29 revoked a real
    session and a real refresh token separately, confirming via independent reads that session-level
    revocation kills every token immediately while refresh-token-level revocation (rotation/reuse
    detection) only rejects the one stale token and leaves the session alive — then demonstrated
    SSO-across-clients for real with a curl-only cookie-jar simulation across two independently
    registered clients. Chapter 30 set a real `passwordPolicy`, triggered and recovered from a real
    brute-force lockout via the Attack Detection API, and configured the OTP Policy tab.
  - **Real finding, affecting the shared lab environment itself**: chapter 28's lab discovered that
    `labs/docker/docker-compose.yml` set `KC_HEALTH_ENABLED=true` but never published port 9000 (the
    management interface `/health`/`/metrics` actually serve on), meaning those endpoints were
    unreachable from the host despite the env var suggesting they should work. Fixed directly in the
    shared compose file (added `"9000:9000"` to `ports` and `KC_METRICS_ENABLED: "true"`), personally
    re-verified end to end by the coordinator (`/health`→200 and `/metrics`→200 on port 9000; `/health`
    →404 on port 8080, confirming the port split chapter 28 described), and documented in
    `labs/docker/README.md`. This is a permanent fix to the shared environment, not a one-off workaround
    — every chapter from 28 onward (and any reader) now gets working health/metrics out of the box.
  - **Real brief correction**: chapter 30's lab found the realm's `passwordPolicy` string is joined with
    the literal word "and", not a semicolon as the original brief assumed — the chapter corrects this
    openly and the glossary entry for `Password policy (Keycloak)` states the correct mechanism.
  - **Cross-chapter dedup catch during merge**: chapters 27 and 28 each independently proposed an
    "Admin event" term (ch27: `Admin event`, scoped to its own automation angle and explicitly
    forward-pointing to ch28; ch28: `Admin event (Keycloak)`, the fuller canonical version) — both
    agents flagged the likely overlap themselves in their own reports without being asked. Merged into
    one entry under ch28's name, folding ch27's governance-gap finding (the bulk-onboarding audit-trail
    gap) into the merged definition, chapters extended to include both `events-and-auditing` and
    `admin-api-and-cli`.
  - Merged 63 new glossary terms (ch27: 15 minus 1 duplicate = 14, ch28: 17, ch29: 15, ch30: 17) plus 24
    extend-existing-term requests across the four chapters (including three substantive text extensions
    on `SSO session`, `Revoke Refresh Token`, and `Client Session Idle/Max` — chapter 29 making chapter
    21's and chapter 7's session settings concrete with real observed runtime behavior — plus one
    extension each on `Security Defenses tab (Realm Settings)` and `Direct access grants (Keycloak
    client setting)` from chapter 30, and 19 chapters-array-only additions) — total glossary now **476
    terms, zero duplicates, zero dangling chapter-slug references** (checked programmatically).
  - Merged 68 new quiz questions (ch27: 17, ch28: 19, ch29: 16, ch30: 16) — total now **525 questions
    across 31 pools, zero duplicate ids** (prefixes `api-`/`evt-`/`sdd-`/`ppm-`, checked against all
    prior prefixes programmatically).
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 27-30 only after all checks passed.
  - **Part V is now complete** (chapters 27-30, 4 chapters total). No gate here — next stop is GATE-5
    after Part VII (chapters 31-38).

### Redesign rollout — Part I converted (chapters 02-09)
- 2026-11-10 chapters 02-09 (8 chapters) converted from the old book format to the chapter-1 slide-deck
  template — 8 parallel subagents (one per chapter, no shared-file writes), then independently
  re-verified by the coordinator:
  - Structural: all 8 files re-parsed cleanly (Python `html.parser`, zero errors), balanced
    `<section class="slide">`/`<svg>`/`<details>`/`<summary>` tags confirmed by direct count, all
    required mount points (`deck-outline-panel`, `mark-all-btn`, `deck-progress-mini`, `deck-position`,
    `deck-viewport`, `pager-prev`/`pager-mark`/`pager-next`, `chapter-glossary-mount`, `quiz-mount`)
    present exactly once each, old-template ids (`sidebar-nav`, `chapter-nav-footer`) confirmed absent.
    Re-served locally and `curl`-confirmed `200` for all 8 chapter pages.
  - No shared data file was touched by any conversion agent (`glossary-data.js`, `quiz-data.js`,
    `nav-data.js`, `BUILD_STATUS.md` all untouched) — this was a presentation/structure pass only, no
    new glossary terms or quiz questions, confirmed via `git diff --stat` showing only the 8 chapter
    files modified.
  - Final slide counts: ch02 12, ch03 12, ch04 16, ch05 11, ch06 15, ch07 13, ch08 12, ch09 13.
  - Self-check: every existing self-check question across all 8 chapters (45 total) got a real,
    chapter-grounded model answer wrapped in `<details>`/`<summary>` (none left unanswered).
  - New diagrams added per the "prefer more visuals" rule, for sections that had zero diagrams
    originally: ch02 +1 (three-orgs intro cards), ch03 +1 (Northwind tenant-isolation scenario), ch04 +1
    (directory-service timeline), ch05 +0 (deepened case-study tie-in instead), ch06 +2 (ABAC, PBAC —
    previously diagram-less), ch07 +2 (trust-ingredients, JIT-provisioning flow), ch08 +2 (token-vs-
    session, claims anatomy), ch09 +2 (AiTM phishing proxy, token-theft replay window).
  - **Real diagram bug found and fixed** (ch07, Figure 7.1): the "OIDC calls this: RP" box used a
    leftover `transform="translate(-60,0)"` compensation hack that made it visually overlap the
    neighboring "SAML calls this: SP" box by 40px. Fixed by repositioning the box directly instead of
    patching it with a transform, confirmed no connector lines cross the wrong box afterward. All other
    diagrams across all 8 chapters were geometry-checked (line-through-box, text-overflow) and found
    already clean — several borderline cases (5-12% over a width estimate) were deliberately left
    unchanged, below the ~15% actionable threshold.
  - One chapter (ch07) judged that its "brokering vs. federation" comparison table shouldn't get a
    `table-compare` modifier class, since neither pattern is objectively the "good" or "bad" one —
    flagged as a case the project may want a neutral "X vs Y" table variant for later, not acted on now.
    No other chapter in this round had a two-column pro-con/problem-solution table to apply the class to.
  - This completes the redesign rollout for **Part I** (chapters 02-09, plus chapter 01 built earlier as
    the reference). Part II (chapters 10-12) is next per the Queue.

### Redesign rollout — Part II converted (chapters 10-12)
- 2026-11-10 chapters 10-12 (3 chapters) converted from the old book format to the chapter-1 slide-deck
  template — 3 parallel subagents (one per chapter, no shared-file writes), then independently
  re-verified by the coordinator:
  - Structural: all 3 files re-parsed cleanly (Python `html.parser`, zero errors), balanced tags
    confirmed, all required mount points present exactly once, old-template ids absent. Re-served
    locally and `curl`-confirmed `200` for all 3 chapter pages. No shared data file was touched by any
    conversion agent (confirmed via `git status --short` showing only the 3 chapter files modified).
  - Final slide counts: ch10 14, ch11 14, ch12 17.
  - Self-check: all 17 existing self-check questions across the 3 chapters got a real, chapter-grounded
    model answer (none left unanswered).
  - New diagrams added: ch10 +1 (compensating-controls/four-eyes flow), ch11 +1 (overlapping-obligations
    map tying all three case-study orgs to their applicable frameworks), ch12 +0 (judged its existing 3
    diagrams + per-concept prose already met the bar, so deepened nothing further).
  - **Real diagram bugs found and fixed, 3 total**: ch10 Figure 10.2 had a connector line whose last 80px
    visually cut into the "Jul review" box instead of stopping at its edge — shortened to clear it. Ch12
    Figure 12.2 (vendor scatter plot) had two real bugs the earlier sitewide audit had missed: an "Okta
    CIC" box sat entirely underneath a later-drawn opaque "Governance & privileged-access tooling" box
    (fully hidden), and the vertical axis line ran straight through that same governance box's interior —
    both fixed by repositioning the Okta CIC box and shortening the axis line. Ch12's agent also
    correctly re-confirmed the one genuine false positive from the earlier audit (a scatter-plot
    reference line deliberately bisecting the Keycloak box) and left it alone, adding a caption note
    making the intent explicit instead.
  - **Table-compare classes**: none applied in this round — every table across all 3 chapters (GDPR/
    HIPAA/PCI-DSS/ISO reference tables, the multi-vendor feature table, the provisioning-vs-governance
    table) is a neutral multi-column reference table, not a two-sided good/bad comparison, so applying
    the class would have been incorrect. All 3 agents independently reached and correctly justified this
    same conclusion.
  - This completes the redesign rollout for **Part II** (chapters 10-12). Part III (chapters 13-18) is
    next per the Queue.

### Redesign rollout — Part III converted (chapters 13-18), plus Part IV chapters 19-24
- 2026-11-10 chapters 13-24 (12 chapters: all of Part III — Protocols, plus the first 6 of Part IV —
  Keycloak Core Administration) converted from the old book format to the chapter-1 slide-deck template
  in one combined 12-way parallel round (one subagent per chapter, no shared-file writes), then
  independently re-verified by the coordinator:
  - Structural: all 12 files re-parsed cleanly (Python `html.parser`, zero errors), `<section
    class="slide">`/`<svg>`/`<details>`/`<summary>` tag counts balanced per file, all 10 required
    mount-point ids present exactly once each, `CURRENT_CHAPTER`/`chapterSlug` confirmed matching on
    every page. Re-served locally (`python3 -m http.server`) and `curl`-confirmed `200` for all 12
    chapter pages. `git status --short` confirmed only the 12 chapter files were modified — no shared
    data file (glossary-data.js/quiz-data.js/nav-data.js) or sibling chapter touched.
  - Final slide counts: ch13 17, ch14 15, ch15 13, ch16 13, ch17 13, ch18 12, ch19 13, ch20 16, ch21 17,
    ch22 13, ch23 16, ch24 16.
  - Self-check: every existing self-check question across all 12 chapters (71 total) got a real,
    chapter-grounded model answer; none left unanswered.
  - New diagrams/examples added: ch13 +1 (Figure 13.4, exact-match vs. loose-match redirect_uri
    validation, illustrating the chapter's own live finding on the Duende demo server); ch20/21/22/23
    added case-study callouts/takeaways extending Northwind's pattern to Riverside Health/Meridian Trust
    where the chapter had only touched one or two of the three orgs; ch19 added a Meridian Trust
    worked example (Oracle vs. Postgres DB-backend choice); ch21 added a reasoning "Try it" exercise
    contrasting Meridian's likely session-timeout posture against Northwind's. Chapters 14-18 and 24
    judged their existing diagram/example density already sufficient and added none.
  - **Real diagram/accessibility bugs found and fixed, 4 total**: ch17 Figure 17.2 had a floating label
    overflowing its viewBox by ~19% (split into two lines); ch22 Figure 22.3's trailing caption was one
    `<text>` element with a literal newline that SVG collapsed into a single ~206-char line overflowing
    the viewBox by ~50% (split into two real `<text>` elements); ch23 had three `fill="#fff"` hardcoded
    white text colors across two diagrams that would have rendered wrong/invisible in dark or print
    themes — replaced with the `diagram-label-on-accent` token class. No connector-through-box bugs were
    found in this round (all 12 agents traced every connector's coordinates against every box's bounds
    and confirmed clean); several borderline text-overflow cases in the 0.7%-19% range were evaluated
    case by case, with the one genuine >15% case (ch17) fixed and all others left alone per the
    threshold rule.
  - **Table-compare classes**: applied once this round — ch20's `start-dev` vs. production-mode table
    (`table-compare table-compare--problem-solution`, less-secure dev mode listed first). Every other
    table across all 12 chapters (grant-type surveys, JWS-vs-JWE, HS256-vs-RS256, SAML-vs-OIDC,
    bindings, deployment/DB-backend references, realm-settings/token-field references, client-type and
    role/scope reference tables) was judged a neutral multi-column reference or a genuine no-good/bad-
    side comparison, not a pro/con or problem/solution pair — several agents added a one-line `<p
    class="note">`/`<p class="diagram-note">` explaining that judgment call inline in the chapter itself.
  - Part III (chapters 13-18) is now fully converted, completing queue item 10c. Part IV (chapters
    19-26) is 6/8 converted (19-24 done); **chapters 25-26, then Part V (27-30), are next** per the
    Queue.

### Redesign rollout — Part IV finished (chapters 25-26) and Part V converted (chapters 27-30)
- 2026-11-10 chapters 25-30 (6 chapters: Part IV's last 2 — Authentication flows, Themes & branding —
  plus all of Part V — Keycloak Operations) converted from the old book format to the chapter-1
  slide-deck template in one 6-way parallel round (one subagent per chapter, no shared-file writes),
  then independently re-verified by the coordinator:
  - Structural: all 6 files re-parsed cleanly (Python `html.parser`, zero errors), `<section
    class="slide">`/`<svg>`/`<details>`/`<summary>` tag counts balanced per file, all 10 required
    mount-point ids present exactly once each, `CURRENT_CHAPTER`/`chapterSlug` confirmed matching on
    every page. Re-served locally and `curl`-confirmed `200` for all 6 chapter pages. `git status
    --short` confirmed only the 6 chapter files were modified; separately confirmed via `git diff
    --stat` that the shared `labs/docker/docker-compose.yml` (flagged off-limits in ch28's brief) was
    left untouched.
  - Final slide counts: ch25 15, ch26 13, ch27 20, ch28 18, ch29 15, ch30 15.
  - Self-check: every existing self-check question across all 6 chapters (35 total) got a real,
    chapter-grounded model answer; none left unanswered.
  - Lab-step counter-reset pattern (first used in ch24): four chapters split a numbered `.lab-steps`
    list across slide boundaries this round (ch25, ch27, ch29, ch30) and each correctly added
    `style="counter-reset: lab-step N"` on the continuation `<ol>` so visible step numbers stayed
    sequential instead of silently resetting to 1 — all four independently verified the pattern against
    ch24 before applying it rather than reinventing it.
  - New diagrams/examples added: ch25 +1 (Figure 25.5, a 3-stage flow-execution/required-actions/
    session-issued timeline, visualizing a timing distinction the chapter's prose already called out
    but hadn't diagrammed); ch26 extended its case-study paragraph to cover Riverside Health and
    Meridian Trust (previously only Northwind). Chapters 27, 28, 29, 30 added none, each judging their
    existing diagram/case-study density already sufficient — all four are real-lab chapters where every
    agent explicitly weighed "don't risk inventing content that might be inaccurate" against padding.
  - **Real diagram bugs found and fixed, 2 total**: ch29 Figure 29.2 (SSO sequence diagram) had a label
    ~29% over its lane-gap width, visually overlapping both lifelines — split into two lines. Ch30
    Figure 30.3 (OTP timeline) had a label ~21% over its box width — split into two stacked lines. No
    connector-through-box bugs were found in this round; several borderline text-overflow cases in the
    1%-14% range were evaluated and correctly left alone per the threshold rule. No hardcoded
    `fill="#fff"`/`fill="white"` text colors were found in any of the 6 chapters (all already used the
    theme-token classes correctly going in).
  - **Table-compare classes**: none applied this round — every table across all 6 chapters (built-in
    flow/requirement-level references, theme-settings/theme.properties references, REST-vs-CLI/export-
    mechanism references, event/health/metrics references, session-object/REST-endpoint/logout-mechanism
    references, password-policy/brute-force/OTP-field references) was judged a neutral multi-column
    reference table, not a pro/con or problem/solution pair, with each agent stating its reasoning.
  - **This completes the full slide-deck/roadmap redesign rollout — all 29 pre-existing chapters
    (02-30) now match chapter 1's template**, closing queue items 10a-10e in full. Part VI's new content
    (chapters 31-36, plus the `labs/spring-boot-resource-server/`/`labs/react-spa/` scaffolding) is next
    per the Queue.

### Part VI, Round 1 — chapters 31-33 (identity brokering, LDAP federation, custom User Storage SPI)
- 2026-11-11: first brand-new-content round of Part VI (not a conversion — `built:false` → `built:true`,
  no prior files existed). Split into two rounds by infrastructure dependency, mirroring Part IV's
  "establish-then-scale" shape: Round 1 = chapters 31-33 (mutually independent, each owning its own new
  `labs/` subfolder), run as 3 parallel subagents, each pre-assigned an isolated Docker Compose project
  name/port (`ch31kc`→18131, `ch32kc`→18132/LDAP 18389, `ch33kc`→18133) up front. Round 2 (chapters
  34-36, needing new `labs/spring-boot-resource-server/`/`labs/react-spa/` scaffolding) is next.
  - **Ch 31 — Identity brokering**: 19 slides, 6 self-check (all answered), 4 diagrams (no genuine bugs;
    one stray `fill:inherit` cosmetic cleanup). Real lab: brokered `northwind-apps` to a second realm
    (`northwind-corporate-idp`) acting as a real external OIDC IdP, full headless curl+cookie-jar round
    trip (authorize → external login → broker callback → First Broker Login → final tokens), confirmed
    via the Admin REST API that the resulting user was JIT-provisioned with a real `federated-identity`
    link, never manually created. **Real bug found while building the chapter itself** (not staged):
    splitting the isolated test stack's browser-facing vs. backend-facing hostnames across two different
    ports produced a genuine `Wrong issuer from token` error — a real Keycloak-behind-a-proxy gotcha,
    written up honestly as a "common mistakes" pitfall rather than smoothed over, with the fix (one
    consistent hostname throughout) documented. Deep-dive links verified live.
  - **Ch 32 — User federation: LDAP & Active Directory**: 16 slides, 5 self-check (all answered), 3
    diagrams (2 genuine text-overflow bugs found and fixed pre-ship). New infra: `labs/ldap-federation/`
    (own `docker-compose.yml` — Postgres + Keycloak 26.8.2 + `osixia/openldap:1.5.0`, live-verified as
    the newest actual final-release tag since the project's v2 line is still prerelease-only — + a
    Meridian Trust staff-directory seed LDIF). Real lab, run end to end: seeded LDAP, federated Keycloak
    against it, full-synced, and **found a real attribute-mapping bug** (the built-in first-name mapper
    read `cn`, which this directory uses for the full display name, producing `firstName: "Julia
    Martinez"` instead of `"Julia"`) — diagnosed via the REST-exposed mapper config, fixed by repointing
    the mapper at `givenName`, re-synced, re-confirmed. Added a custom attribute mapper and a group
    mapper (neither exists by default); proved live LDAP-bind authentication via direct grant (correct
    password succeeds, wrong password rejected) and exercised `testLDAPConnection`'s connection-vs-
    authentication split for real. **Three genuine Docker/LDIF seeding gotchas** hit and documented
    (bind-mounting a single file breaks the image's in-place `sed`; the image's default bootstrap path
    *moves*, not copies, a loaded LDIF, silently deleting the host's seed file on the next clean rebuild;
    a stray occurrence of the word "changetype" in a comment misled the bootstrap script's plain-text
    add-vs-modify detection) — fixed via the image's `LDAP_SEED_INTERNAL_LDIF_PATH` mechanism, confirmed
    reset-safe across a full `down -v`/`up -d` cycle.
  - **Ch 33 — Custom User Storage SPI**: 19 slides, 6 self-check (all answered), 4 diagrams (no genuine
    bugs). New infra: `labs/custom-user-storage-spi/` — a real Maven project (a read-only
    `UserStorageProvider`/`Factory` pair bridging a toy CSV "legacy HR" store for the Riverside Health
    case study). **Live-verified exact Keycloak 26.8.2 SPI Maven coordinates** two independent ways
    (Maven Central metadata and inspecting the real jars inside the running server image) rather than
    guessing — confirmed the 26.4+ module split the brief flagged as a risk is real: `UserStorageProvider`
    now lives in `keycloak-model-storage`, not `keycloak-server-spi`. **Four genuine, kept-not-edited-out
    build/deploy bugs**: (1) an XML comment containing `--` broke the POM parse (hit twice); (2) a real
    compile error — `AbstractUserAdapter` leaves `UserModel.credentialManager()` abstract despite
    "implementing everything but `getUsername()`", fixed by bridging to Keycloak core's own
    `UserCredentialManager`; (3) a Docker bind-mount race — bringing the stack up before the JAR existed
    on disk made Docker silently create an empty directory at that path instead of mounting a file; (4)
    registering the federation component with the realm's human-readable name as `parentId` instead of
    its real UUID silently orphaned it (`user_not_found` despite HTTP 201) — root-caused via `kcadm.sh
    get realms/{realm} --fields id,realm` and fixed. Also re-hit and fixed, in a federated-provider
    context this time, the same Declarative-User-Profile `unmanagedAttributePolicy` gap chapters 22/24
    found. Real successful/failed direct-grant auths and a full container-log error sweep (clean) both
    confirmed.
  - **Coordinator verification**: all 3 files re-parsed cleanly (zero `html.parser` errors), tag balance
    confirmed, all mount-point ids present exactly once, `CURRENT_CHAPTER`/`chapterSlug`/`renderQuiz`
    slugs all matching. Confirmed via `git diff --stat` that `glossary-data.js`/`quiz-data.js`/`nav-
    data.js`/`labs/docker/docker-compose.yml` were untouched by any of the 3 agents (central-merge-only
    discipline, since these are brand-new chapters needing new shared-file content for the first time
    since the redesign rollout began). Re-served the whole site locally and `curl`-confirmed `200` for
    `index.html`/`roadmap.html`/`glossary.html`/`quizzes.html` and all 3 new chapter pages. Confirmed
    both new `labs/` folders' build artifacts (`target/`) are `.gitignore`d, not tracked.
  - **Central glossary/quiz merge** (done by the coordinator, not the agents, to avoid 3-way write
    conflicts on shared files): extended 23 existing terms' `chapters` arrays (5 for ch31 — e.g.
    `Identity Provider (IdP)`, `JIT provisioning`, `Social login`; 15 for ch32 — directory-services terms
    from ch04, plus `Direct access grants`; 3 for ch33 — `Declarative User Profile`/`unmanagedAttribute-
    Policy`/`Unmanaged attribute`) rather than duplicating them, exactly as each agent flagged. Appended
    47 new glossary terms (15 ch31 + 17 ch32 + 15 ch33) — glossary now **523 terms, zero duplicate
    names** (checked programmatically via Node). Appended 3 new quiz pools, 16 questions each (48 total,
    ids prefixed `idb-`/`ldp-`/`spi-`) — quiz pools now **34 pools, 573 questions, zero duplicate ids,
    zero malformed option arrays/out-of-range answer indices** (checked programmatically via Node).
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 31-33 only after all the above passed.
  - **Part VI Round 1 is now complete** (chapters 31-33). **Round 2 (chapters 34-36 + new lab
    scaffolding) is next** per the Queue.

### Part VI, Round 2 — chapters 34-36 (securing a backend API, securing a frontend SPA, M2M auth)
- 2026-11-11: second and final round of Part VI, completing the full Part. 3 parallel subagents, each
  pre-assigned an isolated Docker Compose project name/port (`ch34kc`→18134, `ch35kc`→18135,
  `ch36kc`→18136), same discipline as Round 1.
  - **Ch 34 — Securing a Backend API**: 18 slides, 5 self-check (all answered), 3 diagrams (clean). New
    infra: `labs/spring-boot-resource-server/` — a real Spring Boot app (**live-verified Spring Boot
    4.1.2**, the newest non-release-candidate version on Maven Central at build time, explicitly not the
    newer-looking `4.2.0-RC2`) as an OAuth2 resource server for a Northwind Apps billing API. Real lab,
    full endpoint matrix (401/403/200) all genuinely curled. **Two real, kept-not-staged bugs**: (1) the
    same XML-comment-`--` POM parse error chapter 33 hit independently, caught and fixed again; (2) a
    real "Account is not fully set up" direct-grant failure traced to Keycloak's Declarative User
    Profile requiring `firstName`/`lastName`, computed dynamically as `VERIFY_PROFILE` rather than
    stored in `requiredActions` — cross-checked against independent 2026 community reports of the same
    message on a nearby Keycloak version. The chapter's central teaching gotcha (Spring Security's
    default `JwtGrantedAuthoritiesConverter` not reading Keycloak's `realm_access.roles`) was built in
    deliberately, then *deliberately reverted and replayed* against an already-proven-valid token to
    capture a genuine, non-fabricated 403→200 transcript rather than only describing the bug in prose.
    Also hit and fixed a real Compose port-collision from list-merge semantics (`ports: !override [...]`
    needed, not Compose's default concatenating merge).
  - **Ch 35 — Securing a Frontend SPA**: 22 slides, 6 self-check (all answered), 3 diagrams (clean). New
    infra: `labs/react-spa/`. **Live-verified a real environment blocker before building anything**:
    current Vite requires Node 20.19+/22.12+, but this machine runs Node 18.20.4 — checked each
    candidate Vite major's actual `engines` field rather than guessing, pinned **Vite 6.4.6** (the newest
    Node-18-compatible major) plus a compatible `@vitejs/plugin-react@4.7.0`, documented the reasoning
    honestly in the chapter (same pattern ch32 used for its OpenLDAP image pin). Real app built and run
    (`react-oidc-context@3.3.1` + `oidc-client-ts@3.5.0`) for a Riverside Health patient portal. No real
    browser tool exists in this environment — handled the same way ch13 handled an equivalent gap:
    verified the compiled bundle's config directly, and proved the full protocol round trip for real via
    a curl+cookie-jar PKCE exchange (code_verifier cross-checked via both `openssl` and Python
    `hashlib`) against the exact same Keycloak client. **Found a genuine, honestly-reported contrast**:
    this real Keycloak instance returns distinct, diagnostic `error_description` values per PKCE failure
    cause, unlike chapter 13's Duende demo server (identical generic error for every cause) — and a
    second contrast, that a bad `redirect_uri` here gets an immediate 400 with no redirect at all, where
    chapter 13's demo server had redirected anyway.
  - **Ch 36 — Service-to-Service & Machine-to-Machine Auth**: 17 slides, 6 self-check (all answered), 3
    diagrams (one borderline text-width fix applied pre-ship). No new `labs/` folder — reused
    `labs/docker/docker-compose.yml` unmodified, like ch31, using Keycloak's own Admin REST API as the
    real protected "downstream service" for a Meridian Trust fraud-detection case study. **Real,
    unplanned finding**: a service account with a correctly-confirmed `view-users` role mapping still
    got a flat 401 (not 403) on the Admin REST API, because the Admin REST API runs two independent
    checks — an earlier scope-gated authentication check (the `kc-admin-api` client scope, missing by
    default on a custom client, present by default on `admin-cli`) and a later role-gated authorization
    check — and a correct role mapping doesn't help if the token never clears the first gate. Added the
    scope, got a real 200; proved the real 403 scope boundary on an out-of-scope write; added a second,
    no-role-mappings client to prove a 403 (not 401) from that distinct failure mode too. Correctly
    recognized mid-build that the `kc-admin-api` mechanism was already named by chapter 27 and
    `admin-api-and-cli`'s own glossary, and rewrote the chapter's framing to recall that rather than
    present it as a fresh discovery — the chapter's genuine new contribution is the 401-vs-403
    distinction for a *custom* client that doesn't get the scope for free.
  - **Coordinator verification**: all 3 files re-parsed cleanly, tag balance confirmed, all mount-point
    ids present exactly once, `CURRENT_CHAPTER`/`chapterSlug`/`renderQuiz` slugs matching (fixed one
    mismatch: ch34's `renderQuiz` call said 15 but 18 questions were proposed — corrected to 18 during
    merge). Confirmed via `git diff --stat` that `glossary-data.js`/`quiz-data.js`/`nav-data.js`/`labs/
    docker/docker-compose.yml` were untouched by any of the 3 agents. Confirmed both new lab folders'
    build artifacts (`target/`, `node_modules/`) are `.gitignore`d, not tracked. Re-served the whole site
    and `curl`-confirmed `200` for the shell pages and all 3 new chapters.
  - **Central glossary/quiz merge**: extended 34 existing terms' `chapters` arrays — 4 of them
    (`Service account`, `Client credentials grant`, `Scope (OAuth)`, `Confidential client (Keycloak)`)
    were independently requested by two different agents each and merged to include both new slugs in
    one pass rather than overwriting. Declined one flagged extend-candidate (`Blast radius (token
    theft)`, proposed by ch36) as a coordinator judgment call — the existing term's definition is
    token-theft-specific, and ch36's reasoning, while analogous, was actually about a leaked client
    secret, a distinct scenario; left unextended rather than stretching the term's scope. Appended 40 new
    glossary terms (17 ch34 + 15 ch35 + 8 ch36 — ch34 self-reported 18 but the actual itemized list
    contained 17; harmless subagent miscount, not chased further) — glossary now **563 terms, zero
    duplicate names**. Appended 3 new quiz pools (18 ch34 + 16 ch35 + 16 ch36 = 50 questions) — quiz
    pools now **37 pools, 623 questions, zero duplicate ids, zero malformed entries**. Re-typed 3 of
    ch34's own questions (`bak-03`/`bak-07`/`bak-12`) from `bug` to `scenario` per the agent's own
    flagged recommendation, bringing its scenario share to exactly 50% (9/18) to meet the template's
    floor — content unchanged, only the `type` tag.
  - `nav-data.js`'s `built` flag flipped to `true` for chapters 34-36 only after all the above passed.
  - **Part VI is now fully complete** (chapters 31-36, all 6 chapters). **Part VII (chapters 37-38) is
    next** per the Queue — GATE-5 sits right after it.

## Verify items
- The visual/rendered check noted above under GATE-1 ("What's NOT yet verified") — no browser tool is
  available in this environment, so nobody has actually looked at the rendered pages yet. Recommend the
  user do this before approving GATE-1.
- Chapter 02's three NIST SP 800-63-3/-A/-B links and the CSRC glossary link were live-verified by the
  coordinator (200 OK each) — no outstanding concern, listed here only for the record.
- **The login-attempt permission-classifier discrepancy from GATE-3** (see summary above): one subagent
  (ch15) was blocked by this environment's tool-permission classifier when attempting to script a
  device-flow login POST; another (ch13) reported doing the equivalent successfully for the
  authorization-code flow. Not resolved — worth re-checking if a future chapter's lab depends on
  scripted login working, rather than assuming either outcome.

## Deviations from CLAUDE.md
- None in content/structure. One process deviation, intentional: CLAUDE.md §7 describes a single-queue
  session routine: in practice, the 3 reference-set chapters were built by 3 parallel subagents (at the
  user's explicit request to "use multiple agents to speed up the work") rather than sequentially one at
  a time, with glossary/quiz merging done centrally afterward specifically to avoid shared-file write
  conflicts. This worked cleanly and is a reasonable model for future rounds once GATE-1 is approved —
  see "Open questions" item 4 below, which anticipated this.
- One process deviation discovered during Part IV's chapters 21-26 round, also intentional/unavoidable
  given parallel subagents: `labs/docker/docker-compose.yml`'s default Compose project name and host
  port are shared by every agent running `docker compose up -d` from that directory with no override.
  5 of the 6 parallel agents this round hit a collision (sibling stacks silently recreated/torn down
  mid-session) and each independently isolated their own test under a custom `-p <slug>kc` project name
  and alternate port, while keeping the chapter's reader-facing documented commands as the plain form.
  No chapter content was compromised, but recommend standardizing this (assign each parallel lab-bearing
  chapter its own project name/port up front in the brief) for Part V's parallel rounds.
- **Major design change (2026-11-10), approved** — see CLAUDE.md §4 for the now-official template.
  At the user's explicit request, the site moved away from the old "book" page model (one long
  scrollable chapter page, a global 50-chapter sidebar on every page) toward two new things:
  1. **`index.html` is now a roadmap.sh-style visual roadmap** (vertical path, grouped by Part, one
     chapter per node, clickable once built, showing a progress ring/checkmark once slides are marked
     read) instead of the old shell/stat-tiles/parts-list landing page. New files:
     `assets/css/roadmap.css`, `assets/js/roadmap-view.js`.
  2. **Chapter pages are now slide decks, not single-scroll books**: each chapter's existing content is
     unchanged but re-chunked into `<section class="slide" data-slide="id" data-title="...">` blocks,
     one shown at a time, with a left outline panel (this chapter's slides only, not the global 50-
     chapter list — that panel also carries a "← Back to roadmap" link, a "Mark whole chapter as read"
     toggle button — `#mark-all-btn`, added 2026-11-10 after the user asked for a one-click way to mark
     every slide in a chapter instead of each individually — and the per-slide outline list), a bottom
     pager (Previous / Mark as read / Next), and a "Slide N of M" position label. The pager's Previous/
     Next buttons bridge to the adjacent chapter when at the first/last slide, subsuming the old
     `#chapter-nav-footer` mechanism. New files: `assets/css/slides.css`, `assets/js/slides.js`. Progress
     persists to `localStorage["iam:read:<slug>"] = {total, done: {slideId: true, ...}}`, which
     `roadmap-view.js` reads (without loading any chapter page) to compute per-chapter and overall
     progress on the roadmap. Pattern adapted (clean-room, not copied) from a `localStorage`-backed "mark
     as learned"/per-lesson-pager pattern the user pointed at as a reference. **Every future chapter
     conversion must include the `#mark-all-btn` markup** inside `#deck-outline-panel` (between the
     back-to-roadmap link and `#deck-progress-mini`/`#slide-outline`) — `slides.js` already no-ops
     safely if it's missing, but the feature should be present on every chapter, not just chapter 1.
  - **Bug fixed 2026-11-10**: marking every slide in a chapter as read did not update the roadmap's
    progress ring until a hard refresh. Root cause: navigating back to `index.html` via the browser's
    Back button (or the sidebar's "← Back to roadmap" link, depending on the browser's heuristics) can
    restore the page from the **bfcache** — the exact DOM/JS state frozen on the way out, repainted
    without re-firing `DOMContentLoaded` or re-running `roadmap-view.js`, so the freshly-written
    `localStorage` progress was never re-read. Fixed by also listening for the `pageshow` event (which
    fires on every reveal of the page, with `event.persisted === true` specifically on a bfcache
    restore) and re-running the stats/track render in that case.
  - `book.js`/`quiz.js`/`glossary-data.js`/`quiz-data.js`/`nav-data.js` are all **unchanged** — the new
    chapter template still loads them in the same order and still calls `QuizEngine.renderQuiz(...)`;
    `book.js`'s old `buildSidebar()`/`buildChapterFooter()` simply no-op on the new template since it no
    longer has `#sidebar-nav`/`#chapter-nav-footer` mount ids (verified safe via their existing
    null-guards — no edits needed to book.js itself).
  - **Scope decision (the user chose this explicitly over converting everything at once, or leaving
    00-30 as-is)**: build the new template + convert ONE chapter (01 — What Is IAM?) as a live
    reference first, verify it works, then stop and show the user before touching the other 29
    already-built chapters. Verified via Python's `html.parser` (14 unique slides, balanced tags) and
    via a full jsdom simulation of the real page (slide switching, `localStorage` round-trip, outline
    navigation, pager state, the chapter-to-chapter bridge at slide 0/N-1 all confirmed working) — no
    browser tool exists in this environment, so this jsdom simulation is the strongest verification
    available short of the user's own eyes.
  - **Iterated on chapter 1 based on real feedback before rolling out further** (all 2026-11-10, each
    its own commit): roadmap spine-badge color went through several rounds (blue → rejected → shown 8
    blue shades → shown 8 terracotta shades → Clay `#9C5F45` chosen → tried Muted Clay `#8F6049` →
    rolled back to Clay) — kept as `--milestone-bg`/`--milestone-contrast` tokens in `book.css` so this
    stays a one-line change if tried again. Fixed two real diagram bugs the user caught by eye that a
    script alone wouldn't have found without being told what to look for: connector lines crossing
    through boxes (found by building a geometry-check script and running it against all 31 chapters —
    see `/tmp` scratchpad from that session for the script if it's ever needed again, or rewrite it,
    it's short) and text overflowing its box (same script, character-count × font-size estimate).
    Fixed a table-header-contrast bug (`thead th` used `--bg-alt`, body rows had no background at all
    and just showed the page's `--bg` through — nearly identical values once the palette lightened) and
    added `table-compare--pro-con`/`table-compare--problem-solution` modifier classes that render a ✓/✗
    icon per cell. Added click-to-reveal model answers to self-check (native `<details>`/`<summary>`,
    no JS) and a "Mark whole chapter as read" button. Fixed a real bug where marking slides read didn't
    update the roadmap until the page re-ran its own JS — covered with `storage`/`pageshow`/`focus`/
    `visibilitychange` listeners since the failure mode turned out to be a plain tab-switch-back, not
    the bfcache case fixed first.
  - **New standing rule (2026-11-10, user's explicit instruction)**: converting a chapter to the slide
    format is not just mechanical re-chunking — add more diagrams, sharper worked examples, or deeper
    case-study ties wherever they'd make a concept clearer. The existing minimums (3+ diagrams, etc.)
    are a floor, not a target. See CLAUDE.md §4's own copy of this rule.
  - **Now rolling out to chapters 02-30**, Part by Part, via the usual parallel-subagent-then-merge
    pattern — see "Current unit" above for live status and the Queue below for the Part-by-Part plan.

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
4. **Working mode**: confirmed through three rounds now (GATE-1's 3 chapters, Part I's 7, Part II's 3) —
   parallel subagents per chapter/page + central merge, with the coordinator independently re-verifying
   (structural checks, data-integrity checks, and spot-checking at least one surprising live-research
   claim per round rather than trusting subagent citations blindly). No further confirmation needed;
   this is the standing default.
5. **Part III lab target: resolved 2026-11-10** ("use a public test IdP, continue with chapters 13-18").
   Locked in as Duende Software's public demo IdentityServer (`https://demo.duendesoftware.com`) — see
   CLAUDE.md §5's new "Part III lab target" subsection for the full decision record, the confirmed-live
   client list (`m2m`, `device`, `interactive.public`/`interactive.confidential`), and the known open
   gap (no publicly documented browser-completable redirect_uri for the interactive clients, worked
   around by making the device-flow lab the course's primary "real tokens, real human step" exercise).
