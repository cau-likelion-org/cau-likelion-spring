package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CurrentRoundFinder currentRoundFinder;

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
