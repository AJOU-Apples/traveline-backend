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
public class TravelDayDto {
    private Long id;
    private Integer dayNumber;
    private String date;
    private String displayDate;
    @Builder.Default
    private List<PlaceDto> places = new ArrayList<>();
}

