package com.dmasone.identity.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts application and validation exceptions into RFC 7807 Problem Details.
 *
 * <p>Centralizing exception translation keeps controllers focused on HTTP
 * orchestration and gives API consumers consistent error payloads across
 * versions.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts duplicate-email failures into HTTP 409 responses.
     *
     * @param ex duplicate email exception
     * @return problem detail payload for the API response
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Email already exists");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Converts missing-user failures into HTTP 404 responses.
     *
     * @param ex missing user exception
     * @return problem detail payload for the API response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("User not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Converts bean validation errors into HTTP 400 responses with field details.
     *
     * @param ex validation exception raised by Spring MVC
     * @return problem detail payload with an {@code errors} extension
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error");
        problem.setDetail("Invalid request payload");
        problem.setProperty("errors", fieldErrors(ex));
        return problem;
    }

    /**
     * Converts invalid path parameters, such as malformed UUIDs, into HTTP 400 responses.
     *
     * @param ex type mismatch raised by Spring MVC
     * @return problem detail payload for the API response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid path parameter");
        problem.setDetail("Parameter '%s' has an invalid value".formatted(ex.getName()));
        return problem;
    }

    /**
     * Extracts validation messages in a stable field-name keyed structure.
     *
     * @param ex validation exception raised by Spring MVC
     * @return field error messages ordered as reported by the binding result
     */
    private Map<String, String> fieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }
}
