package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlightRequest {

    // 부분 업데이트 지원: 모든 필드 선택사항
    private String airline;
    private String flightNumber;
    private String departureAirport;
    private String departureAirportCode;
    private LocalDateTime departureTime;
    private String arrivalAirport;
    private String arrivalAirportCode;
    private LocalDateTime arrivalTime;
    private String confirmationNumber;
    private String seatNumber;
    private BigDecimal price;
    private String currency;
    private Boolean isConfirmed;
    private Boolean isSelected;
    private String cabinClass;
    private String passengerName;
    private String bookingUrl;
    private String memo;
}
