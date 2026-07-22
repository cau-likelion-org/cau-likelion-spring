package com.example.cau_likelion_spring.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAU LikeLion API")
                        .description("중앙대학교 멋쟁이사자처럼 백엔드 API 명세서")
                        .version("v0.0.1"));
    }
}
