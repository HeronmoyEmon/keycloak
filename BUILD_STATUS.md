# Build status

Last updated: 2026-11-10 | Docker: 29.6.2 | Java: openjdk 21.0.13.0.3 | Node: v18.20.4 | Python: 3.12.3 |
Git: yes, pushed to a GitHub remote (branch `main`). Deploy target: Vercel (not yet connected — do that
once `index.html` exists, see Open questions). Commits happen per unit; pushes only when the user
explicitly says to (standing instruction from the user — do not push without being asked).

## Current unit

**GATE-1 approved 2026-11-10** ("approve GATE-1, continue with chapters 03-09"). **GATE-2 approved
2026-11-10** ("approve GATE-2, continue with chapters 10-12"). Both approved as built, no feedback.
Confirms the parallel-subagent-then-central-merge working mode as the default (Open questions item 4).

**Part II complete 2026-11-10: chapters 10-12** (Identity Governance & Administration, Compliance for
IAM, the IAM Market Landscape) — 3 chapters, 3 parallel subagents, centrally merged and independently
re-verified (structural checks, data-integrity checks; see Done below for full detail, including the
coordinator's own independent verification of chapter 12's most surprising live-research finding — the
Palo Alto Networks/CyberArk/"Idira" rebrand — via a direct SEC-filing + multi-source web search, not just
trusting the subagent's report).

This closes out Part II. **There is no gate between Part II and Part III** in this project's own queue
(CLAUDE.md's gates sit at GATE-2 after Part I, GATE-3 after Part III) — so unlike every round so far,
nothing is blocking the next unit on user approval. Part III (chapters 13-18, protocols) is bigger (6
chapters) and has real open design decisions the user hasn't weighed in on yet (what ch13-15's curl-only
labs run against — a public test IdP or a throwaway Keycloak instance; see queue item 7's note) — flagged
to the user rather than decided unilaterally before starting that round.

Standing environmental note (first hit in ch09, confirmed again independently in ch10 and ch11): `hhs.gov`
and some other government/vendor domains return HTTP 403 to automated fetches in this sandboxed
environment (looks like bot-blocking, not a dead link). Subagents have been substituting verified
alternative sources (NIST, Cornell LII, etc.) rather than shipping unverified links — continue that
pattern rather than re-discovering the same block each time.

The detailed pre-build briefs for chapters 03-12 have been dropped from this section now that those
chapters are built — the "Done" entries below describe what was actually built (counts, verification,
merge decisions), which is the more useful record going forward; the original brief text is still in
this file's git history if ever needed, per CLAUDE.md §7's routine of keeping "current" lean.

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

### Part III (protocols) — up next
7. Chapters 13-18 (13-15 are curl-only labs against public test IdPs or a throwaway Keycloak instance —
   **decision needed**: which one, before drafting chapter 13 — see Open questions).
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
- **GATE-2: approved 2026-11-10**, no feedback given (approved as built). See summary below.
- GATE-3: not reached — next up, after Part III (chapters 13-18)
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
4. **Working mode**: confirmed through three rounds now (GATE-1's 3 chapters, Part I's 7, Part II's 3) —
   parallel subagents per chapter/page + central merge, with the coordinator independently re-verifying
   (structural checks, data-integrity checks, and spot-checking at least one surprising live-research
   claim per round rather than trusting subagent citations blindly). No further confirmation needed;
   this is the standing default.
5. **Part III lab target (new, blocking chapter 13)**: CLAUDE.md §5/BUILD_STATUS queue item 7 flags that
   chapters 13-15 (OAuth 2.0 core/extended, OpenID Connect) need curl-only hands-on labs, but leaves open
   *what* those labs run against — a public OAuth/OIDC test IdP (e.g. a well-known sandbox IdP) or a
   throwaway local Keycloak instance stood up early, ahead of Part IV's planned chapter 20 install. Each
   has a real trade-off: a public test IdP needs no local setup but the reader doesn't control its
   configuration and it could change/disappear; a throwaway local Keycloak instance is fully controlled
   but means chapter 13 quietly breaks the "Keycloak doesn't show up until chapter 20" rule this course
   has stated explicitly three times already (ch00, ch08, elsewhere) as a deliberate pedagogical choice.
   Recommend: a public, stable, well-known OAuth test IdP for ch13/14 (keeps the "no Keycloak yet" rule
   intact) and reconsider for ch15 (OIDC) once ch13/14's choice is proven out — but this is a real
   decision point, not something to default past. **Needs the user's input before chapter 13 is drafted.**
