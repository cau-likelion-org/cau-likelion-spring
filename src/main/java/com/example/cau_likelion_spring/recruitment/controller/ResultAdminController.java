package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.DocumentResultRequest;
import com.example.cau_likelion_spring.recruitment.dto.DocumentResultResponse;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleRequest;
import com.example.cau_likelion_spring.recruitment.dto.InterviewScheduleResponse;
import com.example.cau_likelion_spring.recruitment.service.ResultAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recruitment Result (Admin)", description = "리크루팅 전형 결과 입력 API")
@RestController
@RequestMapping("/api/recruitment/results")
@RequiredArgsConstructor
public class ResultAdminController {

    private final ResultAdminService resultAdminService;

    @Operation(summary = "서류 결과 입력 (백오피스)",
            description = "현재 회차에 제출된 지원서의 서류 결과를 합격(PASS)/불합격(FAIL)/보류(HOLD)로 저장합니다. "
                    + "체크박스 일괄 변경은 여러 id, 행별 개별 변경은 id 하나를 보내면 됩니다. 기존 결과는 덮어씁니다(이력 미보관). "
                    + "id가 중복되면 400, 현재 회차에 제출된 지원서가 아닌 id가 있으면 404이며 이때는 아무것도 저장되지 않습니다. "
                    + "응답은 요청한 순서대로 지원서별 저장된 결과입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PatchMapping("/document")
    public ResponseEntity<List<DocumentResultResponse>> updateDocumentResults(
            @Valid @RequestBody DocumentResultRequest request) {
        return ResponseEntity.ok(resultAdminService.updateDocumentResults(request));
    }

    @Operation(summary = "확정 면접 일정 입력 (백오피스)",
            description = "지원자 한 명의 확정 면접 일시(필수)와 장소(선택)를 저장합니다. 기존 값은 덮어쓰며, 장소를 비워 보내면 지워집니다. "
                    + "저장은 항상 되며, 확인이 필요하면 응답 warnings에 경고를 담습니다: "
                    + "OUT_OF_AVAILABLE_TIME(지원자가 고른 15분 단위 면접 가능 시간 밖), DUPLICATE_SCHEDULE(같은 일시에 다른 지원자 배정). "
                    + "현재 회차에 제출된 지원서가 아니면 404, 서류 결과가 합격(PASS)이 아니면 400입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{applicationId}/interview")
    public ResponseEntity<InterviewScheduleResponse> updateInterviewSchedule(
            @Parameter(description = "지원서 id") @PathVariable Long applicationId,
            @Valid @RequestBody InterviewScheduleRequest request) {
        return ResponseEntity.ok(resultAdminService.updateInterviewSchedule(applicationId, request));
    }
}
