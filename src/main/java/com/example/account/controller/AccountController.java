package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final LockService redisTestService;


    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request) {
        log.info("[AccountController] create account");
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(), request.getInitialBalance()
                )
        );
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(
            @RequestParam("user_id") Long id) {

        return accountService.getAccountsByUserId(id). stream()
                .map(AccountInfo::from)
                .collect(Collectors.toList());
    }
    @GetMapping("/account/{accountId}")
    public Account getAccountsByAccountId(
            @PathVariable("accountId") Long id) {
        log.info("[AccountController])");
        return accountService.getAccount(id);
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.from(accountService.deleteAccount(
                request.getUserId(), request.getAccountNumber()));
    }

}
