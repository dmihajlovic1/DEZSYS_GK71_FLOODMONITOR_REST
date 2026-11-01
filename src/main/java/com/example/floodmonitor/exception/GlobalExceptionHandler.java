package com.example.floodmonitor.exception;

import com.example.floodmonitor.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Zentrale Fehlerbehandlung: alle Fehler im gleichen Format (JSON oder XML je nach Accept),
 * nie mit Stacktrace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // 404 – z. B. Station ST-999 gibt es nicht
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
  }

  // 400 – ungültige Parameter (Datum, from > to, limit, Warnstufe ...)
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
  }

  // 400 – Parameter hat falschen Typ
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST,
            "Ungültiger Wert '" + ex.getValue() + "' für Parameter '" + ex.getName() + "'", req);
  }

  // 406 – Client will z. B. text/plain. Antwort bewusst als JSON, weil das gewünschte Format nicht geht.
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ApiError> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest req) {
    ApiError body = new ApiError(406, "Not Acceptable",
            "Format nicht unterstützt. Erlaubt: application/json, application/xml", req.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
  }

  // Alles andere: Spring-Fehler (405, 415, unbekannter Pfad ...) behalten ihren Status, sonst 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
    if (ex instanceof ErrorResponse er) {
      String detail = er.getBody().getDetail();
      return build(er.getStatusCode(), detail != null ? detail : ex.getMessage(), req);
    }
    log.error("Unerwarteter Fehler bei {}", req.getRequestURI(), ex); // nur im Server-Log
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Interner Serverfehler", req);
  }

  private ResponseEntity<ApiError> build(HttpStatusCode status, String message, HttpServletRequest req) {
    HttpStatus s = HttpStatus.resolve(status.value());
    String reason = s != null ? s.getReasonPhrase() : "Error";
    return ResponseEntity.status(status).body(new ApiError(status.value(), reason, message, req.getRequestURI()));
  }
}