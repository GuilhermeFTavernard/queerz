/* Jornada de quem responde o quiz.
   Telas: nome → pergunta → gráfico de respostas → pontos e ranking
   → (próxima pergunta ...) → ranking final. */
(async () => {
  const code = new URLSearchParams(window.location.search).get("codigo");
  const quiz = await QuizStore.getByCode(code);

  const viewNotFound = document.getElementById("view-notfound");
  const viewIntro = document.getElementById("view-intro");
  const viewGame = document.getElementById("view-game");

  if (!quiz || quiz.questions.length === 0) {
    viewNotFound.hidden = false;
    return;
  }

  document.title = `${quiz.title} — Queerz`;

  const total = quiz.questions.length;
  const LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  const nameForm = document.getElementById("name-form");
  const nameInput = document.getElementById("player-name");
  const startBtn = document.getElementById("start-btn");
  const headerName = document.getElementById("g-name");
  const headerPill = document.getElementById("g-pill");
  const progressBar = document.getElementById("g-bar");
  const main = document.getElementById("g-main");

  // Estado da partida
  let playerName = "";
  let index = 0;
  let selectedId = null;
  let startedAt = 0;
  let answers = [];

  // ---------- Helpers ----------
  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function plural(count, singular, pluralText) {
    return `${count} ${count === 1 ? singular : pluralText}`;
  }

  function me() {
    return { name: playerName, answers };
  }

  function setHeader(current) {
    headerName.textContent = playerName;
    headerPill.textContent = `${current} / ${total}`;
    progressBar.style.width = `${(current / total) * 100}%`;
  }

  function show(...nodes) {
    main.replaceChildren(...nodes);
    window.scrollTo({ top: 0 });
  }

  function questionHead(question) {
    return [
      el("span", "q-tag", `Pergunta ${index + 1}`),
      el("h1", "q-title", question.text),
    ];
  }

  // ---------- 1. Nome ----------
  function showIntro() {
    document.getElementById("intro-title").textContent = quiz.title;
    document.getElementById("intro-people").textContent = plural(
      Game.participantsCount(quiz),
      "participante",
      "participantes"
    );
    document.getElementById("intro-questions").textContent = plural(total, "pergunta", "perguntas");

    viewGame.hidden = true;
    viewIntro.hidden = false;
    nameInput.value = playerName;
    startBtn.disabled = !nameInput.value.trim();
    nameInput.focus();
  }

  nameInput.addEventListener("input", () => {
    startBtn.disabled = !nameInput.value.trim();
  });

  nameForm.addEventListener("submit", (event) => {
    event.preventDefault();
    const typed = nameInput.value.trim();
    if (!typed) return;

    playerName = typed;
    index = 0;
    answers = [];
    viewIntro.hidden = true;
    viewGame.hidden = false;
    showQuestion();
  });

  // ---------- 2. Pergunta ----------
  function showQuestion() {
    const question = quiz.questions[index];
    selectedId = null;
    setHeader(index + 1);

    const list = el("div", "q-options");
    question.options.forEach((option, i) => {
      const button = el("button", "q-option");
      button.type = "button";
      button.setAttribute("aria-pressed", "false");
      button.dataset.optionId = option.id;
      button.append(
        el("span", "q-option__letter", LETTERS[i]),
        el("span", "q-option__text", option.text)
      );
      button.addEventListener("click", () => {
        selectedId = option.id;
        list.querySelectorAll(".q-option").forEach((item) => {
          const active = item === button;
          item.classList.toggle("is-selected", active);
          item.setAttribute("aria-pressed", String(active));
        });
        okBtn.disabled = false;
      });
      list.appendChild(button);
    });

    const okBtn = el("button", "g-btn", "OK");
    okBtn.type = "button";
    okBtn.disabled = true;
    okBtn.addEventListener("click", confirmAnswer);

    const actions = el("div", "g-actions");
    actions.appendChild(okBtn);

    const wrapper = el("div", "g-main");
    wrapper.append(...questionHead(question), list, actions);
    main.replaceChildren(wrapper);
    window.scrollTo({ top: 0 });

    startedAt = performance.now();
  }

  function confirmAnswer() {
    if (!selectedId) return;

    const question = quiz.questions[index];
    const option = question.options.find((item) => item.id === selectedId);
    const timeMs = Math.round(performance.now() - startedAt);
    const correct = Boolean(option.correct);

    answers.push({
      questionId: question.id,
      optionId: option.id,
      correct,
      timeMs,
      points: Game.pointsFor(correct, timeMs),
    });

    showChart();
  }

  // ---------- 3. Gráfico: quantos escolheram cada alternativa ----------
  function niceMax(value) {
    return Math.max(4, Math.ceil(value / 4) * 4);
  }

  function showChart() {
    const question = quiz.questions[index];
    const mine = answers[answers.length - 1];
    const { counts, correct, wrong } = Game.distribution(quiz, index, me());
    const top = niceMax(Math.max(...Object.values(counts)));

    // Resumo: acertou/errou e quantas pessoas acertaram e erraram
    const summary = el("div", "summary");
    summary.append(
      el(
        "span",
        `summary__verdict summary__verdict--${mine.correct ? "right" : "wrong"}`,
        mine.correct ? "✓ Você acertou!" : "✗ Você errou"
      ),
      el("span", "summary__count", `${plural(correct, "pessoa acertou", "pessoas acertaram")} · ${plural(wrong, "errou", "erraram")}`)
    );

    // Eixo Y
    const axis = el("div", "chart__axis");
    for (let step = 4; step >= 0; step -= 1) {
      axis.appendChild(el("span", "", String(Math.round((top / 4) * step))));
    }

    // Barras
    const bars = el("div", "chart__bars");
    const labels = el("div", "chart__labels");
    question.options.forEach((option, i) => {
      const count = counts[option.id];
      const col = el("div", `chart__col${option.id === mine.optionId ? " is-mine" : ""}`);
      col.title = `${LETTERS[i]}: ${plural(count, "resposta", "respostas")}`;
      col.append(
        el("span", "chart__value", String(count)),
        Object.assign(el("div", "chart__bar"), { style: `height: ${(count / top) * 88}%` })
      );
      bars.appendChild(col);

      const label = el("div", "chart__label");
      label.appendChild(el("span", "", LETTERS[i]));
      if (option.correct) {
        const check = el("span", "chart__check", "✓");
        check.setAttribute("aria-label", "alternativa correta");
        label.appendChild(check);
      }
      labels.appendChild(label);
    });

    const plot = el("div", "chart__plot");
    plot.append(bars, labels);
    const chart = el("div", "chart");
    chart.append(axis, plot);

    const next = el("button", "g-btn", "Próxima ➜");
    next.type = "button";
    next.addEventListener("click", showScore);
    const actions = el("div", "g-actions");
    actions.appendChild(next);

    const wrapper = el("div", "g-main");
    wrapper.append(...questionHead(question), summary, chart, actions);
    show(wrapper);
  }

  // ---------- 4. Pontos e ranking ----------
  function rankingList(ranked) {
    const rows = Game.visibleRows(ranked, 4);
    const best = Math.max(1, ...rows.map((row) => row.total));
    const list = el("ol", "rank");

    rows.forEach((row) => {
      const item = el("li", `rank__row${row.isMe ? " is-me" : ""}`);
      const fill = el("span", "rank__fill");
      fill.style.width = `${(row.total / best) * 100}%`;

      const name = el("span", "rank__name", row.name);
      if (row.isMe) name.appendChild(el("span", "rank__you", "você"));

      item.append(
        fill,
        el("span", "rank__pos", Game.medalFor(row.position)),
        name,
        el("span", "rank__pts", `${row.total} pts`)
      );
      list.appendChild(item);
    });
    return list;
  }

  function positionBlock(entry) {
    const block = el("div", "position");
    block.appendChild(el("span", "position__label", "Sua posição"));

    const medal = Game.medalFor(entry.position);
    block.appendChild(
      el("span", `position__medal${medal.startsWith("#") ? " position__medal--number" : ""}`, medal)
    );

    const totalLine = el("span", "position__total", "Total: ");
    totalLine.appendChild(el("strong", "", `${entry.total} pts`));
    block.appendChild(totalLine);
    return block;
  }

  function showScore() {
    const mine = answers[answers.length - 1];
    const ranked = Game.standings(quiz, index + 1, me());
    const myEntry = ranked.find((entry) => entry.isMe);
    const isLast = index === total - 1;

    const result = el("div", `result result--${mine.correct ? "right" : "wrong"}`);
    result.append(
      el("div", "result__label", mine.correct ? "✓ Resposta correta!" : "✗ Resposta errada"),
      el("div", "result__points", mine.correct ? `+${mine.points} pts` : "0 pts")
    );
    if (mine.correct) result.appendChild(el("div", "result__note", Game.speedLabel(mine.timeMs)));

    const next = el("button", "g-btn", isLast ? "Ver resultado final ➜" : "Próxima pergunta ➜");
    next.type = "button";
    next.addEventListener("click", () => {
      if (isLast) {
        showFinal();
      } else {
        index += 1;
        showQuestion();
      }
    });
    const actions = el("div", "g-actions");
    actions.appendChild(next);

    const wrapper = el("div", "g-main g-main--narrow");
    wrapper.append(result, positionBlock(myEntry), rankingList(ranked), actions);
    show(wrapper);
  }

  // ---------- 5. Ranking final ----------
  function showFinal() {
    setHeader(total);

    const ranked = Game.standings(quiz, total, me());
    const myEntry = ranked.find((entry) => entry.isMe);
    const hits = answers.filter((answer) => answer.correct).length;

    // Salva a partida e atualiza o total de respostas do quiz
    const attempts = Game.saveAttempt(quiz.id, playerName, answers);
    QuizStore.setResponsesCount(quiz.id, attempts);

    const head = el("div", "final");
    head.append(
      el("h1", "final__name", playerName),
      el("div", "final__msg", Game.finalMessage(hits, total)),
      el("div", "final__hits", `${hits} de ${total} ${total === 1 ? "resposta correta" : "respostas corretas"}`),
      el("div", "final__score", `${myEntry.total} pontos`)
    );

    // Fim do quiz: jogar de novo ou sair
    const again = el("button", "g-btn g-btn--row", "Jogar novamente");
    again.type = "button";
    again.addEventListener("click", showIntro);

    const leave = el("a", "g-btn g-btn--row g-btn--ghost", "Sair do quiz");
    leave.href = "publicos.html";

    const actions = el("div", "g-actions g-actions--row");
    actions.append(again, leave);

    const wrapper = el("div", "g-main g-main--narrow");
    wrapper.append(head, positionBlock(myEntry), rankingList(ranked), actions);
    show(wrapper);
  }

  showIntro();
})();
