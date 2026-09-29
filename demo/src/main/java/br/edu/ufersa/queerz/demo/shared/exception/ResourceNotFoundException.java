package br.edu.ufersa.queerz.demo.shared.exception;

/** Entidade não encontrada. Resulta em 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super("%s não encontrado(a) com id %s".formatted(recurso, id));
    }
}
