package br.edu.ufersa.queerz.demo.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Tratamento global de exceções da API (RFC 9457 - ProblemDetail).
 *
 * Atende os @RestController (ex.: POST /api/auth/login) e devolve JSON.
 * As telas Thymeleaf (@Controller) são atendidas pelo WebExceptionHandler.
 * @Order(1) garante que ele tenha prioridade sobre o WebExceptionHandler nos @RestController.
 */
@Order(1)
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://queerz.ufersa.edu.br/erros/";

    public record CampoInvalido(String campo, String mensagem) {}

    // 400 - Bean Validation em @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoInvalido> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new CampoInvalido(e.getField(), e.getDefaultMessage()))
                .toList();

        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos são inválidos.", "validacao", request);
        pd.setProperty("errors", erros);
        return pd;
    }

    // 400 - Bean Validation em parâmetros (classe com @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<CampoInvalido> erros = ex.getConstraintViolations().stream()
                .map(v -> new CampoInvalido(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Parâmetros inválidos",
                "Um ou mais parâmetros são inválidos.", "validacao", request);
        pd.setProperty("errors", erros);
        return pd;
    }

    // 400 - JSON malformado, enum inexistente (ex.: privacidade), tipo incorreto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "O corpo da requisição está malformado ou contém valores inválidos.",
                "corpo-invalido", request);
    }

    // 400 - Ex.: /api/quizzes/abc quando se espera Long
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O parâmetro '%s' possui formato inválido.".formatted(ex.getName()),
                "parametro-invalido", request);
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(),
                "nao-encontrado", request);
    }

    // 409 - conflito detectado pela regra de negócio
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflito de dados", ex.getMessage(), "conflito", request);
    }

    // 409 - violação de integridade vinda do banco (unique de e-mail/código, FK...)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Violação de integridade",
                "A operação viola uma restrição de integridade dos dados (registro duplicado ou em uso).",
                "violacao-integridade", request);
    }

    // 422
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage(),
                "regra-de-negocio", request);
    }

    // Fallback: 500 para o inesperado, sem vazar detalhes internos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest request) throws Exception {
        // Deixa o Spring Security tratar 401/403 (não podem virar 500)
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }

        // Erros nativos do Spring MVC (405, 415, 404 de rota etc.) mantêm o status correto
        if (ex instanceof ErrorResponse er) {
            ProblemDetail pd = er.getBody();
            pd.setInstance(URI.create(request.getRequestURI()));
            pd.setProperty("timestamp", Instant.now());
            return ResponseEntity.status(er.getStatusCode()).headers(er.getHeaders()).body(pd);
        }

        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        ProblemDetail pd = build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", "erro-interno", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private ProblemDetail build(HttpStatus status, String title, String detail,
                                String typeSlug, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create(BASE_TYPE + typeSlug));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
