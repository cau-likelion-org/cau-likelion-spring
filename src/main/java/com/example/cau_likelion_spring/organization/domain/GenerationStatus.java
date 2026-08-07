package com.example.cau_likelion_spring.organization.domain;

/**
 * 기수 활동 상태
 * 전체 기수 중 IN_ACTIVITY는 항상 최대 1개만 존재해야 한다 (GenerationService에서 보장).
 */
public enum GenerationStatus {
    BEFORE_ACTIVITY,   // 활동 전
    IN_ACTIVITY,       // 활동 중
    AFTER_ACTIVITY      // 활동 후
}
