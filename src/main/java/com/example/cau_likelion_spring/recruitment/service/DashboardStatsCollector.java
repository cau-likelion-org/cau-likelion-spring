package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationResult;
import com.example.cau_likelion_spring.recruitment.domain.ApplicationSource;
import com.example.cau_likelion_spring.recruitment.domain.EnrollmentStatus;
import com.example.cau_likelion_spring.recruitment.domain.Grade;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.StatsMetricType;
import com.example.cau_likelion_spring.recruitment.domain.StatsSnapshot;
import com.example.cau_likelion_spring.recruitment.repository.DashboardStatsRepository;
import com.example.cau_likelion_spring.recruitment.repository.DashboardStatsRepository.PartDepartmentCount;
import com.example.cau_likelion_spring.recruitment.repository.DashboardStatsRepository.ValueCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 원본(지원자·지원서·결과)에서 대시보드 지표를 집계해 통계 행(StatsSnapshot) 목록으로 만든다.
 * 실시간 조회는 이 행 목록을 저장하지 않고 바로 응답으로 바꾸고, 스냅샷은 같은 행 목록을 그대로 저장한다
 * → 실시간과 스냅샷의 계산이 항상 같다.
 *
 * - 계산값(합계·차이·비율)까지 모두 행으로 만든다.
 * - 비율은 소수 둘째 자리 반올림, 아랫값이 0이면 null.
 * - 학과는 인원이 아닌 비율(%)만 만든다 (파트별 지원자 수가 드러나지 않게).
 * - 학년·학적·지원 경로는 0명인 값도 만든다.
 * - 날짜별 제출 수는 접수 시작일~마감일(지각 제출이 더 뒤면 그 날짜까지) 0명인 날도 만든다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardStatsCollector {

    private final DashboardStatsRepository dashboardStatsRepository;

    public List<StatsSnapshot> collect(Round round) {
        Long roundId = round.getId();
        Rows rows = new Rows(round);

        // 지원자 수·정원·경쟁률·전환율
        long identified = dashboardStatsRepository.countIdentified(roundId);
        long submitted = dashboardStatsRepository.countSubmitted(roundId);
        long headCount = dashboardStatsRepository.sumHeadCount(roundId);
        rows.add(StatsMetricType.TOTAL_APPLICANTS, submitted);
        rows.add(StatsMetricType.TOTAL_HEAD_COUNT, headCount);
        rows.add(StatsMetricType.TOTAL_COMPETITION_RATE, rate(submitted, headCount));
        rows.add(StatsMetricType.CONVERSION_RATE, rate(identified, submitted));

        // 단계별 인원
        long late = round.getDeadLine() == null ? 0 : dashboardStatsRepository.countLate(roundId, round.getDeadLine());
        rows.add(StatsMetricType.STAGE_IDENTIFIED, identified);
        rows.add(StatsMetricType.STAGE_SUBMITTED, submitted);
        rows.add(StatsMetricType.STAGE_NOT_SUBMITTED, Math.max(identified - submitted, 0));
        rows.add(StatsMetricType.LATE_SUBMITTED, late);
        rows.add(StatsMetricType.STAGE_DOCUMENT_PASSED,
                dashboardStatsRepository.countDocumentResult(roundId, ApplicationResult.PASS.name()));
        rows.add(StatsMetricType.STAGE_DOCUMENT_FAILED,
                dashboardStatsRepository.countDocumentResult(roundId, ApplicationResult.FAIL.name()));
        rows.add(StatsMetricType.STAGE_FINAL_PASSED,
                dashboardStatsRepository.countFinalResult(roundId, ApplicationResult.PASS.name()));
        rows.add(StatsMetricType.STAGE_FINAL_FAILED,
                dashboardStatsRepository.countFinalResult(roundId, ApplicationResult.FAIL.name()));

        addDailySubmissions(round, rows);
        addDepartmentRates(roundId, rows);
        addEnumCounts(StatsMetricType.GRADE_DIST, Grade.values(),
                dashboardStatsRepository.countByGrade(roundId), rows);
        addEnumCounts(StatsMetricType.ENROLLMENT_DIST, EnrollmentStatus.values(),
                dashboardStatsRepository.countByEnrollment(roundId), rows);
        addEnumCounts(StatsMetricType.SOURCE_DIST, ApplicationSource.values(),
                dashboardStatsRepository.countBySource(roundId), rows);

        return rows.list;
    }

    /** 접수 시작일~마감일(지각 제출이 더 뒤면 그 날짜까지) 날짜별 제출 수, 0명인 날 포함 */
    private void addDailySubmissions(Round round, Rows rows) {
        TreeMap<LocalDate, Long> countByDate = new TreeMap<>();
        dashboardStatsRepository.countByDate(round.getId())
                .forEach(dateCount -> countByDate.put(LocalDate.parse(dateCount.getSubmitDate()), dateCount.getCnt()));

        LocalDate start = round.getStartDate() != null ? round.getStartDate().toLocalDate()
                : (countByDate.isEmpty() ? null : countByDate.firstKey());
        LocalDate end = round.getDeadLine() != null ? round.getDeadLine().toLocalDate()
                : (countByDate.isEmpty() ? null : countByDate.lastKey());
        if (start == null || end == null) {
            return;
        }
        if (!countByDate.isEmpty() && countByDate.lastKey().isAfter(end)) {
            end = countByDate.lastKey();
        }
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            rows.add(StatsMetricType.DAILY_SUBMISSIONS, null, date.toString(), countByDate.getOrDefault(date, 0L));
        }
    }

    /**
     * 학과 비율(%). 전체(part=null)는 전체 지원자 대비, 파트별은 그 파트 지원자 대비.
     * 학과 인원은 비율 계산에만 쓰고 저장하지 않는다.
     */
    private void addDepartmentRates(Long roundId, Rows rows) {
        List<PartDepartmentCount> counts = dashboardStatsRepository.countByPartAndDepartment(roundId);

        Map<String, Long> totalByDepartment = new LinkedHashMap<>();
        Map<String, Map<String, Long>> byPart = new TreeMap<>();
        for (PartDepartmentCount count : counts) {
            totalByDepartment.merge(count.getDepartment(), count.getCnt(), Long::sum);
            byPart.computeIfAbsent(count.getPart(), part -> new LinkedHashMap<>())
                    .merge(count.getDepartment(), count.getCnt(), Long::sum);
        }

        addRates(null, totalByDepartment, rows);
        byPart.forEach((part, departmentCounts) -> addRates(part, departmentCounts, rows));
    }

    private void addRates(String part, Map<String, Long> departmentCounts, Rows rows) {
        long total = departmentCounts.values().stream().mapToLong(Long::longValue).sum();
        departmentCounts.forEach((department, count) ->
                rows.add(StatsMetricType.DEPARTMENT_RATE, part, department, percent(count, total)));
    }

    /** enum의 모든 값을 0명 포함으로 만든다 */
    private void addEnumCounts(StatsMetricType metricType, Enum<?>[] values, List<ValueCount> counts, Rows rows) {
        Map<String, Long> countByValue = new HashMap<>();
        counts.forEach(valueCount -> countByValue.put(valueCount.getVal(), valueCount.getCnt()));
        for (Enum<?> value : values) {
            rows.add(metricType, null, value.name(), countByValue.getOrDefault(value.name(), 0L));
        }
    }

    /** numerator ÷ denominator, 소수 둘째 자리 반올림. 아랫값이 0이면 null */
    private Double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** numerator ÷ denominator × 100 (%), 소수 둘째 자리 반올림. 아랫값이 0이면 null */
    private Double percent(long numerator, long denominator) {
        if (denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator * 100)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** 통계 행 목록 (저장 전 엔티티) */
    private static class Rows {

        private final Round round;
        private final List<StatsSnapshot> list = new ArrayList<>();

        private Rows(Round round) {
            this.round = round;
        }

        private void add(StatsMetricType metricType, long value) {
            add(metricType, null, null, (double) value);
        }

        private void add(StatsMetricType metricType, Double value) {
            add(metricType, null, null, value);
        }

        private void add(StatsMetricType metricType, String part, String dimensionKey, long value) {
            add(metricType, part, dimensionKey, (double) value);
        }

        private void add(StatsMetricType metricType, String part, String dimensionKey, Double value) {
            list.add(StatsSnapshot.builder()
                    .round(round)
                    .metricType(metricType)
                    .part(part)
                    .dimensionKey(dimensionKey)
                    .value(value)
                    .basisDeadLine(round.getDeadLine())
                    .build());
        }
    }
}
