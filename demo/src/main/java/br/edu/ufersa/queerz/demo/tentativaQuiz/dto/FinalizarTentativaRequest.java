package br.edu.ufersa.queerz.demo.tentativaquiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Note: NÃO existe campo pontuacaoFinal. A nota é calculada pelo servidor a partir das respostas;
 * se o cliente pudesse enviá-la, qualquer um forjaria a pontuação.
 */
public record FinalizarTentativaRequest(

        @NotNull(message = "O id do quiz é obrigatório")
        @Positive(message = "O id do quiz deve ser positivo")
        Long quizId,

        @NotNull(message = "O tempo total gasto é obrigatório")
        @PositiveOrZero(message = "O tempo total gasto não pode ser negativo")
        Integer tempoTotalGasto,

        @NotEmpty(message = "Envie ao menos uma resposta")
        List<@Valid RespostaRequest> respostas
) {}
