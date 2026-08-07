package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.DesiredTalent;
import com.example.cau_likelion_spring.intro.dto.DesiredTalentRequestDto;
import com.example.cau_likelion_spring.intro.dto.DesiredTalentResponseDto;
import com.example.cau_likelion_spring.intro.repository.DesiredTalentRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesiredTalentService {

    private final DesiredTalentRepository desiredTalentRepository;

    @Transactional
    public DesiredTalentResponseDto createDesiredTalent(DesiredTalentRequestDto request) {
        DesiredTalent desiredTalent = DesiredTalent.builder()
                .partName(request.getPartName())
                .content(request.getContent())
                .build();

        return DesiredTalentResponseDto.from(desiredTalentRepository.save(desiredTalent));
    }

    public List<DesiredTalentResponseDto> getDesiredTalentList() {
        return desiredTalentRepository.findAll().stream()
                .map(DesiredTalentResponseDto::from)
                .toList();
    }

    @Transactional
    public DesiredTalentResponseDto updateDesiredTalent(Long id, DesiredTalentRequestDto request) {
        DesiredTalent desiredTalent = findDesiredTalentById(id);
        desiredTalent.update(request.getPartName(), request.getContent());
        return DesiredTalentResponseDto.from(desiredTalent);
    }

    @Transactional
    public void deleteDesiredTalent(Long id) {
        desiredTalentRepository.delete(findDesiredTalentById(id));
    }

    private DesiredTalent findDesiredTalentById(Long id) {
        return desiredTalentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DESIRED_TALENT_NOT_FOUND, "존재하지 않는 인재상입니다. id=" + id));
    }
}
