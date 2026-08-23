package com.example.cau_likelion_spring.member.repository;

import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Member> findByPart_IdAndRole(Long partId, MemberRole role);

    List<Member> findByPart_IdInAndRole(List<Long> partIds, MemberRole role);

    List<Member> findByRole(MemberRole role);

    /** 이름/기수/파트/권한으로 구성원을 검색한다. 각 파라미터가 null이면 그 조건은 무시된다. */
    @Query("SELECT m FROM Member m "
            + "LEFT JOIN m.part p "
            + "LEFT JOIN p.generation g "
            + "WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:generationNumber IS NULL OR g.number = :generationNumber) "
            + "AND (:partId IS NULL OR p.id = :partId) "
            + "AND (:role IS NULL OR m.role = :role) "
            + "ORDER BY m.id")
    List<Member> search(@Param("name") String name, @Param("generationNumber") Integer generationNumber,
                         @Param("partId") Long partId, @Param("role") MemberRole role);
}
