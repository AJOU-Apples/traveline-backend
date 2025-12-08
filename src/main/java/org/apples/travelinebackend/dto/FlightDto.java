package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FlightDto {
    private Long id;
    private Long travelPlanId;
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
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 좋아요 정보
    @JsonProperty("likes")
    @Builder.Default
    private Integer likeCount = 0;
    private Boolean isLiked;
}

