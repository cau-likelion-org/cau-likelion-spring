package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSendStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import com.example.cau_likelion_spring.notification.repository.EmailSentLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * scheduledSendAt이 지난 PENDING 상태의 EmailSentLog를 찾아 실제로 이메일을 발송하고 결과를 반영한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentEmailSenderService {

    private static final long POLLING_INTERVAL_MILLIS = 60_000L;
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s<>\"]+");

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
        try {
            javaMailSender.send(buildMessage(emailSentLog.getRecipientEmail(), emailSentLog));
            emailSentLog.markSent(EmailSentStatus.SUCCESS);
        } catch (MailException | MessagingException e) {
            log.error("모집 알림 이메일 발송 실패. emailSentLogId={}, recipientEmail={}",
                    emailSentLog.getId(), emailSentLog.getRecipientEmail(), e);
            emailSentLog.markSent(EmailSentStatus.FAILED);
        }
    }

    private MimeMessage buildMessage(String to, EmailSentLog emailSentLog) throws MessagingException {
        RecruitmentText text = emailSentLog.getRecruitmentText();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(text.getTitle());
        helper.setText(toHtml(text.getContent()), true);
        return mimeMessage;
    }

    /**
     * 관리자가 입력한 순수 텍스트 본문을 HTML로 변환한다.
     * HTML 이스케이프 -> URL을 <a> 링크로 치환(관리자 화면 미리보기와 동일하게 실제 발송 메일에서도 링크로 보이도록) -> 줄바꿈을 <br>로 치환 순서로 처리한다.
     */
    private String toHtml(String content) {
        String escaped = HtmlUtils.htmlEscape(content);

        Matcher matcher = URL_PATTERN.matcher(escaped);
        StringBuilder linked = new StringBuilder();
        while (matcher.find()) {
            String url = matcher.group();
            matcher.appendReplacement(linked, Matcher.quoteReplacement("<a href=\"" + url + "\">" + url + "</a>"));
        }
        matcher.appendTail(linked);

        return linked.toString().replace("\r\n", "\n").replace("\n", "<br>");
    }
}
