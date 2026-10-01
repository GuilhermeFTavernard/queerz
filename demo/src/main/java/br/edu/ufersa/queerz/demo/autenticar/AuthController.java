package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import br.edu.ufersa.queerz.demo.shared.exception.InvalidCredentialsException;
import br.edu.ufersa.queerz.demo.usuario.UsuarioApplicationService;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final UsuarioApplicationService usuarioApplicationService;

    public AuthController(AuthApplicationService authApplicationService,
                          UsuarioApplicationService usuarioApplicationService) {
        this.authApplicationService = authApplicationService;
        this.usuarioApplicationService = usuarioApplicationService;
    }

    // ---------------- Cadastro ----------------

    @GetMapping("/cadastro")
    public String telaCadastro(HttpServletRequest request, Model model) {
        if (estaLogado(request)) {
            return "redirect:/home";
        }
        if (!model.containsAttribute("usuarioRequest")) {
            model.addAttribute("usuarioRequest", new UsuarioRequest("", "", ""));
        }
        return "cadastro";
    }

    @ResponseBody
    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("usuarioRequest") UsuarioRequest request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {

        // Erros de validação: volta ao formulário mostrando as mensagens de cada campo
        if (result.hasErrors()) {
            return "cadastro";
        }

        try {
            usuarioApplicationService.cadastrar(request);
        } catch (DuplicateResourceException ex) {
            // E-mail já usado: aponta o erro no próprio campo, sem ir para a página de erro 409
            result.rejectValue("email", "email.duplicado", ex.getMessage());
            return "cadastro";
        }

        // Post/Redirect/Get: evita reenvio do formulário ao atualizar a página
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado! Faça login.");
        return "Conta Criada!";
    }

    // ---------------- Login ----------------


    @GetMapping("/login")
    public String telaLogin(@RequestParam(name = "logout", required = false) String logout,
                            HttpServletRequest request,
                            Model model) {
        if (estaLogado(request)) {
            return "redirect:/home";
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest("", ""));
        }
        // SecurityConfig redireciona para /login?logout depois de sair
        if (logout != null) {
            model.addAttribute("mensagemSucesso", "Você saiu da conta.");
        }
        return "login";
    }

    @ResponseBody
    @PostMapping("/login")
    public String realizarLogin(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                                BindingResult result,
                                HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse,
                                Model model) {

        if (result.hasErrors()) {
            return "login1";
        }

        try {
            // Confere as credenciais e grava o usuário logado na HttpSession
            authApplicationService.login(request, httpRequest, httpResponse);
            return "/home";
        } catch (InvalidCredentialsException ex) {
            // Mesma mensagem para e-mail inexistente e senha errada (não revela qual falhou)
            model.addAttribute("erroLogin", ex.getMessage());
            return "login";
        }
    }

    // ---------------- Helper ----------------

    private boolean estaLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                && session.getAttribute(UsuarioApplicationService.ATTR_USUARIO_LOGADO) != null;
    }
}