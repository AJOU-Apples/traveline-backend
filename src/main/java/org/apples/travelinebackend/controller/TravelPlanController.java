package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.TravelPlanStatus;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.TravelPlanService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {
    
    private final TravelPlanService travelPlanService;
    
    /**
     * 여행 계획 생성
     */
    @PostMapping
    public ResponseEntity<TravelPlanDto> createTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateTravelPlanRequest request) {
        TravelPlanDto createdPlan = travelPlanService.createTravelPlan(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }
    
    /**
     * 내 여행 계획 목록 조회 (필터링 지원)
     * @param status 여행 상태 (PLANNING, ONGOING, COMPLETED, CANCELLED)
     * @param isArchived 아카이브 여부
     */
    @GetMapping("/my")
    public ResponseEntity<List<TravelPlanDto>> getMyTravelPlans(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) TravelPlanStatus status,
            @RequestParam(required = false) Boolean isArchived) {
        List<TravelPlanDto> plans = travelPlanService.getMyTravelPlans(
                currentUser.getId(), status, isArchived);
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 다가오는 여행 조회 (D-day용)
     * 가장 가까운 시작일을 가진 여행 계획 반환
     */
    @GetMapping("/upcoming")
    public ResponseEntity<TravelPlanDto> getUpcomingTravel(
            @AuthenticationPrincipal User currentUser) {
        TravelPlanDto upcomingTravel = travelPlanService.getUpcomingTravel(currentUser.getId());
        if (upcomingTravel == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(upcomingTravel);
    }
    
    /**
     * 여행 계획 목록 조회 (페이징 지원)
     * @param page 페이지 번호 (기본값: 0)
     * @param limit 페이지 당 개수 (기본값: 10)
     */
    @GetMapping
    public ResponseEntity<Page<TravelPlanDto>> getTravelPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<TravelPlanDto> plans = travelPlanService.getTravelPlans(page, limit);
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 모든 여행 계획 조회 (레거시 - 필터링 없음)
     */
    @GetMapping("/all")
    public ResponseEntity<List<TravelPlanDto>> getAllTravelPlans() {
        List<TravelPlanDto> plans = travelPlanService.getAllTravelPlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 특정 여행 계획 조회 (멤버 검증 - 소유자 또는 멤버만 조회 가능)
     */
    @GetMapping("/{planId}")
    public ResponseEntity<TravelPlanDto> getTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        TravelPlanDto plan = travelPlanService.getTravelPlanByIdSecure(planId, currentUser.getId());
        return ResponseEntity.ok(plan);
    }
    
    /**
     * 여행 계획 수정 (소유자 검증)
     */
    @PutMapping("/{planId}")
    public ResponseEntity<TravelPlanDto> updateTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId,
            @Valid @RequestBody UpdateTravelPlanRequest request) {
        TravelPlanDto updatedPlan = travelPlanService.updateTravelPlanWithAuth(
                planId, request, currentUser.getId());
        return ResponseEntity.ok(updatedPlan);
    }
    
    /**
     * 여행 계획 삭제 (Soft Delete, 소유자 검증)
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        travelPlanService.deleteTravelPlanWithAuth(planId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 여행 계획 아카이브 (소유자 검증)
     */
    @PostMapping("/{planId}/archive")
    public ResponseEntity<TravelPlanDto> archiveTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        TravelPlanDto archivedPlan = travelPlanService.archiveTravelPlanWithAuth(
                planId, currentUser.getId());
        return ResponseEntity.ok(archivedPlan);
    }
}

