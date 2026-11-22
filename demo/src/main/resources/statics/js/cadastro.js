/* Tela de criar conta */
(() => {
  const form = document.getElementById("register-form");
  const submitBtn = document.getElementById("submit-btn");
  const formError = document.getElementById("form-error");

  const fields = {
    name: document.getElementById("name"),
    email: document.getElementById("email"),
    password: document.getElementById("password"),
  };

  const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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

    if (!fields.name.value.trim()) {
      setFieldError("name", "Informe seu nome.");
      valid = false;
    }

    if (!EMAIL_PATTERN.test(fields.email.value.trim())) {
      setFieldError("email", "Informe um e-mail válido.");
      valid = false;
    }

    if (fields.password.value.length < 6) {
      setFieldError("password", "A senha precisa ter no mínimo 6 caracteres.");
      valid = false;
    }

    return valid;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearErrors();

    if (!validate()) return;

    submitBtn.disabled = true;
    submitBtn.textContent = "Criando...";

    try {
      await MockApi.register({
        name: fields.name.value,
        email: fields.email.value,
        password: fields.password.value,
      });

      // Conta criada: segue para o login para testar a conta nova
      window.location.href = "login.html?cadastro=ok";
    } catch (error) {
      formError.textContent = error.message;
      formError.hidden = false;
      submitBtn.disabled = false;
      submitBtn.textContent = "Criar conta";
    }
  });
})();
