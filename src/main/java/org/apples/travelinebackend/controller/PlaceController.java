package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.GooglePlacesService;
import org.apples.travelinebackend.service.PlaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final GooglePlacesService googlePlacesService;

    /**
     * 장소 추가
     * POST /api/places
     */
    @PostMapping
    public ResponseEntity<PlaceDto> createPlace(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreatePlaceRequest request) {
        log.info("POST /api/places - userId={}, travelPlanId={}, dayNumber={}", 
                currentUser.getId(), request.getTravelPlanId(), request.getDayNumber());
        
        PlaceDto createdPlace = placeService.createPlace(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlace);
    }

    /**
     * 특정 날짜의 장소 목록 조회
     * GET /api/places?travelPlanId={planId}&dayNumber={dayNumber}
     */
    @GetMapping
    public ResponseEntity<Map<String, List<PlaceDto>>> getPlaces(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId,
            @RequestParam Integer dayNumber) {
        log.info("GET /api/places - userId={}, travelPlanId={}, dayNumber={}", 
                currentUser.getId(), travelPlanId, dayNumber);
        
        List<PlaceDto> places = placeService.getPlacesByDay(travelPlanId, dayNumber, currentUser.getId());
        
        Map<String, List<PlaceDto>> response = new HashMap<>();
        response.put("places", places);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 장소 상세 조회
     * GET /api/places/{placeId}
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDto> getPlace(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long placeId) {
        log.info("GET /api/places/{} - userId={}", placeId, currentUser.getId());
        
        PlaceDto place = placeService.getPlaceById(placeId, currentUser.getId());
        return ResponseEntity.ok(place);
    }

    /**
     * 장소 수정
     * PUT /api/places/{placeId}
     */
    @PutMapping("/{placeId}")
    public ResponseEntity<PlaceDto> updatePlace(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long placeId,
            @Valid @RequestBody UpdatePlaceRequest request) {
        log.info("PUT /api/places/{} - userId={}", placeId, currentUser.getId());
        
        PlaceDto updatedPlace = placeService.updatePlace(placeId, request, currentUser.getId());
        return ResponseEntity.ok(updatedPlace);
    }

    /**
     * 장소 삭제
     * DELETE /api/places/{placeId}
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long placeId) {
        log.info("DELETE /api/places/{} - userId={}", placeId, currentUser.getId());
        
        placeService.deletePlace(placeId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 장소 순서 변경 (Drag & Drop)
     * PATCH /api/places/reorder
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Map<String, List<PlaceDto>>> reorderPlaces(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReorderPlacesRequest request) {
        log.info("PATCH /api/places/reorder - userId={}, travelPlanId={}, dayNumber={}", 
                currentUser.getId(), request.getTravelPlanId(), request.getDayNumber());
        
        List<PlaceDto> reorderedPlaces = placeService.reorderPlaces(request, currentUser.getId());
        
        Map<String, List<PlaceDto>> response = new HashMap<>();
        response.put("places", reorderedPlaces);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 장소 메모 업데이트
     * PUT /api/places/{placeId}/memo
     */
    @PutMapping("/{placeId}/memo")
    public ResponseEntity<PlaceDto> updatePlaceMemo(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long placeId,
            @Valid @RequestBody UpdatePlaceMemoRequest request) {
        log.info("PUT /api/places/{}/memo - userId={}, type={}", 
                placeId, currentUser.getId(), request.getType());
        
        PlaceDto updatedPlace = placeService.updatePlaceMemo(placeId, request, currentUser.getId());
        return ResponseEntity.ok(updatedPlace);
    }

    /**
     * 장소 검색 (Google Places API 프록시)
     * GET /api/places/search?query={query}&latitude={lat}&longitude={lng}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, List<PlaceSearchResult>>> searchPlaces(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String query,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        log.info("GET /api/places/search - userId={}, query={}, lat={}, lng={}", 
                currentUser.getId(), query, latitude, longitude);
        
        List<PlaceSearchResult> results = googlePlacesService.searchPlaces(query, latitude, longitude);
        
        Map<String, List<PlaceSearchResult>> response = new HashMap<>();
        response.put("results", results);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 경로 정보 가져오기 (Google Directions API 프록시)
     * GET /api/places/route?origin={origin}&destination={destination}
     */
    @GetMapping("/route")
    public ResponseEntity<RouteInfo> getRoute(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String origin,
            @RequestParam String destination) {
        log.info("GET /api/places/route - userId={}, origin={}, destination={}", 
                currentUser.getId(), origin, destination);
        
        RouteInfo routeInfo = googlePlacesService.getRoute(origin, destination);
        
        if (routeInfo == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(routeInfo);
    }
}

