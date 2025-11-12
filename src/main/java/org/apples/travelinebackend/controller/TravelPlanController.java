package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.service.TravelPlanService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TravelPlanDto> createTravelPlan(@Valid @RequestBody CreateTravelPlanRequest request) {
        TravelPlanDto createdPlan = travelPlanService.createTravelPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
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
     * 특정 여행 계획 조회
     */
    @GetMapping("/{planId}")
    public ResponseEntity<TravelPlanDto> getTravelPlan(@PathVariable Long planId) {
        TravelPlanDto plan = travelPlanService.getTravelPlanById(planId);
        return ResponseEntity.ok(plan);
    }
    
    /**
     * 여행 계획 수정
     */
    @PutMapping("/{planId}")
    public ResponseEntity<TravelPlanDto> updateTravelPlan(
            @PathVariable Long planId,
            @Valid @RequestBody UpdateTravelPlanRequest request) {
        TravelPlanDto updatedPlan = travelPlanService.updateTravelPlan(planId, request);
        return ResponseEntity.ok(updatedPlan);
    }
    
    /**
     * 여행 계획 삭제 (Soft Delete)
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteTravelPlan(@PathVariable Long planId) {
        travelPlanService.deleteTravelPlan(planId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 여행 계획 아카이브
     */
    @PostMapping("/{planId}/archive")
    public ResponseEntity<TravelPlanDto> archiveTravelPlan(@PathVariable Long planId) {
        TravelPlanDto archivedPlan = travelPlanService.archiveTravelPlan(planId);
        return ResponseEntity.ok(archivedPlan);
    }
}

