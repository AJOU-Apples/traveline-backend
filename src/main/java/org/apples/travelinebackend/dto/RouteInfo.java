package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfo {
    
    private Integer distance;  // meters
    private Integer duration;  // seconds
    private String polyline;   // encoded polyline string
}


