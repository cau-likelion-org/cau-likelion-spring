package com.example.cau_likelion_spring.blog.service;

import com.example.cau_likelion_spring.blog.domain.Blog;
import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import com.example.cau_likelion_spring.blog.dto.BlogRequest;
import com.example.cau_likelion_spring.blog.dto.BlogResponse;
import com.example.cau_likelion_spring.blog.dto.BlogScrapingResponse;
import com.example.cau_likelion_spring.blog.repository.BlogRepository;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogRepository blogRepository;
    private final GenerationRepository generationRepository;
    private final BlogScrapingService blogScrapingService;

    @Transactional
    public BlogResponse create(BlogRequest request) {
        Generation generation = getGeneration(request.generationId());
        BlogScrapingResponse scraped = blogScrapingService.scrape(request.url());

        Blog blog = blogRepository.save(Blog.builder()
                .generation(generation)
                .title(scraped.title())
                .thumbnailUrl(scraped.thumbnailUrl())
                .category(request.category())
                .summary(scraped.description())
                .writer(request.writer())
                .url(request.url())
                .publishedDate(parsePublishedDate(scraped.publishedDate()))
                .build());

        return BlogResponse.of(blog);
    }

    public List<BlogResponse> getAll(Long generationId, BlogCategory category) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Blog> blogs;
        if (generationId != null && category != null) {
            blogs = blogRepository.findByGeneration_IdAndCategory(generationId, category, sort);
        } else if (generationId != null) {
            blogs = blogRepository.findByGeneration_Id(generationId, sort);
        } else if (category != null) {
            blogs = blogRepository.findByCategory(category, sort);
        } else {
            blogs = blogRepository.findAll(sort);
        }

        return blogs.stream()
                .map(BlogResponse::of)
                .toList();
    }

    public BlogResponse getById(Long id) {
        return BlogResponse.of(getBlog(id));
    }

    @Transactional
    public BlogResponse update(Long id, BlogRequest request) {
        Blog blog = getBlog(id);
        Generation generation = getGeneration(request.generationId());
        BlogScrapingResponse scraped = blogScrapingService.scrape(request.url());

        blog.update(generation, scraped.title(), scraped.thumbnailUrl(), request.category(),
                scraped.description(), request.writer(), request.url(), parsePublishedDate(scraped.publishedDate()));

        return BlogResponse.of(blog);
    }

    @Transactional
    public void delete(Long id) {
        blogRepository.delete(getBlog(id));
    }

    /** 스크래핑이 원문 작성일을 못 찾으면 null이 그대로 온다 (yyyy-MM-dd 포맷은 BlogScrapingService가 보장) */
    private LocalDate parsePublishedDate(String publishedDate) {
        return StringUtils.hasText(publishedDate) ? LocalDate.parse(publishedDate) : null;
    }

    private Blog getBlog(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BLOG_NOT_FOUND, "존재하지 않는 블로그입니다. id=" + id));
    }

    private Generation getGeneration(Long id) {
        return generationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERATION_NOT_FOUND, "존재하지 않는 기수입니다. id=" + id));
    }
}
