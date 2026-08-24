package com.example.cau_likelion_spring.assignment.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmitStatus;
import com.example.cau_likelion_spring.assignment.domain.PushNotiLog;
import com.example.cau_likelion_spring.assignment.repository.PushNotiLogRepository;
import com.example.cau_likelion_spring.member.domain.FcmToken;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.repository.FcmTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 과제 평가(승인/반려) 결과를 아기사자 PWA에 FCM 푸시로 알린다. 구성원이 등록한 기기(FCM 토큰)마다 각각 발송한다.
 * 발송 실패(토큰 미등록, FCM 오류 등)는 평가 자체를 막지 않고, 다른 기기로의 발송도 막지 않는다 (기기별로 독립적으로 처리).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentPushNotificationService {

    private final Optional<FirebaseApp> firebaseApp;
    private final PushNotiLogRepository pushNotiLogRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void sendEvaluationNotification(AssignmentSubmit submit) {
        Member member = submit.getSubmitMember();
        if (!member.isPushEnabled()) {
            return;
        }
        List<FcmToken> fcmTokens = fcmTokenRepository.findAllByMember_Id(member.getId());
        if (fcmTokens.isEmpty()) {
            return;
        }
        if (firebaseApp.isEmpty()) {
            log.warn("FirebaseApp이 초기화되지 않아 푸시 알림을 보내지 않습니다. submitId={}", submit.getId());
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(buildTitle(submit))
                .setBody(buildBody(submit))
                .build();

        boolean anySucceeded = false;
        for (FcmToken fcmToken : fcmTokens) {
            Message message = Message.builder()
                    .setToken(fcmToken.getToken())
                    .setNotification(notification)
                    .putData("assignmentId", String.valueOf(submit.getAssignment().getId()))
                    .putData("submitId", String.valueOf(submit.getId()))
                    .putData("week", String.valueOf(submit.getAssignment().getWeek()))
                    .build();

            try {
                FirebaseMessaging.getInstance(firebaseApp.get()).send(message);
                anySucceeded = true;
            } catch (FirebaseMessagingException e) {
                log.warn("과제 평가 알림 발송 실패. submitId={}, memberId={}, fcmTokenId={}",
                        submit.getId(), member.getId(), fcmToken.getId(), e);
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.delete(fcmToken);
                }
            }
        }

        if (anySucceeded) {
            pushNotiLogRepository.save(PushNotiLog.builder().assignmentSubmit(submit).build());
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
