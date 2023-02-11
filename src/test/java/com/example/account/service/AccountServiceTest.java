package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    // accountService가 Repository에 의존하고 있기 때문에
    // 가짜를 만들어서 accountRepository에 주입해준다.
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    // 위에서 만들어준 리포지토리를 넣어준다.
    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌생성 성공")
    void createAccount_SUCCESS() {
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("120").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("121").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto account = accountService.createAccount(1L, 100L);

        // then
        assertThat(account.getAccountNumber()).isEqualTo("121");
        assertThat(account.getUserId()).isEqualTo(12);
    }

    @Test
    @DisplayName("첫 번째 계좌 생성 성공")
    void createFirstAccount_SUCCESS() {
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("100000015").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto account = accountService.createAccount(1L, 100L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getAccountNumber()).isEqualTo("1000000000");
        assertThat(account.getUserId()).isEqualTo(12L);
    }

    @Test
    @DisplayName("유저가 존재하지 않을 경우")
    void createAccount_FAIL_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertThat(ErrorCode.USER_NOT_FOUND).isEqualTo(e.getErrorCode());
    }


    @Test
    @DisplayName("사용자 보유 계좌 10개 이상일 경우 개좌 개설 불가")
    void createAccount_maxAccountIs10() {
        // given
        AccountUser user = AccountUser.builder().id(15L).name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .build()));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(11);

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MAX_ACCOUNT_PER_USER_10);
    }
}