package com.example.cau_likelion_spring.recruitment.repository;

import com.example.cau_likelion_spring.recruitment.domain.FAQ;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 모집공고 FAQ 레포지토리.
 * 랜딩 FAQ(intro.FaqRepository)와 빈 이름이 겹치지 않도록 이름을 RoundFaqRepository로 둔다.
 */
public interface RoundFaqRepository extends JpaRepository<FAQ, Long> {

    List<FAQ> findAllByRoundOrderByDisplayOrderAsc(Round round);

    /** 회차의 FAQ 전체 삭제 (한 번의 DELETE 쿼리) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FAQ f where f.round = :round")
    void deleteAllByRound(Round round);
}
