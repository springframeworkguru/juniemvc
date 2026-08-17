package guru.springframework.juniemvc.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URL = "https://juniemvc.springframework.guru/problems";

    /**
     * Handle BeerOrderException
     */
    @ExceptionHandler(BeerOrderException.class)
    public ResponseEntity<ProblemDetails> handleBeerOrderException(BeerOrderException ex, WebRequest request) {
        ProblemDetails problemDetails = ProblemDetails.builder()
                .type(URI.create(PROBLEM_BASE_URL + "/beer-order-error"))
                .title("Beer Order Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getContextPath()))
                .build();

        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type(URI.create(PROBLEM_BASE_URL + "/validation-error"))
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Input validation failed")
                .instance(URI.create(request.getContextPath()))
                .extensions(errors)
                .build();

        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetails> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        ProblemDetails problemDetails = ProblemDetails.builder()
                .type(URI.create(PROBLEM_BASE_URL + "/constraint-violation"))
                .title("Constraint Violation")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Constraint violation occurred")
                .instance(URI.create(request.getContextPath()))
                .extensions(errors)
                .build();

        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle NotFoundException
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ProblemDetails problemDetails = ProblemDetails.builder()
                .type(URI.create(PROBLEM_BASE_URL + "/not-found"))
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(URI.create(request.getContextPath()))
                .build();

        return new ResponseEntity<>(problemDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleAllExceptions(Exception ex, WebRequest request) {
        ProblemDetails problemDetails = ProblemDetails.builder()
                .type(URI.create(PROBLEM_BASE_URL + "/internal-error"))
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred")
                .instance(URI.create(request.getContextPath()))
                .build();

        return new ResponseEntity<>(problemDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
