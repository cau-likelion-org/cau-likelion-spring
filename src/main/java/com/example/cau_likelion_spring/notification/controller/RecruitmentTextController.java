package com.example.cau_likelion_spring.notification.controller;

import com.example.cau_likelion_spring.notification.dto.RecruitmentTextRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextResponse;
import com.example.cau_likelion_spring.notification.service.RecruitmentTextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment", description = "모집 알림 API")
@RestController
@RequestMapping("/api/recruitment/texts")
@RequiredArgsConstructor
public class RecruitmentTextController {

    private final RecruitmentTextService recruitmentTextService;

    @Operation(summary = "모집 공고 작성 및 발송 예약",
            description = "제목/본문/예정 전송일시와 함께 발송 대상 구독자를 선택해서 공고를 등록합니다. "
                    + "선택된 구독자마다 PENDING 상태의 발송 로그가 생성되며, 응답의 targetCount로 총 발송 대상자 수를 확인할 수 있습니다. "
                    + "ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구독자 ID 포함")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RecruitmentTextResponse> create(@Valid @RequestBody RecruitmentTextRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitmentTextService.create(request));
    }
}
