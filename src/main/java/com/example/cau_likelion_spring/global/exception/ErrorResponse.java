package com.example.cau_likelion_spring.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

/**
 * 모든 에러 응답의 공통 포맷.
 * {
 *   "status": 404,
 *   "code": "SESSION_NOT_FOUND",
 *   "message": "존재하지 않는 세션입니다.",
 *   "errors": []
 * }
 */
@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String code;
    private String message;

    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }

    /** 메시지를 상황에 맞게 덮어쓰고 싶을 때 (예: id 값을 포함시키고 싶을 때) */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.name())
                .message(message)
                .build();
    }

    /** @Valid 검증 실패(MethodArgumentNotValidException)에서 사용 - 어떤 필드가 왜 실패했는지 포함 */
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .errors(ValidationError.of(bindingResult))
                .build();
    }

    @Getter
    @Builder
    public static class ValidationError {
        private String field;
        private String value;
        private String reason;

        public static List<ValidationError> of(BindingResult bindingResult) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> ValidationError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() == null ? "" : error.getRejectedValue().toString())
                            .reason(error.getDefaultMessage())
                            .build())
                    .toList();
        }
    }
}
