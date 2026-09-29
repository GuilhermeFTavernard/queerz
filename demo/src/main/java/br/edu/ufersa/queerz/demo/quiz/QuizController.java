package br.edu.ufersa.queerz.demo.quiz;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class QuizController {
    // Listar quizzes públicos e buscar pelo nome (não precisa estar logado)
    @GetMapping("/quizzes")
    public String listarPublicos(@RequestParam(value = "busca", required = false) String busca, Model model) {
        System.out.println("Termo de busca: " + busca);
        model.addAttribute("termoBusca", busca);
        return "quizzes/publicos";
    }

    // Listar quizzes do usuário autenticado e buscar por nome entre eles
    @GetMapping("/meus-quizzes")
    public String listarMeusQuizzes(@RequestParam(value = "busca", required = false) String busca, Model model) {
        System.out.println("Termo de busca: " + busca);
        model.addAttribute("termoBusca", busca);
        return "quizzes/meus-quizzes";
    }

    // Exibir formulário de criação de quiz
    @GetMapping("/quizzes/novo")
    public String exibirFormNovoQuiz() {
        System.out.println("Exibindo formulário de criação");
        return "quizzes/form-quiz";
    }

    // Criar novo quiz
    @PostMapping("/quizzes")
    public String criarQuiz(@RequestParam("titulo") String titulo,
                            @RequestParam(value = "descricao", required = false) String descricao) {
        System.out.println("Criando quiz | Título: " + titulo + " | Descrição: " + descricao);

        Long idSimulado = 1L;
        return "redirect:/quizzes/" + idSimulado + "/gerenciar";
    }

    // Exibir formulário de edição de quiz
    @GetMapping("/quizzes/{id}")
    public String exibirFormEditarQuiz(@PathVariable("id") Long id, Model model) {
        System.out.println("Carregando edição para o Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "quizzes/form-quiz";
    }

    // Editar um quiz
    @PostMapping("/quizzes/{id}")
    public String editarQuiz(@PathVariable("id") Long id,
                             @RequestParam("titulo") String titulo,
                             @RequestParam(value = "descricao", required = false) String descricao) {
        System.out.println("Atualizando Quiz ID: " + id + " | Novo Título: " + titulo + " | Nova Descrição: " + descricao);
        return "redirect:/meus-quizzes";
    }

    // Deletar um quiz
    @DeleteMapping("/quizzes/{id}")
    public String deletarQuiz(@PathVariable("id") Long id) {
        System.out.println("Excluindo Quiz ID: " + id);
        return "redirect:/meus-quizzes";
    }

    // Painel de gerenciamento das perguntas do quiz
    @GetMapping("/quizzes/{id}/gerenciar")
    public String gerenciarQuiz(@PathVariable("id") Long id, Model model) {
        System.out.println("Acessando painel de gerenciamento do Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "quizzes/gerenciar";
    }

    // Exibir formulário de nova pergunta para um quiz específico
    @GetMapping("/quizzes/{quizId}/perguntas")
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
    @GetMapping("/quizzes/{quizId}/perguntas/{id}")
    public String formEditarPergunta(@PathVariable("id") Long id, Model model) {
        System.out.println("Carregando tela de edição da pergunta: " + id);

        model.addAttribute("perguntaId", id);
        return "perguntas/form-pergunta-edicao";
    }

    // Editar pergunta
    @PostMapping("/quizzes/{quizId}/perguntas/{id}")
    public String editarPergunta(@PathVariable("id") Long id,
                                 @RequestParam("enunciado") String enunciado,
                                 @RequestParam("tempoLimite") int tempoLimite,
                                 @RequestParam("quizId") Long quizId) {
        System.out.println("ID: " + id + ", Enunciado: " + enunciado + ", Tempo Limite: " + tempoLimite + ", Quiz ID: " + quizId);

        return "redirect:/quizzes/" + quizId + "/gerenciar";
    }

    // Criar alternativa vinculada a uma pergunta
    @PostMapping("/quizzes/{quizId}/perguntas/{perguntaId}/alternativas")
    public String criarAlternativa(@PathVariable("perguntaId") Long perguntaId,
                                   @RequestParam("texto") String texto,
                                   @RequestParam(value = "correta", defaultValue = "false") boolean correta) {
        System.out.println("Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/quizzes/{quizId}/perguntas/" + perguntaId;
    }

    // Editar alternativa
    @PostMapping("/quizzes/{quizId}/perguntas/{perguntaId}/alternativas/{id}")
    public String editarAlternativa(@PathVariable("id") Long id,
                                    @RequestParam("texto") String texto,
                                    @RequestParam(value = "correta", defaultValue = "false") boolean correta,
                                    @RequestParam("perguntaId") Long perguntaId) {
        System.out.println("Alternativa ID: " + id + ", Pergunta ID: " + perguntaId + ", Texto: " + texto + ", Correta: " + correta);

        return "redirect:/quizzes/{quizId}/perguntas/" + perguntaId;
    }
}