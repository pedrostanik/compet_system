package com.petshop.api.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.petshop.api.products.service.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every error leaves the API as an RFC 7807 ProblemDetail (application/problem+json) with a
 * "requestId" property. Details meant for developers (exception class, SQL, stack trace) are
 * only logged server-side, under the same requestId — never returned to the client.
 *
 * Spring MVC's own exceptions (malformed JSON, wrong method, unknown path, ResponseStatusException…)
 * are mapped to their proper 4xx by ResponseEntityExceptionHandler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        // Hibernate's own messages name internal classes, so the client gets a fixed text.
        log.info("Not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Registro não encontrado.");
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        // Business messages are written for the user, so they are returned as-is.
        log.info("Business rule violated: {}", ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        // The message holds no row data: the driver runs with logServerErrorDetail=false (application.yaml).
        String sqlState = sqlState(ex);
        log.warn("Data integrity violation (SQLState {}): {}", sqlState, ex.getMostSpecificCause().getMessage());
        return switch (sqlState == null ? "" : sqlState) {
            case "23505" -> problem(HttpStatus.CONFLICT, "Já existe um registro com estes dados.");
            case "23503" -> problem(HttpStatus.CONFLICT,
                    "O registro está vinculado a outros dados e não pode ser alterado ou removido.");
            // 23502 (NOT NULL), 23514 (CHECK) and anything else: the request carried invalid data.
            default -> problem(HttpStatus.BAD_REQUEST, "Dados inválidos ou incompletos.");
        };
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.info("Access denied: {}", ex.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Se o problema persistir, informe o código " + RequestIdFilter.currentId() + ".");
    }

    /** Bean Validation failures (@Valid): 400 with an "errors" map of field → message. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Dados inválidos.");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * Unreadable JSON. When Jackson can tell which field failed (unknown enum value, text in a
     * number field…), report it in the same "errors" map as validation failures.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Não foi possível ler os dados enviados.");
        if (ex.getCause() instanceof JsonMappingException mapping && !mapping.getPath().isEmpty()) {
            body.setDetail("Dados inválidos.");
            body.setProperty("errors", Map.of(jsonPath(mapping.getPath()), "valor inválido"));
        }
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /** "items[0].quantity" style path, matching the field names Bean Validation reports. */
    private static String jsonPath(List<JsonMappingException.Reference> path) {
        StringBuilder sb = new StringBuilder();
        for (JsonMappingException.Reference ref : path) {
            if (ref.getFieldName() != null) {
                if (!sb.isEmpty()) sb.append('.');
                sb.append(ref.getFieldName());
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }

    /** Adds the requestId to the ProblemDetails built by ResponseEntityExceptionHandler too. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body,
                                                          HttpHeaders headers,
                                                          HttpStatusCode statusCode,
                                                          WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            problem.setProperty("requestId", RequestIdFilter.currentId());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    /** PostgreSQL error code (e.g. 23505 = unique violation) from the first SQLException in the cause chain. */
    @Nullable
    private static String sqlState(Throwable ex) {
        for (Throwable t = ex; t != null && t.getCause() != t; t = t.getCause()) {
            if (t instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return null;
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("requestId", RequestIdFilter.currentId());
        return problem;
    }
}
