package br.edu.ufersa.queerz.demo.participante;

public record ParticipanteResponse(
        String apelido
) {
    public static ParticipanteResponse from(Participante participante) {
        return new ParticipanteResponse(participante.apelido());
    }
}