/* Per-chapter slide deck controller. Clean-room build (CLAUDE.md §2.6).
   Reads/writes window.localStorage["iam:read:<chapterSlug>"] = { total, done: { slideId: true, ... } }
   so the roadmap landing page (roadmap-view.js) can compute per-chapter progress without loading
   this chapter's own HTML. Call SlideDeck.init({ chapterSlug: "..." }) after DOM is ready. */
window.SlideDeck = (function () {
  "use strict";

  function safeGet(key) { try { return localStorage.getItem(key); } catch (e) { return null; } }
  function safeSet(key, val) { try { localStorage.setItem(key, val); } catch (e) { /* ignore */ } }

  function stateKey(slug) { return "iam:read:" + slug; }

  function readState(slug) {
    var raw = safeGet(stateKey(slug));
    if (!raw) return { total: 0, done: {} };
    try {
      var s = JSON.parse(raw);
      if (!s || typeof s !== "object") return { total: 0, done: {} };
      if (!s.done || typeof s.done !== "object") s.done = {};
      return s;
    } catch (e) { return { total: 0, done: {} }; }
  }

  function writeState(slug, state) { safeSet(stateKey(slug), JSON.stringify(state)); }

  function pad2(n) { return n < 10 ? "0" + n : "" + n; }

  function init(opts) {
    opts = opts || {};
    var chapterSlug = opts.chapterSlug;
    var viewport = document.getElementById("deck-viewport");
    if (!viewport || !chapterSlug) return;

    var slides = Array.prototype.slice.call(viewport.querySelectorAll(".slide"));
    var total = slides.length;
    if (!total) return;

    var state = readState(chapterSlug);
    state.total = total;
    var current = 0;

    var nav = window.COURSE_NAV || [];
    var idx = nav.findIndex ? nav.findIndex(function (c) { return c.slug === chapterSlug; }) : -1;
    var prevChapter = (idx > 0) ? nav[idx - 1] : null;
    var nextChapter = (idx !== -1 && idx < nav.length - 1) ? nav[idx + 1] : null;
    var currentChapter = idx !== -1 ? nav[idx] : null;
    var root = window.SITE_ROOT || "";
    function chapterHref(c) { return root + "chapters/" + pad2(c.number) + "-" + c.slug + ".html"; }

    var brandEl = document.querySelector(".top-bar .brand");
    if (brandEl && currentChapter) {
      brandEl.title = "IAM & Keycloak Mastery — back to roadmap";
      var safeTitle = (currentChapter.number + ". " + currentChapter.title)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;");
      brandEl.innerHTML = '<span class="mark">IK</span><span class="brand-text">' + safeTitle + "</span>";
    }

    var outlineList = document.getElementById("slide-outline");
    var progressMini = document.getElementById("deck-progress-mini");
    var positionLabel = document.getElementById("deck-position");
    var prevBtn = document.getElementById("pager-prev");
    var nextBtn = document.getElementById("pager-next");
    var markBtn = document.getElementById("pager-mark");
    var markAllBtn = document.getElementById("mark-all-btn");
    var outlineToggle = document.getElementById("outline-toggle");
    var outlinePanel = document.getElementById("deck-outline-panel");

    function doneCount() {
      var n = 0;
      slides.forEach(function (s) { if (state.done[s.dataset.slide]) n++; });
      return n;
    }

    function allRead() { return doneCount() === total; }

    function renderMarkAll() {
      if (!markAllBtn) return;
      var done = allRead();
      markAllBtn.textContent = done ? "Whole chapter marked as read ✓" : "Mark whole chapter as read";
      markAllBtn.classList.toggle("is-done", done);
    }

    function renderOutline() {
      if (!outlineList) return;
      outlineList.innerHTML = slides.map(function (s, i) {
        var id = s.dataset.slide;
        var isRead = !!state.done[id];
        var isCurrent = i === current;
        return '<li><a href="#" data-slide-index="' + i + '" class="' +
          (isCurrent ? "current " : "") + (isRead ? "read" : "") + '">' +
          '<span class="slide-num">' + (i + 1) + '</span>' +
          '<span class="slide-outline-title">' + (s.dataset.title || "") + '</span>' +
          '<span class="slide-check">' + (isRead ? "✓" : "") + '</span>' +
          '</a></li>';
      }).join("");
    }

    function renderProgress() {
      var dc = doneCount();
      var pct = total ? Math.round((dc / total) * 100) : 0;
      if (progressMini) {
        progressMini.innerHTML = dc + " / " + total + " marked read" +
          '<span class="mini-track"><span class="mini-fill" style="width:' + pct + '%"></span></span>';
      }
      if (positionLabel) positionLabel.textContent = "Slide " + (current + 1) + " of " + total;
    }

    function renderPager() {
      if (prevBtn) {
        if (current > 0) {
          prevBtn.textContent = "← " + (slides[current - 1].dataset.title || "Previous");
          prevBtn.disabled = false;
          prevBtn.className = "pager-btn pager-prev";
        } else if (prevChapter) {
          prevBtn.textContent = "← Previous chapter: " + prevChapter.title;
          prevBtn.disabled = false;
          prevBtn.className = "pager-btn pager-prev pager-chapter-link";
        } else {
          prevBtn.textContent = "";
          prevBtn.disabled = true;
        }
      }
      if (nextBtn) {
        if (current < total - 1) {
          nextBtn.textContent = (slides[current + 1].dataset.title || "Next") + " →";
          nextBtn.disabled = false;
          nextBtn.className = "pager-btn pager-next";
        } else if (nextChapter) {
          nextBtn.textContent = "Next chapter: " + nextChapter.title + " →";
          nextBtn.disabled = false;
          nextBtn.className = "pager-btn pager-next pager-chapter-link";
        } else {
          nextBtn.textContent = "";
          nextBtn.disabled = true;
        }
      }
      if (markBtn) {
        var id = slides[current].dataset.slide;
        var isDone = !!state.done[id];
        markBtn.textContent = isDone ? "Marked as read ✓" : "Mark as read";
        markBtn.classList.toggle("is-done", isDone);
      }
    }

    function render() {
      slides.forEach(function (s, i) { s.classList.toggle("is-active", i === current); });
      renderOutline();
      renderProgress();
      renderPager();
      renderMarkAll();
      writeState(chapterSlug, state);
      try { history.replaceState(null, "", "#" + slides[current].dataset.slide); } catch (e) { /* ignore */ }
    }

    function go(i) {
      if (i < 0) {
        if (prevChapter) window.location.href = chapterHref(prevChapter);
        return;
      }
      if (i >= total) {
        if (nextChapter) window.location.href = chapterHref(nextChapter);
        return;
      }
      current = i;
      render();
      window.scrollTo({ top: 0, behavior: "instant" in window ? "instant" : "auto" });
    }

    if (prevBtn) prevBtn.addEventListener("click", function () { go(current - 1); });
    if (nextBtn) nextBtn.addEventListener("click", function () { go(current + 1); });
    if (markBtn) {
      markBtn.addEventListener("click", function () {
        var id = slides[current].dataset.slide;
        state.done[id] = !state.done[id];
        render();
      });
    }
    if (markAllBtn) {
      markAllBtn.addEventListener("click", function () {
        var newValue = !allRead();
        slides.forEach(function (s) { state.done[s.dataset.slide] = newValue; });
        render();
      });
    }
    if (outlineList) {
      outlineList.addEventListener("click", function (e) {
        var a = e.target.closest ? e.target.closest("[data-slide-index]") : null;
        if (!a) return;
        e.preventDefault();
        go(parseInt(a.getAttribute("data-slide-index"), 10));
        if (outlinePanel) outlinePanel.classList.remove("open");
      });
    }
    if (outlineToggle && outlinePanel) {
      outlineToggle.addEventListener("click", function () { outlinePanel.classList.toggle("open"); });
    }
    document.addEventListener("keydown", function (e) {
      var tag = e.target && e.target.tagName;
      if (tag === "INPUT" || tag === "TEXTAREA") return;
      if (e.key === "ArrowRight") go(current + 1);
      else if (e.key === "ArrowLeft") go(current - 1);
    });

    var startIndex = 0;
    var hash = (location.hash || "").slice(1);
    if (hash) {
      var hi = slides.findIndex(function (s) { return s.dataset.slide === hash; });
      if (hi !== -1) startIndex = hi;
    }
    current = startIndex;
    render();
  }

  return { init: init };
})();
