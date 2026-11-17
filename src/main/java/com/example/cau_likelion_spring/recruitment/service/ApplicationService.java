package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.FormOptionsResponse;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartResponse;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RoundPartRepository roundPartRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /**
     * 지원서 1단계 선택지(학년, 학적, 지원 경로, 현재 회차의 지원 파트)를 조회한다.
     * 등록된 파트가 없으면 parts는 빈 목록이다.
     */
    public FormOptionsResponse getFormOptions() {
        Round round = currentRoundFinder.find();
        List<RoundPartResponse> parts = roundPartRepository.findAllByRoundOrderByIdAsc(round).stream()
                .map(RoundPartResponse::from)
                .toList();
        return FormOptionsResponse.of(parts);
    }

    /**
     * 이번 회차에 같은 학번으로 제출된 지원서가 있는지 확인한다 (7.4.1 [다음] 클릭 시).
     * 조회만 하므로 접수 기간은 확인하지 않는다 (기간 판정은 제출 API에서).
     */
    public StudentNumberCheckResponse checkStudentNumber(StudentNumberCheckRequest request) {
        Round round = currentRoundFinder.find();
        boolean exists = applicationRepository.existsByRoundPart_RoundAndStudentNumber(
                round, request.getStudentNumber());
        return StudentNumberCheckResponse.of(exists);
    }
}
