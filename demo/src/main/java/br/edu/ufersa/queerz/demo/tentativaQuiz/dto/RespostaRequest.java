package br.edu.ufersa.queerz.demo.tentativaquiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RespostaRequest(

        @NotNull(message = "O id da pergunta é obrigatório")
        @Positive(message = "O id da pergunta deve ser positivo")
        Long perguntaId,

        @NotNull(message = "O id da alternativa é obrigatório")
        @Positive(message = "O id da alternativa deve ser positivo")
        Long alternativaId
) {}
