package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchResponse {
    private String airline;
    private String flightNumber;
    private String departureAirport;
    private String departureAirportCode;
    private String arrivalAirport;
    private String arrivalAirportCode;
    private String departureTime;  // HH:MM 형식
    private String arrivalTime;    // HH:MM 형식
    private String scheduledDepartureDate;
}

