package br.edu.ufersa.queerz.demo.sessaoQuiz;

import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.EntrarSessaoRequest;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.ResultadoResponse;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.SessaoResponse;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.SubmeterRespostasRequest;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.tentativaQuiz.TentativaQuizApplicationService;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.FinalizarTentativaRequest;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.TentativaResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SessaoQuizController {

    private final SessaoQuizApplicationService sessaoQuizApplicationService;
    private final TentativaQuizApplicationService tentativaQuizApplicationService;
    private final QuizRepository quizRepository;

    public SessaoQuizController(SessaoQuizApplicationService sessaoQuizApplicationService,
                                TentativaQuizApplicationService tentativaQuizApplicationService,
                                QuizRepository quizRepository) {
        this.sessaoQuizApplicationService = sessaoQuizApplicationService;
        this.tentativaQuizApplicationService = tentativaQuizApplicationService;
        this.quizRepository = quizRepository;
    }

    // Jogar um quiz pelo código PIN (Acesso inicial)
    @GetMapping("/sessaoQuiz")
    public String exibirTelaPin(Model model) {
        if (!model.containsAttribute("entrarSessaoRequest")) {
            model.addAttribute("entrarSessaoRequest", new EntrarSessaoRequest("", ""));
        }
        return "sessaoQuiz/entrar-pin";
    }

    // Validação, vinculação de estado na HttpSession e redirecionamento para a sala
    @PostMapping("/sessaoQuiz")
    public String entrarPeloCodigo(@ModelAttribute EntrarSessaoRequest request,
                                   HttpServletRequest httpRequest,
                                   RedirectAttributes redirectAttributes) {
        try {
            SessaoResponse sessao = sessaoQuizApplicationService.entrar(request, httpRequest);
            return "redirect:/sala/" + sessao.codigo();
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/sessaoQuiz";
        }
    }

    // Exibição da sala síncrona lendo a sessão ativa na HttpSession
    @GetMapping("/sala/{codigo}")
    public String salaAoVivo(@PathVariable("codigo") String codigo,
                             HttpServletRequest httpRequest,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            QuizResponse quiz = sessaoQuizApplicationService.buscarQuiz(httpRequest);
            String apelido = (String) httpRequest.getSession().getAttribute(SessaoQuizApplicationService.ATTR_JOGADOR_APELIDO);

            model.addAttribute("codigo", codigo);
            model.addAttribute("apelido", apelido);
            model.addAttribute("quiz", quiz);
            return "sessaoQuiz/sala-ao-vivo";
        } catch (ForbiddenOperationException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/sessaoQuiz";
        }
    }

    // Submeter respostas da sessão síncrona
    @PostMapping("/sala/{codigo}/submeter")
    public String submeterSessaoAoVivo(@PathVariable("codigo") String codigo,
                                       @ModelAttribute SubmeterRespostasRequest request,
                                       HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes) {
        try {
            ResultadoResponse resultado = sessaoQuizApplicationService.submeter(request, httpRequest);
            redirectAttributes.addFlashAttribute("resultado", resultado);
            return "redirect:/quizzes/" + resultado.quizId() + "/resultado";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/sala/" + codigo;
        } catch (ForbiddenOperationException e) {
            return "redirect:/sessaoQuiz";
        }
    }

    // Jogar um quiz público de forma individual/assíncrona (Exige login no sistema)
    @GetMapping("/quizzes/{id}/jogar")
    public String carregarQuizPublico(@PathVariable("id") Long id, Model model) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));

        model.addAttribute("quiz", QuizResponse.from(quiz));
        return "sessaoQuiz/responder-quiz";
    }

    // Submeter respostas de sessaoQuiz individual e gravar a tentativa para o usuário autenticado
    @PostMapping("/quizzes/{id}/submeter")
    public String submeterQuizPublico(@PathVariable("id") Long id,
                                      @ModelAttribute FinalizarTentativaRequest request,
                                      HttpServletRequest httpRequest,
                                      RedirectAttributes redirectAttributes) {
        try {
            TentativaResponse resultado = tentativaQuizApplicationService.finalizar(request, httpRequest);
            redirectAttributes.addFlashAttribute("resultado", resultado);
            return "redirect:/quizzes/" + id + "/resultado";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes/" + id + "/jogar";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Exibição de pontuação e resultado
    @GetMapping("/quizzes/{id}/resultado")
    public String exibirResultado(@PathVariable("id") Long id, Model model) {
        model.addAttribute("quizId", id);
        return "sessaoQuiz/resultado";
    }
}