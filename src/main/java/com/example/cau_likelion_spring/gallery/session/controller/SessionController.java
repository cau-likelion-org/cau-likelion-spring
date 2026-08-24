package com.example.cau_likelion_spring.gallery.session.controller;

import com.example.cau_likelion_spring.gallery.session.dto.SessionCreateRequestDto;
import com.example.cau_likelion_spring.gallery.session.dto.SessionListResponseDto;
import com.example.cau_likelion_spring.gallery.session.dto.SessionResponseDto;
import com.example.cau_likelion_spring.gallery.session.dto.SessionUpdateRequestDto;
import com.example.cau_likelion_spring.gallery.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Session", description = "세션(강의/발표) 게시판 API")
@RestController
@RequestMapping("/api/gallery/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "세션 생성", description = "파트명과 기수 번호로 파트를 찾아 세션을 생성합니다. "
            + "thumbnailUrl/imageUrls는 POST /api/files/SESSION으로 미리 업로드한 URL을 담아 보냅니다.")
    @PostMapping
    public ResponseEntity<SessionResponseDto> createSession(@RequestBody @Valid SessionCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(request));
    }

    @Operation(summary = "세션 상세 조회", description = "세션 id로 상세 정보와 첨부 사진 목록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDto> getSession(
            @Parameter(description = "세션 id") @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    @Operation(summary = "세션 리스트 조회", description = "파트명 / 기수 번호로 필터링할 수 있습니다. 둘 다 생략하면 전체 세션을 조회합니다. "
            + "정렬 기준은 기수 내림차순 -> 세션 날짜 내림차순 -> 파트 이름 오름차순입니다 (기수로 필터링된 경우 해당 기준은 정렬에서 제외).")
    @GetMapping
    public ResponseEntity<List<SessionListResponseDto>> getSessionList(
            @Parameter(description = "파트명 (예: 백엔드)") @RequestParam(required = false) String partName,
            @Parameter(description = "기수 번호 (예: 13)") @RequestParam(required = false) Integer generationNumber) {
        return ResponseEntity.ok(sessionService.getSessionList(partName, generationNumber));
    }

    @Operation(summary = "세션 수정", description = "생략된 필드는 변경하지 않습니다. imageUrls를 보내면(기존에 유지할 URL + POST /api/files/SESSION으로 새로 업로드한 URL) 사진 목록 전체가 교체됩니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<SessionResponseDto> updateSession(
            @Parameter(description = "세션 id") @PathVariable Long id,
            @RequestBody SessionUpdateRequestDto request) {
        return ResponseEntity.ok(sessionService.updateSession(id, request));
    }

    @Operation(summary = "세션 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "세션 id") @PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
