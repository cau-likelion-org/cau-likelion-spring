package com.example.cau_likelion_spring.mypage.controller;

import com.example.cau_likelion_spring.mypage.dto.MemberScoreResponse;
import com.example.cau_likelion_spring.mypage.service.MemberScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 상벌점 내역 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MemberScoreController {

    private final MemberScoreService memberScoreService;

    @Operation(summary = "본인 상벌점 내역 조회", description = "아기사자 본인의 지각/결석/무단결석/지각제출/미제출 횟수와 3점 만점 총점을 조회합니다.")
    @PreAuthorize("hasRole('BABY_LION')")
    @GetMapping("/score")
    public ResponseEntity<MemberScoreResponse> getMyScore(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(memberScoreService.getMyScore(memberId));
    }

    @Operation(summary = "아기사자 상벌점 목록 조회", description = "운영진은 본인 파트 아기사자만, 회장/admin은 전체 아기사자의 "
            + "상벌점 내역을 조회합니다. 기수/파트 구분은 프론트에서 이 목록을 필터링해서 보여주면 됩니다.")
    @PreAuthorize("hasAnyRole('STAFF', 'PRESIDENT', 'ADMIN')")
    @GetMapping("/scores")
    public ResponseEntity<List<MemberScoreResponse>> getScores(@AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(memberScoreService.getScores(requesterId));
    }
}
