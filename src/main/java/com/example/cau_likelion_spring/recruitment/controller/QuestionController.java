package com.example.cau_likelion_spring.recruitment.controller;

import com.example.cau_likelion_spring.recruitment.dto.QuestionResponse;
import com.example.cau_likelion_spring.recruitment.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recruitment Question", description = "리크루팅 2단계 문항 API")
@RestController
@RequestMapping("/api/recruitment/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "파트별 2단계 문항 조회",
            description = "현재 회차의 해당 파트 문항을 순서(sequence, 같으면 만든 순)대로 반환합니다. "
                    + "장문·링크·파일은 charLimit, 객관식은 options를 함께 반환합니다.")
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @Parameter(description = "지원 파트 id (form-options의 parts.id)") @RequestParam Long roundPartId) {
        return ResponseEntity.ok(questionService.getQuestions(roundPartId));
    }
}
