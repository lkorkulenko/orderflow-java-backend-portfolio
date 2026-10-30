package dev.portfolio.orderflow.api;

import dev.portfolio.orderflow.domain.InvalidOrderTransitionException;
import dev.portfolio.orderflow.service.OrderNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ApiErrors {
    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(status.getReasonPhrase());
        return p;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail notFound(OrderNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({InvalidOrderTransitionException.class,
                       DataIntegrityViolationException.class,
                       ObjectOptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail conflict(RuntimeException ex) {
        // Avoid leaking vendor/database error details in a public response.
        String detail = ex instanceof InvalidOrderTransitionException
            ? ex.getMessage() : "The request conflicts with existing data or a concurrent change";
        return problem(HttpStatus.CONFLICT, detail);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail badRequest(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request parameters or JSON payload");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail invalidPayload(MethodArgumentNotValidException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Request validation failed");
    }
}
