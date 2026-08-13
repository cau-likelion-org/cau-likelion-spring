package com.example.cau_likelion_spring.notification.controller;

import com.example.cau_likelion_spring.notification.dto.AvailablePartResponse;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscribeRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentSubscriberResponse;
import com.example.cau_likelion_spring.notification.service.RecruitmentSubscriberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recruitment", description = "모집 알림 API")
@RestController
@RequestMapping("/api/recruitment/subscribers")
@RequiredArgsConstructor
public class RecruitmentSubscriberController {

    private final RecruitmentSubscriberService recruitmentSubscriberService;

    @Operation(summary = "구독 신청 시 선택 가능한 관심 파트 목록 조회",
            description = "가장 최근 기수의 파트 목록을 반환합니다. 로그인 없이 누구나 조회할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/available-parts")
    public ResponseEntity<List<AvailablePartResponse>> getAvailableParts() {
        return ResponseEntity.ok(recruitmentSubscriberService.getAvailableParts());
    }

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

    @Operation(summary = "신청자들이 선택한 관심 파트 이름 목록 조회",
            description = "구독자 목록 필터 드롭다운 구성용입니다. 현재 신청자들이 실제로 선택한 관심 파트 이름만 "
                    + "중복 없이 반환하며, 신청자 명단이 바뀌면(신규 신청, 매년 3/1 초기화 등) 결과도 같이 바뀝니다. "
                    + "ADMIN 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/interest-parts")
    public ResponseEntity<List<String>> getInterestPartNames() {
        return ResponseEntity.ok(recruitmentSubscriberService.getInterestPartNames());
    }

    @Operation(summary = "모집 알림 구독자 목록 조회",
            description = "공고 발송 대상을 선택하기 위한 구독자 목록입니다. interestPartName을 넘기면 해당 이름의 관심 파트를 "
                    + "선택한 구독자만 필터링합니다. 파트는 기수마다 새로 생성되는 데이터라 id가 아닌 이름으로 필터링하며, "
                    + "예를 들어 \"Backend\"로 조회하면 기수와 무관하게 이름이 일치하는 파트를 선택한 구독자가 모두 조회됩니다. "
                    + "ADMIN 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RecruitmentSubscriberResponse>> getAll(
            @Parameter(description = "필터링할 관심 파트 이름", example = "Backend") @RequestParam(required = false) String interestPartName) {
        return ResponseEntity.ok(recruitmentSubscriberService.getAll(interestPartName));
    }
}
