/* Ranking de um quiz: todos os participantes, do maior para o menor total. */
(async () => {
  const code = new URLSearchParams(window.location.search).get("codigo");
  const quiz = await QuizStore.getByCode(code);

  if (!quiz || quiz.questions.length === 0) {
    document.getElementById("rk-notfound").hidden = false;
    return;
  }

  document.title = `Ranking — ${quiz.title} — Queerz`;
  document.getElementById("ranking-main").hidden = false;
  document.getElementById("rk-title").textContent = quiz.title;
  document.getElementById("rk-play").href = `quiz.html?codigo=${encodeURIComponent(quiz.code)}`;

  const ranked = Game.standings(quiz, quiz.questions.length);
  const people = ranked.length;
  document.getElementById("rk-meta").textContent =
    `${people} ${people === 1 ? "participante" : "participantes"} · ` +
    `${quiz.questions.length} ${quiz.questions.length === 1 ? "pergunta" : "perguntas"}`;

  const best = Math.max(1, ...ranked.map((entry) => entry.total));
  const list = document.getElementById("rk-list");

  ranked.forEach((entry) => {
    const item = document.createElement("li");
    item.className = "rank__row";

    const fill = document.createElement("span");
    fill.className = "rank__fill";
    fill.style.width = `${(entry.total / best) * 100}%`;

    const pos = document.createElement("span");
    pos.className = "rank__pos";
    pos.textContent = Game.medalFor(entry.position);

    const name = document.createElement("span");
    name.className = "rank__name";
    name.textContent = entry.name;

    const pts = document.createElement("span");
    pts.className = "rank__pts";
    pts.textContent = `${entry.total} pts`;

    item.append(fill, pos, name, pts);
    list.appendChild(item);
  });
})();
