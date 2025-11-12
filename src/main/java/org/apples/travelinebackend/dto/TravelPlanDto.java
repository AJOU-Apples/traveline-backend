package org.apples.travelinebackend.dto;

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
public class TravelPlanDto {
    private Long id;
    private String title;
    private CityDto destination;
    private String startDate;
    private String endDate;
    private Integer participants;
    private Boolean isArchived;
    @Builder.Default
    private List<TravelDayDto> days = new ArrayList<>();
}

