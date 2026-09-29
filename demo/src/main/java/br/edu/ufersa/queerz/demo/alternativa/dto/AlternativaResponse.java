package br.edu.ufersa.queerz.demo.alternativa.dto;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;

/**
 * Revela qual alternativa é a correta: use SOMENTE nas telas de gerenciamento do dono do quiz.
 * Para quem está jogando, crie um DTO sem o campo "correta".
 */
public record AlternativaResponse(
        Long id,
        String texto,
        boolean correta
) {

    public static AlternativaResponse from(Alternativa alternativa) {
        return new AlternativaResponse(
                alternativa.getId(),
                alternativa.getTexto(),
                alternativa.isCorreta()
        );
    }
}
