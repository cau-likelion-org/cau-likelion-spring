package com.example.cau_likelion_spring.intro.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FAQ 순서 변경 요청 - 바뀐 순서대로 전체 FAQ id를 보낸다 (앞에 있을수록 먼저 노출)
 */
@Getter
@NoArgsConstructor
public class FaqOrderRequestDto {

    @NotEmpty
    private List<@NotNull Long> faqIds;
}
