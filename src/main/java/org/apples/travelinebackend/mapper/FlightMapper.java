package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.FlightDto;
import org.apples.travelinebackend.entity.Flight;
import org.apples.travelinebackend.repository.FlightLikeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlightMapper {

    private final FlightLikeRepository flightLikeRepository;

    public FlightDto toDto(Flight flight) {
        if (flight == null) {
            return null;
        }

        // 좋아요 정보 먼저 조회
        long likeCount = flightLikeRepository.countByFlightId(flight.getId());
        
        FlightDto dto = FlightDto.builder()
                .id(flight.getId())
                .travelPlanId(flight.getTravelPlan().getId())
                .airline(flight.getAirline())
                .flightNumber(flight.getFlightNumber())
                .departureAirport(flight.getDepartureAirport())
                .departureAirportCode(flight.getDepartureAirportCode())
                .departureTime(flight.getDepartureTime())
                .arrivalAirport(flight.getArrivalAirport())
                .arrivalAirportCode(flight.getArrivalAirportCode())
                .arrivalTime(flight.getArrivalTime())
                .confirmationNumber(flight.getConfirmationNumber())
                .seatNumber(flight.getSeatNumber())
                .price(flight.getPrice())
                .currency(flight.getCurrency())
                .isConfirmed(flight.getIsConfirmed())
                .isSelected(flight.getIsSelected())
                .cabinClass(flight.getCabinClass())
                .passengerName(flight.getPassengerName())
                .bookingUrl(flight.getBookingUrl())
                .memo(flight.getMemo())
                .createdBy(flight.getCreatedBy())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .likeCount((int) likeCount)  // Builder에서 직접 설정
                .build();
        
        return dto;
    }

    /**
     * Flight를 DTO로 변환 (좋아요 정보 포함)
     */
    public FlightDto toDto(Flight flight, Long userId) {
        FlightDto dto = toDto(flight);
        if (dto != null && userId != null) {
            // userId가 있을 때만 isLiked 설정 (likeCount는 이미 toDto(flight)에서 설정됨)
            boolean isLiked = flightLikeRepository.existsByFlightIdAndUserId(flight.getId(), userId);
            dto.setIsLiked(isLiked);
        }
        return dto;
    }
}

