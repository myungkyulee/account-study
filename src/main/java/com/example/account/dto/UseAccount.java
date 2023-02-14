package com.example.account.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class UseAccount {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotNull
        @Min(1)
        private Long userId;

        // TODO: @NotNull과의 차이점????
        @NotBlank
        @Size(min = 10, max = 10)
        @Min(100)
        private String accountNumber;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Response{
        private Long userId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;

        public static DeleteAccount.Response from(AccountDto accountDto) {
            return DeleteAccount.Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .unRegisteredAt(accountDto.getRegisteredAt())
                    .build();
        }

    }
}
