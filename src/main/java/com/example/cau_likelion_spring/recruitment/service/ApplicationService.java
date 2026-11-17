package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RoundRepository roundRepository;

    /**
     * 이번 회차에 같은 학번으로 제출된 지원서가 있는지 확인한다 (7.4.1 [다음] 클릭 시).
     * 조회만 하므로 접수 기간은 확인하지 않는다 (기간 판정은 제출 API에서).
     */
    public StudentNumberCheckResponse checkStudentNumber(StudentNumberCheckRequest request) {
        Round round = findCurrentRound();
        boolean exists = applicationRepository.existsByRoundPart_RoundAndStudentNumber(
                round, request.getStudentNumber());
        return StudentNumberCheckResponse.of(exists);
    }

    /** 현재 회차 = 가장 최근에 만든 회차 */
    private Round findCurrentRound() {
        return roundRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND, "진행 중인 모집 회차가 없습니다."));
    }
}
