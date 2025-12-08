package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.ProcessReportRequest;
import org.apples.travelinebackend.dto.ReportDto;
import org.apples.travelinebackend.dto.UpdateReportStatusRequest;
import org.apples.travelinebackend.entity.ReportTargetType;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.AdminContentService;
import org.apples.travelinebackend.service.ReportService;
import org.apples.travelinebackend.util.AdminAuthUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AdminContentService adminContentService;

    /**
     * 신고 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<ReportDto>> getReports(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,  // PENDING, PROCESSED, REJECTED
            @RequestParam(required = false) ReportTargetType contentType) {  // POST, COMMENT
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        Page<ReportDto> reports = reportService.getReports(page, size, status, contentType);
        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 상세 조회
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDto> getReport(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long reportId) {
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        ReportDto report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    /**
     * 신고 상태 변경
     */
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportDto> updateReportStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        ReportDto updatedReport = reportService.updateReportStatus(reportId, request);
        return ResponseEntity.ok(updatedReport);
    }

    /**
     * 신고 처리 완료
     */
    @PatchMapping("/{reportId}/process")
    public ResponseEntity<ReportDto> processReport(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long reportId,
            @Valid @RequestBody ProcessReportRequest request) {
        User user = getCurrentUser();
        AdminAuthUtil.checkAdminRole(user);
        ReportDto report = adminContentService.processReport(reportId, request, user);
        return ResponseEntity.ok(report);
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

