package br.edu.ufersa.queerz.demo.quiz.dto;

import br.edu.ufersa.queerz.demo.quiz.Privacidade;
import br.edu.ufersa.queerz.demo.quiz.Quiz;

public record QuizResponse(
        Long id,
        String titulo,
        String descricao,
        Privacidade privacidade,
        int totalPerguntas
) {
    /** Chamar dentro de uma transação (perguntas é LAZY). */
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitulo(),
                quiz.getDescricao(),
                quiz.getPrivacidade(),
                quiz.getPerguntas().size()
        );
    }
}
