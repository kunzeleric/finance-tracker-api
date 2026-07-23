package com.kunzel.finance_tracker.shared.exceptions;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kunzel.finance_tracker.account.exceptions.InsufficientBalanceException;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {

    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .toList();

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
    problemDetail.setTitle("Requisição Inválida");
    problemDetail.setProperty("errors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Não Encontrado");
    return problemDetail;
  }

  @ExceptionHandler({ InvalidBalanceException.class, InsufficientBalanceException.class })
  public ProblemDetail handleAccountDomainErrors(RuntimeException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Regra de Negócio Violada");
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Requisição Inválida");
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