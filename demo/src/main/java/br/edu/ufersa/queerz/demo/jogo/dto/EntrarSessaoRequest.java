package br.edu.ufersa.queerz.demo.jogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EntrarSessaoRequest(

        @NotBlank(message = "O código PIN é obrigatório")
        @Pattern(regexp = "^[A-Za-z0-9]{4,10}$", message = "O código PIN deve ter de 4 a 10 letras ou números")
        String codigoPin,

        @NotBlank(message = "O apelido é obrigatório")
        @Size(min = 2, max = 30, message = "O apelido deve ter entre 2 e 30 caracteres")
        String apelido
) {}
