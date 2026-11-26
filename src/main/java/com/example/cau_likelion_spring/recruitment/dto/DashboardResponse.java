package com.example.cau_likelion_spring.recruitment.dto;

import com.example.cau_likelion_spring.recruitment.domain.ApplicationSource;
import com.example.cau_likelion_spring.recruitment.domain.EnrollmentStatus;
import com.example.cau_likelion_spring.recruitment.domain.Grade;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.StatsMetricType;
import com.example.cau_likelion_spring.recruitment.domain.StatsSnapshot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 리크루팅 대시보드 응답 (7.9.1).
 * 실시간 집계든 저장된 스냅샷이든 같은 통계 행 목록(StatsSnapshot)에서 만든다.
 * 비율은 소수 둘째 자리 반올림 값이며, 아랫값이 0이면 null이다.
 */
@Schema(description = "리크루팅 대시보드")
public record DashboardResponse(

        @Schema(description = "조회한 회차 id", example = "1")
        Long roundId,

        @Schema(description = "회차명", example = "14기")
        String roundName,

        @Schema(description = "true면 저장된 스냅샷 값(결과 확인 종료 후), false면 실시간 집계 값")
        boolean snapshot,

        @Schema(description = "상단 카드 3개")
        Summary summary,

        @Schema(description = "단계별 인원 카드와 말풍선")
        Stages stages,

        @Schema(description = "서류 제출 추이 그래프")
        DailySubmissions dailySubmissions,

        @Schema(description = "학과 순위 (비율)")
        Departments departments,

        @Schema(description = "학년 분포 (enum 순서, 0명 포함)")
        List<EnumCount> grades,

        @Schema(description = "학적 분포 (enum 순서, 0명 포함)")
        List<EnumCount> enrollments,

        @Schema(description = "지원 경로 분포 (많은 순, 0명 포함)")
        List<EnumCount> sources
) {

    public static DashboardResponse of(Round round, List<StatsSnapshot> rows, boolean snapshot) {
        Map<StatsMetricType, List<StatsSnapshot>> byMetric = new EnumMap<>(StatsMetricType.class);
        rows.forEach(row -> byMetric.computeIfAbsent(row.getMetricType(), type -> new java.util.ArrayList<>()).add(row));

        Summary summary = new Summary(
                count(byMetric, StatsMetricType.TOTAL_APPLICANTS),
                value(byMetric, StatsMetricType.TOTAL_COMPETITION_RATE),
                value(byMetric, StatsMetricType.CONVERSION_RATE));

        Stages stages = new Stages(
                count(byMetric, StatsMetricType.STAGE_IDENTIFIED),
                count(byMetric, StatsMetricType.STAGE_SUBMITTED),
                count(byMetric, StatsMetricType.STAGE_NOT_SUBMITTED),
                count(byMetric, StatsMetricType.LATE_SUBMITTED),
                count(byMetric, StatsMetricType.STAGE_DOCUMENT_PASSED),
                count(byMetric, StatsMetricType.STAGE_DOCUMENT_FAILED),
                count(byMetric, StatsMetricType.STAGE_FINAL_PASSED),
                count(byMetric, StatsMetricType.STAGE_FINAL_FAILED));

        return new DashboardResponse(
                round.getId(),
                round.getName(),
                snapshot,
                summary,
                stages,
                DailySubmissions.of(round, rowsOf(byMetric, StatsMetricType.DAILY_SUBMISSIONS)),
                Departments.of(rowsOf(byMetric, StatsMetricType.DEPARTMENT_RATE)),
                enumCounts(rowsOf(byMetric, StatsMetricType.GRADE_DIST), Grade.values(), Grade::getLabel, false),
                enumCounts(rowsOf(byMetric, StatsMetricType.ENROLLMENT_DIST), EnrollmentStatus.values(),
                        EnrollmentStatus::getLabel, false),
                enumCounts(rowsOf(byMetric, StatsMetricType.SOURCE_DIST), ApplicationSource.values(),
                        ApplicationSource::getLabel, true));
    }

    @Schema(description = "상단 카드")
    public record Summary(
            @Schema(description = "전체 지원자 = 서류 제출자 수", example = "42") long totalApplicants,
            @Schema(description = "전체 경쟁률 = 제출자 ÷ 정원 (\"1.68 : 1\")", example = "1.68") Double competitionRate,
            @Schema(description = "전환율 = 본인 확인 ÷ 제출자 (\"1.24 : 1\")", example = "1.24") Double conversionRate
    ) {
    }

    @Schema(description = "단계별 인원 (보류·미입력은 합격·불합격 어디에도 세지 않음)")
    public record Stages(
            @Schema(description = "본인 확인 (계정 만든 사람)", example = "52") long identified,
            @Schema(description = "서류 제출", example = "42") long submitted,
            @Schema(description = "미제출 = 본인 확인 − 제출", example = "10") long notSubmitted,
            @Schema(description = "지각 제출 (제출 수에 포함)", example = "3") long late,
            @Schema(description = "서류 합격", example = "34") long documentPassed,
            @Schema(description = "서류 불합격", example = "6") long documentFailed,
            @Schema(description = "면접 합격", example = "30") long finalPassed,
            @Schema(description = "면접 불합격", example = "2") long finalFailed
    ) {
    }

    @Schema(description = "서류 제출 추이")
    public record DailySubmissions(
            @Schema(description = "접수 시작일", example = "2026-03-02") LocalDate startDate,
            @Schema(description = "접수 마감일 (지각 제출이 더 뒤면 items는 그 날짜까지 이어짐)", example = "2026-03-09") LocalDate endDate,
            @Schema(description = "날짜별 제출 수 (0명인 날 포함, 날짜 순)") List<DateCount> items
    ) {
        private static DailySubmissions of(Round round, List<StatsSnapshot> rows) {
            List<DateCount> items = rows.stream()
                    .map(row -> new DateCount(LocalDate.parse(row.getDimensionKey()), toLong(row.getValue())))
                    .sorted(Comparator.comparing(DateCount::date))
                    .toList();
            LocalDate start = round.getStartDate() != null ? round.getStartDate().toLocalDate()
                    : (items.isEmpty() ? null : items.get(0).date());
            LocalDate end = round.getDeadLine() != null ? round.getDeadLine().toLocalDate()
                    : (items.isEmpty() ? null : items.get(items.size() - 1).date());
            return new DailySubmissions(start, end, items);
        }
    }

    public record DateCount(
            @Schema(description = "날짜", example = "2026-03-02") LocalDate date,
            @Schema(description = "그날 제출 수", example = "3") long count
    ) {
    }

    @Schema(description = "학과 순위 (비율 높은 순 전부)")
    public record Departments(
            @Schema(description = "전체 지원자 대비 비율") List<DepartmentRate> all,
            @Schema(description = "파트명 → 그 파트 지원자 대비 비율 (파트 필터용)") Map<String, List<DepartmentRate>> byPart
    ) {
        private static Departments of(List<StatsSnapshot> rows) {
            List<DepartmentRate> all = sorted(rows.stream().filter(row -> row.getPart() == null).toList());

            Map<String, List<StatsSnapshot>> rowsByPart = rows.stream()
                    .filter(row -> row.getPart() != null)
                    .collect(Collectors.groupingBy(StatsSnapshot::getPart, TreeMap::new, Collectors.toList()));
            Map<String, List<DepartmentRate>> byPart = new LinkedHashMap<>();
            rowsByPart.forEach((part, partRows) -> byPart.put(part, sorted(partRows)));

            return new Departments(all, byPart);
        }

        private static List<DepartmentRate> sorted(List<StatsSnapshot> rows) {
            return rows.stream()
                    .map(row -> new DepartmentRate(row.getDimensionKey(), row.getValue()))
                    .sorted(Comparator.comparing(DepartmentRate::rate, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(DepartmentRate::name))
                    .toList();
        }
    }

    public record DepartmentRate(
            @Schema(description = "학과명", example = "소프트웨어학부") String name,
            @Schema(description = "비율(%)", example = "47.62") Double rate
    ) {
    }

    public record EnumCount(
            @Schema(description = "enum 값 (요청·DB에 쓰는 값)", example = "INSTAGRAM") String value,
            @Schema(description = "화면 표시용 라벨", example = "인스타그램") String label,
            @Schema(description = "인원", example = "20") long count
    ) {
    }

    // ===== 내부 도우미 =====

    private static List<StatsSnapshot> rowsOf(Map<StatsMetricType, List<StatsSnapshot>> byMetric, StatsMetricType type) {
        return byMetric.getOrDefault(type, List.of());
    }

    /** 합계 지표(행 1개)의 값. 없으면 null */
    private static Double value(Map<StatsMetricType, List<StatsSnapshot>> byMetric, StatsMetricType type) {
        List<StatsSnapshot> rows = rowsOf(byMetric, type);
        return rows.isEmpty() ? null : rows.get(0).getValue();
    }

    private static long count(Map<StatsMetricType, List<StatsSnapshot>> byMetric, StatsMetricType type) {
        return toLong(value(byMetric, type));
    }

    private static long toLong(Double value) {
        return value == null ? 0 : Math.round(value);
    }

    /**
     * enum 분포를 enum 순서로 만든다 (sortByCount면 인원 많은 순, 같으면 enum 순서).
     * 저장된 행에 지금은 없는 enum 값이 있어도(나중에 enum이 바뀐 경우) 라벨 없이 그대로 보여준다.
     */
    private static <E extends Enum<E>> List<EnumCount> enumCounts(List<StatsSnapshot> rows, E[] values,
                                                                   Function<E, String> labelOf, boolean sortByCount) {
        Map<String, E> enumByName = new LinkedHashMap<>();
        for (E value : values) {
            enumByName.put(value.name(), value);
        }
        Map<String, Integer> order = new LinkedHashMap<>();
        int index = 0;
        for (String name : enumByName.keySet()) {
            order.put(name, index++);
        }

        Comparator<EnumCount> byEnumOrder = Comparator.comparing(
                item -> order.getOrDefault(item.value(), Integer.MAX_VALUE));
        Comparator<EnumCount> comparator = sortByCount
                ? Comparator.comparingLong(EnumCount::count).reversed().thenComparing(byEnumOrder)
                : byEnumOrder;

        return rows.stream()
                .filter(row -> Objects.nonNull(row.getDimensionKey()))
                .map(row -> {
                    E value = enumByName.get(row.getDimensionKey());
                    String label = value == null ? row.getDimensionKey() : labelOf.apply(value);
                    return new EnumCount(row.getDimensionKey(), label, toLong(row.getValue()));
                })
                .sorted(comparator)
                .toList();
    }
}
