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
@RequestMapping(AccountController.BASE_PATH)
public class AccountController {
  static final String BASE_PATH = "/api/v1/accounts";

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public ResponseEntity<List<AccountResponse>> fetchAccounts() {
    List<AccountResponse> accounts = accountService.getAllAccountsWithBalance().stream()
        .map(AccountResponse::from).toList();
    return ResponseEntity.ok().body(accounts);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") Long accountId) {
    return ResponseEntity.ok().body(AccountResponse.from(accountService.getAccountWithBalance(accountId)));
  }

  @GetMapping("/balance")
  public ResponseEntity<TotalBalanceResponse> getTotalBalance() {
    return ResponseEntity.ok().body(new TotalBalanceResponse(accountService.getTotalBalance()));
  }

  @PostMapping
  public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
      UriComponentsBuilder uriBuilder) {
    AccountWithBalance createdAccount = accountService.createAccount(request.name(), request.openingBalance(),
        request.type(), request.color(), request.institution());
    URI location = uriBuilder.path(BASE_PATH + "/{id}").buildAndExpand(createdAccount.id()).toUri();

    return ResponseEntity.created(location).body(AccountResponse.from(createdAccount));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AccountResponse> updateAccount(@PathVariable("id") Long accountId,
      @Valid @RequestBody UpdateAccountRequest request) {
    AccountWithBalance updatedAccount = accountService.updateAccount(accountId, request.name(), request.type(),
        request.color(), request.institution());
    return ResponseEntity.ok().body(AccountResponse.from(updatedAccount));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long accountId) {
    accountService.removeAccount(accountId);
    return ResponseEntity.noContent().build();
  }
}
