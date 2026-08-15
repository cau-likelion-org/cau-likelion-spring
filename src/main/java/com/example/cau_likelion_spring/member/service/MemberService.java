package com.example.cau_likelion_spring.member.service;

import com.example.cau_likelion_spring.member.domain.FcmToken;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.dto.FcmTokenRequest;
import com.example.cau_likelion_spring.member.dto.MemberResponse;
import com.example.cau_likelion_spring.member.dto.MemberUpdateRequest;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.member.repository.FcmTokenRepository;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
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
     */
    @Transactional
    public void registerFcmToken(Long memberId, FcmTokenRequest request) {
        Member member = getMember(memberId);
        FcmToken fcmToken = fcmTokenRepository.findByToken(request.fcmToken())
                .orElseGet(() -> FcmToken.builder().member(member).token(request.fcmToken()).build());
        fcmToken.reassignTo(member);
        fcmTokenRepository.save(fcmToken);
    }

    /** 본인 기기의 FCM 토큰을 삭제한다 (로그아웃 시 등). 등록돼 있지 않아도 조용히 넘어간다. */
    @Transactional
    public void deleteFcmToken(Long memberId, FcmTokenRequest request) {
        fcmTokenRepository.deleteByMember_IdAndToken(memberId, request.fcmToken());
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
