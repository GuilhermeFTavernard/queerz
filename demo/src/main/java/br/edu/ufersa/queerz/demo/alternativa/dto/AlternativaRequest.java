package br.edu.ufersa.queerz.demo.alternativa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlternativaRequest(

        @NotBlank(message = "O texto da alternativa é obrigatório")
        @Size(max = 300, message = "O texto deve ter no máximo 300 caracteres")
        String texto,

        @NotNull(message = "Informe se a alternativa é a correta")
        Boolean correta
) {}
