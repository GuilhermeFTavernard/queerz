package br.edu.ufersa.queerz.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/telaCadastro")
public class cadastroController {


    @PostMapping
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha) {

        System.out.println(nome);
        System.out.println(email);
        System.out.println(senha);

        return "login";

    }

}
