package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.SupplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    @PostMapping("/travel-plans/{planId}/supplies")
    public ResponseEntity<SupplyDto> createSupply(
            @PathVariable Long planId,
            @Valid @RequestBody CreateSupplyRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/travel-plans/{}/supplies - 준비물 생성 요청", planId);
        request.setTravelPlanId(planId);
        SupplyDto supply = supplyService.createSupply(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(supply);
    }

    @GetMapping("/travel-plans/{planId}/supplies")
    public ResponseEntity<List<SupplyDto>> getSupplies(
            @PathVariable Long planId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/supplies - 준비물 목록 조회", planId);
        List<SupplyDto> supplies = supplyService.getSuppliesByTravelPlan(planId, user);
        return ResponseEntity.ok(supplies);
    }

    @PatchMapping("/supplies/{supplyId}")
    public ResponseEntity<SupplyDto> updateSupply(
            @PathVariable Long supplyId,
            @Valid @RequestBody UpdateSupplyRequest request,
            @AuthenticationPrincipal User user) {
        log.info("PATCH /api/supplies/{} - 준비물 수정 요청", supplyId);
        SupplyDto supply = supplyService.updateSupply(supplyId, request, user);
        return ResponseEntity.ok(supply);
    }

    @DeleteMapping("/supplies/{supplyId}")
    public ResponseEntity<Void> deleteSupply(
            @PathVariable Long supplyId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/supplies/{} - 준비물 삭제 요청", supplyId);
        supplyService.deleteSupply(supplyId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/travel-plans/{planId}/supplies/summary")
    public ResponseEntity<SupplySummaryDto> getSupplySummary(
            @PathVariable Long planId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/supplies/summary - 준비물 요약 조회", planId);
        SupplySummaryDto summary = supplyService.getSupplySummary(planId, user);
        return ResponseEntity.ok(summary);
    }
}

