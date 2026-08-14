package com.example.cau_likelion_spring.member.service;

import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.dto.FcmTokenRequest;
import com.example.cau_likelion_spring.member.dto.MemberResponse;
import com.example.cau_likelion_spring.member.dto.MemberUpdateRequest;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
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

    /** 본인의 FCM 토큰을 등록/갱신한다. 기기 1개만 지원하며 재등록 시 이전 값을 덮어쓴다. */
    @Transactional
    public void updateFcmToken(Long memberId, FcmTokenRequest request) {
        Member member = getMember(memberId);
        member.updateFcmToken(request.fcmToken());
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
