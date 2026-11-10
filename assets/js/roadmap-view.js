/* Visual roadmap landing page renderer. Clean-room build (CLAUDE.md §2.6).
   Renders a roadmap.sh-style mind-map spine: one node per Part down a central
   trunk, each sprouting a cluster of chapter "leaves" on an alternating side.
   Connector curves between a Part's spine node and its chapter leaves are
   drawn at runtime from real getBoundingClientRect() measurements (box
   heights vary with content/theme/font-scale, so they can't be hardcoded),
   using one shared absolutely-positioned SVG overlay. Reads window.COURSE_NAV
   (nav-data.js) for the chapter list, and scans localStorage["iam:read:<slug>"]
   (written by slides.js on each chapter page) for per-chapter progress. Never
   invents a total slide count for a chapter that hasn't been opened yet. */
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

  function leafHtml(c) {
    var num = pad2(c.number);
    var labBadge = c.hasLab ? '<span class="road-leaf-lab">lab</span>' : "";
    if (!c.built) {
      return (
        '<li class="road-leaf locked">' +
        '<span class="road-leaf-static">' +
        '<span class="road-leaf-num">' + num + '</span>' +
        '<span class="road-leaf-title">' + c.title + labBadge + '</span>' +
        '<span class="road-leaf-status"></span>' +
        '</span></li>'
      );
    }
    var href = "chapters/" + num + "-" + c.slug + ".html";
    var p = readProgress(c.slug);
    var stateClass = "built";
    var statusStyle = "";
    if (p && p.total) {
      if (p.done >= p.total) stateClass = "complete";
      else if (p.done > 0) {
        stateClass = "in-progress";
        statusStyle = ' style="--pct:' + Math.round((p.done / p.total) * 100) + '"';
      }
    }
    return (
      '<li class="road-leaf ' + stateClass + '">' +
      '<a class="road-leaf-link" href="' + href + '">' +
      '<span class="road-leaf-num">' + num + '</span>' +
      '<span class="road-leaf-title">' + c.title + labBadge + '</span>' +
      '<span class="road-leaf-status"' + statusStyle + '></span>' +
      '</a></li>'
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

    var rowsHtml = order.map(function (partName, i) {
      var chapters = byPart[partName];
      var side = (i % 2 === 0) ? "right" : "left";
      var builtCount = chapters.filter(function (c) { return c.built; }).length;
      var bits = partName.split(" — ");
      var partNum = bits[0] || partName;
      var partDesc = bits[1] || "";
      var leaves = '<ul class="road-leaf-cluster">' + chapters.map(leafHtml).join("") + '</ul>';
      var empty = '<div class="road-leaf-cluster-empty"></div>';
      var spine =
        '<div class="road-spine-node"><div class="road-part-badge">' + partNum +
        (partDesc ? '<span class="road-part-desc">' + partDesc + '</span>' : "") +
        '<span class="road-part-progress">' + builtCount + ' / ' + chapters.length + ' built</span>' +
        '</div></div>';
      return '<div class="road-row" data-side="' + side + '">' +
        (side === "left" ? leaves : empty) + spine + (side === "right" ? leaves : empty) +
        '</div>';
    }).join("");

    mount.innerHTML =
      '<div class="road-map" id="road-map">' +
      '<svg class="road-svg-layer" id="road-svg-layer"></svg>' +
      '<div class="road-rows" id="road-rows">' +
      '<div class="road-spine-line"></div>' +
      rowsHtml +
      '</div></div>';

    drawConnectors();
  }

  function drawConnectors() {
    var svg = document.getElementById("road-svg-layer");
    var mapEl = document.getElementById("road-map");
    if (!svg || !mapEl) return;

    var isMobile = false;
    try { isMobile = window.matchMedia && window.matchMedia("(max-width: 760px)").matches; } catch (e) { /* ignore */ }
    if (isMobile) { svg.innerHTML = ""; return; }

    var mapRect = mapEl.getBoundingClientRect();
    if (!mapRect.width || !mapRect.height) return; // not laid out yet (e.g. hidden tab) — nothing to draw
    svg.setAttribute("width", mapRect.width);
    svg.setAttribute("height", mapRect.height);
    svg.setAttribute("viewBox", "0 0 " + mapRect.width + " " + mapRect.height);

    var STANDOFF = 14; // px of breathing room between a box edge and where its connector line starts/ends
    var paths = [];
    var rows = Array.prototype.slice.call(document.querySelectorAll(".road-row"));
    rows.forEach(function (row) {
      var badge = row.querySelector(".road-part-badge");
      var cluster = row.querySelector(".road-leaf-cluster");
      if (!badge || !cluster) return;
      var side = row.getAttribute("data-side");
      var dir = side === "right" ? 1 : -1;
      var badgeRect = badge.getBoundingClientRect();
      var bx = (side === "right" ? badgeRect.right : badgeRect.left) - mapRect.left + dir * STANDOFF;
      var by = (badgeRect.top + badgeRect.height / 2) - mapRect.top;
      var leaves = Array.prototype.slice.call(cluster.querySelectorAll(".road-leaf"));
      leaves.forEach(function (leaf, leafIndex) {
        var leafRect = leaf.getBoundingClientRect();
        var lx = (side === "right" ? leafRect.left : leafRect.right) - mapRect.left - dir * STANDOFF;
        var ly = (leafRect.top + leafRect.height / 2) - mapRect.top;
        // Vary the curve's bend per leaf (0.38 / 0.5 / 0.62, cycling) instead of always splitting the
        // control points at the exact midpoint — a roadmap.sh-style branch reads as organically curved
        // rather than every line being an identical mechanical elbow.
        var split = 0.5 + ((leafIndex % 3) - 1) * 0.12;
        var c1x = bx + (lx - bx) * split;
        var c2x = bx + (lx - bx) * (1 - split);
        paths.push(
          '<path class="road-connector" d="M' + bx.toFixed(1) + ',' + by.toFixed(1) +
          ' C ' + c1x.toFixed(1) + ',' + by.toFixed(1) + ' ' + c2x.toFixed(1) + ',' + ly.toFixed(1) +
          ' ' + lx.toFixed(1) + ',' + ly.toFixed(1) + '"></path>'
        );
      });
    });
    svg.innerHTML = paths.join("");
  }

  var redrawTimer = null;
  function scheduleRedraw() {
    if (redrawTimer) clearTimeout(redrawTimer);
    redrawTimer = setTimeout(drawConnectors, 150);
  }

  document.addEventListener("DOMContentLoaded", function () {
    renderStats();
    renderTrack();
    window.addEventListener("resize", scheduleRedraw);
    var themeBtn = document.getElementById("theme-toggle");
    if (themeBtn) themeBtn.addEventListener("click", scheduleRedraw);
    var fontBtn = document.getElementById("font-toggle");
    if (fontBtn) fontBtn.addEventListener("click", scheduleRedraw);
    // Fonts/webfonts loading after first paint can shift leaf heights; redraw once more shortly after load.
    window.addEventListener("load", scheduleRedraw);
  });
})();
