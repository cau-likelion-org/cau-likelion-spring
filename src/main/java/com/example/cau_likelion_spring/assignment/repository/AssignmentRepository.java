package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** 아기사자 본인 파트의 과제 목록을 주차별로 묶어서 보여줄 때 사용 */
    List<Assignment> findAllByPart_IdOrderByWeekAscEndDateAsc(Long partId);

    /** ADMIN/PRESIDENT가 전체 파트의 과제 목록을 주차별로 묶어서 보여줄 때 사용 */
    List<Assignment> findAllByOrderByWeekAscEndDateAsc();

    List<Assignment> findAllByPart_IdAndWeekOrderByEndDateAsc(Long partId, Integer week);

    /** 벌점표 계산용 - 여러 파트에 걸친 과제를 한 번에 조회할 때 사용 (마이페이지) */
    List<Assignment> findAllByPart_IdIn(List<Long> partIds);
}
