package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccommodationRequest {

    @NotNull(message = "여행 계획 ID는 필수입니다")
    private Long travelPlanId;

    @NotBlank(message = "숙소명은 필수입니다")
    private String name;

    private String address;
    private Double latitude;
    private Double longitude;
    private String placeId;

    @NotNull(message = "체크인 날짜는 필수입니다")
    private LocalDate checkInDate;

    private LocalTime checkInTime;

    @NotNull(message = "체크아웃 날짜는 필수입니다")
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
}

