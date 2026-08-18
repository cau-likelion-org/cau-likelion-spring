package com.example.cau_likelion_spring.intro.controller;

import com.example.cau_likelion_spring.intro.dto.ExposedProjectsRequest;
import com.example.cau_likelion_spring.intro.service.ProjectExposureService;
import com.example.cau_likelion_spring.project.dto.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "랜딩페이지 프로젝트 노출 (Admin)", description = "랜딩페이지 프로젝트 하이라이트에 노출할 프로젝트 지정 API")
@RestController
@RequestMapping("/api/admin/landing/projects")
@RequiredArgsConstructor
public class ProjectExposureController {

    private final ProjectExposureService projectExposureService;

    @Operation(summary = "랜딩페이지 노출 프로젝트 지정", description = "요청에 담긴 id의 프로젝트만 랜딩페이지에 노출(isExposed=true)되고, "
            + "나머지 전체 프로젝트는 비노출(isExposed=false) 처리됩니다. 매 호출마다 전체 노출 목록을 덮어씁니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지정 성공 - 현재 노출 중인 프로젝트 목록 반환"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청에 존재하지 않는 프로젝트 id가 포함됨")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT')")
    @PutMapping("/exposure")
    public ResponseEntity<List<ProjectResponse>> updateExposure(
            @RequestBody @Valid ExposedProjectsRequest request) {
        return ResponseEntity.ok(projectExposureService.updateExposure(request.exposedProjectIds()));
    }
}
