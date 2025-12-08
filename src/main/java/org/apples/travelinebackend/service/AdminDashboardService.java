package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.DashboardStatsDto;
import org.apples.travelinebackend.dto.TrendDataDto;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.apples.travelinebackend.repository.TravelPostRepository;
import org.apples.travelinebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelPostRepository travelPostRepository;

    /**
     * 대시보드 통계 조회
     */
    public DashboardStatsDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfPreviousMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfPreviousMonth = firstDayOfMonth.minusDays(1);

        LocalDateTime startOfCurrentMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfCurrentMonth = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfPreviousMonth = firstDayOfPreviousMonth.atStartOfDay();
        LocalDateTime endOfPreviousMonth = firstDayOfPreviousMonth.plusMonths(1).atStartOfDay();

        // MAU (Monthly Active Users)
        long currentMau = userRepository.countActiveUsersInPeriod(startOfCurrentMonth, endOfCurrentMonth);
        long previousMau = userRepository.countActiveUsersInPeriod(startOfPreviousMonth, endOfPreviousMonth);
        double mauChange = calculateChangeRate(previousMau, currentMau);

        // 여행 계획 통계
        long totalTravelPlans = travelPlanRepository.count();
        long thisMonthTravelPlans = travelPlanRepository.countByCreatedAtBetween(startOfCurrentMonth, endOfCurrentMonth);
        long previousMonthTravelPlans = travelPlanRepository.countByCreatedAtBetween(startOfPreviousMonth, endOfPreviousMonth);
        double travelPlansChange = calculateChangeRate(previousMonthTravelPlans, thisMonthTravelPlans);

        // 여행기 통계
        long totalTravelPosts = travelPostRepository.count();
        long thisMonthTravelPosts = travelPostRepository.countByCreatedAtBetween(startOfCurrentMonth, endOfCurrentMonth);
        long previousMonthTravelPosts = travelPostRepository.countByCreatedAtBetween(startOfPreviousMonth, endOfPreviousMonth);
        double travelPostsChange = calculateChangeRate(previousMonthTravelPosts, thisMonthTravelPosts);

        // 사용자 통계
        long totalUsers = userRepository.count();
        long activeThisMonth = currentMau;
        long newThisMonth = userRepository.countNewUsersInPeriod(startOfCurrentMonth, endOfCurrentMonth);

        return DashboardStatsDto.builder()
                .mau(DashboardStatsDto.MauStats.builder()
                        .current(currentMau)
                        .previous(previousMau)
                        .change(mauChange)
                        .build())
                .travelPlans(DashboardStatsDto.CountStats.builder()
                        .total(totalTravelPlans)
                        .thisMonth(thisMonthTravelPlans)
                        .previousMonth(previousMonthTravelPlans)
                        .change(travelPlansChange)
                        .build())
                .travelPosts(DashboardStatsDto.CountStats.builder()
                        .total(totalTravelPosts)
                        .thisMonth(thisMonthTravelPosts)
                        .previousMonth(previousMonthTravelPosts)
                        .change(travelPostsChange)
                        .build())
                .users(DashboardStatsDto.UserStats.builder()
                        .total(totalUsers)
                        .activeThisMonth(activeThisMonth)
                        .newThisMonth(newThisMonth)
                        .build())
                .build();
    }

    /**
     * 시계열 통계 조회 (그래프용)
     */
    public TrendDataDto getTrendData(String period, int months) {
        LocalDate today = LocalDate.now();
        List<TrendDataDto.TrendPoint> mauPoints = new ArrayList<>();
        List<TrendDataDto.TrendPoint> travelPlanPoints = new ArrayList<>();
        List<TrendDataDto.TrendPoint> travelPostPoints = new ArrayList<>();

        if ("monthly".equals(period)) {
            // 월별 통계
            for (int i = months - 1; i >= 0; i--) {
                YearMonth yearMonth = YearMonth.from(today.minusMonths(i));
                LocalDate monthStart = yearMonth.atDay(1);
                LocalDate monthEnd = yearMonth.atEndOfMonth().plusDays(1);
                LocalDateTime startDateTime = monthStart.atStartOfDay();
                LocalDateTime endDateTime = monthEnd.atStartOfDay();

                String dateKey = yearMonth.toString(); // "2024-01"

                long mau = userRepository.countActiveUsersInPeriod(startDateTime, endDateTime);
                long travelPlans = travelPlanRepository.countByCreatedAtBetween(startDateTime, endDateTime);
                long travelPosts = travelPostRepository.countByCreatedAtBetween(startDateTime, endDateTime);

                mauPoints.add(TrendDataDto.TrendPoint.builder()
                        .date(dateKey)
                        .value(mau)
                        .build());
                travelPlanPoints.add(TrendDataDto.TrendPoint.builder()
                        .date(dateKey)
                        .value(travelPlans)
                        .build());
                travelPostPoints.add(TrendDataDto.TrendPoint.builder()
                        .date(dateKey)
                        .value(travelPosts)
                        .build());
            }
        } else if ("weekly".equals(period)) {
            // 주별 통계 (간단히 월별로 처리)
            // TODO: 실제 주별 통계 구현 필요
            return getTrendData("monthly", months);
        } else {
            // daily - 일별 통계 (간단히 월별로 처리)
            // TODO: 실제 일별 통계 구현 필요
            return getTrendData("monthly", months);
        }

        return TrendDataDto.builder()
                .mau(mauPoints)
                .travelPlans(travelPlanPoints)
                .travelPosts(travelPostPoints)
                .build();
    }

    /**
     * 변화율 계산 (%)
     */
    private double calculateChangeRate(long previous, long current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }
}

