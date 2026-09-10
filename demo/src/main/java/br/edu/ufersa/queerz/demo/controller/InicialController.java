package br.edu.ufersa.queerz.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/telaInicial")
public class InicialController {
    
    @GetMapping
    public String inicial() {
        return "telaInicial";
    }

    @GetMapping("/sign-up")
    public String cadastrar(){
        return  "cadastro";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/quiz")
    public String quiz(){
        return "quiz";
    }

    @GetMapping("/sair")
    public String sair(){
        return "login";
    }
}
