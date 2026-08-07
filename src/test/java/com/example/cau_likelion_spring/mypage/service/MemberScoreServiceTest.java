package com.example.cau_likelion_spring.mypage.service;

import com.example.cau_likelion_spring.assignment.domain.Assignment;
import com.example.cau_likelion_spring.assignment.domain.AssignmentSubmit;
import com.example.cau_likelion_spring.assignment.domain.AssignmentType;
import com.example.cau_likelion_spring.assignment.repository.AssignmentIndividualDeadlineRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentRepository;
import com.example.cau_likelion_spring.assignment.repository.AssignmentSubmitRepository;
import com.example.cau_likelion_spring.attendance.domain.AttendanceStatus;
import com.example.cau_likelion_spring.attendance.domain.DetailAttendance;
import com.example.cau_likelion_spring.attendance.domain.WeeklyAttendance;
import com.example.cau_likelion_spring.attendance.repository.DetailAttendanceRepository;
import com.example.cau_likelion_spring.attendance.repository.WeeklyAttendanceRepository;
import com.example.cau_likelion_spring.global.config.JpaAuditingConfig;
import com.example.cau_likelion_spring.member.domain.Member;
import com.example.cau_likelion_spring.member.domain.MemberRole;
import com.example.cau_likelion_spring.member.repository.MemberRepository;
import com.example.cau_likelion_spring.mypage.domain.MemberScoreCalculator;
import com.example.cau_likelion_spring.mypage.dto.MemberScoreResponse;
import com.example.cau_likelion_spring.organization.domain.Generation;
import com.example.cau_likelion_spring.organization.domain.GenerationStatus;
import com.example.cau_likelion_spring.organization.domain.Part;
import com.example.cau_likelion_spring.organization.repository.GenerationRepository;
import com.example.cau_likelion_spring.organization.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberScoreService의 실제 계산 결과를 검증하는 테스트.
 * 특히 "한 주차에 과제가 여러 개일 때 개별 카운트가 아니라 aggregateWeekly로 대표 상태 하나만 집계되는지"를 확인한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({MemberScoreService.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Generation.year처럼 MySQL에선 문제없지만 H2 표준 모드에선 예약어인 컬럼명이 있어서, 테스트 DB에서만 YEAR를 비예약어로 취급
        "spring.datasource.url=jdbc:h2:mem:mypage-test;DB_CLOSE_DELAY=-1;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true"
})
class MemberScoreServiceTest {

    @Autowired
    private MemberScoreService memberScoreService;

    @Autowired
    private GenerationRepository generationRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WeeklyAttendanceRepository weeklyAttendanceRepository;
    @Autowired
    private DetailAttendanceRepository detailAttendanceRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private AssignmentSubmitRepository assignmentSubmitRepository;
    @Autowired
    private AssignmentIndividualDeadlineRepository assignmentIndividualDeadlineRepository;

    @Test
    void 한_주차에_과제가_여러개면_개별카운트가_아니라_대표상태_하나로_집계된다() {
        Generation generation = generationRepository.save(
                Generation.builder().number(14).year(2026).status(GenerationStatus.IN_ACTIVITY).build());
        Part backend = partRepository.save(Part.builder().generation(generation).name("백엔드").build());

        Member babyLion = memberRepository.save(Member.builder()
                .name("이은지").email("baby1@test.com").role(MemberRole.BABY_LION).part(backend).build());

        // 출결: 지각 1회, 결석 1회, 무단결석 1회
        WeeklyAttendance week1 = weeklyAttendanceRepository.save(
                WeeklyAttendance.builder().date(LocalDate.now().minusWeeks(3)).password("1234").weekNumber(1).build());
        WeeklyAttendance week2 = weeklyAttendanceRepository.save(
                WeeklyAttendance.builder().date(LocalDate.now().minusWeeks(2)).password("1234").weekNumber(2).build());
        WeeklyAttendance week3 = weeklyAttendanceRepository.save(
                WeeklyAttendance.builder().date(LocalDate.now().minusWeeks(1)).password("1234").weekNumber(3).build());

        detailAttendanceRepository.save(DetailAttendance.builder()
                .member(babyLion).weeklyAttendance(week1).status(AttendanceStatus.LATE).build());
        detailAttendanceRepository.save(DetailAttendance.builder()
                .member(babyLion).weeklyAttendance(week2).status(AttendanceStatus.ABSENT).detailReason("사전통보").build());
        detailAttendanceRepository.save(DetailAttendance.builder()
                .member(babyLion).weeklyAttendance(week3).status(AttendanceStatus.UNAUTHORIZED_ABSENT).build());

        // 과제: 3주차에 과제 2개 - 하나는 미제출(마감+5일 훌쩍 지남, 제출 기록 자체를 안 만듦), 하나는 지각제출(마감은 지났지만 5일 이내 승인)
        assignmentRepository.save(Assignment.builder()
                .part(backend).week(3).title("미제출될 과제")
                .endDate(LocalDateTime.now().minusDays(10)).type(AssignmentType.URL).build());
        Assignment lateSubmittedAssignment = assignmentRepository.save(Assignment.builder()
                .part(backend).week(3).title("지각제출될 과제")
                .endDate(LocalDateTime.now().minusDays(3)).type(AssignmentType.URL).build());

        AssignmentSubmit lateSubmit = AssignmentSubmit.builder()
                .assignment(lateSubmittedAssignment).submitMember(babyLion).url("https://example.com").build();
        lateSubmit.approve(babyLion);
        assignmentSubmitRepository.save(lateSubmit);

        MemberScoreResponse response = memberScoreService.getMyScore(babyLion.getId());

        assertThat(response.lateCount()).isEqualTo(1);
        assertThat(response.absentCount()).isEqualTo(1);
        assertThat(response.unauthorizedAbsentCount()).isEqualTo(1);

        // 핵심 검증: 3주차에 과제가 2개(미제출+지각제출)였지만, 주차별 집계라 미제출 1개로만 잡혀야 한다
        // (개별 과제 기준이었다면 missedCount=1, lateSubmitCount=1이 나왔을 것)
        assertThat(response.missedCount()).isEqualTo(1);
        assertThat(response.lateSubmitCount()).isEqualTo(0);

        double expectedTotal = MemberScoreCalculator.calculateTotal(1, 1, 1, 0, 1);
        assertThat(response.total()).isEqualTo(expectedTotal);
    }

    @Test
    void 운영진은_본인_파트만_회장admin은_전체를_조회한다() {
        Generation generation = generationRepository.save(
                Generation.builder().number(14).year(2026).status(GenerationStatus.IN_ACTIVITY).build());
        Part backend = partRepository.save(Part.builder().generation(generation).name("백엔드").build());
        Part frontend = partRepository.save(Part.builder().generation(generation).name("프론트엔드").build());

        Member backendBabyLion = memberRepository.save(Member.builder()
                .name("이은지").email("baby-backend@test.com").role(MemberRole.BABY_LION).part(backend).build());
        Member frontendBabyLion = memberRepository.save(Member.builder()
                .name("김철수").email("baby-frontend@test.com").role(MemberRole.BABY_LION).part(frontend).build());
        Member staff = memberRepository.save(Member.builder()
                .name("박운영").email("staff@test.com").role(MemberRole.STAFF).part(backend).build());
        Member president = memberRepository.save(Member.builder()
                .name("최회장").email("president@test.com").role(MemberRole.PRESIDENT).part(backend).build());

        List<MemberScoreResponse> staffView = memberScoreService.getScores(staff.getId());
        List<MemberScoreResponse> presidentView = memberScoreService.getScores(president.getId());

        assertThat(staffView).extracting(MemberScoreResponse::memberId)
                .containsExactly(backendBabyLion.getId());
        assertThat(presidentView).extracting(MemberScoreResponse::memberId)
                .containsExactlyInAnyOrder(backendBabyLion.getId(), frontendBabyLion.getId());
    }
}
