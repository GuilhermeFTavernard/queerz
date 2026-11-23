package br.edu.ufersa.queerz.demo.shared.exception;

/** Regra de negócio violada (ex.: sala encerrada, quiz sem perguntas). Resulta em 422. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
