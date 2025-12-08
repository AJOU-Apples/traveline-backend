package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionsApiResponse {
    
    private List<Route> routes;
    private String status;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private List<Leg> legs;
        
        @JsonProperty("overview_polyline")
        private OverviewPolyline overviewPolyline;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leg {
        private Distance distance;
        private Duration duration;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distance {
        private Integer value;  // meters
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Duration {
        private Integer value;  // seconds
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewPolyline {
        private String points;
    }
    
    // Conversion method
    public RouteInfo toRouteInfo() {
        if (routes == null || routes.isEmpty()) {
            return null;
        }
        
        Route route = routes.get(0);
        if (route.getLegs() == null || route.getLegs().isEmpty()) {
            return null;
        }
        
        Leg leg = route.getLegs().get(0);
        
        return RouteInfo.builder()
                .distance(leg.getDistance() != null ? leg.getDistance().getValue() : null)
                .duration(leg.getDuration() != null ? leg.getDuration().getValue() : null)
                .polyline(route.getOverviewPolyline() != null ? route.getOverviewPolyline().getPoints() : null)
                .build();
    }
}


