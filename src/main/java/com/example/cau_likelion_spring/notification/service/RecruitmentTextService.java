package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import com.example.cau_likelion_spring.notification.dto.EmailSentLogResponse;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextResponse;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.notification.repository.EmailSentLogRepository;
import com.example.cau_likelion_spring.notification.repository.RecruitmentSubscriberRepository;
import com.example.cau_likelion_spring.notification.repository.RecruitmentTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentTextService {

    private final RecruitmentTextRepository recruitmentTextRepository;
    private final RecruitmentSubscriberRepository recruitmentSubscriberRepository;
    private final EmailSentLogRepository emailSentLogRepository;
    private final RecruitmentEmailSenderService recruitmentEmailSenderService;

    @Transactional
    public RecruitmentTextResponse create(RecruitmentTextRequest request) {
        List<RecruitmentSubscriber> subscribers = getSubscribers(request.subscriberIds());

        RecruitmentText text = recruitmentTextRepository.save(RecruitmentText.builder()
                .title(request.title())
                .content(request.content())
                .scheduledSendAt(request.scheduledSendAt())
                .build());

        List<EmailSentLog> logs = subscribers.stream()
                .map(subscriber -> EmailSentLog.builder()
                        .subscriber(subscriber)
                        .recruitmentText(text)
                        .build())
                .toList();
        emailSentLogRepository.saveAll(logs);

        return RecruitmentTextResponse.of(text, logs.size(), 0, 0);
    }

    public List<RecruitmentTextResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "scheduledSendAt");
        return recruitmentTextRepository.findAll(sort).stream()
                .map(text -> {
                    TextCounts counts = countLogs(text);
                    return RecruitmentTextResponse.of(text, counts.target(), counts.success(), counts.failed());
                })
                .toList();
    }

    public RecruitmentTextResponse getById(Long id) {
        RecruitmentText text = getText(id);
        TextCounts counts = countLogs(text);
        return RecruitmentTextResponse.of(text, counts.target(), counts.success(), counts.failed());
    }

    @Transactional
    public RecruitmentTextResponse update(Long id, RecruitmentTextRequest request) {
        RecruitmentText text = getText(id);
        validateNotSent(text);

        List<RecruitmentSubscriber> subscribers = getSubscribers(request.subscriberIds());
        text.update(request.title(), request.content(), request.scheduledSendAt());

        emailSentLogRepository.deleteAll(emailSentLogRepository.findAllByRecruitmentText(text));
        List<EmailSentLog> logs = subscribers.stream()
                .map(subscriber -> EmailSentLog.builder()
                        .subscriber(subscriber)
                        .recruitmentText(text)
                        .build())
                .toList();
        emailSentLogRepository.saveAll(logs);

        return RecruitmentTextResponse.of(text, logs.size(), 0, 0);
    }

    public List<EmailSentLogResponse> getLogs(Long id, EmailSentStatus status) {
        RecruitmentText text = getText(id);
        List<EmailSentLog> logs = (status != null)
                ? emailSentLogRepository.findAllByRecruitmentTextAndStatus(text, status)
                : emailSentLogRepository.findAllByRecruitmentText(text);
        return logs.stream().map(EmailSentLogResponse::of).toList();
    }

    /**
     * FAILED 상태인 발송 대상들에게 원본 공고와 동일한 제목/본문으로 재전송한다. 기존 실패 로그는 그대로 두고
     * 재전송 시도마다 새로운 EmailSentLog를 남겨 전체 발송 이력이 누적되도록 한다.
     * 구독자가 이미 삭제된 로그는 보낼 대상이 없으므로 재전송에서 제외한다.
     */
    @Transactional
    public List<EmailSentLogResponse> resendFailed(Long id) {
        RecruitmentText text = getText(id);
        List<EmailSentLog> failedLogs = emailSentLogRepository
                .findAllByRecruitmentTextAndStatus(text, EmailSentStatus.FAILED);

        List<EmailSentLog> resentLogs = failedLogs.stream()
                .filter(log -> log.getSubscriber() != null)
                .map(log -> EmailSentLog.builder()
                        .subscriber(log.getSubscriber())
                        .recruitmentText(text)
                        .build())
                .toList();
        emailSentLogRepository.saveAll(resentLogs);
        resentLogs.forEach(recruitmentEmailSenderService::send);

        return resentLogs.stream().map(EmailSentLogResponse::of).toList();
    }

    /**
     * 아직 발송이 시작되지 않은(모든 로그가 PENDING인) 공고를 취소 상태로 전환한다.
     * 취소된 공고는 스케줄러가 더 이상 발송 대상으로 조회하지 않는다.
     */
    @Transactional
    public void cancel(Long id) {
        RecruitmentText text = getText(id);
        validateNotSent(text);
        text.cancel();
    }

    private RecruitmentText getText(Long id) {
        return recruitmentTextRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_TEXT_NOT_FOUND, "존재하지 않는 모집 공고입니다. id=" + id));
    }

    private void validateNotSent(RecruitmentText text) {
        if (emailSentLogRepository.existsByRecruitmentTextAndStatusNot(text, EmailSentStatus.PENDING)) {
            throw new CustomException(ErrorCode.RECRUITMENT_TEXT_ALREADY_SENT,
                    "이미 발송이 시작된 공고는 수정/취소할 수 없습니다. id=" + text.getId());
        }
    }

    /**
     * 재전송으로 같은 구독자에게 로그가 여러 건 쌓일 수 있어, targetCount는 로그 개수가 아닌 서로 다른 구독자 수로 센다.
     * 구독자가 삭제된 로그(subscriber null)는 재전송 대상에서 제외되므로 중복 없이 항상 로그 1건 = 대상 1명이다.
     * successCount/failedCount는 수신자별 최신 발송 결과가 아닌, 누적된 로그(재전송 포함) 기준 건수다.
     */
    private TextCounts countLogs(RecruitmentText text) {
        List<EmailSentLog> logs = emailSentLogRepository.findAllByRecruitmentText(text);
        long distinctSubscriberCount = logs.stream()
                .map(EmailSentLog::getSubscriber)
                .filter(Objects::nonNull)
                .map(RecruitmentSubscriber::getId)
                .distinct()
                .count();
        long deletedSubscriberLogCount = logs.stream()
                .filter(log -> log.getSubscriber() == null)
                .count();
        int target = (int) (distinctSubscriberCount + deletedSubscriberLogCount);

        int success = (int) logs.stream().filter(log -> log.getStatus() == EmailSentStatus.SUCCESS).count();
        int failed = (int) logs.stream().filter(log -> log.getStatus() == EmailSentStatus.FAILED).count();

        return new TextCounts(target, success, failed);
    }

    private record TextCounts(int target, int success, int failed) {
    }

    private List<RecruitmentSubscriber> getSubscribers(List<Long> subscriberIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(subscriberIds);
        List<RecruitmentSubscriber> subscribers = recruitmentSubscriberRepository.findAllById(uniqueIds);

        if (subscribers.size() != uniqueIds.size()) {
            Set<Long> foundIds = subscribers.stream().map(RecruitmentSubscriber::getId).collect(Collectors.toSet());
            List<Long> missingIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new CustomException(ErrorCode.SUBSCRIBER_NOT_FOUND, "존재하지 않는 구독자입니다. ids=" + missingIds);
        }

        return subscribers;
    }
}
