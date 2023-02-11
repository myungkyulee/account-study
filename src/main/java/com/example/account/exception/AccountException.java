package com.example.account.exception;


import com.example.account.type.ErrorCode;
import lombok.*;

import javax.persistence.SecondaryTable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// checked Exception은 트랜잭션을 롤백해주지 않기 때문에 잘 쓰지 않는다.
// 코드상에서도 전부다 써줘야해서 불편하다.
public class AccountException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
