package com.example.cau_likelion_spring.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GenerationCreateRequestDto {

    @NotNull
    private Integer number;

    @NotNull
    private Integer year;

    @NotEmpty
    private List<@NotBlank String> partNames;
}
