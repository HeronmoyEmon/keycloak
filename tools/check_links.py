#!/usr/bin/env python3
"""Site-wide link checker for the IAM & Keycloak Mastery course.

Checks every internal href across index/roadmap/glossary/quizzes/labs/case-studies/exams pages,
chapters/, case-studies/, and exams/ for a file that actually exists on disk, and (with --external)
live-curls every external http(s) link found. Run from the repo root:

    python3 tools/check_links.py            # internal links only, fast
    python3 tools/check_links.py --external # also live-check every external URL (slow, one per line)

Exit code is non-zero if any internal link is broken (external failures are reported but don't fail
the exit code on their own, since third-party sites occasionally rate-limit or block bare curl headers
that a real browser wouldn't trip -- see the printed guidance for a 403).
"""
import argparse
import glob
import os
import re
import subprocess
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

PAGE_GLOBS = ["*.html", "chapters/*.html", "case-studies/*.html", "exams/*.html", "capstone/*.html"]

HREF_RE = re.compile(r'href="([^"]+)"')


def find_pages():
    pages = []
    for pattern in PAGE_GLOBS:
        pages.extend(glob.glob(os.path.join(REPO_ROOT, pattern)))
    return sorted(set(pages))


def is_inside_script_block(content, pos):
    """True if `pos` falls inside a <script>...</script> block (avoids false positives from JS
    template-literal/string-concatenation code that happens to contain the substring href="...")."""
    for m in re.finditer(r"<script\b[^>]*>.*?</script>", content, re.S):
        if m.start() <= pos < m.end():
            return True
    return False


def check_internal(pages):
    broken = []
    external = set()
    for path in pages:
        base_dir = os.path.dirname(path)
        with open(path, encoding="utf-8") as f:
            content = f.read()
        for m in HREF_RE.finditer(content):
            href = m.group(1)
            if is_inside_script_block(content, m.start()):
                continue
            if href.startswith("#") or href.startswith("mailto:") or href.startswith("data:"):
                continue
            if href.startswith("http://") or href.startswith("https://"):
                external.add(href)
                continue
            path_part = href.split("#")[0]
            if not path_part:
                continue
            full = os.path.normpath(os.path.join(base_dir, path_part))
            if not os.path.exists(full):
                broken.append((os.path.relpath(path, REPO_ROOT), href))
    return broken, external


def check_external(urls):
    results = []
    for url in sorted(urls):
        try:
            code = subprocess.run(
                ["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", "-L",
                 "-A", "Mozilla/5.0", "--max-time", "15", url],
                capture_output=True, text=True, timeout=20,
            ).stdout.strip()
        except Exception as e:
            code = f"ERR({e})"
        results.append((code, url))
    return results


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--external", action="store_true", help="also live-check every external URL")
    args = parser.parse_args()

    pages = find_pages()
    broken, external = check_internal(pages)

    print(f"Scanned {len(pages)} pages, found {len(external)} unique external links.")
    if broken:
        print(f"\nBROKEN internal links ({len(broken)}):")
        for path, href in broken:
            print(f"  {path}: {href}")
    else:
        print("Internal links: all OK.")

    if args.external:
        print(f"\nChecking {len(external)} external links (this can take a minute)...")
        results = check_external(external)
        non200 = [(c, u) for c, u in results if c != "200"]
        for code, url in results:
            if code != "200":
                print(f"  {code}  {url}")
        print(f"\n{len(results) - len(non200)}/{len(results)} external links returned 200.")
        if non200:
            print("Non-200 results don't always mean a dead link -- some sites block a bare curl")
            print("request (missing Accept/Accept-Language headers) with a 403 but work fine for a")
            print("real browser. Re-check manually before treating one as broken.")

    sys.exit(1 if broken else 0)


if __name__ == "__main__":
    main()
