package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.EmailSentLog;
import com.example.cau_likelion_spring.notification.domain.EmailSentStatus;
import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.domain.RecruitmentText;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextResponse;
import com.example.cau_likelion_spring.notification.exception.RecruitmentTextAlreadySentException;
import com.example.cau_likelion_spring.notification.exception.RecruitmentTextNotFoundException;
import com.example.cau_likelion_spring.notification.exception.SubscriberNotFoundException;
import com.example.cau_likelion_spring.notification.repository.EmailSentLogRepository;
import com.example.cau_likelion_spring.notification.repository.RecruitmentSubscriberRepository;
import com.example.cau_likelion_spring.notification.repository.RecruitmentTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return RecruitmentTextResponse.of(text, logs.size());
    }

    public List<RecruitmentTextResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "scheduledSendAt");
        return recruitmentTextRepository.findAll(sort).stream()
                .map(text -> RecruitmentTextResponse.of(text, targetCount(text)))
                .toList();
    }

    public RecruitmentTextResponse getById(Long id) {
        RecruitmentText text = getText(id);
        return RecruitmentTextResponse.of(text, targetCount(text));
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

        return RecruitmentTextResponse.of(text, logs.size());
    }

    @Transactional
    public void delete(Long id) {
        RecruitmentText text = getText(id);
        validateNotSent(text);

        emailSentLogRepository.deleteAll(emailSentLogRepository.findAllByRecruitmentText(text));
        recruitmentTextRepository.delete(text);
    }

    private RecruitmentText getText(Long id) {
        return recruitmentTextRepository.findById(id)
                .orElseThrow(() -> new RecruitmentTextNotFoundException(id));
    }

    private void validateNotSent(RecruitmentText text) {
        if (emailSentLogRepository.existsByRecruitmentTextAndStatusNot(text, EmailSentStatus.PENDING)) {
            throw new RecruitmentTextAlreadySentException(text.getId());
        }
    }

    private int targetCount(RecruitmentText text) {
        return (int) emailSentLogRepository.countByRecruitmentText(text);
    }

    private List<RecruitmentSubscriber> getSubscribers(List<Long> subscriberIds) {
        List<RecruitmentSubscriber> subscribers = recruitmentSubscriberRepository.findAllById(subscriberIds);

        if (subscribers.size() != subscriberIds.size()) {
            Set<Long> foundIds = subscribers.stream().map(RecruitmentSubscriber::getId).collect(Collectors.toSet());
            List<Long> missingIds = subscriberIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new SubscriberNotFoundException(missingIds);
        }

        return subscribers;
    }
}
