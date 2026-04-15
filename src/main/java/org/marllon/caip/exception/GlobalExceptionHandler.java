package org.marllon.caip.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.marllon.caip.exception.BusinessRuleException;
import org.marllon.caip.exception.StandardError;
import org.marllon.caip.exception.StandardError.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura regras de negocio disparadas pelo sistema (erro 400)
     * */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardError> handleBusinessRule(BusinessRuleException e, HttpServletRequest request) {
        StandardError err = new StandardError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Violação de Regra de Negócio",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Capturando erros do banco de dados como not found (erro 404)
     * */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> handleNotFound(EntityNotFoundException e, HttpServletRequest request) {
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

    /**
     * capturando erros de validação de json do (@valid) (erro 400)
     * */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
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
     * capturando erros de login e acesso a recursos protegidos (erro 401 ou 403)
     * */
    @ExceptionHandler({AccessDeniedException.class, BadCredentialsException.class})
    public ResponseEntity<StandardError> handleSecurity(Exception e, HttpServletRequest request) {

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
     * Fallback para erros inesperados (erro 500)
     * */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("Erro interno inesperado", e);

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

}
