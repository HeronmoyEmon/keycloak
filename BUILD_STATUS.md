# Build status

Last updated: 2026-11-10 | Docker: 29.6.2 | Java: openjdk 21.0.13.0.3 | Node: v18.20.4 | Python: 3.12.3 |
Git: yes, pushed to a GitHub remote (branch `main`). Deploy target: Vercel (not yet connected — do that
once `index.html` exists, see Open questions). Commits happen per unit; pushes only when the user
explicitly says to.

## Current unit

None in progress. Next up per the queue: **Chapter 00 — How to use this course**.

## Queue

Ordered; one item at a time. Each chapter's full brief gets drafted into this file (replacing its queue
line) when it becomes "Current unit" — see `CLAUDE.md` §6-7. Lab-bearing chapters are marked.

### Reference set (builds the template — stop at **GATE-1** after item 3)
1. `assets/css/book.css` + `assets/css/print.css` + `assets/js/book.js`: the shell design system (light/
   dark/print themes, sidebar nav, diagram/callout/table styles, the "In Keycloak" callout component).
   Clean-room build, not copied from `sde-prep` (CLAUDE.md §2.6).
2. Chapter 00 — How to use this course (brief: CLAUDE.md §6).
3. Chapters 01-02 — What is IAM? / Identity fundamentals (briefs: CLAUDE.md §6). Chapter 02 introduces
   Northwind Apps / Riverside Health / Meridian Trust.
   **→ GATE-1: stop here for review.**

### Part I completion (requires GATE-1 approved)
4. Chapters 03-09 (briefs: draft each from CLAUDE.md §4 template + §5 one-liners when reached).
5. `index.html`, `roadmap.html`, `glossary.html`, `quizzes.html`, `nav-data.js`/`roadmap-data.js`
   populated for all 50 chapters (even unbuilt ones, marked `built: false` per the `sde-prep` index
   convention of disabling not-yet-built links instead of dead-linking them).
   **→ GATE-2: stop here for review.**

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

- GATE-1: not reached
- GATE-2: not reached
- GATE-3: not reached
- GATE-4: not reached
- GATE-5: not reached
- Final QA gate: not reached

## Done

(nothing yet)

## Verify items

(nothing yet)

## Deviations from CLAUDE.md

(none yet)

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
4. **Working mode**: this file assumes sequential single-queue work (one unit at a time, no parallel
   subagent fan-out) since this is a single volume, unlike `sde-prep`'s three-volume parallel rounds. If
   the user wants faster throughput later (e.g. one subagent per Part once the template is proven at
   GATE-1), revisit this.
