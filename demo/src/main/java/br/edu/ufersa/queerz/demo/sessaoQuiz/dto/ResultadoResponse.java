package br.edu.ufersa.queerz.demo.sessaoQuiz.dto;

public record ResultadoResponse(
        Long quizId,
        int pontuacao,
        int acertos,
        int totalPerguntas
) {}
