package com.example.cau_likelion_spring.member.service;

import com.example.cau_likelion_spring.member.domain.Member;
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

    public List<MemberResponse> getAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse update(Long id, MemberUpdateRequest request) {
        Member member = getMember(id);
        Part part = getPart(request.partId());

        member.update(request.name(), request.role(), part);

        return MemberResponse.from(member);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 구성원입니다. id=" + id));
    }

    private Part getPart(Long partId) {
        if (partId == null) {
            return null;
        }
        return partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(ErrorCode.PART_NOT_FOUND, "존재하지 않는 파트입니다. id=" + partId));
    }
}
