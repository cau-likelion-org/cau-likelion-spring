package com.example.cau_likelion_spring.member.controller;

import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.dto.FcmTokenRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member", description = "구성원 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "본인 정보 조회",
            description = "요청자 본인의 구성원 정보를 조회합니다. JWT 액세스 토큰으로 본인을 식별합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원")
    })
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(memberService.getMyInfo(memberId));
    }

    @Operation(summary = "FCM 토큰 등록",
            description = "요청자 본인의 PWA 푸시 알림 수신용 FCM 토큰을 등록합니다. 기기(브라우저)마다 별도로 등록되므로 여러 기기에서 "
                    + "동시에 알림을 받을 수 있습니다. 이미 등록된 토큰이면 그대로 유지되고, 다른 구성원이 쓰던 토큰이면(기기 재사용/계정 전환) "
                    + "이 구성원 소유로 옮겨집니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원")
    })
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FcmTokenRequest request) {
        memberService.registerFcmToken(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "FCM 토큰 삭제",
            description = "요청자 본인 기기의 FCM 토큰을 삭제합니다. 로그아웃하거나 공용 기기에서 알림을 끊고 싶을 때 호출합니다. "
                    + "등록돼 있지 않은 토큰이어도 에러 없이 정상 처리됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @DeleteMapping("/me/fcm-token")
    public ResponseEntity<Void> deleteFcmToken(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FcmTokenRequest request) {
        memberService.deleteFcmToken(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 구성원 조회",
            description = "전체 구성원 목록을 조회합니다. 이름/기수/파트/권한 파라미터로 필터링할 수 있으며, 모두 선택 사항이라 "
                    + "아무것도 지정하지 않으면 전체 구성원이 조회됩니다. 이름은 부분 일치(대소문자 무시)로 검색됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAll(
            @Parameter(description = "이름 (부분 일치)") @RequestParam(required = false) String name,
            @Parameter(description = "기수", example = "13") @RequestParam(required = false) Integer generationNumber,
            @Parameter(description = "파트 ID") @RequestParam(required = false) Long partId,
            @Parameter(description = "역할") @RequestParam(required = false) MemberRole role) {
        return ResponseEntity.ok(memberService.getAll(name, generationNumber, partId, role));
    }

    @Operation(summary = "구성원 정보 수정",
            description = "관리자가 특정 구성원의 이름/로그인 이메일/역할/파트 정보를 수정합니다. 기수는 파트에 종속된 값이라 별도로 지정할 수 없고, "
                    + "파트를 다른 기수 소속 파트로 바꾸면 기수도 함께 바뀝니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원 또는 파트"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(
            @Parameter(description = "구성원 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @Operation(summary = "구성원 삭제",
            description = "관리자가 특정 구성원을 완전히 삭제합니다. 과제 제출/평가 이력, 개별 마감일, 출결 기록, FCM/리프레시 토큰까지 "
                    + "함께 삭제되며, 되돌릴 수 없습니다. 이 구성원이 평가자였던 다른 구성원의 제출 이력은 삭제되지 않고, 평가자 이름은 "
                    + "평가 시점 스냅샷으로 그대로 유지된 채 평가자 참조만 비워집니다. 회원가입 허용 이메일 목록에서도 이 구성원의 이메일이 함께 "
                    + "제거됩니다. PRESIDENT/ADMIN 권한의 구성원은 삭제할 수 없습니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음, 또는 PRESIDENT/ADMIN 권한의 구성원은 삭제 불가"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 구성원")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "구성원 ID", required = true) @PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
