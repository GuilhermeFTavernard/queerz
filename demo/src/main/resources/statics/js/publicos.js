/* Tela de Quizzes públicos (aberta também para quem não está logado) */
(async () => {
  const user = MockApi.currentUser();

  // Navbar: muda conforme exista alguém logado
  if (user) {
    Shell.setupNavbar(user);
    ["user-name", "my-quizzes-link", "logout-btn"].forEach((id) => {
      document.getElementById(id).hidden = false;
    });
  } else {
    ["login-link", "signup-link"].forEach((id) => {
      document.getElementById(id).hidden = false;
    });
  }

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

    const actions = el("div", "quiz-card__actions");
    const join = el("a", "btn btn--soft", "Participe");
    join.href = `quiz.html?codigo=${encodeURIComponent(quiz.code)}`;
    actions.append(...Shell.shareButtons(quiz), join);

    card.append(info, actions);
    return card;
  }

  async function render() {
    const quizzes = await QuizStore.listAll();
    countEl.textContent = `${quizzes.length} ${quizzes.length === 1 ? "quiz criado" : "quizzes criados"}`;
    emptyState.hidden = quizzes.length > 0;
    listEl.hidden = quizzes.length === 0;
    listEl.replaceChildren(...quizzes.map(buildCard));
    IconFallback.apply(listEl);
  }

  // ---------- Janela "Entrar com código" ----------
  const dialog = document.getElementById("code-dialog");
  const form = document.getElementById("code-form");
  const input = document.getElementById("code-input");
  const errorEl = document.getElementById("code-error");

  function setError(message) {
    errorEl.textContent = message || "";
    errorEl.hidden = !message;
    input.setAttribute("aria-invalid", message ? "true" : "false");
  }

  document.getElementById("open-code").addEventListener("click", () => {
    input.value = "";
    setError("");
    dialog.showModal();
    input.focus();
  });

  document.getElementById("code-cancel").addEventListener("click", () => dialog.close());

  // Clicar fora da janela também fecha
  dialog.addEventListener("click", (event) => {
    if (event.target === dialog) dialog.close();
  });

  // Só dígitos
  input.addEventListener("input", () => {
    input.value = input.value.replace(/\D/g, "").slice(0, 6);
    setError("");
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const code = input.value.trim();

    if (code.length !== 6) {
      setError("O código tem 6 dígitos.");
      return;
    }

    const quiz = await QuizStore.getByCode(code);
    if (!quiz) {
      setError("Nenhum quiz encontrado com esse código.");
      return;
    }

    window.location.href = `quiz.html?codigo=${encodeURIComponent(quiz.code)}`;
  });

  render();
})();
