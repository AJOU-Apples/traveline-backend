package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaceRequest {
    
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String placeId;
    private String time;
    private String memo;
    private Boolean isVisited;
}


