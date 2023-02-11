package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_VOT_FOUND("사용자가 없습니다.");

    private final String description;
}
