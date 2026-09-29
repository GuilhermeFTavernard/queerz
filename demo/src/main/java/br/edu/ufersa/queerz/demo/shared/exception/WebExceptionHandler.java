package br.edu.ufersa.queerz.demo.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tratamento global de exceções para as telas (Thymeleaf).
 * Converte exceções de negócio em uma página de erro amigável (templates/erro/erro.html),
 * com o status HTTP correto e sem expor stack trace.
 */
@Order(2)
@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ModelAndView handleDuplicate(DuplicateResourceException ex) {
        return erro(HttpStatus.CONFLICT, "Conflito de dados", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT, "Violação de integridade",
                "A operação viola uma restrição dos dados (registro duplicado ou em uso).");
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(BusinessRuleException ex) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex, HttpServletRequest request) throws Exception {
        // Deixa o Spring (404 de rota, 405, 415...) e o Spring Security (401/403) cuidarem do que é deles
        if (ex instanceof ErrorResponse
                || ex instanceof AccessDeniedException
                || ex instanceof AuthenticationException) {
            throw ex;
        }
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private ModelAndView erro(HttpStatus status, String titulo, String detalhe) {
        ModelAndView mv = new ModelAndView("erro/erro");
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("titulo", titulo);
        mv.addObject("detalhe", detalhe);
        return mv;
    }
}
