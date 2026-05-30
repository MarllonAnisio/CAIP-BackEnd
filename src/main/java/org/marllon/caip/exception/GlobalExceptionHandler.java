package org.marllon.caip.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.marllon.caip.exception.StandardError.ValidationError;
import org.marllon.caip.exception.auth_exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura regras de negocio disparadas pelo sistema (erro 400)
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardError> handleBusinessRule(
            BusinessRuleException e, HttpServletRequest request) {

        log.warn("Business Rule Violation [{}]: {}", request.getRequestURI(), e.getMessage());

        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),  // 400
                "Violação de Regra de Negócio",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Capturando erros de validação de json do (@valid) (erro 400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation Error [{}]", request.getRequestURI());

        List<ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ValidationError(err.getField(), err.getDefaultMessage()))
                .toList();
        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação de Dados",
                "Alguns campos são inválidos. Verifique os detalhes.",
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Capturando erros do nosso Filtro JWT customizado (A peça que faltava!)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardError> handleUnauthorized(UnauthorizedException e, HttpServletRequest request) {
        log.warn("Unauthorized Access Attempt [{}]: {}", request.getRequestURI(), e.getMessage());

        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Não Autorizado",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    /**
     * Capturando erros de login padrão e permissões de Roles (erro 401 ou 403)
     */
    @ExceptionHandler({AccessDeniedException.class, BadCredentialsException.class})
    public ResponseEntity<StandardError> handleSecurity(Exception e, HttpServletRequest request) {
        log.warn("Security Error [{}]: {}", request.getRequestURI(), e.getMessage());

        HttpStatus status = (e instanceof BadCredentialsException) ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String message = (e instanceof BadCredentialsException) ? "Email ou senha incorretos" : "Você não tem permissão para acessar este recurso";

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro de Segurança",
                message,
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Fallback para erros catastróficos inesperados (erro 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleUnexpectedException(Exception e, HttpServletRequest request) {
        // Como esse é um erro grosseiro, tem que ter Stracktrace completo
        log.error("ERRO INTERNO CRÍTICO em [{}]: ", request.getRequestURI(), e);

        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno do Servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(
            ResourceNotFoundException e, HttpServletRequest request) {
        log.info("Resource Not Found [{}]: {}", request.getRequestURI(), e.getMessage());

        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }
}