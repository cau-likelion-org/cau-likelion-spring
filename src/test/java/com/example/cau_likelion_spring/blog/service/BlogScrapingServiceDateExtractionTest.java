package com.example.cau_likelion_spring.blog.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class BlogScrapingServiceDateExtractionTest {

    @Test
    void velog_releasedAt_extractedFromScriptState() throws Exception {
        assertThat(extractPublishedDate("velog_sample.html")).isEqualTo("2026-04-08");
    }

    @Test
    void naver_blogDate_extractedFromTextWithSpaces() throws Exception {
        assertThat(extractPublishedDate("naver_sample.html")).isEqualTo("2026-02-10");
    }

    private String extractPublishedDate(String resourceFileName) throws Exception {
        File file = new File("src/test/resources/" + resourceFileName);
        Document doc = Jsoup.parse(file, "UTF-8");

        BlogScrapingService service = new BlogScrapingService(new ObjectMapper());
        Method method = BlogScrapingService.class.getDeclaredMethod("extractPublishedDate", Document.class);
        method.setAccessible(true);
        return (String) method.invoke(service, doc);
    }
}
