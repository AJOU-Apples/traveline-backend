package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.AccommodationDto;
import org.apples.travelinebackend.dto.CreateAccommodationRequest;
import org.apples.travelinebackend.dto.UpdateAccommodationRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.AccommodationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping
    public ResponseEntity<AccommodationDto> createAccommodation(
            @Valid @RequestBody CreateAccommodationRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/accommodations - 숙소 정보 생성 요청: travelPlanId={}", request.getTravelPlanId());
        AccommodationDto accommodation = accommodationService.createAccommodation(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(accommodation);
    }

    @GetMapping("/travel-plan/{travelPlanId}")
    public ResponseEntity<List<AccommodationDto>> getAccommodationsByTravelPlan(
            @PathVariable Long travelPlanId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/accommodations/travel-plan/{} - 여행 계획별 숙소 목록 조회", travelPlanId);
        List<AccommodationDto> accommodations = accommodationService.getAccommodationsByTravelPlan(travelPlanId, user);
        return ResponseEntity.ok(accommodations);
    }

    @GetMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDto> getAccommodationById(
            @PathVariable Long accommodationId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/accommodations/{} - 숙소 상세 조회", accommodationId);
        AccommodationDto accommodation = accommodationService.getAccommodationById(accommodationId, user);
        return ResponseEntity.ok(accommodation);
    }

    @PutMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDto> updateAccommodation(
            @PathVariable Long accommodationId,
            @Valid @RequestBody UpdateAccommodationRequest request,
            @AuthenticationPrincipal User user) {
        log.info("PUT /api/accommodations/{} - 숙소 정보 수정 요청", accommodationId);
        AccommodationDto accommodation = accommodationService.updateAccommodation(accommodationId, request, user);
        return ResponseEntity.ok(accommodation);
    }

    @DeleteMapping("/{accommodationId}")
    public ResponseEntity<Void> deleteAccommodation(
            @PathVariable Long accommodationId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/accommodations/{} - 숙소 정보 삭제 요청", accommodationId);
        accommodationService.deleteAccommodation(accommodationId, user);
        return ResponseEntity.noContent().build();
    }
}

