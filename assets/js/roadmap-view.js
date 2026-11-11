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
    var startedChapters = 0, completeChapters = 0, slidesRead = 0, slidesTotalKnown = 0;
    var furthest = null;
    nav.forEach(function (c) {
      var p = readProgress(c.slug);
      if (!p) return;
      startedChapters++;
      slidesRead += p.done;
      slidesTotalKnown += p.total;
      if (p.done >= p.total) completeChapters++;
      if (!furthest || c.number > furthest.number) furthest = c;
    });

    var hereValue = "—", hereLabel = "You are here — start with Chapter 0";
    if (furthest) {
      var parts = furthest.part.split(" — ");
      hereValue = parts[0].replace("Part ", "Pt. ");
      hereLabel = "You are here — " + (parts[1] || furthest.part);
    }

    var tiles = [
      { value: String(startedChapters), label: "Chapters started" },
      { value: String(completeChapters), label: "Chapters fully read" },
      { value: slidesTotalKnown ? (slidesRead + " / " + slidesTotalKnown) : "—", label: "Slides marked read" },
      { value: hereValue, label: hereLabel }
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

    var STANDOFF = 16; // px of breathing room between a box edge and where its connector line starts/ends
    var paths = [];
    var rows = Array.prototype.slice.call(document.querySelectorAll(".road-row"));
    rows.forEach(function (row) {
      var badge = row.querySelector(".road-part-badge");
      var cluster = row.querySelector(".road-leaf-cluster");
      if (!badge || !cluster) return;
      var side = row.getAttribute("data-side");
      var dir = side === "right" ? 1 : -1;
      var badgeRect = badge.getBoundingClientRect();
      var badgeX = (side === "right" ? badgeRect.right : badgeRect.left) - mapRect.left + dir * STANDOFF;
      var badgeTop = badgeRect.top - mapRect.top;

      var leaves = Array.prototype.slice.call(cluster.querySelectorAll(".road-leaf"));
      if (!leaves.length) return;

      var leafPoints = leaves.map(function (leaf) {
        var r = leaf.getBoundingClientRect();
        return {
          x: (side === "right" ? r.left : r.right) - mapRect.left - dir * STANDOFF,
          y: (r.top + r.height / 2) - mapRect.top
        };
      });
      var minY = Math.min.apply(null, leafPoints.map(function (p) { return p.y; }));
      var maxY = Math.max.apply(null, leafPoints.map(function (p) { return p.y; }));

      // Individual curved line per chapter, roadmap.sh-style — but instead of pinching every curve
      // through the exact same point on the badge (which is what bunched up for a big Part like IAM
      // Foundations with 9 chapters), spread each curve's starting point across the badge's own edge,
      // proportional to where that chapter sits within the cluster's vertical span. Lines fan out
      // gently from the start instead of all passing through one spot.
      var inset = Math.min(badgeRect.height * 0.22, 9);
      leafPoints.forEach(function (leaf, i) {
        var t = (maxY === minY) ? 0.5 : (leaf.y - minY) / (maxY - minY);
        var by = badgeTop + inset + t * (badgeRect.height - inset * 2);
        var split = 0.5 + ((i % 3) - 1) * 0.1;
        var c1x = badgeX + (leaf.x - badgeX) * split;
        var c2x = badgeX + (leaf.x - badgeX) * (1 - split);
        paths.push(
          '<path class="road-connector" d="M' + badgeX.toFixed(1) + ',' + by.toFixed(1) +
          ' C ' + c1x.toFixed(1) + ',' + by.toFixed(1) + ' ' + c2x.toFixed(1) + ',' + leaf.y.toFixed(1) +
          ' ' + leaf.x.toFixed(1) + ',' + leaf.y.toFixed(1) + '"></path>'
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

  function refresh() {
    renderStats();
    renderTrack();
  }

  document.addEventListener("DOMContentLoaded", function () {
    refresh();
    window.addEventListener("resize", scheduleRedraw);
    var themeBtn = document.getElementById("theme-toggle");
    if (themeBtn) themeBtn.addEventListener("click", scheduleRedraw);
    var fontBtn = document.getElementById("font-toggle");
    if (fontBtn) fontBtn.addEventListener("click", scheduleRedraw);
    // Fonts/webfonts loading after first paint can shift leaf heights; redraw once more shortly after load.
    window.addEventListener("load", scheduleRedraw);
  });

  // This page's own DOMContentLoaded only runs once, at first load — but progress can change in
  // localStorage from a *different* tab/window (chapter page open alongside this one), from the
  // bfcache restoring this exact page unexecuted on Back/Forward, or just from switching back to an
  // already-open tab with no navigation event at all. Cover all three instead of guessing which one
  // applies:
  //   - "storage" fires on every OTHER same-origin document when localStorage changes — the correct,
  //     direct signal for "a chapter page open in another tab just marked something read".
  //   - "pageshow" fires on every reveal of this page, including a bfcache restore
  //     (event.persisted === true there, which DOMContentLoaded does not re-fire for).
  //   - "visibilitychange"/"focus" catch the plain tab-switch-back case, where neither of the above
  //     fires because the page was never unloaded or navigated away from at all.
  window.addEventListener("storage", function (e) {
    if (!e.key || e.key.indexOf("iam:read:") === 0) refresh();
  });
  window.addEventListener("pageshow", refresh);
  window.addEventListener("focus", refresh);
  document.addEventListener("visibilitychange", function () {
    if (document.visibilityState === "visible") refresh();
  });
})();
