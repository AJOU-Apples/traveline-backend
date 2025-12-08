package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.MemberRole;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelPlanFullDto {
    private Long id;
    private String title;
    private CityDto destination;
    private String startDate;
    private String endDate;
    private Integer participants;
    private Boolean isArchived;
    
    @Builder.Default
    private List<MemberDto> members = new ArrayList<>();
    
    // 현재 사용자의 역할
    private MemberRole myRole;
    
    @Builder.Default
    private List<TravelDayFullDto> days = new ArrayList<>();
    
    // Optional: flights, accommodations
    @Builder.Default
    private List<FlightDto> flights = new ArrayList<>();
    
    @Builder.Default
    private List<AccommodationDto> accommodations = new ArrayList<>();
}

