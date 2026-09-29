package br.edu.ufersa.queerz.demo.shared.exception;

/** Conflito de chave de negócio (ex.: e-mail já cadastrado). Resulta em 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
