package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .balanceSnapshot(999L)
                                .transactionId("transactionId")
                                .transactionResultType(S)
                                .transactionType(USE)
                                .amount(1L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto result = transactionService.useBalance(1L, "d", 100L);

        // then
        assertThat(result.getBalanceSnapshot()).isEqualTo(999L);
        assertThat(result.getAmount()).isEqualTo(1L);
        assertThat(result.getTransactionResultType()).isEqualTo(S);
        assertThat(result.getTransactionType()).isEqualTo(USE);
    }

    @Test
    @DisplayName("????????? ???????????? ?????? ?????? ?????? ??????")
    void useBalance_FAIL_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.useBalance(41L, "1234567890", 100L));

        //then
        Assertions.assertThat(ErrorCode.USER_NOT_FOUND).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("????????? ???????????? ?????? ?????? ?????? ??????")
    void useBalance_FAIL_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1L));

        //then
        Assertions.assertThat(ErrorCode.ACCOUNT_NOT_FOUND).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("?????????????????? ????????? ????????? ????????????")
    void useBalance_FAIL_UserUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        AccountUser user2 = AccountUser.builder()
                .id(13L)
                .name("?????????")
                .build();
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
                () -> transactionService.useBalance(1L, "1234567890", 1L));

        //then
        Assertions.assertThat(ErrorCode.USER_ACCOUNT_UN_MATCH).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("?????? ????????? ????????? ????????????")
    void useBalance_FAIL_AccountAlreadyUnRegistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
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
                () -> transactionService.useBalance(1L, "1234567890", 1L));

        //then
        Assertions.assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AMOUNT_EXCEED_BALANCE);
    }

    @Test
    @DisplayName("?????? ????????? ???????????? ??? ??????")
    void exceedAmount_useBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(user)
                .balance(999L)
                .accountNumber("100000015").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "123456", 1000L));
        //then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AMOUNT_EXCEED_BALANCE);
    }

    @Test
    @DisplayName("?????? ???????????? ?????? ??????")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(user)
                .balance(999L)
                .accountNumber("100000015").build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));


        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .balanceSnapshot(999L)
                                .transactedAt(LocalDateTime.now())
                                .transactionId("transactionId")
                                .transactionResultType(S)
                                .transactionType(USE)
                                .amount(1L)
                                .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        transactionService.saveFailedUseTransaction("1000000000", 100L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(100L);
        assertThat(captor.getValue().getBalanceSnapshot()).isEqualTo(999L);
        assertThat(captor.getValue().getTransactionResultType()).isEqualTo(F);
    }

    @Test
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .balanceSnapshot(999L)
                .transactedAt(LocalDateTime.now())
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .amount(200L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .balanceSnapshot(10000L)
                                .transactionId("transactionIdForCancel")
                                .transactionResultType(S)
                                .transactionType(CANCEL)
                                .amount(200L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto result = transactionService.cancelBalance("transactionIdForCancel", "d", 200L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getBalanceSnapshot()).isEqualTo(1000L + 200L);
        assertThat(captor.getValue().getAmount()).isEqualTo(200L);

        assertThat(result.getTransactionResultType()).isEqualTo(S);
        assertThat(result.getTransactionType()).isEqualTo(CANCEL);
        assertThat(result.getBalanceSnapshot()).isEqualTo(10000L);
        assertThat(result.getAmount()).isEqualTo(200L);
    }


    @Test
    @DisplayName("????????? ???????????? ?????? ???????????? ?????? ??????")
    void cancelBalance_FAIL_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(
                        Transaction.builder()
                                .balanceSnapshot(10000L)
                                .transactionId("transactionId")
                                .transactionResultType(S)
                                .transactionType(USE)
                                .amount(200L)
                                .build()
                ));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 1L));

        //then
        Assertions.assertThat(ErrorCode.ACCOUNT_NOT_FOUND).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("????????? ???????????? ?????? ???????????? ?????? ??????")
    void cancelBalance_FAIL_TransactionNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 1L));

        //then
        Assertions.assertThat(ErrorCode.TRANSACTION_NOT_FOUND).isEqualTo(e.getErrorCode());
    }

    @Test
    @DisplayName("??????????????? ????????? ????????? ???????????? ?????? ???")
    void cancelBalance_FAIL_TransactionAccountUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        Account account2 = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(2L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000012").build();

        Transaction transaction = Transaction.builder()
                .account(account2)
                .balanceSnapshot(999L)
                .transactedAt(LocalDateTime.now())
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .amount(200L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 1L));

        //then
        Assertions.assertThat(e.getErrorCode()).isEqualTo(TRANSACTION_ACCOUNT_UN_MATCH);
    }

    @Test
    @DisplayName("??????????????? ????????? ????????? ??? ????????? ?????? ???(???????????? ??????)")
    void cancelBalance_FAIL_CancelMustFully() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        Account account2 = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(2L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .balanceSnapshot(999L)
                .transactedAt(LocalDateTime.now())
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .amount(200L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 201L));

        //then
        Assertions.assertThat(e.getErrorCode()).isEqualTo(CANCEL_MUST_FULLY);
    }

    @Test
    @DisplayName("1????????? ?????? ????????? ????????? ????????? ??????")
    void cancelBalance_FAIL_TooOldOrder() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        Account account2 = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(2L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .balanceSnapshot(999L)
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .amount(200L)
                .build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890", 200L));

        //then
        Assertions.assertThat(e.getErrorCode()).isEqualTo(TOO_OLD_ORDER_TO_CANCEL);
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("?????????")
                .build();
        Account account = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(1L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000015").build();

        Account account2 = Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .id(2L)
                .accountUser(user)
                .balance(1000L)
                .accountNumber("100000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .balanceSnapshot(999L)
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .amount(200L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");

        //then
        Assertions.assertThat(transactionDto.getTransactionType()).isEqualTo(USE);
        Assertions.assertThat(transactionDto.getTransactionResultType()).isEqualTo(S);
        Assertions.assertThat(transactionDto.getAmount()).isEqualTo(200L);
        Assertions.assertThat(transactionDto.getTransactionId()).isEqualTo("transactionId");
    }


    @Test
    @DisplayName("????????? ?????? -?????? ?????? ??????")
    void queryTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        // when
        AccountException e = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        // then
        assertThat(e.getErrorCode()).isEqualTo(TRANSACTION_NOT_FOUND);
    }
}