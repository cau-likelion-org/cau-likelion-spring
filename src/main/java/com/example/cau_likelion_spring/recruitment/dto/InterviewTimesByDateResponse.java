package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.InterviewTime;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 면접 가능 시간 조회 응답 (날짜별로 묶음).
 * 지원자는 times의 id를 골라 제출 API에 보낸다.
 */
@Getter
@Builder
public class InterviewTimesByDateResponse {

    private LocalDate date;
    private List<TimeSlot> times;

    public static InterviewTimesByDateResponse of(LocalDate date, List<InterviewTime> interviewTimes) {
        return InterviewTimesByDateResponse.builder()
                .date(date)
                .times(interviewTimes.stream()
                        .map(TimeSlot::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class TimeSlot {

        private Long id;
        private LocalTime time;

        public static TimeSlot from(InterviewTime interviewTime) {
            return TimeSlot.builder()
                    .id(interviewTime.getId())
                    .time(interviewTime.getInterviewDate().toLocalTime())
                    .build();
        }
    }
}
