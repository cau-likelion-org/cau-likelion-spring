package com.example.cau_likelion_spring.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * assignment 도메인에서 발생하는 예외를 모아둔 클래스. 어디서도 타입으로 catch하지 않고
 * 상태코드+메시지만 쓰이므로, 케이스별 서브클래스 대신 static factory로 구분한다.
 */
public class AssignmentException extends ResponseStatusException {

    private AssignmentException(HttpStatus status, String message) {
        super(status, message);
    }

    public static AssignmentException notFound(Long id) {
        return new AssignmentException(HttpStatus.NOT_FOUND, "존재하지 않는 과제입니다. id=" + id);
    }

    public static AssignmentException partMismatch(Long assignmentId) {
        return new AssignmentException(HttpStatus.FORBIDDEN, "본인 파트의 과제만 관리할 수 있습니다. assignmentId=" + assignmentId);
    }

    public static AssignmentException memberPartMismatch(Long memberId) {
        return new AssignmentException(HttpStatus.FORBIDDEN, "과제 파트에 속하지 않은 아기사자입니다. memberId=" + memberId);
    }

    public static AssignmentException staffPartNotAssigned(Long staffMemberId) {
        return new AssignmentException(HttpStatus.CONFLICT, "운영진에게 배정된 파트가 없습니다. memberId=" + staffMemberId);
    }

    public static AssignmentException babyLionPartNotAssigned(Long memberId) {
        return new AssignmentException(HttpStatus.CONFLICT, "아기사자에게 배정된 파트가 없습니다. memberId=" + memberId);
    }

    public static AssignmentException submissionClosed(Long assignmentId) {
        return new AssignmentException(HttpStatus.CONFLICT, "제출 가능 기한이 지나 더 이상 제출할 수 없습니다. assignmentId=" + assignmentId);
    }

    public static AssignmentException alreadyApproved(Long assignmentId) {
        return new AssignmentException(HttpStatus.CONFLICT, "이미 승인된 과제는 다시 제출할 수 없습니다. assignmentId=" + assignmentId);
    }

    public static AssignmentException submitNotFound(Long submitId) {
        return new AssignmentException(HttpStatus.NOT_FOUND, "존재하지 않는 제출입니다. submitId=" + submitId);
    }

    public static AssignmentException invalidSubmission(String message) {
        return new AssignmentException(HttpStatus.BAD_REQUEST, message);
    }
}
