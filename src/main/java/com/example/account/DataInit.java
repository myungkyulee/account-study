package com.example.account;

import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInit {
    private final AccountUserRepository accountUserRepository;

    @PostConstruct
    public void init() {
        accountUserRepository.save(
                AccountUser.builder()
                        .name("Pororo")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );


        accountUserRepository.save(
                AccountUser.builder()
                        .name("Eddie")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
        accountUserRepository.save(
                AccountUser.builder()
                        .name("Lupi")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

    }

}
