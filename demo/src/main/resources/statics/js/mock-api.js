/* ==========================================================
   Queerz — "back-end" mocado
   Guarda usuários e sessão no localStorage e imita uma API
   assíncrona. Quando o back-end real existir, basta trocar o
   corpo de register() e login() por chamadas fetch().

   ATENÇÃO: a senha fica em texto puro apenas porque isto é um
   mock de desenvolvimento. Nunca faça isso em produção.
   ========================================================== */

const MockApi = (() => {
  const USERS_KEY = "queerz:users";
  const SESSION_KEY = "queerz:session";
  const LATENCY_MS = 400;

  function readUsers() {
    try {
      return JSON.parse(localStorage.getItem(USERS_KEY)) || [];
    } catch {
      return [];
    }
  }

  function writeUsers(users) {
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  }

  function normalizeEmail(email) {
    return String(email).trim().toLowerCase();
  }

  function respond(fn) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        try {
          resolve(fn());
        } catch (error) {
          reject(error);
        }
      }, LATENCY_MS);
    });
  }

  /** Cria uma conta. Rejeita se o e-mail já estiver cadastrado. */
  function register({ name, email, password }) {
    return respond(() => {
      const users = readUsers();
      const normalized = normalizeEmail(email);

      if (users.some((user) => user.email === normalized)) {
        throw new Error("Já existe uma conta com este e-mail.");
      }

      const user = {
        id: Date.now(),
        name: String(name).trim(),
        email: normalized,
        password,
      };
      users.push(user);
      writeUsers(users);

      return { id: user.id, name: user.name, email: user.email };
    });
  }

  /** Valida as credenciais e abre a sessão. */
  function login({ email, password }) {
    return respond(() => {
      const normalized = normalizeEmail(email);
      const user = readUsers().find(
        (item) => item.email === normalized && item.password === password
      );

      if (!user) {
        throw new Error("E-mail ou senha incorretos.");
      }

      const session = { id: user.id, name: user.name, email: user.email };
      localStorage.setItem(SESSION_KEY, JSON.stringify(session));
      return session;
    });
  }

  function currentUser() {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY));
    } catch {
      return null;
    }
  }

  function logout() {
    localStorage.removeItem(SESSION_KEY);
  }

  return { register, login, currentUser, logout };
})();
