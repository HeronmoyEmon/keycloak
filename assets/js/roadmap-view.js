/* Visual roadmap landing page renderer. Clean-room build (CLAUDE.md §2.6).
   Reads window.COURSE_NAV (nav-data.js) for the chapter list, and scans
   localStorage["iam:read:<slug>"] (written by slides.js on each chapter
   page) to compute per-chapter and overall slide-read progress. Never
   invents a total slide count for a chapter that hasn't been opened yet —
   those chapters simply show as "not started" rather than 0/N. */
(function () {
  "use strict";

  function safeGet(key) { try { return localStorage.getItem(key); } catch (e) { return null; } }

  function readProgress(slug) {
    var raw = safeGet("iam:read:" + slug);
    if (!raw) return null;
    try {
      var s = JSON.parse(raw);
      if (!s || typeof s !== "object" || !s.total) return null;
      var done = (s.done && typeof s.done === "object") ? s.done : {};
      var doneCount = Object.keys(done).filter(function (k) { return done[k]; }).length;
      return { done: doneCount, total: s.total };
    } catch (e) { return null; }
  }

  function pad2(n) { return n < 10 ? "0" + n : "" + n; }

  function renderStats() {
    var mount = document.getElementById("stats-mount");
    if (!mount || !window.COURSE_NAV) return;
    var nav = window.COURSE_NAV;
    var totalChapters = nav.length;
    var builtChapters = nav.filter(function (c) { return c.built; }).length;

    var startedChapters = 0, completeChapters = 0, slidesRead = 0, slidesTotalKnown = 0;
    nav.forEach(function (c) {
      var p = readProgress(c.slug);
      if (!p) return;
      startedChapters++;
      slidesRead += p.done;
      slidesTotalKnown += p.total;
      if (p.done >= p.total) completeChapters++;
    });

    var tiles = [
      { value: builtChapters + " / " + totalChapters, label: "Chapters available" },
      { value: String(startedChapters), label: "Chapters started" },
      { value: String(completeChapters), label: "Chapters fully read" },
      { value: slidesTotalKnown ? (slidesRead + " / " + slidesTotalKnown) : "—", label: "Slides marked read" }
    ];
    mount.innerHTML = tiles.map(function (t) {
      return '<div class="stat-tile"><span class="stat-value">' + t.value + '</span>' +
        '<span class="stat-label">' + t.label + '</span></div>';
    }).join("");
  }

  function nodeHtml(c) {
    var num = pad2(c.number);
    if (!c.built) {
      return (
        '<li class="road-node locked">' +
        '<span class="road-card">' +
        '<span class="road-marker"><span class="road-marker-inner">' + num + '</span></span>' +
        '<span class="road-card-body">' +
        '<span class="road-card-title">' + c.title + '</span>' +
        '<span class="road-card-meta"><span class="badge minutes">Coming soon</span></span>' +
        '</span></span></li>'
      );
    }
    var href = "chapters/" + num + "-" + c.slug + ".html";
    var p = readProgress(c.slug);
    var stateClass = "built";
    var marker = '<span class="road-marker-inner">' + num + '</span>';
    var progressText = "";
    var markerStyle = "";
    if (p && p.total) {
      if (p.done >= p.total) {
        stateClass = "complete";
        marker = '<span class="road-marker-inner">✓</span>';
        progressText = '<span class="road-card-progress-text">All ' + p.total + ' slides read</span>';
      } else if (p.done > 0) {
        stateClass = "in-progress";
        var pct = Math.round((p.done / p.total) * 100);
        markerStyle = ' style="--pct:' + pct + '"';
        progressText = '<span class="road-card-progress-text">' + p.done + ' / ' + p.total + ' slides read</span>';
      }
    }
    var labBadge = c.hasLab ? '<span class="badge lab">lab</span>' : "";
    return (
      '<li class="road-node ' + stateClass + '">' +
      '<a class="road-card" href="' + href + '">' +
      '<span class="road-marker"' + markerStyle + '>' + marker + '</span>' +
      '<span class="road-card-body">' +
      '<span class="road-card-title">' + c.title + '</span>' +
      '<span class="road-card-meta"><span class="badge minutes">' + c.minutes + ' min</span>' + labBadge + progressText + '</span>' +
      '</span></a></li>'
    );
  }

  function renderTrack() {
    var mount = document.getElementById("roadmap-track");
    if (!mount || !window.COURSE_NAV) return;
    var nav = window.COURSE_NAV;
    var order = [];
    var byPart = {};
    nav.forEach(function (c) {
      if (!byPart[c.part]) { byPart[c.part] = []; order.push(c.part); }
      byPart[c.part].push(c);
    });

    var html = '<ol class="road-list">' + order.map(function (partName) {
      var chapters = byPart[partName];
      var builtCount = chapters.filter(function (c) { return c.built; }).length;
      var header =
        '<li class="road-group-header"><h2>' + partName + '</h2>' +
        '<span class="road-group-progress">' + builtCount + ' / ' + chapters.length + ' chapters available</span></li>';
      return header + chapters.map(nodeHtml).join("");
    }).join("") + '</ol>';
    mount.innerHTML = html;
  }

  document.addEventListener("DOMContentLoaded", function () {
    renderStats();
    renderTrack();
  });
})();
