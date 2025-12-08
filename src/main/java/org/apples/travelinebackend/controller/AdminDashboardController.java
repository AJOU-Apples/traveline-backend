package org.apples.travelinebackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.DashboardStatsDto;
import org.apples.travelinebackend.dto.TrendDataDto;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.AdminDashboardService;
import org.apples.travelinebackend.util.AdminAuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 대시보드 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(
            @AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser();
        log.debug("대시보드 통계 조회 요청: userId={}, role={}", user != null ? user.getId() : null, user != null ? user.getRole() : null);
        AdminAuthUtil.checkAdminRole(user);
        DashboardStatsDto stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 시계열 통계 조회 (그래프용)
     */
    @GetMapping("/stats/trends")
    public ResponseEntity<TrendDataDto> getTrendData(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(defaultValue = "12") int months) {
        User user = getCurrentUser();
        log.debug("시계열 통계 조회 요청: userId={}, role={}, period={}, months={}", 
                user != null ? user.getId() : null, user != null ? user.getRole() : null, period, months);
        AdminAuthUtil.checkAdminRole(user);
        TrendDataDto trends = adminDashboardService.getTrendData(period, months);
        return ResponseEntity.ok(trends);
    }

    /**
     * SecurityContext에서 현재 사용자 가져오기
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}

