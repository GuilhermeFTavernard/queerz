package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import br.edu.ufersa.queerz.demo.autenticar.dto.TokenResponse;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
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

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado! Faça login.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String telaLogin(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest("", ""));
        }
        return "login";
    }

    @PostMapping("/login")
    public String realizarLogin(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.senha());
            Authentication authentication = authenticationManager.authenticate(authToken);

            Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
            String tokenJWT = tokenService.gerarToken(usuarioAutenticado);
            TokenResponse tokenResponse = TokenResponse.bearer(tokenJWT, 7200);

            redirectAttributes.addFlashAttribute("tokenResponse", tokenResponse);
            return "redirect:/home";

        } catch (AuthenticationException ex) {
            model.addAttribute("erroLogin", "E-mail ou senha incorretos.");
            return "login";
        }
    }
}