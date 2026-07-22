package com.example.cau_likelion_spring.member.controller;

import com.example.cau_likelion_spring.member.dto.MemberResponse;
import com.example.cau_likelion_spring.member.dto.MemberUpdateRequest;
import com.example.cau_likelion_spring.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member", description = "구성원 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "본인 정보 조회",
            description = "요청자 본인의 구성원 정보를 조회합니다. "
                    + "JWT 인증 모듈이 도입되기 전까지는 X-Member-Id 헤더로 본인을 식별하는 임시 방식입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원")
    })
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
            @Parameter(description = "본인 구성원 ID (임시 인증 헤더)", required = true)
            @RequestHeader("X-Member-Id") Long memberId) {
        return ResponseEntity.ok(memberService.getMyInfo(memberId));
    }

    @Operation(summary = "전체 구성원 조회", description = "전체 구성원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAll() {
        return ResponseEntity.ok(memberService.getAll());
    }

    @Operation(summary = "구성원 정보 수정",
            description = "관리자가 특정 구성원의 이름/역할/파트 정보를 수정합니다. "
                    + "(현재는 권한 검증 로직이 없으며, 인증 모듈 도입 후 관리자 권한 검증이 추가될 예정입니다.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원 또는 파트")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(
            @Parameter(description = "구성원 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }
}
