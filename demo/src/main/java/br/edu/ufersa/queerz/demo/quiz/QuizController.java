package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaRequest;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaRequest;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaResponse;
import br.edu.ufersa.queerz.demo.quiz.dto.*;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.usuario.UsuarioApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class QuizController {

    private final QuizApplicationService quizApplicationService;

    public QuizController(QuizApplicationService quizApplicationService) {
        this.quizApplicationService = quizApplicationService;
    }

    // Listar quizzes públicos e buscar pelo nome (público)
    @GetMapping("/quizzes")
    public String listarPublicos(@RequestParam(value = "busca", required = false) String busca, Model model) {
        List<QuizResponse> quizzes = quizApplicationService.listarPublicos(busca);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("busca", busca);
        return "quizzes/publicos";
    }

    // Listar quizzes do usuário autenticado e buscar por nome entre eles
    @ResponseBody
    @GetMapping("/meus-quizzes")
    public String listarMeusQuizzes(@RequestParam(value = "busca", required = false) String busca,
                                    HttpServletRequest httpRequest,
                                    Model model) {
        try {
            List<QuizResponse> meusQuizzes = quizApplicationService.listarMeus(busca, httpRequest);
            model.addAttribute("quizzes", meusQuizzes);
            model.addAttribute("busca", busca);
            return "quizzes/meus-quizzes";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Exibir formulário de criação de quiz
    @GetMapping("/quizzes/novo")
    public String exibirFormNovoQuiz(Model model) {
        if (!model.containsAttribute("quizRequest")) {
            // Inicializa com um valor padrão para a privacidade (ex: Privacidade.PUBLICO ou PRIVADO)
            model.addAttribute("quizRequest", new QuizRequest("", "", Privacidade.PUBLICO));
        }
        return "quizzes/form-quiz";
    }

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    // Criar novo quiz
    @ResponseBody
    @PostMapping("/quizzes")
    public String criarQuiz(@ModelAttribute QuizRequest request,
                            HttpServletRequest httpRequest,
                            RedirectAttributes redirectAttributes) {

    log.info("Chegou quiz");

        try {
            QuizResponse novoQuiz = quizApplicationService.criar(request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Quiz criado com sucesso!");
            return "Quiz criado!";
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "erro 1";
        } catch (ForbiddenOperationException e) {
            return "erro 2";
        }
    }

    // Exibir formulário de edição de quiz
    @GetMapping("/quizzes/{id}")
    public String exibirFormEditarQuiz(@PathVariable("id") Long id,
                                       HttpServletRequest httpRequest,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            QuizResponse quiz = quizApplicationService.buscarPorId(id, httpRequest);
            model.addAttribute("quiz", quiz);
            model.addAttribute("quizRequest", new QuizRequest(quiz.titulo(), quiz.descricao(), quiz.privacidade()));
            return "quizzes/form-quiz";
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/meus-quizzes";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Editar um quiz
    @PostMapping("/quizzes/{id}")
    public String editarQuiz(@PathVariable("id") Long id,
                             @ModelAttribute QuizRequest request,
                             HttpServletRequest httpRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.atualizar(id, request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Quiz atualizado com sucesso!");
            return "redirect:/meus-quizzes";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes/" + id;
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Deletar um quiz
    @DeleteMapping("/quizzes/{id}")
    public String deletarQuiz(@PathVariable("id") Long id,
                              HttpServletRequest httpRequest,
                              RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.remover(id, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Quiz excluído com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
        return "redirect:/meus-quizzes";
    }

    // Painel de gerenciamento das perguntas do quiz
    @GetMapping("/quizzes/{id}/gerenciar")
    public String gerenciarQuiz(@PathVariable("id") Long id,
                                HttpServletRequest httpRequest,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            QuizResponse quiz = quizApplicationService.buscarPorIdComPerguntas(id, httpRequest);
            model.addAttribute("quiz", quiz);
            return "quizzes/gerenciar";
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/meus-quizzes";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Exibir formulário de nova pergunta para um quiz específico
    @GetMapping("/quizzes/{quizId}/perguntas")
    public String formNovaPergunta(@PathVariable("quizId") Long quizId, Model model) {
        model.addAttribute("quizId", quizId);
        if (!model.containsAttribute("perguntaRequest")) {
            // Inicializa o formulário com valor padrão para tempoResposta (ex: 30 segundos)
            model.addAttribute("perguntaRequest", new PerguntaRequest("", 30));
        }
        return "perguntas/form-pergunta";
    }

    // Criar pergunta
    @PostMapping("/quizzes/{quizId}/perguntas")
    public String criarPergunta(@PathVariable("quizId") Long quizId,
                                @ModelAttribute PerguntaRequest request,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.adicionarPergunta(quizId, request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pergunta adicionada com sucesso!");
            return "redirect:/quizzes/" + quizId + "/gerenciar";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes/" + quizId + "/perguntas";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Exibir formulário de edição de pergunta existente
    @GetMapping("/quizzes/{quizId}/perguntas/{id}")
    public String formEditarPergunta(@PathVariable("quizId") Long quizId,
                                     @PathVariable("id") Long id,
                                     HttpServletRequest httpRequest,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            PerguntaResponse pergunta = quizApplicationService.buscarPergunta(quizId, id, httpRequest);
            model.addAttribute("quizId", quizId);
            model.addAttribute("pergunta", pergunta);
            model.addAttribute("perguntaRequest", new PerguntaRequest(pergunta.enunciado(), pergunta.tempoResposta()));
            return "perguntas/form-pergunta-edicao";
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes/" + quizId + "/gerenciar";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Editar pergunta
    @PostMapping("/quizzes/{quizId}/perguntas/{id}")
    public String editarPergunta(@PathVariable("quizId") Long quizId,
                                 @PathVariable("id") Long id,
                                 @ModelAttribute PerguntaRequest request,
                                 HttpServletRequest httpRequest,
                                 RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.atualizarPergunta(quizId, id, request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pergunta atualizada com sucesso!");
            return "redirect:/quizzes/" + quizId + "/gerenciar";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes/" + quizId + "/perguntas/" + id;
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Criar alternativa vinculada a uma pergunta
    @PostMapping("/quizzes/{quizId}/perguntas/{perguntaId}/alternativas")
    public String criarAlternativa(@PathVariable("quizId") Long quizId,
                                   @PathVariable("perguntaId") Long perguntaId,
                                   @ModelAttribute AlternativaRequest request,
                                   HttpServletRequest httpRequest,
                                   RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.adicionarAlternativa(perguntaId, request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Alternativa adicionada com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
        return "redirect:/quizzes/" + quizId + "/perguntas/" + perguntaId;
    }

    // Editar alternativa
    @PostMapping("/quizzes/{quizId}/perguntas/{perguntaId}/alternativas/{id}")
    public String editarAlternativa(@PathVariable("quizId") Long quizId,
                                    @PathVariable("perguntaId") Long perguntaId,
                                    @PathVariable("id") Long id,
                                    @ModelAttribute AlternativaRequest request,
                                    HttpServletRequest httpRequest,
                                    RedirectAttributes redirectAttributes) {
        try {
            quizApplicationService.atualizarAlternativa(quizId, perguntaId, id, request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Alternativa atualizada com sucesso!");
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
        return "redirect:/quizzes/" + quizId + "/perguntas/" + perguntaId;
    }
}