package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateFlightRequest;
import org.apples.travelinebackend.dto.FlightDto;
import org.apples.travelinebackend.dto.UpdateFlightRequest;
import org.apples.travelinebackend.entity.Flight;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.FlightMapper;
import org.apples.travelinebackend.repository.FlightRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final FlightMapper flightMapper;
    private final WebSocketEventService webSocketEventService;

    @Transactional
    public FlightDto createFlight(CreateFlightRequest request, User user) {
        log.info("항공권 정보 생성 요청: travelPlanId={}, userId={}", request.getTravelPlanId(), user.getId());

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + request.getTravelPlanId()));

        if (!travelPlan.hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("항공권을 등록할 권한이 없습니다");
        }

        // 출발/도착 시간 검증
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new BadRequestException("도착 시간은 출발 시간보다 이후여야 합니다");
        }

        // 통화 자동 설정
        String currency = request.getCurrency();
        if (currency == null || currency.isEmpty()) {
            if (travelPlan.getDestination() != null && travelPlan.getDestination().getCurrency() != null) {
                currency = travelPlan.getDestination().getCurrency();
            } else {
                currency = "KRW";
            }
        }

        // Flight 생성
        Flight flight = Flight.builder()
                .travelPlan(travelPlan)
                .airline(request.getAirline())
                .flightNumber(request.getFlightNumber())
                .departureAirport(request.getDepartureAirport())
                .departureAirportCode(request.getDepartureAirportCode())
                .departureTime(request.getDepartureTime())
                .arrivalAirport(request.getArrivalAirport())
                .arrivalAirportCode(request.getArrivalAirportCode())
                .arrivalTime(request.getArrivalTime())
                .confirmationNumber(request.getConfirmationNumber())
                .seatNumber(request.getSeatNumber())
                .price(request.getPrice())
                .currency(currency)
                .isConfirmed(request.getIsConfirmed() != null ? request.getIsConfirmed() : false)
                .isSelected(request.getIsSelected() != null ? request.getIsSelected() : false)
                .cabinClass(request.getCabinClass())
                .passengerName(request.getPassengerName())
                .bookingUrl(request.getBookingUrl())
                .memo(request.getMemo())
                .createdBy(user.getId())
                .build();

        Flight saved = flightRepository.save(flight);
        log.info("항공권 정보 생성 완료: flightId={}", saved.getId());

        return flightMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<FlightDto> getFlightsByTravelPlan(Long travelPlanId, User user) {
        log.info("여행 계획별 항공권 목록 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("여행 계획에 접근할 권한이 없습니다");
        }

        List<Flight> flights = flightRepository.findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        return flights.stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FlightDto getFlightById(Long flightId, User user) {
        log.info("항공권 상세 조회: flightId={}", flightId);

        Flight flight = flightRepository.findByIdAndDeletedAtIsNull(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("항공권 정보를 찾을 수 없습니다: " + flightId));

        if (!flight.getTravelPlan().hasAccess(user.getId())) {
            throw new ForbiddenException("항공권 정보에 접근할 권한이 없습니다");
        }

        return flightMapper.toDto(flight);
    }

    @Transactional
    public FlightDto updateFlight(Long flightId, UpdateFlightRequest request, User user) {
        log.info("항공권 정보 수정 요청: flightId={}, userId={}", flightId, user.getId());

        Flight flight = flightRepository.findByIdAndDeletedAtIsNull(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("항공권 정보를 찾을 수 없습니다: " + flightId));

        if (!flight.getTravelPlan().hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("항공권 정보를 수정할 권한이 없습니다");
        }

        // 출발/도착 시간 검증 (둘 다 제공된 경우만)
        if (request.getDepartureTime() != null && request.getArrivalTime() != null) {
            if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
                throw new BadRequestException("도착 시간은 출발 시간보다 이후여야 합니다");
            }
        }

        // 부분 업데이트: null이 아닌 값만 업데이트
        if (request.getAirline() != null) flight.setAirline(request.getAirline());
        if (request.getFlightNumber() != null) flight.setFlightNumber(request.getFlightNumber());
        if (request.getDepartureAirport() != null) flight.setDepartureAirport(request.getDepartureAirport());
        if (request.getDepartureAirportCode() != null) flight.setDepartureAirportCode(request.getDepartureAirportCode());
        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalAirport() != null) flight.setArrivalAirport(request.getArrivalAirport());
        if (request.getArrivalAirportCode() != null) flight.setArrivalAirportCode(request.getArrivalAirportCode());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getConfirmationNumber() != null) flight.setConfirmationNumber(request.getConfirmationNumber());
        if (request.getSeatNumber() != null) flight.setSeatNumber(request.getSeatNumber());
        if (request.getPrice() != null) flight.setPrice(request.getPrice());
        if (request.getCurrency() != null) flight.setCurrency(request.getCurrency());
        if (request.getIsConfirmed() != null) flight.setIsConfirmed(request.getIsConfirmed());
        if (request.getIsSelected() != null) flight.setIsSelected(request.getIsSelected());
        if (request.getCabinClass() != null) flight.setCabinClass(request.getCabinClass());
        if (request.getPassengerName() != null) flight.setPassengerName(request.getPassengerName());
        if (request.getBookingUrl() != null) flight.setBookingUrl(request.getBookingUrl());
        if (request.getMemo() != null) flight.setMemo(request.getMemo());

        Flight updated = flightRepository.save(flight);
        log.info("항공권 정보 수정 완료: flightId={}", flightId);

        FlightDto flightDto = flightMapper.toDto(updated);
        
        // WebSocket 이벤트 브로드캐스트
        webSocketEventService.broadcastFlightUpdated(updated.getTravelPlan().getId(), flightDto);

        return flightDto;
    }

    @Transactional
    public void deleteFlight(Long flightId, User user) {
        log.info("항공권 정보 삭제 요청: flightId={}, userId={}", flightId, user.getId());

        Flight flight = flightRepository.findByIdAndDeletedAtIsNull(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("항공권 정보를 찾을 수 없습니다: " + flightId));

        if (!flight.getTravelPlan().hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("항공권 정보를 삭제할 권한이 없습니다");
        }

        flight.setDeletedAt(LocalDateTime.now());
        flightRepository.save(flight);

        log.info("항공권 정보 삭제 완료: flightId={}", flightId);
    }
}

