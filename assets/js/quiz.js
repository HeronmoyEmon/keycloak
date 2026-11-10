/* Generic quiz engine, driven by window.QUIZ_POOLS (quiz-data.js). Clean-room build (CLAUDE.md §2.6). */
window.QuizEngine = (function () {
  "use strict";

  function safeGet(key) {
    try { return localStorage.getItem(key); } catch (e) { return null; }
  }
  function safeSet(key, val) {
    try { localStorage.setItem(key, val); } catch (e) { /* ignore */ }
  }

  function shuffle(arr) {
    var a = arr.slice();
    for (var i = a.length - 1; i > 0; i--) {
      var j = Math.floor(Math.random() * (i + 1));
      var t = a[i]; a[i] = a[j]; a[j] = t;
    }
    return a;
  }

  function prepareQuestion(q) {
    var order = shuffle(q.options.map(function (_, i) { return i; }));
    return {
      id: q.id, type: q.type, question: q.question, explanation: q.explanation,
      options: order.map(function (i) { return q.options[i]; }),
      answerIndex: order.indexOf(q.answerIndex)
    };
  }

  function scoreKey(slug) { return "iam:quiz:" + slug; }

  function readScores(slug) {
    var raw = safeGet(scoreKey(slug));
    if (!raw) return { best: 0, last: 0, attempts: 0 };
    try { return JSON.parse(raw); } catch (e) { return { best: 0, last: 0, attempts: 0 }; }
  }

  function writeScore(slug, correct, total) {
    var s = readScores(slug);
    s.last = correct;
    s.lastTotal = total;
    s.best = Math.max(s.best || 0, correct);
    s.attempts = (s.attempts || 0) + 1;
    safeSet(scoreKey(slug), JSON.stringify(s));
    return s;
  }

  function renderQuiz(containerId, slug, count) {
    var el = document.getElementById(containerId);
    if (!el) return;
    var pool = (window.QUIZ_POOLS && window.QUIZ_POOLS[slug]) || [];
    if (!pool.length) {
      el.innerHTML = '<p class="lede">No quiz questions yet for this chapter.</p>';
      return;
    }
    var n = Math.min(count || 10, pool.length);
    var questions = shuffle(pool).slice(0, n).map(prepareQuestion);
    var current = 0;
    var correctCount = 0;

    function renderQuestion() {
      var q = questions[current];
      var optionsHtml = q.options.map(function (opt, i) {
        return '<li><button class="quiz-option" data-index="' + i + '">' + opt + "</button></li>";
      }).join("");
      el.innerHTML =
        '<div class="quiz-progress"><span>Question ' + (current + 1) + " of " + questions.length +
        '</span><span>' + q.type + "</span></div>" +
        '<div class="quiz-question">' + q.question + "</div>" +
        '<ul class="quiz-options">' + optionsHtml + "</ul>" +
        '<div class="quiz-controls"></div>';
      el.querySelectorAll(".quiz-option").forEach(function (btn) {
        btn.addEventListener("click", function () { onAnswer(parseInt(btn.dataset.index, 10)); });
      });
    }

    function onAnswer(chosenIndex) {
      var q = questions[current];
      var buttons = el.querySelectorAll(".quiz-option");
      buttons.forEach(function (btn, i) {
        btn.disabled = true;
        if (i === q.answerIndex) btn.classList.add("correct");
        else if (i === chosenIndex) btn.classList.add("incorrect");
      });
      if (chosenIndex === q.answerIndex) correctCount++;
      var controls = el.querySelector(".quiz-controls");
      var explanation = document.createElement("div");
      explanation.className = "quiz-explanation";
      explanation.textContent = q.explanation || "";
      el.insertBefore(explanation, controls);
      var btn = document.createElement("button");
      btn.className = "btn";
      btn.textContent = current + 1 < questions.length ? "Next question" : "See score";
      btn.addEventListener("click", function () {
        current++;
        if (current < questions.length) renderQuestion();
        else renderScore();
      });
      controls.appendChild(btn);
    }

    function renderScore() {
      var s = writeScore(slug, correctCount, questions.length);
      el.innerHTML =
        '<div class="quiz-score-summary">' +
        '<div class="score-big">' + correctCount + " / " + questions.length + "</div>" +
        "<p>Best: " + s.best + " / " + questions.length + " &middot; Attempts: " + s.attempts + "</p>" +
        '<button class="btn secondary" id="quiz-retake">Retake</button>' +
        "</div>";
      document.getElementById("quiz-retake").addEventListener("click", function () {
        renderQuiz(containerId, slug, count);
      });
    }

    renderQuestion();
  }

  return { renderQuiz: renderQuiz, readScores: readScores };
})();
