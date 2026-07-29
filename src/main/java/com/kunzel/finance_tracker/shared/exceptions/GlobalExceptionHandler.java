package com.kunzel.finance_tracker.shared.exceptions;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kunzel.finance_tracker.account.exceptions.InsufficientBalanceException;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;

import tools.jackson.databind.DatabindException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {

    List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> Map.of(
            "campo", fieldError.getField(),
            "mensagem", fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "Valor inválido."))
        .toList();

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
    problemDetail.setTitle("Requisição Inválida");
    problemDetail.setProperty("errors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {

    Throwable cause = ex.getCause();

    if (cause instanceof DatabindException jme) {
      Throwable rootCause = jme.getCause();

      String message = (rootCause instanceof IllegalArgumentException && rootCause.getMessage() != null)
          ? rootCause.getMessage()
          : "Valor inválido no corpo da requisição.";

      String field = jme.getPath().stream()
          .map(DatabindException.Reference::getPropertyName)
          .filter(name -> name != null)
          .reduce((first, second) -> second)
          .orElse("desconhecido");

      ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
          HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
      problemDetail.setTitle("Requisição Inválida");
      problemDetail.setProperty("errors", List.of(Map.of("campo", field, "mensagem", message)));

      return ResponseEntity.badRequest().body(problemDetail);
    }

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Corpo da requisição malformado.");
    problemDetail.setTitle("Requisição Inválida");

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Não Encontrado");
    return problemDetail;
  }

  @ExceptionHandler(InvalidBalanceException.class)
  public ProblemDetail handleAccountBalanceException(RuntimeException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Requisição Inválida");
    return problemDetail;
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ProblemDetail handleAccountInsufficientException(RuntimeException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Regra de Negócio Violada");
    return problemDetail;
  }

  @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
  public ProblemDetail handleIllegalArgumentAndState(RuntimeException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Requisição Inválida");
    return problemDetail;
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(RuntimeException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Violação de Integridade de Dados");
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado.");
    problemDetail.setTitle("Erro Interno");
    return problemDetail;
  }
}