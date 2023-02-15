package com.example.account.exception;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.example.account.type.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.example.account.type.ErrorCode.INVALID_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // AccountException만 처리할 수 있음
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e) {
        log.error("{} is occured", e.getErrorCode());

        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occured", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException is occured", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription());
    }

    // AccountException이 아닌 나머지 Exception을 처리하는 메소드
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(AccountException e) {
        log.error("Exception is occured", e);

        return new ErrorResponse(INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR.getDescription());
    }
}
