package br.edu.ufersa.queerz.demo.tentativaQuiz.quiz;

import br.edu.ufersa.queerz.demo.quiz.tentativaQuizController;
import br.edu.ufersa.queerz.demo.tentativaQuiz.TentativaQuiz;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tentativa")
public class tentativaQuizController {

    @PostMapping("/iniciar")
    public String iniciar() {

        System.out.println("Tentativa iniciada!");

        return "quiz";
    }

    @PostMapping("/finalizar")
    public String finalizar(TentativaQuiz tentativa) {

        System.out.println(
                "Pontuação: " + tentativa.getPontuacaoFinal()
        );

        System.out.println(
                "Tempo: " + tentativa.getTempoTotalGasto()
        );

        return "resultado";
    }

}
