package br.edu.ufersa.queerz.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/telaInicial")
public class telaInicial {
    
    @GetMapping
    public String telaInicial() {
        return "telaInicial";
    }

    @GetMapping("/telaCadastro")
    public String cadastrar(){
        return  "cadastro";
    }

    @GetMapping("/telaLogin")
    public String login(){
        return "login";
    }
}
