package org.apples.travelinebackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTravelPlanRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotNull(message = "목적지는 필수입니다")
    @Valid
    private CityDto destination;

    @NotBlank(message = "시작일은 필수입니다")
    private String startDate;

    @NotBlank(message = "종료일은 필수입니다")
    private String endDate;

    @NotNull(message = "참가자 수는 필수입니다")
    @Min(value = 1, message = "참가자는 최소 1명 이상이어야 합니다")
    private Integer participants;

    @Builder.Default
    private List<TravelDayDto> days = new ArrayList<>();
}
