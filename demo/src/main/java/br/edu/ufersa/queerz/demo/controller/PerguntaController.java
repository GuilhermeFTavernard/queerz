package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PerguntaController {

    // Exibir formulário de nova pergunta para um quiz específico
    @GetMapping("/quizzes/{quizId}/perguntas/nova")
    public String formNovaPergunta(@PathVariable("quizId") Long quizId, Model model) {
        System.out.println("GET /quizzes/" + quizId + "/perguntas/nova -> Exibindo form para o Quiz ID: " + quizId);

        model.addAttribute("quizId", quizId);
        return "perguntas/form-pergunta";
    }

    // 8. Criar pergunta
    @PostMapping("/quizzes/{quizId}/perguntas")
    public String criarPergunta(@PathVariable("quizId") Long quizId,
                                @RequestParam("enunciado") String enunciado,
                                @RequestParam("tempoLimite") int tempoLimite) {
        System.out.println("POST /quizzes/" + quizId + "/perguntas");
        System.out.println("Parâmetros recebidos -> Enunciado: " + enunciado + ", Tempo Limite: " + tempoLimite);

        return "redirect:/quizzes/" + quizId + "/gerenciar";
    }

    // Exibir formulário de edição de pergunta existente
    @GetMapping("/perguntas/{id}/editar")
    public String formEditarPergunta(@PathVariable("id") Long id, Model model) {
        System.out.println("GET /perguntas/" + id + "/editar -> Carregando tela de edição da pergunta: " + id);

        model.addAttribute("perguntaId", id);
        return "perguntas/form-pergunta-edicao";
    }

    // 9. Editar pergunta
    @PostMapping("/perguntas/{id}/editar")
    public String editarPergunta(@PathVariable("id") Long id,
                                 @RequestParam("enunciado") String enunciado,
                                 @RequestParam("tempoLimite") int tempoLimite,
                                 @RequestParam("quizId") Long quizId) {
        System.out.println("POST /perguntas/" + id + "/editar");
        System.out.println("Parâmetros recebidos -> ID: " + id + ", Enunciado: " + enunciado + ", Tempo Limite: " + tempoLimite + ", Quiz ID: " + quizId);

        return "redirect:/quizzes/" + quizId + "/gerenciar";
    }

    // 10. Criar alternativa vinculada a uma pergunta
    @PostMapping("/perguntas/{perguntaId}/alternativas")
    public String criarAlternativa(@PathVariable("perguntaId") Long perguntaId,
                                   @RequestParam("texto") String texto,
                                   @RequestParam(value = "correta", defaultValue = "false") boolean correta) {
        System.out.println("POST /perguntas/" + perguntaId + "/alternativas");
        System.out.println("Parâmetros recebidos -> Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/perguntas/" + perguntaId + "/editar";
    }

    // 11. Editar alternativa
    @PostMapping("/alternativas/{id}/editar")
    public String editarAlternativa(@PathVariable("id") Long id,
                                    @RequestParam("texto") String texto,
                                    @RequestParam(value = "correta", defaultValue = "false") boolean correta,
                                    @RequestParam("perguntaId") Long perguntaId) {
        System.out.println("POST /alternativas/" + id + "/editar");
        System.out.println("Parâmetros recebidos -> Alternativa ID: " + id + ", Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/perguntas/" + perguntaId + "/editar";
    }
}