package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationDto {
    private Long id;
    private Long travelPlanId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String placeId;
    private LocalDate checkInDate;
    private LocalTime checkInTime;
    private LocalDate checkOutDate;
    private LocalTime checkOutTime;
    private String confirmationNumber;
    private BigDecimal price;
    private String currency;
    private Boolean isConfirmed;
    private Boolean isSelected;
    private String phoneNumber;
    private String email;
    private String bookingUrl;
    private String memo;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

