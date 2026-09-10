package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    // 1. Exibir tela de cadastro
    @GetMapping("/cadastro")
    public String telaCadastro() {
        return "cadastro";
    }

    // 2. Processar o cadastro
    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha) {
        System.out.println("Cadastro -> Nome: " + nome + ", Email: " + email + ", Senha: " + senha);
        return "redirect:/login"; // Redireciona para o login após cadastrar
    }

    // 3. Exibir tela de login
    @GetMapping("/login")
    public String telaLogin() {
        return "login";
    }

    // 4. Processar o login
    @PostMapping("/login")
    public String realizarLogin(@RequestParam String email,
                                @RequestParam String senha) {
        System.out.println("Login -> Email: " + email + ", Senha: " + senha);
        return "redirect:/home"; // Redireciona para a home após autenticar
    }
}