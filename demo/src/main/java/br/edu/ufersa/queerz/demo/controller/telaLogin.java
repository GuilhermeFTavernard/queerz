package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login")
public class telaLogin {

        @PostMapping("/login")
        public String realizarLogin(
                @RequestParam String email,
                @RequestParam String senha) {

            /*Aqui seria a validação*/
            System.out.println("Email: " + email);
            System.out.println("Senha: " + senha);

            return "home";
        }
    }
}
