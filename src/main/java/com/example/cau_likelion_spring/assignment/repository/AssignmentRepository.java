package com.example.cau_likelion_spring.assignment.repository;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** 아기사자 본인 파트의 과제 목록을 주차별로 묶어서 보여줄 때 사용 */
    List<Assignment> findAllByPart_IdOrderByWeekAscEndDateAsc(Long partId);

    List<Assignment> findAllByPart_IdAndWeekOrderByEndDateAsc(Long partId, Integer week);
}
