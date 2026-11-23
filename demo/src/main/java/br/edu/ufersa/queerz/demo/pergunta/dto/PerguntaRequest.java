package br.edu.ufersa.queerz.demo.pergunta.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PerguntaRequest(

        @NotBlank(message = "O enunciado é obrigatório")
        @Size(min = 5, max = 500, message = "O enunciado deve ter entre 5 e 500 caracteres")
        String enunciado,

        @NotNull(message = "O tempo de resposta é obrigatório")
        @Min(value = 5, message = "O tempo mínimo é 5 segundos")
        @Max(value = 300, message = "O tempo máximo é 300 segundos")
        Integer tempoResposta
) {}
