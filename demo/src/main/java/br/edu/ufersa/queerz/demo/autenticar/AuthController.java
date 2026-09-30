package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/cadastro")
    public String telaCadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha,
                            RedirectAttributes redirectAttributes) {
        // (hoje não salva nada)
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado! Faça login.");
        return "redirect:/login";
    }

    @ResponseBody
    @GetMapping("/login")
    public String telaLogin(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest("", ""));
        }
        return "HELLO WORLD";
    }

    @PostMapping("/login")
    public String realizarLogin(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                                BindingResult result,
                                HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse,
                                Model model) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));

            // evita fixação de sessão: troca o id se já existia uma sessão
            if (httpRequest.getSession(false) != null) {
                httpRequest.changeSessionId();
            }

            // grava o usuário logado na HttpSession
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return "redirect:/home";

        } catch (AuthenticationException ex) {
            model.addAttribute("erroLogin", "E-mail ou senha incorretos.");
            return "login";
        }
    }
}