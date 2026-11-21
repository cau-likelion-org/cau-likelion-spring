package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.DocumentPublishReserveRequest;
import com.example.cau_likelion_spring.recruitment.dto.FinalPublishReserveRequest;
import com.example.cau_likelion_spring.recruitment.dto.ResultPublishResponse;
import com.example.cau_likelion_spring.recruitment.service.ResultPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruitment Result Publish (Admin)", description = "리크루팅 결과 공개 예약 API")
@RestController
@RequestMapping("/api/recruitment/result-publish")
@RequiredArgsConstructor
public class ResultPublishController {

    private final ResultPublishService resultPublishService;

    private static final String CONDITION_RULE = "공개 조건: 전원 서류 결과 입력(보류 포함), 서류 합격자 전원 면접 일시·장소 입력, "
            + "서류 합격·불합격 문구 2종 저장(기본 문구 그대로는 미작성). ";
    private static final String STATE_RULE = "상태는 INPUTTING(입력 중) → RESERVED(공개 예약) → PUBLISHED(공개, 예약 시각이 지나면 자동)로 바뀌며, "
            + "예약 중에는 서류 결과·면접 일정·서류 문구를 수정할 수 없습니다(409). ";
    private static final String RESPONSE_RULE = "응답은 예약 현황(상태, 예약 일시, 조건 충족 여부)입니다. ADMIN/PRESIDENT 권한이 필요합니다.";

    @Operation(summary = "서류 결과 공개 예약 현황 조회 (백오피스)",
            description = "서류 결과의 공개 상태, 예약 일시, 조건별 충족 여부와 달성 수(achieved)·전체 수(total)를 반환합니다. "
                    + "reservable은 [예약] 버튼, cancellable은 [예약 취소] 버튼 활성 여부입니다. "
                    + CONDITION_RULE + STATE_RULE + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping("/document")
    public ResponseEntity<ResultPublishResponse> getDocumentPublishStatus() {
        return ResponseEntity.ok(resultPublishService.getDocumentPublishStatus());
    }

    @Operation(summary = "서류 결과 공개 예약 (백오피스)",
            description = "서류 결과 공개 일시를 예약합니다. 예약 시각이 되면 보류자를 제외하고 일괄 공개됩니다. "
                    + CONDITION_RULE + "조건 미충족 409, 과거 시각 400, 이미 예약·공개 상태면 409(변경은 취소 후 다시 예약)입니다. "
                    + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/document")
    public ResponseEntity<ResultPublishResponse> reserveDocument(@Valid @RequestBody DocumentPublishReserveRequest request) {
        return ResponseEntity.ok(resultPublishService.reserveDocument(request));
    }

    @Operation(summary = "서류 결과 공개 예약 취소 (백오피스)",
            description = "공개 전 예약을 취소하고 입력 중 상태로 되돌립니다. 예약이 없거나 이미 공개됐거나, "
                    + "최종 결과가 예약되어 있으면(최종 예약 먼저 취소) 409입니다 (공개 후 수정은 결과 입력 화면에서 바로 수정). "
                    + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @DeleteMapping("/document")
    public ResponseEntity<ResultPublishResponse> cancelDocument() {
        return ResponseEntity.ok(resultPublishService.cancelDocument());
    }

    private static final String FINAL_CONDITION_RULE = "공개 조건: 서류 합격자 전원 면접 결과 입력(보류 포함), "
            + "최종 합격·불합격 문구 2종 저장(기본 문구 그대로는 미작성). 서류 결과 공개를 먼저 예약해야 하며, 아니면 409입니다. ";
    private static final String FINAL_STATE_RULE = "상태는 INPUTTING(입력 중) → RESERVED(공개 예약) → PUBLISHED(공개, 예약 시각이 지나면 자동)로 바뀌며, "
            + "예약 중에는 면접 결과·최종 문구를 수정할 수 없습니다(409). ";

    @Operation(summary = "최종 결과 공개 예약 현황 조회 (백오피스)",
            description = "최종 결과의 공개 상태, 예약 일시, 결과 확인 종료일, 조건별 충족 여부와 달성 수(achieved)·전체 수(total)를 반환합니다. "
                    + "reservable은 [예약] 버튼, cancellable은 [예약 취소] 버튼 활성 여부입니다. "
                    + FINAL_CONDITION_RULE + FINAL_STATE_RULE + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @GetMapping("/final")
    public ResponseEntity<ResultPublishResponse> getFinalPublishStatus() {
        return ResponseEntity.ok(resultPublishService.getFinalPublishStatus());
    }

    @Operation(summary = "최종 결과 공개 예약 (백오피스)",
            description = "최종 결과 공개 일시와 결과 확인 종료일을 함께 설정합니다. 예약 시각이 되면 보류자를 제외하고 일괄 공개됩니다. "
                    + FINAL_CONDITION_RULE
                    + "일정 규칙: 현재 < 서류 결과 공개 일시 < 최종 공개 일시 < 결과 확인 종료일 ≤ 서류 모집 마감일 + 3개월(불합격자 보관 기간), 위반 시 400. "
                    + "조건 미충족 409, 이미 예약·공개 상태면 409(변경은 취소 후 다시 예약)입니다. " + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/final")
    public ResponseEntity<ResultPublishResponse> reserveFinal(@Valid @RequestBody FinalPublishReserveRequest request) {
        return ResponseEntity.ok(resultPublishService.reserveFinal(request));
    }

    @Operation(summary = "최종 결과 공개 예약 취소 (백오피스)",
            description = "공개 전 예약을 취소하고 입력 중 상태로 되돌립니다. 결과 확인 종료일도 함께 비워집니다. "
                    + "예약이 없거나 이미 공개됐으면 409입니다 (공개 후 수정은 결과 입력 화면에서 바로 수정). " + RESPONSE_RULE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @DeleteMapping("/final")
    public ResponseEntity<ResultPublishResponse> cancelFinal() {
        return ResponseEntity.ok(resultPublishService.cancelFinal());
    }
}
