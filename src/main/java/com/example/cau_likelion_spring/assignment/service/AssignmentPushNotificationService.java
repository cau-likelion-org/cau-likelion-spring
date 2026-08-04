package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.PushNotiLog;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.member.domain.Member;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 과제 평가(승인/반려) 결과를 아기사자 PWA에 FCM 푸시로 알린다.
 * 발송 실패(토큰 미등록, FCM 오류 등)는 평가 자체를 막지 않고 조용히 넘어간다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentPushNotificationService {

    private final Optional<FirebaseApp> firebaseApp;
    private final PushNotiLogRepository pushNotiLogRepository;

    @Transactional
    public void sendEvaluationNotification(AssignmentSubmit submit) {
        Member member = submit.getSubmitMember();
        if (!StringUtils.hasText(member.getFcmToken())) {
            return;
        }
        if (firebaseApp.isEmpty()) {
            log.warn("FirebaseApp이 초기화되지 않아 푸시 알림을 보내지 않습니다. submitId={}", submit.getId());
            return;
        }

        Message message = Message.builder()
                .setToken(member.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(buildTitle(submit))
                        .setBody(buildBody(submit))
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance(firebaseApp.get()).send(message);
            pushNotiLogRepository.save(PushNotiLog.builder().assignmentSubmit(submit).build());
        } catch (FirebaseMessagingException e) {
            log.warn("과제 평가 알림 발송 실패. submitId={}, memberId={}", submit.getId(), member.getId(), e);
        }
    }

    private String buildTitle(AssignmentSubmit submit) {
        Assignment assignment = submit.getAssignment();
        String result = submit.getStatus() == AssignmentSubmitStatus.APPROVED ? "승인" : "반려";
        return "%d주차 과제 %s 알림 - %s".formatted(assignment.getWeek(), result, assignment.getTitle());
    }

    private String buildBody(AssignmentSubmit submit) {
        if (submit.getStatus() == AssignmentSubmitStatus.REJECTED) {
            return "(사유 : " + submit.getRejectionReason() + ")";
        }
        return "";
    }
}
