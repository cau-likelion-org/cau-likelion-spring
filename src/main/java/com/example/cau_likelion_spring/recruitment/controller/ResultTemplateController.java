package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.domain.ResultTemplateType;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplatePreviewRequest;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplatePreviewResponse;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplateResponse;
import com.example.cau_likelion_spring.recruitment.dto.ResultTemplateSaveRequest;
import com.example.cau_likelion_spring.recruitment.service.ResultTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruitment Result Template (Admin)", description = "리크루팅 결과 안내 문구 템플릿 API")
@RestController
@RequestMapping("/api/recruitment/result-templates")
@RequiredArgsConstructor
public class ResultTemplateController {

    private final ResultTemplateService resultTemplateService;

    private static final String VARIABLE_RULE = "변수 토큰은 {이름}, {면접 일시}, {면접 대기 장소}이며 한 문구에 여러 번 넣을 수 있습니다. "
            + "면접 변수({면접 일시}, {면접 대기 장소})는 서류 합격(DOCUMENT_PASS) 템플릿에서만 쓸 수 있고, 다른 템플릿에 넣으면 400입니다. ";

    @Operation(summary = "결과 안내 문구 템플릿 조회 (백오피스)",
            description = "현재 회차의 해당 종류 템플릿을 반환합니다. "
                    + "저장 전이면 기본 문구와 usingDefault=true로 내려갑니다. "
                    + "variables는 변수 삽입 버튼 목록이며, enabled=false인 버튼은 비활성으로 표시하면 됩니다. "
                    + "ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping("/{type}")
    public ResponseEntity<ResultTemplateResponse> getTemplate(
            @Parameter(description = "템플릿 종류") @PathVariable ResultTemplateType type) {
        return ResponseEntity.ok(resultTemplateService.getTemplate(type));
    }

    @Operation(summary = "결과 안내 문구 저장 (백오피스)",
            description = "현재 회차의 템플릿 하나를 저장합니다(처음이면 생성, 이후 덮어쓰기). 문구는 필수이며 2000자 이하이고, 해당 전형(서류·최종)의 결과 공개 예약 중이면 409입니다. "
                    + VARIABLE_RULE + "ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/{type}")
    public ResponseEntity<ResultTemplateResponse> saveTemplate(
            @Parameter(description = "템플릿 종류") @PathVariable ResultTemplateType type,
            @Valid @RequestBody ResultTemplateSaveRequest request) {
        return ResponseEntity.ok(resultTemplateService.saveTemplate(type, request));
    }

    @Operation(summary = "결과 안내 문구 미리보기 (백오피스)",
            description = "편집 중인 문구의 변수를 선택한 지원자의 실제 값으로 바꿔서 반환합니다(저장하지 않음). "
                    + "면접 일시는 '2027년 3월 14일 (일) 15:30' 형식이며, 면접 일시·장소가 아직 입력되지 않았으면 '미정'으로 표시됩니다. "
                    + VARIABLE_RULE + "현재 회차에 제출된 지원서가 아니면 404입니다. ADMIN/PRESIDENT 권한이 필요합니다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PostMapping("/{type}/preview")
    public ResponseEntity<ResultTemplatePreviewResponse> preview(
            @Parameter(description = "템플릿 종류") @PathVariable ResultTemplateType type,
            @Valid @RequestBody ResultTemplatePreviewRequest request) {
        return ResponseEntity.ok(resultTemplateService.preview(type, request));
    }
}
