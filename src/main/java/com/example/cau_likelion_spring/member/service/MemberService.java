package com.example.cau_likelion_spring.member.service;

import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.assignment.repository.SubmissionFileRepository;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.auth.repository.RefreshTokenRepository;
import com.example.cau_likelion_spring.member.domain.FcmToken;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.dto.FcmTokenRequest;
import com.example.cau_likelion_spring.member.dto.MemberResponse;
import com.example.cau_likelion_spring.member.dto.MemberUpdateRequest;
import com.example.cau_likelion_spring.member.dto.PushSettingRequest;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.member.repository.FcmTokenRepository;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PartRepository partRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AssignmentSubmitRepository assignmentSubmitRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final PushNotiLogRepository pushNotiLogRepository;
    private final AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;
    private final DetailAttendanceRepository detailAttendanceRepository;
    private final S3Uploader s3Uploader;

    public MemberResponse getMyInfo(Long memberId) {
        return MemberResponse.from(getMember(memberId));
    }

    /** 이름/기수/파트/권한으로 구성원을 검색한다. 각 파라미터는 선택이며, 지정하지 않으면 전체 조회된다. */
    public List<MemberResponse> getAll(String name, Integer generationNumber, Long partId, MemberRole role) {
        return memberRepository.search(name, generationNumber, partId, role).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse update(Long id, MemberUpdateRequest request) {
        Member member = getMember(id);
        Part part = getPart(request.partId());
        validateEmailNotTaken(member, request.email());

        member.update(request.name(), request.email(), request.role(), part);

        return MemberResponse.from(member);
    }

    /**
     * 본인 기기의 FCM 토큰을 등록한다. 기기(브라우저)마다 별도로 등록되므로 여러 개를 동시에 가질 수 있다.
     * 이미 등록된 토큰이면(재등록/갱신) 그대로 유지되고, 다른 구성원이 쓰던 토큰이면(기기 재사용, 계정 전환) 이 구성원 소유로 옮겨진다.
     * 계정의 푸시 알림 수신 설정(pushEnabled)도 함께 켜진다.
     */
    @Transactional
    public void registerFcmToken(Long memberId, FcmTokenRequest request) {
        Member member = getMember(memberId);
        FcmToken fcmToken = fcmTokenRepository.findByToken(request.fcmToken())
                .orElseGet(() -> FcmToken.builder().member(member).token(request.fcmToken()).build());
        fcmToken.reassignTo(member);
        fcmTokenRepository.save(fcmToken);
        member.enablePush();
    }

    /**
     * 본인 기기의 FCM 토큰을 삭제한다 (로그아웃 시 등). 등록돼 있지 않아도 조용히 넘어간다.
     * 이 기기의 토큰만 지워질 뿐 계정의 푸시 알림 수신 설정(pushEnabled)은 바뀌지 않으므로,
     * 다시 로그인해 토큰을 재등록하면 이전 설정 그대로 알림을 받는다.
     */
    @Transactional
    public void deleteFcmToken(Long memberId, FcmTokenRequest request) {
        fcmTokenRepository.deleteByMember_IdAndToken(memberId, request.fcmToken());
    }

    /** 계정의 푸시 알림 수신 여부를 사용자가 직접 켜고 끈다. 기기별 FCM 토큰과 별개로 로그아웃해도 유지된다. */
    @Transactional
    public MemberResponse updatePushSetting(Long memberId, PushSettingRequest request) {
        Member member = getMember(memberId);
        if (request.pushEnabled()) {
            member.enablePush();
        } else {
            member.disablePush();
        }
        return MemberResponse.from(member);
    }

    /**
     * 구성원을 완전히 삭제한다 (제출/평가 이력, 개별 마감일, 출결, FCM/리프레시 토큰까지 전부 함께 삭제).
     * 회원가입 허용 이메일은 가입 시점(AuthService.join)에 이미 소진돼 삭제되므로 여기서 따로 지우지 않는다.
     * 이 구성원이 평가자(reviewMember)였던 다른 사람의 제출은 지우지 않고 평가자 참조만 비운다.
     * PRESIDENT/ADMIN 권한의 구성원은 삭제할 수 없다.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @Transactional
    public void delete(Long id) {
        Member member = getMember(id);
        if (member.getRole() == MemberRole.PRESIDENT || member.getRole() == MemberRole.ADMIN) {
            throw new CustomException(ErrorCode.MEMBER_DELETE_NOT_ALLOWED,
                    "회장/관리자 권한의 구성원은 삭제할 수 없습니다. id=" + id);
        }

        submissionFileRepository.findAllByAssignmentSubmit_SubmitMember_Id(id)
                .forEach(file -> s3Uploader.deleteByUrl(file.getFileUrl()));
        pushNotiLogRepository.deleteAllByAssignmentSubmit_SubmitMember_Id(id);
        submissionFileRepository.deleteAllByAssignmentSubmit_SubmitMember_Id(id);
        assignmentSubmitRepository.clearReviewMemberById(id);
        assignmentSubmitRepository.deleteAllBySubmitMember_Id(id);
        assignmentIndividualDeadlineRepository.deleteAllByMember_Id(id);
        detailAttendanceRepository.deleteAllByMember_Id(id);
        fcmTokenRepository.deleteAllByMember_Id(id);
        refreshTokenRepository.deleteByMember_Id(id);

        memberRepository.delete(member);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id));
    }

    private void validateEmailNotTaken(Member member, String email) {
        if (member.getEmail().equals(email)) {
            return;
        }
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일입니다. email=" + email);
        }
    }

    private Part getPart(Long partId) {
        if (partId == null) {
            return null;
        }
        return partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(ErrorCode.PART_NOT_FOUND, "존재하지 않는 파트입니다. id=" + partId));
    }
}
