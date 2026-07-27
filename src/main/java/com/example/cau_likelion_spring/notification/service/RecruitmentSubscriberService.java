package com.example.cau_likelion_spring.notification.service;

import com.example.cau_likelion_spring.notification.domain.RecruitmentSubscriber;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscribeRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscriberResponse;
import com.example.cau_likelion_spring.notification.exception.DuplicateSubscriptionException;
import com.example.cau_likelion_spring.notification.repository.RecruitmentSubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
