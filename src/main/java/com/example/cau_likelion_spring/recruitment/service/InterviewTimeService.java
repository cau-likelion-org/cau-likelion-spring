package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.InterviewTime;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.InterviewTimesByDateResponse;
import com.example.cau_likelion_spring.recruitment.repository.InterviewTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewTimeService {

    private final InterviewTimeRepository interviewTimeRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /**
     * 현재 회차의 면접 가능 시간을 날짜별로 묶어 조회한다 (7.4.1 면접 가능 시간 격자).
     * 등록된 시간이 없으면 빈 목록을 반환한다.
     */
    public List<InterviewTimesByDateResponse> getInterviewTimes() {
        Round round = currentRoundFinder.find();

        // 일시 오름차순으로 조회했으므로 LinkedHashMap으로 날짜 순서를 유지한다
        Map<LocalDate, List<InterviewTime>> timesByDate = interviewTimeRepository
                .findAllByRoundOrderByInterviewDateAsc(round).stream()
                .collect(Collectors.groupingBy(
                        interviewTime -> interviewTime.getInterviewDate().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return timesByDate.entrySet().stream()
                .map(entry -> InterviewTimesByDateResponse.of(entry.getKey(), entry.getValue()))
                .toList();
    }
}
