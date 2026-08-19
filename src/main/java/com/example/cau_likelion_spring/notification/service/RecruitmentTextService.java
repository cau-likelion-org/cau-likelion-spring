package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import com.example.cau_likelion_spring.notification.dto.RecipientResponse;
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

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
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

        List<EmailSentLog> logs = saveLogsFor(text, subscribers);

        List<RecipientResponse> recipients = toPendingRecipients(subscribers);
        return RecruitmentTextResponse.of(text, logs.size(), 0, 0, recipients);
    }

    public List<RecruitmentTextResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledSendAt");
        return recruitmentTextRepository.findAll(sort).stream()
                .map(text -> {
                    TextCounts counts = countLogs(text);
                    return RecruitmentTextResponse.of(
                            text, counts.target(), counts.success(), counts.failed(), counts.recipients());
                })
                .toList();
    }

    public RecruitmentTextResponse getById(Long id) {
        RecruitmentText text = getText(id);
        TextCounts counts = countLogs(text);
        return RecruitmentTextResponse.of(
                text, counts.target(), counts.success(), counts.failed(), counts.recipients());
    }

    @Transactional
    public RecruitmentTextResponse update(Long id, RecruitmentTextRequest request) {
        RecruitmentText text = getText(id);
        validateNotSent(text);

        List<RecruitmentSubscriber> subscribers = getSubscribers(request.subscriberIds());
        text.update(request.title(), request.content(), request.scheduledSendAt());

        emailSentLogRepository.deleteAll(emailSentLogRepository.findAllByRecruitmentText(text));
        List<EmailSentLog> logs = saveLogsFor(text, subscribers);

        List<RecipientResponse> recipients = toPendingRecipients(subscribers);
        return RecruitmentTextResponse.of(text, logs.size(), 0, 0, recipients);
    }

    /**
     * FAILED 상태인 발송 대상들에게 원본 공고와 동일한 제목/본문으로 새 공고를 하나 더 만들어 즉시 재전송한다.
     * 원본 공고는 그대로 두고(수정하지 않음), 재전송 건은 별도 공고 row + 로그로 남아 목록에서 독립된 한 건으로 보인다.
     * 구독자가 이미 삭제된 로그는 보낼 대상이 없으므로 재전송에서 제외한다.
     */
    @Transactional
    public RecruitmentTextResponse resendFailed(Long id) {
        RecruitmentText originalText = getText(id);
        List<EmailSentLog> failedLogs = emailSentLogRepository
                .findAllByRecruitmentTextAndStatus(originalText, EmailSentStatus.FAILED);
        List<RecruitmentSubscriber> failedSubscribers = failedLogs.stream()
                .map(EmailSentLog::getSubscriber)
                .filter(subscriber -> subscriber != null)
                .toList();

        if (failedSubscribers.isEmpty()) {
            throw new CustomException(ErrorCode.RECRUITMENT_TEXT_NO_RESEND_TARGET,
                    "재전송할 실패 대상이 없습니다. id=" + id);
        }

        RecruitmentText resendText = recruitmentTextRepository.save(RecruitmentText.builder()
                .title(originalText.getTitle())
                .content(originalText.getContent())
                .scheduledSendAt(LocalDateTime.now())
                .build());

        List<EmailSentLog> resentLogs = saveLogsFor(resendText, failedSubscribers);
        resentLogs.forEach(recruitmentEmailSenderService::send);
        resendText.markSent();

        TextCounts counts = countLogs(resendText);
        return RecruitmentTextResponse.of(
                resendText, counts.target(), counts.success(), counts.failed(), counts.recipients());
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

    private List<EmailSentLog> saveLogsFor(RecruitmentText text, List<RecruitmentSubscriber> subscribers) {
        List<EmailSentLog> logs = subscribers.stream()
                .map(subscriber -> EmailSentLog.builder()
                        .subscriber(subscriber)
                        .recruitmentText(text)
                        .build())
                .toList();
        return emailSentLogRepository.saveAll(logs);
    }

    private List<RecipientResponse> toPendingRecipients(List<RecruitmentSubscriber> subscribers) {
        return subscribers.stream()
                .map(subscriber -> new RecipientResponse(subscriber.getEmail(), EmailSentStatus.PENDING))
                .toList();
    }

    /**
     * 재전송은 이제 원본과 별개의 공고(RecruitmentText)로 남기 때문에, 한 공고의 로그는 항상 수신자 1명당 1건이다
     * (같은 공고에 같은 구독자의 로그가 중복될 일이 없음). 그래서 target은 단순히 로그 개수와 같다.
     */
    private TextCounts countLogs(RecruitmentText text) {
        List<EmailSentLog> logs = emailSentLogRepository.findAllByRecruitmentText(text);

        int target = logs.size();
        int success = (int) logs.stream().filter(log -> log.getStatus() == EmailSentStatus.SUCCESS).count();
        int failed = (int) logs.stream().filter(log -> log.getStatus() == EmailSentStatus.FAILED).count();

        List<RecipientResponse> recipients = logs.stream()
                .map(log -> new RecipientResponse(log.getRecipientEmail(), log.getStatus()))
                .toList();

        return new TextCounts(target, success, failed, recipients);
    }

    private record TextCounts(int target, int success, int failed, List<RecipientResponse> recipients) {
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
