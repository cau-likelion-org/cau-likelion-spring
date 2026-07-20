package com.example.cau_likelion_spring.session.controller;

import com.example.cau_likelion_spring.session.dto.SessionCreateRequestDto;
import com.example.cau_likelion_spring.session.dto.SessionListResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionResponseDto;
import com.example.cau_likelion_spring.session.dto.SessionUpdateRequestDto;
import com.example.cau_likelion_spring.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // 세션 생성
    @PostMapping
    public ResponseEntity<SessionResponseDto> createSession(@Valid @RequestBody SessionCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(request));
    }

    // 세션 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDto> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    // 세션 리스트 조회 (파트명 / 기수 둘 다 옵셔널 필터)
    @GetMapping
    public ResponseEntity<List<SessionListResponseDto>> getSessionList(
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) Integer generationNumber) {
        return ResponseEntity.ok(sessionService.getSessionList(partName, generationNumber));
    }

    // 세션 수정 (사진 목록 포함)
    @PatchMapping("/{id}")
    public ResponseEntity<SessionResponseDto> updateSession(@PathVariable Long id,
                                                              @RequestBody SessionUpdateRequestDto request) {
        return ResponseEntity.ok(sessionService.updateSession(id, request));
    }

    // 세션 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
