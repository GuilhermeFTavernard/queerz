package br.edu.ufersa.queerz.demo.tentativaQuiz;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.FinalizarTentativaRequest;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.TentativaResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tentativa")
public class TentativaQuizController {

    private final TentativaQuizApplicationService tentativaQuizApplicationService;

    public TentativaQuizController(TentativaQuizApplicationService tentativaQuizApplicationService) {
        this.tentativaQuizApplicationService = tentativaQuizApplicationService;
    }

    /**
     * Lista o histórico de tentativas do usuário autenticado na sessão HTTP.
     */
    @GetMapping("/minhas")
    public String listarMinhas(HttpServletRequest request, Model model) {
        try {
            List<TentativaResponse> tentativas = tentativaQuizApplicationService.listarMinhas(request);
            model.addAttribute("tentativas", tentativas);
            return "tentativa/minhas-tentativas";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    /**
     * Exibe o ranking Top 10 de um quiz específico.
     */
    @GetMapping("/ranking/{quizId}")
    public String ranking(@PathVariable Long quizId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TentativaResponse> ranking = tentativaQuizApplicationService.ranking(quizId, request);
            model.addAttribute("ranking", ranking);
            model.addAttribute("quizId", quizId);
            return "tentativa/ranking";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes";
        }
    }

    /**
     * Recebe as respostas do formulário, envia para correção no Service e redireciona para a tela de resultado.
     */
    @PostMapping("/finalizar")
    public String finalizar(@ModelAttribute FinalizarTentativaRequest requestDTO,
                            HttpServletRequest httpRequest,
                            RedirectAttributes redirectAttributes) {
        try {
            TentativaResponse resultado = tentativaQuizApplicationService.finalizar(requestDTO, httpRequest);
            redirectAttributes.addFlashAttribute("resultado", resultado);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Quiz finalizado com sucesso!");
            return "redirect:/tentativa/resultado";
        } catch (BusinessRuleException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/quizzes";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    /**
     * Página visual de resultado exibindo os dados gravados via FlashAttribute.
     */
    @GetMapping("/resultado")
    public String exibirResultado() {
        return "tentativa/resultado";
    }
}