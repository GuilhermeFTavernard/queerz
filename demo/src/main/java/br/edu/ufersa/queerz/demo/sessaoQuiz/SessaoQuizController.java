package br.edu.ufersa.queerz.demo.sessaoQuiz;

import br.edu.ufersa.queerz.demo.sessaoQuiz.SessaoQuiz;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sessao")
public class SessaoQuizController {
    @GetMapping("/criar")
    public String criarSessao() {
        return "criarSessao";
    }

    @PostMapping("/criar")
    public String salvarSessao(SessaoQuiz sessao) {

        System.out.println("Código: " + sessao.getCodigo());
        System.out.println("Status: " + sessao.getStatus());

        return "sessao";
    }

    @GetMapping("/entrar")
    public String telaEntrar() {
        return "entrarSessao";
    }

    @PostMapping("/entrar")
    public String entrar(@RequestParam String codigo) {

        System.out.println("Código informado: " + codigo);

        return "sessao";
    }
}
