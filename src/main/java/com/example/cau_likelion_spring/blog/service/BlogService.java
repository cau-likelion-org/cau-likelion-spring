package com.example.cau_likelion_spring.blog.service;

import com.example.cau_likelion_spring.blog.domain.Blog;
import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import com.example.cau_likelion_spring.blog.dto.BlogRequest;
import com.example.cau_likelion_spring.blog.dto.BlogResponse;
import com.example.cau_likelion_spring.blog.dto.BlogScrapingResponse;
import com.example.cau_likelion_spring.blog.exception.BlogNotFoundException;
import com.example.cau_likelion_spring.blog.repository.BlogRepository;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.exception.GenerationNotFoundException;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                scraped.description(), request.writer(), request.url());

        return BlogResponse.of(blog);
    }

    @Transactional
    public void delete(Long id) {
        blogRepository.delete(getBlog(id));
    }

    private Blog getBlog(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException(id));
    }

    private Generation getGeneration(Long id) {
        return generationRepository.findById(id)
                .orElseThrow(() -> new GenerationNotFoundException(id));
    }
}
