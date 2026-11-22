/* ==========================================================
   Queerz — quizzes mocados
   Guarda os quizzes de cada usuário no localStorage.
   Todas as funções retornam Promises para que, no futuro, cada
   uma possa virar uma chamada fetch() sem mudar quem as usa.

   Formato de um quiz:
   {
     id, ownerId, code, title, description,
     questions: [{ id, text, options: [{ id, text, correct }] }],
     responsesCount, createdAt, updatedAt
   }
   ========================================================== */

const QuizStore = (() => {
  const KEY = "queerz:quizzes";

  function readAll() {
    try {
      return JSON.parse(localStorage.getItem(KEY)) || [];
    } catch {
      return [];
    }
  }

  function writeAll(quizzes) {
    localStorage.setItem(KEY, JSON.stringify(quizzes));
  }

  function newId() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
      return window.crypto.randomUUID();
    }
    return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
  }

  /** Código de 6 dígitos que identifica o quiz no link público. */
  function newCode(all) {
    let code;
    do {
      code = String(Math.floor(100000 + Math.random() * 900000));
    } while (all.some((quiz) => quiz.code === code));
    return code;
  }

  /** Quizzes do usuário, do mais novo para o mais antigo. */
  function list(ownerId) {
    const mine = readAll()
      .filter((quiz) => quiz.ownerId === ownerId)
      .sort((a, b) => b.createdAt - a.createdAt);
    return Promise.resolve(mine);
  }

  function get(id, ownerId) {
    const quiz = readAll().find((item) => item.id === id && item.ownerId === ownerId);
    return Promise.resolve(quiz || null);
  }

  /** Cria (sem id) ou atualiza (com id) um quiz do usuário. */
  function save(data, ownerId) {
    const all = readAll();
    const now = Date.now();

    if (data.id) {
      const index = all.findIndex((item) => item.id === data.id && item.ownerId === ownerId);
      if (index === -1) return Promise.reject(new Error("Quiz não encontrado."));

      all[index] = {
        ...all[index],
        title: data.title,
        description: data.description,
        questions: data.questions,
        updatedAt: now,
      };
      writeAll(all);
      return Promise.resolve(all[index]);
    }

    const quiz = {
      id: newId(),
      ownerId,
      code: newCode(all),
      title: data.title,
      description: data.description,
      questions: data.questions,
      responsesCount: 0,
      createdAt: now,
      updatedAt: now,
    };
    all.push(quiz);
    writeAll(all);
    return Promise.resolve(quiz);
  }

  function remove(id, ownerId) {
    const all = readAll();
    const remaining = all.filter((item) => !(item.id === id && item.ownerId === ownerId));
    writeAll(remaining);
    return Promise.resolve(remaining.length !== all.length);
  }

  /** Todos os quizzes (públicos), do mais novo para o mais antigo. */
  function listAll() {
    return Promise.resolve(readAll().sort((a, b) => b.createdAt - a.createdAt));
  }

  /** Busca um quiz pelo código de 6 dígitos (qualquer dono). */
  function getByCode(code) {
    const clean = String(code || "").trim();
    const quiz = readAll().find((item) => item.code === clean);
    return Promise.resolve(quiz || null);
  }

  /** Atualiza o total de respostas de um quiz. */
  function setResponsesCount(id, count) {
    const all = readAll();
    const index = all.findIndex((item) => item.id === id);
    if (index === -1) return Promise.resolve(false);
    all[index] = { ...all[index], responsesCount: count };
    writeAll(all);
    return Promise.resolve(true);
  }

  return { list, listAll, get, getByCode, save, remove, setResponsesCount, newId };
})();
