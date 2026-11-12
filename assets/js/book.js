/* Shared chrome: theme, font-scale, sidebar nav. Clean-room build (CLAUDE.md §2.6).
   Each page sets, before loading this file:
     window.SITE_ROOT = ""          // on root-level pages (index.html, roadmap.html, ...)
     window.SITE_ROOT = "../"       // on pages one level deep (chapters/, case-studies/)
     window.CURRENT_CHAPTER = "slug" // optional, highlights + enables prev/next footer */
(function () {
  "use strict";

  var THEMES = ["light", "dark", "sepia"];
  var FONT_SCALES = [0.92, 1, 1.08, 1.18];

  function safeGet(key) {
    try { return localStorage.getItem(key); } catch (e) { return null; }
  }
  function safeSet(key, val) {
    try { localStorage.setItem(key, val); } catch (e) { /* ignore: private mode, blocked storage */ }
  }

  function applyTheme(theme) {
    if (theme === "light") {
      document.documentElement.setAttribute("data-theme", "light");
    } else {
      document.documentElement.setAttribute("data-theme", theme);
    }
  }

  function applyFontScale(scale) {
    document.documentElement.style.setProperty("--font-scale", scale);
  }

  function initThemeAndFont() {
    var theme = safeGet("iam:theme");
    if (THEMES.indexOf(theme) === -1) theme = "light";
    applyTheme(theme);
    var scale = parseFloat(safeGet("iam:font-scale"));
    if (!isNaN(scale) && FONT_SCALES.indexOf(scale) !== -1) applyFontScale(scale);
  }

  function cycleTheme() {
    var current = safeGet("iam:theme");
    var idx = THEMES.indexOf(current);
    var next = THEMES[(idx + 1) % THEMES.length];
    safeSet("iam:theme", next);
    applyTheme(next);
  }

  function cycleFontScale() {
    var current = parseFloat(safeGet("iam:font-scale")) || 1;
    var idx = FONT_SCALES.indexOf(current);
    var next = FONT_SCALES[(idx === -1 ? 0 : idx + 1) % FONT_SCALES.length];
    safeSet("iam:font-scale", next);
    applyFontScale(next);
  }

  function pad2(n) { return n < 10 ? "0" + n : "" + n; }

  function buildSidebar() {
    var mount = document.getElementById("sidebar-nav");
    if (!mount || !window.COURSE_NAV) return;
    var root = window.SITE_ROOT || "";
    var currentPart = null;
    window.COURSE_NAV.forEach(function (ch) {
      if (ch.slug === window.CURRENT_CHAPTER) currentPart = ch.part;
    });
    var html = "";
    var lastPart = null;
    window.COURSE_NAV.forEach(function (ch) {
      if (ch.part !== lastPart) {
        if (lastPart !== null) html += "</ul></details>";
        var openAttr = ch.part === currentPart ? " open" : "";
        html += '<details class="part-group"' + openAttr + '><summary class="part-title">' + ch.part +
          "</summary><ul class=\"nav-list\">";
        lastPart = ch.part;
      }
      var href = root + "chapters/" + pad2(ch.number) + "-" + ch.slug + ".html";
      var isCurrent = window.CURRENT_CHAPTER === ch.slug;
      var cls = "nav-link" + (isCurrent ? " current" : "") + (ch.built ? "" : " unbuilt");
      var label = ch.built ? ("<a class=\"" + cls + "\" href=\"" + href + "\">") : ("<span class=\"" + cls + "\">");
      var closeTag = ch.built ? "</a>" : "</span>";
      html += "<li>" + label +
        '<span class="num">' + pad2(ch.number) + "</span><span>" + ch.title +
        (ch.built ? "" : " — not built yet") + "</span>" +
        (ch.hasLab && ch.built ? '<span class="lab-dot" title="Has a hands-on lab"></span>' : "") +
        closeTag + "</li>";
    });
    if (lastPart !== null) html += "</ul></details>";
    mount.innerHTML = html;
  }

  function wireTopBar() {
    var themeBtn = document.getElementById("theme-toggle");
    if (themeBtn) themeBtn.addEventListener("click", cycleTheme);
    var fontBtn = document.getElementById("font-toggle");
    if (fontBtn) fontBtn.addEventListener("click", cycleFontScale);
    var sidebarToggle = document.getElementById("sidebar-toggle");
    var sidebar = document.querySelector(".sidebar");
    if (sidebarToggle && sidebar) {
      sidebarToggle.addEventListener("click", function () {
        sidebar.classList.toggle("open");
      });
    }
  }

  function buildChapterFooter() {
    var mount = document.getElementById("chapter-nav-footer");
    if (!mount || !window.COURSE_NAV || !window.CURRENT_CHAPTER) return;
    var idx = window.COURSE_NAV.findIndex(function (c) { return c.slug === window.CURRENT_CHAPTER; });
    if (idx === -1) return;
    var prev = window.COURSE_NAV[idx - 1];
    var next = window.COURSE_NAV[idx + 1];
    var root = window.SITE_ROOT || "";
    var html = "";
    if (prev) {
      html += '<a class="prev" href="' + root + "chapters/" + pad2(prev.number) + "-" + prev.slug + '.html">' +
        '<span class="dir">&larr; Previous</span>' + prev.title + "</a>";
    } else {
      html += "<span></span>";
    }
    if (next) {
      html += '<a class="next" href="' + root + "chapters/" + pad2(next.number) + "-" + next.slug + '.html">' +
        '<span class="dir">Next &rarr;</span>' + next.title + "</a>";
    }
    mount.innerHTML = html;
  }

  function renderChapterGlossary() {
    var mount = document.getElementById("chapter-glossary-mount");
    if (!mount || !window.GLOSSARY || !window.CURRENT_CHAPTER) return;
    var terms = window.GLOSSARY.filter(function (g) {
      return g.chapters && g.chapters.indexOf(window.CURRENT_CHAPTER) !== -1;
    }).sort(function (a, b) { return a.term.localeCompare(b.term); });
    if (!terms.length) {
      mount.innerHTML = '<p class="lede">Not merged into the master glossary yet.</p>';
      return;
    }
    mount.innerHTML = "<dl>" + terms.map(function (t) {
      return "<dt><strong>" + t.term + "</strong></dt><dd>" + t.definition + "</dd>";
    }).join("") + "</dl>";
  }

  /* Copy-to-clipboard button, auto-injected around every <pre> on the page.
     Plain command blocks get a floating "Copy" button; a <pre data-filename="...">
     gets a header bar naming the file, with the button in it, so the markup for a
     lab's file listing never needs anything beyond that one attribute. */

  /* Many transcript blocks (no data-filename -- those are real files, copied verbatim,
     never touched here) mix a "$ command" / ">>> expr" prompt with its own literal
     output in one block, for readability on the page. Copying the raw text and pasting
     it into a real shell tries to execute the output lines too. This extracts only the
     actual input a reader typed: strips the prompt, keeps a command's own continuation
     lines and comments, and drops bare output lines. A block with no prompt line at all
     (a pure command, or a pure output transcript) is returned unchanged. The prompt is
     "$" followed by a literal space -- never bare "$NAME", so a real shell variable
     reference at the start of a line (e.g. a file's own "$KC create ...") is never
     mistaken for a prompt in the first place; file blocks are excluded before this
     function is ever called anyway, as a second, independent safeguard.

     A command keeps spanning lines (stays "mid-command", not yet back to bare output)
     for three real reasons, not just a trailing backslash: an open quote that hasn't
     closed yet (single and double tracked separately, correctly ignoring a quote
     character of the other kind while already inside one -- "'{\"a\":\"b\"}'" never
     mistakes its inner double quotes for bash's own; quote-scanning itself stops at the
     first unquoted "#", since that's where a real shell comment starts -- without this,
     an apostrophe in a trailing English comment, e.g. "# northwind-dashboard's internal
     id", reads as an unclosed quote and corrupts everything after it, a real bug this
     already shipped with once), a shell block keyword that hasn't been closed yet
     (for/while/if/until ... do/then, closed by a line that's just done/fi/}), and the
     ordinary trailing-backslash case. Missing any one of these for real content (not
     just a contrived test) silently truncates or corrupts a real command when copied --
     confirmed by running the actual extracted output against every real prompt-bearing
     block on the site (68 of them) and finding zero remaining issues, not just by
     reading the regex. A speculative "..." Python-continuation-prompt case was tried
     and removed: nothing on the site ever uses it for real, and it falsely matched a
     real "... 5 ClusterRoles, 5 RoleBindings ..." truncation-ellipsis line in a
     chapter 42 kubectl output, corrupting that block -- removed rather than special-
     cased, since it had no real benefit to weigh against the real bug. */
  function extractCopyText(raw) {
    var lines = raw.split("\n");
    var hasPrompt = lines.some(function (l) {
      return /^\s*\$ /.test(l) || /^\s*>>> /.test(l);
    });
    if (!hasPrompt) return raw;

    var kept = [];
    var continuing = false;
    var inSingle = false;
    var inDouble = false;
    var blockDepth = 0;

    function scanQuotes(line) {
      for (var i = 0; i < line.length; i++) {
        var c = line.charAt(i);
        if (c === "#" && !inSingle && !inDouble) break; // unquoted # starts a comment; rest of line doesn't count
        if (c === "'" && !inDouble) inSingle = !inSingle;
        else if (c === '"' && !inSingle) inDouble = !inDouble;
      }
    }
    function endsWithContinuation(line) { return /\\\s*$/.test(line); }
    function opensBlock(line) { return /\b(do|then)\s*$/.test(line.trim()) || /\{\s*$/.test(line.trim()); }
    function closesBlock(line) { return /^(done|fi|\})\s*(#.*)?$/.test(line.trim()); }

    function afterKeepingLine(line) {
      scanQuotes(line);
      if (inSingle || inDouble) { continuing = true; return; }
      if (closesBlock(line) && blockDepth > 0) blockDepth--;
      else if (opensBlock(line)) blockDepth++;
      continuing = endsWithContinuation(line) || blockDepth > 0;
    }

    for (var i = 0; i < lines.length; i++) {
      var line = lines[i];

      if (continuing) {
        kept.push(line);
        afterKeepingLine(line);
        continue;
      }
      if (/^\s*\$ /.test(line)) {
        var strippedDollar = line.replace(/^\s*\$ /, "");
        kept.push(strippedDollar);
        afterKeepingLine(strippedDollar);
        continue;
      }
      if (/^\s*>>> /.test(line)) {
        kept.push(line.replace(/^\s*>>> /, ""));
        continue;
      }
      if (/^\s*#/.test(line) || line.trim() === "") {
        kept.push(line); // comments and blank lines are safe to paste, and useful to keep
        continue;
      }
      // anything else on an unprefixed line is the command's own real output -- drop it
    }
    return kept.join("\n");
  }

  function copyText(text, btn) {
    var original = btn.textContent;
    function showCopied() {
      btn.textContent = "Copied!";
      btn.classList.add("copied");
      setTimeout(function () {
        btn.textContent = original;
        btn.classList.remove("copied");
      }, 1500);
    }
    function fallback() {
      try {
        var ta = document.createElement("textarea");
        ta.value = text;
        ta.style.position = "fixed";
        ta.style.opacity = "0";
        document.body.appendChild(ta);
        ta.focus();
        ta.select();
        document.execCommand("copy");
        document.body.removeChild(ta);
        showCopied();
      } catch (e) { /* clipboard unavailable; silently do nothing */ }
    }
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(showCopied, fallback);
    } else {
      fallback();
    }
  }

  function initCodeBlocks() {
    var pres = document.querySelectorAll("pre");
    pres.forEach(function (pre) {
      if (pre.closest(".code-block")) return; // already wrapped (defensive, shouldn't happen)
      var filename = pre.getAttribute("data-filename");
      var wrapper = document.createElement("div");
      wrapper.className = "code-block" + (filename ? "" : " code-block--plain");
      pre.parentNode.insertBefore(wrapper, pre);
      wrapper.appendChild(pre);

      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "code-copy-btn";
      btn.textContent = "Copy";
      btn.setAttribute("aria-label", filename ? "Copy " + filename + " to clipboard" : "Copy command to clipboard");
      btn.addEventListener("click", function () {
        var codeEl = pre.querySelector("code") || pre;
        var raw = codeEl.textContent;
        copyText(filename ? raw : extractCopyText(raw), btn);
      });

      if (filename) {
        var header = document.createElement("div");
        header.className = "code-block-header";
        var nameSpan = document.createElement("span");
        nameSpan.className = "code-block-filename";
        nameSpan.textContent = filename;
        header.appendChild(nameSpan);
        header.appendChild(btn);
        wrapper.insertBefore(header, pre);
      } else {
        wrapper.appendChild(btn);
      }
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    initThemeAndFont();
    buildSidebar();
    wireTopBar();
    buildChapterFooter();
    renderChapterGlossary();
    initCodeBlocks();
  });
})();
