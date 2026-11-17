package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.InterviewTimesByDateResponse;
import com.example.cau_likelion_spring.recruitment.service.InterviewTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recruitment Interview Time", description = "리크루팅 면접 가능 시간 API")
@RestController
@RequestMapping("/api/recruitment/interview-times")
@RequiredArgsConstructor
public class InterviewTimeController {

    private final InterviewTimeService interviewTimeService;

    @Operation(summary = "면접 가능 시간 조회",
            description = "현재 회차의 면접 가능 시간을 날짜별로 묶어 일시 오름차순으로 반환합니다. 등록된 시간이 없으면 빈 배열입니다.")
    @GetMapping
    public ResponseEntity<List<InterviewTimesByDateResponse>> getInterviewTimes() {
        return ResponseEntity.ok(interviewTimeService.getInterviewTimes());
    }
}
