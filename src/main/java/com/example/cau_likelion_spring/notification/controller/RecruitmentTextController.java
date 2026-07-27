package com.example.cau_likelion_spring.notification.controller;

import com.example.cau_likelion_spring.notification.dto.RecruitmentTextRequest;
import com.example.cau_likelion_spring.notification.dto.RecruitmentTextResponse;
import com.example.cau_likelion_spring.notification.service.RecruitmentTextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Operation(summary = "모집 공고 목록 조회",
            description = "등록된 모집 공고 목록을 예정 전송일시 오름차순으로 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RecruitmentTextResponse>> getAll() {
        return ResponseEntity.ok(recruitmentTextService.getAll());
    }

    @Operation(summary = "모집 공고 단건 조회", description = "공고 ID로 상세 정보를 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentTextResponse> getById(
            @Parameter(description = "공고 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(recruitmentTextService.getById(id));
    }

    @Operation(summary = "모집 공고 수정",
            description = "아직 발송이 시작되지 않은 공고만 수정할 수 있습니다. 발송 대상 구독자 목록도 다시 지정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고 또는 구독자 ID 포함"),
            @ApiResponse(responseCode = "409", description = "이미 발송이 시작되어 수정 불가")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RecruitmentTextResponse> update(
            @Parameter(description = "공고 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RecruitmentTextRequest request) {
        return ResponseEntity.ok(recruitmentTextService.update(id, request));
    }

    @Operation(summary = "모집 공고 삭제",
            description = "아직 발송이 시작되지 않은 공고만 삭제할 수 있습니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고"),
            @ApiResponse(responseCode = "409", description = "이미 발송이 시작되어 삭제 불가")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "공고 ID", required = true) @PathVariable Long id) {
        recruitmentTextService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
