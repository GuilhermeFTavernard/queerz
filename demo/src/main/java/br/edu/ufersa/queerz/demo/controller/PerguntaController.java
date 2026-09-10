package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PerguntaController {

    // Exibir formulário de nova pergunta para um quiz específico
    @GetMapping("/quizzes/{quizId}/perguntas/nova")
    public String formNovaPergunta(@PathVariable("quizId") Long quizId, Model model) {
        System.out.println("Exibindo form para o Quiz ID: " + quizId);

        model.addAttribute("quizId", quizId);
        return "perguntas/form-pergunta";
    }

    // Criar pergunta
    @PostMapping("/quizzes/{quizId}/perguntas")
    public String criarPergunta(@PathVariable("quizId") Long quizId,
                                @RequestParam("enunciado") String enunciado,
                                @RequestParam("tempoLimite") int tempoLimite) {
        System.out.println("Quiz:" + quizId);
        System.out.println("Enunciado: " + enunciado + ", Tempo Limite: " + tempoLimite);

        return "redirect:/quizzes/" + quizId + "/gerenciar";
    }

    // Exibir formulário de edição de pergunta existente
    @GetMapping("/perguntas/{id}")
    public String formEditarPergunta(@PathVariable("id") Long id, Model model) {
        System.out.println("Carregando tela de edição da pergunta: " + id);

        model.addAttribute("perguntaId", id);
        return "perguntas/form-pergunta-edicao";
    }

    // Editar pergunta
    @PostMapping("/perguntas/{id}")
    public String editarPergunta(@PathVariable("id") Long id,
                                 @RequestParam("enunciado") String enunciado,
                                 @RequestParam("tempoLimite") int tempoLimite,
                                 @RequestParam("quizId") Long quizId) {
        System.out.println("ID: " + id + ", Enunciado: " + enunciado + ", Tempo Limite: " + tempoLimite + ", Quiz ID: " + quizId);

        return "redirect:/quizzes/" + quizId + "/gerenciar";
    }

    // Criar alternativa vinculada a uma pergunta
    @PostMapping("/perguntas/{perguntaId}/alternativas")
    public String criarAlternativa(@PathVariable("perguntaId") Long perguntaId,
                                   @RequestParam("texto") String texto,
                                   @RequestParam(value = "correta", defaultValue = "false") boolean correta) {
        System.out.println("Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/perguntas/" + perguntaId;
    }

    // Editar alternativa
    @PostMapping("/alternativas/{id}")
    public String editarAlternativa(@PathVariable("id") Long id,
                                    @RequestParam("texto") String texto,
                                    @RequestParam(value = "correta", defaultValue = "false") boolean correta,
                                    @RequestParam("perguntaId") Long perguntaId) {
        System.out.println("Alternativa ID: " + id + ", Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/perguntas/" + perguntaId;
    }
}