package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.QuestionOrderRequest;
import com.example.cau_likelion_spring.recruitment.dto.QuestionResponse;
import com.example.cau_likelion_spring.recruitment.dto.QuestionSaveRequest;
import com.example.cau_likelion_spring.recruitment.service.QuestionAdminService;
import com.example.cau_likelion_spring.recruitment.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recruitment Question", description = "리크루팅 2단계 문항 API")
@RestController
@RequestMapping("/api/recruitment/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionAdminService questionAdminService;

    @Operation(summary = "파트별 2단계 문항 조회",
            description = "현재 회차의 해당 파트 문항을 순서(sequence, 같으면 만든 순)대로 반환합니다. "
                    + "장문·링크·파일은 charLimit, 객관식은 options를 함께 반환합니다.")
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @Parameter(description = "지원 파트 id (form-options의 parts.id)") @RequestParam Long roundPartId) {
        return ResponseEntity.ok(questionService.getQuestions(roundPartId));
    }

    private static final String LOCK_RULE = "서류 접수 시작일(startDate)이 지났거나 해당 파트에 지원서가 접수됐으면 409입니다(시작일 미설정이면 가능). "
            + "ADMIN/PRESIDENT 권한이 필요합니다.";
    private static final String TYPE_RULE = "유형은 LONG_TEXT(장문: charLimit 필수, options 불가) / "
            + "SINGLE_CHOICE(객관식 단일: options 2개 이상, charLimit 불가)만 가능하며, 규칙 위반 시 400입니다. ";

    @Operation(summary = "2단계 문항 등록 (백오피스)",
            description = "현재 회차 해당 파트에 문항 하나를 맨 뒤 순서로 추가합니다. 선택지는 배열 순서대로 노출됩니다. "
                    + TYPE_RULE + LOCK_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @Parameter(description = "지원 파트 id (파트 정원 조회 API의 parts.roundPartId = form-options의 parts.id)") @RequestParam Long roundPartId,
            @Valid @RequestBody QuestionSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionAdminService.createQuestion(roundPartId, request));
    }

    @Operation(summary = "2단계 문항 수정 (백오피스)",
            description = "문항 하나를 통째로 덮어씁니다(부분 수정 아님, 유형 변경 가능). 순서는 그대로 유지되고, "
                    + "선택지는 보낸 목록으로 교체되어 선택지 id가 새로 발급됩니다. "
                    + TYPE_RULE + "현재 회차의 문항이 아니면 404입니다. " + LOCK_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @Parameter(description = "2단계 문항 id") @PathVariable Long questionId,
            @Valid @RequestBody QuestionSaveRequest request) {
        return ResponseEntity.ok(questionAdminService.updateQuestion(questionId, request));
    }

    @Operation(summary = "2단계 문항 삭제 (백오피스)",
            description = "문항과 그 내용·선택지를 삭제합니다. 남은 문항의 순서는 그대로 유지됩니다. "
                    + "현재 회차의 문항이 아니면 404입니다. " + LOCK_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@Parameter(description = "2단계 문항 id") @PathVariable Long questionId) {
        questionAdminService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "2단계 문항 순서 변경 (백오피스)",
            description = "바뀐 순서대로 파트의 전체 문항 id를 보내면 앞에서부터 1, 2, 3...으로 순서를 저장합니다. "
                    + "파트의 전체 문항 id가 중복 없이 모두 포함되어야 합니다(아니면 400). "
                    + "응답은 조회 API와 같은 형식입니다. " + LOCK_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PatchMapping("/order")
    public ResponseEntity<List<QuestionResponse>> changeOrder(
            @Parameter(description = "지원 파트 id (파트 정원 조회 API의 parts.roundPartId = form-options의 parts.id)") @RequestParam Long roundPartId,
            @Valid @RequestBody QuestionOrderRequest request) {
        return ResponseEntity.ok(questionAdminService.changeOrder(roundPartId, request));
    }
}
