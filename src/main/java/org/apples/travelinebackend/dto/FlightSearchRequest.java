package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {

    @NotBlank(message = "항공사 코드는 필수입니다")
    @Pattern(regexp = "^[A-Z0-9]{2,3}$", message = "유효한 항공사 코드를 입력해주세요 (예: KE, OZ)")
    private String carrierCode;

    @NotBlank(message = "항공편명은 필수입니다")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "유효한 항공편명을 입력해주세요 (예: 101, 705)")
    private String flightNumber;

    @NotBlank(message = "출발일은 필수입니다")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "출발일은 YYYY-MM-DD 형식이어야 합니다")
    private String scheduledDepartureDate;
}

