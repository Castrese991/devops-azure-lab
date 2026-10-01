package it.castrese.lab.auth;

import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ProblemDetail status(ResponseStatusException e) {
    return ProblemDetail.forStatusAndDetail(
        e.getStatusCode(), e.getReason() == null ? "Request failed" : e.getReason());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ProblemDetail invalid(Exception e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Invalid request: check fields and types");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ProblemDetail conflict(Exception e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "Resource already exists or violates a constraint");
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception e) {
    LoggerFactory.getLogger(ApiErrors.class).error("Unhandled request failure", e);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }
}
