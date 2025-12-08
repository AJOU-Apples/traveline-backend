package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelDayFullDto {
    private Long id;
    private Integer dayNumber;
    private String date;
    private String displayDate;
    
    @Builder.Default
    private List<PlaceFullDto> places = new ArrayList<>();
}

