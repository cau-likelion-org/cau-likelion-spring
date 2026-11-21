package com.example.cau_likelion_spring.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전역에서 사용하는 에러 코드.
 * 새로운 예외 상황이 생기면 여기에 하나씩 추가하고, CustomException(ErrorCode.XXX)로 던지면 된다.
 */
@Getter
public enum ErrorCode {

    // ===== Common (400/405/500) =====
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 값이 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // ===== Auth =====
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리프레시 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ===== Member =====
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_DELETE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "회장/관리자 권한의 구성원은 삭제할 수 없습니다."),
    EMAIL_NOT_ALLOWED(HttpStatus.FORBIDDEN, "회원가입이 허용되지 않은 이메일입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    ALLOWED_USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예비 회원입니다."),

    // ===== Organization (Generation / Part) =====
    GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 기수입니다."),
    CURRENT_GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 기수로 지정된 기수가 없습니다."),
    PART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파트입니다."),
    DUPLICATE_GENERATION(HttpStatus.CONFLICT, "이미 존재하는 기수입니다."),

    // ===== Session =====
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."),

    // ===== Intro (Track / Activity / FAQ / DesiredTalent / Curriculum) =====
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 트랙입니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 활동입니다."),
    FAQ_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 FAQ입니다."),
    FAQ_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "순서 변경 요청에 전체 FAQ id가 중복 없이 모두 포함되어야 합니다."),
    DESIRED_TALENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 인재상입니다."),
    CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 커리큘럼입니다."),
    ROADMAP_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 로드맵 이미지가 없습니다."),

    // ===== Project =====
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트입니다."),
    PROJECT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트 이미지입니다."),
    PROJECT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트 링크입니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트 멤버입니다."),
    PLATFORM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 플랫폼입니다."),

    // ===== Attendance =====
    WEEKLY_ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 출석 이벤트입니다."),
    DETAIL_ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 출석 기록입니다."),
    INVALID_ATTENDANCE_PASSWORD(HttpStatus.BAD_REQUEST, "출석 비밀번호가 올바르지 않습니다."),
    ALREADY_CHECKED_ATTENDANCE(HttpStatus.CONFLICT, "이미 출석 체크가 완료되었습니다."),
    DUPLICATE_WEEKLY_ATTENDANCE(HttpStatus.CONFLICT, "해당 날짜에 이미 출석부가 존재합니다."),
    ATTENDANCE_CHECK_CLOSED(HttpStatus.BAD_REQUEST, "출석 가능한 시간이 아닙니다."),
    ATTENDANCE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "결석 또는 공결로 변경할 때는 사유를 입력해야 합니다."),

    // ===== Assignment =====
    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 과제입니다."),
    ASSIGNMENT_SUBMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 과제 제출 내역입니다."),
    ASSIGNMENT_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 제출한 과제입니다."),
    ASSIGNMENT_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "과제 제출 기한이 지났습니다."),
    ASSIGNMENT_PART_MISMATCH(HttpStatus.FORBIDDEN, "본인 파트의 과제만 관리할 수 있습니다."),
    ASSIGNMENT_MEMBER_PART_MISMATCH(HttpStatus.FORBIDDEN, "과제 파트에 속하지 않은 아기사자입니다."),
    ASSIGNMENT_STAFF_PART_NOT_ASSIGNED(HttpStatus.CONFLICT, "운영진에게 배정된 파트가 없습니다."),
    ASSIGNMENT_BABY_LION_PART_NOT_ASSIGNED(HttpStatus.CONFLICT, "아기사자에게 배정된 파트가 없습니다."),

    // ===== Gallery =====
    GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 갤러리입니다."),
    GALLERY_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 갤러리 이미지입니다."),

    // ===== History =====
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 히스토리입니다."),
    HISTORY_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 히스토리 이미지입니다."),

    // ===== Project Gallery =====
    PROJECT_GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로젝트 게시물입니다."),

    // ===== Blog =====
    BLOG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 블로그 글입니다."),
    INVALID_SCRAPING_URL(HttpStatus.BAD_REQUEST, "유효하지 않은 스크래핑 URL입니다."),
    BLOG_SCRAPING_FAILED(HttpStatus.BAD_GATEWAY, "블로그 스크래핑에 실패했습니다."),

    // ===== Notification =====
    SUBSCRIBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 구독자입니다."),
    RECRUITMENT_TEXT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모집 공고입니다."),
    RECRUITMENT_TEXT_ALREADY_SENT(HttpStatus.CONFLICT, "이미 발송된 모집 공고입니다."),
    RECRUITMENT_TEXT_NO_RESEND_TARGET(HttpStatus.CONFLICT, "재전송할 실패 대상이 없습니다."),

    // ===== Recruitment =====
    RECRUITMENT_ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "진행 중인 모집 회차가 없습니다."),
    RECRUITMENT_INVALID_ROUND_SCHEDULE(HttpStatus.BAD_REQUEST, "모집공고 일정 순서가 올바르지 않습니다."),
    RECRUITMENT_ROUND_PART_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 회차의 지원 파트가 아닙니다."),
    RECRUITMENT_ROUND_PART_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 정원이 등록된 파트입니다."),
    RECRUITMENT_DUPLICATE_PART_HEAD_COUNT(HttpStatus.BAD_REQUEST, "같은 파트의 정원이 중복으로 요청되었습니다."),
    RECRUITMENT_APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지원자입니다."),
    RECRUITMENT_NOT_IN_APPLY_PERIOD(HttpStatus.BAD_REQUEST, "지원서 접수 기간이 아닙니다."),
    RECRUITMENT_INVALID_INTERVIEW_TIME(HttpStatus.BAD_REQUEST, "선택할 수 없는 면접 가능 시간입니다."),
    RECRUITMENT_ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 지원 내역이 있습니다."),
    RECRUITMENT_DUPLICATE_STUDENT_NUMBER(HttpStatus.CONFLICT, "이미 지원 내역이 있는 학번입니다."),
    RECRUITMENT_INVALID_ANSWER(HttpStatus.BAD_REQUEST, "지원서 답변이 올바르지 않습니다."),
    RECRUITMENT_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 회차의 2단계 문항이 아닙니다."),
    RECRUITMENT_INVALID_QUESTION(HttpStatus.BAD_REQUEST, "2단계 문항 입력값이 올바르지 않습니다."),
    RECRUITMENT_QUESTION_LOCKED(HttpStatus.CONFLICT, "서류 접수가 시작된 후에는 문항을 수정할 수 없습니다."),
    RECRUITMENT_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 회차에 제출된 지원서가 아닙니다."),
    RECRUITMENT_NOT_DOCUMENT_PASSED(HttpStatus.BAD_REQUEST, "서류 합격자만 입력할 수 있습니다."),
    RECRUITMENT_DUPLICATE_APPLICATION_ID(HttpStatus.BAD_REQUEST, "같은 지원서가 중복으로 요청되었습니다."),
    RECRUITMENT_INVALID_RESULT_TEMPLATE(HttpStatus.BAD_REQUEST, "결과 안내 문구 입력값이 올바르지 않습니다."),
    RECRUITMENT_INVALID_PUBLISH_SCHEDULE(HttpStatus.BAD_REQUEST, "결과 공개 일정이 올바르지 않습니다."),
    RECRUITMENT_PUBLISH_CONDITION_NOT_MET(HttpStatus.CONFLICT, "결과 공개 조건을 만족하지 않습니다."),
    RECRUITMENT_PUBLISH_STATE_CONFLICT(HttpStatus.CONFLICT, "현재 결과 공개 상태에서는 처리할 수 없습니다."),
    RECRUITMENT_RESULT_LOCKED(HttpStatus.CONFLICT, "결과 공개 예약 중에는 수정할 수 없습니다. 예약을 취소한 후 수정해주세요."),
    RECRUITMENT_EMAIL_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "인증번호는 잠시 후 다시 요청해주세요."),
    RECRUITMENT_EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증번호 이메일 발송에 실패했습니다."),
    RECRUITMENT_EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    RECRUITMENT_EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    RECRUITMENT_EMAIL_CODE_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "인증번호 입력 횟수를 초과했습니다. 인증번호를 다시 요청해주세요."),

    // ===== File Upload (global/util) =====
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 허용된 용량을 초과했습니다."),
    IMAGE_PROCESSING_BUSY(HttpStatus.TOO_MANY_REQUESTS, "지금 이미지 처리 요청이 많아 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
