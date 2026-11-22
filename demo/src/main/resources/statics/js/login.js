/* Tela de login */
(() => {
  const form = document.getElementById("login-form");
  const submitBtn = document.getElementById("submit-btn");
  const formError = document.getElementById("form-error");
  const formSuccess = document.getElementById("form-success");

  const fields = {
    email: document.getElementById("email"),
    password: document.getElementById("password"),
  };

  const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  // Veio do cadastro: avisa que a conta foi criada
  if (new URLSearchParams(window.location.search).get("cadastro") === "ok") {
    formSuccess.textContent = "Conta criada! Entre com seu e-mail e senha para testar.";
    formSuccess.hidden = false;
  }

  function setFieldError(name, message) {
    const input = fields[name];
    const output = document.getElementById(`${name}-error`);
    input.setAttribute("aria-invalid", message ? "true" : "false");
    output.textContent = message || "";
    output.hidden = !message;
  }

  function clearErrors() {
    Object.keys(fields).forEach((name) => setFieldError(name, ""));
    formError.hidden = true;
    formError.textContent = "";
  }

  function validate() {
    let valid = true;

    if (!EMAIL_PATTERN.test(fields.email.value.trim())) {
      setFieldError("email", "Informe um e-mail válido.");
      valid = false;
    }

    if (!fields.password.value) {
      setFieldError("password", "Informe sua senha.");
      valid = false;
    }

    return valid;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearErrors();
    formSuccess.hidden = true;

    if (!validate()) return;

    submitBtn.disabled = true;
    submitBtn.textContent = "Entrando...";

    try {
      await MockApi.login({
        email: fields.email.value,
        password: fields.password.value,
      });

      // Login válido: segue para a tela de Meus quizzes
      window.location.href = "meus-quizzes.html";
      return;
    } catch (error) {
      formError.textContent = error.message;
      formError.hidden = false;
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Entrar";
    }
  });
})();
