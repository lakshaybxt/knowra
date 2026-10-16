package io.github.lakshaybxt.knowra.handler;

import io.github.lakshaybxt.knowra.dto.ApiErrorEnvelope;
import io.github.lakshaybxt.knowra.exception.DocumentProcessingException;
import io.github.lakshaybxt.knowra.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorEnvelope> handleResourceNotFoundException(
      ResourceNotFoundException ex,
      HttpServletRequest request) {

    log.warn("Resource not found: {}", ex.getMessage());

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message(ex.getMessage())
        .status(HttpStatus.NOT_FOUND.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(errorResponse);
  }

  @ExceptionHandler(DocumentProcessingException.class)
  public ResponseEntity<ApiErrorEnvelope> handleDocumentProcessingException(
      DocumentProcessingException ex,
      HttpServletRequest request) {

    log.warn("Document processing error: {}", ex.getMessage());

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message(ex.getMessage())
        .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(errorResponse);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiErrorEnvelope> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex,
      HttpServletRequest request) {

    log.warn("File upload size exceeded: {}", ex.getMessage());

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message("File size exceeds the maximum limit")
        .status(HttpStatus.CONTENT_TOO_LARGE.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(HttpStatus.CONTENT_TOO_LARGE)
        .body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorEnvelope> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> validationErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            DefaultMessageSourceResolvable::getDefaultMessage
        ));

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message("Validation failed for the request")
        .status(HttpStatus.BAD_REQUEST.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .errors(validationErrors)
        .build();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorEnvelope> handleIllegalArgumentException(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    log.warn("Illegal argument: {}", ex.getMessage());

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message(ex.getMessage())
        .status(HttpStatus.BAD_REQUEST.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
  }

  // Handles unexpected exceptions (500)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorEnvelope> handleGlobalException(
      Exception ex,
      HttpServletRequest request) {

    log.error("Unexpected error occurred at {}",
        request.getRequestURI(), ex);

    ApiErrorEnvelope errorResponse = ApiErrorEnvelope.builder()
        .success(false)
        .message("An unexpected error occurred")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }
}
