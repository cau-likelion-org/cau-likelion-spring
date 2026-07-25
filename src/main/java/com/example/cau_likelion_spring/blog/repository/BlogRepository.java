package com.example.cau_likelion_spring.blog.repository;

import com.example.cau_likelion_spring.blog.domain.Blog;
import com.example.cau_likelion_spring.blog.domain.BlogCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    List<Blog> findByGeneration_IdAndCategory(Long generationId, BlogCategory category, Sort sort);

    List<Blog> findByGeneration_Id(Long generationId, Sort sort);

    List<Blog> findByCategory(BlogCategory category, Sort sort);
}
