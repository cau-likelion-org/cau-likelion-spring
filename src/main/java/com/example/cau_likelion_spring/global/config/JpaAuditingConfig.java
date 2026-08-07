package com.example.cau_likelion_spring.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity의 @CreatedDate, @LastModifiedDate가 동작하려면
 * JPA Auditing이 활성화되어 있어야 함
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
