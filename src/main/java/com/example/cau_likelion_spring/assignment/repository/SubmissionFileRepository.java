package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {

    void deleteAllByAssignmentSubmit_Assignment(Assignment assignment);

    /** 과제 삭제 시 S3에 올라간 첨부파일을 함께 정리하기 위해, DB에서 지우기 전에 URL 목록을 먼저 조회 */
    List<SubmissionFile> findAllByAssignmentSubmit_Assignment(Assignment assignment);

    /** PENDING 상태의 제출을 '수정'할 때 기존 첨부파일을 지우고 새로 넣기 위해 사용 */
    void deleteAllByAssignmentSubmit(AssignmentSubmit assignmentSubmit);

    List<SubmissionFile> findAllByAssignmentSubmit(AssignmentSubmit assignmentSubmit);

    List<SubmissionFile> findAllByAssignmentSubmitIn(List<AssignmentSubmit> assignmentSubmits);

    /** 회원 탈퇴/삭제 시 본인 제출물에 딸린 첨부파일을 S3에서 함께 정리하기 위해, DB에서 지우기 전에 URL 목록을 먼저 조회 */
    List<SubmissionFile> findAllByAssignmentSubmit_SubmitMember_Id(Long memberId);

    void deleteAllByAssignmentSubmit_SubmitMember_Id(Long memberId);
}
