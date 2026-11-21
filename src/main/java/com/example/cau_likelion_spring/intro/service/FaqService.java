package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Faq;
import com.example.cau_likelion_spring.intro.dto.FaqOrderRequestDto;
import com.example.cau_likelion_spring.intro.dto.FaqRequestDto;
import com.example.cau_likelion_spring.intro.dto.FaqResponseDto;
import com.example.cau_likelion_spring.intro.repository.FaqRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    // 노출 순서 오름차순, 순서가 없는(컬럼 추가 전) FAQ는 맨 뒤에 생성 순으로
    private static final Comparator<Faq> DISPLAY_ORDER = Comparator
            .comparing(Faq::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Faq::getId);

    private final FaqRepository faqRepository;

    @Transactional
    public FaqResponseDto createFaq(FaqRequestDto request) {
        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .displayOrder(faqRepository.findMaxDisplayOrder() + 1) // 새 FAQ는 맨 뒤에 노출
                .build();

        return FaqResponseDto.from(faqRepository.save(faq));
    }

    public List<FaqResponseDto> getFaqList() {
        return faqRepository.findAll().stream()
                .sorted(DISPLAY_ORDER)
                .map(FaqResponseDto::from)
                .toList();
    }

    @Transactional
    public FaqResponseDto updateFaq(Long id, FaqRequestDto request) {
        Faq faq = findFaqById(id);
        faq.update(request.getQuestion(), request.getAnswer());
        return FaqResponseDto.from(faq);
    }

    /**
     * 전체 FAQ의 노출 순서를 요청한 id 순서대로 1부터 다시 매긴다.
     * 일부만 보내면 순서가 꼬일 수 있어, 전체 FAQ id가 중복 없이 모두 있어야 한다.
     */
    @Transactional
    public List<FaqResponseDto> changeFaqOrder(FaqOrderRequestDto request) {
        List<Long> faqIds = request.getFaqIds();
        Map<Long, Faq> faqById = faqRepository.findAll().stream()
                .collect(Collectors.toMap(Faq::getId, Function.identity()));

        Set<Long> requestedIds = new HashSet<>(faqIds);
        if (requestedIds.size() != faqIds.size() || !requestedIds.equals(faqById.keySet())) {
            throw new CustomException(ErrorCode.FAQ_ORDER_MISMATCH,
                    "전체 FAQ id가 중복 없이 모두 포함되어야 합니다. 요청=" + faqIds + ", 전체=" + faqById.keySet());
        }

        for (int i = 0; i < faqIds.size(); i++) {
            faqById.get(faqIds.get(i)).changeDisplayOrder(i + 1);
        }
        // 영속 상태(더티 체킹)라 별도 save() 호출 불필요

        return faqIds.stream()
                .map(faqById::get)
                .map(FaqResponseDto::from)
                .toList();
    }

    @Transactional
    public void deleteFaq(Long id) {
        faqRepository.delete(findFaqById(id));
    }

    private Faq findFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND, "존재하지 않는 FAQ입니다. id=" + id));
    }
}
