package com.example.cau_likelion_spring.notification.controller;

import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscribeRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscriberResponse;
import com.example.cau_likelion_spring.notification.service.RecruitmentSubscriberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment", description = "모집 알림 API")
@RestController
@RequestMapping("/api/recruitment/subscribers")
@RequiredArgsConstructor
public class RecruitmentSubscriberController {

    private final RecruitmentSubscriberService recruitmentSubscriberService;

    @Operation(summary = "모집 알림 이메일 구독 신청",
            description = "이메일을 입력받아 모집 공고 알림 구독자로 등록합니다. 로그인 없이 누구나 신청할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "구독 신청 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (이메일 형식 오류 등)"),
            @ApiResponse(responseCode = "409", description = "이미 구독 중인 이메일")
    })
    @PostMapping
    public ResponseEntity<RecruitmentSubscriberResponse> subscribe(
            @Valid @RequestBody RecruitmentSubscribeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitmentSubscriberService.subscribe(request));
    }
}
