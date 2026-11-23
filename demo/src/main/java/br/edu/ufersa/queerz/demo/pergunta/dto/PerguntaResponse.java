package br.edu.ufersa.queerz.demo.pergunta.dto;

import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaResponse;
import br.edu.ufersa.queerz.demo.pergunta.Pergunta;

import java.util.List;

public record PerguntaResponse(
        Long id,
        String enunciado,
        int tempoResposta,
        List<AlternativaResponse> alternativas
) {
    /** Chamar dentro de uma transação (alternativas é LAZY). */
    public static PerguntaResponse from(Pergunta pergunta) {
        return new PerguntaResponse(
                pergunta.getId(),
                pergunta.getEnunciado(),
                pergunta.getTempoResposta(),
                pergunta.getAlternativas().stream().map(AlternativaResponse::from).toList()
        );
    }
}
