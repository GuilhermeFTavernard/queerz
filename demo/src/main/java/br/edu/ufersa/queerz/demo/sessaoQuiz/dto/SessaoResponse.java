package br.edu.ufersa.queerz.demo.sessaoQuiz.dto;

import br.edu.ufersa.queerz.demo.participante.ParticipanteResponse;
import br.edu.ufersa.queerz.demo.participante.Participante;
import br.edu.ufersa.queerz.demo.sessaoQuiz.SessaoQuiz;

import java.util.List;

public record SessaoResponse(
        String codigo,
        Long quizId,
        String quizTitulo,
        List<ParticipanteResponse> participantes
) {

    public static SessaoResponse from(SessaoQuiz sessao, List<Participante> participantes) {
        List<ParticipanteResponse> participantesDto = participantes.stream()
                .map(ParticipanteResponse::from)
                .toList();

        return new SessaoResponse(
                sessao.getCodigo(),
                sessao.getQuiz().getId(),
                sessao.getQuiz().getTitulo(),
                participantesDto
        );
    }
}