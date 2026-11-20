package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.recruitment.domain.FAQ;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.dto.RoundFaqRequest;
import com.example.cau_likelion_spring.recruitment.dto.RoundFaqResponse;
import com.example.cau_likelion_spring.recruitment.repository.RoundFaqRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 모집공고(회차) FAQ 관리.
 * 관리자 화면에서 FAQ를 추가·삭제·순서 변경한 뒤 한 번에 저장하므로,
 * 저장 시 회차의 기존 FAQ를 모두 지우고 요청 순서대로 새로 만든다.
 * (FAQ를 참조하는 다른 엔티티가 없어 id가 바뀌어도 영향 없음)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundFaqService {

    private final RoundFaqRepository roundFaqRepository;
    private final RoundRepository roundRepository;
    private final CurrentRoundFinder currentRoundFinder;

    /** 현재 회차의 FAQ (노출 순서대로) */
    public List<RoundFaqResponse> getCurrentFaqs() {
        return findFaqs(currentRoundFinder.find());
    }

    /** 회차의 FAQ 목록을 요청한 목록으로 통째로 바꾼다 (최소 1개는 @NotEmpty로 검증) */
    @Transactional
    public List<RoundFaqResponse> replaceFaqs(Long roundId, RoundFaqRequest request) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_ROUND_NOT_FOUND,
                        "존재하지 않는 모집 회차입니다. roundId=" + roundId));

        roundFaqRepository.deleteAllByRound(round);

        List<FAQ> faqs = new ArrayList<>();
        for (int i = 0; i < request.faqs().size(); i++) {
            RoundFaqRequest.FaqItem item = request.faqs().get(i);
            faqs.add(FAQ.builder()
                    .round(round)
                    .question(item.question())
                    .answer(item.answer())
                    .displayOrder(i + 1)
                    .build());
        }

        return roundFaqRepository.saveAll(faqs).stream()
                .map(RoundFaqResponse::from)
                .toList();
    }

    private List<RoundFaqResponse> findFaqs(Round round) {
        return roundFaqRepository.findAllByRoundOrderByDisplayOrderAsc(round).stream()
                .map(RoundFaqResponse::from)
                .toList();
    }
}
