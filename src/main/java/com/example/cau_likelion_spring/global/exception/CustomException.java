package com.example.cau_likelion_spring.global.exception;

import lombok.Getter;

/**
 * 프로젝트 전역에서 사용하는 커스텀 예외.
 * 사용 예:
 *   throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
 *   throw new CustomException(ErrorCode.SESSION_NOT_FOUND, "존재하지 않는 세션입니다. id=" + id);
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 기본 메세지
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message); // 입력한 메세지
        this.errorCode = errorCode;
    }
}
