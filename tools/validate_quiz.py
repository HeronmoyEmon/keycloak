#!/usr/bin/env python3
"""Validator for assets/js/quiz-data.js.

Loads window.QUIZ_POOLS via Node and checks: every question has the required fields, every
answerIndex is a valid index into that question's own options array, and every question id is
globally unique across every pool on the site (not just unique within its own pool). Run from the
repo root:

    python3 tools/validate_quiz.py

Exit code is non-zero if any check fails.
"""
import json
import os
import subprocess
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
QUIZ_DATA = os.path.join(REPO_ROOT, "assets", "js", "quiz-data.js")

REQUIRED_FIELDS = ["id", "type", "question", "options", "answerIndex", "explanation"]
VALID_TYPES = {"concept", "scenario", "bug", "compare"}

NODE_SCRIPT = """
global.window = {};
require(process.argv[1]);
process.stdout.write(JSON.stringify(window.QUIZ_POOLS));
"""


def load_pools():
    result = subprocess.run(
        ["node", "-e", NODE_SCRIPT, QUIZ_DATA],
        capture_output=True, text=True, cwd=REPO_ROOT,
    )
    if result.returncode != 0:
        print("Failed to load quiz-data.js via node:", result.stderr, file=sys.stderr)
        sys.exit(2)
    return json.loads(result.stdout)


def main():
    pools = load_pools()
    errors = []
    seen_ids = {}
    total_questions = 0

    for slug, questions in pools.items():
        for q in questions:
            total_questions += 1
            missing = [f for f in REQUIRED_FIELDS if f not in q]
            if missing:
                errors.append(f"{slug}/{q.get('id', '?')}: missing fields {missing}")
                continue
            if q["id"] in seen_ids:
                errors.append(f"{slug}/{q['id']}: duplicate id (also in {seen_ids[q['id']]})")
            else:
                seen_ids[q["id"]] = slug
            if not isinstance(q["options"], list) or not q["options"]:
                errors.append(f"{slug}/{q['id']}: options is not a non-empty list")
                continue
            if not isinstance(q["answerIndex"], int) or not (0 <= q["answerIndex"] < len(q["options"])):
                errors.append(f"{slug}/{q['id']}: answerIndex {q['answerIndex']!r} out of range for {len(q['options'])} options")
            if q["type"] not in VALID_TYPES:
                errors.append(f"{slug}/{q['id']}: type {q['type']!r} not one of {sorted(VALID_TYPES)}")

    print(f"Pools: {len(pools)}, questions: {total_questions}, unique ids: {len(seen_ids)}")
    if errors:
        print(f"\nFAILED ({len(errors)} issue(s)):")
        for e in errors:
            print(f"  {e}")
        sys.exit(1)
    print("All checks passed.")


if __name__ == "__main__":
    main()
