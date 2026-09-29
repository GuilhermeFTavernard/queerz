package br.edu.ufersa.queerz.demo.jogo.dto;

public record ResultadoResponse(
        Long quizId,
        int pontuacao,
        int acertos,
        int totalPerguntas
) {}
