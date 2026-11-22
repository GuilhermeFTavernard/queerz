/* Tela de Meus quizzes */
(async () => {
  const user = Shell.requireUser();
  if (!user) return;
  Shell.setupNavbar(user);

  const pageHead = document.getElementById("page-head");
  const countEl = document.getElementById("quiz-count");
  const emptyState = document.getElementById("empty-state");
  const listEl = document.getElementById("quiz-list");

  function pluralize(count, singular, plural) {
    return `${count} ${count === 1 ? singular : plural}`;
  }

  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function buildCard(quiz) {
    const card = el("article", "card quiz-card");

    // Informações
    const info = el("div", "quiz-card__info");
    info.appendChild(el("h2", "quiz-card__title", quiz.title));
    if (quiz.description) {
      info.appendChild(el("p", "quiz-card__description", quiz.description));
    }

    const meta = el("div", "quiz-card__meta");
    meta.appendChild(
      el("span", "chip chip--questions", pluralize(quiz.questions.length, "pergunta", "perguntas"))
    );
    meta.appendChild(
      el("span", "chip chip--responses", pluralize(quiz.responsesCount, "resposta", "respostas"))
    );
    meta.appendChild(
      el("span", "quiz-card__date", new Date(quiz.createdAt).toLocaleDateString("pt-BR"))
    );
    info.appendChild(meta);

    // Ações
    const actions = el("div", "quiz-card__actions");

    const editLink = el("a", "btn btn--soft");
    editLink.href = `editor.html?id=${encodeURIComponent(quiz.id)}`;
    editLink.appendChild(Shell.icon("icon-edit.svg", 17, 17));
    editLink.appendChild(document.createTextNode("Editar"));

    const deleteBtn = el("button", "btn btn--danger");
    deleteBtn.type = "button";
    deleteBtn.title = "Excluir quiz";
    deleteBtn.setAttribute("aria-label", `Excluir o quiz ${quiz.title}`);
    deleteBtn.appendChild(Shell.icon("icon-bin.svg", 17, 21));
    deleteBtn.addEventListener("click", async () => {
      if (!window.confirm(`Excluir o quiz "${quiz.title}"? Essa ação não pode ser desfeita.`)) return;
      await QuizStore.remove(quiz.id, user.id);
      Shell.toast("Quiz excluído.");
      render();
    });

    actions.append(...Shell.shareButtons(quiz), editLink, deleteBtn);
    card.append(info, actions);
    return card;
  }

  async function render() {
    const quizzes = await QuizStore.list(user.id);

    countEl.textContent = `${quizzes.length} ${quizzes.length === 1 ? "quiz criado" : "quizzes criados"}`;
    pageHead.classList.toggle("is-empty", quizzes.length === 0);
    emptyState.hidden = quizzes.length > 0;
    listEl.hidden = quizzes.length === 0;

    listEl.replaceChildren(...quizzes.map(buildCard));
    IconFallback.apply(listEl);
  }

  render();
})();
