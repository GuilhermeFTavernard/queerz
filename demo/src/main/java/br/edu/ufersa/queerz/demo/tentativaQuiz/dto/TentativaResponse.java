package br.edu.ufersa.queerz.demo.tentativaQuiz.dto;

import br.edu.ufersa.queerz.demo.tentativaQuiz.TentativaQuiz;

import java.time.LocalDateTime;

public record TentativaResponse(
        Long id,
        Long quizId,
        int pontuacaoFinal,
        int tempoTotalGasto,
        LocalDateTime dataFinalizacao
) {
    public static TentativaResponse from(TentativaQuiz tentativa) {
        return new TentativaResponse(
                tentativa.getId(),
                tentativa.getQuiz().getId(),
                tentativa.getPontuacaoFinal(),
                tentativa.getTempoTotalGasto(),
                tentativa.getDataFinalizacao()
        );
    }
}
