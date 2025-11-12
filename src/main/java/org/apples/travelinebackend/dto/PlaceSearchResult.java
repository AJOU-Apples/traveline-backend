package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResult {
    
    private String name;
    
    @JsonProperty("formatted_address")
    private String address;
    
    @JsonProperty("place_id")
    private String placeId;
    
    private Geometry geometry;
    
    private List<String> types;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        private Location location;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @JsonProperty("lat")
        private Double latitude;
        
        @JsonProperty("lng")
        private Double longitude;
    }
    
    // Convenience methods
    public Double getLatitude() {
        if (geometry != null && geometry.getLocation() != null) {
            return geometry.getLocation().getLatitude();
        }
        return null;
    }
    
    public Double getLongitude() {
        if (geometry != null && geometry.getLocation() != null) {
            return geometry.getLocation().getLongitude();
        }
        return null;
    }
}


