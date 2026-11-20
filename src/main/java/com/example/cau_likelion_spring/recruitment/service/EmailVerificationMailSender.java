package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** 인증번호 메일을 작성해 발송한다 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationMailSender {

    private static final String SUBJECT = "[멋쟁이사자처럼 중앙대] 이메일 인증번호 안내";

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void send(String to, String code, Duration codeTtl) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(SUBJECT);
            helper.setText("인증번호: " + code + "\n\n"
                    + codeTtl.toMinutes() + "분 안에 인증번호를 입력해주세요.\n"
                    + "본인이 요청하지 않았다면 이 메일을 무시해주세요.");
            javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            log.error("인증번호 이메일 발송 실패. recipientEmail={}", to, e);
            throw new CustomException(ErrorCode.RECRUITMENT_EMAIL_SEND_FAILED);
        }
    }
}
