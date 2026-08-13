package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.dto.AvailablePartResponse;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscribeRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscriberResponse;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.notification.repository.RecruitmentSubscriberRepository;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentSubscriberService {

    private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;
    private final GenerationRepository generationRepository;
    private final PartRepository partRepository;

    @Transactional
    public RecruitmentSubscriberResponse subscribe(RecruitmentSubscribeRequest request) {
        if (recruitmentSubscriberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_SUBSCRIPTION, "이미 구독 중인 이메일입니다. email=" + request.email());
        }

        List<Part> interestParts = findPartsOrThrow(request.interestPartIds());

        RecruitmentSubscriber subscriber = recruitmentSubscriberRepository.save(
                RecruitmentSubscriber.builder()
                        .email(request.email())
                        .name(request.name())
                        .interestParts(interestParts)
                        .build());

        return RecruitmentSubscriberResponse.of(subscriber);
    }

    /**
     * interestPartName으로 필터링한다 (id 아님). Part는 기수마다 새로 생성되는 row라 12기 "Backend"와
     * 13기 "Backend"는 id가 다른데, "백엔드 지원자 전체"를 보고 싶은 게 실제 니즈라 이름 기준으로 묶는다.
     */
    public List<RecruitmentSubscriberResponse> getAll(String interestPartName) {
        List<RecruitmentSubscriber> subscribers = (interestPartName != null)
                ? recruitmentSubscriberRepository.findAllByInterestParts_NameOrderByRegisteredAtDesc(interestPartName)
                : recruitmentSubscriberRepository.findAll(Sort.by(Sort.Direction.DESC, "registeredAt"));

        return subscribers.stream()
                .map(RecruitmentSubscriberResponse::of)
                .toList();
    }

    /**
     * 신청 폼에 노출할 관심 파트 목록. 가장 최근 기수(number 최댓값)의 파트를 기준으로 삼는다.
     * (모집 알림은 다음 기수가 생성되기 전에 미리 받기 때문에, 다음 기수가 아닌 직전 기수의 파트를 기준으로 함)
     */
    public List<AvailablePartResponse> getAvailableParts() {
        Generation latestGeneration = generationRepository.findTopByOrderByNumberDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "등록된 기수가 없습니다."));

        return partRepository.findAllByGeneration_Id(latestGeneration.getId()).stream()
                .map(AvailablePartResponse::of)
                .toList();
    }

    private List<Part> findPartsOrThrow(List<Long> partIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(partIds);
        List<Part> parts = partRepository.findAllById(uniqueIds);

        if (parts.size() != uniqueIds.size()) {
            Set<Long> foundIds = parts.stream().map(Part::getId).collect(Collectors.toSet());
            List<Long> missingIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new CustomException(ErrorCode.PART_NOT_FOUND, "존재하지 않는 파트입니다. interestPartIds=" + missingIds);
        }

        return parts;
    }

    /**
     * 매년 3월 1일 00:00(KST)에 구독자 목록 전체 초기화.
     * 기존 EmailSentLog는 남고 subscriber_id만 null로 바뀜 (EmailSentLog.subscriber에 ON DELETE SET NULL 설정됨).
     */
    @Scheduled(cron = "0 0 0 1 3 *", zone = "Asia/Seoul")
    @Transactional
    public void resetAll() {
        long count = recruitmentSubscriberRepository.count();
        recruitmentSubscriberRepository.deleteAllInBatch();
        log.info("모집 알림 구독자 연간 초기화 완료. 삭제된 구독자 수: {}", count);
    }
}
