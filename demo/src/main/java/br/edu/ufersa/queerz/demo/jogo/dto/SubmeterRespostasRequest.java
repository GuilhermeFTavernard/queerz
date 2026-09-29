package br.edu.ufersa.queerz.demo.jogo.dto;

import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.RespostaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmeterRespostasRequest(

        @NotEmpty(message = "Envie ao menos uma resposta")
        List<@Valid RespostaRequest> respostas
) {}
