package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class JogoController {

    // 12. Jogar um quiz pelo código PIN (Acesso inicial)
    @GetMapping("/jogar")
    public String exibirTelaPin() {
        System.out.println("GET /jogar -> Exibindo tela de inserção de PIN");
        return "jogo/entrar-pin";
    }

    // 12. Validação e redirecionamento para a sessão síncrona
    @PostMapping("/jogar/entrar")
    public String entrarPeloCodigo(@RequestParam("codigoPin") String codigoPin,
                                   @RequestParam("apelido") String apelido,
                                   RedirectAttributes redirectAttributes) {
        System.out.println("POST /jogar/entrar -> Tentando entrar na sala com PIN: " + codigoPin + " | Apelido: " + apelido);

        redirectAttributes.addAttribute("codigo", codigoPin);
        redirectAttributes.addFlashAttribute("apelido", apelido);
        return "redirect:/sala/{codigo}";
    }

    // Exibição da sala síncrona
    @GetMapping("/sala/{codigo}")
    public String salaAoVivo(@PathVariable("codigo") String codigo,
                             @ModelAttribute("apelido") String apelido,
                             Model model) {
        System.out.println("GET /sala/" + codigo + " -> Entrando na sala síncrona com Apelido: " + apelido);
        model.addAttribute("codigo", codigo);
        model.addAttribute("apelido", apelido);
        return "jogo/sala-ao-vivo";
    }

    // 13. Jogar um quiz público de forma individual/assíncrona (Exige login)
    @GetMapping("/quizzes/{id}/jogar")
    public String carregarQuizPublico(@PathVariable("id") Long id, Model model) {
        System.out.println("GET /quizzes/" + id + "/jogar -> Carregando tela de jogo individual para Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "jogo/responder-quiz";
    }

    // 13. Submeter respostas, calcular nota e registrar tentativa
    @PostMapping("/quizzes/{id}/submeter")
    public String submeterQuizPublico(@PathVariable("id") Long id,
                                      @RequestParam Map<String, String> respostas,
                                      RedirectAttributes redirectAttributes) {
        System.out.println("POST /quizzes/" + id + "/submeter -> Respostas submetidas: " + respostas);

        int pontuacaoSimulada = 100;
        redirectAttributes.addFlashAttribute("pontuacao", pontuacaoSimulada);
        return "redirect:/quizzes/" + id + "/resultado";
    }

    // Exibição de pontuação e ranking oficial do quiz
    @GetMapping("/quizzes/{id}/resultado")
    public String exibirResultado(@PathVariable("id") Long id, Model model) {
        System.out.println("GET /quizzes/" + id + "/resultado -> Exibindo resultados do Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "jogo/resultado";
    }
}