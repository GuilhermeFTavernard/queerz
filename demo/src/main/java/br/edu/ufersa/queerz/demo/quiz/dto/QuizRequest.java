package br.edu.ufersa.queerz.demo.quiz.dto;

import br.edu.ufersa.queerz.demo.quiz.Privacidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 150, message = "O título deve ter entre 3 e 150 caracteres")
        String titulo,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "A privacidade é obrigatória")
        Privacidade privacidade
) {}
