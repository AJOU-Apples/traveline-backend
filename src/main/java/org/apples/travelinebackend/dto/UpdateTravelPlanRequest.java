package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTravelPlanRequest {
    
    private String title;
    
    private String destination;
    
    private String startDate;
    
    private String endDate;
    
    @Min(value = 1, message = "참가자는 최소 1명 이상이어야 합니다")
    private Integer participants;
    
    private List<TravelDayDto> days;
}

