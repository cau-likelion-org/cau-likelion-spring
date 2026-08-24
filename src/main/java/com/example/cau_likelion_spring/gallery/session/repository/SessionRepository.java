package com.example.cau_likelion_spring.gallery.session.repository;

import com.example.cau_likelion_spring.gallery.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findById(Long id);

    // 전체 조회 (필터 없음): 기수 내림차순 -> 세션 날짜 내림차순 -> 파트 이름 오름차순
    List<Session> findAllByOrderByPart_Generation_NumberDescSessionDateDescPart_NameAsc();

    // 파트로만 필터링된 조회: 기수는 고정이 아니므로 정렬에 포함, 기수 내림차순 -> 세션 날짜 내림차순 -> 파트 이름 오름차순
    // (파트가 이미 필터로 고정돼 있어 파트 이름 정렬 자체는 사실상 의미 없음 - 동일 파트끼리만 있으므로)
    List<Session> findByPart_NameOrderByPart_Generation_NumberDescSessionDateDescPart_NameAsc(String name);

    // 기수로만 필터링된 조회: 기수는 이미 고정이라 정렬에서 제외, 세션 날짜 내림차순 -> 파트 이름 오름차순
    List<Session> findByPart_Generation_NumberOrderBySessionDateDescPart_NameAsc(int number);

    // 파트 + 기수로 필터링된 조회: 기수는 이미 고정이라 정렬에서 제외, 세션 날짜 내림차순 -> 파트 이름 오름차순
    // (파트도 이미 필터로 고정돼 있어 파트 이름 정렬 자체는 사실상 의미 없음)
    List<Session> findByPart_NameAndPart_Generation_NumberOrderBySessionDateDescPart_NameAsc(String partName, int number);
}
