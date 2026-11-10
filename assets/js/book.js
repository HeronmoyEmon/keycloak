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
    var theme = safeGet("iam:theme") || "dark-or-light-auto";
    if (THEMES.indexOf(theme) !== -1) applyTheme(theme);
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
    var html = "";
    var lastPart = null;
    window.COURSE_NAV.forEach(function (ch) {
      if (ch.part !== lastPart) {
        if (lastPart !== null) html += "</ul>";
        html += '<h2 class="part-title">' + ch.part + "</h2><ul class=\"nav-list\">";
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
    if (lastPart !== null) html += "</ul>";
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

  document.addEventListener("DOMContentLoaded", function () {
    initThemeAndFont();
    buildSidebar();
    wireTopBar();
    buildChapterFooter();
    renderChapterGlossary();
  });
})();
