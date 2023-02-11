package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;


    /**
     * 사용자가 있는지 조회
     * 계좌 번호를 생성하고
     * 계좌를 저장하고 반환값을 넘긴다.
     */

    // AccountDto를 사용하는 이유는 Entity 클래스는 특별한 클래스이기 때문에
    // lazy로딩을 할 때 오류가 발생할 수 있다.
    // 또한 필요한 정보가 적을 수도 많을 수도 있기 때문에 dto클래스를 사용한다.
    // 레이어 간에 데이터를 넘길 때 Entity 클래스는 잘 쓰지 않는다.
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        // user가 있다면 user를 반환하고 없다면 예외를 던진다.
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_VOT_FOUND));

        // 최근에 등록된 계좌번호를 조회하고 거기에 1을 더해서 새로운 계좌번호를 얻는다.
        String newAccountNumber = accountUserRepository.findFirstByOrderByIdDesc()
                .map(account -> Integer.parseInt(account.getAccountNumber()) + 1 + "")
                .orElse("1000000000"); // 처음 등록되는 계좌번호

        return AccountDto.fromEntity(
                accountRepository.save(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber(newAccountNumber)
                                .balance(initialBalance)
                                .registeredAt(LocalDateTime.now())
                                .accountStatus(AccountStatus.IN_USE)
                                .build()
                )
        );
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }
}
