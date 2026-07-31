package com.example.cau_likelion_spring.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 어노테이션이 동작하려면 스케줄링이 활성화되어 있어야 함
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
