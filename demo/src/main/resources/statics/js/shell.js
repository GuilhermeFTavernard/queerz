/* ==========================================================
   Queerz — casca das telas logadas
   Proteção de rota, navbar com o nome do usuário e avisos.
   ========================================================== */

const Shell = (() => {
  /** Retorna o usuário logado ou manda para o login. */
  function requireUser() {
    const user = MockApi.currentUser();
    if (!user) {
      window.location.replace("login.html");
      return null;
    }
    return user;
  }

  /** Preenche "Olá, Fulano" e liga o botão Sair. */
  function setupNavbar(user) {
    const nameEl = document.getElementById("user-name");
    if (nameEl) nameEl.textContent = `Olá, ${user.name}`;

    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => {
        MockApi.logout();
        window.location.href = "login.html";
      });
    }
  }

  let toastTimer = null;

  /** Aviso rápido no rodapé da tela. */
  function toast(message) {
    let el = document.getElementById("toast");
    if (!el) {
      el = document.createElement("div");
      el.id = "toast";
      el.className = "toast";
      el.setAttribute("role", "status");
      document.body.appendChild(el);
    }
    el.textContent = message;
    el.classList.add("is-visible");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("is-visible"), 2200);
  }

  /** <img> de ícone; se o arquivo não existir, vira um "?" (ver icons.js). */
  function icon(file, width, height) {
    const img = document.createElement("img");
    img.className = "btn__icon";
    img.setAttribute("data-icon", "");
    img.src = `assets/${file}`;
    img.width = width;
    img.height = height;
    img.alt = "";
    return img;
  }

  /** Link público do quiz. */
  function quizLink(quiz) {
    return new URL(`quiz.html?codigo=${quiz.code}`, window.location.href).href;
  }

  /** Cópia "à moda antiga": funciona em navegadores/endereços sem a API moderna. */
  function legacyCopy(text) {
    const area = document.createElement("textarea");
    area.value = text;
    area.setAttribute("readonly", "");
    area.style.cssText = "position:fixed;top:0;left:-9999px;opacity:0;";
    document.body.appendChild(area);

    const previous = document.activeElement;
    area.focus();
    area.select();
    area.setSelectionRange(0, text.length);

    let ok = false;
    try {
      ok = document.execCommand("copy");
    } catch {
      ok = false;
    }
    area.remove();
    if (previous && typeof previous.focus === "function") previous.focus();
    return ok;
  }

  async function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(text);
        return true;
      } catch {
        // cai para o método antigo
      }
    }
    return legacyCopy(text);
  }

  /** Botões "código/link" e "Ranking", iguais em Meus quizzes e Quizzes públicos. */
  function shareButtons(quiz) {
    const linkBtn = document.createElement("button");
    linkBtn.className = "btn btn--soft";
    linkBtn.type = "button";
    linkBtn.title = "Copiar link do quiz";
    linkBtn.append(icon("icon-link.svg", 19, 19), document.createTextNode(quiz.code));
    linkBtn.addEventListener("click", async () => {
      const link = quizLink(quiz);
      const copied = await copyText(link);
      if (copied) {
        toast("Link do quiz copiado!");
      } else {
        // Plano B: mostra o link já selecionado para copiar com Ctrl+C
        window.prompt("Não consegui copiar automaticamente. Copie o link abaixo (Ctrl+C):", link);
      }
    });

    const rankingLink = document.createElement("a");
    rankingLink.className = "btn btn--soft";
    rankingLink.href = `ranking.html?codigo=${encodeURIComponent(quiz.code)}`;
    rankingLink.append(icon("icon-ranking.svg", 14, 16), document.createTextNode("Ranking"));

    return [linkBtn, rankingLink];
  }

  return { requireUser, setupNavbar, toast, icon, quizLink, copyText, shareButtons };
})();
