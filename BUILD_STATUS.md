# Build status

Last updated: 2026-11-10 | Docker: 29.6.2 | Java: openjdk 21.0.13.0.3 | Node: v18.20.4 | Python: 3.12.3 |
Git: yes, pushed to a GitHub remote (branch `main`). Deploy target: Vercel (not yet connected — do that
once `index.html` exists, see Open questions). Commits happen per unit; pushes only when the user
explicitly says to (standing instruction from the user — do not push without being asked).

## Current unit

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

**Redesign approved 2026-11-10 ("looks good") — now rolling out to chapters 02-30, ahead of Part VI.**
The slide-deck + roadmap redesign (see the Done-section design note below for full detail: slide
chunking, `#mark-all-btn`, click-to-reveal self-check answers, `table-compare` icons, the diagram
bug classes to check for, and the "prefer more visuals/examples" standing rule) is no longer a pending
proposal — CLAUDE.md §4 has been rewritten to describe it as the official template. Converting the 29
already-built chapters (02-30) to match chapter 1 is now the active unit, worked through Part by Part
via the usual parallel-subagent-then-merge pattern, before Part VI's new content resumes. Each
conversion agent also re-verifies/fixes diagram geometry and adds a model answer to every self-check
question while it's in there, per the new template. **Progress**: Part I (chapters 02-09) in progress.

Standing environmental note (first hit in ch09, confirmed again independently in ch10 and ch11):
`hhs.gov` and some other government/vendor domains return HTTP 403 to automated fetches in this
sandboxed environment (looks like bot-blocking, not a dead link). Subagents have been substituting
verified alternative sources (NIST, Cornell LII, etc.) rather than shipping unverified links — continue
that pattern rather than re-discovering the same block each time.

Detailed pre-build briefs for finished chapters are kept out of this section once built — the "Done"
entries below describe what was actually built (counts, verification, merge decisions, live-research
findings), which is the more useful record going forward; original brief text is in this file's git
history if ever needed, per CLAUDE.md §7's routine of keeping "current" lean.

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
10a. Part I (chapters 02-09, 8 chapters) — **in progress**.
10b. Part II (chapters 10-12, 3 chapters).
10c. Part III (chapters 13-18, 6 chapters).
10d. Part IV (chapters 19-26, 8 chapters).
10e. Part V (chapters 27-30, 4 chapters).

### Part VI (federation & integration) — queued after the redesign rollout
11. `labs/spring-boot-resource-server/`, `labs/react-spa/` scaffolding (built alongside chapters 34-35,
    not before — no point scaffolding a lab before its chapter is drafted).
12. Chapters 31-36 — built as slide decks from the start, no conversion needed.

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
- **GATE-2: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- **GATE-3: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- GATE-4: not reached — next up, after Part IV (chapters 19-26) + the `labs/docker/` base environment
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
