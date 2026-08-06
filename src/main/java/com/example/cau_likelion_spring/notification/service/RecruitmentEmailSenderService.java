package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSendStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import com.example.cau_likelion_spring.notification.repository.EmailSentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * scheduledSendAt이 지난 PENDING 상태의 EmailSentLog를 찾아 실제로 이메일을 발송하고 결과를 반영한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentEmailSenderService {

    private static final long POLLING_INTERVAL_MILLIS = 60_000L;

    private final JavaMailSender javaMailSender;
    private final EmailSentLogRepository emailSentLogRepository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Scheduled(fixedDelay = POLLING_INTERVAL_MILLIS)
    @Transactional
    public void sendDueEmails() {
        List<EmailSentLog> dueLogs = emailSentLogRepository
                .findAllByStatusAndRecruitmentText_StatusAndRecruitmentText_ScheduledSendAtBefore(
                        EmailSentStatus.PENDING, RecruitmentSendStatus.SCHEDULED, LocalDateTime.now());

        if (dueLogs.isEmpty()) {
            return;
        }

        log.info("발송 대상 모집 알림 이메일 {}건 처리 시작", dueLogs.size());
        dueLogs.forEach(this::send);

        dueLogs.stream()
                .map(EmailSentLog::getRecruitmentText)
                .distinct()
                .forEach(this::markSentIfComplete);
    }

    private void markSentIfComplete(RecruitmentText text) {
        boolean stillPending = emailSentLogRepository.existsByRecruitmentTextAndStatus(text, EmailSentStatus.PENDING);
        if (!stillPending) {
            text.markSent();
        }
    }

    /**
     * 스케줄러의 자동 발송뿐 아니라 실패 건 재전송(RecruitmentTextService)에서도 재사용된다.
     */
    public void send(EmailSentLog emailSentLog) {
        RecruitmentSubscriber subscriber = emailSentLog.getSubscriber();
        if (subscriber == null) {
            log.warn("구독자가 삭제되어 발송할 수 없는 로그입니다. emailSentLogId={}", emailSentLog.getId());
            emailSentLog.markSent(EmailSentStatus.FAILED);
            return;
        }

        try {
            javaMailSender.send(buildMessage(subscriber.getEmail(), emailSentLog));
            emailSentLog.markSent(EmailSentStatus.SUCCESS);
        } catch (MailException e) {
            log.error("모집 알림 이메일 발송 실패. emailSentLogId={}, subscriberEmail={}",
                    emailSentLog.getId(), subscriber.getEmail(), e);
            emailSentLog.markSent(EmailSentStatus.FAILED);
        }
    }

    private SimpleMailMessage buildMessage(String to, EmailSentLog emailSentLog) {
        RecruitmentText text = emailSentLog.getRecruitmentText();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(text.getTitle());
        message.setText(text.getContent());
        return message;
    }
}
