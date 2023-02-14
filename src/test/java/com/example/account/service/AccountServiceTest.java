package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.AccountRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    @DisplayName("계좌해지 성공")
    void deleteAccount_SUCCESS() {
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto account = accountService.deleteAccount(1L, "12");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertThat(account.getAccountNumber()).isEqualTo("1000000012");
        assertThat(account.getUserId()).isEqualTo(12);
        assertThat(captor.getValue().getAccountStatus())
                .isEqualTo(AccountStatus.UNREGISTERED);
    }

    @Test
    @DisplayName("유저가 존재하지 않아 계좌 해지 실패")
    void deleteAccount_FAIL_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertThat(ErrorCode.USER_NOT_FOUND).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("계좌가 존재하지 않아 계좌 해지 실패")
    void deleteAccount_FAIL_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertThat(ErrorCode.ACCOUNT_NOT_FOUND).isEqualTo(e.getErrorCode());
    }


    @Test
    @DisplayName("계좌소유주와 유저가 달라서 해지실패")
    void deleteAccount_FAIL_UserUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        AccountUser user2 = AccountUser.builder()
                .id(13L)
                .name("임꺽정")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user2)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertThat(ErrorCode.USER_ACCOUNT_UN_MATCH).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("잔액이 있어서 해지실패")
    void deleteAccount_FAIL_balanceNotEmpty() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        AccountUser user2 = AccountUser.builder()
                .id(13L)
                .name("임꺽정")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(10L)
                        .accountNumber("1000000012")
                        .build()));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertThat(ErrorCode.BALANCE_NOT_EMPTY).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 계좌로 해지실패")
    void deleteAccount_FAIL_AccountAlreadyUnRegistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
    }

    @Test
    @DisplayName("유저의 계좌들을 전부 조회")
    void getAccountsByUserId_SUCCESS() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("홍길동")
                .build();
        List<Account> accounts =
                List.of(Account.builder()
                                .accountNumber("123214214")
                                .accountUser(user)
                                .balance(1000L).build(),
                        Account.builder()
                                .accountNumber("11111111")
                                .accountUser(user)
                                .balance(2000L).build(),
                        Account.builder()
                                .accountNumber("22222222")
                                .accountUser(user)
                                .balance(3000L).build());

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        // when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        // then
        assertThat(3).isEqualTo(accountDtos.size());
        assertThat("123214214").isEqualTo(accountDtos.get(0).getAccountNumber());
        assertThat(1000L).isEqualTo(accountDtos.get(0).getBalance());
        assertThat("11111111").isEqualTo(accountDtos.get(1).getAccountNumber());
        assertThat(2000L).isEqualTo(accountDtos.get(1).getBalance());
        assertThat("22222222").isEqualTo(accountDtos.get(2).getAccountNumber());
        assertThat(3000L).isEqualTo(accountDtos.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException e = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        // then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }


}