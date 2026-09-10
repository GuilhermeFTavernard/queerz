package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class QuizController {

    // 1. Listar quizzes públicos e buscar pelo nome (não precisa estar logado)
    @GetMapping("/quizzes")
    public String listarPublicos(@RequestParam(value = "busca", required = false) String busca, Model model) {
        System.out.println("GET /quizzes -> Termo de busca: " + busca);
        model.addAttribute("termoBusca", busca);
        return "quizzes/publicos";
    }

    // 2. Listar quizzes do usuário autenticado e buscar por nome entre eles
    @GetMapping("/meus-quizzes")
    public String listarMeusQuizzes(@RequestParam(value = "busca", required = false) String busca, Model model) {
        System.out.println("GET /meus-quizzes -> Termo de busca: " + busca);
        model.addAttribute("termoBusca", busca);
        return "quizzes/meus-quizzes";
    }

    // 3. Exibir formulário de criação de quiz
    @GetMapping("/quizzes/novo")
    public String exibirFormNovoQuiz() {
        System.out.println("GET /quizzes/novo -> Exibindo formulário de criação");
        return "quizzes/form-quiz";
    }

    // 4. Criar novo quiz
    @PostMapping("/quizzes")
    public String criarQuiz(@RequestParam("titulo") String titulo,
                            @RequestParam(value = "descricao", required = false) String descricao) {
        System.out.println("POST /quizzes -> Criando quiz | Título: " + titulo + " | Descrição: " + descricao);

        Long idSimulado = 1L;
        return "redirect:/quizzes/" + idSimulado + "/gerenciar";
    }

    // 5. Exibir formulário de edição de quiz
    @GetMapping("/quizzes/{id}/editar")
    public String exibirFormEditarQuiz(@PathVariable("id") Long id, Model model) {
        System.out.println("GET /quizzes/" + id + "/editar -> Carregando edição para o Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "quizzes/form-quiz";
    }

    // 6. Editar um quiz
    @PostMapping("/quizzes/{id}/editar")
    public String editarQuiz(@PathVariable("id") Long id,
                             @RequestParam("titulo") String titulo,
                             @RequestParam(value = "descricao", required = false) String descricao) {
        System.out.println("POST /quizzes/" + id + "/editar -> Atualizando Quiz ID: " + id + " | Novo Título: " + titulo + " | Nova Descrição: " + descricao);
        return "redirect:/meus-quizzes";
    }

    // 7. Deletar um quiz
    @PostMapping("/quizzes/{id}/excluir")
    public String deletarQuiz(@PathVariable("id") Long id) {
        System.out.println("POST /quizzes/" + id + "/excluir -> Excluindo Quiz ID: " + id);
        return "redirect:/meus-quizzes";
    }

    // Painel de gerenciamento das perguntas do quiz
    @GetMapping("/quizzes/{id}/gerenciar")
    public String gerenciarQuiz(@PathVariable("id") Long id, Model model) {
        System.out.println("GET /quizzes/" + id + "/gerenciar -> Acessando painel de gerenciamento do Quiz ID: " + id);
        model.addAttribute("quizId", id);
        return "quizzes/gerenciar";
    }
}