package br.edu.ufersa.queerz.demo.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tratamento global de exceções da aplicação (monolito com páginas Thymeleaf).
 *
 * Converte exceções de negócio e falhas inesperadas em uma página de erro amigável
 * (templates/erro/erro.html), com o status HTTP correto e sem expor stack trace.
 *
 * Erros de validação de formulário NÃO passam por aqui: os controllers recebem
 * o DTO com @Valid + BindingResult e voltam ao próprio formulário mostrando as mensagens.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - entidade não encontrada
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    // 409 - conflito de chave de negócio (ex.: e-mail já cadastrado)
    @ExceptionHandler(DuplicateResourceException.class)
    public ModelAndView handleDuplicate(DuplicateResourceException ex) {
        return erro(HttpStatus.CONFLICT, "Conflito de dados", ex.getMessage());
    }

    // 409 - violação de integridade vinda do banco (unique, FK...)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT, "Violação de integridade",
                "A operação viola uma restrição dos dados (registro duplicado ou em uso).");
    }

    // 422 - regra de negócio violada
    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(BusinessRuleException ex) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
    }

    // 400 - parâmetro inválido na URL (ex.: /quizzes/abc quando se espera Long),
    //       violação de @Validated em parâmetros ou formulário sem BindingResult no controller
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
                       ConstraintViolationException.class,
                       BindException.class})
    public ModelAndView handleBadRequest(Exception ex) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Um ou mais dados enviados são inválidos.");
    }

    // 500 - qualquer erro inesperado: loga internamente, mostra mensagem genérica
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
