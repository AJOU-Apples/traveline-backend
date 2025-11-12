package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.FlightDto;
import org.apples.travelinebackend.entity.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public FlightDto toDto(Flight flight) {
        if (flight == null) {
            return null;
        }

        return FlightDto.builder()
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
                .build();
    }
}

