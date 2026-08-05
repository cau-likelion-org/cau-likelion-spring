package com.example.cau_likelion_spring.member.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.member.domain.AllowedUserEmail;
import com.example.cau_likelion_spring.member.dto.AllowedUserEmailResponse;
import com.example.cau_likelion_spring.member.dto.AllowedUserEmailSyncRequest;
import com.example.cau_likelion_spring.member.repository.AllowedUserEmailRepository;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllowedUserEmailService {

    private final AllowedUserEmailRepository allowedUserEmailRepository;
    private final GenerationRepository generationRepository;

    public List<AllowedUserEmailResponse> getList(Long generationId) {
        return allowedUserEmailRepository.findAllByGeneration_Id(generationId).stream()
                .map(AllowedUserEmailResponse::from)
                .toList();
    }

    /**
     * 화면에 보이는 목록 전체를 그대로 받아 DB와의 차이를 계산해 반영한다.
     * id가 있으면 수정, 없으면 신규 생성, 요청에 없는 기존 항목은 삭제.
     */
    @Transactional
    public List<AllowedUserEmailResponse> sync(Long generationId, AllowedUserEmailSyncRequest request) {
        Generation generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + generationId));

        Map<Long, AllowedUserEmail> remaining = allowedUserEmailRepository.findAllByGeneration_Id(generationId).stream()
                .collect(Collectors.toMap(AllowedUserEmail::getId, Function.identity()));

        List<AllowedUserEmail> result = new ArrayList<>();
        for (AllowedUserEmailSyncRequest.Item item : request.items()) {
            if (item.id() == null) {
                result.add(allowedUserEmailRepository.save(AllowedUserEmail.builder()
                        .generation(generation)
                        .name(item.name())
                        .allowedEmail(item.email())
                        .build()));
                continue;
            }

            AllowedUserEmail entity = remaining.remove(item.id());
            if (entity == null) {
                throw new CustomException(ErrorCode.ALLOWED_USER_EMAIL_NOT_FOUND, "존재하지 않는 예비 회원입니다. id=" + item.id());
            }
            entity.update(item.name(), item.email());
            result.add(entity);
        }

        // 요청 목록에 없는(=화면에서 삭제된) 기존 항목 제거
        allowedUserEmailRepository.deleteAll(remaining.values());

        return result.stream().map(AllowedUserEmailResponse::from).toList();
    }
}
