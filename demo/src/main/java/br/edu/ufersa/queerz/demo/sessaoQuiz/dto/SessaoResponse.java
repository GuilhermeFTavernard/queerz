package br.edu.ufersa.queerz.demo.jogo.dto;

import br.edu.ufersa.queerz.demo.jogo.Jogo;

public record SessaoResponse(
        String codigo,
        String apelido,
        Long quizId,
        String quizTitulo
) {

    /** O apelido não é persistido na sessão; vem da requisição de entrada. */
    public static SessaoResponse from(SessaoQuiz sessao, String apelido) {
        return new SessaoResponse(
                sessao.getCodigo(),
                apelido,
                sessao.getQuiz().getId(),
                sessao.getQuiz().getTitulo()
        );
    }
}
