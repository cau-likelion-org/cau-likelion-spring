package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Faq;
import com.example.cau_likelion_spring.intro.dto.FaqRequestDto;
import com.example.cau_likelion_spring.intro.dto.FaqResponseDto;
import com.example.cau_likelion_spring.intro.repository.FaqRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    @Transactional
    public FaqResponseDto createFaq(FaqRequestDto request) {
        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .build();

        return FaqResponseDto.from(faqRepository.save(faq));
    }

    public List<FaqResponseDto> getFaqList() {
        return faqRepository.findAll().stream()
                .map(FaqResponseDto::from)
                .toList();
    }

    @Transactional
    public FaqResponseDto updateFaq(Long id, FaqRequestDto request) {
        Faq faq = findFaqById(id);
        faq.update(request.getQuestion(), request.getAnswer());
        return FaqResponseDto.from(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        faqRepository.delete(findFaqById(id));
    }

    private Faq findFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 FAQ입니다. id=" + id));
    }
}
