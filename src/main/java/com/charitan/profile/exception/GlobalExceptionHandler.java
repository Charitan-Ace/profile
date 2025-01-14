package com.charitan.profile.exception;

import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
    // Collect error messages from the exception
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    ErrorResponse errorResponse = new ErrorResponse(errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(value = ParseException.class)
  ResponseEntity<Object> handleParseException(ParseException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User unauthenticated");
  }

  @ExceptionHandler(value = AuthorizationDeniedException.class)
  ResponseEntity<Object> handlingAuthorizationDeniedException(
      AuthorizationDeniedException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized");
  }

  @ExceptionHandler(value = AccessDeniedException.class)
  ResponseEntity<Object> handlingAccessDeniedException(AuthorizationDeniedException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized");
  }
}
