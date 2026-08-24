package com.example.cau_likelion_spring.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 우리가 직접 던지는 커스텀 예외 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException: code={}, message={}", errorCode.name(), e.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /** @Valid 검증 실패 (요청 DTO의 @NotBlank, @NotNull 등) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getBindingResult()));
    }

    /** 필수 @RequestParam이 요청에서 아예 빠졌을 때 (예: /api/assignments/president?partId= 없이 호출) */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());

        String message = "필수 파라미터가 누락되었습니다: " + e.getParameterName();
        return ResponseEntity.status(ErrorCode.MISSING_PARAMETER.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.MISSING_PARAMETER, message));
    }

    /** @RequestParam/@PathVariable 값이 기대한 타입으로 변환 안 될 때 (예: Long 자리에 문자열, enum에 없는 값) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: name={}, value={}", e.getName(), e.getValue());

        String message = String.format("'%s' 값이 올바르지 않습니다: %s", e.getName(), e.getValue());
        return ResponseEntity.status(ErrorCode.TYPE_MISMATCH.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.TYPE_MISMATCH, message));
    }

    /** @PreAuthorize 권한 검사 실패 (예: STAFF 전용 API를 ADMIN이 호출했는데 ADMIN이 누락된 경우 등) */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("AuthorizationDeniedException: {}", e.getMessage());

        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    /** spring.servlet.multipart.max-file-size/max-request-size(application.yml)를 넘는 업로드 - UploadDomain별 제한 검증 전에 여기서 먼저 걸림 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException: {}", e.getMessage());

        return ResponseEntity.status(ErrorCode.FILE_SIZE_EXCEEDED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED));
    }

    /** 위에서 잡지 못한 모든 예외 - 마지막 안전망. 스택트레이스는 로그로만 남기고 응답엔 노출 안 함 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
