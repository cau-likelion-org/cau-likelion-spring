package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscribeRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscriberResponse;
import com.example.cau_likelion_spring.notification.exception.DuplicateSubscriptionException;
import com.example.cau_likelion_spring.notification.repository.RecruitmentSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentSubscriberService {

    private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;

    @Transactional
    public RecruitmentSubscriberResponse subscribe(RecruitmentSubscribeRequest request) {
        if (recruitmentSubscriberRepository.existsByEmail(request.email())) {
            throw new DuplicateSubscriptionException(request.email());
        }

        RecruitmentSubscriber subscriber = recruitmentSubscriberRepository.save(
                RecruitmentSubscriber.builder()
                        .email(request.email())
                        .build());

        return RecruitmentSubscriberResponse.of(subscriber);
    }

    public List<RecruitmentSubscriberResponse> getAll() {
        return recruitmentSubscriberRepository.findAll(Sort.by(Sort.Direction.DESC, "registeredAt")).stream()
                .map(RecruitmentSubscriberResponse::of)
                .toList();
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
