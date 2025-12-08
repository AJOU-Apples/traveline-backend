package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateFlightRequest;
import org.apples.travelinebackend.dto.FlightDto;
import org.apples.travelinebackend.dto.FlightSearchRequest;
import org.apples.travelinebackend.dto.FlightSearchResponse;
import org.apples.travelinebackend.dto.UpdateFlightRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.AmadeusService;
import org.apples.travelinebackend.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final AmadeusService amadeusService;

    @PostMapping
    public ResponseEntity<FlightDto> createFlight(
            @Valid @RequestBody CreateFlightRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/flights - 항공권 정보 생성 요청: travelPlanId={}", request.getTravelPlanId());
        FlightDto flight = flightService.createFlight(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(flight);
    }

    @GetMapping("/travel-plan/{travelPlanId}")
    public ResponseEntity<List<FlightDto>> getFlightsByTravelPlan(
            @PathVariable Long travelPlanId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/flights/travel-plan/{} - 여행 계획별 항공권 목록 조회", travelPlanId);
        List<FlightDto> flights = flightService.getFlightsByTravelPlan(travelPlanId, user);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<FlightDto> getFlightById(
            @PathVariable Long flightId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/flights/{} - 항공권 상세 조회", flightId);
        FlightDto flight = flightService.getFlightById(flightId, user);
        return ResponseEntity.ok(flight);
    }

    @PutMapping("/{flightId}")
    public ResponseEntity<FlightDto> updateFlight(
            @PathVariable Long flightId,
            @Valid @RequestBody UpdateFlightRequest request,
            @AuthenticationPrincipal User user) {
        log.info("PUT /api/flights/{} - 항공권 정보 수정 요청", flightId);
        FlightDto flight = flightService.updateFlight(flightId, request, user);
        return ResponseEntity.ok(flight);
    }

    @DeleteMapping("/{flightId}")
    public ResponseEntity<Void> deleteFlight(
            @PathVariable Long flightId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/flights/{} - 항공권 정보 삭제 요청", flightId);
        flightService.deleteFlight(flightId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Amadeus API를 통한 항공편 정보 검색
     */
    @PostMapping("/search")
    public Mono<ResponseEntity<FlightSearchResponse>> searchFlight(
            @Valid @RequestBody FlightSearchRequest request) {
        log.info("POST /api/flights/search - 항공편 정보 검색: {} {}, 날짜: {}", 
                request.getCarrierCode(), request.getFlightNumber(), request.getScheduledDepartureDate());
        
        return amadeusService.getFlightStatus(
                request.getCarrierCode().toUpperCase(),
                request.getFlightNumber().toUpperCase(),
                request.getScheduledDepartureDate()
        )
        .map(flightInfo -> {
            if (flightInfo != null) {
                return ResponseEntity.ok(flightInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body((FlightSearchResponse) null);
            }
        })
        .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

