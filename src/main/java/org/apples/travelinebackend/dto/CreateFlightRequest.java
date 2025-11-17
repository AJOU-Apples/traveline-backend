package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateFlightRequest {

    @NotNull(message = "여행 계획 ID는 필수입니다")
    private Long travelPlanId;

    @NotBlank(message = "항공사는 필수입니다")
    private String airline;

    @NotBlank(message = "항공편명은 필수입니다")
    private String flightNumber;

    @NotBlank(message = "출발 공항은 필수입니다")
    private String departureAirport;

    private String departureAirportCode;

    @NotNull(message = "출발 시간은 필수입니다")
    private LocalDateTime departureTime;

    @NotBlank(message = "도착 공항은 필수입니다")
    private String arrivalAirport;

    private String arrivalAirportCode;

    @NotNull(message = "도착 시간은 필수입니다")
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

