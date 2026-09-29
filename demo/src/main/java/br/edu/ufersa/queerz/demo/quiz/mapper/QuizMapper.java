package br.edu.ufersa.queerz.demo.quiz.mapper;

import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizRequest;
import org.springframework.stereotype.Component;

/** Converte QuizRequest -> entidade. A saída está em QuizResponse / QuizDetalheResponse / QuizJogoResponse.from(...). */
@Component
public class QuizMapper {

    public Quiz toEntity(QuizRequest request) {
        Quiz quiz = new Quiz();
        updateEntity(request, quiz);
        return quiz;
    }

    public void updateEntity(QuizRequest request, Quiz quiz) {
        quiz.setTitulo(request.titulo());
        quiz.setDescricao(request.descricao());
        quiz.setPrivacidade(request.privacidade());
    }
}
