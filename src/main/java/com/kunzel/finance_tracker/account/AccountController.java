package com.kunzel.finance_tracker.account;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.kunzel.finance_tracker.account.dtos.AccountResponse;
import com.kunzel.finance_tracker.account.dtos.CreateAccountRequest;
import com.kunzel.finance_tracker.account.dtos.TotalBalanceResponse;
import com.kunzel.finance_tracker.account.dtos.UpdateAccountRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public ResponseEntity<List<AccountResponse>> fetchAccounts() {
    List<AccountResponse> accounts = accountService.getAllAccounts().stream().map(AccountResponse::new).toList();
    return ResponseEntity.ok().body(accounts);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") Long accountId) {
    Account accountToBeFound = accountService.getAccountById(accountId);
    return ResponseEntity.ok().body(new AccountResponse(accountToBeFound));
  }

  @GetMapping("/balance")
  public ResponseEntity<TotalBalanceResponse> getTotalBalance() {
    return ResponseEntity.ok().body(new TotalBalanceResponse(accountService.getTotalBalance()));
  }

  @PostMapping
  public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
      UriComponentsBuilder uriBuilder) {
    Account createdAccount = accountService.createAccount(request.name(), request.initialBalance(), request.type());
    URI location = uriBuilder.path("/accounts/{id}").buildAndExpand(createdAccount.getId()).toUri();

    return ResponseEntity.created(location).body(new AccountResponse(createdAccount));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AccountResponse> updateAccount(@PathVariable("id") Long accountId,
      @Valid @RequestBody UpdateAccountRequest request) {
    Account updatedAccount = accountService.updateAccount(accountId, request.name(), request.type());
    return ResponseEntity.ok().body(new AccountResponse(updatedAccount));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long accountId) {
    accountService.removeAccount(accountId);
    return ResponseEntity.noContent().build();
  }
}
