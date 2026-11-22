/* Editor de quiz: serve para criar (sem ?id) e para editar (?id=...) */
(async () => {
  const user = Shell.requireUser();
  if (!user) return;
  Shell.setupNavbar(user);

  const LIST_PAGE = "meus-quizzes.html";
  const MIN_OPTIONS = 2;
  const MAX_OPTIONS = 26; // A–Z
  const LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  const form = document.getElementById("quiz-form");
  const titleInput = document.getElementById("quiz-title");
  const descriptionInput = document.getElementById("quiz-description");
  const questionsEl = document.getElementById("questions");
  const addQuestionBtn = document.getElementById("add-question");
  const saveBtn = document.getElementById("save-btn");
  const formError = document.getElementById("form-error");
  const pageTitle = document.getElementById("page-title");

  // ---------- Modo criar x editar ----------
  const editId = new URLSearchParams(window.location.search).get("id");
  let editing = null;

  if (editId) {
    editing = await QuizStore.get(editId, user.id);
    if (!editing) {
      window.location.replace(LIST_PAGE);
      return;
    }
    pageTitle.textContent = "Editar Quiz";
    document.title = "Editar quiz — Queerz";
  }

  // ---------- Helpers de DOM ----------
  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function showError(message) {
    formError.textContent = message;
    formError.hidden = false;
    formError.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  function clearError() {
    formError.hidden = true;
    formError.textContent = "";
    titleInput.removeAttribute("aria-invalid");
  }

  // ---------- Alternativas ----------
  function setCorrect(row, correct) {
    row.classList.toggle("is-correct", correct);
    const check = row.querySelector(".option__check");
    check.setAttribute("aria-checked", String(correct));
    check.textContent = correct ? "✓" : "";
  }

  function buildOption(option = {}) {
    const row = el("div", "option");
    row.dataset.optionId = option.id || QuizStore.newId();

    const check = el("button", "option__check");
    check.type = "button";
    check.setAttribute("role", "checkbox");
    check.setAttribute("aria-label", "Marcar como correta");
    // Pode haver mais de uma alternativa correta
    check.addEventListener("click", () => setCorrect(row, !row.classList.contains("is-correct")));

    const letter = el("span", "option__letter");

    const input = el("input", "option__input");
    input.type = "text";
    input.maxLength = 200;
    input.autocomplete = "off";
    input.value = option.text || "";

    row.append(check, letter, input);
    setCorrect(row, Boolean(option.correct));
    return row;
  }

  /** Recalcula as letras (A, B, C...) e os placeholders de uma pergunta. */
  function refreshOptions(card) {
    const rows = card.querySelectorAll(".option");
    rows.forEach((row, index) => {
      const letter = LETTERS[index];
      row.querySelector(".option__letter").textContent = `${letter}.`;
      row.querySelector(".option__input").placeholder = `Alternativa ${letter}`;
    });
    card.querySelector(".question__add-option").hidden = rows.length >= MAX_OPTIONS;
  }

  // ---------- Perguntas ----------
  function renumberQuestions() {
    questionsEl.querySelectorAll(".question").forEach((card, index) => {
      card.querySelector(".pill").textContent = `Pergunta ${index + 1}`;
    });
  }

  function buildQuestion(question = {}) {
    const card = el("section", "card question");
    card.dataset.questionId = question.id || QuizStore.newId();

    const head = el("div", "question__head");
    const pill = el("span", "pill");
    const removeBtn = el("button", "link-btn link-btn--muted", "✕ Remover");
    removeBtn.type = "button";
    removeBtn.addEventListener("click", () => {
      card.remove();
      renumberQuestions();
    });
    head.append(pill, removeBtn);

    const textarea = el("textarea", "field__input field__textarea");
    textarea.placeholder = "Escreva a pergunta aqui...";
    textarea.maxLength = 300;
    textarea.rows = 2;
    textarea.value = question.text || "";

    const block = el("div", "question__options");
    block.appendChild(
      el("p", "question__hint", "Alternativas — clique no círculo para marcar a correta")
    );

    const options = question.options && question.options.length
      ? question.options
      : Array.from({ length: MIN_OPTIONS }, () => ({}));
    options.forEach((option) => block.appendChild(buildOption(option)));

    const addOptionBtn = el("button", "link-btn link-btn--primary question__add-option", "+ Adicionar alternativa");
    addOptionBtn.type = "button";
    addOptionBtn.addEventListener("click", () => {
      const rows = block.querySelectorAll(".option");
      if (rows.length >= MAX_OPTIONS) return;
      const row = buildOption();
      block.appendChild(row);
      refreshOptions(card);
      row.querySelector(".option__input").focus();
    });

    card.append(head, textarea, block, addOptionBtn);
    return card;
  }

  function addQuestion(question, { focus = false } = {}) {
    const card = buildQuestion(question);
    questionsEl.appendChild(card);
    refreshOptions(card);
    renumberQuestions();
    if (focus) {
      card.scrollIntoView({ behavior: "smooth", block: "center" });
      card.querySelector("textarea").focus({ preventScroll: true });
    }
  }

  // ---------- Leitura e validação ----------
  /** Lê o formulário. Alternativas em branco são descartadas. */
  function collect() {
    const questions = [...questionsEl.querySelectorAll(".question")].map((card) => ({
      id: card.dataset.questionId,
      text: card.querySelector("textarea").value.trim(),
      options: [...card.querySelectorAll(".option")]
        .map((row) => ({
          id: row.dataset.optionId,
          text: row.querySelector(".option__input").value.trim(),
          correct: row.classList.contains("is-correct"),
        }))
        .filter((option) => option.text),
    }));

    return {
      title: titleInput.value.trim(),
      description: descriptionInput.value.trim(),
      questions,
    };
  }

  /** Retorna uma mensagem de erro ou null se estiver tudo certo. */
  function validate(data) {
    if (!data.title) {
      titleInput.setAttribute("aria-invalid", "true");
      titleInput.focus();
      return "Dê um título ao quiz.";
    }

    if (data.questions.length === 0) {
      return "Adicione pelo menos uma pergunta.";
    }

    for (let i = 0; i < data.questions.length; i += 1) {
      const question = data.questions[i];
      const label = `Pergunta ${i + 1}`;

      if (!question.text) return `${label}: escreva o enunciado.`;
      if (question.options.length < MIN_OPTIONS) {
        return `${label}: preencha pelo menos ${MIN_OPTIONS} alternativas.`;
      }
      if (!question.options.some((option) => option.correct)) {
        return `${label}: marque pelo menos uma alternativa correta.`;
      }
    }

    return null;
  }

  // ---------- Eventos ----------
  addQuestionBtn.addEventListener("click", () => addQuestion(undefined, { focus: true }));

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearError();

    const data = collect();
    const problem = validate(data);
    if (problem) {
      showError(problem);
      return;
    }

    saveBtn.disabled = true;
    saveBtn.textContent = "Salvando...";

    try {
      await QuizStore.save(editing ? { ...data, id: editing.id } : data, user.id);
      window.location.href = LIST_PAGE;
    } catch (error) {
      showError(error.message);
      saveBtn.disabled = false;
      saveBtn.textContent = "Salvar Quiz";
    }
  });

  // ---------- Estado inicial ----------
  if (editing) {
    titleInput.value = editing.title;
    descriptionInput.value = editing.description || "";
    editing.questions.forEach((question) => addQuestion(question));
  }
})();
