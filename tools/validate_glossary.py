#!/usr/bin/env python3
"""Validator for assets/js/glossary-data.js.

Loads window.GLOSSARY via Node (cross-checked against window.COURSE_NAV from nav-data.js) and
checks: no duplicate term names, no term with an empty `chapters` array (orphaned, never surfaced
on any chapter page), and every slug in every term's `chapters` array actually exists in
nav-data.js. Run from the repo root:

    python3 tools/validate_glossary.py

Exit code is non-zero if any check fails.
"""
import json
import os
import subprocess
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GLOSSARY_DATA = os.path.join(REPO_ROOT, "assets", "js", "glossary-data.js")
NAV_DATA = os.path.join(REPO_ROOT, "assets", "js", "nav-data.js")

NODE_SCRIPT = """
global.window = {};
require(process.argv[1]);
require(process.argv[2]);
process.stdout.write(JSON.stringify({glossary: window.GLOSSARY, nav: window.COURSE_NAV}));
"""


def load_data():
    result = subprocess.run(
        ["node", "-e", NODE_SCRIPT, GLOSSARY_DATA, NAV_DATA],
        capture_output=True, text=True, cwd=REPO_ROOT,
    )
    if result.returncode != 0:
        print("Failed to load data files via node:", result.stderr, file=sys.stderr)
        sys.exit(2)
    return json.loads(result.stdout)


def main():
    data = load_data()
    glossary = data["glossary"]
    nav = data["nav"]
    nav_slugs = {c["slug"] for c in nav}

    errors = []
    seen_terms = set()

    for g in glossary:
        term = g.get("term")
        if term in seen_terms:
            errors.append(f"duplicate term: {term!r}")
        else:
            seen_terms.add(term)

        chapters = g.get("chapters") or []
        if not chapters:
            errors.append(f"orphaned term (empty chapters array): {term!r}")
        for slug in chapters:
            if slug not in nav_slugs:
                errors.append(f"{term!r}: references unknown chapter slug {slug!r}")

    nav_slug_list = [c["slug"] for c in nav]
    dup_nav_slugs = {s for s in nav_slug_list if nav_slug_list.count(s) > 1}
    if dup_nav_slugs:
        errors.append(f"duplicate slugs in nav-data.js: {sorted(dup_nav_slugs)}")

    print(f"Glossary terms: {len(glossary)}, nav chapters: {len(nav)}")
    if errors:
        print(f"\nFAILED ({len(errors)} issue(s)):")
        for e in errors:
            print(f"  {e}")
        sys.exit(1)
    print("All checks passed.")


if __name__ == "__main__":
    main()
