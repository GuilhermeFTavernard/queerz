package br.edu.ufersa.queerz.demo.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail em formato inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email
) {}
